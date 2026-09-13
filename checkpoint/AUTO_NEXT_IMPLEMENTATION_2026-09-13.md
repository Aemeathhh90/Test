# Auto Next implementation

- Uses Media3 `Player.STATE_ENDED`.
- Auto Next ON: calls existing `onNextEpisode()` once per video.
- Auto Next OFF: does not advance automatically.
- Guard resets when `videoUrl` changes.
- Runtime/build verification is still pending.
