package com.example.ui.components

import com.example.ui.components.CoachIconButton as IconButton

import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import com.example.ui.components.coachClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
    initialCurrency: String = "BLUE", // "BLUE" or "ORANGE"
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedCurrency by remember { mutableStateOf(initialCurrency) }
    var selectedPackIndex by remember { mutableStateOf(0) }
    var isPurchasing by remember { mutableStateOf(false) }

    val blueEssenceBalance by SubscriptionManager.blueEssence.collectAsState()
    val orangeEssenceBalance by SubscriptionManager.orangeEssence.collectAsState()

    val bluePacks = listOf(
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

    // Esencia Naranja: 1$ por 1 esencia
    val orangePacks = listOf(
        Triple("1 EN", 1L, "$1.00 USD"),
        Triple("5 EN", 5L, "$5.00 USD"),
        Triple("10 EN", 10L, "$10.00 USD"),
        Triple("25 EN", 25L, "$25.00 USD"),
        Triple("50 EN", 50L, "$50.00 USD"),
        Triple("100 EN", 100L, "$100.00 USD")
    )

    val currentPacks = if (selectedCurrency == "BLUE") bluePacks else orangePacks
    val isOrange = selectedCurrency == "ORANGE"
    val accentColor = if (isOrange) HextechGold else HextechCyan
    val currentIconRes = if (isOrange) R.drawable.ic_orange_essence else R.drawable.ic_blue_essence
    val currentCurrencyName = if (isOrange) "Esencia Naranja" else "Esencia Azul"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.5.dp, accentColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = currentIconRes),
                            contentDescription = currentCurrencyName,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                com.example.util.tr("Comprar $currentCurrencyName"),
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                fontSize = 16.sp
                            )
                            Text(
                                text = com.example.util.tr(if (isOrange) "Saldo actual: $orangeEssenceBalance EN" else "Saldo actual: $blueEssenceBalance EA"),
                                color = Color(0xFF94A3B8),
                                fontSize = 11.5.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Cerrar"), tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Selector de Moneda: Esencia Azul vs Esencia Naranja
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E293B))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .coachClickable {
                                if (selectedCurrency != "BLUE") {
                                    selectedCurrency = "BLUE"
                                    selectedPackIndex = 0
                                }
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = if (!isOrange) HextechCyan.copy(alpha = 0.25f) else Color.Transparent,
                        border = if (!isOrange) BorderStroke(1.dp, HextechCyan) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = R.drawable.ic_blue_essence),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                com.example.util.tr("Esencia Azul"),
                                color = if (!isOrange) HextechCyan else Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .coachClickable {
                                if (selectedCurrency != "ORANGE") {
                                    selectedCurrency = "ORANGE"
                                    selectedPackIndex = 0
                                }
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isOrange) HextechGold.copy(alpha = 0.25f) else Color.Transparent,
                        border = if (isOrange) BorderStroke(1.dp, HextechGold) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = R.drawable.ic_orange_essence),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                com.example.util.tr("Esencia Naranja"),
                                color = if (isOrange) HextechGold else Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                                text = com.example.util.tr("En mantenimiento: Las compras de $currentCurrencyName están temporalmente deshabilitadas por mantenimiento técnico."),
                                color = Color.White,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = com.example.util.tr(if (isOrange) "👑 Modo Administrador: Recarga de Esencia Naranja ($1 USD = 1 EN)" else "👑 Modo Administrador: Recarga de Esencia Azul sin restricciones."),
                            color = accentColor,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Selección de paquetes scrollable
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(currentPacks) { index, pack ->
                        val isSelected = selectedPackIndex == index
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .coachClickable { selectedPackIndex = index },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) accentColor.copy(alpha = 0.15f) else Color(0xFF1E293B),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) accentColor else Color(0xFF334155)
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
                                        painter = painterResource(id = currentIconRes),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = com.example.util.tr(pack.first),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Text(
                                    text = com.example.util.tr(pack.third),
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón de Comprar
                HextechAnimatedButton(
                    onClick = {
                        if (isAdmin) {
                            isPurchasing = true
                            scope.launch {
                                val pack = currentPacks[selectedPackIndex.coerceIn(0, currentPacks.lastIndex)]
                                runCatching {
                                    if (isOrange) SubscriptionManager.addOrangeEssence(pack.second)
                                    else SubscriptionManager.addBlueEssence(pack.second)
                                }.onSuccess {
                                    Toast.makeText(context, com.example.util.appTr("¡Recarga de ${pack.first} aplicada exitosamente!"), Toast.LENGTH_LONG).show()
                                    onDismiss()
                                }.onFailure { Toast.makeText(context, it.message, Toast.LENGTH_LONG).show() }
                                isPurchasing = false

                            }
                        }
                    },
                    enabled = isAdmin && !isPurchasing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    backgroundColor = if (isAdmin) accentColor else Color(0xFF334155),
                    borderColor = if (isAdmin) HextechGold else Color.Transparent,
                    glowColor = if (isAdmin) accentColor else Color.Transparent,
                    enableShimmer = isAdmin && !isPurchasing,
                    enablePulse = isAdmin && !isPurchasing
                ) {
                    if (isPurchasing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (isAdmin) Color(0xFF0F172A) else Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = com.example.util.tr(if (isAdmin) "Comprar (Sin Restricciones)" else "Comprar (En Mantenimiento)"),
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

