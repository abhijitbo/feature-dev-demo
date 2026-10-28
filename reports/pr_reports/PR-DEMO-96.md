# PR Report — DEMO-96

**Date:** 2026-10-28
**Branch:** feature/DEMO-96-allow-customers-to-cancel-a-placed-order
**PR URL:** https://github.com/abhijitbo/feature-dev-demo/pull/2
**Jira Story:** DEMO-96 — Allow customers to cancel a placed order with a reason
**Jira URL:** https://jira-team-1321.atlassian.net/browse/DEMO-96

---

## Story Summary
Let customers cancel a placed order and give a reason:
- Orders get a lifecycle status: `PLACED` or `CANCELLED`.
- Only `PLACED` orders can be cancelled.
- The reason is required, must not be blank, and is at most 255 characters.
- The cancellation time comes from an injected `Clock`.

## Confluence Documentation Referenced
None. Searches in space `DEMO` for "DEMO-96" and "cancel order reason" returned no pages.

## Acceptance Criteria Traceability
| AC | Criterion | File | Status |
|---|---|---|---|
| AC-01 | `placeOrder` stores the order with status `PLACED` | `Order.java:40-42`, `OrderService.java:88-90` | ✅ Implemented |
| AC-02 | `cancelOrder` saves `CANCELLED` + reason + clock instant; returns updated order | `Order.java:53-55`, `OrderService.java:173-192` | ✅ Implemented |
| AC-03 | Already `CANCELLED` → `IllegalStateException` naming id and status; not saved | `OrderService.java:185-188` | ✅ Implemented |
| AC-04 | Unknown order id → `OrderNotFoundException` | `OrderService.java:184` (via `getOrder`) | ✅ Implemented |
| AC-05 | Null id / null-blank reason / reason > 255 → `IllegalArgumentException`; repository untouched | `OrderService.java:174-183` | ✅ Implemented |

## Implementation Details
| File | Change Type | Description |
|---|---|---|
| `src/main/java/com/demo/orders/model/OrderStatus.java:1-11` | New | `PLACED` / `CANCELLED` |
| `src/main/java/com/demo/orders/model/Order.java:17-55` | Modified | `status`, `cancellationReason`, `cancelledAt`; invariant; 3-arg constructor defaults to `PLACED`; `cancel(reason, at)` |
| `src/main/java/com/demo/orders/service/OrderService.java:30-72` | Modified | `MAX_CANCELLATION_REASON_LENGTH`; `Clock` injected via a new 3-arg constructor; existing constructors default to `Clock.systemUTC()` |
| `src/main/java/com/demo/orders/service/OrderService.java:81-91` | Modified | `placeOrder` rejects non-`PLACED` orders |
| `src/main/java/com/demo/orders/service/OrderService.java:162-192` | Modified | New `cancelOrder(orderId, reason)` |

## Code Documentation Added
| File | Type | Description |
|---|---|---|
| `OrderStatus.java` | Javadoc | Enum, `@since DEMO-96` |
| `Order.java` | Javadoc + inline | New record components, 3-arg constructor, `cancel`; inline comment on the cancellation invariant |
| `OrderService.java` | Javadoc | New constructor, updated `placeOrder` contract, `cancelOrder`, reason-length constant |

## Unit Tests Added
| Test Class | Test Method | AC Covered |
|---|---|---|
| OrderTest | `constructor_whenThreeArguments_defaultsToPlacedWithoutCancellationDetails` | AC-01 |
| OrderTest | `cancel_whenPlaced_returnsCancelledCopyAndLeavesOriginalUnchanged` | AC-02 |
| OrderTest | `cancel_whenReasonIsNull_throwsIllegalArgumentException` | AC-02 |
| OrderTest | `cancel_whenTimeIsNull_throwsIllegalArgumentException` | AC-02 |
| OrderTest | `constructor_whenPlacedWithCancellationDetails_throwsIllegalArgumentException` | Invariant |
| OrderTest | `constructor_whenStatusIsNull_throwsNullPointerException` | Invariant |
| OrderServiceTest | `placeOrder_whenNewOrder_storesStatusPlaced` | AC-01 |
| OrderServiceTest | `placeOrder_whenOrderIsCancelled_throwsIllegalArgumentException` | AC-01 |
| OrderServiceTest | `cancelOrder_whenPlaced_savesCancelledOrderWithReasonAndClockTime` | AC-02 |
| OrderServiceTest | `cancelOrder_whenReasonIsExactlyMaxLength_cancelsOrder` | AC-02, AC-05 |
| OrderServiceTest | `cancelOrder_whenAlreadyCancelled_throwsIllegalStateException` | AC-03 |
| OrderServiceTest | `cancelOrder_whenOrderMissing_throwsOrderNotFoundException` | AC-04 |
| OrderServiceTest | `cancelOrder_whenOrderIdIsNull_throwsIllegalArgumentException` | AC-05 |
| OrderServiceTest | `cancelOrder_whenReasonIsNullOrBlank_throwsIllegalArgumentException` (×4) | AC-05 |
| OrderServiceTest | `cancelOrder_whenReasonTooLong_throwsIllegalArgumentException` | AC-05 |

## Test Execution
| Metric | Value |
|---|---|
| Total Tests | 56 (18 new, 38 existing) |
| Passed | 56 |
| Failed | 0 |
| SAST Result | ⚠️ Not run — `sonar.space` not configured |
| Test Report | `reports/unit_test_reports/DEMO-96/test-summary.md` |

## Commits
```
5fa4368 feat(DEMO-96): allow customers to cancel a placed order with a reason
```
(plus the docs commit that adds this report)

## PR Details
- **Title:** feat(DEMO-96): Allow customers to cancel a placed order with a reason
- **Base Branch:** main
- **Head Branch:** feature/DEMO-96-allow-customers-to-cancel-a-placed-order
- **URL:** https://github.com/abhijitbo/feature-dev-demo/pull/2

## Jira Update
- Comment added: Yes, with the PR link, branch, implementation summary and test results
- Status transitioned to: In Progress. The workflow has no "In Review" status (only To Do / In Progress / Done).

## Notes for Reviewers
- **`placeOrder` behaviour change:** it now rejects an order that isn't `PLACED`. This is what guarantees AC-01.
- **No ownership check:** `cancelOrder` doesn't check that the caller owns the order. The service has no authentication yet.
- **Checkout gap:** `checkout` still accepts a `CANCELLED` order. No AC covers it; this would make a good follow-up story.

## Next Step
Run `pr-review` skill with `DEMO-96` for structured code review before merging.
