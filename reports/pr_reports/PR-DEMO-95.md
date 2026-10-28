# PR Report — DEMO-95

**Date:** 2026-10-28
**Branch:** feature/DEMO-95-apply-tiered-discount-to-order-total
**PR URL:** https://github.com/abhijitbo/feature-dev-demo/pull/1
**Jira Story:** DEMO-95 — Apply tiered discount to order total at checkout
**Jira URL:** https://jira-team-1321.atlassian.net/browse/DEMO-95

---

## Story Summary
Apply an automatic discount at checkout based on order value (volume: 0% / 5% / 10%) and customer loyalty tier (STANDARD 0%, SILVER 2%, GOLD 5%). The two percentages are added and capped at 15%. All money uses `BigDecimal`, rounded HALF_UP to 2 places.

## Confluence Documentation Referenced
- [PRD - E-commerce platform](https://jira-team-1321.atlassian.net/wiki/spaces/DEMO/pages/15073281). §4.4 says the order summary itemizes subtotal and discount. It defines no discount rules.
- No page references DEMO-95 directly.

## Acceptance Criteria Traceability
| AC | Criterion | File | Status |
|---|---|---|---|
| AC-01 | Subtotal < 100.00 + STANDARD → no discount, total = subtotal | `TieredDiscountPolicy.java:54-62` | ✅ Implemented |
| AC-02 | Volume discount 5% (100.00–499.99) / 10% (≥ 500.00) | `TieredDiscountPolicy.java:54-62` | ✅ Implemented |
| AC-03 | Loyalty % (SILVER 2%, GOLD 5%) added, combined ≤ 15% | `TieredDiscountPolicy.java:50-51, 64-70` | ✅ Implemented |
| AC-04 | `CheckoutSummary` exposes subtotal, discountPercent, discountAmount, total — BigDecimal scale 2 HALF_UP | `CheckoutSummary.java`, `OrderService.java:127-137` | ✅ Implemented |
| AC-05 | Null order id / null customer / empty order → `IllegalArgumentException` with message | `OrderService.java:116-125` | ✅ Implemented |

## Implementation Details
| File | Change Type | Description |
|---|---|---|
| `src/main/java/com/demo/orders/discount/DiscountPolicy.java:1-23` | New | Extension point for checkout discount rules |
| `src/main/java/com/demo/orders/discount/TieredDiscountPolicy.java:1-71` | New | BR-1 volume + BR-2 loyalty, BR-3 cap; constants for all thresholds and percentages |
| `src/main/java/com/demo/orders/model/Customer.java:1-18` | New | Customer id + loyalty tier (did not previously exist) |
| `src/main/java/com/demo/orders/model/CustomerTier.java:1-12` | New | STANDARD / SILVER / GOLD |
| `src/main/java/com/demo/orders/model/CheckoutSummary.java:1-26` | New | Checkout result value object |
| `src/main/java/com/demo/orders/service/OrderService.java:30-53` | Modified | Injects `DiscountPolicy`; the existing 1-arg constructor defaults to `TieredDiscountPolicy` |
| `src/main/java/com/demo/orders/service/OrderService.java:102-138` | Modified | New `checkout(String orderId, Customer customer)` |

## Code Documentation Added
| File | Type | Description |
|---|---|---|
| `DiscountPolicy.java` | Javadoc | Interface and `discountPercent` contract |
| `TieredDiscountPolicy.java` | Javadoc | Class-level business rules BR-1 to BR-3, `discountPercent` |
| `Customer.java`, `CustomerTier.java`, `CheckoutSummary.java` | Javadoc | Record/enum and component docs, `@since DEMO-95` |
| `OrderService.java` | Javadoc + inline | New constructor and `checkout`; inline comment on the rounding strategy (BR-4) |

## Unit Tests Added
| Test Class | Test Method | AC Covered |
|---|---|---|
| TieredDiscountPolicyTest | `discountPercent_whenBelowThresholdAndStandard_returnsZero` | AC-01 |
| TieredDiscountPolicyTest | `discountPercent_whenSubtotalIsZero_returnsZero` | AC-01 |
| TieredDiscountPolicyTest | `discountPercent_whenStandardCustomer_returnsVolumePercent` (×5) | AC-02 |
| TieredDiscountPolicyTest | `discountPercent_whenLoyalCustomer_addsLoyaltyToVolumePercent` (×6) | AC-03 |
| TieredDiscountPolicyTest | `discountPercent_whenGoldAndVeryLargeOrder_neverExceedsCap` | AC-03 |
| TieredDiscountPolicyTest | `discountPercent_whenSubtotalIsNull_throwsIllegalArgumentException` | Negative |
| TieredDiscountPolicyTest | `discountPercent_whenSubtotalIsNegative_throwsIllegalArgumentException` | Negative |
| TieredDiscountPolicyTest | `discountPercent_whenCustomerIsNull_throwsIllegalArgumentException` | AC-05 |
| OrderServiceTest | `checkout_whenBelowThresholdAndStandard_appliesNoDiscount` | AC-01 |
| OrderServiceTest | `checkout_whenSubtotalIsZero_returnsZeroTotal` | AC-01 |
| OrderServiceTest | `checkout_whenSubtotalExactly100_appliesFivePercent` | AC-02 |
| OrderServiceTest | `checkout_whenSubtotalJustBelow500_appliesFivePercentRoundedHalfUp` | AC-02, AC-04 |
| OrderServiceTest | `checkout_whenSubtotalExactly500_appliesTenPercent` | AC-02 |
| OrderServiceTest | `checkout_whenSilverCustomer_addsTwoPercentToVolume` | AC-03 |
| OrderServiceTest | `checkout_whenGoldCustomerBelowVolumeThreshold_appliesLoyaltyOnly` | AC-03 |
| OrderServiceTest | `checkout_whenGoldCustomerAndLargeOrder_capsAtFifteenPercent` | AC-03 |
| OrderServiceTest | `checkout_whenCustomPolicy_appliesPolicyPercent` | AC-03 |
| OrderServiceTest | `checkout_whenFractionalPrices_returnsAllValuesAtScaleTwo` | AC-04 |
| OrderServiceTest | `checkout_whenOrderIdIsNull_throwsIllegalArgumentException` | AC-05 |
| OrderServiceTest | `checkout_whenCustomerIsNull_throwsIllegalArgumentException` | AC-05 |
| OrderServiceTest | `checkout_whenOrderHasNoItems_throwsIllegalArgumentException` | AC-05 |
| OrderServiceTest | `checkout_whenOrderMissing_throwsOrderNotFoundException` | AC-05 |

## Test Execution
| Metric | Value |
|---|---|
| Total Tests | 38 (31 new, 7 existing) |
| Passed | 38 |
| Failed | 0 |
| SAST Result | ⚠️ Not run — `sonar.space` not configured |
| Test Report | `reports/unit_test_reports/DEMO-95/test-summary.md` |

## Commits
```
6201b6a feat(DEMO-95): apply tiered volume + loyalty discount at checkout
```
(plus the docs commit that adds this report)

## PR Details
- **Title:** feat(DEMO-95): Apply tiered discount to order total at checkout
- **Base Branch:** main
- **Head Branch:** feature/DEMO-95-apply-tiered-discount-to-order-total
- **URL:** https://github.com/abhijitbo/feature-dev-demo/pull/1

## Jira Update
- Comment added: Yes, two comments. The first covers the implementation; the second has the PR link.
- Status transitioned to: In Progress. The workflow has no "In Review" status (only To Do / In Progress / Done).

## Notes for Reviewers
- `Customer` did not exist in the codebase, so a minimal record was added.
- `checkout` takes an order id, so AC-05's "null order" is enforced as a null `orderId`.
- `checkout` does not check that `order.customerId()` matches `customer.id()`. No AC requires this; it would make a good follow-up story.

## Next Step
Run the `pr-review` skill with `DEMO-95` for a structured code review before merging.
