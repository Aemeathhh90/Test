# KakaAnime / AniLab — MASTER CHECKPOINT

**Tanggal:** 13 September 2026  
**Repo:** `KakaAnime/KakaAnime` — Private  
**Platform:** Android  
**Source of truth:** `main`  
**Prioritas:** P0 — Provider E2E & Streaming Foundation

> MASTER ini sengaja dibuat ringkas. Detail teknis dan sejarah berada di folder `checkpoint/`.

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

```text
Search → Anime Detail → Episode → Stream Resolution
→ valid .m3u8 / .mpd / .mp4 → Media3 → onRenderedFirstFrame() → 🟢 PASS
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

- Direct stream dan embed source didukung.
- Resolver dipisahkan dari provider adapter dan reusable bila memungkinkan.
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
 → SamehadakuEpisodeExtractor
 → Host Extractor Registry
 → Stream Resolver / Validator
 → Media3
 → onRenderedFirstFrame()
```

Bitrise `provider_e2e` menjalankan:
`SamehadakuProviderE2ETest#onePieceEpisodeSevenRendersFirstFrame`

Status tetap 🟡 sampai first-frame PASS terbukti.

---

## 🧭 Klasifikasi Temuan — LOCKED

| Status | Arti | Tindakan |
|---|---|---|
| 🔴 **BUG** | Implementasi/logic/configuration kita salah | Cari akar masalah lalu fix |
| 🟡 **WORKAROUND / TAMBALAN** | Solusi sementara | Catat dan cari solusi final |
| 🟢 **ENHANCEMENT** | Sistem bekerja tetapi dapat ditingkatkan | Opsional |
| ⚪ **LOCKED / SUDAH BENAR** | Sudah terbukti bekerja | Jangan disentuh tanpa alasan teknis kuat |
| ⚫ **BLOCKED / MENTOK** | Terbukti terhambat batasan eksternal/teknis setelah audit | Eskalasi dan cari alternatif |

### Prosedur saat mentok

```text
FAIL → Pastikan bukan BUG → Cari akar masalah
→ Audit kode + log + request/response + dependency/config
→ Cari referensi terbukti → Bandingkan pendekatan
→ Implementasi native AniLab → E2E
→ Jika tetap mustahil karena batasan eksternal → ⚫ BLOCKED
```

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

Tujuannya agar error menjadi pengetahuan proyek, bukan sekadar tambalan.

---

## 📂 Struktur Checkpoint — LOCKED

```text
checkpoint/
├── MASTER_CHECKPOINT.md
├── PROVIDER_MAPPING.md
├── providers/
│   ├── OTAKUDESU_CHECKPOINT.md
│   ├── SAMEHADAKU_CHECKPOINT.md
│   └── <provider>_CHECKPOINT.md
├── extractors/
│   └── EXTRACTOR_CHECKPOINT.md
├── backend/
│   └── BACKEND_CHECKPOINT.md
├── player/
│   └── PLAYER_CHECKPOINT.md
├── ui/
│   └── UI_CHECKPOINT.md
└── builds/
    └── BUILD_<number>.md
```

Aturan: checkpoint historis tidak di-overwrite. Perubahan penting ditambahkan sebagai checkpoint/addendum baru.

---

## 🔵 Backend

Fondasi tersedia; detail backend perlu diaudit terhadap source terbaru sebelum production/green.

---

## 🔴 Berikutnya

1. Rerun Samehadaku E2E setelah fix recursion.
2. Jika FAIL, ikuti klasifikasi + protokol error sebelum perubahan.
3. Jika BLOCKED, audit dan cari pendekatan alternatif.
4. Sahkan Samehadaku hanya dengan `onRenderedFirstFrame()`.
5. Audit backend dari `main`.
6. Lanjut provider berikutnya setelah path stabil.
7. Target P0: **29/29 provider E2E PASS**.

---

## 📋 Rules

- `main` adalah source of truth.
- Jangan membuat branch untuk workflow provider rutin.
- Jangan mengejar status hijau dengan test palsu.
- Jangan menghapus/overwrite checkpoint historis.
- Setiap perubahan kode/config/arsitektur nyata wajib dicatat.
- CloudStream adalah referensi pola, bukan dependency AniLab.
