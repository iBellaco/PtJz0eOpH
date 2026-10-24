package com.example.service.screen

import com.example.model.Champion
import com.example.model.LaneRole

/** Reads the same player title before and after selection, independently of champion affinity. */
object AllyDraftNameReader {
    data class Reading(val lane: LaneRole? = null, val champion: Champion? = null, val ambiguous: Boolean = false)
    // Full-title OCR readings observed in compressed captures, not fuzzy substrings.
    private val observedLaneTitles=mapOf("calle del daagon" to LaneRole.ADC,"calle del daagcn" to LaneRole.ADC,
        "calle del damgon" to LaneRole.ADC,"calle del barcn" to LaneRole.TOP,
        "calle del bardn" to LaneRole.TOP,"cale cntral" to LaneRole.MID,
        "cmle cntral" to LaneRole.MID,"cale (ntral" to LaneRole.MID,
        "jnala" to LaneRole.JUNGLE,"jmala" to LaneRole.JUNGLE,
        "ajoyo" to LaneRole.SUPPORT,"aporo" to LaneRole.SUPPORT)

    /** An exact intact title takes precedence over a destructively processed alternative. */
    internal fun preferOriginal(original: Reading, withoutBadge: Reading): Reading =
        if (original.ambiguous || original.champion != null || original.lane != null) original else withoutBadge

    fun readTitleRows(lines: List<Pair<String, Int>>, rowDistance: Int, catalog: List<Champion>,
        titleMaxCenterY: Int = Int.MAX_VALUE): Reading {
        val titleLines = lines.filter { it.second <= titleMaxCenterY }
        val top = titleLines.minOfOrNull { it.second } ?: return Reading()
        val title = titleLines.filter { it.second <= top + rowDistance }.map { it.first }
        val reading = read(title, catalog)
        if (reading.ambiguous || reading.lane != null || reading.champion != null) return reading
        // Native OCR can split the complete title into neighbouring fragments
        // (V + I, LEE + SIN). Join only this same top row, never the player's row.
        val joined=read(listOf(title.joinToString(" ")),catalog)
        if(joined.ambiguous || joined.lane!=null || joined.champion!=null) return joined
        // A long lane title may wrap. A player's second-line name cannot become
        // the selected champion when the first line was unreadable.
        if (title.any { Regex("\\b(calle|carril|linea|rota|lane)\\b").containsMatchIn(DraftValidationLayer.normalize(it)) }) {
            return Reading(lane = laneTitle(titleLines.sortedBy { it.second }.joinToString(" ") { it.first }))
        }
        return Reading()
    }

    /**
     * Require the complete two-letter title after badge suppression too.
     * Unknown readings such as IA, IAA and rVI cannot establish an identity.
     */
    internal fun readBadgeSeparatedShortTitle(lines: List<Pair<String, Int>>, catalog: List<Champion>): Reading {
        val text = lines.sortedBy { it.second }.joinToString(" ") { it.first }
        val key = DraftValidationLayer.normalize(text).replace(" ", "")
        if (key !in setOf("vi", "v1", "vl")) return Reading()
        val champion = catalog.firstOrNull { it.id == "vi" }
        return champion?.let { Reading(champion = it) } ?: Reading()
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
        // Match the whole raw title before legacy badge stripping, so a player
        // nickname such as XCS JNALA cannot turn into a compressed lane alias.
        observedLaneTitles[DraftValidationLayer.normalize(text)]?.let { return it }
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
