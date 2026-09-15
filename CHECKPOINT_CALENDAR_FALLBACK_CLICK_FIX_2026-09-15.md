# Checkpoint — Calendar fallback click fix

## Branch
`ui-integration-08`

## Fix
Calendar V1 previously converted unmatched AniList schedule entries into synthetic `Anime` UI entries, but the click callback only searched `animeList`. As a result, a visible fallback entry could fail to open.

The bridge now resolves the selected item against the schedule and returns the same synthetic fallback `Anime` when no catalog match exists.

## Validation
- Static code audit: PASS for the corrected resolution path.
- Android Build: PENDING; no workflow was dispatched automatically.
- Provider E2E: NOT RUN.

## Status
🟡 FIXED / BUILD VALIDATION PENDING

## Commit
`f6971074fa5fe175a9383ab26835590fd4ec8468`
