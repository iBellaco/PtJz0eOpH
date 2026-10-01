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
        val auth = getAuth() ?: return

        auth.addIdTokenListener(FirebaseAuth.IdTokenListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _isSignedIn.value = !isGuestOrUnauthenticated(user)
            if (user != null && !isGuestOrUnauthenticated(user)) {
                user.getIdToken(false).addOnSuccessListener { result ->
                    val claims = result.claims
                    val isAdmin = claims["admin"] == true
                    _isAdminClaim.value = isAdmin
                }.addOnFailureListener {
                    _isAdminClaim.value = false
                }
            } else {
                _isAdminClaim.value = false
            }
        })

        auth.addAuthStateListener(FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _isSignedIn.value = !isGuestOrUnauthenticated(user)
            if (user != null && !isGuestOrUnauthenticated(user)) {
                user.getIdToken(false).addOnSuccessListener { result ->
                    val claims = result.claims
                    val isAdmin = claims["admin"] == true
                    _isAdminClaim.value = isAdmin
                }.addOnFailureListener {
                    _isAdminClaim.value = false
                }
            } else {
                _isAdminClaim.value = false
            }
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
        val user = getAuth()?.currentUser
        if (isGuestOrUnauthenticated(user)) {
            _isAdminClaim.value = false
            onComplete(false)
            return
        }
        user!!.getIdToken(true).addOnSuccessListener { result ->
            val isAdmin = result.claims["admin"] == true
            _isAdminClaim.value = isAdmin
            onComplete(isAdmin)
        }.addOnFailureListener {
            onComplete(_isAdminClaim.value)
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
