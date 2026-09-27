package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.AccountProfileManager
import com.example.util.AuthManager
import com.example.util.SubscriptionHistoryManager
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BlueEssenceStoreDialog(
    profileId: String,
    isAdmin: Boolean = AuthManager.isCurrentUserAdmin(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val profiles by AccountProfileManager.allProfiles.collectAsState()
    val prof = profiles.find { it.id == profileId } ?: profiles.firstOrNull()

    if (!isAdmin) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Servicio temporalmente fuera de servicio", Toast.LENGTH_LONG).show()
            onDismiss()
        }
        return
    }

    if (prof == null) return

    // Tabs: 0 = Tienda EA, 1 = Canjear Economía, 2 = Historial
    var selectedTab by remember { mutableStateOf(0) }
    var selectedCurrency by remember { mutableStateOf("BLUE") } // "BLUE" or "ORANGE"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            color = HextechDarkBg,
            border = BorderStroke(1.dp, HextechGold)
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HextechSurface)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = if (selectedCurrency == "BLUE") R.drawable.ic_blue_essence else R.drawable.ic_orange_essence),
                            contentDescription = if (selectedCurrency == "BLUE") "Esencia Azul" else "Esencia Naranja",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(if (selectedCurrency == "BLUE") "Economía de Esencia Azul (Admin)" else "Economía de Esencia Naranja (Admin)", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Saldo: ${if (selectedCurrency == "BLUE") prof.blueEssence else prof.orangeEssence} ${if (selectedCurrency == "BLUE") "EA" else "EN"}", color = HextechCyan, fontSize = 13.sp)
                        }
                    }
                    HextechAnimatedIconButton(
                        onClick = onDismiss,
                        size = 32.dp,
                        backgroundColor = Color.Transparent,
                        borderColor = Color.Transparent,
                        glowColor = HextechGold
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }
                
                // Currency Switcher
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedCurrency = "BLUE" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (selectedCurrency == "BLUE") HextechCyan else HextechSurface),
                        modifier = Modifier.weight(1f)
                    ) { Text("Esencia Azul") }
                    Button(
                        onClick = { selectedCurrency = "ORANGE" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (selectedCurrency == "ORANGE") HextechGold else HextechSurface),
                        modifier = Modifier.weight(1f)
                    ) { Text("Esencia Naranja") }
                }

                HorizontalDivider(color = HextechGold.copy(alpha = 0.5f))

                // Tabs Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { selectedTab = 0 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 0) HextechGold else HextechSurface,
                            contentColor = if (selectedTab == 0) HextechDarkBg else HextechGold
                        ),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tienda EA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { selectedTab = 1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 1) HextechGold else HextechSurface,
                            contentColor = if (selectedTab == 1) HextechDarkBg else HextechGold
                        ),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Canjear Items", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { selectedTab = 2 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 2) HextechGold else HextechSurface,
                            contentColor = if (selectedTab == 2) HextechDarkBg else HextechGold
                        ),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Historial", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                when (selectedTab) {
                    0 -> {
                        // Tienda EA (Paquetes)
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text("Recargar Esencias Azules (Admin)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            val packs = if (selectedCurrency == "BLUE") listOf(
                                Triple(400, 3.99, "Paquete Básico"),
                                Triple(1000, 8.99, "Paquete Épico"),
                                Triple(2500, 19.99, "Paquete Legendario"),
                                Triple(5300, 39.99, "Cofre de Artesano")
                            ) else listOf(
                                Triple(1, 1.00, "1 Esencia Naranja"),
                                Triple(5, 5.00, "5 Esencias Naranjas"),
                                Triple(10, 10.00, "10 Esencias Naranjas"),
                                Triple(25, 25.00, "25 Esencias Naranjas")
                            )
                            items(packs) { (amount, price, title) ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (selectedCurrency == "BLUE") {
                                                AccountProfileManager.buyBlueEssence(context, profileId, amount, price)
                                            } else {
                                                AccountProfileManager.buyOrangeEssence(context, profileId, amount, price)
                                            }
                                            Toast.makeText(context, "+$amount ${if (selectedCurrency == "BLUE") "EA" else "EN"} añadidos con éxito", Toast.LENGTH_SHORT).show()
                                        },
                                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                                    border = BorderStroke(1.dp, HextechCardBorder),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = painterResource(id = if (selectedCurrency == "BLUE") R.drawable.ic_blue_essence else R.drawable.ic_orange_essence),
                                            contentDescription = null,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(title, color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("+$amount ${if (selectedCurrency == "BLUE") "Esencias Azules" else "Esencias Naranjas"}", color = HextechCyan, fontSize = 12.sp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(HextechGold)
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text("$$price", color = HextechDarkBg, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // Canjear Economía (Suscripciones, Avatares, Temas basados en precios)
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text("Economía de Canje (Basado en Precios de Suscripción)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Usa tus Esencias Azules para adquirir suscripciones, avatares o temas exclusivos.", color = TextSecondary, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            val economyItems = if (selectedCurrency == "BLUE") listOf(
                                Triple("Suscripción Premium (Mensual)", 1500, "Equivalente a $5.00 USD - Acceso completo por 30 días"),
                                Triple("Suscripción Premium (Anual)", 15000, "Equivalente a $55.00 USD - Acceso completo por 1 año"),
                                Triple("Avatar Exclusivo Coleccionista", 300, "Desbloquea un avatar legendario único para tu perfil"),
                                Triple("Tema Hextech Personalizado", 500, "Desbloquea el tema visual exclusivo para la interfaz")
                            ) else listOf(
                                Triple("Suscripción Creador (Mensual)", 5, "Acceso completo por 30 días")
                            )

                            items(economyItems) { (title, cost, desc) ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                                    border = BorderStroke(1.dp, HextechCardBorder),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(title, color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Image(
                                                    painter = painterResource(id = if (selectedCurrency == "BLUE") R.drawable.ic_blue_essence else R.drawable.ic_orange_essence),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("$cost ${if (selectedCurrency == "BLUE") "EA" else "EN"}", color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(desc, color = TextMuted, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        HextechAnimatedButton(
                                            onClick = {
                                                val success = if (selectedCurrency == "BLUE") {
                                                    AccountProfileManager.spendBlueEssence(context, profileId, cost)
                                                } else {
                                                    AccountProfileManager.spendOrangeEssence(context, profileId, cost)
                                                }
                                                if (success) {
                                                    scope.launch {
                                                        // ... lógica de suscripción ...
                                                    }
                                                    Toast.makeText(context, "¡Canje exitoso de '$title'!", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(context, "${if (selectedCurrency == "BLUE") "Esencias Azules" else "Esencias Naranjas"} insuficientes (Necesitas $cost ${if (selectedCurrency == "BLUE") "EA" else "EN"})", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            backgroundColor = HextechGold,
                                            borderColor = HextechCyan,
                                            glowColor = HextechGold,
                                            modifier = Modifier.fillMaxWidth().height(38.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            enableShimmer = true
                                        ) {
                                            Text("Canjear con ${if (selectedCurrency == "BLUE") "Esencias Azules" else "Esencias Naranjas"}", color = HextechDarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        // Historial
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            val historyItems = if (selectedCurrency == "BLUE") prof.purchaseHistory.map { Triple(it.amount, it.price, it.timestamp) } else prof.orangePurchaseHistory.map { Triple(it.amount, it.price, it.timestamp) }
                            if (historyItems.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                        Text("No hay registros de compras de esencia", color = TextMuted)
                                    }
                                }
                            } else {
                                items(historyItems.reversed()) { (amount, price, timestamp) ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = HextechSurfaceVariant),
                                        border = BorderStroke(1.dp, HextechCardBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Image(
                                                    painter = painterResource(id = if (selectedCurrency == "BLUE") R.drawable.ic_blue_essence else R.drawable.ic_orange_essence),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = "+$amount ${if (selectedCurrency == "BLUE") "Esencias Azules" else "Esencias Naranjas"}",
                                                        color = HextechCyan,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
                                                    Text(text = date, color = TextMuted, fontSize = 10.sp)
                                                }
                                            }
                                            Text(
                                                text = "$$price USD",
                                                color = HextechGold,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
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
}
