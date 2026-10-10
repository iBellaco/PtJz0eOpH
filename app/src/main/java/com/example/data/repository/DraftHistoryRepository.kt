package com.example.data.repository

import android.content.Context
import com.example.data.AccountProfileManager
import com.example.data.WildRiftRepository
import com.example.data.local.AppDatabase
import com.example.data.local.entity.SavedDraftEntity
import com.example.data.local.entity.SavedDraftSlotData
import com.example.model.DraftAnalysisResult
import com.example.model.DraftSlot
import com.example.model.Champion
import com.example.model.LaneRole
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object DraftHistoryRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    fun getAllDrafts(context: Context): Flow<List<SavedDraftEntity>> {
        return AppDatabase.getDatabase(context).draftDao().getAllDrafts()
    }

    fun getDraftsByProfile(context: Context, profileId: String): Flow<List<SavedDraftEntity>> {
        return if (profileId == "ALL" || profileId.isBlank()) {
            AppDatabase.getDatabase(context).draftDao().getAllDrafts()
        } else {
            AppDatabase.getDatabase(context).draftDao().getDraftsByProfile(profileId)
        }
    }

    suspend fun getDraftById(context: Context, id: Long): SavedDraftEntity? {
        return AppDatabase.getDatabase(context).draftDao().getDraftById(id)
    }

    suspend fun checkDraftExists(
        context: Context,
        myRole: LaneRole,
        allies: List<DraftSlot>,
        enemies: List<DraftSlot>,
        accountProfileId: String? = null
    ): Boolean {
        val activeProfile = AccountProfileManager.getActiveProfile(context)
        val profileId = accountProfileId ?: activeProfile.id
        
        val allyDataList = allies.map {
            SavedDraftSlotData(
                championId = it.champion.id,
                championName = it.champion.name,
                role = it.assignedRole.name,
                avatarUrl = it.champion.avatarUrl
            )
        }
        val enemyDataList = enemies.map {
            SavedDraftSlotData(
                championId = it.champion.id,
                championName = it.champion.name,
                role = it.assignedRole.name,
                avatarUrl = it.champion.avatarUrl
            )
        }
        
        val allyPicksJson = json.encodeToString(allyDataList)
        val enemyPicksJson = json.encodeToString(enemyDataList)
        
        val draftDao = AppDatabase.getDatabase(context).draftDao()
        val existingDraft = draftDao.findExistingDraft(
            profileId = profileId,
            userRole = myRole.name,
            allyPicksJson = allyPicksJson,
            enemyPicksJson = enemyPicksJson
        )
        return existingDraft != null
    }

    suspend fun saveDraft(
        context: Context,
        myRole: LaneRole,
        isFirstPick: Boolean,
        allies: List<DraftSlot>,
        enemies: List<DraftSlot>,
        analysis: DraftAnalysisResult,
        title: String? = null,
        notes: String = "",
        matchResult: String = "PENDING",
        accountProfileId: String? = null,
        accountProfileName: String? = null,
        isLegendary: Boolean = false,
        matchMode: String = if (isLegendary) "LEGENDARY" else "RANKED",
        myScore: String = "",
        allowDuplicate: Boolean = false
    ): Long {
        val normalizedScore = com.example.data.DraftScoreFormat.normalize(myScore)
        require(matchMode in setOf("RANKED", "LEGENDARY")) { "Modo de partida inválido" }
        val activeProfile = AccountProfileManager.getActiveProfile(context)
        val profileId = accountProfileId ?: activeProfile.id
        val profileName = accountProfileName ?: activeProfile.name

        val allyDataList = allies.map {
            SavedDraftSlotData(
                championId = it.champion.id,
                championName = it.champion.name,
                role = it.assignedRole.name,
                avatarUrl = it.champion.avatarUrl
            )
        }

        val enemyDataList = enemies.map {
            SavedDraftSlotData(
                championId = it.champion.id,
                championName = it.champion.name,
                role = it.assignedRole.name,
                avatarUrl = it.champion.avatarUrl
            )
        }

        val myChampion = allies.filter { it.assignedRole == myRole }.singleOrNull()?.champion
            ?.takeUnless { it.id.isBlank() || it.id.equals("empty", true) }
        val enemyLaneOpponent = enemies.filter { it.assignedRole == myRole }.singleOrNull()?.champion
            ?.takeUnless { it.id.isBlank() || it.id.equals("empty", true) }
        
        val bestPick = analysis.bestOverallPick ?: analysis.recommendations.firstOrNull()
        val estimatedWr = if (myChampion != null) {
            val eval = WildRiftRepository.evaluateChampion(
                champ = myChampion,
                myRole = myRole,
                allies = allies.map { it.champion },
                enemies = enemies.map { it.champion },
                enemyLaneOpponent = enemyLaneOpponent
            )
            eval.estimatedWinrate
        } else {
            bestPick?.estimatedWinrate ?: 50.0
        }

        val defaultTitle = if (myChampion != null && enemyLaneOpponent != null) {
            "${myChampion.name} vs ${enemyLaneOpponent.name} (${myRole.displayName})"
        } else if (myChampion != null) {
            "${myChampion.name} (${myRole.displayName})"
        } else {
            "Draft en ${myRole.displayName}"
        }

        val entity = SavedDraftEntity(
            title = title?.ifBlank { defaultTitle } ?: defaultTitle,
            timestamp = System.currentTimeMillis(),
            userRole = myRole.name,
            isFirstPick = isFirstPick,
            allyPicksJson = json.encodeToString(allyDataList),
            enemyPicksJson = json.encodeToString(enemyDataList),
            myChampionId = myChampion?.id ?: "",
            myChampionName = myChampion?.name ?: "",
            enemyLaneOpponentName = enemyLaneOpponent?.name ?: "",
            estimatedWinrate = estimatedWr,
            matchResult = matchResult,
            allyDamagePhysical = analysis.allyPhysicalDamagePercent,
            allyDamageMagic = analysis.allyMagicDamagePercent,
            allyDamageTrue = analysis.allyTrueDamagePercent,
            enemyDamagePhysical = analysis.physicalDamagePercent,
            enemyDamageMagic = analysis.magicDamagePercent,
            enemyDamageTrue = analysis.trueDamagePercent,
            winConditionNotes = analysis.directMatchupWarning ?: analysis.allyCompositionWarning ?: "",
            directMatchupWarning = analysis.directMatchupWarning ?: "",
            notes = notes,
            accountProfileId = profileId,
            accountProfileName = profileName,
            isLegendary = isLegendary || matchMode == "LEGENDARY",
            matchMode = matchMode,
            myScore = normalizedScore
        )

        val draftDao = AppDatabase.getDatabase(context).draftDao()
        val existingDraft = if (!allowDuplicate) {
            draftDao.findExistingDraft(
                profileId = profileId,
                userRole = myRole.name,
                allyPicksJson = entity.allyPicksJson,
                enemyPicksJson = entity.enemyPicksJson
            )
        } else {
            null // Al confirmar guardar duplicado, se inserta una partida nueva e independiente
        }

        return if (existingDraft != null) {
            val updated = existingDraft.copy(
                title = entity.title,
                notes = if (notes.isNotBlank()) notes else existingDraft.notes,
                matchResult = matchResult,
                myChampionId = entity.myChampionId,
                myChampionName = entity.myChampionName,
                enemyLaneOpponentName = entity.enemyLaneOpponentName,
                estimatedWinrate = estimatedWr,
                timestamp = System.currentTimeMillis(),
                accountProfileName = profileName,
                isLegendary = isLegendary || matchMode == "LEGENDARY",
                matchMode = matchMode,
                myScore = if (normalizedScore.isNotBlank()) normalizedScore else existingDraft.myScore
            )
            draftDao.updateDraft(updated)
            existingDraft.id
        } else {
            val freshEntity = if (allowDuplicate) {
                entity.copy(
                    id = 0L,
                    timestamp = System.currentTimeMillis()
                )
            } else {
                entity
            }
            draftDao.insertDraft(freshEntity)
        }
    }

    suspend fun updateMatchResult(context: Context, id: Long, result: String) {
        AppDatabase.getDatabase(context).draftDao().updateMatchResult(id, result)
    }

    suspend fun updateNotes(context: Context, id: Long, notes: String) {
        AppDatabase.getDatabase(context).draftDao().updateNotes(id, notes)
    }

    suspend fun updateAccountProfile(context: Context, id: Long, profileId: String, profileName: String) {
        AppDatabase.getDatabase(context).draftDao().updateAccountProfile(id, profileId, profileName)
    }

    suspend fun deleteDraft(context: Context, id: Long) {
        AppDatabase.getDatabase(context).draftDao().deleteDraftById(id)
    }

    suspend fun clearDraftsByProfile(context: Context, profileId: String) {
        AppDatabase.getDatabase(context).draftDao().clearDraftsByProfile(profileId)
    }

    suspend fun clearAllDrafts(context: Context) {
        AppDatabase.getDatabase(context).draftDao().clearAllDrafts()
    }

    fun parseDraftSlots(jsonString: String): List<DraftSlot> {
        return try {
            val dataList = json.decodeFromString<List<SavedDraftSlotData>>(jsonString)
            dataList.mapNotNull { data ->
                val champ = WildRiftRepository.getChampionById(data.championId)
                    ?: WildRiftRepository.champions.find { it.name.equals(data.championName, ignoreCase = true) }
                val role = try {
                    LaneRole.valueOf(data.role)
                } catch (_: Exception) {
                    champ?.primaryRole ?: LaneRole.MID
                }
                if (champ != null) {
                    DraftSlot(champ, role)
                } else null
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
