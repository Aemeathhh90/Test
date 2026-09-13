# KakaAnime — Account Dashboard UI Audit

**Tanggal:** 13 September 2026  
**Branch:** `main`  
**Scope:** Account Dashboard + Edit Profile only

## Reference

Primary layout reference: screenshot supplied by Shin. Visual language/interaction should remain KakaAnime-specific with ReDantotsu-inspired UX.

## Current progress

### 🟢 Completed in this batch
- Account header now includes a compact Settings shortcut.
- Dashboard now has the large Banner Atas structural area above the profile identity card.
- Dashboard statistics now use the target hierarchy: Anime Watched / Episode Watched / Favorites.
- Diamonds remain in the profile/monetization row instead of competing with the three primary statistics.
- Watch history persistence is now used for Episode Watched counting, with fallback to the legacy last-watched map for anime count compatibility.
- Episode opening records a watch-history entry so the Episode Watched statistic has a real persisted source.
- Account menu remains locked to: Settings → Profile → Appearance → Premium → Notifications → About KakaAnime.

### 🟡 Still needs correction / refinement
1. Banner Atas is currently a structural visual placeholder; Premium custom upload/media is not implemented yet.
2. Profile avatar still renders initials + preset color. Free static photo picker and Premium animated profile are not implemented yet.
3. Premium banner still has no selectable custom media. Its Upgrade to Premium / Premium Aktif function remains fixed.
4. Episode Watched history currently records an episode when the app opens it; a later playback-completion threshold can refine the definition of “watched” if desired.
5. Settings/Notifications are placeholder dialogs; they are not final screens.
6. Appearance is still a compact dialog and needs a dedicated expandable experience later.
7. About KakaAnime is still a lightweight dialog and needs a final dedicated UI later.
8. Edit Profile still needs the obsolete Premium Banner block removed/reworked and the real media-picker architecture implemented.

## Target Account Dashboard structure

1. Header: Account + subtitle + settings shortcut.
2. Customizable Banner Atas.
3. Profile card: avatar, username, bio, Free/Premium status, diamonds, edit.
4. Premium banner: custom background/media + Upgrade to Premium/Premium Aktif state.
5. Stats: Anime Watched / Episode Watched / Favorites; Diamonds shown without breaking hierarchy.
6. Menu: Settings / Profile / Appearance / Premium / Notifications / About KakaAnime.

## Implementation order — LOCKED

1. Account Dashboard visual structure + statistics foundation — 🟢 batch progress, final visual polish pending.
2. Watched data model/history — 🟢 persistence foundation exists; completion semantics can be refined during verification.
3. Edit Profile static avatar + Banner Atas + Banner Premium + Premium gating — 🟡 next major batch.
4. Settings — 🔴 not started as final screen.
5. Appearance — 🟡 basic persistence exists; dedicated final UI not started.
6. Notifications — 🔴 not started as final screen.
7. About KakaAnime — 🟡 basic dialog exists; final UI not started.
8. Final Account audit + Android build/runtime verification — 🔴 pending.

## Technical notes

- `KakaAnimePreferences` persists `WatchHistoryEntry` records separately from the legacy `title -> last watched episode` map.
- `WatchHistoryEntry` contains title, episode, optional episode title, watched timestamp, and duration.
- The current Dashboard uses `watchHistory.size` for Episode Watched and unique watched titles for Anime Watched.
- Do not fake Episode Watched from the latest episode number or number of available provider episodes.
- The current `MainActivity` records watch history when an episode is opened; this keeps the lock/history contract aligned with the existing diamond consumption flow.

## Audit conclusion

Account Dashboard is **🟡 in progress**, not final. The structure and statistics foundation are now in place, but custom media, Edit Profile, and final Account sub-screens remain. Continue with **Edit Profile + media customization** only after this checkpoint, while preserving rollback traceability on `main`.
