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
| 🟡 Samehadaku | **Sedang dites** | Target One Piece Episode 7; discovery sekarang mengikuti CloudStream-style HTML-first |
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
- Dynamic domain/endpoint dan remote configuration tetap menjadi arah arsitektur.
- CloudStream hanya **referensi arsitektur**, bukan dependency AniLab.
- Kompleksitas provider/host/resolver tidak ditampilkan kepada user.

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

Pendekatan terbaru:

```text
Samehadaku HTML source
 → CloudStream-style Search / Detail / Episode discovery
 → Episode page URL
 → Extractor Registry
 → Stream Resolver
 → Stream Validator
 → Media3
 → onRenderedFirstFrame()
```

Temuan audit:
- CloudStream Samehadaku yang aktif menggunakan HTML-first discovery pada `v2.samehadaku.how`.
- Search memakai query WordPress `?s=...` dan selector kartu anime.
- Detail dan episode diambil langsung dari halaman anime.
- Link episode kemudian diberikan ke jalur extractor (`loadExtractor`-style).
- Situs Samehadaku saat ini memang menyediakan halaman One Piece dan daftar episode, termasuk Episode 7.
- Implementasi gateway/API tetap dipertahankan sebagai **fallback**, bukan jalur utama.

Bitrise `provider_e2e` sekarang menjalankan:

`SamehadakuProviderE2ETest#onePieceEpisodeSevenRendersFirstFrame`

**Status:** 🟡 menunggu hasil E2E setelah perubahan CloudStream-style.

---

## 🧭 Klasifikasi Temuan & Aturan Perbaikan — LOCKED

Setiap error/temuan **wajib diklasifikasikan sebelum kode diubah**. Jangan langsung menambal hanya supaya test hijau.

| Status | Arti | Tindakan |
|---|---|---|
| 🔴 **BUG** | Implementasi/logic/configuration kita memang salah | Cari akar masalah lalu **fix** |
| 🟡 **WORKAROUND / TAMBALAN** | Solusi sementara agar sistem tetap berjalan | Boleh sementara, tetapi **bukan solusi final**; cari pengganti yang lebih sehat |
| 🟢 **ENHANCEMENT** | Sistem sudah bekerja, tetapi masih dapat ditingkatkan | Tidak wajib; lakukan bila ada manfaat teknis/UX yang jelas |
| ⚪ **LOCKED / SUDAH BENAR** | Sudah terbukti bekerja dan perubahan tidak diperlukan | **Jangan disentuh** tanpa alasan teknis yang kuat |
| ⚫ **BLOCKED / MENTOK** | Pendekatan saat ini benar-benar mentok atau ada batasan eksternal | Jangan memaksa dengan tambalan; lakukan eskalasi dan cari referensi/pendekatan alternatif |

### Prosedur saat mentok (⚫ BLOCKED)

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
   ├─ CloudStream / extension
   ├─ extractor/provider open-source
   ├─ dokumentasi/library
   ├─ aplikasi sejenis
   └─ implementasi/versi provider lain yang relevan
 ↓
Bandingkan beberapa pendekatan
 ↓
Pilih solusi paling sehat
 ↓
Implementasi native AniLab
 ↓
E2E / validasi nyata
 ↓
Jika tetap mustahil karena batasan eksternal → tetap ⚫ BLOCKED
```

**Aturan penting:**
- Jangan menyebut sesuatu sebagai “nggak bisa diperbaiki” sebelum akar masalah dan referensi alternatif diperiksa.
- Jangan menambal bug berulang kali jika akar masalahnya belum diselesaikan.
- Referensi eksternal digunakan untuk menemukan **pola solusi**, bukan untuk membuat AniLab bergantung pada proyek lain tanpa alasan.
- Jika solusi sementara dipakai, harus jelas bahwa itu 🟡 WORKAROUND dan dicatat untuk diganti bila solusi final ditemukan.
- Jika sebuah bagian sudah terbukti benar, terutama yang sudah mencapai `onRenderedFirstFrame()`, perlakukan sebagai ⚪ LOCKED agar tidak dibongkar tanpa alasan.
- Setiap perubahan kode/config/arsitektur yang nyata harus dicatat dalam checkpoint/addendum.

---

## 🔵 Backend

**Status:** Fondasi tersedia; detail backend dipisahkan dari MASTER dan perlu diaudit terhadap source terbaru sebelum diberi status production/green.

- Backend diperlakukan sebagai service/API terpisah dari aplikasi Android.
- Detail source, endpoint, deployment, environment, security, dan audit backend:
  → `BACKEND_CHECKPOINT.md`
- MASTER hanya menyimpan status ringkas agar tetap mudah dibaca.

---

## 🔴 Berikutnya

1. Jalankan ulang Samehadaku E2E setelah perubahan HTML-first.
2. Jika search lolos, lanjut audit detail → episode 7 → stream → first frame.
3. Jika FAIL, klasifikasikan dulu: 🔴 BUG / 🟡 WORKAROUND / 🟢 ENHANCEMENT / ⚪ LOCKED / ⚫ BLOCKED.
4. Jika ⚫ BLOCKED, cari dan bandingkan referensi/pendekatan alternatif sebelum memutuskan tidak bisa.
5. Sahkan Samehadaku hanya jika benar-benar first-frame PASS.
6. Audit backend dari source `main`, lalu isi `BACKEND_CHECKPOINT.md` dengan fakta aktual.
7. Setelah provider stabil, lanjut provider berikutnya.
8. Target akhir P0: **29/29 provider E2E PASS**.

---

## 📚 Checkpoint Detail

| File | Isi |
|---|---|
| `BACKEND_CHECKPOINT.md` | Backend/API, deployment, environment, security, dan audit |
| `EXTRACTOR_CHECKPOINT.md` | Extractor Registry, resolver, validator, extractor |
| `MASTER_CHECKPOINT_UPDATE_2026-09-13_BUILD44.md` | Build #44 + Otakudesu E2E history |
| `PROVIDER_MAPPING_2026-09-13.md` | Mapping/provider reference |
| `PROVIDER_CHECKPOINT.md` | Detail adapter + hasil E2E provider, jika tersedia |
| `PLAYER_CHECKPOINT.md` | Media3 + VideoPlayer |
| `UI_CHECKPOINT.md` | UI/Home/detail/player UI |

> Checkpoint lama **tidak dihapus**. Versi MASTER sebelumnya tetap tersedia melalui Git history; perubahan ini hanya membuat file MASTER aktif lebih ringkas dan memisahkan detail backend.

---

## 🧭 Rules

- Jangan mengejar status hijau dengan test palsu.
- Jangan menandai provider green tanpa bukti playback nyata.
- Jangan membuat branch untuk workflow rutin; `main` adalah source of truth.
- Jangan overwrite checkpoint historis; gunakan addendum/checkpoint baru.
- Jangan mengubah arsitektur yang sudah terbukti tanpa alasan teknis yang jelas.
- Jangan mengarang endpoint/status backend; verifikasi source `main` terlebih dahulu.
- CloudStream adalah referensi utama untuk pola provider/extractor; implementasi AniLab tetap native dan tidak bergantung pada CloudStream.
- **Wajib klasifikasikan temuan sebelum memperbaiki:** 🔴 BUG / 🟡 WORKAROUND / 🟢 ENHANCEMENT / ⚪ LOCKED / ⚫ BLOCKED.
- **Jika mentok, eskalasi dan cari referensi/pendekatan alternatif terlebih dahulu; jangan langsung menyerah atau menumpuk tambalan.**
