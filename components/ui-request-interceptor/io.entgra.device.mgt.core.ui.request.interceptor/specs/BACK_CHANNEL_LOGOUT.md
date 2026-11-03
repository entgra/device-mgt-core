# OIDC Back-Channel Logout Specification

Copyright (c) 2018 - 2026, Entgra (Pvt) Ltd. All Rights Reserved.

## 1. Purpose

This document explains how OpenID Connect (OIDC) back-channel logout works in the UI request interceptor.
It is intended for developers, reviewers, deployers, and support engineers. Protocol terms are explained where they
are first used, and the main flows are described in the order in which they happen.

Back-channel logout lets the Identity Provider notify an application that an authenticated OIDC session has ended.
The notification is sent directly from the Identity Provider to the server. It does not depend on the user's browser
loading a logout page.

The implementation has two goals:

- invalidate a matching session immediately when it is held by the node receiving the logout request; and
- prevent the session from being used on any other node in the cluster.

Session state is therefore stored in the shared `SESSION_DS` database. A small in-memory registry is also maintained
on each node, but it is used only to find and invalidate servlet sessions owned by that node. The database is the
authoritative source of session state.

## 2. Scope

Back-channel logout applies only to sessions created by the `/ssoLogin` authorization-code flow. Password-based and
default token login flows do not create OIDC session mappings and are not affected by this mechanism.

The implementation requires a logout token containing an OIDC `sid` claim. Subject-only logout is not supported.
The registered OAuth application therefore declares that session identifiers are required.

The implementation does not revoke OAuth access tokens or refresh tokens. Those tokens may have their own lifetime
and revocation rules at the Identity Provider.

## 3. Important Terms

- **Identity Provider (IdP):** the server that authenticates the user and issues OIDC tokens.
- **Client ID:** the identifier of the OAuth application used by a UI request-handler context.
- **`sid`:** an opaque identifier assigned by the IdP to one of its login sessions.
- **HTTP session:** the servlet-container session used by one deployed UI request-handler application.
- **Logout token:** a signed JWT sent by the IdP to the back-channel logout endpoint.
- **Session mapping:** the database relationship between an IdP `sid` and a local HTTP session ID.
- **Tombstone:** a retained `REVOKED` session-state row that prevents delayed login callbacks from recreating a
  session after logout has already occurred.

An OIDC session belongs to an IdP login session, not directly to a user or device. If several local HTTP sessions are
mapped to the same `sid`, one logout token invalidates all of them for the target application. Sessions on other
devices are logged out only when the IdP assigned them the same `sid` or sends separate logout tokens for their
session identifiers.

## 4. OAuth Application Registration

When `/ssoLogin` cannot find cached OAuth application credentials, it creates the OAuth application through dynamic
client registration (DCR). The registration includes:

- a callback expression that accepts the request-handler's exact SSO callback and UI application login URLs;
- `backchannelLogoutUri`, pointing to the request-handler's `/backchannelLogout` endpoint; and
- `backchannelLogoutSessionRequired` set to `true`.

For a default local deployment, the registered callback expression has this form:

```text
regexp=(^https://localhost:9443/entgra-ui-request-handler/ssoLoginCallback|https:\/\/localhost:9443\/[^/]+\/login$)
```

The actual host, port, and request-handler context are derived from the deployment at runtime. The authorization
request itself continues to use the exact `/ssoLoginCallback` URL as its `redirect_uri`; the regular expression is
used only when registering the OAuth application.

The back-channel logout URI has this form:

```text
https://<public-core-host>:<port>/<application>-ui-request-handler/backchannelLogout
```

If required application registration fails, SSO login does not continue with an incompletely configured client.

## 5. Configuration

The runtime configuration is read from:

```text
repository/conf/ui-request-interceptor.xml
```

Example:

```xml
<UIRequestInterceptorConfiguration>
    <BackChannelLogout>
        <Enabled>true</Enabled>
        <Issuer>https://localhost:9443/oauth2/token</Issuer>
        <JwksUri>https://localhost:9443/oauth2/jwks</JwksUri>
        <AllowedClockSkewSeconds>60</AllowedClockSkewSeconds>
        <StaleMappingRetentionSeconds>86400</StaleMappingRetentionSeconds>
    </BackChannelLogout>
</UIRequestInterceptorConfiguration>
```

| Setting | Meaning |
| --- | --- |
| `Enabled` | Enables ID-token validation, SQL session mapping, request-time state checks, and logout-token processing. |
| `Issuer` | Exact issuer value expected in ID tokens and logout tokens. |
| `JwksUri` | HTTPS endpoint from which the IdP's public signing keys are obtained. |
| `AllowedClockSkewSeconds` | Permitted clock difference when checking token time claims. The default is 60 seconds. |
| `StaleMappingRetentionSeconds` | How long revoked state is retained for replay safety and delayed-request handling. The default is 86,400 seconds. |

System-property placeholders such as `${property.name}` are resolved when the configuration is loaded. If the file
or the `BackChannelLogout` section is absent, back-channel logout is disabled for compatibility with older
installations. An enabled configuration must provide both the issuer and JWKS URI.

## 6. Session Database

The feature supplies a separate datasource named `SESSION_DS`, available through JNDI as `jdbc/SESSION_DS`. All
nodes in a cluster must use the same database. Schema scripts are supplied for H2, MySQL, PostgreSQL, Oracle, and
Microsoft SQL Server under `dbscripts/session`.

### 6.1 `DM_OIDC_SESSION_STATE`

This table stores the authoritative state for an IdP session. Its key is issuer, client ID, and `sid`. Important
values include the subject, application name, `ACTIVE` or `REVOKED` status, logout-token `jti`, timestamps, and the
retention expiry time.

### 6.2 `DM_OIDC_SESSION_MAPPING`

This table connects an IdP session state to one or more local HTTP sessions. A mapping is uniquely identified by
application name and HTTP session ID. Several mappings can point to the same `sid`, allowing one logout token to end
all matching local sessions.

### 6.3 `DM_OIDC_SESSION_CLEANUP`

This table coordinates stale-row cleanup across the cluster. Only the node that obtains the cleanup lease performs a
cleanup pass, avoiding the same work on every node.

The database stores identifiers and lifecycle metadata only. ID tokens, logout tokens, access tokens, refresh tokens,
client secrets, and signing keys are not stored in these tables.

## 7. SSO Login and Session Registration

The session mapping is established after the authorization-code callback completes:

1. `/ssoLoginCallback` exchanges the authorization code for tokens.
2. The returned ID token is kept long enough to validate it and create the mapping.
3. The signature is verified using the configured JWKS.
4. The issuer, audience, authorized party when applicable, expiry, issue time, optional not-before time, subject, and
   `sid` are validated.
5. The service starts a database transaction and locks the matching session-state row when one already exists.
6. An active state and the local HTTP-session mapping are inserted or updated.
7. The transaction is committed before the servlet session is marked as OIDC-mapped and added to the node-local
   registry.

When back-channel logout is enabled, SSO login is rejected if the ID token is absent, invalid, or has no `sid`. The
partially established servlet session is invalidated instead of allowing an authenticated session without a shared
mapping.

If logout wins a race against the login callback, the callback sees the retained `REVOKED` state and cannot recreate
the session. Concurrent registrations retry in a fresh transaction after a duplicate insert or conflicting update.

## 8. Back-Channel Logout Endpoint

Each deployed UI request-handler context exposes:

```text
POST /backchannelLogout
Content-Type: application/x-www-form-urlencoded

logout_token=<signed-jwt>
```

The request must contain exactly one non-empty `logout_token`. `GET` is not supported.

### 8.1 Logout-token validation

The endpoint accepts only an RS256-signed JWT whose signature can be verified with the configured JWKS. It also
requires:

- an issuer exactly matching the configured issuer;
- at least one audience value that can identify the registered client mapping;
- a valid `exp` claim;
- an `iat` claim that is not unacceptably far in the future;
- a valid optional `nbf` claim;
- a non-empty `jti`;
- a non-empty `sid`;
- an `events` object containing an empty
  `http://schemas.openid.net/event/backchannel-logout` member; and
- no `nonce` claim.

Unsigned tokens, including tokens using `alg=none`, and tokens using algorithms other than RS256 are rejected. Token
contents and signing material are not written to application logs.

### 8.2 Processing order

After validation, logout is processed in this order:

1. Find and lock the matching session-state row using issuer, an audience client ID, `sid`, and application name.
2. Read every HTTP session mapped to that state.
3. Change the authoritative state to `REVOKED`, record the `jti` and revocation time, and commit the transaction.
4. Remove each matching session from the receiving node's local registry.
5. Immediately invalidate every matching servlet session found on that node.

The database is always revoked before local invalidation. A process failure after the commit therefore cannot leave
the shared state active.

If no state exists yet, the service creates a revoked tombstone for the candidate client IDs. This handles a logout
that arrives before a delayed SSO callback. Repeated delivery for an already revoked session is treated as a
successful, idempotent operation.

### 8.3 HTTP responses

| Status | Meaning |
| --- | --- |
| `200 OK` | The logout token was valid. This includes unknown, already ended, or already revoked sessions. |
| `400 Bad Request` | The request format or logout token was invalid. |
| `500 Internal Server Error` | Configuration, session service, JWKS processing, or persistence prevented safe processing. |

Responses do not reveal whether a particular session identifier existed.

## 9. Cluster Behavior

The node receiving `/backchannelLogout` can directly invalidate only servlet sessions held in its own memory. A
session may belong to another node, especially when requests are not using sticky routing.

Cluster-wide enforcement is provided by `OidcSessionStateFilter`:

1. Every authenticated request carrying the OIDC-mapped marker is checked against `SESSION_DS`.
2. If the mapping is missing or its state is `REVOKED`, the local servlet session is invalidated.
3. The request is rejected with `401 Unauthorized`.
4. If the database check fails, the request fails closed with `500 Internal Server Error`.

Login, SSO callback, back-channel logout, and ordinary login/logout endpoints are excluded from this filter. Sessions
created by non-OIDC login flows are also skipped because they do not carry the OIDC-mapped marker.

This design gives immediate invalidation on the node receiving the notification and guaranteed invalidation on the
next request for a session owned by another node. It does not attempt unsupported cross-node access to servlet
container memory.

Database transactions and row locks establish a consistent order between registration and logout. Portable no-op
updates are used to obtain write locks because not every supported database accepts `SELECT ... FOR UPDATE` in the
same way.

## 10. Ordinary Logout and Session Expiry

`OidcSessionListener` runs when a mapped servlet session expires or is invalidated through ordinary logout. It removes
the node-local registry entry and deletes an active SQL mapping.

Revoked state is deliberately retained until its configured expiry. Keeping the tombstone makes repeated logout
delivery safe and prevents an older callback from reviving a logged-out session.

Active mappings update their last-seen and expiry values during request checks. Writes are throttled to avoid updating
the database on every request. Stale cleanup is attempted opportunistically during registration and logout, with a
cluster-wide lease limiting cleanup to approximately once per hour.

## 11. Failure and Recovery Expectations

- If the shared database is temporarily unavailable, mapped authenticated requests fail closed instead of bypassing
  logout enforcement.
- If local servlet-session invalidation fails because the session already ended, logout still succeeds because the
  database was revoked first.
- Registration and logout operations retry once in a new transaction when a concurrent node wins an insert or update
  race.
- Cleanup failure does not fail login or logout. A later operation retries cleanup after the lease interval.
- All nodes must use matching issuer, JWKS, retention, datasource, and schema settings.
- Database clocks are not used for token validation; node clocks should be synchronized so configured skew remains
  meaningful.

## 12. Operational Checklist

Before enabling back-channel logout:

1. Configure every node to use the same production `SESSION_DS` database.
2. Apply the matching database script from `dbscripts/session`.
3. Configure the correct issuer and HTTPS JWKS URI in `ui-request-interceptor.xml`.
4. Confirm that the IdP can reach every registered `/backchannelLogout` URI through the load balancer or gateway.
5. Confirm that new OAuth applications contain the back-channel URI and require a session identifier.
6. Confirm that the IdP issues ID tokens containing `sid` and sends RS256-signed logout tokens.
7. Keep node clocks synchronized.
8. Monitor warnings and errors related to token validation, datasource access, and cleanup.

## 13. Expected Verification

Automated and integration testing should cover:

- successful ID-token session registration;
- missing, invalid, or `sid`-less ID tokens;
- valid local logout and multiple local sessions mapped to one `sid`;
- logout received by a different cluster node;
- logout arriving before the login callback;
- repeated logout-token delivery;
- invalid signature, issuer, audience, time claims, event, `jti`, `sid`, nonce, and algorithm;
- concurrent registration and logout ordering;
- ordinary logout and timeout cleanup;
- temporary database failures; and
- DCR payload values for callback and back-channel logout metadata.

