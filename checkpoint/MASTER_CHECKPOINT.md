# KakaAnime / AniLab — MASTER CHECKPOINT

**Tanggal:** 14 September 2026  
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

---

## 🧭 Klasifikasi Temuan — LOCKED

| Status | Arti | Tindakan |
|---|---|---|
| 🔴 **BUG** | Implementasi/logic/configuration kita salah | Cari akar masalah lalu fix |
| 🟡 **WORKAROUND / TAMBALAN** | Solusi sementara | Catat dan cari solusi final |
| 🟢 **ENHANCEMENT** | Sistem bekerja tetapi dapat ditingkatkan | Opsional |
| ⚪ **LOCKED / SUDAH BENAR** | Sudah terbukti bekerja | Jangan disentuh tanpa alasan teknis kuat |
| ⚫ **BLOCKED / MENTOK** | Terbukti terhambat batasan eksternal/teknis setelah audit | Eskalasi dan cari alternatif |

---

## 🔍 Checkpoint Diagnosis — LOCKED

**Checkpoint bukan hanya changelog. Checkpoint adalah knowledge base diagnosis proyek.** Setiap pembaruan checkpoint yang berkaitan dengan bug, test failure, regression, atau masalah teknis wajib meninggalkan jejak diagnosis yang bisa dipakai kembali pada obrolan/fase berikutnya.

Untuk setiap masalah penting, catat:

1. 🔴 **Error / gejala** — pesan error, assertion, crash, atau perilaku yang terlihat.
2. 🔎 **Root cause** — penyebab yang sudah dibuktikan; jika belum terbukti, tulis sebagai **hipotesis**, bukan fakta.
3. 🧩 **Lapisan gagal** — UI / state / repository / backend / provider / extractor / validator / normalizer / selector / metadata handoff / Media3 / runtime.
4. 🏗️ **Arsitektur sebelum fix** — jalur data/flow yang menyebabkan kegagalan.
5. 📚 **Referensi yang dipakai** — GitHub, CloudStream, dokumentasi resmi, log historis, atau checkpoint terkait; jelaskan bagian pola yang relevan.
6. 🔧 **Fix** — perubahan konkret dan commit SHA jika sudah masuk `main`.
7. 🧪 **Validasi** — test/build/runtime evidence yang benar-benar dijalankan.
8. 📊 **Hasil** — PASS, FAIL, PARTIAL, atau UNKNOWN.
9. 🧠 **Pelajaran diagnosis** — pola yang harus dicari jika masalah serupa muncul lagi.
10. 📌 **Dampak** — komponen lain yang perlu diaudit setelah perubahan.
11. ➡️ **Next diagnostic step** — hanya jika masih gagal; harus spesifik pada lapisan berikutnya, bukan tebakan umum.

### Aturan diagnosis

```text
FAIL
 ↓
Catat gejala + bukti
 ↓
Tentukan lapisan gagal
 ↓
Audit root cause
 ↓
Cari referensi bila memang relevan
 ↓
Bandingkan dengan arsitektur KakaAnime
 ↓
FIX jika root cause cukup kuat
 ↓
VALIDASI
 ↓
PASS → catat hasil + checkpoint
FAIL → catat diagnosis baru + klasifikasi ulang
```

- Jangan menghapus diagnosis lama setelah bug diperbaiki.
- Jangan menulis root cause sebagai fakta sebelum ada bukti yang cukup.
- Jangan menumpuk patch tanpa memperbarui diagnosis.
- Jika diagnosis baru membantah diagnosis lama, **catat koreksinya**, jangan menimpa sejarah.
- Checkpoint yang terkait harus saling direferensikan agar kasus serupa dapat ditelusuri.
- Sebelum memperbaiki bug baru, audit checkpoint terkait terlebih dahulu.
- Jika bug sudah pernah terjadi, gunakan diagnosis sebelumnya sebagai baseline dan verifikasi apakah pola kegagalannya sama.

### Contoh Provider / Media3

```text
Search                 🟢
Anime detail           🟢
Episode discovery      🟢
Extractor candidate    🟢
Validation             🔴
  └─ StreamType UNKNOWN

atau

Validation             🟢
Normalization          🟢
Selection              🟢
Metadata handoff       🔴
  └─ type/headers hilang

atau

Media3 prepare         🟢
STATE_READY             🟢
First frame             🔴
  └─ onRenderedFirstFrame() tidak terjadi
```

Tujuan: saat provider/player gagal di masa depan, kita dapat langsung mengetahui **lantai diagnosis terakhir**, bukti yang tersedia, dan perbaikan yang pernah dicoba.

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
8. 🧠 Diagnosis reusable untuk kasus berikutnya

---

## 🔧 Aturan kerja audit / reference / fix — LOCKED

```text
AUDIT
 ↓
Tentukan root cause
 ↓
Cari referensi? YA / TIDAK
 ↓
Jika YA → CloudStream / GitHub / docs resmi
 ↓
Cek kecocokan dengan arsitektur KakaAnime
 ↓
FIX jika root cause sudah cukup kuat
 ↓
E2E / VALIDASI
 ↓
Jika gagal → audit ulang dan tentukan apakah referensi tambahan diperlukan
```

- Cari referensi jika masalah menyangkut extractor/provider, Media3/player, WebView, parsing/stream resolution, atau ada kemungkinan solusi mapan sudah tersedia.
- Tidak perlu mencari referensi tambahan jika root cause sudah jelas dan fix sederhana/terverifikasi.
- Referensi adalah pola/validasi teknis; jangan copy-paste dependency CloudStream ke KakaAnime.
- Jangan menambal berdasarkan tebakan ketika root cause belum cukup kuat.
- Setiap referensi penting dan diagnosis hasil audit harus tercatat di checkpoint terkait.

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

Aturan: checkpoint historis tidak di-overwrite. Perubahan penting ditambahkan sebagai checkpoint/addendum baru. **MASTER hanya menyimpan aturan/status ringkas; detail diagnosis tetap di checkpoint/addendum spesifik.**

---

## 📋 Rules — LOCKED

- `main` adalah source of truth.
- Jangan membuat branch untuk workflow rutin.
- Jangan mengejar status hijau dengan test palsu.
- Jangan menghapus/overwrite checkpoint historis.
- Setiap perubahan kode/config/arsitektur nyata wajib dicatat.
- Setiap bug/failure penting wajib dicatat sebagai diagnosis yang reusable.
- Sebelum memperbaiki masalah baru, audit checkpoint terkait terlebih dahulu.
- Root cause yang belum terbukti harus ditandai sebagai hipotesis.
- Referensi dicari bila relevan; tidak mencari hanya demi formalitas.
- CloudStream adalah referensi pola, bukan dependency KakaAnime.
- **Core app lebih dulu daripada provider expansion.**
- **Jika provider gagal dan tidak memblokir core, checkpoint penyebab lalu pause provider.**
- **Provider akan digunakan kembali sebagai integration-testing dan bug-hunting layer setelah core stabil.**
- **Jangan mengubah komponen yang sudah LOCKED tanpa alasan teknis kuat.**
- **Bug yang terbukti boleh langsung diperbaiki; setelah fix wajib audit ulang + validasi + checkpoint.**

### Workflow trigger untuk obrolan baru

Jika Shin membuka obrolan baru dan mengatakan:

> **“Denia checkpoint, git, commit, aturan kerja”**

maka gunakan `main` dan checkpoint KakaAnime sebagai source of truth, baca aturan diagnosis di atas, pertahankan history, audit checkpoint terkait sebelum bekerja, dan lanjutkan dari status terakhir yang tercatat — tanpa menganggap status hijau hanya karena compile/build pernah sukses.
