package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppUserSecondaryRole
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGoldLight
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.SubscriptionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondaryRoleSelectionDialog(
    currentSecondaryRole: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSaving by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = HextechDarkBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(HextechGold.copy(alpha = 0.5f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = HextechGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Rol Secundario",
                            color = HextechGoldLight,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Rango competitivo mostrado en tu perfil",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Current role preview banner
            val activeRole = AppUserSecondaryRole.fromId(currentSecondaryRole)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = HextechSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, activeRole.primaryColor.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "ROL SECUNDARIO ACTIVO",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = activeRole.displayName,
                            color = activeRole.primaryColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    SecondaryRoleBadge(
                        secondaryRole = currentSecondaryRole,
                        size = RoleBadgeSize.NORMAL
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SELECCIONA TU ROL / RANGO",
                color = HextechGold,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AppUserSecondaryRole.assignableSecondaryRoles) { roleItem ->
                    val isSelected = activeRole == roleItem
                    val roleBorderColor = if (isSelected) roleItem.primaryColor else HextechCardBorder.copy(alpha = 0.6f)
                    val bgAlpha = if (isSelected) 0.22f else 0.08f

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isSaving) {
                                if (isSelected) return@clickable
                                isSaving = true
                                SubscriptionManager.changeSecondaryRole(
                                    roleId = roleItem.id,
                                    onSuccess = {
                                        isSaving = false
                                        Toast.makeText(
                                            context,
                                            "Rol secundario actualizado a ${roleItem.displayName}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onDismiss()
                                    },
                                    onError = { err ->
                                        isSaving = false
                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = HextechSurface,
                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, roleBorderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            roleItem.primaryColor.copy(alpha = bgAlpha),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(roleItem.primaryColor)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = roleItem.displayName,
                                            color = if (isSelected) roleItem.primaryColor else TextPrimary,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                            fontSize = 13.5.sp
                                        )
                                        if (roleItem.tierLevel >= 6) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "ALTO ELO",
                                                color = roleItem.primaryColor.copy(alpha = 0.85f),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(
                                        text = roleItem.description,
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (roleItem != AppUserSecondaryRole.NONE) {
                                    SecondaryRoleBadge(
                                        secondaryRole = roleItem.id,
                                        size = RoleBadgeSize.COMPACT
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(roleItem.primaryColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = HextechDarkBg,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
