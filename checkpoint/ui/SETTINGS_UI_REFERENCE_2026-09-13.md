# KakaAnime Settings UI Reference — 2026-09-13

## Status
- 🟢 Settings main screen redesigned and routed from Account Dashboard.
- 🟢 Playback section locked to approved feature set.
- 🟢 Downloads section locked to approved feature set.
- 🟢 Data & Cache includes Clear Cache, Image Cache, Data Usage, Analytics, and Crash Reports.
- 🟢 Account & Sync reduced to Backup Data and Restore Data.
- 🟢 Appearance, Notifications, About KakaAnime, AniList, subtitle controls, and Privacy Policy removed from Settings.
- 🟡 Backup/Restore storage implementation is UI-prepared and remains for the dedicated data-management implementation pass.
- 🟡 Download manager integration remains dependent on the download feature implementation.
- 🟡 Android Build workflow is running for the Settings commits; do not claim green until the workflow finishes successfully.

## Approved Structure

### Playback
- Video Player
- Default Quality
- Auto Next Episode
- Auto Skip Intro — Premium
- Auto Skip Outro — Premium

### Downloads
- Download Quality
- Download Location
- Download over Wi-Fi only
- Max Concurrent Downloads
- Download Notifications
- Storage
- Manage Downloads
- Clear All Downloads

### Data & Cache
- Clear Cache
- Image Cache
- Data Usage
- Analytics
- Crash Reports

### Account & Sync
- Backup Data
- Restore Data

## UI Direction
- Main visual direction follows the approved Settings reference: dark/glass cards with a clear icon for every section and row.
- Keep Settings operational; Appearance, Notifications, About, and Privacy Policy belong to their own Account destinations.
- No subtitle controls because KakaAnime does not expose a subtitle feature.
- 1080p remains Premium-gated.

## Commits
- `de22cb1e07ad390b0eb2d04ad92594ad097c694a` — `settings: add redesigned settings screen`
- `7bbead951e278fa5f10bc13b04b5584d89087e14` — `account: open redesigned settings screen`

## Next
1. Verify Android Build workflow result.
2. If green, keep Settings UI locked.
3. Continue with Appearance only after its reference is approved.
