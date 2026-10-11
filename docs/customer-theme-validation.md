# Customer UI, themes and input validation

Implemented in `ui/sutura-app`. The customer screens use a shared runtime palette,
neutral surfaces, orange primary actions, consistent input controls, compact
service shortcuts and a flat bottom navigation bar. The layout and appearance
choices draw on the [Binance Settings guide](https://www.binance.com/en-IN/support/faq/detail/0a498da1689f4f56982b50a75721f310).

Light, Dark and System are available from Settings. The choice persists
through AsyncStorage. System follows device appearance changes. QR scan areas
and the existing logo artwork retain their white background for readability.
Receipt PDFs retain their printable design.

Input behavior:

- Amounts reject negatives, exponent notation, grouping separators and more than
  four decimal places; submit validation enforces 0.01 through 1,000,000,000,000.
- Phone fields use a telephone keyboard and reject letters; submit validation
  normalizes local Gambian and international formats.
- Names support international letters, marks, spaces and common name punctuation.
- Email fields use email input and validate the address before submission.
- Registration passwords respect the existing hash's 72-byte UTF-8 limit.
- Wallet currency is chosen from GMD, USD, EUR, GBP and XOF.
- Support fields retain their existing subject/message length limits.
- Transfers require review and confirmation; the review preserves four decimal
  places instead of rounding a submitted amount to two.

Validation on 10 October 2026:

- Customer TypeScript and Expo web export passed.
- Nineteen input-validation and demo tests passed.
- Browser flows exercised theme switching, persistence after reload, system
  appearance changes, registration errors, restricted payment inputs, transfer
  review/confirmation, deposit/withdrawal QR generation and demo verification.
- Customer screenshots reviewed at 320, 390 and 768 pixels. No browser runtime
  errors or horizontal document overflow in the completed run.

Local harness and screenshots are in the parent workspace's
`.local-validation/ui-review`. Native keyboard, safe-area and device appearance
acceptance still requires an emulator or physical phone. Agent and merchant
clients now follow this visual system too; see [role UI alignment](role-ui-consistency.md).

## Customer layout revision

Login and registration now use a compact brand bar, email/phone login tabs, and
a password visibility control. Appearance selection is available only in Settings. Primary
buttons share the same rounded rectangle shape as payment actions.

Settings opens a Mode & appearance sheet with Light, Dark and System rows and a
selected check. The inline theme tiles have been removed. This follows the
appearance entry and mode choices documented in the official Binance guide above,
while retaining Sutura branding.

Home uses an open balance overview and Add money action instead of a carousel of
outlined wallet cards. Wallets uses currency balance rows with a selected marker,
compact payment actions and an Add wallet sheet. No balances from different
currencies are combined.

Wallet selection uses a searchable sheet shared by Home, Wallets and payment
screens. Inactive wallets cannot be selected in the sheet. Send starts with the
recipient phone field, followed by the funding wallet and amount; the duplicate
balance banner is removed. Review and explicit confirmation remain required.

Additional browser checks cover wallet search/no matches, wallet creation and
selection, and showing/hiding the login password. This revision changes the
customer client only.

Registration now has three steps: name, contact details (required phone and
optional email), and password. Each Continue validates its own fields. Back
preserves entries, and only Create account on the final step submits to the
existing registration endpoint. Login remains a single screen. Appearance is
available only in Settings.

## Duplicate UI cleanup

- Top up and Withdraw use one funding wallet selector and balance; the separate
  balance banners and their shared component have been removed.
- Wallets uses its currency rows for selection, without another picker above them.
- More keeps one profile entry, one Settings gear and one entry per service. The
  extra verification banner and repeated account/settings/support footer are gone.
- Settings keeps one support link. Wallet selection stays in Wallets and payment
  flows; verification stays in Personal account and the service directory.
- Home no longer repeats Wallets and Activity as shortcut tiles. Transfer and
  Withdraw remain intentional payment shortcuts.

TypeScript, 19 input/demo tests and Expo web export passed after this cleanup.

## Home visual hierarchy

Home now groups the balance in a neutral panel with an orange accent and uses
larger Transfer/Withdraw action tiles with explanatory labels. Empty activity has
an illustrated introduction rather than a lone placeholder sentence. Accounts
with completed transactions show incoming/outgoing totals for the selected
currency, using the activity history returned by the app. Other currencies and
pending/failed transactions are excluded. Balance privacy applies to those totals.

## Scan & pay

The center customer navigation tab is Scan. Phone transfers remain accessible
from Home, Wallets and More. Native scanning uses Expo Camera with explicit
permission, back camera and QR-only detection. Manual payment-link/reference
entry has been removed. Web directs users to the phone scanner and retains the
local sample merchant payment in demo mode.
Camera setup follows [Expo Camera documentation](https://docs.expo.dev/versions/latest/sdk/camera/).

Supported merchant QR contents are the server-generated `plink-` reference,
`/pay/<reference>`, an HTTP(S) payment URL with that path, or
`suturaapp://pay/<reference>`. Only the extracted reference is sent to the
configured Sutura API. QR-provided amounts and destinations are never trusted,
and scanned URLs are never opened or fetched directly.

The app looks up `GET /api/v1/payment-links/ref/{reference}`, shows the returned
merchant/amount/currency, allows an active matching-currency funding wallet, and
requires Confirm payment before POSTing its wallet ID to the existing `/pay`
endpoint. The server controls expiry, authorization, transfer policy, and replay
protection. Paid, review and other results have distinct presentation. Connection
mode changes abort confirmation rather than converting a live payment into a
successful sample payment.

Demo offers a single local sample merchant collection for GMD 250. Replaying it
never debits twice. Parser and demo payment tests cover unrelated/malformed QR
codes, mismatched currency, insufficient funds, activity recording and replay.
The demo API remains identical across all three mobile clients.

Native camera permission/scanning and live merchant settlement still require
physical-device/emulator and running-server validation. Expo config includes the
camera plugin and disables Android audio recording permission for this scanner.

Scan revision validation: customer TypeScript and web export passed; all 22
customer tests passed. Browser exercised Scan navigation, invalid reference,
local sample lookup, review, confirmation and payment completion, followed by the
existing transfer/QR/verification/theme/registration flows without runtime errors
or horizontal overflow.

## Activity visual consistency

Activity defaults to History and uses the shared neutral/orange visual language:
underline History/Expenses/Income tabs, one period picker sheet, a neutral summary
panel and flat transaction rows with type icons and status labels. The old chart
and stacked segmented controls are no longer used by this screen.

All displayed history follows the selected currency and period. History includes
pending/failed payments; totals and Expenses/Income include completed payments
only. Transaction rows, including Home's rows, respect the balance privacy toggle.
The list remains virtualized and opens existing transaction details on press.
