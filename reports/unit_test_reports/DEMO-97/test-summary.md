# Unit Test Summary — DEMO-97

**Story:** DEMO-97 — Block checkout of a cancelled order
**Timestamp:** 2026-10-28T19:41:05+05:30
**Command:** `mvn test --no-transfer-progress` (full suite; Java 21, Maven 3.9.6, JUnit 5.11.4, Mockito 5.14.2)
**Raw reports:** `TEST-com.demo.orders.model.OrderTest.xml`, `TEST-com.demo.orders.service.OrderServiceTest.xml`, `TEST-com.demo.orders.discount.TieredDiscountPolicyTest.xml`

## Totals

| Suite | Tests | Passed | Failed | Errors | Skipped |
|---|---|---|---|---|---|
| `OrderServiceTest` (4 new + 33 existing) | 37 | 37 | 0 | 0 | 0 |
| `OrderTest` (existing, regression) | 6 | 6 | 0 | 0 | 0 |
| `TieredDiscountPolicyTest` (existing, regression) | 17 | 17 | 0 | 0 | 0 |
| **Total** | **60** | **60** | **0** | **0** | **0** |

## `src/test/java/com/demo/orders/service/OrderServiceTest.java` (new tests)

| Test | AC | Result |
|---|---|---|
| `checkout_whenOrderIsCancelled_throwsIllegalStateException` | AC-01 (negative) | ✅ Pass |
| `checkout_whenCancelledOrderHasNoItems_throwsIllegalStateException` | AC-01 (edge) | ✅ Pass |
| `checkout_whenOrderIsCancelled_doesNotCallDiscountPolicy` | AC-02 | ✅ Pass |
| `checkout_whenOrderIsPlaced_appliesDiscountAsBefore` | AC-03 (happy) | ✅ Pass |

All 56 tests that existed before this story still pass, including the DEMO-95 checkout and discount tests (AC-03 regression).

## SAST

Not run — `sonar.space` not configured in `.claude/config.yaml`.
