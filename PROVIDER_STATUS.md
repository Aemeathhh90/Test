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
| 1 | Otakudesu | 🟡 | Pending CI result |
| 2 | Samehadaku | 🟡 | Dedicated E2E + all-provider gate pending |
| 3 | AllAnime | 🟡 | Dedicated E2E + all-provider gate pending |
| 4 | Kuronime | 🟡 | Dedicated E2E + all-provider gate pending |
| 5 | Animasu | 🟡 | All-provider gate pending |
| 6 | AnimeIndo | 🟡 | All-provider gate pending |
| 7 | Zoronime | 🟡 | All-provider gate pending |
| 8 | Anoboy | 🟡 | All-provider gate pending |
| 9 | AnimeKompi | 🟡 | All-provider gate pending |
| 10 | Doronime | 🟡 | All-provider gate pending |
| 11 | Hunter no Sekai | 🟡 | All-provider gate pending |
| 12 | Gomunime | 🟡 | All-provider gate pending |
| 13 | NeoNime | 🟡 | All-provider gate pending |
| 14 | YLNime | 🟡 | All-provider gate pending |
| 15 | NontonAnimeID | 🟡 | All-provider gate pending |
| 16 | Animeisme | 🟡 | All-provider gate pending |
| 17 | Animeku | 🟡 | All-provider gate pending |
| 18 | Oploverz | 🟡 | All-provider gate pending |
| 19 | Kuramanime | 🟡 | All-provider gate pending |
| 20 | Wibudesu | 🟡 | All-provider gate pending |
| 21 | Meownime | 🟡 | All-provider gate pending |
| 22 | Anibatch | 🟡 | All-provider gate pending |
| 23 | Nimegami | 🟡 | All-provider gate pending |
| 24 | Drivenime | 🟡 | All-provider gate pending |
| 25 | Anitoki | 🟡 | All-provider gate pending |
| 26 | RiiE | 🟡 | All-provider gate pending |
| 27 | Kusonime | 🟡 | All-provider gate pending |
| 28 | Animekuindo | 🟡 | All-provider gate pending |
| 29 | AnimeSail | 🟡 | All-provider gate pending |

## Current count

- 🟢 E2E proven: **0**
- 🟡 Implemented but not E2E proven: **29**
- 🔴 Confirmed dead with current attributable evidence: **0**
- Runtime registry total: **29**

The previous batch screenshot reported 10 “dedicated/strong” providers and 1 “confirmed dead”. Those labels were implementation/batch labels, not the strict playback status above. The provider ID for the historical dead entry is not preserved in the current status data, so it is intentionally not guessed here.

## E2E gate

Dedicated tests currently cover Kuronime, AllAnime, and Samehadaku. In addition, `AllProvidersPlaybackE2eTest.kt` now derives its provider list directly from `ProviderFactory.createRegistry().all()` and runs every registered provider through the complete strict flow. A provider can only be promoted to 🟢 after that run actually reaches Media3 `onRenderedFirstFrame()` with no playback error.

The CI workflow now includes an `all-providers-playback` emulator job using API 35 / Pixel 6. This is deliberately separate from the three dedicated provider jobs so failures are attributable while the complete registry is still gated together.
