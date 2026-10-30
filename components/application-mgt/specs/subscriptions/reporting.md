# Subscription Reporting

[Tracking](../spec-register.md) | [Gaps](../gap-register.md) | [Verification](../verification-matrix.md)

## Service dispatch and response distinctions

getSubscriptions, getStatusBaseSubscriptions and getStatistics select a helper
from subscriptionInfo.subscriptionType: ROLE, GROUP, USER or DEVICE, using exact
string comparison. Unsupported types throw UnsupportedOperationException; this
is not the same dispatch contract as older case-insensitive reporting methods.

Device-based getSubscriptions returns an empty SubscriptionResponse; device-based
getSubscriptionStatistics returns null. Do not advertise uniform populated
statistics across all four types. User/group/role helpers have their own DAO and
membership logic; their complete authorization/error matrix remains APP-GAP-18.

## Status-based device data

The inspected device helper resolves release/application in the current tenant
and throws NotFoundException for missing records. The unsubscribe boolean is
selected by exact subscriptionStatus comparison. NEW selects device IDs outside
the previously selected subscription IDs; other paths load matching subscription
rows. triggeredBy is passed to the DAO.

The helper uses the effective filter status to choose the NEW branch, but maps
database statuses from subscriptionInfo.deviceSubscriptionStatus. Different values
in these fields are not proven equivalent. Device name, enrollment status, owner
and application device type are passed to device-management for count and paging.
The count is computed separately from the paginated device list.

Returned device data includes ID/identifier, owner, type, name, enrollment status,
ownership and last-update date, plus trigger actor/time, subscription status/type/ID.
The shared mapper selects unsubscribedBy for unsubscribe, but uses subscribedTimestamp
for triggeredAt in both cases. Record this as observed behavior, not intended event semantics.

## Statistics and legacy reporting

Shared percentage calculation is numerator/denominator * 100, returning zero
when denominator <= 0. NEW uses total minus completed/pending/failed; there is no
clamping in this helper. Membership and denominator choices differ by helper.

getAppInstalledDevices considers a device installed when subscription is completed
and not unsubscribed, or when unsubscribe is not completed. Empty candidate sets
return empty data with zero totals; otherwise device-management supplies paging.
getAppInstalledSubscribers handles USER/ROLE/GROUP ignoring case, uses separate
DAO counts and assigns the same count to recordsFiltered and recordsTotal.
Other subtypes fall through to empty data/count rather than the new provider's exception.

Operation-history and categorized-count methods reject null/empty UUID with
IllegalArgumentException and missing tenant release with NotFoundException.
History enriches rows with available device-operation details; an operation-service
exception is wrapped in RuntimeException. Counts return Device, Group, Role and
User subscription/unsubscription totals; these are not installation-success totals.

## Requirements and acceptance

| ID | Basis | Requirement | Acceptance scenario |
| --- | --- | --- | --- |
| APP-SUB-004 | Observed reporting baseline; helper edges partial | Preserve provider selection, device empty/null results, filter/count distinctions and percentage calculation; do not equate subscription counts with installed devices. | AC-APP-SUB-004: each helper type, unsupported/case-variant type, NEW/other statuses, conflicting filter statuses, owner/name/status filtering, zero denominator and count/page comparison. |
| APP-SUB-005 | Observed history/count and legacy behavior | Preserve UUID validation, operation enrichment, category count meanings and legacy installed-device selection. | AC-APP-SUB-005: blank/unknown/foreign UUID, absent operation details, device-service failure, completed/failed install and uninstall, empty candidates and repeated subscriber categories. |

Sources: SubscriptionManagerImpl reporting methods and subscription-management
provider/helper implementations. APP-GAP-10/18 retain full
caller authorization, helper membership, service-error and timestamp validation work.
