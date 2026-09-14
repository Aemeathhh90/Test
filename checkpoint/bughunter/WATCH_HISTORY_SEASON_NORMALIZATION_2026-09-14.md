# Bug Hunter — Watch History Season Normalization — 2026-09-14

## Status
- 🟢 Confirmed bug fixed.
- 🟡 Build/CI still unverified.

## Finding
Season-aware watch history compared `seasonTitle` with exact string equality. Equivalent values such as `Season 1` and `season 1` could create duplicate history entries or fail to delete an existing entry.

## Fix
`KakaAnimePreferences.recordWatchHistory()` and `deleteWatchHistoryEntry()` now compare normalized season titles (trimmed, lowercase, collapsed whitespace). Stored new entries also trim blank season titles to null.

## Scope
- Anime Group identity remains unchanged.
- Season number remains authoritative when present.
- Diamond/unlock storage was audited and remains season-aware; no change was required there.
- Legacy title-based storage remains untouched.

## Verification
- Audited `SeasonAwareStateRepository` and `AnimeStateIdentity`.
- Audited MainActivity unlock flow: unlock checks/writes use `AnimeStateIdentity`.
- Audited Player previous/next wiring: navigation delegates back through `openEpisode()`, so unlock rules are re-applied per target episode.
- No provider availability status was fabricated.

## Next
Continue Bug Hunter with Watched/History callers, then Player previous/next and back-navigation edge cases.
