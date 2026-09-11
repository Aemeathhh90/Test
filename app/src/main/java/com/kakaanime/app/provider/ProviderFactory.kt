package com.kakaanime.app.provider

object ProviderFactory {

    fun createRegistry(): ProviderRegistry {
        return ProviderRegistry().apply {
            // Real providers are registered independently so the router can
            // fail over without coupling provider logic to the player.
            register(OtakudesuProvider())
        }
    }

    fun createEngine(): ProviderEngine {
        return ProviderEngine(
            registry = createRegistry()
        )
    }
}
