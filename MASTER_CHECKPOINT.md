# 🧭 MASTER CHECKPOINT KAKAANIME / ANILAB — UPDATE TERBARU

**Tanggal:** 12 September 2026  
**Repo:** `KakaAnime/KakaAnime` — Private  
**Nama produk:** **AniLab** (nama GitHub/repo sementara tetap KakaAnime)  
**Platform:** Android  
**Branch utama:** `main`  
**Prioritas aktif:** P0 — Provider E2E & Streaming Foundation

---

## 🆕 KEPUTUSAN PRODUK ANI LAB — FITUR TAMBAHAN

Fitur berikut disepakati masuk roadmap dan belum dianggap selesai sampai implementasi + validasi terbukti.

### 🟢 Target Baru

1. **Picture-in-Picture (PiP)**
   - Video tetap berjalan dalam jendela kecil ketika user keluar dari aplikasi.
   - Harus terintegrasi dengan lifecycle Media3 tanpa mengganggu playback normal.

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
   - Target sinkronisasi/backup:
     - Favorite
     - History
     - Episode progress
     - Watched state
     - Pengaturan player
     - Theme/tampilan
   - User dapat login kembali pada perangkat lain untuk memulihkan data.

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

## 🔵 PRINSIP UX ANI LAB

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

Alur utama user tetap sesederhana mungkin:

```text
Cari anime
→ Pilih anime
→ Pilih episode
→ ▶ Nonton
```

---

## 🔴 STATUS IMPLEMENTASI FITUR BARU

Semua fitur baru di atas masih **🔴 BELUM DIKERJAKAN**, kecuali hanya menjadi target/keputusan roadmap. Jangan memberi status 🟢 sebelum implementasi dan validasi selesai.

### Urutan rekomendasi Denia

1. P0 Bitrise Build Success
2. Provider E2E + Streaming Foundation
3. Smart Auto-Fallback
4. Persistence + Episode Watch Progress
5. PiP
6. Sleep Timer
7. Report Broken
8. Google Login + Backup/Restore
9. Recently Added/Watched — keputusan final belakangan

---

> Catatan: bagian ini merupakan tambahan roadmap. Bagian checkpoint teknis yang sudah ada sebelumnya tetap menjadi acuan dan tidak dihapus.
