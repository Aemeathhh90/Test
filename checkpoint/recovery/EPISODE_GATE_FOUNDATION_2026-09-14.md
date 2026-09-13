# Episode Gate Foundation — 2026-09-14

## Status
- 🟢 Episode Gate blueprint locked
- 🟢 Locked episode access now routes through a dedicated Episode Gate before rewarded ad
- 🟢 No standalone "watch ad to collect diamonds" action
- 🟢 Free user with 1+ diamond consumes 1 diamond immediately and opens the requested episode
- 🟢 Free user with 0 diamonds sees Episode Gate and must choose rewarded ad flow or Premium
- 🟢 Rewarded ad grants 2 diamonds and 1 diamond is immediately consumed for the requested episode
- 🟢 After rewarded completion, the requested episode opens directly; no success popup
- 🟢 Player previous/next and episode-list navigation continue to use the same monetization gate
- 🟡 Physical/device UX verification of the actual rewarded-ad lifecycle and countdown remains
- 🟡 Core V1 audit remains in progress
- ⏸ Download implementation
- ⏸ Watch Together/Social backend implementation

## UI/UX decision
Initial state: Episode Terkunci with Premium and Tonton Iklan & Buka options.
During the real rewarded ad: use the ad's own countdown/lifecycle; do not add a second success dialog.
On reward completion: close the gate and enter the player directly.

## Technical changes
- Added `app/src/main/java/com/kakaanime/app/monetization/EpisodeGateDialog.kt` as the reusable V1 gate UI foundation.
- Updated `MainActivity.kt` so `openEpisode()` no longer launches a rewarded ad immediately when diamonds are 0; it first stores the requested episode as the gate target.
- Added a single pending episode target so player navigation is covered by the same gate path.
- Reward handling uses the existing diamond economy and persists the post-consumption balance.

## Important rule
Diamonds are access tokens, not a free-standing ad-farming currency. There is no UI action whose only purpose is to watch an ad and accumulate diamonds.

## Commits
- `0266695c46f11fc67453b91599be723e91fe1ed2` — add reusable Episode Gate foundation
- `4732c6d151a4eeeb42fbcd7f04362dc7731ad17c` — wire Episode Gate into episode opening and player navigation

## Next step
Verify the latest commit with GitHub Actions/build, then continue Core V1 audit. Do not expand Social implementation yet.
