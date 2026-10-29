package com.example.data

/** Shared durable identity storage for queued and direct account operations. */
internal object AccountOperationIds {
    private val storage by lazy {
        val prefs = com.google.firebase.FirebaseApp.getInstance().applicationContext
            .getSharedPreferences("pending_economy_operations", android.content.Context.MODE_PRIVATE)
        PendingOperationIds({ prefs.getString(it, null) },
            { key, value -> prefs.edit().putString(key, value).commit() },
            { key -> prefs.edit().remove(key).commit() })
    }

    fun reserve(key: String, proposed: String): String = storage.reserve(key, proposed)
    fun adopt(key: String, id: String) = storage.adopt(key, id)
    fun complete(key: String): Boolean = storage.complete(key)
}
