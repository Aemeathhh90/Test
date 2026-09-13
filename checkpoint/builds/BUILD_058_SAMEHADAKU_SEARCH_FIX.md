# Build #58 — Samehadaku Search Failure + Fix

Date: 2026-09-13
Repo: KakaAnime/KakaAnime
Branch: main

## Classification

🔴 BUG — Samehadaku E2E failed before detail/episode/stream/playback because the provider search returned an empty result list.

## Error

WarpBuild/GitHub Actions Build #57 reached the Android instrumented test successfully, but the test failed at the first assertion:

`java.lang.AssertionError: Samehadaku search returned no results`

The failure occurred in:

`SamehadakuProviderE2ETest#onePieceEpisodeSevenRendersFirstFrame`

No Media3 playback was attempted in this run.

## Root cause

The previous Samehadaku search implementation only parsed two legacy card wrappers:

- `div.animepost`
- `article.animpost`

The current Samehadaku site is live, and current references continue to use `v2.samehadaku.how`, but the provider parser was too narrow for the current/variant HTML search-card structure. The JSON gateways were only fallback paths and did not produce a result in the E2E environment.

External audit also confirmed the current Samehadaku site is live and lists One Piece, while the Wajik API has a documented history of Samehadaku 403 issues. Therefore the failure is treated as a provider parser/fallback BUG, not as a blocked external source.

## Architecture before

`SamehadakuProvider.search()`
→ GET `v2.samehadaku.how/?s=<query>`
→ parse only `div.animepost, article.animpost`
→ if empty, try JSON gateways
→ empty result
→ E2E fails before detail/episode/stream.

## Architecture after

`SamehadakuProvider.search()`
→ try WordPress search route
→ try pretty `/search/<slug>/` route
→ try `/search/?q=<query>` route
→ parse multiple current/legacy card wrappers (`.bs/.bsx`, `.animepost/.animpost`, etc.)
→ final HTML fallback scans `/anime/` links
→ JSON gateways remain last resort.

## Code change

Updated:

`app/src/main/java/com/kakaanime/app/provider/SamehadakuProvider.kt`

Commit:
`c2d643433d6a92086968f3427d4bcdc6093dc82c`

New blob SHA:
`5fa474e65b259dd359e46a719eff5152da541f21`

## Validation

Not yet E2E-validated. The next validation must run the same manual GitHub Actions / WarpBuild workflow against `main`.

Do not mark Samehadaku green from this fix alone.

## Next gate

`search → anime detail → episode 7 → stream resolution → typed stream → Media3 → onRenderedFirstFrame()`

Samehadaku remains 🔴 until `onRenderedFirstFrame()` is observed.
