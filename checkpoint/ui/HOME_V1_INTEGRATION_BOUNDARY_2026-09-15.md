# Home V1 Integration Boundary — 2026-09-15

## Status
🟡 INTEGRATED / BUILD VALIDATION PENDING

## Audit result
- `Test/ui-integration-08` already contains the provider-aware MainActivity and catalog state.
- Home V1 presentation exists under `ui/home` and is kept UI-only.
- Legacy `ReDantotsuHomeScreen` is now a compatibility entry point to Home V1.
- Catalog loading remains in the app/integration boundary through the existing backend repository path.
- AniList poster metadata and season-aware continue-watching state are mapped before entering Home V1 UI models.
- The obsolete `AnimeDataHomeMapper.kt` crossing the UI/data boundary was removed.

## Preserved behavior
- Anime card click still routes to the existing `selectedAnime` / Detail flow.
- Continue-watching entries route to the existing episode opening flow.
- Provider, playback, billing, monetization, and backend implementation were not moved into the UI screen.
- Existing MainActivity was not wholesale replaced.

## Remaining before GREEN
- Route Home V1 profile / notification / diamond / premium / Watch Together actions into MainActivity callbacks.
- Run the Android Build validation on the integration branch.
- Only after build passes, create the GREEN checkpoint and deliberate integration into main.

## Provider
Provider E2E remains separate/manual and is not required for this UI checkpoint.
