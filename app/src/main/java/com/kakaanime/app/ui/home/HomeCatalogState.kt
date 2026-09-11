package com.kakaanime.app.ui.home

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.kakaanime.app.data.AnimeCatalog
import com.kakaanime.app.data.AnimeData

@Immutable
data class HomeCatalogState(
    val items: List<AnimeData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeCatalogController {

    var state by mutableStateOf(HomeCatalogState())
        private set

    suspend fun load() {
        state = state.copy(
            isLoading = true,
            error = null
        )

        try {
            val result = AnimeCatalog.service.getLatestUpdates()

            state = HomeCatalogState(
                items = result,
                isLoading = false,
                error = null
            )
        } catch (e: Exception) {
            state = HomeCatalogState(
                items = emptyList(),
                isLoading = false,
                error = e.message ?: "Gagal mengambil data anime"
            )
        }
    }

    suspend fun search(query: String) {
        if (query.isBlank()) {
            load()
            return
        }

        state = state.copy(
            isLoading = true,
            error = null
        )

        try {
            val result = AnimeCatalog.service.search(query)

            state = HomeCatalogState(
                items = result,
                isLoading = false,
                error = null
            )
        } catch (e: Exception) {
            state = state.copy(
                isLoading = false,
                error = e.message ?: "Pencarian gagal"
            )
        }
    }
}
