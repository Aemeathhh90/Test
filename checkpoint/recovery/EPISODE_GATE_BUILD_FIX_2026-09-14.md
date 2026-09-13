# Episode Gate Build Fix — 2026-09-14

## Scope
Stabilize the Episode Gate foundation already wired into `main` so the project can proceed to build/runtime verification.

## Locked Episode Gate behavior
- Free user with 1+ diamond: consume 1 diamond and open the requested episode directly.
- Free user with 0 diamonds: show Episode Gate.
- Gate options: Premium or `Tonton Iklan & Buka`.
- Rewarded ad grants 2 diamonds; 1 diamond is immediately consumed for the requested episode.
- After reward completion: open the requested episode directly; no success popup.
- No standalone ad-to-farm-diamonds action.
- Player Previous / Next / episode-list navigation uses the same `openEpisode()` gate path.

## Build fixes applied
1. `MainActivity.kt`
   - Removed the extra closing brace that caused `Expecting a top level declaration` during release compilation.
2. `ReDantotsuHomeScreen.kt`
   - Corrected the Compose `clip` import from `androidx.compose.ui.clip` to `androidx.compose.ui.draw.clip`.
3. `LibraryTabsScreen.kt`
   - Moved `LocalContext.current` out of the `AlertDialog` button lambda so composable invocation is no longer performed from a non-composable callback.

## Git checkpoints
- `59f556b0882185e25e00252202f28d098ddc0438` — fix Library history composable context
- `8f2d7f85e1c04c3c3ecfde0ba897639a4d066771` — restore Home clip import
- `5c65eedae355ed23907f116949fe5f7cb709989f` — restore Episode Gate build syntax

## Validation
- Previous Episode Gate commit build failed on these three Kotlin compile issues; the failures were confirmed from GitHub Actions logs.
- A new Android release build is running after the fixes.
- Status remains 🟡 until the new build completes successfully and physical/device rewarded-ad lifecycle is verified.

## Rule
Do not mark Episode Gate 🟢 from source inspection alone. Build success is required first, then device/runtime verification for the actual rewarded-ad lifecycle.
