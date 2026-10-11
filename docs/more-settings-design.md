# More, Settings and demo verification

The customer app uses an account header, verification entry, searchable service
shortcuts and grouped settings rows. White surfaces, charcoal icons and orange
accents match Sutura. Interactive rows navigate to existing Sutura features;
language shows the supported English configuration. Appearance provides Light,
Dark and System options with a device-local saved preference.
The balance switch updates the existing wallet privacy preference for this session.

Research references, reviewed 10 October 2026:

- [Binance Account Center and service shortcuts](https://www.binance.com/en-IN/support/faq/detail/a24c83038cb442c1a0ecb528b44384a7)
- [Binance Settings](https://www.binance.com/en-IN/support/faq/detail/0a498da1689f4f56982b50a75721f310)
- [Binance identity verification](https://www.binance.com/en/support/faq/detail/360027287111)

These inform information hierarchy and navigation without adding crypto services
or unsupported account controls to Sutura.

In demo mode, Start verification opens local document selection, sample document,
sample selfie/liveness explanation, review and simulated submission steps.
It never opens a real verification link, accesses the camera, uploads evidence
or changes the KYC API status. Leaving the flow discards it. Live verification
continues through the existing hosted verification integration.

Validation: customer TypeScript and Expo web export passed. Chrome checks covered
service search/empty state, Settings navigation, privacy toggle and the complete
simulated verification flow. Returning to the overview confirmed NOT_STARTED
remains unchanged. More screenshots were reviewed at 320, 390 and 768 pixels
without horizontal document overflow. Native acceptance remains separate.
