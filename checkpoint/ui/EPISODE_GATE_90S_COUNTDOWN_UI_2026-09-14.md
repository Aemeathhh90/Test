# Episode Gate — 90 Second Countdown UI — 2026-09-14

## Status
- 🟢 Episode Gate foundation remains locked
- 🟢 Existing rewarded-ad reward callback remains authoritative for diamond rewards
- 🟢 Added 90-second Episode Gate waiting state and circular countdown UI
- 🟢 Added episode thumbnail/title/date context to the gate
- 🟢 Added Premium shortcut: `Premium • Tanpa Menunggu`
- 🟢 Added explicit waiting/ad lifecycle information state
- 🟢 Countdown completion opens the pending episode through the existing persisted unlock path
- 🟢 No standalone diamond-farming action
- 🟡 Android build verification pending latest GitHub Actions run
- 🟡 Physical/device rewarded-ad lifecycle verification pending
- ⚪ Final Interaction Pass remains deferred until core features and UI/UX foundations are complete

## UI/UX decision
The Episode Gate now follows the approved reference direction:
1. KakaAnime header with close action.
2. Compact episode thumbnail card with episode number, title, and release date when available.
3. Locked state with clear `Episode Terkunci` messaging.
4. `Tonton Iklan & Buka` as the primary route.
5. `Premium • Tanpa Menunggu` as the premium shortcut.
6. After starting the access flow, show `Mohon Tunggu` with a 90-second circular countdown (`01:30` → `00:00`).
7. The waiting state explains that the ad may be playing or unavailable; if an actual rewarded callback arrives first, playback opens immediately.
8. If the waiting timer completes, the pending episode is unlocked through the existing persistence mechanism and playback opens.
9. `Batal` remains available during the waiting state.

## Monetization safety
The 90-second timer is an app-level access fallback. It does not award diamonds. AdMob's `onUserEarnedReward()` remains the only source of rewarded-ad diamonds. Existing premium and diamond rules are unchanged.

## Files
- `app/src/main/java/com/kakaanime/app/monetization/EpisodeGateDialog.kt`
- `app/src/main/java/com/kakaanime/app/MainActivity.kt`

## Commits
- `d4e90e2c6ceb134c84bccd295bfb2f8987d4443d` — waiting countdown UI
- `0c5a7429612645d9404ecbcc42612384dd113f11` — wire 90-second fallback access into episode opening

## Validation
- Static implementation review: 🟢
- UI foundation implementation: 🟢
- Reward callback remains authoritative: 🟢
- GitHub Actions: 🟡 pending latest run
- Device/runtime: 🟡 pending
- Real rewarded-ad lifecycle: 🟡 pending

## Next
Audit the complete Gate flow end-to-end after build verification: locked episode → gate → rewarded ad/unavailable state → countdown → unlock → player → reopen unlocked episode. Keep final gesture/animation Interaction Pass deferred.
