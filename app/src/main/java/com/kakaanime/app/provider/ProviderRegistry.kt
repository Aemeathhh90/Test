package com.kakaanime.app.provider

class ProviderRegistry {

    private val providers =
        mutableListOf<AnimeProvider>()

    fun register(
        provider: AnimeProvider
    ) {

        if (
            providers.none {
                it.id == provider.id
            }
        ) {
            providers.add(provider)
        }
    }

    fun registerAll(
        items: List<AnimeProvider>
    ) {

        items.forEach {
            register(it)
        }
    }

    fun remove(
        providerId: String
    ) {

        providers.removeAll {
            it.id == providerId
        }
    }

    fun get(
        providerId: String
    ): AnimeProvider? {

        return providers.firstOrNull {
            it.id == providerId
        }
    }

    fun all(): List<AnimeProvider> {

        return providers
            .sortedBy {
                it.priority
            }
            .toList()
    }

    fun clear() {

        providers.clear()
    }
}
