# KakaAnime UI — Reference-First Implementation Rule

**Tanggal:** 13 September 2026  
**Repo:** `KakaAnime/KakaAnime`  
**Branch:** `main`  
**Status:** 🔒 LOCKED WORKFLOW RULE

## Tujuan

UI KakaAnime yang ditargetkan mengikuti level referensi aplikasi tertentu (terutama ReDantotsu) **tidak boleh dibuat berdasarkan tebakan visual semata**.

## Aturan Wajib

```text
USER REQUIREMENT
      ↓
IDENTIFY REFERENCE
      ↓
AUDIT REFERENCE SOURCE CODE / OFFICIAL DOCS
      ↓
UNDERSTAND IMPLEMENTATION + UX PATTERN
      ↓
COMPARE WITH KakaAnime ARCHITECTURE
      ↓
COMPARE ALTERNATIVES WHEN RELEVANT
      ↓
PROPOSE RECOMMENDED APPROACH TO USER
      ↓
USER AGREEMENT / CLEAR TECHNICAL DECISION
      ↓
IMPLEMENT NATIVE KakaAnime CODE
      ↓
VERIFY
      ↓
COMMIT
      ↓
UPDATE CHECKPOINT
```

## Reference Policy

- Jika user meminta UI/UX mengikuti aplikasi tertentu, cari source code referensinya terlebih dahulu jika source tersedia.
- Untuk ReDantotsu, audit implementasi aktual dari repository/source code, bukan hanya screenshot atau ingatan visual.
- Untuk komponen teknis seperti Video Player, Media3/ExoPlayer, seek controls, progress bar, quality selector, auto-next, auto-skip, fullscreen/landscape, navigation, animation, dan theme, cari implementasi atau dokumentasi teknis yang relevan sebelum menentukan struktur kode.
- Jika terdapat beberapa pendekatan yang masuk akal, bandingkan trade-off-nya dan berikan rekomendasi kepada user sebelum melakukan perubahan arsitektur besar.
- Reference source adalah acuan pola dan validasi teknis; **jangan copy-paste dependency atau arsitektur pihak lain secara buta**.
- Implementasi akhir harus tetap native terhadap arsitektur KakaAnime dan tidak boleh merusak komponen yang sudah terbukti/LOCKED tanpa alasan teknis kuat.

## Jika Referensi Tidak Ditemukan

Jangan mengarang seolah-olah implementasi tersebut berasal dari referensi.

Catat bahwa referensi tidak ditemukan, jelaskan keterbatasannya, lalu tawarkan alternatif berdasarkan dokumentasi resmi, library yang digunakan KakaAnime, atau implementasi open-source lain yang dapat diverifikasi.

## Keputusan UI

Untuk setiap perubahan UI penting, catat:

1. Referensi yang diaudit.
2. Source/file/komponen yang relevan jika tersedia.
3. Bagian yang diadaptasi.
4. Perbedaan yang sengaja dibuat untuk KakaAnime.
5. Alternatif yang dibandingkan jika relevan.
6. Alasan pendekatan yang dipilih.
7. Status validasi.

## Prinsip Utama

> **Jangan tebak UI dari visual lalu langsung menulis kode. Cari referensinya dulu, audit implementasinya, bandingkan bila perlu, sarankan pilihan terbaik ke user, baru implementasikan.**

Aturan ini berlaku terutama untuk Video Player dan UI/UX yang secara eksplisit diminta mengikuti ReDantotsu, Saikou, atau referensi aplikasi/source tertentu lainnya.
