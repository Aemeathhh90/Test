# KakaAnime / AniLab — MASTER CHECKPOINT

**Tanggal:** 13 September 2026  
**Repo:** `KakaAnime/KakaAnime` — Private  
**Platform:** Android  
**Source of truth:** `main`  
**Prioritas:** P0 — Provider E2E & Streaming Foundation

> MASTER ini sengaja dibuat ringkas. Detail teknis dan sejarah berada di folder `checkpoint/`.

---

## 🚦 Status Utama

### P0 — Provider E2E

**Confirmed:** `1 / 29`

| Provider | Status | Keterangan |
|---|---|---|
| 🟢 Otakudesu | **E2E PASS** | First frame terbukti di Media3; LOCKED |
| 🟡 Samehadaku | **Sedang dites** | Target One Piece Episode 7; extensionless-embed fix applied, E2E pending |
| 🔴 27 provider lainnya | Belum tervalidasi | Belum boleh dihitung green |

**P0:** 🔴 Belum selesai

### Provider Gate

```text
Search → Anime Detail → Episode → Stream Resolution
→ valid .m3u8 / .mpd / .mp4 → Media3 → onRenderedFirstFrame() → 🟢 PASS
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

- Direct stream dan embed source didukung.
- Resolver dipisahkan dari provider adapter dan reusable bila memungkinkan.
- Provider failover tidak boleh membuat aplikasi crash.
- Data provider dinormalisasi ke model internal AniLab.
- CloudStream hanya referensi arsitektur; AniLab tetap native.
- UNKNOWN tidak boleh dipromosikan menjadi media type hanya berdasarkan HTTP 206.
- Browser fallback dapat menerima URL hasil extractor/player, bukan hanya URL halaman awal.

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

```text
Samehadaku HTML source
 → Search / Detail / Episode discovery
 → Episode page URL
 → SamehadakuEpisodeExtractor
 → Host Extractor Registry
 → Stream Resolver / Validator
 → extensionless embed-page fallback
 → Browser fallback when needed
 → Media3
 → onRenderedFirstFrame()
```

### Latest fix — extensionless embed

Bitrise Build #57 proved the flow could reach stream resolution but still selected an `UNKNOWN` stream type. The reference audit found that CloudStream's Samehadaku implementation performs a second-stage GET on extensionless embed URLs and extracts `<video><source>` / equivalent media URLs.

AniLab now implements that pattern natively in `SamehadakuEpisodeExtractor`:
- existing host extractor path remains first;
- if no typed host stream is produced, fetch the embed page;
- parse `video source`, `source`, `video` media URLs and `data-page` URL payloads;
- preserve embed URL as Referer;
- extensionless media remains UNKNOWN initially so the existing strict `StreamValidator` can prove its actual type.

Code commit: `68fb9f51d689b8139d507b2393a05f90a1909fbd`  
Checkpoint: `checkpoint/builds/BUILD_061_SAMEHADAKU_EXTENSIONLESS_EMBED_FIX.md`

Previous UNKNOWN-stream fix remains:
- `StreamValidator`: UNKNOWN + HTTP 206 is no longer accepted as valid; supported formats are probed before classification.
- `StreamResolver`: browser fallback tries extracted URLs plus original input URLs.
- Checkpoint: `checkpoint/builds/BUILD_060_UNKNOWN_STREAM_FIX.md`

E2E validation is still pending. Status remains 🟡 until `onRenderedFirstFrame()` is proven.

Bitrise `provider_e2e` / GitHub Actions provider workflow targets:
`SamehadakuProviderE2ETest#onePieceEpisodeSevenRendersFirstFrame`

---

## 🧭 Klasifikasi Temuan — LOCKED

| Status | Arti | Tindakan |
|---|---|---|
| 🔴 **BUG** | Implementasi/logic/configuration kita salah | Cari akar masalah lalu fix |
| 🟡 **WORKAROUND / TAMBALAN** | Solusi sementara | Catat dan cari solusi final |
| 🟢 **ENHANCEMENT** | Sistem bekerja tetapi dapat ditingkatkan | Opsional |
| ⚪ **LOCKED / SUDAH BENAR** | Sudah terbukti bekerja | Jangan disentuh tanpa alasan teknis kuat |
| ⚫ **BLOCKED / MENTOK** | Terbukti terhambat batasan eksternal/teknis setelah audit | Eskalasi dan cari alternatif |

### Prosedur saat mentok

```text
FAIL → Pastikan bukan BUG → Cari akar masalah
→ Audit kode + log + request/response + dependency/config
→ Cari referensi terbukti → Bandingkan pendekatan
→ Implementasi native AniLab → E2E
→ Jika tetap mustahil karena batasan eksternal → ⚫ BLOCKED
```

---

## 📚 Aturan Penjelasan Error — LOCKED

Setiap error penting dicatat dengan:

1. 🔴 Error yang terjadi
2. 🔎 Akar masalah
3. 🏗️ Arsitektur sebelum fix
4. 🟢 Arsitektur setelah fix
5. 🔧 Perubahan yang dilakukan
6. 🧪 Validasi
7. 📌 Dampak ke arsitektur lain

Tujuannya agar error menjadi pengetahuan proyek, bukan sekadar tambalan.

### Aturan kerja audit / reference / fix — LOCKED

```text
AUDIT
 ↓
Tentukan root cause
 ↓
Cari referensi? YA / TIDAK
 ↓
Jika YA → CloudStream / GitHub / docs resmi
 ↓
Cek kecocokan dengan arsitektur AniLab
 ↓
FIX jika root cause sudah cukup kuat
 ↓
E2E / VALIDASI
 ↓
Jika gagal → audit ulang dan tentukan apakah referensi tambahan diperlukan
```

- Cari referensi jika masalah menyangkut extractor/provider, Media3/player, WebView, parsing/stream resolution, atau ada kemungkinan solusi mapan sudah tersedia.
- Tidak perlu mencari referensi tambahan jika root cause sudah jelas dan fix sederhana/terverifikasi.
- Referensi adalah pola/validasi teknis; jangan copy-paste dependency CloudStream ke AniLab.
- Jangan menambal berdasarkan tebakan ketika root cause belum cukup kuat.

---

## 📂 Struktur Checkpoint — LOCKED

```text
checkpoint/
├── MASTER_CHECKPOINT.md
├── PROVIDER_MAPPING.md
├── providers/
├── extractors/
├── backend/
├── player/
├── ui/
└── builds/
```

Aturan: checkpoint historis tidak di-overwrite. Perubahan penting ditambahkan sebagai checkpoint/addendum baru.

---

## 🔵 Backend

Fondasi tersedia; detail backend perlu diaudit terhadap source terbaru sebelum production/green.

---

## 🔴 Berikutnya

1. Jalankan Bitrise Samehadaku E2E setelah extensionless-embed fix.
2. Jika FAIL, baca error/log baru sebelum perubahan berikutnya.
3. Jika masalah baru membutuhkan pengetahuan eksternal, lakukan reference check lagi.
4. Sahkan Samehadaku hanya dengan `onRenderedFirstFrame()`.
5. Audit backend dari `main`.
6. Lanjut provider berikutnya setelah path stabil.
7. Target P0: **29/29 provider E2E PASS**.

---

## 📋 Rules

- `main` adalah source of truth.
- Jangan membuat branch untuk workflow provider rutin.
- Jangan mengejar status hijau dengan test palsu.
- Jangan menghapus/overwrite checkpoint historis.
- Setiap perubahan kode/config/arsitektur nyata wajib dicatat.
- CloudStream adalah referensi pola, bukan dependency AniLab.
