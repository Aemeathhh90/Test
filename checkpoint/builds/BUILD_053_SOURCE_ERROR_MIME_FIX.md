# Build #53 — Samehadaku Media3 Source Error / MIME Fix

**Tanggal:** 13 September 2026  
**Provider:** Samehadaku  
**Target:** One Piece Episode 7  
**Status:** 🔴 BUG → fix diterapkan, E2E belum rerun

## 1. 🔴 Error yang terjadi

Bitrise Build #53 berhasil compile, install APK/test APK, menjalankan `SamehadakuProviderE2ETest#onePieceEpisodeSevenRendersFirstFrame`, dan melewati search → detail → episode 7 → stream resolution.

Kegagalan terjadi pada playback:

```text
java.lang.AssertionError:
Media3 did not render the first frame within 90s.
Playback error=Source error
```

Tidak ada `onRenderedFirstFrame()` sehingga Samehadaku tetap belum green.

## 2. 🔎 Akar masalah

Audit source menunjukkan E2E sebelumnya membuat:

```kotlin
MediaItem.fromUri(stream.url)
```

sementara `ProviderStream.type` sudah membawa informasi HLS/DASH. Setelah redirect, URL stream tidak selalu mempertahankan suffix `.m3u8` atau `.mpd`.

Media3 mendokumentasikan bahwa bila URI HLS/DASH tidak memiliki ekstensi standar, MIME type perlu diberikan secara eksplisit melalui `MediaItem.Builder.setMimeType(...)`.

Jadi candidate dapat lolos HTTP/manifest validation tetapi Media3 masih dapat menginterpretasikan URI sebagai progressive source dan menghasilkan `Source error`.

## 3. 🏗️ Arsitektur sebelum fix

```text
Samehadaku
  ↓
Episode discovery
  ↓
SamehadakuEpisodeExtractor
  ↓
Host Extractor
  ↓
StreamValidator
  ↓
ProviderStream(type=HLS/DASH, url=possibly signed/redirected)
  ↓
MediaItem.fromUri(url)   ← MIME type hilang
  ↓
Media3
  ↓
🔴 Source error
```

## 4. 🟢 Arsitektur setelah fix

```text
Samehadaku
  ↓
Episode discovery
  ↓
SamehadakuEpisodeExtractor
  ↓
Host Extractor
  ↓
StreamValidator
  ↓
ProviderStream(type=HLS/DASH)
  ↓
MediaItem.Builder
  ├─ URI
  └─ explicit MIME from ProviderStream.type
  ↓
Media3 HLS/DASH source
  ↓
onRenderedFirstFrame()
```

## 5. 🔧 Perubahan yang dilakukan

File:
`app/src/androidTest/java/com/kakaanime/app/provider/SamehadakuProviderE2ETest.kt`

Perubahan:

- `MediaItem.fromUri(stream.url)` diganti dengan `MediaItem.Builder()`.
- `StreamType.HLS` → `MimeTypes.APPLICATION_M3U8`.
- `StreamType.DASH` → `MimeTypes.APPLICATION_MPD`.
- MP4/unknown tetap menggunakan URI biasa.
- Logging semua candidate stream ditambahkan agar URL/type/quality/header dapat diaudit jika masih gagal.
- Logging Media3 ditingkatkan: error code, message, dan cause chain.

Commit fix:
`a8223e6a06fc60fc7d0498ab254c0517d5da90c8`

## 6. 🧪 Validasi

Belum tervalidasi sampai Bitrise menjalankan E2E baru.

Gate berikutnya:

```text
Bitrise
 → Samehadaku Episode 7
 → candidate stream log
 → Media3 dengan explicit MIME
 → onRenderedFirstFrame()
```

Hanya `onRenderedFirstFrame()` yang boleh mengubah status menjadi 🟢.

Jika masih gagal, gunakan error code + cause chain dari Build berikutnya untuk membedakan MIME issue dari header/segment/session issue.

## 7. 📌 Dampak ke arsitektur lain

- Provider discovery dan extractor tidak diubah.
- Extractor Registry recursion fix tetap dipertahankan.
- StreamValidator tidak diubah.
- Hanya playback E2E yang diperjelas agar normalized `StreamType` benar-benar digunakan oleh Media3.
- Temuan ini juga menjadi kandidat audit PlayerCore, karena PlayerCore saat ini masih menggunakan `MediaItem.fromUri(url)` tanpa MIME hint. Jangan menandai PlayerCore LOCKED sampai jalur stream bertipe dari provider benar-benar diteruskan ke player production.
