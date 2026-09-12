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
| 🟡 Samehadaku | **Sedang dites** | Target One Piece Episode 7; discovery HTML-first |
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
- CloudStream hanya referensi arsitektur; AniLab tetap native.

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
 → Extractor Registry
 → Stream Resolver
 → Stream Validator
 → Media3
 → onRenderedFirstFrame()
```

Bitrise `provider_e2e` menjalankan:

`SamehadakuProviderE2ETest#onePieceEpisodeSevenRendersFirstFrame`

Status tetap 🟡 sampai first-frame PASS terbukti.

---

## 🧭 Klasifikasi Temuan & Aturan Perbaikan — LOCKED

Setiap error/temuan **wajib diklasifikasikan sebelum kode diubah**.

| Status | Arti | Tindakan |
|---|---|---|
| 🔴 **BUG** | Implementasi/logic/configuration kita salah | Cari akar masalah lalu **fix** |
| 🟡 **WORKAROUND / TAMBALAN** | Solusi sementara | Catat sebagai sementara dan cari solusi final |
| 🟢 **ENHANCEMENT** | Sistem sudah bekerja, tetapi dapat ditingkatkan | Opsional, berdasarkan manfaat teknis/UX |
| ⚪ **LOCKED / SUDAH BENAR** | Sudah terbukti bekerja | Jangan disentuh tanpa alasan teknis kuat |
| ⚫ **BLOCKED / MENTOK** | Mentok karena batasan eksternal/teknis setelah audit | Eskalasi dan cari pendekatan alternatif |

### Prosedur saat mentok

```text
FAIL
 ↓
Pastikan bukan BUG
 ↓
Cari akar masalah
 ↓
Audit kode + log + request/response + dependency/config
 ↓
Cari referensi solusi yang sudah terbukti
 ↓
Bandingkan pendekatan
 ↓
Pilih solusi paling sehat
 ↓
Implementasi native AniLab
 ↓
E2E / validasi nyata
 ↓
Jika tetap mustahil karena batasan eksternal → ⚫ BLOCKED
```

Jangan menyebut “nggak bisa diperbaiki” sebelum akar masalah dan alternatif diperiksa. Jangan menumpuk tambalan tanpa memahami akar masalah. Bagian yang sudah terbukti, terutama `onRenderedFirstFrame()`, diperlakukan sebagai ⚪ LOCKED.

---

## 📚 Aturan Penjelasan Error & Arsitektur — LOCKED

Setiap error penting yang ditemukan selama pengembangan **wajib dijelaskan dan dicatat dengan struktur berikut**, agar pengembangan dapat dipahami dan dilanjutkan dari checkpoint:

### 1. 🔴 Error yang terjadi
- Tulis error/message yang muncul.
- Tentukan tahap pipeline tempat error terjadi.

### 2. 🔎 Akar masalah
- Jelaskan penyebab teknis sebenarnya.
- Bedakan BUG kita, masalah provider/external, konfigurasi, dependency, atau masalah lain.

### 3. 🏗️ Arsitektur sebelum fix
- Gambarkan alur/dependency yang menyebabkan error.
- Jika berupa recursion, dependency cycle, data flow, atau pipeline failure, tampilkan diagram sederhananya.

Contoh:

```text
Registry
 ↓
Extractor
 ↓
Registry
 ↓
Extractor
 ↓
∞ → StackOverflowError
```

### 4. 🟢 Arsitektur setelah fix
- Gambarkan alur baru setelah perbaikan.
- Jelaskan tanggung jawab tiap komponen dan batas dependency-nya.

Contoh:

```text
Provider
 ↓
Provider/Server Discovery
 ↓
Stream Resolver
 ↓
Host Extractor Registry
 ↓
ProviderStream
 ↓
Media3
 ↓
onRenderedFirstFrame()
```

### 5. 🔧 Perubahan yang dilakukan
- Sebutkan file/komponen yang diubah.
- Jelaskan kenapa perubahan diperlukan.
- Tandai apakah perubahan tersebut 🔴 BUG FIX, 🟡 WORKAROUND, 🟢 ENHANCEMENT, atau lainnya.
- Catat commit dan build bila tersedia.

### 6. 🧪 Validasi
Selalu tunjukkan titik validasi terbaru:

```text
Search              ?
Detail              ?
Episode             ?
Stream discovery    ?
Stream validation   ?
Media3              ?
onRenderedFirstFrame ?
```

Jangan menyatakan provider 🟢 tanpa bukti `onRenderedFirstFrame()`.

### 7. 📌 Dampak ke arsitektur lain
Jika perubahan dapat memengaruhi provider, extractor, resolver, validator, Media3, UI, backend, atau komponen lain, jelaskan dampaknya sebelum melanjutkan.

**Tujuan aturan ini:** setiap error menjadi pengetahuan proyek yang tersimpan, bukan sekadar bug yang ditambal. Pengembang harus dapat memahami **apa yang rusak → kenapa rusak → bagaimana arsitekturnya → apa yang diubah → bagaimana dibuktikan → apa dampaknya**.

---

## 🔵 Backend

Status: fondasi tersedia; detail backend perlu diaudit terhadap source terbaru sebelum diberi status production/green.

---

## 🔴 Berikutnya

1. Jalankan ulang Samehadaku E2E setelah fix recursion.
2. Jika FAIL, ikuti aturan klasifikasi + penjelasan error di atas sebelum perubahan berikutnya.
3. Jika ⚫ BLOCKED, audit dan cari referensi/pendekatan alternatif terlebih dahulu.
4. Sahkan Samehadaku hanya jika benar-benar first-frame PASS.
5. Audit backend dari source `main`.
6. Setelah provider stabil, lanjut provider berikutnya.
7. Target akhir P0: **29/29 provider E2E PASS**.

---

## 📋 Rules

- `main` adalah source of truth.
- Jangan membuat branch untuk workflow provider rutin.
- Jangan mengejar status hijau dengan test palsu.
- Jangan overwrite checkpoint historis; gunakan addendum/checkpoint baru.
- Setiap perubahan kode/config/arsitektur yang nyata wajib dicatat.
- Setiap error penting wajib dijelaskan dengan **error → akar masalah → arsitektur sebelum → arsitektur sesudah → perubahan → validasi → dampak**.
- CloudStream adalah referensi pola, bukan dependency AniLab.
- Jika mentok, eskalasi sebelum menyerah.
