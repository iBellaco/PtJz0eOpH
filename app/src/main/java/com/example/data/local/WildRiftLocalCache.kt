package com.example.data.local

import android.content.Context
import android.util.Log
import com.example.data.WildRiftRepository
import com.example.data.WildRiftSpellsAndRunes
import com.example.model.Champion
import com.example.model.MapObjectiveItem
import com.example.model.RuneItem
import com.example.model.SummonerSpellItem
import com.example.model.WildRiftItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object WildRiftLocalCache {

    private const val TAG = "WildRiftLocalCache"
    private const val PREFS_NAME = "wildrift_local_cache"
    private const val KEY_ITEMS = "cached_items"
    private const val KEY_CHAMPIONS = "cached_champions"
    private const val KEY_RUNES = "cached_runes"
    private const val KEY_SPELLS = "cached_spells"
    private const val KEY_OBJECTIVES = "cached_objectives"
    private const val KEY_PATCH_VERSION = "cached_patch_version"
    private const val KEY_LAST_SYNC_TIME = "cached_last_sync_time"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    fun saveToLocalCache(
        context: Context,
        items: List<WildRiftItem>? = null,
        champions: List<Champion>? = null,
        runes: List<RuneItem>? = null,
        spells: List<SummonerSpellItem>? = null,
        objectives: List<MapObjectiveItem>? = null,
        patchVersion: String? = null
    ) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()

            items?.let {
                editor.putString(KEY_ITEMS, json.encodeToString(it))
            }
            champions?.let {
                editor.putString(KEY_CHAMPIONS, json.encodeToString(it))
            }
            runes?.let {
                editor.putString(KEY_RUNES, json.encodeToString(it))
            }
            spells?.let {
                editor.putString(KEY_SPELLS, json.encodeToString(it))
            }
            objectives?.let {
                editor.putString(KEY_OBJECTIVES, json.encodeToString(it))
            }
            patchVersion?.let {
                editor.putString(KEY_PATCH_VERSION, it)
            }
            editor.putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis())
            editor.apply()
            Log.d(TAG, "Datos de Wild Rift cacheados localmente con éxito.")
        } catch (e: Exception) {
            Log.e(TAG, "Error guardando caché local de Wild Rift: ${e.message}", e)
        }
    }

    fun loadFromLocalCache(context: Context): Boolean {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            var hasLoadedAny = false

            val patch = prefs.getString(KEY_PATCH_VERSION, null)
            if (!patch.isNullOrBlank()) {
                WildRiftRepository.CURRENT_PATCH_VERSION = patch
                hasLoadedAny = true
            }

            val itemsJson = prefs.getString(KEY_ITEMS, null)
            if (!itemsJson.isNullOrBlank()) {
                val loadedItems = json.decodeFromString<List<WildRiftItem>>(itemsJson)
                
                if (loadedItems.isNotEmpty()) {
                    // Filter out spells that were previously saved as basic items
                    val spellIds = com.example.data.WildRiftSpellsAndRunes.summonerSpells.map { it.id }.toSet()
                    val filteredItems = loadedItems.filter { item ->
                        val isSpell = item.id.endsWith("_basic") && item.id.replace("_basic", "") in spellIds.map { it.replace("spell_", "") }
                        !isSpell && !item.id.startsWith("spell_")
                    }
                    val canonicalMap = com.example.data.WildRiftItemsData.list.associateBy { it.id }
                    val nameMap = com.example.data.WildRiftItemsData.list.associateBy { it.name.lowercase().trim() }
                    val sanitizedItems = filteredItems.map { item ->
                        val canonical = canonicalMap[item.id] ?: nameMap[item.name.lowercase().trim()]
                        if (canonical != null && (item.iconUrl.isBlank() || !item.iconUrl.startsWith("http") || item.iconUrl.contains("placeholder"))) {
                            item.copy(iconUrl = canonical.iconUrl)
                        } else {
                            item
                        }
                    }
                    WildRiftRepository.items = if (sanitizedItems.isNotEmpty()) sanitizedItems else com.example.data.WildRiftItemsData.list
                    hasLoadedAny = true
                }

            }

            val championsJson = prefs.getString(KEY_CHAMPIONS, null)
            if (!championsJson.isNullOrBlank()) {
                try {
                    val loadedChamps = json.decodeFromString<List<Champion>>(championsJson)
                    if (loadedChamps.isNotEmpty()) {
                        val normalizedChamps = loadedChamps.map { champ ->
                            if (champ.avatarUrl.isBlank() || champ.avatarUrl.startsWith("http")) {
                                champ.copy(avatarUrl = "file:///android_asset/champions/${champ.id}.png")
                            } else {
                                champ
                            }
                        }
                        WildRiftRepository.champions.clear()
                        WildRiftRepository.champions.addAll(normalizedChamps)
                        hasLoadedAny = true
                    }
                } catch (e: Exception) {
                    android.util.Log.e("WildRiftLocalCache", "Corrupted champions cache, clearing", e)
                    prefs.edit().remove(KEY_CHAMPIONS).apply()
                }
            }

            val runesJson = prefs.getString(KEY_RUNES, null)
            if (!runesJson.isNullOrBlank()) {
                val loadedRunes = json.decodeFromString<List<RuneItem>>(runesJson)
                val canonicalMap = WildRiftSpellsAndRunes.runes.associateBy { it.id }
                val canonicalIds = canonicalMap.keys
                val filteredRunes = loadedRunes.filter { it.id.startsWith("rune_") || it.id in canonicalIds }
                    .map { rune ->
                        val canonical = canonicalMap[rune.id]
                        if (canonical != null) {
                            rune.copy(iconUrl = canonical.iconUrl, category = canonical.category)
                        } else {
                            rune
                        }
                    }

                val hasMissingKeystones = filteredRunes.none { it.id == "fortalecimiento" }
                val hasKeystones = filteredRunes.any { it.category.trim().equals("Clave", ignoreCase = true) || it.category.trim().contains("Clave", ignoreCase = true) }
                if (hasMissingKeystones || filteredRunes.isEmpty() || !hasKeystones) {
                    WildRiftRepository.runes = WildRiftSpellsAndRunes.runes
                    saveToLocalCache(context, runes = WildRiftSpellsAndRunes.runes)
                } else {
                    WildRiftRepository.runes = filteredRunes
                }
                hasLoadedAny = true
            } else {
                WildRiftRepository.runes = WildRiftSpellsAndRunes.runes
                saveToLocalCache(context, runes = WildRiftSpellsAndRunes.runes)
            }

            val spellsJson = prefs.getString(KEY_SPELLS, null)
            if (!spellsJson.isNullOrBlank()) {
                val loadedSpells = json.decodeFromString<List<SummonerSpellItem>>(spellsJson)
                val canonicalSpellIds = WildRiftSpellsAndRunes.summonerSpells.map { it.id }.toSet()
                val filteredSpells = loadedSpells.filter { it.id.startsWith("spell_") || it.id in canonicalSpellIds }
                if (filteredSpells.isEmpty()) {
                    WildRiftRepository.summonerSpells = WildRiftSpellsAndRunes.summonerSpells
                    saveToLocalCache(context, spells = WildRiftSpellsAndRunes.summonerSpells)
                } else {
                    WildRiftRepository.summonerSpells = filteredSpells
                    hasLoadedAny = true
                }
            } else {
                WildRiftRepository.summonerSpells = WildRiftSpellsAndRunes.summonerSpells
                saveToLocalCache(context, spells = WildRiftSpellsAndRunes.summonerSpells)
            }

            val objectivesJson = prefs.getString(KEY_OBJECTIVES, null)
            if (!objectivesJson.isNullOrBlank()) {
                val loadedObjectives = json.decodeFromString<List<MapObjectiveItem>>(objectivesJson)
                if (loadedObjectives.isNotEmpty()) {
                    WildRiftRepository.mapObjectives = loadedObjectives
                    hasLoadedAny = true
                }
            }

            if (hasLoadedAny) {
                WildRiftRepository.LAST_SYNC_STATUS = "Datos locales cargados desde caché offline"
            }
            return hasLoadedAny
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando caché local: ${e.message}", e)
            return false
        }
    }

    fun getLastSyncTime(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_SYNC_TIME, 0L)
    }

    fun clearCache(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
