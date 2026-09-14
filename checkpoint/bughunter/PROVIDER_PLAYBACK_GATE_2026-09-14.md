# Bug Hunter — Provider Playback Gate — 2026-09-14

## Scope
Grouped Fix P0: provider/extractor playback safety.

## Audit finding
The provider extractor pipeline could validate candidates, fail to obtain a typed candidate, and then return raw extracted URLs through a final fallback. That allowed an `UNKNOWN` stream to reach later normalization/player selection. Historical runtime evidence showed this failure mode reaching Media3 and producing `ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED` / `UnrecognizedInputFormatException`.

## Change
`app/src/main/java/com/kakaanime/app/provider/extractor/StreamResolver.kt`

The resolver now:
- returns only HTTP(S) streams that passed validation and have a known supported `StreamType`;
- keeps WebView as a fallback for JavaScript-driven player pages;
- rejects `UNKNOWN` WebView results;
- removes the raw `extracted` fallback entirely.

## Decision
- 🟢 Keep ProviderEngine, SmartProviderRouter, ProviderPlaybackResolver, deduplication, normalization, validator, extractor registry, and WebView fallback.
- 🔴 Remove the unsafe raw-stream escape path.
- 🟡 Keep provider-specific gateway fallbacks temporarily; audit/remove unproven providers separately.
- 🔴 Do not add new providers during Bug Hunt.

## Verification
Source was re-fetched from `main` after the change and the new resolver content was confirmed. Android Actions build evidence is still not green; recent runs have failed before exposing usable step logs. Therefore this checkpoint does **not** claim release-build or first-frame success.

## Next
Audit the Media3 handoff and provider-specific candidate headers/type handling, then run the smallest available provider/runtime test. Only mark provider green after actual `onRenderedFirstFrame()` evidence.
