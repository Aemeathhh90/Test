# KakaAnime — Home Premium & Diamond Header V1

**Status:** LOCKED
**Date:** 2026-09-14
**Branch:** `main`

## Decision

Premium and Diamond are integrated into the Home header/profile area. Premium is **not** a separate Home section or bottom banner.

## Header Layout

- Profile avatar
- Nickname
- Small **Premium** badge beside the nickname
- Short profile tagline
- Premium duration/status text below the nickname
- Diamond balance card below the profile area
- Watch Together and message/notification shortcuts remain in the header action area

### Premium presentation

Example:

```text
Shin Tempest   👑 Premium
Watch more anime, a happier you.
Premium aktif • 27 hari lagi
```

Rules:
- `Premium` badge communicates the user's Premium status/identity.
- Premium duration is shown separately as status text.
- The badge itself is not used to display the remaining duration.
- When Premium expires, the status must reflect that the user is no longer Premium.
- No intrusive Premium popup is required for Free users.

## Diamond presentation

Example:

```text
💎 7.848 Diamond   >
```

Rules:
- Diamond is the Free-user viewing currency.
- Diamond balance is visible in the Home header.
- Tapping the Diamond card opens the Diamond / Top Up UI.
- Diamond UI can contain current balance, Get Diamond / Watch Ad, Top Up Diamond, and later transaction history.
- Diamond is not a separate Home content section.

## Premium features

- 1080p
- Auto Skip Intro
- Auto Skip Outro

## Home content order

1. Header / Profile
2. Search Anime
3. Filter
4. Lanjut Nonton
5. New Updates
6. Anime Musiman / Seasonal Anime
7. Trending Now
8. Anime Completed
9. Recommended

Premium and Diamond do not occupy separate numbered Home sections.

## Visual reference

The provided KakaAnime Home screenshot is the visual reference for the header composition: profile/avatar, Premium badge, Diamond card, Watch Together, Pesan, and Notifikasi. The latest locked bottom navigation blueprint remains authoritative over the screenshot's older navigation labels.
