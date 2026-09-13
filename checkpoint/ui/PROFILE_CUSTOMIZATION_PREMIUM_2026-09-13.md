# KakaAnime — Profile Customization Premium Addendum

**Tanggal:** 13 September 2026  
**Repo:** `KakaAnime/KakaAnime` — Private  
**Branch:** `main`

## 🟢 Requirement baru — disepakati Shin

Customisasi profil dibagi menjadi fitur Free dan Premium.

### Free
- 🟢 Edit username
- 🟢 Edit bio
- 🟢 Ubah foto profil statis
- 🔴 Custom Banner Atas
- 🔴 Custom Banner Premium
- 🔴 Foto profil bergerak

### Premium
- 🟢 Semua fitur Free
- 🟢 Upload/custom **Banner Atas**
- 🟢 Upload/custom **Banner Premium**
- 🟢 **Foto profil bergerak**

## Media custom

Banner Atas, Banner Premium, dan foto profil Premium direncanakan mendukung media bergerak dengan batasan agar tidak membebani aplikasi:
- gambar biasa (JPG/PNG/WEBP)
- GIF/animasi
- video pendek bila diperlukan
- pembatasan ukuran, resolusi, dan durasi untuk media bergerak
- media disimpan secara persisten agar tetap tersedia setelah aplikasi dibuka kembali

## UX Edit Profile

Edit Profile akan menyediakan area terpisah:
1. Foto Profil → ubah foto profil
2. Banner Atas → ubah banner atas
3. Banner Premium → ubah banner premium
4. Username / Bio
5. Simpan

Untuk user Free, kontrol custom Banner Atas, Banner Premium, dan foto profil bergerak harus mengarah ke alur Upgrade to Premium, bukan popup iklan yang mengganggu.

## Aturan Premium Banner

Banner Premium tetap merupakan banner untuk kartu/status Premium:
- Free → `Upgrade to Premium`
- Premium → `Premium Aktif`

Yang dapat dicustom user adalah **media/background banner**, bukan status atau fungsi Premium.

## 🔴 Implementasi

Requirement sudah dicatat sebagai target fitur. Implementasi upload/media picker, penyimpanan URI/media, rendering animasi, batas ukuran/durasi, dan gating Premium belum dikerjakan pada checkpoint ini.

## Catatan kompatibilitas

Implementasi tidak boleh mengganggu persistence profile yang sudah ada. Custom banner lama berbasis index tetap dapat dipertahankan untuk backward compatibility sampai migrasi media custom benar-benar diterapkan.
