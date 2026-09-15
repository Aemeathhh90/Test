# KakaAnime / AniLab — MASTER CHECKPOINT

**Tanggal:** 15 September 2026  
**Repo utama:** `Aemeathhh90/Test`  
**Platform:** Android  
**Source of truth:** `main`  
**Prioritas:** Core App + Provider integration layer

> MASTER ini sengaja ringkas. Detail teknis dan sejarah tetap berada di `checkpoint/`.

---

## 🚦 Status Utama

| Area | Status | Keterangan |
|---|---|---|
| 🟡 Provider | **Terpisah / investigasi aktif** | Workspace dedicated: `Aemeathhh90/vider`. Provider E2E manual-only. |
| 🟡 UI/UX | **Terpisah / pengembangan aktif** | Workspace dedicated: `Aemeathhh90/uy-uk`. Android Build menjadi gate rutin. |
| 🟢 Main integration | **Safety net / integration** | `Aemeathhh90/Test` menyimpan integrasi, checkpoint, history, dan release state. |
| 🟡 Provider E2E | **Special test** | Hanya dijalankan ketika perlu membuktikan provider → stream → playback. |

### Repository split

```text
Aemeathhh90/Test
├── main integration / release / checkpoints / history
│
├── Aemeathhh90/vider
│   └── Provider / Source Resolver / Extraction / Stream / Playback / E2E
│
└── Aemeathhh90/uy-uk
    └── UI/UX / Home / Detail / Search / Favorite / Social / Profile /
        Diamond / Premium / Theme / Navigation / Player UI
```

---

## 🔒 Mandatory Pre-flight — LOCKED

**Sebelum audit ATAU implementasi, wajib cek repository terlebih dahulu.**

```text
CHECK CURRENT REPO
      ↓
CHECK BRANCH + LATEST COMMIT
      ↓
READ RELEVANT CHECKPOINT / HISTORY
      ↓
INSPECT CURRENT CODE / WORKFLOW / LOGS
      ↓
AUDIT / DESIGN REVIEW
      ↓
REFERENCE CHECK IF NEEDED
      ↓
IMPLEMENT
      ↓
VALIDATE
      ↓
CHECKPOINT
```

- Jangan menganggap repository masih sama dengan percakapan, screenshot, atau memory sebelumnya.
- Jangan mengubah file sebelum state saat ini diketahui.
- Untuk repo baru, cek README, commit awal, struktur, workflow, dan checkpoint yang tersedia sebelum menambahkan kode.
- Jika audit menemukan perbedaan dari checkpoint lama, **state repository saat ini menjadi baseline** dan perbedaannya dicatat.

---

## 🧭 Klasifikasi Temuan — LOCKED

| Status | Arti | Tindakan |
|---|---|---|
| 🔴 **BUG** | Implementasi/logic/configuration kita salah | Cari akar masalah lalu fix |
| 🟡 **WORKAROUND** | Solusi sementara | Catat dan cari solusi final bila diperlukan |
| 🟢 **ENHANCEMENT** | Sistem bekerja tetapi dapat ditingkatkan | Opsional |
| ⚪ **LOCKED** | Sudah terbukti benar/ditetapkan | Jangan disentuh tanpa alasan teknis kuat |
| ⚫ **BLOCKED** | Terbukti terhambat batasan eksternal/teknis setelah audit | Eskalasi dan cari alternatif |

---

## 🔍 Checkpoint Diagnosis — LOCKED

**Checkpoint bukan sekadar changelog. Checkpoint adalah knowledge base diagnosis proyek.**

Untuk masalah penting, catat:

1. 🔴 **Error / gejala** — pesan error, assertion, crash, atau perilaku yang terlihat.
2. 🔎 **Root cause** — penyebab yang sudah dibuktikan; jika belum terbukti, tulis sebagai **hipotesis**.
3. 🧩 **Lapisan gagal** — UI / state / repository / backend / provider / extractor / validator / normalizer / selector / metadata handoff / Media3 / runtime.
4. 🏗️ **Arsitektur sebelum fix** — jalur data/flow yang menyebabkan kegagalan.
5. 📚 **Referensi yang dipakai** — GitHub, CloudStream, dokumentasi resmi, log historis, atau checkpoint terkait.
6. 🔧 **Fix** — perubahan konkret dan commit SHA.
7. 🧪 **Validasi** — test/build/runtime evidence yang benar-benar dijalankan.
8. 📊 **Hasil** — PASS, FAIL, PARTIAL, atau UNKNOWN.
9. 🧠 **Pelajaran diagnosis** — pola yang harus dicari jika masalah serupa muncul lagi.
10. 📌 **Dampak** — komponen lain yang perlu diaudit setelah perubahan.
11. ➡️ **Next diagnostic step** — hanya jika masih gagal; harus spesifik pada lapisan berikutnya.

### Aturan diagnosis

- Jangan menghapus diagnosis lama setelah bug diperbaiki.
- Jangan menulis root cause sebagai fakta sebelum ada bukti cukup.
- Jangan menumpuk patch tanpa memperbarui diagnosis.
- Jika diagnosis baru membantah diagnosis lama, **catat koreksinya**, jangan menimpa sejarah.
- Sebelum memperbaiki bug baru, audit checkpoint terkait terlebih dahulu.
- Jika bug pernah terjadi, gunakan diagnosis sebelumnya sebagai baseline dan verifikasi apakah pola kegagalannya sama.

---

## 🔧 Audit → Reference → Fix → Validate — LOCKED

```text
REPO CHECK
 ↓
AUDIT CURRENT STATE
 ↓
Tentukan root cause / hipotesis
 ↓
Cari referensi? YA / TIDAK
 ↓
Jika YA → CloudStream / GitHub / docs resmi
 ↓
Cek kecocokan dengan arsitektur KakaAnime
 ↓
FIX / IMPLEMENT jika evidence cukup kuat
 ↓
VALIDASI
 ↓
PASS → checkpoint
FAIL → diagnosis baru + audit ulang
```

- Referensi dicari bila masalah menyangkut extractor/provider, Media3/player, WebView, parsing/stream resolution, atau pola mapan yang dapat mengurangi ketidakpastian.
- Tidak perlu mencari referensi tambahan jika root cause sudah jelas dan fix sederhana/terverifikasi.
- Referensi adalah pola/validasi teknis; jangan copy-paste dependency CloudStream ke KakaAnime.
- Jangan menambal berdasarkan tebakan ketika root cause belum cukup kuat.

---

## 🎬 Provider Gate — LOCKED

Provider hanya dinyatakan 🟢 jika alur berikut terbukti sampai Media3:

```text
Search → Anime Detail → Episode → Stream Resolution
→ valid .m3u8 / .mpd / .mp4 → Media3
→ onRenderedFirstFrame() → 🟢 PASS
```

Build/compile sukses saja **tidak cukup** untuk menyatakan playback provider hijau.

---

## 🧪 Testing Policy — LOCKED

- **Android Build** = gate rutin untuk perubahan biasa.
- **Provider E2E** = special test, manual-only.
- Jangan menjalankan Provider E2E untuk perubahan UI biasa.
- Jangan dispatch GitHub Actions dari assistant; user yang menjalankan workflow manual ketika dibutuhkan.
- Provider E2E yang gagal tidak otomatis memblokir pekerjaan UI/UX yang tidak bergantung pada provider.
- Setelah provider → stream → playback terbukti, hasilnya dicatat sebagai checkpoint sebelum integrasi.

---

## 📂 Checkpoint Structure — LOCKED

```text
checkpoint/
├── README.md                 ← index aktif
├── MASTER_CHECKPOINT.md      ← dashboard + aturan locked
├── PROVIDER_MAPPING.md
├── providers/                ← history/provider diagnosis
├── extractors/               ← extractor/stream architecture
├── backend/                  ← backend notes
├── player/                   ← Media3/player
├── ui/                       ← UI decisions
├── social/                   ← Social/Profile decisions
├── builds/                   ← CI/E2E evidence
├── audit/                    ← audit evidence
├── recovery/                 ← recovery/rollback evidence
├── bughunter/                ← focused bug hunts
└── data/                     ← data/model checkpoints
```

**Aturan history:** jangan menghapus atau overwrite checkpoint historis hanya demi merapikan tampilan. Jika keputusan berubah, buat checkpoint/addendum baru dan hubungkan dengan keputusan lama.

---

## 📋 Work Rules — LOCKED

- `main` adalah source of truth untuk repo utama.
- Dedicated provider/UI repo dikembangkan independen dari main integration.
- Routine validation menggunakan Android Build.
- Provider E2E manual-only.
- Jangan mengejar status hijau dengan test palsu.
- Setiap perubahan kode/config/arsitektur nyata wajib dicatat.
- Setiap bug/failure penting wajib dicatat sebagai diagnosis reusable.
- Root cause yang belum terbukti harus ditandai sebagai hipotesis.
- CloudStream adalah referensi pola, bukan dependency KakaAnime.
- Core app dan UI/UX tidak perlu menunggu provider E2E jika tidak bergantung pada provider.
- Komponen yang sudah LOCKED tidak diubah tanpa alasan teknis kuat.
- Bug yang terbukti boleh langsung diperbaiki; setelah fix wajib validasi + checkpoint.

### Workflow trigger untuk obrolan baru

Jika Shin membuka obrolan baru dan mengatakan:

> **“Denia checkpoint, git, commit, aturan kerja”**

gunakan repo utama + checkpoint sebagai source of truth, **cek repository state terlebih dahulu**, baca aturan diagnosis, pertahankan history, audit checkpoint terkait sebelum bekerja, dan lanjutkan dari status terakhir yang benar-benar tercatat.
