package com.example.util

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import com.example.data.AvatarCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

object SubscriptionManager {
    private val _userRole = MutableStateFlow("free")
    val userRole: StateFlow<String> = _userRole.asStateFlow()
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _isBanned = MutableStateFlow(false)
    val isBanned: StateFlow<Boolean> = _isBanned.asStateFlow()

    private val _isVerified = MutableStateFlow(false)
    val isVerified: StateFlow<Boolean> = _isVerified.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _premiumUntil = MutableStateFlow<Long?>(null)
    val premiumUntil: StateFlow<Long?> = _premiumUntil.asStateFlow()

    private val _currentAvatarId = MutableStateFlow("default_poro")
    val currentAvatarId: StateFlow<String> = _currentAvatarId.asStateFlow()

    private val _currentRankBorder = MutableStateFlow("NONE")
    val currentRankBorder: StateFlow<String> = _currentRankBorder.asStateFlow()

    private val _currentSecondaryRole = MutableStateFlow("none")
    val currentSecondaryRole: StateFlow<String> = _currentSecondaryRole.asStateFlow()

    private val _activeFramePreference = MutableStateFlow("AUTO")
    val activeFramePreference: StateFlow<String> = _activeFramePreference.asStateFlow()

    private val _unlockedAvatars = MutableStateFlow<List<String>>(emptyList())
    val unlockedAvatars: StateFlow<List<String>> = _unlockedAvatars.asStateFlow()

    private val _blueEssence = MutableStateFlow(0L)
    val blueEssence: StateFlow<Long> = _blueEssence.asStateFlow()

    private val _unreadMessagesCount = MutableStateFlow(0)
    val unreadMessagesCount: StateFlow<Int> = _unreadMessagesCount.asStateFlow()

    private val _unreadModeratorSupportCount = MutableStateFlow(0)
    val unreadModeratorSupportCount: StateFlow<Int> = _unreadModeratorSupportCount.asStateFlow()

    private var roleListener: ListenerRegistration? = null
    private var messagesListener: ListenerRegistration? = null
    private var supportReportsListener: ListenerRegistration? = null
    private var supportReportsEmailListener: ListenerRegistration? = null
    private var moderatorSupportReportsListener: ListenerRegistration? = null
    private var heartbeatJob: kotlinx.coroutines.Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private var unreadMessagesSubcollection = 0
    private var unreadSupportReports = 0
    private var unreadPrivateArray = 0
    private var unreadModeratorSupportReports = 0
    private var hasUnreadFromDoc = false
    private var docUnreadCount = 0

    private fun recalculateUnreadCount() {
        val auth = AuthManager.getAuth()
        val user = auth?.currentUser
        if (user == null || AuthManager.isGuestOrUnauthenticated(user)) {
            _unreadMessagesCount.value = 0
            _unreadModeratorSupportCount.value = 0
            return
        }

        val userSpecificUnread = maxOf(unreadMessagesSubcollection + unreadSupportReports, unreadPrivateArray)
        _unreadMessagesCount.value = userSpecificUnread.coerceAtLeast(0)
    }

    fun setUnreadMessagesCount(count: Int) {
        val clean = count.coerceAtLeast(0)
        unreadMessagesSubcollection = clean
        unreadSupportReports = 0
        unreadPrivateArray = clean
        docUnreadCount = clean
        hasUnreadFromDoc = (clean > 0)
        recalculateUnreadCount()
    }

    init {
        com.example.util.AuthManager.getAuth()?.addAuthStateListener {
            val user = it.currentUser
            if (AuthManager.isGuestOrUnauthenticated(user)) {
                _userRole.value = "free"
                _userName.value = ""
                _isPremium.value = false
                _isBanned.value = false
                _isVerified.value = false
                _currentAvatarId.value = "default_poro"
                _unlockedAvatars.value = emptyList()
                _blueEssence.value = 0L
                _unreadMessagesCount.value = 0
                _unreadModeratorSupportCount.value = 0
                unreadMessagesSubcollection = 0
                unreadSupportReports = 0
                unreadPrivateArray = 0
                unreadModeratorSupportReports = 0
                hasUnreadFromDoc = false
                docUnreadCount = 0
                roleListener?.remove()
                roleListener = null
                messagesListener?.remove()
                messagesListener = null
                supportReportsListener?.remove()
                supportReportsListener = null
                supportReportsEmailListener?.remove()
                supportReportsEmailListener = null
                moderatorSupportReportsListener?.remove()
                moderatorSupportReportsListener = null
                heartbeatJob?.cancel()
                heartbeatJob = null
            }
        }
    }

    fun startHeartbeat(uid: String) {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (true) {
                try {
                    FirebaseFirestore.getInstance().collection("users").document(uid)
                        .set(
                            mapOf(
                                "last_active" to System.currentTimeMillis(),
                                "is_online" to true
                            ),
                            SetOptions.merge()
                        )
                } catch (e: Exception) {
                    Log.w("SubscriptionManager", "Heartbeat update failed: ${e.message}")
                }
                kotlinx.coroutines.delay(300_000L) // Ping every 5 minutes
            }
        }
    }

    fun stopHeartbeat(uid: String? = null) {
        heartbeatJob?.cancel()
        heartbeatJob = null
        val targetUid = uid ?: AuthManager.getAuth()?.currentUser?.uid
        if (targetUid != null) {
            scope.launch {
                try {
                    FirebaseFirestore.getInstance().collection("users").document(targetUid)
                        .set(
                            mapOf(
                                "last_active" to System.currentTimeMillis(),
                                "is_online" to false
                            ),
                            SetOptions.merge()
                        )
                } catch (_: Exception) {}
            }
        }
    }

    fun init(context: Context) {
        val auth = AuthManager.getAuth()
        val user = auth?.currentUser

        if (AuthManager.isGuestOrUnauthenticated(user)) {
            _userRole.value = "free"
            _userName.value = ""
            _isPremium.value = false
            _isBanned.value = false
            _currentAvatarId.value = "default_poro"
            _unlockedAvatars.value = emptyList()
            roleListener?.remove()
            roleListener = null
            heartbeatJob?.cancel()
            heartbeatJob = null
            return
        }

        startHeartbeat(user!!.uid)

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(user.uid)

        // Bootstrap document creation using get() first
        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                if (snapshot == null || !snapshot.exists()) {
                    val initialName = user.displayName?.takeIf { it.isNotBlank() } ?: user.email?.substringBefore("@") ?: ""
                    _userName.value = initialName
                    _currentAvatarId.value = "default_poro"
                    _unlockedAvatars.value = listOf("default_poro")
                    // Create if it doesn't exist. Use SetOptions.merge() just in case.
                    val userData = hashMapOf(
                        "role" to "free",
                        "email" to (user.email ?: ""),
                        "name" to initialName,
                        "avatarId" to "default_poro",
                        "unlockedAvatars" to listOf("default_poro"),
                        "last_active" to System.currentTimeMillis(),
                        "is_online" to true
                    )
                    userRef.set(userData, SetOptions.merge())
                } else {
                    val dbName = snapshot.getString("name") ?: ""
                    if (dbName.isNotBlank()) {
                        _userName.value = dbName
                    }
                    val dbAvatarId = snapshot.getString("avatarId") ?: "default_poro"
                    val dbRankBorder = snapshot.getString("rankBorder") ?: "NONE"
                    val dbSecondaryRole = snapshot.getString("secondaryRole") ?: "none"
                    val dbFramePref = snapshot.getString("activeFramePreference") ?: "AUTO"
                    _currentRankBorder.value = dbRankBorder
                    _currentSecondaryRole.value = dbSecondaryRole
                    _activeFramePreference.value = dbFramePref
                    _currentAvatarId.value = dbAvatarId
                    @Suppress("UNCHECKED_CAST")
                    val dbUnlocked = snapshot.get("unlockedAvatars") as? List<String> ?: listOf("default_poro")
                    _unlockedAvatars.value = dbUnlocked

                    val updateData = hashMapOf<String, Any>(
                        "last_active" to System.currentTimeMillis(),
                        "is_online" to true
                    )
                    userRef.set(updateData, SetOptions.merge())
                }
            }
            
            messagesListener?.remove()
            messagesListener = db.collection("users").document(user.uid)
                .collection("messages")
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        unreadMessagesSubcollection = snapshot.documents.count { doc ->
                            doc.getBoolean("isRead") == false
                        }
                        recalculateUnreadCount()
                    }
                }

            supportReportsListener?.remove()
            supportReportsEmailListener?.remove()

            val unreadSupportMap = java.util.concurrent.ConcurrentHashMap<String, Boolean>()

            fun checkAndUpdateSupportUnread() {
                unreadSupportReports = unreadSupportMap.values.count { it }
                recalculateUnreadCount()
            }

            supportReportsListener = db.collection("support_reports")
                .whereEqualTo("userId", user.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        for (doc in snapshot.documents) {
                            val userRead = doc.getBoolean("userRead")
                            val hasNewAdminReply = doc.getBoolean("hasNewAdminReply") == true
                            val hasNewReply = doc.getBoolean("hasNewReply") == true
                            val isLastReplyFromSupport = doc.getBoolean("isLastReplyFromSupport") == true
                            val isUnread = (userRead == false) || hasNewAdminReply || (hasNewReply && isLastReplyFromSupport)
                            unreadSupportMap[doc.id] = isUnread
                        }
                        checkAndUpdateSupportUnread()
                    }
                }

            val uEmail = user.email
            if (!uEmail.isNullOrBlank()) {
                supportReportsEmailListener = db.collection("support_reports")
                    .whereEqualTo("userEmail", uEmail.trim())
                    .addSnapshotListener { snapshot, error ->
                        if (error == null && snapshot != null) {
                            for (doc in snapshot.documents) {
                                val userRead = doc.getBoolean("userRead")
                                val hasNewAdminReply = doc.getBoolean("hasNewAdminReply") == true
                                val hasNewReply = doc.getBoolean("hasNewReply") == true
                                val isLastReplyFromSupport = doc.getBoolean("isLastReplyFromSupport") == true
                                val isUnread = (userRead == false) || hasNewAdminReply || (hasNewReply && isLastReplyFromSupport)
                                unreadSupportMap[doc.id] = isUnread
                            }
                            checkAndUpdateSupportUnread()
                        }
                    }
            }

            // Listen for real-time changes
            roleListener?.remove()
            roleListener = userRef.addSnapshotListener { listenSnapshot, error ->
                if (error != null) {
                    Log.e("SubscriptionManager", "Error listening to role", error)
                    return@addSnapshotListener
                }

                if (listenSnapshot != null && listenSnapshot.exists()) {
                    var role = listenSnapshot.getString("role") ?: "free"
                    val isAdminClaim = AuthManager.isCurrentUserAdmin()
                    if (isAdminClaim || role == "admin") {
                        role = "admin"
                    }
                    val sessionToken = listenSnapshot.getString("sessionToken")
                    val remoteDeviceId = listenSnapshot.getString("lastDeviceId")
                    val remoteTimestamp = listenSnapshot.getLong("lastActiveTimestamp") ?: 0L
                    com.example.util.DeviceAndSessionManager.handleSessionChanged(sessionToken, remoteDeviceId, remoteTimestamp, context)
                    val banned = listenSnapshot.getBoolean("banned") ?: false
                    val name = listenSnapshot.getString("name") ?: ""
                    val avatarId = listenSnapshot.getString("avatarId") ?: "default_poro"
                    val rankBorder = listenSnapshot.getString("rankBorder") ?: "NONE"
                    val secondaryRole = listenSnapshot.getString("secondaryRole") ?: "none"
                    val framePref = listenSnapshot.getString("activeFramePreference") ?: "AUTO"
                    val blueEs = listenSnapshot.getLong("blueEssence") ?: 0L
                    val until = listenSnapshot.getLong("premiumUntil")
                    @Suppress("UNCHECKED_CAST")
                    val unlocked = listenSnapshot.get("unlockedAvatars") as? List<String> ?: listOf("default_poro")

                    if (name.isNotBlank()) {
                        _userName.value = name
                    }
                    _userRole.value = role
                    _isBanned.value = (role == "banned" || banned)
                    _premiumUntil.value = until
                    _blueEssence.value = blueEs
                    
                    val isVerifiedDoc = listenSnapshot.getBoolean("isVerified") ?: listenSnapshot.getBoolean("verified") ?: false
                    _isVerified.value = isVerifiedDoc || isAdminClaim || role == "admin" || role == "moderador"

                    val isPrem = when {
                        isAdminClaim || role == "admin" || role == "moderador" -> true
                        role == "premium" || role == "creador_vip" || role == "streamer" -> {
                            until == null || until == 0L || until > System.currentTimeMillis()
                        }
                        else -> false
                    }
                    _isPremium.value = isPrem
                    _currentAvatarId.value = avatarId
                    _currentSecondaryRole.value = secondaryRole
                    _activeFramePreference.value = framePref
                    
                    _unlockedAvatars.value = unlocked

                    // Sincronizar conteo de mensajes no leídos desde el documento de usuario de forma limpia y exacta
                    val hasUnread = listenSnapshot.getBoolean("hasUnreadMessages") ?: false
                    val remoteDocCount = listenSnapshot.getLong("unreadMessagesCount")?.toInt() ?: if (hasUnread) 1 else 0
                    @Suppress("UNCHECKED_CAST")
                    val privateMsgs = listenSnapshot.get("privateMessages") as? List<Map<String, Any>>
                    val unreadInArray = privateMsgs?.count { (it["isRead"] as? Boolean) == false } ?: 0

                    hasUnreadFromDoc = hasUnread
                    docUnreadCount = remoteDocCount
                    unreadPrivateArray = unreadInArray
                    updateModeratorSupportListener(db, role)
                    recalculateUnreadCount()
                } else {
                    val isEmailAdmin = AuthManager.isCurrentUserAdmin()
                    val fallbackRole = if (isEmailAdmin) "admin" else "free"
                    _userName.value = user.displayName?.takeIf { it.isNotBlank() } ?: user.email?.substringBefore("@") ?: ""
                    _userRole.value = fallbackRole
                    _isPremium.value = isEmailAdmin
                    _isVerified.value = isEmailAdmin
                    _premiumUntil.value = null
                    _currentAvatarId.value = "default_poro"
                    _unlockedAvatars.value = emptyList()
                    updateModeratorSupportListener(db, fallbackRole)
                    recalculateUnreadCount()
                }
            }
        }
    }

    private fun updateModeratorSupportListener(db: FirebaseFirestore, currentRole: String) {
        val auth = AuthManager.getAuth()
        val user = auth?.currentUser
        if (user == null || AuthManager.isGuestOrUnauthenticated(user)) {
            moderatorSupportReportsListener?.remove()
            moderatorSupportReportsListener = null
            unreadModeratorSupportReports = 0
            _unreadModeratorSupportCount.value = 0
            return
        }

        if (currentRole.equals("moderador", ignoreCase = true) || currentRole.equals("admin", ignoreCase = true) || AuthManager.isCurrentUserAdmin()) {
            if (moderatorSupportReportsListener == null) {
                moderatorSupportReportsListener = db.collection("support_reports")
                    .addSnapshotListener { snapshot, error ->
                        if (error == null && snapshot != null) {
                            val activeRole = _userRole.value
                            val pendingDocs = snapshot.documents.filter { doc ->
                                val tag = doc.getString("tag") ?: doc.getString("type") ?: ""
                                val isSponsor = tag.equals("PATROCINADOR", ignoreCase = true) || doc.getBoolean("isSponsor") == true
                                if (activeRole.equals("moderador", ignoreCase = true) && isSponsor) {
                                    return@filter false
                                }
                                val status = doc.getString("status") ?: "PENDIENTE"
                                val isRead = doc.getBoolean("isRead") ?: false
                                val hasNewUserReply = doc.getBoolean("hasNewUserReply") ?: false
                                val isResolved = status.equals("SOLUCIONADO", true) || 
                                                 status.equals("CERRADO", true) || 
                                                 status.equals("CLOSED", true) ||
                                                 status.equals("RESUELTO", true)
                                !isResolved && (status.equals("PENDIENTE", true) || status.equals("UNREAD", true) || !isRead || hasNewUserReply)
                            }
                            unreadModeratorSupportReports = pendingDocs.size
                            _unreadModeratorSupportCount.value = unreadModeratorSupportReports
                            recalculateUnreadCount()
                        }
                    }
            }
        } else {
            moderatorSupportReportsListener?.remove()
            moderatorSupportReportsListener = null
            unreadModeratorSupportReports = 0
            _unreadModeratorSupportCount.value = 0
        }
    }

    fun canEquipAvatar(avatarId: String): Boolean {
        if (_isPremium.value || _userRole.value == "admin" || _userRole.value == "moderador") return true
        if (_unlockedAvatars.value.contains(avatarId)) return true
        val avatar = AvatarCatalog.avatars.find { it.id == avatarId }
        if (avatar != null && (avatar.isDefault || avatar.rarity.equals("común", true) || avatar.rarity.equals("comun", true))) return true
        return false
    }

    fun changeAvatar(
        avatarId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val user = AuthManager.getAuth()?.currentUser
        if (AuthManager.isGuestOrUnauthenticated(user)) {
            onError("Inicia sesión para cambiar de avatar")
            return
        }

        if (!canEquipAvatar(avatarId)) {
            onError("Este avatar requiere suscripción Premium.")
            return
        }

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(user!!.uid)
        
        userRef.set(hashMapOf("avatarId" to avatarId), SetOptions.merge())
            .addOnSuccessListener {
                _currentAvatarId.value = avatarId
                    
                onSuccess()
            }
            .addOnFailureListener {
                onError("Error al actualizar el avatar: ${it.message}")
            }
    }

    fun changeRankBorder(borderId: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val user = AuthManager.getAuth()?.currentUser
        if (AuthManager.isGuestOrUnauthenticated(user)) {
            onError("Inicia sesión para cambiar de marco")
            return
        }
        if (!_isPremium.value && _userRole.value != "admin" && borderId != "NONE") {
            onError("Esta característica es exclusiva para usuarios Premium.")
            return
        }
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(user!!.uid)
        userRef.set(hashMapOf("rankBorder" to borderId), SetOptions.merge())
            .addOnSuccessListener { _currentRankBorder.value = borderId; onSuccess() }
            .addOnFailureListener { onError("Error al actualizar el marco: ${it.message}") }
    }

    fun changeSecondaryRole(roleId: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val user = AuthManager.getAuth()?.currentUser
        if (AuthManager.isGuestOrUnauthenticated(user)) {
            onError("Inicia sesión para cambiar tu rol secundario")
            return
        }
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(user!!.uid)
        val normalizedId = roleId.trim().lowercase()
        userRef.set(hashMapOf("secondaryRole" to normalizedId), SetOptions.merge())
            .addOnSuccessListener {
                _currentSecondaryRole.value = normalizedId
                onSuccess()
            }
            .addOnFailureListener { onError("Error al actualizar rol secundario: ${it.message}") }
    }

    fun changeActiveFramePreference(preference: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val user = AuthManager.getAuth()?.currentUser
        if (AuthManager.isGuestOrUnauthenticated(user)) {
            onError("Inicia sesión para cambiar la preferencia de marco")
            return
        }
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(user!!.uid)
        val normalized = preference.trim().uppercase()
        userRef.set(hashMapOf("activeFramePreference" to normalized), SetOptions.merge())
            .addOnSuccessListener {
                _activeFramePreference.value = normalized
                onSuccess()
            }
            .addOnFailureListener { onError("Error al actualizar preferencia de marco: ${it.message}") }
    }

    fun purchaseSubscription(durationMillis: Long, planName: String, price: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        // Pagos in-app desactivados temporalmente. 
        // El cliente NUNCA debe escribir `role` o `premiumUntil`.
        onError("Pagos desactivados hasta integración con Google Play Billing.")
    }

    fun formatDuration(until: Long?): String {
        if (until == null || until == 0L) return "Vitalicio / Permanente"
        val diff = until - System.currentTimeMillis()
        if (diff <= 0) return "Expirado"

        val secondsTotal = diff / 1000
        val seconds = secondsTotal % 60
        val minutesTotal = secondsTotal / 60
        val minutes = minutesTotal % 60
        val hoursTotal = minutesTotal / 60
        val hours = hoursTotal % 24
        val daysTotal = hoursTotal / 24
        val days = daysTotal % 365
        val years = daysTotal / 365

        val parts = mutableListOf<String>()
        if (years > 0) parts.add("$years año" + if (years > 1L) "s" else "")
        if (days > 0) parts.add("$days día" + if (days > 1L) "s" else "")
        if (hours > 0) parts.add("$hours hora" + if (hours > 1L) "s" else "")
        if (minutes > 0) parts.add("$minutes minuto" + if (minutes > 1L) "s" else "")
        if (seconds > 0) parts.add("$seconds segundo" + if (seconds > 1L) "s" else "")

        return parts.joinToString(", ") + " restantes"
    }

    fun isExpiringSoon(): Boolean {
        if (_userRole.value == "admin" || _userRole.value == "moderador") return false
        if (!_isPremium.value) return false
        val until = _premiumUntil.value ?: return false
        if (until == 0L) return false
        val diff = until - System.currentTimeMillis()
        // Consider expiring soon if less than 3 days (72 hours) and still positive
        return diff in 1..(3L * 24 * 60 * 60 * 1000L)
    }

    fun getRemainingPremiumTimeFormatted(): String {
        if (_userRole.value == "admin") return "Acceso Administrador (Vitalicio)"
        if (_userRole.value == "moderador") return "Acceso Moderador (Vitalicio)"
        if (!_isPremium.value) return "Sin suscripción activa"
        val until = _premiumUntil.value ?: return "Activo (Permanente)"
        if (until == 0L) return "Activo (Permanente)"
        return formatDuration(until)
    }

    suspend fun addBlueEssence(amount: Long) {
        val user = AuthManager.getAuth()?.currentUser ?: return
        if (AuthManager.isGuestOrUnauthenticated(user)) return
        val db = FirebaseFirestore.getInstance()
        try {
            val userRef = db.collection("users").document(user.uid)
            userRef.update("blueEssence", FieldValue.increment(amount)).await()
            _blueEssence.value = _blueEssence.value + amount
        } catch (e: Exception) {
            Log.e("SubscriptionManager", "Error incrementing blue essence", e)
        }
    }
}
