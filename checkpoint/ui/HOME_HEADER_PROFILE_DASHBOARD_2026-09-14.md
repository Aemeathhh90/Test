# KakaAnime — Home Header Profile Dashboard

**Tanggal:** 14 September 2026  
**Source of truth:** `main`  
**Status:** 🟢 Implemented; 🟡 build verification pending

## Perubahan

- Home header mengikuti referensi yang dipilih Shin.
- Brand/title `KakaAnime` dan tagline `Watch Anime, Together.` berada di top bar.
- Tombol **Message** dan **Notification** ditempatkan di atas profile header.
- Search bar di Home dihapus sesuai arahan terbaru; tidak menambahkan search kedua.
- Profile header menggunakan background artwork yang hanya mengisi bagian atas panel, bukan full-screen.
- Background diberi overlay gradient agar teks tetap terbaca.
- Dashboard shortcut mempertahankan tiga fungsi inti:
  - Diamond
  - Premium
  - Watch Together
- Continue Watching tetap membuka episode terakhir secara langsung melalui callback monetization yang sudah ada.
- Section Home tetap backend-driven.
- Offline Mode tidak ditambahkan kembali.

## Technical Notes

- Background artwork memakai poster/metadata image yang sudah tersedia dari `AniListMetadataService`, sehingga tidak menambah asset gambar statis baru.
- Message dan Notification saat ini adalah UI action placeholders; integrasi Social/Notification backend tetap ditunda sampai fase Social + Watch Together.
- Search logic dan `SearchAnimeRow` dihapus dari Home karena search tidak lagi menjadi bagian dari header Home versi desain ini. Search discovery dapat ditambahkan kembali pada layar Explore bila dibutuhkan, tanpa mengubah header.

## Commit

`091a341e28982912a4863bce15a66768d0cb2bb6` — `ui: refine Home profile header and top actions`

## Status Audit

- 🟢 Header/profile dashboard
- 🟢 Half-height artwork background
- 🟢 Message + notification placement
- 🟢 No duplicate/new search bar
- 🟢 Existing Continue Watching behavior preserved
- 🟡 Android build/runtime verification
- 🟡 Core V1 audit
- ⏸ Watch Together + Social implementation
- 🟢 Download remains roadmap
- ❌ Offline Mode removed

## Next Step

Verify the new Home UI with Android build. If green, continue Core V1 audit without entering Social/Watch Together implementation yet.
