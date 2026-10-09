# Merchant collections

A merchant creates a single-use collection against their own active wallet.
Currency must match the wallet, amount must be positive with at most four
fractional digits, and expiry is 1–90 days. The server generates the checkout URL
`/pay/<reference>`; callers cannot replace it with an arbitrary payment URL.
The merchant dashboard links to checkout.

A signed-in customer opens the URL, selects an active wallet in the payment
currency and confirms. The amount and destination come from the stored link,
not the customer's request. The backend locks the link and uses the existing
transfer flow. ALLOW posts both sides and marks the link PAID. REVIEW moves no
money and reports REVIEW until the signed case callback resolves it. BLOCK
reports FAILED. Link status is derived from the linked transaction so approved,
rejected and reversed payments stay consistent without a second callback.

The same payer retry returns the existing payment. A different payer or source
wallet cannot claim an already-used link. Failed links are single use too; the
merchant creates another link for a new payment attempt. Expiry prevents a new
payment but does not cancel an existing review. Existing review approvals still
recheck balance, limits and wallet status. A reversal reports REFUNDED.

Authenticated API:

- `POST /api/v1/payment-links`: create (merchant wallet owner only).
- `GET /api/v1/payment-links`: list the caller's links.
- `GET /api/v1/payment-links/ref/{reference}`: checkout details/status.
- `POST /api/v1/payment-links/ref/{reference}/pay`: body `{ "walletId": 123 }`.

Apply migration `006_merchant_collections.sql` to existing schemas. Old links
with custom URLs retain those URLs; newly created links use Sutura checkout.
