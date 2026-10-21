package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.DraftDao
import com.example.data.local.dao.FavoriteBuildsDao
import com.example.data.local.entity.SavedDraftEntity
import com.example.data.local.entity.FavoriteBuildEntity

@Database(
    entities = [SavedDraftEntity::class, FavoriteBuildEntity::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun draftDao(): DraftDao
    abstract fun favoriteBuildsDao(): FavoriteBuildsDao

    companion object {
        /**
         * Keep local drafts and favorites across every schema version that has
         * been shipped. Versions 1-3 only changed application code; 3-4 added
         * favorites and 4-5 added match metadata to saved drafts.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) = Unit
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) = Unit
        }
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS favorite_builds (buildId TEXT NOT NULL PRIMARY KEY, timestamp INTEGER NOT NULL)"
                )
            }
        }
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE saved_drafts ADD COLUMN matchMode TEXT NOT NULL DEFAULT 'RANKED'")
                database.execSQL("ALTER TABLE saved_drafts ADD COLUMN myScore TEXT NOT NULL DEFAULT ''")
            }
        }
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) = Unit
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wildrift_coach_database"
                )
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6
                )
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
