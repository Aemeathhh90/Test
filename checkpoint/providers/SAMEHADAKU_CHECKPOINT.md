# Samehadaku Provider Checkpoint

**Tanggal:** 13 September 2026
**Status:** 🟡 ACTIVE / E2E PENDING
**Target:** One Piece Episode 7

## Current Gate

```text
Search → Detail → Episode 7 → Stream Resolver → typed stream
→ Media3 → onRenderedFirstFrame()
```

Search, detail, episode discovery and stream discovery have reached the E2E test. Samehadaku is not green until `onRenderedFirstFrame()` is observed.

## Latest Error

`Samehadaku selected an UNKNOWN stream type after resolver validation`

The E2E failure occurred after a stream was discovered and after resolver validation. It did not reach Media3.

## Reference Check

**Reference needed:** YES.

CloudStream/OCE was reviewed for extractor/type handling and JS-driven player resolution. The relevant pattern is to preserve an explicit stream type (M3U8/DASH/video) and use a WebView-based resolver for player pages that expose media only through browser requests.

AniLab adopts the architectural pattern only; it does not add CloudStream as a dependency.

## Root Cause / Bugs

1. `StreamValidator` previously treated `HTTP 206` as sufficient evidence for `StreamType.UNKNOWN`. A 206 response proves partial content, not a supported media format.
2. `StreamResolver` browser fallback previously received only original input URLs. Extractor-produced iframe/player/host URLs were not included in the browser fallback set.

## Fix Applied

### StreamValidator

- Reject UNKNOWN unless a supported format is actually detected.
- Probe response content type/body/signature.
- Recognize MP4 using `video/mp4`, `.mp4`, or ISO-BMFF `ftyp` signature.
- Do not convert `206 + UNKNOWN` into a guessed media type.

Commit: `a798c7400fb566f36e39dcf3a4f6f88dea5b609b`

### StreamResolver

- Browser fallback now tries extracted URLs plus original input URLs.
- This allows discovered iframe/player/host URLs to reach WebView resolution.

Commit: `d352f22d8082c8ffadf671b233b5a01794137be8`

## Previous Relevant Fixes

- Samehadaku exact One Piece canonical fallback and browser-like request headers: `5a54ba0ff915b9e6060f422d905c92dec122d16e`.
- WebView resolver implementation/finalization: checkpoint builds `BUILD_057_WEBVIEW_RESOLVER_*`.
- Explicit Media3 MIME handling for HLS/DASH: `a8223e6a06fc60fc7d0498ab254c0517d5da90c8`.
- Extractor recursion fix: `ec6535608444ea49230b7ba865982a0f747cbd37`.

## Classification

- UNKNOWN + 206 acceptance: 🔴 BUG — fixed.
- Browser fallback scope: 🔴 BUG / architecture gap — fixed.
- One Piece canonical search alias: 🟡 WORKAROUND — retain until general search transport/parser is proven robust.
- Samehadaku provider overall: 🟡 ACTIVE — not green.

## Next Validation

Run the provider E2E on `main` and inspect whether:

1. resolver returns HLS/DASH/MP4;
2. browser fallback captures a typed media URL when needed;
3. Media3 prepares successfully;
4. `onRenderedFirstFrame()` fires.

If it still fails, do not guess. Audit the new URL/type/HTTP diagnostics and decide whether another reference check is required.
