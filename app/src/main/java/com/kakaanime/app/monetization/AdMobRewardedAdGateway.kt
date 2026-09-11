package com.kakaanime.app.monetization

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * AdMob rewarded adapter for the Diamond economy.
 *
 * Test rewarded unit is used until the real AniLab AdMob unit ID is supplied.
 * A reward is granted only from onUserEarnedReward().
 */
class AdMobRewardedAdGateway(
    context: Context,
    private val rewardedAdUnitId: String = TEST_REWARDED_AD_UNIT_ID,
    private val rewardDiamonds: Int = 2
) : RewardedAdGateway {

    companion object {
        const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    }

    private val activityContext = context
    private val appContext = context.applicationContext
    private var rewardedAd: RewardedAd? = null
    private var loading = false

    init {
        MobileAds.initialize(appContext) {
            preload()
        }
    }

    override fun isReady(): Boolean = rewardedAd != null

    override fun show(onReward: (diamonds: Int) -> Unit, onUnavailable: () -> Unit) {
        val activity = findActivity(activityContext)
        val ad = rewardedAd

        if (activity == null || ad == null) {
            preload()
            onUnavailable()
            return
        }

        rewardedAd = null
        ad.show(activity) { _: RewardItem ->
            onReward(rewardDiamonds)
        }
        preload()
    }

    private fun preload() {
        if (loading || rewardedAd != null) return
        loading = true
        RewardedAd.load(
            appContext,
            rewardedAdUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    loading = false
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loading = false
                    rewardedAd = null
                }
            }
        )
    }

    private fun findActivity(context: Context): Activity? {
        var current = context
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return current as? Activity
    }
}
