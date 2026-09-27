package com.example.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.tr

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val username by viewModel.username.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val passwordStrength by viewModel.passwordStrength.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.resetSuccessState()
            onRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthHeader(
            title = "Crear una cuenta",
            subtitle = "Regístrate para comenzar"
        )

        AuthTextField(
            value = username,
            onValueChange = viewModel::updateUsername,
            label = "Nombre de usuario"
        )
        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = email,
            onValueChange = viewModel::updateEmail,
            label = "Correo electrónico"
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        PasswordTextField(
            value = password,
            onValueChange = viewModel::updatePassword,
            label = "Contraseña",
            imeAction = ImeAction.Next
        )

        PasswordStrengthIndicator(strength = passwordStrength)

        Spacer(modifier = Modifier.height(8.dp))

        PasswordTextField(
            value = confirmPassword,
            onValueChange = viewModel::updateConfirmPassword,
            label = "Confirmar contraseña",
            imeAction = ImeAction.Done
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        // Password Requirements
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(tr("Requisitos de contraseña:"), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(tr("- Mínimo 8 caracteres"), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(tr("- Una letra mayúscula y minúscula"), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(tr("- Un número y carácter especial"), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = HextechCyan.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = HextechCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tr("Para poder iniciar sesión, deberás verificar tu correo electrónico con el enlace de confirmación que te enviaremos."),
                    color = TextPrimary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.error != null) {
            Text(
                text = tr(uiState.error!!),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }

        AuthPrimaryButton(
            text = "Crear perfil",
            onClick = { viewModel.register() },
            isLoading = uiState.isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = tr("¿Ya tienes una cuenta? "), color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
            com.example.ui.components.HextechAnimatedTextLink(
                text = tr("Inicia sesión"),
                onClick = onNavigateToLogin,
                color = HextechCyan
            )
        }
    }
}
