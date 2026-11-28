package com.example.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.util.AuthManager
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuthException

enum class PasswordStrength(val label: String) {
    NONE(""),
    WEAK("Débil"),
    MEDIUM("Media"),
    STRONG("Fuerte")
}

data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val deletionCancelled: Boolean = false,
    val authScreen: AuthScreenType = AuthScreenType.LOGIN
)

enum class AuthScreenType {
    LOGIN, REGISTER, FORGOT_PASSWORD, EMAIL_VERIFICATION
}

class AuthViewModel : ViewModel() {

    private val auth = AuthManager.getAuth()

    private val _uiState = MutableStateFlow(AuthState())
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _passwordStrength = MutableStateFlow(PasswordStrength.NONE)
    val passwordStrength: StateFlow<PasswordStrength> = _passwordStrength.asStateFlow()

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
        clearError()
    }

    fun updateUsername(newUsername: String) {
        _username.value = newUsername
        clearError()
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
        _passwordStrength.value = calculatePasswordStrength(newPassword)
        clearError()
    }

    fun updateConfirmPassword(newConfirm: String) {
        _confirmPassword.value = newConfirm
        clearError()
    }

    fun resetSuccessState() {
        _uiState.update { it.copy(isSuccess = false, error = null, deletionCancelled = false) }
    }

    fun navigateTo(screen: AuthScreenType) {
        _uiState.update { it.copy(authScreen = screen, error = null, isSuccess = false, deletionCancelled = false) }
        if (screen == AuthScreenType.LOGIN || screen == AuthScreenType.REGISTER) {
             // Keep email, but maybe clear passwords if we want
        }
    }

    private fun clearError() {
        if (_uiState.value.error != null) {
            _uiState.update { it.copy(error = null) }
        }
    }

    fun login() {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _uiState.update { it.copy(error = "Por favor, completa todos los campos.") }
            return
        }
        
        if (auth == null) {
            _uiState.update { it.copy(error = "Firebase no configurado. Falta google-services.json.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, deletionCancelled = false) }
            try {
                val result = auth.signInWithEmailAndPassword(_email.value.trim(), _password.value).await()
                val firebaseUser = result.user
                if (firebaseUser != null) {
                    firebaseUser.reload().await()
                    if (!firebaseUser.isEmailVerified) {
                        firebaseUser.sendEmailVerification().await()
                        throw IllegalStateException("Debes confirmar tu correo electrónico antes de entrar. Te hemos enviado un nuevo enlace.")
                    }
                    // The credential and verification checks are complete. Let
                    // the profile appear now; maintenance work continues without
                    // holding up a valid sign-in or overwriting session presence.
                    startPostSignInMaintenance(firebaseUser, _email.value.trim())
                }
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                auth.signOut()
                val errorMsg = localizedAuthError(e)
                _uiState.update { it.copy(isLoading = false, error = errorMsg) }
            }
        }
    }

    /**
     * Cleanup and profile defaults are useful, but neither is allowed to keep
     * a verified account on the sign-in screen. The session transaction owns
     * online/session fields, so this maintenance deliberately never writes them.
     */
    private fun startPostSignInMaintenance(firebaseUser: FirebaseUser, fallbackEmail: String) {
        viewModelScope.launch {
            try {
                com.example.data.AccountDeletionRepository.cancelAfterSignIn()
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                Log.w("AuthViewModel", "No se pudo cancelar la eliminación pendiente tras iniciar sesión", error)
            }

            try {
                ensureSignedInProfile(firebaseUser, fallbackEmail)
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                Log.w("AuthViewModel", "No se pudo completar el perfil tras iniciar sesión", error)
            }
        }
    }

    private suspend fun ensureSignedInProfile(firebaseUser: FirebaseUser, fallbackEmail: String) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(firebaseUser.uid)
        FirebaseFirestore.getInstance().runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val now = System.currentTimeMillis()
            val updates = mutableMapOf<String, Any>(
                "uid" to firebaseUser.uid,
                "email" to (firebaseUser.email ?: fallbackEmail),
                "name" to (firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "Usuario"),
                "last_active" to now
            )
            if (!snapshot.exists() || snapshot.getString("role").isNullOrBlank()) updates["role"] = "free"
            if (!snapshot.exists() || snapshot.get("createdAt") == null) updates["createdAt"] = now
            if (!snapshot.exists() || snapshot.get("blueEssence") == null) updates["blueEssence"] = 0L
            if (!snapshot.exists() || snapshot.get("orangeEssence") == null) updates["orangeEssence"] = 0L
            transaction.set(userRef, updates, SetOptions.merge())
        }.await()
    }

    fun register() {
        if (_uiState.value.isLoading) return
        if (_email.value.isBlank() || _password.value.isBlank() || _username.value.isBlank()) {
            _uiState.update { it.copy(error = "Por favor, completa todos los campos.") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_email.value.trim()).matches()) {
            _uiState.update { it.copy(error = "El formato del correo electrónico no es válido.") }
            return
        }
        if (_password.value != _confirmPassword.value) {
            _uiState.update { it.copy(error = "Las contraseñas no coinciden.") }
            return
        }
        if (_password.value.length < 8) {
            _uiState.update { it.copy(error = "La contraseña debe tener mínimo 8 caracteres.") }
            return
        }
        if (!PasswordPolicy.accepts(_password.value)) {
            _uiState.update { it.copy(error = authText(com.example.R.string.password_requirements_error)) }
            return
        }

        if (auth == null) {
            _uiState.update { it.copy(error = "Firebase no configurado. Falta google-services.json.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, deletionCancelled = false) }
            var accountCreated = false
            try {
                val result = auth.createUserWithEmailAndPassword(_email.value.trim(), _password.value).await()
                val firebaseUser = checkNotNull(result.user)
                accountCreated = true
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(_username.value.trim())
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()
                firebaseUser.sendEmailVerification().await()
                // The verified login creates the profile. Unverified accounts cannot write it.

                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                if (accountCreated) {
                    auth.signOut()
                    _uiState.update { it.copy(isLoading = false, authScreen = AuthScreenType.LOGIN,
                        error = authText(com.example.R.string.registration_created_retry_verification)) }
                    return@launch
                }
                val rawMsg = e.localizedMessage ?: ""
                val errorMsg = when {
                    rawMsg.contains("email address is already in use", ignoreCase = true) || rawMsg.contains("already in use", ignoreCase = true) -> "Este correo electrónico ya está registrado. Evita crear múltiples cuentas con el mismo correo."
                    rawMsg.contains("badly formatted", ignoreCase = true) || rawMsg.contains("invalid email", ignoreCase = true) -> "El formato del correo electrónico es inválido."
                    rawMsg.contains("network", ignoreCase = true) -> "Error de red. Verifica tu conexión a internet."
                    rawMsg.contains("too many requests", ignoreCase = true) || rawMsg.contains("blocked all requests", ignoreCase = true) -> "Demasiados intentos. Espera unos minutos y vuelve a intentarlo."
                    else -> localizedAuthError(e, fallback = "La cuenta no pudo crearse. Verifica tus datos.")
                }
                _uiState.update { it.copy(isLoading = false, error = errorMsg) }
            }
        }
    }
    
    fun resetPassword() {
        if (_email.value.isBlank()) {
            _uiState.update { it.copy(error = "Por favor, ingresa tu correo electrónico.") }
            return
        }
        
        if (auth == null) {
            _uiState.update { it.copy(error = "Firebase no configurado.") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, deletionCancelled = false) }
            try {
                auth.sendPasswordResetEmail(_email.value).await()
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                val errorMsg = localizedAuthError(e, fallback = "No se pudo enviar el enlace de recuperación.")
                _uiState.update { it.copy(isLoading = false, error = errorMsg) }
            }
        }
    }

    /** Never expose Firebase/English provider text in the app UI. The returned keys are
     * translated by the offline catalog according to the language selected in Coach. */
    private fun localizedAuthError(error: Throwable, fallback: String = "Error de autenticación. Verifica tus credenciales."): String {
        val code = (error as? FirebaseAuthException)?.errorCode.orEmpty().lowercase()
        val raw = error.localizedMessage.orEmpty().lowercase()
        if (raw.contains("debes confirmar tu correo") || raw.contains("no se pudo comprobar la solicitud")) {
            return error.localizedMessage.orEmpty()
        }
        return when {
            code.contains("too_many") || raw.contains("too many requests") || raw.contains("blocked all requests") ->
                "Demasiados intentos. Espera unos minutos y vuelve a intentarlo."
            code.contains("invalid_credential") || code.contains("wrong_password") || code.contains("user_not_found") ||
                raw.contains("supplied auth credential") || raw.contains("invalid credential") ->
                "El correo o la contraseña no son correctos."
            code.contains("invalid_email") || raw.contains("badly formatted") ->
                "El formato del correo electrónico no es válido."
            code.contains("network") || raw.contains("network") ->
                "Error de red. Verifica tu conexión a internet."
            code.contains("user_disabled") -> "Esta cuenta está deshabilitada. Contacta con soporte."
            else -> fallback
        }
    }

    private fun authText(id: Int): String = com.google.firebase.FirebaseApp.getInstance().applicationContext
        .createConfigurationContext(android.content.res.Configuration().apply {
            setLocale(com.example.util.AppLanguage.locale(com.example.util.AppLanguage.current.value))
        }).getString(id)

    private fun calculatePasswordStrength(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength.NONE
        if (password.length < 6) return PasswordStrength.WEAK
        
        var hasUpper = false
        var hasLower = false
        var hasDigit = false
        var hasSpecial = false
        
        password.forEach {
            if (it.isUpperCase()) hasUpper = true
            else if (it.isLowerCase()) hasLower = true
            else if (it.isDigit()) hasDigit = true
            else hasSpecial = true
        }
        
        val points = listOf(hasUpper, hasLower, hasDigit, hasSpecial).count { it }
        
        return when {
            PasswordPolicy.accepts(password) -> PasswordStrength.STRONG
            points >= 2 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }
}
