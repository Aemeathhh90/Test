# KakaAnime — Account Dashboard UI Audit

**Tanggal:** 13 September 2026  
**Branch:** `main`  
**Scope:** Account Dashboard + watch-history visual data foundation

## Current progress

### 🟢 Completed
- Account header includes compact Settings shortcut.
- Dashboard has Banner Atas structural area above profile identity card.
- Dashboard statistics use Anime Watched / Episode Watched / Favorites.
- Diamonds remain in the profile/monetization row.
- Watch history persistence is used for Episode Watched counting.
- Episode opening records a persisted watch-history entry.
- Account menu remains locked to Settings → Profile → Appearance → Premium → Notifications → About KakaAnime.
- Watch-history model now supports an episode-specific thumbnail URL.
- Continue Watching now reads the latest watch-history entry per anime and renders its episode thumbnail when available.
- Episode Watched now renders the thumbnail stored on that exact episode record instead of resolving an anime poster for every episode.
- API and HTML-first Otakudesu episode discovery now carry episode thumbnail data when present in the source HTML/API.
- History updates preserve an existing episode thumbnail when a later update does not provide one.

### 🟡 Still needs correction / refinement
1. Existing history entries created before thumbnail persistence naturally have no episode thumbnail.
2. Banner Atas custom upload/media is not implemented yet.
3. Profile static photo picker and Premium animated profile are not implemented yet.
4. Premium banner custom media is not implemented yet.
5. Episode Watched currently records on episode open; completion threshold can refine the meaning later.
6. Settings/Notifications are placeholder dialogs.
7. Appearance is still a compact dialog.
8. About KakaAnime is still a lightweight dialog.
9. Edit Profile still needs the obsolete Premium Banner block removed/reworked and real media-picker architecture.
10. Android build/runtime verification is still pending; no claim of green build is made from connector-only edits.

## Watch-history thumbnail contract

- `WatchHistoryEntry` stores `episodeThumbnailUrl` separately from the anime poster.
- Continue Watching uses the thumbnail belonging to the exact latest watched episode.
- Episode Watched uses the thumbnail belonging to each exact episode record.
- No anime poster is intentionally substituted for an episode thumbnail in these two episode-focused surfaces.
- Provider data can supply the thumbnail; if unavailable, the UI shows a neutral fallback rather than incorrectly presenting the anime poster as the episode image.

## Important commits

- `b51a128f2ec14d3162b6686752a147013d400090` — add `thumbnailUrl` to `ProviderEpisode`.
- `e4e8e4d122ec28e7682f51c86636e39455a9988d` — persist `episodeThumbnailUrl` in watch history.
- `bd5348ce0ef72fe0dc600d4e7d43a9663b132dcf` — restore Otakudesu provider while adding API episode-thumbnail mapping.
- `c129ed06355ef9dbd7ae0f29d9ce980abf2e6c1b` — render exact episode thumbnails in Episode Watched.
- `3b53b751e02af187a535a8eff187b500abd7f15b` — use exact episode thumbnail history entries for Continue Watching.
- `dc6d5569fd1112377c354b818de7c3d5565cb7ef` — add HTML episode-thumbnail extraction.
- `28824154d1a55d8ef3a95ad38c398ddfc43d60a9` — pass web episode thumbnails into provider episode models.
- `fd240f75e3ef318982d47824a5cc1ea12551ebd3` — preserve episode thumbnail data in watch-history updates.
- `3bdc3852109de6e03e08d1c76490d4517c22a908` — persist provider episode thumbnail when recording history.

## Next step

Continue with **Edit Profile + media customization** while preserving rollback traceability on `main`.

**Verification note:** GitHub connector edits are committed and fetched back from `main`; Android build/runtime remains 🟡 until verified in Codespaces/device.
