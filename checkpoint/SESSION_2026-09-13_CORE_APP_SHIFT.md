# KakaAnime — Session Checkpoint: Core App First

**Tanggal:** 13 September 2026  
**Source of truth:** `main`  
**Status:** 🟢 Workflow priority updated and acknowledged

## Aturan kerja aktif

1. `main` tetap menjadi source of truth.
2. Provider E2E yang sedang berjalan diselesaikan/dicatat sampai hasil validasi tersedia.
3. Provider hanya boleh dinyatakan 🟢 jika alur terbukti sampai Media3 `onRenderedFirstFrame()`.
4. Jika provider E2E gagal tetapi tidak memblokir Core App: audit root cause secara terarah → checkpoint → **PAUSE PROVIDER**.
5. Jangan stacking patch provider tanpa batas hanya untuk mengejar status hijau.
6. **Core App menjadi prioritas utama:** Home V1 → Detail → Favorite/Library → History/Watching → Video Player → Premium/Diamond/Ads → Settings/Customization → Notification.
7. Provider tambahan ditunda sampai Core App cukup stabil.
8. Setelah Core App stabil, provider dipakai sebagai integration-testing + bug-hunting layer.
9. Komponen yang sudah LOCKED tidak diubah tanpa alasan teknis kuat.
10. Setiap perubahan kode/config/arsitektur nyata wajib dibuat checkpoint.
11. Target coverage `29/29 provider E2E PASS` tetap target jangka panjang, tetapi bukan blocker Core App.

## Posisi sesi

- 🟢 Recovery/main sebagai source of truth
- 🟢 Resolver/validator/provider foundation yang sudah ada dipertahankan
- 🟡 Provider validation terakhir: catat hasil E2E sebelum berpindah penuh
- 🟡 Audit jalur Otakudesu Episode 7 → stream → Media3 masih menjadi validasi teknis bila diperlukan
- 🔴 Core App belum dinyatakan selesai
- 🔴 Provider expansion belum dimulai

## Next focus

Mulai dari audit kondisi Core App di `main`, lalu kerjakan roadmap Core App secara berurutan. Jangan melakukan perubahan provider kecuali hasil integration testing menunjukkan provider tersebut memang menjadi root cause.

**Catatan:** checkpoint ini mendokumentasikan perubahan aturan kerja; bukan klaim bahwa seluruh Core App atau seluruh provider sudah lulus E2E.
