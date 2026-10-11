package com.example.data.backup

import android.content.Context
import android.net.Uri
import com.example.data.AccountProfile
import com.example.data.AccountProfileManager
import com.example.data.local.AppDatabase
import com.example.data.local.entity.SavedDraftEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class BackupDraftItem(
    val id: Long = 0,
    val title: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val userRole: String = "MID",
    val isFirstPick: Boolean = false,
    val allyPicksJson: String = "[]",
    val enemyPicksJson: String = "[]",
    val myChampionId: String = "",
    val myChampionName: String = "",
    val enemyLaneOpponentName: String = "",
    val estimatedWinrate: Double = 50.0,
    val matchResult: String = "VICTORY",
    val allyDamagePhysical: Int = 0,
    val allyDamageMagic: Int = 0,
    val allyDamageTrue: Int = 0,
    val enemyDamagePhysical: Int = 0,
    val enemyDamageMagic: Int = 0,
    val enemyDamageTrue: Int = 0,
    val winConditionNotes: String = "",
    val directMatchupWarning: String = "",
    val notes: String = "",
    val accountProfileId: String = "default",
    val accountProfileName: String = "Cuenta Principal",
    val isLegendary: Boolean = false,
    val matchMode: String = "RANKED",
    val myScore: String = ""
)

@Serializable
data class WrTierListBackupPayload(
    val version: Int = 1,
    val app: String = "WildRiftCoach",
    val exportDate: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val totalProfiles: Int = 0,
    val totalDrafts: Int = 0,
    val activeProfileId: String = "default",
    val profiles: List<AccountProfile> = emptyList(),
    val drafts: List<BackupDraftItem> = emptyList()
)

data class ImportResult(
    val success: Boolean,
    val profilesImported: Int = 0,
    val draftsImported: Int = 0,
    val message: String = ""
)

object BackupRestoreManager {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = true
    }

    /**
     * Generates a complete JSON backup of all profiles, active settings,
     * and saved draft history (which feeds the Personal Tier List).
     */
    suspend fun generateBackupJson(context: Context): String = withContext(Dispatchers.IO) {
        AccountProfileManager.init(context)
        val profiles = AccountProfileManager.allProfiles.value
        val activeProfileId = AccountProfileManager.activeProfileId.value
        
        val db = AppDatabase.getDatabase(context)
        val allDrafts = db.draftDao().getAllDrafts().firstOrNull() ?: emptyList()

        val backupDraftItems = allDrafts.map { entity ->
            BackupDraftItem(
                id = entity.id,
                title = entity.title,
                timestamp = entity.timestamp,
                userRole = entity.userRole,
                isFirstPick = entity.isFirstPick,
                allyPicksJson = entity.allyPicksJson,
                enemyPicksJson = entity.enemyPicksJson,
                myChampionId = entity.myChampionId,
                myChampionName = entity.myChampionName,
                enemyLaneOpponentName = entity.enemyLaneOpponentName,
                estimatedWinrate = entity.estimatedWinrate,
                matchResult = entity.matchResult,
                allyDamagePhysical = entity.allyDamagePhysical,
                allyDamageMagic = entity.allyDamageMagic,
                allyDamageTrue = entity.allyDamageTrue,
                enemyDamagePhysical = entity.enemyDamagePhysical,
                enemyDamageMagic = entity.enemyDamageMagic,
                enemyDamageTrue = entity.enemyDamageTrue,
                winConditionNotes = entity.winConditionNotes,
                directMatchupWarning = entity.directMatchupWarning,
                notes = entity.notes,
                accountProfileId = entity.accountProfileId,
                accountProfileName = entity.accountProfileName,
                isLegendary = entity.isLegendary,
                matchMode = entity.matchMode,
                myScore = entity.myScore
            )
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val payload = WrTierListBackupPayload(
            version = 1,
            app = "WildRiftCoach",
            exportDate = dateFormat.format(Date()),
            timestamp = System.currentTimeMillis(),
            totalProfiles = profiles.size,
            totalDrafts = backupDraftItems.size,
            activeProfileId = activeProfileId,
            profiles = profiles,
            drafts = backupDraftItems
        )

        json.encodeToString(payload)
    }

    /**
     * Imports profiles and drafts from a JSON string.
     * @param merge If true, keeps existing profiles & drafts and adds non-duplicate entries.
     *              If false, replaces all current profiles and drafts with the backup data.
     */
    suspend fun restoreFromJson(context: Context, rawJson: String, merge: Boolean = true): ImportResult = withContext(Dispatchers.IO) {
        try {
            val payload = json.decodeFromString<WrTierListBackupPayload>(rawJson)
            if (payload.profiles.isEmpty() && payload.drafts.isEmpty()) {
                return@withContext ImportResult(false, 0, 0, "El archivo no contiene perfiles ni historiales válidos.")
            }

            AccountProfileManager.init(context)
            val currentProfiles = AccountProfileManager.allProfiles.value.toMutableList()

            // 1. Process profiles
            val finalProfiles = if (merge) {
                val profileMap = currentProfiles.associateBy { it.id }.toMutableMap()
                payload.profiles.forEach { importedProf ->
                    if (!profileMap.containsKey(importedProf.id)) {
                        profileMap[importedProf.id] = importedProf
                    }
                }
                profileMap.values.toList()
            } else {
                if (payload.profiles.isNotEmpty()) payload.profiles else currentProfiles
            }

            AccountProfileManager.importProfiles(context, finalProfiles, payload.activeProfileId)

            // 2. Process drafts
            val db = AppDatabase.getDatabase(context)
            val draftDao = db.draftDao()

            if (!merge) {
                draftDao.clearAllDrafts()
            }

            val existingDrafts = draftDao.getAllDrafts().firstOrNull() ?: emptyList()
            var draftsAddedCount = 0

            payload.drafts.forEach { item ->
                val alreadyExists = merge && existingDrafts.any { 
                    it.timestamp == item.timestamp && it.myChampionName == item.myChampionName && it.userRole == item.userRole 
                }

                if (!alreadyExists) {
                    val entity = SavedDraftEntity(
                        id = if (merge) 0 else item.id, // Auto-generate id on merge
                        title = item.title,
                        timestamp = item.timestamp,
                        userRole = item.userRole,
                        isFirstPick = item.isFirstPick,
                        allyPicksJson = item.allyPicksJson,
                        enemyPicksJson = item.enemyPicksJson,
                        myChampionId = item.myChampionId,
                        myChampionName = item.myChampionName,
                        enemyLaneOpponentName = item.enemyLaneOpponentName,
                        estimatedWinrate = item.estimatedWinrate,
                        matchResult = item.matchResult,
                        allyDamagePhysical = item.allyDamagePhysical,
                        allyDamageMagic = item.allyDamageMagic,
                        allyDamageTrue = item.allyDamageTrue,
                        enemyDamagePhysical = item.enemyDamagePhysical,
                        enemyDamageMagic = item.enemyDamageMagic,
                        enemyDamageTrue = item.enemyDamageTrue,
                        winConditionNotes = item.winConditionNotes,
                        directMatchupWarning = item.directMatchupWarning,
                        notes = item.notes,
                        accountProfileId = item.accountProfileId,
                        accountProfileName = item.accountProfileName,
                        isLegendary = item.isLegendary,
                        matchMode = item.matchMode,
                        myScore = com.example.data.DraftScoreFormat.normalize(item.myScore)
                    )
                    draftDao.insertDraft(entity)
                    draftsAddedCount++
                }
            }

            ImportResult(
                success = true,
                profilesImported = payload.profiles.size,
                draftsImported = draftsAddedCount,
                message = "Restauración completada con éxito."
            )
        } catch (e: Exception) {
            ImportResult(
                success = false,
                message = "Error al procesar el archivo JSON: ${e.localizedMessage ?: "Formato inválido"}"
            )
        }
    }

    /**
     * Reads text content from a content Uri (SAF picker).
     */
    suspend fun readTextFromUri(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri) 
            ?: throw IllegalStateException("No se pudo abrir el archivo.")
        BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
            reader.readText()
        }
    }

    /**
     * Writes text content to a content Uri (SAF save picker).
     */
    suspend fun writeTextToUri(context: Context, uri: Uri, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val outputStream = context.contentResolver.openOutputStream(uri)
                ?: throw IllegalStateException("No se pudo abrir el destino para guardar.")
            outputStream.use { os ->
                os.write(content.toByteArray(Charsets.UTF_8))
                os.flush()
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
