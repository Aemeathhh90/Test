# KakaAnime — Season-Aware State Repository Foundation

Date: 2026-09-14

## Status

- 🟢 `KakaAnimePreferences` already exposes additive season-aware storage.
- 🟢 `AnimeStateIdentity` defines canonical Group + Season + Episode identity.
- 🟢 `SeasonAwareStateRepository` now provides a small UI-facing facade over that storage.
- 🟢 Legacy title-based storage remains untouched for compatibility.
- 🟡 MainActivity wiring is still pending.
- 🟡 Android build/runtime verification is pending; GitHub combined status returned no CI statuses for the repository commit.

## Canonical state model

- Favorite: Anime Group level.
- Watched progress: Anime Group + Season.
- Unlocked episode: Anime Group + Season + Episode.
- Watch history: Anime Group + Season + Episode.

## Why this layer exists

The facade keeps UI code from depending directly on SharedPreferences keys and allows the migration/wiring step to be implemented without creating another storage format.

## Next

1. Wire MainActivity to create an `AnimeStateIdentity` for the selected anime.
2. Use season-aware favorite/unlocked/watched state with legacy fallback.
3. Feed the season-aware state into Library/History.
4. Run build/CI verification before marking the state migration complete.
