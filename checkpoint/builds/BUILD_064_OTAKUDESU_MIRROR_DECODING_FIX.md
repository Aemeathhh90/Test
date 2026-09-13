# BUILD 064 — Otakudesu Mirror Decoding Fix

Date: 2026-09-13

## Status

🟡 WORKAROUND / FIX UNDER VALIDATION

## Trigger

Dedicated Otakudesu E2E built and launched successfully, but failed at:

`Otakudesu returned no streams for episode 7`

Build, APK packaging, emulator, and instrumentation setup were successful. The test did not reach Media3 or `onRenderedFirstFrame()`.

## Error → Root Cause → Architecture

### Error

`OtakudesuProviderE2ETest#onePieceEpisodeSevenRendersFirstFrame` returned zero streams.

### Root cause found by audit

The previous native `OtakudesuServerExtractor` did not match the current CloudStream OtakuDesu flow in two critical places:

1. `data-content` was parsed as if it were plain JSON. Current CloudStream decodes `data-content` from Base64 first, then parses JSON fields `id`, `i`, and `q`.
2. The nonce/action regex treated `window.__x__nonce` as the nonce action value. Current CloudStream targets the action nested in the mirrorstream data object:
   - nonce action: `data: { action: "..." }`
   - mirror action: `nonce: ..., action: "..."`

Because mirror entries could not be parsed, the mirror AJAX loop had no valid `id/i/q` requests, producing zero streams.

Reference: Yūzōnō `OtakuDesu.kt` PR #899, head `b022f9b4e2ed55b504d3b5f2d93d7a0d81e553bd`.

## Code change

Commit: `108e258766cb1416f5d07f1e9002faf0f854381c`

Updated `OtakudesuServerExtractor.kt`:

- Decode Base64 `data-content` before JSON parsing.
- Align nonce/action regexes with the current CloudStream implementation.
- Preserve a fallback action regex.
- Pass the actual episode origin as AJAX `Referer` instead of hardcoding `otakudesu.blog`.
- Keep `X-Requested-With: XMLHttpRequest`.
- Keep the existing ProviderStream → Validator → Media3 architecture unchanged.

## Classification

- Previous mirror parser: 🔴 BUG — incompatible with the current OtakuDesu encoded mirror payload.
- Current fix: 🟡 WORKAROUND / FIX UNDER VALIDATION.
- Provider architecture: ⚪ LOCKED.
- `onRenderedFirstFrame()` proof: not reached yet.

## Validation required

Run only:

`OtakudesuProviderE2ETest#onePieceEpisodeSevenRendersFirstFrame`

Success requires:

`Search → Detail → Episode 7 → mirror AJAX → embed/host resolution → ProviderStream → Media3 → onRenderedFirstFrame()`

Do not mark Otakudesu 🟢 until `onRenderedFirstFrame()` is proven.

## External reference note

CloudStream/Yūzōnō PR #899 is still open; its implementation was used as a reference pattern, not copied wholesale. AniLab keeps its own native extractor architecture.
