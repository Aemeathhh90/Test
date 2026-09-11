package com.kakaanime.app.monetization

/**
 * Central V1 entitlement rules. UI and playback should depend on this model
 * instead of duplicating monetization checks.
 */
data class MonetizationState(
    val diamonds: Int = 0,
    val isPremium: Boolean = false
) {
    fun canWatchFreeEpisode(): Boolean = isPremium || diamonds > 0

    fun canUse1080p(): Boolean = isPremium

    fun canAutoSkip(): Boolean = isPremium

    fun canDownload(): Boolean = isPremium
}

object DiamondRules {
    const val DIAMONDS_PER_REWARDED_AD = 2
    const val DIAMONDS_PER_EPISODE = 1

    fun afterRewardedAd(state: MonetizationState): MonetizationState =
        state.copy(diamonds = state.diamonds + DIAMONDS_PER_REWARDED_AD)

    fun consumeForEpisode(state: MonetizationState): MonetizationState? {
        if (state.isPremium) return state
        if (state.diamonds < DIAMONDS_PER_EPISODE) return null
        return state.copy(diamonds = state.diamonds - DIAMONDS_PER_EPISODE)
    }
}

sealed interface WatchGate {
    data object Allowed : WatchGate
    data object PremiumRequired : WatchGate
    data object RewardedAdRequired : WatchGate
}

fun MonetizationState.watchGate(): WatchGate = when {
    isPremium -> WatchGate.Allowed
    diamonds > 0 -> WatchGate.Allowed
    else -> WatchGate.RewardedAdRequired
}
