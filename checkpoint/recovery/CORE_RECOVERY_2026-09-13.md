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
- 🟢 Android release build verification passed for the latest recovery changes
- 🟡 Core behavioral/UI audit in progress
- ⏸️ New feature expansion remains paused during recovery audit

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

## Verification
- Android Build `#427` — 🟢 success for `b514a1848458cf631b631bcbde467d464f43166a`
- Android Build `#428` — 🟢 success for `ffaf9fe1ac17c0041893414005547138450d68f0`; release APK uploaded
- Android Build `#429` — 🟢 success for `ba28b970118efc0b42c6206d46bf2f7d2ebabddc`; release APK uploaded
- Build #428 release artifact: `KakaAnime-release`, 16,738,422 bytes, SHA-256 `2b59e9813be67b6520357d2e02660f3460df38a9efa99c939ca09cdfcf2e1e39`
- Build #429 release artifact: `KakaAnime-release`, 16,738,422 bytes, SHA-256 `5c5533eab7cab66c0f0da391b16ef00fc0f7aa5e939b52d2110d309fc0a60d93`

## Technical notes
- Free episode access remains 1 diamond per video; if no diamond is available, the rewarded-ad flow is used before opening the episode.
- Premium users continue to bypass diamond consumption.
- Watched state is now persisted only when `ProviderPlaybackResolver.resolve(...)` returns a non-null playable URL; failed provider resolution no longer creates a false watched record.
- Watch History retains Anime Watched and Episode Watched with search, sort, grid/list, and thumbnail support.
- Player settings for Auto Next, Auto Skip Intro/Outro, and Default Quality are persisted and consumed by the player.
- Player Back returns from portrait Player to the selected Detail screen; landscape Back still exits fullscreen to portrait.
- Download remains intentionally untouched because the Download stage has not started.
- Main remains the source of truth; do not merge PR #2.

## Next step
Continue the core behavioral audit with Detail → Episode List → Lock/Watched → Player entry → Back to Detail. Verify that Home/Continue Watching and Watch History refresh correctly after returning from Player. Do not expand new features until the recovery audit is stable.
