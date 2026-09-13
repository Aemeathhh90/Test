# KakaAnime — Working Rules

## 1. UI foundation before implementation
For every new feature, do not immediately write production code.

First discuss and agree on the UI foundation:
- purpose and user flow
- screen/layout structure
- buttons, states, labels, and interactions
- free vs premium behavior when relevant
- edge cases and empty/loading/error states

Only after the UI foundation is agreed should implementation begin.

## 2. Applies beyond UI
The same discussion-first rule applies to other meaningful changes, including:
- monetization and economy rules
- navigation and player behavior
- data/state changes
- backend/provider behavior
- permissions and integrations
- major refactors or architecture changes

Do not "tembak kode" based on an assumption when the intended behavior has not been agreed.

## 3. Preserve existing foundations
Before changing an existing feature, inspect the current implementation and checkpoint. Do not casually replace or remove an established behavior.

## 4. Build before declaring complete
A feature is not marked complete just because code was committed. The relevant Android build must pass, and runtime behavior should be verified when practical.

## 5. Checkpoint discipline
After a meaningful completed foundation/change, record a checkpoint so the project can be resumed safely without losing the agreed behavior.

## 6. Status markers
Use:
- 🟢 completed and verified
- 🟡 in progress / awaiting verification
- 🔴 blocked or failing

Never mark a feature 🟢 when the build or required verification is still failing.
