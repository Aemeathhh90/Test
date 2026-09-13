# BUILD 062 — Otakudesu Compile Fix

## Classification
🔴 BUG — implementation error in the Otakudesu server extractor.

## Evidence
The dedicated Otakudesu E2E workflow uploaded log showed Kotlin compilation failure in `OtakudesuServerExtractor.kt`:
- unresolved `id`
- unresolved `i`
- unresolved `q`

The failing code used `PlaybackCandidate` for mirror entries, but `PlaybackCandidate` only contained `url` and `quality`.

## Root cause
Mirror AJAX entries need three separate values (`id`, `i`, `q`) before the AJAX request can be made. They were incorrectly represented as a playback candidate URL, then accessed as if the candidate had `id`, `i`, and `q` fields.

## Fix
Introduced a dedicated `MirrorEntry(id, i, q)` model and changed mirror parsing to return `MirrorEntry`. `PlaybackCandidate(url, quality)` remains reserved for resolved playback/download candidates.

## Validation status
🟡 E2E pending. The next validation must use the dedicated `Otakudesu E2E` workflow. Do not mark Otakudesu green until the test proves `onRenderedFirstFrame()`.

## Commit
`e628dcaa800c8502b8aa8a6f66337633b063d5b9`
