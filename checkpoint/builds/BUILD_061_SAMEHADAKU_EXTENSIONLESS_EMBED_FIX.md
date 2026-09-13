# Build/Change 061 — Samehadaku Extensionless Embed Fix

**Tanggal:** 13 September 2026  
**Repo:** `KakaAnime/KakaAnime`  
**Branch:** `main`  
**Status:** 🟡 FIX APPLIED — E2E PENDING

## 🔴 Error

Bitrise Build #57 reached:

```text
Search → Anime Detail → Episode 7 → Resolver → Stream found → Validator
→ selected stream type = UNKNOWN → E2E stopped
```

Otakudesu remains 🟢 E2E PASS and is LOCKED.

## 🔎 Root cause

The Samehadaku `player_ajax` flow can return an embed/player URL without a `.m3u8`, `.mpd`, or `.mp4` suffix. The previous `SamehadakuEpisodeExtractor` handed that URL to the generic host resolver. If that path could not produce a typed stream, AniLab could still end up with an UNKNOWN candidate.

Reference audit found the CloudStream Samehadaku implementation performs a second-stage GET of the embed page and extracts the actual media URL from `<video><source>`. It also handles hosts that expose a `data-page` payload containing a media URL.

Reference sources:
- reCloudStream: https://github.com/recloudstream/cloudstream
- CloudStream Samehadaku implementation reviewed during the audit.

## 🏗️ Architecture before

```text
player_ajax
  ↓
embed URL
  ↓
host extractor / StreamResolver
  ↓
UNKNOWN or typed stream
```

The extensionless embed page itself was not parsed by the Samehadaku-specific extractor.

## 🟢 Architecture after

```text
player_ajax
  ↓
embed URL
  ↓
host extractor / StreamResolver
  ↓
typed stream? ── yes → return typed stream
      │
      no
      ↓
GET embed page
      ↓
<video><source> / <video src> / data-page URL
      ↓
ProviderStream
      ↓
outer StreamValidator
      ↓
HLS / DASH / MP4 classification
      ↓
Media3
      ↓
onRenderedFirstFrame()
```

## 🔧 Code change

`SamehadakuEpisodeExtractor.kt` now:

1. Keeps the existing host extractor path first.
2. If that path does not return a typed stream, performs a Samehadaku-specific second-stage embed-page request.
3. Extracts URLs from:
   - `video source[src]`
   - `source[src]`
   - `video[src]`
   - corresponding `data-src` attributes
   - `#app[data-page]` / `[data-page]` JSON payloads containing `url`
4. Preserves the embed URL as `Referer`.
5. Leaves extensionless media as `StreamType.UNKNOWN` initially so the existing outer `StreamValidator` can prove the actual type from HTTP/content instead of guessing.
6. Does **not** globally map UNKNOWN to MP4.
7. Does **not** change Otakudesu or weaken StreamValidator.

Commit containing the code change: `68fb9f51d689b8139d507b2393a05f90a1909fbd`

## 🧪 Validation

Not yet validated by Bitrise E2E in this checkpoint.

Required gate:

```text
Samehadaku One Piece Episode 7
→ stream resolved
→ validator proves supported type
→ Media3 playback
→ onRenderedFirstFrame()
```

Only then may Samehadaku change from 🟡 to 🟢.

## 📌 Impact

- Samehadaku only: no global resolver behavior change.
- StreamValidator remains strict.
- Otakudesu path remains untouched/LOCKED.
- Browser fallback remains available for cases that still require JS execution.
- This is a native AniLab implementation of the proven CloudStream pattern, not a CloudStream dependency.
