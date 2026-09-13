# Build #51/52 — Samehadaku Discovery Checkpoint

**Date:** 13 September 2026  
**Provider:** Samehadaku  
**Status:** 🟡 pending first-frame validation

## Build #51 finding

`SamehadakuProviderE2ETest#onePieceEpisodeSevenRendersFirstFrame` reached playback but Media3 failed with `Source error` before `onRenderedFirstFrame()`.

**Classification:** 🔴 BUG — discovery/resolution did not yet follow the provider's actual server/download flow. URL validation alone was not sufficient proof of Media3 playability.

## Reference pattern

Independent Samehadaku implementations use:

`Episode HTML → server/download discovery → player_ajax / iframe → host extractor → direct media → player`

## Native AniLab implementation

Commit: `9d30ba905d2bebb0dc793efe6265f31d6fcb9c26`

New file: `app/src/main/java/com/kakaanime/app/provider/extractor/extractors/SamehadakuEpisodeExtractor.kt`

It handles `div#downloadb li a`, `#server > ul > li > div`, `player_ajax`, iframe/direct media, referer/header propagation, and delegates host URLs to AniLab's host extractor path.

The registry integration uses a nested host-only registry so the Samehadaku episode extractor does not recursively call itself.

## Gate

Samehadaku is not green until Media3 reaches `onRenderedFirstFrame()`.
