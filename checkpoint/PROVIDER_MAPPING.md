# 🧭 KakaAnime Provider Mapping — 13 September 2026

This is the canonical provider map. Detailed provider-specific mapping belongs under `checkpoint/providers/`.

**Repo:** `KakaAnime/KakaAnime`  
**Branch:** `main`  
**Scope:** 29 provider targets  
**Rule:** Mapping is not proof of playback. Only `Media3 onRenderedFirstFrame()` can make a provider 🟢.

## 29 Provider Targets

| # | Provider | ID | Family | E2E |
|---:|---|---|---|---|
| 1 | Otakudesu | `otakudesu` | Server/mirror + extractor | 🟢 PASS |
| 2 | Samehadaku | `samehadaku` | HTML/player + iframe/AJAX | 🟡 Pending |
| 3 | Animasu | `animasu` | Base64/iframe | 🔴 |
| 4 | AnimeIndo | `animeindo` | API/HTML + iframe | 🔴 |
| 5 | Zoronime | `zoronime` | Server/mirror | 🔴 |
| 6 | Anoboy | `anoboy` | iframe → extractor | 🔴 |
| 7 | AnimeKompi | `animekompi` | API/HTML → stream | 🔴 |
| 8 | Kuronime | `kuronime` | Base64/iframe + custom HLS | 🔴 |
| 9 | Doronime | `doronime` | Remote/API | 🔴 |
| 10 | Hunter no Sekai | `hunter-no-sekai` | Remote/API | 🔴 |
| 11 | Gomunime | `gomunime` | AJAX → mirror → iframe/MP4 | 🔴 |
| 12 | NeoNime | `neonime` | iframe → extractor | 🔴 |
| 13 | YLNime | `ylnime` | Remote/API | 🔴 |
| 14 | NontonAnimeID | `nontonanimeid` | AJAX → iframe | 🔴 |
| 15 | Animeisme | `animeisme` | Remote/API | 🔴 |
| 16 | Animeku | `animeku` | Remote/API/direct | 🔴 |
| 17 | Oploverz | `oploverz` | Base64 → iframe | 🔴 |
| 18 | Kuramanime | `kuramanime` | API/direct source | 🔴 |
| 19 | Wibudesu | `wibudesu` | Remote/API | 🔴 |
| 20 | Meownime | `meownime` | Remote/API | 🔴 |
| 21 | Anibatch | `anibatch` | Remote/API | 🔴 |
| 22 | Nimegami | `nimegami` | Base64 JSON → direct stream | 🔴 |
| 23 | Drivenime | `drivenime` | Remote/API | 🔴 |
| 24 | Anitoki | `anitoki` | Remote/API/direct | 🔴 |
| 25 | RiiE | `riie` | Remote/API | 🔴 |
| 26 | Kusonime | `kusonime` | API/HTML → stream | 🔴 |
| 27 | Animekuindo | `animekuindo` | Remote/API | 🔴 |
| 28 | AnimeSail | `animesail` | Multi-player/iframe | 🔴 |
| 29 | AllAnime | `allanime` | API/source graph | 🔴 |

## Family Strategy

- **Direct/generic iframe:** Anoboy, NeoNime → generic extractor first.
- **Base64 → iframe:** Animasu, Oploverz → reusable Base64 mechanism only when proven necessary.
- **AJAX → player/mirror:** Gomunime, NontonAnimeID → reusable AJAX layer only after real E2E evidence.
- **API/direct:** AnimeKompi, Kuramanime, Nimegami, Kusonime and suitable RemoteSource providers → normalize into `ProviderStream`.
- **Custom HLS/player:** Kuronime, AnimeSail → static HTML → JS → AJAX/source → session handling → WebView only if proven necessary.
- **API/source graph:** AllAnime → dedicated adapter.

## Strict Gate

```text
Search → Detail → Episode → getStreams()
→ ProviderStream → Extractor/Resolver → Validator
→ Media3 → onRenderedFirstFrame() → 🟢
```

## Provider-specific rule

Each provider gets its own file under `checkpoint/providers/`. That file records the concrete URL/HTML/API mapping, extraction flow, headers/referer, E2E evidence, failures, fixes, and architecture decisions. Do not invent detailed mappings before the provider is actually audited.
