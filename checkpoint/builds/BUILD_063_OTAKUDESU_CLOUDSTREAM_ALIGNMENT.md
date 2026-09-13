# BUILD 063 — Otakudesu CloudStream Alignment

## Classification
🔴 BUG FIX + 🟡 RESOLVER ALIGNMENT

## Evidence
The latest Otakudesu E2E build successfully compiled, built the APK, installed the emulator, and reached the provider stage, but failed with `Otakudesu returned no streams for episode 7`.

## Reference audit
CloudStream/Yuzono OtakuDesu PR #899 explicitly targets `No available videos` and changes the mirrorstream script selection, AJAX headers, Base64/JSON mirror parsing, AJAX response parsing, and host handling. The reference flow is episode -> mirrorstream script -> nonce/action -> AJAX -> `data-content` -> JSON (`id`, `i`, `q`) -> AJAX -> response `data` -> Base64 -> iframe/source/video -> host extractor.

## Root cause found in AniLab
AniLab's AJAX helper was returning only a substring from every AJAX response. That is unsafe for the mirror response because the response must be parsed as JSON and its `data` field decoded. The same helper was used for nonce and mirror responses, conflating two different response shapes.

## Fix
- Added `postAjaxRaw()` so AJAX responses are preserved intact.
- Added `extractAjaxData()` for JSON `data` extraction with fallback parsing.
- Nonce is extracted from the raw AJAX response.
- Mirror response is extracted from `data`, then Base64-decoded to HTML.
- Mirror entries remain a dedicated `MirrorEntry(id, i, q)` model.
- Mirror `data-content` parsing now prefers `JSONObject`, with the existing tuple fallback retained.
- Added OtakuDesu `Referer` to AJAX requests.
- Mirrorstream script discovery now accepts the explicit `window.__x__nonce` marker as well as existing fallbacks.
- Kept AniLab's existing typed `ProviderStream` -> `StreamValidator` -> Media3 architecture unchanged.
- Kept direct-media query-string recognition intact.

## Reference-derived host coverage
The CloudStream reference identifies Filedon/UserVideo/UserDrive/SameVideo, DesuStream family, VidHide, StreamWish/FileLions, Mp4upload, YourUpload/Yuplod, and Blogger as relevant hosts. AniLab currently has native coverage for some of these and generic fallback for others; no unverified host is marked green.

## Validation
🟡 Pending dedicated Otakudesu E2E.

Do not mark Otakudesu green until the test proves `onRenderedFirstFrame()`.

## Commit
`23fec8244f11415c8df1c9e9fd4f82ee867fc4c0`
