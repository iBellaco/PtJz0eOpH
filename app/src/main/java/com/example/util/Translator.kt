package com.example.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import com.example.data.WildRiftRepository

val LocalLanguage = compositionLocalOf { "" }

/** One reactive language for labels, catalog fields and generated coaching, including standalone windows. */
@Composable
fun currentAppLanguage(): String {
    val selected by AppLanguage.current.collectAsState()
    return AppLanguage.normalize(LocalLanguage.current.ifBlank { selected })
}

@Composable
fun tr(key: String): String = trStr(currentAppLanguage(), key)

@Composable
fun trNullable(text: String?): String? = text?.let { tr(it) }

@Composable
fun tr(text: AnnotatedString): AnnotatedString {
    val language = currentAppLanguage()
    // Translate styled segments without dropping links, colours or emphasis.
    val boundaries = (listOf(0, text.length) + text.spanStyles.flatMap { listOf(it.start, it.end) } +
        text.paragraphStyles.flatMap { listOf(it.start, it.end) } +
        text.getStringAnnotations(0, text.length).flatMap { listOf(it.start, it.end) }).distinct().sorted()
    val complete = trStr(language, text.text)
    val pieces = boundaries.zipWithNext().map { (start, end) -> trStr(language, text.text.substring(start, end)) }
    val usePieces = pieces.joinToString("") == complete
    return buildAnnotatedString {
        val positions = mutableMapOf(0 to 0)
        if (usePieces) {
            boundaries.zipWithNext().forEachIndexed { i, (_, end) ->
                append(pieces[i]); positions[end] = length
            }
        } else {
            // Translate the complete sentence before restoring emphasis; style boundaries
            // must never turn a known sentence into unknown Spanish fragments.
            append(complete)
            boundaries.forEach { offset -> positions[offset] =
                if (text.isEmpty()) 0 else (offset.toLong() * length / text.length).toInt() }
        }
        text.spanStyles.forEach { addStyle(it.item, positions.getValue(it.start), positions.getValue(it.end)) }
        text.paragraphStyles.forEach { addStyle(it.item, positions.getValue(it.start), positions.getValue(it.end)) }
        text.getStringAnnotations(0, text.length).forEach {
            addStringAnnotation(it.tag, it.item, positions.getValue(it.start), positions.getValue(it.end))
        }
    }
}

fun trStr(lang: String, key: String): String {
    val language = AppLanguage.normalize(lang)
    // The Spanish catalog is authoritative. Reverse UI aliases must not rename
    // an item or replace its updated Spanish description with another source.
    if (language == "es" && key.trim().lowercase(java.util.Locale.ROOT) in spanishItemPhrases) return key
    DynamicTranslations.get(language, key)?.let { return it }
    if (language != "pt") return key
    WildRiftRepository.items.forEach {
        if (it.name.equals(key, true) && it.namePt.isNotBlank()) return it.namePt
        if (it.stats.equals(key, true) && it.statsPt.isNotBlank()) return it.statsPt
        if (it.passive.equals(key, true) && it.passivePt.isNotBlank()) return it.passivePt
        if (it.coachTip.equals(key, true) && it.coachTipPt.isNotBlank()) return it.coachTipPt
    }
    WildRiftRepository.champions.forEach {
        if (it.name.equals(key, true) && it.namePt.isNotBlank()) return it.namePt
        if (it.title.equals(key, true) && it.titlePt.isNotBlank()) return it.titlePt
    }
    WildRiftRepository.runes.forEach {
        if (it.name.equals(key, true) && it.namePt.isNotBlank()) return it.namePt
        if (it.description.equals(key, true) && it.descriptionPt.isNotBlank()) return it.descriptionPt
    }
    WildRiftRepository.summonerSpells.forEach {
        if (it.name.equals(key, true) && it.namePt.isNotBlank()) return it.namePt
        if (it.description.equals(key, true) && it.descriptionPt.isNotBlank()) return it.descriptionPt
    }
    return key
}

private val spanishItemPhrases by lazy {
    com.example.data.WildRiftItemsData.list.flatMap {
        listOf(it.name, it.stats, it.passive, it.coachTip) + it.getStatsList("es")
    }.map { it.trim().lowercase(java.util.Locale.ROOT) }.toHashSet()
}
