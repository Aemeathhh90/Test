# Bug Hunt — MainActivity Stream Session Race Fix

Date: 2026-09-14
Branch: `main`

## Status

🟢 Fixed by code audit; build verification remains pending.

## Fix

`MainActivity.kt` now assigns a monotonically changing `streamSessionId` whenever the stream resolver effect starts. The delayed 15-second watched timer captures that session ID and records watched only if the same session is still active and its first frame has been rendered.

The first-frame callback records the active rendered session instead of using a global boolean.

## Commit

`abf16b1b37fbedbaa6d7eae7377a3a805db73691`

## Verification

Static audit confirms the old delayed coroutine can no longer record a newer resolver session because the captured session ID must match the current session ID. Build/CI status is still pending and therefore is not claimed green.

## Next

Audit provider health isolation and season-aware search deduplication.
