# SuturaÃ¢â‚¬â€œSentinel scoring contract

Sutura calls Sentinel's Java API. Sentinel calls inference services and owns its
cases. Only Sutura posts money to its ledger.

## Configuration

Set `SUTURA_SENTINEL_BASE_URL` to the Sentinel API origin (normally
`http://localhost:8080` for direct local runs), and `SUTURA_SENTINEL_API_KEY` to
the API key issued for Sutura's Sentinel tenant. Do not use a staff JWT or expose
this key to mobile/browser clients. For Docker Desktop talking to a host API,
the example origin is `http://host.docker.internal:8080`; other environments
need an address reachable from the Sutura container.

The previous direct-Python fraud URL/key and fail-open/review bypass settings
are retired. Set the two Sentinel variables in existing private `.env` files
and external deployment secrets. Sentinel runs separately from the Sutura
production Compose file.

## Request and response

`POST /api/integration/transactions/score`, with `X-Api-Key` and JSON:

```json
{
  "externalCustomerId": "7",
  "externalTransactionId": "transfer-42",
  "amount": 100.00,
  "currency": "GMD",
  "timestamp": "2026-10-04T10:00:00Z",
  "channel": "api",
  "location": "GM",
  "counterparty": null
}
```

Customer IDs currently map to the Sutura user ID as a string. Sentinel requires
that customer to exist in the calling tenant: its KYC completion currently
creates/updates the customer. Sutura now provides hosted KYC through `/api/v1/kyc/sessions`;
a missing Sentinel customer fails closed instead of being silently registered
by the scoring client. Actual transfer counterparty context is a follow-up.

The client requires matching external customer/transaction IDs, a nonempty
Sentinel transaction ID, a finite anomaly score in `[0,1]`, an explanation and
one of `ALLOW`, `REVIEW`, `BLOCK`. It adapts these to the existing Sutura fraud
decision DTO; `features` stores provider, Sentinel transaction/case references
and any model attribution supplied by Sentinel. Local admin fraud labels remain
local audit annotations; Sentinel staff resolve Sentinel cases.

| Result | Transfer behaviour |
| --- | --- |
| Allow | Post synchronously and complete under one reserved ledger trace |
| Review | Persist pending with a linked case; no money moves until signed approval |
| Block | Persist failed; no posting |
| Timeout, authentication failure or invalid response | Persist failed; no posting and no automatic rescore |

Transfers create the product record before scoring. Posting, limit usage, and
completion share its database transaction. Review does not reserve funds: an
approval rechecks current balance, active wallets, currency and limits. Failed
rechecks mark the transfer failed. Request keys are scoped to the authenticated
user; retries with changed request details are rejected. Sentinel also rejects
reusing a tenant's external transaction reference with changed payment details.

Existing deposit/withdrawal gates still run at initiation. Their settlement-time
decisions and durable review paths remain follow-up work. Execution errors can
still roll back a transfer's evaluation record; this is not a complete durable
failed-attempt audit for all exception paths.

## Signed callbacks

Configure the Sutura Sentinel tenant using the tenant administrator's
`PUT /api/admin/settings/webhook` with `webhookUrl` pointing to Sutura's
`/api/v1/webhooks/sentinel` and `signingSecret` containing a random secret of
32–256 characters. Configure that same secret as
`SUTURA_SENTINEL_WEBHOOK_SECRET` in Sutura and set
`SUTURA_SENTINEL_TENANT_CODE` to the tenant's code. The signing secret is separate
from its API key and must stay server-side. Omitting `signingSecret` in later
settings updates retains the existing secret.

The payload contains `eventId`, `tenantCode`, and `event`. Requests carry:

- `X-Sentinel-Event-Id`: the matching payload event ID.
- `X-Sentinel-Timestamp`: decimal Unix seconds; Sutura allows 300 seconds of skew.
- `X-Sentinel-Signature`: lowercase hex HMAC-SHA256 of the UTF-8 timestamp,
  a literal period, and the exact raw request body bytes.

Sentinel stores events in its database transaction and dispatches committed rows
every five seconds. Acknowledgement marks delivery complete; failures retry with
backoff up to one hour, retaining the event ID/body and using a fresh timestamp.
Events are enqueued only when the tenant has a webhook URL configured. This is
at-least-once delivery; Sutura stores the payload hash with each processed event.
An identical replay is acknowledged; reusing an ID with another body is rejected.
Receipt and domain updates commit together.

Transfer `CASE_DECIDED` events must match case ID, Sentinel transaction ID,
external transaction reference and initiating customer. `APPROVE` revalidates
and executes a pending review; `REJECT` fails it; `ESCALATE` keeps it pending.
Terminal transfers cannot execute again. Sentinel serializes decisions and
refuses changes to resolved cases. KYC callbacks cause an authenticated status
fetch from Sentinel rather than trusting verification scores in the event body.

## Database upgrade

Existing PostgreSQL deployments must manually apply Sutura migrations `001`
and `002` under `server/sutura/db/migrations` and Sentinel's
`backend/db/migrations/001_signed_webhook_outbox.sql`. There is no automatic
migration runner. Local `ddl-auto=update` can create schema additions, but the
SQL also reserves historical ledger traces. The unique Sentinel reference index
requires reconciling any historical duplicate references before applying it.
These migrations have not been applied to a live PostgreSQL database.

## Local verification

From `server/sutura`, run:

```powershell
.\mvnw.cmd -B '-Dtest=FraudDetectionClientTest,FraudDetectionServiceTest,KycServiceTest,SentinelKycClientTest,WalletPrivilegeTest,TransferComplianceTest,SentinelCallbackTest,LedgerReservationTest,LedgerPostingValidationTest' test
```

Tests use local HTTP servers, domain mocks, and an isolated H2 database for
concurrent trace reservation/rollback. They do not demonstrate a running
PostgreSQL/Redis/two-system deployment or all settled payment scenarios.
The broader proposal review is in
`../sentinel/docs/proposal/implementation-review.md` from the repository root.

Hosted-KYC start/status/refresh APIs and the web verification screen are
documented in [kyc-onboarding.md](kyc-onboarding.md). Mobile registration routes
to verification. Elevated wallet tiers/caps require a stored VERIFIED result;
verification does not automatically increase limits.
