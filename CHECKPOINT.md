# KakaAnime — Project Checkpoint

## Social / Profile UI — LOCKED

### Navigation
- My Profile is accessible from the user's own avatar/profile photo.
- Home keeps the user's own avatar in the top-right as a direct My Profile entry point.
- Do not add separate My Profile buttons throughout Social.

### Clickable User Identity
- Any user avatar/profile photo or username shown in social-related UI can be tapped.
- Tapping the current user's own avatar/username opens **My Profile**.
- Tapping another user's avatar/username opens **Other User Profile**.

### Applies to
- Global Chat
- Anime Chat
- Active Friends
- Watch Together participant lists
- Other social surfaces where a user identity is displayed

### Profile distinction
- My Profile: editable account/profile view plus personal activity/favorites/stats as defined later.
- Other User Profile: public profile focused on identity and social/activity information; privacy controls determine what is visible.

## Social Layout — LOCKED
- Global + Anime Chat are combined in one compact secondary card.
- Watch Together is the larger, visually stronger featured card.
- Active Friends remains a smaller section below.
- Do not make the Global/Anime card visually equal to Watch Together.

## Implementation status
- Design checkpoint only; UI implementation is intentionally deferred until provider E2E/playback is stable.
