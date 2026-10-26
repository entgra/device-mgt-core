# Geo Fence Spec

## Purpose

A geo fence is a named boundary (circle or polygon) assigned to device groups.
Each fence has event triggers (`IN`, `OUT`, `STATIONARY`) and actions. Creating
or changing a fence stores it for the tenant and pushes an event operation to
the devices in those groups.

The list API also returns the counts the Geo Fences pager needs. A name search
must report how many fences match that name. The unfiltered tenant total stays
a separate field.

UI behaviour is specified in
`emm-proprietary-plugins/.../endpoint.mgt.ui/Specs/geo-fences.md`.

## Audience And Components

| Concern | Detail |
| --- | --- |
| Layer | Device Management API, geo service, geo DAO |
| Base path | `/api/device-mgt/v1.0/geo-services` |
| Scope | `dm:geo:geo-fence:manage` |
| Permission | `/device-mgt/devices/owning-device/manage-geo-fence` |
| Consumers | Endpoint Manager Geo Fences page and device-location overlay |
| Status | API-backed |

`/alerts/{alertType}` (Within, Exit, Speed, Proximity, Stationery, Traffic) is a
separate Siddhi alert API under `dm:geo:alerts:manage`. Creating a fence does
not deploy those plans.

## Source Files

| Area | Path |
| --- | --- |
| REST | `device.mgt.api/.../GeoLocationBasedService.java`, `GeoLocationBasedServiceImpl.java` |
| Request bean | `device.mgt.api/.../jaxrs/beans/GeofenceWrapper.java` |
| Domain | `device.mgt.common/.../geo/service/GeofenceData.java`, `GeoFenceEventMeta.java` |
| Service | `device.mgt.core/.../geo/service/GeoLocationProviderServiceImpl.java` |
| Device push | `device.mgt.core/.../event/config/EventOperationExecutor.java` |
| Count SQL | `device.mgt.core/.../dao/impl/AbstractGeofenceDAOImpl.java` (`getGeofenceCount`) |
| Paged list SQL | `.../dao/impl/geofence/GenericGeofenceDAOImpl.java`, `SQLServerGeofenceDAOImpl.java` |
| Tables | `features/.../dbscripts/cdm/*.sql` (`DM_GEOFENCE*`, `DM_DEVICE_EVENT*`) |

`GenericGeofenceDAOImpl` and `SQLServerGeofenceDAOImpl` extend
`AbstractGeofenceDAOImpl` and do not override the count methods. The filtered
count is `COUNT(*)` with `FENCE_NAME LIKE ?`, which both databases accept.
Those subclasses only override the list query because paging syntax differs
(`LIMIT/OFFSET` versus `OFFSET/FETCH`). Both list queries already apply the
same `FENCE_NAME LIKE` predicate when a name is present.

## Stored Shape

| Table | Holds |
| --- | --- |
| `DM_GEOFENCE` | Name, description, latitude, longitude, radius, `GEO_JSON`, `FENCE_SHAPE`, owner, tenant |
| `DM_GEOFENCE_GROUP_MAPPING` | Fence to device group |
| `DM_DEVICE_EVENT` | Trigger row: `EVENT_SOURCE`, `EVENT_LOGIC`, `ACTIONS` |
| `DM_DEVICE_EVENT_GROUP_MAPPING` | Which groups receive the event |
| `DM_GEOFENCE_EVENT_MAPPING` | Fence to event |

| Shape | Stored fields |
| --- | --- |
| `circle` | `latitude`, `longitude`, `radius` (meters) |
| `polygon` | `geoJson` (stringified Leaflet lat/lng rings, not a GeoJSON Feature), `fenceShape=polygon` |

`eventConfig` items use `eventLogic` `IN`, `OUT`, or `STATIONARY`. On create,
the service sets `EVENT_SOURCE` to `GEOFENCE`. Actions are stored as JSON
(`actionType`, typically `MAIL`).

Foreign keys on the mapping tables are `ON DELETE NO ACTION`, so a fence row
cannot be removed while mapping rows still point at it. Service delete removes
mappings and events first.

## Create, Update, Delete

```
POST   /geo-services/geo-fence
GET    /geo-services/geo-fence/{fenceId}?requireEventData=
PUT    /geo-services/geo-fence/{fenceId}?eventIds=
DELETE /geo-services/geo-fence/{fenceId}
```

Create body (`GeofenceWrapper`): `fenceName`, `description`, `fenceShape`,
circle or polygon fields, `groupIds`, and `eventConfig` (`eventLogic`,
`actions`, and `id` on update).

| Step | What happens |
| --- | --- |
| Validate | Fence payload and event configuration. Invalid input returns `400` |
| Persist | Insert `DM_GEOFENCE`, cache the fence, insert group mappings |
| Events | Insert `DM_DEVICE_EVENT` (`EVENT_SOURCE=GEOFENCE`) and fence/group mappings |
| Push | `EVENT_CONFIG` operation whose payload is `GeoFenceEventMeta` plus the triggers |
| Update | `EVENT_UPDATE` for the remaining groups. Groups removed from the fence get `EVENT_REVOKE` |
| Delete | `EVENT_REVOKE`, then mapping rows, events, and the fence. Unknown id returns `404` |

The push runs only when event operation tasks are enabled in device-management
configuration. The fence is still stored when that task is disabled.

Devices in a mapped group receive the fence through group membership. Enter,
exit, and stationary checks run on the device from `eventLogic`. The server
does not test each location update against `DM_GEOFENCE`.

| Result | HTTP | When |
| --- | --- | --- |
| Created or updated | `201` | Fence and events saved |
| Invalid payload | `400` | Validation failed |
| Unknown fence | `404` | Update or delete id does not exist |
| Persist or event failure | `500` | Database, event, or operation task error |

## List And Name Search

```
GET /api/device-mgt/v1.0/geo-services/geo-fence?offset={n}&limit={n}&name={text}&requireEventData={bool}
```

| Query | Role |
| --- | --- |
| `offset`, `limit` | Page window. Used when `offset >= 0` and `limit != 0` |
| `name` | Partial fence name. Omitted or empty means no name filter |
| `requireEventData` | When true, events are attached to each returned fence |

Name matching is a SQL `LIKE` of `%name%` (collation decides case). It is the
same predicate on the paged list and on the filtered count. It is not an exact
match.

When `limit` is `0`, the call is not paged: a `name` returns every match, and
no `name` returns every fence for the tenant. The device-location overlay uses
that unpaged call.

### Response counts

| Field | Meaning |
| --- | --- |
| `data` | Fences in this response |
| `recordsTotal` | Size of `data`. On a paged call this is the page length, not the size of the match set |
| `recordsFiltered` | Number of fences that match the current query. With no name, this equals the tenant total |
| `totalDeviceCount` | Unfiltered fence count for the tenant. Unchanged by `name` |

`totalDeviceCount` is the historical name of the tenant-wide fence count on
this payload. It is not a device count.

| Request | `recordsFiltered` | `totalDeviceCount` |
| --- | --- | --- |
| No `name` | All fences for the tenant | Same number |
| `name` set | Fences whose name matches `%name%` | All fences for the tenant |

Example: 53 fences in the tenant, 15 names contain `2`, `limit=10`.

| Page | `data` length | `recordsTotal` | `recordsFiltered` | `totalDeviceCount` |
| --- | --- | --- | --- | --- |
| `offset=0` | 10 | 10 | 15 | 53 |
| `offset=10` | 5 | 5 | 15 | 53 |

A search that matches nothing returns `data: []`, `recordsTotal: 0`, and
`recordsFiltered: 0`. `totalDeviceCount` stays the tenant total.

The pager total for a name search is `recordsFiltered`. `recordsTotal` must not
be used as the match count: on a full page it equals `limit`, so a search with
more matches than one page would hide the rest. `totalDeviceCount` must not be
used as the search total: it stays 53 and later pages request offsets past the
matches.

| Result | HTTP | When |
| --- | --- | --- |
| Page returned | `200` | List and count succeed |
| Count or list failure | `500` | Geo service throws while reading fences or the filtered count |

## Correct Behaviour

- A fence is visible in the list only after it is stored for the current tenant.
- Group assignment decides which devices receive `EVENT_CONFIG`, `EVENT_UPDATE`, or `EVENT_REVOKE`.
- Circle and polygon geometry round-trip through get-by-id and the paged list when `requireEventData=true`.
- Name search pages by `recordsFiltered`. Clearing `name` makes that count equal `totalDeviceCount`.
- Create, update, and delete do not read `recordsFiltered`.
