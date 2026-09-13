# KakaAnime Workflow Checkpoint — Audit First

**Tanggal:** 13 September 2026
**Repo:** `KakaAnime/KakaAnime`
**Branch:** `main`
**Purpose:** Menetapkan cara kerja lanjutan KakaAnime agar setiap progress diaudit sebelum perubahan baru dibuat.

## Cara Kerja Wajib

Setiap pekerjaan KakaAnime mengikuti urutan:

1. 🔍 **Audit kondisi aktual `main` terlebih dahulu**
   - Cek kode, struktur, konfigurasi, dan implementasi yang benar-benar sudah ada.
   - Jangan berasumsi sebuah class/function/fitur ada hanya karena pernah direncanakan atau disebut di checkpoint lama.
   - Gunakan implementasi aktual di `main` sebagai sumber kebenaran.

2. 🧩 **Cari gap terhadap requirement**
   - Bandingkan requirement V1 dengan kondisi kode aktual.
   - Bedakan dengan jelas:
     - 🟢 sudah benar/selesai
     - 🟡 sudah ada tetapi belum lengkap atau perlu verifikasi
     - 🔴 belum ada
   - Jika fitur sudah ada dan benar, jangan dibuat ulang.

3. 🛠️ **Kerjakan hanya gap yang diperlukan**
   - Pertahankan implementasi yang sudah stabil.
   - Hindari perubahan arsitektur besar tanpa alasan teknis yang kuat.
   - Jangan membuat abstraction/class/API fiktif yang tidak ada di repo.

4. 💾 **Commit setiap perubahan bermakna**
   - Urutan wajib: **change → commit → update checkpoint → verify commit → continue**.
   - Jangan menyatakan perubahan selesai sebelum commit dapat diverifikasi di `main`.

5. 🧪 **Verifikasi setelah perubahan**
   - Audit ulang file yang berubah.
   - Bila memungkinkan, lakukan build/test yang relevan.
   - Jangan mengejar status hijau dengan test palsu atau mengubah test hanya agar terlihat PASS.

6. 📋 **Update checkpoint**
   - Catat status 🟢/🟡/🔴.
   - Catat commit SHA.
   - Catat perubahan teknis penting.
   - Catat gap yang masih tersisa.
   - Catat next step.
   - Jangan menghapus atau menimpa checkpoint historis.

## Aturan Kritis GitHub

- `main` adalah source of truth.
- Jika operasi GitHub gagal, **audit/fetch repo atau file terlebih dahulu sebelum retry**.
- Jangan menganggap write berhasil hanya karena request terkirim; verifikasi commit dan isi file.
- PR/branch eksperimen tidak boleh di-merge sembarangan ke `main`.
- PR `complex-stream-resolution` tetap perlu audit terpisah sebelum keputusan merge.

## Status Core App Saat Checkpoint

- 🟢 Home backend-driven
- 🟢 AniList poster integration
- 🟢 Continue Watching persistence
- 🟢 Provider-driven episode list
- 🟢 Episode Lock untuk episode yang belum ditonton
- 🟢 Quality menu 360p/480p/720p + 1080p Premium UI
- 🟡 Real quality switching ke stream URL aktual
- 🟡 Auto Next behavior aktual
- 🟡 New Updates berbasis watched/followed update logic
- 🟡 UI customization/color picker
- 🔴 Provider E2E + real-stream bug hunting
- 🔴 Provider expansion
- 🔴 Comments V2

## Prinsip Audit Quality

Untuk stream quality, jangan mengasumsikan adanya `StreamQuality`, `preferredQuality`, atau `StreamSelector` sebelum ditemukan di kode aktual. Gunakan model dan pipeline yang benar-benar ada, termasuk `ProviderStream.quality`, lalu audit bagaimana quality tersebut dinormalisasi, dipilih, dan akhirnya diterapkan ke URL/player.

## Next Step

Audit implementasi **Real Quality Switching** di `main` terlebih dahulu. Setelah gap aktual ditemukan, lakukan perubahan minimal yang diperlukan, commit, update checkpoint, verifikasi, lalu lanjut ke tahap berikutnya.
