# KakaAnime UI/UX Checkpoint — 2026-09-15

## Current visual direction
- Dantotsu = primary visual reference for Home, Calendar, Social, Library, Profile, Anime Detail, Episode, and global bottom navigation.
- Saikou = Settings reference only.
- No feature expansion outside the agreed blueprint; current work is UI polish plus existing red-flag logic fixes.

## Home
- Home follows the Dantotsu/Redantotsu-inspired visual language agreed in chat.
- Remove Lv and WDRG from Home.
- Top header: Message + Notification. Search and Settings icons removed from the top header; the dedicated search field remains below.
- **IMPORTANT:** Do NOT change the text under Home posters. User explicitly likes the existing poster text. Only polish poster visuals (size/proportion, spacing, radius, overlay, badge/image treatment).

## Profile — current next target
Profile Home should follow the provided reference while keeping KakaAnime's identity:
- Nickname on top.
- **Username directly below Nickname.**
- **Bio directly below Username.**
- No Lv and no WDRG.
- Keep the agreed internal Profile Home buttons/sections.
- Include Search + Filter.
- Include Lanjut Nonton in the agreed reference style.
- Do not redesign the overall structure unnecessarily; focus on Dantotsu visual polish.

## Anime Detail / Episode
- Dantotsu is the reference, not Redantotsu.
- Header/image area stays persistent when switching INFO ↔ WATCH/EPISODES.
- Detail bottom navigation is separate from the global 5-tab navigation.
- Source selector becomes Season 1/2/3/4 in the UI; provider selection stays internal.
- Episode UI: compact horizontal list with small thumbnail, episode name/title, date, play/lock overlay, duration, and progress bar when it looks good. Grid remains an alternate layout using the same visual language.
- Not-yet-released anime must have a clear "Belum Rilis" state; do not confuse it with provider failure.

## Global navigation
- Floating/pill-like Dantotsu-style bottom navigation: Home, Calendar, Social, Library, Profile.
- It stays at the very bottom and must not be pushed upward.
- Anime Detail has its own separate compact INFO/WATCH navigation.

## Settings
- Saikou visual language only.
- Main Settings page directly exposes Light / Dark / Auto theme control.
- Simple spacious rows/categories; adapt categories to KakaAnime features.

## Red logic still to fix later
- Global Android Back architecture.
- Episode Gate: Free -> watch ad -> +2 diamonds -> consume 1 diamond -> play; Premium direct. Remove obsolete 90-second wait/countdown logic.
- Provider E2E: Otakudesu and Samehadaku first failing resolver stage still needs CCTV evidence before any provider patch.
- Episode loading performance: diagnose first, then optimize with timeout/cache/background refresh as appropriate.

## Workflow rule
CCTV/diagnostics -> identify first failing point -> targeted fix -> commit/checkpoint -> Android Build -> E2E. No blind patching.
