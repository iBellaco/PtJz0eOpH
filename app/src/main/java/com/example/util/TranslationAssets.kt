package com.example.util

/** One catalog definition shared by the application, tools and regression audits. */
object TranslationAssets {
    val portugueseFiles = listOf("translations_pt.json", "translations_support_pt.json", "translations_ui_pt.json")
    fun load(read: (String) -> Map<String, String>): TranslationCatalog = TranslationCatalog(
        portugueseFiles.fold(emptyMap()) { result, file -> result + read(file) },
        read("translations_es.json"), read("translations_pt_aliases.json")
    )
}
