# KakaAnime Provider Status

Branch: `provider-batch-2026-09-12`

## Status rules

- 🟢 **Green — E2E proven:** Search → anime detail → episode → stream resolution → runtime stream validation → Media3 reaches the first rendered video frame.
- 🟡 **Yellow — implemented, not E2E proven:** Adapter/gateway exists, but there is no successful playback evidence yet.
- 🔴 **Red — confirmed dead:** A current test has proven that the provider cannot complete the required flow. An old/unattributed note is not enough to assign red.

A dedicated adapter is **not** automatically green.

## Current registry audit

`ProviderFactory` currently registers **29 providers**. `DemoProvider.kt` exists as a source file but is not registered, and HiAnime is intentionally excluded. Therefore the runtime registry is 29, not 30.

| # | Provider | Current status | E2E evidence |
|---:|---|:---:|---|
| 1 | Otakudesu | 🟡 | Pending |
| 2 | Samehadaku | 🟡 | Pending |
| 3 | AllAnime | 🟡 | E2E harness added; not yet passed |
| 4 | Kuronime | 🟡 | E2E harness added; not yet passed |
| 5 | Animasu | 🟡 | Pending |
| 6 | AnimeIndo | 🟡 | Pending |
| 7 | Zoronime | 🟡 | Pending |
| 8 | Anoboy | 🟡 | Pending |
| 9 | AnimeKompi | 🟡 | Pending |
| 10 | Doronime | 🟡 | Pending |
| 11 | Hunter no Sekai | 🟡 | Pending |
| 12 | Gomunime | 🟡 | Pending |
| 13 | NeoNime | 🟡 | Pending |
| 14 | YLNime | 🟡 | Pending |
| 15 | NontonAnimeID | 🟡 | Pending |
| 16 | Animeisme | 🟡 | Pending |
| 17 | Animeku | 🟡 | Pending |
| 18 | Oploverz | 🟡 | Pending |
| 19 | Kuramanime | 🟡 | Pending |
| 20 | Wibudesu | 🟡 | Pending |
| 21 | Meownime | 🟡 | Pending |
| 22 | Anibatch | 🟡 | Pending |
| 23 | Nimegami | 🟡 | Pending |
| 24 | Drivenime | 🟡 | Pending |
| 25 | Anitoki | 🟡 | Pending |
| 26 | RiiE | 🟡 | Pending |
| 27 | Kusonime | 🟡 | Pending |
| 28 | Animekuindo | 🟡 | Pending |
| 29 | AnimeSail | 🟡 | Pending |

## Current count

- 🟢 E2E proven: **0**
- 🟡 Implemented but not E2E proven: **29**
- 🔴 Confirmed dead with current attributable evidence: **0**
- Runtime registry total: **29**

The previous batch screenshot reported 10 “dedicated/strong” providers and 1 “confirmed dead”. Those labels were implementation/batch labels, not the strict playback status above. The provider ID for the historical dead entry is not preserved in the current status data, so it is intentionally not guessed here.

## E2E gate

Kuronime and AllAnime now have isolated instrumentation tests at:

- `app/src/androidTest/java/com/kakaanime/app/provider/KuronimePlaybackE2eTest.kt`
- `app/src/androidTest/java/com/kakaanime/app/provider/AllAnimePlaybackE2eTest.kt`

Both tests require a real provider stream to pass runtime validation and then require Media3/ExoPlayer to reach `onRenderedFirstFrame()`. The CI emulator workflow was also corrected after the first run failed before tests because the runner image no longer exposed the requested `Pixel_2` device profile.
