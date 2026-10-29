package com.example.util

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthManager {

    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    private val _isAdminClaim = MutableStateFlow(false)
    val isAdminClaim: StateFlow<Boolean> = _isAdminClaim.asStateFlow()
    private val _permissionsLoading = MutableStateFlow(true)
    val permissionsLoading: StateFlow<Boolean> = _permissionsLoading.asStateFlow()
    private var permissionRequest = 0L

    const val GUEST_READER_EMAIL = "coach.guest.reader@gmail.com"

    fun isUserSignedIn(): Boolean {
        val user = getAuth()?.currentUser ?: return false
        return !user.isAnonymous && user.email != GUEST_READER_EMAIL
    }

    fun isGuestOrUnauthenticated(user: com.google.firebase.auth.FirebaseUser?): Boolean {
        if (user == null) return true
        return user.isAnonymous || user.email == GUEST_READER_EMAIL
    }

    init {
        setupAuthListeners()
    }

    private fun setupAuthListeners() {
        val auth = getAuth() ?: run { _permissionsLoading.value = false; return }

        auth.addIdTokenListener(FirebaseAuth.IdTokenListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _isSignedIn.value = !isGuestOrUnauthenticated(user)
            readClaims(forceRefresh = false)
        })

        auth.addAuthStateListener(FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _isAdminClaim.value = false
            _isSignedIn.value = !isGuestOrUnauthenticated(user)
            readClaims(forceRefresh = true)
        })
    }

    fun isFirebaseInitialized(): Boolean {
        return try {
            FirebaseApp.getInstance()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getAuth(): FirebaseAuth? {
        return if (isFirebaseInitialized()) {
            FirebaseAuth.getInstance()
        } else {
            null
        }
    }

    fun refreshClaims(onComplete: (Boolean) -> Unit = {}) {
        readClaims(forceRefresh = true, onComplete = onComplete)
    }

    private fun readClaims(forceRefresh: Boolean, onComplete: (Boolean) -> Unit = {}) {
        val request = ++permissionRequest
        val user = getAuth()?.currentUser
        if (isGuestOrUnauthenticated(user)) {
            _isAdminClaim.value = false
            _permissionsLoading.value = false
            onComplete(false)
            return
        }
        _permissionsLoading.value = true
        user!!.getIdToken(forceRefresh).addOnSuccessListener { result ->
            val isAdmin = result.claims["admin"] == true && result.claims["email_verified"] == true
            if (permissionRequest == request && getAuth()?.currentUser?.uid == user.uid) {
                _isAdminClaim.value = isAdmin
                _permissionsLoading.value = false
                onComplete(isAdmin)
            } else onComplete(false)
        }.addOnFailureListener {
            if (permissionRequest == request && getAuth()?.currentUser?.uid == user.uid) {
                _isAdminClaim.value = false
                _permissionsLoading.value = false
            }
            onComplete(false)
        }
    }

    fun isCurrentUserAdmin(): Boolean {
        return _isAdminClaim.value
    }

    fun isCurrentUserEmailVerified(): Boolean {
        val user = getAuth()?.currentUser ?: return false
        if (isGuestOrUnauthenticated(user)) return false
        return user.isEmailVerified
    }
}
