package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.DraftDao
import com.example.data.local.dao.FavoriteBuildsDao
import com.example.data.local.entity.SavedDraftEntity
import com.example.data.local.entity.FavoriteBuildEntity

@Database(
    entities = [SavedDraftEntity::class, FavoriteBuildEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun draftDao(): DraftDao
    abstract fun favoriteBuildsDao(): FavoriteBuildsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wildrift_coach_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
