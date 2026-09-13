# Build #57 — Samehadaku Browser-Backed Resolver Implementation

Date: 13 September 2026
Repository: KakaAnime/KakaAnime
Branch: main
Status: 🔴 BUG → fix implemented; Bitrise validation pending

## Error
Build #57 failed before Media3 with:

`Samehadaku selected an UNKNOWN stream type after resolver validation`

## Root cause
HTTP/regex extraction can stop at a JavaScript-driven player/iframe and never observe the final media request. CloudStream/OCE references use browser-backed interception for this class of player page.

## Architecture before
Samehadaku episode → player/host URL → HTTP extractors → validator → UNKNOWN → E2E stops.

## Architecture after
Samehadaku episode → HTTP/host extractors → validator → if no typed stream, browser-backed resolver → capture m3u8/mpd/mp4/webm request → typed ProviderStream → validator/player.

## Native AniLab changes
- Added `WebViewStreamResolver.kt` using Android WebView request interception.
- Added `BrowserMediaResolver.kt` as the resolver-facing adapter.
- Added `AppContextProvider.kt` and `KakaAnimeApplication.kt` so browser infrastructure has an application context.
- Registered `KakaAnimeApplication` in `AndroidManifest.xml`.
- Updated `StreamResolver.kt` to use the browser fallback only when validation does not produce a typed stream.
- Captured WebView cookies and propagated them to the resulting ProviderStream headers.
- No blind UNKNOWN → HLS forcing was added.

## Reference basis
CloudStream's WebViewResolver pattern executes player JavaScript and observes media URLs; OCE's SamehadakuPage configuration explicitly uses WebView interception for `(m3u8|mp4)`. Android's `WebViewClient.shouldInterceptRequest` is designed to observe resource requests while returning null lets WebView continue loading the resource.

## Validation gate
Build #58 must prove:

`Samehadaku Episode 7 → typed stream → Media3 → onRenderedFirstFrame()`

Only the actual first-frame callback can turn Samehadaku green.
