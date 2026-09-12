# KakaAnime Roadmap

## Status legend
- 🟢 Done / verified
- 🟡 In progress / implemented but not fully verified
- 🔴 Blocked / failed
- ⚪ Planned

## V0.1 — Foundation
- 🟢 Android project foundation
- 🟢 Provider architecture and registry
- 🟢 Stream normalization, validation, and selection
- 🟢 Provider E2E test harness
- 🟡 Provider batch E2E verification

## V1 — Core KakaAnime experience

### Home & Library
- 🟡 Home UI — Saikou-inspired direction
- ⚪ Anime search and discovery polish
- ⚪ Favorite — single Favorite list
- ⚪ Watching / episode progress integration
- ⚪ New Updates for anime receiving new episodes
- ⚪ Unwatched episode lock indicator

### Video Player
- 🟢 VideoPlayer → PlayerController → PlayerCore single-player foundation
- 🟡 ReDantotsu-level controller UI
- ⚪ Small progress bar + small round thumb
- ⚪ 10-second forward/back controls
- ⚪ Previous/next episode controls
- ⚪ Auto-next episode control
- ⚪ Premium skip intro/outro
- ⚪ Auto skip intro/outro implementation

### Diamonds, Ads & Premium
- ⚪ Free user earns 2 diamonds per successfully completed ad
- ⚪ 1 diamond unlocks 1 video/episode
- ⚪ If diamonds are 0, require an ad before playback
- ⚪ No unnecessary pop-ups for normal free-user flow
- ⚪ 1080p requires Premium
- ⚪ Premium removes ad/diamond restrictions where applicable
- ⚪ **Ad-unavailable fallback popup**
  - If a rewarded ad cannot be loaded/shown, display a waiting popup instead of leaving the user stuck.
  - Show a clear countdown (example: ~94 seconds, configurable).
  - Explain that the ad is unavailable and the user can wait for the fallback reward.
  - On successful countdown completion, grant the configured diamond/key reward once.
  - Prevent duplicate reward claims from repeated popup closes/reopens.
  - Provide a Premium upgrade action as an optional no-wait path.
  - Record the fallback result separately from a successfully watched ad.

### UI Customization
- ⚪ Custom accent/theme color picker without requiring hex codes
- ⚪ Dark/light appearance support

## V2 — Expansion
- ⚪ Comments
- ⚪ More advanced social/community features
- ⚪ Additional player enhancements
- ⚪ Further provider expansion and maintenance

## Provider verification gate
A provider is only marked 🟢 after real E2E playback proves:

`Search → Detail → Episode → Resolve Stream → .m3u8/.mpd/.mp4 → Media3 onRenderedFirstFrame()`

Implementation alone is not enough to mark a provider green.

Current provider batch target: **29 registered providers**.
