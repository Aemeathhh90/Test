package com.kakaanime.app.data

import com.kakaanime.app.provider.ProviderFactory

object RepositoryFactory {

    fun createAnimeRepository(): ProviderAnimeRepository {
        val engine = ProviderFactory.createEngine()
        return ProviderAnimeRepository(engine)
    }
}
