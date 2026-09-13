# Build 055 — UNKNOWN Stream Type Detection Fix

## Status
🟡 FIX APPLIED — validation pending in Bitrise. Build #54 remains the failing baseline.

## Error
Media3 failed before first frame with `ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED` / `UnrecognizedInputFormatException`. The extractor list was progressive (MP4/TS/Matroska/etc.), indicating Media3 was not receiving a resolved HLS/DASH media type.

## Root cause
`StreamValidator` only read the response body for streams already classified as HLS/DASH. For `StreamType.UNKNOWN`, it used an empty body, so a signed/tokenized manifest URL without a `.m3u8`/`.mpd` suffix and with a generic content type could remain UNKNOWN. The resolver then returned that UNKNOWN stream to the E2E player, which could fall back to progressive extraction.

## Architecture before
Samehadaku/host extractor → ProviderStream(UNKNOWN) → validator does not inspect manifest body → UNKNOWN survives → Media3 progressive extraction → UnrecognizedInputFormatException.

## Architecture after
Samehadaku/host extractor → ProviderStream(UNKNOWN) → validator inspects a bounded response body + Content-Type + final URL → detects HLS/DASH → ProviderStream(HLS/DASH) → E2E passes explicit Media3 MIME → HLS/DASH playback can select the correct MediaSource.

## Code change
File: `app/src/main/java/com/kakaanime/app/provider/extractor/StreamValidator.kt`

Commit: `7f652886368fe2390d29cc111b2e15013ede7ed4`

Changes:
- UNKNOWN streams now read a bounded response preview (`take(32_768)`) during validation.
- HLS/DASH detection now uses final URL, Content-Type, and manifest markers (`#EXTM3U`, `<MPD`).
- The detected type is written back into `ProviderStream.type` before returning the validated stream.
- HLS/DASH validation accepts the corresponding manifest markers or matching Content-Type.
- MP4 validation behavior remains supported.

## Validation required
Run Bitrise `provider_e2e` against `main` with:
`com.kakaanime.app.provider.SamehadakuProviderE2ETest#onePieceEpisodeSevenRendersFirstFrame`

PASS requires `onRenderedFirstFrame()` proof. Do not mark Samehadaku green on build success alone.

## Impact
This is a centralized extractor-layer fix, so it can benefit other providers that return signed/tokenized HLS/DASH URLs without standard extensions. Production player architecture is not considered fixed until the production playback path carries the resolved stream type and headers.

## Classification
🟡 WORKAROUND / TAMBALAN until E2E proves the selected stream reaches Media3 as HLS/DASH and renders the first frame. If E2E still fails, audit the exact `E2E_STREAM` type/URL and host extractor output before stacking another patch.
