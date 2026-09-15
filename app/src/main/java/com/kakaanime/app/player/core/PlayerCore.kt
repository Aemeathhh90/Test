package com.kakaanime.app.player.core

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.kakaanime.app.provider.NormalizedStream
import com.kakaanime.app.provider.StreamMetadataCache
import com.kakaanime.app.provider.StreamType

class PlayerCore(
    context: Context
) {

    val player: ExoPlayer = ExoPlayer.Builder(context).build()

    var isReady: Boolean = false
        private set
    var isPlaying: Boolean = false
        private set
    var hasError: Boolean = false
        private set
    var errorMessage: String? = null
        private set

    private val listener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            isReady = playbackState == Player.STATE_READY
            Log.d(TAG, "STREAM_CCTV_PLAYER_STATE state=$playbackState ready=$isReady")
        }
        override fun onIsPlayingChanged(playing: Boolean) {
            isPlaying = playing
            Log.d(TAG, "STREAM_CCTV_PLAYER_PLAYING playing=$playing")
        }
        override fun onPlayerError(error: PlaybackException) {
            hasError = true
            errorMessage = error.message
            Log.e(
                TAG,
                "STREAM_CCTV_PLAYER_ERROR code=${error.errorCodeName} " +
                    "message=${error.message ?: "none"} " +
                    "cause=${causeChain(error.cause)}"
            )
        }
    }

    init {
        player.addListener(listener)
    }

    fun setVideo(url: String) {
        val cached = StreamMetadataCache.find(url)
        Log.d(
            TAG,
            "STREAM_CCTV_PLAYER_INPUT metadata=${cached != null} type=${cached?.type ?: StreamType.UNKNOWN} " +
                "provider=${cached?.providerId ?: ""} url=${url.take(180)}"
        )
        val stream = cached ?: NormalizedStream(providerId = "", url = url)
        setVideo(stream)
    }

    fun setVideo(stream: NormalizedStream) {
        hasError = false
        errorMessage = null
        isReady = false

        Log.d(
            TAG,
            "STREAM_CCTV_PLAYER_MEDIA type=${stream.type} quality=${stream.quality} " +
                "provider=${stream.providerId} headers=${stream.headers.keys} url=${stream.url.take(180)}"
        )

        val mediaItem = MediaItem.Builder()
            .setUri(stream.url)
            .apply { mimeTypeFor(stream.type)?.let(::setMimeType) }
            .build()

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .apply {
                if (stream.headers.isNotEmpty()) {
                    setDefaultRequestProperties(stream.headers)
                }
            }
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
        val mediaSource = mediaSourceFactory.createMediaSource(mediaItem)

        player.setMediaSource(mediaSource)
        player.prepare()
        Log.d(TAG, "STREAM_CCTV_PLAYER_PREPARE type=${stream.type} url=${stream.url.take(180)}")
    }

    private fun mimeTypeFor(type: StreamType): String? = when (type) {
        StreamType.HLS -> MimeTypes.APPLICATION_M3U8
        StreamType.DASH -> MimeTypes.APPLICATION_MPD
        StreamType.MP4 -> MimeTypes.VIDEO_MP4
        StreamType.UNKNOWN -> null
    }

    private fun causeChain(cause: Throwable?): String =
        generateSequence(cause) { it.cause }
            .take(5)
            .joinToString(" -> ") { it::class.java.simpleName + ": " + (it.message ?: "") }
            .ifBlank { "none" }

    fun play() = player.play()
    fun pause() = player.pause()
    fun togglePlayPause() { if (player.isPlaying) pause() else play() }
    fun seekBack(milliseconds: Long = 10_000L) = player.seekTo((player.currentPosition - milliseconds).coerceAtLeast(0L))
    fun seekForward(milliseconds: Long = 10_000L) = player.seekTo((player.currentPosition + milliseconds).coerceAtMost(player.duration.coerceAtLeast(0L)))
    fun seekTo(position: Long) = player.seekTo(position.coerceAtLeast(0L))
    fun setSpeed(speed: Float) = player.setPlaybackParameters(PlaybackParameters(speed))
    fun release() { player.removeListener(listener); player.release() }

    private companion object {
        const val TAG = "KakaAnime-StreamCCTV"
    }
}
