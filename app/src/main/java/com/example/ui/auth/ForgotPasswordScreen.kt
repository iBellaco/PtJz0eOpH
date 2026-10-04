package com.example.ui.auth

import com.example.ui.components.coachClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.TextPrimary
import com.example.util.tr

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val email by viewModel.email.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthHeader(
            title = "Recuperar contraseña",
            subtitle = "Ingresa tu correo para recibir un enlace"
        )

        if (uiState.isSuccess) {
            Text(
                text = tr("Se ha enviado un enlace de recuperación a tu correo electrónico."),
                color = HextechCyan,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            AuthPrimaryButton(
                text = "Volver a iniciar sesión",
                onClick = onNavigateToLogin
            )
        } else {
            AuthTextField(
                value = email,
                onValueChange = viewModel::updateEmail,
                label = "Correo electrónico"
            )

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
                text = "Enviar enlace de recuperación",
                onClick = { viewModel.resetPassword() },
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            com.example.ui.components.HextechAnimatedTextLink(
                text = tr("Volver a iniciar sesión"),
                onClick = onNavigateToLogin,
                color = HextechCyan
            )
        }
    }
}
