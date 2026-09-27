package com.example.util

import android.content.Context
import org.json.JSONObject
import com.example.data.WildRiftRepository
import com.example.R
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DynamicTranslations {
    @Volatile
    private var ptMap: Map<String, String>? = null

    private val scope = CoroutineScope(Dispatchers.IO + CoroutineExceptionHandler { _, t ->
        AppLogger.e("Translations", "Error loading translations safely", t)
    })

    fun load(context: Context) {
        if (ptMap != null) return
        
        scope.launch {
            loadSync(context)
        }
    }

    fun loadSync(context: Context) {
        if (ptMap != null) return
        try {
            synchronized(this) {
                if (ptMap == null) {
                    val jsonStr = context.assets.open("translations_pt.json").bufferedReader().use { it.readText() }
                    val json = JSONObject(jsonStr)
                    val map = HashMap<String, String>(json.length() * 2)
                    val iter = json.keys()
                    while (iter.hasNext()) {
                        val key = iter.next()
                        map[key] = json.getString(key)
                    }
                    ptMap = map
                }
            }
        } catch (e: Exception) {
            AppLogger.e("Translations", "Failed to load dynamic translations", e)
        }
    }

    fun get(lang: String, key: String): String? {
        if (lang != "pt") return null
        if (ptMap == null) {
            com.example.WildRiftApp.instance?.let { loadSync(it) }
        }
        val staticTranslation = ptMap?.get(key) ?: ptMap?.get(key.trim())
        if (staticTranslation != null) return staticTranslation
        
        // ----------------------------------------------------
        // DYNAMIC REPOSITORY LOOKUP
        // ----------------------------------------------------
        val itemByName = WildRiftRepository.items.find { it.name.equals(key, ignoreCase = true) }
        if (itemByName != null && itemByName.namePt.isNotBlank()) return itemByName.namePt
        
        val itemByStats = WildRiftRepository.items.find { it.stats.equals(key, ignoreCase = true) }
        if (itemByStats != null && itemByStats.statsPt.isNotBlank()) return itemByStats.statsPt
        
        val itemByPassive = WildRiftRepository.items.find { it.passive.equals(key, ignoreCase = true) }
        if (itemByPassive != null && itemByPassive.passivePt.isNotBlank()) return itemByPassive.passivePt
        
        val champByName = WildRiftRepository.champions.find { it.name.equals(key, ignoreCase = true) }
        if (champByName != null && champByName.namePt.isNotBlank()) return champByName.namePt
        
        val champByTitle = WildRiftRepository.champions.find { it.title.equals(key, ignoreCase = true) }
        if (champByTitle != null && champByTitle.titlePt.isNotBlank()) return champByTitle.titlePt
        
        val runeByName = WildRiftRepository.runes.find { it.name.equals(key, ignoreCase = true) }
        if (runeByName != null && runeByName.namePt.isNotBlank()) return runeByName.namePt
        
        val runeByDesc = WildRiftRepository.runes.find { it.description.equals(key, ignoreCase = true) }
        if (runeByDesc != null && runeByDesc.descriptionPt.isNotBlank()) return runeByDesc.descriptionPt
        
        val spellByName = WildRiftRepository.summonerSpells.find { it.name.equals(key, ignoreCase = true) }
        if (spellByName != null && spellByName.namePt.isNotBlank()) return spellByName.namePt
        
        val spellByDesc = WildRiftRepository.summonerSpells.find { it.description.equals(key, ignoreCase = true) }
        if (spellByDesc != null && spellByDesc.descriptionPt.isNotBlank()) return spellByDesc.descriptionPt
        
        return null
    }
}
