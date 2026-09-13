# Build #56 — Samehadaku E2E Still Fails at Media3 Source Parsing

Date: 2026-09-13
Repository: KakaAnime/KakaAnime
Branch: main

## Status

- Build #56 🔴 FAIL
- Gradle packaging issue from Build #55 is resolved: `:app:packageDebug`, `:app:assembleDebug`, and `:app:assembleDebugAndroidTest` all completed successfully.
- Samehadaku E2E reaches Media3 playback but fails before `onRenderedFirstFrame()`.

## Error

`Media3 did not render the first frame within 90s. Playback error=Source error code=ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED cause=UnrecognizedInputFormatException`

Media3 reports only progressive extractors (`FlvExtractor`, `Mp4Extractor`, `TsExtractor`, etc.) and does not select HLS/DASH parsing.

## Root cause / current diagnosis

The explicit MIME fix from the previous iteration did not resolve the failure. The strongest diagnosis is that the selected `ProviderStream.type` is still `UNKNOWN` (or otherwise not HLS/DASH) at the E2E player boundary, so the test does not assign `MimeTypes.APPLICATION_M3U8` or `MimeTypes.APPLICATION_MPD`.

The uploaded Build #56 log does not contain the expected `E2E_STREAM[...]` diagnostic lines, so the exact selected candidate type is not directly proven by this build. Do not mark Samehadaku green.

## Architecture before

Samehadaku direct/host extraction -> ProviderStream -> validation -> E2E Media3.

Stream type is commonly inferred from URL suffix. Tokenized/extensionless media URLs can remain `UNKNOWN`.

## Architecture after / intended next fix

Samehadaku extraction -> validator/response sniffing -> preserve validated stream type -> E2E Media3 explicit MIME -> first-frame gate.

For `UNKNOWN`, validation must inspect safe response metadata/body sufficiently to distinguish HLS/DASH when the URL has no `.m3u8`/`.mpd` suffix, then the resolved `ProviderStream.type` must be preserved into the E2E player.

## Validation required next

1. Add/restore explicit diagnostics for every resolved stream and the selected stream type.
2. Verify `StreamResolver` preserves the `ProviderStream` returned by `StreamValidator` rather than falling back to the original UNKNOWN candidate.
3. If selected type is UNKNOWN, fix validator/content sniffing rather than forcing MIME blindly.
4. Rerun Samehadaku Episode 7 E2E.
5. PASS requires actual `onRenderedFirstFrame()`.

## Build timing

Bitrise runner: 16 GB (`g2.linux.gcp.c2.4c-16g`).
- Install missing Android SDK components: 2.2 min
- Android Build for UI Testing: 5.2 min
- Instrumented Test: 2.6 min
- Total: 10.7 min

The `Android Build for UI Testing` task itself ran Gradle successfully in 5m 4s. SDK setup also consumed 2.2 min. Build-time optimization should be handled separately after the provider failure is fixed, so debugging remains controlled.

## Important

Do not mark Samehadaku green. Do not stack a blind MIME-forcing workaround until the actual `ProviderStream.type` and validator propagation are verified.
