package com.insecureshop

import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class ProductDetail(
    var id: Int,
    var name: String,
    var imageUrl: String?,
    var price: String,
    var rating: Int,
    var qty: Int = 0
) {
    val url: String
        get() = Config.WEBSITE_DOMAIN
}