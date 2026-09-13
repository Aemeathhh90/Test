# KakaAnime Interaction System — 2026-09-13

## Purpose
Establish KakaAnime's own interaction language. Saikou, Dantotsu, forks, and ReDantotsu are references for proven interaction patterns, not the product identity.

## Status
- 🟢 Interaction principles defined
- 🟢 Scroll policy defined
- 🟢 Tap/press policy defined
- 🟢 Navigation gesture policy defined
- 🟢 Animation policy defined
- 🟢 Player gesture boundary defined
- 🟡 Settings implementation still requires final visual/interaction pass

## Core rules
### Scroll
- Use native Compose lazy scrolling as the base.
- Preserve natural fling/inertial behavior.
- Avoid unnecessary nested scrolling and recomposition that causes scroll jumps.
- Keep image loading from visibly disrupting list position.

### Tap and press
- Interactive rows/cards use the whole visible row/card as the hit area.
- Maintain accessible touch targets.
- Provide subtle pressed-state feedback before/while an action occurs.
- Do not require tapping a tiny icon or chevron to activate a row.

### Navigation
- Android back gesture remains native unless a screen has a documented reason to override it.
- Screen transitions should be smooth and short rather than abrupt.
- Horizontal swipe is opt-in per screen/function, never a global gesture.

### Animation
- Prefer lightweight spring/physics-style motion for selected states, overlays, navigation, and expandable content where it improves perceived responsiveness.
- Avoid animating every component.
- Target smooth rendering and avoid animation that increases layout/recomposition cost.

### Overlay / sheets
- Sliding panels/sheets may be used where they improve context and discoverability.
- Glass/blur is optional visual treatment, not an interaction requirement.

### Screen-specific gestures
- Settings: vertical scroll + tap only; no swipe-to-delete or arbitrary horizontal gestures.
- Calendar/date selectors: horizontal swipe may change date when that behavior is explicit in the UI.
- Player: separate gesture system for seek/brightness/volume/episode controls; player gestures must not leak into general app UI.

## Reference lineage
- Saikou: interaction/UI foundation reference.
- Dantotsu: UI and animation patterns.
- Active forks: useful evidence for gesture/navigation fixes.
- ReDantotsu: spring animation, scrolling/rendering stability, sliding settings overlay concepts.

## Design principle
KakaAnime should feel smooth and responsive for the same technical reasons as these references, while maintaining its own UI/UX identity. Future implementation notes should say **KakaAnime UI/UX**, with external projects cited only as reference sources.

## Next
Apply this system to Settings, verify build, then lock Settings UI/UX before moving to Appearance.
