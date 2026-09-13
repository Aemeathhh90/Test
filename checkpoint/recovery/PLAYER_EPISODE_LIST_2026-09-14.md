# Player Episode List — 2026-09-14

## Status
- 🟢 Player Episode List now uses the provider episode list instead of only previous/current/next placeholders.
- 🟢 Every provider episode is rendered through a horizontally scrollable list up to the current provider maximum.
- 🟢 Episode cards are clickable and route through the existing `openEpisode()` monetization flow.
- 🟢 Watched episode state is reflected with the existing lock behavior.
- 🟢 Provider episode list is refreshed when the selected anime opens and then every 10 minutes while that anime remains open, so newly released episodes can appear automatically.
- 🟢 Previous/Next Player navigation now derives adjacent episode numbers from provider data when available.
- 🟡 GitHub Actions release build verification pending for the latest changes.
- 🟡 Provider/real-stream E2E remains pending.
- 🟡 Core V1 behavioral/UI audit continues.
- ⏸️ Backend + Watch Together remains deferred until Core V1 is stable.
- ⏸️ Download + Offline Mode remains deferred.

## Code Commits
- `16bb833dbbd267d2f6a8dd442c9749135196d1a1` — `player: make episode list provider-driven and clickable`
- `67b0f8130c6018c399630ab27edfdbda20fe03a4` — `player: connect provider episode list interactions`
- `e28be0a1324d2dd5b0e07504038b68a8c965653a` — `provider: refresh episode list while anime is open`

## Behavior
The Player Episode List now reflects the provider's available episodes. Newly released episodes are picked up by periodic provider refresh while the anime remains open. Selecting an episode still goes through the existing diamond/premium gate before opening the stream.

## Next Step
Verify the latest commit with GitHub Actions. If green, continue the Core V1 bug hunt around player state, watched-state synchronization, and back/navigation behavior.