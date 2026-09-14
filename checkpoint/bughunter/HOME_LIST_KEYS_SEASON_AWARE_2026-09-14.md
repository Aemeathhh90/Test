# Checkpoint — Bug Hunter: Home List Keys Season-Aware

Date: 2026-09-14

## Scope
Audit confirmed that Home LazyRow item keys used title + episode only, which is unsafe once multiple seasons share the same title.

## Fix
- 🟢 Continue Watching keys now include `animeGroupId + seasonNumber + seasonTitle + episode`.
- 🟢 Anime catalog card keys now include season identity, preventing S1/S2 rows with the same title from colliding in Compose item reuse.
- 🟢 No new feature or gesture layer added.

## Verification
- 🟢 Change applied directly to `main`.
- 🟡 Android build/runtime verification remains pending because CI status has not provided a successful build result.

## Commit
`fc563254631b9cc63434a5a3438c44de7d17cfc4` — `fix: make Home list keys season-aware`

## Next Bug Hunter
Audit active Season Selection → Episode Availability → Diamond Unlock isolation → Watched/History → Player next/previous → Back navigation.
