package com.example.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges

/** Missing access identities are server-confirmed; absence of a marker is not a deletion. */
internal object ManagedUsersDirectory {
    enum class Status { SYNCING, LIVE, OFFLINE, ERROR }
    data class State(
        val status: Status = Status.SYNCING,
        val users: List<Map<String, Any>> = emptyList(),
        val lastSyncAt: Long = 0L,
        val error: FirebaseFirestoreException? = null
    )

    fun isVisible(data: Map<String, Any>): Boolean = data["authAccountExists"] != false

    internal fun createdAtMillis(data: Map<String, Any>): Long {
        val value = data["createdAt"] ?: data["created_at"] ?: data["registrationDate"] ?: data["timestamp"]
        return when (value) {
            is Number -> value.toLong()
            is com.google.firebase.Timestamp -> value.toDate().time
            is java.util.Date -> value.time
            is String -> value.toLongOrNull() ?: listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd'T'HH:mm:ssX", "yyyy-MM-dd HH:mm:ss"
            ).firstNotNullOfOrNull { pattern -> runCatching {
                java.text.SimpleDateFormat(pattern, java.util.Locale.US).parse(value)?.time
            }.getOrNull() } ?: 0L
            else -> 0L
        }
    }

    internal fun newestFirst(users: List<Map<String, Any>>): List<Map<String, Any>> = users.sortedWith(
        compareByDescending<Map<String, Any>> { createdAtMillis(it) }
            .thenBy { (it["email"] ?: it["uid"] ?: "").toString().lowercase() }
    )

    /** Public build history is not a private account directory. Never widen directory access. */
    fun creatorCardVisible(canReadDirectory: Boolean, matchedProfile: Map<String, Any>?): Boolean =
        !canReadDirectory || (matchedProfile != null && isVisible(matchedProfile))

    fun transition(previous: State, fromCache: Boolean, users: List<Map<String, Any>>, now: Long,
                   error: FirebaseFirestoreException? = null): State = when {
        error != null -> State(Status.ERROR, lastSyncAt = previous.lastSyncAt, error = error)
        fromCache -> State(Status.OFFLINE, lastSyncAt = previous.lastSyncAt)
        else -> State(Status.LIVE, newestFirst(users.filter(::isVisible)), now)
    }

    fun observe(onResult: (State) -> Unit): ListenerRegistration {
        var state = State()
        onResult(state)
        val registration = try { FirebaseFirestore.getInstance().collection("users")
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                val users = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    doc.data?.takeIf(::isVisible)?.plus("uid" to doc.id)
                }
                state = transition(state, snapshot == null || snapshot.metadata.isFromCache, users,
                    System.currentTimeMillis(), error)
                onResult(state)
            }
        } catch (_: IllegalStateException) {
            // Local/offline Hub screens must also work before the remote service is initialized.
            onResult(State(Status.ERROR, error = FirebaseFirestoreException(
                "Directory not initialized", FirebaseFirestoreException.Code.FAILED_PRECONDITION)))
            ListenerRegistration { }
        }
        return registration
    }

    fun listen(onResult: (List<Map<String, Any>>, FirebaseFirestoreException?) -> Unit): ListenerRegistration =
        observe { state ->
            if (state.status != Status.SYNCING) onResult(state.users, state.error ?:
                if (state.status == Status.OFFLINE) FirebaseFirestoreException("Sin conexión / Sem conexão", FirebaseFirestoreException.Code.UNAVAILABLE) else null)
        }
}
