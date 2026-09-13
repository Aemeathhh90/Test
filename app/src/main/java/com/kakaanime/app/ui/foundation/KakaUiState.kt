package com.kakaanime.app.ui.foundation

/** Shared async state model for feature screens. Keep business logic outside composables. */
sealed interface KakaUiState<out T> {
    data object Loading : KakaUiState<Nothing>
    data class Success<T>(val data: T) : KakaUiState<T>
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : KakaUiState<Nothing>
}
