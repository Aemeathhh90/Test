# KakaAnime Foundation 1.0 — Design Tokens

Date: 2026-09-13

## Status
- 🟢 Shared spacing tokens added
- 🟢 Shared radius tokens added
- 🟢 Minimum touch target token added
- 🟢 Shared icon/progress sizing tokens added
- 🟢 Shared typography hierarchy added
- 🟢 Existing accent and dark/light theme behavior preserved
- 🟡 Existing screens still need migration to the shared tokens
- 🔴 Full visual audit/build verification not completed yet

## Commit
- `511de4d004cf128eddefe61edba004788baf4ce0`
- `foundation: add KakaAnime design tokens and typography`

## Token baseline
- Screen padding: 18dp
- Section gap: 14dp
- Card gap: 12dp
- Compact gap: 8dp
- Card radius: 20dp
- Small radius: 14dp
- Pill radius: 999dp
- Minimum touch target: 48dp
- Icon size: 24dp
- Small icon size: 20dp
- Progress height: 4dp

## Typography baseline
- Headline: 24sp / 30sp bold
- Title large: 20sp / 26sp semibold
- Title medium: 16sp / 22sp semibold
- Body large: 16sp / 22sp
- Body medium: 14sp / 20sp
- Body small: 12sp / 16sp
- Label large: 14sp / 20sp semibold
- Label medium: 12sp / 16sp medium

## Technical note
These are KakaAnime-owned foundation tokens. Saikou, Dantotsu and ReDantotsu remain reference sources only; their UI values are not copied as dependencies.

## Next step
Apply the tokens to the Settings screen first, then verify the build before continuing to Appearance and the remaining core screens.
