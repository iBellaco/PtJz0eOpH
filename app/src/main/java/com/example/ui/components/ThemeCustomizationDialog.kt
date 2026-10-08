package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.ui.components.coachClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.util.tr


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeCustomizationBottomSheet(
    isPremium: Boolean = false,
    onOpenPremiumPlans: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentTheme = AppThemeManager.currentTheme
    val isOledMode = AppThemeManager.isOledMode
    val isParticlesEnabled = AppThemeManager.isParticlesEnabled
    var previewTheme by remember { mutableStateOf(currentTheme) }
    var showPremiumRequiredDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentTheme) {
        previewTheme = currentTheme
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = HextechDarkBg,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = HextechGold.copy(alpha = 0.6f)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .verticalScroll(rememberScrollState())
                .testTag("theme_scroll")
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = HextechGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = tr("Personalización de Temas"),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = tr("Elige tu estética visual"),
                            color = TextSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }
                HextechAnimatedIconButton(
                    onClick = onDismiss,
                    size = 32.dp,
                    backgroundColor = Color.Transparent,
                    borderColor = Color.Transparent,
                    glowColor = HextechGold
                ) {
                    Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Cerrar"), tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // OLED Ultra-Black Switch Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (isOledMode) Color(0xFF000000) else HextechSurface),
                border = BorderStroke(1.2.dp, if (isOledMode) HextechGold else HextechCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = CircleShape,
                            color = if (isOledMode) HextechGold.copy(alpha = 0.2f) else HextechSurfaceVariant,
                            border = BorderStroke(1.dp, if (isOledMode) HextechGold else HextechCardBorder),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isOledMode) Icons.Default.DarkMode else Icons.Default.Brightness4,
                                    contentDescription = null,
                                    tint = if (isOledMode) HextechGold else HextechCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = tr("Modo OLED Ultra-Black"),
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = tr("Contraste puro para pantallas AMOLED/OLED"),
                                color = TextSecondary,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                    Switch(
                        checked = isOledMode,
                        onCheckedChange = { enabled ->
                            AppThemeManager.setOledMode(enabled, context)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = HextechGold,
                            checkedTrackColor = HextechGold.copy(alpha = 0.4f),
                            checkedBorderColor = HextechGold
                        ),
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            // Particles Toggle Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurface),
                border = BorderStroke(1.2.dp, if (isParticlesEnabled) HextechGold else HextechCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = CircleShape,
                            color = HextechSurfaceVariant,
                            border = BorderStroke(1.dp, if (isParticlesEnabled) HextechGold else HextechCardBorder),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isParticlesEnabled) HextechGold else HextechCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = tr("Partículas Mágicas"),
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                if (!isPremium) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Brush.horizontalGradient(listOf(HextechGold, Color(0xFFD4AF37))))
                                            .padding(horizontal = 5.dp, vertical = 1.5.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = com.example.util.trNullable("Premium"),
                                                tint = HextechDarkBg,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = com.example.util.tr("PREMIUM"),
                                                color = HextechDarkBg,
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                            Text(
                                text = tr("Efecto de partículas en la barra de navegación"),
                                color = TextSecondary,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                    Switch(
                        checked = isParticlesEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && !isPremium) {
                                showPremiumRequiredDialog = true
                            } else {
                                AppThemeManager.setParticlesEnabled(enabled, context)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = HextechGold,
                            checkedTrackColor = HextechGold.copy(alpha = 0.4f),
                            checkedBorderColor = HextechGold
                        ),
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Friendly Coach Advice Card
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = HextechGold.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(com.example.util.tr("💡"), fontSize = 18.sp)
                    Column {
                        Text(
                            text = tr("Consejo del Coach Soberano"),
                            color = HextechGold,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tr("¡Cada región altera la energía y colores de la interfaz! Selecciona tu región favorita para sincronizar tu estilo."),
                            color = TextSecondary,
                            fontSize = 10.5.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // Guide text
            Text(
                text = tr("💡 Desliza horizontalmente en el explorador de regiones para descubrir más temas. Presiona 'Aplicar' para usar el tema seleccionado."),
                color = TextCyan,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )

            // 1. Tarjeta Fija de Vista Previa Interactiva en Vivo (Siempre visible al scrollear)
            RegionVisualPreviewGridCard(
                inspectedTheme = previewTheme,
                isApplied = currentTheme == previewTheme,
                isPremium = isPremium,
                onShowPremiumAlert = { showPremiumRequiredDialog = true },
                onApply = {
                    AppThemeManager.setTheme(previewTheme, context)
                }
            )

            // Carrusel Horizontal de Regiones Estilo Póster
            Text(
                text = tr("Explorador Visual de Regiones"),
                color = HextechGold,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 2.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(start = 2.dp, end = 2.dp, bottom = 24.dp)
            ) {
                items(AppTheme.entries, key = { "carousel_${it.id}" }) { theme ->
                    val isSelected = currentTheme == theme
                    val isInspected = previewTheme == theme
                    Card(
                        modifier = Modifier
                            .width(130.dp)
                            .height(82.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .coachClickable { previewTheme = theme },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = theme.surface),
                        border = BorderStroke(
                            if (isSelected) 2.dp else if (isInspected) 1.5.dp else 0.8.dp,
                            if (isSelected) HextechGold else if (isInspected) theme.primary else theme.cardBorder
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            // Placeholder for region splash
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                theme.primary.copy(alpha = 0.2f),
                                                theme.surface
                                            )
                                        )
                                    )
                            )

                            // Glowing overlay if selected
                            if (isSelected || isInspected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.radialGradient(
                                                listOf(
                                                    theme.primary.copy(alpha = if (isSelected) 0.3f else 0.15f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                    )
                            }

                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = tr(theme.regionTag),
                                    color = theme.primaryLight,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = tr(theme.titleKey),
                                    color = theme.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = HextechGold,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(16.dp)
                                )
                            } else if (!isPremium) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(5.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(HextechDarkBg.copy(alpha = 0.75f))
                                        .border(0.8.dp, HextechGold.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = com.example.util.trNullable("Premium"),
                                            tint = HextechGold,
                                            modifier = Modifier.size(10.dp)
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

    if (showPremiumRequiredDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumRequiredDialog = false },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = HextechGold.copy(alpha = 0.15f),
                    border = BorderStroke(1.5.dp, HextechGold),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = HextechGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = tr("Tema Exclusivo Premium"),
                    color = HextechGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = tr("Para aplicar estos temas de Coach inspirados en las regiones de Runaterra necesitas una suscripción Premium activa. No son temas oficiales ni acreditan aprobación de Riot Games."),
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = tr("¡Desbloquea todos los temas por región, partículas mágicas, avatares legendarios y herramientas tácticas avanzadas!"),
                        color = HextechCyan,
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                HextechAnimatedButton(
                    onClick = {
                        showPremiumRequiredDialog = false
                        onOpenPremiumPlans()
                    },
                    backgroundColor = HextechGold,
                    borderColor = HextechCyan,
                    glowColor = HextechGold,
                    shape = RoundedCornerShape(10.dp),
                    enableShimmer = true
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = HextechDarkBg,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tr("Desbloquear con Premium"),
                        color = HextechDarkBg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            },
            dismissButton = {
                HextechAnimatedTextLink(
                    text = tr("Entendido"),
                    onClick = { showPremiumRequiredDialog = false },
                    color = TextMuted,
                    fontSize = 13.sp
                )
            },
            containerColor = HextechSurface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.border(1.2.dp, HextechGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
        )
    }
}

@Composable
private fun RegionVisualPreviewGridCard(
    inspectedTheme: AppTheme,
    isApplied: Boolean,
    isPremium: Boolean,
    onShowPremiumAlert: () -> Unit = {},
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = inspectedTheme.surface),
        border = BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(
                listOf(
                    inspectedTheme.primary,
                    inspectedTheme.secondary,
                    inspectedTheme.primaryGlow
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            inspectedTheme.background.copy(alpha = 0.85f),
                            inspectedTheme.surface.copy(alpha = 0.95f)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            // Header: Lore Title + Tag + Live Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(inspectedTheme.primary, inspectedTheme.primaryDark)
                                )
                            )
                            .border(1.5.dp, inspectedTheme.secondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = inspectedTheme.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tr(inspectedTheme.titleKey),
                                color = inspectedTheme.textPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                letterSpacing = 0.3.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(inspectedTheme.primary.copy(alpha = 0.2f))
                                    .border(0.8.dp, inspectedTheme.primary.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 1.5.dp)
                            ) {
                                Text(
                                    text = tr(inspectedTheme.regionTag),
                                    color = inspectedTheme.primaryLight,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = tr("Vista previa en tiempo real de paleta y elementos"),
                            color = inspectedTheme.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Lore Snippet
            Text(
                text = tr(inspectedTheme.descKey),
                color = inspectedTheme.textSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Section: Visual Swatch Grid (Primary & Secondary Focus)
            Text(
                text = tr("Paleta de colores del tema:"),
                color = inspectedTheme.textPrimary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4-Column Color Swatch Grid with Hex codes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorSwatchGridItem(
                    modifier = Modifier.weight(1f),
                    color = inspectedTheme.primary,
                    glowColor = inspectedTheme.primaryGlow,
                    title = "Primario",
                    subtitle = "Acciones / Draft",
                    textColor = inspectedTheme.textPrimary,
                    isFeatured = true
                )
                ColorSwatchGridItem(
                    modifier = Modifier.weight(1f),
                    color = inspectedTheme.secondary,
                    glowColor = inspectedTheme.secondaryGlow,
                    title = "Secundario",
                    subtitle = "Acentos / Oro",
                    textColor = inspectedTheme.textPrimary,
                    isFeatured = true
                )
                ColorSwatchGridItem(
                    modifier = Modifier.weight(1f),
                    color = inspectedTheme.primaryGlow,
                    glowColor = inspectedTheme.primaryLight,
                    title = "Resplandor",
                    subtitle = "Aura Rúnica",
                    textColor = inspectedTheme.textPrimary,
                    isFeatured = false
                )
                ColorSwatchGridItem(
                    modifier = Modifier.weight(1f),
                    color = inspectedTheme.surface,
                    glowColor = inspectedTheme.cardBorder,
                    title = "Superficie",
                    subtitle = "Contenedor",
                    textColor = inspectedTheme.textPrimary,
                    isFeatured = false
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live Mini Mockup UI Preview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(inspectedTheme.background)
                    .border(1.dp, inspectedTheme.cardBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Mini mock icon
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(inspectedTheme.primary.copy(alpha = 0.25f))
                                .border(1.dp, inspectedTheme.primary, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = com.example.util.tr("WR"),
                                color = inspectedTheme.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = com.example.util.tr("Coach Live UI"),
                                color = inspectedTheme.textPrimary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(inspectedTheme.secondary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = com.example.util.tr("Sinergia S+ / Nivel Soberano"),
                                    color = inspectedTheme.secondary,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Apply Action Button inside Preview (with Premium alert badge on the left when non-premium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!isPremium) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Brush.horizontalGradient(listOf(HextechGold, Color(0xFFD4AF37))))
                                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                    .testTag("theme_premium_notice_button")
                                    .coachClickable { onShowPremiumAlert() }
                                    .padding(horizontal = 6.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = com.example.util.trNullable("Premium"),
                                        tint = HextechDarkBg,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = com.example.util.tr("PREMIUM"),
                                        color = HextechDarkBg,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        HextechAnimatedButton(
                            onClick = {
                                if (!isPremium && !isApplied) onShowPremiumAlert() else onApply()
                            },
                            backgroundColor = if (isApplied) inspectedTheme.surfaceVariant else inspectedTheme.primary,
                            borderColor = if (isApplied) inspectedTheme.secondary else inspectedTheme.primaryGlow,
                            glowColor = if (isApplied) inspectedTheme.secondary else inspectedTheme.primary,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.heightIn(min = 48.dp).testTag("theme_apply_button"),
                            scaleDown = 0.92f,
                            enableShimmer = !isApplied
                        ) {
                            if (isApplied) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = inspectedTheme.secondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tr("Aplicado"),
                                    color = inspectedTheme.secondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = inspectedTheme.background,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tr("Aplicar"),
                                    color = inspectedTheme.background,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSwatchGridItem(
    modifier: Modifier,
    color: Color,
    glowColor: Color,
    title: String,
    subtitle: String,
    textColor: Color,
    isFeatured: Boolean
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(HextechDarkBg.copy(alpha = 0.6f))
            .border(
                if (isFeatured) 1.2.dp else 0.5.dp,
                if (isFeatured) color.copy(alpha = 0.8f) else HextechCardBorder.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Swatch Pill with Glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(color, glowColor)
                    )
                )
                .border(0.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = tr(title),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = tr(subtitle),
            color = TextMuted,
            fontSize = 8.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PrimarySecondaryColorChip(
    color: Color,
    label: String,
    isAccent: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(HextechDarkBg.copy(alpha = 0.7f))
            .border(1.dp, color.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
                .border(0.8.dp, Color.White.copy(alpha = 0.6f), CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = tr(label),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 9.5.sp
        )
    }
}

