# Home Premium Offline Label Fix — 2026-09-14

## Change
Removed the obsolete `Offline` label from the Home Premium card because Offline Mode was removed from the KakaAnime roadmap.

Current label:
`1080p • Auto Skip • Premium`

Download remains a future roadmap feature and was not implemented by this change.

## Status
- 🟢 Offline Mode reference removed from this Home UI card
- 🟢 Download roadmap remains intact; no download implementation added
- 🟢 Continue Watching resume fix remains in place
- 🟢 CI Build #461 — success
- 🟢 CI Build #462 — success
- 🟡 Core V1 audit continues
- ⏸ Watch Together + Social/backend until Core V1 audit/foundation is complete
- 🔒 Security hardening remains an end-stage task

## Commits
- `d9c8a9601c7ec659bb019edebf12474af7cdb742` — initial label change
- `a6c0bb5d5edcb32d4a7f2ddb154cf37dd0d4473d` — correction removing accidental invalid import; final code state

## Technical note
The second commit is the final intended state. No feature behavior was changed beyond removing the obsolete Offline label.

## Next step
Continue Core V1 audit, prioritizing navigation/state persistence and provider loading/error paths. Keep Watch Together/Social and Download deferred until the core audit is complete.
