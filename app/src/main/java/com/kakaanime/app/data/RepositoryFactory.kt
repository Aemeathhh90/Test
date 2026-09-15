package com.kakaanime.app.data

import com.kakaanime.provider.ProviderFactory

object RepositoryFactory {
    fun createAnimeRepository(): ProviderAnimeRepository {
        return ProviderAnimeRepository(ProviderFactory.createEngine())
    }
}
