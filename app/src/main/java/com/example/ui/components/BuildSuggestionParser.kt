package com.example.ui.components

import android.os.Build
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.model.Champion
import com.example.model.RuneItem
import com.example.model.SummonerSpellItem
import com.example.data.WildRiftItemsData
import com.example.data.WildRiftSpellsAndRunes
import com.example.data.WildRiftRepository
import com.example.model.WildRiftItem
import java.util.Locale

data class ParsedBuildSuggestion(
    val championName: String,
    val championAvatar: String?,
    val championObj: Champion? = null,
    val role: String?,
    val coreItems: List<WildRiftItem>,
    val situationalItems: List<WildRiftItem>,
    val altSituationalItems: List<WildRiftItem>,
    val boots: WildRiftItem?,
    val keystoneRune: RuneItem?,
    val secondaryRunes: List<RuneItem>,
    val spells: List<SummonerSpellItem>,
    val tacticalNotes: String?
)

private fun normalizeSearchString(text: String): String {
    return text.lowercase(Locale.ROOT)
        .replace("á", "a")
        .replace("é", "e")
        .replace("í", "i")
        .replace("ó", "o")
        .replace("ú", "u")
        .replace("ü", "u")
        .replace("ñ", "n")
        .replace(Regex("[^a-z0-9]"), "")
        .trim()
}

private fun findChampionByName(rawName: String): Champion? {
    val clean = rawName.trim().removePrefix("•").trim()
    if (clean.isEmpty() || clean.equals("Ninguno", ignoreCase = true) || clean.equals("N/A", ignoreCase = true) || clean.equals("General", ignoreCase = true) || clean.equals("Campeon General", ignoreCase = true) || clean.equals("Campeón General", ignoreCase = true)) return null
    val norm = normalizeSearchString(clean)

    // Búsqueda exacta
    WildRiftRepository.champions.firstOrNull { it.name.equals(clean, ignoreCase = true) || it.nameEn.equals(clean, ignoreCase = true) }?.let { return it }
    WildRiftRepository.champions.firstOrNull { normalizeSearchString(it.name) == norm || normalizeSearchString(it.nameEn) == norm || it.id.equals(norm, ignoreCase = true) }?.let { return it }
    WildRiftRepository.champions.firstOrNull {
        val normEs = normalizeSearchString(it.name)
        val normEn = normalizeSearchString(it.nameEn)
        normEs.contains(norm) || norm.contains(normEs) || normEn.contains(norm) || norm.contains(normEn)
    }?.let { return it }
    return null
}

private fun findItemByName(rawName: String): WildRiftItem? {
    val clean = rawName.trim()
    if (clean.isEmpty() || clean.equals("Ninguno", ignoreCase = true) || clean.equals("N/A", ignoreCase = true)) return null
    val norm = normalizeSearchString(clean)

    // Búsqueda exacta primero
    WildRiftItemsData.list.firstOrNull { it.name.equals(clean, ignoreCase = true) || it.nameEn.equals(clean, ignoreCase = true) }?.let { return it }
    WildRiftItemsData.list.firstOrNull { normalizeSearchString(it.name) == norm || normalizeSearchString(it.nameEn) == norm }?.let { return it }

    // Búsqueda por contención
    WildRiftItemsData.list.firstOrNull {
        val normEs = normalizeSearchString(it.name)
        val normEn = normalizeSearchString(it.nameEn)
        normEs.contains(norm) || norm.contains(normEs) || normEn.contains(norm) || norm.contains(normEn)
    }?.let { return it }

    return null
}

private fun findRuneByName(rawName: String): RuneItem? {
    val clean = rawName.trim().removePrefix("Clave:").removePrefix("Secundarias:").trim()
    if (clean.isEmpty() || clean.equals("Ninguno", ignoreCase = true) || clean.equals("Ninguna", ignoreCase = true) || clean.equals("N/A", ignoreCase = true)) return null
    val norm = normalizeSearchString(clean)

    WildRiftSpellsAndRunes.runes.firstOrNull { it.name.equals(clean, ignoreCase = true) || it.nameEn.equals(clean, ignoreCase = true) }?.let { return it }
    WildRiftSpellsAndRunes.runes.firstOrNull { normalizeSearchString(it.name) == norm || normalizeSearchString(it.nameEn) == norm }?.let { return it }
    WildRiftSpellsAndRunes.runes.firstOrNull {
        val normEs = normalizeSearchString(it.name)
        val normEn = normalizeSearchString(it.nameEn)
        normEs.contains(norm) || norm.contains(normEs) || normEn.contains(norm) || norm.contains(normEn)
    }?.let { return it }
    return null
}

private fun findSpellByName(rawName: String): SummonerSpellItem? {
    val clean = rawName.trim()
    if (clean.isEmpty() || clean.equals("Ninguno", ignoreCase = true) || clean.equals("N/A", ignoreCase = true)) return null
    val norm = normalizeSearchString(clean)

    WildRiftSpellsAndRunes.summonerSpells.firstOrNull { it.name.equals(clean, ignoreCase = true) || it.nameEn.equals(clean, ignoreCase = true) }?.let { return it }
    WildRiftSpellsAndRunes.summonerSpells.firstOrNull { normalizeSearchString(it.name) == norm || normalizeSearchString(it.nameEn) == norm }?.let { return it }
    WildRiftSpellsAndRunes.summonerSpells.firstOrNull {
        val normEs = normalizeSearchString(it.name)
        val normEn = normalizeSearchString(it.nameEn)
        normEs.contains(norm) || norm.contains(normEs) || normEn.contains(norm) || norm.contains(normEn)
    }?.let { return it }
    return null
}

fun parseBuildSuggestionFromText(text: String, title: String = ""): ParsedBuildSuggestion? {
    if (!text.contains("SUGERENCIA DE BUILD", ignoreCase = true) &&
        !text.contains("OBJETOS CORE", ignoreCase = true) &&
        !text.contains("OBJETOS PRINCIPALES", ignoreCase = true) &&
        !text.contains("OBJETOS SITUACIONALES", ignoreCase = true) &&
        !text.contains("RUNAS", ignoreCase = true) &&
        !text.contains("CAMPEÓN", ignoreCase = true) &&
        !text.contains("CAMPEON", ignoreCase = true) &&
        !title.contains("Build", ignoreCase = true)) {
        return null
    }

    try {
        val lines = text.lines()
        var champName = ""
        var role: String? = null
        val coreItemsList = mutableListOf<WildRiftItem>()
        val sitItemsList = mutableListOf<WildRiftItem>()
        val altSitItemsList = mutableListOf<WildRiftItem>()
        var bootsItem: WildRiftItem? = null
        var keystone: RuneItem? = null
        val secondaryRunesList = mutableListOf<RuneItem>()
        val spellsList = mutableListOf<SummonerSpellItem>()
        val notesBuilder = StringBuilder()
        var isReadingNotes = false

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue

            if (isReadingNotes) {
                notesBuilder.appendLine(rawLine)
                continue
            }

            val upper = line.uppercase(Locale.ROOT)

            if (upper.contains("CAMPEÓN:") || upper.contains("CAMPEON:")) {
                val value = line.substringAfter(":").trim()
                if (value.contains("(") && value.contains(")")) {
                    champName = value.substringBefore("(").trim().removePrefix("•").trim()
                    role = value.substringAfter("(").substringBefore(")").trim()
                } else {
                    champName = value.removePrefix("•").trim()
                }
            } else if (upper.contains("ROL/LÍNEA:") || upper.contains("ROL/LINEA:") || upper.contains("LÍNEA / ROL:") || upper.contains("LINEA / ROL:") || upper.contains("ROL:")) {
                role = line.substringAfter(":").trim().removePrefix("•").trim()
            } else if (upper.contains("BOTAS") || upper.contains("ENCANTAMIENTO")) {
                val valStr = line.substringAfter(":").trim().removePrefix("•").trim()
                findItemByName(valStr)?.let { bootsItem = it }
            } else if (upper.contains("RUNAS:") || upper.contains("RUNA CLAVE:") || upper.contains("RUNA PRINCIPAL:")) {
                val valStr = line.substringAfter(":").trim().removePrefix("•").trim()
                if (valStr.contains("|") || valStr.contains("+") || valStr.contains(",")) {
                    val parts = valStr.split(Regex("[|+,]")).map { it.trim().removePrefix("Clave:").removePrefix("Secundarias:").trim() }.filter { it.isNotEmpty() }
                    if (parts.isNotEmpty()) {
                        findRuneByName(parts[0])?.let { keystone = it }
                        for (i in 1 until parts.size) {
                            findRuneByName(parts[i])?.let {
                                if (!secondaryRunesList.contains(it)) secondaryRunesList.add(it)
                            }
                        }
                    }
                } else {
                    findRuneByName(valStr)?.let { keystone = it }
                }
            } else if (upper.contains("SECUNDARIAS:") || upper.contains("RUNAS SECUNDARIAS:")) {
                val valStr = line.substringAfter(":").trim().removePrefix("•").trim()
                val parts = valStr.split(Regex("[,|+]| - | • ")).map { it.trim() }.filter { it.isNotEmpty() }
                for (part in parts) {
                    findRuneByName(part)?.let {
                        if (!secondaryRunesList.contains(it)) secondaryRunesList.add(it)
                    }
                }
            } else if (upper.contains("HECHIZOS:") || upper.contains("HECHIZOS DE INVOCADOR:")) {
                val valStr = line.substringAfter(":").trim().removePrefix("•").trim()
                val parts = valStr.split(Regex("[,|+]| / | - | • ")).map { it.trim() }.filter { it.isNotEmpty() }
                for (part in parts) {
                    findSpellByName(part)?.let {
                        if (!spellsList.contains(it)) spellsList.add(it)
                    }
                }
            } else if (upper.contains("OBJETOS CORE") || upper.contains("OBJETOS PRINCIPALES")) {
                val valStr = line.substringAfter(":").trim().removePrefix("•").trim()
                val parts = valStr.split(Regex("[•,+]| - ")).map { it.trim() }.filter { it.isNotEmpty() }
                for (part in parts) {
                    findItemByName(part)?.let {
                        if (!coreItemsList.contains(it)) coreItemsList.add(it)
                    }
                }
            } else if (upper.contains("OBJETOS SITUACIONALES") || upper.contains("SITUACIONALES (7-8)") || upper.contains("SITUACIONALES (7 Y 8)")) {
                val valStr = line.substringAfter(":").trim().removePrefix("•").trim()
                val parts = valStr.split(Regex("[•,+]| - ")).map { it.trim() }.filter { it.isNotEmpty() }
                for (part in parts) {
                    findItemByName(part)?.let {
                        if (!sitItemsList.contains(it)) sitItemsList.add(it)
                    }
                }
            } else if (upper.contains("ALTERNATIVAS SITUACIONALES") || upper.contains("ALT SITUACIONAL") || upper.contains("ALT SIT")) {
                val valStr = line.substringAfter(":").trim().removePrefix("•").trim()
                val parts = valStr.split(Regex("[•,+]| - ")).map { it.trim() }.filter { it.isNotEmpty() }
                for (part in parts) {
                    findItemByName(part)?.let {
                        if (!altSitItemsList.contains(it)) altSitItemsList.add(it)
                    }
                }
            } else if (upper.contains("JUSTIFICACIÓN TÁCTICA") || upper.contains("JUSTIFICACION TACTICA") || upper.contains("NOTAS / EXPLICACIÓN TÁCTICA:") || upper.contains("NOTAS / EXPLICACION TACTICA:") || upper.contains("EXPLICACIÓN TÁCTICA:") || upper.contains("EXPLICACION TACTICA:") || upper.contains("NOTAS:")) {
                isReadingNotes = true
                val remaining = line.substringAfter(":").trim()
                if (remaining.isNotEmpty()) {
                    notesBuilder.appendLine(remaining)
                }
            } else if (Regex("""^[1-6]\.\s*""").containsMatchIn(line)) {
                // Item core 1 a 6
                val itemName = line.replace(Regex("""^[1-6]\.\s*"""), "").trim()
                findItemByName(itemName)?.let { if (!coreItemsList.contains(it)) coreItemsList.add(it) }
            } else if (line.startsWith("7.") || line.startsWith("8.") || upper.contains("SITUACIONAL 1") || upper.contains("SITUACIONAL 2")) {
                val itemName = line.substringAfter(":").ifEmpty { line.replace(Regex("""^[78]\.\s*(\(.*\))?\s*:?"""), "") }.trim()
                findItemByName(itemName)?.let { if (!sitItemsList.contains(it)) sitItemsList.add(it) }
            }
        }

        // Si el campeón no fue detectado en el cuerpo, extraerlo del título (ej: "Sugerencia de Build para Zed (Mid)")
        if (champName.isBlank() && title.isNotBlank()) {
            if (title.contains("para ", ignoreCase = true)) {
                val extracted = title.substringAfter("para ", "").substringBefore("(").trim()
                if (extracted.isNotBlank()) {
                    val matchChamp = findChampionByName(extracted)
                    champName = matchChamp?.name ?: extracted
                }
            }
            if (champName.isBlank()) {
                for (c in WildRiftRepository.champions) {
                    if (title.contains(c.name, ignoreCase = true) || title.contains(c.nameEn, ignoreCase = true)) {
                        champName = c.name
                        break
                    }
                }
            }
            if (role == null && title.contains("(") && title.contains(")")) {
                role = title.substringAfter("(").substringBefore(")").trim()
            }
        }

        if (champName.isBlank() && coreItemsList.isEmpty() && sitItemsList.isEmpty() && bootsItem == null) {
            return null
        }

        // Buscar el objeto Champion real de la base de datos para obtener avatarUrl oficial y tier
        val foundChamp = if (champName.isNotBlank()) findChampionByName(champName) else null
        val effectiveChampName = foundChamp?.name ?: champName.ifBlank { "Campeón" }
        val champAvatar = foundChamp?.avatarUrl?.takeIf { it.isNotBlank() }

        return ParsedBuildSuggestion(
            championName = effectiveChampName,
            championAvatar = champAvatar,
            championObj = foundChamp,
            role = role,
            coreItems = coreItemsList,
            situationalItems = sitItemsList,
            altSituationalItems = altSitItemsList,
            boots = bootsItem,
            keystoneRune = keystone,
            secondaryRunes = secondaryRunesList,
            spells = spellsList,
            tacticalNotes = notesBuilder.toString().trim().ifEmpty { null }
        )
    } catch (e: Exception) {
        return null
    }
}
