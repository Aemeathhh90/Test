# Bug Hunter — Stream Session First-Frame Callback

Date: 2026-09-14

## Status
🟢 Callback ownership hardening committed.

## Confirmed bug
`VideoPlayerScreen` registered a long-lived Media3 `Player.Listener` inside `DisposableEffect`, but the listener captured the `onRenderedFirstFrame` lambda from an earlier composition. `MainActivity` had introduced stream session IDs, so the listener could report a stale session ID after recomposition/retry.

## Fix
The Player listener now uses Compose `rememberUpdatedState(onRenderedFirstFrame)`. The listener itself keeps its existing lifecycle keys, while the callback it invokes is always the latest callback supplied by the current composition.

This is the Compose-recommended pattern for long-lived callbacks used by `DisposableEffect`. citeturn3search1turn3search4

## Evidence
- Fix commit: `31096e466ebcb0156feeebd3bb6996466beaa8e8`
- Cleanup commit: `7d818af48112f247868ebf4d73a41aa2f2ededb4`
- Patch: +2/-1 in `VideoPlayerScreen.kt`.
- Temporary patch workflow was removed immediately after applying the fix.
- Build/runtime: 🟡 pending actual Android build/device evidence.

## Audit result
The stream timer now has both protections:
1. timer coroutine is scoped to a stream session ID;
2. first-frame callback resolves through the latest composition callback rather than a stale closure.

Do not mark the runtime behavior fully verified until the app actually renders a real first frame on device.

## Next bug-hunt target
Calendar season matching.
