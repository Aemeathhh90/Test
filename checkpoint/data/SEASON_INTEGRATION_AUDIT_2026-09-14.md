# KakaAnime — Season Integration Audit

Date: 2026-09-14

## Decision

Catalog/listing may expose each season as a separate anime entry (for example, `Solo Leveling S1` and `Solo Leveling S2`). Opening either entry must retain the shared anime-group identity and show a season selector in Detail, with the selected season opened by default.

Internal identity remains:

`Anime Group -> Season -> Episode -> Player`

## Audit findings

### AnimeData

A recent status commit accidentally removed existing catalog metadata fields from `AnimeData.kt`. The fields were recovered from the parent commit and preserved while adding `HIATUS` to `AnimeStatus`.

Current season-aware fields:
- `animeGroupId`
- `seasonNumber`
- `seasonTitle`
- `searchAliases`

Existing catalog metadata such as poster, description, genres, rating, episode counts, and update timestamp is preserved.

### ProviderModels

`ProviderAnime` already contains season/group/alias fields. `ProviderEpisode` also contains season/group and availability fields. This is a foundation, not proof that the live provider currently populates those fields.

### AnimeMapper

Previously dropped provider season fields and mapped `season` to null. The mapper now carries:
- group id
- season number
- season title
- search aliases
- season title into legacy `season`
- provider episode thumbnail
- provider episode availability
- HIATUS status

### Provider source

`OtakudesuProvider` currently constructs provider results without reliably populating season metadata. Therefore end-to-end automatic season discovery is **not complete** yet. We must not claim the provider can already distinguish all real-world seasons until its source/API mapping supplies those identities.

### Anime Detail

Current Detail still accepts the legacy `Anime` model and a single episode list. It does not yet render a real season selector. This is the next UI/data bridge and must be implemented without breaking the current KakaAnime visual identity.

## Status

Season data foundation: 🟢 audited and mapper bridge added.
Season catalog splitting: 🟡 pending provider/catalog identity bridge.
Season selector in Detail: 🟡 pending UI foundation implementation.
History/Favorite season identity: 🟡 pending audit/integration.
Player season identity: 🟡 pending bridge after Detail identity is stable.
Build/runtime: 🟡 not verified by CI in this checkpoint.

## Next step

Implement the season-aware catalog/detail bridge in small, testable changes: first establish a reusable season identity/group model and catalog projection, then add the Detail season selector so an entry opened as S2 remains on S2 while still exposing S1/S2 choices. Do not add complex gestures/animations until the final global Interaction Pass.
