# Build #52 — Samehadaku Recursion Fix

**Date:** 13 September 2026  
**Provider:** Samehadaku  
**Target:** One Piece Episode 7 → Media3 → `onRenderedFirstFrame()`

## Failure

Bitrise Build #52 succeeded at CI/build level, but the Android instrumented test failed before playback with `java.lang.StackOverflowError`. The stack alternated between `SamehadakuEpisodeExtractor` and `ExtractorRegistry`.

**Classification:** 🔴 BUG in our implementation. `onRenderedFirstFrame()` was not reached.

## Root Cause

The registry used `SamehadakuEpisodeExtractor().takeIf { includeSamehadakuEpisodeExtractor }`. Kotlin constructs the extractor before `takeIf` evaluates the condition. The extractor constructor creates a nested `ExtractorRegistry(false)`, which recursively constructs the Samehadaku extractor again.

## Architecture before

```text
ExtractorRegistry
 ↓
SamehadakuEpisodeExtractor
 ↓
StreamResolver
 ↓
ExtractorRegistry(false)
 ↓
SamehadakuEpisodeExtractor
 ↓
∞ → StackOverflowError
```

## Fix

Commit: `ec6535608444ea49230b7ba865982a0f747cbd37`  
File: `app/src/main/java/com/kakaanime/app/provider/extractor/ExtractorRegistry.kt`

`SamehadakuEpisodeExtractor()` is now constructed only inside an explicit `if (includeSamehadakuEpisodeExtractor)` block. The nested host registry excludes the Samehadaku episode extractor.

## Architecture after

```text
Samehadaku Provider
 ↓
SamehadakuEpisodeExtractor
 ↓
Host-only ExtractorRegistry
 ↓
ProviderStream
 ↓
Validator
 ↓
Media3
 ↓
onRenderedFirstFrame()
```

## Next Gate

Rerun Bitrise and verify recursion is gone, then continue Search → Detail → Episode 7 → Stream Resolution → Media3 → first frame.
