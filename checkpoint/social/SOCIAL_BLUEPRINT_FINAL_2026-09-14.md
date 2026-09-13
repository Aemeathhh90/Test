# KakaAnime Social Blueprint — FINAL — 2026-09-14

## Navigation
Bottom Navigation is fixed at five items:
1. Home
2. Calendar
3. Social
4. Library
5. Profile

Social is a dedicated bottom-navigation destination. Watch Together is NOT a separate bottom-nav item.

## Social Home order
1. Watch Together — primary action / flagship
2. Active Friends — presence such as Online, Watching, In Room, Away, Offline
3. Recent Messages — latest DM previews; See All opens full Messages
4. Friends — entry to friend management
5. Notifications
6. Global Chat — V2

The separate Messages shortcut/button on Social Home is removed because Recent Messages already provides the entry point to DM. A Message shortcut on Home may remain as a cross-navigation shortcut, but it is not a second Social menu item.

## Friends
- My Friends
- Friend Requests
- Search Users is inside Friends, not a separate Social Home button
- View Profile
- Add Friend
- Block / Report

## Watch Together
- Create Room
- Join Room
- Active Rooms
- My Rooms
- Invite Friends
- Room Members
- Host role
- Synchronized playback
- Room Chat (text-only)

Watch Together can be launched from Social and via a prominent shortcut on Home. The shortcut does not create another navigation destination.

## Social foundation
- Friend Activity: show relevant activity such as watching an anime, finishing an episode, or creating a Watch Together room.
- Presence states: Online, Watching, Watching Together/In Room, Away, Offline.
- Privacy controls for activity/presence visibility.
- Text-only social interactions.
- Moderation/security foundation: Block, Report, rate limiting/anti-spam, message moderation capability.

## V2
- Global Chat
- Anime / Episode Chat
- Spoiler tags
- Mute
- Additional social features only if justified later

## Explicitly excluded
- Social Feed / Timeline
- Like / Repost system
- User photo/video uploads
- GIF/sticker uploads
- Voice chat
- File sharing

## UI reference direction
Primary visual reference is the first KakaAnime Social concept board discussed on 2026-09-14: clean dark anime UI, rounded cards, Watch Together as the dominant action, Active Friends, Recent Messages, and compact Social actions. This is a design reference, not a pixel-perfect copy.

## Implementation rule
This checkpoint locks the Social information architecture/feature boundaries. Do not begin backend/UI implementation until Core V1 foundation/audit is ready. Future implementation should build on this structure rather than introducing duplicate navigation destinations.
