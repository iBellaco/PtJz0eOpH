package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppUserSecondaryRole
import com.example.ui.theme.*
import com.example.util.SubscriptionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrameSelectionDialog(
    currentAvatarId: String,
    currentRankBorder: String,
    currentSecondaryRole: String,
    isAdmin: Boolean,
    currentActivePreference: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activeTheme = AppThemeManager.currentTheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSaving by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(currentActivePreference.uppercase()) }

    val secRoleObj = remember(currentSecondaryRole) {
        if (currentSecondaryRole.isNotBlank() && currentSecondaryRole != "none") {
            AppUserSecondaryRole.fromId(currentSecondaryRole)
        } else AppUserSecondaryRole.NONE
    }
    val hasSecRoleFrame = secRoleObj != AppUserSecondaryRole.NONE && secRoleObj.frameDrawableRes != null
    val hasSpecialFrame = isAdmin || (currentRankBorder != "NONE" && currentRankBorder.isNotBlank())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = activeTheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(activeTheme.cardBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Marco de Perfil / Visualización",
                color = activeTheme.secondary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Selecciona el marco que deseas equipar en tu avatar.",
                color = activeTheme.textSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Vista previa en vivo del avatar con la opción seleccionada
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(activeTheme.surfaceVariant.copy(alpha = 0.4f), CircleShape)
                    .border(1.dp, activeTheme.cardBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                UserAvatarView(
                    avatarId = currentAvatarId,
                    rankBorder = currentRankBorder,
                    secondaryRole = currentSecondaryRole,
                    isAdmin = isAdmin,
                    equippedFrame = selectedOption,
                    size = 46.dp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Opción 1: Sin Marco / Desactivado
            FrameOptionCard(
                title = "Sin Marco (Desactivado)",
                subtitle = "Oculta el marco exterior y muestra solo el avatar.",
                icon = Icons.Default.VisibilityOff,
                iconTint = activeTheme.textSecondary,
                isSelected = selectedOption == "NONE",
                isEnabled = true,
                activeTheme = activeTheme,
                onClick = { selectedOption = "NONE" }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Opción 2: Automático
            FrameOptionCard(
                title = "Automático (Recomendado)",
                subtitle = "Muestra el marco disponible de mayor jerarquía.",
                icon = Icons.Default.Star,
                iconTint = HextechGold,
                isSelected = selectedOption == "AUTO",
                isEnabled = true,
                activeTheme = activeTheme,
                onClick = { selectedOption = "AUTO" }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Opción 3: Rol Secundario
            val secRoleTitle = if (hasSecRoleFrame) "Marco de ${secRoleObj.displayName}" else "Marco de Rol Secundario"
            val secRoleSubtitle = if (hasSecRoleFrame) {
                "Marco exclusivo otorgado por Moderación/Administración."
            } else {
                "Requiere un rol secundario asignado (Esmeralda a Soberano)."
            }
            FrameOptionCard(
                title = secRoleTitle,
                subtitle = secRoleSubtitle,
                icon = Icons.Default.Shield,
                iconTint = if (hasSecRoleFrame) secRoleObj.primaryColor else activeTheme.textSecondary.copy(alpha = 0.5f),
                isSelected = selectedOption == "SECONDARY",
                isEnabled = hasSecRoleFrame,
                activeTheme = activeTheme,
                onClick = {
                    if (hasSecRoleFrame) {
                        selectedOption = "SECONDARY"
                    } else {
                        Toast.makeText(context, "No posees un marco de rol secundario asignado", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Opción 4: Marco Especial / Rango Competitivo / Admin
            val specialTitle = if (isAdmin) {
                "Marco de Administrador"
            } else if (currentRankBorder != "NONE") {
                "Marco de Rango $currentRankBorder"
            } else {
                "Marco Especial / Rango"
            }
            val specialSubtitle = if (hasSpecialFrame) {
                "Marco de estatus especial o rango competitivo."
            } else {
                "No posees un marco especial de rango disponible."
            }
            FrameOptionCard(
                title = specialTitle,
                subtitle = specialSubtitle,
                icon = Icons.Default.Star,
                iconTint = if (hasSpecialFrame) HextechGold else activeTheme.textSecondary.copy(alpha = 0.5f),
                isSelected = selectedOption == "SPECIAL" || selectedOption == "RANK",
                isEnabled = hasSpecialFrame,
                activeTheme = activeTheme,
                onClick = {
                    if (hasSpecialFrame) {
                        selectedOption = "SPECIAL"
                    } else {
                        Toast.makeText(context, "No posees un marco especial de rango disponible", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Guardar
            Button(
                onClick = {
                    isSaving = true
                    SubscriptionManager.changeActiveFramePreference(
                        preference = selectedOption,
                        onSuccess = {
                            isSaving = false
                            Toast.makeText(context, "Marco de perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        onError = { err ->
                            isSaving = false
                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HextechGold,
                    contentColor = HextechDarkBg
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = HextechDarkBg,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Guardar Preferencia",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FrameOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    isSelected: Boolean,
    isEnabled: Boolean,
    activeTheme: AppTheme,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) activeTheme.primary.copy(alpha = 0.15f) else activeTheme.surfaceVariant.copy(alpha = if (isEnabled) 0.45f else 0.2f)
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) HextechGold else activeTheme.cardBorder.copy(alpha = if (isEnabled) 1f else 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isEnabled) iconTint else activeTheme.textSecondary.copy(alpha = 0.4f),
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = title,
                        color = if (isEnabled) activeTheme.textPrimary else activeTheme.textSecondary.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = if (isEnabled) activeTheme.textSecondary else activeTheme.textSecondary.copy(alpha = 0.4f),
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) HextechGold else activeTheme.textSecondary.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
