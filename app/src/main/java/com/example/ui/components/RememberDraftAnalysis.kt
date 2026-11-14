package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import com.example.data.WildRiftRepository
import com.example.model.Champion
import com.example.model.DraftAnalysisResult
import com.example.model.LaneRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

internal data class DraftAnalysisRequest(
    val role: LaneRole?,
    val allies: List<Champion>,
    val enemies: List<Champion>,
    val opponent: Champion?,
    val firstPick: Boolean,
    val language: String,
    val bans: Set<String> = emptySet(),
    val favorites: Set<String> = emptySet(),
    val favoritesOnly: Boolean = false
)

// The activity and overlay share one CPU worker instead of competing with the game.
private val analysisDispatcher = Dispatchers.Default.limitedParallelism(1)
private val pendingAnalysis = DraftAnalysisResult(
    physicalDamagePercent = 0, magicDamagePercent = 0, trueDamagePercent = 0,
    frontlineStatus = "", directMatchupWarning = null, directCounterBestPick = null,
    recommendations = emptyList()
)

internal fun isDraftAnalysisPending(analysis: DraftAnalysisResult): Boolean = analysis === pendingAnalysis

internal suspend fun calculateDraftAnalysis(
    request: DraftAnalysisRequest,
    calculate: (DraftAnalysisRequest) -> DraftAnalysisResult = { input ->
        WildRiftRepository.analyzeDraft(
            myRole = input.role, allies = input.allies, enemies = input.enemies,
            enemyLaneOpponent = input.opponent, isFirstPick = input.firstPick,
            lang = input.language, bannedChampionIds = input.bans,
            favoriteChampionIds = input.favorites,
            favoriteOnlyRecommendations = input.favoritesOnly
        )
    }
): DraftAnalysisResult = withContext(analysisDispatcher) {
    ensureActive()
    calculate(request).also { ensureActive() }
}

/** Immutable inputs; cancelled requests cannot publish recommendations for an older draft. */
@Composable
internal fun rememberDraftAnalysis(request: DraftAnalysisRequest): DraftAnalysisResult = key(request) {
    val analysis by produceState(pendingAnalysis, request) {
        value = pendingAnalysis
        value = calculateDraftAnalysis(request)
    }
    analysis
}
