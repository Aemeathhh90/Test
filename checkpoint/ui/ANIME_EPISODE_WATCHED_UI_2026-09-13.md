# KakaAnime — Anime Watched & Episode Watched UI

**Tanggal:** 13 September 2026  
**Branch:** main

## Reference locked

The user-provided reference is the UI blueprint.

- Left reference: Grid/Kotak mode for both Anime Watched and Episode Watched.
- Right reference: List/Scroll-down mode for both Anime Watched and Episode Watched.
- Poster/card proportions stay compact and follow the reference/ReDantotsu direction.
- Both screens support Grid ↔ List switching.

## Anime Watched

- Grid 4-column compact poster layout.
- List layout with poster on the left and metadata on the right.
- Title, latest watched episode, progress bar, status, last-watched time, overflow menu.
- Filters: All / In Progress / Completed / Dropped.
- Search, Sort, Total, Grid/List toggle.
- Anime poster lookup uses AniList.

## Episode Watched

- Grid 4-column compact episode layout.
- List layout with thumbnail on the left.
- Anime title, episode number, episode title when available, last-watched time, duration badge, overflow menu.
- Filters: All / Today / This Week / This Month.
- Search, Sort, Total, Grid/List toggle.
- Episode history is persisted locally.

## Data foundation

- Added persistent `WatchHistoryEntry` records instead of relying only on the old anime → last episode map.
- Existing last-watched map remains for Continue Watching compatibility.
- New playback opens record the watched anime/episode and timestamp.
- Duration is stored in the history model and displays `—:—` until an actual duration is captured.
- Episode title field exists and is ready to be populated from provider episode metadata.

## Account integration

- Account now exposes Anime Watched and Episode Watched entry points.
- Anime Watched stat opens the Anime Watched screen.
- Episode Watched is available as an Account menu entry.
- Account Dashboard statistics are still not finalized; they will be recalculated after these real history screens/data are stable.

## Status

- 🟢 UI reference locked
- 🟢 Anime Watched Grid/List UI
- 🟢 Episode Watched Grid/List UI
- 🟢 Search / Sort / Total
- 🟢 Required filters
- 🟢 Persistent watched episode history foundation
- 🟢 Account entry points
- 🟢 AniList poster lookup
- 🟡 Actual Android build/runtime verification
- 🟡 Accurate episode duration capture from player
- 🟡 Provider episode-title population across all real providers

## Commits

- `2e146234ec22de442837b750e727d965eff73c2e` — AniList poster lookup
- `95ca1f9e340c14ece0390d49e05b4670b7e500ed` — persistent watched history
- `412df5b4809b4f2f999df3ca263ebe271333a332` — compact grid/list history UI
- `b74219f64d4ac5c6bccc13cb16537ef8c606942a` — Compose cleanup/final history UI
- `1df474a2a7a91ce5a13b2f975afff1479cc436dd` — playback history recording/shared anime catalog
- `7a717f679353e118c30968a04f639237cf0d7bce` — Account entry points

## Next step

Audit the Android build/runtime result first. Then improve actual duration capture and provider episode-title population before returning to the Account Dashboard final pass.
