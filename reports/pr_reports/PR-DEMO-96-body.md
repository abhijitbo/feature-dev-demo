## Jira Story
- **ID:** DEMO-96
- **Summary:** Allow customers to cancel a placed order with a reason
- **Priority:** Medium
- **Story Points:** Not set
- **Assignee:** Unassigned
- **Link:** https://jira-team-1321.atlassian.net/browse/DEMO-96

## Feature Description
Customers cannot currently cancel an order once it is placed; support staff have to do it manually. Customer service wants customers to cancel their own orders before fulfilment, with a recorded reason, to reduce support tickets and to analyse why orders are cancelled.

**Business rules**
- **BR-1** Every order has a status: `PLACED` when stored by `placeOrder`, `CANCELLED` once cancelled.
- **BR-2** Only a `PLACED` order can be cancelled.
- **BR-3** Reason required: not null, not blank, at most 255 characters.
- **BR-4** A cancelled order records the reason and `cancelledAt` from an injected `java.time.Clock`.

Out of scope: refunds, notifications, partial cancellation, shipment-based restrictions.

## Confluence Documentation Referenced
None found. Both searches in space `DEMO` (story key; "cancel order reason") returned no pages, so the Jira story is the only spec.

## Acceptance Criteria
| AC | Criterion | Status |
|---|---|---|
| AC-01 | `placeOrder` stores the order with status `PLACED` | ✅ Implemented |
| AC-02 | `cancelOrder` saves status `CANCELLED`, the reason and the clock's instant; returns the updated order | ✅ Implemented |
| AC-03 | Already `CANCELLED` → `IllegalStateException` naming the order id and status; not saved again | ✅ Implemented |
| AC-04 | Unknown order id → `OrderNotFoundException` | ✅ Implemented |
| AC-05 | Null order id, null/blank reason or reason > 255 chars → `IllegalArgumentException`; repository not called | ✅ Implemented |

## Implementation Summary
- New `OrderStatus` enum (`PLACED`, `CANCELLED`).
- `Order` record gains `status`, `cancellationReason` and `cancelledAt`:
  - The existing 3-argument constructor is kept and defaults to `PLACED`, so existing callers compile unchanged.
  - The compact constructor enforces that the cancellation details are set exactly when the status is `CANCELLED`.
  - `cancel(reason, at)` returns a cancelled copy of the order.
- `OrderService` changes:
  - A `java.time.Clock` is injected through a new 3-argument constructor. The existing constructors default to `Clock.systemUTC()`.
  - `cancelOrder(orderId, reason)` validates its input before touching the repository (AC-05), then loads the order (AC-04), rejects a non-`PLACED` order (AC-03) and saves the cancelled copy (AC-02).
  - `placeOrder` now rejects an order that is not `PLACED` (AC-01).

## Changes Made
| File | Change Type | Description |
|---|---|---|
| `src/main/java/com/demo/orders/model/OrderStatus.java:1-11` | New | Lifecycle status enum |
| `src/main/java/com/demo/orders/model/Order.java:17-55` | Modified | New components, invariant, PLACED default, `cancel` |
| `src/main/java/com/demo/orders/service/OrderService.java:30-72` | Modified | `MAX_CANCELLATION_REASON_LENGTH`, `Clock` injection |
| `src/main/java/com/demo/orders/service/OrderService.java:81-91` | Modified | `placeOrder` accepts only `PLACED` orders |
| `src/main/java/com/demo/orders/service/OrderService.java:161-192` | Modified | `cancelOrder` |
| `src/test/java/com/demo/orders/model/OrderTest.java` | New | 6 tests |
| `src/test/java/com/demo/orders/service/OrderServiceTest.java` | Modified | 12 tests added; fixed `Clock` in setup |

## Unit Tests Added
| Test Class | Test Method | What It Covers |
|---|---|---|
| OrderTest | `constructor_whenThreeArguments_defaultsToPlacedWithoutCancellationDetails` | AC-01 |
| OrderTest | `cancel_whenPlaced_returnsCancelledCopyAndLeavesOriginalUnchanged`, `cancel_whenReasonIsNull…`, `cancel_whenTimeIsNull…` | AC-02 |
| OrderTest | `constructor_whenPlacedWithCancellationDetails…`, `constructor_whenStatusIsNull…` | Record invariants |
| OrderServiceTest | `placeOrder_whenNewOrder_storesStatusPlaced`, `placeOrder_whenOrderIsCancelled_throwsIllegalArgumentException` | AC-01 |
| OrderServiceTest | `cancelOrder_whenPlaced_savesCancelledOrderWithReasonAndClockTime`, `cancelOrder_whenReasonIsExactlyMaxLength_cancelsOrder` | AC-02 |
| OrderServiceTest | `cancelOrder_whenAlreadyCancelled_throwsIllegalStateException` | AC-03 |
| OrderServiceTest | `cancelOrder_whenOrderMissing_throwsOrderNotFoundException` | AC-04 |
| OrderServiceTest | `cancelOrder_whenOrderIdIsNull…`, `cancelOrder_whenReasonIsNullOrBlank…` (×4), `cancelOrder_whenReasonTooLong…` | AC-05 |

**Result:** 56/56 passing (18 new, 38 existing). See `reports/unit_test_reports/DEMO-96/test-summary.md`.

## SAST / Quality Gate
- SonarQube result: Not run (`sonar.space` not configured)
- Critical issues resolved: None

## Test Plan
- [x] All ACs produce correct output
- [x] Edge cases handled (null, empty, blank, 255/256-character boundary)
- [x] No regression in related existing functionality (DEMO-95 checkout tests pass)
- [x] Unit tests pass locally

## Implementation Checklist
- [x] Minimal scope — targeted to ACs only
- [x] All new public methods have Javadoc / docstrings
- [x] Inline comments explain non-obvious logic
- [x] Unit tests: happy path + edge cases + negative paths
- [ ] SAST clean (or issues resolved) — not configured
- [x] Jira story updated with PR link

## Reviewer notes
- `placeOrder` now rejects an order whose status isn't `PLACED`. AC-01 requires that placed orders are stored as `PLACED`, and rejecting is the only way to guarantee it without silently rewriting the order.
- `cancelOrder` doesn't check who is cancelling (that the caller owns the order). No AC asks for it; it belongs with authentication, which this service doesn't have yet.
- `checkout` still accepts a `CANCELLED` order. No AC covers this; it's worth a follow-up story.

## Next Step
> Run `pr-review` skill with `DEMO-96` for a structured code review before merging.

🤖 Generated with [Claude Code](https://claude.com/claude-code) — AI Pair Programming Copilot
