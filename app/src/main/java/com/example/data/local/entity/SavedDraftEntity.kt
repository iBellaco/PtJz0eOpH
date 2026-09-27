package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
data class SavedDraftSlotData(
    val championId: String,
    val championName: String,
    val role: String,
    val avatarUrl: String = ""
)

@Entity(tableName = "saved_drafts")
data class SavedDraftEntity(
    @PrimaryKey(autoGenerate = true)
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
    val matchResult: String = "PENDING", // "PENDING", "VICTORY", "DEFEAT"
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
    val matchMode: String = "RANKED", // "RANKED", "LEGENDARY", "NORMAL"
    val myScore: String = "" // e.g. "12/2/8" or "KDA 5.0"
)
