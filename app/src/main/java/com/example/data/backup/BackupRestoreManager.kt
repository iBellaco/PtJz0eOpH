package com.example.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    internal const val MAX_BACKUP_BYTES = 8 * 1024 * 1024
    private val restoreMutex = Mutex()

    internal fun validatePayload(payload: WrTierListBackupPayload) {
        require(payload.version == 1 && payload.app == "WildRiftCoach")
        require(payload.profiles.size <= 100 && payload.drafts.size <= 10_000)
        require(payload.profiles.map { it.id }.distinct().size == payload.profiles.size)
        require(payload.profiles.all { it.id.isNotBlank() && it.id.length <= 256 && it.name.length <= 256 })
        val ids = payload.drafts.filter { it.id != 0L }.map { it.id }
        require(ids.distinct().size == ids.size)
        require(payload.drafts.all {
            it.id >= 0 && it.timestamp >= 0 && it.estimatedWinrate.isFinite() &&
                it.estimatedWinrate in 0.0..100.0 && it.notes.length <= 100_000 &&
                it.allyPicksJson.length <= 100_000 && it.enemyPicksJson.length <= 100_000 &&
                json.parseToJsonElement(it.allyPicksJson) is kotlinx.serialization.json.JsonArray &&
                json.parseToJsonElement(it.enemyPicksJson) is kotlinx.serialization.json.JsonArray
        })
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = false
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
        restoreMutex.withLock {
        try {
            require(rawJson.length <= MAX_BACKUP_BYTES)
            require(rawJson.toByteArray(Charsets.UTF_8).size <= MAX_BACKUP_BYTES)
            val payload = json.decodeFromString<WrTierListBackupPayload>(rawJson)
            validatePayload(payload)
            if (payload.profiles.isEmpty() && payload.drafts.isEmpty()) {
                return@withLock ImportResult(false, 0, 0, "El archivo no contiene perfiles ni historiales válidos.")
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

            // 2. Process drafts
            val db = AppDatabase.getDatabase(context)
            val draftDao = db.draftDao()

            var draftsAddedCount = 0
            db.withTransaction {
            if (!merge) {
                draftDao.clearAllDrafts()
            }

            val existingDrafts = draftDao.getAllDrafts().firstOrNull() ?: emptyList()
            val known = existingDrafts.map { Triple(it.timestamp, it.myChampionName, it.userRole) }.toMutableSet()

            payload.drafts.forEach { item ->
                val key = Triple(item.timestamp, item.myChampionName, item.userRole)
                val alreadyExists = merge && !known.add(key)

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
            }
            AccountProfileManager.importProfiles(context, finalProfiles, payload.activeProfileId)

            ImportResult(
                success = true,
                profilesImported = payload.profiles.size,
                draftsImported = draftsAddedCount,
                message = "Restauración completada con éxito."
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            ImportResult(
                success = false,
                message = "No se pudo restaurar el respaldo. Comprueba su formato, versión y tamaño."
            )
        }
        }
    }

    /**
     * Reads text content from a content Uri (SAF picker).
     */
    suspend fun readTextFromUri(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri) 
            ?: throw IllegalStateException("No se pudo abrir el archivo.")
        BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
            val content = StringBuilder()
            val buffer = CharArray(8192)
            while (true) {
                val count = reader.read(buffer)
                if (count < 0) break
                require(content.length + count <= MAX_BACKUP_BYTES) { "El respaldo supera el tamaño permitido." }
                content.append(buffer, 0, count)
            }
            content.toString().also {
                require(it.toByteArray(Charsets.UTF_8).size <= MAX_BACKUP_BYTES) { "El respaldo supera el tamaño permitido." }
            }
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
