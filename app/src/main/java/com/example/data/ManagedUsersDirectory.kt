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

    /** Public build history is not a private account directory. Never widen directory access. */
    fun creatorCardVisible(canReadDirectory: Boolean, matchedProfile: Map<String, Any>?): Boolean =
        !canReadDirectory || (matchedProfile != null && isVisible(matchedProfile))

    fun transition(previous: State, fromCache: Boolean, users: List<Map<String, Any>>, now: Long,
                   error: FirebaseFirestoreException? = null): State = when {
        error != null -> State(Status.ERROR, lastSyncAt = previous.lastSyncAt, error = error)
        fromCache -> State(Status.OFFLINE, lastSyncAt = previous.lastSyncAt)
        else -> State(Status.LIVE, users.filter(::isVisible), now)
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
