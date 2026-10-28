## Jira Story
- **ID:** DEMO-95
- **Summary:** Apply tiered discount to order total at checkout
- **Priority:** Medium
- **Story Points:** Not set
- **Assignee:** Unassigned
- **Link:** https://jira-team-1321.atlassian.net/browse/DEMO-95

## Feature Description
Customers currently pay the full order total regardless of order size or loyalty tier. Marketing wants to reward large orders and loyal customers with an automatic discount applied at checkout, so that average order value and repeat-purchase rate increase.

**Business rules**
- **BR-1 Volume** (subtotal before discount): < 100.00 → 0% · 100.00–499.99 → 5% · ≥ 500.00 → 10%
- **BR-2 Loyalty** (`Customer.tier`): STANDARD → 0% · SILVER → 2% · GOLD → 5%
- **BR-3** Volume + loyalty are added; combined discount capped at 15%
- **BR-4** All money is `BigDecimal`, HALF_UP, 2 decimal places

Out of scope: coupons/promo codes, tax & shipping, persisting the discount.

## Confluence Documentation Referenced
- [PRD - E-commerce platform](https://jira-team-1321.atlassian.net/wiki/spaces/DEMO/pages/15073281) — §4.4 Checkout & Payments: order summary itemizes subtotal and discount. No discount rules defined there; the Jira story is the source of truth.

## Acceptance Criteria
| AC | Criterion | Status |
|---|---|---|
| AC-01 | Subtotal < 100.00 + STANDARD → no discount, total = subtotal | ✅ Implemented |
| AC-02 | Volume discount 5% (100.00–499.99) / 10% (≥ 500.00) | ✅ Implemented |
| AC-03 | SILVER/GOLD loyalty % added to volume %, never above 15% | ✅ Implemented |
| AC-04 | Result exposes subtotal, discountPercent, discountAmount, total as BigDecimal scale 2 HALF_UP | ✅ Implemented |
| AC-05 | Null order (id), null customer, or empty order → `IllegalArgumentException` with descriptive message | ✅ Implemented |

## Implementation Summary
- New `com.demo.orders.discount` package with a `DiscountPolicy` interface (so rules can be extended later, per the story's technical notes) and `TieredDiscountPolicy`, which implements BR-1 to BR-3.
- `OrderService#checkout(String orderId, Customer customer)` loads the order, computes the subtotal with the existing `calculateSubtotal`, asks the policy for the percentage and returns a `CheckoutSummary`. The discount amount is rounded once, HALF_UP, to 2 places (BR-4).
- The repo had no `Customer` type, although the story refers to `Customer.tier`. This PR adds a minimal `Customer` record and a `CustomerTier` enum.
- The existing `OrderService(OrderRepository)` constructor is kept and defaults to `TieredDiscountPolicy`. A new 2-arg constructor accepts a custom policy.
- Because `checkout` takes an order **id**, AC-05's "null order" is enforced as a null `orderId`. An unknown id raises the existing `OrderNotFoundException`, as `getOrder` already does.

## Changes Made
| File | Change Type | Description |
|---|---|---|
| `src/main/java/com/demo/orders/discount/DiscountPolicy.java:1-23` | New | Discount extension point |
| `src/main/java/com/demo/orders/discount/TieredDiscountPolicy.java:1-71` | New | BR-1 volume + BR-2 loyalty, BR-3 cap |
| `src/main/java/com/demo/orders/model/Customer.java:1-18` | New | Customer id + tier record |
| `src/main/java/com/demo/orders/model/CustomerTier.java:1-12` | New | STANDARD / SILVER / GOLD |
| `src/main/java/com/demo/orders/model/CheckoutSummary.java:1-26` | New | Checkout result value object |
| `src/main/java/com/demo/orders/service/OrderService.java:30-53, 102-138` | Modified | Policy injection; `checkout` method |
| `src/test/java/com/demo/orders/discount/TieredDiscountPolicyTest.java` | New | 17 tests |
| `src/test/java/com/demo/orders/service/OrderServiceTest.java` | Modified | 14 checkout tests added |

## Unit Tests Added
| Test Class | Test Method | What It Covers |
|---|---|---|
| TieredDiscountPolicyTest | `discountPercent_whenBelowThresholdAndStandard_returnsZero`, `…whenSubtotalIsZero_returnsZero` | AC-01 |
| TieredDiscountPolicyTest | `discountPercent_whenStandardCustomer_returnsVolumePercent` (×5 boundaries) | AC-02 |
| TieredDiscountPolicyTest | `discountPercent_whenLoyalCustomer_addsLoyaltyToVolumePercent` (×6), `…whenGoldAndVeryLargeOrder_neverExceedsCap` | AC-03 |
| TieredDiscountPolicyTest | `…whenSubtotalIsNull…`, `…whenSubtotalIsNegative…`, `…whenCustomerIsNull_throwsIllegalArgumentException` | Negative / AC-05 |
| OrderServiceTest | `checkout_whenBelowThresholdAndStandard_appliesNoDiscount`, `checkout_whenSubtotalIsZero_returnsZeroTotal` | AC-01 |
| OrderServiceTest | `checkout_whenSubtotalExactly100…`, `…JustBelow500…RoundedHalfUp`, `…Exactly500_appliesTenPercent` | AC-02 |
| OrderServiceTest | `checkout_whenSilverCustomer…`, `…GoldCustomerBelowVolumeThreshold…`, `…GoldCustomerAndLargeOrder_capsAtFifteenPercent`, `…whenCustomPolicy_appliesPolicyPercent` | AC-03 |
| OrderServiceTest | `checkout_whenFractionalPrices_returnsAllValuesAtScaleTwo` | AC-04 |
| OrderServiceTest | `checkout_whenOrderIdIsNull…`, `…whenCustomerIsNull…`, `…whenOrderHasNoItems…`, `…whenOrderMissing_throwsOrderNotFoundException` | AC-05 |

**Result:** 38/38 passing (31 new, 7 existing). See `reports/unit_test_reports/DEMO-95/test-summary.md`.

## SAST / Quality Gate
- SonarQube result: Not run (`sonar.space` not configured)
- Critical issues resolved: None

## Test Plan
- [x] All ACs produce correct output
- [x] Edge cases handled (null, empty, boundary values)
- [x] No regression in related existing functionality
- [x] Unit tests pass locally

## Implementation Checklist
- [x] Minimal scope — targeted to ACs only
- [x] All new public methods have Javadoc / docstrings
- [x] Inline comments explain non-obvious logic
- [x] Unit tests: happy path + edge cases + negative paths
- [ ] SAST clean (or issues resolved) — not configured
- [x] Jira story updated with PR link

## Reviewer notes
- `checkout` does not check that `order.customerId()` matches `customer.id()`. No AC requires it, but it is worth a follow-up story.
- With the current rules the maximum combined discount is exactly 15% (10% + 5%), so the cap can't be exceeded yet. It is enforced in the policy so that future rule changes stay bounded.

## Next Step
> Run `pr-review` skill with `DEMO-95` for a structured code review before merging.

🤖 Generated with [Claude Code](https://claude.com/claude-code)
