package com.example.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
        _uiState.update { it.copy(authScreen = screen, error = null, isSuccess = false) }
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
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = auth.signInWithEmailAndPassword(_email.value.trim(), _password.value).await()
                val firebaseUser = result.user
                if (firebaseUser != null) {
                    val cancelled = try { com.example.data.AccountDeletionRepository.cancelAfterSignIn() }
                    catch (failure: Exception) {
                        if (failure is kotlinx.coroutines.CancellationException) throw failure
                        throw IllegalStateException("No se pudo comprobar la solicitud de eliminación. Revisa tu conexión o contacta a soporte.")
                    }
                    _uiState.update { it.copy(deletionCancelled = cancelled) }
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val userDocRef = db.collection("users").document(firebaseUser.uid)
                    val snap = userDocRef.get().await()
                    val userData = mutableMapOf<String, Any>(
                        "uid" to firebaseUser.uid,
                        "email" to (firebaseUser.email ?: _email.value.trim()),
                        "name" to (firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "Usuario"),
                        "last_active" to System.currentTimeMillis(),
                        "is_online" to false
                    )
                    if (!snap.exists()) {
                        userData["role"] = "free"
                        userData["createdAt"] = System.currentTimeMillis()
                        userData["blueEssence"] = 0L
                        userData["orangeEssence"] = 0L
                        userDocRef.set(userData, com.google.firebase.firestore.SetOptions.merge()).await()
                    } else {
                        // The confirmed session transaction owns presence updates.
                    }
                }
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                // Do not leave an authenticated session when a deletion cannot be cancelled.
                auth.signOut()
                val errorMsg = e.localizedMessage ?: "Error de autenticación. Verifica tus credenciales."
                _uiState.update { it.copy(isLoading = false, error = errorMsg) }
            }
        }
    }

    fun register() {
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
        if (calculatePasswordStrength(_password.value) != PasswordStrength.STRONG) {
            _uiState.update { it.copy(error = "Contraseña débil. Debe contener al menos 3 de: mayúsculas, minúsculas, números, caracteres especiales.") }
            return
        }

        if (auth == null) {
            _uiState.update { it.copy(error = "Firebase no configurado. Falta google-services.json.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = auth.createUserWithEmailAndPassword(_email.value.trim(), _password.value).await()
                val firebaseUser = result.user
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(_username.value.trim())
                    .build()
                firebaseUser?.updateProfile(profileUpdates)?.await()
                firebaseUser?.sendEmailVerification()?.await()

                if (firebaseUser != null) {
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val userDocRef = db.collection("users").document(firebaseUser.uid)
                    val userData = mapOf(
                        "uid" to firebaseUser.uid,
                        "email" to (firebaseUser.email ?: _email.value.trim()),
                        "name" to _username.value.trim(),
                        "role" to "free",
                        "createdAt" to System.currentTimeMillis(),
                        "blueEssence" to 0L,
                        "orangeEssence" to 0L,
                        "last_active" to System.currentTimeMillis(),
                        "is_online" to false
                    )
                    userDocRef.set(userData, com.google.firebase.firestore.SetOptions.merge()).await()
                }

                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                val rawMsg = e.localizedMessage ?: ""
                val errorMsg = when {
                    rawMsg.contains("email address is already in use", ignoreCase = true) || rawMsg.contains("already in use", ignoreCase = true) -> "Este correo electrónico ya está registrado. Evita crear múltiples cuentas con el mismo correo."
                    rawMsg.contains("badly formatted", ignoreCase = true) || rawMsg.contains("invalid email", ignoreCase = true) -> "El formato del correo electrónico es inválido."
                    rawMsg.contains("network", ignoreCase = true) -> "Error de red. Verifica tu conexión a internet."
                    else -> "Este correo ya se encuentra registrado o la cuenta no pudo crearse. Verifica tus datos."
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
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                auth.sendPasswordResetEmail(_email.value).await()
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Error al enviar el enlace de recuperación."
                _uiState.update { it.copy(isLoading = false, error = errorMsg) }
            }
        }
    }

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
            password.length >= 8 && points >= 3 -> PasswordStrength.STRONG
            points >= 2 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }
}
