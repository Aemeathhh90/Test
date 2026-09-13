# KakaAnime Library UI Blueprint V1 — LOCKED — 2026-09-14

## Status

🟢 **LOCKED — Core V1 UI reference**

Library is the single bottom-navigation destination for the user's personal collection and watch history.

## Bottom Navigation

Fixed five destinations:

1. Home
2. Calendar
3. Social
4. Library
5. Profile

Favorite, History, and Continue Watching do **not** become separate bottom-navigation destinations.

## Library structure

```text
Library
├── Favorite
└── History
    ├── Continue Watching
    └── Recently Watched
```

## Visual direction

Final design combines:
- **Modern layout** as the primary direction.
- A **small amount of Premium/hero treatment** for Continue Watching.
- Dark-first KakaAnime visual language.
- Rounded cards.
- Clean spacing.
- Accent color follows KakaAnime customization.
- Practical browsing over excessive decoration.

This is an original KakaAnime direction inspired by the previously audited anime-app references; it is not a pixel-perfect copy.

## Favorite tab

### Purpose
A single list containing anime explicitly marked as Favorite.

### UI

- Library header.
- Search affordance may be present.
- Two main tabs: `Favorite` and `History`.
- Favorite uses a clean poster grid.
- Poster card contains:
  - anime poster
  - status badge when available
  - title
  - latest episode
  - rating
- Tap card → Anime Detail.

### Empty state

```text
♡
Belum ada Favorite
Tambahkan anime ke Favorite agar mudah ditemukan di sini.
[ Jelajahi Anime ]
```

### Explicitly excluded

- Favorite categories.
- Favorite Ongoing / Favorite Completed submenus.
- Plan to Watch.
- Watching category.

## History tab

History is the user's watch continuation area.

### Continue Watching

Displayed first and uses a **mini-hero card**, not a full-screen hero.

Card contains:
- backdrop/episode artwork
- anime title
- latest watched episode
- progress bar
- watched / duration information
- play/continue affordance

Tap → directly resume the corresponding episode in Player.

`Lihat semua` can expose the complete Continue Watching set without creating a new bottom-navigation destination.

### Recently Watched

Displayed below Continue Watching as a compact list.

Each item contains:
- thumbnail
- anime title
- episode number/title
- relative watched time
- progress when relevant
- overflow menu for item management

Tap → directly open that episode in Player.

Order: newest → oldest.

## History management

V1 supports:
- Delete one history item.
- Delete all history with confirmation.

History data is shared with Home Continue Watching and Player watch-state logic; do not create a duplicate Continue Watching data store.

```text
                    Watch History
                         │
                 ┌───────┴───────┐
                 ↓               ↓
               Home           Library
        Continue Watching      History
```

## Episode watch indicator

Episode watch state continues to follow the existing V1 rule:

- Unwatched episode → 🔒 indicator.
- Watched episode → lock indicator disappears / watched state is shown.
- No `Mark as watched` action.
- No `Mark all as watched` action.

## Home integration

Home may show a `Continue Watching` section, but it is sourced from the same watch-history data used by Library.

The Home shortcut does not create another Library/History navigation destination.

## Empty History

```text
◷
Belum ada riwayat tontonan
Mulai menonton anime sekarang dan riwayatmu akan muncul di sini.
[ Jelajahi Anime ]
```

## V1 scope

🟢 Favorite
🟢 History
🟢 Continue Watching
🟢 Recently Watched
🟢 Delete one history item
🟢 Delete all history
🟢 Shared watch-history state with Home
🟢 Shared watch-state with Player
🟢 Local persistence foundation

## Not in V1

🔴 Mark as watched
🔴 Mark all as watched
🔴 Plan to Watch
🔴 Favorite categories
🔴 Upload media
🔴 Voice/file sharing
🟡 Global Chat — V2
🟡 Episode Chat — V2

## Implementation constraints

- Do not add Favorite or History to bottom navigation separately.
- Do not introduce duplicate watch-history storage for Continue Watching.
- Do not reintroduce removed Favorite categories.
- Preserve existing locked player/watch-state architecture unless a concrete bug requires change.
- UI implementation must be verified by Android build/runtime before this UI is considered implementation-green.

## Decision

**This blueprint is the locked visual/information-architecture reference for Library V1.**

Next implementation priority remains **Home V1**, followed by Library implementation/audit as part of Core App stabilization.
