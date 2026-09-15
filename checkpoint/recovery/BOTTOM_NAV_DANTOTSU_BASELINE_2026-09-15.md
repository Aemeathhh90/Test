# KakaAnime Checkpoint — Dantotsu Bottom Navigation Baseline — 2026-09-15

## Scope
Lock the first cross-screen Dantotsu baseline change after the build/back stabilization checkpoint.

## Change
- `KakaBottomNavigation` now applies Android navigation-bar insets with `navigationBarsPadding()`.
- The floating pill keeps its Dantotsu-style placement while avoiding the system navigation area.
- Horizontal/vertical spacing remains compact; no tab names or five-tab order changed.

## Preserved
- Home / Calendar / Social / Library / Profile order.
- Existing selected-state behavior.
- No provider, playback, monetization, or episode-gate logic changes.

## Validation
- Commit containing the UI change: `e54234933e56d31c7bcb6b2c713bb18339e22e22`.
- Android Build must be checked for this checkpoint before declaring it green.

## Next
Continue the all-screen Dantotsu visual baseline, then run an integrated Android Build and APK review before the Bug Hunter pass.
