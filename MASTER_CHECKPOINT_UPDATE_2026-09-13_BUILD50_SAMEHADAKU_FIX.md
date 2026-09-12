# KakaAnime / AniLab — Samehadaku E2E Fix Checkpoint

**Tanggal:** 13 September 2026  
**Repo:** `KakaAnime/KakaAnime` — Private  
**Source of truth:** `main`

## Build #50 Failure

Bitrise `provider_e2e` correctly executed:

`SamehadakuProviderE2ETest#onePieceEpisodeSevenRendersFirstFrame`

The run failed before stream resolution / Media3 playback because the E2E test selected the wrong search result:

`/anime/one-piece-heroines`

The previous selector used a broad `title.contains("One Piece")`, so `One Piece Heroines` matched before the canonical `One Piece` result.

**Failure stage:** Search result selection → wrong anime selected.  
**Not a streaming failure.**  
**Samehadaku remains 🟡 and is not green.

## Fix Applied

Commit:

`9528b96082b2be7003afdf16114b09d6cf1dd7e1`

File:

`app/src/androidTest/java/com/kakaanime/app/provider/SamehadakuProviderE2ETest.kt`

The regression test now selects the canonical result by either:

- exact title `One Piece`, or
- canonical `/anime/one-piece` path.

It no longer falls back to an arbitrary first search result for this fixed E2E case.

## Next Gate

Run the same Bitrise provider E2E again.

Expected progression:

```text
Search canonical One Piece
 → Detail
 → Episode 7
 → getStreams()
 → HTTP(S) stream
 → Media3
 → onRenderedFirstFrame()
```

Only a real `onRenderedFirstFrame()` result can move Samehadaku to 🟢.

## Checkpoint Rule

This fix is recorded separately so the historical MASTER/checkpoint data remains intact. Future code changes must continue to be accompanied by a checkpoint update/commit.
