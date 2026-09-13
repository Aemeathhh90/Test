# KakaAnime Calendar → AniList Detail — 2026-09-14

## Status
- 🟢 Calendar entries no longer depend on `localAnime` to be clickable.
- 🟢 AniList schedule entries can now be converted into an `Anime` model and opened in Detail.
- 🟢 Code change committed directly to `main`.
- 🟡 Android release build verification pending.
- 🟡 Core navigation/state/UI audit continues.
- 🟡 Provider/real-stream E2E remains pending.
- 🟡 Backend + Watch Together remains planned, not started.
- ⏸️ Download + Offline Mode remains deferred.

## Change
- `8dc08309e3b772f58ff619b3850cb1a9d385559b` — `fix: open AniList calendar entries in detail`
- Calendar now uses existing local metadata when available, otherwise creates a safe `Anime` model from the AniList schedule entry.
- This removes the confirmed Calendar → Detail dead-end for anime not present in the small local dataset.

## Technical note
- Provider episode loading already runs from the selected anime title, so the generated model can continue into the existing provider-driven Detail flow.
- Player episode navigation still needs a separate audit against the provider episode list; no speculative change made here.

## Next step
Verify the new commit with GitHub Actions. If green, continue the Core V1 navigation/state audit and inspect Detail → Provider → Player behavior.
