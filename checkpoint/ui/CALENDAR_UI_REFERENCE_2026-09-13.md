# Calendar UI Reference / Implementation Checkpoint

**Tanggal:** 13 September 2026  
**Source of truth:** `main`  
**Reference utama:** screenshot Calendar/Schedule yang diberikan Shin

## 🟢 Audit findings

- Screenshot user menjadi reference utama untuk layout Calendar/Schedule.
- ReDantotsu v1.0.7 confirms the useful behavior pattern of countdown badges on Home/Calendar and a green/active indicator for episodes airing today. Official release notes also changed Calendar wording from `Episode X` to `Ep X`. See official release notes: https://github.com/AsrOfficialDev/ReDantotsu/releases/tag/v1.0.7
- AniSync is a useful native Android/Compose reference for a seven-day airing calendar and selected-day schedule behavior; its source includes a Weekly Calendar implementation.
- KakaAnime does not need a calendar dependency for this target. The screenshot can be implemented with Compose `LazyRow` + `LazyColumn` + a custom timeline rail.

## 🟢 Data audit

`AniListCalendarService` previously returned only title, episode, airing time, cover, and AniList URL. The target card also needs format, score, duration, and genres.

The service now queries:
- `format`
- `averageScore`
- `duration`
- `genres`
- cover image
- title
- airing time / episode

The query now paginates through AniList `Page` results up to five pages and starts from the beginning of the local day so today's already-aired episodes can appear with an `Aired` state.

## 🟢 UI implementation

`CalendarScreen.kt` now follows the reference structure:

- Header: `Schedule` + `Track upcoming anime episodes`
- Search and overflow icon positions
- Monday → Sunday seven-day selector
- Today marker / selected-day highlight
- Selected-day heading + episode count
- Vertical time column
- Timeline dot + connecting line
- Poster/title/episode metadata card
- Format + score + duration metadata
- Genres row
- `Aired` / `Airing Soon` badge
- Live countdown badge for upcoming episodes
- Favorite indicator
- Per-card overflow icon
- Empty and loading states

## 🟡 Not yet verified

- Android compile/runtime verification.
- Visual comparison on a real device against the supplied screenshot.
- Search/overflow actions are visual placeholders; behavior is not part of this checkpoint.
- Anime click matching still uses title matching; AniList-ID mapping can be improved later.

## 🔴 Not claimed

- No claim that Calendar is production-green until Android build/runtime validation is completed.
- No provider dependency was introduced.

## Commits

1. `8fbf1a209aa3bed89ed4e7517c7557639ce2f573` — `calendar: enrich airing schedule metadata`
2. `8c9afab9e896fa26616d7554828c237261fff26b` — `calendar: redesign schedule timeline UI`

## Next step

Run Android build/runtime verification. If the screen compiles, compare the actual device UI with the supplied reference and make only targeted visual corrections.
