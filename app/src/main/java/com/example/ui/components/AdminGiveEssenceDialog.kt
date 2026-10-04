package com.example.ui.components

import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachOutlinedButton as OutlinedButton
import com.example.ui.components.CoachTextButton as TextButton

import com.example.util.tr

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import kotlinx.coroutines.launch

@Composable
fun AdminGiveEssenceDialog(
    userUid: String,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    onBalancesUpdated: (Map<String, Any>) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var failed by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("BLUE") } // "BLUE" or "ORANGE"
    var isAddition by remember { mutableStateOf(true) } // true: Añadir (+), false: Descontar (-)
    var isProcessing by remember { mutableStateOf(false) }

    // Message notification settings
    var notifyUser by remember { mutableStateOf(true) }
    var isCustomMessage by remember { mutableStateOf(false) }
    var customTitle by remember { mutableStateOf("") }
    var customBody by remember { mutableStateOf("") }

    val defaultTitle = remember(isAddition) {
        if (isAddition) "¡Recompensa de Esencias!" else "Ajuste de Saldo de Esencias"
    }

    val defaultBody = remember(isAddition, selectedCurrency, amount) {
        val currTag = if (selectedCurrency == "BLUE") "Esencias Azules (EA)" else "Esencias Naranjas (EN)"
        val amtDisplay = amount.ifBlank { "0" }
        if (isAddition) {
            "¡Felicidades! Se han acreditado +$amtDisplay $currTag a tu cuenta de Coach. ¡Disfrútalas en el catálogo y tienda!"
        } else {
            "Se ha realizado un ajuste de -$amtDisplay $currTag en tu saldo por parte del equipo de administración."
        }
    }

    val effectiveTitle = if (isCustomMessage && customTitle.isNotBlank()) customTitle.trim() else defaultTitle
    val effectiveBody = if (isCustomMessage && customBody.isNotBlank()) customBody.trim() else defaultBody

    val activeColor = if (selectedCurrency == "BLUE") Color(0xFF0EA5E9) else Color(0xFFFF9E1B)

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, activeColor),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = if (selectedCurrency == "BLUE") R.drawable.ic_blue_essence else R.drawable.ic_orange_essence),
                        contentDescription = com.example.util.tr("Esencia"),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr("Ajustar Esencias de Usuario"),
                        color = activeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Currency selector (Blue / Orange)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedCurrency = "BLUE" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedCurrency == "BLUE") Color(0xFF0EA5E9) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_blue_essence),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(tr("Azul (EA)"), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { selectedCurrency = "ORANGE" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedCurrency == "ORANGE") Color(0xFFFF8C00) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_orange_essence),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(tr("Naranja (EN)"), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Operation toggle: Añadir (+) vs Descontar (-)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { isAddition = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isAddition) Color(0xFF00FF7F).copy(alpha = 0.2f) else Color.Transparent
                        ),
                        border = BorderStroke(1.dp, if (isAddition) Color(0xFF00FF7F) else Color.Gray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(tr("+ Añadir"), color = if (isAddition) Color(0xFF00FF7F) else Color.Gray, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { isAddition = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (!isAddition) Color(0xFFFF5252).copy(alpha = 0.2f) else Color.Transparent
                        ),
                        border = BorderStroke(1.dp, if (!isAddition) Color(0xFFFF5252) else Color.Gray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(tr("- Descontar"), color = if (!isAddition) Color(0xFFFF5252) else Color.Gray, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Amount input field
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(tr("Cantidad de esencias"), color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = activeColor
                    )
                )

                // Quick preset buttons
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("100", "500", "1000", "5000").forEach { preset ->
                        OutlinedButton(
                            onClick = { amount = preset },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.5.dp, activeColor.copy(alpha = 0.5f))
                        ) {
                            Text(com.example.util.tr("+$preset"), fontSize = 10.5.sp, color = activeColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // User Notification Section
                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.5.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = if (notifyUser) Color(0xFF38BDF8) else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tr("Notificar al usuario en buzón"),
                                    color = if (notifyUser) Color.White else Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Switch(
                                checked = notifyUser,
                                onCheckedChange = { notifyUser = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = activeColor,
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color(0xFF334155)
                                )
                            )
                        }

                        AnimatedVisibility(visible = notifyUser) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                // Toggle between Default & Custom message
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = com.example.util.tr(if (isCustomMessage) "Mensaje personalizado:" else "Mensaje predeterminado:"),
                                        color = if (isCustomMessage) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    TextButton(
                                        onClick = {
                                            if (!isCustomMessage) {
                                                customTitle = defaultTitle
                                                customBody = defaultBody
                                            }
                                            isCustomMessage = !isCustomMessage
                                        },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isCustomMessage) Icons.Default.Refresh else Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = if (isCustomMessage) Color(0xFF38BDF8) else Color(0xFFF59E0B),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = com.example.util.tr(if (isCustomMessage) "Usar predeterminado" else "Personalizar mensaje"),
                                            color = if (isCustomMessage) Color(0xFF38BDF8) else Color(0xFFF59E0B),
                                            fontSize = 10.5.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                if (isCustomMessage) {
                                    OutlinedTextField(
                                        value = customTitle,
                                        onValueChange = { customTitle = it },
                                        label = { Text(tr("Título del mensaje"), color = Color.Gray, fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFFF59E0B)
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    OutlinedTextField(
                                        value = customBody,
                                        onValueChange = { customBody = it },
                                        label = { Text(tr("Contenido del mensaje..."), color = Color.Gray, fontSize = 11.sp) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFFF59E0B)
                                        )
                                    )
                                } else {
                                    Surface(
                                        color = Color(0xFF0F172A),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(0.5.dp, Color(0xFF334155)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.ChatBubbleOutline,
                                                    contentDescription = null,
                                                    tint = Color(0xFF38BDF8),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = com.example.util.tr(defaultTitle),
                                                    color = Color(0xFF38BDF8),
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = com.example.util.tr(defaultBody),
                                                color = Color(0xFFCBD5E1),
                                                fontSize = 11.sp,
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (failed) Text(tr("No se pudo completar la operación. Comprueba tu conexión y vuelve a intentarlo."), color = Color(0xFFEF4444))

                // Actions buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, enabled = !isProcessing) {
                        Text(tr("Cancelar"), color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsed = amount.toLongOrNull()
                            if (parsed != null && parsed > 0) {
                                isProcessing = true
                                failed = false
                                scope.launch {
                                    runCatching { com.example.data.AdminEssenceAdjustment.apply(userUid, parsed, selectedCurrency,
                                        isAddition, notifyUser, effectiveTitle, if (isCustomMessage) effectiveBody else null) }
                                        .onSuccess { updated -> onBalancesUpdated(updated); onSuccess(); onDismiss() }
                                        .onFailure { failed = true }
                                    isProcessing = false
                                }
                            }
                        },
                        enabled = !isProcessing && amount.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = activeColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Text(com.example.util.tr(if (isAddition) "Añadir Esencia" else "Descontar Esencia"), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
