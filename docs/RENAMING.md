# Sutura naming

Sutura is the ledger-based payments platform. Sentinel is the separate identity
verification and fraud service described in the final-year project proposal.

## Workspace layout

- Rename the outer repository folder to `sutura-server` after closing processes
  that use it. All internal folder names have already been updated.
- Backend: `server/sutura`; Java package: `com.mamadou.sutura`.
- Entry point: `SuturaApplication`; Maven artifact: `sutura`.
- Customer app: `ui/sutura-app`; agent app: `ui/sutura-agent`.
- Configuration properties: `sutura.*`; environment variables: `SUTURA_*`.
- Database default: `sutura`. Existing databases must be renamed or configured
  explicitly using `POSTGRES_DB` and `SPRING_DATASOURCE_URL`.

## External resources and rebuilds

Repository links, Git remotes, API domains, email addresses, and logo resource
URLs now use Sutura. The matching GitHub repository, DNS, mailboxes, and hosted
logo assets must exist under those names before those references work.
This code change does not create or rename resources on external services.

Rebuild applications to refresh compiled output and caches. Reconfigure
external deployment secrets, app-store identifiers, and deep links to match.
Sign in again and regenerate agent QR codes after changing stored session keys
and QR identifiers. Keep the existing Docker Compose project name to reuse its
named database and Redis volumes.
