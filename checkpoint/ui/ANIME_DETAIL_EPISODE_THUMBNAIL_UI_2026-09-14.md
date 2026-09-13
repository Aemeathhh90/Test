# Anime Detail — Episode Thumbnail UI Foundation

**Tanggal:** 14 September 2026  
**Repo:** `KakaAnime/KakaAnime`  
**Branch:** `main`

## Decision

Episode-focused surfaces use the exact episode thumbnail when the provider supplies one. Anime poster/backdrop is not substituted for an episode thumbnail in these surfaces. Missing provider thumbnails use a neutral image fallback.

## UI/UX foundation

- Compact horizontal episode cards.
- Episode thumbnail: approximately 104dp × 68dp with rounded corners.
- Lock icon is overlaid directly on the episode thumbnail for unwatched/locked episodes.
- Play icon is overlaid directly on the thumbnail for watched episodes.
- Episode number, title, and release date remain the primary text hierarchy.
- `NEW` is a compact pill on the information side.
- Existing episode filters and search remain intact.
- Episode selection continues to route through the existing monetization/watch gate.
- No download action or new gesture system added.

## Existing data foundation reused

`ProviderEpisode.thumbnailUrl` and the watch-history episode thumbnail contract were implemented in the earlier thumbnail work. This UI batch only connects that established data to Anime Detail.

## Implementation

`app/src/main/java/com/kakaanime/app/AnimeDetailScreen.kt`

Commit: `2aa139c1d2c26ea6d32c9ff1218d4393eeb050c5`

## Validation

- Static implementation: 🟢
- UI foundation: 🟢
- Android build/runtime: 🟡 pending CI/device evidence
- Provider thumbnail E2E: 🟡 depends on live provider data
- Final Interaction Pass: ⚪ deferred until all feature foundations and UI/UX are complete

## Next

Audit the episode thumbnail rendering and then continue the Player UI/UX foundation. Do not add complex gestures before the final Interaction Pass.
