# KakaAnime — Account Dashboard Work Order

**Tanggal:** 13 September 2026  
**Branch:** main

## 🎯 Keputusan kerja

Account Dashboard **tidak difinalkan sekarang**. Dashboard akan diperbaiki setelah fitur/data sumber statistiknya memiliki UI dan fondasi yang nyata.

Alasannya: statistik Dashboard harus membaca data fitur yang benar, bukan angka sementara yang nantinya harus dibongkar ulang.

## 🟢 Keputusan yang sudah dikunci

- Primary layout reference tetap screenshot Account yang diberikan Shin.
- Visual/UX KakaAnime tetap dibuat berbeda dari reference, dengan kualitas dan pola interaksi bergaya ReDantotsu.
- Sebelum aplikasi dianggap siap/rilis, seluruh Account area akan mendapat audit final.
- Dashboard final dilakukan **setelah** Anime Watched, Episode Watched, dan Favorite mempunyai UI/data yang cukup untuk menjadi sumber statistik.

## 🟡 Urutan implementasi Account yang baru

1. **Anime Watched**
   - Buat/finalisasi UI daftar anime yang benar-benar pernah ditonton.
   - Bangun fondasi data yang dapat menjadi sumber statistik Anime Watched.

2. **Episode Watched**
   - Buat/finalisasi UI/history episode yang benar-benar ditonton.
   - Data harus mencatat episode yang benar-benar watched.
   - Jangan menghitung Episode Watched dari jumlah episode tersedia atau sekadar nomor episode terakhir.

3. **Favorite**
   - Finalisasi UI/data Favorite.
   - Jumlah Favorite harus dapat dibaca oleh Dashboard.

4. **Edit Profile**
   - Username
   - Bio
   - Static profile photo untuk Free
   - Premium: animated profile photo, custom Banner Atas, custom Banner Premium

5. **Account Dashboard — FINAL PASS**
   Setelah tiga fitur statistik di atas memiliki UI/data nyata, kembali ke Dashboard untuk:
   - Anime Watched = jumlah anime unik yang pernah ditonton
   - Episode Watched = jumlah episode yang benar-benar tercatat sebagai watched
   - Favorite = jumlah anime Favorite
   - Diamonds = saldo diamond saat ini
   - menyempurnakan profile card, banner, spacing, typography, dan visual berdasarkan reference + ReDantotsu direction

6. Setelah Account Dashboard final, lanjut ke:
   - Settings
   - Appearance
   - Notifications
   - About KakaAnime
   - Premium & Diamonds
   - Final Account audit

## 🔴 Yang sengaja ditunda

- Dashboard statistics final
- Dashboard visual final
- Detailed Settings
- Detailed Notifications
- Final Account audit

## 🧭 Aturan anti-pengulangan

Jangan kembali mengimplementasikan statistik Dashboard dengan data dummy/placeholder sebelum Anime Watched, Episode Watched, dan Favorite memiliki sumber data yang jelas.

Setiap perubahan mengikuti workflow:

**change → commit → update checkpoint → verify commit → continue**

## Next action

Mulai dari **Anime Watched**, lalu lanjut **Episode Watched → Favorite → Edit Profile → Account Dashboard final**. Setelah seluruh Account area selesai, lakukan audit menyeluruh sebelum aplikasi dinyatakan siap.
