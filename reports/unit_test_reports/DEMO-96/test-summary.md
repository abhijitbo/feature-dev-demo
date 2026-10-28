# Unit Test Summary — DEMO-96

**Story:** DEMO-96 — Allow customers to cancel a placed order with a reason
**Timestamp:** 2026-10-28T19:18:06+05:30
**Command:** `mvn test --no-transfer-progress` (full suite; Java 21, Maven 3.9.6, JUnit 5.11.4, Mockito 5.14.2)
**Raw reports:** `TEST-com.demo.orders.model.OrderTest.xml`, `TEST-com.demo.orders.service.OrderServiceTest.xml`, `TEST-com.demo.orders.discount.TieredDiscountPolicyTest.xml`

## Totals

| Suite | Tests | Passed | Failed | Errors | Skipped |
|---|---|---|---|---|---|
| `OrderTest` (new) | 6 | 6 | 0 | 0 | 0 |
| `OrderServiceTest` (12 new + 21 existing) | 33 | 33 | 0 | 0 | 0 |
| `TieredDiscountPolicyTest` (existing, regression) | 17 | 17 | 0 | 0 | 0 |
| **Total** | **56** | **56** | **0** | **0** | **0** |

## `src/test/java/com/demo/orders/model/OrderTest.java` (new)

| Test | AC | Result |
|---|---|---|
| `constructor_whenThreeArguments_defaultsToPlacedWithoutCancellationDetails` | AC-01 | ✅ Pass |
| `cancel_whenPlaced_returnsCancelledCopyAndLeavesOriginalUnchanged` | AC-02 | ✅ Pass |
| `cancel_whenReasonIsNull_throwsIllegalArgumentException` | AC-02 (negative) | ✅ Pass |
| `cancel_whenTimeIsNull_throwsIllegalArgumentException` | AC-02 (negative) | ✅ Pass |
| `constructor_whenPlacedWithCancellationDetails_throwsIllegalArgumentException` | Invariant | ✅ Pass |
| `constructor_whenStatusIsNull_throwsNullPointerException` | Invariant | ✅ Pass |

## `src/test/java/com/demo/orders/service/OrderServiceTest.java` (new tests)

| Test | AC | Result |
|---|---|---|
| `placeOrder_whenNewOrder_storesStatusPlaced` | AC-01 | ✅ Pass |
| `placeOrder_whenOrderIsCancelled_throwsIllegalArgumentException` | AC-01 (negative) | ✅ Pass |
| `cancelOrder_whenPlaced_savesCancelledOrderWithReasonAndClockTime` | AC-02 | ✅ Pass |
| `cancelOrder_whenReasonIsExactlyMaxLength_cancelsOrder` | AC-02 / AC-05 (boundary) | ✅ Pass |
| `cancelOrder_whenAlreadyCancelled_throwsIllegalStateException` | AC-03 | ✅ Pass |
| `cancelOrder_whenOrderMissing_throwsOrderNotFoundException` | AC-04 | ✅ Pass |
| `cancelOrder_whenOrderIdIsNull_throwsIllegalArgumentException` | AC-05 | ✅ Pass |
| `cancelOrder_whenReasonIsNullOrBlank_throwsIllegalArgumentException` ×4 (null, "", spaces, tab/newline) | AC-05 | ✅ Pass |
| `cancelOrder_whenReasonTooLong_throwsIllegalArgumentException` (256 chars) | AC-05 | ✅ Pass |

All 38 tests that existed before this story still pass, including the DEMO-95 checkout and discount tests.

## SAST

Not run — `sonar.space` not configured in `.claude/config.yaml`.
