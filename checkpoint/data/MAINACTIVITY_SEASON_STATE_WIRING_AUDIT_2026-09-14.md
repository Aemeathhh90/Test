# KakaAnime — MainActivity Season State Wiring Audit

Date: 2026-09-14

## Audit

MainActivity already receives season-aware catalog identity and passes season context into provider playback and Anime Detail. However, local Favorite, Watched, and Episode Unlock state was still read/written through legacy title-based APIs.

## Canonical foundation

- `AnimeStateIdentity`: Anime Group + Season + Episode identity.
- `KakaAnimePreferences`: additive season-aware storage APIs are present while legacy APIs remain for compatibility.
- `SeasonAwareStateRepository`: facade added to keep UI layers independent from storage keys.

## Current status

- 🟢 Identity model foundation.
- 🟢 Season-aware Preferences APIs.
- 🟢 Season-aware repository facade.
- 🟡 MainActivity migration/wiring: pending because the file requires a full-file replacement and the current connector retrieval is truncated; no partial overwrite was made.
- 🟡 Build/runtime: pending; no CI status evidence is available yet.

## Safety decision

Do not replace MainActivity with a reconstructed/truncated copy. Preserve the current working file until the complete source can be safely patched.

## Next

Wire MainActivity to `SeasonAwareStateRepository` with legacy fallback, then audit Library/History consumers before changing their public interfaces.
