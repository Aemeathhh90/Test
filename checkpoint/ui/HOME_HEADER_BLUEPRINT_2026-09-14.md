# Home Header Blueprint — 2026-09-14

## Status
- 🟢 Reference approved by Shin
- 🟢 Foundation-first approach retained; final UI polish remains later
- 🟡 Implementation follows after Core V1 audit/foundation work

## Approved Home direction
- Half-height anime artwork background in the profile header; artwork does not fill the whole screen.
- User identity: avatar, nickname, bio, and Premium User / Free User status.
- Remove WDRG, LVL, Key and similar gamification fields from the header.
- Quick actions: Diamond, large Watch Together, Message, Notification.
- Message and Notification remain near Watch Together only; remove duplicate Message/Notification icons from the top system/battery area.
- Settings is not shown on Home; Settings remains accessible from the Account Dashboard/Profile.
- One Home search bar remains below the dashboard, with a Filter button beside it.
- Do not add a separate Featured section.

## Approved Home section order
1. Header / profile dashboard
2. Diamond + Watch Together + Message + Notification
3. Search + Filter
4. Continue Watching
5. New Updates
6. Anime Musiman (current / previous seasons)
7. Anime Tamat
8. Trending Now
9. Bottom navigation

## Anime card status badge
Use a small status badge consistently on poster cards where provider/backend data supports it:
- ONGOING
- TAMAT
- HIATUS
- UPCOMING (when available)

Status must come from normalized provider/backend data; do not guess status from episode count.

## Architecture note
This blueprint is a UI target, not permission to prematurely rewrite the Home implementation. Core state, navigation, persistence, provider integration, monetization, history, and favorite foundations remain higher priority. Final UI polish happens after the foundation and Core V1 audit are stable.
