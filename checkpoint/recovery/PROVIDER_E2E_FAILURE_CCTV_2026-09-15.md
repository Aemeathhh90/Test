# Checkpoint — Provider E2E Failure / CCTV Follow-up

Date: 2026-09-15

## E2E result

Latest provider E2E artifact: `logs_94499286803.zip`

- `OtakudesuProviderE2ETest#onePieceEpisodeSevenRendersFirstFrame` — FAILED
- `SamehadakuProviderE2ETest#onePieceEpisodeSevenRendersFirstFrame` — FAILED
- `SamehadakuEpisodeDiagnosticsTest#onePieceEpisodeSevenReportsServerHosts` — no test failure reported in the run summary
- `:app:connectedDebugAndroidTest` — FAILED because 2 provider tests failed
- Total test run: about 10m 37s

## Infrastructure note

Emulator startup/logs also reported:

`ERROR | Unable to connect to adb daemon on port: 5037`

The emulator subsequently ran the tests, so this is recorded as an infrastructure warning rather than proven provider root cause.

## Provider failure

Observed failures remain stream acquisition failures. The current artifact does not expose enough `STREAM_CCTV_*` output to prove the first failing resolver/provider stage.

Therefore **NO provider logic patch is justified yet**.

## Required next diagnostic

1. Preserve current provider implementation.
2. Improve provider E2E workflow observability so emulator `adb logcat` is dumped after tests even when the test command fails.
3. Filter/upload `STREAM_CCTV_*` and provider diagnostic tags as an artifact.
4. Re-run E2E.
5. Identify the first failing stage from CCTV.
6. Apply one targeted provider fix only after root cause is proven.

## Rule

Do not use the two `no streams` failures as evidence for a specific extractor/API bug until CCTV confirms where the stream pipeline stops.
