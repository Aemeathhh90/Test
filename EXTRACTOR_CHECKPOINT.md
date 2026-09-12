# 🧩 EXTRACTOR CHECKPOINT — KAKAANIME / ANILAB

**Tanggal checkpoint:** 13 September 2026  
**Repo:** `KakaAnime/KakaAnime` — Private  
**Branch source of truth:** `main`  
**Scope:** Provider, Stream Resolver, Extractor, Stream Validation, dan E2E playback.  
**Dokumen ini:** checkpoint khusus P0 Extractor/Streaming. Tidak menggantikan `MASTER_CHECKPOINT.md`.

---

## 0. Aturan Checkpoint

- `main` adalah source of truth.
- Jangan membuat branch untuk pekerjaan provider rutin.
- Jangan menganggap provider selesai hanya karena source code/compile berhasil.
- Provider baru menjadi 🟢 setelah E2E nyata mencapai `Media3 onRenderedFirstFrame()`.
- Jangan menambah teknologi extractor kompleks sebelum failure nyata membuktikan bahwa teknologi tersebut diperlukan.
- Jika implementasi baru membuat provider yang sebelumnya PASS menjadi FAIL, gunakan versi PASS sebagai safety baseline dan lakukan perbandingan sebelum perubahan lanjutan.
- Checkpoint ini bersifat append/update: jangan menghapus sejarah PASS/FAIL sebelumnya.

---

# 1. 🎯 FINAL STREAMING GATE

```text
Search
  ↓
Anime Detail
  ↓
Episode
  ↓
Episode / Server Discovery
  ↓
Stream Resolver
  ↓
Extractor Registry
  ↓
Direct Media / Embed Resolver
  ↓
.m3u8 / .mpd / .mp4
  ↓
Stream Validator
  ↓
Media3 / ExoPlayer
  ↓
onRenderedFirstFrame()
  ↓
🟢 PROVIDER PASS
```

Compile/build sukses saja belum cukup.

---

# 2. 🧱 EXTRACTOR ARCHITECTURE

## Target architecture

```text
Provider Adapter
      ↓
Episode / Server Discovery
      ↓
Stream Resolver
      ↓
Extractor Registry
      ├─ Direct Media
      ├─ Host-specific Resolver
      ├─ JavaScript Media
      └─ Generic Embed
      ↓
ProviderStream
      ↓
Stream Validator
      ↓
Media3
```

### Status

- 🟢 Provider Adapter layer tersedia.
- 🟢 Episode/server discovery digunakan oleh provider yang sudah proven.
- 🟢 `StreamResolver` tersedia.
- 🟢 `ExtractorRegistry` tersedia.
- 🟢 `StreamValidator` tersedia.
- 🟢 `ProviderStream` menjadi model stream internal.
- 🟢 Media3 playback gate tersedia.
- 🔴 Full provider-wide validation belum selesai.

---

# 3. 🧩 EXTRACTOR REGISTRY

Current registered extractors:

```text
OtakudesuServerExtractor
KrakenFilesExtractor
PixelDrainExtractor
JavascriptMediaExtractor
GenericEmbedExtractor
GenericDirectExtractor
```

### Prinsip

- Extractor dipilih berdasarkan kemampuan `canHandle(url)`.
- Extractor diurutkan berdasarkan priority.
- Provider tidak perlu mengetahui seluruh detail host extractor.
- Extractor yang reusable harus dipakai lintas provider bila format host sama.
- Jangan membuat satu extractor khusus untuk setiap provider bila generic/reusable resolver sudah cukup.

### Status

🟢 **REGISTRY FOUNDATION PROVEN** melalui E2E Otakudesu.

---

# 4. 🔄 STREAM RESOLVER

Current direction:

```text
Input URLs
  ↓
Deduplicate
  ↓
Extractor Registry
  ↓
Parallel extraction
  ↓
ProviderStream candidates
  ↓
StreamValidator
  ↓
Validated streams preferred
  ↓
Media3
```

### Status

🟢 Parallel extractor resolution tersedia.  
🟢 Candidate deduplication tersedia.  
🟢 Stream validation tersedia.  
🟢 Proven through Otakudesu E2E.

---

# 5. 🛡️ STREAM VALIDATOR

Validator harus membedakan minimal:

- HLS → `#EXTM3U`
- DASH → `<MPD`
- MP4/other playable media → content type / valid partial response
- Redirect → final URL diperbarui
- Headers/referer → dipertahankan bila diperlukan

### Status

🟢 **FOUNDATION PROVEN** pada Otakudesu E2E.

---

# 6. 🔌 CURRENT EXTRACTORS

## 6.1 OtakudesuServerExtractor

**Priority:** highest dedicated server extractor.  
**Status:** 🟢 **PROVEN**.

Flow:

```text
Otakudesu Episode
  ↓
Server / mirror discovery
  ↓
AJAX / nonce / player data
  ↓
Mirror URL
  ↓
Host detection
  ├─ direct media
  ├─ PixelDrain
  ├─ KrakenFiles
  └─ GenericEmbed fallback
  ↓
ProviderStream
  ↓
Media3
```

### E2E evidence

- 🟢 One Piece Episode 7
- 🟢 Search
- 🟢 Detail
- 🟢 Episode
- 🟢 Stream resolution
- 🟢 Media3
- 🟢 `onRenderedFirstFrame()`

**Otakudesu extractor foundation = locked as proven baseline.**

---

## 6.2 KrakenFilesExtractor

- 🟢 Registered.
- 🟢 Reusable host extractor.
- 🟡 Wider provider coverage still needs E2E evidence.

---

## 6.3 PixelDrainExtractor

- 🟢 Registered.
- 🟢 Reusable host extractor.
- 🟡 Wider provider coverage still needs E2E evidence.

---

## 6.4 JavascriptMediaExtractor

Handles common static/JavaScript media discovery:

- direct media URLs
- `atob` / base64 patterns
- base64-like tokens
- common JS keys such as `file`, `src`, `source`, `hls`, `m3u8`, `videoUrl`, `playlist`, `contentUrl`, `stream`
- URL unescaping
- referer propagation

Status: 🟢 **PROVEN in the Otakudesu E2E path.**

---

## 6.5 GenericEmbedExtractor

Handles:

```text
player/embed URL
  ↓
fetch page
  ↓
iframe / source / video / JS media discovery
  ↓
recursive embed chain
  ↓
direct media candidate
```

Current direction:
- recursive embed resolution up to bounded depth
- no arbitrary JavaScript execution
- no WebView by default

Status: 🟢 **AVAILABLE**.  
Status across other providers: 🔴 **requires individual E2E proof**.

---

## 6.6 GenericDirectExtractor

Handles direct HTTP(S) media candidates that do not require a host-specific resolver.

Status: 🟢 available.  
Provider-wide proof: 🔴 pending.

---

# 7. 🗺️ PROVIDER MAPPING — 29 TARGETS

## Family A — Direct / generic iframe

**Anoboy, NeoNime**

Expected:

```text
Episode/player URL
  ↓
StreamResolver
  ↓
GenericEmbedExtractor
  ↓
Direct Media
```

Decision: do not create dedicated extractor unless E2E proves generic extraction insufficient.

## Family B — Base64 → iframe

**Animasu, Oploverz**

Expected:

```text
Encoded server/player data
  ↓
Base64 decode
  ↓
iframe
  ↓
StreamResolver
```

Decision: prefer one reusable Base64 mechanism instead of duplicated provider-specific code.

## Family C — AJAX → player/mirror

**Gomunime, NontonAnimeID**

Expected:

```text
Episode page
  ↓
AJAX
  ↓
player/mirror data
  ↓
iframe/direct media
  ↓
StreamResolver
```

Decision: introduce reusable AJAX player/source helper only after a real E2E failure requires it.

## Family D — API / direct source

**AnimeKompi, Kuramanime, Nimegami, Kusonime + RemoteSource providers**

Expected:

```text
Provider API
  ↓
normalized JSON
  ↓
ProviderStream
  ↓
StreamValidator
  ↓
Media3
```

Decision: reuse `RemoteSourceProvider` where schema allows; specialize only when evidence requires it.

## Family E — Custom HLS / player

**Kuronime, AnimeSail**

Escalation:

```text
static HTML
  ↓
JS extraction
  ↓
AJAX/source endpoint
  ↓
redirect/session handling
  ↓
WebView/network interception ONLY IF PROVEN NECESSARY
```

## Family F — API/source graph

**AllAnime**

Dedicated API/source adapter. Do not force AllAnime through a generic HTML provider path.

---

# 8. 📋 29 PROVIDER CHECKLIST

| # | Provider | Family | Status |
|---:|---|---|---|
| 1 | Otakudesu | Server/mirror + extractor | 🟢 E2E PASS |
| 2 | Samehadaku | HTML/player + iframe/AJAX | 🟡 Fix/ret-test |
| 3 | Animasu | Base64/iframe | 🔴 |
| 4 | AnimeIndo | API/HTML + iframe | 🔴 |
| 5 | Zoronime | Server/mirror | 🔴 |
| 6 | Anoboy | iframe → extractor | 🔴 |
| 7 | AnimeKompi | API/HTML → stream | 🔴 |
| 8 | Kuronime | Base64/iframe + custom HLS | 🔴 |
| 9 | Doronime | Remote/API | 🔴 |
| 10 | Hunter no Sekai | Remote/API | 🔴 |
| 11 | Gomunime | AJAX → mirror | 🔴 |
| 12 | NeoNime | iframe → extractor | 🔴 |
| 13 | YLNime | Remote/API | 🔴 |
| 14 | NontonAnimeID | AJAX → iframe | 🔴 |
| 15 | Animeisme | Remote/API | 🔴 |
| 16 | Animeku | Remote/API/direct | 🔴 |
| 17 | Oploverz | Base64 → iframe | 🔴 |
| 18 | Kuramanime | API/direct | 🔴 |
| 19 | Wibudesu | Remote/API | 🔴 |
| 20 | Meownime | Remote/API | 🔴 |
| 21 | Anibatch | Remote/API | 🔴 |
| 22 | Nimegami | Base64 JSON → direct | 🔴 |
| 23 | Drivenime | Remote/API | 🔴 |
| 24 | Anitoki | Remote/API/direct | 🔴 |
| 25 | RiiE | Remote/API | 🔴 |
| 26 | Kusonime | API/HTML → stream | 🔴 |
| 27 | Animekuindo | Remote/API | 🔴 |
| 28 | AnimeSail | Multi-player/iframe | 🔴 |
| 29 | AllAnime | API/source graph | 🔴 |

**Current confirmed E2E:** 1/29.  
**Target:** 29/29.

> Samehadaku is intentionally not green yet because the latest run stopped at compile-time before the streaming gate. Its compile fix is in commit `2bf480b2e389d08611ae28eb495029d2cb5214b1`; the next Bitrise run must prove playback.

---

# 9. 🚦 IMPLEMENTATION ORDER

### Batch 1 — Foundation validation

1. 🟢 Otakudesu
2. 🟡 Samehadaku

### Batch 2 — Cheap iframe

3. 🔴 Anoboy
4. 🔴 NeoNime

### Batch 3 — Base64

5. 🔴 Animasu
6. 🔴 Oploverz

### Batch 4 — API/direct

7. 🔴 Kuramanime
8. 🔴 AnimeKompi
9. 🔴 Nimegami
10. 🔴 Kusonime

### Batch 5 — AJAX/mirror

11. 🔴 Gomunime
12. 🔴 NontonAnimeID

### Batch 6 — Server/mirror

13. 🔴 Zoronime
14. 🔴 AnimeIndo

### Batch 7 — Custom player

15. 🔴 Kuronime
16. 🔴 AnimeSail

### Batch 8 — Dedicated API graph

17. 🔴 AllAnime

### Batch 9 — Remaining RemoteSource

18. 🔴 Doronime
19. 🔴 Hunter no Sekai
20. 🔴 YLNime
21. 🔴 Animeisme
22. 🔴 Animeku
23. 🔴 Wibudesu
24. 🔴 Meownime
25. 🔴 Anibatch
26. 🔴 Drivenime
27. 🔴 Anitoki
28. 🔴 RiiE
29. 🔴 Animekuindo

---

# 10. 📈 EXTRACTOR ESCALATION RULE

| Failure nyata | Eskalasi berikutnya |
|---|---|
| Episode/player URL tidak ditemukan | perbaiki provider parser / iframe extraction |
| iframe chain berhenti | improve GenericEmbedExtractor |
| Base64 payload | reusable Base64 decoder |
| AJAX player diperlukan | AJAX/source resolver |
| JS URL tersembunyi | improve JavascriptMediaExtractor |
| Packed JS | packed-JS handling |
| Redirect gagal | redirect-aware resolver |
| m3u8 ditemukan tapi playback gagal | headers/referer/session |
| HLS master bermasalah | variant selection |
| Media hanya terlihat lewat browser/network | WebView/network interception |

**WebView adalah last resort, bukan fondasi awal.**

---

# 11. 🧪 E2E TEST STRATEGY

Setiap provider wajib memiliki regression test yang membuktikan minimal:

```text
Search
→ Detail
→ Episode 7
→ getStreams()
→ HTTP(S) ProviderStream
→ Media3
→ onRenderedFirstFrame()
```

### Existing tests

- 🟢 `OtakudesuProviderE2ETest#onePieceEpisodeSevenRendersFirstFrame`
- 🟡 `SamehadakuProviderE2ETest#onePieceEpisodeSevenRendersFirstFrame` — menunggu build compile + playback proof

### CI strategy

- Run melalui Bitrise `provider_e2e`.
- Test individual provider saat debugging.
- Full 29-provider run menjadi final gate setelah individual paths stabil.

---

# 12. 🧯 RECOVERY / REGRESSION RULE

Jika provider yang sebelumnya PASS kemudian FAIL setelah perubahan:

```text
FAIL
 ↓
Identifikasi titik gagal
 ↓
Bandingkan dengan baseline PASS
 ↓
Pertahankan bagian yang terbukti bekerja
 ↓
Fix hanya bagian yang rusak / berubah
 ↓
E2E ulang
 ↓
PASS
```

Jangan melakukan rollback buta.

Checkpoint harus menyimpan bukti:
- commit
- build number bila tersedia
- provider
- episode
- titik failure/pass
- perubahan yang menyebabkan regression bila diketahui.

---

# 13. 📝 CHECKPOINT LOG

### 13 Sep 2026 — Initial Extractor Checkpoint

- 🟢 Extractor Registry foundation tersedia.
- 🟢 Stream Resolver foundation tersedia.
- 🟢 Stream Validator foundation tersedia.
- 🟢 Otakudesu Server Extractor terbukti.
- 🟢 KrakenFiles/PixelDrain extractor terdaftar.
- 🟢 JavascriptMediaExtractor terbukti pada Otakudesu path.
- 🟢 Generic Embed/Direct extractor tersedia.
- 🟢 Otakudesu One Piece Episode 7 mencapai `onRenderedFirstFrame()`.
- 🟡 Samehadaku implementation diperbarui ke HTML-first `v2.samehadaku.how`.
- 🔴 Samehadaku E2E terbaru belum terbukti karena run terakhir berhenti pada compile error.
- 🟢 Compile error `Sequence<ProviderEpisode>` → `List<ProviderEpisode>` diperbaiki pada commit `2bf480b2e389d08611ae28eb495029d2cb5214b1`.
- ⏳ Next gate: Bitrise `provider_e2e` ulang untuk Samehadaku.

---

# 14. 🔖 QUICK RESUME

Kalau project berhenti dan ingin dilanjutkan, gunakan urutan:

```text
1. Baca EXTRACTOR_CHECKPOINT.md
2. Cek status provider terakhir
3. Cek commit terakhir
4. Jalankan E2E provider yang sedang aktif
5. Jika FAIL → debug titik failure
6. Jika PASS → tandai 🟢 dan lanjut batch berikutnya
7. Jangan menambah extractor kompleks tanpa bukti kebutuhan
```

### Current next action

**Jalankan ulang Bitrise `provider_e2e` untuk Samehadaku dari `main`.**

Jika PASS:

```text
Samehadaku 🟢
2 / 29 E2E green
↓
Anoboy + NeoNime
```

Jika FAIL:

```text
Samehadaku 🔴/🟡
↓
ambil log
↓
tentukan titik failure
↓
fix targeted
↓
E2E ulang
```

---

## 🔒 Dokumen terkait

- `MASTER_CHECKPOINT.md` — checkpoint keseluruhan project.
- `PROVIDER_MAPPING_2026-09-13.md` — peta teknis 29 provider.
- `MASTER_CHECKPOINT_UPDATE_2026-09-13_BUILD44.md` — sejarah Build #44 dan bukti Otakudesu E2E.

**Extractor checkpoint ini tidak menggantikan dokumen-dokumen tersebut.**
