package com.kakaanime.app.data

object AnimeCatalog {

    private val repository: ProviderAnimeRepository by lazy {
        RepositoryFactory.createAnimeRepository()
    }

    val service: AnimeCatalogService by lazy {
        AnimeCatalogService(repository)
    }
}
