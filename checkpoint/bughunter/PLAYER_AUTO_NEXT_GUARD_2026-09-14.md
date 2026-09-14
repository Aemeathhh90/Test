# Bug Hunt — Player Auto-Next Guard

Date: 2026-09-14
Branch: `main`

## Status

🟢 Fixed

## Finding

The player auto-next coroutine could invoke `onNextEpisode()` whenever playback reached the end if `autoNext` was enabled, even when the UI had no actual next provider episode. MainActivity guarded the callback, but the player component contract itself was unsafe.

## Fix

`ReDantotsuPlayerController.kt` now requires `nextEpisode != null` before triggering auto-next and includes `nextEpisode` in the effect keys so the decision stays synchronized with the current episode list.

Commit containing the fix:

- `a162e7fa4a6f7c86d4079aed1471b24f38adad0d`

## Verification

Static audit confirms the guard is present. CI/build evidence is still pending (`statuses: []` was observed for the fix commit), so this checkpoint does not claim a release build is green.

## Next

Continue the full Bug Hunt with the confirmed MainActivity stream-session race, then provider health isolation and Calendar season-aware matching.
