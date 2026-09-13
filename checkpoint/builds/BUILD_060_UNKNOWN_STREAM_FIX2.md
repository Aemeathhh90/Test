# Build Checkpoint Addendum — UNKNOWN Stream Fix 2

**Tanggal:** 13 September 2026
**Related checkpoint:** `BUILD_060_UNKNOWN_STREAM_FIX.md`
**Status:** 🟡 FIX APPLIED — E2E VALIDATION PENDING

## Correction

After applying the UNKNOWN-stream fix, the validator's byte-probe text conversion was made explicit with `String(bytes, charset)` to avoid relying on a Kotlin byte-array conversion overload.

## Final Source Commit

`3fa39ff7f3daa11c1bcc19f35444fdbbb161dc62`

This commit contains the finalized `StreamValidator.kt` implementation. The previous validator fix commit `a798c7400fb566f36e39dcf3a4f6f88dea5b609b` is retained as history.

## Intended Behavior

```text
UNKNOWN stream
 ↓
HTTP response probe
 ↓
HLS / DASH / MP4 proven?
 ├─ YES → typed ProviderStream
 └─ NO  → reject
```

HTTP 206 alone is never sufficient to classify UNKNOWN.

## Validation

No E2E result is claimed by this checkpoint. The next provider E2E run must verify the actual Samehadaku Episode 7 path and ultimately `onRenderedFirstFrame()`.
