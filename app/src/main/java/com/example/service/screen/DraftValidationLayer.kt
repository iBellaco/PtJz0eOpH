package com.example.service.screen

import com.example.model.Champion
import com.example.model.LaneRole
import java.text.Normalizer
import java.util.Locale

/**
 * Capa de Validación y Filtrado Inteligente para el Escáner de Selección de Campeones (Draft).
 * Elimina falsos positivos (como apodos de invocador estilo "XCS Lucianito"),
 * clasifica roles en múltiples idiomas (ES, EN, PT), y resuelve la asignación
 * óptima de roles garantizando consistencia (Zero-Guessing).
 */
object DraftValidationLayer {

    // Palabras y frases de ruido de interfaz que deben descartarse inmediatamente
    private val NOISE_WORDS = setOf(
        "draft", "coach", "tiers", "tier", "champs", "campeon", "campeones",
        "aliado", "aliados", "rival", "rivales", "enemigo", "enemigos", "vs", "versus",
        "marca", "estelar", "eterna", "beta", "primera", "segunda", "seleccion", "selección",
        "eleccion", "elección", "escolha", "selecao", "seleção", "pick", "picks",
        "auto-scan", "autoscan", "activo", "detener", "asistente", "ajustes",
        "bloquear", "elegir", "jugador", "player", "tarjeta", "aumento", "usó", "uso",
        "combatamos", "juntos", "excelente", "composicion", "composición", "oponentes",
        "eligiendo", "equipo", "buscando", "emparejamiento", "listo", "esperando",
        "fijar", "preseleccion", "preselección", "fase", "bloqueando", "maestria", "maestría",
        "nivel", "lvl", "lv", "puntos", "pts", "pnt", "rango", "insignia", "emblema",
        "primera seleccion", "primera selección", "primera eleccion", "primera elección",
        "segunda seleccion", "segunda selección", "segunda eleccion", "segunda elección",
        "1a eleccion", "1ª eleccion", "1.a eleccion", "1.ª eleccion", "1a seleccion", "1ª seleccion",
        "2a eleccion", "2ª eleccion", "2.a eleccion", "2.ª eleccion", "2a seleccion", "2ª seleccion",
        "primer pick", "segundo pick", "first pick", "second pick",
        "primeira escolha", "segunda escolha", "primeira selecao", "segunda selecao",
        "orden de seleccion", "orden de selección", "orden de eleccion", "orden de elección",
        // Hechizos de Invocador (ES, EN, PT) para evitar que aparezcan como nombres o texto en el Hub
        "destello", "flash", "castigo", "smite", "golpe", "ignicion", "ignición", "ignite", "incendiar",
        "fantasmal", "ghost", "fantasma", "barrera", "barrier", "curacion", "curación", "heal", "curar", "cura",
        "extenuacion", "extenuación", "exhaust", "exaustao", "exaustão", "claridad", "clarity",
        "purificacion", "purificación", "cleanse", "purificar", "teleport", "teletransporte", "teleportacion",
        "teleportación", "nieve", "mark", "dash", "snowball", "golpear", "aplastamiento", "hechizo", "hechizos",
        "spells", "spell", "feitiço", "feitiços"
    )

    fun isNoiseText(text: String): Boolean {
        val norm = normalize(text).trim()
        if (norm.length < 2) return true
        // Descartar números puros o temporizadores de draft (ej: "18", "25", "30", "0:15", "100%")
        if (norm.matches(Regex("^[0-9\\s:.,%#-]+$"))) return true
        if (norm.startsWith("jugador") || norm.startsWith("player") || norm.startsWith("jogador")) return true
        
        // Descartar frases compuestas o menciones de orden de selección o interfaz
        if (norm.contains("primera") || norm.contains("segunda") ||
            norm.contains("seleccion") || norm.contains("selecao") ||
            norm.contains("eleccion") || norm.contains("escolha") ||
            norm.contains("pick") || norm.contains("orden") ||
            norm.contains("primer") || norm.contains("segundo") ||
            norm.contains("legendaria") || norm.contains("lendaria") ||
            norm.contains("legendary") ||
            norm.contains("buscando oponentes") || norm.contains("combatamos juntos")) {
            return true
        }

        if (NOISE_WORDS.contains(norm)) return true
        val words = norm.split(Regex("\\s+"))
        return words.size == 1 && NOISE_WORDS.contains(words[0])
    }

    // Prefijos o etiquetas comunes de clanes/equipos en nombres de invocador
    private val SUMMONER_PREFIX_REGEX = Regex("^(xcs|tag|fnc|t1|g2|wr|clan|team|pro|tv|ttv|yt|god)\\s+", RegexOption.IGNORE_CASE)

    // Sufijos diminutivos o de apodos que distinguen a un invocador de un campeón real (ej: Lucianito != Lucian)
    private val SUMMONER_DIMINUTIVES = listOf(
        "ito", "ita", "itos", "itas", "cito", "cita", "citos", "citas",
        "inho", "inha", "zinho", "zinha", "ano", "ana", "zera", "god",
        "king", "pro", "boy", "girl", "99", "69", "777", "123", "01"
    )

    /**
     * Determina si la pantalla actual corresponde a una Selección de Clasificatoria Legendaria
     * (Legendary Ranked / Fila Lendária) en Wild Rift, donde todos los nombres de invocador
     * están estrictamente anonimizados (ej: "Jugador en cla...", "Jogador na cla...").
     */
    fun isLegendaryRankedDraft(fullOcrText: String, hasRealSummonerNames: Boolean = false): Boolean {
        if (hasRealSummonerNames) return false
        val norm = normalize(fullOcrText).lowercase(Locale.ROOT)
        
        // La clasificatoria normal dice "Clasificatoria". La Legendaria dice explícitamente "Clasificatoria Legendaria"
        if (norm.contains("clasificatoria legendaria") ||
            norm.contains("clasificatoria legend") ||
            norm.contains("fila lendaria") ||
            norm.contains("fila lendária") ||
            norm.contains("legendary ranked") ||
            norm.contains("legendary queue")) {
            return true
        }
        
        // Detección por anonimización estricta múltiple de Riot (mínimo 3 slots con patrón anónimo)
        val anonMatches = Regex("(jugador\\s*en\\s*cla|jogador\\s*na\\s*cla|player\\s*in\\s*leg)").findAll(norm).count()
        return anonMatches >= 3
    }

    /**
     * Determina si un texto corresponde a un nombre de invocador y NO a un campeón.
     * Ejemplo: "XCS Lucianito", "Gaby11anos", "martincho137", "CacauVegannah" -> true (Invocador)
     * Ejemplo: "LUCIAN", "URGOT", "CAITLYN", "JARVAN IV", "DR. MUNDO" -> false (Campeón real)
     */
    fun isLikelySummonerName(rawText: String, championName: String? = null): Boolean {
        val trimmed = rawText.trim()
        if (trimmed.isBlank() || trimmed.length < 2) return false
        if (isNoiseText(trimmed)) return false
        if (parseRoleFromText(trimmed) != null) return false

        // Si coincide con el nombre o ID de un campeón explícito, nunca es invocador
        if (championName != null) {
            val stripped = stripLeadingMasteryOrRoleIcon(trimmed)
            val normTrimmed = normalize(trimmed)
            val normStripped = normalize(stripped)
            val normChamp = normalize(championName)
            if (normTrimmed == normChamp || normStripped == normChamp ||
                normTrimmed.replace(" ", "") == normChamp.replace(" ", "") ||
                normStripped.replace(" ", "") == normChamp.replace(" ", "")) {
                return false
            }
            if (normChamp.contains("jarvan") && (normTrimmed.contains("jarvan") || normStripped.contains("jarvan"))) {
                return false
            }
        }

        // Si contiene prefijo de clan conocido (ej: "XCS Faker", "T1 Gumayusi")
        if (SUMMONER_PREFIX_REGEX.containsMatchIn(trimmed)) {
            return true
        }

        // Si contiene números mezclados con letras (típico de invocadores como Gaby11anos, martincho137)
        // Excepto si es "Jarvan 4"
        if (Regex("[a-zA-Z]{2,}[0-9]+").containsMatchIn(trimmed) && !trimmed.lowercase(Locale.ROOT).contains("jarvan")) {
            return true
        }

        // Si contiene sufijos de diminutivo claramente de apodo (ej: Lucianito != Lucian)
        val lower = trimmed.lowercase(Locale.ROOT)
        for (dim in SUMMONER_DIMINUTIVES) {
            if (lower.endsWith(dim) && lower.length >= 6) {
                return true
            }
        }

        return false
    }

    /**
     * Valida si una palabra específica es un nombre o alias válido de campeón,
     * descartando diminutivos o apodos de invocador (ej: "lucianito" NO es "lucian").
     */
    fun isValidChampionToken(token: String, championId: String): Boolean {
        val cleanToken = normalize(token).replace(Regex("[^a-z0-9]"), "")
        val cleanChamp = normalize(championId).replace(Regex("[^a-z0-9]"), "")

        // Coincidencia exacta
        if (cleanToken == cleanChamp) return true

        // Si el token termina con un sufijo de apodo, rechazar inmediatamente
        for (dim in SUMMONER_DIMINUTIVES) {
            if (cleanToken.endsWith(dim) && cleanToken.length > cleanChamp.length) {
                return false
            }
        }

        // Aliases oficiales compuestos reconocidos
        val validMultiPartAliases = when (cleanChamp) {
            "drmundo", "dr_mundo" -> setOf("mundo", "drmundo")
            "jarvaniv", "jarvan_iv" -> setOf("jarvan", "j4", "jarvaniv")
            "missfortune", "miss_fortune" -> setOf("mf", "missfortune")
            "twistedfate", "twisted_fate" -> setOf("tf", "twistedfate")
            "nunuandwillump", "nunu_and_willump" -> setOf("nunu", "willump")
            "aurelionsol", "aurelion_sol" -> setOf("asol", "aurelion")
            "leesin", "lee_sin" -> setOf("lee", "leesin")
            "masteryi", "master_yi" -> setOf("yi", "masteryi")
            "tahmkench", "tahm_kench" -> setOf("tahm", "kench")
            "xinzhao", "xin_zhao" -> setOf("xin", "xinzhao")
            else -> emptySet()
        }

        if (validMultiPartAliases.contains(cleanToken)) {
            return true
        }

        return false
    }

    /**
     * Limpia cualquier icono de maestría, insignia de elo o glifo de carril al inicio del texto.
     * En Wild Rift, cada slot muestra: [ICONO_MAESTRIA_O_ROL] + [NOMBRE_DE_LINEA o NOMBRE_DE_CAMPEON].
     * El icono siempre se mantiene a la izquierda y debe ignorarse.
     * Casos soportados:
     * - Símbolos y puntuación: • JINX, > JINX, » JINX, * JINX, # JINX, ~ JINX, / JINX, \ JINX, etc.
     * - Letras y glifos aislados del icono: V JINX, W JINX, Y JINX, M JINX, I JINX, T JINX, X JINX, K JINX
     * - Niveles de maestría o rango: LV7 JINX, M7 JINX, 7 JINX, 1 JINX, VII JINX, M6 JINX
     * - Lo mismo para líneas: • APOYO, > CALLE CENTRAL, V DÚO, M7 APOYO, V CALLE DEL BARÓN
     * - Prefijos pegados sin espacio: VJINX -> JINX, VCALLE -> CALLE, VAPOYO -> APOYO, VJUNGLA -> JUNGLA
     */
    fun stripLeadingMasteryOrRoleIcon(rawText: String): String {
        var clean = rawText.trim()
        if (clean.isBlank()) return ""

        var changed = true
        var loops = 0
        while (changed && loops < 6) {
            changed = false
            loops++

            // 1. Quitar símbolos no alfanuméricos y delimitadores iniciales (ej: "• JINX", "> APOYO", "» CAITLYN", "[7] JINX", "(M7) JINX")
            val withoutSymbols = clean.replace(Regex("^[\\W_]+"), "").trim()
            if (withoutSymbols != clean) {
                clean = withoutSymbols
                changed = true
            }

            // 2. Quitar etiquetas explícitas de maestría (ej: "M7 JINX", "M7JINX", "LV10 JINX", "LEVEL 7 JINX", "MAESTRIA 7 JINX")
            val withoutMasteryTag = clean.replace(Regex("^(m[0-9]{1,2}|lv[0-9]{1,2}|lvl[0-9]{1,2}|level\\s*[0-9]{1,2}|maestria\\s*[0-9]*|maestría\\s*[0-9]*|mastery\\s*[0-9]*|elo)\\s*", RegexOption.IGNORE_CASE), "").trim()
            if (withoutMasteryTag != clean && withoutMasteryTag.isNotBlank()) {
                clean = withoutMasteryTag
                changed = true
            }

            // 3. Quitar números romanos de maestría iniciales seguidos de espacio (ej: "VII JINX" -> "JINX", "IV DARIUS" -> "DARIUS")
            val withoutRomanMastery = clean.replace(Regex("^(viii|vii|iv|iii|ii|ix|x)\\s+", RegexOption.IGNORE_CASE), "").trim()
            if (withoutRomanMastery != clean && withoutRomanMastery.isNotBlank()) {
                clean = withoutRomanMastery
                changed = true
            }

            // Caso especial "VI <CAMPEON>" (ej: "VI DARIUS" -> "DARIUS"). Si va seguido de otro texto no vacío, es maestría 6
            if (clean.length > 3 && clean.startsWith("VI ", ignoreCase = true)) {
                val remainder = clean.substring(3).trim()
                if (remainder.isNotBlank()) {
                    clean = remainder
                    changed = true
                }
            }

            // 4. Quitar glifos o letras aisladas del icono separadas por espacio (de 1 a 3 caracteres)
            // Ejemplos: "V JINX" -> "JINX", "W JINX" -> "JINX", "7 JINX" -> "JINX", "• APOYO" -> "APOYO"
            val spaceIndex = clean.indexOf(' ')
            if (spaceIndex in 1..3) {
                val remainder = clean.substring(spaceIndex + 1).trim()
                val normLower = clean.lowercase(Locale.ROOT)
                val isKnownTwoWordChamp = normLower.startsWith("dr ") || normLower.startsWith("lee ") ||
                    normLower.startsWith("xin ") || normLower.startsWith("jarvan ") ||
                    normLower.startsWith("miss ") || normLower.startsWith("twisted ") ||
                    normLower.startsWith("master ") || normLower.startsWith("aurelion ") ||
                    normLower.startsWith("tahm ")
                if (!isKnownTwoWordChamp && remainder.isNotBlank()) {
                    clean = remainder
                    changed = true
                }
            }
        }

        // 5. Desprender prefijos pegados de 1 a 3 letras a palabras clave de líneas (ej: "vcalle" -> "calle", "vapoyo" -> "apoyo", "vjungla" -> "jungla")
        val lower = clean.lowercase(Locale.ROOT)
        val rolePrefixes = listOf("calle", "carril", "linea", "apoyo", "soporte", "jungla", "baron", "central", "dragon", "dragón", "duo", "dúo")
        for (rp in rolePrefixes) {
            for (pLen in 1..3) {
                if (lower.length >= rp.length + pLen && lower.substring(pLen).startsWith(rp)) {
                    val candidate = clean.substring(pLen).trim()
                    if (candidate.isNotBlank()) {
                        clean = candidate
                        break
                    }
                }
            }
        }

        return clean
    }

    /**
     * Normaliza un texto eliminando tildes y caracteres especiales.
     */
    fun normalize(text: String): String {
        val decomposed = Normalizer.normalize(text, Normalizer.Form.NFD)
        return decomposed.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .lowercase(Locale.ROOT)
            .trim()
    }

    /**
     * Analiza una línea o bloque de texto y detecta si contiene una indicación explícita de carril (ES/EN/PT).
     * Soporta prefijos de iconos de elo alto, maestría o asignación de rol (ej: "> CARRIL CENTRAL", "• MID", "vmid").
     */
    fun parseRoleFromText(rawText: String): LaneRole? {
        val stripped = stripLeadingMasteryOrRoleIcon(rawText)
        val lower = normalize(rawText)
        val lowerStripped = normalize(stripped)
        if (lower.isBlank() && lowerStripped.isBlank()) return null

        // Ignorar identificadores de jugador genéricos como "Jugador 1", "Player 2", etc.
        if (lower.startsWith("jugador") || lower.startsWith("player") || lower.startsWith("jogador")) {
            return null
        }

        // Limpieza de posibles iconos o artefactos iniciales (símbolos, números o 1-3 letras iniciales que representan insignias)
        val textWithoutSymbols = lower.replace(Regex("^[^a-zA-Z0-9]+"), "").trim()
        val textWithoutSymbolsStripped = lowerStripped.replace(Regex("^[^a-zA-Z0-9]+"), "").trim()
        val tokens = textWithoutSymbols.split(Regex("[\\s,.:;\\-_/()]+")).filter { it.isNotBlank() }

        // Evaluamos tanto la cadena completa como tokens individuales y combinaciones
        val candidatesToEvaluate = mutableListOf<String>()
        candidatesToEvaluate.add(lower)
        if (lowerStripped.isNotBlank()) candidatesToEvaluate.add(lowerStripped)
        candidatesToEvaluate.add(textWithoutSymbols)
        if (textWithoutSymbolsStripped.isNotBlank()) candidatesToEvaluate.add(textWithoutSymbolsStripped)

        // Si hay varios tokens y el primero parece un icono/prefijo de elo (ej: "v carril central", "1 mid", "o duo")
        if (tokens.size >= 2) {
            candidatesToEvaluate.add(tokens.drop(1).joinToString(" "))
        }
        if (tokens.size >= 3) {
            candidatesToEvaluate.add(tokens.drop(2).joinToString(" "))
        }

        for (cand in candidatesToEvaluate) {
            // TOP
            if (cand.contains("calle del baron") || cand.contains("calle del barón") ||
                cand.contains("calle de baron") || cand.contains("calle de barón") ||
                cand.contains("calle baron") || cand.contains("calle barón") ||
                cand.contains("carril del baron") || cand.contains("carril del barón") ||
                cand.contains("carril de baron") || cand.contains("carril de barón") ||
                cand.contains("carril baron") || cand.contains("carril barón") ||
                cand.contains("linea del baron") || cand.contains("linea del barón") ||
                cand.contains("linea de baron") || cand.contains("linea de barón") ||
                cand.contains("linea baron") || cand.contains("linea barón") ||
                cand.contains("carril superior") || cand.contains("calle superior") ||
                cand.contains("linea superior") || cand.contains("carril solo") ||
                cand.contains("calle solo") || cand.contains("linea solo") ||
                cand.contains("baron lane") || cand.contains("rota de barao") ||
                cand.contains("rota do barao") || cand.contains("rota de barão") ||
                cand.contains("rota do barão") || cand.contains("rota do topo") ||
                cand.contains("rota solo") || cand.contains("solo lane") ||
                cand == "baron" || cand == "barao" || cand == "barão" || cand == "solo" || cand == "top") {
                return LaneRole.TOP
            }

            // MID
            if (cand.contains("calle central") || cand.contains("carril central") ||
                cand.contains("linea central") || cand.contains("calle medio") ||
                cand.contains("carril medio") || cand.contains("linea medio") ||
                cand.contains("calle de enmedio") || cand.contains("calle de en medio") ||
                cand.contains("linea de enmedio") || cand.contains("linea de en medio") ||
                cand.contains("carril de enmedio") || cand.contains("carril de en medio") ||
                cand.contains("mid lane") || cand.contains("rota do meio") ||
                cand.contains("rota central") || cand.contains("middle lane") ||
                cand == "central" || cand == "medio" || cand == "meio" || cand == "mid") {
                return LaneRole.MID
            }

            // ADC / DÚO
            if (cand.contains("calle del dragon") || cand.contains("calle del dragón") ||
                cand.contains("calle de dragon") || cand.contains("calle de dragón") ||
                cand.contains("calle dragon") || cand.contains("calle dragón") ||
                cand.contains("carril del dragon") || cand.contains("carril del dragón") ||
                cand.contains("carril de dragon") || cand.contains("carril de dragón") ||
                cand.contains("carril dragon") || cand.contains("carril dragón") ||
                cand.contains("linea del dragon") || cand.contains("linea del dragón") ||
                cand.contains("linea de dragon") || cand.contains("linea de dragón") ||
                cand.contains("linea dragon") || cand.contains("linea dragón") ||
                cand.contains("calle duo") || cand.contains("calle dúo") ||
                cand.contains("carril duo") || cand.contains("carril dúo") ||
                cand.contains("linea duo") || cand.contains("linea dúo") ||
                cand.contains("duo lane") || cand.contains("dragon lane") ||
                cand.contains("rota do dragao") || cand.contains("rota do dragão") ||
                cand.contains("rota duo") || cand.contains("carril bot") ||
                cand.contains("calle bot") || cand.contains("linea bot") ||
                cand.contains("bot lane") || cand == "duo" || cand == "dúo" || cand == "adc" || cand == "tirador" || cand == "bot") {
                return LaneRole.ADC
            }

            // SUPPORT / APOYO
            if (cand.contains("apoyo") || cand.contains("soporte") ||
                cand.contains("suporte") || cand.contains("support") ||
                cand.contains("rota suporte") || cand.contains("carril apoyo") ||
                cand.contains("linea apoyo") || cand.contains("carril soporte") ||
                cand.contains("linea soporte") || cand == "soporte" || cand == "suporte" || cand == "support" || cand == "apoyo" || cand == "sup") {
                return LaneRole.SUPPORT
            }

            // JUNGLA
            if (cand.contains("jungla") || cand.contains("jungle") ||
                cand.contains("cacador") || cand.contains("caçador") ||
                cand.contains("selva") || cand.contains("carril jungla") ||
                cand.contains("linea jungla") || cand.contains("rota selva") ||
                cand.contains("rota caçador") || cand.contains("rota cacador") ||
                cand == "jungla" || cand == "jungle" || cand == "cacador" || cand == "caçador" || cand == "selva" || cand == "jg") {
                return LaneRole.JUNGLE
            }
        }

        // Búsqueda por tokens individuales
        for (token in tokens) {
            when (token) {
                "baron", "barao", "barão", "top", "solo", "topo", "superior" -> return LaneRole.TOP
                "jungla", "jungle", "cacador", "caçador", "selva", "jg" -> return LaneRole.JUNGLE
                "mid", "medio", "meio", "central" -> return LaneRole.MID
                "adc", "duo", "dúo", "dragon", "dragón", "dragao", "dragão", "tirador", "atirador", "bot" -> return LaneRole.ADC
                "soporte", "support", "suporte", "sup", "supp", "apoyo" -> return LaneRole.SUPPORT
            }
        }

        // Búsqueda por prefijo pegado (ej: "vmid", "1mid", "omid", "vcentral", "vcarrilcentral", "vcalledelbaron", "ojungla", "osoporte", "vduo")
        val compact = textWithoutSymbols.replace(" ", "")
        val compactStripped = textWithoutSymbolsStripped.replace(" ", "")
        for (c in listOf(compactStripped, compact)) {
            when {
                c.endsWith("carrilcentral") || c.endsWith("lineacentral") || c.endsWith("callecentral") || c.endsWith("mid") || c.endsWith("central") || c.endsWith("medio") -> return LaneRole.MID
                c.endsWith("calledelbaron") || c.endsWith("calledebaron") || c.endsWith("callebaron") || c.endsWith("carrildebaron") || c.endsWith("carrildelbaron") || c.endsWith("carrilbaron") || c.endsWith("lineadelbaron") || c.endsWith("baron") || c.endsWith("solo") || c.endsWith("top") -> return LaneRole.TOP
                c.endsWith("carriljungla") || c.endsWith("callejungla") || c.endsWith("lineajungla") || c.endsWith("jungla") || c.endsWith("jungle") || c.endsWith("cacador") || c.endsWith("caçador") || c.endsWith("selva") -> return LaneRole.JUNGLE
                c.endsWith("calledeldragon") || c.endsWith("callededragon") || c.endsWith("calledragon") || c.endsWith("carrilduo") || c.endsWith("calleduo") || c.endsWith("carrildeldragon") || c.endsWith("duo") || c.endsWith("adc") || c.endsWith("tirador") || c.endsWith("dragon") -> return LaneRole.ADC
                c.endsWith("carrilsoporte") || c.endsWith("carrilapoyo") || c.endsWith("calleapoyo") || c.endsWith("callesoporte") || c.endsWith("soporte") || c.endsWith("suporte") || c.endsWith("support") || c.endsWith("apoyo") || c.endsWith("sup") -> return LaneRole.SUPPORT
            }
        }

        return null
    }

    data class ResolvedTeam(
        val assignments: Map<LaneRole, Champion>,
        val confidences: Map<LaneRole, Int>
    )

    /**
     * Valida y filtra el resultado de un escaneo de equipo para evitar duplicaciones,
     * roles colisionados y campeones inválidos, calculando probabilidades dinámicas de rol.
     */
    fun resolveTeamRolesDetailed(
        scannedSlots: List<ScannedSlotInfo>,
        allChampions: List<Champion>,
        auditList: MutableList<String>,
        isAllyTeam: Boolean = true
    ): ResolvedTeam {
        val standardRoles = listOf(LaneRole.TOP, LaneRole.JUNGLE, LaneRole.MID, LaneRole.ADC, LaneRole.SUPPORT)
        val finalMap = mutableMapOf<LaneRole, Champion>()
        val confidences = mutableMapOf<LaneRole, Int>()
        val availableRoles = standardRoles.toMutableList()
        val assignedChampionIds = mutableSetOf<String>()

        val validSlots = scannedSlots.filter { it.champion != null }
        val primaryRoleCounts = validSlots.groupBy { it.champion!!.primaryRole }.mapValues { it.value.size }
        val explicitlyAssignedRoles = mutableSetOf<LaneRole>()

        // 0. ASIGNACIÓN INICIAL POR HECHIZO CASTIGO (SMITE) -> ROL JUNGLA INEQUÍVOCO (Certeza 100%)
        // Si un slot aliado tiene Castigo (Smite), dicho slot es indiscutiblemente el Jungla del equipo.
        val allySlotWithSmite = if (isAllyTeam) {
            scannedSlots.find { s ->
                s.summonerSpells.any { it.equals("Castigo", ignoreCase = true) || it.equals("Smite", ignoreCase = true) }
            }
        } else null

        if (allySlotWithSmite != null) {
            allySlotWithSmite.explicitRole = LaneRole.JUNGLE
            val smiteChamp = allySlotWithSmite.champion
            if (smiteChamp != null && availableRoles.contains(LaneRole.JUNGLE)) {
                finalMap[LaneRole.JUNGLE] = smiteChamp
                confidences[LaneRole.JUNGLE] = 100
                availableRoles.remove(LaneRole.JUNGLE)
                assignedChampionIds.add(smiteChamp.id)
                allySlotWithSmite.assignedRole = LaneRole.JUNGLE
                explicitlyAssignedRoles.add(LaneRole.JUNGLE)
                auditList.add("Hechizo Castigo detectado: ${smiteChamp.name} -> JUNGLA (100% certeza)")
            }
        }

        // 1. ASIGNACIÓN POR ROL EXPLÍCITO DETECTADO EN EL SLOT (Certeza 100%)
        for (slot in validSlots) {
            val champ = slot.champion ?: continue
            val expRole = slot.explicitRole

            if (expRole != null && availableRoles.contains(expRole) && !assignedChampionIds.contains(champ.id)) {
                finalMap[expRole] = champ
                confidences[expRole] = 100
                availableRoles.remove(expRole)
                assignedChampionIds.add(champ.id)
                slot.assignedRole = expRole
                explicitlyAssignedRoles.add(expRole)
                if (champ.primaryRole != expRole) {
                    auditList.add("Rol explícito: ${champ.name} -> ${expRole.shortName} (100% certeza)")
                }
            }
        }

        // 2. ASIGNACIÓN POR HECHIZO CASTIGO (SMITE) -> ROL JUNGLA INEQUÍVOCO (Certeza 100%)
        if (isAllyTeam && availableRoles.contains(LaneRole.JUNGLE)) {
            for (slot in validSlots) {
                val champ = slot.champion ?: continue
                if (assignedChampionIds.contains(champ.id)) continue

                val hasSmite = slot.summonerSpells.any {
                    it.equals("Castigo", ignoreCase = true) || it.equals("Smite", ignoreCase = true)
                }
                if (hasSmite) {
                    finalMap[LaneRole.JUNGLE] = champ
                    confidences[LaneRole.JUNGLE] = 100
                    availableRoles.remove(LaneRole.JUNGLE)
                    assignedChampionIds.add(champ.id)
                    slot.assignedRole = LaneRole.JUNGLE
                    explicitlyAssignedRoles.add(LaneRole.JUNGLE)
                    auditList.add("Hechizo Castigo detectado: ${champ.name} -> JUNGLA (100% certeza)")
                    break
                }
            }
        }

        // 2.5 RESOLUCIÓN INTELIGENTE DE DUPLAS Y FLEX PICKS META (Wild Rift Pro Matchups)
        // Ejemplo: Jax + Malphite -> Jax TOP, Malphite SUPPORT/MID.
        // Ejemplo: Sett + Midlaner -> Sett TOP.
        // Ejemplo: Lux + Midlaner -> Lux SUPPORT.
        // Ejemplo: Yasuo + Malphite -> Yasuo MID, Malphite TOP/SUP.
        // Ejemplo: Volibear + Jax + Malphite -> Jax TOP, Volibear JUNGLE, Malphite SUPPORT.
        val unassignedValidSlots = validSlots.filter { !assignedChampionIds.contains(it.champion?.id) }
        val unassignedChampIds = unassignedValidSlots.mapNotNull { it.champion?.id }.toSet()

        if (unassignedChampIds.contains("jax") && unassignedChampIds.contains("malphite")) {
            val jaxSlot = unassignedValidSlots.find { it.champion?.id == "jax" }
            val malphiteSlot = unassignedValidSlots.find { it.champion?.id == "malphite" }
            if (jaxSlot != null && malphiteSlot != null) {
                if (availableRoles.contains(LaneRole.TOP)) {
                    finalMap[LaneRole.TOP] = jaxSlot.champion!!
                    confidences[LaneRole.TOP] = 95
                    availableRoles.remove(LaneRole.TOP)
                    assignedChampionIds.add("jax")
                    jaxSlot.assignedRole = LaneRole.TOP
                    auditList.add("Sinergia de Draft: Jax asignado a TOP (Barón)")
                }
                val malphRole = when {
                    availableRoles.contains(LaneRole.SUPPORT) -> LaneRole.SUPPORT
                    availableRoles.contains(LaneRole.MID) -> LaneRole.MID
                    availableRoles.contains(LaneRole.JUNGLE) -> LaneRole.JUNGLE
                    else -> availableRoles.firstOrNull()
                }
                if (malphRole != null) {
                    finalMap[malphRole] = malphiteSlot.champion!!
                    confidences[malphRole] = 90
                    availableRoles.remove(malphRole)
                    assignedChampionIds.add("malphite")
                    malphiteSlot.assignedRole = malphRole
                    auditList.add("Sinergia de Draft: Malphite flex adaptado a ${malphRole.shortName} junto a Jax TOP")
                }
            }
        }

        // Si Volibear está con Jax o Malphite, Volibear se prioriza como Jungla si está disponible
        if (unassignedChampIds.contains("volibear") && availableRoles.contains(LaneRole.JUNGLE)) {
            val voliSlot = unassignedValidSlots.find { it.champion?.id == "volibear" }
            if (voliSlot != null && !assignedChampionIds.contains("volibear")) {
                finalMap[LaneRole.JUNGLE] = voliSlot.champion!!
                confidences[LaneRole.JUNGLE] = 95
                availableRoles.remove(LaneRole.JUNGLE)
                assignedChampionIds.add("volibear")
                voliSlot.assignedRole = LaneRole.JUNGLE
                auditList.add("Sinergia de Draft: Volibear asignado a JUNGLA")
            }
        }

        // Si Lux está con otro mago de carril central (ej. Viktor, Veigar, Ahri, Syndra, Zed, Yasuo), Lux toma SUPPORT
        if (unassignedChampIds.contains("lux") && availableRoles.contains(LaneRole.SUPPORT)) {
            val hasOtherMid = unassignedValidSlots.any {
                it.champion?.id != "lux" && (it.champion?.primaryRole == LaneRole.MID || it.champion?.id in listOf("viktor", "ahri", "syndra", "zed", "yasuo", "vex", "katarina", "akali", "aurelionsol", "zoe"))
            }
            if (hasOtherMid) {
                val luxSlot = unassignedValidSlots.find { it.champion?.id == "lux" }
                if (luxSlot != null && !assignedChampionIds.contains("lux")) {
                    finalMap[LaneRole.SUPPORT] = luxSlot.champion!!
                    confidences[LaneRole.SUPPORT] = 90
                    availableRoles.remove(LaneRole.SUPPORT)
                    assignedChampionIds.add("lux")
                    luxSlot.assignedRole = LaneRole.SUPPORT
                    auditList.add("Sinergia de Draft: Lux flex adaptada a APOYO (con otro Midlaner presente)")
                }
            }
        }

        // 3. ASIGNACIÓN POR ROL PRIMARIO DEL CAMPEÓN
        for (slot in validSlots) {
            val champ = slot.champion ?: continue
            if (assignedChampionIds.contains(champ.id)) continue

            val primary = champ.primaryRole
            if (availableRoles.contains(primary)) {
                finalMap[primary] = champ
                val sharedPrimary = (primaryRoleCounts[primary] ?: 1) > 1
                confidences[primary] = if (sharedPrimary) 85 else 95

                availableRoles.remove(primary)
                assignedChampionIds.add(champ.id)
                slot.assignedRole = primary
            }
        }

        // 4. ASIGNACIÓN POR ROLES SECUNDARIOS (FLEX PICKS: Certeza 80%)
        for (slot in validSlots) {
            val champ = slot.champion ?: continue
            if (assignedChampionIds.contains(champ.id)) continue

            val secMatch = champ.secondaryRoles.firstOrNull { availableRoles.contains(it) }
            if (secMatch != null) {
                finalMap[secMatch] = champ
                confidences[secMatch] = 80
                availableRoles.remove(secMatch)
                assignedChampionIds.add(champ.id)
                slot.assignedRole = secMatch
                auditList.add("Flex pick: ${champ.name} adaptado a ${secMatch.shortName} (80% certeza)")
            }
        }

        // 5. ASIGNACIÓN DE ROLES RESTANTES POR MEJOR AFINIDAD
        for (slot in validSlots) {
            val champ = slot.champion ?: continue
            if (assignedChampionIds.contains(champ.id)) continue

            // Si el rol primario o alguno secundario ya fue tomado pero hay uno compatible disponible
            val bestRole = availableRoles.firstOrNull { r -> champ.primaryRole == r || champ.secondaryRoles.contains(r) }
                ?: availableRoles.firstOrNull()

            if (bestRole != null) {
                finalMap[bestRole] = champ
                confidences[bestRole] = 65
                availableRoles.remove(bestRole)
                assignedChampionIds.add(champ.id)
                slot.assignedRole = bestRole
            }
        }

        // 6. OPTIMIZACIÓN POST-ASIGNACIÓN (Maximizar afinidad sin tocar roles explícitos ni Castigo)
        var changed = true
        while (changed) {
            changed = false
            val assignedRoles = finalMap.keys.toList()
            for (i in 0 until assignedRoles.size) {
                for (j in i + 1 until assignedRoles.size) {
                    val roleA = assignedRoles[i]
                    val roleB = assignedRoles[j]

                    // No intercambiar roles que fueron detectados explícitamente por texto o Castigo
                    if (explicitlyAssignedRoles.contains(roleA) || explicitlyAssignedRoles.contains(roleB)) {
                        continue
                    }

                    val champA = finalMap[roleA]!!
                    val champB = finalMap[roleB]!!
                    
                    val slotA = validSlots.find { it.champion?.id == champA.id }
                    val slotB = validSlots.find { it.champion?.id == champB.id }
                    fun getSlotRole(idx: Int) = when (idx) {
                        0 -> LaneRole.TOP
                        1 -> LaneRole.JUNGLE
                        2 -> LaneRole.MID
                        3 -> LaneRole.ADC
                        4 -> LaneRole.SUPPORT
                        else -> null
                    }
                    val slotBonusA = if (slotA != null && getSlotRole(slotA.slotIndex) == roleA) 1 else 0
                    val slotBonusB = if (slotB != null && getSlotRole(slotB.slotIndex) == roleB) 1 else 0

                    val scoreA = (if (champA.primaryRole == roleA) 3 else if (champA.secondaryRoles.contains(roleA)) 1 else 0) + slotBonusA
                    val scoreB = (if (champB.primaryRole == roleB) 3 else if (champB.secondaryRoles.contains(roleB)) 1 else 0) + slotBonusB
                    val currentTotal = scoreA + scoreB
                    
                    val swappedSlotBonusA = if (slotA != null && getSlotRole(slotA.slotIndex) == roleB) 1 else 0
                    val swappedSlotBonusB = if (slotB != null && getSlotRole(slotB.slotIndex) == roleA) 1 else 0

                    val swappedScoreA = (if (champA.primaryRole == roleB) 3 else if (champA.secondaryRoles.contains(roleB)) 1 else 0) + swappedSlotBonusA
                    val swappedScoreB = (if (champB.primaryRole == roleA) 3 else if (champB.secondaryRoles.contains(roleA)) 1 else 0) + swappedSlotBonusB
                    val swappedTotal = swappedScoreA + swappedScoreB
                    
                    if (swappedTotal > currentTotal) {
                        finalMap[roleA] = champB
                        finalMap[roleB] = champA
                        confidences[roleA] = if (swappedScoreB >= 3) 95 else 80
                        confidences[roleB] = if (swappedScoreA >= 3) 95 else 80
                        slotA?.assignedRole = roleB
                        slotB?.assignedRole = roleA
                        changed = true
                        auditList.add("Heurística: Intercambio de $roleA (${champB.name}) y $roleB (${champA.name}) para optimizar afinidad y posición de slot.")
                    }
                }
            }
        }

        return ResolvedTeam(finalMap, confidences)
    }

    /**
     * Compatibilidad estándar que devuelve únicamente el mapa de roles asignados.
     */
    fun resolveTeamRoles(
        scannedSlots: List<ScannedSlotInfo>,
        allChampions: List<Champion>,
        auditList: MutableList<String>
    ): Map<LaneRole, Champion> {
        return resolveTeamRolesDetailed(scannedSlots, allChampions, auditList).assignments
    }
}
