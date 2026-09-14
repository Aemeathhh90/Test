# Bug Hunter — Provider Health Operation Isolation

Date: 2026-09-14

## Status
🟢 Provider health isolation fix committed.

## Confirmed bug
`SmartProviderRouter` previously stored provider health by provider ID only. A stream failure could therefore cool down the same provider for search, anime lookup, or episode lookup.

## Fix
Health state is now keyed by:

- provider ID
- operation (`SEARCH`, `ANIME`, `EPISODES`, `STREAMS`)

Success/failure/cooldown handling now stays scoped to the operation that actually failed.

Search deduplication was also hardened to retain season-aware identity fields instead of collapsing same-title/same-year seasons solely by title and year.

## Evidence
- Commit: `5f6148ddb0863e1dc1b924e01f3c438a277352bd`
- File: `app/src/main/java/com/kakaanime/app/provider/SmartProviderRouter.kt`
- Build/CI: not yet verified; do not mark build green without actual CI/device evidence.

## Next bug-hunt target
Return to the MainActivity stream-session first-frame race and harden callback ownership without introducing unrelated changes.
