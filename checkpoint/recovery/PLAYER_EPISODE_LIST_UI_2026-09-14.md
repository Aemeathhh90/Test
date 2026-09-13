# Player Episode List UI — 2026-09-14

## Status
- 🟢 Player Episode List uses a compact horizontal carousel inside the Video Player.
- 🟢 Episode cards show the episode number only; long episode titles are intentionally omitted.
- 🟢 Cards remain clickable and continue through the existing monetization flow.
- 🟢 Current episode is highlighted.
- 🟢 Unwatched episodes keep the lock indicator.
- 🟢 Watched episodes show a compact progress indicator.
- 🟢 Provider list remains dynamic and can grow when the provider reports newly released episodes.
- 🟡 Progress is currently binary (watched = full bar); true resume percentage per episode is a future enhancement because persisted per-episode playback position is not yet part of the current data model.
- 🟡 GitHub Actions verification pending for this UI commit.
- 🟡 Core V1 behavioral/UI audit continues.
- ⏸️ Download + Offline Mode remains deferred.
- ⏸️ Backend + Watch Together remains deferred until Core V1 is stable.

## Code Commit
- `a59cd9285993b18a8a141a2cc927b968ef22898a` — `ui: refine player episode carousel with progress`

## UX Direction
The Player Episode List is intentionally horizontal and compact so it does not compete with the video. The list is designed around number + state (active, watched/progress, locked) rather than long episode titles.

## Next Step
Verify the latest GitHub Actions build. If green, continue the Core V1 bug hunt without touching Download/Offline or Social/Watch Together yet.
