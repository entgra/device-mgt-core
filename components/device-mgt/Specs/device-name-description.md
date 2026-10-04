# Device Name And Description Persistence Spec

## Purpose

Device display name and description are stored on `DM_DEVICE` (`NAME`, `DESCRIPTION`).
Leading and trailing whitespace is removed before those values are written so
enroll, rename, and device-info updates do not persist padded strings.

## Audience And Components

| Concern | Detail |
| --- | --- |
| Layer | Device Management core (DAO + provider) |
| Consumers | Device Management API, platform enrollment plugins, device-info updates, Endpoint Manager rename UI |
| Status | API-backed |

## Source Files

| Area | Path |
| --- | --- |
| Persist (insert / update) | `device.mgt.core/.../dao/impl/AbstractDeviceDAOImpl.java` (`addDevice`, `updateDevice`) |
| Rename | `device.mgt.core/.../service/DeviceManagementProviderServiceImpl.java` (`updateDeviceName`) |
| Rename API | `device.mgt.api/.../DeviceManagementServiceImpl.java` (`POST .../rename`) |
| Device-info name change | `device.mgt.core/.../device/details/mgt/impl/DeviceInformationManagerImpl.java` |

## Persistence Contract

| Field | Column | Trim before persist |
| --- | --- | --- |
| Name | `DM_DEVICE.NAME` | Yes (null stays null) |
| Description | `DM_DEVICE.DESCRIPTION` | Yes (null stays null) |

Trim is applied in `AbstractDeviceDAOImpl` for every `addDevice` and `updateDevice`
call. Trimmed values are written back onto the in-memory `Device` so callers and
cache see the same value that was stored.

## Rename API

```
POST /api/device-mgt/v1.0/devices/type/{deviceType}/id/{deviceId}/rename
Body: { "name": "<new name>" }
```

| Result | HTTP | When |
| --- | --- | --- |
| Success | `201` | Name updated (notification may add a `Warning` header) |
| Missing body / empty name | `400` | Name not provided or blank after API empty check |
| Device missing | `404` | Unknown type/id |
| Same name | `409` | Trimmed incoming name equals the current persisted name |

`updateDeviceName` trims the incoming name before the same-name conflict check and
before `updateDevice`, so `" Foo "` conflicts with an existing `"Foo"` and is not
treated as a distinct rename.

## Enrolment And Device Info

- Enrolment paths that call `deviceDAO.addDevice` persist a trimmed name and description.
- Device-info payloads that change `DEVICE_NAME` and call `deviceDAO.updateDevice`
  also persist a trimmed name (via the DAO trim).

## Correct Behaviour

Only leading and trailing whitespace is removed. Spaces between words are kept.

| Input | Persisted |
| --- | --- |
| `"  Trim Test  "` | `Trim Test` |
| `"  This is a description.  "` | `This is a description.` |

- Rename with only surrounding spaces around the current name returns `409`
- UI and GET device responses after a successful rename show the trimmed name
