# KakaAnime / AniLab — MASTER CHECKPOINT

**Tanggal:** 13 September 2026  
**Repo:** `KakaAnime/KakaAnime` — Private  
**Platform:** Android  
**Source of truth:** `main`  
**Prioritas:** P0 — Core App First; Provider menjadi integration-testing layer

> MASTER ini sengaja dibuat ringkas. Detail teknis dan sejarah berada di folder `checkpoint/`.

---

## 🚦 Status Utama

### P0 — Core App First

Provider tidak lagi menjadi blocker utama setelah E2E provider yang sedang berjalan selesai divalidasi.

| Area | Status | Keterangan |
|---|---|---|
| 🟡 Provider E2E aktif | **Otakudesu/Samehadaku validation** | Selesaikan test yang sedang berjalan; jangan melakukan perubahan provider tanpa audit hasil test |
| 🟡 Core App | **UI + feature implementation berjalan** | Home/Detail/Player progress aktif; Calendar UI reference implementation selesai, menunggu build/runtime verification |
| 🔴 Provider expansion | **Ditunda** | Provider tambahan dikerjakan setelah core aplikasi stabil |

### Provider Gate

Provider hanya dinyatakan 🟢 jika alur berikut terbukti sampai Media3:

```text
Search → Anime Detail → Episode → Stream Resolution
→ valid .m3u8 / .mpd / .mp4 → Media3 → onRenderedFirstFrame() → 🟢 PASS
```

Build/compile sukses saja **tidak cukup**.

### Aturan perpindahan prioritas

```text
E2E provider yang sedang berjalan
        ↓
   🟢 PASS → checkpoint → lanjut Core App
        │
   🔴 FAIL → audit hasil/log → checkpoint
        ↓
   jika bukan blocker yang jelas/core tidak bergantung padanya
        ↓
   PROVIDER PAUSE → lanjut Core App
        ↓
   Provider dibuka kembali di fase Integration Testing
```

**Prinsip:** jangan menghabiskan fase pengembangan inti hanya untuk mengejar resolver/provider yang belum stabil. Provider tetap dicatat, tidak dihapus, dan akan menjadi target integration testing + bug hunting setelah core aplikasi siap.

---

## 🎯 Core App Roadmap — PRIORITAS

Setelah E2E provider yang sedang berjalan selesai dicatat, fokus utama berpindah ke:

1. 🔴 **Home V1**
   - layout utama
   - daftar anime
   - Search
   - New Update
   - navigation/loading/error state

2. 🔴 **Anime Detail**
   - poster
   - judul
   - genre
   - sinopsis
   - episode list
   - status episode/watch indicator

3. 🔴 **Favorite / Library**
   - tambah/hapus Favorite
   - daftar Favorite
   - persistence

4. 🔴 **History / Watching**
   - episode terakhir
   - progress
   - Continue Watching

5. 🔴 **Video Player**
   - UI mengikuti level referensi ReDantotsu yang sudah disepakati
   - progress bar kecil + thumb bulat
   - maju/mundur 10 detik
   - next/previous episode
   - auto-next
   - landscape layout

6. 🔴 **Subscription / Premium**
   - 1080p premium
   - skip intro/outro premium

7. 🔴 **Diamond + Ads**
   - 2 diamond per iklan
   - 1 diamond = 1 video
   - diamond 0 → wajib menonton iklan sebelum video
   - tidak menggunakan popup yang mengganggu

8. 🔴 **Settings / Customization**
   - theme
   - custom accent color
   - color picker tanpa input HEX manual

9. 🔴 **New Update / Notification**
   - update episode untuk anime yang diikuti/difavorite

10. 🟡 **Calendar / Schedule UI**
   - reference screenshot audit: 🟢
   - AniList schedule metadata: 🟢
   - seven-day selector + timeline + airing/countdown cards: 🟢
   - Android build/runtime verification: 🟡
   - visual device comparison: 🟡

11. 🧪 **Integration Testing & Bug Hunting**
   - Search → Detail → Episode → Provider → Stream → Player
   - network failure
   - loading/error state
   - state/history/favorite
   - player lifecycle
   - provider failover
   - bug yang muncul dari pemakaian aplikasi end-to-end

Provider tambahan dikerjakan **setelah** core app cukup stabil untuk menjadi target integration testing.

Provider expansion bukan blocker untuk Calendar/Core UI.

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

- Otakudesu E2E sebelumnya berhasil sampai `onRenderedFirstFrame()`; jalur yang sudah terbukti tidak boleh diubah tanpa alasan teknis kuat.
- Extractor Registry teruji.
- Stream Resolver teruji.
- Stream Validator teruji.
- JS Media Extractor teruji.
- Otakudesu Server Extractor teruji.
- Media3 playback path teruji.
- Android foundation: AGP 8.9.2, Kotlin 2.1.20, Media3 1.6.1, Java/Kotlin 17, Gradle 8.11.1.

---

## 🟡 Aktif Sekarang — Provider Validation Terakhir

Saat ini provider yang sedang dikerjakan hanya diselesaikan sampai titik validasi yang sedang ditunggu. Setelah hasilnya dicatat, **jangan otomatis lanjut mengejar provider berikutnya**.

### Samehadaku

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

### Latest Samehadaku fix — extensionless embed

Bitrise Build #57 proved the flow could reach stream resolution but still selected an `UNKNOWN` stream type. The reference audit found that CloudStream's Samehadaku implementation performs a second-stage GET on extensionless embed URLs and extracts `<video><source>` / equivalent media URLs.

AniLab now implements that pattern natively in `SamehadakuEpisodeExtractor`:
- existing host extractor path remains first;
- if no typed host stream is produced, fetch the embed page;
- parse `video source`, `source`, `video` media URLs and `data-page` URL payloads;
- preserve embed URL as Referer;
- extensionless media remains UNKNOWN initially so the existing strict `StreamValidator` can prove its actual type.

Code commit: `68fb9f51d689b8139d507b2393a05f90a1909fbd`  
Checkpoint: `checkpoint/builds/BUILD_061_SAMEHADAKU_EXTENSIONLESS_EMBED_FIX.md`

### Otakudesu host resolver

A dedicated native `OtakudesuHostExtractor` was added and registered to cover host patterns observed in the CloudStream reference audit, including FileDon/UserVideo/UserDrive/SameVideo, VidHide, Blogger/Blogspot, Mp4Upload, YourUpload/Yuplod, StreamWish/FileLions.

Relevant commits:
- `2616c9651a6bb89c0bf393a2c5cb71ddbfd20740`
- `a39a92626535141bc7c170794822b6cc1167f175`
- `39b54f51df572f11225105e5d065ede1a3646b0c`
- checkpoint: `e577bd26607011785e8622df6abd0519a21a9cf8`
- checkpoint file: `checkpoint/builds/BUILD_064_OTAKUDESU_HOST_RESOLVER.md`

E2E validation remains the source of truth. Do not mark a provider green without `onRenderedFirstFrame()`.

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

**Tambahan aturan provider:** jika provider E2E gagal dan kegagalan tersebut tidak memblokir core app, jangan stacking patch tanpa batas. Catat root cause + status, checkpoint, lalu **pause provider** dan lanjutkan core app. Provider dibuka kembali saat Integration Testing.

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
 ↓
Jika provider terus menjadi blocker non-core → checkpoint → PAUSE PROVIDER → CORE APP
```

- Cari referensi jika masalah menyangkut extractor/provider, Media3/player, WebView, parsing/stream resolution, atau ada kemungkinan solusi mapan sudah tersedia.
- Tidak perlu mencari referensi tambahan jika root cause sudah jelas dan fix sederhana/terverifikasi.
- Referensi adalah pola/validasi teknis; jangan copy-paste dependency CloudStream ke AniLab.
- Jangan menambal berdasarkan tebakan ketika root cause belum cukup kuat.
- Provider expansion bukan prioritas selama core app belum stabil.

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

## 🔵 Calendar / Schedule Audit Addendum

Reference utama adalah screenshot Schedule yang diberikan Shin. ReDantotsu v1.0.7 mengonfirmasi pola countdown badge pada Calendar/Home dan indikator episode yang tayang hari ini. AniSync menjadi referensi tambahan untuk weekly airing calendar native Android. KakaAnime mengimplementasikan pola tersebut secara native Compose tanpa menambah library calendar.

Commit UI/data:
- `8fbf1a209aa3bed89ed4e7517c7557639ce2f573` — enrich AniList schedule metadata + pagination + include today's aired entries.
- `8c9afab9e896fa26616d7554828c237261fff26b` — Schedule UI redesign.
- `514691be036794f393835a27da12f933bf678687` — Calendar checkpoint.

Detail checkpoint: `checkpoint/ui/CALENDAR_UI_REFERENCE_2026-09-13.md`

Status:
- 🟢 Reference audit
- 🟢 AniList metadata mapping
- 🟢 Seven-day selector
- 🟢 Timeline + cards
- 🟢 Aired/Airing Soon + countdown
- 🟡 Android build/runtime verification
- 🟡 Real-device visual comparison

---

## 🔴 Berikutnya

1. Jalankan E2E provider yang sedang ditunggu dan catat hasilnya.
2. Jika PASS → checkpoint → jangan jadikan provider sebagai blocker; pindah ke Core App.
3. Jika FAIL → audit error/log baru satu kali secara terarah.
4. Jika FAIL tersebut tidak memblokir core → checkpoint penyebab → **PAUSE PROVIDER**.
5. Lanjutkan Core App Roadmap dan lakukan Android build/runtime verification untuk Calendar serta fitur UI yang baru diubah.
6. Setelah core cukup stabil → **Integration Testing & Bug Hunting** memakai provider yang tersedia.
7. Buka kembali provider tambahan satu per satu berdasarkan bug/coverage yang ditemukan saat integration testing.

### Target baru

```text
CORE APP STABLE
      ↓
INTEGRATION TESTING
      ↓
PROVIDER + REAL STREAM
      ↓
BUG HUNTING
      ↓
FIX + E2E
      ↓
PROVIDER EXPANSION
```

Target `29/29 provider E2E PASS` tetap menjadi target coverage jangka panjang, **bukan blocker untuk menyelesaikan core app**.

---

## 📋 Rules — LOCKED

- `main` adalah source of truth.
- Jangan membuat branch untuk workflow provider rutin.
- Jangan mengejar status hijau dengan test palsu.
- Jangan menghapus/overwrite checkpoint historis.
- Setiap perubahan kode/config/arsitektur nyata wajib dicatat.
- CloudStream adalah referensi pola, bukan dependency AniLab.
- **Core app lebih dulu daripada provider expansion.**
- **Jika provider gagal dan tidak memblokir core, checkpoint lalu pause provider.**
- **Provider akan digunakan kembali sebagai integration-testing dan bug-hunting layer setelah core stabil.**
- **Jangan mengubah komponen yang sudah LOCKED tanpa alasan teknis kuat.**
