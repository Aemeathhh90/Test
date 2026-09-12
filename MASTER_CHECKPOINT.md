# KakaAnime / AniLab — MASTER CHECKPOINT

**Tanggal:** 13 September 2026  
**Repo:** `KakaAnime/KakaAnime` — Private  
**Platform:** Android  
**Source of truth:** `main`  
**Prioritas:** P0 — Provider E2E & Streaming Foundation

> MASTER ini sengaja dibuat ringkas. Detail teknis dan sejarah tetap tersedia di checkpoint khusus serta Git history.

---

## 🚦 Status Utama

### P0 — Provider E2E

**Confirmed:** `1 / 29`

| Provider | Status | Keterangan |
|---|---|---|
| 🟢 Otakudesu | **E2E PASS** | First frame terbukti di Media3 |
| 🟡 Samehadaku | **Sedang dites** | Target One Piece Episode 7 |
| 🔴 27 provider lainnya | Belum tervalidasi | Belum boleh dihitung green |

**P0:** 🔴 Belum selesai

### Provider Gate

Provider hanya boleh 🟢 setelah seluruh jalur ini benar-benar berhasil:

```text
Search
 → Anime Detail
 → Episode
 → Stream Resolution
 → valid .m3u8 / .mpd / .mp4
 → Media3
 → onRenderedFirstFrame()
 → 🟢 PASS
```

Build/compile sukses saja **tidak cukup**.

---

## 🔧 Architecture — LOCKED

```text
Provider Adapter
      ↓
Episode / Server Discovery
      ↓
Stream Resolver
      ↓
Extractor Registry
      ↓
Stream Validator
      ↓
ProviderStream
      ↓
Media3
      ↓
onRenderedFirstFrame()
```

Prinsip utama:
- Direct stream dan embed source sama-sama didukung.
- Resolver dipisahkan dari provider adapter dan dibuat reusable bila memungkinkan.
- Provider failover tidak boleh membuat aplikasi crash.
- Data provider dinormalisasi ke model internal AniLab.
- Dynamic domain/endpoint dan remote configuration tetap menjadi arah arsitektur.
- CloudStream hanya **referensi arsitektur**, bukan dependency AniLab.
- Kompleksitas provider/host/resolver tidak ditampilkan kepada user.

---

## 🟢 Foundation Terbukti

- Otakudesu E2E berhasil sampai `onRenderedFirstFrame()`.
- Extractor Registry teruji.
- Stream Resolver teruji.
- Stream Validator teruji.
- JS Media Extractor teruji.
- Otakudesu Server Extractor teruji.
- Media3 playback path teruji.
- Android foundation: AGP 8.9.2, Kotlin 2.1.20, Media3 1.6.1, Java/Kotlin 17, Gradle 8.11.1.

---

## 🟡 Aktif Sekarang — Samehadaku

Pendekatan yang dipakai:

```text
Samehadaku
 → gateway/API discovery
 → ProviderStream candidates
 → Extractor Registry
 → Stream Resolver
 → Stream Validator
 → Media3
 → onRenderedFirstFrame()
```

HTML-first approach sebelumnya gagal, sehingga jalur API/gateway dipakai sebagai jalur utama untuk pengujian saat ini.

Bitrise `provider_e2e` sekarang menjalankan:

`SamehadakuProviderE2ETest#onePieceEpisodeSevenRendersFirstFrame`

**Status:** 🟡 menunggu hasil E2E.

---

## 🔵 Backend

**Status:** Fondasi tersedia; detail backend dipisahkan dari MASTER dan perlu diaudit terhadap source terbaru sebelum diberi status production/green.

- Backend diperlakukan sebagai service/API terpisah dari aplikasi Android.
- Detail source, endpoint, deployment, environment, security, dan audit backend:
  → `BACKEND_CHECKPOINT.md`
- MASTER hanya menyimpan status ringkas agar tetap mudah dibaca.

---

## 🔴 Berikutnya

1. Sahkan Samehadaku hanya jika benar-benar first-frame PASS.
2. Jika FAIL, bandingkan dengan error sebelumnya dan perbaiki akar masalah.
3. Audit backend dari source `main`, lalu isi `BACKEND_CHECKPOINT.md` dengan fakta aktual.
4. Setelah provider stabil, lanjut provider berikutnya.
5. Target akhir P0: **29/29 provider E2E PASS**.

---

## 📚 Checkpoint Detail

| File | Isi |
|---|---|
| `BACKEND_CHECKPOINT.md` | Backend/API, deployment, environment, security, dan audit |
| `EXTRACTOR_CHECKPOINT.md` | Extractor Registry, resolver, validator, extractor |
| `MASTER_CHECKPOINT_UPDATE_2026-09-13_BUILD44.md` | Build #44 + Otakudesu E2E history |
| `PROVIDER_MAPPING_2026-09-13.md` | Mapping/provider reference |
| `PROVIDER_CHECKPOINT.md` | Detail adapter + hasil E2E provider, jika tersedia |
| `PLAYER_CHECKPOINT.md` | Media3 + VideoPlayer |
| `UI_CHECKPOINT.md` | UI/Home/detail/player UI |

> Checkpoint lama **tidak dihapus**. Versi MASTER sebelumnya tetap tersedia melalui Git history; perubahan ini hanya membuat file MASTER aktif lebih ringkas dan memisahkan detail backend.

---

## 🧭 Rules

- Jangan mengejar status hijau dengan test palsu.
- Jangan menandai provider green tanpa bukti playback nyata.
- Jangan membuat branch untuk workflow rutin; `main` adalah source of truth.
- Jangan overwrite checkpoint historis; gunakan addendum/checkpoint baru.
- Jangan mengubah arsitektur yang sudah terbukti tanpa alasan teknis yang jelas.
- Jangan mengarang endpoint/status backend; verifikasi source `main` terlebih dahulu.
