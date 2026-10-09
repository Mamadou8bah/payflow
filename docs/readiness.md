# Readiness checklist

Updated 9 October 2026. Virtual cards are excluded by the user's explicit scope
decision; the Sentinel LaTeX proposal has been updated accordingly.

## Implemented and checked

- Hosted KYC start/status/refresh, web/mobile entry points, signed callback refresh.
- Verified-only elevated wallet tiers/caps; synthetic evidence cannot verify.
- Durable transfer allow/review/block, signed matched review resolution and retries.
- Unique ledger trace reservation, balanced positive postings and synchronous transfer execution.
- PostgreSQL transfer tests: concurrent spending, idempotent replay, changed-key rejection,
  review approval, frozen-wallet approval failure, rollback after posting and safe retry.
- Sutura startup fixes: distinct idempotency beans, one executor configuration,
  security dependency cycle removal, cache auto-configuration, reserved column and index names.
- Deposit/withdrawal lifecycle timestamps now call their inherited audit callbacks.
- Agent cash completion locks the operation and wallet, checks settlement-time
  scoring, updates the pending product transaction and records its ledger trace.
- Deposit/withdrawal request keys are scoped to actor and operation; fingerprints
  bind the payment details, and mismatched currencies are refused.
- Provider deposits credit the ledger and complete the original pending transaction atomically.
- Payout funds are reserved before contacting the provider; success settles the hold,
  failure releases it once, and unknown outcomes retain the hold for reconciliation.
- Provider callbacks authenticate raw JSON with HMAC-SHA512, validate stored payment
  details, accept the documented payload/transfer events, and reject mismatched replays.
- Provider HTTP requests use payment intents/transfers with stable upstream keys,
  bounded timeouts and internal references in metadata. Sandbox acceptance remains required.
- Customer web payouts select Wave/Afrimoney and collect a phone number; hosted
  checkout opens on web/mobile, and agent requests stay in the internal cash flow.
- Single-use merchant checkout at `/pay/<reference>` runs through the durable
  transfer/review path; merchant ownership, currency and expiry are enforced.
- Reversals lock the original payment, scope retry keys to the actor and enforce
  ownership/admin access before returning a receipt. Concurrent refund tests pass.
- Transfers, reversals and payout holds/releases now record wallet-history rows
  in the ledger transaction; reconciliation reads and writes stay in one transaction.
- Money inputs are bounded and reject excess precision before database rounding.
- Runnable Python transaction scoring and image validation/challenge services.
- A trained synthetic amount/time baseline with real SHAP explanations, a held-out
  evaluation report and a caller-supplied PaySim CSV training path.
- Sentinel integration checks with PostgreSQL and actual Python HTTP endpoints.

## Still required before claiming complete readiness

1. **Identity training and evaluation.** Training, manifest validation, four
   PyTorch baseline networks, artifact loading and inference are implemented.
   See `sentinel/services/cv-ml/README.md`. No evaluated identity weights are
   supplied. Separate labelled identity/attack data is still needed; PaySim
   cannot train face, OCR, tampering or anti-spoof models. Verification stays
   unavailable until all four artifacts meet independent holdout gates.
2. **Payment data evaluation.** PaySim is now available locally at
   `sentinel/services/transaction-ml/data/paysim.csv`. Training has not been run
   on it. Synthetic checks demonstrate plumbing only; evaluate real holdout
   metrics and assess the limited amount/time inputs before relying on scores.
3. **Provider acceptance and recovery.** The adapter now follows the documented
   payment-intent, transfer and signed webhook formats. It still needs sandbox
   credentials and an actual checkout/payout acceptance run. Historical payouts
   without a hold, provider reversals and unresolved timeouts require explicit
   reconciliation; never automatically refund an unknown outcome.
4. **Settlement review acceptance.** Cash and provider requests now persist
   review/block decisions. Matched deposit approval credits once; payout
   approval waits for the recovery worker to reserve funds before dispatch.
   Cash approval still requires an agent action within 15 minutes. Received
   provider deposits rejected by review remain uncredited and need provider
   refund/reconciliation; automatic provider reversals are not implemented.
5. **Live cross-product demonstration.** Registration, role restrictions, merchant
   collections, reversals/reconciliation, native camera permissions and failure
   recovery still need complete scenario coverage. Existing client build checks
   do not replace device/browser and two-application acceptance tests.
6. **Deployment.** Apply the manual migrations, configure independent credentials,
   and validate the intended PostgreSQL/Redis deployment. Docker builds/Compose
   execution have not been tested because Docker is unavailable in this shell.

## Database upgrade

Apply `server/sutura/db/migrations/001` through `008` in order on an existing
Sutura PostgreSQL schema. Reconcile historical duplicate non-null payment keys
before adding the new unique indexes. New scoped keys are different from old
global keys; drain old in-flight requests before switching versions. Do not
reissue an old provider request under a new key without reconciliation.

Sentinel has a separate outbox migration in
`sentinel/backend/db/migrations/001_signed_webhook_outbox.sql` from the workspace
root. Fresh schemas are created by Hibernate locally; that is not an automatic
production migration strategy.

## Provider references

The adapter contract was checked against ModemPay's official documentation on
7 October 2026: [webhooks](https://docs.modempay.com/documentation/core/webhooks),
[payment intents](https://docs.modempay.com/documentation/payment-intents/create),
and [mobile payouts](https://docs.modempay.com/documentation/payouts/mobile-money).
Use `x-modem-signature` (or the explicitly configured signature header).
Callback metadata must preserve `sutura_reference`; unknown references never
create a payment. Local signed fixture tests are not a live provider acceptance run.

## Local verification

See [validation-2026-10-09.md](validation-2026-10-09.md) for the latest commands,
results and remaining acceptance work. These changes are local and uncommitted.
