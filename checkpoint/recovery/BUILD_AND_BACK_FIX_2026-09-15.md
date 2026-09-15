# KakaAnime Checkpoint — Build & Back Fix — 2026-09-15

## Scope
Stabilization checkpoint before continuing the Dantotsu baseline UI batch.

## Changes locked
- Global Android Back navigation remains centralized in `MainActivity`.
- Back handler uses the Compose Activity API (`androidx.activity.compose.BackHandler`).
- Back priority: dismiss episode gate → cancel unlock → player to detail → detail to home → close premium/watch-together → switch secondary tab to Home → exit from Home root.
- Profile Compose compile fixes: explicit `AsyncImage` named `model`/`modifier` arguments and `ProfileQuickCard` receives its `Modifier` from the parent `Row` so `weight()` is used in the correct scope.
- Home Compose `clip` import uses `androidx.compose.ui.draw.clip`.
- No provider logic was changed in this stabilization pass.
- No Home poster text was changed.

## Current UI direction
- Dantotsu is the primary visual reference for Anime Detail, Calendar, Social, Library, Profile, Home and global bottom navigation.
- Saikou remains the reference for Settings.
- Final intended Home hierarchy remains: Profile Header → Search + Filter → Featured → Continue Watching → New Updates → Anime Tamat → Trending Now → Popular Anime.
- Home poster rows remain horizontally swipeable.
- Profile identity remains Nickname → @username → Bio, with a half-height banner/fade and no Lv/WDRG.

## Validation state
- Android Build was triggered after the compile fixes; its result must be checked before declaring this checkpoint green.
- Provider E2E remains separately blocked by unresolved stream first-frame failures; do not patch providers without CCTV evidence.

## Next step
1. Confirm Android Build result.
2. If green, freeze this checkpoint as the compile baseline.
3. Continue the all-screen Dantotsu baseline pass: Anime Detail → Calendar → Social → Library → Profile → global bottom nav consistency.
4. Then run one integrated Android build/APK review before user-led Bug Hunter.
