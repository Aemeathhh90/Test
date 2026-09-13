# KakaAnime Calendar Compile Fix — 2026-09-14

## Status
- 🟢 Calendar `Surface` calls now use explicit Material3 `shape =` arguments.
- 🟢 Fix committed directly to `main`.
- 🟡 Android release build verification pending.
- 🟡 Core behavioral/UI audit continues.
- 🟡 Provider/real-stream E2E remains pending.
- ⏸️ Download + Offline Mode remains deferred.

## Change
- `a5f3adce918a65841b6ba98eb0c7f49742dca6ed` — `fix: use explicit Material3 Surface shape parameter`
- Corrected Calendar's `Surface` invocations so Kotlin resolves the Material3 shape parameter explicitly.

## Verification
- Code commit: 🟢 `a5f3adce918a65841b6ba98eb0c7f49742dca6ed`
- Android release build: 🟡 pending

## Next step
Verify this commit with GitHub Actions. If green, continue the core navigation/state audit and then implement Calendar → AniList Detail integration without expanding Download/Offline.
