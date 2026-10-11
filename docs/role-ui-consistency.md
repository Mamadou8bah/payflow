# Merchant and agent UI alignment

The customer app is the visual reference. Agent and merchant clients now use the
same Space Grotesk typography, neutral white and charcoal surfaces, Sutura orange,
8px primary button corners, 12–16px panels, focused input borders, compact brand
headers and theme-aware text/status colors.

Both clients support persisted Light, Dark and System modes from Settings.
Appearance controls are absent from login. Each standalone app owns its own
preference key. Native status bars and web page backgrounds follow the selection.

Agent changes include a flat bottom navigation matching the customer layout,
a neutral session summary instead of gradient cards, Cash-in/Cash-out action
tiles, flat operation rows, matching Activity/Queue empty states, a Settings entry
in More, and the shared login form style with password visibility. Cash forms
retain their existing submit flow and idempotency keys while constraining phone
and amount drafts to the same formats as the customer app. Session summary counts
are displayed without combining cash values from different currencies.

Merchant changes include a compact dashboard header with Settings, neutral
balance/link summary, owner verification row, matching welcome/login and
registration typography, orange registration progress, password visibility and
theme-aware verification/pending screens. Balance overview now includes only
active wallets of its displayed currency, rather than adding different currencies.
Sign out is available in Settings. Existing registration, OTP, approval and hosted
verification routes are preserved. No inactive navigation items were introduced.

Validation uses TypeScript and Expo web export for both clients and browser
checks at 320, 390 and 768px, with offline role-specific demos. Coverage includes
agent lookup/confirmation/completion, dark mode persistence, role logins, password
visibility, cash input restrictions and merchant registration rendering. Native
camera, keyboard and safe-area acceptance still requires device testing.

Completed validation: both TypeScript checks and Expo web exports passed. Browser
flows passed with no runtime errors or horizontal overflow at the checked widths;
Light/Dark/System transitions and persistence were checked in both apps. Agent
completion and cash-input constraints, merchant registration rendering and both
login password toggles passed. A merchant dashboard regression test verifies that
suspended wallets and other currencies do not inflate the displayed available
balance; the mobile CI matrix now runs this test.
