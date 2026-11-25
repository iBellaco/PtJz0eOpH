package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
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
    var customPrimary by remember { mutableStateOf(AppThemeManager.customThemeColors?.primary) }
    var customSecondary by remember { mutableStateOf(AppThemeManager.customThemeColors?.secondary) }
    var customGlow by remember { mutableStateOf(AppThemeManager.customThemeColors?.glow) }
    var customAlternate by remember { mutableStateOf(AppThemeManager.customThemeColors?.alternate) }
    var activeCustomPalette by remember { mutableStateOf<CustomPaletteSection?>(null) }
    val themeScrollState = rememberScrollState()

    val customPreviewColors = CustomThemeColors(
        primary = customPrimary ?: AppTheme.CUSTOM.primary,
        secondary = customSecondary ?: AppTheme.CUSTOM.secondary,
        glow = customGlow ?: AppTheme.CUSTOM.primaryGlow,
        alternate = customAlternate ?: AppTheme.CUSTOM.surfaceVariant
    )

    val hasCompleteCustomPalette = customPrimary != null && customSecondary != null &&
        customGlow != null && customAlternate != null
    val customPaletteMatchesApplied = hasCompleteCustomPalette &&
        AppThemeManager.customThemeColors == CustomThemeColors(
            primary = customPrimary!!,
            secondary = customSecondary!!,
            glow = customGlow!!,
            alternate = customAlternate!!
        )
    val previewIsApplied = currentTheme == previewTheme &&
        (previewTheme != AppTheme.CUSTOM || customPaletteMatchesApplied)

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
                canApply = previewTheme != AppTheme.CUSTOM || hasCompleteCustomPalette,
                customColors = if (previewTheme == AppTheme.CUSTOM) customPreviewColors else null,
                onPrimaryColorClick = if (previewTheme == AppTheme.CUSTOM) {
                    { activeCustomPalette = CustomPaletteSection.PRIMARY }
                } else null,
                onSecondaryColorClick = if (previewTheme == AppTheme.CUSTOM) {
                    { activeCustomPalette = CustomPaletteSection.SECONDARY }
                } else null,
                onGlowColorClick = if (previewTheme == AppTheme.CUSTOM) {
                    { activeCustomPalette = CustomPaletteSection.GLOW }
                } else null,
                onAlternateColorClick = if (previewTheme == AppTheme.CUSTOM) {
                    { activeCustomPalette = CustomPaletteSection.ALTERNATE }
                } else null,
                onShowPremiumAlert = { showPremiumRequiredDialog = true },
                onApply = {
                    if (previewTheme == AppTheme.CUSTOM) {
                        if (hasCompleteCustomPalette) {
                            AppThemeManager.setCustomThemeColors(
                                primary = customPrimary!!,
                                secondary = customSecondary!!,
                                glow = customGlow!!,
                                alternate = customAlternate!!,
                                context = context
                            )
                            AppThemeManager.setTheme(AppTheme.CUSTOM, context)
                        }
                    } else {
                        AppThemeManager.setTheme(previewTheme, context)
                    }
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
                            )

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

            if (previewTheme == AppTheme.CUSTOM) {
                CustomThemeEditor(
                    primary = customPrimary,
                    secondary = customSecondary,
                    glow = customGlow,
                    alternate = customAlternate,
                    onSectionSelected = { activeCustomPalette = it }
                )
            }
            }

            activeCustomPalette?.let { section ->
                val selected = when (section) {
                    CustomPaletteSection.PRIMARY -> customPrimary
                    CustomPaletteSection.SECONDARY -> customSecondary
                    CustomPaletteSection.GLOW -> customGlow
                    CustomPaletteSection.ALTERNATE -> customAlternate
                }
                CustomColorPickerOverlay(
                    section = section,
                    selected = selected,
                    onSelected = { color ->
                        when (section) {
                            CustomPaletteSection.PRIMARY -> customPrimary = color
                            CustomPaletteSection.SECONDARY -> customSecondary = color
                            CustomPaletteSection.GLOW -> customGlow = color
                            CustomPaletteSection.ALTERNATE -> customAlternate = color
                        }
                    },
                    onDismiss = { activeCustomPalette = null }
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
    customColors: CustomThemeColors? = null,
    onPrimaryColorClick: (() -> Unit)? = null,
    onSecondaryColorClick: (() -> Unit)? = null,
    onGlowColorClick: (() -> Unit)? = null,
    onAlternateColorClick: (() -> Unit)? = null,
    onShowPremiumAlert: () -> Unit = {},
    onApply: () -> Unit
) {
    val previewPrimary = customColors?.primary ?: inspectedTheme.primary
    val previewSecondary = customColors?.secondary ?: inspectedTheme.secondary
    val previewGlow = customColors?.glow ?: inspectedTheme.primaryGlow
    val previewAlternate = customColors?.alternate ?: inspectedTheme.surfaceVariant
    val previewPrimaryLight = customColors?.primary?.copy(alpha = 0.82f) ?: inspectedTheme.primaryLight
    val previewPrimaryDark = customColors?.primary?.copy(alpha = 0.68f) ?: inspectedTheme.primaryDark
    val previewSecondaryGlow = customColors?.glow ?: inspectedTheme.secondaryGlow
    val previewCardBorder = customColors?.alternate ?: inspectedTheme.cardBorder

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = inspectedTheme.surface),
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
                                    listOf(previewPrimary, previewPrimaryDark)
                                )
                            )
                            .border(1.5.dp, previewSecondary, CircleShape),
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
                                    .background(previewPrimary.copy(alpha = 0.2f))
                                    .border(0.8.dp, previewPrimary.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 1.5.dp)
                            ) {
                                Text(
                                    text = tr(inspectedTheme.regionTag),
                                    color = previewPrimaryLight,
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
                    color = previewPrimary,
                    glowColor = previewGlow,
                    title = "Primario",
                    subtitle = "Acciones / Draft",
                    textColor = inspectedTheme.textPrimary,
                    isFeatured = true,
                    onClick = onPrimaryColorClick,
                    testTag = if (onPrimaryColorClick != null) "custom_preview_primary" else null
                )
                ColorSwatchGridItem(
                    modifier = Modifier.weight(1f),
                    color = previewSecondary,
                    glowColor = previewSecondaryGlow,
                    title = "Secundario",
                    subtitle = "Acentos / Oro",
                    textColor = inspectedTheme.textPrimary,
                    isFeatured = true,
                    onClick = onSecondaryColorClick,
                    testTag = if (onSecondaryColorClick != null) "custom_preview_secondary" else null
                )
                ColorSwatchGridItem(
                    modifier = Modifier.weight(1f),
                    color = previewGlow,
                    glowColor = previewPrimaryLight,
                    title = "Resplandor",
                    subtitle = "Aura Rúnica",
                    textColor = inspectedTheme.textPrimary,
                    isFeatured = false,
                    onClick = onGlowColorClick,
                    testTag = if (onGlowColorClick != null) "custom_preview_glow" else null
                )
                ColorSwatchGridItem(
                    modifier = Modifier.weight(1f),
                    color = previewAlternate,
                    glowColor = previewCardBorder,
                    title = "Suplente",
                    subtitle = "Estados / soporte",
                    textColor = inspectedTheme.textPrimary,
                    isFeatured = false,
                    onClick = onAlternateColorClick,
                    testTag = if (onAlternateColorClick != null) "custom_preview_alternate" else null
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live Mini Mockup UI Preview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(inspectedTheme.background)
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
                                .border(1.dp, previewPrimary, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = com.example.util.tr("WR"),
                                color = previewPrimary,
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
                                        .background(previewSecondary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = com.example.util.tr("Sinergia S+ / Nivel Soberano"),
                                    color = previewSecondary,
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
                            backgroundColor = if (isApplied) previewAlternate else previewPrimary,
                            borderColor = if (isApplied) previewSecondary else previewGlow,
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
                                    tint = previewSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tr("Aplicado"),
                                    maxLines = 1,
                                    color = previewSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = if (canApply) inspectedTheme.background else TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tr("Aplicar"),
                                    maxLines = 1,
                                    color = if (canApply) inspectedTheme.background else TextMuted,
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
    val customThemeReady = AppThemeManager.customThemeColors != null

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
                    text = tr("13 estilos Premium independientes de las regiones."),
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
                        if (isApplied) HextechGold else if (isInspected) theme.primary else theme.cardBorder
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(theme.primary.copy(alpha = 0.2f), theme.surface)
                                    )
                                )
                        )
                        if (isApplied || isInspected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                theme.primary.copy(alpha = if (isApplied) 0.3f else 0.15f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                        if (theme != AppTheme.CUSTOM || customThemeReady) {
                            ThemeColorDots(
                                theme = theme,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(7.dp)
                            )
                        } else {
                            Text(
                                text = tr("Sin colores"),
                                color = theme.textSecondary,
                                fontSize = 8.5.sp,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(7.dp)
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
                                fontSize = 8.5.sp,
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
                        if (isApplied) {
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
    }
}

private val customColorPalette = listOf(
    Color(0xFFF44336), Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7),
    Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF00BCD4),
    Color(0xFF009688), Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39),
    Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFFF5722),
    Color(0xFF795548), Color(0xFF607D8B), Color(0xFFFFFFFF), Color(0xFF111827)
)

private enum class CustomPaletteSection(val id: String, val title: String) {
    PRIMARY("primary", "Primario"),
    SECONDARY("secondary", "Secundario"),
    GLOW("glow", "Resplandor"),
    ALTERNATE("alternate", "Suplente")
}

@Composable
private fun CustomThemeEditor(
    primary: Color?,
    secondary: Color?,
    glow: Color?,
    alternate: Color?,
    onSectionSelected: (CustomPaletteSection) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_theme_editor"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = tr("Personaliza tu tema"),
                color = HextechGold,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = tr("Toca una sección para abrir su paleta de colores."),
                color = TextSecondary,
                fontSize = 10.5.sp,
                lineHeight = 14.sp
            )
            CustomPaletteSectionButton(CustomPaletteSection.PRIMARY, primary) {
                onSectionSelected(CustomPaletteSection.PRIMARY)
            }
            CustomPaletteSectionButton(CustomPaletteSection.SECONDARY, secondary) {
                onSectionSelected(CustomPaletteSection.SECONDARY)
            }
            CustomPaletteSectionButton(CustomPaletteSection.GLOW, glow) {
                onSectionSelected(CustomPaletteSection.GLOW)
            }
            CustomPaletteSectionButton(CustomPaletteSection.ALTERNATE, alternate) {
                onSectionSelected(CustomPaletteSection.ALTERNATE)
            }
        }
    }
}

@Composable
private fun CustomPaletteSectionButton(
    section: CustomPaletteSection,
    selected: Color?,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .testTag("custom_section_${section.id}"),
        shape = RoundedCornerShape(10.dp),
        color = HextechDarkBg.copy(alpha = 0.55f),
        border = BorderStroke(
            if (selected == null) 1.dp else 1.5.dp,
            if (selected == null) HextechCardBorder else HextechCyan
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(selected ?: HextechSurfaceVariant)
                    .border(1.dp, if (selected == null) HextechCardBorder else HextechGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (selected != null) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (selected.luminance() > 0.55f) Color.Black else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tr(section.title),
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = tr(if (selected == null) "Seleccionar color" else "Seleccionado"),
                    color = if (selected == null) TextMuted else HextechCyan,
                    fontSize = 9.5.sp,
                    modifier = Modifier.testTag("custom_palette_${section.id}_status")
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = tr("Elegir color"),
                tint = HextechCyan,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CustomColorPickerOverlay(
    section: CustomPaletteSection,
    selected: Color?,
    onSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.68f))
            .clickable { onDismiss() }
            .testTag("custom_color_picker_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clickable { }
                .testTag("custom_color_picker_dialog"),
            shape = RoundedCornerShape(18.dp),
            color = HextechSurface,
            border = BorderStroke(1.2.dp, HextechGold.copy(alpha = 0.65f)),
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${tr("Elegir color")}: ${tr(section.title)}",
                            color = HextechGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.testTag("custom_color_picker_title")
                        )
                        Text(
                            text = tr("La vista previa cambia antes de aplicar."),
                            color = TextSecondary,
                            fontSize = 10.5.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp).testTag("custom_color_picker_close")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = tr("Cerrar"), tint = TextMuted)
                    }
                }

                customColorPalette.chunked(4).forEachIndexed { rowIndex, rowColors ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowColors.forEachIndexed { columnIndex, color ->
                            CustomColorSwatch(
                                color = color,
                                isSelected = selected == color,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("custom_color_${section.id}_${rowIndex * 4 + columnIndex}"),
                                onClick = { onSelected(color) }
                            )
                        }
                        repeat(4 - rowColors.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("custom_color_picker_done")
                ) {
                    Text(tr("Listo"), color = HextechCyan)
                }
            }
        }
    }
}

@Composable
private fun CustomColorSwatch(
    color: Color,
    isSelected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = color,
        border = BorderStroke(
            if (isSelected) 3.dp else 1.dp,
            if (isSelected) HextechGold else Color.White.copy(alpha = 0.42f)
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = if (color.luminance() > 0.55f) Color.Black else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ThemeColorDots(theme: AppTheme, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf(theme.background, theme.primary, theme.secondary).forEach { color ->
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(0.6.dp, Color.White.copy(alpha = 0.35f), CircleShape)
            )
        }
    }
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
private fun ColorSwatchGridItem(
    modifier: Modifier,
    color: Color,
    glowColor: Color,
    title: String,
    subtitle: String,
    textColor: Color,
    isFeatured: Boolean,
    onClick: (() -> Unit)? = null,
    testTag: String? = null
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(textColor.copy(alpha = if (isLightAppTheme) 0.06f else 0.08f))
            .border(
                if (isFeatured) 1.2.dp else 0.5.dp,
                if (isFeatured) color.copy(alpha = 0.8f) else HextechCardBorder.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .padding(6.dp)
            .then(
                onClick?.let { click ->
                    Modifier.clickable(role = Role.Button, onClick = click)
                } ?: Modifier
            )
            .then(testTag?.let { Modifier.testTag(it) } ?: Modifier),
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
            color = textColor.copy(alpha = 0.72f),
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

