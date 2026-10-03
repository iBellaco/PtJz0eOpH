package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.util.tr

@Composable
fun AuthHeader(title: String, subtitle: String, compact: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
    ) {
        // App Logo Placeholder (using a stylized Icon)
        Box(
            modifier = Modifier
                .size(if (compact) 42.dp else 58.dp)
                .clip(CircleShape)
                .background(HextechCyan.copy(alpha = 0.1f))
                .border(2.dp, HextechGold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = tr("Logo"),
                tint = HextechCyan,
                modifier = Modifier.size(if (compact) 24.dp else 34.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = tr(title),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tr(subtitle),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextSecondary
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    errorMessage: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(tr(label)) },
            leadingIcon = leadingIcon,
            modifier = Modifier.fillMaxWidth(),
            isError = isError,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HextechCyan,
                unfocusedBorderColor = HextechCardBorder,
                focusedLabelColor = HextechCyan,
                unfocusedLabelColor = TextMuted,
                cursorColor = HextechCyan,
                errorBorderColor = DangerRed,
                errorLabelColor = DangerRed,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
        AnimatedVisibility(visible = isError && errorMessage != null) {
            Text(
                text = tr(errorMessage ?: ""),
                color = DangerRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    errorMessage: String? = null,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(tr(label)) },
            modifier = Modifier.fillMaxWidth(),
            isError = isError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = imeAction
            ),
            keyboardActions = keyboardActions,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = tr("Mostrar contraseña"), tint = TextMuted)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HextechCyan,
                unfocusedBorderColor = HextechCardBorder,
                focusedLabelColor = HextechCyan,
                unfocusedLabelColor = TextMuted,
                cursorColor = HextechCyan,
                errorBorderColor = DangerRed,
                errorLabelColor = DangerRed,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
        AnimatedVisibility(visible = isError && errorMessage != null) {
            Text(
                text = tr(errorMessage ?: ""),
                color = DangerRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun PasswordStrengthIndicator(strength: PasswordStrength) {
    val color by animateColorAsState(
        targetValue = when (strength) {
            PasswordStrength.NONE -> HextechCardBorder
            PasswordStrength.WEAK -> DangerRed
            PasswordStrength.MEDIUM -> TierAColor
            PasswordStrength.STRONG -> TierSColor
        }, label = "strengthColor"
    )
    
    val fillPercentage = when (strength) {
        PasswordStrength.NONE -> 0f
        PasswordStrength.WEAK -> 0.33f
        PasswordStrength.MEDIUM -> 0.66f
        PasswordStrength.STRONG -> 1f
    }

    val animatedProgress by animateFloatAsState(targetValue = fillPercentage, animationSpec = tween(500), label = "strengthProgress")

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(tr("Seguridad de contraseña:"), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(
                text = tr(strength.label),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 1..3) {
                val barColor = if (animatedProgress >= i / 3f) color else HextechCardBorder
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(barColor)
                )
            }
        }
    }
}

@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    com.example.ui.components.HextechAnimatedButton(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        backgroundBrush = androidx.compose.ui.graphics.Brush.horizontalGradient(
            listOf(HextechCyan, HextechBlue)
        ),
        borderColor = HextechGold,
        glowColor = HextechCyan,
        enableShimmer = true,
        enablePulse = true,
        scaleDown = 0.92f
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = HextechDarkBg,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = tr(text),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    color = HextechDarkBg
                )
            )
        }
    }
}

@Composable
fun AuthSecondaryButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    com.example.ui.components.HextechAnimatedOutlinedButton(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        borderColor = HextechCyan,
        glowColor = HextechCyan,
        scaleDown = 0.92f
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = HextechCyan,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = tr(text),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp,
                    color = HextechCyan
                )
            )
        }
    }
}

@Composable
fun AuthDivider(text: String = "o") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f).height(1.dp).background(HextechCardBorder))
        Text(
            text = tr(text),
            modifier = Modifier.padding(horizontal = 16.dp),
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
        Box(modifier = Modifier.weight(1f).height(1.dp).background(HextechCardBorder))
    }
}

@Composable
fun SocialLoginButton(
    text: String,
    onClick: () -> Unit,
    iconRes: Int? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = 380f
        ),
        label = "socialBtnScale"
    )

    OutlinedButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, HextechCardBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = TextPrimary
        )
    ) {
        Text(text = tr(text), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
    }
}
