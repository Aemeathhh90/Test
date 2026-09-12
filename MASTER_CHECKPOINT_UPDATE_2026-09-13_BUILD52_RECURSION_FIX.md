# KakaAnime / AniLab — Build #52 Checkpoint

**Tanggal:** 13 September 2026  
**Source of truth:** `main`  
**Build:** Bitrise #52  
**Provider:** Samehadaku  
**Target:** One Piece Episode 7 → Media3 → `onRenderedFirstFrame()`

## Result

- Bitrise Build #52 overall: SUCCESS at CI/build level.
- Android Instrumented Test: **FAIL** before playback validation.
- Failure: `java.lang.StackOverflowError`.
- Stack trace repeatedly alternated between `SamehadakuEpisodeExtractor` and `ExtractorRegistry` constructors.
- Therefore Samehadaku remains **🔴 BUG**, not green.
- `onRenderedFirstFrame()` was not reached; no provider PASS is claimed.

## Root Cause

`ExtractorRegistry` attempted to conditionally add `SamehadakuEpisodeExtractor` using:

```kotlin
SamehadakuEpisodeExtractor().takeIf { includeSamehadakuEpisodeExtractor }
```

Kotlin still constructs `SamehadakuEpisodeExtractor()` before evaluating `takeIf`. The Samehadaku extractor constructor creates a nested `StreamResolver(ExtractorRegistry(includeSamehadakuEpisodeExtractor = false))`, which recursively constructs another Samehadaku extractor before the boolean can prevent it. This produced the StackOverflowError seen in Build #52.

## Fix

Commit:
`ec6535608444ea49230b7ba865982a0f747cbd37`

File:
`app/src/main/java/com/kakaanime/app/provider/extractor/ExtractorRegistry.kt`

The registry now constructs `SamehadakuEpisodeExtractor()` **only inside an explicit `if (includeSamehadakuEpisodeExtractor)` block**. The disabled nested registry therefore contains no Samehadaku extractor and can safely be created by `SamehadakuEpisodeExtractor`.

## Classification

**🔴 BUG — confirmed root cause in our implementation.**

Not a provider/site limitation and not a Media3 playback failure. Do not classify as BLOCKED.

## Next Gate

1. Build/re-run Bitrise `provider_e2e` on `main`.
2. Confirm the StackOverflowError is gone.
3. Re-evaluate Search → Detail → Episode 7 → Stream Resolution → Media3 → `onRenderedFirstFrame()`.
4. Only mark Samehadaku 🟢 if the first frame is actually rendered.

## Rule

Build/CI success alone does not make the provider green. The Provider Gate remains locked to real Media3 first-frame proof.
