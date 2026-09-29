package com.example.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import com.example.data.WildRiftRepository

val LocalLanguage = compositionLocalOf { "es" }

@Composable
fun tr(key: String): String = trStr(LocalLanguage.current, key)

@Composable
fun trNullable(text: String?): String? = text?.let { tr(it) }

@Composable
fun tr(text: AnnotatedString): AnnotatedString {
    val language = LocalLanguage.current
    // Translate styled segments without dropping links, colours or emphasis.
    val boundaries = (listOf(0, text.length) + text.spanStyles.flatMap { listOf(it.start, it.end) } +
        text.paragraphStyles.flatMap { listOf(it.start, it.end) } +
        text.getStringAnnotations(0, text.length).flatMap { listOf(it.start, it.end) }).distinct().sorted()
    return buildAnnotatedString {
        val positions = mutableMapOf(0 to 0)
        boundaries.zipWithNext().forEach { (start, end) ->
            append(trStr(language, text.text.substring(start, end)))
            positions[end] = length
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
