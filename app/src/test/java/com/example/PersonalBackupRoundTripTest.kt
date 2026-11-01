package com.example

import android.app.Application
import com.example.data.backup.BackupRestoreManager
import com.example.data.local.AppDatabase
import com.example.data.local.entity.SavedDraftEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class PersonalBackupRoundTripTest {
    @Test fun rejectedBackupLeavesExistingHistoryUntouched() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val dao = AppDatabase.getDatabase(context).draftDao()
        dao.clearAllDrafts()
        try {
            dao.insertDraft(SavedDraftEntity(title = "Conservar"))
            val backup = BackupRestoreManager.generateBackupJson(context)
            val invalid = backup.replace("\"version\": 1", "\"version\": 999")
            assertNotEquals(backup, invalid)
            assertFalse(BackupRestoreManager.restoreFromJson(context, invalid, false).success)
            assertEquals("Conservar", dao.getAllDrafts().first().single().title)
        } finally { dao.clearAllDrafts() }
    }
    @Test fun insertionFailureRollsBackDeletionAndEarlierInserts() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val db = AppDatabase.getDatabase(context)
        val dao = db.draftDao()
        dao.clearAllDrafts()
        try {
            dao.insertDraft(SavedDraftEntity(title = "Importado", timestamp = 1))
            dao.insertDraft(SavedDraftEntity(title = "Fallar", timestamp = 2))
            val backup = BackupRestoreManager.generateBackupJson(context)
            dao.clearAllDrafts()
            dao.insertDraft(SavedDraftEntity(title = "Anterior"))
            db.openHelper.writableDatabase.execSQL("CREATE TRIGGER reject_test_draft BEFORE INSERT ON saved_drafts WHEN NEW.title = 'Fallar' BEGIN SELECT RAISE(ABORT, 'test failure'); END")
            assertFalse(BackupRestoreManager.restoreFromJson(context, backup, false).success)
            assertEquals("Anterior", dao.getAllDrafts().first().single().title)
        } finally {
            db.openHelper.writableDatabase.execSQL("DROP TRIGGER IF EXISTS reject_test_draft")
            dao.clearAllDrafts()
        }
    }
    @Test fun personalHistoryRestoresChampionResultKdaAndQueueAfterLocalDeletion() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val dao = AppDatabase.getDatabase(context).draftDao()
        dao.clearAllDrafts()
        try {
            dao.insertDraft(SavedDraftEntity(title="Mi partida", myChampionId="teemo",
                myChampionName="Teemo", userRole="TOP", matchResult="VICTORY",
                myScore="12/2/8", isLegendary=true, matchMode="LEGENDARY"))
            val backup = BackupRestoreManager.generateBackupJson(context)
            dao.clearAllDrafts()
            assertTrue(dao.getAllDrafts().first().isEmpty())
            val restored = BackupRestoreManager.restoreFromJson(context, backup, merge=false)
            assertTrue(restored.message, restored.success)
            val draft = dao.getAllDrafts().first().single()
            assertEquals("teemo", draft.myChampionId)
            assertEquals("VICTORY", draft.matchResult)
            assertEquals("12/2/8", draft.myScore)
            assertEquals("LEGENDARY", draft.matchMode)
            assertTrue(draft.isLegendary)
        } finally { dao.clearAllDrafts() }
    }
}
