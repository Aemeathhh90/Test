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

## Audit → Reference → Fix → Validate rule

For every meaningful technical bug, use this order:

1. **Audit first** — identify the exact failing stage and root-cause candidates from the current code/logs.
2. **Reference check** — search CloudStream, official Android/Media3 documentation, GitHub implementations, or other proven implementations when the problem involves provider/extractor/player architecture.
3. **Compatibility decision** — explicitly state whether the reference fits AniLab/KakaAnime's current architecture. Do not copy a reference blindly.
4. **Fix** — implement the smallest native AniLab change that addresses the proven root cause. Do not stack speculative patches.
5. **Validate** — run the relevant build/E2E gate and inspect the new evidence.
6. **Escalate only when needed** — if the fix still fails, decide whether another reference search is useful. Search again when the new evidence points to a different or more specific technical problem; otherwise continue debugging from the evidence.
7. **Checkpoint** — record the audit, reference decision, code/config change, validation result, and classification (`BUG`, `WORKAROUND`, `ENHANCEMENT`, `LOCKED`, or `BLOCKED`) when the change is meaningful.

### Practical shorthand

- **Fix?** → Yes, when the root cause is sufficiently supported.
- **Cari referensi?** → Yes when external/proven patterns can reduce uncertainty; no when the current evidence is already sufficient and another search would add noise.
- **Cocok?** → Always compare the reference against AniLab's existing architecture before implementation.
- **Fix dulu atau cari lagi?** → If a proven compatible reference exists, fix. If the root cause remains uncertain or the reference does not match, search/audit again first.

## Migration note

Older checkpoint files that still exist at repository root are historical documents being migrated into this structure. Their Git history remains intact; migration does not rewrite historical commits.
