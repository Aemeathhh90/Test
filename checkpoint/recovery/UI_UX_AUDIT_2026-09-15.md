# KakaAnime UI/UX Audit Checkpoint — 2026-09-15

## Status
Audit mode only. No UI/UX fixes from this audit have been applied yet.

## 🔴 High Priority / Must Fix

### 1. Global Android System Back
- System Back currently can exit the Activity from non-root screens because screen navigation is state-driven without a global back policy.
- Target behavior:
  - Episode Gate open → System Back closes the gate.
  - Player → Detail.
  - Detail → Home.
  - Premium → previous/root Profile flow.
  - Watch Together → Social.
  - Home/root → normal Android app exit behavior.
- Do not make the app depend on an in-app Back button. In-app Back is only a shortcut.
- Fix at the navigation/state architecture level, not with scattered per-screen patches.

### 2. Episode Gate — Size / Layout
- Dialog is too large and consumes too much vertical space.
- Reduce overall dimensions/padding while keeping the episode preview and primary actions readable.

### 3. Episode Gate — Monetization Flow Is Wrong
Current UI has:
- Tonton Iklan & Buka
- Tunggu 90 Detik
- Premium • Tanpa Menunggu

Required KakaAnime concept:
- Free → Tonton Iklan → receive 2 diamonds → 1 diamond is consumed for the episode → play.
- Premium → play directly; premium entitlements apply.
- Remove the 90-second waiting option from the UI and flow.
- Remove obsolete copy referring to the 90-second countdown.
- During implementation, audit for remaining countdown logic so it is not merely hidden in the UI.

### 4. Episode Loading Performance
- Anime Detail can show a long loading state before episodes appear.
- Current resolver path can search candidates/providers sequentially.
- Target: fast first display, provider timeout/fallback, and cached episode list with background refresh where appropriate.
- Do not optimize blindly; preserve provider correctness and diagnose timing/root cause before changing provider behavior.

### 5. Home Layout / Contrast
- Hero profile section is crowded.
- Avatar is too large.
- Premium badge is cramped/cut off.
- Name/status/level hierarchy needs cleanup.
- Quick-stat cards are too dark and text contrast is poor.
- Large gray/empty area appears below Home content.
- Several dark texts appear on dark backgrounds across the app.

## 🟡 Polish / Medium Priority
- Social cards and spacing need minor refinement.
- Profile sections are somewhat dense.
- Premium page hierarchy can be improved.
- Settings screens are functional but visually dense.
- Search empty state can feel too empty.
- Some poster/card sizing and spacing can be refined.

## 🟢 Keep / Good Foundation
- Social Home overall structure.
- Friends list structure.
- Messages structure.
- Profile foundation.
- Playback Settings grouping.
- Download Settings grouping.
- Episode card concept, including watched/locked state.
- Diamond/Premium concept and overall monetization direction, except the incorrect 90-second option.

## Implementation Rule
When Shin chooses which items to fix, group related changes into a targeted implementation batch. Follow the project debugging rule: diagnose → identify root cause → targeted fix → checkpoint/commit → Android Build → E2E/runtime verification. Avoid blind tambal-sulam.
