# Settlement review

Migration 008 adds the actual customer and scored external reference to each
product transaction. Signed decisions must match the case, Sentinel transaction,
customer and `settlement:<reference>` before changing a payment. The domain
operation is locked before its product transaction to match settlement lock order.
Receipts and decisions commit together; duplicate decisions cannot post twice.

Cash intents are screened when the agent attempts completion. Review persists
as PENDING_REVIEW; blocked/unavailable scoring persists FAILED. APPROVE changes
the operation to APPROVED and starts a 15 minute completion window. The agent
must look up the reference again and complete it before exchanging cash. Approval
callbacks never exchange cash. A new operation is required after approval expiry.

Provider deposits are screened after a matched successful provider callback.
Review stores the provider identifier and leaves funds uncredited. APPROVE
credits the original deposit once, under wallet/status/limit checks. A rejected
received deposit remains uncredited with its provider identifier for refund or
reconciliation. Automatic provider refunds/reversals are not implemented.

Provider payouts are screened before a hold or upstream call. Review creates
neither. APPROVE marks the payout APPROVED; the recovery worker checks approved
payouts every 10 seconds, reserves funds in a committed transaction, then dispatches
with the original stable provider key. Wallet status, available balance and limits
are rechecked before reserving. REJECT marks FAILED without debiting the wallet.

`SUTURA_PAYMENTS_APPROVED_PAYOUT_RECOVERY_ENABLED=false` disables the worker;
`sutura.payments.approved-payout-recovery-delay-ms` changes its delay. Multiple
workers lock the operation before dispatch, and existing provider IDs prevent
redispatch. A provider timeout keeps the reservation and requires reconciliation;
the worker deliberately selects APPROVED only, never unknown PROCESSING results.
A crash between reservation and provider dispatch also needs reconciliation or a
retry of the original create request using its original idempotency key. Never
release a hold or issue a new payout key without confirming the provider outcome.

Real provider sandbox acceptance and recovery drills remain required. Local
signed fixtures validate database behaviour, not external provider guarantees.
