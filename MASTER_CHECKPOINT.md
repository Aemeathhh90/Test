# KakaAnime — Master Checkpoint

Last updated: 2026-09-12

## Current base

- Branch: `main`
- Recovery progress from the Codespace branch has been restored into `main`.
- `gradlew` is present as the standard Gradle launcher and is executable (`100755`).
- Gradle wrapper includes `gradle-wrapper.jar` and Gradle 8.11.1 configuration.
- Backup branch created before recovery: `backup-main-before-recovery-2026-09-12`.

## Status

### 🟢 Confirmed in repository

- Android Gradle project structure.
- Android Gradle Plugin 8.9.2.
- Kotlin 2.1.20 + Compose compiler plugin.
- Java/Kotlin JVM target 17.
- Compose + Material 3 + extended Material icons.
- Media3 ExoPlayer/UI 1.6.1.
- ReDantotsu-style player controller architecture.
- 10-second seek back/forward in player core.
- Landscape/portrait player layouts.
- Episode previous/next callbacks.
- Auto-next toggle UI.
- Playback speed menu.
- Quality selector UI with 1080p marked Premium.
- Intro/outro timestamps are part of the anime/player model.
- Premium-only automatic intro/outro skipping logic exists in the player.
- Favorite tab and favorite state exist.
- History tab and watched-episode state exist at runtime.
- Calendar screen exists.
- Profile screen exists.
- Premium screen exists.
- Rewarded AdMob integration uses Google's test App ID/unit ID.
- Diamond rules: 2 diamonds per rewarded ad, 1 diamond per episode.
- 1080p/auto-skip/download entitlement rules are Premium-only in the monetization model.
- Provider engine, registry, normalization, routing and multiple provider implementations exist.
- Backend folder with Node server/data exists.
- Android manifest has INTERNET permission and AdMob test App ID.

### 🟡 Needs validation or completion

- Actual Bitrise Android build has not yet been re-run after recovery.
- Provider playback must be validated end-to-end on a real build/device.
- Current Home still uses a small local anime list rather than the complete provider catalog.
- Current player entry point still uses a demo Bunny video URL instead of the resolved provider stream.
- Player title/episode metadata is currently hard-coded in the player UI and needs to receive selected anime/episode data.
- Episode cards in the player are currently presentation-only and need episode navigation wiring.
- Auto-next toggle exists, but automatic transition to the next episode is not fully wired.
- 1080p is visually locked, but actual stream-quality enforcement needs provider-quality mapping.
- Download UI exists, but actual premium download implementation is not complete.
- Favorite/history state is currently in-memory and needs persistence.
- Watched lock indicators need to be connected to the real watched-episode state across screens.
- New Updates needs a real update feed and notification/state flow.
- Premium subscription screen exists, but Play Billing/product setup is not connected.
- Real AdMob production IDs are not configured; test IDs must remain for development only.
- UI color customization/color picker is not complete.
- Provider gateway availability can change and requires live validation.

### 🔴 Not part of the current V1 completion yet

- Comments V2 (intentionally deferred).
- Production Play Store billing configuration.
- Production release signing/release APK pipeline.

## Immediate execution order

1. Validate/fix Android compilation after recovery.
2. Re-run Bitrise build without relying on GitHub Actions.
3. Wire provider-resolved streams into the player instead of the demo video URL.
4. Finish Home V1 using provider/catalog data.
5. Persist Favorite + History + watched episode state.
6. Finish New Updates.
7. Finish Diamond/Ad flow and Premium entitlement enforcement.
8. Finish Premium subscription integration.
9. Finish UI customization/color picker.
10. Run provider/device validation and prepare release build.

## Important safety rule for future work

Do not delete the recovery or backup history until the replacement implementation has passed build and playback validation. New feature work should be added on top of this checkpoint rather than replacing working player/provider code blindly.
