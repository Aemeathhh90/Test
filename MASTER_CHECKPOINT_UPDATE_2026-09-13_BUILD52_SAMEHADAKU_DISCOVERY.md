# KakaAnime / AniLab — Samehadaku Discovery Fix Checkpoint

**Tanggal:** 13 September 2026
**Source of truth:** `main`
**Status:** 🟡 Samehadaku E2E belum divalidasi ulang

## 1. Temuan Build #51

Bitrise Build #51 menjalankan:

`SamehadakuProviderE2ETest#onePieceEpisodeSevenRendersFirstFrame`

Search/detail/episode selection sudah melewati tahap sebelumnya, tetapi Media3 gagal mencapai `onRenderedFirstFrame()` dengan `Playback error=Source error`.

Klasifikasi sebelum perubahan:

**🔴 BUG — jalur discovery/resolution Samehadaku belum mengikuti struktur server/link aktual.**

Validator sebelumnya hanya membuktikan URL/manifest dapat diakses, bukan bahwa stream tersebut benar-benar merupakan candidate yang dapat dimainkan Media3.

## 2. Referensi yang dibandingkan

Dua implementasi CloudStream Samehadaku yang relevan sama-sama memakai pola:

`Episode HTML → server/download link discovery → host extractor/loadExtractor → direct media → player`

Referensi 1:
`naufalnamikaze-boop/samehadaku-repo2` — Samehadaku menggunakan `v2.samehadaku.how`, membaca daftar server `#server > ul > li > div`, melakukan `player_ajax` dengan `post/nume/type`, mengambil iframe, lalu menyerahkan host URL ke `loadExtractor`.

Referensi 2:
`HatsuneMikuUwU/cloudstream-extensions-uwu` — Samehadaku menggunakan `div#downloadb li a`, lalu `loadFixedExtractor()` → `loadExtractor()`, sambil mempertahankan referer, headers, dan quality.

OCE juga memperkuat pola bahwa provider/link collection dan host extractor dipisahkan, serta stream perlu diverifikasi lebih jauh daripada sekadar URL terlihat seperti HLS.

CloudStream hanya digunakan sebagai referensi pola. AniLab tetap native dan tidak menambahkan CloudStream sebagai dependency.

## 3. Implementasi native AniLab

### Commit `9d30ba905d2bebb0dc793efe6265f31d6fcb9c26`

File baru:
`app/src/main/java/com/kakaanime/app/provider/extractor/extractors/SamehadakuEpisodeExtractor.kt`

Fungsi utama:
- mengenali episode page Samehadaku;
- membaca `div#downloadb li a[href]` sebagai jalur discovery utama;
- membaca `#server > ul > li > div` sebagai jalur server discovery;
- melakukan POST `wp-admin/admin-ajax.php` dengan `action=player_ajax`, `post`, `nume`, `type`;
- mempertahankan `Referer`, `Origin`, `X-Requested-With`, dan User-Agent pada AJAX request;
- mengambil iframe/direct media dari response AJAX;
- fallback ke iframe langsung pada episode page;
- mengirim URL host/embed ke resolver host AniLab;
- direct `.m3u8/.mpd/.mp4/.webm` diberi `ProviderStream` dengan Referer/User-Agent;
- dedupe hasil stream.

### Commit `91b3a8b8fc345dba72fbc4cbcc4f185a979b093d`

`ExtractorRegistry` sekarang mendaftarkan `SamehadakuEpisodeExtractor` dengan priority 120.

Registry mendapat flag internal:
`includeSamehadakuEpisodeExtractor`

Flag ini dipakai nested host resolver agar Samehadaku episode extractor tidak memanggil dirinya sendiri secara rekursif.

## 4. Jalur baru

```text
Samehadaku Search
  ↓
Anime Detail
  ↓
Episode 7 URL
  ↓
SamehadakuEpisodeExtractor
  ├─ #downloadb links
  ├─ #server data-post/data-nume/data-type
  │    ↓
  │  player_ajax
  │    ↓
  │  iframe/embed
  └─ iframe fallback
  ↓
Host Extractor Registry
  ↓
Stream Validator
  ↓
Media3
  ↓
onRenderedFirstFrame()
```

## 5. Status / aturan

- Samehadaku **belum green**.
- Jangan menganggap compile/URL validation sebagai PASS.
- Gate tetap `onRenderedFirstFrame()`.
- Build berikutnya harus menguji ulang One Piece Episode 7.
- Jika masih `Source error`, klasifikasikan berdasarkan error detail dan request/response aktual sebelum menambah patch.
- Otakudesu dan komponen yang sudah terbukti first-frame tetap dianggap ⚪ LOCKED.

## 6. Next gate

Bitrise `provider_e2e` harus menjalankan kembali:

`SamehadakuProviderE2ETest#onePieceEpisodeSevenRendersFirstFrame`

Target:
**Media3 `onRenderedFirstFrame()` → Samehadaku 🟢 E2E PASS.**
