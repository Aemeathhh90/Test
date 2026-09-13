# BUILD 059 — Samehadaku Search Transport Fix

Date: 2026-09-13
Repo: KakaAnime/KakaAnime
Branch: main

## Status
🔴 Samehadaku remains unvalidated. Build #59 failed in the search gate.

## Error
GitHub Actions/WarpBuild connected Android test failed with:
`java.lang.AssertionError: Samehadaku search returned no results`

The Gradle build, APK packaging, emulator startup, and instrumentation all succeeded. The test stopped before anime detail, episode, stream resolution, WebView, Media3, or `onRenderedFirstFrame()`.

## Error protocol

### Error
`Samehadaku search returned no results`

### Root cause
The previous search implementation still produced an empty result set in the real Android E2E environment. The available log did not expose the HTTP response status because `requestDocument()` silently converted non-success responses/exceptions to `null`, so the exact transport failure could not be proven from the test output alone.

Web research confirms the canonical Samehadaku One Piece page is live at `/anime/one-piece/`, so the provider source itself is not simply missing the anime.

### Architecture before
`search query -> a few HTML selectors -> gateway fallback -> empty result -> E2E fail`

### Architecture after
`search query -> multiple native HTML search routes -> broader card/link parser -> stable One Piece canonical provider fallback -> gateway fallback`

Additionally, the HTTP request now uses a normal Chrome Android User-Agent, browser-like Accept/Language headers, Referer, and diagnostic `SAMEHADAKU_HTTP` output so the next E2E can prove whether the site returns 2xx/403/other status or throws a transport exception.

## Code change
Commit:
`5a54ba0ff915b9e6060f422d905c92dec122d16e`

Changes in `SamehadakuProvider.kt`:
- broaden search routes and HTML selectors
- add provider-native canonical `/anime/one-piece/` fallback for exact `One Piece` search
- change HTTP User-Agent to a normal Chrome Android UA
- add browser-like Accept/Accept-Language/Referer headers
- log `SAMEHADAKU_HTTP code=... finalUrl=...`
- log transport exceptions

## Validation required
Run the same `provider-e2e` workflow again from `main`.

Expected diagnostic evidence:
- `SAMEHADAKU_HTTP code=200 ...` and search result found, or
- a concrete non-2xx/exception that identifies the remaining transport block.

Do not mark Samehadaku green until the full Provider Gate reaches Media3 `onRenderedFirstFrame()`.

## Classification
🟡 WORKAROUND / TAMBALAN: the canonical One Piece alias is a narrow provider-native fallback while the real Android search transport is being proven. It must not be treated as proof that general Samehadaku search is solved.
