# Bug Hunt — Anime Detail + Player

**Tanggal:** 14 September 2026  
**Scope:** Anime Detail, Episode Gate, Player, watch state, diamond state

## Findings

### 🔴 FIXED — Diamond dapat terpotong berulang untuk episode yang sama

`MainActivity.openEpisode()` sekarang memeriksa persisted `unlocked_episodes` sebelum konsumsi diamond. Episode yang sudah dibuka lewat diamond/rewarded ad tidak meminta diamond lagi ketika dibuka ulang.

Commits:
- `9598bc0ad93d0c8100f022a8acf48b07b2b15472` — persistence layer
- `465ef0184333821a88cb2b6633bbe9763e91ea2e` — gate integration + key fix

### 🟢 FIXED — Watch history tidak lagi bergantung hanya pada URL resolver

Sebelumnya episode dicatat sebagai watched setelah delay 15 detik jika URL stream berhasil ditemukan. URL yang valid belum membuktikan video benar-benar tampil.

Fix:
- `VideoPlayerScreen` memasang `Player.Listener` dan meneruskan `onRenderedFirstFrame()`.
- `MainActivity` menyimpan `streamFirstFrameRendered` dan meresetnya setiap episode/resolution cycle.
- Delay 15 detik tetap dipertahankan agar indikator watched tidak muncul seketika, tetapi sekarang history hanya direkam jika Media3 sudah benar-benar merender frame pertama.

Commits:
- `c88036fdf6b57cdd6e5147b0dda10629a53b3229` — first-frame callback
- `a250f24b0ff99120f02d799d1cb787edcbfee6a3` — watch-history guard

### 🟡 Risk — Watch history belum merupakan completion tracking penuh

Status watched masih ditentukan setelah ambang 15 detik, bukan setelah episode selesai. Ini dipertahankan untuk V1 dan perlu keputusan foundation terpisah jika nanti ingin resume/progress yang lebih akurat.

### 🟢 FIXED — Quality switch dapat memicu resolver/restart ganda

Akar masalahnya adalah effect berbasis `selectedQuality` dan effect berbasis `qualityRequest` sama-sama melakukan resolution. Setelah user memilih kualitas, `selectedQuality` berubah dan dapat menjalankan resolver kedua kalinya.

Fix:
- Resolution hanya dijalankan dari `qualityRequest`.
- Effect quality tidak lagi bergantung pada `selectedQuality` sebagai trigger resolver.
- Hasil resolver yang sama dengan media URL aktif tidak dipasang ulang.
- Posisi playback tetap dipertahankan saat URL kualitas benar-benar berubah.

Commit:
- `8ae6cdd442ad54564f36897d5878af6f5857fe35`

### 🟡 Risk — Stream resolver/provider behavior

`ProviderPlaybackResolver` tetap perlu diuji pada network/device nyata. Tidak ada klaim provider green dari perubahan ini.

## Locked / no change

- Premium gate 1080p.
- Auto-skip intro/outro tetap Premium-only.
- Final interaction/gesture pass tetap ditunda sampai foundation fitur selesai.
- Download tetap pending dan tidak disentuh.

## Validation status

Static audit: 🟢  
Bug fix implementation: 🟢  
Android build: 🟡 pending latest CI result  
Device runtime: 🟡 pending  
Provider E2E: 🟡 pending actual first-frame/device evidence

## Next bug-hunt targets

1. Detail → episode → gate → rewarded ad → player.
2. Reopen same unlocked episode and verify zero additional diamond charge.
3. Kill/relaunch app and verify unlocked episode persists.
4. Player → previous/next → gate behavior.
5. Stream failure → retry → no duplicate charge.
6. Rotation/landscape lifecycle and player state.
7. Watched state/history consistency after Library changes.
8. Build/CI verification for the latest commits.
