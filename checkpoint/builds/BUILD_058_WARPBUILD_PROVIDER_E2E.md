# BUILD #58 — WarpBuild Provider E2E Setup

Date: 2026-09-13
Repo: KakaAnime/KakaAnime
Branch: main

## Status

🟡 CI migration/setup — workflow added, runtime result not yet tested.

Bitrise could not start the next provider E2E run because the available build credits were exhausted. WarpBuild was already connected and exposed Linux x86-64 GitHub runners, so the provider E2E gate was moved to a manual GitHub Actions workflow using WarpBuild.

## Architecture

Before:

`main → Bitrise provider_e2e → Samehadaku Episode 7 → Media3 → onRenderedFirstFrame()`

After:

`main → GitHub Actions / WarpBuild → Android emulator (KVM) → Samehadaku Episode 7 → Media3 → onRenderedFirstFrame()`

The application/provider code was not changed by this migration.

## Workflow

File:
`.github/workflows/provider-e2e.yml`

Commit:
`ef4f93e49f93d9e5c08b480a5a164f9f2c0c1160`

Runner:
`warp-ubuntu-latest-x64-16x;nested-virtualization.enabled=true`

The workflow:
- runs manually with `workflow_dispatch`
- uses JDK 17
- uses Gradle setup/cache action
- enables and verifies `/dev/kvm`
- boots Android API 35 Google APIs x86_64 emulator
- runs the exact P0 test:
  `com.kakaanime.app.provider.SamehadakuProviderE2ETest#onePieceEpisodeSevenRendersFirstFrame`
- uploads Android test reports when available

## Validation Gate

This setup is not considered a provider success yet.

Samehadaku remains 🔴 until the E2E test proves:

`search → detail → episode 7 → typed stream → Media3 playback → onRenderedFirstFrame()`

## Next action

Open GitHub → Actions → Provider E2E → Run workflow on `main`.

Then inspect:
- KVM verification
- Gradle/package result
- `E2E_STREAM` type
- `E2E_PROVIDER` type
- `E2E_MEDIA3_ERROR` if present
- `onRenderedFirstFrame`

Do not modify provider code before the first WarpBuild E2E result unless the workflow itself fails to start/build.
