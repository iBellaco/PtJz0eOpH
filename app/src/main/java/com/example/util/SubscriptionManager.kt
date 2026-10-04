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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

object SubscriptionManager {
    private val _userRole = MutableStateFlow("free")
    val userRole: StateFlow<String> = _userRole.asStateFlow()
    private val _secondaryRole = MutableStateFlow("")
    val secondaryRole: StateFlow<String> = _secondaryRole.asStateFlow()
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _isBanned = MutableStateFlow(false)
    val isBanned: StateFlow<Boolean> = _isBanned.asStateFlow()

    private val _isVerified = MutableStateFlow(false)
    val isVerified: StateFlow<Boolean> = _isVerified.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private var premiumPlan = ""
    private val _premiumUntil = MutableStateFlow<Long?>(null)
    val premiumUntil: StateFlow<Long?> = _premiumUntil.asStateFlow()

    private val _currentAvatarId = MutableStateFlow("default_poro")
    val currentAvatarId: StateFlow<String> = _currentAvatarId.asStateFlow()

    private val _currentRankBorder = MutableStateFlow("NONE")
    val currentRankBorder: StateFlow<String> = _currentRankBorder.asStateFlow()

    private val _unlockedAvatars = MutableStateFlow<List<String>>(emptyList())
    val unlockedAvatars: StateFlow<List<String>> = _unlockedAvatars.asStateFlow()

    private val _blueEssence = MutableStateFlow(0L)
    val blueEssence: StateFlow<Long> = _blueEssence.asStateFlow()

    private val _orangeEssence = MutableStateFlow(0L)
    val orangeEssence: StateFlow<Long> = _orangeEssence.asStateFlow()

    private val _unreadMessagesCount = MutableStateFlow(0)
    val unreadMessagesCount: StateFlow<Int> = _unreadMessagesCount.asStateFlow()
    private val _currentUserUid = MutableStateFlow("")
    val currentUserUid: StateFlow<String> = _currentUserUid.asStateFlow()
    private val _unreadMessageRoutes = MutableStateFlow<Map<String, Set<com.example.data.NotificationPanel>>>(emptyMap())
    val unreadMessageRoutes: StateFlow<Map<String, Set<com.example.data.NotificationPanel>>> = _unreadMessageRoutes.asStateFlow()
    private var inboxDocuments = emptyList<Map<String, Any>>()
    private var inboxArray = emptyList<Map<String, Any>>()
    private val _unreadMessageIds = MutableStateFlow<Set<String>>(emptySet())
    val unreadMessageIds: StateFlow<Set<String>> = _unreadMessageIds.asStateFlow()

    private val _unreadModeratorSupportCount = MutableStateFlow(0)
    val unreadModeratorSupportCount: StateFlow<Int> = _unreadModeratorSupportCount.asStateFlow()

    private var roleListener: ListenerRegistration? = null
    private var messagesListener: ListenerRegistration? = null
    private var supportReportsListener: ListenerRegistration? = null
    private var supportReportsEmailListener: ListenerRegistration? = null
    private var moderatorSupportReportsListener: ListenerRegistration? = null
    private var premiumExpirationJob: kotlinx.coroutines.Job? = null
    private var heartbeatJob: kotlinx.coroutines.Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private var unreadMessagesSubcollection = emptySet<String>()
    private var unreadSupportReports = emptySet<String>()
    private var unreadPrivateArray = emptySet<String>()
    private var unreadModeratorSupportReports = 0
    private var hasUnreadFromDoc = false
    private var docUnreadCount = 0

    private fun recalculateUnreadCount() {
        val auth = AuthManager.getAuth()
        val user = auth?.currentUser
        if (user == null || AuthManager.isGuestOrUnauthenticated(user)) {
            _unreadMessagesCount.value = 0
            _unreadMessageIds.value = emptySet()
            _unreadModeratorSupportCount.value = 0
            return
        }

        val unique = unreadMessagesSubcollection + unreadSupportReports + unreadPrivateArray
        val actualPersonalUnread = unique.size
        _unreadMessageIds.value = unique
        _unreadMessageRoutes.value = (inboxArray + inboxDocuments).mapNotNull { data ->
            com.example.data.InboxNotificationPolicy.key(data)?.takeIf { it in unique }?.let { it to com.example.data.PanelNotificationPolicy.messagePanels(data) }
        }.groupBy({ it.first }, { it.second }).mapValues { (_, panels) -> panels.flatten().toSet() }
        _unreadMessagesCount.value = actualPersonalUnread

        if (actualPersonalUnread == 0 && (hasUnreadFromDoc || docUnreadCount > 0)) {
            hasUnreadFromDoc = false
            docUnreadCount = 0
            scope.launch {
                try {
                    FirebaseFirestore.getInstance().collection("users").document(user.uid)
                        .set(
                            mapOf(
                                "hasUnreadMessages" to false,
                                "unreadMessagesCount" to 0
                            ),
                            SetOptions.merge()
                        )
                } catch (_: Exception) {}
            }
        }
    }

    fun setUnreadMessageIds(ids: Set<String>) {
        _unreadMessageIds.value = ids
        _unreadMessagesCount.value = ids.size
    }

    init {
        scope.launch {
            AuthManager.isAdminClaim.collect { claim ->
                _isPremium.value = com.example.model.PremiumAccessPolicy.isActive(_userRole.value, _premiumUntil.value,
                    secondary = _secondaryRole.value, adminClaim = claim, banned = _isBanned.value, granted = com.example.model.PremiumAccessPolicy.hasGrant(premiumPlan))
            }
        }
        com.example.util.AuthManager.getAuth()?.addAuthStateListener {
            val user = it.currentUser
            _currentUserUid.value = if (AuthManager.isGuestOrUnauthenticated(user)) "" else user?.uid.orEmpty()
            if (AuthManager.isGuestOrUnauthenticated(user)) {
                _userRole.value = "free"
                _secondaryRole.value = ""
                _userName.value = ""
                _isPremium.value = false
                _premiumUntil.value = null
                premiumExpirationJob?.cancel()
                _isBanned.value = false
                _isVerified.value = false
                _currentAvatarId.value = "default_poro"
                _unlockedAvatars.value = emptyList()
                _blueEssence.value = 0L
                _orangeEssence.value = 0L
                _unreadMessagesCount.value = 0
                _unreadMessageIds.value = emptySet()
                _unreadMessageRoutes.value = emptyMap()
                inboxArray = emptyList(); inboxDocuments = emptyList()
                premiumPlan = ""
                _unreadModeratorSupportCount.value = 0
                unreadMessagesSubcollection = emptySet()
                unreadSupportReports = emptySet()
                unreadPrivateArray = emptySet()
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

    private var presenceContext: Context? = null

    fun startHeartbeat(uid: String) {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (true) {
                try {
                    presenceContext?.let { DeviceAndSessionManager.updatePresence(uid, it, true) }
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
                    presenceContext?.let { DeviceAndSessionManager.updatePresence(targetUid, it, false) }
                } catch (_: Exception) {}
            }
        }
    }

    fun init(context: Context) {
        presenceContext = context.applicationContext
        val auth = AuthManager.getAuth()
        val user = auth?.currentUser
        _currentUserUid.value = if (AuthManager.isGuestOrUnauthenticated(user)) "" else user?.uid.orEmpty()

        if (AuthManager.isGuestOrUnauthenticated(user)) {
            _userRole.value = "free"
            _userName.value = ""
            _isPremium.value = false
            _premiumUntil.value = null
            premiumExpirationJob?.cancel()
            com.example.data.StreamerPublicationLifecycle.stop(context.applicationContext)
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
        com.example.data.StreamerPublicationLifecycle.start(context.applicationContext, user.uid)

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
                        "is_online" to false
                    )
                    userRef.set(userData, SetOptions.merge())
                } else {
                    val dbName = snapshot.getString("name") ?: ""
                    if (dbName.isNotBlank()) {
                        _userName.value = dbName
                    }
                    val dbAvatarId = snapshot.getString("avatarId") ?: "default_poro"
                    val dbRankBorder = snapshot.getString("rankBorder") ?: "NONE"
                    _currentRankBorder.value = dbRankBorder
                    _currentAvatarId.value = dbAvatarId
                    @Suppress("UNCHECKED_CAST")
                    val dbUnlocked = snapshot.get("unlockedAvatars") as? List<String> ?: listOf("default_poro")
                    _unlockedAvatars.value = dbUnlocked


                }
            }
            
            messagesListener?.remove()
            messagesListener = db.collection("users").document(user.uid)
                .collection("messages")
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        inboxDocuments = snapshot.documents.map { it.data.orEmpty() + ("id" to it.id) }
                        unreadMessagesSubcollection = com.example.data.InboxNotificationPolicy.unreadKeys(inboxDocuments)
                        recalculateUnreadCount()
                    }
                }

            supportReportsListener?.remove()
            supportReportsEmailListener?.remove()

            var unreadSupportByUid = emptySet<String>()
            var unreadSupportByEmail = emptySet<String>()

            fun checkAndUpdateSupportUnread() {
                unreadSupportReports = unreadSupportByUid + unreadSupportByEmail
                recalculateUnreadCount()
            }

            fun isReportUnreadForUser(doc: com.google.firebase.firestore.DocumentSnapshot): Boolean {
                val status = doc.getString("status") ?: ""
                val isClosed = status.equals("SOLUCIONADO", true) ||
                               status.equals("CERRADO", true) ||
                               status.equals("CLOSED", true) ||
                               status.equals("RESUELTO", true) ||
                               status.equals("ELIMINADO", true) ||
                               status.equals("DELETED", true)
                if (isClosed) return false

                val hasNewAdminReply = doc.getBoolean("hasNewAdminReply") == true || doc.getBoolean("hasNewReply") == true
                if (hasNewAdminReply) return true

                val userRead = doc.getBoolean("userRead")
                if (userRead == true) return false

                val adminReply = doc.getString("adminReply") ?: ""
                @Suppress("UNCHECKED_CAST")
                val conversation = doc.get("conversation") as? List<Map<String, Any>> ?: emptyList()
                val lastEntry = conversation.lastOrNull()
                val lastSenderRole = (lastEntry?.get("senderRole") as? String)?.uppercase() ?: ""
                val isLastReplyFromSupport = lastSenderRole in listOf("SUPPORT", "ADMIN", "MODERADOR") || adminReply.isNotBlank()

                // Si soporte respondió y el usuario aún no lo ha marcado como leído:
                return isLastReplyFromSupport && (userRead == false || doc.getBoolean("isRead") == false)
            }

            supportReportsListener = db.collection("support_reports")
                .whereEqualTo("userId", user.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        unreadSupportByUid = snapshot.documents.filter(::isReportUnreadForUser).map { "support:${it.id}" }.toSet()
                        checkAndUpdateSupportUnread()
                    }
                }

            val uEmail = user.email
            if (!uEmail.isNullOrBlank()) {
                supportReportsEmailListener = db.collection("support_reports")
                    .whereEqualTo("userEmail", uEmail.trim())
                    .addSnapshotListener { snapshot, error ->
                        if (error == null && snapshot != null) {
                            unreadSupportByEmail = snapshot.documents.filter(::isReportUnreadForUser).map { "support:${it.id}" }.toSet()
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
                    // Cached/local snapshots cannot revoke a session before its server confirmation.
                    if (!listenSnapshot.metadata.isFromCache && !listenSnapshot.metadata.hasPendingWrites()) {
                        com.example.util.DeviceAndSessionManager.handleSessionChanged(sessionToken, remoteDeviceId, context = context,
                            active = listenSnapshot.getBoolean("is_online") == true)
                    }
                    val banned = listenSnapshot.getBoolean("banned") ?: false
                    val name = listenSnapshot.getString("name") ?: ""
                    val avatarId = listenSnapshot.getString("avatarId") ?: "default_poro"
                    val rankBorder = listenSnapshot.getString("rankBorder") ?: "NONE"
                    val blueEs = listenSnapshot.getLong("blueEssence") ?: 0L
                    val until = com.example.model.PremiumAccessPolicy.deadline(listenSnapshot.get("premiumUntil"))
                    premiumPlan = listenSnapshot.getString("subscriptionPlan").orEmpty()
                    @Suppress("UNCHECKED_CAST")
                    val unlocked = listenSnapshot.get("unlockedAvatars") as? List<String> ?: listOf("default_poro")

                    if (name.isNotBlank()) {
                        _userName.value = name
                    }
                    _userRole.value = role
                    if (role == "admin" && !banned) com.example.data.DatabaseStatisticsWorker.schedule(context)
                    _secondaryRole.value = listenSnapshot.getString("secondaryRole") ?: ""
                    _isBanned.value = (role == "banned" || banned)
                    _premiumUntil.value = until
                    _blueEssence.value = blueEs
                    _orangeEssence.value = listenSnapshot.getLong("orangeEssence") ?: 0L
                    
                    val isVerifiedDoc = listenSnapshot.getBoolean("isVerified") ?: listenSnapshot.getBoolean("verified") ?: false
                    _isVerified.value = isVerifiedDoc || isAdminClaim || role == "admin" || role == "moderador"

                    premiumExpirationJob?.cancel()
                    _isPremium.value = com.example.model.PremiumAccessPolicy.isActive(role, until,
                        secondary = _secondaryRole.value, adminClaim = isAdminClaim, banned = banned, granted = com.example.model.PremiumAccessPolicy.hasGrant(premiumPlan))
                    if (until != null && until > System.currentTimeMillis() &&
                        !com.example.model.PremiumAccessPolicy.isLifetime(role, _secondaryRole.value, isAdminClaim)) {
                        premiumExpirationJob = scope.launch {
                            kotlinx.coroutines.delay((until - System.currentTimeMillis()).coerceAtLeast(1L))
                            _isPremium.value = com.example.model.PremiumAccessPolicy.isActive(_userRole.value, _premiumUntil.value,
                                secondary = _secondaryRole.value, adminClaim = AuthManager.isAdminClaim.value, banned = _isBanned.value, granted = com.example.model.PremiumAccessPolicy.hasGrant(premiumPlan))
                        }
                    }
                    _currentAvatarId.value = avatarId
                    
                    _unlockedAvatars.value = unlocked

                    // Sincronizar conteo de mensajes no leídos desde el documento de usuario de forma limpia y exacta
                    val hasUnread = listenSnapshot.getBoolean("hasUnreadMessages") ?: false
                    val remoteDocCount = listenSnapshot.getLong("unreadMessagesCount")?.toInt() ?: if (hasUnread) 1 else 0
                    @Suppress("UNCHECKED_CAST")
                    val privateMsgs = listenSnapshot.get("privateMessages") as? List<Map<String, Any>>
                    inboxArray = privateMsgs.orEmpty()
                    val unreadInArray = com.example.data.InboxNotificationPolicy.unreadKeys(inboxArray)

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
                    _secondaryRole.value = ""
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
                moderatorSupportReportsListener = com.example.data.SupportTicketAccess.staffQuery()
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
                                val isRead = doc.getBoolean("staffRead") ?: (doc.getString("status").orEmpty().uppercase() in listOf("READ", "LEIDO", "LEÍDO"))
                                val hasNewUserReply = doc.getBoolean("hasNewUserReply") ?: false
                                val isResolved = com.example.data.SupportConversationPolicy.isClosed(status) || status.equals("SOLUCIONADO", true) ||
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
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(user!!.uid)
        userRef.set(hashMapOf("rankBorder" to borderId), SetOptions.merge())
            .addOnSuccessListener { _currentRankBorder.value = borderId; onSuccess() }
            .addOnFailureListener { onError("Error al actualizar el marco: ${it.message}") }
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
        if (years > 0) parts.add("$years " + appTr(if (years > 1L) "años" else "año"))
        if (days > 0) parts.add("$days " + appTr(if (days > 1L) "días" else "día"))
        if (hours > 0) parts.add("$hours hora" + if (hours > 1L) "s" else "")
        if (minutes > 0) parts.add("$minutes minuto" + if (minutes > 1L) "s" else "")
        if (seconds > 0) parts.add("$seconds segundo" + if (seconds > 1L) "s" else "")

        return parts.joinToString(", ") + " restantes"
    }

    fun isExpiringSoon(): Boolean {
        if (com.example.model.PremiumAccessPolicy.isLifetime(_userRole.value, _secondaryRole.value, AuthManager.isAdminClaim.value)) return false
        if (!_isPremium.value) return false
        val until = _premiumUntil.value ?: return false
        if (until == 0L) return false
        val diff = until - System.currentTimeMillis()
        // Consider expiring soon if less than 3 days (72 hours) and still positive
        return diff in 1..(3L * 24 * 60 * 60 * 1000L)
    }

    fun getRemainingPremiumTimeFormatted(): String {
        if (_userRole.value == "admin") return "Acceso Administrador (Vitalicio)"
        if (_userRole.value == "moderador" || _secondaryRole.value == "moderador") return "Acceso Moderador (Vitalicio)"
        if (!_isPremium.value) return "Sin suscripción activa"
        val until = _premiumUntil.value ?: return "Sin suscripción activa"
        if (until == 0L) return "Sin suscripción activa"
        return formatDuration(until)
    }

    suspend fun addBlueEssence(amount: Long, reason: String? = null) {
        val user = AuthManager.getAuth()?.currentUser ?: return
        if (AuthManager.isGuestOrUnauthenticated(user)) return
        val db = FirebaseFirestore.getInstance()
        try {
            val userRef = db.collection("users").document(user.uid)
            val currentBlue = _blueEssence.value
            val finalAmount = if (amount < 0 && currentBlue + amount < 0) -currentBlue else amount
            if (finalAmount == 0L) return
            userRef.update("blueEssence", FieldValue.increment(finalAmount)).await()
            _blueEssence.value = (currentBlue + finalAmount).coerceAtLeast(0L)

            val isSub = reason?.contains("Suscrip", ignoreCase = true) == true
            val isAdmin = reason?.contains("Admin", ignoreCase = true) == true
            val effectiveReason = reason ?: if (finalAmount > 0) "Recarga de Esencia Azul" else "Consumo de Esencia Azul"
            val status = when {
                isSub && finalAmount < 0 -> "Descontado por Suscripción"
                isSub && finalAmount > 0 -> "Añadido por Suscripción"
                isAdmin && finalAmount < 0 -> "Descontado por Administrador"
                isAdmin && finalAmount > 0 -> "Añadido por Administrador"
                finalAmount > 0 -> "Añadido"
                else -> "Descontado"
            }
            val amountStr = if (finalAmount > 0) "+$finalAmount EA" else "$finalAmount EA"
            SubscriptionHistoryManager.addRecordForUser(user.uid, 0L, effectiveReason, status, amountStr)
        } catch (e: Exception) {
            Log.e("SubscriptionManager", "Error incrementing blue essence", e)
        }
    }

    suspend fun addOrangeEssence(amount: Long, reason: String? = null) {
        val user = AuthManager.getAuth()?.currentUser ?: return
        if (AuthManager.isGuestOrUnauthenticated(user)) return
        val db = FirebaseFirestore.getInstance()
        try {
            val userRef = db.collection("users").document(user.uid)
            val currentOrange = _orangeEssence.value
            val finalAmount = if (amount < 0 && currentOrange + amount < 0) -currentOrange else amount
            if (finalAmount == 0L) return
            userRef.update("orangeEssence", FieldValue.increment(finalAmount)).await()
            _orangeEssence.value = (currentOrange + finalAmount).coerceAtLeast(0L)

            val isSub = reason?.contains("Suscrip", ignoreCase = true) == true
            val isAdmin = reason?.contains("Admin", ignoreCase = true) == true
            val effectiveReason = reason ?: if (finalAmount > 0) "Recarga de Esencia Naranja" else "Consumo de Esencia Naranja"
            val status = when {
                isSub && finalAmount < 0 -> "Descontado por Suscripción"
                isSub && finalAmount > 0 -> "Añadido por Suscripción"
                isAdmin && finalAmount < 0 -> "Descontado por Administrador"
                isAdmin && finalAmount > 0 -> "Añadido por Administrador"
                finalAmount > 0 -> "Añadido"
                else -> "Descontado"
            }
            val amountStr = if (finalAmount > 0) "+$finalAmount EN" else "$finalAmount EN"
            SubscriptionHistoryManager.addRecordForUser(user.uid, 0L, effectiveReason, status, amountStr)
        } catch (e: Exception) {
            Log.e("SubscriptionManager", "Error incrementing orange essence", e)
        }
    }
}
