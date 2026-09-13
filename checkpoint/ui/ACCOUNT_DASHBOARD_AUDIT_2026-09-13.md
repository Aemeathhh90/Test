# KakaAnime — Account Dashboard UI Audit

**Tanggal:** 13 September 2026  
**Branch:** `main`  
**Scope:** Account Dashboard + Edit Profile only

## Reference

Primary layout reference: screenshot supplied by Shin. Visual language/interaction should remain KakaAnime-specific with ReDantotsu-inspired UX.

## Current vs target

### 🟢 Already present
- Account header + subtitle.
- Profile identity card with username/bio/status.
- Edit Profile entry.
- Premium CTA/status banner.
- Appearance entry.
- Profile entry.
- Premium entry.
- Notifications entry.
- Settings entry.
- About KakaAnime entry.
- Existing profile persistence for name, bio, avatar preset, theme and accent.

### 🟡 Needs correction / refinement
1. **Top Account banner** is missing from current dashboard. Target has a large profile/banner header above the profile card.
2. **Settings shortcut in header** is missing. Target reference has a compact settings icon in the Account header.
3. **Profile avatar** currently renders initials + color only. Target architecture now requires static custom image for Free and animated image for Premium.
4. **Premium banner** currently has no custom media. Requirement is custom Premium banner media for Premium users while preserving Upgrade to Premium / Premium Aktif state text.
5. **Top banner customization** is not implemented. Premium users need custom Banner Atas.
6. **Statistics** currently has Anime, Favorite, Diamonds only. Target requires Anime Watched + Episode Watched + Favorites, with Diamonds retained as a separate account value or compact stat if layout permits.
7. **Episode Watched** cannot currently be calculated accurately because watched persistence stores `title -> last watched episode`, not a set/history of watched episode records.
8. **Account menu order** has been corrected to Settings → Profile → Appearance → Premium → Notifications → About KakaAnime.
9. **Settings/Notifications** are currently placeholder dialogs; they are not final screens.
10. **Appearance** is currently a compact dialog. Target architecture calls for a dedicated Appearance experience that can grow without overcrowding Account Dashboard.
11. **About KakaAnime** is currently a lightweight dialog and needs a final dedicated UI later.
12. **Edit Profile** still contains the obsolete `Premium Banner` informational block saying it cannot be changed. This conflicts with the new Premium customization requirement and must be replaced during implementation.
13. **Edit Profile** currently offers only preset avatar colors; custom photo picker and Premium animated profile are not implemented.
14. **Edit Profile** currently uses generated gradient preview rather than actual selectable Banner Atas media.

## Target Account Dashboard structure

1. Header: Account + subtitle + settings shortcut.
2. Customizable Banner Atas.
3. Profile card: avatar, username, bio, Free/Premium status, diamonds, edit.
4. Premium banner: custom background/media + Upgrade to Premium/Premium Aktif state.
5. Stats: Anime Watched / Episode Watched / Favorites; Diamonds shown without breaking hierarchy.
6. Menu: Settings / Profile / Appearance / Premium / Notifications / About KakaAnime.

## Implementation order

1. Finish Account Dashboard visual structure and stats layout.
2. Fix watched data model for accurate Episode Watched.
3. Finish Edit Profile static avatar + Banner Atas + Banner Premium + Premium gating.
4. Then implement Settings.
5. Then Appearance.
6. Then Notifications.
7. Then About KakaAnime.
8. Final Account audit and build/runtime verification later.

## Audit conclusion

Current Account is **not yet 🟢 final**. It has the basic shell, but several reference-critical pieces are missing or outdated. Do not mark Account Dashboard complete until the 🟡 items above are resolved.
