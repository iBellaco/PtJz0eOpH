package com.example.data.analytics

import com.example.data.WildRiftRepository
import com.example.data.local.entity.SavedDraftEntity
import com.example.data.local.entity.SavedDraftSlotData
import com.example.model.Champion
import com.example.model.LaneRole
import kotlinx.serialization.json.Json

enum class TierGrade(val label: String, val description: String, val minWinRate: Double) {
    PROVISIONAL("—", "En evaluación / Menos de 5 resultados", 0.0),
    S_PLUS("S+", "75% o más de victorias registradas", 75.0),
    S("S", "60% a menos de 75% de victorias registradas", 60.0),
    A("A", "50% a menos de 60% de victorias registradas", 50.0),
    B("B", "35% a menos de 50% de victorias registradas", 35.0),
    C("C", "Menos de 35% de victorias registradas", 0.0)
}

data class MatchupRecord(
    val opponentName: String,
    val opponentAvatarUrl: String,
    val wins: Int,
    val losses: Int,
    val total: Int,
    val winRate: Double
)

data class AllySynergyRecord(
    val allyName: String,
    val allyAvatarUrl: String,
    val role: LaneRole,
    val wins: Int,
    val losses: Int,
    val total: Int,
    val winRate: Double
)

data class RolePerformanceRecord(
    val role: LaneRole,
    val wins: Int,
    val losses: Int,
    val total: Int,
    val winRate: Double
)

data class PersonalChampionStats(
    val championId: String,
    val championName: String,
    val avatarUrl: String,
    val primaryRole: LaneRole,
    val totalGames: Int,
    val wins: Int,
    val losses: Int,
    val pending: Int,
    val winRate: Double,
    val tier: TierGrade,
    val tierScore: Double,
    val avgEstimatedWr: Double,
    val roleBreakdown: List<RolePerformanceRecord>,
    val matchups: List<MatchupRecord>,
    val allies: List<AllySynergyRecord>,
    val draftMatches: List<SavedDraftEntity>,
    val coachVerdict: String,
    val avgScore: String = "",
    val avgKda: Double = 0.0,
    val best1v1Matchup: MatchupRecord? = null,
    val kdaGames: Int = 0
)

data class PersonalOverviewStats(
    val totalGames: Int,
    val totalWins: Int,
    val totalLosses: Int,
    val totalPending: Int,
    val overallWinRate: Double,
    val signatureChampion: PersonalChampionStats?,
    val bestRole: LaneRole?,
    val bestRoleWinRate: Double,
    val nemesisOpponent: MatchupRecord?,
    val bestAllySynergy: AllySynergyRecord?,
    val best1v1Matchup: MatchupRecord? = null,
    val top1v1Matchups: List<MatchupRecord> = emptyList(),
    val worst1v1Matchups: List<MatchupRecord> = emptyList(),
    val coach1v1Analysis: String = "",
    val excludedUnidentifiedGames: Int = 0
)

data class PersonalTierListResult(
    val overview: PersonalOverviewStats,
    val tierSPlus: List<PersonalChampionStats>,
    val tierS: List<PersonalChampionStats>,
    val tierA: List<PersonalChampionStats>,
    val tierB: List<PersonalChampionStats>,
    val tierC: List<PersonalChampionStats>,
    val allRankedChampions: List<PersonalChampionStats>,
    val roleDistribution: List<RolePerformanceRecord>,
    val provisionalChampions: List<PersonalChampionStats> = emptyList()
)

object PersonalTierListManager {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Calcula la Tier List Personal y las estadísticas detalladas del jugador
     * basadas en su historial de drafts y partidas guardadas.
     * Solo cuenta el campeón identificado del jugador. Un resultado de equipo no
     * demuestra que se ganó el duelo de línea. El KDA es contexto opcional, no un
     * premio de puntuación: evita favorecer a quien registra solo sus mejores KDA.
     */
    fun calculatePersonalTierList(
        drafts: List<SavedDraftEntity>,
        roleFilter: LaneRole? = null,
        lang: String = "es",
        modeFilter: String = "ALL" // "ALL", "RANKED", "LEGENDARY"
    ): PersonalTierListResult {
        // Filtramos drafts por modo (Normal vs Legendaria)
        val modeFilteredDrafts = when (modeFilter.uppercase()) {
            "LEGENDARY" -> drafts.filter { it.isLegendary || it.matchMode.equals("LEGENDARY", true) }
            "RANKED" -> drafts.filter { !it.isLegendary && it.matchMode.equals("RANKED", true) }
            else -> drafts
        }

        // Filtramos drafts si hay rol activo
        val activeDrafts = if (roleFilter != null) {
            modeFilteredDrafts.filter { draft ->
                val draftRole = recordedRole(draft)
                draftRole == roleFilter
            }
        } else modeFilteredDrafts
        val identifiedDrafts = activeDrafts.filter { extractPlayerChampionName(it).isNotBlank() }

        // Mapa de datos por campeón: Nombre del campeón (en minúsculas/normalizado) -> Lista de partidas
        val champDraftsMap = mutableMapOf<String, MutableList<SavedDraftEntity>>()

        for (draft in identifiedDrafts) {
            val champName = extractPlayerChampionName(draft)
            if (champName.isNotBlank()) {
                champDraftsMap.getOrPut(champName.lowercase()) { mutableListOf() }.add(draft)
            }
        }

        // Obtener el catálogo base de campeones según el filtro de rol
        val catalogChampions = if (roleFilter != null) {
            WildRiftRepository.champions.filter { champ ->
                champ.primaryRole == roleFilter || champ.secondaryRoles.contains(roleFilter) ||
                        champDraftsMap.containsKey(champ.name.lowercase()) ||
                        champDraftsMap.containsKey(champ.id.lowercase())
            }
        } else {
            WildRiftRepository.champions
        }
        // Imported records remain usable even if that catalog entry is unavailable.
        val baseChampions = catalogChampions + champDraftsMap.mapNotNull { (key, matches) ->
            if (catalogChampions.any { it.name.equals(key, true) || it.id.equals(key, true) }) null
            else Champion(id = matches.first().myChampionId.ifBlank { key },
                name = extractPlayerChampionName(matches.first()),
                avatarUrl = extractChampionAvatar(matches.first(), key).orEmpty(),
                primaryRole = recordedRole(matches.first()) ?: LaneRole.MID)
        }

        val championStatsList = mutableListOf<PersonalChampionStats>()
        // Procesar exclusivamente campeones presentes en el historial identificado.
        for (champ in baseChampions) {

            val matches = (champDraftsMap[champ.name.lowercase()].orEmpty() +
                champDraftsMap[champ.id.lowercase()].orEmpty()).distinct()
            if (matches.isEmpty()) continue

            var wins = 0
            var losses = 0
            var pending = 0
            var sumEstimatedWr = 0.0

            val roleCountMap = mutableMapOf<LaneRole, Pair<Int, Int>>() // Role -> Pair(wins, losses)
            val matchupMap = mutableMapOf<String, Pair<Int, Int>>() // Opponent -> Pair(wins, losses)
            val allyMap = mutableMapOf<String, Pair<Int, Int>>() // Ally -> Pair(wins, losses)

            for (match in matches) {
                val isWin = match.matchResult.equals("VICTORY", ignoreCase = true)
                val isLoss = match.matchResult.equals("DEFEAT", ignoreCase = true)

                if (isWin) wins++
                else if (isLoss) losses++
                else pending++

                sumEstimatedWr += match.estimatedWinrate

                val role = recordedRole(match)
                if (role != null) {
                    val currentRoleStats = roleCountMap.getOrDefault(role, Pair(0, 0))
                    roleCountMap[role] = Pair(
                        currentRoleStats.first + (if (isWin) 1 else 0),
                        currentRoleStats.second + (if (isLoss) 1 else 0)
                    )
                }

                // Matchup directo (rival en línea)
                val enemyOpponent = recordedLaneOpponent(match)
                if (enemyOpponent.isNotBlank() && (isWin || isLoss)) {
                    val currentM = matchupMap.getOrDefault(enemyOpponent, Pair(0, 0))
                    matchupMap[enemyOpponent] = Pair(
                        currentM.first + (if (isWin) 1 else 0),
                        currentM.second + (if (isLoss) 1 else 0)
                    )
                }

                // Aliados en partida
                val allies = extractAllies(match, champ.name)
                for (ally in allies.distinct().filter { isWin || isLoss }) {
                    val currentA = allyMap.getOrDefault(ally, Pair(0, 0))
                    allyMap[ally] = Pair(
                        currentA.first + (if (isWin) 1 else 0),
                        currentA.second + (if (isLoss) 1 else 0)
                    )
                }
            }

            val totalDecided = wins + losses
            val winRate = if (totalDecided > 0) (wins.toDouble() / totalDecided * 100.0) else 0.0
            val avgEstimated = if (matches.isNotEmpty()) sumEstimatedWr / matches.size else champ.winrate

            // Rol principal
            val primaryRole = if (roleCountMap.isNotEmpty()) {
                roleCountMap.maxByOrNull { it.value.first + it.value.second }?.key ?: champ.primaryRole
            } else {
                roleFilter ?: champ.primaryRole
            }

            // Desglose por roles
            val roleBreakdown = roleCountMap.map { (role, counts) ->
                val totalR = counts.first + counts.second
                val wrR = if (totalR > 0) (counts.first.toDouble() / totalR * 100.0) else 0.0
                RolePerformanceRecord(role, counts.first, counts.second, totalR, wrR)
            }.sortedByDescending { it.total }

            // Matchups
            val matchupList = matchupMap.map { (oppName, counts) ->
                val totalM = counts.first + counts.second
                val wrM = if (totalM > 0) (counts.first.toDouble() / totalM * 100.0) else 0.0
                val oppObj = WildRiftRepository.champions.find { it.name.equals(oppName, ignoreCase = true) }
                MatchupRecord(oppName, oppObj?.avatarUrl ?: "", counts.first, counts.second, totalM, wrM)
            }.sortedWith(compareByDescending<MatchupRecord> { it.total }.thenBy { it.winRate })

            // Aliados
            val allyList = allyMap.map { (allyName, counts) ->
                val totalA = counts.first + counts.second
                val wrA = if (totalA > 0) (counts.first.toDouble() / totalA * 100.0) else 0.0
                val allyObj = WildRiftRepository.champions.find { it.name.equals(allyName, ignoreCase = true) }
                AllySynergyRecord(
                    allyName = allyName,
                    allyAvatarUrl = allyObj?.avatarUrl ?: "",
                    role = allyObj?.primaryRole ?: LaneRole.SUPPORT,
                    wins = counts.first,
                    losses = counts.second,
                    total = totalA,
                    winRate = wrA
                )
            }.sortedWith(compareByDescending<AllySynergyRecord> { it.total }.thenByDescending { it.winRate })

            // Asignación de Tier por Win Rate real y Score personal
            val scoresList = matches.filter { it.matchResult.equals("VICTORY", true) || it.matchResult.equals("DEFEAT", true) }
                .mapNotNull { it.myScore.trim().ifBlank { null } }
            val kdaValues = scoresList.mapNotNull { parseKdaFromScore(it) }
            val avgKda = if (kdaValues.isNotEmpty()) kdaValues.average() else 0.0
            val avgScoreStr = if (kdaValues.isNotEmpty()) {
                String.format(java.util.Locale.US, "%.1f KDA", avgKda)
            } else ""

            val bestChamp1v1 = matchupList.filter { it.total >= 5 && it.winRate > 50.0 }.maxWithOrNull(
                compareBy<MatchupRecord> { it.winRate }.thenBy { it.wins }
            )

            // Four neutral prior results reduce the impact of a single win/loss.
            val tierScore = if (totalDecided > 0) (wins + 2.0) / (totalDecided + 4.0) * 100.0 else 0.0

            val tierGrade = when {
                totalDecided < 5 -> TierGrade.PROVISIONAL
                winRate >= 75.0 && wins >= 1 -> TierGrade.S_PLUS
                winRate >= 60.0 && wins >= 1 -> TierGrade.S
                winRate >= 50.0 -> TierGrade.A
                winRate >= 35.0 -> TierGrade.B
                else -> TierGrade.C
            }

            val coachVerdict = generateCoachVerdict(champ.name, winRate, totalDecided, primaryRole, lang) +
                kdaContext(avgKda, kdaValues.size, totalDecided, lang)

            championStatsList.add(
                PersonalChampionStats(
                    championId = champ.id,
                    championName = champ.name,
                    avatarUrl = champ.avatarUrl,
                    primaryRole = primaryRole,
                    totalGames = matches.size,
                    wins = wins,
                    losses = losses,
                    pending = pending,
                    winRate = winRate,
                    tier = tierGrade,
                    tierScore = tierScore,
                    avgEstimatedWr = avgEstimated,
                    roleBreakdown = roleBreakdown,
                    matchups = matchupList,
                    allies = allyList,
                    draftMatches = matches.sortedByDescending { it.timestamp },
                    coachVerdict = coachVerdict,
                    avgScore = avgScoreStr,
                    avgKda = avgKda,
                    best1v1Matchup = bestChamp1v1,
                    kdaGames = kdaValues.size
                )
            )
        }

        // Ordenamos todos los campeones:
        // Primero por Score/Winrate (los que tienen partidas ganadas y alto winrate), luego por partidas jugadas, y los de 0% ordenados alfabéticamente
        val allRanked = championStatsList.sortedWith(
            compareByDescending<PersonalChampionStats> { it.tierScore }
                .thenByDescending { it.winRate }
                .thenByDescending { it.wins }
                .thenByDescending { it.totalGames }
                .thenBy { it.championName }
        )

        // Agrupación por Tiers
        val sPlusList = allRanked.filter { it.tier == TierGrade.S_PLUS }
        val sList = allRanked.filter { it.tier == TierGrade.S }
        val aList = allRanked.filter { it.tier == TierGrade.A }
        val bList = allRanked.filter { it.tier == TierGrade.B }
        val cList = allRanked.filter { it.tier == TierGrade.C }

        // Estadísticas Globales del Historial
        val totalGames = identifiedDrafts.size
        val totalWins = identifiedDrafts.count { it.matchResult.equals("VICTORY", ignoreCase = true) }
        val totalLosses = identifiedDrafts.count { it.matchResult.equals("DEFEAT", ignoreCase = true) }
        val totalPending = totalGames - (totalWins + totalLosses)
        val overallWr = if (totalWins + totalLosses > 0) (totalWins.toDouble() / (totalWins + totalLosses) * 100.0) else 0.0

        val signatureChamp = allRanked.maxWithOrNull(compareBy<PersonalChampionStats> { it.totalGames }
            .thenBy { it.wins + it.losses }.thenBy { it.tierScore })

        // Mejor rol
        val allRoleMatches = identifiedDrafts.filter { recordedRole(it) != null }.groupBy { recordedRole(it)!! }
        val rolePerformances = allRoleMatches.map { (role, dList) ->
            val w = dList.count { it.matchResult.equals("VICTORY", ignoreCase = true) }
            val l = dList.count { it.matchResult.equals("DEFEAT", ignoreCase = true) }
            val tot = w + l
            val wr = if (tot > 0) (w.toDouble() / tot * 100.0) else 0.0
            RolePerformanceRecord(role, w, l, tot, wr)
        }.sortedByDescending { it.total }

        val bestRoleRecord = rolePerformances.filter { it.total >= 5 }.maxByOrNull { (it.wins + 2.0) / (it.total + 4.0) }

        // Nemesis y Mejor Sinergia global
        val allMatchups = championStatsList.flatMap { it.matchups }
            .groupBy { it.opponentName }
            .map { (name, list) ->
                val w = list.sumOf { it.wins }
                val l = list.sumOf { it.losses }
                val tot = w + l
                val wr = if (tot > 0) (w.toDouble() / tot * 100.0) else 0.0
                MatchupRecord(name, list.firstOrNull()?.opponentAvatarUrl ?: "", w, l, tot, wr)
            }
        // 1v1 Matchups globales y análisis del Coach
        val decidedMatchups = allMatchups.filter { it.total >= 5 }
        val top1v1 = decidedMatchups.filter { it.winRate > 50.0 }.sortedWith(
            compareByDescending<MatchupRecord> { it.winRate }
                .thenByDescending { it.wins }
                .thenByDescending { it.total }
        )
        val best1v1 = top1v1.firstOrNull()

        val worst1v1 = decidedMatchups.filter { it.winRate < 50.0 }.sortedWith(
            compareBy<MatchupRecord> { it.winRate }
                .thenByDescending { it.losses }
                .thenByDescending { it.total }
        )
        val nemesis = worst1v1.firstOrNull()

        val allAllies = championStatsList.flatMap { it.allies }
            .groupBy { it.allyName }
            .map { (name, list) ->
                val w = list.sumOf { it.wins }
                val l = list.sumOf { it.losses }
                val tot = w + l
                val wr = if (tot > 0) (w.toDouble() / tot * 100.0) else 0.0
                val f = list.first()
                AllySynergyRecord(name, f.allyAvatarUrl, f.role, w, l, tot, wr)
            }
        val bestAlly = allAllies.filter { it.total >= 5 && it.winRate > 50.0 }.maxByOrNull { (it.wins + 2.0) / (it.total + 4.0) }

        val coach1v1Analysis = generateCoach1v1Analysis(top1v1, worst1v1, best1v1, nemesis, lang)

        val overview = PersonalOverviewStats(
            totalGames = totalGames,
            totalWins = totalWins,
            totalLosses = totalLosses,
            totalPending = totalPending,
            overallWinRate = overallWr,
            signatureChampion = signatureChamp,
            bestRole = bestRoleRecord?.role,
            bestRoleWinRate = bestRoleRecord?.winRate ?: 0.0,
            nemesisOpponent = nemesis,
            bestAllySynergy = bestAlly,
            best1v1Matchup = best1v1,
            top1v1Matchups = top1v1,
            worst1v1Matchups = worst1v1,
            coach1v1Analysis = coach1v1Analysis,
            excludedUnidentifiedGames = activeDrafts.size - identifiedDrafts.size
        )

        return PersonalTierListResult(
            overview = overview,
            tierSPlus = sPlusList,
            tierS = sList,
            tierA = aList,
            tierB = bList,
            tierC = cList,
            allRankedChampions = allRanked,
            roleDistribution = rolePerformances,
            provisionalChampions = allRanked.filter { it.tier == TierGrade.PROVISIONAL }
        )
    }

    private fun recordedRole(draft: SavedDraftEntity): LaneRole? =
        LaneRole.entries.firstOrNull { it.name.equals(draft.userRole.trim(), true) }

    fun draftsForMatchup(drafts: List<SavedDraftEntity>, player: String, opponent: String,
                         role: LaneRole?, profileId: String): List<SavedDraftEntity> = drafts.filter {
        role != null && recordedRole(it) == role && it.accountProfileId == profileId &&
            extractPlayerChampionName(it).equals(player, true) && recordedLaneOpponent(it).equals(opponent, true)
    }

    private fun slots(value: String): List<SavedDraftSlotData> =
        runCatching { json.decodeFromString<List<SavedDraftSlotData>>(value) }.getOrDefault(emptyList())

    private fun validIdentity(id: String, name: String): Boolean =
        !id.equals("empty", true) && !name.equals("empty", true) && (id.isNotBlank() || name.isNotBlank())

    private fun canonicalName(id: String, name: String): String =
        WildRiftRepository.champions.firstOrNull { id.isNotBlank() && it.id.equals(id.trim(), true) }?.name
            ?: WildRiftRepository.champions.firstOrNull { name.isNotBlank() && it.name.equals(name.trim(), true) }?.name
            ?: name.trim().ifBlank { id.trim() }

    fun extractPlayerChampionName(draft: SavedDraftEntity): String {
        if (draft.myChampionId.isNotBlank() || draft.myChampionName.isNotBlank()) {
            return if (validIdentity(draft.myChampionId, draft.myChampionName))
                canonicalName(draft.myChampionId, draft.myChampionName) else ""
        }
        val role = recordedRole(draft) ?: return ""
        val ownSlot = slots(draft.allyPicksJson).filter { it.role.equals(role.name, true) }.singleOrNull()
            ?: return ""
        return if (validIdentity(ownSlot.championId, ownSlot.championName))
            canonicalName(ownSlot.championId, ownSlot.championName) else ""
    }

    private fun extractChampionAvatar(draft: SavedDraftEntity, champName: String): String? {
        return slots(draft.allyPicksJson).find { canonicalName(it.championId, it.championName).equals(champName, true) }?.avatarUrl
    }

    fun recordedLaneOpponent(draft: SavedDraftEntity): String {
        val role = recordedRole(draft) ?: return ""
        if (draft.enemyLaneOpponentName.isNotBlank()) {
            return if (validIdentity("", draft.enemyLaneOpponentName)) canonicalName("", draft.enemyLaneOpponentName) else ""
        }
        val opponent = slots(draft.enemyPicksJson).filter { it.role.equals(role.name, true) }.singleOrNull()
            ?: return ""
        return if (validIdentity(opponent.championId, opponent.championName))
            canonicalName(opponent.championId, opponent.championName) else ""
    }

    private fun extractAllies(draft: SavedDraftEntity, myChampName: String): List<String> {
        return slots(draft.allyPicksJson).filter { validIdentity(it.championId, it.championName) }
            .map { canonicalName(it.championId, it.championName) }.filter { !it.equals(myChampName, true) }
    }

    private fun generateCoachVerdict(champName: String, winRate: Double, games: Int, role: LaneRole, lang: String): String {
        val rate = String.format(java.util.Locale.US, "%.1f", winRate)
        val diagnosis = if (lang == "pt") {
            when {
                games == 0 -> "Diagnóstico: ainda não há resultados finalizados com $champName."
                games < 5 -> "Diagnóstico: $games resultados com $champName ($rate% de vitórias); amostra pequena, classificação provisória."
                else -> "Diagnóstico: $games resultados com $champName ($rate% de vitórias). Isso descreve partidas, não domínio da rota."
            }
        } else {
            when {
                games == 0 -> "Diagnóstico: todavía no hay resultados finalizados con $champName."
                games < 5 -> "Diagnóstico: $games resultados con $champName ($rate% de victorias); muestra pequeña, clasificación provisional."
                else -> "Diagnóstico: $games resultados con $champName ($rate% de victorias). Esto describe partidas, no dominio de la línea."
            }
        }
        return diagnosis + if (lang == "pt") {
            " Decisão: revise uma derrota em ${role.getLocalizedName("pt")} antes de mudar sua seleção. Micro e macro: confira a primeira morte, a posição da onda e o objetivo disponível; este histórico não registra esses eventos. Regra: compare partidas da mesma rota e registre o resultado, sem concluir a causa apenas pela taxa de vitórias."
        } else {
            " Decisión: revisa una derrota en ${role.displayName} antes de cambiar tu selección. Micro y macro: comprueba la primera muerte, la posición de la oleada y el objetivo disponible; este historial no registra esos eventos. Regla: compara partidas de la misma línea y registra el resultado, sin deducir la causa solo por el porcentaje de victorias."
        }
    }

    private fun kdaContext(avg: Double, count: Int, decided: Int, lang: String): String {
        if (count == 0) return if (lang == "pt") " KDA não informado; não entra na análise." else " KDA no informado; no interviene en el análisis."
        val value = String.format(java.util.Locale.US, "%.1f", avg)
        val context = if (lang == "pt") " KDA médio: $value, informado em $count de $decided resultados. "
            else " KDA promedio: $value, informado en $count de $decided resultados. "
        return context + if (avg < 2.0) {
            if (lang == "pt") "Revise mortes antes de objetivos; confirme no replay se houve uma troca útil."
            else "Revisa las muertes antes de objetivos; confirma en la repetición si hubo un intercambio útil."
        } else {
            if (lang == "pt") "Compare eliminações e assistências com objetivos conquistados; o KDA não comprova impacto nem vitória na rota."
            else "Compara eliminaciones y asistencias con objetivos obtenidos; el KDA no demuestra impacto ni victoria en la línea."
        }
    }

    fun parseKdaFromScore(scoreStr: String): Double? {
        val clean = scoreStr.trim()
        val scoreboard = Regex("""([0-9]{1,3})\s*[/ -]\s*([0-9]{1,3})\s*[/ -]\s*([0-9]{1,3})""").matchEntire(clean)
        if (scoreboard != null) {
            val (kills, deaths, assists) = scoreboard.destructured
            return (kills.toDouble() + assists.toDouble()) / deaths.toDouble().coerceAtLeast(1.0)
        }
        // Strict compatibility for older imports storing a ratio, never extract digits from notes.
        val ratio = Regex("""(?:KDA\s+)?([0-9]+(?:[.,][0-9]+)?)(?:\s+KDA)?""", RegexOption.IGNORE_CASE).matchEntire(clean)
            ?: return null
        return ratio.groupValues[1].replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0.0 }
    }

    fun generateCoach1v1Analysis(
        topMatchups: List<MatchupRecord>,
        worstMatchups: List<MatchupRecord>,
        bestMatchup: MatchupRecord?,
        nemesis: MatchupRecord?,
        lang: String = "es"
    ): String {
        val records = listOfNotNull(bestMatchup, nemesis).distinctBy { it.opponentName }.filter { it.total >= 5 }
        if (records.isEmpty()) return if (lang == "pt")
            "Ainda não há cinco resultados contra um mesmo rival de rota. Os resultados são de partidas completas; não comprovam vitórias em duelos 1v1."
            else "Todavía no hay cinco resultados contra un mismo rival de línea. Los resultados son de partidas completas; no demuestran victorias en duelos 1v1."
        val summary = records.joinToString("; ") { "${it.opponentName}: ${it.wins}V - ${it.losses}D (${it.total})" }
        return if (lang == "pt")
            "Diagnóstico: resultados de partidas com estes rivais de rota: $summary. Não são probabilidades de ganhar um duelo. Decisão: revise uma derrota contra o rival com mais resultados. Micro e macro: confira onda, recursos e posição dos dois junglers antes da troca; esses eventos não estão neste histórico. Regra: o resultado da equipe não comprova quem venceu a rota."
        else "Diagnóstico: resultados de partidas con estos rivales de línea: $summary. No son probabilidades de ganar un duelo. Decisión: revisa una derrota contra el rival con más resultados. Micro y macro: comprueba oleada, recursos y posición de ambos junglas antes del intercambio; esos eventos no están en este historial. Regla: el resultado del equipo no demuestra quién ganó la línea."
    }
}
