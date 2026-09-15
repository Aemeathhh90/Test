# Vider Integration Checkpoint — 2026-09-15

## Status

🟡 Provider library integration is wired; Android Build validation is pending.

## Changes

- `vider` now declares library coordinates `com.kakaanime:provider`.
- `Test/settings.gradle.kts` maps that module to the standalone public `Aemeathhh90/vider` Git repository through Gradle source dependency support.
- `Test/app/build.gradle.kts` consumes the provider source from the `main` branch.
- `Test/app/.../provider/ProviderPlaybackResolver.kt` is now a compatibility facade that delegates resolution and episode discovery to the standalone `vider` implementation.
- Existing app-facing provider models/API remain intact during migration, reducing UI/player churn.

## Why this boundary

The app does not copy provider implementation files from `vider` and does not maintain a second active resolver implementation. Existing UI/player code can continue using the compatibility facade while provider logic moves to the dedicated repository.

## Validation

- `vider` extractor parity checkpoint is green and its latest Android Build passed before this integration.
- `Test` Android Build must pass before this checkpoint is considered green.
- Provider E2E remains manual-only and has not been used as routine validation.

## Next gate

1. Confirm `Test` Android Build resolves and compiles the Git source dependency.
2. If green, remove/retire duplicated local provider implementation in a separate checkpointed step.
3. Then wire the Android WebView browser resolver into the app-facing facade so browser fallback is active in production playback.
4. Manual Provider E2E remains the final proof of real playback/first frame.
