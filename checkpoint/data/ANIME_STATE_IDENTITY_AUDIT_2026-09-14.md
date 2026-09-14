# KakaAnime — Anime State Identity Audit

Date: 2026-09-14

## Status

- 🟢 Audit complete: legacy local state is title-based.
- 🟢 New identity model added without deleting legacy storage.
- 🟢 Favorite identity is Anime Group level.
- 🟢 Episode identity is Anime Group + Season + Episode.
- 🟢 Migration policy refuses ambiguous title mappings.
- 🟡 Existing Preferences storage is not migrated/wired yet because the write was intentionally kept separate from this foundation step.
- 🟡 Build/runtime verification remains pending.

## Why

The app must distinguish the same episode number across seasons while keeping Favorite at Anime Group level. Legacy title-only data must remain readable until catalog context is available.

## New foundation

`AnimeStateIdentity.kt` provides the stable key model and a conservative migration policy. A legacy title may only be migrated automatically when catalog resolution proves exactly one matching Anime Group.

## Next

1. Wire `AnimeStateIdentity` into `KakaAnimePreferences`.
2. Add catalog-aware migration for existing favorites/history/unlocked episodes.
3. Update MainActivity, Library, and related screens to consume the new identity.
4. Verify build before marking CI green.
