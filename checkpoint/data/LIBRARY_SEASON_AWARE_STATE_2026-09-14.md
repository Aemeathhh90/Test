# KakaAnime Checkpoint — Library Season-Aware State

**Tanggal:** 14 September 2026  
**Repo:** `KakaAnime/KakaAnime`  
**Branch:** `main`

## Perubahan

### 🟢 Library Favorite
- Library sekarang membaca Favorite season-aware melalui `favoriteGroupIds`.
- Favorite tetap ditampilkan **satu item per Anime Group**, sehingga Season 1/2 tidak menggandakan koleksi.
- Legacy title-based Favorite tetap dibaca sebagai compatibility fallback dan dipetakan ke group jika katalog memberikan mapping.

### 🟢 Library History
- History utama sekarang memakai `SeasonAwareWatchHistoryEntry` melalui `SeasonAwareStateRepository`.
- Identity History mengikuti `Anime Group + Season + Episode`.
- Riwayat lama berbasis title masih dibaca sebagai fallback hanya jika mapping title menghasilkan satu group yang tidak ambigu.
- Item History menampilkan label Season jika tersedia.
- Delete dan Clear History menggunakan storage season-aware.

### 🟢 UI/UX
- Tidak menambah fitur baru atau gesture baru.
- Struktur Library yang sudah dipoles dipertahankan.
- Season hanya ditampilkan sebagai konteks informasi; Favorite tetap group-level.

## Audit

- `SeasonAwareStateRepository` sudah menyediakan facade untuk favorite, watched, unlocked, history, delete, dan clear. 
- `KakaAnimePreferences` sudah memiliki storage season-aware dan compatibility storage legacy.
- Tidak ada penghapusan legacy data pada perubahan ini.

## Status verifikasi

- 🟢 Source wiring: selesai.
- 🟡 Build/CI: belum terverifikasi pada checkpoint ini.
- 🟡 Runtime/device: belum terverifikasi.

## Next

1. Audit `FavoriteScreen` terhadap group-level Favorite + season-level progress.
2. Audit `CalendarScreen` karena masih menerima/menampilkan state yang berasal dari MainActivity.
3. Setelah Library/Favorite/Calendar konsisten, lanjut global state consistency audit sebelum fitur core berikutnya.
