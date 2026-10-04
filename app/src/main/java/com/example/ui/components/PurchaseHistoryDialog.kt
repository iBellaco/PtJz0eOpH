package com.example.ui.components

import com.example.util.tr

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.ui.components.coachClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.HextechSurfaceVariant
import com.example.util.SubscriptionManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BlueEssenceTransaction(
    val id: String,
    val title: String,
    val category: String, // "RECARGA", "AVATAR", "TEMA", "SUSCRIPCION", "CREADOR"
    val amountText: String,
    val isPositive: Boolean,
    val timestamp: Long,
    val status: String = "Completado"
)

@Composable
fun PurchaseHistoryDialog(
    isAdmin: Boolean,
    onDismiss: () -> Unit
) {
    val blueEssenceBalance by SubscriptionManager.blueEssence.collectAsStateWithLifecycle()
    val isPremium by SubscriptionManager.isPremium.collectAsStateWithLifecycle()
    val userName by SubscriptionManager.userName.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf("TODOS") }

    // Generar transacciones dinámicas basadas en el estado del usuario y balance
    val transactions = remember(blueEssenceBalance, isPremium, selectedFilter) {
        val list = mutableListOf<BlueEssenceTransaction>()

        if (blueEssenceBalance > 0) {
            list.add(
                BlueEssenceTransaction(
                    id = "EA-INIT-901",
                    title = "Bono de Bienvenida / Saldo Inicial",
                    category = "RECARGA",
                    amountText = "+${blueEssenceBalance} EA",
                    isPositive = true,
                    timestamp = System.currentTimeMillis() - 86400000L * 3
                )
            )
        }

        if (isPremium) {
            list.add(
                BlueEssenceTransaction(
                    id = "EA-SUB-882",
                    title = "Pase Coach Élite Exclusivo",
                    category = "SUSCRIPCION",
                    amountText = "Activo",
                    isPositive = true,
                    timestamp = System.currentTimeMillis() - 86400000L * 7
                )
            )
        }

        // Historial representativo del ecosistema
        list.add(
            BlueEssenceTransaction(
                id = "EA-AVT-542",
                title = "Avatar Exclusivo: Poro Hextech",
                category = "AVATAR",
                amountText = "-500 EA",
                isPositive = false,
                timestamp = System.currentTimeMillis() - 86400000L * 12
            )
        )
        list.add(
            BlueEssenceTransaction(
                id = "EA-RCG-319",
                title = "Recarga de Esencia Azul (Tier 1)",
                category = "RECARGA",
                amountText = "+2,500 EA",
                isPositive = true,
                timestamp = System.currentTimeMillis() - 86400000L * 18
            )
        )

        when (selectedFilter) {
            "RECARGAS" -> list.filter { it.category == "RECARGA" }
            "AVATARES" -> list.filter { it.category == "AVATAR" || it.category == "TEMA" }
            "SUSCRIPCIONES" -> list.filter { it.category == "SUSCRIPCION" }
            else -> list
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(18.dp),
            color = HextechDarkBg,
            border = BorderStroke(1.5.dp, HextechCyan)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_blue_essence),
                            contentDescription = tr("Esencia Azul"),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = tr("Historial de Esencia Azul"),
                                fontWeight = FontWeight.Black,
                                color = HextechCyan,
                                fontSize = 17.sp
                            )
                            Text(
                                text = tr("Movimientos, recargas y canjes exclusivos"),
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                    HextechAnimatedIconButton(
                        onClick = onDismiss,
                        size = 32.dp,
                        backgroundColor = Color.Transparent,
                        borderColor = Color.Transparent,
                        glowColor = HextechCyan
                    ) {
                        Icon(Icons.Default.Close, contentDescription = tr("Cerrar"), tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Balance Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    border = BorderStroke(1.2.dp, HextechCyan.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = tr("SALDO DISPONIBLE"),
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_blue_essence),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = com.example.util.tr("$blueEssenceBalance"),
                                    color = HextechCyan,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = com.example.util.tr("EA"),
                                    color = HextechGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isPremium) HextechGold.copy(alpha = 0.15f) else Color(0xFF1E293B),
                            border = BorderStroke(1.dp, if (isPremium) HextechGold else Color(0xFF334155))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isPremium) HextechGold else Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = com.example.util.tr(if (isPremium) "Exclusivo PRO" else "Cuenta Activa"),
                                    color = if (isPremium) HextechGold else Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Filtros de categoría
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val filters = listOf(
                        "TODOS" to "Todos",
                        "RECARGAS" to "Recargas",
                        "AVATARES" to "Canjes",
                        "SUSCRIPCIONES" to "Pases"
                    )
                    filters.forEach { (key, label) ->
                        val isSelected = selectedFilter == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) HextechCyan else HextechSurfaceVariant)
                                .border(1.dp, if (isSelected) HextechCyan else HextechCardBorder, RoundedCornerShape(8.dp))
                                .coachClickable { selectedFilter = key }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = com.example.util.tr(label),
                                color = if (isSelected) HextechDarkBg else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Lista de transacciones
                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.History, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(tr("No hay movimientos en esta categoría"), color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(transactions, key = { it.id }) { tx ->
                            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(tx.timestamp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = HextechSurface,
                                border = BorderStroke(0.8.dp, HextechCardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (tx.isPositive) Color(0xFF10B981).copy(alpha = 0.15f)
                                                    else DangerRed.copy(alpha = 0.15f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (tx.isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                                contentDescription = null,
                                                tint = if (tx.isPositive) Color(0xFF10B981) else DangerRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = com.example.util.tr(tx.title),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = com.example.util.tr("ID: ${tx.id}"),
                                                    color = Color.Gray,
                                                    fontSize = 10.5.sp
                                                )
                                                Text(
                                                    text = com.example.util.tr(" • $dateStr"),
                                                    color = Color.Gray,
                                                    fontSize = 10.5.sp
                                                )
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = com.example.util.tr(tx.amountText),
                                            fontWeight = FontWeight.Black,
                                            color = if (tx.isPositive) Color(0xFF10B981) else HextechCyan,
                                            fontSize = 13.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = com.example.util.tr(tx.status),
                                                color = Color(0xFF10B981),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
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
