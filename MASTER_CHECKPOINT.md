# 🧭 MASTER CHECKPOINT KAKAANIME / ANILAB — UPDATE TERBARU

**Tanggal:** 12 September 2026  
**Repo:** `KakaAnime/KakaAnime` — Private  
**Nama produk:** **AniLab** (nama GitHub/repo sementara tetap KakaAnime)  
**Platform:** Android  
**Branch utama:** `main`  
**Prioritas aktif:** P0 — Provider E2E & Streaming Foundation

---

## 📌 Legenda Status

- 🟢 **SELESAI / SUDAH DIKONFIRMASI** — implementasi dan statusnya sudah terbukti.
- 🔴 **BELUM SELESAI** — belum dikerjakan atau belum terbukti berjalan.
- 🔵 **KEPUTUSAN ARSITEKTUR TERKUNCI** — keputusan desain/arsitektur yang tidak boleh diubah sembarangan.
- 🟡 **DIPERTIMBANGKAN** — ide yang belum dikunci sebagai target.
- ⚪ **DITUNDA / V2** — sengaja tidak masuk target V1 saat ini.

> **Aturan penting:** implementasi yang baru "ada di source code" belum boleh dianggap 🟢 apabila belum lolos validasi yang memang dibutuhkan. Khusus provider, status 🟢 hanya setelah E2E playback benar-benar berhasil sampai Media3 menerima stream dan menghasilkan `onRenderedFirstFrame()`.

---

# 🎯 TARGET AKHIR P0

```text
Search
  ↓
Anime Detail
  ↓
Episode
  ↓
Resolve Stream
  ↓
.m3u8 / .mpd / .mp4
  ↓
Media3
  ↓
onRenderedFirstFrame()
  ↓
🟢 PLAYBACK BERHASIL
```

**Status P0:** 🔴 **BELUM SELESAI**

---

# 1. 🔴 PROVIDER SYSTEM — TARGET 29 PROVIDER

**Target:** 29 provider  
**Status E2E:** 🔴 **0 / 29 provider terkonfirmasi green**

Implementasi/provider code sudah tersedia di repository dan sebagian sudah diperbaiki, tetapi belum dihitung selesai sebelum E2E playback lolos.

### Provider utama

1. 🔴 **Otakudesu**
2. 🔴 **Samehadaku**
3. 🔴 **AllAnime**
4. 🔴 **Kuronime**
5. 🔴 **123Anime**
6. 🔴 **25 provider RemoteSourceProvider lainnya**

### Gate provider

Setiap provider harus melewati:

```text
Search
→ Anime Detail
→ Episode
→ Stream Resolution
→ valid .m3u8 / .mpd / .mp4
→ Media3
→ onRenderedFirstFrame()
→ 🟢
```

**Catatan:** jangan memberi status 🟢 hanya karena extractor/API/provider class berhasil dibuat.

---

# 2. 🔴 SAMEHADAKU

**Status:** 🔴 **IMPLEMENTASI ADA, E2E BELUM GREEN**

### Reference yang dikunci

- `v2.samehadaku.how`
- Search
- Detail
- Episode extraction
- Stream extraction
- Host/fallback extractor

### Output yang ditargetkan

- `.webm`
- `.m3u8`
- beberapa host/fallback extractor

### Playback gate

`Media3 onRenderedFirstFrame()` harus berhasil.

---

# 3. 🔴 KURONIME

**Status:** 🔴 **IMPLEMENTASI ADA, E2E BELUM GREEN**

### Reference

- `tv1.kuronime.vip`
- Search pagination
- Episode extraction
- Mirror extraction
- Base64 decode
- iframe
- Animeku
- mp4upload
- yourupload
- streamlare
- linkbox
- QuickJS untuk extractor Animeku

### Playback gate

Hasil extractor harus berakhir pada stream yang dapat dimainkan Media3.

---

# 4. 🔴 ALLANIME

**Status:** 🔴 **IMPLEMENTASI ADA, E2E BELUM GREEN**

### API flow

```text
Search
  ↓
Anime
  ↓
Episodes
  ↓
Episode Info
  ↓
Episode URL
  ↓
Play
```

Endpoint `/play` diperlakukan sebagai jalur yang cocok untuk player, tetapi tetap harus divalidasi sampai playback nyata.

---

# 5. 🔴 123ANIME

**Status:** 🔴 **PROVIDER/FALLBACK SUDAH DIRENCANAKAN, E2E BELUM GREEN**

### API target

- Search
- Anime
- Episode
- Episode Stream
- fallback/remote source

---

# 6. 🔴 PROVIDER ENGINE / ROUTER / NORMALIZER

### Yang sudah tersedia

- Provider registry
- Provider routing
- Provider normalization
- Multiple provider implementations
- Remote source provider architecture
- Fallback/extractor architecture

### Yang belum selesai

🔴 Validasi E2E seluruh provider.  
🔴 Validasi kualitas stream.  
🔴 Validasi fallback saat provider utama gagal.  
🔴 Validasi timeout/error handling di kondisi nyata.

---

# 6A. 🔵 PROVIDER ENGINE — DYNAMIC PROVIDER & DOMAIN ARCHITECTURE

**Status arsitektur:** 🔵 **LOCKED DIRECTION**

- Provider Engine menjadi fondasi utama untuk seluruh provider AniLab.
- Setiap provider dibuat sebagai adapter/provider terpisah sehingga perubahan satu provider tidak merusak provider lainnya.
- Provider Engine tidak mengekspos detail provider, domain, API, embed, resolver, atau host kepada user.
- Setiap provider memiliki:
  - `providerId`
  - nama provider
  - priority
  - search handler
  - anime detail handler
  - episode handler
  - stream handler
  - resolver handler bila diperlukan.

### 🔵 Dynamic Domain / Endpoint

- Domain provider tidak boleh menjadi ketergantungan permanen yang sulit diubah di APK.
- Endpoint/domain yang memungkinkan harus dapat dikonfigurasi secara remote.
- Jika domain provider berpindah tetapi struktur API/parser tetap kompatibel:
  - konfigurasi domain dapat diperbarui dari server/remote config
  - AniLab menggunakan domain baru secara otomatis
  - tidak perlu update APK.
- Provider dapat memiliki:
  - primary domain
  - secondary/fallback domain
  - API endpoint
  - stream endpoint
  - resolver endpoint.
- Jika primary domain gagal, Provider Engine dapat mencoba fallback yang tersedia.
- Perubahan konfigurasi domain tidak boleh membutuhkan perubahan source code APK jika hanya berupa perubahan endpoint.

### 🔵 Provider Resolution Flow

```text
User
 ↓
Search Anime
 ↓
Provider Engine
 ↓
Provider Adapter
 ↓
Anime Detail
 ↓
Episode
 ↓
Stream Discovery
 ↓
┌─────────────────────┐
│ Direct Stream       │
│ .mp4 / .m3u8 / .mpd │
└──────────┬──────────┘
           │
           │ direct
           ↓
        Media3

atau

┌─────────────────────┐
│ Embed Source        │
│ iframe / player URL │
└──────────┬──────────┘
           ↓
     Embed Resolver
           ↓
    Direct Media URL
           ↓
         Media3
```

- Direct stream dan Embed Source dianggap sebagai dua jalur streaming resmi.
- Embed tidak ditampilkan sebagai pilihan teknis kepada user.
- Provider Engine otomatis menentukan apakah URL merupakan:
  - HLS
  - DASH
  - MP4
  - Embed.
- Jika Embed:
  - otomatis diteruskan ke Embed Resolver
  - resolver mencari direct playable media
  - hasil akhirnya diberikan ke Media3.
- User tidak perlu memilih resolver secara manual.

### 🔵 Resolver Architecture

- Resolver dibuat terpisah dari Provider Adapter.
- Satu resolver dapat digunakan oleh beberapa provider jika format host/embed-nya sama.
- Resolver dapat memiliki fallback resolver.
- Provider tidak boleh mengandung logic resolver yang terlalu spesifik jika logic tersebut bisa digunakan provider lain.
- Jika satu host embed berubah, logic resolver diperbaiki sekali dan dapat digunakan kembali oleh provider lain yang memakai host tersebut.

### 🔵 Provider Failover

- Jika provider utama gagal, Provider Engine dapat mencoba fallback yang tersedia.
- Kegagalan provider tidak boleh membuat aplikasi crash.
- Error provider harus dikembalikan dalam bentuk status yang terkontrol.
- Provider Engine membedakan:
  - domain unavailable
  - API unavailable
  - anime tidak ditemukan
  - episode tidak ditemukan
  - stream tidak ditemukan
  - resolver gagal
  - direct stream gagal.
- Jika satu source gagal, Engine dapat mencoba source/resolver lain sebelum memberikan kegagalan kepada player.

### 🔵 Remote Configuration

- Konfigurasi provider yang aman untuk diekspos ke client dapat diperbarui secara remote.
- Remote configuration dapat mengatur:
  - domain
  - endpoint
  - priority
  - provider enabled/disabled
  - fallback domain
  - resolver mapping
  - timeout tertentu.
- Perubahan konfigurasi tidak membutuhkan update APK selama logic provider tetap kompatibel.
- Secret/API key sensitif tidak disimpan di Remote Config client.

### 🔵 Provider Normalization

- Semua provider harus menghasilkan model data internal AniLab yang sama.
- Provider tidak boleh memaksa UI memahami format data masing-masing website.
- Data dinormalisasi menjadi:
  - Anime
  - Episode
  - Stream
  - Download
  - Metadata.
- UI AniLab tetap sama walaupun sumber/provider berbeda.

### 🔵 Provider Router

- Provider Router memilih provider berdasarkan:
  - availability
  - priority
  - kemampuan provider
  - hasil pencarian
  - keberhasilan resolver.
- Router dapat berpindah provider/source secara internal tanpa meminta user memilih secara manual.
- Provider yang gagal tidak boleh memblokir provider lain.
- Tidak menampilkan istilah teknis seperti API, iframe, resolver, host, atau endpoint kepada user.

### 🔵 Provider E2E Gate

Setiap provider baru dianggap 🟢 selesai hanya setelah:

```text
Search
 ↓
Anime Detail
 ↓
Episode
 ↓
Resolve Stream
 ↓
Direct / Embed Resolver
 ↓
.m3u8 / .mpd / .mp4
 ↓
Media3
 ↓
onRenderedFirstFrame()
 ↓
🟢 PASS
```

- Build/compile sukses saja belum cukup.
- Provider harus benar-benar mampu menghasilkan playback.
- Target akhir: 29/29 provider E2E 🟢.

### 🔵 Domain Change Policy

- Domain berubah, struktur tetap kompatibel → 🟢 remote configuration.
- Domain + endpoint berubah → 🟢 remote configuration jika parameter yang dibutuhkan sudah didukung.
- Format API berubah → 🟡 evaluasi adapter.
- HTML/parser berubah → 🔴 perlu perbaikan provider.
- Metode embed/stream berubah → 🔴 perlu perbaikan resolver/provider.
- Update APK hanya diperlukan jika perubahan membutuhkan logic baru di dalam aplikasi.

### 🔵 User Experience Rule

- Semua kompleksitas provider berada di belakang layar.
- User hanya melihat:

```text
Anime
 ↓
Episode
 ↓
▶ Play
```

- User tidak melihat:
  - Provider
  - API
  - iframe
  - resolver
  - host
  - endpoint.
- Provider Engine harus membuat AniLab terasa seperti satu layanan streaming meskipun sumber di belakangnya terdiri dari banyak provider.

### 🔵 Status Provider Engine

- Provider Engine architecture → 🔵 LOCKED DIRECTION
- Dynamic domain configuration → 🔵 LOCKED DIRECTION
- Direct Stream + Embed Source → 🔵 LOCKED DIRECTION
- Embed Resolver → 🔵 LOCKED DIRECTION
- Provider Failover → 🔵 LOCKED DIRECTION
- Provider Normalization → 🔵 LOCKED DIRECTION
- Provider Router → 🔵 LOCKED DIRECTION
- Provider E2E Gate → 🔵 LOCKED DIRECTION
- Target provider → 29 provider E2E 🟢

---

# 7. 🔴 P0 — STREAMING FOUNDATION

### Target

- Provider stream benar-benar masuk ke player.
- Demo Bunny URL tidak lagi menjadi sumber utama playback.
- Stream `.m3u8`, `.mpd`, atau `.mp4` dapat diterima Media3.
- Player menerima URL hasil resolver secara dinamis.
- Playback mencapai `onRenderedFirstFrame()`.

### Status

🔴 **Belum selesai.**

Saat ini player masih mempunyai jalur demo dan provider-resolved stream belum terbukti E2E.

---

# 8. 🟢 / 🔴 P0 — CI / RUNNER

## GitHub

- 🟢 Organization: `KakaAnime`
- 🟢 Repository: `KakaAnime/KakaAnime`
- 🟢 Repository private
- 🟢 Workflow permissions sudah dikonfigurasi
- 🟢 GitHub Actions tersedia

## Backup / Recovery

- 🟢 Recovery progress dari Codespace sudah dipulihkan ke `main`.
- 🟢 Backup branch dibuat sebelum recovery:
  `backup-main-before-recovery-2026-09-12`
- 🟢 `main` memiliki Gradle wrapper standar.
- 🟢 `gradlew` executable (`100755`).
- 🟢 `gradle-wrapper.jar` tersedia.
- 🟢 Gradle wrapper dikonfigurasi ke Gradle `8.11.1`.

## Bitrise

- 🟢 Bitrise project/workflow sudah dibuat.
- 🟢 Workflow Android `run_tests` tersedia.
- 🟢 Git clone repository berhasil.
- 🟢 **Build #15 SUCCESS** — `run_tests` selesai tanpa error.
- 🟢 Install missing Android SDK components berhasil.
- 🟢 Android Unit Test berhasil.
- 🟢 Save Gradle Cache berhasil.
- 🟡 APK/release build tetap perlu divalidasi pada tahap release/device validation.

### Fix yang mengantar Build #15 ke SUCCESS

- 🟢 `play-services-ads` diturunkan dari `25.4.0` ke `24.8.0`.
- 🟢 Kotlin project tetap `2.1.20`.
- 🟢 AGP tetap `8.9.2`.
- 🟢 Java/JVM target tetap `17`.
- 🟢 Gradle wrapper tetap `8.11.1`.

## WarpBuild

- 🟢 WarpBuildBot sudah dikonfigurasi.
- 🟢 Workflow provider E2E dirancang menggunakan WarpBuild.
- 🟢 Target runner: 4 vCPU / 16 GB RAM.
- 🟢 Nested virtualization aktif pada konfigurasi yang dipilih.
- 🔵 Tetap menggunakan 4x terlebih dahulu untuk menghemat biaya.
- 🔵 Tidak naik ke 8x kecuali 4x terbukti tidak mampu.
- 🔴 E2E provider belum green.

---

# 9. 🔴 P0 — E2E WORKFLOW

### Workflow

- 🟢 Workflow manual sudah dibuat.
- 🟢 Nama: **Provider E2E WarpBuild**
- 🟢 Target provider mencakup Otakudesu, Kuronime, AllAnime, Samehadaku, 123Anime, dan final gate seluruh provider.

### Run terbaru

- 🟢 Run #1 sudah pernah dijalankan.
- 🔴 Status belum menghasilkan E2E green.
- 🔴 Runner sebelumnya masih berada pada tahap provisioning/queue dan belum menghasilkan bukti playback lengkap.

### Strategi

- 🔵 Gunakan runner eksternal agar tidak bergantung pada GitHub Actions minutes.
- 🔵 Debug berdasarkan log nyata.
- 🔵 Provider individual digunakan untuk debugging.
- 🔵 Full 29-provider run hanya menjadi final gate setelah jalur individual stabil.

---

# 10. 🟢 PROJECT FOUNDATION — ANDROID

### Build foundation

- 🟢 Android Gradle project structure
- 🟢 Android Gradle Plugin `8.9.2`
- 🟢 Kotlin `2.1.20`
- 🟢 Kotlin Compose compiler plugin
- 🟢 Java/Kotlin JVM target `17`
- 🟢 Jetpack Compose
- 🟢 Material 3
- 🟢 Extended Material icons
- 🟢 Media3 ExoPlayer/UI `1.6.1`

### Gradle

- 🟢 Standard Gradle wrapper tersedia.
- 🟢 `gradlew` executable.
- 🟢 Gradle `8.11.1` configured.
- 🟢 Bitrise `run_tests` Build #15 SUCCESS.
- 🔴 Full APK/release/device validation belum selesai.

---

# 11. 🔴 P1 — SMART PLAYBACK

## Target

- 🔴 Resume playback
- 🔴 Simpan posisi terakhir
- 🔴 Continue Watching
- 🔴 Episode progression
- 🔴 Next Episode
- 🔴 Previous Episode
- 🔴 Auto Next
- 🔴 Playback history persistence
- 🔴 Integrasi Diamond
- 🔴 Integrasi Premium
- 🔴 Auto Skip Intro
- 🔴 Auto Skip Outro

### Auto Skip

- 🔵 **Premium-only**
- 🔴 Belum divalidasi sebagai alur playback penuh dari stream provider nyata.

---

# 12. 🟢 PLAYER FOUNDATION — REFERENCE ReDantotsu

### Arsitektur/UI yang sudah ada

- 🟢 ReDantotsu-style player controller architecture
- 🟢 Portrait player
- 🟢 Landscape player
- 🟢 Seek mundur 10 detik
- 🟢 Seek maju 10 detik
- 🟢 Progress bar
- 🟢 Player callbacks
- 🟢 Previous/Next episode callbacks
- 🟢 Auto-next toggle UI
- 🟢 Playback speed menu
- 🟢 Quality selector
- 🟢 Intro/outro timestamp model

### Yang masih merah

- 🔴 Provider stream belum terbukti masuk ke player E2E.
- 🔴 Next/Previous episode belum selesai sebagai alur data nyata.
- 🔴 Auto Next belum benar-benar melakukan transisi episode secara end-to-end.
- 🔴 Dynamic title/episode metadata belum sepenuhnya tersambung dari pilihan user.
- 🔴 Quality enforcement berdasarkan stream provider belum selesai.

---

# 13. 🔴 P2 — HOME & DATA

## Fondasi

Sudah pernah tersedia:

- 🟢 Anime list foundation
- 🟢 Anime card foundation
- 🟢 Anime detail foundation
- 🟢 Backend anime loading foundation

## Home V1

- 🔴 Home belum final.
- 🔴 Home masih menggunakan daftar anime lokal kecil sebagai fondasi/demo.
- 🔴 Belum menggunakan katalog provider secara penuh.
- 🔴 Search → detail → episode harus memakai data provider nyata.

### Target Home V1

- 🔴 Provider catalog
- 🔴 Search
- 🔴 Anime detail
- 🔴 Episode list
- 🔴 Integrasi Favorite
- 🔴 Integrasi History
- 🔴 Integrasi watched state
- 🔴 New Updates

---

# 14. 🔴 FAVORITE / HISTORY / WATCHED

### Foundation

- 🟢 Favorite tab/state tersedia.
- 🟢 History tab tersedia.
- 🟢 Watched episode state tersedia saat runtime.

### Belum selesai

- 🔴 Persistence setelah app restart.
- 🔴 Favorite tersimpan permanen.
- 🔴 History tersimpan permanen.
- 🔴 Watched state lintas screen.
- 🔴 Lock icon episode benar-benar terhubung dengan state watched yang persisten.

### Keputusan

- 🔵 Hanya **Favorite** sebagai kategori favorite utama.
- 🔵 Tidak menggunakan kategori Favorite Completed/Plan to Watch terpisah.
- 🔵 Tidak menggunakan tombol manual `Mark as watched` sebagai fitur utama.
- 🔵 Episode yang belum ditonton menggunakan indikator lock sesuai rancangan.

---

# 15. 🔴 NEW UPDATES

### Target

Menampilkan anime yang mendapatkan episode baru berdasarkan anime yang diikuti/ditonton user.

### Status

- 🔴 Update feed nyata belum selesai.
- 🔴 State/notification belum selesai.
- 🔴 Belum terhubung ke provider catalog/update checker.

---

# 16. 🔴 P3 — DIAMOND & ADS

### Aturan yang sudah dikunci

- 🟢 Rewarded ad memberikan **2 diamonds**.
- 🟢 1 diamond = 1 episode.
- 🟢 Jika diamond = 0, user harus mendapatkan diamond melalui rewarded ad sebelum menonton episode gratis.
- 🟢 Premium tidak membutuhkan diamond untuk entitlement Premium.
- 🟢 Tidak menggunakan popup mengganggu untuk free user sebagai rancangan utama.

### AdMob

- 🟢 Rewarded AdMob integration tersedia.
- 🟢 Google test App ID/unit ID digunakan untuk development.
- 🔴 Production AdMob IDs belum dikonfigurasi.
- 🔴 End-to-end ad → reward → watch episode belum divalidasi pada build nyata.

---

# 16A. 🔵 EPISODE ACCESS — AUTOMATIC DIAMOND CONSUMPTION

**Status:** 🔵 **KEPUTUSAN FLOW TERKUNCI — IMPLEMENTASI BELUM SELESAI**

- Non-Premium access ditentukan oleh jumlah Diamond yang tersedia.
- Jika Diamond ≥ 1 saat episode ditekan:
  - otomatis konsumsi 1 Diamond
  - unlock episode
  - langsung mulai playback
  - tanpa popup/confirmation.
- Jika Diamond = 0:
  - jalankan flow Tonton Iklan.
- Rewarded Ad tersedia:
  - tampilkan AdMob Rewarded Ad
  - reward +2 Diamond
  - otomatis gunakan 1 Diamond
  - unlock episode
  - langsung playback.
- Rewarded Ad tidak tersedia/gagal load:
  - gunakan fallback timer 30 detik
  - setelah timer selesai +2 Diamond
  - otomatis gunakan 1 Diamond
  - unlock episode
  - langsung playback.
- Premium:
  - Diamond = `UNLIMITED`
  - tidak ada pengurangan Diamond
  - tidak ada iklan
  - tidak ada timer
  - episode langsung playback.
- Tidak menggunakan popup konfirmasi Diamond.
- Flow harus tetap minimal dan mengikuti UX ReDantotsu.

### 🔵 Flow Final

```text
TAP EPISODE
    ↓
Premium?
 ├─ YA → Unlimited Diamond → langsung PLAY
 │
 └─ TIDAK
      ↓
   Diamond ≥ 1?
    ├─ YA → otomatis -1 Diamond → UNLOCK → langsung PLAY
    │
    └─ TIDAK → tampilkan "Tonton Iklan"
              ↓
         AdMob tersedia?
          ├─ YA → tonton Rewarded Ad
          │       → +2 Diamond
          │       → otomatis -1 Diamond
          │       → UNLOCK → PLAY
          │
          └─ TIDAK → timer 30 detik
                    → +2 Diamond
                    → otomatis -1 Diamond
                    → UNLOCK → PLAY
```

---

# 17. 🔴 P3 — PREMIUM / SUBSCRIPTION

### Entitlement model

- 🟢 Premium state/model tersedia.
- 🟢 1080p = Premium.
- 🟢 Auto Skip Intro = Premium.
- 🟢 Auto Skip Outro = Premium.
- 🟢 Download = Premium.
- 🔵 Premium memiliki Diamond `UNLIMITED` untuk akses episode.
- 🔵 Premium tidak melakukan konsumsi Diamond saat playback.

### Belum selesai

- 🔴 Play Billing belum terhubung.
- 🔴 Product ID belum dikonfigurasi untuk production.
- 🔴 Purchase/restore entitlement belum diuji.
- 🔴 Premium entitlement belum diuji end-to-end pada playback provider nyata.

---

# 18. 🔴 P3 — QUALITY / 1080P

### Keputusan

- 🔵 1080p hanya untuk Premium.

### Status

- 🟢 Quality selector UI tersedia.
- 🔴 Enforcement nyata berdasarkan kualitas stream provider belum selesai.
- 🔴 Belum memastikan provider menyediakan mapping 720p/1080p yang konsisten.

---

# 19. 🔴 DOWNLOAD

### Status

- 🟢 Download entitlement rule Premium sudah ada.
- 🔴 Implementasi download sebenarnya belum selesai.
- 🔴 Download manager/storage/error handling belum selesai.
- 🔴 Belum divalidasi dengan stream provider nyata.

---

# 20. 🔴 P4 — UI CUSTOMIZATION

### Target

- 🔴 Custom color/theme.
- 🔴 Color picker ramah pengguna.
- 🔴 Tidak meminta user memasukkan kode HEX secara manual.
- 🔵 Arah visual mengikuti fleksibilitas customization seperti referensi Saikou.

---

# 21. 🟢 / 🔴 SCREENS FOUNDATION

- 🟢 Home foundation
- 🟢 Anime detail foundation
- 🟢 Favorite tab foundation
- 🟢 History tab foundation
- 🟢 Calendar screen
- 🟢 Profile screen
- 🟢 Premium screen
- 🟢 Video player foundation
- 🔴 Final data integration antar-screen
- 🔴 Provider-backed Home final
- 🔴 Persistent user state antar-screen

---

# 22. 🔴 BACKEND

### Foundation

- 🟢 Backend folder tersedia.
- 🟢 Node server/data foundation tersedia.
- 🟢 Endpoint anime foundation pernah tersedia.

### Belum selesai

- 🔴 Backend/provider E2E final.
- 🔴 Production-grade error handling.
- 🔴 Stream resolver end-to-end.
- 🔴 Provider fallback final.

---

# 23. 🆕 FITUR ANI LAB — TAMBAHAN YANG SUDAH DISEPAKATI

## 🟢 TARGET BARU — implementasi masih 🔴

1. **Picture-in-Picture (PiP)**
   - Video tetap berjalan dalam jendela kecil ketika user keluar dari aplikasi.
   - Terintegrasi dengan lifecycle Media3.

2. **Sleep Timer**
   - 15 menit
   - 30 menit
   - 45 menit
   - 60 menit
   - Setelah episode selesai

3. **Report Broken Episode / Stream**
   - Video tidak bisa diputar
   - Audio bermasalah
   - Subtitle/track bermasalah
   - Buffering/error
   - Episode salah
   - Sumber bermasalah
   - Detail provider/source tetap ditangani AniLab di belakang layar.

4. **Smart Auto-Fallback**
   - Tidak ada source/provider switcher manual di UI.
   - Jika resolver atau playback sumber awal gagal, AniLab otomatis mencoba sumber/provider alternatif.
   - Menggunakan timeout dan batas retry agar tidak looping tanpa akhir.
   - User cukup fokus menonton.

   ```text
   Episode
     ↓
   AniLab pilih sumber terbaik
     ↓
   Resolve
     ↓
   Gagal / stream tidak valid
     ↓
   Auto Fallback
     ↓
   Sumber alternatif
     ↓
   ▶ PLAY
   ```

5. **Episode Watch Progress**
   - Progress tontonan terlihat secara subtle pada daftar episode.
   - Mengikuti bahasa visual ReDantotsu yang elegan.
   - Harus menyatu dengan desain AniLab, bukan menjadi elemen UI yang mengganggu.
   - Terhubung dengan posisi playback dan watched state persisten.

6. **Google Login + Backup / Restore**
   - Google Login menjadi fondasi identitas user.
   - Target sinkronisasi/backup: Favorite, History, Episode progress, Watched state, pengaturan player, dan theme/tampilan.
   - Login kembali pada perangkat lain dapat memulihkan data.

---

# 24. 🆕 ANI LAB — EPISODE ACCESS / LOCKED WATCH FLOW

**Status:** 🔴 **TARGET — BELUM DIIMPLEMENTASIKAN**

Fitur yang direferensikan dari video user ditambahkan sebagai target terpisah tanpa menghapus aturan Diamond/Premium yang sudah ada.

### Aturan akses

- 🔵 Episode yang belum memiliki entitlement watch tetap terlihat sebagai episode **locked**.
- 🔵 User dapat membuka episode melalui entitlement gratis (Diamond/rewarded ad) sesuai aturan monetisasi yang sudah dikunci, atau melalui **Premium**.
- 🔵 Episode yang entitlement-nya sudah diperoleh menjadi **unlocked** dan dapat diputar.
- 🔵 State lock/unlock harus konsisten dengan watched/entitlement state dan persistence.
- 🔴 Belum ada implementasi nyata.

### UI / UX

- 🔵 Visual, typography/font, spacing, iconography, bottom sheet/dialog, transition, animation, gesture dan interaction pattern mengikuti **arah desain ReDantotsu**.
- 🔵 Tampilan harus terasa native dengan AniLab, bukan copy-paste UI ReDantotsu.
- 🔵 Lock indicator dibuat subtle dan tidak mengganggu daftar episode.
- 🔵 Unlock flow harus jelas tetapi tetap minimal: user tahu apa yang dibutuhkan untuk menonton tanpa dibombardir popup.
- 🔵 Gesture dan animasi harus halus, cepat, dan konsisten dengan player/app navigation AniLab.
- 🔴 Detail visual final menunggu implementasi UI dan validasi device nyata.

### Integrasi monetisasi

- 🔵 Free user: gunakan Diamond/rewarded-ad flow yang sudah ditetapkan.
- 🔵 Premium: bypass entitlement Diamond untuk episode yang termasuk Premium.
- 🔴 Validasi end-to-end ad/reward → unlock → playback belum dilakukan.

### Integrasi playback

```text
Episode locked
    ↓
User tap episode
    ↓
AniLab cek entitlement
    ├── Premium → Unlock → Resolve → Play
    └── Free → Diamond / Rewarded Ad → Unlock → Resolve → Play
```

### Non-negotiable UX

- 🔵 Tidak menampilkan provider/source kepada user.
- 🔵 Tidak menambahkan manual source switcher.
- 🔵 Tidak memaksa user memahami resolver/embed/host.
- 🔵 Semua kompleksitas source tetap ditangani AniLab di belakang layar.

---

### 🟡 Masih Dipertimbangkan

- **Recently Added / Recently Watched**
  - Belum menjadi fitur wajib.
  - Dipertimbangkan setelah persistence dan New Updates lebih matang.
  - Harus menghindari tumpang tindih dengan Continue Watching dan New Updates.

### ❌ Dicoret

- **Subtitle Manager** — provider sudah menyediakan subtitle Indonesia dan target audio Jepang.
- **Manual Source Switcher** — user tidak perlu memilih sumber/provider.
- **Global Search + Provider Filter** — AniLab diarahkan serba otomatis; user cukup mencari anime dan menonton.

---

# 25. 🔵 PRINSIP UX ANI LAB

> **User cukup nonton, AniLab yang mengurus sisanya.**

Kompleksitas berikut sebaiknya tersembunyi dari user:

- Pemilihan provider
- Provider fallback
- Extractor
- Host/mirror
- Stream resolver
- Validasi stream
- Retry/timeout
- Pemilihan sumber terbaik

Alur utama user:

```text
Cari anime
→ Pilih anime
→ Pilih episode
→ ▶ Nonton
```

Provider/source bukan keputusan manual user.

---

# 26. 🔴 STATUS IMPLEMENTASI FITUR BARU

Semua fitur baru masih **🔴 BELUM DIKERJAKAN**, kecuali hanya menjadi target/keputusan roadmap.

### Urutan rekomendasi Denia

1. 🟢 Bitrise Build #15 SUCCESS
2. 🔴 Provider E2E + Streaming Foundation
3. 🔴 Smart Auto-Fallback
4. 🔴 Persistence + Episode Watch Progress
5. 🔴 PiP
6. 🔴 Sleep Timer
7. 🔴 Report Broken
8. 🔴 Google Login + Backup/Restore
9. 🔴 Episode Access / Locked Watch Flow
10. 🟡 Recently Added/Watched — keputusan final belakangan

---

# 27. 🔴 P5 — FINAL DEVICE VALIDATION

Setelah P0–P4 stabil:

- 🔴 Install APK pada device nyata.
- 🔴 Portrait validation.
- 🔴 Landscape validation.
- 🔴 Player controls validation.
- 🔴 Provider playback validation.
- 🔴 Auto fallback validation.
- 🔴 Diamond/ad validation.
- 🔴 Premium validation.
- 🔴 Download validation.
- 🔴 PiP validation.
- 🔴 Sleep timer validation.
- 🔴 Login/sync validation.
- 🔴 Backup/restore validation.
- 🔴 Final crash/error sweep.

---

# 28. 🔴 FINAL RELEASE

Target akhir:

```text
Provider E2E
    ↓
Streaming stable
    ↓
Player stable
    ↓
Home/Data stable
    ↓
Persistence stable
    ↓
Monetization stable
    ↓
AniLab UX stable
    ↓
Device validation
    ↓
Release build
    ↓
🔴 → 🟢
```

### Rename terakhir

🔵 **Nama GitHub/repository tetap KakaAnime untuk sementara.**  
🔵 **Nama produk target: AniLab.**  
🔵 Rename repository/package/application identity dilakukan **paling akhir**, setelah fitur dan build stabil, menjelang pembuatan/release aplikasi final.

---

# 🧾 CHECKPOINT TERAKHIR

### 🟢 Sudah terbukti / terkunci

- Android foundation
- ReDantotsu-style player foundation
- Standard Gradle wrapper
- Recovery ke main
- Bitrise project/workflow
- **Bitrise Build #15 SUCCESS**
- AdMob compatibility fix (`play-services-ads:24.8.0`)
- WarpBuild workflow foundation
- Provider architecture foundation
- 29-provider target
- Diamond rules
- **Automatic Diamond Consumption flow**
- **Premium Diamond = UNLIMITED**
- Premium entitlement model
- AdMob test integration
- AniLab product direction
- PiP target
- Sleep Timer target
- Report Broken target
- Smart Auto-Fallback target
- Episode Watch Progress target
- Google Login + Backup/Restore target
- **Episode Access / Locked Watch Flow target**
- **Provider Engine — Dynamic Provider & Domain Architecture**

### 🔴 Fokus pengerjaan berikutnya

**P0 — Provider E2E → Streaming Foundation**

Build CI sudah 🟢. Sekarang gate berikutnya adalah membuktikan provider nyata sampai Media3 `onRenderedFirstFrame()`.

### 🟡 Dipertimbangkan

- Recently Added / Recently Watched

### ❌ Dicoret

- Subtitle Manager
- Manual Source Switcher
- Global Search + Provider Filter

---

# 🛡️ MASTER SAFETY RULE

**Jangan hapus recovery/backup history sebelum replacement implementation lolos build dan playback validation.**

Semua feature baru harus ditambahkan di atas checkpoint ini. Jangan mengganti player/provider yang sudah ada secara membabi buta hanya demi merapikan kode.

**Status keseluruhan AniLab saat checkpoint ini:** 🔴 **V1 BELUM SELESAI — P0 PROVIDER E2E MASIH MENJADI GATE UTAMA. CI/Bitrise SUDAH GREEN.**