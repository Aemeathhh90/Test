# KakaAnime — Account Dashboard UI Reference

**Tanggal:** 13 September 2026  
**Repo:** `KakaAnime/KakaAnime` — Private  
**Branch:** `main`

## 🎯 Primary UI reference

Screenshot yang diberikan Shin menjadi **referensi utama layout Account Dashboard dan Edit Profile**.

KakaAnime mengikuti **hierarki/layout utama screenshot**, tetapi bahasa visual dan interaction UX dibuat dengan **gaya ReDantotsu**: glass-like surfaces, spacing yang lebih ringan, typography hierarchy, compact controls, subtle transitions, dan interaction feedback yang konsisten.

Referensi bukan untuk disalin pixel-for-pixel.

## Account Dashboard target

Urutan utama:
1. Header `Account` + subtitle + settings shortcut.
2. Banner/profile header di bagian atas.
3. Profile card: avatar, username, bio, status Free/Premium, diamonds, edit profile.
4. Premium CTA banner.
5. Statistik dalam satu row:
   - Anime Watched
   - Episode Watched
   - Favorites
   - Diamonds bila ruang/layout memungkinkan tanpa merusak hierarchy.
6. Menu Account:
   - Profile
   - Premium & Diamonds
   - Appearance
   - Notifications
   - Settings
   - About KakaAnime
7. Bottom navigation tetap menjadi navigation utama aplikasi.

## Statistik

`Anime Watched` = jumlah anime unik yang pernah ditonton.

`Episode Watched` = jumlah episode yang benar-benar telah tercatat sebagai ditonton. Jangan menghitungnya dari jumlah anime atau jumlah episode tersedia.

`Favorites` = jumlah anime yang tersimpan sebagai Favorite.

`Diamonds` = saldo diamond saat ini.

**Catatan teknis:** model watched saat ini menyimpan `title -> last watched episode`, sehingga metrik Episode Watched yang akurat membutuhkan perubahan data model/history sebelum dinyatakan 🟢.

## UX direction — ReDantotsu-inspired

- Jangan membuat Account terasa seperti halaman settings Android biasa.
- Gunakan card/surface hierarchy yang lembut dan compact.
- Icon, text, chevron, touch target, dan spacing harus konsisten.
- Interaksi antar halaman memakai transisi ringan, bukan perpindahan kasar.
- Premium state harus terlihat jelas tetapi tidak mendominasi seluruh dashboard.
- Custom KakaAnime identity tetap dipertahankan; ReDantotsu menjadi referensi UX, bukan dependency.

## Status audit

- 🟢 Screenshot/layout direction dikunci sebagai primary reference.
- 🟢 ReDantotsu-inspired UX direction dikunci.
- 🟡 Current Account Dashboard masih perlu penyesuaian visual terhadap reference.
- 🟡 Episode Watched membutuhkan model history yang benar sebelum statistik dinyatakan final.
- 🟡 Menu Settings/Notifications/Premium masih perlu diselesaikan satu per satu.

## Next

Selesaikan **Account Dashboard UI** terlebih dahulu sampai struktur dan visualnya final, lalu lanjut berurutan ke Edit Profile → Settings → Appearance → Notifications → About → Premium → final Account audit.
