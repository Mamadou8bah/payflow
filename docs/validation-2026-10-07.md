# Local validation — 7 October 2026

Checks used isolated local PostgreSQL 17 databases on port 15432. No live payment
was sent. Virtual cards are excluded.

| Check | Result |
| --- | --- |
| Sutura backend | 95 tests passed on PostgreSQL and again on default H2; logs in `server/sutura/target/*readiness-check.log` |
| Sentinel backend | 17 tests passed, including actual Python HTTP calls |
| Transaction Python service | 14 tests passed |
| Identity evidence Python service | 12 tests passed |
| Customer web and mobile TypeScript | Passed |
| Sentinel production web build | Passed; existing bundle-size warning |
| Sutura production web build | Passed with its required HTTPS API URL setting |
| Merchant checkout runtime | HTTP 200; checkout heading rendered |
| Sutura migrations 001–007 | Applied successfully on the isolated schema |

The web build used `https://api.sutura.example` as a build-time setting. This is
not a deployed backend. Docker is unavailable in this environment.
Temporary PostgreSQL, Python and web validation processes were stopped after
the checks. The isolated database files remain under `.local-validation` in the
workspace for inspection.

## Repeat checks

Run `./mvnw.cmd -B test` from `sutura/server/sutura`. The test profile defaults to
H2. To run against PostgreSQL, set `TEST_DATABASE_URL`, `TEST_DATABASE_USER` and
`TEST_DATABASE_DRIVER` to your isolated test database. Local checks used
`jdbc:postgresql://127.0.0.1:15432/sutura_validation`, user `validation`, and driver
`org.postgresql.Driver`.

Run `python -m pytest -q` from each Python service directory. Run `npm run build`
from each web directory, setting `NEXT_PUBLIC_API_BASE_URL` to the actual HTTPS
API address for Sutura. Run `npx tsc --noEmit` from the customer mobile directory.
Each Python service README includes start and training commands.

## Synthetic evaluation

The amount/time baseline used 16,000 training and 4,000 held-out synthetic rows,
seed 42. Precision was 0.52778, recall 0.18905, false-positive rate 0.00895 and
average precision 0.39377. Training writes the full provenance and evaluation
report beside the ignored local model artifact.

These are model-only synthetic results, not PaySim or production measurements.
Low recall remains a material limitation. Live inference returned actual SHAP
attributions over amount/time features; rules are identified separately.

## Remaining acceptance work

Identity verification needs evaluated OCR, face, anti-spoof and tampering models
and labelled samples. The evidence service intentionally cannot grant VERIFIED.
PaySim data was unavailable. ModemPay needs sandbox acceptance with configured
credentials. Cash/provider fraud review still fails closed without the durable
resumption implemented for transfers and merchant collections. Camera/device
flows, deployment, provider recovery and broader role-based browser acceptance
remain unverified. See [readiness.md](readiness.md).
