# KakaAnime Social + Watch Together Plan — 2026-09-14

## Status
- 🟢 Product direction agreed: KakaAnime will have a Social layer centered on anime and Watch Together.
- 🟢 Social communication scope is text-only.
- 🟢 Watch Together is planned as a flagship differentiating feature.
- 🟢 Planned social basics: profile, follow/friends, DM text, room membership, and Watch Together room chat.
- 🔴 Backend implementation has not started.
- 🔴 Social UI implementation has not started.
- 🟡 Core V1 behavioral/UI audit remains the active priority.
- 🟡 Provider/real-stream E2E remains pending.
- ⏸️ Download + Offline Mode remains deferred to its planned stage.

## Product Scope
Social is intentionally not a general-purpose media platform. No user-uploaded video, photo, sticker/GIF, voice call, or file sharing is planned in this scope.

Primary experience:
Anime → Friends → Watch Together → Text Chat

## Watch Together Direction
The future backend should support room creation/joining, host/member roles, synchronized play/pause/seek, episode changes, room membership, invites, and text chat. Real-time synchronization is expected to use a persistent real-time channel such as WebSocket rather than polling alone.

## Architecture Note
Future backend foundation should be designed so Social and Watch Together can share authentication/user identity, room state, and messaging infrastructure without modifying protected Core V1 behavior unnecessarily.

## Scope Decision
Do not implement Social or Watch Together during the current audit. Finish Core V1 audit and confirmed bug fixes first. Once Core V1 is stable and verified, open Backend + Watch Together as a separate planned stage.

## Next Step
Continue the current Core V1 navigation/state/UI audit. Backend and Social remain planned, not active work.
