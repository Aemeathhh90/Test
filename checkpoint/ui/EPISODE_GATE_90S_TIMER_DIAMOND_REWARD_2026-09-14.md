# Episode Gate — 90s Timer + Diamond Reward — 2026-09-14

## Decision
The 90-second Episode Gate timer is a genuine free-access fallback for users who cannot receive a rewarded ad (for example, no ad is available). It is not an imitation of AdMob's ad duration and does not replace the rewarded-ad callback.

## Reward rule
- Rewarded Ad completion: +2 diamonds, then 1 diamond is consumed for the requested episode.
- 90-second timer completion: +2 diamonds, then 1 diamond is consumed for the requested episode.
- Net balance change for either successful path: +1 diamond.
- The timer path does not claim an AdMob reward.
- Premium bypasses both the ad and the timer.

## UI/UX
- Episode header includes exact episode thumbnail, episode number/title, and release date when available.
- Locked state uses a prominent lock icon.
- Waiting state uses a circular 90-second countdown from 01:30 to 00:00.
- Copy clearly explains that the timer is an alternative when the ad is unavailable.
- Premium CTA is labeled "Premium • Tanpa Menunggu".
- Cancel remains available while waiting.
- No success popup; after access is granted, navigation goes directly to the Player.

## Implementation
- `EpisodeGateDialog.kt` owns the 90-second waiting presentation and timeout callback.
- `MainActivity.kt` now grants the same 2-diamond economy on timer completion before consuming 1 diamond and persisting the episode unlock.
- Existing rewarded-ad path remains callback-based through `onUserEarnedReward()`.

## Validation
- 🟢 UI/UX implementation committed.
- 🟢 Timer reward logic committed.
- 🟡 Android build verification pending latest GitHub Actions run.
- 🟡 Device/runtime verification pending.
- 🟡 Real AdMob lifecycle verification pending.

## Commits
- `48a0827213fd540d5c609453db03380d85430244` — grant diamonds on Episode Gate timer fallback
- `5fb181a025fb26f40d2d71050e0903a5116292ec` — clarify Episode Gate timer diamond reward

## Next
Continue Bug Hunter Pass 2 after CI validation. Keep final Interaction Pass deferred until feature foundations and UI/UX are complete.
