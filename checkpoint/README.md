# KakaAnime Checkpoint System

This folder is the source for project continuity, locked decisions, technical diagnosis, and historical validation.

## Source of truth

- `main` is the integration/release source of truth.
- `checkpoint/MASTER_CHECKPOINT.md` is the compact project dashboard and locked-rule reference.
- Historical checkpoints are evidence, not disposable changelogs.

## Mandatory work order — LOCKED

Before **any audit, investigation, implementation, refactor, dependency change, or workflow change**:

```text
CHECK CURRENT REPO
      ↓
CHECK BRANCH + LATEST COMMIT
      ↓
READ RELEVANT CHECKPOINT / HISTORY
      ↓
INSPECT CURRENT CODE / WORKFLOW / LOGS
      ↓
AUDIT ROOT CAUSE OR DESIGN STATE
      ↓
REFERENCE CHECK IF NEEDED
      ↓
IMPLEMENT
      ↓
VALIDATE
      ↓
CHECKPOINT
```

Never begin from an assumption that the repository still matches an older conversation, screenshot, or memory.

## Checkpoint structure

- `MASTER_CHECKPOINT.md` — compact status, locked rules, architecture gates.
- `PROVIDER_MAPPING.md` — provider map and provider-family decisions.
- `providers/` — provider-specific technical checkpoints.
- `extractors/` — extractor and stream-resolution architecture history.
- `backend/` — backend architecture and audit notes.
- `player/` — Media3/video-player decisions and validation.
- `ui/` — UI architecture and locked UI decisions.
- `social/` — Social/Profile decisions.
- `builds/` — historical CI/E2E validation.
- `audit/` — audit evidence and root-cause investigations.
- `recovery/` — recovery/rollback evidence.
- `bughunter/` — focused bug-hunting records.
- `data/` — data/model-related checkpoints.

### Active vs historical

`MASTER_CHECKPOINT.md` and this README are the active index. Topic-specific files preserve history and should not be rewritten merely to make the latest state look cleaner. When a decision changes, add a new checkpoint/addendum and link the old decision rather than erasing its history.

## Audit → Reference → Fix → Validate

For every meaningful technical bug or architecture change:

1. **Audit first** — use the current repository, code, logs, and relevant checkpoint history.
2. **Reference check** — search CloudStream, official Android/Media3 documentation, GitHub implementations, or other proven implementations when useful.
3. **Compatibility decision** — explicitly state whether the reference fits KakaAnime's current architecture. Do not copy a reference blindly.
4. **Fix / implement** — make the smallest change supported by the evidence.
5. **Validate** — run the relevant build/test gate and inspect the new evidence.
6. **Checkpoint** — record what changed, why, evidence, validation, and classification when meaningful.

### Classification

- 🔴 **BUG** — implementation/logic/configuration is wrong.
- 🟡 **WORKAROUND** — temporary solution; keep looking for a final fix when appropriate.
- 🟢 **ENHANCEMENT** — working system improvement.
- ⚪ **LOCKED** — proven decision/behavior; do not change without technical reason.
- ⚫ **BLOCKED** — proven external/technical blocker after audit.

## Testing rules

- Android Build is the routine validation gate.
- Provider E2E is manual-only and is used when provider → stream → playback evidence is required.
- UI/UX work does not wait for Provider E2E when provider behavior is unrelated.
- GitHub Actions are not dispatched by the assistant; the user manually starts E2E when requested.

## Work split

- `Aemeathhh90/vider` — dedicated Provider workspace.
- `Aemeathhh90/uy-uk` — dedicated UI/UX workspace.
- `Aemeathhh90/Test` — main integration/release repository and historical safety net.

## Migration note

Older checkpoint files remain preserved so their Git history and diagnostic value are not lost. Cleanup means creating a clear index and separating active state from history; it does **not** mean deleting useful evidence.
