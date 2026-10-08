package com.example.service.screen

import com.example.model.Champion
import com.example.model.LaneRole

/** Reads the same player title before and after selection, independently of champion affinity. */
object AllyDraftNameReader {
    data class Reading(val lane: LaneRole? = null, val champion: Champion? = null, val ambiguous: Boolean = false)

    fun readTitleRows(lines: List<Pair<String, Int>>, rowDistance: Int, catalog: List<Champion>,
        titleMaxCenterY: Int = Int.MAX_VALUE): Reading {
        val titleLines = lines.filter { it.second <= titleMaxCenterY }
        val top = titleLines.minOfOrNull { it.second } ?: return Reading()
        val title = titleLines.filter { it.second <= top + rowDistance }.map { it.first }
        val reading = read(title, catalog)
        if (reading.ambiguous || reading.lane != null || reading.champion != null) return reading
        // A long lane title may wrap. A player's second-line name cannot become
        // the selected champion when the first line was unreadable.
        if (title.any { Regex("\\b(calle|carril|linea|rota|lane)\\b").containsMatchIn(DraftValidationLayer.normalize(it)) }) {
            return Reading(lane = laneTitle(titleLines.sortedBy { it.second }.joinToString(" ") { it.first }))
        }
        return Reading()
    }

    fun read(lines: List<String>, catalog: List<Champion>): Reading {
        val names = lines.mapNotNull { ChampionNameResolver.findChampionInNameBand(it, catalog) }.distinctBy { it.id }
        val lanes = (lines + lines.joinToString(" ")).mapNotNull { laneTitle(it) }.distinct()
        return when {
            names.size > 1 || lanes.size > 1 || (names.isNotEmpty() && lanes.isNotEmpty()) -> Reading(ambiguous = true)
            names.size == 1 -> Reading(champion = names.single())
            lanes.size == 1 -> Reading(lane = lanes.single())
            else -> Reading()
        }
    }

    private fun laneTitle(text: String): LaneRole? {
        val clean = DraftValidationLayer.normalize(DraftValidationLayer.stripLeadingMasteryOrRoleIcon(text))
        val vocabulary = setOf("calle", "carril", "linea", "rota", "lane", "del", "de", "do", "da", "d", "el", "la",
            "baron", "barao", "dragon", "dragao", "central", "medio", "meio", "jungla", "jungle", "selva",
            "soporte", "support", "suporte", "apoyo", "apoio", "duo", "dupla", "solo", "top", "mid", "adc", "sup", "jg", "atirador", "tirador", "superior", "inferior")
        if (clean.split(Regex("\\s+")).any { it !in vocabulary }) return null
        return DraftValidationLayer.parseRoleFromText(clean)
    }

    fun identifiesUser(text: String, knownNames: List<String>): Boolean {
        fun compact(value: String) = DraftValidationLayer.normalize(value).filter { it.isLetterOrDigit() }
        val value = compact(text)
        return value in setOf("tu", "you", "voce", "yo", "eu") ||
            Regex("\\((?:tu|tú|you|você|voce)\\)", RegexOption.IGNORE_CASE).containsMatchIn(text) ||
            knownNames.map(::compact).any { it.length >= 3 && it == value }
    }
}
