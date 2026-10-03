package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Explicit in-app choice is authoritative, independent of country and device language. */
object AppLanguage {
    private val _current = MutableStateFlow("es")
    val current = _current.asStateFlow()
    fun normalize(value: String?): String = if (value?.trim()?.lowercase(java.util.Locale.ROOT)?.startsWith("pt") == true) "pt" else "es"
    private var preferences: SharedPreferences? = null
    private var applicationContext: Context? = null
    fun locale(language: String): java.util.Locale = java.util.Locale.forLanguageTag(
        if (normalize(language) == "pt") "pt-BR" else "es-419"
    )
    @Suppress("DEPRECATION")
    private fun applyLocale(context: Context, language: String) {
        val locale = locale(language)
        java.util.Locale.setDefault(locale)
        val config = android.content.res.Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
    fun localizedContext(context: Context): Context {
        val language = normalize(context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString("selected_language", "es"))
        val config = android.content.res.Configuration(context.resources.configuration)
        config.setLocale(locale(language))
        return context.createConfigurationContext(config)
    }
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        if (key == "selected_language") {
            _current.value = normalize(prefs.getString(key, "es"))
            applicationContext?.let { applyLocale(it, _current.value) }
        }
    }
    fun initialize(context: Context) {
        if (preferences != null && applicationContext === context.applicationContext) return
        preferences?.unregisterOnSharedPreferenceChangeListener(listener)
        val prefs = context.applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        preferences = prefs
        applicationContext = context.applicationContext
        val savedLanguage = prefs.getString("selected_language", "es")
        _current.value = normalize(savedLanguage)
        if (savedLanguage != _current.value) prefs.edit().putString("selected_language", _current.value).apply()
        applyLocale(context.applicationContext, _current.value)
        prefs.registerOnSharedPreferenceChangeListener(listener)
        DynamicTranslations.loadSync(context.applicationContext)
    }
    fun select(context: Context, language: String) {
        initialize(context)
        val normalized = normalize(language)
        _current.value = normalized
        applyLocale(context.applicationContext, normalized)
        preferences!!.edit().putString("selected_language", normalized).putBoolean("is_language_set", true).apply()
    }
}

fun appTr(text: String): String = trStr(AppLanguage.current.value, text)
