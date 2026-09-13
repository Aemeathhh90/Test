# Build #50 — Samehadaku E2E Fix

**Date:** 13 September 2026  
**Provider:** Samehadaku  
**Workflow:** `provider_e2e`

## Failure

The E2E selected `/anime/one-piece-heroines` because the test used broad `title.contains("One Piece")` matching.

**Failure stage:** Search result selection.  
**Not a streaming failure.**  
Samehadaku remained 🟡.

## Fix

Commit: `9528b96082b2be7003afdf16114b09d6cf1dd7e1`  
File: `app/src/androidTest/java/com/kakaanime/app/provider/SamehadakuProviderE2ETest.kt`

The test now selects exact title `One Piece` or canonical `/anime/one-piece` path instead of arbitrary first match.

## Next Gate

Search → Detail → Episode 7 → getStreams() → Media3 → `onRenderedFirstFrame()`.

Only real first-frame proof can make Samehadaku 🟢.
