# Unit Test Summary — DEMO-95

**Story:** DEMO-95 — Apply tiered discount to order total at checkout
**Timestamp:** 2026-10-28T16:48:34+05:30
**Command:** `mvn test --no-transfer-progress` (Java 21, Maven 3.9.6, JUnit 5.11.4, Mockito 5.14.2)
**Raw reports:** `TEST-com.demo.orders.discount.TieredDiscountPolicyTest.xml`, `TEST-com.demo.orders.service.OrderServiceTest.xml`

## Totals

| Suite | Tests | Passed | Failed | Errors | Skipped |
|---|---|---|---|---|---|
| `TieredDiscountPolicyTest` (new) | 17 | 17 | 0 | 0 | 0 |
| `OrderServiceTest` (14 new + 7 existing) | 21 | 21 | 0 | 0 | 0 |
| **Total** | **38** | **38** | **0** | **0** | **0** |

## `src/test/java/com/demo/orders/discount/TieredDiscountPolicyTest.java`

| Test | AC | Result |
|---|---|---|
| `discountPercent_whenBelowThresholdAndStandard_returnsZero` | AC-01 | ✅ Pass |
| `discountPercent_whenSubtotalIsZero_returnsZero` | AC-01 (edge) | ✅ Pass |
| `discountPercent_whenStandardCustomer_returnsVolumePercent` ×5 (99.99, 100.00, 499.99, 500.00, 10000.00) | AC-02 | ✅ Pass |
| `discountPercent_whenLoyalCustomer_addsLoyaltyToVolumePercent` ×6 (SILVER/GOLD × 3 bands) | AC-03 | ✅ Pass |
| `discountPercent_whenGoldAndVeryLargeOrder_neverExceedsCap` | AC-03 | ✅ Pass |
| `discountPercent_whenSubtotalIsNull_throwsIllegalArgumentException` | negative | ✅ Pass |
| `discountPercent_whenSubtotalIsNegative_throwsIllegalArgumentException` | negative | ✅ Pass |
| `discountPercent_whenCustomerIsNull_throwsIllegalArgumentException` | AC-05 | ✅ Pass |

## `src/test/java/com/demo/orders/service/OrderServiceTest.java` (new checkout tests)

| Test | AC | Result |
|---|---|---|
| `checkout_whenBelowThresholdAndStandard_appliesNoDiscount` | AC-01 | ✅ Pass |
| `checkout_whenSubtotalIsZero_returnsZeroTotal` | AC-01 (edge) | ✅ Pass |
| `checkout_whenSubtotalExactly100_appliesFivePercent` | AC-02 | ✅ Pass |
| `checkout_whenSubtotalJustBelow500_appliesFivePercentRoundedHalfUp` | AC-02, AC-04 | ✅ Pass |
| `checkout_whenSubtotalExactly500_appliesTenPercent` | AC-02 | ✅ Pass |
| `checkout_whenSilverCustomer_addsTwoPercentToVolume` | AC-03 | ✅ Pass |
| `checkout_whenGoldCustomerBelowVolumeThreshold_appliesLoyaltyOnly` | AC-03 | ✅ Pass |
| `checkout_whenGoldCustomerAndLargeOrder_capsAtFifteenPercent` | AC-03 | ✅ Pass |
| `checkout_whenCustomPolicy_appliesPolicyPercent` | AC-03 (extension point) | ✅ Pass |
| `checkout_whenFractionalPrices_returnsAllValuesAtScaleTwo` | AC-04 | ✅ Pass |
| `checkout_whenOrderIdIsNull_throwsIllegalArgumentException` | AC-05 | ✅ Pass |
| `checkout_whenCustomerIsNull_throwsIllegalArgumentException` | AC-05 | ✅ Pass |
| `checkout_whenOrderHasNoItems_throwsIllegalArgumentException` | AC-05 | ✅ Pass |
| `checkout_whenOrderMissing_throwsOrderNotFoundException` | AC-05 (related) | ✅ Pass |

The 7 existing `placeOrder` / `getOrder` / `calculateSubtotal` tests still pass (no regression).

## SAST

Not run — `sonar.space` not configured in `.claude/config.yaml`.
