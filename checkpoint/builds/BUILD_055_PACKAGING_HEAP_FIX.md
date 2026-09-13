# Build 055 — Packaging Heap Fix

## Status
🔴 BUILD #55 FAIL — E2E was skipped because Android packaging failed first.

## Error
`Execution failed for task ':app:packageDebug'` → `PackageAndroidArtifact$IncrementalSplitterRunnable`.
The log also reports:
`Unable to strip the following libraries, packaging them as they are: libandroidx.graphics.path.so.`

The strip message is a warning; the fatal task is `packageDebug`. The uploaded Bitrise log does not contain the nested `Caused by:` / OOM exception, so the exact inner exception is not proven by this log alone.

## Root cause assessment
Strong candidate: Gradle heap was constrained to only `-Xmx1024m` with `-XX:MaxMetaspaceSize=384m`. The failure occurs in the APK packaging/splitting stage, and `PackageAndroidArtifact$IncrementalSplitterRunnable` is known to surface when packaging runs out of JVM memory. This is classified as a likely resource/configuration issue, not a provider/extractor bug.

## Architecture before
Bitrise 16 GB machine → Gradle JVM max heap 1 GB / metaspace 384 MB → compile/dex/native merge succeeds → `packageDebug` fails → emulator wait + instrumented E2E skipped.

## Architecture after
Bitrise 16 GB machine → Gradle JVM max heap 4 GB / metaspace 1 GB → packageDebug has adequate JVM memory → continue to emulator + provider E2E.

## Code/config change
Updated root `gradle.properties` on `main`:
- `org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m -Dfile.encoding=UTF-8`
- Kept `org.gradle.workers.max=1` and `org.gradle.parallel=false` to avoid unnecessary concurrent memory pressure.

Commit: `306ebbd33e6bc57c7a39c0cbeb5c12ea88fba510`

## Validation
Run the same Bitrise `provider_e2e` workflow. First gate is `:app:packageDebug` / `:app:assembleDebug` completing. Only after packaging succeeds should the Samehadaku Episode 7 E2E be evaluated.

If packageDebug still fails, obtain the nested `Caused by:` with `--stacktrace` before making another config change; do not blame `libandroidx.graphics.path.so` solely from the warning line.

## Impact
This change affects build infrastructure only. Provider/extractor code is untouched. The previous UNKNOWN stream type fix remains intact and will be tested only after the build/package gate passes.
