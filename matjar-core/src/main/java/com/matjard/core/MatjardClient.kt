package com.matjard.core

import android.content.Context
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.HttpException
import java.io.IOException

/**
 * One entry point for everything both apps do with the Matjar D backend.
 * Construct once per process: `val client = MatjardClient(context)`.
 *
 * Reads throw [IOException]/[HttpException] on failure — callers catch and
 * surface a message. Admin writes also throw on non-2xx responses with the
 * PostgREST message when available.
 */
class MatjardClient(context: Context) {

    val tokens = TokenStore(context.applicationContext)
    val language = LanguageStore(context.applicationContext)
    private val api = ApiProvider { tokens.accessToken }

    // ------------------------------------------------------------------
    // Catalog reads (public)
    // ------------------------------------------------------------------

    suspend fun categories(includeInactive: Boolean = false): List<Category> {
        val filters = mutableMapOf(
            "select" to "*",
            "order" to "sort_order.asc",
        )
        if (!includeInactive) filters["is_active"] = "eq.true"
        return api.rest.listCategories(filters)
    }

    suspend fun products(
        categoryId: String? = null,
        featuredOnly: Boolean = false,
        search: String? = null,
        includeInactive: Boolean = false,
    ): List<Product> {
        val filters = mutableMapOf(
            "select" to "*",
            "order" to "created_at.desc",
        )
        if (!includeInactive) filters["is_active"] = "eq.true"
        categoryId?.let { filters["category_id"] = "eq.$it" }
        if (featuredOnly) filters["is_featured"] = "eq.true"
        search?.trim()?.takeIf { it.isNotEmpty() }?.let { q ->
            val needle = q.replace("*", "")
            filters["or"] = "(name_ar.ilike.*$needle*,name_fr.ilike.*$needle*,brand.ilike.*$needle*)"
        }
        return api.rest.listProducts(filters)
    }

    suspend fun featuredProducts(): List<Product> = products(featuredOnly = true)

    suspend fun product(id: String): Product? =
        api.rest.listProducts(mapOf("id" to "eq.$id", "select" to "*")).firstOrNull()

    suspend fun banners(includeInactive: Boolean = false): List<Banner> {
        val filters = mutableMapOf(
            "select" to "*",
            "order" to "sort_order.asc",
        )
        if (!includeInactive) filters["is_active"] = "eq.true"
        return api.rest.listBanners(filters)
    }

    suspend fun settings(): StoreSettings {
        return api.rest.getSettings(mapOf("id" to "eq.1", "select" to "*")).firstOrNull()
            ?: error("store_settings row missing — run the matjar_d_schema migration")
    }

    // ------------------------------------------------------------------
    // Customer flows
    // ------------------------------------------------------------------

    suspend fun placeOrder(
        customerName: String,
        customerPhone: String,
        wilaya: String,
        address: String,
        note: String?,
        items: List<CartItemDto>,
    ): PlaceOrderResult {
        val response = api.rest.placeOrder(
            PlaceOrderRequest(customerName, customerPhone, wilaya, address, note, items)
        )
        if (!response.isSuccessful) throw IOException(errorMessage(response))
        return response.body() ?: throw IOException("Empty response from place_order")
    }

    suspend fun trackOrder(orderNumber: Long, phone: String): TrackOrderResult? {
        val response = api.rest.trackOrder(TrackOrderRequest(orderNumber, phone))
        if (!response.isSuccessful) throw IOException(errorMessage(response))
        return response.body()
    }

    // ------------------------------------------------------------------
    // Admin auth
    // ------------------------------------------------------------------

    /** true when a session exists and the token is valid (refreshing if needed). */
    suspend fun ensureFreshToken(): Boolean {
        if (!tokens.hasSession()) return false
        if (tokens.expiresAtMillis > System.currentTimeMillis() + 60_000) return true
        val refresh = tokens.refreshToken ?: return false
        return try {
            val auth = api.gotrue.refresh(RefreshRequest(refresh))
            tokens.save(auth)
            true
        } catch (e: Exception) {
            tokens.clear()
            false
        }
    }

    /** Throws IOException with a user-readable message on failure. */
    suspend fun signIn(email: String, password: String): AuthResponse {
        val auth = try {
            api.gotrue.signIn(SignInRequest(email.trim(), password))
        } catch (e: HttpException) {
            // 4xx => bad credentials; anything else => generic failure
            throw IOException(if (e.code() in 400..499) "invalidCredentials" else "loginError")
        }
        tokens.save(auth)
        return auth
    }

    /** ينشئ حساب responsble جديد في Supabase Auth. يحفظ الجلسة إذا أعيدت (تأكيد تلقائي مغلق). */
    suspend fun signUp(email: String, password: String): AuthResponse {
        val auth = try {
            api.gotrue.signUp(SignUpRequest(email.trim(), password))
        } catch (e: HttpException) {
            throw IOException(if (e.code() in 400..499) "signUpError" else "signUpError")
        }
        tokens.save(auth)
        return auth
    }

    fun signOut() = tokens.clear()

    fun signedIn(): Boolean = tokens.hasSession()

    /** Probes admin_users (RLS: a user sees at most their own row). */
    suspend fun isAdmin(): Boolean = try {
        ensureFreshToken() && api.rest.adminMembership(select = "id", limit = "1").isNotEmpty()
    } catch (e: Exception) {
        false
    }

    // ------------------------------------------------------------------
    // Admin users & roles
    // ------------------------------------------------------------------

    /** كل المسؤولين مع رتبهم (يراها كل مستخدم لنفسه فقط عبر RLS، لكن المالك يرى الكل إذا خُفّفت السياسة). */
    suspend fun listAdminUsers(): List<AdminUser> =
        api.rest.listAdminUsers(select = "id, email, full_name, phone, hire_date, salary, address, role")

    /** ينشئ حساب موظف جديد مع معلومات شخصية كاملة. */
    suspend fun addAdminUser(
        email: String,
        password: String,
        role: String,
        fullName: String? = null,
        phone: String? = null,
        hireDate: String? = null,
        salary: Double? = null,
        address: String? = null,
    ) {
        val auth = try {
            api.gotrue.signUp(SignUpRequest(email.trim(), password))
        } catch (e: HttpException) {
            throw IOException(if (e.code() in 400..499) "signUpError" else "signUpError")
        }
        val userId = auth.user?.id ?: throw IOException("لم يتم استلام معرّف المستخدم من Supabase")
        val fields = mutableMapOf<String, Any?>(
            "id" to userId,
            "email" to email,
            "role" to role,
        )
        fullName?.let { fields["full_name"] = it }
        phone?.let { fields["phone"] = it }
        hireDate?.let { fields["hire_date"] = it }
        salary?.let { fields["salary"] = it }
        address?.let { fields["address"] = it }
        ensureOk(api.rest.createAdminUser(fields), "createAdminUser")
    }

    /** يُحدّث معلومات موظف (الرتبة والمعلومات الشخصية). */
    suspend fun updateAdminUserProfile(
        userId: String,
        role: String?,
        fullName: String?,
        phone: String?,
        hireDate: String?,
        salary: Double?,
        address: String?,
    ) {
        val fields = mutableMapOf<String, Any?>()
        role?.let { fields["role"] = it }
        fullName?.let { fields["full_name"] = it }
        phone?.let { fields["phone"] = it }
        hireDate?.let { fields["hire_date"] = it }
        salary?.let { fields["salary"] = it }
        address?.let { fields["address"] = it }
        if (fields.isNotEmpty()) {
            ensureOk(api.rest.updateAdminUserRole("eq.$userId", fields), "updateAdminUserProfile")
        }
    }

    /** يُحدّث رتبة مسؤول. */
    suspend fun updateAdminUserRole(userId: String, role: String) =
        ensureOk(api.rest.updateAdminUserRole("eq.$userId", mapOf("role" to role)), "updateAdminUserRole")

    /** يحذف مسؤول من القائمة. */
    suspend fun deleteAdminUser(userId: String) =
        ensureOk(api.rest.deleteAdminUser("eq.$userId"), "deleteAdminUser")

    /** إذا كان جدول admin_users فارغاً، يصنع المستخدم الحالي مالكاً (owner) ويعيد رتبته. */
    suspend fun ensureOwnerIfFirst(): String {
        return try {
            val resp = api.rest.setOwnerIfFirst(RoleRequest("owner"))
            if (!resp.isSuccessful) throw IOException(errorMessage(resp))
            resp.body()?.role ?: "staff"
        } catch (e: Exception) {
            "staff"
        }
    }

    /** يتحقق مما إذا وجد أي مسؤول مسجل في جدول admin_users (لإظهار/إخفاء زر التسجيل الذاتي). */
    suspend fun anyAdminExists(): Boolean = try {
        api.rest.listAdminUsers(select = "id").isNotEmpty()
    } catch (e: Exception) {
        false
    }

    /** رتبة المستخدم الحالي (يُجلب من admin_users). */
    suspend fun currentUserRole(): String? {
        return try {
            val me = tokens.accessToken ?: return null
            // Decode JWT payload to get user id, then look up role
            val parts = me.split(".")
            if (parts.size < 2) return null
            val payload = parts[1]
            val json = String(java.util.Base64.getDecoder().decode(payload))
            val obj = com.google.gson.JsonParser.parseString(json).asJsonObject
            val uid = obj.get("sub")?.asString ?: return null
            val users = api.rest.listAdminUsers(select = "id, role").filter { it.id == uid }
            users.firstOrNull()?.role
        } catch (e: Exception) {
            null
        }
    }

    // ------------------------------------------------------------------
    // Email OTP & Notifications (via Supabase Edge Functions)
    // ------------------------------------------------------------------

    /** يرسل رمز مصادقة بريد إلكتروني (6 أرقام) ينتهي بعد دقيقة واحدة عبر Edge Function. */
    suspend fun sendLoginOTP(email: String) {
        val result = try {
            invokeEdgeFunction("send-otp", mapOf("email" to email, "purpose" to "login"))
        } catch (e: Exception) {
            throw IOException("تعذر إرسال رمز المصادقة: ${e.message}")
        }
        if (result != true) throw IOException("فشل إرسال رمز المصادقة")
    }

    /** يتحقق من رمز المصادقة المرسل للبريد الإلكتروني عبر Edge Function. */
    suspend fun verifyLoginOTP(email: String, code: String): Boolean {
        return try {
            invokeEdgeFunction("verify-otp", mapOf("email" to email, "code" to code)) == true
        } catch (e: Exception) {
            false
        }
    }

    /** يرسل إشعاراً للمسؤول (يُخزّن في جدول الإشعارات). */
    suspend fun sendAdminNotification(title: String, message: String, type: String = "info") {
        try {
            val resp = api.rest.createAdminNotification(CreateAdminNotificationRequest(title, message, type))
            if (!resp.isSuccessful) throw IOException("فشل إرسال الإشعار")
        } catch (e: Exception) {
            // الإشعارات غير ضرورية لسير العمل الأساسي
        }
    }

    /** يجلب الإشعارات غير المقروءة للمسؤول. */
    suspend fun listAdminNotifications(): List<AdminNotification> {
        return try {
            api.rest.listAdminNotifications(select = "*", order = "created_at.desc")
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** يحدد إشعاراً كمقروء. */
    suspend fun markNotificationRead(id: String) =
        ensureOk(api.rest.updateAdminNotification("eq.$id", mapOf("is_read" to true)), "markNotificationRead")

    // ------------------------------------------------------------------
    // Edge Function helper
    // ------------------------------------------------------------------

    /** يستدعي دالة Edge Function في ترسل الطلب عبر HTTP ويحلل الاستجابة كـ JSON. */
    private suspend fun invokeEdgeFunction(slug: String, body: Map<String, Any?>): Any? {
        val url = "${Supabase.PROJECT_URL}/functions/v1/$slug"
        val jsonBody = com.google.gson.Gson().toJson(body)
        val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())
        val request = okhttp3.Request.Builder()
            .url(url)
            .header("apikey", Supabase.PUBLISHABLE_KEY)
            .header("Authorization", "Bearer ${Supabase.PUBLISHABLE_KEY}")
            .header("Content-Type", "application/json")
            .post(requestBody)
            .build()
        val client = okhttp3.OkHttpClient()
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""
        response.close()
        if (!response.isSuccessful) throw IOException("HTTP ${response.code}: $responseBody")
        return try {
            com.google.gson.JsonParser.parseString(responseBody).asJsonObject.get("success")?.asBoolean
        } catch (e: Exception) {
            null
        }
    }

    // ------------------------------------------------------------------
    // Admin catalog writes
    // ------------------------------------------------------------------

    suspend fun createCategory(fields: Map<String, Any?>) =
        ensureOk(api.rest.createCategory(fields), "createCategory")

    suspend fun updateCategory(id: String, fields: Map<String, Any?>) =
        ensureOk(api.rest.updateCategory("eq.$id", fields), "updateCategory")

    suspend fun deleteCategory(id: String) =
        ensureOk(api.rest.deleteCategory("eq.$id"), "deleteCategory")

    suspend fun createProduct(fields: Map<String, Any?>) =
        ensureOk(api.rest.createProduct(fields), "createProduct")

    suspend fun updateProduct(id: String, fields: Map<String, Any?>) =
        ensureOk(api.rest.updateProduct("eq.$id", fields), "updateProduct")

    suspend fun deleteProduct(id: String) =
        ensureOk(api.rest.deleteProduct("eq.$id"), "deleteProduct")

    suspend fun createBanner(fields: Map<String, Any?>) =
        ensureOk(api.rest.createBanner(fields), "createBanner")

    suspend fun updateBanner(id: String, fields: Map<String, Any?>) =
        ensureOk(api.rest.updateBanner("eq.$id", fields), "updateBanner")

    suspend fun deleteBanner(id: String) =
        ensureOk(api.rest.deleteBanner("eq.$id"), "deleteBanner")

    suspend fun updateSettings(fields: Map<String, Any?>) {
        val withTimestamp = fields + ("updated_at" to java.time.Instant.now().toString())
        ensureOk(api.rest.updateSettings("eq.1", withTimestamp), "updateSettings")
    }

    // ------------------------------------------------------------------
    // Admin orders
    // ------------------------------------------------------------------

    suspend fun orders(status: String? = null): List<Order> {
        val filters = mutableMapOf(
            "select" to "*",
            "order" to "created_at.desc",
            "limit" to "200",
        )
        status?.let { filters["status"] = "eq.$it" }
        return api.rest.listOrders(filters)
    }

    suspend fun orderItems(orderId: String): List<OrderItem> =
        api.rest.listOrderItems(mapOf("order_id" to "eq.$orderId", "select" to "*"))

    suspend fun setOrderStatus(orderId: String, status: String) =
        ensureOk(api.rest.updateOrderStatus("eq.$orderId", mapOf("status" to status)), "setOrderStatus")

    // ------------------------------------------------------------------
    // Admin images
    // ------------------------------------------------------------------

    suspend fun uploadProductImage(bytes: ByteArray, mime: String): String {
        val ext = when (mime) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val name = "prod_${System.currentTimeMillis()}_${(1000..9999).random()}.$ext"
        val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
        val response = api.storage.uploadProductImage(name, mime, "true", body)
        if (!response.isSuccessful) throw IOException(errorMessage(response))
        return "${Supabase.PROJECT_URL}/storage/v1/object/public/${Supabase.PRODUCT_IMAGES_BUCKET}/$name"
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private fun ensureOk(response: Response<*>, op: String) {
        if (!response.isSuccessful) throw IOException("$op failed: ${errorMessage(response)}")
    }

    private fun errorMessage(response: Response<*>): String {
        val raw = response.errorBody()?.string()
        return extractMessage(raw) ?: "HTTP ${response.code()}"
    }

    private fun extractMessage(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return try {
            JsonParser.parseString(raw).asJsonObject.let { obj ->
                listOf("message", "msg", "error_description", "error", "hint")
                    .firstNotNullOfOrNull { key -> obj.get(key)?.takeIf { it.isJsonPrimitive }?.asString }
            }
        } catch (e: Exception) {
            null
        }
    }
}
