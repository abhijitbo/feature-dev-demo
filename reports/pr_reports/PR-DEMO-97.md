# PR Report — DEMO-97

**Date:** 2026-10-28
**Branch:** feature/DEMO-97-block-checkout-of-a-cancelled-order
**PR URL:** https://github.com/abhijitbo/feature-dev-demo/pull/3
**Jira Story:** DEMO-97 — Block checkout of a cancelled order
**Jira URL:** https://jira-team-1321.atlassian.net/browse/DEMO-97

---

## Story Summary
`OrderService#checkout` must not price a cancelled order:
- **BR-1:** only an order with status `PLACED` can be checked out.
- **BR-2:** checkout of a `CANCELLED` order is rejected before any pricing or discount calculation runs.

Out of scope: new order statuses, changes to cancellation (DEMO-96), refunds.

## Confluence Documentation Referenced
None. Searches in space `DEMO` for `text ~ "DEMO-97"` and `text ~ "checkout cancelled order"` returned no pages.

## Acceptance Criteria Traceability
| AC | Criterion | File | Status |
|---|---|---|---|
| AC-01 | `CANCELLED` order → `IllegalStateException` naming order id and status | `OrderService.java:146-150` | ✅ Implemented |
| AC-02 | `CANCELLED` order → `DiscountPolicy` not called | `OrderService.java:146-150` | ✅ Implemented |
| AC-03 | `PLACED` order → result unchanged (DEMO-95 checkout tests still pass) | `OrderService.java:146-150` (guard passes through) | ✅ Implemented |

## Implementation Details
| File | Change Type | Description |
|---|---|---|
| `src/main/java/com/demo/orders/service/OrderService.java:135` | Modified | Javadoc `@throws IllegalStateException` on `checkout` |
| `src/main/java/com/demo/orders/service/OrderService.java:146-150` | Modified | Status guard after `getOrder`, before the empty-items check and pricing; throws `IllegalStateException("order <id> cannot be checked out because its status is <status>")` |
| `src/test/java/com/demo/orders/service/OrderServiceTest.java:62` | Modified | DEMO-97 block in class Javadoc; imports for `DiscountPolicy` and `Mockito.mock` |
| `src/test/java/com/demo/orders/service/OrderServiceTest.java:365-408` | Modified | 4 tests added |

## Code Documentation Added
| File | Type | Description |
|---|---|---|
| `OrderService.java` | Javadoc | `@throws IllegalStateException` on `checkout` (line 135) |
| `OrderService.java` | Inline | Comment on the status guard (line 146) explaining BR-1 and BR-2 |
| `OrderServiceTest.java` | Javadoc | DEMO-97 block in the class Javadoc (line 62) |

## Unit Tests Added
| Test Class | Test Method | AC Covered |
|---|---|---|
| OrderServiceTest | `checkout_whenOrderIsCancelled_throwsIllegalStateException` | AC-01 |
| OrderServiceTest | `checkout_whenCancelledOrderHasNoItems_throwsIllegalStateException` | AC-01 (status check runs before the empty-items check) |
| OrderServiceTest | `checkout_whenOrderIsCancelled_doesNotCallDiscountPolicy` | AC-02 |
| OrderServiceTest | `checkout_whenOrderIsPlaced_appliesDiscountAsBefore` | AC-03 |

## Test Execution
| Metric | Value |
|---|---|
| Total Tests | 60 (4 new, 56 existing) |
| Passed | 60 |
| Failed | 0 |
| SAST Result | ⚠️ Not run — `sonar.space` not configured |
| Test Report | `reports/unit_test_reports/DEMO-97/test-summary.md` |

## Commits
```
69b4a93 feat(DEMO-97): block checkout of a cancelled order
```
(plus the docs commit that adds this report)

## PR Details
- **Title:** feat(DEMO-97): Block checkout of a cancelled order
- **Base Branch:** main
- **Head Branch:** feature/DEMO-97-block-checkout-of-a-cancelled-order
- **URL:** https://github.com/abhijitbo/feature-dev-demo/pull/3

## Jira Update
- Comment added: Yes, with the PR link, branch, implementation summary and test results
- Status transitioned to: In Progress (the workflow has no "In Review" or "Code Review" status; "Done" was not selected)

## Review Gate (Step 9.5)
- Ran: yes. No Critical or High findings. 0 fix rounds.
- Low notes:
  - The `checkout` Javadoc keeps `@since DEMO-95`. The new `IllegalStateException` is documented via `@throws` only (repo convention: one `@since` per member).
  - The AC-02 test uses a Mockito mock of `DiscountPolicy`, while the other checkout tests use the real `TieredDiscountPolicy`. This is deliberate, so the test can verify no interaction.
  - The guard is `status != PLACED`, so any status added later is blocked at checkout by default (safe, per BR-1).

## Notes for Reviewers
- **Guard placement:** the status check runs before the empty-items check, so a cancelled order with no items reports the status error.
- **Scope:** only `checkout` changed in production code. `cancelOrder` and `placeOrder` from DEMO-96 are untouched.

## Next Step
> Run `pr-review` skill with `DEMO-97` for a structured code review before merging.

## Model Usage

| Group | Steps | Tier | Model | Escalated | Status |
|---|---|---|---|---|---|
| — | orchestrator | session | Claude Opus 5.5 (session not started with `/model sonnet`) | — | — |
| G1 | P1–P7, 0, 1, 2 | cheap | Claude Haiku 5.5 | no | ok |
| G2 | 3, 3.5 | mid | Claude Sonnet 5.5 | no | ok |
| G3 | plan_gate (5a) | mid | Claude Sonnet 5.5 | no | ok |
| G4 | 4 | cheap | Claude Haiku 5.5 | no | ok |
| G5 | 5 | mid | Claude Sonnet 5.5 | no | ok |
| G6 | 6, 7 (7_sql_scan skipped: no SQL) | mid | Claude Sonnet 5.5 | no | ok |
| G7 | 8, 9 | cheap | Claude Haiku 5.5 | no | ok |
| G8 | 9.5 | mid | Claude Sonnet 5.5 | no | ok |
| G9 | 10_pr_body | mid | Claude Sonnet 5.5 | no | ok |
| G10 | 10_pr_create, 11, 12 | cheap | Claude Haiku 5.5 | no | ok |
| G11 | 13 | cheap | Claude Haiku 5.5 | no | runs after this report |

Complexity S, no SQL, no signals → no escalation to premium. `10a` (Azure DevOps AB# check) not applicable.
