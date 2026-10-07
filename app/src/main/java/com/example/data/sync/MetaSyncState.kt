package com.example.data.sync

sealed class MetaSyncState {
    object Idle : MetaSyncState()
    object Syncing : MetaSyncState()
    data class Success(val timestamp: String) : MetaSyncState()
    data class Error(val message: String) : MetaSyncState()
}
