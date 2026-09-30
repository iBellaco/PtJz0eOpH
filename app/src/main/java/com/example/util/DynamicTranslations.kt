package com.example.util

import android.content.Context
import org.json.JSONObject

object DynamicTranslations {
    @Volatile private var catalog: TranslationCatalog? = null
    fun load(context: Context) = loadSync(context)
    fun loadSync(context: Context) {
        if (catalog != null) return
        synchronized(this) {
            if (catalog != null) return
            try {
                fun read(name: String): Map<String, String> {
                    val json = JSONObject(context.assets.open(name).bufferedReader().use { it.readText() })
                    return json.keys().asSequence().associateWith { json.getString(it) }
                }
                catalog = TranslationCatalog(read("translations_pt.json"), read("translations_es.json"), read("translations_pt_aliases.json"))
            } catch (e: Exception) {
                AppLogger.e("Translations", "Unable to load offline language catalog", e)
            }
        }
    }
    fun get(language: String, text: String): String? {
        if (catalog == null) com.example.WildRiftApp.instance?.let { loadSync(it) }
        return catalog?.translate(language, text)?.takeIf { it != text }
    }
}
