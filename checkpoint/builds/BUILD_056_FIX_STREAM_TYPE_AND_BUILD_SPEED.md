# Build #56 — Samehadaku stream-type fix + build-speed optimization

Date: 2026-09-13
Repo: KakaAnime/KakaAnime
Branch: main

## Classification
- 🔴 BUG: Samehadaku E2E selected a stream that Media3 treated as progressive and failed with `UnrecognizedInputFormatException`.
- 🟡 WORKAROUND/TAMBALAN: None added to force Samehadaku to HLS/DASH blindly.
- 🟢 ENHANCEMENT: Gradle parallel workers and local build caching enabled.

## Build #56 evidence
- Gradle APK/test APK build succeeded in 5m 4s.
- Instrumented Samehadaku Episode 7 failed with `ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED` and `UnrecognizedInputFormatException`.
- Error listed only progressive extractors (Mp4Extractor/TsExtractor/MatroskaExtractor/etc.).
- Total Bitrise runtime was 10.7 min: SDK 2.2 min, Android Build for UI Testing 5.2 min, instrumented test 2.6 min.

## Root cause analysis
Media3 falls back to progressive extraction when a media URL has no recognized extension and no explicit MIME type. GitHub Media3 issue #2898 documents the same behavior for extension-less HLS URLs.

The KakaAnime resolver already validates streams, but the E2E test previously selected the first HTTP(S) candidate regardless of `ProviderStream.type`. That could select an UNKNOWN wrapper/candidate even when a typed candidate existed later.

## Architecture before
Samehadaku episode → extractor/resolver → candidate list → first HTTP(S) candidate → Media3 MIME only when type known → progressive fallback for UNKNOWN → first-frame failure.

## Architecture after
Samehadaku episode → extractor/resolver → HTTP response/content sniffing → normalized HLS/DASH/MP4 type → E2E logs all candidates → typed candidates preferred → explicit Media3 MIME → onRenderedFirstFrame gate.

## Changes
1. `StreamValidator.kt`
   - UNKNOWN streams no longer use a Range request.
   - UNKNOWN streams receive full manifest/body sniffing up to 64 KiB.
   - Added media Accept header for HLS/DASH/video.
   - HLS/DASH detection uses final URL, Content-Type, and manifest body.
   - Redirected final URL and detected type are preserved in `ProviderStream`.

2. `SamehadakuProviderE2ETest.kt`
   - Uses Android `Log` diagnostics so `E2E_STREAM[...]` is visible in CI logs.
   - Prefers HLS/DASH/MP4 candidates over UNKNOWN candidates.
   - Fails explicitly if resolver still returns only UNKNOWN stream types.
   - Keeps explicit Media3 MIME mapping and `onRenderedFirstFrame()` as the gate.

3. `gradle.properties`
   - `org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m`
   - `org.gradle.workers.max=4`
   - `org.gradle.parallel=true`
   - `org.gradle.caching=true`

## Validation
Not yet validated by a new Bitrise build. Next run must use `main` and inspect:
- `packageDebug` / APK build time.
- `E2E_STREAM[...]` diagnostics.
- Selected `ProviderStream.type`.
- Media3 first-frame result.

## Guardrails
- Do not mark Samehadaku green until `onRenderedFirstFrame()` passes.
- Do not force HLS/DASH based only on provider name.
- If the next E2E still fails, use the new `E2E_STREAM` diagnostics to determine whether the remaining failure is extractor resolution, HTTP response classification, headers, or actual manifest/playability.
