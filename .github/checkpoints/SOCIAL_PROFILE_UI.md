# KakaAnime — Social/Profile UI Checkpoint

Status: DESIGN LOCKED — NOT IMPLEMENTED

## Social hierarchy
- Social Global + Anime remain inside ONE compact secondary card.
- Watch Together is a larger, visually stronger featured card.
- Active Friends remains a smaller section below Watch Together.
- Do not make the Global/Anime card visually equal to Watch Together.

## Profile navigation
- Home has the user's own avatar in the top-right as a direct My Profile entry point.
- No extra My Profile button is required inside Social.
- Any user avatar/photo or username/identity shown in social contexts is clickable.
- Clicking the current user's own avatar/username opens My Profile.
- Clicking another user's avatar/username opens Other User Profile.
- This interaction should be reused across Global Chat, Anime Chat, Active Friends, Watch Together participants, and other social surfaces.
- Profile navigation should rely on existing avatars/user identities rather than adding redundant buttons.

## My Profile
Planned sections: avatar, username, bio, online status, favorites, watching/activity, lightweight stats, and Edit Profile.

## Other User Profile
Planned public profile view focused on identity and social activity, with privacy-controlled visibility for items such as favorites, watching activity, and online status.

## Implementation rule
Do not implement this UI checkpoint until the current provider E2E / playback foundation is stable. Preserve this checkpoint while provider work continues.
