# Anime State Identity — Wiring Plan

Date: 2026-09-14

## Audit
- `AnimeStateIdentity` exists as the new identity foundation.
- `KakaAnimePreferences` still stores legacy title-based Favorite, watched, unlocked, and watch-history state.
- `MainActivity` still consumes those legacy title-based APIs.

## Decision
Wire the new identity incrementally rather than replacing legacy storage in one large change.

### Target identity
- Favorite: Anime Group level.
- Watched / unlocked / history: Anime Group + Season + Episode.
- Legacy title state remains readable during migration.
- Ambiguous legacy title mappings must not be auto-migrated.

## Next implementation order
1. Add additive season-aware preference APIs.
2. Add catalog-aware migration helper.
3. Switch MainActivity state writes/reads to the new identity.
4. Update Library to render Group + Season identity.
5. Audit Calendar consumers.
6. Build/CI verification.

## Verification
- Build/runtime: 🟡 pending.
- No provider/runtime green claim until actual evidence exists.
