package com.example.data

import android.content.Context
import androidx.work.*
import com.example.util.AuthManager
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

/** The shared request is authoritative; foreground timers and persisted work only clean expired rows. */
object StreamerPublicationLifecycle {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var listener: ListenerRegistration? = null
    private var deadlineJob: Job? = null
    private var owner: String? = null
    fun start(context: Context, uid: String) {
        stop(context)
        owner = uid
        listener = StreamerRepository.requests.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            val data = snapshot.data.orEmpty()
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
    }
    fun stop(context: Context) {
        listener?.remove(); listener = null
        deadlineJob?.cancel(); deadlineJob = null
        owner?.let { WorkManager.getInstance(context).cancelUniqueWork("streamer-expiration-$it") }
        owner = null
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
