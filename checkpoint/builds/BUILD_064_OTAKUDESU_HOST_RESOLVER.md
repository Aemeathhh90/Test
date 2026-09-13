# Build 064 — Otakudesu Host Resolver Fallback

**Tanggal:** 13 September 2026
**Status:** 🔴 BUG fix implemented; E2E pending
**Source of truth:** `main`

## Error

Latest Otakudesu Episode 7 E2E still stopped at:

`Otakudesu returned no streams for episode 7`

The failure occurred before Media3 playback / first-frame validation.

## Root cause

Audit against CloudStream OtakuDesu PR #899 showed that AniLab had aligned the mirror AJAX layer (Base64 `data-content`, nonce/action parsing, AJAX headers), but its post-AJAX host routing was still incomplete.

CloudStream routes several returned embed hosts through host-specific extraction, notably FileDon/UserVideo/UserDrive/SameVideo (`div#app[data-page]` → `props.url`) and dedicated host handling for VidHide, Blogger, MP4Upload, YourUpload and StreamWish/FileLions.

AniLab previously sent most of these hosts through `GenericEmbedExtractor`, which is extension-oriented and can miss extensionless final media.

## Architecture before

```text
Otakudesu episode
 → mirror AJAX
 → iframe/embed URL
 → GenericEmbedExtractor
 → empty result for unsupported/extensionless host
 → no ProviderStream
```

## Architecture after

```text
Otakudesu episode
 → mirror AJAX
 → iframe/embed URL
 → ExtractorRegistry
 → OtakudesuHostExtractor
 → host page / data-page / source / video extraction
 → ProviderStream (UNKNOWN allowed temporarily)
 → StreamValidator classifies actual media
 → Media3
```

## Changes

Added:

`app/src/main/java/com/kakaanime/app/provider/extractor/extractors/OtakudesuHostExtractor.kt`

Capabilities:
- FileDon/UserVideo/UserDrive/SameVideo `data-page` / `props.url` extraction.
- `<source src>`, `<video src>`, iframe/embed and `data-src` extraction.
- direct `.m3u8/.mpd/.mp4/.mkv/.webm` detection.
- extensionless media remains `UNKNOWN` instead of being guessed as MP4; the existing strict validator must prove its type.
- preserves the embed URL as Referer.

Updated:

`ExtractorRegistry.kt`

The new host extractor is registered with priority 115, above the generic extractors and below the provider episode extractor (priority 120).

## Reference check

CloudStream/Yūzōnō OtakuDesu PR #899 was rechecked. The PR is currently open and its patch explicitly adds host-specific extractors and the FileDon/UserVideo/UserDrive/SameVideo `data-page` parser.

Reference:
`https://github.com/yuzono/anime-extensions/pull/899`

## Validation

Not yet proven by E2E. Do **not** mark Otakudesu green from this change alone.

Required proof:

`Search → Detail → Episode 7 → Stream → Validator → Media3 → onRenderedFirstFrame()`

## Classification

🔴 BUG → host routing gap identified and fixed; remains pending E2E validation.
