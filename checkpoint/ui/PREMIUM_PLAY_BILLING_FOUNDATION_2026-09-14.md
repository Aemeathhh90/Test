# KakaAnime — Premium V1 + Google Play Billing Foundation

**Tanggal:** 14 September 2026  
**Area:** Premium / Monetization  
**Status:** 🟡 Foundation + UI/UX implemented; build/runtime + Play Console product verification pending

## UI reference locked

User-approved Premium reference covers:
- Premium landing/banner
- 1 / 3 / 6 / 12 month plan selection
- Premium benefits
- payment confirmation
- Premium active
- subscription detail
- payment success
- subscription expired

For KakaAnime V1, payment UI is intentionally simplified to **Google Play Billing** instead of custom GoPay/DANA/OVO/QRIS handling.

## Implemented foundation

### Files
- `app/src/main/java/com/kakaanime/app/premium/PremiumModels.kt`
- `app/src/main/java/com/kakaanime/app/premium/PlayBillingGateway.kt`
- `app/src/main/java/com/kakaanime/app/premium/PremiumScreen.kt`
- `app/src/main/java/com/kakaanime/app/MainActivity.kt`
- `app/build.gradle.kts`

### Google Play Billing
- Play Billing Library `9.1.0` added.
- Product ID locked for V1 foundation: `premium`.
- Base-plan IDs:
  - `monthly`
  - `quarterly`
  - `semiannual`
  - `annual`
- Product prices are read from Google Play at runtime instead of hardcoding payment amounts.
- Billing client uses pending-purchase support and automatic service reconnection.
- Product details are queried fresh before launching a purchase flow.
- Active subscription purchases are queried when BillingClient connects.
- Purchased subscriptions are acknowledged when needed.
- Pending purchases do not grant Premium.

## UI/UX implemented

- KakaAnime dark visual language + shared design tokens.
- Rounded Premium hero/banner.
- Premium badge/state.
- Four selectable plan cards with saving badges.
- Runtime Google Play price display.
- Benefit hierarchy for unlimited watching, 1080p, auto skip, and download.
- Google Play payment information card.
- Pill-shaped primary CTA: `Lanjut ke Google Play`.
- Loading state while fetching products.
- Unavailable/error state with retry.
- Existing app screen transition remains in use.
- No custom payment-method list was added.
- No complex gesture layer added; final Interaction Pass remains deferred until all feature foundations and UI/UX are complete.

## Entitlement rule

A successful `PURCHASED` Premium subscription currently updates the local `MonetizationState.isPremium` so the V1 app can exercise the Premium path.

**Production security requirement:** purchase token verification and entitlement authority must be moved to the secure backend before release. Google recommends sending purchases to a secure backend for verification before granting entitlement.

## Not yet implemented

- Google Play Console product/base-plan configuration.
- Internal-test purchase verification on a Play-distributed build.
- Secure backend purchase-token verification.
- Subscription lifecycle sync: renewal, grace period, on-hold, pause, expiry.
- Subscription management/deep-link UI.
- Final Premium Active / Detail / Success / Expired screens from the reference.
- Production price/offer configuration.

## Validation

GitHub Actions Android Build was triggered by the Premium changes. At checkpoint creation time, the latest Premium UI build run was still **in progress**, so this area must remain 🟡.

Do not mark Premium 🟢 until:
1. Android build passes.
2. App opens Premium screen on a device/emulator.
3. Google Play product details resolve from a configured test product.
4. Test purchase reaches `PURCHASED` and entitlement is correctly reflected.
5. Subscription expiry/lifecycle behavior is verified through Play testing/backend.

## Related official reference

Google Play Billing Library 9.1.0 is the current documented release as of this checkpoint. See Android Developers Play Billing release notes and integration guide for the supported APIs and lifecycle requirements.
