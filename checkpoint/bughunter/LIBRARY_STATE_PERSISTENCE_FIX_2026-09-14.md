# Bug Hunter — Library State Persistence Fix — 2026-09-14

## Scope
Grouped fix for confirmed Library/history persistence bugs found during the state-persistence + Favorite + Library + Calendar sweep.

## Confirmed bugs
- `LibraryTabsScreen` reads both season-aware history and legacy title-based history during migration.
- `clearHistory()` previously cleared only the season-aware store, so legacy entries could immediately reappear after `Hapus semua`.
- Per-entry delete previously removed only the season-aware entry; a matching legacy entry could reappear after refresh.

## Fix
- `SeasonAwareStateRepository.clearHistory()` now clears both season-aware and legacy history stores.
- `SeasonAwareStateRepository.deleteHistory(...)` now accepts the legacy title and removes the matching legacy entry as well.
- `LibraryTabsScreen` passes the displayed history entry title into the grouped delete operation.

## Audit notes
- Favorite storage is group-scoped and Library has an intentional legacy-title fallback; no separate confirmed favorite correctness bug was found in this pass.
- Calendar matching is season-aware and title/alias normalized; no confirmed persistence blocker was found there.
- No new feature work was introduced.

## Status
- 🟢 Library state-persistence bug group fixed in commits:
  - `dfb7b19284493fd96b7028036696f6901e333674`
  - `540a6b88ed00c879d939597a5db5147ac754bf2d`
- 🟡 Android Release build still requires fresh successful CI evidence.
- 🔴 Previously confirmed Samehadaku UNKNOWN-stream escape and Media3 unsupported-container runtime issues remain mapped and intentionally untouched during this grouped state fix.

## Next
Continue Bug Hunt mapping of remaining core state/UI areas, then perform the final grouped fixes and fresh build/audit. Do not mark Bug Hunt complete until release build and runtime evidence are satisfactory.
