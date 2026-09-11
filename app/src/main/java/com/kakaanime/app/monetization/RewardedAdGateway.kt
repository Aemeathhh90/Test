package com.kakaanime.app.monetization

/**
 * Adapter boundary for the rewarded-ad SDK.
 * The app must grant diamonds only after the SDK reports a completed reward.
 */
interface RewardedAdGateway {
    fun isReady(): Boolean
    fun show(onReward: (diamonds: Int) -> Unit, onUnavailable: () -> Unit)
}

class NoOpRewardedAdGateway : RewardedAdGateway {
    override fun isReady(): Boolean = false

    override fun show(onReward: (diamonds: Int) -> Unit, onUnavailable: () -> Unit) {
        onUnavailable()
    }
}
