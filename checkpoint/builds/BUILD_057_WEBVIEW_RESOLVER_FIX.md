# Build #57 — Samehadaku WebView Resolver Fix

Status: 🔴 BUG — fix in progress

Build #57 proved that Samehadaku reaches stream resolution but the selected stream remains UNKNOWN after validation. The next fix follows CloudStream's proven WebView interception pattern for JavaScript-driven player pages: execute the player page in Android WebView and capture m3u8/mpd/mp4 requests.

References audited:
- CloudStream WebViewResolver pattern.
- OCE SamehadakuPage configuration using WebView interception for `(m3u8|mp4)`.

Native AniLab goal:
Episode page → player/iframe → WebView execution → media request interception → typed ProviderStream → StreamValidator → Media3 → onRenderedFirstFrame().

Samehadaku remains 🔴 until the real `onRenderedFirstFrame()` gate passes.
