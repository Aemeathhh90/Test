# Player Direct Episode Swipe — 2026-09-14

## Status
- 🟢 Direct horizontal swipe is now wired onto the video surface.
- 🟢 Swipe left (>= 120px) requests the next episode.
- 🟢 Swipe right (>= 120px) requests the previous episode.
- 🟢 Short horizontal drags are ignored to reduce accidental episode changes.
- 🟢 Swipe uses the existing `onPreviousEpisode` / `onNextEpisode` callbacks, so MainActivity's existing diamond/Premium monetization gate remains in the path.
- 🟢 Both portrait and landscape player surfaces use the same swipe behavior.
- 🟡 Physical-device UX verification remains pending.
- 🟡 Core V1 behavioral/UI audit continues.
- ⏸️ Download + Offline Mode remains deferred.
- ⏸️ Backend + Watch Together remains deferred until Core V1 is stable.

## Code Commit
- `2682fd02bdf4ddfefce1cda6b5ef7dd0fc8081f7` — `player: add direct horizontal episode swipe`

## Technical Notes
- Gesture handling is implemented with Compose `detectHorizontalDragGestures` on the video surface.
- The gesture is threshold-based rather than reacting to every drag event.
- Existing player controls and episode-list clicks remain unchanged.

## Next Step
- Verify the new commit with GitHub Actions.
- If green, continue the Core V1 audit for remaining behavioral gaps before starting Social/Watch Together or Download/Offline.