package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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

enum class LegalTab(val titleRes: String) {
    PRIVACY("Privacidad"),
    TERMS("Términos"),
    THIRD_PARTY("Terceros")
}

@Composable
fun PrivacyPolicyDialog(
    isMandatoryAcceptance: Boolean = false,
    onAccept: () -> Unit = {},
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
                            text = tr("Información Legal"),
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isMandatoryAcceptance) tr("Cerrar y Salir") else tr("Cerrar"),
                            tint = TextSecondary
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
                                    text = tr(tab.titleRes),
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
                        text = tr("Debes aceptar los Términos de Servicio y la Política de Privacidad para poder ingresar a la aplicación. Si cierras esta ventana, la aplicación se cerrará."),
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFFF4D4D).copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF6666))
                        ) {
                            Text(
                                text = tr("Rechazar y Salir"),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = onAccept,
                            modifier = Modifier.weight(1.3f),
                            colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = tr("Aceptar y Entrar"),
                                color = HextechDarkBg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
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
                            text = tr("Cerrar"),
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
        title = tr("1. Qué datos recopilamos y por qué"),
        body = tr("Queremos ser 100% transparentes: Coach está diseñada exclusivamente para el análisis táctico de partidas y drafting en League of Legends: Wild Rift (Parche 7.3). Si decides autenticarte en la aplicación, almacenamos únicamente tu correo electrónico y tu nombre de perfil para gestionar tu sesión, tu nivel de suscripción y tus configuraciones sincronizadas en la nube. No vendemos, no comercializamos ni compartimos tu información personal con ninguna entidad externa.")
    )

    PolicySection(
        title = tr("2. Permisos del Asistente Flotante y Captura en Vivo"),
        body = tr("Para brindar asistencia en tiempo real durante la selección de campeones, la app solicita permiso de superposición (Overlay - SYSTEM_ALERT_WINDOW) y proyección de pantalla. Dichas capturas se procesan de forma 100% local y autónoma en la memoria RAM de tu dispositivo mediante algoritmos de visión en tiempo real y OCR. Las imágenes no se envían a servidores externos, no se almacenan permanentemente en el disco y se descartan inmediatamente tras su lectura. Ninguna grabación de video ni audio se almacena ni se transmite.")
    )

    PolicySection(
        title = tr("3. Almacenamiento Local y Recursos Offline"),
        body = tr("Tus listas de nivel personales (Tier Lists), historial de borradores de draft, notas y preferencias se guardan de forma segura en la base de datos local SQLite (Room) y almacenamiento interno de tu teléfono. Puedes restablecer o eliminar completamente estos datos borrando el almacenamiento de la app desde los ajustes del sistema operativo Android.")
    )

    PolicySection(
        title = tr("4. Cero Publicidad y Rastreo Comercial"),
        body = tr("La aplicación no incluye anuncios publicitarios, banners intrusivos ni kits de desarrollo (SDKs) de publicidad o rastreo de terceros. Ofrecemos una experiencia completamente limpia, privada y enfocada en el rendimiento competitivo.")
    )

    PolicySection(
        title = tr("5. Seguridad y Cifrado de Conexión"),
        body = tr("Todas las comunicaciones entre la aplicación y los servicios de base de datos en tiempo real utilizan protocolos seguros con cifrado HTTPS/TLS v1.3 para garantizar la integridad y confidencialidad absoluta de tu cuenta.")
    )
}

@Composable
private fun TermsOfServiceContent() {
    PolicySection(
        title = tr("1. Aceptación de los Términos"),
        body = tr("Al descargar, instalar o utilizar la aplicación Coach, aceptas cumplir estos Términos de Servicio. Si no estás de acuerdo con alguna disposición, te solicitamos abstenerte de utilizar la aplicación.")
    )

    PolicySection(
        title = tr("2. Propósito y Uso Permitido"),
        body = tr("Esta aplicación es una herramienta de asistencia táctica, aprendizaje y análisis estratégico para League of Legends: Wild Rift. No modifica archivos del juego, no interactúa con la memoria del proceso del juego ni vulnera las políticas de juego limpio de Riot Games. Opera exclusivamente mediante captura de pantalla externa, análisis estadístico y recomendaciones tácticas adaptadas al Parche 7.3.")
    )

    PolicySection(
        title = tr("3. Cuentas y Suscripciones"),
        body = tr("El acceso a funciones avanzadas (como análisis con IA, historial de partidas guardadas y herramientas personalizadas) se gestiona mediante tu cuenta de usuario. Eres responsable de mantener la confidencialidad de tus credenciales. Nos reservamos el derecho de suspender accesos en caso de uso abusivo o vulneración de seguridad.")
    )

    PolicySection(
        title = tr("4. Disponibilidad del Servicio y Metagame"),
        body = tr("Nos esforzamos por mantener la información de campeones, runas, objetos y parches actualizada constantemente con cada versión oficial de Wild Rift; sin embargo, no garantizamos disponibilidad ininterrumpida ante mantenimientos o cambios imprevistos en los servidores del juego.")
    )

    PolicySection(
        title = tr("5. Limitación de Responsabilidad"),
        body = tr("La aplicación se proporciona 'tal cual' para propósitos informativos y de entretenimiento. No nos hacemos responsables por pérdidas de partidas clasificatorias, sanciones de cuentas de terceros ni por el mal uso de las herramientas proporcionadas.")
    )
}

@Composable
private fun ThirdPartyAgreementsContent() {
    PolicySection(
        title = tr("1. Descargo Oficial de Riot Games"),
        body = tr("Coach no cuenta con el respaldo de Riot Games y no refleja las opiniones ni los puntos de vista de Riot Games ni de ninguna persona involucrada oficialmente en la producción o administración de las propiedades de Riot Games. Riot Games y todas las propiedades asociadas son marcas comerciales o marcas comerciales registradas de Riot Games, Inc.")
    )

    PolicySection(
        title = tr("2. Política de Propiedad Intelectual 'Legal Jibber Jabber'"),
        body = tr("Esta aplicación cumple rigurosamente con la política de Riot Games 'Legal Jibber Jabber' para proyectos comunitarios sin fines de usurpación de marca. Todos los nombres de campeones, habilidades, objetos, runas y activos visuales de League of Legends: Wild Rift pertenecen en su totalidad a Riot Games, Inc.")
    )

    PolicySection(
        title = tr("3. Infraestructura Segura en la Nube"),
        body = tr("Utilizamos infraestructura en la nube con servidores seguros y bases de datos cifradas para la sincronización de perfiles, autenticación y almacenamiento de estados de suscripción, operando bajo estrictas normas de seguridad y privacidad.")
    )

    PolicySection(
        title = tr("4. Bibliotecas de Código Abierto (Open Source)"),
        body = tr("Esta aplicación utiliza componentes de software libre licenciados bajo Apache 2.0 y MIT, incluyendo Jetpack Compose, Kotlinx Coroutines, AndroidX Room, Coil Image Loader, Material 3 y ML Kit Text Recognition.")
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
            .clickable { onCheckedChange(!checked) }
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
            text = text,
            color = TextPrimary,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}
