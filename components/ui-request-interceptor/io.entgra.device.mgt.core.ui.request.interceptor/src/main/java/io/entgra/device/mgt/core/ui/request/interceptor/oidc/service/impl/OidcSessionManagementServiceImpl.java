/*
 * Copyright (c) 2018 - 2026, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.device.mgt.core.ui.request.interceptor.oidc.service.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.bean.BackChannelLogoutResult;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.bean.OidcSessionMapping;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.bean.OidcSessionRegistration;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.config.BackChannelLogoutConfiguration;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.config.OidcConfigurationManager;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.dao.OidcSessionManagementDAO;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.dao.OidcSessionManagementDAOFactory;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.dao.impl.OidcSessionManagementDAOImpl;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.exception.OidcSessionManagementException;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.exception.OidcTokenValidationException;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.service.OidcSessionManagementService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Validates OIDC ID/logout tokens and maintains their servlet-session mappings in {@code SESSION_DS}.
 *
 * <p>Tokens and signing material are never persisted or written to logs. Revoked rows are retained for a configured
 * period to make repeated identity-provider logout delivery idempotent.</p>
 */
public class OidcSessionManagementServiceImpl implements OidcSessionManagementService {

    private static final Log log = LogFactory.getLog(OidcSessionManagementServiceImpl.class);
    private static final String ACTIVE = "ACTIVE";
    private static final String REVOKED = "REVOKED";
    private static final String LOGOUT_EVENT = "http://schemas.openid.net/event/backchannel-logout";
    private static final String CLEANUP_TASK = "OIDC_SESSION_CLEANUP";
    private static final long CLEANUP_INTERVAL_MILLIS = 60 * 60 * 1000L;
    private static final long MAX_SESSION_TOUCH_INTERVAL_MILLIS = 60 * 1000L;
    private final OidcSessionManagementDAO dao;
    private final BackChannelLogoutConfiguration configuration;
    private final DefaultJWTProcessor<SecurityContext> jwtProcessor;

    public OidcSessionManagementServiceImpl() throws OidcSessionManagementException {
        this(new OidcSessionManagementDAOImpl(), configuration());
    }

    OidcSessionManagementServiceImpl(OidcSessionManagementDAO dao,
                                     BackChannelLogoutConfiguration configuration)
            throws OidcSessionManagementException {
        this(dao, configuration, jwkSource(configuration));
    }

    OidcSessionManagementServiceImpl(OidcSessionManagementDAO dao,
                                     BackChannelLogoutConfiguration configuration,
                                     JWKSource<SecurityContext> jwkSource)
            throws OidcSessionManagementException {
        this.dao = dao;
        this.configuration = configuration;
        if (isEnabled()) {
            if (isBlank(configuration.getIssuer()) || jwkSource == null) {
                throw new OidcSessionManagementException(
                        "OIDC back-channel logout issuer and JWKS URI must be configured");
            }
            jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource));
            log.info("OIDC back-channel logout session management is enabled");
        } else {
            jwtProcessor = null;
            if (log.isDebugEnabled()) {
                log.debug("OIDC back-channel logout session management is disabled");
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return configuration != null && configuration.isEnabled();
    }

    @Override
    public void registerSession(OidcSessionRegistration registration)
            throws OidcSessionManagementException, OidcTokenValidationException {
        if (!isEnabled()) { return; }
        JWTClaimsSet claims = validateToken(registration.getIdToken(), registration.getClientId(), false);
        String sid = stringClaim(claims, "sid");
        if (isBlank(sid)) { throw new OidcTokenValidationException("ID token does not contain a sid claim"); }

        long now = System.currentTimeMillis();
        OidcSessionMapping mapping = new OidcSessionMapping();
        mapping.setIssuer(claims.getIssuer());
        mapping.setClientId(registration.getClientId());
        mapping.setSid(sid);
        mapping.setSubject(claims.getSubject());
        mapping.setApplicationName(registration.getApplicationName());
        mapping.setHttpSessionId(registration.getHttpSessionId());
        mapping.setStatus(ACTIVE);
        mapping.setCreatedAt(now);
        mapping.setLastSeenAt(now);
        long sessionTimeoutMillis = registration.getSessionTimeoutMillis() > 0 ?
                registration.getSessionTimeoutMillis() : retentionMillis();
        mapping.setSessionTimeoutMillis(sessionTimeoutMillis);
        mapping.setExpiresAt(now + sessionTimeoutMillis);

        try {
            persistRegistration(mapping);
            if (log.isDebugEnabled()) {
                log.debug("Registered OIDC session mapping for application: " +
                        registration.getApplicationName());
            }
        } catch (OidcTokenValidationException e) {
            safeRollback();
            throw e;
        } catch (Exception e) {
            safeRollback();
            safeClose();
            // A second node may have created the SID state while this node was trying to insert it. Retry the full
            // operation in a fresh transaction so the winner's state is locked and checked before adding a mapping.
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Retrying concurrent OIDC session registration for application: " +
                            registration.getApplicationName());
                }
                persistRegistration(mapping);
                if (log.isDebugEnabled()) {
                    log.debug("Registered OIDC session mapping after a concurrent update for application: " +
                            registration.getApplicationName());
                }
            } catch (OidcTokenValidationException validationException) {
                safeRollback();
                throw validationException;
            } catch (Exception retryFailure) {
                safeRollback();
                throw new OidcSessionManagementException("Unable to persist OIDC session mapping", retryFailure);
            }
        } finally {
            safeClose();
        }
        cleanupIfDue(now);
    }

    @Override
    public BackChannelLogoutResult processLogoutToken(String logoutToken, String expectedClientId,
                                                       String applicationName)
            throws OidcSessionManagementException, OidcTokenValidationException {
        if (!isEnabled()) { throw new OidcTokenValidationException("OIDC back-channel logout is disabled"); }
        JWTClaimsSet claims = validateToken(logoutToken, expectedClientId, true);
        String sid = stringClaim(claims, "sid");
        String jti = claims.getJWTID();
        List<String> candidateClientIds = isBlank(expectedClientId) ? claims.getAudience() :
                Collections.singletonList(expectedClientId);
        long now = System.currentTimeMillis();
        BackChannelLogoutResult result;
        try {
            result = processLogout(claims.getIssuer(), candidateClientIds, sid, applicationName, jti, now);
        } catch (Exception e) {
            safeRollback();
            safeClose();
            try {
                result = processLogout(claims.getIssuer(), candidateClientIds, sid, applicationName, jti, now);
            } catch (Exception retryFailure) {
                safeRollback();
                throw new OidcSessionManagementException("Unable to revoke OIDC session mapping", retryFailure);
            }
        } finally {
            safeClose();
        }
        cleanupIfDue(now);
        return result;
    }

    @Override
    public boolean isSessionRevoked(String applicationName, String httpSessionId)
            throws OidcSessionManagementException {
        if (!isEnabled()) { return false; }
        long now = System.currentTimeMillis();
        try {
            OidcSessionManagementDAOFactory.openConnection();
            return dao.isRevoked(applicationName, httpSessionId, now,
                    now - MAX_SESSION_TOUCH_INTERVAL_MILLIS);
        } catch (Exception e) {
            throw new OidcSessionManagementException("Unable to check OIDC session state", e);
        } finally {
            safeClose();
        }
    }

    @Override
    public void removeSession(String applicationName, String httpSessionId)
            throws OidcSessionManagementException {
        if (!isEnabled()) { return; }
        try {
            OidcSessionManagementDAOFactory.openConnection();
            dao.deleteActiveBySession(applicationName, httpSessionId);
            if (log.isDebugEnabled()) {
                log.debug("Removed active OIDC session mapping for application: " + applicationName);
            }
        } catch (Exception e) {
            throw new OidcSessionManagementException("Unable to remove OIDC session mapping", e);
        } finally {
            safeClose();
        }
    }

    private void persistRegistration(OidcSessionMapping mapping) throws Exception {
        OidcSessionManagementDAOFactory.beginTransaction();
        OidcSessionMapping state = dao.getSessionStateForUpdate(mapping.getIssuer(), mapping.getClientId(),
                mapping.getSid(), mapping.getApplicationName());
        if (state == null) {
            dao.addSessionState(mapping);
        } else {
            if (REVOKED.equals(state.getStatus())) {
                throw new OidcTokenValidationException(
                        "The OIDC provider session was already terminated by a back-channel logout");
            }
            if (dao.updateActiveSessionState(mapping) == 0) {
                throw new OidcTokenValidationException(
                        "The OIDC provider session was terminated while the login was being completed");
            }
        }
        if (dao.updateSessionMapping(mapping) == 0) {
            dao.addSessionMapping(mapping);
        }
        OidcSessionManagementDAOFactory.commitTransaction();
    }

    private BackChannelLogoutResult processLogout(String issuer, List<String> candidateClientIds, String sid,
                                                   String applicationName, String jti, long now) throws Exception {
        OidcSessionManagementDAOFactory.beginTransaction();
        OidcSessionMapping selectedState = null;
        List<String> selectedSessionIds = Collections.emptyList();
        for (String clientId : candidateClientIds) {
            OidcSessionMapping state = dao.getSessionStateForUpdate(issuer, clientId, sid, applicationName);
            if (state == null) {
                continue;
            }
            List<String> sessionIds = dao.getHttpSessionIds(issuer, clientId, sid, applicationName);
            if (selectedState == null || ACTIVE.equals(state.getStatus()) || !sessionIds.isEmpty()) {
                selectedState = state;
                selectedSessionIds = sessionIds;
            }
            if (!sessionIds.isEmpty()) {
                break;
            }
        }

        if (selectedState == null) {
            // Retain a SID-level tombstone even when the callback has not registered its servlet session yet. A
            // concurrent or delayed callback will then observe the revoked state instead of recreating the session.
            for (String clientId : candidateClientIds) {
                OidcSessionMapping tombstone = new OidcSessionMapping();
                tombstone.setIssuer(issuer);
                tombstone.setClientId(clientId);
                tombstone.setSid(sid);
                tombstone.setApplicationName(applicationName);
                tombstone.setStatus(REVOKED);
                tombstone.setLogoutJti(jti);
                tombstone.setCreatedAt(now);
                tombstone.setLastSeenAt(now);
                tombstone.setRevokedAt(now);
                tombstone.setExpiresAt(now + retentionMillis());
                dao.addSessionState(tombstone);
            }
            OidcSessionManagementDAOFactory.commitTransaction();
            return new BackChannelLogoutResult(Collections.emptyList(), false);
        }

        boolean alreadyRevoked = REVOKED.equals(selectedState.getStatus());
        if (!alreadyRevoked) {
            dao.revoke(issuer, selectedState.getClientId(), sid, applicationName, jti, now,
                    now + retentionMillis());
        }
        OidcSessionManagementDAOFactory.commitTransaction();
        if (log.isDebugEnabled()) {
            log.debug("Processed OIDC logout token for application: " + applicationName +
                    ", mapped sessions: " + selectedSessionIds.size() + ", already revoked: " + alreadyRevoked);
        }
        return new BackChannelLogoutResult(selectedSessionIds, !selectedSessionIds.isEmpty());
    }

    private void cleanupIfDue(long now) {
        boolean claimed = false;
        try {
            OidcSessionManagementDAOFactory.openConnection();
            claimed = dao.claimCleanup(CLEANUP_TASK, now, now - CLEANUP_INTERVAL_MILLIS);
        } catch (Exception e) {
            log.warn("Unable to claim stale OIDC session cleanup; session processing will continue", e);
        } finally {
            safeClose();
        }
        if (!claimed) {
            return;
        }
        try {
            OidcSessionManagementDAOFactory.beginTransaction();
            dao.deleteExpired(now);
            OidcSessionManagementDAOFactory.commitTransaction();
            if (log.isDebugEnabled()) {
                log.debug("Removed expired OIDC session state after claiming the cluster-wide cleanup lease");
            }
        } catch (Exception e) {
            safeRollback();
            log.warn("Unable to remove stale OIDC session state; a later cleanup will retry", e);
        } finally {
            safeClose();
        }
    }

    private JWTClaimsSet validateToken(String token, String clientId, boolean logout)
            throws OidcTokenValidationException {
        if (isBlank(token)) { throw new OidcTokenValidationException("OIDC token is missing"); }
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!JWSAlgorithm.RS256.equals(signedJWT.getHeader().getAlgorithm())) {
                throw new OidcTokenValidationException("Unsupported OIDC token signing algorithm");
            }
            JWTClaimsSet claims = jwtProcessor.process(signedJWT, null);
            long now = System.currentTimeMillis();
            long skew = configuration.getAllowedClockSkewSeconds() * 1000L;
            if (!configuration.getIssuer().equals(claims.getIssuer())) {
                throw new OidcTokenValidationException("OIDC token issuer is invalid");
            }
            List<String> audience = claims.getAudience();
            if (audience == null || audience.isEmpty() ||
                    (!isBlank(clientId) && !audience.contains(clientId))) {
                throw new OidcTokenValidationException("OIDC token audience is invalid");
            }
            if (!isBlank(clientId) && audience.size() > 1 && !clientId.equals(stringClaim(claims, "azp"))) {
                throw new OidcTokenValidationException("OIDC token authorized party is invalid");
            }
            Date expiration = claims.getExpirationTime();
            if (expiration == null || expiration.getTime() + skew < now) {
                throw new OidcTokenValidationException("OIDC token is expired or has no expiration");
            }
            Date issuedAt = claims.getIssueTime();
            Date notBefore = claims.getNotBeforeTime();
            if (issuedAt == null || issuedAt.getTime() - skew > now ||
                    (notBefore != null && notBefore.getTime() - skew > now)) {
                throw new OidcTokenValidationException("OIDC token has an invalid issue or not-before time");
            }
            if (logout) {
                validateLogoutClaims(claims);
            } else if (isBlank(claims.getSubject())) {
                throw new OidcTokenValidationException("ID token does not contain a subject");
            }
            return claims;
        } catch (ParseException | BadJOSEException | JOSEException e) {
            throw new OidcTokenValidationException("OIDC token validation failed", e);
        }
    }

    private void validateLogoutClaims(JWTClaimsSet claims) throws OidcTokenValidationException {
        if (isBlank(claims.getJWTID()) || isBlank(stringClaim(claims, "sid"))) {
            throw new OidcTokenValidationException("Logout token is missing a required claim");
        }
        if (claims.getClaim("nonce") != null) {
            throw new OidcTokenValidationException("Logout token must not contain nonce");
        }
        Object eventsClaim = claims.getClaim("events");
        if (!(eventsClaim instanceof Map)) {
            throw new OidcTokenValidationException("Logout token has no back-channel logout event");
        }
        Object logoutEvent = ((Map<?, ?>) eventsClaim).get(LOGOUT_EVENT);
        if (!(logoutEvent instanceof Map) || !((Map<?, ?>) logoutEvent).isEmpty()) {
            throw new OidcTokenValidationException("Logout token has an invalid back-channel logout event");
        }
    }

    private static String stringClaim(JWTClaimsSet claims, String name) throws OidcTokenValidationException {
        try { return claims.getStringClaim(name); }
        catch (ParseException e) { throw new OidcTokenValidationException("Invalid " + name + " claim", e); }
    }

    private long retentionMillis() { return configuration.getStaleMappingRetentionSeconds() * 1000L; }
    private static boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }
    private static BackChannelLogoutConfiguration configuration() throws OidcSessionManagementException {
        return OidcConfigurationManager.getConfiguration();
    }
    private static JWKSource<SecurityContext> jwkSource(BackChannelLogoutConfiguration configuration)
            throws OidcSessionManagementException {
        if (configuration == null || !configuration.isEnabled()) {
            return null;
        }
        if (isBlank(configuration.getJwksUri())) {
            throw new OidcSessionManagementException("OIDC back-channel logout JWKS URI must be configured");
        }
        try {
            return new RemoteJWKSet<>(new URL(configuration.getJwksUri()));
        } catch (MalformedURLException e) {
            throw new OidcSessionManagementException("Invalid OIDC JWKS URI", e);
        }
    }
    private static void safeRollback() {
        try {
            OidcSessionManagementDAOFactory.rollbackTransaction();
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to roll back OIDC session transaction; it may not have been opened", e);
            }
        }
    }

    private static void safeClose() {
        try {
            OidcSessionManagementDAOFactory.closeConnection();
        } catch (Exception e) {
            log.warn("Unable to close OIDC session database connection", e);
        }
    }
}
