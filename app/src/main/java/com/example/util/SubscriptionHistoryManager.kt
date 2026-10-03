package com.example.util

import android.util.Log
import com.example.model.SubscriptionRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object SubscriptionHistoryManager {

    private const val TAG = "SubscriptionHistory"

    suspend fun getHistory(userId: String? = null, userEmail: String? = null): List<SubscriptionRecord> {
        val targetUid = userId
            ?: AuthManager.getAuth()?.currentUser?.uid
            ?: FirebaseAuth.getInstance().currentUser?.uid
            ?: return emptyList()

        val targetEmail = (userEmail
            ?: AuthManager.getAuth()?.currentUser?.email
            ?: FirebaseAuth.getInstance().currentUser?.email)?.trim()

        val db = FirebaseFirestore.getInstance()
        val records = mutableListOf<SubscriptionRecord>()
        val seenIds = mutableSetOf<String>()

        // 1. Fetch from primary user's subscription_history subcollection
        try {
            val snapshot = db.collection("users").document(targetUid)
                .collection("subscription_history")
                .get()
                .await()

            for (doc in snapshot.documents) {
                SubscriptionRecord.fromDocument(doc)?.let { record ->
                    if (seenIds.add(record.id)) {
                        records.add(record)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching subscription history for uid: $targetUid", e)
        }

        // 2. Cross-reference other UID documents that share the same email (e.g. duplicate accounts)
        if (!targetEmail.isNullOrBlank()) {
            try {
                val emailMatches = db.collection("users")
                    .whereEqualTo("email", targetEmail)
                    .get()
                    .await()

                for (userDoc in emailMatches.documents) {
                    if (userDoc.id != targetUid) {
                        val otherSubSnap = db.collection("users").document(userDoc.id)
                            .collection("subscription_history")
                            .get()
                            .await()

                        for (doc in otherSubSnap.documents) {
                            SubscriptionRecord.fromDocument(doc)?.let { record ->
                                if (seenIds.add(record.id)) {
                                    records.add(record)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error cross-referencing subscription history by email: $targetEmail", e)
            }
        }

        // Gifts are committed with the account update, including on deployments that
        // only permit the server to write the billing history subcollection.
        try {
            val profile = db.collection("users").document(targetUid).get().await()
            val embedded = (profile.get("subscriptionHistory") as? List<*>).orEmpty().filterIsInstance<Map<String, Any>>()
            for (data in embedded) {
                val id = (data["id"] as? String).orEmpty()
                if (id.isNotBlank() && seenIds.add(id)) records += SubscriptionRecord(id = id,
                    timestamp = com.example.model.PremiumAccessPolicy.deadline(data["timestamp"]) ?: 0L,
                    durationMillis = (data["durationMillis"] as? Number)?.toLong() ?: 0L,
                    planName = (data["planName"] as? String).orEmpty(), status = (data["status"] as? String).orEmpty(),
                    amount = (data["amount"] as? String).orEmpty(), source = (data["source"] as? String).orEmpty())
            }
            // Recover the last legacy gift without inventing a purchase or writing on read.
            if (records.none { !it.isEssenceTransaction } && com.example.model.PremiumAccessPolicy.hasGrant(profile.getString("subscriptionPlan"))) {
                val date = com.example.model.PremiumAccessPolicy.deadline(profile.get("lastModifiedByAdmin"))
                val days = Regex("""\((\d+)""").find(profile.getString("subscriptionPlan").orEmpty())?.groupValues?.get(1)?.toLongOrNull()
                if (date != null && days != null) records += SubscriptionRecord(id = "legacy_premium_gift_$date", timestamp = date,
                    durationMillis = days * com.example.model.PremiumAccessPolicy.DAY_MILLIS,
                    planName = "Suscripción Premium regalada", status = "Completado", amount = "Regalo", source = "ADMIN_GIFT")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not read embedded subscription history", e)
        }

        return records.sortedByDescending { it.timestamp }
    }

    suspend fun addRecord(durationMillis: Long, planName: String, status: String, amount: String) {
        val user = AuthManager.getAuth()?.currentUser ?: FirebaseAuth.getInstance().currentUser ?: return
        addRecordForUser(user.uid, durationMillis, planName, status, amount)
    }

    suspend fun addRecordForUser(uid: String, durationMillis: Long, planName: String, status: String, amount: String) {
        val db = FirebaseFirestore.getInstance()
        val now = System.currentTimeMillis()
        val recordData = hashMapOf(
            "timestamp" to now,
            "durationMillis" to durationMillis,
            "planName" to planName,
            "status" to status,
            "amount" to amount,
            "created_at" to now
        )
        try {
            db.collection("users").document(uid)
                .collection("subscription_history")
                .add(recordData)
                .await()
            Log.d(TAG, "Subscription record created successfully for $uid: $planName")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add subscription record for $uid", e)
        }
    }
}

