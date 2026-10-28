## Jira Story
- **ID:** DEMO-97
- **Summary:** Block checkout of a cancelled order
- **Priority:** Medium
- **Story Points:** Not set
- **Assignee:** Unassigned
- **Link:** https://jira-team-1321.atlassian.net/browse/DEMO-97

## Feature Description
DEMO-96 lets customers cancel a placed order, but `OrderService#checkout` still prices a cancelled order as if it were live. A customer (or a retried client request) could therefore check out an order that has already been cancelled, which would lead to charging for an order the customer no longer wants.

**Business rules**
- **BR-1** Only an order with status `PLACED` can be checked out.
- **BR-2** Checkout of an order with status `CANCELLED` is rejected before any pricing or discount calculation runs.

Out of scope: new order statuses, changes to cancellation (DEMO-96), refunds.

## Confluence Documentation Referenced
None found. Both searches in space `DEMO` (`text ~ "DEMO-97"`, `text ~ "checkout cancelled order"`) returned no pages, so the Jira story is the only spec.

## Acceptance Criteria
| AC | Criterion | Status |
|---|---|---|
| AC-01 | `CANCELLED` order → `IllegalStateException` whose message names the order id and status `CANCELLED` | ✅ Implemented |
| AC-02 | `CANCELLED` order → `DiscountPolicy` not called | ✅ Implemented |
| AC-03 | `PLACED` order → result unchanged (existing DEMO-95 checkout tests still pass) | ✅ Implemented |

## Implementation Summary
- `OrderService#checkout` gains a status guard right after `getOrder` and before the empty-items check and pricing.
- The guard is `status != PLACED`. It throws `IllegalStateException("order <id> cannot be checked out because its status is <status>")`.
- The Javadoc gains an `@throws IllegalStateException` line.
- Nothing else in production code changed.

## Changes Made
| File | Change Type | Description |
|---|---|---|
| `src/main/java/com/demo/orders/service/OrderService.java:135` | Modified | Javadoc `@throws IllegalStateException` on `checkout` |
| `src/main/java/com/demo/orders/service/OrderService.java:146-150` | Modified | Status guard before the empty-items check and pricing |
| `src/test/java/com/demo/orders/service/OrderServiceTest.java:62` | Modified | DEMO-97 block in class Javadoc; imports for `DiscountPolicy` and `Mockito.mock` |
| `src/test/java/com/demo/orders/service/OrderServiceTest.java:365-408` | Modified | 4 tests added |

## Unit Tests Added
| Test Class | Test Method | What It Covers |
|---|---|---|
| OrderServiceTest | `checkout_whenOrderIsCancelled_throwsIllegalStateException` | AC-01 |
| OrderServiceTest | `checkout_whenCancelledOrderHasNoItems_throwsIllegalStateException` | AC-01 (status check runs before the empty-items check) |
| OrderServiceTest | `checkout_whenOrderIsCancelled_doesNotCallDiscountPolicy` | AC-02 (mocked `DiscountPolicy`, `verifyNoInteractions`) |
| OrderServiceTest | `checkout_whenOrderIsPlaced_appliesDiscountAsBefore` | AC-03 |

**Result:** 60/60 passing (4 new, 56 existing). See `reports/unit_test_reports/DEMO-97/test-summary.md`.

## SAST / Quality Gate
- SonarQube result: Not run (`sonar.space` not configured)
- Critical issues resolved: None

## Test Plan
- [x] All ACs produce correct output
- [x] Edge cases handled (cancelled order with no items)
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
Pre-PR review gate ran with no Critical or High findings and 0 fix rounds. Low findings:
- The `checkout` Javadoc keeps `@since DEMO-95`. The new `IllegalStateException` is documented via `@throws` only (repo convention: one `@since` per member).
- The AC-02 test uses a Mockito mock of `DiscountPolicy`, while the other checkout tests use the real `TieredDiscountPolicy`. This is deliberate, so the test can verify no interaction.
- The guard is `status != PLACED`, so any status added later is blocked at checkout by default (safe, per BR-1).

## Next Step
> Run `pr-review` skill with `DEMO-97` for a structured code review before merging.

🤖 Generated with [Claude Code](https://claude.com/claude-code) — AI Pair Programming Copilot
