# Device Type OS Version Labels Spec

## Purpose

Device-type platform versions are listed for Publisher Supported OS Versions
dropdowns and for application release validation. Android stores SDK API levels
as `VersionName` (for example `26`). Clear display labels (for example
`Android 8.0`) come from optional `VersionLabel` entries in each platform’s
device-type XML and are attached when versions are returned from the API.

`VersionName` remains the persisted / submitted value used for OS range checks.
Labels are display-only and are not stored in `DM_DEVICE_TYPE_PLATFORM`.

## Audience And Components

| Concern | Detail |
| --- | --- |
| Layer | Device Management core + Device Type admin API |
| Consumers | Application Publisher UI (create app, add release, edit release), any client of device-type versions |
| Status | API-backed |

## Source Files

| Area | Path |
| --- | --- |
| XML model | `device.mgt.common/.../type/mgt/DeviceTypePlatformVersion.java` (`VersionName`, optional `VersionLabel`) |
| API / internal DTO | `device.mgt.core/.../dto/DeviceTypeVersion.java` (`versionName`, `versionLabel`, `versionStatus`) |
| Persist / seed | `DeviceManagementProviderServiceImpl.initializeDeviceTypeVersions` (seeds `VersionName` only) |
| Enrich on read | `DeviceManagementProviderServiceImpl.getDeviceTypeVersions` → `enrichDeviceTypeVersionLabels` |
| GET API | `device.mgt.api/.../admin/DeviceTypeManagementAdminServiceImpl.java` `GET /{deviceTypeName}/versions` |
| Android XML | `emm-proprietary-plugins/.../android.feature/.../devicetypes/android.xml` |
| iOS XML | `emm-proprietary-plugins/.../ios.feature/.../devicetypes/ios.xml` |
| Windows XML | `emm-proprietary-plugins/.../windows.feature/.../devicetypes/windows.xml` |

## XML Contract

Under `DeviceTypePlatformDetails` in each device-type XML:

```xml
<DeviceTypePlatformVersion>
    <VersionName>26</VersionName>
    <VersionLabel>Android 8.0</VersionLabel>
</DeviceTypePlatformVersion>
```

| Element | Required | Role |
| --- | --- | --- |
| `VersionName` | Yes | Stored in DB; used in release `supportedOsVersions` ranges and validation |
| `VersionLabel` | No | User-facing label returned on GET versions |

### Platform conventions

| Device type | `VersionName` meaning | Example label |
| --- | --- | --- |
| android | SDK API level (integer string) | `Android 8.0`, `Android 14`, or `Android API 37` when no marketing name exists |
| ios | Marketing OS version string | `iOS 14.0` |
| windows | Marketing OS version string | `Windows 10` |

When a new OS version is added, update the platform XML only. No frontend map
update is required for the label to appear after the plugin is redeployed and
the version is available from GET.

## Persistence

Table: `DM_DEVICE_TYPE_PLATFORM`

| Column | Source |
| --- | --- |
| `VERSION_NAME` | XML `VersionName` |
| `VERSION_STATUS` | Status (for example ACTIVE) |

`VersionLabel` is **not** persisted. On each `getDeviceTypeVersions`, the
provider loads labels from the registered device-type plugin’s
`DeviceTypePlatformDetails` and sets `versionLabel` on each DTO before return.

## API

```
GET /api/device-mgt/v1.0/admin/device-types/{deviceTypeName}/versions
```

Response items include at least:

| Field | Type | Notes |
| --- | --- | --- |
| `versionName` | string | Value to submit in Supported OS ranges |
| `versionLabel` | string \| null | Present when XML defines `VersionLabel` |
| `versionStatus` | string | ACTIVE / INACTIVE / REMOVED as applicable |
| `deviceTypeName` | string | Device type |

Enrichment failure must not fail the GET: versions are still returned; missing
labels leave `versionLabel` unset.

## Correct Behaviour

| Case | Expected |
| --- | --- |
| Android API 26 with label in XML | `versionName=26`, `versionLabel=Android 8.0` |
| Version in DB but no XML label | `versionName` returned; `versionLabel` absent / null |
| New XML `VersionName` not yet in DB | Seeded on device-type init; later GET can include label |
| Release payload `supportedOsVersions` | Still uses `versionName` values (for example `26-34`), never labels |

## Related Specs

- Publisher UI: `proprietary-commons/.../application.mgt.publisher.ui/Specs/create-applications/supported-os-versions.md`
- App create validation (ranges): `device-mgt-core/.../application-mgt/Specs/create-applications/`
