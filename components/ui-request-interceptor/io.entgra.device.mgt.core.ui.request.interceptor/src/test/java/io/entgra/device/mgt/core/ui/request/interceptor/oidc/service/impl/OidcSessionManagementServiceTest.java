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

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.bean.BackChannelLogoutResult;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.bean.OidcSessionRegistration;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.config.BackChannelLogoutConfiguration;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.dao.OidcSessionManagementDAOFactory;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.dao.impl.OidcSessionManagementDAOImpl;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.exception.OidcTokenValidationException;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.jdbcx.JdbcDataSource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class OidcSessionManagementServiceTest {

    private static final String ISSUER = "https://idp.example.test/oauth2/token";
    private static final String CLIENT_ID = "device-ui-client";
    private static final String APPLICATION = "device";
    private RSAKey signingKey;
    private OidcSessionManagementServiceImpl service;
    private JdbcDataSource dataSource;

    @BeforeClass
    public void init() throws Exception {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:oidc-session-test;DB_CLOSE_DELAY=-1");
        OidcSessionManagementDAOFactory.init(dataSource);
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE DM_OIDC_SESSION_STATE (ISSUER VARCHAR(255) NOT NULL, " +
                    "CLIENT_ID VARCHAR(255) NOT NULL, SID VARCHAR(255) NOT NULL, SUBJECT VARCHAR(255), " +
                    "APPLICATION_NAME VARCHAR(100) NOT NULL, SESSION_STATUS VARCHAR(16) NOT NULL, " +
                    "LOGOUT_JTI VARCHAR(255), CREATED_AT BIGINT NOT NULL, " +
                    "LAST_SEEN_AT BIGINT NOT NULL, REVOKED_AT BIGINT, EXPIRES_AT BIGINT NOT NULL, " +
                    "PRIMARY KEY (ISSUER, CLIENT_ID, SID))");
            statement.execute("CREATE TABLE DM_OIDC_SESSION_MAPPING (ISSUER VARCHAR(255) NOT NULL, " +
                    "CLIENT_ID VARCHAR(255) NOT NULL, SID VARCHAR(255) NOT NULL, APPLICATION_NAME VARCHAR(100) NOT NULL, " +
                    "HTTP_SESSION_ID VARCHAR(128) NOT NULL, CREATED_AT BIGINT NOT NULL, LAST_SEEN_AT BIGINT NOT NULL, " +
                    "SESSION_TIMEOUT_MS BIGINT NOT NULL, EXPIRES_AT BIGINT NOT NULL, " +
                    "PRIMARY KEY (APPLICATION_NAME, HTTP_SESSION_ID), FOREIGN KEY (ISSUER, CLIENT_ID, SID) " +
                    "REFERENCES DM_OIDC_SESSION_STATE (ISSUER, CLIENT_ID, SID) ON DELETE CASCADE)");
            statement.execute("CREATE TABLE DM_OIDC_SESSION_CLEANUP (TASK_NAME VARCHAR(64) PRIMARY KEY, " +
                    "LAST_RUN_AT BIGINT NOT NULL)");
            statement.execute("INSERT INTO DM_OIDC_SESSION_CLEANUP VALUES ('OIDC_SESSION_CLEANUP', 0)");
        }
        signingKey = new RSAKeyGenerator(2048).keyID("test-key").generate();
        BackChannelLogoutConfiguration configuration = new BackChannelLogoutConfiguration();
        configuration.setEnabled(true);
        configuration.setIssuer(ISSUER);
        configuration.setJwksUri("https://unused.example.test/jwks");
        configuration.setAllowedClockSkewSeconds(30);
        configuration.setStaleMappingRetentionSeconds(3600);
        JWKSet jwkSet = new JWKSet(signingKey.toPublicJWK());
        service = new OidcSessionManagementServiceImpl(new OidcSessionManagementDAOImpl(), configuration,
                (selector, context) -> selector.select(jwkSet));
    }

    @Test
    public void testRegistrationRevocationAndRetransmission() throws Exception {
        String sid = "sid-valid";
        String sessionId = "http-session-valid";
        service.registerSession(new OidcSessionRegistration(idToken(sid), CLIENT_ID, APPLICATION,
                sessionId, 60000));
        Assert.assertFalse(service.isSessionRevoked(APPLICATION, sessionId));

        String logoutToken = logoutToken(sid, "logout-jti", signingKey, true, true, false);
        BackChannelLogoutResult result = service.processLogoutToken(logoutToken, CLIENT_ID, APPLICATION);
        Assert.assertTrue(result.isMappingFound());
        Assert.assertEquals(result.getHttpSessionIds(), Collections.singletonList(sessionId));
        Assert.assertTrue(service.isSessionRevoked(APPLICATION, sessionId));

        BackChannelLogoutResult retransmission = service.processLogoutToken(logoutToken, CLIENT_ID, APPLICATION);
        Assert.assertTrue(retransmission.isMappingFound());
        Assert.assertEquals(retransmission.getHttpSessionIds(), Collections.singletonList(sessionId));
        service.removeSession(APPLICATION, sessionId);
        Assert.assertTrue(service.isSessionRevoked(APPLICATION, sessionId),
                "Ordinary cleanup must retain a revoked row for idempotent delivery");
    }

    @Test
    public void testOneSidCanMapToMultipleServletSessions() throws Exception {
        service.registerSession(new OidcSessionRegistration(idToken("sid-upsert"), CLIENT_ID, APPLICATION,
                "old-session", 60000));
        service.registerSession(new OidcSessionRegistration(idToken("sid-upsert"), CLIENT_ID, APPLICATION,
                "new-session", 60000));
        Assert.assertFalse(service.isSessionRevoked(APPLICATION, "old-session"));
        Assert.assertFalse(service.isSessionRevoked(APPLICATION, "new-session"));

        BackChannelLogoutResult result = service.processLogoutToken(
                logoutToken("sid-upsert", "multi-session-logout", signingKey, true, true, false),
                CLIENT_ID, APPLICATION);
        Assert.assertEqualsNoOrder(result.getHttpSessionIds().toArray(),
                new String[] {"old-session", "new-session"});
        Assert.assertTrue(service.isSessionRevoked(APPLICATION, "old-session"));
        Assert.assertTrue(service.isSessionRevoked(APPLICATION, "new-session"));
    }

    @Test(expectedExceptions = OidcTokenValidationException.class)
    public void testRevokedSidCannotBeRegisteredAgain() throws Exception {
        String sid = "sid-cannot-reactivate";
        service.registerSession(new OidcSessionRegistration(idToken(sid), CLIENT_ID, APPLICATION,
                "revoked-session", 60000));
        service.processLogoutToken(logoutToken(sid, "revoke-before-register", signingKey, true, true, false),
                CLIENT_ID, APPLICATION);
        service.registerSession(new OidcSessionRegistration(idToken(sid), CLIENT_ID, APPLICATION,
                "late-session", 60000));
    }

    @Test(expectedExceptions = OidcTokenValidationException.class)
    public void testUnknownLogoutCreatesTombstoneForDelayedCallback() throws Exception {
        String sid = "sid-logout-before-callback";
        BackChannelLogoutResult result = service.processLogoutToken(
                logoutToken(sid, "early-logout", signingKey, true, true, false), CLIENT_ID, APPLICATION);
        Assert.assertFalse(result.isMappingFound());
        service.registerSession(new OidcSessionRegistration(idToken(sid), CLIENT_ID, APPLICATION,
                "delayed-session", 60000));
    }

    @Test
    public void testConcurrentRegistrationsPreserveBothSessions() throws Exception {
        String sid = "sid-concurrent-registration";
        String token = idToken(sid);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<?> first = executor.submit(() -> registerAfterBarrier(token, "concurrent-session-1", ready, start));
            Future<?> second = executor.submit(() -> registerAfterBarrier(token, "concurrent-session-2", ready, start));
            Assert.assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            first.get(5, TimeUnit.SECONDS);
            second.get(5, TimeUnit.SECONDS);

            BackChannelLogoutResult result = service.processLogoutToken(
                    logoutToken(sid, "concurrent-registration-logout", signingKey, true, true, false),
                    CLIENT_ID, APPLICATION);
            Assert.assertEqualsNoOrder(result.getHttpSessionIds().toArray(),
                    new String[] {"concurrent-session-1", "concurrent-session-2"});
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void testConcurrentLogoutAlwaysWinsOverRegistration() throws Exception {
        String sid = "sid-concurrent-logout";
        String token = idToken(sid);
        service.registerSession(new OidcSessionRegistration(token, CLIENT_ID, APPLICATION,
                "existing-concurrent-session", 60000));
        String logoutToken = logoutToken(sid, "concurrent-logout", signingKey, true, true, false);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<?> registration = executor.submit(() ->
                    registerAfterBarrier(token, "racing-concurrent-session", ready, start));
            Future<?> logout = executor.submit(() -> {
                awaitBarrier(ready, start);
                try {
                    service.processLogoutToken(logoutToken, CLIENT_ID, APPLICATION);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            Assert.assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            try {
                registration.get(5, TimeUnit.SECONDS);
            } catch (java.util.concurrent.ExecutionException e) {
                Assert.assertTrue(e.getCause() instanceof RuntimeException &&
                                e.getCause().getCause() instanceof OidcTokenValidationException,
                        "Only a logout-won validation failure is expected from the racing registration");
            }
            logout.get(5, TimeUnit.SECONDS);
            Assert.assertTrue(service.isSessionRevoked(APPLICATION, "existing-concurrent-session"));
            Assert.assertTrue(service.isSessionRevoked(APPLICATION, "racing-concurrent-session"));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void testMissingMappingFailsClosed() throws Exception {
        Assert.assertTrue(service.isSessionRevoked(APPLICATION, "missing-session"));
    }

    @Test
    public void testMultiAudienceLogoutToken() throws Exception {
        String sid = "sid-multi-audience";
        String sessionId = "http-session-multi-audience";
        service.registerSession(new OidcSessionRegistration(idToken(sid), CLIENT_ID, APPLICATION,
                sessionId, 60000));

        long now = System.currentTimeMillis();
        JWTClaimsSet claims = new JWTClaimsSet.Builder().issuer(ISSUER)
                .audience(Arrays.asList("another-client", CLIENT_ID)).jwtID("multi-audience-jti")
                .claim("sid", sid).issueTime(new Date(now)).expirationTime(new Date(now + 60000))
                .claim("events", Collections.singletonMap(
                        "http://schemas.openid.net/event/backchannel-logout", Collections.emptyMap()))
                .build();

        BackChannelLogoutResult result = service.processLogoutToken(sign(claims, signingKey), null, APPLICATION);
        Assert.assertTrue(result.isMappingFound());
        Assert.assertEquals(result.getHttpSessionIds(), Collections.singletonList(sessionId));
        Assert.assertTrue(service.isSessionRevoked(APPLICATION, sessionId));
    }

    @Test(expectedExceptions = OidcTokenValidationException.class)
    public void testBadSignatureRejected() throws Exception {
        RSAKey otherKey = new RSAKeyGenerator(2048).keyID("test-key").generate();
        service.processLogoutToken(logoutToken("unknown", "bad-signature", otherKey, true, true, false),
                CLIENT_ID, APPLICATION);
    }

    @Test(expectedExceptions = OidcTokenValidationException.class)
    public void testMissingLogoutEventRejected() throws Exception {
        service.processLogoutToken(logoutToken("unknown", "missing-event", signingKey, false, true, false),
                CLIENT_ID, APPLICATION);
    }

    @Test(expectedExceptions = OidcTokenValidationException.class)
    public void testInvalidLogoutEventValueRejected() throws Exception {
        long now = System.currentTimeMillis();
        JWTClaimsSet claims = new JWTClaimsSet.Builder().issuer(ISSUER).audience(CLIENT_ID)
                .jwtID("invalid-event-value").claim("sid", "unknown").issueTime(new Date(now))
                .expirationTime(new Date(now + 60000)).claim("events", Collections.singletonMap(
                        "http://schemas.openid.net/event/backchannel-logout", "invalid"))
                .build();
        service.processLogoutToken(sign(claims, signingKey), CLIENT_ID, APPLICATION);
    }

    @Test(expectedExceptions = OidcTokenValidationException.class)
    public void testNonceRejected() throws Exception {
        service.processLogoutToken(logoutToken("unknown", "nonce", signingKey, true, true, true),
                CLIENT_ID, APPLICATION);
    }

    @Test(expectedExceptions = OidcTokenValidationException.class)
    public void testExpiredLogoutTokenRejected() throws Exception {
        service.processLogoutToken(logoutToken("unknown", "expired", signingKey, true, false, false),
                CLIENT_ID, APPLICATION);
    }

    @Test(priority = 100)
    public void testPooledConnectionStateIsRestored() throws Exception {
        JdbcDataSource pooledDataSource = new JdbcDataSource();
        pooledDataSource.setURL("jdbc:h2:mem:oidc-pool-state-test;DB_CLOSE_DELAY=-1");
        JdbcConnectionPool connectionPool = JdbcConnectionPool.create(pooledDataSource);
        try {
            OidcSessionManagementDAOFactory.init(connectionPool);
            OidcSessionManagementDAOFactory.beginTransaction();
            Assert.assertFalse(OidcSessionManagementDAOFactory.getConnection().getAutoCommit());
            OidcSessionManagementDAOFactory.commitTransaction();
            OidcSessionManagementDAOFactory.closeConnection();

            try (Connection returnedConnection = connectionPool.getConnection()) {
                Assert.assertTrue(returnedConnection.getAutoCommit(),
                        "A pooled connection must be returned in auto-commit mode");
            }
        } finally {
            connectionPool.dispose();
            OidcSessionManagementDAOFactory.init(dataSource);
        }
    }

    private String idToken(String sid) throws Exception {
        long now = System.currentTimeMillis();
        JWTClaimsSet claims = new JWTClaimsSet.Builder().issuer(ISSUER).audience(CLIENT_ID)
                .subject("alice").claim("sid", sid).issueTime(new Date(now))
                .expirationTime(new Date(now + 60000)).build();
        return sign(claims, signingKey);
    }

    private void registerAfterBarrier(String token, String sessionId, CountDownLatch ready, CountDownLatch start) {
        awaitBarrier(ready, start);
        try {
            service.registerSession(new OidcSessionRegistration(token, CLIENT_ID, APPLICATION, sessionId, 60000));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void awaitBarrier(CountDownLatch ready, CountDownLatch start) {
        ready.countDown();
        try {
            if (!start.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for concurrent OIDC test start");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for concurrent OIDC test start", e);
        }
    }

    private String logoutToken(String sid, String jti, RSAKey key, boolean event, boolean validExpiry,
                               boolean nonce) throws Exception {
        long now = System.currentTimeMillis();
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder().issuer(ISSUER).audience(CLIENT_ID)
                .jwtID(jti).claim("sid", sid).issueTime(new Date(now))
                .expirationTime(new Date(validExpiry ? now + 60000 : now - 60000));
        if (event) {
            builder.claim("events", Collections.singletonMap(
                    "http://schemas.openid.net/event/backchannel-logout", Collections.emptyMap()));
        }
        if (nonce) {
            builder.claim("nonce", "not-allowed");
        }
        return sign(builder.build(), key);
    }

    private String sign(JWTClaimsSet claims, RSAKey key) throws Exception {
        SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(),
                claims);
        jwt.sign(new RSASSASigner(key));
        return jwt.serialize();
    }
}
