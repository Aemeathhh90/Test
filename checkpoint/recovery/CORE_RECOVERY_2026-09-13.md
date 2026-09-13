# KakaAnime Core Recovery — 2026-09-13

## Status
- 🟢 Core release recovery fixes recorded
- 🟢 Player episode monetization gate fixed
- 🟢 Player stream retry trigger fixed
- 🟢 Watch History filter consistency fixed
- 🟢 Player settings synchronization fixed
- 🟢 Player Back navigation wired back to Detail
- 🟢 Exact individually-watched episode locking restored
- 🟢 Watched state is recorded only after a valid stream resolves
- 🟢 Home Continue Watching refresh trigger added after watched state changes
- 🟢 Appearance settings now load from persisted preferences before the first UI render
- 🟢 Android Build #435 passed for the appearance persistence fix
- 🟡 Core behavioral/UI audit in progress
- 🟡 Provider/real-stream E2E remains pending
- ⏸️ New feature expansion remains paused during recovery audit
- ⏸️ Download + Offline Mode remains deferred to the final Download stage

## Recovery changes
- `6be7e121ee6ba7b77e236a3dfc89e0067c871928` — attempted EditProfile clip recovery
- `685fd8c4b4f02236e48c78370533d688ccedcbfb` — attempted Profile clip recovery
- `bf67da72ef17b8ab04dacd31bb57644b203b4546` — WatchHistory callback recovery
- `5baa7752be8f8d2749217bed46966cddaabbc9d3` — restore correct `androidx.compose.ui.draw.clip` import in EditProfileScreen
- `41747aec257977b74c47ef63c3e926ab88ad84b9` — restore correct `androidx.compose.ui.draw.clip` import in ProfileScreen
- `4ee039d137c6a9a183ff1b6b1106c01b8bcd999f` — checkpoint after core clip recovery
- `fc3bbbeda284b77f16c307a8f8725a4176081ef0` — enforce player episode monetization and retry
- `30f03263665f49280b056d3b145853deab523705` — remove unsupported Dropped history filter
- `e131b7834aa16ab801660653e3e04862a0cfdc0f` — sync player settings with persisted preferences
- `40643b832a7d456f2c71cad61f22e4168c89d177` — restore player Back navigation contract
- `bf7cde140edf6cfcba889dd74f72159783c6bc89` — wire Player Back to return to Detail
- `691c34018d6e8c7155dc49b3f88f5c8d42cd2a93` — correct `PlayerSurface` AndroidView parameter order after release build exposed the Kotlin compile error
- `46b003e7f44c711c8e07993c5455e27223a88a76` — lock only individually unwatched episodes
- `b8304c286d3aa8f3f522b333ae5846b3d7ab62c3` — pass exact watched episode numbers from Watch History into Detail
- `b514a1848458cf631b631bcbde467d464f43166a` — keep exact watched episode numbers in the Detail contract
- `ffaf9fe1ac17c0041893414005547138450d68f0` — record episode watched only after stream resolution succeeds
- `ba28b970118efc0b42c6206d46bf2f7d2ebabddc` — checkpoint update after the watched-state fix
- `7f56cb73d4199b564eeb252488edd116283f0bdd` — refresh Home Continue Watching from persisted history when the watched-state refresh key changes
- `4772c1b116ab5fe02baded3d3a4def9280947b35` — increment Home refresh key when a playable episode is recorded as watched
- `f901db30c0c4ed77df98222102ff2ef3f59de900` — restore persisted Appearance state on app startup

## Verification
- Android Build `#427` — 🟢 success for `b514a1848458cf631b631bcbde467d464f43166a`
- Android Build `#428` — 🟢 success for `ffaf9fe1ac17c0041893414005547138450d68f0`; release APK uploaded
- Android Build `#429` — 🟢 success for `ba28b970118efc0b42c6206d46bf2f7d2ebabddc`; release APK uploaded
- Android Build `#434` — 🟢 success for `c75542bdd799226e9e43c903acdeacbc37d609ae`; repaired Home syntax after refresh wiring
- Android Build `#435` — 🟢 success for `f901db30c0c4ed77df98222102ff2ef3f59de900`; persisted Appearance startup fix

## Technical notes
- Free episode access remains 1 diamond per video; if no diamond is available, the rewarded-ad flow is used before opening the episode.
- Premium users continue to bypass diamond consumption.
- Watched state is persisted only when `ProviderPlaybackResolver.resolve(...)` returns a non-null playable URL; failed provider resolution no longer creates a false watched record.
- Home Continue Watching reloads persisted Watch History whenever the in-app watched-state refresh key changes.
- Watch History retains Anime Watched and Episode Watched with search, sort, grid/list, and thumbnail support.
- Player settings for Auto Next, Auto Skip Intro/Outro, and Default Quality are persisted and consumed by the player.
- Appearance now initializes from persisted dark-mode and accent settings in `MainActivity`, preventing a startup flash/reset to default settings.
- Player Back returns from portrait Player to the selected Detail screen; landscape Back still exits fullscreen to portrait.
- Download remains intentionally untouched because Download + Offline Mode is a later stage.
- Main remains the source of truth; do not merge PR #2.

## Next step
Continue the core behavioral audit from the source of truth: Detail → Episode List → individual Lock/Watched state → Player entry → Back to Detail → Home/Continue Watching → Watch History. Audit persistence and state restoration before expanding features. Provider/real-stream testing remains a separate later integration track, and Download/Offline Mode stays deferred.
