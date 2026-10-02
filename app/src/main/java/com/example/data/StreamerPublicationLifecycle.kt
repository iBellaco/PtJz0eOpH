package com.example.data

import android.content.Context
import androidx.work.*
import com.example.util.AuthManager
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

/** The shared request is authoritative; foreground timers and persisted work only clean expired rows. */
object StreamerPublicationLifecycle {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var listener: ListenerRegistration? = null
    private var deadlineJob: Job? = null
    private var historyListener: ListenerRegistration? = null
    private var historyJob: Job? = null
    private var owner: String? = null
    fun start(context: Context, uid: String) {
        stop(context)
        owner = uid
        listener = StreamerRepository.requests.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            val data = snapshot.data.orEmpty()
            scope.launch { StreamerHistoryCache.merge(context, uid, listOfNotNull(data.takeIf { it.isNotEmpty() })) }
            deadlineJob?.cancel()
            val workName = "streamer-expiration-$uid"
            if (data["status"] == "PENDING") {
                val delayMillis = (StreamerPublicationPolicy.expiresAt(data) - System.currentTimeMillis()).coerceAtLeast(0L)
                val work = OneTimeWorkRequestBuilder<StreamerExpiryWorker>()
                    .setInputData(workDataOf("ownerUid" to uid))
                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
                WorkManager.getInstance(context).enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, work)
                deadlineJob = scope.launch {
                    delay(delayMillis)
                    StreamerRepository.expire(uid)
                }
            } else WorkManager.getInstance(context).cancelUniqueWork(workName)
        }
        historyListener = StreamerRepository.history(uid).addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            historyJob?.cancel()
            val next = snapshot.documents.mapNotNull { it.data }.map(StreamerPublicationPolicy::historyExpiresAt)
                .filter { it > 0L }.minOrNull()
            if (next == null) WorkManager.getInstance(context).cancelUniqueWork("streamer-history-$uid")
            else {
                val wait = (next - System.currentTimeMillis()).coerceAtLeast(0L)
                enqueueHistoryCleanup(context, uid, wait)
                historyJob = scope.launch { delay(wait); StreamerRepository.pruneHistory(uid) }
            }
        }
    }
    fun stop(context: Context) {
        listener?.remove(); listener = null
        deadlineJob?.cancel(); deadlineJob = null
        historyListener?.remove(); historyListener = null
        historyJob?.cancel(); historyJob = null
        owner?.let {
            WorkManager.getInstance(context).cancelUniqueWork("streamer-expiration-$it")
            WorkManager.getInstance(context).cancelUniqueWork("streamer-history-$it")
        }
        owner = null
    }
}

private fun enqueueHistoryCleanup(context: Context, uid: String, wait: Long,
    policy: ExistingWorkPolicy = ExistingWorkPolicy.REPLACE) {
    val work = OneTimeWorkRequestBuilder<StreamerHistoryExpiryWorker>()
        .setInputData(workDataOf("ownerUid" to uid)).setInitialDelay(wait, TimeUnit.MILLISECONDS)
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
    WorkManager.getInstance(context).enqueueUniqueWork("streamer-history-$uid", policy, work)
}

class StreamerHistoryExpiryWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        val uid = inputData.getString("ownerUid") ?: return Result.success()
        val user = AuthManager.getAuth()?.currentUser
        if (AuthManager.isGuestOrUnauthenticated(user) || user?.uid != uid) return Result.success()
        val result = StreamerRepository.pruneHistory(uid)
        if (result.isFailure) {
            val denied = generateSequence(result.exceptionOrNull()) { it.cause }.any {
                (it as? com.google.firebase.firestore.FirebaseFirestoreException)?.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED
            }
            return if (denied) Result.failure() else Result.retry()
        }
        return try {
            val rows = StreamerRepository.history(uid).get(com.google.firebase.firestore.Source.SERVER)
            val snapshot = rows.await()
            val now = System.currentTimeMillis()
            val next = snapshot.documents.mapNotNull { it.data }.map(StreamerPublicationPolicy::historyExpiresAt)
                .filter { it > now }.minOrNull()
            if (next != null) enqueueHistoryCleanup(applicationContext, uid, next - now, ExistingWorkPolicy.APPEND_OR_REPLACE)
            Result.success()
        } catch (_: Exception) { Result.retry() }
    }
}

class StreamerExpiryWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        val uid = inputData.getString("ownerUid") ?: return Result.success()
        val user = AuthManager.getAuth()?.currentUser
        if (AuthManager.isGuestOrUnauthenticated(user) || user?.uid != uid) return Result.success()
        val result = StreamerRepository.expire(uid)
        val denied = generateSequence(result.exceptionOrNull()) { it.cause }.any {
            (it as? com.google.firebase.firestore.FirebaseFirestoreException)?.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED
        }
        return when { result.isSuccess -> Result.success(); denied -> Result.failure(); else -> Result.retry() }
    }
}
