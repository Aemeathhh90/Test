# Bug Hunt — MainActivity Stream Session Race

Date: 2026-09-14
Branch: `main`

## Status

🔴 Confirmed — fix pending

## Finding

The 15-second watched timer in `MainActivity.kt` is tied to the resolver effect, but it has no stream-session generation guard. A resolver restart (episode/season/premium/retry change) can create a new stream session while an older delayed coroutine is still alive. The old coroutine can later observe the new `streamFirstFrameRendered` state and record the wrong session as watched.

Current logic resets `streamFirstFrameRendered`, but reset alone does not invalidate an already-running delayed coroutine.

## Required fix

Introduce a monotonically changing stream-session identity/generation. Each resolver run captures its own session ID. The delayed watched record may execute only when:

- the captured session ID is still current;
- the first frame belongs to that same session;
- selected anime/season/episode still match;
- a valid resolved stream still exists.

The first-frame callback should also be associated with the active session rather than acting as a global boolean for all stream generations.

## Verification

No code change is claimed in this checkpoint yet. Previous direct write attempt was blocked, so this file records the confirmed finding and the exact fix contract.

## Next

Implement the session-generation guard without changing player/UI behavior, then audit the timer lifecycle again.
