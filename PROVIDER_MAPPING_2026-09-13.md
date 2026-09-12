# 🧭 KakaAnime Provider Mapping — 13 September 2026

**Repository:** `KakaAnime/KakaAnime`
**Branch:** `main`
**Purpose:** Pemetaan teknis seluruh target provider sebelum implementasi/E2E provider-by-provider.
**Scope:** 29 provider yang terdaftar di `ProviderFactory`.

> Dokumen ini adalah peta implementasi, bukan bukti provider sudah bekerja. Provider hanya boleh diberi status 🟢 setelah E2E mencapai `Media3 onRenderedFirstFrame()`.

---

## 1. Target 29 Provider

| # | Provider | ID | Mapping family | Implementasi KakaAnime | E2E |
|---:|---|---|---|---|---|
| 1 | Otakudesu | `otakudesu` | Server/mirror + extractor | Dedicated | 🟢 PASS |
| 2 | Samehadaku | `samehadaku` | HTML/player + iframe/AJAX | Dedicated | 🔴 Pending |
| 3 | Animasu | `animasu` | Base64/iframe | Remote adapter → Base64/iframe | 🔴 |
| 4 | AnimeIndo | `animeindo` | API/HTML + iframe | Remote adapter → API/iframe | 🔴 |
| 5 | Zoronime | `zoronime` | Server/mirror | Remote adapter → server resolver | 🔴 |
| 6 | Anoboy | `anoboy` | iframe → extractor | Remote adapter → generic extractor | 🔴 |
| 7 | AnimeKompi | `animekompi` | API/HTML → stream | Remote adapter → API/direct | 🔴 |
| 8 | Kuronime | `kuronime` | Base64 → iframe + custom HLS | Dedicated/custom resolver | 🔴 |
| 9 | Doronime | `doronime` | Remote/API | Remote adapter | 🔴 |
| 10 | Hunter no Sekai | `hunter-no-sekai` | Remote/API | Remote adapter | 🔴 |
| 11 | Gomunime | `gomunime` | AJAX → mirror → iframe/MP4 | Dedicated resolver candidate | 🔴 |
| 12 | NeoNime | `neonime` | iframe → extractor | Remote adapter → generic extractor | 🔴 |
| 13 | YLNime | `ylnime` | Remote/API | Remote adapter | 🔴 |
| 14 | NontonAnimeID | `nontonanimeid` | AJAX → iframe | Dedicated/AJAX resolver candidate | 🔴 |
| 15 | Animeisme | `animeisme` | Remote/API | Remote adapter | 🔴 |
| 16 | Animeku | `animeku` | Remote/API/direct | Remote adapter | 🔴 |
| 17 | Oploverz | `oploverz` | Base64 → iframe | Remote adapter → Base64/iframe | 🔴 |
| 18 | Kuramanime | `kuramanime` | API/direct source | Remote adapter → direct/HLS | 🔴 |
| 19 | Wibudesu | `wibudesu` | Remote/API | Remote adapter | 🔴 |
| 20 | Meownime | `meownime` | Remote/API | Remote adapter | 🔴 |
| 21 | Anibatch | `anibatch` | Remote/API | Remote adapter | 🔴 |
| 22 | Nimegami | `nimegami` | Base64 JSON → direct stream | Remote adapter → Base64/JSON | 🔴 |
| 23 | Drivenime | `drivenime` | Remote/API | Remote adapter | 🔴 |
| 24 | Anitoki | `anitoki` | Remote/API/direct | Remote adapter | 🔴 |
| 25 | RiiE | `riie` | Remote/API | Remote adapter | 🔴 |
| 26 | Kusonime | `kusonime` | API/HTML → stream | Remote adapter → API/direct | 🔴 |
| 27 | Animekuindo | `animekuindo` | Remote/API | Remote adapter | 🔴 |
| 28 | AnimeSail | `animesail` | Multi-player/iframe | Dedicated/custom resolver candidate | 🔴 |
| 29 | AllAnime | `allanime` | API/source resolution | Dedicated API adapter | 🔴 |

---

## 2. Mapping Family

### Family A — Direct / generic iframe

**Providers:** Anoboy, NeoNime.

Expected path:

```text
Provider
  ↓
episode/player URL
  ↓
StreamResolver
  ↓
GenericEmbedExtractor
  ↓
ExtractorRegistry
  ↓
ProviderStream
```

**Decision:** Do not create a dedicated extractor unless E2E proves generic extraction insufficient.

---

### Family B — Base64 → iframe

**Providers:** Animasu, Oploverz.

Expected path:

```text
Provider
  ↓
encoded server/player data
  ↓
Base64 decode
  ↓
iframe URL
  ↓
StreamResolver
  ↓
ExtractorRegistry
```

**Decision:** Prefer a small reusable Base64 decode utility in provider-layer code rather than duplicating provider-specific decode logic.

---

### Family C — AJAX → player/mirror

**Providers:** Gomunime, NontonAnimeID.

Expected path:

```text
Episode page
  ↓
AJAX request
  ↓
mirror/player data
  ↓
iframe/direct media
  ↓
StreamResolver
```

**Decision:** Add a reusable AJAX player helper only after the first real provider in this family fails with the current engine. Do not add WebView yet.

Gomunime is the more complex reference because its server response can expose `frame` and `mp4` mirror types.

---

### Family D — API / direct source

**Providers:** AnimeKompi, Kuramanime, Nimegami, Kusonime and other RemoteSource providers where the gateway already exposes structured stream URLs.

Expected path:

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

**Decision:** Keep `RemoteSourceProvider` as the common adapter while adding dedicated handling only where the gateway's schema is insufficient.

---

### Family E — Custom HLS / player logic

**Providers:** Kuronime, AnimeSail.

These are intentionally later in the queue because they have provider-specific player behavior.

Potential escalation order:

```text
static HTML
  ↓
JS URL extraction
  ↓
AJAX/source endpoint
  ↓
redirect/session handling
  ↓
WebView/network interception ONLY IF PROVEN NECESSARY
```

---

### Family F — API/source graph

**Provider:** AllAnime.

AllAnime is not treated as a normal WordPress/HTML provider. Existing CloudStream implementations show an API-oriented architecture around `allanime.to` / `api.allanime.co`.

**Decision:** build a dedicated AllAnime adapter rather than forcing it through the generic RemoteSourceProvider.

---

## 3. Implementation Order

The recommended order is based on **maximum reuse with minimum risk**:

### Batch 1 — Foundation validation

1. 🟢 Otakudesu — already proven
2. 🟡 Samehadaku — first next E2E

Goal: validate the generic resolver against a second real Indonesian provider.

### Batch 2 — Cheap iframe providers

3. Anoboy
4. NeoNime

Goal: prove that `GenericEmbedExtractor` can cover simple iframe chains without new extractors.

### Batch 3 — Base64 providers

5. Animasu
6. Oploverz

Goal: add/reuse Base64 decoding only if the provider response actually requires it.

### Batch 4 — API/direct providers

7. Kuramanime
8. AnimeKompi
9. Nimegami
10. Kusonime

Goal: validate the current `RemoteSourceProvider` normalization against several schemas.

### Batch 5 — AJAX/mirror providers

11. Gomunime
12. NontonAnimeID

Goal: introduce one reusable AJAX player/mirror layer if static extraction is insufficient.

### Batch 6 — Server/mirror providers

13. Zoronime
14. AnimeIndo

Goal: provider-specific server discovery and fallback handling.

### Batch 7 — Custom player providers

15. Kuronime
16. AnimeSail

Goal: handle custom HLS/player logic based on concrete E2E failures.

### Batch 8 — Dedicated API graph

17. AllAnime

Goal: dedicated API/source adapter and resolver path.

### Batch 9 — Remaining RemoteSource providers

18. Doronime
19. Hunter no Sekai
20. YLNime
21. Animeisme
22. Animeku
23. Wibudesu
24. Meownime
25. Anibatch
26. Drivenime
27. Anitoki
28. RiiE
29. Animekuindo

These are processed against the existing multi-gateway adapter first. Dedicated implementations are created only when E2E evidence requires them.

---

## 4. Strict E2E Gate

Every provider uses the same acceptance rule:

```text
Search
  ↓
Anime Detail
  ↓
Episode 7
  ↓
getStreams()
  ↓
HTTP(S) ProviderStream
  ↓
StreamResolver / Extractor
  ↓
StreamValidator
  ↓
Media3 / ExoPlayer
  ↓
onRenderedFirstFrame()
  ↓
🟢 PROVIDER PASS
```

A provider is **not** green merely because:

- search works;
- detail works;
- episode list works;
- an extractor class exists;
- an `.m3u8` URL was found;
- an API returned a URL;
- the project compiled.

The final gate is real playback reaching `onRenderedFirstFrame()`.

---

## 5. Extractor Escalation Rules

Do not add all possible extraction technology up front.

| Real failure | Next escalation |
|---|---|
| Episode/player URL not resolved | provider parser / iframe extraction |
| iframe chain stops | GenericEmbedExtractor improvement |
| Base64 payload | reusable Base64 decoder |
| AJAX player required | AJAX/source resolver |
| JS URL hidden | JavascriptMediaExtractor improvement |
| Packed JS | packed-JS handling |
| Redirect chain breaks | redirect-aware resolver |
| m3u8 found but playback fails | headers/referer/session handling |
| HLS master quality problem | HLS variant selection |
| Network media only visible in browser | WebView/network interception |

**WebView is the last escalation, not the default implementation.**

---

## 6. Current Runtime Architecture

The existing architecture remains:

```text
ProviderFactory
      ↓
ProviderRegistry
      ↓
AnimeProvider
      ↓
Episode / server discovery
      ↓
StreamResolver
      ↓
ExtractorRegistry
      ↓
ProviderStream
      ↓
StreamValidator
      ↓
Media3
```

`ProviderRegistry` sorts providers by priority and prevents duplicate provider IDs. `ProviderFactory` currently registers all 29 target providers. This mapping document does not change that runtime behavior.

---

## 7. Current Status

```text
29 providers target

🟢 E2E proven:  1 / 29
🔴 Remaining:  28 / 29

🟢 Extractor system: proven on Otakudesu E2E
🟢 Stream Resolver: proven on Otakudesu E2E
🟢 Stream Validator: proven on Otakudesu E2E
🟢 Media3 first-frame gate: proven on Otakudesu E2E

P0 GLOBAL: 🔴 NOT COMPLETE
```

### Important

`SamehadakuProviderE2ETest` is already present on `main`. Its PASS must be established by CI before Samehadaku becomes 🟢.

---

## 8. Reference Decisions

CloudStream remains the primary architecture reference. Its provider/extractor pattern is used as a design reference, not as a runtime dependency.

Additional repositories can be used to understand individual provider behavior, but they do not replace the central CloudStream reference architecture.

The mapping is intentionally **implementation-driven**:

> Find the smallest reusable mechanism that makes the next real provider pass. Then lock that mechanism with E2E before moving on.

---

## 9. History / Safety

- `main` remains the source of truth.
- No branch is required for routine provider mapping/implementation.
- Existing `MASTER_CHECKPOINT.md` is not overwritten.
- Existing Build #44 addendum remains historical evidence for Otakudesu and the extractor foundation.
- This file is a new mapping document and does not replace the historical checkpoint.
