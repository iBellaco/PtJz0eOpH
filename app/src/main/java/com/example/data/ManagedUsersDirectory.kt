package com.example.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges

/** Missing access identities are server-confirmed; absence of a marker is not a deletion. */
internal object ManagedUsersDirectory {
    fun isVisible(data: Map<String, Any>): Boolean = data["authAccountExists"] != false

    fun listen(onResult: (List<Map<String, Any>>, FirebaseFirestoreException?) -> Unit): ListenerRegistration =
        FirebaseFirestore.getInstance().collection("users")
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                // A stale offline cache cannot prove that an account still exists.
                val users = if (error != null || snapshot == null || snapshot.metadata.isFromCache) emptyList()
                else snapshot.documents.mapNotNull { doc ->
                    doc.data?.takeIf(::isVisible)?.plus("uid" to doc.id)
                }
                onResult(users, error)
            }
}
