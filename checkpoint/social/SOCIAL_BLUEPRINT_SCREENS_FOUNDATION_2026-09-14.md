# KakaAnime — Social Blueprint Screens Foundation

Date: 2026-09-14
Branch: `main`

## Design rule

The supplied Social Updated Blueprint is the structure reference. KakaAnime's existing visual language is the UI/UX reference. The blueprint is not copied pixel-for-pixel; the implementation should be adapted where a cleaner KakaAnime experience is better.

## Screens added in this pass

🟢 Notifications V1 foundation — `NotificationsScreen.kt`
- Notification list
- Friend / Watch Together / Message / System categories represented locally
- KakaAnime rounded-card visual language

🟢 Search Users V1 foundation — `SearchUsersScreen.kt`
- Search field for username/ID
- User results
- Add Friend action placeholder
- Profile destination callback

🟢 Other User Profile V1 foundation — `OtherUserProfileScreen.kt`
- Avatar/header
- Username
- Friends / Followers / Following stats
- Add Friend / Message actions
- Currently Watching
- Recent Activity

🟢 Messages + Chat Detail foundation already exists — `MessagesScreen.kt`

## Current Social V1 foundation map

1. Social Home — existing
2. Watch Together — existing
3. Room Lobby — existing foundation inside Watch Together flow
4. Watch Room — existing
5. Friends — existing
6. Messages — existing
7. Chat Detail — existing
8. Notifications — added
9. Search Users — added
10. Other User Profile — added

## Remaining work

🟡 Wire Social Home destinations into these screens.
🟡 Refine any visual mismatches against the KakaAnime UI language while preserving the blueprint structure.
🟡 Build/runtime verification.

Interaction/gesture/animation polish remains deferred until all feature foundations are complete.
