package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechGold
import com.example.util.SubscriptionManager
import kotlinx.coroutines.launch

@Composable
fun BuyEssenceDialog(
    isAdmin: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedPackIndex by remember { mutableStateOf(1) }
    var isPurchasing by remember { mutableStateOf(false) }

    val packs = listOf(
        Triple("10 EA", 10L, "$1.00 USD"),
        Triple("20 EA", 20L, "$2.00 USD"),
        Triple("30 EA", 30L, "$3.00 USD"),
        Triple("40 EA", 40L, "$4.00 USD"),
        Triple("50 EA", 50L, "$5.00 USD"),
        Triple("60 EA", 60L, "$6.00 USD"),
        Triple("70 EA", 70L, "$7.00 USD"),
        Triple("80 EA", 80L, "$8.00 USD"),
        Triple("90 EA", 90L, "$9.00 USD"),
        Triple("100 EA", 100L, "$10.00 USD"),
        Triple("250 EA", 250L, "$25.00 USD"),
        Triple("500 EA", 500L, "$50.00 USD"),
        Triple("1,000 EA", 1000L, "$100.00 USD")
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.5.dp, HextechCyan)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.ic_blue_essence),
                            contentDescription = "Esencia Azul",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Comprar Esencia Azul",
                            fontWeight = FontWeight.Bold,
                            color = HextechCyan,
                            fontSize = 17.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
                    }
                }

                Divider(color = Color(0xFF1E293B), modifier = Modifier.padding(vertical = 10.dp))

                // Estado de mantenimiento para no administradores
                if (!isAdmin) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = DangerRed.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "En mantenimiento: Las compras de Esencia Azul están temporalmente deshabilitadas por mantenimiento técnico.",
                                color = Color.White,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = HextechGold.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "👑 Modo Administrador Activo: Acceso de compra y recarga sin restricciones.",
                            color = HextechGold,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Selección de paquetes
                packs.forEachIndexed { index, pack ->
                    val isSelected = selectedPackIndex == index
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedPackIndex = index },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) HextechCyan.copy(alpha = 0.15f) else Color(0xFF1E293B),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) HextechCyan else Color(0xFF334155)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.foundation.Image(
                                    painter = painterResource(id = R.drawable.ic_blue_essence),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = pack.first,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = pack.third,
                                color = HextechCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Botón de Comprar
                Spacer(modifier = Modifier.height(16.dp))

                HextechAnimatedButton(
                    onClick = {
                        if (isAdmin) {
                            isPurchasing = true
                            scope.launch {
                                val pack = packs[selectedPackIndex]
                                SubscriptionManager.addBlueEssence(pack.second)
                                isPurchasing = false
                                Toast.makeText(context, "¡Recarga de ${pack.first} aplicada exitosamente!", Toast.LENGTH_LONG).show()
                                onDismiss()
                            }
                        }
                    },
                    enabled = isAdmin && !isPurchasing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    backgroundColor = if (isAdmin) HextechCyan else Color(0xFF334155),
                    borderColor = if (isAdmin) HextechGold else Color.Transparent,
                    glowColor = if (isAdmin) HextechCyan else Color.Transparent,
                    enableShimmer = isAdmin && !isPurchasing,
                    enablePulse = isAdmin && !isPurchasing
                ) {
                    if (isPurchasing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (isAdmin) Color(0xFF0F172A) else Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAdmin) "Comprar (Sin Restricciones)" else "Comprar (En Mantenimiento)",
                            color = if (isAdmin) Color(0xFF0F172A) else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        }
    }
}
