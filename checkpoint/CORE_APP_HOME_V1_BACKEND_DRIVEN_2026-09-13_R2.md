# KakaAnime — Core App / Home V1 Checkpoint R2

Date: 2026-09-13
Source of truth: `main`

## Current progress
- 🟢 AniList posters connected on Home.
- 🟢 Continue Watching uses persisted watched-episode history.
- 🟢 Player quality labels: 360p / 480p / 720p / 1080p Premium.
- 🟢 Provider-driven episode list exposed through `ProviderPlaybackResolver.episodes()`.
- 🟢 Detail screen loads provider episode list and provider episode metadata.
- 🟢 Unwatched episode rows now show a lock icon; watched rows show a checkmark and no lock.
- 🟡 Real quality switching still needs to change the resolved stream URL.
- 🟡 Auto Next UI exists; real automatic episode progression still needs verification.
- 🟡 New Updates still needs true watched/followed update logic.
- 🟡 UI customization/color picker remains unfinished.
- 🔴 Real-stream integration/E2E and bug hunting remain later V1 work.
- 🔴 Provider expansion remains later work.
- 🔴 Comments remain V2.

## Commits
- `ffd52360221aa59f91e7eccbc656f3a8b6a4acdb` — detail: show lock icon for unwatched episodes
- Previous V1 commits remain in history; no historical checkpoint was overwritten.

## Next
1. 🟡 Connect quality selection to actual provider stream resolution.
2. 🟡 Verify Auto Next.
3. 🟡 Complete New Updates.
4. 🟡 Complete UI customization.
5. 🔴 Integration/E2E + bug hunt.

## Guardrails
- Do not merge PR #2 (`complex-stream-resolution`) blindly.
- Do not claim build success without an actual verified build/status.
- If a GitHub operation fails, inspect repo/file state before retrying.
