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
5. Statistik dalam satu row.
6. Menu Account dengan urutan final:
   - ⚙️ Settings
   - 👤 Profile
   - 🎨 Appearance
   - 💎 Premium
   - 🔔 Notifications
   - ℹ️ About KakaAnime
7. Bottom navigation tetap menjadi navigation utama aplikasi.

## Menu UX

Urutan menu di atas sekarang **dikunci sebagai versi yang dipilih Shin**. Ikon visual mengikuti makna emoji/reference tersebut, sementara rendering Android tetap memakai icon vector Material yang konsisten dengan bahasa UI KakaAnime.

- `Settings` membuka entry settings dasar sementara; detail settings dapat dikembangkan tanpa mengubah urutan/layout.
- `Profile` membuka Edit Profile.
- `Appearance` membuka kontrol theme/accent.
- `Premium` membuka Premium flow.
- `Notifications` memiliki entry screen/dialog sementara; integrasi notifikasi aktual tetap tahap berikutnya.
- `About KakaAnime` membuka informasi aplikasi.

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
- 🟢 Menu Account order dikunci: Settings → Profile → Appearance → Premium → Notifications → About.
- 🟢 Semua enam entry menu sekarang memiliki action/entry UI dasar.
- 🟡 Current Account Dashboard masih perlu penyesuaian visual terhadap reference.
- 🟡 Episode Watched membutuhkan model history yang benar sebelum statistik dinyatakan final.
- 🟡 Detail Settings/Notifications masih perlu diselesaikan.

## Latest change

Commit: `8cd7a1e57e628e080984410b3aaeeca6f25263a6`  
`account: align menu with approved reference order`

## Next

Selesaikan **Account Dashboard UI** sampai struktur dan visualnya final, lalu lanjut berurutan ke Edit Profile → Settings → Appearance → Notifications → About → Premium → final Account audit.
