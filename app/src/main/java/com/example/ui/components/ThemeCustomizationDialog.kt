package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.ui.components.coachClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
    var previewNavStyle by remember { mutableStateOf(AppThemeManager.currentNavStyle) }
    var previewNavTab by remember { mutableIntStateOf(0) }
    val themeScrollState = rememberScrollState()
    val previewIsApplied = currentTheme == previewTheme

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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(themeScrollState)
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

            // Barra de navegación personalizable: preferencia exclusiva Premium.
            Card(
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurface),
                border = BorderStroke(1.dp, HextechGold.copy(alpha = .45f))
            ) {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Navigation, null, tint = HextechCyan, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(tr("Barra de navegación"), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(tr("Elige su animación y colores según el tema activo"), color = TextSecondary, fontSize = 10.5.sp)
                        }
                        if (!isPremium) Icon(Icons.Default.Lock, "Premium", tint = HextechGold, modifier = Modifier.size(17.dp))
                    }
                    CoachNavStylePicker(previewNavStyle, isPremium,
                        onSelected = { previewNavStyle = it },
                        onPremiumRequired = { showPremiumRequiredDialog = true })
                    if (isPremium) {
                        Text(tr("Vista previa"), color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        CoachNavStyleRenderer(previewNavStyle, CoachNavPreviewItems(), previewNavTab,
                            onSelected = { previewNavTab = it }, modifier = Modifier.testTag("theme_nav_preview"))
                        Button(
                            onClick = { AppThemeManager.setNavStyle(previewNavStyle, context) },
                            enabled = previewNavStyle != AppThemeManager.currentNavStyle,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("theme_nav_apply")
                        ) { Text(tr("Aplicar navegación")) }
                    } else {
                        Text(tr("Personalización de navegación exclusiva Premium"), color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }

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
                text = tr("💡 Explora las regiones y, debajo, los temas Premium de la aplicación. Presiona 'Aplicar' solo cuando la vista previa sea la que quieres usar."),
                color = TextCyan,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )

            // 1. Tarjeta Fija de Vista Previa Interactiva en Vivo (Siempre visible al scrollear)
            RegionVisualPreviewGridCard(
                inspectedTheme = previewTheme,
                isApplied = previewIsApplied,
                isPremium = isPremium,
                onShowPremiumAlert = { showPremiumRequiredDialog = true },
                onApply = { AppThemeManager.setTheme(previewTheme, context) }
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
                contentPadding = PaddingValues(start = 2.dp, end = 2.dp, bottom = 8.dp)
            ) {
                items(AppTheme.entries.filterNot { it.isApplicationTheme }, key = { "carousel_${it.id}" }) { theme ->
                    val isSelected = currentTheme == theme
                    val isInspected = previewTheme == theme
                    Card(
                        modifier = Modifier
                            .width(130.dp)
                            .height(82.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .coachClickable { previewTheme = theme }
                            .testTag("theme_region_${theme.id.lowercase()}"),
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

                            ThemeColorDots(
                                theme = theme,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(7.dp)
                                    .height(10.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .background(theme.surface.copy(alpha = 0.9f))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = tr(theme.regionTag),
                                        color = AppThemeManager.readableSecondary(theme),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = tr(theme.titleKey),
                                        color = AppThemeManager.readablePrimary(theme),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
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
                                PremiumThemeLockBadge(
                                    testTag = null,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(5.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Temas independientes de la región. Se muestran debajo del
            // explorador para que los doce estilos de aplicación no se
            // confundan con una región de Runaterra.
            ApplicationThemeSection(
                previewTheme = previewTheme,
                currentTheme = currentTheme,
                isPremium = isPremium,
                onSelect = { previewTheme = it }
            )

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
    canApply: Boolean = true,
    onShowPremiumAlert: () -> Unit = {},
    onApply: () -> Unit
) {
    val previewPrimary = inspectedTheme.primary
    val previewSecondary = inspectedTheme.secondary
    val previewGlow = inspectedTheme.primaryGlow
    val previewAlternate = inspectedTheme.surfaceVariant
    val previewPrimaryDark = inspectedTheme.primaryDark
    val previewCardBorder = inspectedTheme.cardBorder
    val previewBackground = inspectedTheme.background
    val previewSurface = inspectedTheme.surface
    val previewTextPrimary = AppThemeManager.readablePrimary(inspectedTheme)
    val previewTextSecondary = AppThemeManager.readableSecondary(inspectedTheme)
    val previewPrimaryReadable = AppThemeManager.readableAccent(previewPrimary, inspectedTheme)
    val previewSecondaryReadable = AppThemeManager.readableAccent(previewSecondary, inspectedTheme)
    val previewApplyBackground = if (isApplied) previewAlternate else previewPrimary
    val previewApplyForeground = AppThemeManager.readableOn(previewApplyBackground)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = previewSurface),
        border = BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(
                listOf(
                    previewPrimary,
                    previewSecondary,
                    previewGlow
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
                            previewBackground.copy(alpha = 0.85f),
                            previewSurface.copy(alpha = 0.95f)
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
                                    listOf(previewPrimary, previewPrimaryDark)
                                )
                            )
                            .border(1.5.dp, previewSecondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = previewTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tr(inspectedTheme.titleKey),
                                color = previewTextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                letterSpacing = 0.3.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(previewPrimary.copy(alpha = 0.2f))
                                    .border(0.8.dp, previewPrimary.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 1.5.dp)
                            ) {
                                Text(
                                    text = tr(inspectedTheme.regionTag),
                                    color = AppThemeManager.readableSecondary(inspectedTheme),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (inspectedTheme.isApplicationTheme && !isPremium) {
                                Spacer(modifier = Modifier.width(5.dp))
                                PremiumThemeLockBadge(testTag = "theme_preview_premium_badge")
                            }
                        }
                        Text(
                            text = tr("Vista previa en tiempo real de paleta y elementos"),
                            color = previewTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Lore Snippet
            Text(
                text = tr(inspectedTheme.descKey),
                color = previewTextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Section: Visual palette bar. Keep the presentation identical to
            // the regional cards: four solid tones, never four gradients.
            Text(
                text = tr("Paleta de colores del tema:"),
                color = previewTextPrimary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            FourTonePaletteBar(
                theme = inspectedTheme,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .testTag("theme_preview_palette")
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "Primario" to previewPrimary,
                    "Secundario" to previewSecondary,
                    "Resplandor" to previewGlow,
                    "Suplente" to previewAlternate
                ).forEach { (label, color) ->
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Text(
                            text = tr(label),
                            color = previewTextSecondary,
                            fontSize = 8.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live Mini Mockup UI Preview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(previewBackground)
                    .border(1.dp, previewCardBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Mini mock icon
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(previewPrimary.copy(alpha = 0.25f))
                                .border(1.dp, previewPrimaryReadable, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = com.example.util.tr("WR"),
                                color = previewPrimaryReadable,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = com.example.util.tr("Coach Live UI"),
                                color = previewTextPrimary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(previewSecondaryReadable)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = com.example.util.tr("Sinergia S+ / Nivel Soberano"),
                                    color = previewSecondaryReadable,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Apply Action Button inside Preview (with Premium alert badge on the left when non-premium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                            enabled = canApply,
                            backgroundColor = previewApplyBackground,
                            borderColor = if (isApplied) previewSecondaryReadable else previewGlow,
                            glowColor = if (isApplied) previewSecondary else previewPrimary,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("theme_apply_button"),
                            scaleDown = 0.92f,
                            enableShimmer = !isApplied && canApply
                        ) {
                            if (isApplied) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = previewApplyForeground,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tr("Aplicado"),
                                    maxLines = 1,
                                    color = previewApplyForeground,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = if (canApply) previewApplyForeground else TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tr("Aplicar"),
                                    maxLines = 1,
                                    color = if (canApply) previewApplyForeground else TextMuted,
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
private fun ApplicationThemeSection(
    previewTheme: AppTheme,
    currentTheme: AppTheme,
    isPremium: Boolean,
    onSelect: (AppTheme) -> Unit
) {
    val applicationThemes = AppTheme.entries.filter { it.isApplicationTheme }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tr("Temas de la aplicación"),
                    color = HextechGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = tr("12 estilos Premium independientes de las regiones."),
                    color = TextSecondary,
                    fontSize = 10.5.sp,
                    lineHeight = 14.sp
                )
            }
            if (!isPremium) {
                PremiumThemeLockBadge(testTag = "theme_application_premium_badge")
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("theme_application_carousel"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(start = 2.dp, end = 2.dp, bottom = 8.dp)
        ) {
            items(applicationThemes, key = { "application_carousel_${it.id}" }) { theme ->
                val isApplied = currentTheme == theme
                val isInspected = previewTheme == theme
                Card(
                    modifier = Modifier
                        .width(130.dp)
                        .height(82.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .coachClickable { onSelect(theme) }
                        .testTag("theme_application_${theme.id.lowercase()}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surface),
                    border = BorderStroke(
                        if (isApplied) 2.dp else if (isInspected) 1.5.dp else 0.8.dp,
                        if (isApplied) HextechGold else if (isInspected) AppThemeManager.readableAccent(theme.primary, theme) else theme.cardBorder
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        ThemeColorDots(
                            theme = theme,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(25.dp)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .background(theme.surface.copy(alpha = 0.92f))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = tr(theme.regionTag),
                                    color = AppThemeManager.readableSecondary(theme),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = tr(theme.titleKey),
                                    color = AppThemeManager.readablePrimary(theme),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (isApplied) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AppThemeManager.readableAccent(theme.primary, theme),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(6.dp)
                                    .size(16.dp)
                            )
                        } else if (!isPremium) {
                            PremiumThemeLockBadge(
                                testTag = null,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FourTonePaletteBar(theme: AppTheme, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp)),
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        listOf(theme.primary, theme.secondary, theme.primaryGlow, theme.surfaceVariant).forEach { color ->
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(color)
            )
        }
    }
}

@Composable
private fun ThemeColorDots(theme: AppTheme, modifier: Modifier = Modifier) {
    FourTonePaletteBar(theme, modifier)
}

@Composable
private fun PremiumThemeLockBadge(
    testTag: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .then(modifier)
            .clip(RoundedCornerShape(4.dp))
            .background(HextechDarkBg.copy(alpha = 0.75f))
            .border(0.8.dp, HextechGold.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = tr("Premium"),
            tint = HextechGold,
            modifier = Modifier.size(10.dp)
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

