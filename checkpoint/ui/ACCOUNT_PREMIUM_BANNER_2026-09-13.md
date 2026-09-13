# KakaAnime Account Premium Banner Checkpoint — 2026-09-13

## Decision
- 🟢 Account banner is reserved for the Premium conversion/status banner.
- 🟢 Free user: `Upgrade to Premium` CTA.
- 🟢 Premium user: `Premium Aktif` state using the same banner area.
- 🟢 Custom profile banner selection is removed from the Edit Profile UI.
- 🟢 Avatar customization remains available independently.

## Technical notes
- The existing Account dashboard already uses monetization state as the source of truth for Free/Premium and keeps the Premium CTA connected to the existing Premium flow.
- Edit Profile no longer exposes banner presets or saves a user-selected banner.
- Legacy local banner preference methods may remain for backward compatibility and are no longer used by the profile editor.
- Android build/runtime verification remains intentionally deferred.

## Commit
- `d25f6b5cc07504bf59356bd857bffd6382a3535d` — `account: make premium upgrade banner non-customizable`

## Next step
- Continue the Bottom Navigation audit, especially History and the final tab structure.
