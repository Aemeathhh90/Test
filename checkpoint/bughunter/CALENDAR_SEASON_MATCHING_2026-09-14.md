# Bug Hunter — Calendar Season Matching

Date: 2026-09-14

## Status
🟢 Calendar season matching fix committed.

## Confirmed bug
Calendar previously selected the first catalog anime whose title matched the AniList schedule title. In a multi-season catalog this could select the wrong season, while schedule titles containing season markers could fall back to a season-less synthetic Anime object.

## Fix
Calendar matching now:
- considers `searchAliases` as well as the canonical title;
- compares season-stripped group titles;
- extracts `Season N` / `S N` from schedule titles;
- prefers the catalog candidate whose `seasonNumber` matches the schedule season;
- preserves parsed group/season identity when creating a fallback Anime object.

## Evidence
- Fix commit: `60515642497aa86ebc82dee3e82cb5a31e0ff20e`
- Cleanup commit: `47ff35c040db0ce24016c17a62de0e1222984b5c`
- File: `app/src/main/java/com/kakaanime/app/CalendarScreen.kt`
- Build/runtime: 🟡 pending actual Android build/device evidence.

## Next bug-hunt target
Continue the remaining mapped candidates, especially Player episode navigation fallback and provider episode merge behavior, then perform a second full audit before declaring Bug Hunter complete.
