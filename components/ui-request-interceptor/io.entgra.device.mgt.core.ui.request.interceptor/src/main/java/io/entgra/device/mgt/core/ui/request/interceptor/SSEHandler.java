/*
 * Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.ui.request.interceptor;

import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationManagementDAOException;
import io.entgra.device.mgt.core.notification.mgt.core.util.NotificationEventBroker;
import io.entgra.device.mgt.core.notification.mgt.core.util.NotificationListener;
import io.entgra.device.mgt.core.notification.mgt.core.dao.NotificationManagementDAO;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.NotificationManagementDAOFactory;
import io.entgra.device.mgt.core.ui.request.interceptor.beans.AuthData;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@WebServlet(urlPatterns = {"/ConnectSSE"}, asyncSupported = true)
public class SSEHandler extends HttpServlet implements NotificationListener {
    private static final Log log = LogFactory.getLog(SSEHandler.class);
    // map to store list of AsyncContexts per user
    private static final Map<String, List<AsyncContext>> userStreams = new ConcurrentHashMap<>();
    private final NotificationManagementDAO notificationDAO =
            NotificationManagementDAOFactory.getNotificationManagementDAO();

    /**
     * initializes the servlet and registers this instance as a notification listener.
     * this allows the server-side broker to push notification events to any active SSE streams.
     * @param config the servlet configuration provided by the container
     */
    @Override
    public void init(ServletConfig config) {
        // register as a notification listener
        NotificationEventBroker.registerListener(this);
    }

    /**
     * called by {@link NotificationEventBroker} when a message should be delivered to users.
     * for each username, write the event to all active SSE connections tracked in {@link #userStreams}.
     * @param message the message/payload to send
     * @param usernames list of usernames that should receive the message
     */
    @Override
    public void onMessage(String message, List<String> usernames) {
        for (String username : usernames) {
            List<AsyncContext> contexts = userStreams.get(username);
            if (contexts != null) {
                for (AsyncContext ac : new ArrayList<>(contexts)) {
                    try {
                        PrintWriter out = ac.getResponse().getWriter();
                        writeSseData(out, message);
                        if (out.checkError()) {
                            closeStream(username, ac);
                        }
                    } catch (IOException e) {
                        closeStream(username, ac);
                        log.debug("Error writing SSE message. Removing stream.", e);
                    }
                }
            }
        }
    }

    /**
     * opens an SSE stream for the authenticated user.
     * - requires a valid UI session
     * - binds the stream to the session username
     * - enforces same-origin when Origin header is present
     * @param req incoming servlet request
     * @param res outgoing servlet response
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) {
        req.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
        if (!isSameOriginRequest(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        HttpSession session = req.getSession(false);
        if (session == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        // authData can be stored under different keys depending on the login flow (normal/tenant-context/default).
        AuthData authData = (AuthData) session.getAttribute(HandlerConstants.SESSION_AUTH_DATA_KEY);
        if (authData == null) {
            authData = (AuthData) session.getAttribute(HandlerConstants.SESSION_TENANT_CONTEXT_AUTH_DATA_KEY);
        }
        if (authData == null) {
            authData = (AuthData) session.getAttribute(HandlerConstants.SESSION_DEFAULT_AUTH_DATA_KEY);
        }
        String username = null;
        if (authData != null && authData.getUsername() != null && !authData.getUsername().trim().isEmpty()) {
            username = authData.getUsername().trim();
        } else {
            Object userWithDomain = session.getAttribute(HandlerConstants.USERNAME_WITH_DOMAIN);
            if (userWithDomain instanceof String && !((String) userWithDomain).trim().isEmpty()) {
                username = ((String) userWithDomain).trim();
            }
        }
        if (authData == null || username == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        final String sessionUsername = username;
        final String notificationUsername = normalizeUsernameForNotifications(sessionUsername);
        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, private");
        res.setHeader("Pragma", "no-cache");
        res.setHeader("X-Content-Type-Options", "nosniff");
        res.setHeader("X-Accel-Buffering", "no");
        res.setContentType("text/event-stream");
        res.setCharacterEncoding("UTF-8");
        final AsyncContext ac = req.startAsync();
        ac.setTimeout(0);
        userStreams.computeIfAbsent(notificationUsername, k -> new CopyOnWriteArrayList<>()).add(ac);
        if (!notificationUsername.equals(sessionUsername)) {
            userStreams.computeIfAbsent(sessionUsername, k -> new CopyOnWriteArrayList<>()).add(ac);
        }
        ac.addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent event) {
                removeContext(ac);
            }
            @Override
            public void onTimeout(AsyncEvent event) {
                removeContext(ac);
            }
            @Override
            public void onError(AsyncEvent event) {
                removeContext(ac);
            }
            @Override
            public void onStartAsync(AsyncEvent event) {
            }
        });
        try {
            PrintWriter out = ac.getResponse().getWriter();
            try {
                NotificationManagementDAOFactory.openConnection();
                int count = notificationDAO.getUnreadNotificationCountForUser(notificationUsername);
                String initialPayload = String.format(
                        "{\"message\":\"Connected to notification service.\",\"unreadCount\":%d}", count);
                writeSseData(out, initialPayload);
            } catch (NotificationManagementDAOException e) {
                String msg = "Error fetching unread notification count for user: " + notificationUsername;
                log.error(msg, e);
                closeStream(notificationUsername, ac);
            } catch (SQLException e) {
                String msg = "Error retrieving unread notification count for user: " + notificationUsername;
                log.error(msg, e);
                closeStream(notificationUsername, ac);
            } finally {
                NotificationManagementDAOFactory.closeConnection();
            }
        } catch (IOException e) {
            closeStream(notificationUsername, ac);
            log.debug("Error writing initial SSE payload. Removing stream.", e);
        }
    }

    /**
     * removes a single {@link AsyncContext} from the in-memory stream registry for a user.
     * this ensures we do not leak contexts after disconnects/errors and keeps per-user state accurate.
     * @param username the authenticated username associated with this stream
     * @param ac the async context to remove
     */
    private void removeContext(String username, AsyncContext ac) {
        if (username == null) {
            return;
        }
        List<AsyncContext> contextList = userStreams.get(username);
        if (contextList != null) {
            contextList.remove(ac);
            if (contextList.isEmpty()) {
                userStreams.remove(username, contextList);
            }
        }
    }

    /**
     * Removes the given {@link AsyncContext} from all user stream lists.
     * This is required because a single SSE connection can be registered under multiple username keys
     * (e.g., "admin" and "admin@carbon.super") to match different publisher formats.
     *
     * @param ac async context to remove
     */
    private void removeContext(AsyncContext ac) {
        for (Map.Entry<String, List<AsyncContext>> entry : userStreams.entrySet()) {
            List<AsyncContext> list = entry.getValue();
            if (list != null) {
                list.remove(ac);
                if (list.isEmpty()) {
                    userStreams.remove(entry.getKey(), list);
                }
            }
        }
    }

    /**
     * closes an SSE connection safely and removes it from tracking.
     * prevents resource leaks (open sockets/threads) when the client disconnects or an error occurs.
     * @param username the authenticated username associated with this stream
     * @param ac the async context to complete
     */
    private void closeStream(String username, AsyncContext ac) {
        removeContext(username, ac);
        removeContext(ac);
        try {
            ac.complete();
        } catch (IllegalStateException ignore) {
        } catch (Exception e) {
            log.debug("Unexpected error while completing SSE async context.", e);
        }
    }

    /**
     * writes an SSE event using "data:" lines and flushes immediately.
     * @param out response writer
     * @param data payload to send (can be JSON)
     */
    private static void writeSseData(PrintWriter out, String data) {
        if (data == null) {
            data = "";
        }
        String normalized = data.replace("\r\n", "\n").replace("\r", "\n");
        String[] lines = normalized.split("\n", -1);
        for (String line : lines) {
            out.write("data: " + line + "\n");
        }
        out.write("\n");
        out.flush();
    }

    /**
     * same-origin check for browser-based SSE.
     * allows requests without an Origin header (non-browser clients).
     * @param req incoming request
     * @return true if origin is absent or matches the computed expected origin; false otherwise
     */
    private static boolean isSameOriginRequest(HttpServletRequest req) {
        String origin = req.getHeader("Origin");
        if (origin == null || origin.trim().isEmpty()) {
            return true;
        }
        origin = origin.trim();
        String expected = getExpectedOrigin(req);
        return expected.equalsIgnoreCase(origin);
    }

    /**
     * computes the expected origin for the current request.
     * uses {@code X-Forwarded-Proto} and {@code X-Forwarded-Host} when present so that deployments behind a reverse
     * proxy compare against the browser-visible origin.
     * @param req incoming request
     * @return expected origin string
     */
    private static String getExpectedOrigin(HttpServletRequest req) {
        String scheme = headerOrDefault(req, "X-Forwarded-Proto", req.getScheme());
        String host = headerOrDefault(req, "X-Forwarded-Host", req.getServerName());
        if (host.contains(":")) {
            return scheme + "://" + host;
        }
        int port = req.getServerPort();
        boolean isDefaultPort = ("http".equalsIgnoreCase(scheme) && port == 80) ||
                ("https".equalsIgnoreCase(scheme) && port == 443);
        return isDefaultPort ? (scheme + "://" + host) : (scheme + "://" + host + ":" + port);
    }

    /**
     * reads a header value with trimming and safe fallback.
     * if the header contains a comma-separated list (common with forwarded headers), this returns the first value.
     * @param req incoming request
     * @param header header name to read
     * @param defaultValue fallback value if header missing/blank
     * @return the resolved header value
     */
    private static String headerOrDefault(HttpServletRequest req, String header, String defaultValue) {
        String value = req.getHeader(header);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        String first = value.split(",", 2)[0].trim();
        return first.isEmpty() ? defaultValue : first;
    }

    /**
     * normalizes a session username into the username format used by the notification module/DAO.
     * @param sessionUsername username resolved from the UI session
     * @return username to use when querying/storing/streaming notifications
     */
    private static String normalizeUsernameForNotifications(String sessionUsername) {
        if (sessionUsername == null) {
            return null;
        }
        if (sessionUsername.endsWith("@carbon.super")) {
            return sessionUsername.substring(0, sessionUsername.length() - "@carbon.super".length());
        }
        return sessionUsername;
    }
}
