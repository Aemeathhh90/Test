package com.kakaanime.app.provider

object ProviderFactory {

    fun createRegistry(): ProviderRegistry {
        return ProviderRegistry().apply {
            // Providers are registered independently so the router can
            // fall back when one source is unavailable.
            register(DemoProvider())
        }
    }

    fun createEngine(): ProviderEngine {
        return ProviderEngine(
            registry = createRegistry()
        )
    }
}
