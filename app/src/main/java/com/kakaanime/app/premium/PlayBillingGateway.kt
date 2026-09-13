package com.kakaanime.app.premium

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

/**
 * Thin V1 bridge between KakaAnime Premium UI and Google Play Billing.
 *
 * Product configuration belongs in Play Console:
 * productId = premium
 * base plans = monthly / quarterly / semiannual / annual
 *
 * Entitlement verification should move to the secure backend before production release.
 */
class PlayBillingGateway(
    private val activity: Activity,
    private val onPremiumEntitled: () -> Unit,
    private val onBillingState: (PremiumBillingState) -> Unit,
    private val onMessage: (String) -> Unit
) {
    companion object {
        const val PREMIUM_PRODUCT_ID = "premium"
    }

    private val billingClient = BillingClient.newBuilder(activity)
        .setListener { billingResult, purchases ->
            handlePurchases(billingResult, purchases.orEmpty())
        }
        .enablePendingPurchases()
        .build()

    fun connectAndLoad() {
        if (billingClient.isReady) {
            queryProducts()
            queryOwnedSubscriptions()
            return
        }

        onBillingState(PremiumBillingState.Loading)
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProducts()
                    queryOwnedSubscriptions()
                } else {
                    onBillingState(
                        PremiumBillingState.Unavailable(
                            billingResult.debugMessage.ifBlank { "Google Play Billing belum tersedia." }
                        )
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                onBillingState(
                    PremiumBillingState.Unavailable("Koneksi Google Play terputus. Coba lagi.")
                )
            }
        })
    }

    fun launchPurchase(basePlanId: String) {
        if (!billingClient.isReady) {
            onMessage("Google Play belum siap. Coba lagi sebentar.")
            connectAndLoad()
            return
        }

        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(PREMIUM_PRODUCT_ID)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    )
                )
                .build()
        ) { result, queryResult ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                onMessage(result.debugMessage.ifBlank { "Paket Premium belum tersedia." })
                return@queryProductDetailsAsync
            }

            val product = queryResult.productDetailsList.firstOrNull()
            val offer = product?.subscriptionOfferDetails
                ?.firstOrNull { it.basePlanId == basePlanId }

            if (product == null || offer == null) {
                onMessage("Paket ini belum tersedia di Google Play.")
                return@queryProductDetailsAsync
            }

            val params = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(product)
                            .setOfferToken(offer.offerToken)
                            .build()
                    )
                )
                .build()

            billingClient.launchBillingFlow(activity, params)
        }
    }

    fun refresh() {
        connectAndLoad()
    }

    fun destroy() {
        billingClient.endConnection()
    }

    private fun queryProducts() {
        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(PREMIUM_PRODUCT_ID)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    )
                )
                .build()
        ) { result, queryResult ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                onBillingState(
                    PremiumBillingState.Unavailable(
                        result.debugMessage.ifBlank { "Paket Premium belum tersedia." }
                    )
                )
                return@queryProductDetailsAsync
            }

            val product = queryResult.productDetailsList.firstOrNull()
            if (product == null) {
                onBillingState(
                    PremiumBillingState.Unavailable("Premium belum dikonfigurasi di Google Play Console.")
                )
                return@queryProductDetailsAsync
            }

            val offers = product.subscriptionOfferDetails.orEmpty().mapNotNull { offer ->
                val plan = KakaPremiumPlans.firstOrNull { it.basePlanId == offer.basePlanId }
                    ?: return@mapNotNull null
                val phase = offer.pricingPhases.pricingPhaseList.lastOrNull()
                PremiumOffer(
                    plan = plan,
                    productDetails = product,
                    offerToken = offer.offerToken,
                    formattedPrice = phase?.formattedPrice ?: plan.fallbackPrice
                )
            }

            if (offers.isEmpty()) {
                onBillingState(PremiumBillingState.Unavailable("Belum ada paket Premium yang aktif di Google Play."))
            } else {
                onBillingState(PremiumBillingState.Ready(offers))
            }
        }
    }

    private fun queryOwnedSubscriptions() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(result, purchases)
            }
        }
    }

    private fun handlePurchases(result: BillingResult, purchases: List<Purchase>) {
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            if (result.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
                onMessage(result.debugMessage.ifBlank { "Pembayaran Google Play gagal diproses." })
            }
            return
        }

        purchases
            .filter { PREMIUM_PRODUCT_ID in it.products }
            .forEach { purchase ->
                when (purchase.purchaseState) {
                    Purchase.PurchaseState.PURCHASED -> {
                        acknowledgeIfNeeded(purchase)
                        onPremiumEntitled()
                    }
                    Purchase.PurchaseState.PENDING -> {
                        onMessage("Pembayaran masih menunggu konfirmasi Google Play.")
                    }
                }
            }
    }

    private fun acknowledgeIfNeeded(purchase: Purchase) {
        if (purchase.isAcknowledged) return

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { result ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                onMessage("Pembayaran sudah diterima, tetapi konfirmasi Google Play perlu dicoba lagi.")
            }
        }
    }
}
