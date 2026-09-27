package com.example.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.tr

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgot: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.resetSuccessState()
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthHeader(
            title = "Bienvenido de nuevo",
            subtitle = "Inicia sesión para continuar"
        )

        AuthTextField(
            value = email,
            onValueChange = viewModel::updateEmail,
            label = "Correo electrónico"
        )
        Spacer(modifier = Modifier.height(6.dp))
        
        // Acceso rápido horizontal para dominios de correo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val domains = listOf("@gmail.com", "@outlook.com", "@hotmail.com")
            domains.forEach { domain ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clickable {
                            val base = if (email.contains("@")) email.substringBefore("@") else email
                            val cleanBase = if (base.isBlank()) "usuario" else base
                            viewModel.updateEmail(cleanBase + domain)
                        }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = domain,
                            color = HextechCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        PasswordTextField(
            value = password,
            onValueChange = viewModel::updatePassword,
            label = "Contraseña",
            imeAction = ImeAction.Done
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            com.example.ui.components.HextechAnimatedTextLink(
                text = tr("¿Olvidaste tu contraseña?"),
                onClick = onNavigateToForgot,
                color = HextechCyan
            )
        }

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
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        AuthPrimaryButton(
            text = "Iniciar Sesión",
            onClick = { viewModel.login() },
            isLoading = uiState.isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = tr("¿No tienes una cuenta? "), color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
            com.example.ui.components.HextechAnimatedTextLink(
                text = tr("Regístrate"),
                onClick = onNavigateToRegister,
                color = HextechCyan
            )
        }
    }
}
