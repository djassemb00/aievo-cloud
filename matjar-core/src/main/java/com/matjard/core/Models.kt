package com.matjard.core

import com.google.gson.annotations.SerializedName

// ---------------------------------------------------------------------------
// Catalog
// ---------------------------------------------------------------------------

data class Category(
    val id: String,
    @SerializedName("name_ar") val nameAr: String,
    @SerializedName("name_fr") val nameFr: String,
    val icon: String = "devices",
    @SerializedName("sort_order") val sortOrder: Int = 0,
    @SerializedName("is_active") val isActive: Boolean = true,
) {
    fun name(lang: String) = if (lang == "fr") nameFr else nameAr
}

data class Product(
    val id: String,
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("name_ar") val nameAr: String,
    @SerializedName("name_fr") val nameFr: String,
    @SerializedName("description_ar") val descriptionAr: String? = null,
    @SerializedName("description_fr") val descriptionFr: String? = null,
    val brand: String? = null,
    val sku: String? = null,
    val price: Double,
    @SerializedName("old_price") val oldPrice: Double? = null,
    val stock: Int = 0,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("is_featured") val isFeatured: Boolean = false,
    @SerializedName("is_active") val isActive: Boolean = true,
) {
    fun name(lang: String) = if (lang == "fr") nameFr else nameAr
    fun description(lang: String) = if (lang == "fr") descriptionFr else descriptionAr
    fun discountPercent(): Int? =
        oldPrice?.takeIf { it > price && it > 0 }?.let { ((1 - price / it) * 100).toInt() }
}

data class Banner(
    val id: String,
    @SerializedName("title_ar") val titleAr: String,
    @SerializedName("title_fr") val titleFr: String,
    @SerializedName("subtitle_ar") val subtitleAr: String? = null,
    @SerializedName("subtitle_fr") val subtitleFr: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("sort_order") val sortOrder: Int = 0,
    @SerializedName("is_active") val isActive: Boolean = true,
) {
    fun title(lang: String) = if (lang == "fr") titleFr else titleAr
    fun subtitle(lang: String) = if (lang == "fr") subtitleFr else subtitleAr
}

data class StoreSettings(
    val id: Int = 1,
    @SerializedName("store_name_ar") val storeNameAr: String,
    @SerializedName("store_name_fr") val storeNameFr: String,
    val phone: String,
    val whatsapp: String? = null,
    val email: String? = null,
    @SerializedName("address_ar") val addressAr: String? = null,
    @SerializedName("address_fr") val addressFr: String? = null,
    @SerializedName("hours_ar") val hoursAr: String? = null,
    @SerializedName("hours_fr") val hoursFr: String? = null,
    @SerializedName("delivery_fee") val deliveryFee: Double = 400.0,
    @SerializedName("free_delivery_min") val freeDeliveryMin: Double = 50000.0,
    val currency: String = "DZD",
    @SerializedName("logo_url") val logoUrl: String? = null,
    @SerializedName("facebook_url") val facebookUrl: String? = null,
    @SerializedName("instagram_url") val instagramUrl: String? = null,
    @SerializedName("tiktok_url") val tiktokUrl: String? = null,
) {
    fun storeName(lang: String) = if (lang == "fr") storeNameFr else storeNameAr
    fun address(lang: String) = if (lang == "fr") addressFr else addressAr
    fun hours(lang: String) = if (lang == "fr") hoursFr else hoursAr
}

// ---------------------------------------------------------------------------
// Orders
// ---------------------------------------------------------------------------

data class Order(
    val id: String,
    @SerializedName("order_number") val orderNumber: Long,
    @SerializedName("customer_name") val customerName: String,
    @SerializedName("customer_phone") val customerPhone: String,
    val wilaya: String,
    val address: String,
    val note: String? = null,
    val status: String, // new | preparing | out_for_delivery | delivered | cancelled
    @SerializedName("delivery_fee") val deliveryFee: Double,
    val total: Double,
    @SerializedName("created_at") val createdAt: String,
)

data class OrderItem(
    val id: Long? = null,
    @SerializedName("order_id") val orderId: String,
    @SerializedName("product_id") val productId: String? = null,
    @SerializedName("product_name") val productName: String,
    @SerializedName("unit_price") val unitPrice: Double,
    val quantity: Int,
)

// ---------------------------------------------------------------------------
// place_order RPC (transactional, computes pricing server-side)
// ---------------------------------------------------------------------------

data class CartItemDto(
    @SerializedName("product_id") val productId: String,
    val quantity: Int,
)

data class PlaceOrderRequest(
    @SerializedName("p_customer_name") val customerName: String,
    @SerializedName("p_customer_phone") val customerPhone: String,
    @SerializedName("p_wilaya") val wilaya: String,
    @SerializedName("p_address") val address: String,
    @SerializedName("p_note") val note: String?,
    @SerializedName("p_items") val items: List<CartItemDto>,
)

data class PlaceOrderResult(
    @SerializedName("order_id") val orderId: String,
    @SerializedName("order_number") val orderNumber: Long,
    val subtotal: Double,
    @SerializedName("delivery_fee") val deliveryFee: Double,
    val total: Double,
)

// ---------------------------------------------------------------------------
// track_order RPC (order number + phone)
// ---------------------------------------------------------------------------

data class TrackOrderRequest(
    @SerializedName("p_order_number") val orderNumber: Long,
    @SerializedName("p_phone") val phone: String,
)

data class TrackedItem(
    val name: String,
    val qty: Int,
    @SerializedName("unit_price") val unitPrice: Double,
)

data class TrackOrderResult(
    @SerializedName("order_number") val orderNumber: Long,
    val status: String,
    val total: Double,
    @SerializedName("delivery_fee") val deliveryFee: Double,
    @SerializedName("created_at") val createdAt: String,
    val wilaya: String,
    val items: List<TrackedItem>,
)

// ---------------------------------------------------------------------------
// Supabase Auth (admin app)
// ---------------------------------------------------------------------------

data class SignInRequest(val email: String, val password: String)

/** طلب إنشاء حساب مسؤول جديد (sign-up عبر Supabase GoTrue). */
data class SignUpRequest(val email: String, val password: String)

data class RefreshRequest(@SerializedName("refresh_token") val refreshToken: String)

data class UserInfo(val id: String, val email: String? = null)

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("expires_in") val expiresIn: Long,
    @SerializedName("token_type") val tokenType: String? = null,
    val user: UserInfo? = null,
)

data class AdminUserRow(val id: String)

/** مستخدم مسؤول مع رتبته وصلاحياته. */
data class AdminUser(
    val id: String,
    val email: String?,
    @SerializedName("role") val role: String, // owner | manager | staff
)
