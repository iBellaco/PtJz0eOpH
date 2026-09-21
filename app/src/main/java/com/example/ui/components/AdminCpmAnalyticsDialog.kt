package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AppNotice
import com.example.data.AppNoticeAnalyticsManager
import com.example.data.AppNoticeManager
import com.example.data.NoticeMetrics
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCpmAnalyticsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val notices by AppNoticeManager.notices.collectAsState()
    val metricsMap by AppNoticeAnalyticsManager.metricsMap.collectAsState()
    val baseCpmRate by AppNoticeAnalyticsManager.baseCpmRate.collectAsState()
    val startDateMs by AppNoticeAnalyticsManager.trackingStartDate.collectAsState()
    val isSyncing by AppNoticeAnalyticsManager.isSyncing.collectAsState()
    val lastSyncTime by AppNoticeAnalyticsManager.lastSyncTime.collectAsState()

    LaunchedEffect(Unit) {
        AppNoticeAnalyticsManager.syncFromCloud(context)
        AppNoticeManager.syncFromCloud(context)
    }

    var showEditCpmDialog by remember { mutableStateOf(false) }
    var editingNoticeId by remember { mutableStateOf<String?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showRecommendationInfoDialog by remember { mutableStateOf(false) }
    var noticeToDelete by remember { mutableStateOf<AppNotice?>(null) }
    var cpmInputText by remember { mutableStateOf(String.format(Locale.US, "%.2f", baseCpmRate)) }
    var selectedTagFilter by remember { mutableStateOf("TODAS") }

    // Función unificadora de etiquetas para tratar 'ADS' y 'PUBLICIDAD' como la misma categoría
    fun normalizeNoticeTag(tag: String): String {
        val trimmed = tag.trim()
        if (trimmed.isBlank()) return "PUBLICIDAD"
        val upper = trimmed.uppercase(Locale.ROOT)
        if (upper == "ADS" || upper == "AD" || upper == "PUBLICIDAD" || upper == "PUBLICIDADES" || upper == "SPONSOR" || upper == "PATROCINADOR") {
            return "PUBLICIDAD"
        }
        return upper
    }

    // Presupuestos de anuncios
    var showEditBudgetDialog by remember { mutableStateOf(false) }
    var editingBudgetNoticeId by remember { mutableStateOf<String?>(null) }
    var editingBudgetNoticeTitle by remember { mutableStateOf("") }
    var budgetInputText by remember { mutableStateOf("") }
    val totalCampaignBudget = remember(notices) { notices.sumOf { it.budget } }

    val totalImpressions = remember(metricsMap) { AppNoticeAnalyticsManager.getTotalImpressions() }
    val totalClicks = remember(metricsMap) { AppNoticeAnalyticsManager.getTotalClicks() }
    val totalFullscreen = remember(metricsMap) { AppNoticeAnalyticsManager.getTotalFullscreenViews() }
    val totalRevenue = remember(totalImpressions, baseCpmRate, notices) { AppNoticeAnalyticsManager.getTotalRevenue(baseCpmRate, notices) }
    val overallCtr = remember(totalImpressions, totalClicks) { AppNoticeAnalyticsManager.getOverallCtr() }

    // Recomendación dinámica inteligente recalculada en tiempo real
    val dynamicRec = remember(metricsMap, totalImpressions, totalClicks, totalFullscreen, overallCtr) {
        AppNoticeAnalyticsManager.calculateRecommendedCpm()
    }

    val formattedStartDate = remember(startDateMs) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.format(Date(startDateMs))
    }

    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager }

    fun copyReport() {
        val report = AppNoticeAnalyticsManager.generateSummaryReport(notices)
        val clip = ClipData.newPlainText("Reporte CPM Coach", report)
        clipboardManager?.setPrimaryClip(clip)
        Toast.makeText(context, "Reporte CPM copiado al portapapeles", Toast.LENGTH_SHORT).show()
    }

    // Diálogo de confirmación para eliminar anuncio
    if (noticeToDelete != null) {
        val targetNotice = noticeToDelete!!
        AlertDialog(
            onDismissRequest = { noticeToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = DangerRed, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("¿Eliminar Anuncio?", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "¿Estás seguro de que deseas eliminar permanentemente este anuncio?",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = HextechSurfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "\"${targetNotice.title}\"",
                            color = HextechGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Esta acción borrará el registro de la nube y de todos los dispositivos, retirándolo de la rotación publicitaria de inmediato.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        AppNoticeManager.deleteNotice(context, targetNotice.id)
                        Toast.makeText(context, "Anuncio eliminado exitosamente", Toast.LENGTH_SHORT).show()
                        noticeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { noticeToDelete = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = HextechDarkBg
        )
    }

    if (showRecommendationInfoDialog) {
        AlertDialog(
            onDismissRequest = { showRecommendationInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = HextechGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Algoritmo de CPM Recomendado", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "El precio sugerido se calcula y actualiza dinámicamente según tus métricas reales y benchmarks globales de apps de eSports/gaming:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = HextechSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("• Nivel / Calificación: ${dynamicRec.tierName}", color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("• CPM Recomendado Actual: $${String.format(Locale.US, "%.2f", dynamicRec.recommendedCpm)} USD", color = Color(0xFF00FF66), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("• Rango sugerido de venta: $${String.format(Locale.US, "%.2f", dynamicRec.suggestedPriceRange.first)} - $${String.format(Locale.US, "%.2f", dynamicRec.suggestedPriceRange.second)} USD", color = HextechGold, fontSize = 11.5.sp)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("• Benchmark Mercado Gaming: $${String.format(Locale.US, "%.2f", dynamicRec.marketBenchmarkMin)} - $${String.format(Locale.US, "%.2f", dynamicRec.marketBenchmarkMax)} USD", color = TextMuted, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("💡 Criterio del Sistema:", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(dynamicRec.reasoning, color = TextPrimary, fontSize = 11.5.sp, lineHeight = 15.sp)
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📅 Proyección de Precios Fijos (Sponsor):", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        IconButton(
                            onClick = {
                                val presentationText = """
PRECIOS PUBLICITARIOS - COACH APP
CPM Recomendado (por cada 1,000 vistas): ${'$'}${String.format(Locale.US, "%.2f", dynamicRec.recommendedCpm)} USD

Proyección de Paquetes (Precios Fijos):
- 1 Día: ${'$'}${String.format(Locale.US, "%.0f", dynamicRec.price1Day)} USD
- 3 Días: ${'$'}${String.format(Locale.US, "%.0f", dynamicRec.price3Days)} USD
- 1 Semana: ${'$'}${String.format(Locale.US, "%.0f", dynamicRec.price1Week)} USD
- 1 Mes: ${'$'}${String.format(Locale.US, "%.0f", dynamicRec.price1Month)} USD
- 1 Año: ${'$'}${String.format(Locale.US, "%.0f", dynamicRec.price1Year)} USD

Estos precios están calculados en base a nuestras analíticas activas y engagement de la audiencia.
                                """.trimIndent()
                                val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Precios CPM", presentationText)
                                clipboardManager.setPrimaryClip(clip)
                                Toast.makeText(context, "Presentación copiada al portapapeles", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar precios", tint = HextechCyan, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = HextechSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("1 Día", color = TextSecondary, fontSize = 11.sp)
                                Text("$${String.format(Locale.US, "%.0f", dynamicRec.price1Day)} USD", color = Color(0xFF00FF66), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("3 Días", color = TextSecondary, fontSize = 11.sp)
                                Text("$${String.format(Locale.US, "%.0f", dynamicRec.price3Days)} USD", color = Color(0xFF00FF66), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("1 Semana", color = TextSecondary, fontSize = 11.sp)
                                Text("$${String.format(Locale.US, "%.0f", dynamicRec.price1Week)} USD", color = Color(0xFF00FF66), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("1 Mes", color = TextSecondary, fontSize = 11.sp)
                                Text("$${String.format(Locale.US, "%.0f", dynamicRec.price1Month)} USD", color = Color(0xFF00FF66), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("1 Año", color = TextSecondary, fontSize = 11.sp)
                                Text("$${String.format(Locale.US, "%.0f", dynamicRec.price1Year)} USD", color = Color(0xFF00FF66), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        AppNoticeAnalyticsManager.setBaseCpm(context, dynamicRec.recommendedCpm)
                        showRecommendationInfoDialog = false
                        Toast.makeText(context, "Tarifa fijada al precio recomendado: $${dynamicRec.recommendedCpm} USD", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold)
                ) {
                    Text("Aplicar Recomendado ($${String.format(Locale.US, "%.2f", dynamicRec.recommendedCpm)})", color = HextechDarkBg, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecommendationInfoDialog = false }) {
                    Text("Cerrar", color = TextSecondary)
                }
            },
            containerColor = HextechDarkBg
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text("¿Reiniciar Métricas de Anuncios?", color = HextechGold, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Esta acción restablecerá a 0 las impresiones y clics únicos diarios de todos los avisos para iniciar un nuevo período de campaña o facturación.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        AppNoticeAnalyticsManager.resetMetrics(context)
                        showResetConfirmDialog = false
                        Toast.makeText(context, "Métricas restablecidas a cero", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Reiniciar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = HextechDarkBg
        )
    }

    if (showEditCpmDialog) {
        if (editingNoticeId != null) {
            // Modal de edición de CPM específico para un anuncio individual
            AlertDialog(
                onDismissRequest = { 
                    showEditCpmDialog = false 
                    editingNoticeId = null
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, tint = HextechGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tarifa CPM Individual (USD)", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Configura el costo específico para este anuncio. Si lo dejas vacío o en 0, usará la tarifa global ($${String.format(Locale.US, "%.2f", baseCpmRate)} USD / 1k):",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = cpmInputText,
                            onValueChange = { cpmInputText = it },
                            label = { Text("CPM en USD ($ por 1k vistas)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = HextechGold) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HextechGold,
                                unfocusedBorderColor = HextechCyan.copy(alpha = 0.5f),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(1.00, 2.50, dynamicRec.recommendedCpm, 5.00).distinct().forEach { rate ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            cpmInputText = String.format(Locale.US, "%.2f", rate)
                                        },
                                    color = HextechSurfaceVariant,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = "$$rate",
                                        color = HextechCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val parsed = cpmInputText.replace(',', '.').toDoubleOrNull()
                            AppNoticeAnalyticsManager.setNoticeCpm(context, editingNoticeId!!, parsed)
                            Toast.makeText(context, "CPM individual actualizado", Toast.LENGTH_SHORT).show()
                            showEditCpmDialog = false
                            editingNoticeId = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HextechGold)
                    ) {
                        Text("Guardar", color = HextechDarkBg, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showEditCpmDialog = false 
                        editingNoticeId = null
                    }) {
                        Text("Cancelar", color = TextSecondary)
                    }
                },
                containerColor = HextechDarkBg
            )
        } else {
            // Modal Global Completo: Tarifas Publicitarias, Desglose de Precios (Imágenes, Videos, Publicidad, Tráfico, Clics)
            val imageCpm = baseCpmRate
            val videoCpm = baseCpmRate * 2.5
            val fullscreenCpm = baseCpmRate * 1.5
            val estimatedCpc = if (overallCtr > 0.0) (baseCpmRate / 1000.0) / (overallCtr / 100.0) else 0.15

            AlertDialog(
                onDismissRequest = { showEditCpmDialog = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFF00FF66), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tarifas & Desglose CPM", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Surface(
                            color = Color(0xFF00FF66).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.8.dp, Color(0xFF00FF66).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "Auto-Actualizable",
                                color = Color(0xFF00FF66),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 1. Algoritmo Recomendado y Sincronización Automática
                            item {
                                Surface(
                                    color = HextechGold.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = HextechGold, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("CPM Dinámico Recomendado", color = HextechGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Text(
                                                text = "$${String.format(Locale.US, "%.2f", dynamicRec.recommendedCpm)} USD",
                                                color = Color(0xFF00FF66),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${dynamicRec.tierName} • Rango de mercado: $${String.format(Locale.US, "%.2f", dynamicRec.marketBenchmarkMin)} - $${String.format(Locale.US, "%.2f", dynamicRec.marketBenchmarkMax)} USD",
                                            color = HextechCyan,
                                            fontSize = 9.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = {
                                                cpmInputText = String.format(Locale.US, "%.2f", dynamicRec.recommendedCpm)
                                                AppNoticeAnalyticsManager.setBaseCpm(context, dynamicRec.recommendedCpm)
                                                Toast.makeText(context, "Tarifa sincronizada a $${String.format(Locale.US, "%.2f", dynamicRec.recommendedCpm)} USD", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = HextechGold, contentColor = HextechDarkBg),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            Text("Sincronizar y Aplicar Automático", fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                                        }
                                    }
                                }
                            }

                            // 2. Precios por Formato Multimedia (Imágenes vs Videos vs Fullscreen)
                            item {
                                Surface(
                                    color = HextechSurfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.35f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Precios por Formato Multimedia (CPM):", color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)

                                        // Imágenes
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Imágenes y Banners (1.0x)", color = TextPrimary, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                                                Text("Costo: $${String.format(Locale.US, "%.4f", imageCpm / 1000.0)} USD por vista", color = TextMuted, fontSize = 9.sp)
                                            }
                                            Text("$${String.format(Locale.US, "%.2f", imageCpm)} / 1k", color = Color(0xFF00FF66), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Divider(color = HextechCyan.copy(alpha = 0.15f))

                                        // Videos
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Videos MP4 & Clips (2.5x)", color = TextPrimary, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                                                Text("Costo: $${String.format(Locale.US, "%.4f", videoCpm / 1000.0)} USD por vista", color = TextMuted, fontSize = 9.sp)
                                            }
                                            Text("$${String.format(Locale.US, "%.2f", videoCpm)} / 1k", color = Color(0xFF00FF66), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Divider(color = HextechCyan.copy(alpha = 0.15f))

                                        // Pantalla Completa
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Pantalla Completa / Full (1.5x)", color = TextPrimary, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                                                Text("Costo: $${String.format(Locale.US, "%.4f", fullscreenCpm / 1000.0)} USD por vista", color = TextMuted, fontSize = 9.sp)
                                            }
                                            Text("$${String.format(Locale.US, "%.2f", fullscreenCpm)} / 1k", color = Color(0xFF00FF66), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // 3. Paquetes Publicitarios por Período (1 Día, 3 Días, 1 Semana, 1 Mes, 1 Año)
                            item {
                                Surface(
                                    color = HextechSurfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.35f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Paquetes de Publicidad por Tiempo:", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text("Precios calculados según el volumen de usuarios y engagement:", color = TextMuted, fontSize = 8.5.sp)
                                        Spacer(modifier = Modifier.height(2.dp))

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("1 Día:", color = TextSecondary, fontSize = 10.sp)
                                            Text("$${String.format(Locale.US, "%.0f", dynamicRec.price1Day)} USD", color = Color(0xFF00FF66), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("3 Días:", color = TextSecondary, fontSize = 10.sp)
                                            Text("$${String.format(Locale.US, "%.0f", dynamicRec.price3Days)} USD", color = Color(0xFF00FF66), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("1 Semana:", color = TextSecondary, fontSize = 10.sp)
                                            Text("$${String.format(Locale.US, "%.0f", dynamicRec.price1Week)} USD", color = Color(0xFF00FF66), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("1 Mes:", color = TextSecondary, fontSize = 10.sp)
                                            Text("$${String.format(Locale.US, "%.0f", dynamicRec.price1Month)} USD", color = Color(0xFF00FF66), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("1 Año:", color = TextSecondary, fontSize = 10.sp)
                                            Text("$${String.format(Locale.US, "%.0f", dynamicRec.price1Year)} USD", color = Color(0xFF00FF66), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // 4. Métricas de Tráfico & Costo por Clic (CPC / eCPC)
                            item {
                                Surface(
                                    color = HextechSurfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFCC66FF).copy(alpha = 0.35f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Tráfico y Rendimiento de Clics:", color = Color(0xFFCC66FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Tráfico Total Registrado:", color = TextSecondary, fontSize = 10.sp)
                                            Text("${String.format(Locale.US, "%,d", totalImpressions)} imp. únicas", color = HextechCyan, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Clics Únicos Totales:", color = TextSecondary, fontSize = 10.sp)
                                            Text("$totalClicks clics (${String.format(Locale.US, "%.2f", overallCtr)}% CTR)", color = HextechGold, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Costo Estimado por Clic (eCPC):", color = TextSecondary, fontSize = 10.sp)
                                            Text("$${String.format(Locale.US, "%.3f", estimatedCpc)} USD", color = Color(0xFF00FF66), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Valor Total del Tráfico:", color = TextSecondary, fontSize = 10.sp)
                                            Text("$${String.format(Locale.US, "%.2f", totalRevenue)} USD", color = Color(0xFF00FF66), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // 5. Ajuste Manual de Tarifa Base
                            item {
                                Column {
                                    Text("Ajuste Manual de Tarifa Base:", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = cpmInputText,
                                        onValueChange = { cpmInputText = it },
                                        label = { Text("CPM Base en USD ($ por 1k vistas)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = HextechGold) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = HextechGold,
                                            unfocusedBorderColor = HextechCyan.copy(alpha = 0.5f),
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        listOf(1.00, 2.50, dynamicRec.recommendedCpm, 5.00, 10.00).distinct().forEach { rate ->
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        cpmInputText = String.format(Locale.US, "%.2f", rate)
                                                    },
                                                color = HextechSurfaceVariant,
                                                shape = RoundedCornerShape(6.dp),
                                                border = BorderStroke(1.dp, if (rate == dynamicRec.recommendedCpm) HextechGold else HextechCyan.copy(alpha = 0.3f))
                                            ) {
                                                Text(
                                                    text = "$$rate",
                                                    color = if (rate == dynamicRec.recommendedCpm) Color(0xFF00FF66) else HextechCyan,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val presentationText = """
TARIFAS PUBLICITARIAS - COACH APP
CPM Base: ${'$'}${String.format(Locale.US, "%.2f", baseCpmRate)} USD / 1,000 impresiones

Precios por Formato:
- Imágenes y Banners (1.0x): ${'$'}${String.format(Locale.US, "%.2f", imageCpm)} USD / 1k (${'$'}${String.format(Locale.US, "%.4f", imageCpm / 1000.0)} USD por vista)
- Videos MP4 y Clips (2.5x): ${'$'}${String.format(Locale.US, "%.2f", videoCpm)} USD / 1k (${'$'}${String.format(Locale.US, "%.4f", videoCpm / 1000.0)} USD por vista)
- Pantalla Completa / Fullscreen (1.5x): ${'$'}${String.format(Locale.US, "%.2f", fullscreenCpm)} USD / 1k (${'$'}${String.format(Locale.US, "%.4f", fullscreenCpm / 1000.0)} USD por vista)

Paquetes por Tiempo (Precios Fijos):
- 1 Día: ${'$'}${String.format(Locale.US, "%.0f", dynamicRec.price1Day)} USD
- 3 Días: ${'$'}${String.format(Locale.US, "%.0f", dynamicRec.price3Days)} USD
- 1 Semana: ${'$'}${String.format(Locale.US, "%.0f", dynamicRec.price1Week)} USD
- 1 Mes: ${'$'}${String.format(Locale.US, "%.0f", dynamicRec.price1Month)} USD
- 1 Año: ${'$'}${String.format(Locale.US, "%.0f", dynamicRec.price1Year)} USD

Métricas de Tráfico y Rendimiento:
- Impresiones Únicas Registradas: ${String.format(Locale.US, "%,d", totalImpressions)}
- Clics Únicos Totales: ${totalClicks} (CTR promedio: ${String.format(Locale.US, "%.2f", overallCtr)}%)
- Costo Estimado por Clic (eCPC): ${'$'}${String.format(Locale.US, "%.3f", estimatedCpc)} USD
- Valor Monetario del Tráfico: ${'$'}${String.format(Locale.US, "%.2f", totalRevenue)} USD
                                """.trimIndent()
                                val clip = ClipData.newPlainText("Tarifario Completo Coach", presentationText)
                                clipboardManager?.setPrimaryClip(clip)
                                Toast.makeText(context, "Tarifario copiado al portapapeles", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = HextechCyan),
                            border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copiar Tarifario", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val parsed = cpmInputText.replace(',', '.').toDoubleOrNull() ?: baseCpmRate
                                AppNoticeAnalyticsManager.setBaseCpm(context, parsed)
                                Toast.makeText(context, "Tarifa CPM actualizada: $$parsed USD", Toast.LENGTH_SHORT).show()
                                showEditCpmDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HextechGold, contentColor = HextechDarkBg)
                        ) {
                            Text("Guardar", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditCpmDialog = false }) {
                        Text("Cerrar", color = TextSecondary)
                    }
                },
                containerColor = HextechDarkBg
            )
        }
    }

    if (showEditBudgetDialog && editingBudgetNoticeId != null) {
        AlertDialog(
            onDismissRequest = {
                showEditBudgetDialog = false
                editingBudgetNoticeId = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF00FF66), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Presupuesto de Campaña", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Anuncio: \"$editingBudgetNoticeTitle\"",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = budgetInputText,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
                                budgetInputText = input
                            }
                        },
                        label = { Text("Presupuesto en USD ($)") },
                        placeholder = { Text("0.00") },
                        leadingIcon = {
                            Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color(0xFF00FF66))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Permite calcular el saldo restante, porcentaje de consumo y monitorear el gasto de este anuncio publicitario en tiempo real.",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = budgetInputText.toDoubleOrNull() ?: 0.0
                        AppNoticeManager.updateNoticeBudget(context, editingBudgetNoticeId!!, parsed)
                        Toast.makeText(context, "Presupuesto de anuncio guardado: $${String.format(Locale.US, "%.2f", parsed)} USD", Toast.LENGTH_SHORT).show()
                        showEditBudgetDialog = false
                        editingBudgetNoticeId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66), contentColor = HextechDarkBg)
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEditBudgetDialog = false
                    editingBudgetNoticeId = null
                }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = HextechDarkBg
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        BackHandler(enabled = true) {
            onDismiss()
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = HextechDarkBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Header en barra fija superior
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = HextechSurface,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, Color(0xFF00FF66).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFF00FF66), Color(0xFF009933)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = HextechDarkBg,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Métricas de Monetización & CPM",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00FF66),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color(0xFF00FF66), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (isSyncing) "Sincronizando con la nube..." else "Sincronizado en tiempo real • Multidispositivo",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSyncing) HextechGold else TextSecondary,
                                        fontSize = 10.5.sp
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = {
                                    AppNoticeAnalyticsManager.syncFromCloud(context) { success ->
                                        if (success) {
                                            Toast.makeText(context, "Métricas sincronizadas en tiempo real", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    AppNoticeManager.syncFromCloud(context)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Actualizar métricas",
                                    tint = if (isSyncing) HextechGold else HextechCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextPrimary)
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {

                Spacer(modifier = Modifier.height(14.dp))

                // Dynamic Recommended CPM Intelligence Banner
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showRecommendationInfoDialog = true },
                    color = HextechSurfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(HextechGold, Color(0xFF00FF66))))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(HextechGold.copy(alpha = 0.15f))
                                    .border(1.dp, HextechGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = HextechGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "CPM Recomendado: $${String.format(Locale.US, "%.2f", dynamicRec.recommendedCpm)} USD",
                                        color = Color(0xFF00FF66),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFF00FF66).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Auto-Actualizado",
                                            color = Color(0xFF00FF66),
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${dynamicRec.tierName} • Rango: $${String.format(Locale.US, "%.2f", dynamicRec.suggestedPriceRange.first)} - $${String.format(Locale.US, "%.2f", dynamicRec.suggestedPriceRange.second)} USD (Toca para ver criterio)",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = HextechGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // KPI Overview Banner Cards
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.2.dp, HextechCyan.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = HextechGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Rendimiento Publicitario Global", color = HextechGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Surface(
                                modifier = Modifier.clickable {
                                    cpmInputText = String.format(Locale.US, "%.2f", baseCpmRate)
                                    showEditCpmDialog = true
                                },
                                color = HextechGold.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("CPM Actual: $${String.format(Locale.US, "%.2f", baseCpmRate)}/1k ✎", color = HextechGold, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 4 KPI Mini-Cards con Presupuesto y Consumo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Presupuesto Total
                            KpiCard(
                                modifier = Modifier.weight(1f),
                                label = "Presupuesto Total",
                                value = "$${String.format(Locale.US, "%.2f", totalCampaignBudget)}",
                                subtext = if (totalCampaignBudget > 0) "${String.format(Locale.US, "%.1f", (totalRevenue / totalCampaignBudget) * 100.0)}% consumido" else "Sin asignar",
                                accentColor = Color(0xFF00FF66),
                                icon = Icons.Default.AccountBalanceWallet
                            )

                            // Consumo / Ingresos Estimados
                            KpiCard(
                                modifier = Modifier.weight(1f),
                                label = "Consumo / Gasto",
                                value = "$${String.format(Locale.US, "%.2f", totalRevenue)}",
                                subtext = "USD con CPM actual",
                                accentColor = HextechGold,
                                icon = Icons.Default.AttachMoney
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Impresiones Únicas
                            KpiCard(
                                modifier = Modifier.weight(1f),
                                label = "Impresiones Únicas",
                                value = String.format(Locale.US, "%,d", totalImpressions),
                                subtext = "1 x disp / día",
                                accentColor = HextechCyan,
                                icon = Icons.Default.Visibility
                            )

                            // Clics Únicos y CTR
                            KpiCard(
                                modifier = Modifier.weight(1f),
                                label = "Clics Únicos",
                                value = "${totalClicks} únicos",
                                subtext = "${String.format(Locale.US, "%.2f", overallCtr)}% CTR",
                                accentColor = Color(0xFFCC66FF),
                                icon = Icons.Default.TouchApp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Toolbar (Copiar reporte / Reset / Info)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { copyReport() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = HextechCyan.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copiar Reporte", color = HextechCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showResetConfirmDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935).copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFFFF6666), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reiniciar", color = Color(0xFFFF6666), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // TAG NAVIGATION BAR (Scrollable Navigation Chips / Tabs)
                val allTags = remember(notices) {
                    val rawTags = notices.map { normalizeNoticeTag(it.tag) }.distinct().sorted()
                    listOf("TODAS") + rawTags
                }

                ScrollableTabRow(
                    selectedTabIndex = allTags.indexOf(selectedTagFilter).coerceAtLeast(0),
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.Transparent,
                    contentColor = HextechGold,
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    allTags.forEach { tagItem ->
                        val isSelected = selectedTagFilter == tagItem
                        val countInTag = if (tagItem == "TODAS") notices.size else notices.count { normalizeNoticeTag(it.tag) == tagItem }
                        
                        Tab(
                            selected = isSelected,
                            onClick = { selectedTagFilter = tagItem },
                            text = {
                                Surface(
                                    color = if (isSelected) HextechGold.copy(alpha = 0.2f) else HextechSurfaceVariant,
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) HextechGold else HextechCyan.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (tagItem != "TODAS") {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) HextechGold else HextechCyan)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                        }
                                        Text(
                                            text = if (tagItem == "TODAS") "TODAS ($countInTag)" else "$tagItem ($countInTag)",
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) HextechGold else TextSecondary
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // List of notices grouped by Tag or filtered by tag navigation
                val filteredNotices = remember(notices, selectedTagFilter) {
                    if (selectedTagFilter == "TODAS") notices
                    else notices.filter { normalizeNoticeTag(it.tag) == selectedTagFilter }
                }

                if (filteredNotices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay anuncios para la etiqueta seleccionada", color = TextSecondary, fontSize = 12.sp)
                    }
                } else {
                    val groupedNotices = remember(filteredNotices) {
                        filteredNotices.groupBy { normalizeNoticeTag(it.tag) }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        groupedNotices.forEach { (tag, noticesInTag) ->
                            item(key = "header_$tag") {
                                val tagTotalImps = noticesInTag.sumOf { (metricsMap[it.id]?.impressions ?: 0L) }
                                val tagTotalClicks = noticesInTag.sumOf { (metricsMap[it.id]?.clicks ?: 0L) }
                                val tagTotalFullscreen = noticesInTag.sumOf { (metricsMap[it.id]?.fullscreenViews ?: 0L) }
                                val tagTotalBudget = noticesInTag.sumOf { it.budget }
                                val tagRevenue = noticesInTag.sumOf { 
                                    val mult = if (it.videoUrl.isNotBlank()) {
                                        if (it.videoUrl.contains("video") || it.videoUrl.endsWith(".mp4") || it.videoUrl.contains("youtube")) 2.5 else 1.5
                                    } else 1.0
                                    (metricsMap[it.id]?.calculateRevenue(baseCpmRate, mult) ?: 0.0) 
                                }
                                val tagCtr = if (tagTotalImps > 0) (tagTotalClicks.toDouble() / tagTotalImps.toDouble()) * 100.0 else 0.0

                                val tagColor = when {
                                    tag.contains("importante", ignoreCase = true) -> HextechGold
                                    tag.contains("publicidad", ignoreCase = true) -> Color(0xFF00FF66)
                                    tag.contains("oferta", ignoreCase = true) -> HextechCyan
                                    tag.contains("mantenimiento", ignoreCase = true) -> Color(0xFFFF3333)
                                    tag.contains("noticia", ignoreCase = true) -> Color(0xFFCC66FF)
                                    tag.contains("streamer", ignoreCase = true) -> Color(0xFFFF66CC)
                                    else -> HextechCyan
                                }

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = tagColor.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, tagColor.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(tagColor)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = tag.uppercase(),
                                                    color = tagColor,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "(${noticesInTag.size})",
                                                    color = TextMuted,
                                                    fontSize = 10.sp
                                                )
                                            }

                                            Text(
                                                text = if (tagTotalBudget > 0) "Presup: $${String.format(Locale.US, "%.0f", tagTotalBudget)} • Gasto: $${String.format(Locale.US, "%.0f", tagRevenue)} USD" else "Gasto: $${String.format(Locale.US, "%.0f", tagRevenue)} USD",
                                                color = Color(0xFF00FF66),
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(3.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "👁️ ${String.format(Locale.US, "%,d", tagTotalImps)} imp.",
                                                color = HextechCyan,
                                                fontSize = 9.sp
                                            )
                                            Text(
                                                text = "🖱️ $tagTotalClicks clics (${String.format(Locale.US, "%.1f", tagCtr)}%)",
                                                color = HextechGold,
                                                fontSize = 9.sp
                                            )
                                            Text(
                                                text = "📱 $tagTotalFullscreen full",
                                                color = Color(0xFFCC66FF),
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }

                            items(noticesInTag, key = { it.id }) { notice ->
                                val metrics = metricsMap[notice.id] ?: NoticeMetrics(notice.id)
                                NoticeAnalyticsItemCard(
                                    notice = notice,
                                    metrics = metrics,
                                    baseCpm = baseCpmRate,
                                    onEditCustomCpm = {
                                        editingNoticeId = notice.id
                                        cpmInputText = if (metrics.customCpmRate != null) String.format(Locale.US, "%.2f", metrics.customCpmRate) else ""
                                        showEditCpmDialog = true
                                    },
                                    onEditBudget = {
                                        editingBudgetNoticeId = notice.id
                                        editingBudgetNoticeTitle = notice.title
                                        budgetInputText = if (notice.budget > 0) String.format(Locale.US, "%.2f", notice.budget) else ""
                                        showEditBudgetDialog = true
                                    },
                                    onDelete = {
                                        noticeToDelete = notice
                                    }
                                )
                            }
                        }
                    }
                }
                }

                // Barra inferior fija de acciones
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = HextechSurface,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, Color(0xFF00FF66).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { copyReport() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = HextechCyan),
                            border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copiar Reporte", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66), contentColor = HextechDarkBg),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cerrar Panel", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    subtext: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        modifier = modifier,
        color = HextechSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = accentColor, fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
            Text(subtext, color = TextMuted, fontSize = 9.5.sp)
        }
    }
}

@Composable
private fun NoticeAnalyticsItemCard(
    notice: AppNotice,
    metrics: NoticeMetrics,
    baseCpm: Double,
    onEditCustomCpm: () -> Unit,
    onEditBudget: () -> Unit,
    onDelete: () -> Unit
) {
    val mediaMultiplier = if (notice.videoUrl.isNotBlank()) {
        if (notice.videoUrl.contains("video") || notice.videoUrl.endsWith(".mp4") || notice.videoUrl.contains("youtube")) 2.5 else 1.5
    } else 1.0
    val revenue = metrics.calculateRevenue(baseCpm, mediaMultiplier)

    fun getTagColor(tag: String): Color {
        val l = tag.lowercase(Locale.ROOT)
        return when {
            l.contains("importante") -> HextechGold
            l.contains("publicidad") || l.contains("ads") -> Color(0xFF00FF66)
            l.contains("oferta") -> HextechCyan
            l.contains("mantenimiento") -> Color(0xFFFF3333)
            l.contains("noticia") -> Color(0xFFCC66FF)
            l.contains("streamer") -> Color(0xFFFF66CC)
            else -> HextechCyan
        }
    }

    val tagColor = getTagColor(notice.tag)
    val now = System.currentTimeMillis()
    val isExpired = notice.expiresAtMillis > 0L && now >= notice.expiresAtMillis
    val isDepleted = notice.budget > 0.0 && revenue >= notice.budget

    val (statusText, statusColor, statusBg) = when {
        isExpired -> Triple("Expirado", Color(0xFFFF5555), Color(0xFFFF3333).copy(alpha = 0.15f))
        isDepleted -> Triple("Agotado", Color(0xFFFFB300), Color(0xFFFF9900).copy(alpha = 0.15f))
        !notice.isEnabled -> Triple("Inactivo", TextMuted, Color.Gray.copy(alpha = 0.15f))
        else -> Triple("Activo", Color(0xFF00FF66), Color(0xFF00FF66).copy(alpha = 0.15f))
    }

    val expDateStr = remember(notice.expiresAtMillis) {
        if (notice.expiresAtMillis > 0L) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            sdf.format(Date(notice.expiresAtMillis))
        } else ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HextechSurface.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (notice.isEnabled && !isExpired) tagColor.copy(alpha = 0.4f) else Color.Gray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val displayTag = if (notice.tag.trim().uppercase(Locale.ROOT) in listOf("ADS", "AD", "PUBLICIDAD", "PUBLICIDADES", "SPONSOR")) "PUBLICIDAD" else notice.tag.trim().ifBlank { "GENERAL" }
                    Surface(
                        color = tagColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, tagColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = displayTag.uppercase(),
                            color = tagColor,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = notice.title,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.8.dp, statusColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar Anuncio",
                            tint = Color(0xFFFF5555),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (isExpired) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Venció el $expDateStr",
                    color = Color(0xFFFF6666),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            } else if (notice.expiresAtMillis > 0L) {
                Spacer(modifier = Modifier.height(3.dp))
                val diff = notice.expiresAtMillis - now
                val days = diff / (1000 * 60 * 60 * 24)
                val hours = (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
                val timeRemainingStr = if (days > 0) "${days}d ${hours}h" else "${hours}h"
                Text(
                    text = "Expira en $timeRemainingStr ($expDateStr)",
                    color = HextechCyan,
                    fontSize = 9.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Presupuesto de Campaña y Consumo
            Surface(
                color = HextechSurfaceVariant.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.8.dp, if (notice.budget > 0) Color(0xFF00FF66).copy(alpha = 0.35f) else Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = if (notice.budget > 0) Color(0xFF00FF66) else HextechGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Presupuesto:",
                                color = TextSecondary,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (notice.budget > 0) "$${String.format(Locale.US, "%.2f", notice.budget)} USD" else "Sin asignar",
                                color = if (notice.budget > 0) Color(0xFF00FF66) else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onEditBudget() },
                            color = HextechGold.copy(alpha = 0.15f),
                            border = BorderStroke(0.8.dp, HextechGold.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar presupuesto", tint = HextechGold, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Presupuesto", color = HextechGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val spentRatio = if (notice.budget > 0) (revenue / notice.budget) else 0.0
                    val spentPercent = spentRatio * 100.0
                    val remaining = notice.budget - revenue

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gasto: $${String.format(Locale.US, "%.2f", revenue)} USD",
                            color = HextechGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (notice.budget > 0) {
                            Text(
                                text = if (remaining >= 0) "Saldo: $${String.format(Locale.US, "%.2f", remaining)} USD" else "Excedido por $${String.format(Locale.US, "%.2f", -remaining)} USD",
                                color = if (remaining >= 0) HextechCyan else DangerRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (notice.budget > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val progressFloat = spentRatio.toFloat().coerceIn(0f, 1f)
                        val barColor = when {
                            spentPercent >= 100.0 -> DangerRed
                            spentPercent >= 80.0 -> HextechGold
                            else -> Color(0xFF00FF66)
                        }
                        LinearProgressIndicator(
                            progress = progressFloat,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = barColor,
                            trackColor = Color.White.copy(alpha = 0.08f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${String.format(Locale.US, "%.1f", spentPercent)}% consumido",
                                color = barColor,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (spentPercent >= 100.0) {
                                Text("Presupuesto agotado", color = DangerRed, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Metrics Grid for this notice
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Impresiones
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Imp. Únicas", color = TextMuted, fontSize = 9.sp)
                    Text(
                        String.format(Locale.US, "%,d", metrics.impressions),
                        color = HextechCyan,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Clics / CTR
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Únicos / Totales", color = TextMuted, fontSize = 9.sp)
                    Text(
                        "${metrics.clicks} / ${metrics.totalRawClicks}",
                        color = HextechGold,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Pantalla Completa
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Fullscreen", color = TextMuted, fontSize = 9.sp)
                    Text(
                        "${metrics.fullscreenViews}",
                        color = Color(0xFFCC66FF),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Ingresos Generados
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val label = if (metrics.customCpmRate != null) "Tarifa (CPM ★)" else "Tarifa CPM"
                        val multLabel = if (mediaMultiplier > 1.0) " [x${mediaMultiplier}]" else ""
                        Text(label + multLabel, color = TextMuted, fontSize = 9.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar CPM",
                            tint = HextechGold,
                            modifier = Modifier
                                .size(12.dp)
                                .clickable { onEditCustomCpm() }
                        )
                    }
                    Text(
                        "$${String.format(Locale.US, "%.2f", metrics.customCpmRate ?: baseCpm)}",
                        color = Color(0xFF00FF66),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
