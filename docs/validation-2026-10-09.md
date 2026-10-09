# Validation — 9 October 2026

This extends the 7 October validation with identity training/inference and
durable cash/provider review. Full PaySim training was deliberately not run.
Only small synthetic fixtures were trained to validate code paths.

## Checks

- Sutura: `./mvnw.cmd -B test` — 100 tests passed on H2 after settlement
  review implementation and service renaming. A later PostgreSQL run includes
  two additional cash-withdrawal/block-payout cases: **102 passed**, no failures or errors.
- PostgreSQL: isolated local PostgreSQL 17, loopback port 15432, validation
  database. `TEST_DATABASE_URL`, `TEST_DATABASE_USER`, `TEST_DATABASE_DRIVER`
  select PostgreSQL for the same test suite. The initial 100 tests passed;
  migration 008 also applied successfully to the existing validation schema.
  The final PostgreSQL run passed all **102 tests** after the final Java changes.
- Transaction Python: `python -m pytest -q` — 14 passed, including training
  roundtrip and SHAP additivity. The real local PaySim CSV was not trained.
- Identity Python: `python -m pytest -q` — 24 passed, including all four network
  heads, OCR backward pass, tiny training roundtrip, subject/image leakage
  rejection, holdout gates, corrupt/nonfinite artifacts, challenge replay and
  replacement of earlier frames with an unchanged final selfie.
- Agent mobile: `npm install --no-audit --no-fund`, `npx tsc --noEmit` — passed.
  Installed the existing declared session-storage dependencies and updated lock.
- Both repositories: `git diff --check` passed (existing line-ending warnings).

## Behaviour checked

Cash review persists before returning to the agent. Matched approval leaves
cash unchanged until the agent completes; rejected operations never post.
Provider deposit review leaves the wallet unchanged until matched approval,
then credits once. Payout review sends no provider request and creates no hold;
approval permits a committed reservation before dispatch. Duplicate completion
and dispatch do not repeat wallet movements. Unknown upstream outcomes retain
holds. Approved payouts have a scheduled recovery worker; unknown processing
outcomes are excluded and need reconciliation.

Identity artifacts are trained locally and loaded only with matching hashes,
compatible weights and passing validation/test counts. Baseline models are not
claimed to achieve production accuracy. Without all accepted identity artifacts,
VERIFIED and elevated wallet limits remain unavailable.

## Remaining acceptance

Train and evaluate PaySim; collect separately labelled identity/attack datasets
and train/evaluate all four identity tasks. Run real ModemPay sandbox checkout,
payout and failure/recovery scenarios with credentials. Test camera permissions
and capture on physical devices. Validate Docker builds and production settings
on a Docker-enabled host. Rejected received deposits and unknown payout outcomes
require provider refund/reconciliation; automatic provider reversals are absent.
See [readiness.md](readiness.md) and [settlement-reviews.md](settlement-reviews.md).
