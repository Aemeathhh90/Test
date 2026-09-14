# Bug Hunter — Provider Playback Gate — 2026-09-14

## Scope
Grouped Fix P0: provider/extractor playback safety and Media3 handoff.

## Audit finding
The provider extractor pipeline could validate candidates, fail to obtain a typed candidate, and then return raw extracted URLs through a final fallback. That allowed an `UNKNOWN` stream to reach later normalization/player selection. Historical runtime evidence showed this failure mode reaching Media3 and producing `ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED` / `UnrecognizedInputFormatException`.

A second handoff issue was confirmed: `NormalizedStream` carries stream type and headers, but the current UI/player boundary reduced the result to a URL before `PlayerCore`. `PlayerCore` then created a bare `MediaItem.fromUri(url)`, leaving Media3 to guess the container.

## Changes
1. `StreamResolver.kt`
   - returns only HTTP(S) streams that passed validation and have a known supported `StreamType`;
   - keeps WebView as a fallback for JavaScript-driven player pages;
   - rejects `UNKNOWN` WebView results;
   - removes the raw `extracted` fallback entirely.

2. `PlayerCore.kt` — commit `b3a77f546be5324f7037141c8c87fd3a82969fae`
   - builds `MediaItem` explicitly;
   - supplies HLS/DASH/MP4/WebM MIME type when the resolved URL exposes a matching media extension;
   - keeps the player API backward-compatible for the current call sites.

## Decision
- 🟢 Keep ProviderEngine, SmartProviderRouter, ProviderPlaybackResolver, deduplication, normalization, validator, extractor registry, and WebView fallback.
- 🔴 Remove the unsafe raw-stream escape path.
- 🟡 Keep provider-specific gateway fallbacks temporarily; audit/remove unproven providers separately.
- 🔴 Do not add new providers during Bug Hunt.
- 🟡 Media3 handoff is improved, but headers and provider-supplied MIME type still need to be carried end-to-end where URL extension is insufficient.

## Verification
The source was re-fetched from `main` before modification and the PlayerCore change was committed directly to `main`. No first-frame evidence exists yet. Android Actions build evidence is still not green, so this checkpoint does not claim release-build or provider playback success.

## Next
Audit and repair the remaining metadata handoff so `NormalizedStream.type` and `NormalizedStream.headers` survive into Media3. Then run the smallest available provider/runtime test. Provider may only become 🟢 after actual `onRenderedFirstFrame()` evidence.
