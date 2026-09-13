# Build 055 — UNKNOWN Stream Type Detection Fix

## Status
🔴 FAIL from Build #54 remains the baseline; this checkpoint records the next fix to validate.

## Error
Media3 failed before first frame with `ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED` / `UnrecognizedInputFormatException`. The extractor list was progressive (MP4/TS/Matroska/etc.), indicating Media3 was not receiving a resolved HLS/DASH media type.

## Root cause
`StreamValidator` only read the response body for streams already classified as HLS/DASH. For `StreamType.UNKNOWN`, it used an empty body, so a signed/tokenized manifest URL without a `.m3u8`/`.mpd` suffix and with a generic content type could remain UNKNOWN. The resolver then returned that UNKNOWN stream to the E2E player, which could fall back to progressive extraction.

## Architecture before
Samehadaku/host extractor → ProviderStream(UNKNOWN) → validator does not inspect manifest body → UNKNOWN survives → Media3 progressive extraction → UnrecognizedInputFormatException.

## Architecture after
Samehadaku/host extractor → ProviderStream(UNKNOWN) → validator peeks at response body + Content-Type + final URL → detects HLS/DASH → ProviderStream(HLS/DASH) → E2E passes explicit Media3 MIME → HLS/DASH playback can select the correct MediaSource.

## Code change
`app/src/main/java/com/kakaanime/app/provider/extractor/StreamValidator.kt`
- Inspect a bounded response preview for UNKNOWN streams using `response.peekBody(32_768)`.
- Detect HLS/DASH from final URL, Content-Type, and manifest markers (`#EXTM3U`, `<MPD`).
- Validate the detected type and return it in `ProviderStream.type`.
- Preserve the response body for Media3 by using OkHttp `peekBody` rather than consuming `response.body`.

## Validation required
Run Bitrise `provider_e2e` against `main` with:
`com.kakaanime.app.provider.SamehadakuProviderE2ETest#onePieceEpisodeSevenRendersFirstFrame`

PASS requires `onRenderedFirstFrame()` proof. Do not mark Samehadaku green on build success alone.

## Impact
This is a centralized extractor-layer fix, so it can benefit other providers that return signed/tokenized HLS/DASH URLs without standard extensions. Production player architecture is not considered fixed until the production playback path carries the resolved stream type and headers.
