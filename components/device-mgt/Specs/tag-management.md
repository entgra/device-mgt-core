# Tag Management Spec

## Purpose

Tags are tenant-scoped labels used to group devices. Administrators create,
rename, and delete tags, then assign or remove those tags on devices.

Creating a tag whose name is already stored for the tenant must fail. The
insert path itself skips an existing name, so the create API checks first and
returns a clear error instead of HTTP 201.

UI behaviour is specified in
`emm-proprietary-plugins/.../endpoint.mgt.ui/Specs/tags.md`.

## Audience And Components

| Concern | Detail |
| --- | --- |
| Layer | Device Management API, tag service, tag DAO |
| Base path | `/api/device-mgt/v1.0/tags` |
| Consumers | Endpoint Manager Tags page, device tag assignment, related-tags view |
| Status | API-backed |

## Source Files

| Area | Path |
| --- | --- |
| REST contract | `device.mgt.api/.../jaxrs/service/api/TagManagementService.java` |
| REST impl | `device.mgt.api/.../jaxrs/service/impl/TagManagementServiceImpl.java` |
| Request beans | `device.mgt.api/.../jaxrs/beans/TagInfo.java`, `TagMappingInfo.java` |
| Service | `device.mgt.core/.../service/TagManagementProviderServiceImpl.java` |
| DAO | `device.mgt.core/.../dao/impl/TagDAOImpl.java` |
| Tables | `features/.../dbscripts/cdm/*.sql` (`DM_TAG`, `DM_DEVICE_TAG_MAPPING`) |

## Scopes And Permissions

| Action | Scope | Permission |
| --- | --- | --- |
| List tags | `tm:tags:view` | `/device-mgt/tags/view` |
| Create tag | `tm:tags:create` | `/device-mgt/tags/create` |
| Update tag | `tm:tags:update` | `/device-mgt/tags/update` |
| Delete tag | `tm:tags:delete` | `/device-mgt/tags/delete` |
| Assign tags to devices | `tm:tags:mapping:create` | `/device-mgt/tags/mapping/create` |
| Remove tags from devices | `tm:tags:mapping:delete` | `/device-mgt/tags/mapping/delete` |
| View tag mappings | `tm:tags:mapping:view` | `/device-mgt/tags/mapping/view` |

## Stored Shape

| Table | Holds |
| --- | --- |
| `DM_TAG` | `ID`, `NAME` (`VARCHAR(255) NOT NULL`), `DESCRIPTION`, `TENANT_ID` |
| `DM_DEVICE_TAG_MAPPING` | `ENROLMENT_ID`, `TAG_ID`, `TENANT_ID` |

`DM_TAG` has unique key `DM_TAG_NAME_TENANT_UNIQUE` on (`NAME`, `TENANT_ID`).

`NAME` is declared without a case-sensitive collation. On MySQL the column
uses the database default, which is case-insensitive, so `tag1` and `Tag1`
are the same name for both the unique key and `WHERE NAME = ?`.

## Create

```
POST /tags
```

Scope: `tm:tags:create`.

Body is a list of tag objects:

```json
[{ "name": "tag1", "description": "optional" }]
```

| Result | HTTP | Body |
| --- | --- | --- |
| Name is new for the tenant | `201` | Request list |
| Name already exists for the tenant, including a different letter case | `400` | `ErrorResponse.message` = `A tag with this name already exists. Please use a different tag name.` |
| Tag name is null | `400` | `ErrorResponse.message` from `BadRequestException` (`Tag name cannot be null or empty.`) |
| Persistence failure | `500` | `Error occurred while adding tags.` |

The create API calls `getTagByName` before insert. `getTagByName` runs
`SELECT * FROM DM_TAG WHERE NAME = ? AND TENANT_ID = ?`. A matching row makes
`tagNameExists` true and the API returns `400` without inserting.

`TagManagementProviderService.addTags` stays insert-if-not-exists
(`INSERT ... SELECT ... WHERE NOT EXISTS`). Device-tag mapping and existing
service tests rely on that. Duplicate rejection for the create API is only in
`TagManagementServiceImpl.addTag`.

## List, Get, Update, Delete

```
GET    /tags
GET    /tags/{tagId}
PUT    /tags?tagId={id}
PUT    /tags?tagName={name}
DELETE /tags/{tagId}
```

| Call | Success | Not found |
| --- | --- | --- |
| `GET /tags` | `200`, tag list | — |
| `GET /tags/{tagId}` | `200`, one tag | `404` |
| `PUT` | `200`, updated tag | `404` when id or current name is missing |
| `DELETE /tags/{tagId}` | `204` | `404` |

Update looks up the tag by `tagId` or `tagName`, then sets `name` and
`description` from the body. If another tag in the tenant already has the new
name, the service throws `BadRequestException` with
`Tag with name: {name} already exists.` and the API returns `400` with that
message. The same case-insensitive `NAME = ?` comparison applies.

## Device Mapping

```
POST   /tags/mapping
DELETE /tags/mapping
```

Body (`TagMappingInfo`): `deviceIdentifiers`, `deviceType`, `tags` (names).

| Call | Success | Notes |
| --- | --- | --- |
| `POST /tags/mapping` | `201` | Assigns the named tags to the devices. Missing tag rows are inserted by `addTags` (insert-if-not-exists). A mapping that already exists returns `400` with `Tag is already mapped to this device.` |
| `DELETE /tags/mapping` | `204` | Removes those tag names from the devices |

Devices in one assign or unassign request must be the same device type. The
UI enforces that before calling these endpoints.

## Correct Behaviour

1. The first create of a name returns `201` and the tag appears in `GET /tags`.
2. A second create of that name, or the same name with different letter case, returns `400` and does not add another row.
3. The `400` message is `A tag with this name already exists. Please use a different tag name.`
4. Rename to a name already used by a different tag returns `400`.
5. Assigning tags does not fail just because the tag row already exists.
