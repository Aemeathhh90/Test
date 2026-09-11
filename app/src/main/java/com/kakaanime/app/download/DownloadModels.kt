package com.kakaanime.app.download

enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class DownloadItem(
    val animeId: String,
    val episode: Int,
    val title: String,
    val localPath: String? = null,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val progress: Float = 0f,
    val quality: Int = 720,
    val createdAt: Long = 0L
)

/** Offline playback must only resolve app-managed local files. */
fun DownloadItem.isPlayableOffline(): Boolean =
    status == DownloadStatus.COMPLETED && !localPath.isNullOrBlank()
