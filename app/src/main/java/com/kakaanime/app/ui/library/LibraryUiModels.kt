package com.kakaanime.app.ui.library

import com.kakaanime.app.ui.home.HomeAnimeUi

data class LibraryUiState(
    val favorites: List<HomeAnimeUi> = emptyList(),
    val searchQuery: String = "",
)
