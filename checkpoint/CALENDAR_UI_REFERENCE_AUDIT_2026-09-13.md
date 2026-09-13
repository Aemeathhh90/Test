# Calendar UI — Reference Audit & Implementation

**Tanggal:** 13 September 2026  
**Repo:** `KakaAnime/KakaAnime`  
**Branch:** `main`

## Reference audit

- ReDantotsu was checked before changing the Calendar UI.
- ReDantotsu v1.0.7 release notes explicitly mention Calendar countdown badges for the next airing episode and a green dot indicator for episodes airing today.
- ReDantotsu also uses a modern Liquid Glass visual language and pill-shaped navigation/surfaces.
- KakaAnime remains native Compose; ReDantotsu is used as a UX/reference pattern, not as a dependency.
- Additional schedule references were reviewed, including modern AniList clients with weekly airing calendar patterns.

## Implementation

Updated `app/src/main/java/com/kakaanime/app/CalendarScreen.kt`.

- 🟢 Calendar remains a dedicated bottom-navigation destination.
- 🟢 7-day horizontal date selector.
- 🟢 Today indicator.
- 🟢 Airing schedule sourced from AniList.
- 🟢 Poster + episode + local airing time.
- 🟢 Countdown badge (`d/h/m`, `<1m`, `NOW`).
- 🟢 Today airing indicator uses a dedicated dot.
- 🟢 Episode label uses `Ep X` rather than the longer `Episode X` form.
- 🟢 Rounded translucent/glass-like surfaces adapted to KakaAnime's Material theme.
- 🟢 Favorite indicator retained.
- 🟢 Loading and empty states retained.

## Commit

`ec94d1a333a651abe4d269bc3122d33cfc14c7b4` — `calendar: align schedule UI with reference patterns`

## Validation

- 🟢 File re-fetched from `main` after commit; new Calendar implementation is present.
- 🟡 Android build/runtime verification remains pending.
- 🟡 Visual verification on the user's device remains pending.

## Important

This checkpoint records a reference-informed implementation, not a claim of pixel-perfect parity with ReDantotsu. Final visual parity requires device screenshot/runtime verification.
