package com.example.ui.components

import com.example.ui.components.CoachTab as Tab

import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachOutlinedButton as OutlinedButton

import com.example.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.ui.components.coachClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.util.tr
import com.example.util.localizedString

enum class LegalTab(@androidx.annotation.StringRes val titleRes: Int) {
    PRIVACY(R.string.legal_privacy),
    TERMS(R.string.legal_terms),
    THIRD_PARTY(R.string.legal_third_party)
}

@Composable
fun PrivacyPolicyDialog(
    isMandatoryAcceptance: Boolean = false,
    onAccept: () -> Unit = {},
    onChangeLanguage: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(LegalTab.PRIVACY) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = !isMandatoryAcceptance,
            dismissOnBackPress = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .border(1.5.dp, HextechGold.copy(alpha = 0.7f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = HextechSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = when (selectedTab) {
                                LegalTab.PRIVACY -> Icons.Default.Security
                                LegalTab.TERMS -> Icons.Default.Description
                                LegalTab.THIRD_PARTY -> Icons.Default.Handshake
                            },
                            contentDescription = null,
                            tint = HextechGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = localizedString(R.string.legal_informacion_legal),
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Selector
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = HextechDarkBg,
                    contentColor = HextechGold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = HextechGold,
                            height = 2.5.dp
                        )
                    },
                    modifier = Modifier.border(1.dp, HextechCardBorder, RoundedCornerShape(10.dp))
                ) {
                    LegalTab.entries.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    text = localizedString(tab.titleRes),
                                    fontSize = 12.5.sp,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == tab) HextechGold else TextSecondary
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content scrollable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (selectedTab) {
                        LegalTab.PRIVACY -> PrivacyPolicyContent()
                        LegalTab.TERMS -> TermsOfServiceContent()
                        LegalTab.THIRD_PARTY -> ThirdPartyAgreementsContent()
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isMandatoryAcceptance) {
                    Text(
                        text = localizedString(R.string.legal_debes_aceptar_los_terminos_de_servicio_y_la_politica_de_privacida),
                        color = HextechCyan.copy(alpha = 0.9f),
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onChangeLanguage != null) {
                            OutlinedButton(
                                onClick = onChangeLanguage,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                                border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.6f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = HextechCyan)
                            ) {
                                Text(
                                    text = localizedString(R.string.legal_cambiar_idioma),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                            border = BorderStroke(1.dp, Color(0xFFFF4D4D).copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF6666))
                        ) {
                            Text(
                                text = localizedString(R.string.legal_rechazar_y_salir),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }

                        Button(
                            onClick = onAccept,
                            modifier = Modifier.weight(1.1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HextechGold)
                        ) {
                            Text(
                                text = localizedString(R.string.legal_aceptar_y_entrar),
                                color = HextechDarkBg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = localizedString(R.string.legal_cerrar),
                            color = HextechDarkBg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyPolicyContent() {
    PolicySection(
        title = localizedString(R.string.legal_1_que_datos_recopilamos_y_por_que),
        body = localizedString(R.string.legal_queremos_ser_100_transparentes_coach_esta_disenada_exclusivamente)
    )

    PolicySection(
        title = localizedString(R.string.legal_2_permisos_del_asistente_flotante_y_captura_en_vivo),
        body = localizedString(R.string.legal_para_brindar_asistencia_en_tiempo_real_durante_la_seleccion_de_ca)
    )

    PolicySection(
        title = localizedString(R.string.legal_3_almacenamiento_local_y_recursos_offline),
        body = localizedString(R.string.legal_tus_listas_de_nivel_personales_tier_lists_historial_de_borradores)
    )

    PolicySection(
        title = localizedString(R.string.legal_4_cero_publicidad_y_rastreo_comercial),
        body = localizedString(R.string.legal_la_aplicacion_no_incluye_anuncios_publicitarios_banners_intrusivo)
    )

    PolicySection(
        title = localizedString(R.string.legal_5_seguridad_y_cifrado_de_conexion),
        body = localizedString(R.string.legal_todas_las_comunicaciones_entre_la_aplicacion_y_los_servicios_de_b)
    )
}

@Composable
private fun TermsOfServiceContent() {
    PolicySection(
        title = localizedString(R.string.legal_1_aceptacion_de_los_terminos),
        body = localizedString(R.string.legal_al_descargar_instalar_o_utilizar_la_aplicacion_coach_aceptas_cump)
    )

    PolicySection(
        title = localizedString(R.string.legal_2_proposito_y_uso_permitido),
        body = localizedString(R.string.legal_esta_aplicacion_es_una_herramienta_de_asistencia_tactica_aprendiz)
    )

    PolicySection(
        title = localizedString(R.string.legal_3_cuentas_y_suscripciones),
        body = localizedString(R.string.legal_el_acceso_a_funciones_avanzadas_como_analisis_con_ia_historial_de)
    )

    PolicySection(
        title = localizedString(R.string.legal_4_disponibilidad_del_servicio_y_metagame),
        body = localizedString(R.string.legal_nos_esforzamos_por_mantener_la_informacion_de_campeones_runas_obj)
    )

    PolicySection(
        title = localizedString(R.string.legal_5_limitacion_de_responsabilidad),
        body = localizedString(R.string.legal_la_aplicacion_se_proporciona_tal_cual_para_propositos_informativo)
    )
}

@Composable
private fun ThirdPartyAgreementsContent() {
    PolicySection(
        title = localizedString(R.string.legal_1_descargo_oficial_de_riot_games),
        body = localizedString(R.string.legal_coach_no_cuenta_con_el_respaldo_de_riot_games_y_no_refleja_las_op)
    )

    PolicySection(
        title = localizedString(R.string.legal_2_politica_de_propiedad_intelectual_legal_jibber_jabber),
        body = localizedString(R.string.legal_esta_aplicacion_cumple_rigurosamente_con_la_politica_de_riot_game)
    )

    PolicySection(
        title = localizedString(R.string.legal_3_infraestructura_segura_en_la_nube),
        body = localizedString(R.string.legal_utilizamos_infraestructura_en_la_nube_con_servidores_seguros_y_ba)
    )

    PolicySection(
        title = localizedString(R.string.legal_4_bibliotecas_de_codigo_abierto_open_source),
        body = localizedString(R.string.legal_esta_aplicacion_utiliza_componentes_de_software_libre_licenciados)
    )
}

@Composable
fun PolicySection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            color = HextechGoldLight,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = body,
            color = TextSecondary,
            fontSize = 12.5.sp,
            lineHeight = 17.5.sp
        )
    }
}

@Composable
private fun LegalCheckbox(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .coachClickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = HextechGold,
                uncheckedColor = TextSecondary,
                checkmarkColor = HextechDarkBg
            )
        )
        Text(
            text = com.example.util.tr(text),
            color = TextPrimary,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}
