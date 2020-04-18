/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel
 * Last modified 4/18/20 1:32 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geekstools.floatshort.PRO.Utils.InAppStore.DigitalAssets

import com.android.billingclient.api.BillingClient

class InAppBillingData {

    object SKU {
        val SKUS = HashMap<String, ArrayList<String>>()

        /**
         * One Time Purchase: Sku for Donation - Sku: donation
         **/
        const val InAppItemDonation = "donation"
        /**
         * One Time Purchase: Sku for Floating Widgets - Sku: floating.widgets
         **/
        const val InAppItemFloatingWidgets = "floating.widgets"
        /**
         * Subscription Purchase: Sku for Security Services - Sku: security.services
         **/
        const val InAppItemSecurityServices = "security.services"
        /**
         * Subscription Purchase: Sku for Search Engine - Sku: search.engines
         **/
        const val InAppItemSearchEngines = "search.engines"
    }

    init {
        InAppBillingData.SKU.SKUS[BillingClient.SkuType.INAPP] = arrayListOf(SKU.InAppItemDonation, SKU.InAppItemFloatingWidgets)
        InAppBillingData.SKU.SKUS[BillingClient.SkuType.SUBS] = arrayListOf(SKU.InAppItemSecurityServices, SKU.InAppItemSearchEngines)
    }

    /**
     * BillingClient.SkuType.INAPP
     * OR
     * BillingClient.SkuType.SUBS
     **/
    fun getAllSkusByType(@BillingClient.SkuType skuType: String) : ArrayList<String>? {

        return InAppBillingData.SKU.SKUS[skuType]
    }
}