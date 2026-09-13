# KakaAnime Foundation P0 — 2026-09-13

## Status

- 🟢 Shared async UI state model added.
- 🟢 Reusable skeleton loading components added.
- 🟢 Reusable error + retry component added.
- 🟢 Bounded in-memory TTL query/data cache primitive added.
- 🟡 Existing Home/Detail/Settings/History/Calendar screens still need migration onto these primitives.
- 🟡 Smart image preload policy is not wired yet.
- 🟡 Player lifecycle/background soft-recovery is not locked yet.
- 🔴 New episode notification scheduler/UI is not implemented.
- 🔴 Persistent video cache is not implemented.

## Commits

- `bf66da166ebaedf62d3e2eb266aa2d2622b3afc1` — foundation: add shared async UI state
- `b2e98d0766838f0198b3e394f581b8d371f5fc4b` — foundation: add reusable skeleton loading UI
- `99eaba166f68684972f9774426612c07fc7126de` — foundation: add reusable error retry state
- `ea3065c5d4db67c9b72fd2eba858d9255c040421` — foundation: add bounded TTL data cache

## Technical notes

These primitives are dependency-free beyond existing Compose APIs. The cache is intentionally small, bounded, in-memory, and TTL-based; it does not replace persistent storage or video caching. The UI state model keeps loading/success/error states explicit so feature screens can avoid ad-hoc booleans and inconsistent retry handling.

Compose performance guidance favors stable state, lazy layouts, cached calculations, and limiting unnecessary recomposition. The foundation follows that direction without introducing a new framework or dependency.

## Next step

1. Verify the current compile result first.
2. If green, migrate the highest-traffic Home/Detail data paths to `KakaUiState` + `KakaDataCache`.
3. Add navigation-aware image preloading using the existing Coil stack.
4. Then implement player lifecycle soft-recovery, followed by notification/countdown foundations.

## Rule

`change → commit → update checkpoint → verify commit → continue`.
