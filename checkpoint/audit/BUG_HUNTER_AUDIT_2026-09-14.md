# KakaAnime Bug Hunter Audit — 2026-09-14

## Repository
- Source of truth: `KakaAnime/KakaAnime`
- Branch: `main`
- Scope: Core V1 navigation, Home, Library, Profile, watch history, episode/player flow.

## Findings

### P0 — Bottom navigation architecture mismatch
Current navigation is Home / Calendar / History / Favorite / Profile. The locked V1 blueprint requires Home / Calendar / Social / Library / Profile, with Favorite and History living inside Library.

### P1 — Library is currently History-only
`LibraryScreen.kt` is a wrapper around `AnimeWatchedScreen`; it does not yet implement the locked Library V1 Favorite | History structure.

### P1 — Profile Favorite statistic is non-functional
Anime Watched and Episode Watched stats open screens, but Favorite has no click action.

### P1 — Watch History semantics
`MainActivity` records an episode as watched immediately after a stream URL resolves. This can count an episode before meaningful playback occurs. Needs player/playback-position semantics later.

### P1 — Episode navigation edge case
Previous/next currently have fallback episode numbers when provider episode numbers are unavailable. This can attempt an episode that the provider does not expose.

### P2 — Home V1 mismatch
Home still contains WDRG/Lv. badges, Recommended and a Premium reference card, and does not currently expose the locked Search + Filter structure.

## Audit rule
No code changes are included in this audit checkpoint. Findings are to be fixed incrementally on `main`, with each real code change checkpointed immediately.
