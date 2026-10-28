# Application Management Architecture Baseline

[Tracking](spec-register.md) | [Gaps](gap-register.md) | [Verification](verification-matrix.md) | [Sources](source-map.md)

## Boundaries

`application.mgt.common` supplies service interfaces, DTOs, wrappers and exceptions.
`application.mgt.core` supplies implementations, DAO dialects, storage/transfer,
lifecycle evaluation, tasks and OSGi activation. Publisher/Store/artifact HTTP
adapters are in proprietary commons, not children of this component POM. Feature
packaging provides runtime bundles, XML configuration/templates and schema scripts.

```text
Publisher / Store / artifact API / trusted internal caller
    -> common service contract
    -> ApplicationManager / SubscriptionManager / ReviewManager / integrations
        -> DAO factory -> ConnectionManagerUtil -> Carbon JNDI datasource
        -> storage / transfer / remote downloader -> filesystem or external URL
        -> device-management / identity / VPP services
```

## Activation and execution

DS.activate initializes the configured datasource and lifecycle manager, then
registers SPApplicationManager, ApplicationManager, ReviewManager,
SubscriptionManager, ApplicationStorageManager, AppmDataHandler,
VPPApplicationManager and FileTransferService. It schedules subscription cleanup.
Configured extension classes are selected through existing utility/config paths;
the default implementation is not the only possible deployment.

Transactions are manual JDBC, thread-local and connection-scoped. No distributed
transaction between APPM, device management, remote services and artifact storage
is established by these registrations. Remote release creation and rating updates
use separate threads in the inspected implementations. Their completion cannot
be inferred from the initiating request's successful return.

## Persistence map

| Concern | Principal tables / boundary |
| --- | --- |
| Applications and releases | AP_APP, AP_APP_RELEASE |
| Lifecycle and visibility | AP_APP_LIFECYCLE_STATE, AP_UNRESTRICTED_ROLE |
| Catalog organization | AP_APP_CATEGORY, AP_APP_TAG and mapping tables |
| Reviews/favourites | AP_APP_REVIEW, AP_APP_FAVOURITES |
| Subscriptions | AP_DEVICE_SUBSCRIPTION, AP_USER_SUBSCRIPTION, AP_GROUP_SUBSCRIPTION, AP_ROLE_SUBSCRIPTION, AP_APP_SUB_OP_MAPPING |
| Scheduling | AP_SCHEDULED_SUBSCRIPTION |
| Integrations | AP_IDENTITY_SERVER, AP_IS_SP_APP_MAPPING, AP_VPP_USER, AP_ASSETS, AP_VPP_ASSOCIATION |
| Firmware | AP_APP_DEVICE_MODEL; device firmware models belong to device management |

Use the engine-specific scripts and actual deployed migrations as authority for
constraints. A service-level precheck is not equivalent to a unique constraint.

## Shared contracts

See [cross-cutting requirements](cross-cutting-requirements.md) for APP-X-001 through
APP-X-004, [runtime](runtime/runtime.md) for APP-RUN-001 through APP-RUN-003, and
[gap register](gap-register.md) for unresolved deployment and ownership questions.
No claim is made that shared storage, cache invalidation or scheduler singleton
execution is configured in production.
