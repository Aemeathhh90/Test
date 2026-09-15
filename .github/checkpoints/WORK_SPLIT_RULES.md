# KakaAnime Work Split Rules

## Purpose
Separate slow provider validation from UI/UX development so provider E2E does not block UI progress.

## Workspaces
- `work/provider`: provider, source resolver, stream extraction, subtitle/language resolution, playback integration, and provider E2E diagnostics.
- `work/ui-ux`: Home, Anime Detail, episode UI, Favorite, Search, Profile, Social, Diamond, Premium, theme/color customization, navigation, and player UI.
- `main`: integration/release checkpoint only. Changes enter main only after the relevant workspace passes its checks.

## Test policy
1. Routine validation uses Android Build.
2. Provider E2E is manual-only and is run only when provider -> stream -> playback evidence is required.
3. Do not run Provider E2E for ordinary UI changes.
4. Do not dispatch GitHub Actions from the assistant; the user manually starts Provider E2E when requested.
5. A failing Provider E2E must not block independent UI/UX work unless the change depends on provider behavior.

## Checkpoint policy
- Every meaningful completed milestone gets a clear commit.
- Do not overwrite working code with speculative fixes.
- Prefer root-cause fixes over repeated patches.
- Before integrating provider work into `main`, verify the provider contract and playback path.
- Before integrating UI/UX work into `main`, Android Build must pass.

## Integration order
1. Develop provider and UI/UX independently.
2. Keep both branches/builds green where applicable.
3. Integrate provider + UI/UX into `main` only at a deliberate checkpoint.
4. Run a final integration Android Build before release testing.
