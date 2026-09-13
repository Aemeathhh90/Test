package com.kakaanime.app.premium

import com.android.billingclient.api.ProductDetails

/**
 * KakaAnime V1 Premium plan presentation.
 * Prices are supplied by Google Play at runtime; these values are only UI labels.
 */
data class PremiumPlan(
    val basePlanId: String,
    val title: String,
    val fallbackPrice: String,
    val savingLabel: String?
)

val KakaPremiumPlans = listOf(
    PremiumPlan("monthly", "1 Bulan", "Harga di Google Play", null),
    PremiumPlan("quarterly", "3 Bulan", "Harga di Google Play", "Hemat 10%"),
    PremiumPlan("semiannual", "6 Bulan", "Harga di Google Play", "Hemat 15%"),
    PremiumPlan("annual", "12 Bulan", "Harga di Google Play", "Hemat 25%")
)

data class PremiumOffer(
    val plan: PremiumPlan,
    val productDetails: ProductDetails,
    val offerToken: String,
    val formattedPrice: String
)

sealed interface PremiumBillingState {
    data object Loading : PremiumBillingState
    data class Ready(val offers: List<PremiumOffer>) : PremiumBillingState
    data class Unavailable(val message: String) : PremiumBillingState
}
