# Bug Hunt — Anime Detail + Player

**Tanggal:** 14 September 2026  
**Scope:** Anime Detail, Episode Gate, Player, watch state, diamond state

## Findings

### 🔴 BUG — Diamond dapat terpotong berulang untuk episode yang sama

**Akar masalah:** `MainActivity.openEpisode()` sebelumnya hanya memeriksa Premium lalu langsung memanggil `DiamondRules.consumeForEpisode()`. Tidak ada state persisted untuk episode yang sudah dibuka. Akibatnya episode yang sama dapat meminta/consume diamond lagi ketika dibuka ulang, termasuk sebelum history 15 detik tercatat.

**Fix:** tambah persisted `unlocked_episodes` di `KakaAnimePreferences`. Episode yang dibuka menggunakan diamond atau rewarded ad sekarang ditandai unlocked; pembukaan ulang episode yang sama tidak mengonsumsi diamond lagi. Premium tetap bypass gate.

Commits:
- `9598bc0ad93d0c8100f022a8acf48b07b2b15472` — persistence layer
- `465ef0184333821a88cb2b6633bbe9763e91ea2e` — gate integration + key fix

### 🟡 Risk — Watch history dicatat setelah 15 detik, bukan berdasarkan playback completion

Saat ini `MainActivity` menandai episode sebagai watched setelah stream berhasil ditemukan dan delay 15 detik selesai. Ini menjaga indikator progress dasar tetap hidup, tetapi belum merupakan completion tracking yang akurat.

**Tindakan:** tidak ditambal pada bug hunt ini karena perubahan completion semantics perlu UI/data foundation tersendiri. Target integration testing/future watch-state pass.

### 🟡 Risk — Stream resolver berjalan di Compose effect

`ProviderPlaybackResolver.resolve()` dipanggil dari `LaunchedEffect`, yang secara teknis coroutine-friendly, tetapi provider latency/error behavior tetap perlu diuji pada device/network nyata.

**Tindakan:** integration testing.

### 🟢 Locked / no change

- Premium gate untuk 1080p.
- Auto-skip intro/outro tetap Premium-only.
- Provider green status tidak berubah tanpa `onRenderedFirstFrame()` evidence.
- Final interaction/gesture pass tetap ditunda.

## Validation status

Static audit: 🟢  
Bug fix implementation: 🟢  
Android build: 🟡 pending latest CI result  
Device runtime: 🟡 pending  
Provider E2E: unchanged / separate gate

## Next bug-hunt targets

1. Detail → episode → gate → rewarded ad → player.
2. Reopen same unlocked episode and verify zero additional diamond charge.
3. Kill/relaunch app and verify unlocked episode persists.
4. Player → previous/next → gate behavior.
5. Stream failure → retry → no duplicate charge.
6. Rotation/landscape lifecycle and player state.
7. Quality switch and Premium 1080p gating.
8. Watched state/history consistency.
