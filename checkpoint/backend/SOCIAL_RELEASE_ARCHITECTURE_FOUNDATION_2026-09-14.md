# KakaAnime — Social Release Architecture Foundation

Date: 2026-09-14
Status: Planning decision locked; implementation deferred.

## Product scope V1
- Social is text-only plus Watch Together.
- No voice/video calls, voice notes, photo/video upload, stickers, file sharing, group chat, or social feed.
- Global Chat and Episode Chat remain V2 placeholders.

## Backend architecture
- REST/API is the backbone for profile, friends, search, favorites, history, currently watching, and friend activity.
- WebSocket is reserved for realtime DM text, friend-request events, social notifications, Watch Together room state/playback sync, and online/offline presence if needed.
- DM messages are stored permanently, paginated, text-only, and rate-limited/anti-spam protected.
- V1 deletion policy: Delete for Me. Delete for Everyone can be considered later.

## Identity and authentication
- Permanent internal User ID (UUID/ULID) is the relation key.
- Username is unique/searchable, changeable with a 30-day cooldown; previous usernames should be reserved for roughly 30–90 days.
- Nickname/display name can change freely.
- Email remains private.
- Authentication: email + password with verification, plus Google Sign-In; both resolve to one KakaAnime User ID.
- Unverified accounts may have restricted/rate-limited Social access.

## Initial production infrastructure target
- VPS baseline: 2 vCPU, 8 GB RAM, 80–100 GB NVMe/SSD, Linux.
- PostgreSQL and initial Redis can run on the same VPS.
- HTTPS/TLS and DNS should sit behind Cloudflare; Cloudflare Free is sufficient initially.
- Domain is required for stable API/WebSocket endpoints.
- Transactional email should use an external provider rather than self-hosting mail.
- Anime video streams must not pass through or be stored on the KakaAnime application VPS.
- Scale later to a larger VPS/Redis and eventually separate API, WebSocket, database, and workers only when traffic requires it.

## Implementation order
AUTH → PROFILE → FRIENDS → MESSAGES → NOTIFICATIONS → WATCH TOGETHER → REALTIME SYNC.

## Scope guard
- Social Blueprint is the source of truth for structure/flow.
- Existing KakaAnime UI/UX identity is the visual source of truth.
- New features require an audit and UI foundation agreement before implementation.
- Complex gestures/interactions wait for the final global Interaction Pass.
- Backend implementation waits until the current Bug Hunt/core stabilization is complete.

## Bug Hunt gate
No new Social backend implementation during Bug Hunt. After Bug Hunt is green and core/UI are stable, use this checkpoint as the starting architecture for production backend/VPS planning.

## Open before launch
Security/privacy policy, moderation/anti-abuse, database backup/restore, observability/alerts, account recovery, email provider selection, Play Store compliance, and final VPS/provider selection still require review.
