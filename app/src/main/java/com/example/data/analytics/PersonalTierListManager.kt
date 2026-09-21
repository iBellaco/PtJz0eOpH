package com.example.data.analytics

import com.example.data.WildRiftRepository
import com.example.data.local.entity.SavedDraftEntity
import com.example.data.local.entity.SavedDraftSlotData
import com.example.data.repository.DraftHistoryRepository
import com.example.model.Champion
import com.example.model.LaneRole
import kotlinx.serialization.json.Json

enum class TierGrade(val label: String, val description: String, val minWinRate: Double) {
    S_PLUS("S+", "God Tier / Dominio Absoluto", 75.0),
    S("S", "Élite / Alto Rendimiento", 60.0),
    A("A", "Sólido / Competitivo", 50.0),
    B("B", "En Aprendizaje / Irregular", 35.0),
    C("C", "Bajo Rendimiento / Requiere Práctica", 0.0)
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
    val coachVerdict: String
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
    val bestAllySynergy: AllySynergyRecord?
)

data class PersonalTierListResult(
    val overview: PersonalOverviewStats,
    val tierSPlus: List<PersonalChampionStats>,
    val tierS: List<PersonalChampionStats>,
    val tierA: List<PersonalChampionStats>,
    val tierB: List<PersonalChampionStats>,
    val tierC: List<PersonalChampionStats>,
    val allRankedChampions: List<PersonalChampionStats>,
    val roleDistribution: List<RolePerformanceRecord>
)

object PersonalTierListManager {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Calcula la Tier List Personal y las estadísticas detalladas del jugador
     * basadas en su historial de drafts y partidas guardadas.
     * Si un campeón no tiene partidas registradas, se ubica en el Tier C con 0% Win Rate.
     * A medida que se registran victorias y derrotas, los campeones se reordenan
     * dinámicamente por Win Rate y volumen de juego.
     */
    fun calculatePersonalTierList(
        drafts: List<SavedDraftEntity>,
        roleFilter: LaneRole? = null,
        lang: String = "es",
        modeFilter: String = "ALL" // "ALL", "RANKED", "LEGENDARY"
    ): PersonalTierListResult {
        // Filtramos drafts por modo (Normal vs Legendaria)
        val modeFilteredDrafts = when (modeFilter.uppercase()) {
            "LEGENDARY" -> drafts.filter { it.isLegendary }
            "RANKED" -> drafts.filter { !it.isLegendary }
            else -> drafts
        }

        // Filtramos drafts si hay rol activo
        val activeDrafts = if (roleFilter != null) {
            modeFilteredDrafts.filter { draft ->
                val draftRole = try { LaneRole.valueOf(draft.userRole) } catch (_: Exception) { LaneRole.MID }
                draftRole == roleFilter
            }
        } else modeFilteredDrafts

        // Mapa de datos por campeón: Nombre del campeón (en minúsculas/normalizado) -> Lista de partidas
        val champDraftsMap = mutableMapOf<String, MutableList<SavedDraftEntity>>()

        for (draft in activeDrafts) {
            val champName = extractPlayerChampionName(draft)
            if (champName.isNotBlank()) {
                champDraftsMap.getOrPut(champName.lowercase()) { mutableListOf() }.add(draft)
            }
        }

        // Obtener el catálogo base de campeones según el filtro de rol
        val baseChampions = if (roleFilter != null) {
            WildRiftRepository.champions.filter { champ ->
                champ.primaryRole == roleFilter || champ.secondaryRoles.contains(roleFilter) ||
                        champDraftsMap.containsKey(champ.name.lowercase()) ||
                        champDraftsMap.containsKey(champ.id.lowercase())
            }
        } else {
            WildRiftRepository.champions
        }

        val championStatsList = mutableListOf<PersonalChampionStats>()
        val processedKeys = mutableSetOf<String>()

        // 1. Procesar todos los campeones del catálogo de Wild Rift
        for (champ in baseChampions) {
            processedKeys.add(champ.name.lowercase())
            processedKeys.add(champ.id.lowercase())

            val matches = champDraftsMap[champ.name.lowercase()]
                ?: champDraftsMap[champ.id.lowercase()]
                ?: emptyList()

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

                val role = try { LaneRole.valueOf(match.userRole) } catch (_: Exception) { champ.primaryRole }
                val currentRoleStats = roleCountMap.getOrDefault(role, Pair(0, 0))
                roleCountMap[role] = Pair(
                    currentRoleStats.first + (if (isWin) 1 else 0),
                    currentRoleStats.second + (if (isLoss) 1 else 0)
                )

                // Matchup directo (rival en línea)
                val enemyOpponent = match.enemyLaneOpponentName.ifBlank { extractLaneOpponent(match, role) }
                if (enemyOpponent.isNotBlank()) {
                    val currentM = matchupMap.getOrDefault(enemyOpponent, Pair(0, 0))
                    matchupMap[enemyOpponent] = Pair(
                        currentM.first + (if (isWin) 1 else 0),
                        currentM.second + (if (isLoss) 1 else 0)
                    )
                }

                // Aliados en partida
                val allies = extractAllies(match, champ.name)
                for (ally in allies) {
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

            // Asignación de Tier por Win Rate real
            val volumeBonus = Math.min(15.0, totalDecided * 2.0)
            val tierScore = if (totalDecided > 0) (winRate + volumeBonus) else 0.0

            val tierGrade = when {
                totalDecided == 0 -> TierGrade.C // Sin partidas decididas se ubica en el escalón más bajo con 0% WR
                winRate >= 75.0 && wins >= 1 -> TierGrade.S_PLUS
                winRate >= 60.0 && wins >= 1 -> TierGrade.S
                winRate >= 50.0 -> TierGrade.A
                winRate >= 35.0 -> TierGrade.B
                else -> TierGrade.C
            }

            val coachVerdict = generateCoachVerdict(champ.name, winRate, totalDecided, primaryRole, lang)

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
                    coachVerdict = coachVerdict
                )
            )
        }

        // 2. Procesar cualquier campeón extra presente en drafts que no estuviera en el catálogo
        for ((champKey, matches) in champDraftsMap) {
            if (!processedKeys.contains(champKey)) {
                val firstMatch = matches.first()
                val champName = firstMatch.myChampionName.ifBlank { champKey.replaceFirstChar { it.uppercase() } }
                val champObj = WildRiftRepository.champions.find { it.name.equals(champName, ignoreCase = true) }
                val avatarUrl = champObj?.avatarUrl ?: matches.firstNotNullOfOrNull { extractChampionAvatar(it, champName) } ?: ""

                var wins = 0
                var losses = 0
                var pending = 0
                var sumEstimatedWr = 0.0

                for (match in matches) {
                    val isWin = match.matchResult.equals("VICTORY", ignoreCase = true)
                    val isLoss = match.matchResult.equals("DEFEAT", ignoreCase = true)
                    if (isWin) wins++ else if (isLoss) losses++ else pending++
                    sumEstimatedWr += match.estimatedWinrate
                }

                val totalDecided = wins + losses
                val winRate = if (totalDecided > 0) (wins.toDouble() / totalDecided * 100.0) else 0.0
                val primaryRole = try { LaneRole.valueOf(firstMatch.userRole) } catch (_: Exception) { LaneRole.MID }
                val volumeBonus = Math.min(15.0, totalDecided * 2.0)
                val tierScore = if (totalDecided > 0) (winRate + volumeBonus) else 0.0

                val tierGrade = when {
                    totalDecided == 0 -> TierGrade.C
                    winRate >= 75.0 && wins >= 1 -> TierGrade.S_PLUS
                    winRate >= 60.0 && wins >= 1 -> TierGrade.S
                    winRate >= 50.0 -> TierGrade.A
                    winRate >= 35.0 -> TierGrade.B
                    else -> TierGrade.C
                }

                val coachVerdict = generateCoachVerdict(champName, winRate, totalDecided, primaryRole, lang)

                championStatsList.add(
                    PersonalChampionStats(
                        championId = firstMatch.myChampionId.ifBlank { champKey },
                        championName = champName,
                        avatarUrl = avatarUrl,
                        primaryRole = primaryRole,
                        totalGames = matches.size,
                        wins = wins,
                        losses = losses,
                        pending = pending,
                        winRate = winRate,
                        tier = tierGrade,
                        tierScore = tierScore,
                        avgEstimatedWr = if (matches.isNotEmpty()) sumEstimatedWr / matches.size else 50.0,
                        roleBreakdown = emptyList(),
                        matchups = emptyList(),
                        allies = emptyList(),
                        draftMatches = matches.sortedByDescending { it.timestamp },
                        coachVerdict = coachVerdict
                    )
                )
            }
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
        val totalGames = activeDrafts.size
        val totalWins = activeDrafts.count { it.matchResult.equals("VICTORY", ignoreCase = true) }
        val totalLosses = activeDrafts.count { it.matchResult.equals("DEFEAT", ignoreCase = true) }
        val totalPending = totalGames - (totalWins + totalLosses)
        val overallWr = if (totalWins + totalLosses > 0) (totalWins.toDouble() / (totalWins + totalLosses) * 100.0) else 0.0

        val signatureChamp = allRanked.firstOrNull { it.totalGames >= 1 && (it.wins + it.losses > 0) && it.winRate >= 50.0 }
            ?: allRanked.firstOrNull { it.totalGames >= 1 }

        // Mejor rol
        val allRoleMatches = activeDrafts.groupBy {
            try { LaneRole.valueOf(it.userRole) } catch (_: Exception) { LaneRole.MID }
        }
        val rolePerformances = allRoleMatches.map { (role, dList) ->
            val w = dList.count { it.matchResult.equals("VICTORY", ignoreCase = true) }
            val l = dList.count { it.matchResult.equals("DEFEAT", ignoreCase = true) }
            val tot = w + l
            val wr = if (tot > 0) (w.toDouble() / tot * 100.0) else 0.0
            RolePerformanceRecord(role, w, l, tot, wr)
        }.sortedByDescending { it.total }

        val bestRoleRecord = rolePerformances.filter { it.total > 0 }.maxByOrNull { it.winRate * 0.7 + (it.total * 3.0) }

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
        val nemesis = allMatchups.filter { it.losses >= 1 }.minByOrNull { it.winRate }

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
        val bestAlly = allAllies.filter { it.wins >= 1 }.maxByOrNull { it.winRate * 0.6 + it.total * 4.0 }

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
            bestAllySynergy = bestAlly
        )

        return PersonalTierListResult(
            overview = overview,
            tierSPlus = sPlusList,
            tierS = sList,
            tierA = aList,
            tierB = bList,
            tierC = cList,
            allRankedChampions = allRanked,
            roleDistribution = rolePerformances
        )
    }

    private fun extractPlayerChampionName(draft: SavedDraftEntity): String {
        if (draft.myChampionName.isNotBlank()) return draft.myChampionName
        val allies = DraftHistoryRepository.parseDraftSlots(draft.allyPicksJson)
        val userRole = try { LaneRole.valueOf(draft.userRole) } catch (_: Exception) { LaneRole.MID }
        val mySlot = allies.find { it.assignedRole == userRole } ?: allies.firstOrNull()
        return mySlot?.champion?.name ?: ""
    }

    private fun extractChampionAvatar(draft: SavedDraftEntity, champName: String): String? {
        val allies = DraftHistoryRepository.parseDraftSlots(draft.allyPicksJson)
        return allies.find { it.champion.name.equals(champName, ignoreCase = true) }?.champion?.avatarUrl
    }

    private fun extractLaneOpponent(draft: SavedDraftEntity, userRole: LaneRole): String {
        val enemies = DraftHistoryRepository.parseDraftSlots(draft.enemyPicksJson)
        val opp = enemies.find { it.assignedRole == userRole } ?: enemies.firstOrNull()
        return opp?.champion?.name ?: ""
    }

    private fun extractAllies(draft: SavedDraftEntity, myChampName: String): List<String> {
        val allies = DraftHistoryRepository.parseDraftSlots(draft.allyPicksJson)
        return allies.map { it.champion.name }.filter { !it.equals(myChampName, ignoreCase = true) }
    }

    private fun generateCoachVerdict(champName: String, winRate: Double, games: Int, role: LaneRole, lang: String): String {
        return when {
            games == 0 -> {
                if (lang == "en") "No finished matches recorded yet. Mark wins or defeats to unlock tactical grade."
                else if (lang == "pt") "Nenhuma partida finalizada registrada ainda. Marque vitórias ou derrotas."
                else "Aún no hay partidas finalizadas registradas. Marca victorias o derrotas para desbloquear tu calificación."
            }
            winRate >= 75.0 -> {
                if (lang == "en") "⚡ God Tier Pick! Exceptional mastery in ${role.displayName}. Safe pick for climbing Soberano ranks."
                else if (lang == "pt") "⚡ God Tier Pick! Maestria excepcional em ${role.displayName}. Pick seguro para subir elo."
                else "⚡ ¡Pick Imparable! Maestría absoluta en ${role.displayName}. Es tu mejor herramienta para subir a Soberano."
            }
            winRate >= 60.0 -> {
                if (lang == "en") "🔥 Elite Performance in ${role.displayName}. Strong lane pressure and high win condition conversion."
                else if (lang == "pt") "🔥 Desempenho de elite em ${role.displayName}. Alta pressão de rota e conversão de vitórias."
                else "🔥 Rendimiento de Élite en ${role.displayName}. Gran impacto en escaramuzas tempranas y peleas por Dragón."
            }
            winRate >= 50.0 -> {
                if (lang == "en") "🛡️ Solid & Competitive. Refine H1/H2 wave control and objective rotation timings to reach 65%+."
                else if (lang == "pt") "🛡️ Sólido e competitivo. Refine o controle de ondas com H1/H2 para superar 65%."
                else "🛡️ Rendimiento Sólido. Ajusta la gestión de oleadas con H1/H2 y rotaciones a Heraldo para elevar tu winrate al 65%+."
            }
            winRate >= 35.0 -> {
                if (lang == "en") "⚠️ Volatile Results. Needs deeper wave state awareness and match-specific situational items."
                else if (lang == "pt") "⚠️ Resultados instáveis. Requer atenção à itemização situacional e posicionamento."
                else "⚠️ Resultados Irregulares. Revisa tu itemización defensiva situacional y evita pelear antes de tu pico de poder con H4."
            }
            else -> {
                if (lang == "en") "❌ Critical Struggle. High death count or negative matchups. Practice trading patterns in normal matches."
                else if (lang == "pt") "❌ Dificuldade alta. Pratique combos e trocas em partidas normais antes de ranquear."
                else "❌ Dificultad en Fase de Líneas. Evita enfrentamientos desfavorables sin visión y practica combos clave en partidas normales."
            }
        }
    }
}
