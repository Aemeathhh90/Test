package com.kakaanime.app.provider

object ProviderFactory {

    fun createRegistry(): ProviderRegistry {

        return ProviderRegistry().apply {

            register(
                DemoProvider()
            )
        }
    }

    fun createEngine(): ProviderEngine {

        return ProviderEngine(
            registry = createRegistry()
        )
    }
}
