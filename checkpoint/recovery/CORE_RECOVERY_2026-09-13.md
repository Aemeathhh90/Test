# KakaAnime Core Recovery — 2026-09-13

## Status
- 🟢 Core release build verified green
- 🟢 Player episode monetization gate fixed
- 🟢 Player stream retry trigger fixed
- 🟢 Watch History filter consistency fixed
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

## Verification
GitHub Actions Android Build `#416` on `fc3bbbeda284b77f16c307a8f8725a4176081ef0` completed successfully. The `build` job passed `Assemble release APK` and `Upload release APK`, producing the `KakaAnime-release` artifact.

The player audit found two concrete behavioral issues in `MainActivity.kt`:
1. Next/Previous episode changed episodes directly and bypassed the diamond/rewarded-ad gate for free users.
2. The stream error retry assigned the same episode value, so the `LaunchedEffect` resolver key did not change and retry could be a no-op.

Commit `fc3bbbeda284b77f16c307a8f8725a4176081ef0` fixes both by routing Next/Previous through `openEpisode()` and adding a retry counter to the stream resolver effect.

The Watch History audit found an unsupported `Dropped` filter. The history status model only produces `In Progress` and `Completed`, so the filter could never return results. Commit `30f03263665f49280b056d3b145853deab523705` removes that dead filter instead of inventing a Dropped state that is not part of the KakaAnime requirements.

## Technical notes
- Free episode access remains 1 diamond per video; if no diamond is available, the rewarded-ad flow is used before opening the episode.
- Premium users continue to bypass diamond consumption.
- Watch History retains Anime Watched and Episode Watched with search, sort, grid/list, and thumbnail support.
- No provider or player dependency was added for the recovery fixes.
- Main remains the source of truth.

## Next step
Verify the Actions build triggered by `30f03263665f49280b056d3b145853deab523705`. If green, continue the core behavioral audit with the next concrete regression found. Do not expand new features until the recovery audit is stable.
