# Validation — 10 October 2026

Sutura and Sentinel remain independent repositories. No live provider credentials
or labelled identity samples were available for this run.

Implemented:
- Agent cash-in/cash-out creation using the registered customer phone, exact decimal
  request amounts, stable retry keys and the existing settlement confirmation API.
  Completion requests are protected against concurrent taps in the agent context.
- Customer and agent support tickets persisted by the API, with an admin queue,
  replies, resolution status, ownership filtering and optimistic update checks.
  The customer list shows its latest 100 tickets; staff queue pages contain 50.
  Staff replies are retrieved on refresh; this is not live chat or email delivery.
- Migration `009_support_tickets.sql` for existing PostgreSQL databases.
- Correct `spring.data.redis` configuration in base and production profiles.
- Sutura web standalone Docker image and optional `web` production Compose profile.
- Separate repository CI workflows for backend tests, ML tests and client builds.
- Sentinel full local Compose app profile, real backend/dashboard Dockerfiles,
  API/WebSocket proxy, persisted uploads and an independent Maven wrapper.
- Production Sentinel dashboard builds do not fall back automatically to demo data.

Validation:
- Sutura: 110 backend tests passed, including support method access, owner filtering,
  stale reply prevention and agent customer lookup tests.
- Sentinel: 17 backend tests passed; 2 live-service tests skipped.
- Python services: 22 transaction-scoring tests and 24 identity-service tests passed.
- Customer, agent and merchant mobile applications: TypeScript checks passed.
- Sutura web production build and Sentinel dashboard production build passed.
- Compose/workflow YAML parsed successfully. Redis nesting checked explicitly.
- Actual Docker image builds and container startup remain untested: Docker is not
  installed in this environment. CI workflows have not yet run on GitHub.

For the Sutura web container, set `NEXT_PUBLIC_API_BASE_URL` to the externally
reachable HTTPS API address, then run from `server`:

```powershell
docker compose -f docker-compose.prod.yml --profile web up --build -d
```

This requires the existing production configuration, database migrations and
credentials. The frontend API address is compiled into the image; rebuild after
changing it. Bind a TLS reverse proxy to the loopback-only web port 3000.

Still required before claiming production readiness: ModemPay sandbox acceptance,
labelled identity model training and evaluation, fraud policy calibration on
representative transactions, deployment verification, and complete device/browser
acceptance scenarios. PaySim accuracy does not certify real payment decisions.
