# Provider / Media3 Metadata Handoff — Diagnosis Checkpoint

**Tanggal:** 14 September 2026  
**Scope:** Provider stream → player handoff  
**Status:** 🟡 FIX APPLIED — runtime first-frame verification masih pending

## 1. 🔴 Gejala / Error

Historical provider E2E mencapai stream resolution, tetapi Media3 gagal sebelum first frame dengan `ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED` / `UnrecognizedInputFormatException`. Sebelumnya juga ditemukan candidate `StreamType.UNKNOWN` setelah resolver validation.

## 2. 🔎 Diagnosis

### Confirmed

`NormalizedStream` membawa metadata penting:
- `type`
- `headers`
- `quality`

Tetapi jalur UI/player lama sering menurunkan hasil resolver menjadi `String url`. Akibatnya metadata stream tidak ikut sampai `PlayerCore`.

`PlayerCore` sebelumnya membangun `MediaItem` langsung dari URL. Untuk URL extensionless, MIME type tidak dapat dipastikan dari URL saja. Header seperti Referer/User-Agent juga dapat dibutuhkan oleh host tertentu.

### Hypothesis still pending

Kehilangan metadata ini merupakan penyebab langsung kegagalan Media3 pada semua kasus historical. Bukti yang ada sangat kuat untuk kasus extensionless/protected stream, tetapi tetap harus divalidasi dengan first-frame runtime setelah fix.

## 3. 🧩 Layer yang gagal

```text
Provider / Extractor       🟢 candidate ditemukan
Stream validation          🟢 typed stream dapat dibuktikan pada jalur yang lolos
Normalization              🟢 NormalizedStream memiliki type + headers
Metadata handoff            🔴 sebelumnya dipangkas menjadi URL
Media3 source construction  🔴 metadata tidak tersedia pada player
onRenderedFirstFrame        ⚪ belum diverifikasi setelah fix
```

## 4. 🏗️ Sebelum fix

```text
ProviderPlaybackResolver
        ↓
NormalizedStream(type, headers, url)
        ↓
UI/MainActivity
        ↓
String url                 ← type + headers hilang
        ↓
PlayerController
        ↓
PlayerCore
        ↓
Media3 MediaItem(url)
```

## 5. 🔧 Perubahan

### A. Metadata cache untuk boundary URL

Commit: `f067cd3243f4f230601b3fcfb09af7c57a23e991`

`NormalizedStream` sekarang menyimpan metadata stream berdasarkan URL melalui `StreamMetadataCache` dengan batas 128 entry. Ini menjaga metadata ketika boundary lama masih hanya membawa `String url`.

### B. Media3 sekarang memakai metadata stream

Commit: `8d06551c7f4c9193b152fef8c69ddac1309078e7`

`PlayerCore`:
- mengambil `NormalizedStream` dari cache jika caller hanya memberikan URL;
- menggunakan MIME berdasarkan `StreamType`;
- membuat `DefaultHttpDataSource.Factory`;
- meneruskan request headers dari provider;
- membuat `DefaultMediaSourceFactory` dari data source tersebut;
- menyiapkan Media3 memakai `MediaSource` yang sudah membawa request configuration.

### C. Selector defensif

Commit: `f12776c9f9833010fad007ca79130d2ba3934a0d`

`StreamSelector` sekarang menolak `StreamType.UNKNOWN`, sehingga stream yang belum terbukti tidak dapat dipilih sebagai best stream.

## 6. 📚 Referensi / alasan teknis

Media3 mendukung MIME hint melalui `MediaItem.Builder.setMimeType()`. Untuk request headers, konfigurasi data source digunakan agar header benar-benar masuk ke HTTP request; metadata MIME saja tidak cukup untuk membawa header request.

## 7. 🧪 Validasi

- Source file setelah perubahan harus diaudit ulang sebelum build.
- Android Actions sebelumnya masih gagal dan beberapa log tidak dapat diambil karena BlobNotFound.
- Belum ada bukti baru `onRenderedFirstFrame()` setelah fix ini.

**Status validasi:** 🟡 PARTIAL

## 8. 🧠 Diagnosis reusable

Jika di masa depan:

```text
Search       🟢
Detail       🟢
Episode      🟢
Resolver     🟢
Stream       🟢
Media3       🔴 parsing / source error
```

jangan langsung menyimpulkan extractor rusak. Audit terlebih dahulu apakah `NormalizedStream.type` dan `headers` masih utuh ketika masuk ke Media3.

Untuk URL extensionless, jangan mengandalkan suffix URL untuk menentukan container.

## 9. 📌 Dampak / audit berikutnya

- Quality switching yang masih memanggil `setVideo(resolved.url)` sekarang dapat mengambil metadata dari cache karena `NormalizedStream` otomatis mendaftarkan dirinya.
- Jika runtime masih gagal, diagnosis berikutnya harus dimulai dari request aktual: URL final, response Content-Type, headers yang dikirim, dan Media3 error setelah source creation.
- Jangan menambah provider baru sebelum jalur ini terbukti.

## 10. ➡️ Next diagnostic step

```text
Build
 ↓
Install / runtime provider test
 ↓
Search → Episode → resolve
 ↓
Media3 prepare
 ↓
onRenderedFirstFrame()
```

Jika gagal lagi, catat error baru sebagai diagnosis baru; jangan menghapus checkpoint ini.
