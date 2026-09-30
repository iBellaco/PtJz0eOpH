package com.example.data

import com.example.model.WildRiftItem

data class SituationalItemInfo(
    val name: String,
    val iconUrl: String,
    val categoryName: String,
    val purpose: String,
    val bestAgainst: List<String>,
    val keyEffect: String,
    val recommendationTip: String
)

object SituationalItemAdvisor {

    private val adviceMap = mapOf(
        "Cota de Espinas" to SituationalItemInfo(
            name = "Cota de Espinas",
            iconUrl = "file:///android_asset/offline_images/ab38f2866c6c041524f8f14b1749fc1c.png",
            categoryName = "Anti-Curación & Armadura",
            purpose = "Mitiga curaciones y robo de vida de atacantes físicos continuos mientras refleja daño mágico.",
            bestAgainst = listOf("Aatrox", "Warwick", "Maestro Yi", "Tryndamere", "Jinx", "Yone", "Olaf", "Irelia"),
            keyEffect = "Aplica 40% de Heridas Graves al recibir ataques de los rivales e inmovilizarlos.",
            recommendationTip = "Cómpralo si el equipo rival tiene 2 o más campeones basados en robo de vida o duelistas AD."
        ),
        "Recordatorio Mortal" to SituationalItemInfo(
            name = "Recordatorio Mortal",
            iconUrl = "file:///android_asset/offline_images/3b9e64690847f3bc956e9db35a455f32.png",
            categoryName = "Anti-Curación & Penetración AD",
            purpose = "Destruye tanques y anula la regeneración masiva de curadores enemigos para tiradores y asesinos.",
            bestAgainst = listOf("Soraka", "Yuumi", "Dr. Mundo", "Vladimir", "Swain", "Sion", "Aatrox", "Volibear"),
            keyEffect = "Otorga 30% de Penetración de Armadura y 40% de Heridas Graves con golpes físicos.",
            recommendationTip = "Prioridad absoluta en ADCs si el soporte rival es de sanación (Soraka/Yuumi) o hay un coloso imparable."
        ),
        "Morellonomicón" to SituationalItemInfo(
            name = "Morellonomicón",
            iconUrl = "file:///android_asset/offline_images/473e58dc0df0c96012529455146ce012.png",
            categoryName = "Anti-Curación AP",
            purpose = "Aplica reducción de sanación a múltiples enemigos simultáneamente con daño mágico en área.",
            bestAgainst = listOf("Vladimir", "Soraka", "Swain", "Warwick", "Yuumi", "Samira", "Ekko"),
            keyEffect = "Insignia Maldita: Infligir daño mágico aplica 40% de Heridas Graves por 3 segundos.",
            recommendationTip = "Imprescindible para magos de poke o daño en área (Ziggs, Brand, Lux, Morgana) frente a healers."
        ),
        "Colmillo de Serpiente" to SituationalItemInfo(
            name = "Colmillo de Serpiente",
            iconUrl = "file:///android_asset/offline_images/8d0a2f1589e177a2bf2ad148cb31a65f.png",
            categoryName = "Anti-Escudos (Letalidad)",
            purpose = "Destruye y reduce drásticamente la potencia de los escudos defensivos enemigos.",
            bestAgainst = listOf("Sett", "Karma", "Lulu", "Shen", "Janna", "Braum", "Sion", "Lux", "Riven"),
            keyEffect = "Destructor de Escudos: Reduce la ganancia de escudos en 50% (35% a distancia) y drena escudos activos.",
            recommendationTip = "Compra obligatoria contra composiciones 'Proteger al Carry' con soportes de escudos dobles o Sterak."
        ),
        "Presagio de Randuin" to SituationalItemInfo(
            name = "Presagio de Randuin",
            iconUrl = "file:///android_asset/offline_images/1420e397855c6263e113cf0a16d4bb71.png",
            categoryName = "Anti-Crítico & Velocidad de Ataque",
            purpose = "Reduce el impacto de los impactos críticos y drena la velocidad de ataque enemiga.",
            bestAgainst = listOf("Yasuo", "Yone", "Jinx", "Caitlyn", "Tristana", "Tryndamere", "Lucian"),
            keyEffect = "Humildad: Reduce el daño crítico recibido en 16% y reduce la velocidad de ataque del agresor.",
            recommendationTip = "El mejor objeto defensivo frente a composiciones con tiradores hiper-carry o hermanos de viento."
        ),
        "Fuerza de la Naturaleza" to SituationalItemInfo(
            name = "Fuerza de la Naturaleza",
            iconUrl = "file:///android_asset/offline_images/e1f9d816eb318d20e5c768c4fa05290d.png",
            categoryName = "Anti-Daño Mágico Continuo",
            purpose = "Otorga la máxima resistencia mágica y reducción porcentual frente a magos de daño en el tiempo.",
            bestAgainst = listOf("Brand", "Swain", "Aurelion Sol", "Teemo", "Gwen", "Katarina", "Kassadin", "Lillia"),
            keyEffect = "Absorción: Acumula stacks al recibir daño mágico hasta otorgar 25% de reducción de daño mágico.",
            recommendationTip = "Cómpralo cuando los rivales tengan 2 o más fuentes de daño AP continuo o quemaduras."
        ),
        "Reloj de Arena de Zhonya" to SituationalItemInfo(
            name = "Reloj de Arena de Zhonya",
            iconUrl = "file:///android_asset/offline_images/3b32bd3dbf4245dc0952c48bc603bcc8.png",
            categoryName = "Inmunidad / Estasis Activa",
            purpose = "Permite esquivar habilidades definitivas fatales y combos explosivos de eliminación rápida.",
            bestAgainst = listOf("Zed", "Syndra", "Fizz", "Kayn", "Katarina", "Talon", "Malphite", "Nocturne"),
            keyEffect = "Estasis: Te vuelve invulnerable e inalcanzable durante 2.5 segundos (no puedes moverte ni atacar).",
            recommendationTip = "Usa el encanto de Zhonya justo cuando el asesino lance su definitiva sobre ti para anularla por completo."
        ),
        "Baile de la muerte" to SituationalItemInfo(
            name = "Baile de la muerte",
            iconUrl = "file:///android_asset/offline_images/7f8482a5143b2c02ad323ce93df371f1.png",
            categoryName = "Anti-Burst AD & Supervivencia (Parche 7.3a)",
            purpose = "Convierte el daño de ráfaga en daño diferido (Cauterizar 30%) y limpia el sangrado curando en derribos (Reto 90% AD adicional). Coste ajustado a 3300g.",
            bestAgainst = listOf("Zed", "Kha'Zix", "Rengar", "Talon", "Pantheon", "Jayce", "Draven"),
            keyEffect = "Cauterizar: 30% del daño recibido se difiere en 3s. Reto: Derribos limpian el sangrado y curan 90% de AD adicional.",
            recommendationTip = "Clave para luchadores y duelistas cuando necesites entrar a peleas intensas sin ser eliminado de una sola ráfaga."
        ),
        "Rencor de Serylda" to SituationalItemInfo(
            name = "Rencor de Serylda",
            iconUrl = "file:///android_asset/offline_images/422b305b36178590bc9ddd6e826c22ba.png",
            categoryName = "Penetración & Ralentización",
            purpose = "Otorga penetración de armadura porcentual y hace que todas las habilidades dañinas ralenticen.",
            bestAgainst = listOf("Ornn", "Malphite", "Nautilus", "Garen", "Nasus", "Darius", "K'Sante"),
            keyEffect = "Frío Intenso: Las habilidades dañinas ralentizan un 30% a los enemigos durante 1 segundo.",
            recommendationTip = "Excelente para tiradores de habilidades (Ezreal, Varus, Jhin) o asesinos para kitear a tanques lentos."
        ),
        "Recuerdos de lord Dominik" to SituationalItemInfo(
            name = "Recuerdos de lord Dominik",
            iconUrl = "file:///android_asset/offline_images/c3893b84c990398e6ed58b03c16cafa0.webp",
            categoryName = "Anti-Tanques & Vida Máxima",
            purpose = "Penetra armadura masiva (+36%) y destroza a enemigos que acumulan mucha vida adicional mediante daño porcentual.",
            bestAgainst = listOf("Sion", "Dr. Mundo", "Cho'Gath", "Ornn", "Malphite", "Sett", "Volibear", "Nautilus"),
            keyEffect = "Verdugo de gigantes: Inflige hasta un 12% de daño adicional según la vida adicional del enemigo (máximo con 1200 de vida adicional) y +36% Penetración de Armadura.",
            recommendationTip = "El mejor objeto antitanques para tiradores críticos frente a campeones que acumulen Corazón de Acero o Warmog."
        ),
        "Ángel Guardián" to SituationalItemInfo(
            name = "Ángel Guardián",
            iconUrl = "file:///android_asset/offline_images/dddefc0a5f24a544b89699b38a9a35e1.png",
            categoryName = "Resurrección en Combate",
            purpose = "Otorga una segunda oportunidad de vida en las peleas de equipo decisivas de juego tardío.",
            bestAgainst = listOf("Composiciones de dive agresivo", "Asesinos con combos all-in (Diana, Akali, Zed, Kayn)"),
            keyEffect = "Renacimiento: Al recibir daño letal, resucitas tras 4s con 50% de vida base y 30% de maná.",
            recommendationTip = "Armar como 4to o 5to objeto en Carries para poder jugar agresivo sin temor a ser cazado antes del Barón."
        ),
        "Fauces de Malmortius" to SituationalItemInfo(
            name = "Fauces de Malmortius",
            iconUrl = "file:///android_asset/offline_images/89889a5db477564f0dded7057e9a1916.png",
            categoryName = "Escudo Salvavidas Anti-Mágico",
            purpose = "Genera un escudo gigantesco contra daño mágico al bajar de 35% de vida para resistir ejecuciones.",
            bestAgainst = listOf("Evelynn", "Akali", "Syndra", "Veigar", "Fizz", "Ekko", "Kassadin"),
            keyEffect = "Línea de Vida: Otorga escudo mágico equivalente a 200 + 20% de vida máxima y omnivampirismo.",
            recommendationTip = "La mejor alternativa frente a asesinos AP para campeones basados en daño de ataque (AD)."
        ),
        "Báculo del Vacío" to SituationalItemInfo(
            name = "Báculo del Vacío",
            iconUrl = "file:///android_asset/offline_images/f4c23d99ec30ef6893a81208846a8831.png",
            categoryName = "Penetración Mágica Porcentual",
            purpose = "Ignora un porcentaje masivo de la resistencia mágica enemiga para que tus hechizos no pierdan daño.",
            bestAgainst = listOf("Galio", "Mundo", "Ornn", "Malphite", "Shen", "Alistar", "Braum"),
            keyEffect = "Disolución: Otorga 45% de Penetración Mágica porcentual.",
            recommendationTip = "Obligatorio como 3er o 4to objeto para cualquier mago si el equipo enemigo construye Resistencia Mágica."
        ),
        "Coraza del Muerto" to SituationalItemInfo(
            name = "Coraza del Muerto",
            iconUrl = "file:///android_asset/offline_images/3a33fd10d1e6f9e3f55dd6b553970311.png",
            categoryName = "Movilidad & Iniciación",
            purpose = "Otorga velocidad de rotación rápida por el mapa y ralentiza al primer objetivo golpeado.",
            bestAgainst = listOf("Composiciones de poke móvil", "Tiradores sin dash (Jhin, Ashe, Miss Fortune)"),
            keyEffect = "Naufragador: Aumenta velocidad de movimiento hasta +50 y descarga daño adicional con ralentización.",
            recommendationTip = "Ideal para tanques e iniciadores que necesitan flanquear o cazar rivales desposicionados."
        ),
        "Protector Pétreo" to SituationalItemInfo(
            name = "Protector Pétreo",
            iconUrl = "file:///android_asset/offline_images/e361b2bafad7a688cb9f134d5c210943.png",
            categoryName = "Megashield de Equipo (Activa)",
            purpose = "Multiplica drásticamente la resistencia para absorber el foco de daño de 5 enemigos en teamfights.",
            bestAgainst = listOf("Teamfights masivas 5v5", "Iniciaciones directas contra composiciones de alto daño combinado"),
            keyEffect = "Pétreo: Otorga un escudo del 30% de tu vida máxima (aumentado a 100% si hay 3+ enemigos cerca).",
            recommendationTip = "Actívalo justo después de entrar con tu iniciación principal para sobrevivir al contraataque rival."
        ),
        "Corazón de Hielo" to SituationalItemInfo(
            name = "Corazón de Hielo",
            iconUrl = "file:///android_asset/offline_images/989ee173a52f7cfc8ea3fd107415dc38.png",
            categoryName = "Aura Reductora de Ataque",
            purpose = "Reduce de forma pasiva continua la velocidad de ataque de todos los enemigos en un radio cercano.",
            bestAgainst = listOf("Maestro Yi", "Jinx", "Tryndamere", "Tristana", "Maestro Yi", "Vayne", "Kai'Sa"),
            keyEffect = "Aura Helada: Reduce la velocidad de ataque de los enemigos cercanos en un 20%.",
            recommendationTip = "Fantástico en tanques de primera línea cuando los rivales dependen de acumular autoataques."
        ),
        "Velo de la Banshee" to SituationalItemInfo(
            name = "Velo de la Banshee",
            iconUrl = "file:///android_asset/offline_images/8501d4d39cb74524631ed6ef74b1f410.png",
            categoryName = "Escudo Anti-Hechizos AP",
            purpose = "Bloquea automáticamente la próxima habilidad enemiga para evitar ser cazado o estuneado.",
            bestAgainst = listOf("Blitzcrank", "Malphite", "Nautilus", "Thresh", "Ahri", "Lux", "Morgana"),
            keyEffect = "Anular: Otorga un escudo de hechizos que bloquea la siguiente habilidad hostil (recarga en 40s).",
            recommendationTip = "Vital para magos inmóviles que no pueden permitirse recibir un gancho o CC antes de una pelea."
        ),
        "Filo de la Noche" to SituationalItemInfo(
            name = "Filo de la Noche",
            iconUrl = "file:///android_asset/offline_images/e450b4ac7163f1de8de7cfe932744c45.png",
            categoryName = "Escudo Anti-Hechizos AD",
            purpose = "Otorga letalidad, vida y un escudo de hechizos para asegurar que los asesinos culminen su combo.",
            bestAgainst = listOf("Lulu", "Vayne", "Syndra", "Vex", "Poppy", "Gragas"),
            keyEffect = "Anular: Escudo de hechizos que bloquea la siguiente habilidad enemiga.",
            recommendationTip = "Permite a los asesinos saltar sobre el Carry sin ser interrumpidos por habilidades de desenganche."
        ),
        "Coraza dual purpúrea" to SituationalItemInfo(
            name = "Coraza dual purpúrea",
            iconUrl = "file:///android_asset/offline_images/amaranths_twinguard.webp",
            categoryName = "Resistencia Híbrida & Tenacidad (Parche 7.3)",
            purpose = "Aumenta un 30% la armadura y resistencia mágica en combate prolongado, otorgando además tenacidad masiva.",
            bestAgainst = listOf("Composiciones de daño mixto (AD + AP)", "Peleas grupales largas 5v5"),
            keyEffect = "Resistencia: Otorga +30% Armadura, +30% Resistencia Mágica y +20% Tenacidad a cargas máximas.",
            recommendationTip = "El mejor objeto defensivo de late-game para tanques e iniciadores frente a daño variado."
        ),
        "Segador de esencia" to SituationalItemInfo(
            name = "Segador de esencia",
            iconUrl = "file:///android_asset/offline_images/essence_reaver.webp",
            categoryName = "Crítico, Maná & Daño por Habilidad (Parche 7.3)",
            purpose = "Potencia los ataques básicos tras lanzar habilidades e inflige daño crítico amplificado mientras restaura maná.",
            bestAgainst = listOf("Carries basados en habilidades (Lucian, Ezreal, Corki, Xayah, Gangplank)"),
            keyEffect = "Espada Hechizada: Las habilidades potencian el siguiente básico infligiendo daño adicional y restaurando maná faltante.",
            recommendationTip = "Core item indiscutible para tiradores y duelistas que consumen gran cantidad de maná y rotan habilidades constantemente."
        ),
        "Navaja de asalto" to SituationalItemInfo(
            name = "Navaja de asalto",
            iconUrl = "file:///android_asset/offline_images/stormrazor.webp",
            categoryName = "Energizado, Kiting & Burst AD (Parche 7.3)",
            purpose = "Genera un impacto energizado con daño relámpago, otorga un aumento explosivo de velocidad de movimiento y ralentiza al enemigo.",
            bestAgainst = listOf("Enemigos móviles difíciles de alcanzar", "Tiradores de kiting a distancia (Caitlyn, Jinx, Tristana, Kai'Sa)"),
            keyEffect = "Paralizar: Los ataques energizados infligen daño mágico adicional, ralentizan 75% por 0.5s y otorgan velocidad de movimiento.",
            recommendationTip = "Excelente primer o segundo objeto para tener control de espaciado y atrapar rivales con el primer impacto."
        ),
        "Diadema susurrante" to SituationalItemInfo(
            name = "Diadema susurrante",
            iconUrl = "file:///android_asset/offline_images/whispering_headband.webp",
            categoryName = "Soporte Encantador & Maná (Parche 7.3a)",
            purpose = "Otorga vida, maná y poder de curaciones y escudos con Armonía (+0.25% por maná). Al alcanzar 700 de maná evoluciona a Diadema melodiosa.",
            bestAgainst = listOf("Composiciones de desgaste", "Soportes encantadores (Lulu, Janna, Nami, Karma, Sona, Seraphine)"),
            keyEffect = "Armonía (7.3a): Concede un 0.25% de poder de curaciones y escudos adicional según el maná. Carga de maná evoluciona el objeto.",
            recommendationTip = "Excelente compra para soportes encantadores que requieren regeneración y escalado de sanación en fase de líneas."
        ),
        "Diadema melodiosa" to SituationalItemInfo(
            name = "Diadema melodiosa",
            iconUrl = "file:///android_asset/offline_images/diadem_of_songs.webp",
            categoryName = "Soporte Encantador Evolucionado (Parche 7.3a)",
            purpose = "Forma final tras acumular 700 de maná. Concede Armonía al 0.25% y sana al aliado con menor vida en combate durante peleas de equipo.",
            bestAgainst = listOf("Peleas de equipo prolongadas 5v5", "Composiciones de asedio y desgaste"),
            keyEffect = "Cántico Sagrado: Cura al aliado con menor vida en combate al usar habilidades o curar/escudar. Armonía: +0.25% curación y escudos.",
            recommendationTip = "Maximiza radicalmente la supervivencia de todo el equipo en peleas por Dragón y Barón Nashor."
        ),
        "Diadema de canciones" to SituationalItemInfo(
            name = "Diadema melodiosa",
            iconUrl = "file:///android_asset/offline_images/diadem_of_songs.webp",
            categoryName = "Soporte Encantador Evolucionado (Parche 7.3a)",
            purpose = "Forma final tras acumular 700 de maná. Concede Armonía al 0.25% y sana al aliado con menor vida en combate durante peleas de equipo.",
            bestAgainst = listOf("Peleas de equipo prolongadas 5v5", "Composiciones de asedio y desgaste"),
            keyEffect = "Cántico Sagrado: Cura al aliado con menor vida en combate al usar habilidades o curar/escudar. Armonía: +0.25% curación y escudos.",
            recommendationTip = "Maximiza radicalmente la supervivencia de todo el equipo en peleas por Dragón y Barón Nashor."
        ),
        "Hoz espectral" to SituationalItemInfo(
            name = "Hoz espectral",
            iconUrl = "file:///android_asset/offline_images/spectral_sickle.webp",
            categoryName = "Soporte Ofensivo / Hostigamiento",
            purpose = "Objeto inicial para soportes que hostigan con daño a distancia. Genera 65 de oro por intercambio cerca de aliados y evoluciona en Guadaña de la Niebla Negra.",
            bestAgainst = listOf("Líneas de rango agresivas (Lux, Senna, Karma, Nami, Pyke, Ashe)"),
            keyEffect = "Tributo: 1 orbe cada 30s que otorga 65 oro y cura al golpear rivales. Misión 750 oro.",
            recommendationTip = "Indispensable para soportes de hostigamiento para no perder economía frente a la muerte de súbditos."
        ),
        "Guadaña de la Niebla Negra" to SituationalItemInfo(
            name = "Guadaña de la Niebla Negra",
            iconUrl = "file:///android_asset/offline_images/905380f9e777f9fb79a8ade29433cf27.webp",
            categoryName = "Soporte Ofensivo Evolucionado",
            purpose = "Evolución completa de Hoz espectral. Otorga daño adaptable, velocidad de rotación hacia el carry aliado y daño aumentado contra guardianes de visión.",
            bestAgainst = listOf("Composiciones de asedio, visión y rotaciones rápidas"),
            keyEffect = "Estallido de alma: Otorga estadísticas crecientes periódicamente y +30% velocidad hacia el compañero ideal.",
            recommendationTip = "Acelera las rotaciones y el control de mapa en juego medio y tardío."
        ),
        "Escudo reliquia" to SituationalItemInfo(
            name = "Escudo reliquia",
            iconUrl = "file:///android_asset/offline_images/relic_shield.webp",
            categoryName = "Soporte Tanque / Ejecución",
            purpose = "Objeto inicial para soportes cuerpo a cuerpo o de iniciación. Permite ejecutar súbditos bajo 65% de vida para compartir oro y vida con el tirador.",
            bestAgainst = listOf("Líneas de combate cuerpo a cuerpo y tanques (Nautilus, Leona, Braum, Thresh, Alistar)"),
            keyEffect = "Tributo: Ejecuta súbditos compartiendo botín y otorgando orbes de energía.",
            recommendationTip = "Fundamental en tanques de apoyo para asegurar el empuje de oleadas y la economía del carry."
        ),
        "Baluarte de la montaña" to SituationalItemInfo(
            name = "Baluarte de la montaña",
            iconUrl = "file:///android_asset/offline_images/90729ecdec9a7cb238b1156edbce22f6.webp",
            categoryName = "Soporte Tanque Evolucionado",
            purpose = "Evolución de Escudo reliquia. Concede vida progresiva, celeridad y velocidad extrema para auxiliar al tirador vinculado.",
            bestAgainst = listOf("Peleas de equipo de primera línea y protección del carry"),
            keyEffect = "Estallido de alma: Proporciona hasta 250 de vida y rotación veloz hacia el compañero ideal.",
            recommendationTip = "Garantiza presencia constante en el mapa junto al tirador prioritario."
        ),
        "Abrazo del serafín" to SituationalItemInfo(
            name = "Abrazo del serafín",
            iconUrl = "file:///android_asset/offline_images/0bfb36d8b1370271319b5895293c4056.webp",
            categoryName = "Escalado AP & Escudo Salvavidas",
            purpose = "Evolución final del Báculo del arcángel al completar 700 de maná. Otorga AP proporcional al maná y un escudo salvavidas crítico del 16% del maná al bajar del 35% de vida.",
            bestAgainst = listOf("Magos dependientes de maná frente a asesinos de ráfaga (Kassadin, Orianna, Ryze, Ziggs, Anivia)"),
            keyEffect = "Salvavidas: Escudo vitalicio del 16% de maná máximo al recibir daño letal. Asombro: +2% AP por maná.",
            recommendationTip = "El mejor objeto de seguridad y escalado para magos de control en peleas tardías."
        ),
        "El gran invierno" to SituationalItemInfo(
            name = "El gran invierno",
            iconUrl = "file:///android_asset/offline_images/ae7584f101f22466aaeaca45438a8371.webp",
            categoryName = "Tanque Escudo & Maná (Parche 7.3a)",
            purpose = "Forma evolucionada de Llegada del invierno (+500 vida, +1200 maná). Otorga un escudo masivo (Coloso helado) al inmovilizar o ralentizar a un enemigo.",
            bestAgainst = listOf("Tanques y colosos con alto gasto de maná (Nautilus, Sion, Gragas, Malphite, Alistar, Shen)"),
            keyEffect = "Coloso helado: Escudo de 120 + 4.5% del maná al obstaculizar movimiento (+80% si hay varios enemigos).",
            recommendationTip = "Otorga una durabilidad colosal en peleas de equipo a campeones con habilidades de control de masas."
        ),
        "Llegada del invierno" to SituationalItemInfo(
            name = "Llegada del invierno",
            iconUrl = "file:///android_asset/offline_images/ed3f1fac19870153dfdc6bed7a071edd.webp",
            categoryName = "Tanque Escudo & Maná (Parche 7.3a)",
            purpose = "Objeto inicial de maná y vida (+500 vida, +500 maná). Acumula 700 de maná para transformarse en El gran invierno.",
            bestAgainst = listOf("Tanques con alto gasto de maná en fase de líneas"),
            keyEffect = "Asombro: +15% de vida por maná gastado. Carga de maná: Transforma en El gran invierno.",
            recommendationTip = "Primera compra indispensable para tanques que necesitan resolver sus problemas de maná desde el inicio."
        ),
        "Muramana" to SituationalItemInfo(
            name = "Muramana",
            iconUrl = "file:///android_asset/offline_images/08f2da37400fbff1179c90b251358dac.webp",
            categoryName = "Daño Físico & Escalado de Maná (Parche 7.3a)",
            purpose = "Forma evolucionada de Manamune (+40 DA, +1200 maná). Aplica daño físico de impacto adicional en ataques básicos y habilidades.",
            bestAgainst = listOf("Tiradores y asesinos de daño físico dependientes de habilidades (Ezreal, Varus, Jayce, Senna, Corki)"),
            keyEffect = "Impacto: +1.5% maná como daño en ataques y +3.5% maná en habilidades. Asombro: +2% DA por maná.",
            recommendationTip = "Pico de poder letal en juego medio que maximiza el daño continuo y de ráfaga."
        ),
        "Manamune" to SituationalItemInfo(
            name = "Manamune",
            iconUrl = "file:///android_asset/offline_images/aba4fb973b550edadd3be632fb355f74.webp",
            categoryName = "Daño Físico & Escalado de Maná (Parche 7.3a)",
            purpose = "Objeto inicial de AD y maná (+40 DA, +500 maná). Acumula 700 de maná para evolucionar en Muramana.",
            bestAgainst = listOf("Tiradores y duelistas de spam de habilidades"),
            keyEffect = "Asombro: +2% DA según el maná. Carga de maná: Transforma en Muramana.",
            recommendationTip = "Permite a tiradores basados en habilidades hostigar sin preocuparse por la falta de maná."
        ),
        "Flechas de los Yun Tal" to SituationalItemInfo(
            name = "Flechas de los Yun Tal",
            iconUrl = "file:///android_asset/offline_images/yun_tal_wildarrows.webp",
            categoryName = "Daño Físico & Crítico con Ráfaga (Parche 7.3a)",
            purpose = "Mejorado en el Parche 7.3a con +35% velocidad de ataque base y +35% de velocidad de ataque en Ráfaga tras asestar golpes críticos con sangrado continuo.",
            bestAgainst = listOf("Tiradores de autoataques continuos (Jinx, Tristana, Draven, Caitlyn, Ashe, Kai'Sa)"),
            keyEffect = "Ráfaga (7.3a): Otorga +35% velocidad de ataque tras crítico (25s enfriamiento). Hojas sangrientas: Aplica sangrado físico continuo.",
            recommendationTip = "Primer objeto estelar de crítico tras las mejoras del Parche 7.3a para acelerar el pico de poder en juego temprano y medio."
        ),
        "Velo del amanecer" to SituationalItemInfo(
            name = "Velo del amanecer",
            iconUrl = "file:///android_asset/offline_images/dawnshroud.webp",
            categoryName = "Inmovilización & Revelación Defensiva (Parche 7.3)",
            purpose = "Otorga daño en área y revelación al inmovilizar o sufrir inmovilizaciones de campeones enemigos.",
            bestAgainst = listOf("Iniciadores y buceadores enemigos", "Peleas de equipo en zonas cerradas"),
            keyEffect = "Luz solar: Al inmovilizar o sufrir inmovilización, revela y daña a los enemigos cercanos.",
            recommendationTip = "Ideal para tanques y soportes de iniciación (Leona, Nautilus, Alistar, Thresh)."
        ),
        "Desesperanza eterna" to SituationalItemInfo(
            name = "Desesperanza eterna",
            iconUrl = "file:///android_asset/offline_images/unending_despair.webp",
            categoryName = "Drenado de Vida & Armadura para Tanques (Parche 7.3)",
            purpose = "Drena continuamente la vida de los enemigos cercanos cada pocos segundos en combate, curando al portador.",
            bestAgainst = listOf("Peleas grupales cuerpo a cuerpo prolongadas", "Enemigos que se agrupan en cuellos de botella"),
            keyEffect = "Angustia: Cada 4 segundos en combate con campeones, inflige daño mágico a enemigos cercanos y te cura por el daño infligido.",
            recommendationTip = "Objeto obligatorio para tanques de primera línea (Ornn, Sion, Malphite, Nautilus, Amumu) en peleas 5v5."
        ),
        "Ecos de Helia" to SituationalItemInfo(
            name = "Ecos de Helia",
            iconUrl = "file:///android_asset/offline_images/echoes_of_helia.webp",
            categoryName = "Soporte Encantador, Daño & Curación (Parche 7.3)",
            purpose = "Acumula fragmentos de alma al dañar a campeones enemigos y los consume al curar o escudar a un aliado para sanarlo e infligir daño.",
            bestAgainst = listOf("Soportes de utilidad agresivos (Nami, Sona, Karma, Milio, Seraphine, Lulu)"),
            keyEffect = "Sifón de Almas: Infligir daño a un enemigo otorga un fragmento de alma. Curar o poner escudo a un aliado consume fragmentos para curar al aliado y dañar al enemigo más cercano.",
            recommendationTip = "El mejor objeto para maximizar el impacto de los encantadores en intercambios 2v2 de carril y peleas de dragón."
        ),
        "Rookern Kaénico" to SituationalItemInfo(
            name = "Rookern Kaénico",
            iconUrl = "file:///android_asset/offline_images/93b2f7e8e684cf1ec4aabcdc13c89c8e.webp",
            categoryName = "Anti-Mágico Puro & Escudo AP",
            purpose = "Genera un escudo de absorción mágica masivo fuera de combate que mitiga por completo el daño de ráfaga AP.",
            bestAgainst = listOf("Syndra", "Veigar", "Zoe", "Evelynn", "Kassadin", "Lux", "Brand"),
            keyEffect = "Ruina de Magos: Tras 12s sin daño mágico, otorga un escudo mágico del 14% de tu vida máxima.",
            recommendationTip = "Prioridad absoluta contra composiciones de doble mago o hipercarry mágico."
        ),
        "Manto de la Duodécima Hora" to SituationalItemInfo(
            name = "Manto de la Duodécima Hora",
            iconUrl = "file:///android_asset/offline_images/0529996e54514079a61b4cee270526fb.webp",
            categoryName = "Supervivencia Crítica & Desenganche",
            purpose = "Otorga una inyección masiva de vida adicional y velocidad al caer por debajo del 35% de vida.",
            bestAgainst = listOf("Asesinos de ejecución rápida", "Peleas cerradas al límite de vida"),
            keyEffect = "Línea de Vida: Otorga hasta 45% de vida adicional y 50% de resistencia a ralentizaciones al bajar del 35% HP.",
            recommendationTip = "Perfecto para colosos e iniciadores que se sumergen en la línea trasera enemiga."
        ),
        "Corona Abrasadora" to SituationalItemInfo(
            name = "Corona Abrasadora",
            iconUrl = "file:///android_asset/offline_images/8c585684ae11f4bd1818521dc47bcc3d.webp",
            categoryName = "Quemadura Porcentual para Tanques",
            purpose = "Quema a los enemigos por porcentaje de su vida máxima con cada ataque y habilidad.",
            bestAgainst = listOf("Sion", "Dr. Mundo", "Cho'Gath", "Ornn", "Heartsteel Users"),
            keyEffect = "Toque Ardiente: Inflige 1.4% de la vida máxima del objetivo como daño mágico por segundo.",
            recommendationTip = "Excelente en tanques para derretir a otros colosos con mucha vida sin sacrificar defensas."
        ),
        "Tridente de Oceánida" to SituationalItemInfo(
            name = "Tridente de Oceánida",
            iconUrl = "file:///android_asset/offline_images/f800a2be044a17e73fe7079161df361a.webp",
            categoryName = "Anti-Escudos para Magos (AP)",
            purpose = "Destruye y reduce los escudos enemigos al infligir daño mágico con habilidades de área o impacto individual.",
            bestAgainst = listOf("Karma", "Lulu", "Sett", "Shen", "Janna", "Lux", "Braum"),
            keyEffect = "Arma Letal: Reduce la potencia de los escudos enemigos hasta un 60% (45% en área).",
            recommendationTip = "Imprescindible para magos cuando el rival cuenta con soportes de escudos o Sterak/Arcoescudo."
        ),
        "Espada Sierra Quimopunk" to SituationalItemInfo(
            name = "Espada Sierra Quimopunk",
            iconUrl = "file:///android_asset/offline_images/823ee379ad90c01f692a5a054015ffdd.png",
            categoryName = "Anti-Curación para Luchadores (AD)",
            purpose = "Otorga daño, vida y aceleración de habilidad mientras aplica reducción de curación continua.",
            bestAgainst = listOf("Aatrox", "Warwick", "Vladimir", "Soraka", "Dr. Mundo", "Olaf"),
            keyEffect = "Heridas Graves: Aplica 40% de reducción de curación a campeones enemigos al golpearlos con daño físico.",
            recommendationTip = "El objeto anti-sanación óptimo para luchadores que necesitan durabilidad y daño balanceado."
        ),
        "Cimitarra Mercurial" to SituationalItemInfo(
            name = "Cimitarra Mercurial",
            iconUrl = "file:///android_asset/offline_images/bfba991bdd26cac9dc642cd79060d0f5.webp",
            categoryName = "Purificación de CC para Carries AD",
            purpose = "Elimina todo el control de masas inmediatamente y otorga tenacidad para reposicionarse.",
            bestAgainst = listOf("Veigar", "Skarner", "Warwick", "Ashe", "Leona", "Twisted Fate"),
            keyEffect = "Fajín de Mercurio: Limpia todo el CC activo y otorga 30% de tenacidad durante 1.5s.",
            recommendationTip = "Indispensable para tiradores cuando el rival tiene aturdimientos o supresiones directas."
        ),
        "Arco axiomático" to SituationalItemInfo(
            name = "Arco axiomático",
            iconUrl = "file:///android_asset/offline_images/3580a8fd19fbb3ab6be5cadb022aa07a.png",
            categoryName = "Letalidad & Reset de Definitiva (H4)",
            purpose = "Reembolsa 20% del enfriamiento total de la Habilidad Definitiva al conseguir derribos.",
            bestAgainst = listOf("Composiciones de squishies frágiles", "Zed", "Pyke", "Miss Fortune", "Varus", "Kha'Zix"),
            keyEffect = "Oleada: Los derribos reembolsan un 20% del enfriamiento total de tu H4 (Definitiva).",
            recommendationTip = "Imprescindible para asesinos y tiradores de letalidad en peleas de equipo caóticas donde requieres tu Definitiva en múltiples ocasiones."
        )
    )

    fun getAdvice(itemName: String, lang: String = "es"): SituationalItemInfo {
        // Direct search or partial match
        val matched = adviceMap.entries.firstOrNull { 
            it.key.equals(itemName, ignoreCase = true) || 
            itemName.contains(it.key, ignoreCase = true) ||
            it.key.contains(itemName, ignoreCase = true)
        }
        
        val baseAdvice = matched?.value ?: run {
            val itemData = WildRiftItemsData.getItemByName(itemName)
            val effectText = if (!itemData?.passive.isNullOrBlank()) itemData!!.passive else "Objeto estratégico seleccionado para contrarrestar amenazas específicas de la composición rival."
            val statsText = if (!itemData?.stats.isNullOrBlank()) itemData!!.stats else "Mejora estadísticas y pasivas críticas para neutralizar las condiciones de victoria del rival."
            SituationalItemInfo(
                name = itemData?.name ?: itemName,
                iconUrl = itemData?.iconUrl ?: WildRiftItemsData.getItemIconByName(itemName),
                categoryName = itemData?.category ?: "Objeto Situacional Adaptativo",
                purpose = effectText,
                bestAgainst = listOf("Composiciones rivales especializadas", "Amenazas prioritarias de la partida"),
                keyEffect = statsText,
                recommendationTip = "Constrúyelo según el estado de la partida para contrarrestar el daño o mecánicas del enemigo."
            )
        }

        // Ensure iconUrl is populated from WildRiftItemsData if blank or placeholder
        val finalIcon = if (baseAdvice.iconUrl.isBlank() || baseAdvice.iconUrl.contains("1001.png")) {
            val repoIcon = WildRiftItemsData.getItemIconByName(baseAdvice.name)
            if (repoIcon.isNotBlank()) repoIcon else baseAdvice.iconUrl
        } else {
            baseAdvice.iconUrl
        }

        return baseAdvice.copy(
            name = com.example.util.trStr(lang, baseAdvice.name),
            categoryName = com.example.util.trStr(lang, baseAdvice.categoryName),
            purpose = com.example.util.trStr(lang, baseAdvice.purpose),
            keyEffect = com.example.util.trStr(lang, baseAdvice.keyEffect),
            recommendationTip = com.example.util.trStr(lang, baseAdvice.recommendationTip),
            bestAgainst = baseAdvice.bestAgainst.map { com.example.util.trStr(lang, it) },
            iconUrl = finalIcon
        )
    }
}
