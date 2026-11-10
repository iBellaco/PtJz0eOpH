package com.example.data

import com.example.util.AuthManager
import com.example.util.appTr
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.tasks.await
import java.util.UUID

object SponsorReviewPolicy {
    fun pending(data: Map<String, Any>): Boolean = data["isApproved"] == false &&
        (data["refundedAtMillis"] as? Number)?.toLong().let { it == null || it == 0L } &&
        data["status"] !in setOf("REFUNDED", "REJECTED")

    fun publicNotice(id: String, data: Map<String, Any>): Map<String, Any> =
        data.filterKeys { it in setOf("title", "content", "videoUrl", "expandedImageUrl", "externalUrl",
            "titleColor", "contentColor", "budget", "budgetUnit", "durationValue", "durationUnit",
            "isApproved", "isEnabled", "approvedAtMillis", "expiresAtMillis") } +
            mapOf("id" to id, "tag" to "PUBLICIDAD")

    fun visibleOnHome(data: Map<String, Any>, now: Long): Boolean =
        data["isApproved"] == true && data["isEnabled"] == true &&
            data["tag"]?.toString()?.uppercase() == "PUBLICIDAD" &&
            ((data["expiresAtMillis"] as? Number)?.toLong() ?: 0L).let { it == 0L || it > now } &&
            (data["title"]?.toString()?.isNotBlank() == true ||
                data["content"]?.toString()?.isNotBlank() == true)

    fun notice(id: String, d: Map<String, Any>) = AppNotice(
        id=id, title=d["title"] as? String ?: "", content=d["content"] as? String ?: "",
        videoUrl=d["videoUrl"] as? String ?: "", expandedImageUrl=d["expandedImageUrl"] as? String ?: "",
        externalUrl=d["externalUrl"] as? String ?: "", titleColor=d["titleColor"] as? String ?: "#FFD700",
        tag="PUBLICIDAD", budget=(d["budget"] as? Number)?.toDouble() ?: 0.0,
        budgetUnit=d["budgetUnit"] as? String ?: "day", durationValue=(d["durationValue"] as? Number)?.toInt() ?: 1,
        durationUnit=d["durationUnit"] as? String ?: "day", isApproved=d["isApproved"] == true,
        isEnabled=d["isEnabled"] == true, approvedAtMillis=(d["approvedAtMillis"] as? Number)?.toLong() ?: 0,
        expiresAtMillis=(d["expiresAtMillis"] as? Number)?.toLong() ?: 0,
        sponsorEmail=d["sponsorEmail"] as? String ?: "")
}

/** Commits an existing moderation decision immediately; only pre-charge submissions use recovery queueing. */
object SponsorReviewRepository {
    /** Include durable submissions even before the worker creates the charged advertisement. */
    fun observe(onChange: (List<Map<String, Any>>) -> Unit, onError: () -> Unit): List<com.google.firebase.firestore.ListenerRegistration> {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        var ads = emptyList<Map<String, Any>>()
        var queued = emptyList<Map<String, Any>>()
        fun publish() {
            val known = ads.map { it["id"] }.toSet()
            onChange(ads.filter { SponsorReviewPolicy.pending(it) } + queued.filter { it["id"] !in known })
        }
        val adListener = db.collection("pending_sponsor_ads").addSnapshotListener { snapshot, error ->
            if (error != null) { onError(); return@addSnapshotListener }
            ads = snapshot?.documents.orEmpty().map { it.data.orEmpty() + ("id" to it.id) }
            publish()
        }
        val queueListener = db.collection("economy_requests").whereEqualTo("payload.action", "SPONSOR")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { onError(); return@addSnapshotListener }
                queued = snapshot?.documents.orEmpty().filter { it.getString("status") in setOf("PENDING", "PROCESSING", "REVIEW") }.mapNotNull { row ->
                    @Suppress("UNCHECKED_CAST")
                    val notice = row.get("payload.notice") as? Map<String, Any> ?: return@mapNotNull null
                    val id = row.getString("operationId") ?: return@mapNotNull null
                    notice + mapOf("id" to id, "queuedRequestId" to row.id, "isApproved" to false,
                        "createdAt" to (row.getTimestamp("createdAt")?.toDate()?.time ?: 0L))
                }
                publish()
            }
        return listOf(adListener, queueListener)
    }
    suspend fun resolve(id: String, approve: Boolean, queuedRequestId: String? = null) {
        require(id.matches(Regex("[a-zA-Z0-9_-]{8,80}")))
        val user = AuthManager.getAuth()?.currentUser ?: error(appTr("Inicia sesión"))
        check(!AuthManager.isGuestOrUnauthenticated(user)) { appTr("Inicia sesión") }
        val token = user.getIdToken(true).await()
        check(token.claims["admin"] == true) { appTr("No tienes permisos para esta operación") }

        val db = FirebaseFirestore.getInstance()
        val adRef = db.collection("pending_sponsor_ads").document(id)
        val registryRef = db.collection("system_config").document("app_notices")
        val publicNoticeRef = db.collection("app_notices").document(id)
        val suffix = if (approve) "approve" else "reject"
        // A fresh identifier prevents an old REVIEW row from blocking a new,
        // already-authorized moderation decision. The server remains
        // idempotent on the advertisement/refund itself.
        val operationId = "sponsor_review_${id.take(32)}_${suffix}_${UUID.randomUUID().toString().take(8)}"
        val queueRef = db.collection("economy_requests").document("${user.uid}~$operationId")
        val authTime = (token.claims["auth_time"] as? Number)?.toLong() ?: 0L
        val cleanQueuedRequestId = queuedRequestId?.takeIf { it.matches(Regex("[a-zA-Z0-9_~-]{8,210}")) }

        val resolvedDirectly = db.runTransaction { tx ->
            val ad = tx.get(adRef)
            if (!ad.exists()) return@runTransaction false

            val data = ad.data.orEmpty()
            val alreadyApproved = data["isApproved"] == true
            val rejected = data["status"] in setOf("REJECTED", "REFUNDED") ||
                ((data["refundedAtMillis"] as? Number)?.toLong() ?: 0L) > 0L
            check(!(approve && rejected) && !(!approve && alreadyApproved)) {
                appTr("La solicitud ya fue resuelta")
            }

            val registry = tx.get(registryRef)
            @Suppress("UNCHECKED_CAST")
            val notices = ((registry.get("notices") as? List<Map<String, Any>>).orEmpty())
                .filterNot { it["id"] == id }
            val now = System.currentTimeMillis()

            if (approve) {
                val unit = data["durationUnit"]?.toString()?.lowercase().orEmpty()
                val unitMillis = when (unit) {
                    "hour" -> 60L * 60L * 1000L
                    "week" -> 7L * 24L * 60L * 60L * 1000L
                    "month" -> 30L * 24L * 60L * 60L * 1000L
                    else -> 24L * 60L * 60L * 1000L
                }
                val maximum = when (unit) { "hour" -> 12; "day", "" -> 3; "week" -> 2; "month" -> 6; else -> 3 }
                val duration = ((data["durationValue"] as? Number)?.toInt() ?: 1).coerceIn(1, maximum)
                val previousApprovedAt = (data["approvedAtMillis"] as? Number)?.toLong() ?: 0L
                val previousExpiresAt = (data["expiresAtMillis"] as? Number)?.toLong() ?: 0L
                val changes = mapOf<String, Any>(
                    "isApproved" to true,
                    "isEnabled" to true,
                    "status" to "APROBADA",
                    "approvedAtMillis" to previousApprovedAt.takeIf { alreadyApproved && it > 0L }.let { it ?: now },
                    "expiresAtMillis" to previousExpiresAt.takeIf { alreadyApproved && it > 0L }.let {
                        it ?: (now + unitMillis * duration)
                    }
                )
                tx.update(adRef, changes)
                val publicNotice = SponsorReviewPolicy.publicNotice(id, data + changes)
                tx.set(registryRef, mapOf("notices" to (notices + publicNotice)), SetOptions.merge())
                // A dedicated public document avoids losing the publication if
                // another device still holds an old copy of the shared list.
                tx.set(publicNoticeRef, publicNotice)
            } else {
                if (rejected) {
                    tx.set(registryRef, mapOf("notices" to notices), SetOptions.merge())
                    tx.delete(publicNoticeRef)
                    return@runTransaction true
                }
                // The visible decision is committed immediately. A private,
                // signed queue row in the same transaction asks the trusted
                // worker to verify the original charge and refund it exactly
                // once; the UI no longer waits on an unavailable callable.
                tx.update(adRef, mapOf("status" to "REJECTED", "isApproved" to false, "isEnabled" to false))
                tx.set(registryRef, mapOf("notices" to notices), SetOptions.merge())
                tx.delete(publicNoticeRef)
                val payload = mutableMapOf<String, Any>(
                    "action" to "SPONSOR_RESOLVE",
                    "id" to operationId,
                    "sponsorId" to id,
                    "approved" to false
                )
                cleanQueuedRequestId?.let { payload["queuedRequestId"] = it }
                tx.set(queueRef, mapOf(
                    "userId" to user.uid,
                    "operationId" to operationId,
                    "payload" to payload,
                    "status" to "PENDING",
                    "schema" to 2,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "authTime" to authTime
                ))
            }
            true
        }.await()

        if (!resolvedDirectly) {
            // A queued submission has no advertisement to publish yet. Resolve
            // it through the trusted service now; a queued fallback is NOT an
            // approval and must never be reported as visible to the moderator.
            val payload = mutableMapOf<String, Any>(
                "sponsorId" to id,
                "approved" to approve
            )
            cleanQueuedRequestId?.let { payload["queuedRequestId"] = it }
            EconomyServiceClient.call("SPONSOR_RESOLVE", payload, operationId,
                awaitQueuedResult = false)
        }
        if (approve) {
            // Do not claim that the advertisement is on Inicio until the
            // canonical public document can be read from the server. A cache
            // hit or a successfully enqueued command is insufficient.
            val published = withTimeoutOrNull(8_000L) {
                publicNoticeRef.get(Source.SERVER).await()
            }
            if (published == null ||
                !SponsorReviewPolicy.visibleOnHome(published.data.orEmpty(), System.currentTimeMillis())) {
                throw EconomyUnconfirmedException()
            }
            AppNoticeManager.acceptVerifiedSponsor(
                com.google.firebase.FirebaseApp.getInstance().applicationContext, id, published.data.orEmpty())
        }
    }
}
