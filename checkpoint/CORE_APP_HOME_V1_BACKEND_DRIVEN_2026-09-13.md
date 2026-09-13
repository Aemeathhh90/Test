# Core App Checkpoint — Home V1 Backend Driven

**Tanggal:** 13 September 2026
**Repo:** `KakaAnime/KakaAnime`
**Branch:** `main`

## Perubahan

- Home V1 sekarang mengambil data melalui `AnimeRepository`.
- Repository memanggil `ApiClient` di `Dispatchers.IO`, sehingga network call tidak dilakukan di UI thread.
- Backend JSON dipetakan ke model `Anime` yang dipakai oleh UI.
- Jika backend/Codespace sementara tidak tersedia, Home menggunakan data lokal sebagai fallback agar UI tidak blank.
- Search, Featured, Trending, New Updates, Completed, dan Recommended menggunakan dataset backend yang sudah dimuat.
- New Updates sekarang diurutkan berdasarkan episode terbaru, bukan sekadar `reversed()`.

## Status

- 🟢 Home V1 memiliki jalur data backend → repository → UI.
- 🟢 Search bekerja terhadap dataset backend yang dimuat.
- 🟢 Fallback lokal tersedia.
- 🟡 Poster masih placeholder karena payload backend saat ini belum menyediakan `posterUrl`.
- 🟡 Continue Watching masih perlu dihubungkan ke history persistence.
- 🟡 New Updates masih berbasis latest episode dari payload, belum sistem notifikasi/update user.
- 🔴 Episode → real provider stream belum tersambung.

## Aturan berikutnya

Jangan mengejar provider tambahan. Lanjutkan Core App: Detail → persistence → episode/stream integration. Setiap tahap yang selesai dibuat checkpoint baru.

## Commits terkait

- `1e0ce11a454e567e129573b788cf8e5d3c8f7451` — backend-backed anime repository
- `5f37040ee4ba6ad44ffc81d05fdfaacfad1ea817` — Home V1 backend driven
