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

### Watch Together V1 Monetization Rule — CHECKPOINT
- 🟢 **Premium is required to create a Watch Together room and become Host.**
- 🟢 **Free users may join a room created by a Premium Host and watch together.**
- 🟢 While inside a valid Watch Together room, Free participants follow the Host's episode changes automatically.
- 🟢 A Free participant does **not** spend diamonds or watch an ad when the Premium Host changes to another episode while they remain in the room.
- 🟢 Free participants cannot create their own Watch Together room.
- 🟢 Free participants cannot take over Host control.
- 🟢 The Watch Together entitlement applies only while the Free user is inside a valid room.
- 🟢 Once a Free user leaves the room and watches normally, the regular Free-user diamond/ad rules apply again.
- 🟢 No ad/diamond gate should interrupt the Watch Together episode-change flow for Free participants.

Example:
Premium Host starts One Piece Episode 7 → Free participant joins → Host changes to Episode 8 → Free participant automatically follows to Episode 8 without an ad or diamond requirement.

## Architecture Note
Future backend foundation should be designed so Social and Watch Together can share authentication/user identity, room state, and messaging infrastructure without modifying protected Core V1 behavior unnecessarily.

## Scope Decision
Do not implement Social or Watch Together during the current audit. Finish Core V1 audit and confirmed bug fixes first. Once Core V1 is stable and verified, open Backend + Watch Together as a separate planned stage.

## Next Step
Continue the current Core V1 navigation/state/UI audit. Backend and Social remain planned, not active work.
