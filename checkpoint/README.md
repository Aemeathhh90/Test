# KakaAnime Checkpoint System

This folder is the source for project continuity and technical history.

## Structure

- `MASTER_CHECKPOINT.md` — compact project dashboard and locked rules.
- `PROVIDER_MAPPING.md` — global map of the 29 provider targets and their families.
- `providers/` — one technical checkpoint per provider. Create/update the provider file when that provider is actively audited or changed.
- `extractors/` — extractor and stream-resolution architecture history.
- `backend/` — backend architecture and audit notes.
- `player/` — Media3/video-player decisions and validation.
- `ui/` — UI architecture and decisions.
- `builds/` — historical CI/E2E build checkpoints.

## Rules

1. `main` is the source of truth.
2. Do not overwrite historical checkpoints.
3. Every meaningful code/config/architecture change gets recorded.
4. Provider status is not green until Media3 reaches `onRenderedFirstFrame()`.
5. Provider-specific mapping belongs in `providers/<PROVIDER>_CHECKPOINT.md`.
6. Do not invent provider details before the provider is actually audited.

## Migration note

Older checkpoint files that still exist at repository root are historical documents being migrated into this structure. Their Git history remains intact; migration does not rewrite historical commits.
