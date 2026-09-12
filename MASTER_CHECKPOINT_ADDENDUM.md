# 🧩 MASTER CHECKPOINT — ADDENDUM ANI LAB

**Tanggal:** 12 September 2026  
**Status:** Additive — tidak menggantikan `MASTER_CHECKPOINT.md`  
**Produk:** AniLab  
**GitHub repository:** tetap `KakaAnime/KakaAnime` untuk sementara

> Dokumen ini berisi keputusan dan perkembangan yang ditambahkan setelah Master Checkpoint utama. Master Checkpoint utama tetap menjadi baseline proyek dan tidak boleh dihapus/diganti hanya karena ada fitur baru.

---

## 🟢 Fitur baru yang disepakati masuk roadmap

1. **Picture-in-Picture (PiP)**
   - Video tetap berjalan dalam jendela kecil saat user keluar dari aplikasi.
   - Harus terintegrasi dengan lifecycle Media3.

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
   - Memakai timeout dan batas retry agar tidak looping tanpa akhir.
   - User cukup fokus menonton.

5. **Episode Watch Progress**
   - Progress tontonan ditampilkan secara subtle pada daftar episode.
   - Mengikuti bahasa visual ReDantotsu yang elegan.
   - Harus menyatu dengan desain AniLab.
   - Terhubung dengan posisi playback dan watched state persisten.

6. **Google Login + Backup / Restore**
   - Google Login menjadi fondasi identitas user.
   - Target sinkronisasi: Favorite, History, Episode Progress, Watched State, pengaturan player, dan theme/tampilan.
   - Login kembali pada perangkat lain dapat memulihkan data.

---

## 🟡 Masih dipertimbangkan

- **Recently Added / Recently Watched**
  - Belum wajib.
  - Ditinjau setelah persistence dan New Updates lebih matang.
  - Tidak boleh tumpang tindih dengan Continue Watching/New Updates.

---

## ❌ Dicoret

- **Subtitle Manager** — provider sudah menyediakan subtitle Indonesia dan target audio Jepang.
- **Manual Source Switcher** — user tidak perlu memilih sumber/provider.
- **Global Search + Provider Filter** — AniLab diarahkan serba otomatis; user cukup mencari anime dan menonton.

---

## 🔵 Prinsip UX AniLab

**User cukup nonton, AniLab yang mengurus sisanya.**

Kompleksitas provider, extractor, host/mirror, resolver, validasi stream, retry/timeout, dan pemilihan sumber sebaiknya tetap berada di belakang layar.

Alur user:

```text
Cari anime
→ Pilih anime
→ Pilih episode
→ ▶ Nonton
```

---

## 🟢 Build terbaru

- **Bitrise Build #15: SUCCESS**
- `run_tests` berhasil.
- Install Android SDK berhasil.
- Android Unit Test berhasil.
- Gradle cache berhasil disimpan.
- Compatibility fix AdMob: `play-services-ads` `25.4.0` → `24.8.0`.

---

## 🔴 Urutan pengerjaan setelah Build Success

1. Provider E2E + Streaming Foundation
2. Smart Auto-Fallback
3. Persistence + Episode Watch Progress
4. PiP
5. Sleep Timer
6. Report Broken
7. Google Login + Backup/Restore
8. Recently Added/Watched — keputusan final belakangan

---

## 🛡️ Aturan preservasi checkpoint

- `MASTER_CHECKPOINT.md` adalah baseline utama.
- Addendum ini hanya menambahkan keputusan baru.
- Jangan menghapus baseline/recovery history.
- Fitur baru tidak boleh dianggap 🟢 sebelum implementasi dan validasi benar-benar selesai.
- Nama produk **AniLab** belum mengubah nama GitHub/repository. Rename dilakukan paling akhir menjelang aplikasi final/release.
