package com.matjard.core

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Url
import okhttp3.ResponseBody

interface GoTrueApi {
    @POST("auth/v1/token?grant_type=password")
    suspend fun signIn(@Body body: SignInRequest): AuthResponse

    @POST("auth/v1/signup")
    suspend fun signUp(@Body body: SignUpRequest): AuthResponse

    @POST("auth/v1/token?grant_type=refresh_token")
    suspend fun refresh(@Body body: RefreshRequest): AuthResponse
}

interface PostgrestApi {
    // ---- catalog ----------------------------------------------------------
    @GET("rest/v1/categories")
    suspend fun listCategories(@QueryMap filters: Map<String, String>): List<Category>

    @GET("rest/v1/products")
    suspend fun listProducts(@QueryMap filters: Map<String, String>): List<Product>

    @GET("rest/v1/banners")
    suspend fun listBanners(@QueryMap filters: Map<String, String>): List<Banner>

    @GET("rest/v1/store_settings")
    suspend fun getSettings(@QueryMap filters: Map<String, String>): List<StoreSettings>

    // ---- admin catalog writes ---------------------------------------------
    @POST("rest/v1/categories")
    suspend fun createCategory(
        @Body fields: Map<String, @JvmSuppressWildcards Any?>,
        @Header("Prefer") prefer: String = "return=minimal",
    ): Response<ResponseBody>

    @PATCH("rest/v1/categories")
    suspend fun updateCategory(
        @Query("id") idFilter: String,
        @Body fields: Map<String, @JvmSuppressWildcards Any?>,
    ): Response<ResponseBody>

    @DELETE("rest/v1/categories")
    suspend fun deleteCategory(@Query("id") idFilter: String): Response<ResponseBody>

    @POST("rest/v1/products")
    suspend fun createProduct(
        @Body fields: Map<String, @JvmSuppressWildcards Any?>,
        @Header("Prefer") prefer: String = "return=minimal",
    ): Response<ResponseBody>

    @PATCH("rest/v1/products")
    suspend fun updateProduct(
        @Query("id") idFilter: String,
        @Body fields: Map<String, @JvmSuppressWildcards Any?>,
    ): Response<ResponseBody>

    @DELETE("rest/v1/products")
    suspend fun deleteProduct(@Query("id") idFilter: String): Response<ResponseBody>

    @POST("rest/v1/banners")
    suspend fun createBanner(
        @Body fields: Map<String, @JvmSuppressWildcards Any?>,
        @Header("Prefer") prefer: String = "return=minimal",
    ): Response<ResponseBody>

    @PATCH("rest/v1/banners")
    suspend fun updateBanner(
        @Query("id") idFilter: String,
        @Body fields: Map<String, @JvmSuppressWildcards Any?>,
    ): Response<ResponseBody>

    @DELETE("rest/v1/banners")
    suspend fun deleteBanner(@Query("id") idFilter: String): Response<ResponseBody>

    @PATCH("rest/v1/store_settings")
    suspend fun updateSettings(
        @Query("id") idFilter: String,
        @Body fields: Map<String, @JvmSuppressWildcards Any?>,
    ): Response<ResponseBody>

    // ---- admin orders -------------------------------------------------------
    @GET("rest/v1/orders")
    suspend fun listOrders(@QueryMap filters: Map<String, String>): List<Order>

    @GET("rest/v1/order_items")
    suspend fun listOrderItems(@QueryMap filters: Map<String, String>): List<OrderItem>

    @PATCH("rest/v1/orders")
    suspend fun updateOrderStatus(
        @Query("id") idFilter: String,
        @Body fields: Map<String, @JvmSuppressWildcards Any?>,
    ): Response<ResponseBody>

    // ---- admin membership probe ---------------------------------------------
    @GET("rest/v1/admin_users")
    suspend fun adminMembership(@Query("select") select: String = "id", @Query("limit") limit: String = "1"): List<AdminUserRow>

    // ---- admin users (roles) -------------------------------------------------
    @GET("rest/v1/admin_users")
    suspend fun listAdminUsers(@Query("select") select: String = "*"): List<AdminUser>

    @POST("rest/v1/admin_users")
    suspend fun createAdminUser(
        @Body fields: Map<String, @JvmSuppressWildcards Any?>,
        @Header("Prefer") prefer: String = "return=representation",
    ): Response<ResponseBody>

    @PATCH("rest/v1/admin_users")
    suspend fun updateAdminUserRole(
        @Query("id") idFilter: String,
        @Body fields: Map<String, @JvmSuppressWildcards Any?>,
    ): Response<ResponseBody>

    @DELETE("rest/v1/admin_users")
    suspend fun deleteAdminUser(@Query("id") idFilter: String): Response<ResponseBody>

    // ---- RPC: make first user owner ------------------------------------------
    @POST("rest/v1/rpc/set_owner_if_first")
    suspend fun setOwnerIfFirst(@Body body: RoleRequest): Response<RoleResult>

    // ---- RPCs ----------------------------------------------------------------
    @POST("rest/v1/rpc/place_order")
    suspend fun placeOrder(@Body body: PlaceOrderRequest): Response<PlaceOrderResult>

    @POST("rest/v1/rpc/track_order")
    suspend fun trackOrder(@Body body: TrackOrderRequest): Response<TrackOrderResult>
}

// طلب تعيين رتبة أول مستخدم
data class RoleRequest(@SerializedName("p_role") val role: String)
data class RoleResult(@SerializedName("role") val role: String)

interface StorageApi {
    @POST("storage/v1/object/product-images/{file}")
    suspend fun uploadProductImage(
        @Path("file") file: String,
        @Header("Content-Type") contentType: String,
        @Header("x-upsert") upsert: String = "true",
        @Body body: RequestBody,
    ): Response<ResponseBody>
}

/**
 * Builds the Retrofit services for the Matjar D backend. The [tokenProvider]
 * is evaluated on every request so sign-in/sign-out takes effect immediately.
 */
class ApiProvider(private val tokenProvider: () -> String? = { null }) {

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                    .header("apikey", Supabase.PUBLISHABLE_KEY)
                tokenProvider()?.let { builder.header("Authorization", "Bearer $it") }
                chain.proceed(builder.build())
            }
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Supabase.PROJECT_URL + "/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    val gotrue: GoTrueApi by lazy { retrofit.create(GoTrueApi::class.java) }
    val rest: PostgrestApi by lazy { retrofit.create(PostgrestApi::class.java) }
    val storage: StorageApi by lazy { retrofit.create(StorageApi::class.java) }
}
