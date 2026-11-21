package com.matjard.core

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/** Persists the admin Supabase session (tokens + user) in private prefs. */
class TokenStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("matjard_admin_session", Context.MODE_PRIVATE)

    val accessToken: String? get() = prefs.getString(KEY_ACCESS, null)
    val refreshToken: String? get() = prefs.getString(KEY_REFRESH, null)
    val userEmail: String? get() = prefs.getString(KEY_EMAIL, null)
    val userId: String? get() = prefs.getString(KEY_USER_ID, null)
    val expiresAtMillis: Long get() = prefs.getLong(KEY_EXPIRES_AT, 0L)

    fun save(auth: AuthResponse) {
        prefs.edit()
            .putString(KEY_ACCESS, auth.accessToken)
            .putString(KEY_REFRESH, auth.refreshToken)
            .putLong(KEY_EXPIRES_AT, System.currentTimeMillis() + auth.expiresIn * 1000)
            .putString(KEY_EMAIL, auth.user?.email)
            .putString(KEY_USER_ID, auth.user?.id)
            .apply()
    }

    fun clear() = prefs.edit().clear().apply()

    fun hasSession(): Boolean = !accessToken.isNullOrBlank()

    companion object {
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EXPIRES_AT = "expires_at"
    }
}

/** Arabic/French language preference shared by both apps. */
class LanguageStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("matjard_language", Context.MODE_PRIVATE)

    fun lang(): String = prefs.getString(KEY_LANG, "ar") ?: "ar"

    fun setLang(lang: String) {
        prefs.edit().putString(KEY_LANG, lang).apply()
    }

    /** Wrap [base] with the stored locale so resources + RTL follow the choice. */
    fun wrapContext(base: Context): Context {
        val locale = Locale(lang())
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }

    companion object { private const val KEY_LANG = "lang" }
}
