# KakaAnime Repository Split Checkpoint — 2026-09-15

## Classification
🟢 LOCKED / WORKFLOW STRUCTURE

## Repository audit
Before changing structure, the current repositories were checked.

### Main
- `Aemeathhh90/Test`
- Existing Android project, provider E2E workflow, checkpoint history, and integration state.
- Remains the main integration/release safety net.

### Provider
- `Aemeathhh90/vider`
- Existing `README.md` confirmed.
- Repository is intentionally kept as the dedicated Provider workspace.
- Work rules added in `RULES.md`.

### UI/UX
- `Aemeathhh90/uy-uk`
- Existing `README.md` confirmed.
- Repository is intentionally kept as the dedicated UI/UX workspace.
- Work rules added in `RULES.md`.

## Locked workflow

```text
Repository check
→ checkpoint/history check
→ current code/workflow audit
→ reference check when useful
→ implementation
→ Android Build / relevant validation
→ checkpoint
```

No audit or implementation should start from an assumed repository state.

## Work split

- Provider: `vider`
- UI/UX: `uy-uk`
- Integration/release/history: `Test`

Provider E2E remains manual-only and does not block unrelated UI/UX work.

## Changes recorded

- Main work-split rules updated: `1eb1d6c0b18587e4ec8ed0086d632e6fc1dad67b`
- Checkpoint index reorganized: `48e91ced0e5b353a3672d28b399581cdac6ae209`
- Master checkpoint consolidated: `017cdd8a0e1e5de1b7459268964f86f62a395665`
- Provider repo rules: `8e58755e909261835a71e0306ee1b3a64043ecb7`
- UI/UX repo rules: `93752ffdf18e51d15d44e9c036d67f209e4afcf7`

## Next step
Do not implement application code yet. First audit the relevant repository in full, then establish its initial structure/checkpoint from the actual current state.
