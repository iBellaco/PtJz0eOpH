package com.example.ui.screens

import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachIconButton as IconButton

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import com.example.ui.components.coachClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.tr

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen(
    onNavigateBack: () -> Unit
) {
    var faqSearchQuery by remember { mutableStateOf("") }
    var selectedFaq by remember { mutableStateOf<Pair<String, String>?>(null) }

    val faqs = remember {
        listOf(
            Pair(
                "¿Cómo funciona el overlay flotante durante la partida?",
                "Básicamente, al activar el asistente te aparece una burbuja en la pantalla que puedes mover donde te sea más cómoda. Cuando entras a selección de campeones, la abres y te tira al toque las recomendaciones según tu línea y el draft enemigo. Lo armé para que no tengas que salirte del juego ni marearte buscando guías."
            ),
            Pair(
                "¿Para qué sirve exactamente cada botón y sección de la app?",
                "Te cuento rápido cómo está armado: el botón de selección de campeones te abre el asistente de draft interactivo para armar tus composiciones y counters en tiempo real. La sección de Tier List te muestra el meta actual ordenado por tiers (S+, S, A) según winrate y desempeño en las rankeds. Las estadísticas recopilan datos actualizados por rol y enfrentamientos directos. El panel de usuario te deja gestionar tu cuenta, verificar tu estado premium y revisar tu historial de partidas guardadas con Room."
            ),
            Pair(
                "¿De dónde salen las estadísticas, los campeones y las builds?",
                "Todo el contenido viene directo del meta competitivo oficial de Wild Rift y servidores de alto elo (Soberano/Grandmaster). Analizamos constantemente las builds de los mejores jugadores del mundo, las runas óptimas, los hechizos de invocador que más se usan por línea y las guías de objetos core o situacionales (como cortacuras o fajines) para que siempre tengas la información más precisa."
            ),
            Pair(
                "¿Por qué cambian las recomendaciones de campeones si elijo 1er Pick?",
                "Buena pregunta. No es lo mismo pickear de los primeros que ir de counter en la última ronda. Si vas de primer pick, la app te sugiere campeones seguros que encajen bien en cualquier composición y no tengan counters fáciles. Si eres el último, te busca el counter perfecto contra el rival que ya sacó la cara."
            ),
            Pair(
                "¿El asistente consume mucha batería o me genera lag jugando?",
                "Cero lag. Está optimizado con aceleración por hardware y un motor súper ligero. Gasta menos del 2% de batería por hora, así que puedes jugar tranquilo tus ranked sin miedo a bajones de FPS ni tirones en las teamfights."
            ),
            Pair(
                "¿Cómo se guardan mis partidas y drafts en el historial?",
                "Si tienes la versión premium, con darle al botón de guardar al terminar la selección, la partida se almacena de una en tu base de datos local con Room. Así puedes revisar luego tus composiciones, ver con qué campeones tienes más winrate y repasar qué falló o funcionó."
            ),
            Pair(
                "¿Cómo se identifican las habilidades en Wild Rift (H1, H2, H3 y H4)?",
                "En Wild Rift las habilidades de los controles táctiles se denominan oficialmente Habilidad 1 (H1), Habilidad 2 (H2), Habilidad 3 (H3) y Definitiva (H4 o Ulti). Diseñamos la aplicación exclusivamente orientada a la experiencia táctil de Wild Rift para que reconozcas tus botones al instante durante las partidas."
            ),
            Pair(
                "¿Cómo funciona la suscripción y qué beneficios incluye?",
                "La suscripción te da acceso total a funciones avanzadas como guardar tus drafts, personalizar avatares y temas visuales exclusivos. Ten en cuenta que actualmente el servicio de suscripción está temporalmente fuera de servicio mientras preparamos pasarelas de pago oficiales, pero podrás disfrutarlo muy pronto."
            ),
            Pair(
                "¿Cómo puedo personalizar los temas visuales de la aplicación?",
                "Puedes cambiar el estilo visual de la app desde tu perfil o ajustes seleccionando entre varios temas de color inspirados en las regiones de Runaterra. Así adaptas la interfaz y los acentos cromáticos al diseño de tu preferencia."
            ),
            Pair(
                "¿Cómo puedo cambiar mi avatar en la aplicación?",
                "Es muy sencillo. Solo entra a tu perfil y toca el botón 'Cambiar Avatar'. Se abrirá el catálogo completo donde puedes elegir tu icono preferido, desde poros divertidos hasta avatares legendarios de campeones."
            ),
            Pair(
                "¿Cómo funciona el sistema de Tier List y las valoraciones S+, S y A?",
                "La Tier List clasifica a los campeones basándose en su tasa de victoria (winrate), tasa de selección y desempeño general en partidas clasificatorias de alto elo. Los campeones en Tier S+ son prioritarios para el meta actual por su versatilidad y poder en manos expertas, mientras que los tiers S y A ofrecen opciones sólidas adaptadas a composiciones específicas."
            ),
            Pair(
                "¿Cómo funcionan los servidores y regiones en la aplicación?",
                "La tier list utiliza únicamente el meta Global, con referencias de BestBuildWR, WildRiftFire y WildRiftCore. La última lista guardada permanece disponible sin conexión."
            ),
            Pair(
                "¿Me pueden banear en Wild Rift por usar esta burbuja flotante?",
                "Coach no puede garantizar ausencia de sanciones ni afirmar aprobación de Riot. El asistente analiza la pantalla autorizada durante la selección; esa forma de funcionamiento no sustituye el cumplimiento de las políticas del juego. Consulta los requisitos oficiales y usa únicamente funciones autorizadas."
            ),
            Pair(
                "¿Puedo cambiar el tamaño de la burbuja flotante?",
                "Por ahora la burbuja tiene un tamaño estándar diseñado para no estorbar, pero estamos trabajando en una actualización para que puedas escalar su tamaño o hacerla semi-transparente."
            ),
            Pair(
                "¿Qué pasa si abro la burbuja en medio de una teamfight por accidente?",
                "Te tapará parte de la pantalla, así que te recomendamos abrirla SOLO durante la fase de selección de campeones (Draft), pantalla de carga o mientras estás muerto. Puedes moverla a una esquina muerta de tu pantalla para evitar toques accidentales."
            ),
            Pair(
                "¿Por qué la burbuja se cierra sola a veces?",
                "Esto suele ocurrir si el sistema operativo (Android) está matando los procesos en segundo plano para ahorrar batería. Asegúrate de darle permisos de 'Mostrar sobre otras apps' y quitarle la restricción de batería al asistente."
            ),
            Pair(
                "¿Tengo que usar el mismo correo de mi cuenta de Riot Games?",
                "No es necesario. Puedes usar cualquier correo para registrarte en la aplicación. No te pedimos vinculación directa con Riot Games, todo tu progreso en la app es independiente."
            ),
            Pair(
                "Olvidé mi contraseña, ¿cómo la recupero?",
                "En la pantalla de inicio de sesión, dale a '¿Olvidaste tu contraseña?'. Te enviaremos un correo con un enlace seguro para que la cambies."
            ),
            Pair(
                "¿Se pueden tener dos cuentas en la misma aplicación?",
                "No simultáneamente. Tendrás que cerrar sesión en tu cuenta actual desde el panel de usuario e iniciar con la otra."
            ),
            Pair(
                "¿Por qué mi nombre de usuario aparece diferente al del juego?",
                "Porque la app no se vincula mágicamente a tu cuenta de Riot. El nombre que ves es el que pusiste al registrarte. Puedes cambiarlo en la pestaña de Configuración/Perfil de nuestra app."
            ),
            Pair(
                "¿Por qué me recomienda campeones que no tengo comprados?",
                "El asistente recomienda estrictamente basándose en la victoria matemática y el meta. Aún no tiene cómo saber tu inventario (champion pool). Te sugerimos mirar la 2da o 3ra opción si no tienes la primera recomendación."
            ),
            Pair(
                "¿Puedo filtrar las recomendaciones solo para mi rol principal?",
                "Sí, cuando abres el asistente de Draft, asegúrate de seleccionar el icono de tu carril (Top, Jungla, Mid, ADC o Support) para que las sugerencias de campeones se adapten únicamente a esa línea."
            ),
            Pair(
                "¿Qué significa 'Win Condition' o Condición de Victoria?",
                "Es el objetivo principal o la manera en que tu composición de equipo debe jugar para asegurar ganar. Por ejemplo: 'Proteger al tirador', 'Pelear en la jungla' o 'Presionar líneas separadas (Split-push)'."
            ),
            Pair(
                "Mi equipo hizo 'Troll Pick', ¿el asistente sabe cómo compensarlo?",
                "¡Sí! Si tu equipo saca 4 ADCs, el algoritmo detectará que falta daño mágico, tanque y control de masas, y te sugerirá fuertemente llenar esos vacíos (ej. un tanque AP como Galio, Gragas o un support de engage)."
            ),
            Pair(
                "¿Cada cuánto tiempo se actualiza la Tier List?",
                "Se actualiza de forma dinámica varias veces a la semana, y hace un reinicio masivo cada vez que sale un parche oficial de balance en Wild Rift."
            ),
            Pair(
                "Mi campeón favorito está en Tier C, ¿significa que no debo usarlo?",
                "Para nada. Si tienes mucha maestría con un campeón Tier C, probablemente jugarás mejor con él que intentando usar un Tier S+ que no dominas. La Tier List mide el potencial general, pero tu habilidad es la que acarrea."
            ),
            Pair(
                "¿Por qué algunos campeones nuevos no aparecen en ninguna Tier List al salir?",
                "Esperamos al menos de 3 a 5 días para recopilar suficientes datos de winrate y banrate en rankeds de elo alto antes de clasificarlos con precisión en un Tier."
            ),
            Pair(
                "¿De dónde sacan las combinaciones de runas que recomiendan?",
                "Las builds combinan referencias del meta Global con el rol y la composición de la partida. Los objetos, botas y runas situacionales se recomiendan según el daño, los controles y las amenazas rivales."
            ),
            Pair(
                "¿El asistente recomienda objetos situacionales (como cortacuras)?",
                "Totalmente. Si el equipo enemigo tiene a Soraka, Dr. Mundo o Aatrox, te saltará una alerta recomendando que armes 'Llamada del Verdugo' o 'Orbe del Olvido' como objeto situacional prioritario."
            ),
            Pair(
                "¿Cómo funciona la calculadora de penetración de armadura/mágica?",
                "Toma en cuenta las resistencias base del campeón enemigo por nivel y los objetos que lleva armados, y te dice exactamente qué porcentaje de tu daño vas a aplicar, sugiriendo si rentan objetos porcentuales o planos."
            ),
            Pair(
                "¿Qué hago si soy Premium pero sigo viendo funciones bloqueadas?",
                "Prueba darle a 'Restaurar Compras' o cerrando sesión y volviendo a entrar. Si el problema persiste, contacta al soporte desde el panel de reportes de bugs."
            ),
            Pair(
                "¿Si cambio de celular pierdo mis drafts guardados?",
                "Si usas la versión gratuita (que usa Room de manera local), sí los pierdes al cambiar de dispositivo. Con la versión Premium, los drafts se respaldan en la nube."
            ),
            Pair(
                "Si soy nivel bajo en Wild Rift (ej. Hierro), ¿me sirve usar la aplicación?",
                "¡Por supuesto! De hecho es el mejor momento para usarla. Te ayudará a aprender los roles, qué compran los campeones, cómo contrarrestar y asimilar mejor los conceptos del juego."
            ),
            Pair(
                "¿La app juega la partida por mí?",
                "Jajaja, claro que no. Somos una app de análisis y estrategia (Coach), no un bot ni un software de trampas (hacks). Las mecánicas y el macro juego en partida dependen al 100% de ti."
            ),
            Pair(
                "¿Me ayuda a subir a Soberano si soy Hierro 4?",
                "Te damos las herramientas teóricas, el conocimiento táctico, las builds y los counters perfectos... pero si sigues fallando el destello (Flash) contra el muro, la app no puede salvarte. ¡A practicar mecánicas!"
            ),
            Pair(
                "¿El asistente se da cuenta si estoy lagueado o si tengo mal ping?",
                "No, no monitorizamos la conexión de tu red al juego. Si estás a 999ms, el asistente no sabrá por qué te mataron bajo torre."
            ),
            Pair(
                "¿Por qué no me sale el muñeco de práctica en las sugerencias?",
                "El pobre muñeco de práctica ya sufre mucho, no hace falta buscarle un counter. La app solo evalúa a los campeones reales."
            ),
            Pair(
                "¿Me recomiendan jugar Teemo Jungla si el asistente lo sugiere?",
                "Si nuestro algoritmo hiper-avanzado (o los dioses del caos) te sugiere Teemo Jungla, ten por seguro que las matemáticas le respaldan... Pero asume las consecuencias si tu equipo te flamea."
            ),
            Pair(
                "Si pierdo mi promoción a Maestro, ¿es culpa de la aplicación?",
                "Oficialmente: no. Siempre puedes culpar al Jungla, es una regla universal de los MOBAs. Nosotros te dimos el draft perfecto, pero no podemos controlar al Yasuo 0/10 de tu equipo."
            ),
            Pair(
                "¿La aplicación tiene modo oscuro o claro?",
                "Actualmente la app tiene un esquema de color inspirado en tecnología 'Hextech' muy oscuro, ideado para cansar la vista lo menos posible. Así que sí, es modo oscuro siempre."
            ),
            Pair(
                "¿Se puede conectar a Discord para que mis amigos vean mis drafts?",
                "En este momento no hay integración nativa para transmitir tu draft directo a Discord desde la app, pero siempre puedes compartir pantalla."
            ),
            Pair(
                "¿Qué pasa si mi celular es de gama baja?",
                "La app es súper ligera. A menos que tu celular esté peleando por abrir la calculadora, podrá correr nuestra app en segundo plano mientras juegas Wild Rift."
            ),
            Pair(
                "¿La burbuja tiene modo invisible?",
                "No invisible total, pero la interfaz se minimiza a un botoncito discreto que no molestará tu línea de visión."
            ),
            Pair(
                "¿Por qué el asistente no habla con voz humana?",
                "Leer consume menos concentración auditiva en partidas intensas donde necesitas escuchar los pings y habilidades. Aunque puede ser una idea para el futuro (Coach por voz)."
            ),
            Pair(
                "¿Si el rival pilla mi counter directo, qué recomienda hacer la app?",
                "La sección de 'Plan de Juego Macro' te sugerirá jugar bajo torre, pedir gankeos (ayuda) al jungla, y priorizar objetos defensivos o de supervivencia."
            ),
            Pair(
                "¿La app sabe si un campeón está roto en el parche actual?",
                "Absolutamente. Los campeones rotos (Tier S+) saltarán como primera recomendación si están disponibles y se adaptan a la composición."
            ),
            Pair(
                "¿Cómo reporto si veo una estadística claramente equivocada?",
                "En el menú principal tienes un botón de 'Reporte de Bugs'. Escríbenos ahí qué campeón o estadística crees que está mal y nuestro equipo lo validará con la base de datos oficial."
            ),
            Pair(
                "¿Si mi Yasuo tiene maestría 7, la app puede evitar que se vaya 0/10?",
                "Lamentablemente, el pico de poder de Yasuo al 0/10 es un evento canónico del universo que ninguna inteligencia artificial puede detener. Solo reza."
            ),
            Pair(
                "¿La aplicación puede rastrear la dirección IP del Teemo enemigo para ir a buscarlo en la vida real?",
                "No apoyamos la violencia fuera del juego, por más que ese Teemo te haya hecho pisar tres hongos seguidos. Respira hondo y compra un Lente del Oráculo."
            ),
            Pair(
                "¿Qué hago si mi gato pisa el celular y me elige a Yuumi con Castigo (Smite)?",
                "Disfruta de la experiencia inmersiva. Yuumi es un gato, tu gato la eligió. Es el destino. Además, robar el Barón con Yuumi es la máxima humillación."
            ),
            Pair(
                "¿Puedo usar la burbuja del asistente como escudo contra la definitiva de Garen?",
                "Nuestra burbuja flotante está hecha de código indestructible, pero lamentablemente solo bloquea tus dudas existenciales, no espadazos gigantes de justicia demaciana de 1000 de daño verdadero."
            ),
            Pair(
                "¿Por qué la app no me dice cómo convencer a mi equipo de hacer el Dragón?",
                "Ni la Inteligencia Artificial más potente del mundo ha logrado descifrar la psicología de un equipo en partidas clasificatorias de Wild Rift que prefiere ir a farmear los lobos en vez de hacer el Dragón Anciano. Es un misterio de la humanidad."
            ),
            Pair(
                "¿La app incluye un botón de 'rendición automática' al minuto 5?",
                "No, ¡nunca te rindas! Bueno, a menos que el Nexo enemigo tenga 10 de vida y tu equipo esté peleando por el escurridizo del río. Ahí te damos permiso moral de llorar."
            ),
            Pair(
                "¿Por qué el asistente no flamea a mi Jungla por mí para ahorrarme tiempo?",
                "Mantenemos un ambiente libre de toxicidad. Si quieres decirle algo a tu jungla, dile que aprecias sus intentos. Sorpréndelo. Rompe la Matrix."
            ),
            Pair(
                "¿Si meto el celular en el microondas cargarán más rápido los recursos offline?",
                "Solo si quieres jugar Wild Rift en calidad de cenizas. Por favor, usa el cargador normal. El modo 'Hextech' de la app no requiere fisión nuclear."
            ),
            Pair(
                "¿Esta app me ayudará a conseguir pareja?",
                "Si te acercas a alguien y le dices 'hey, mi asistente me dijo que nuestras sinergias son Tier S+', puede que funcione. Si no, al menos ganarás tus Rankeds. Es un ganar-ganar."
            )
        )
    }

    val faqLanguage = com.example.util.currentAppLanguage()
    val filteredFaqs = remember(faqSearchQuery, faqLanguage) {
        if (faqSearchQuery.isBlank()) faqs
        else faqs.filter {
            com.example.util.trStr(faqLanguage, it.first).contains(faqSearchQuery, ignoreCase = true) ||
            com.example.util.trStr(faqLanguage, it.second).contains(faqSearchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = HextechDarkBg,
        topBar = {
            TopAppBar(
                title = { Text(tr("Preguntas Frecuentes"), color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr("Volver"), tint = HextechGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HextechSurface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = faqSearchQuery,
                onValueChange = { faqSearchQuery = it },
                placeholder = { Text(tr("Buscar pregunta o duda..."), color = TextMuted, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                textStyle = TextStyle(fontSize = 13.sp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    if (faqSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { faqSearchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Limpiar"), tint = TextMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HextechCyan,
                    unfocusedBorderColor = HextechCardBorder,
                    focusedContainerColor = HextechSurface,
                    unfocusedContainerColor = HextechSurface
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredFaqs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tr("No se encontraron preguntas que coincidan con tu búsqueda."),
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            } else {
                filteredFaqs.forEach { pair ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .coachClickable { selectedFaq = pair },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechSurface),
                        border = BorderStroke(1.dp, HextechCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = HextechGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = tr(pair.first),
                                color = HextechGoldLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

    if (selectedFaq != null) {
        AlertDialog(
            onDismissRequest = { selectedFaq = null },
            containerColor = HextechSurface,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = HextechGold, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr(selectedFaq!!.first),
                        color = HextechGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp
                    )
                }
            },
            text = {
                Text(
                    text = tr(selectedFaq!!.second),
                    color = TextPrimary,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { selectedFaq = null },
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(tr("Entendido"), color = HextechDarkBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        )
    }
}
