# KakaAnime Work Split Rules

## Purpose
Separate slow provider validation from UI/UX development so provider E2E does not block UI progress.

## Mandatory pre-flight — LOCKED
Before **any audit, investigation, implementation, refactor, dependency change, or workflow change**:

1. Check the current repository first.
2. Check the current branch/ref and latest commit.
3. Inspect the relevant files, architecture, existing checkpoints, and current tests/workflows.
4. Compare the current state with the last known checkpoint before deciding what to change.
5. Do not assume the repository matches an older conversation, screenshot, or memory.

No implementation starts before this repository check is complete.

## Workspaces
- `work/provider`: provider, source resolver, stream extraction, subtitle/language resolution, playback integration, and provider E2E diagnostics.
- `work/ui-ux`: Home, Anime Detail, episode UI, Favorite, Search, Profile, Social, Diamond, Premium, theme/color customization, navigation, and player UI.
- `main`: integration/release checkpoint only. Changes enter main only after the relevant workspace passes its checks.
- `Aemeathhh90/vider`: dedicated provider workspace/repository.
- `Aemeathhh90/uy-uk`: dedicated UI/UX workspace/repository.

## Audit → Reference → Fix → Validate
1. Audit the actual current repository/code/logs first.
2. Identify the failing/affected layer and separate proven facts from hypotheses.
3. Search external references only when they reduce technical uncertainty.
4. Check reference compatibility with the current KakaAnime architecture before using it.
5. Fix the proven root cause; avoid speculative patch stacking.
6. Validate with the smallest relevant test first.
7. Record meaningful results in a checkpoint.

## Test policy
1. Routine validation uses Android Build.
2. Provider E2E is manual-only and is run only when provider -> stream -> playback evidence is required.
3. Do not run Provider E2E for ordinary UI changes.
4. Do not dispatch GitHub Actions from the assistant; the user manually starts Provider E2E when requested.
5. A failing Provider E2E must not block independent UI/UX work unless the change depends on provider behavior.

## Checkpoint policy
- Every meaningful completed milestone gets a clear commit/checkpoint.
- Do not overwrite historical diagnosis just to make the latest status look cleaner.
- Record error, evidence, root cause/hypothesis, affected layer, fix, validation, result, and next step when applicable.
- Prefer root-cause fixes over repeated patches.
- Before integrating provider work into `main`, verify the provider contract and playback path.
- Before integrating UI/UX work into `main`, Android Build must pass.

## Integration order
1. Develop provider and UI/UX independently.
2. Keep both workspaces/builds green where applicable.
3. Integrate provider + UI/UX into `main` only at a deliberate checkpoint.
4. Run a final integration Android Build before release testing.
