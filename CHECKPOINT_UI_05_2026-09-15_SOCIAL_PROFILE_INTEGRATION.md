# Checkpoint UI-05 — Social + Profile V1 Integration

## Branch
`ui-integration-08`

## Base checkpoint
`e78563ad77b2575892424685445470df5855f8bb` — Calendar V1 integration boundary

## Integrated
- Social V1 presentation from `uy-uk` is now wired through the existing `SocialScreen` compatibility entry point.
- Social V1 keeps the locked layout: compact GLOBAL/ANIME cards, larger NONTON BARENG card, Active Friends.
- Friend rows open the UI-only Other User Profile V1 boundary.
- Profile V1 UI models and presentation components are present under `ui/profile`.
- Existing provider/backend/data/social business logic is not imported into the UI presentation layer.
- Existing rich root `ProfileScreen` remains preserved as the main-account/business boundary; no destructive replacement was made.

## Deliberate preservation
- Existing Watch Together callback from `MainActivity` remains the integration boundary.
- Existing root Profile flow (settings, appearance, premium, history-backed activity) remains intact rather than being replaced wholesale by the thinner UI-only Profile V1 presentation.

## Validation
- Static source audit: PASS for the new UI boundary and removed legacy Social-only references.
- Android Build: PENDING. No GitHub Actions workflow was dispatched automatically.
- Provider E2E: NOT RUN; remains a separate manual validation track.

## Status
🟡 INTEGRATED / BUILD VALIDATION PENDING
