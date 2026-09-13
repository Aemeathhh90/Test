# Build Checkpoint — UNKNOWN Stream Resolver Fix

**Tanggal:** 13 September 2026
**Provider:** Samehadaku
**Target:** One Piece Episode 7 E2E
**Status:** 🟡 FIX APPLIED — E2E VALIDATION PENDING

## Error

E2E stopped at:

`Samehadaku selected an UNKNOWN stream type after resolver validation`

The flow had already passed Search → Anime Detail → Episode 7 → Resolver → Stream discovery.

## Audit / Reference Decision

Reference check: **YA**.

CloudStream's extractor model explicitly carries stream type information (M3U8/DASH/video) instead of treating an unknown response as a playable type. CloudStream/OCE also uses a provider → extractor boundary and WebView-based resolution for JavaScript-driven hosts.

Reference was judged compatible with AniLab's existing architecture. AniLab remains native and does not depend on CloudStream.

## Root Cause Found

`StreamValidator` previously allowed:

`StreamType.UNKNOWN + HTTP 206 => valid`

HTTP 206 only proves partial byte delivery; it does not prove a supported media format. This allowed an UNKNOWN `ProviderStream` to survive validation.

A second architecture gap was found in `StreamResolver`: browser fallback was attempted only against the original input URLs, not the URLs produced by extractors (for example iframe/player/host URLs).

## Architecture Before

```text
Episode URL
 ↓
Extractor
 ↓
Extracted host/player URL
 ↓
Validator
 ↓
UNKNOWN + 206 could be accepted
 ↓
E2E fails on UNKNOWN
```

Browser fallback could also receive only the original episode URL.

## Architecture After

```text
Episode URL
 ↓
Extractor
 ↓
Extracted host/player URLs
 ↓
Validator
 ├─ HLS / DASH / MP4 proven → typed stream
 └─ UNKNOWN → rejected
 ↓
if no typed stream
 ↓
Browser fallback over extracted URLs + original input URLs
 ↓
HLS / DASH / MP4
 ↓
Media3
 ↓
onRenderedFirstFrame()
```

## Code Changes

### `StreamValidator.kt`

- UNKNOWN is no longer accepted merely because the response is HTTP 206.
- Response probing now checks content type/body/signature.
- MP4 can be recognized from `video/mp4`, known URL suffix, or the ISO-BMFF `ftyp` signature.
- Unsupported/unknown responses remain rejected.

Commit: `a798c7400fb566f36e39dcf3a4f6f88dea5b609b`

### `StreamResolver.kt`

- Browser fallback now tries all extracted URLs as well as original input URLs.
- This allows JS-driven iframe/player hosts discovered by provider-specific extractors to reach WebView resolution.
- UNKNOWN streams are not intentionally promoted to a guessed media type.

Commit: `d352f22d8082c8ffadf671b233b5a01794137be8`

## Validation Required

Next provider E2E must prove:

1. Search passes.
2. Detail passes.
3. Episode 7 passes.
4. Resolver returns a typed stream.
5. Media3 accepts the correct MIME/format.
6. `onRenderedFirstFrame()` fires.

Samehadaku remains 🟡 until step 6 is proven.

## Impact

- Search/Detail/Episode architecture is unchanged.
- Provider Gate remains unchanged.
- No fake green status.
- This fix is reusable for other providers with extension-less/signed media URLs and JS-driven player pages.
