package com.nfcresenas.app.ui

import android.net.Uri
import com.nfcresenas.app.data.Business

/**
 * Public landing-page URL encoded into the business QR.
 * The GitHub Pages site is deployed separately from the Android app.
 */
object PublicLandingUrl {
    private const val BASE_URL = "https://laloma381-cmyk.github.io/NFC-Resenas-Android/"

    fun forBusiness(business: Business): String {
        return Uri.parse(BASE_URL).buildUpon()
            .appendQueryParameter("name", business.name)
            .appendQueryParameter("category", business.category)
            .appendQueryParameter("address", business.address)
            .appendQueryParameter("phone", business.phone)
            .appendQueryParameter("review", business.googleReviewUrl)
            .appendQueryParameter("instagram", business.instagramUrl)
            .appendQueryParameter("facebook", business.facebookUrl)
            .build()
            .toString()
    }
}
