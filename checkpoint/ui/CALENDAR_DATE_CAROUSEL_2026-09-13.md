# KakaAnime Calendar Date Carousel — 2026-09-13

## Status
- 🟢 Calendar remains AniList-first and uses `AiringSchedule` data.
- 🟢 Top date selector is now horizontally swipeable instead of being limited to the current Monday–Sunday week.
- 🟢 Date window covers 7 previous days through 30 future days around today.
- 🟢 Selecting a date filters the timeline to that exact local calendar date.
- 🟢 Selected date is brought into view automatically.
- 🟢 `Today` action appears when viewing another date and returns to the current day.
- 🟢 Calendar keeps the existing timeline cards, posters, metadata, genres, status, countdown, and Detail navigation.
- 🟢 AniList schedule request now covers the same 37-day window (7 past + 30 future).
- 🟡 Android build/runtime verification is still pending.
- 🔴 MAL/Jikan fallback is architectural only; not implemented yet because AniList remains sufficient for the current Calendar path.

## Commit
- `4e163593ddfba35d912055d8b94dcf726e8bcc72` — `calendar: make date selector swipeable`

## Technical notes
- No calendar dependency was added; native Compose `LazyRow`/`LazyListState` is used.
- The selected date remains `LocalDate`, while airing timestamps are converted using the device's system timezone before filtering.
- The Calendar UI remains aligned to the previously approved reference: Schedule header, horizontal date selector, date/episode count, vertical timeline, status/countdown badges, and anime cards.

## Next step
- Verify the new date carousel and Calendar → Detail path on Android.
- Then continue the next core UI audit/build verification before provider expansion.
