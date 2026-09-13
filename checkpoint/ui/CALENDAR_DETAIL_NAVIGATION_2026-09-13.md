# Calendar → Anime Detail Navigation Checkpoint

**Tanggal:** 13 September 2026
**Repo:** `KakaAnime/KakaAnime`
**Source of truth:** `main`

## 🟢 Change
Calendar schedule cards must open the corresponding KakaAnime Anime Detail screen when tapped. They must not behave as a schedule-only/static screen.

## 🟢 Current navigation
```text
Calendar card
  ↓
onAnimeClick
  ↓
selectedAnime
  ↓
AnimeScreen.DETAIL
  ↓
Episode selection
  ↓
Video Player
```

The existing Calendar → Detail callback was already wired in `MainActivity`; the problem was title matching. AniList can return a season subtitle or alternate capitalization, so an exact title comparison could leave the card without a matching local `Anime` object.

## 🟢 Fix
`AniListCalendarService` now keeps the AniList media ID and canonicalizes the currently supported app catalog entries before the Calendar performs its existing title match:

- AniList media `21` → `One Piece`
- AniList media `176496` → `Solo Leveling`
- unknown media IDs keep their AniList title and continue using the existing fallback behavior.

AniList documents the media ID as the stable media identifier and exposes the associated media ID from an airing schedule. citehttps://docs.anilist.co/reference/object/airingschedule

## Technical commit
- `7a61644fcc6eb30d892d120bd03ef7ff79e0e8e1` — `calendar: normalize schedule titles for detail navigation`

## 🟡 Validation
- Code path verified against current `main` wiring.
- AniList IDs verified against current AniList entries: One Piece `21`; Solo Leveling Season 2 `176496`.
- Android build/runtime verification remains pending.
- Real-device tap test remains pending.

## Next step
Build the Android app and test:
1. Calendar → One Piece → Anime Detail.
2. Calendar → Solo Leveling → Anime Detail.
3. Anime Detail → Episode → Player.
4. Unknown AniList title → remains safe without crashing.
