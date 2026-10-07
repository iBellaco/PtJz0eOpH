package com.example.util

import com.example.data.SituationalItemAdvisor
import com.example.data.WildRiftItemsData
import com.example.data.WildRiftSpellsAndRunes
import java.text.Normalizer
import java.util.Locale

/** Keep build-specific advice separate from immutable catalog descriptions. */
object BuildElementAdvice {
    fun isFlash(name: String): Boolean = key(name) in setOf("destello", "flash")

    private fun key(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase(Locale.ROOT)
            .replace("[^a-z0-9]+".toRegex(), " ")
            .trim()

    fun resolve(name: String, entries: List<Pair<String, String>>, fallback: String, catalogDescription: String = ""): String {
        if (isFlash(name)) return ""
        fun compact(value: String) = key(value).replace(" ", "")
        val wanted = compact(name)
        return entries.firstOrNull { compact(it.first) == wanted && it.second.isNotBlank() }
            ?.second?.trim()?.takeUnless { it == catalogDescription.trim() } ?: fallback.trim()
    }

    /**
     * Generates advice for the tapped item instead of repeating the generic champion plan.
     * Situational items always name the trigger and concrete enemy examples.
     */
    fun contextualItemAdvice(
        itemName: String,
        championName: String,
        roleName: String,
        language: String,
        situational: Boolean
    ): String {
        val lang = AppLanguage.normalize(language)
        val pt = lang == "pt"
        val catalog = WildRiftItemsData.getItemByName(itemName)
        val strategic = SituationalItemAdvisor.getAdvice(itemName, lang)
        val localizedName = catalog?.getLocalizedName(lang) ?: strategic.name
        val catalogTip = catalog?.getLocalizedCoachTip(lang).orEmpty()
        val purpose = strategic.purpose.takeIf { it.isNotBlank() }
            ?: catalog?.getLocalizedPassive(lang).orEmpty().substringBefore("\n")
        val against = strategic.bestAgainst
            .filterNot {
                it.contains("Composiciones rivales especializadas", ignoreCase = true) ||
                    it.contains("Amenazas prioritarias", ignoreCase = true)
            }
            .take(8)
            .joinToString(", ")
        val trigger = strategic.recommendationTip.takeIf {
            it.isNotBlank() && !it.contains("según el estado de la partida", ignoreCase = true)
        }.orEmpty()
        val coreHint = coreItemMatchupHint(itemName, pt)

        return if (situational) {
            if (pt) buildString {
                appendLine("Quando usar $localizedName:")
                appendLine(trigger.ifBlank { catalogTip.ifBlank { purpose } })
                if (against.isNotBlank()) appendLine("\nContra quais campeões/composições:\n$against")
                appendLine("\nPor que funciona com $championName ($roleName):")
                appendLine(catalogTip.ifBlank { purpose })
                append("\nRegra de compra: não substitua o núcleo por padrão; troque um item apenas quando essa ameaça for uma das condições principais da partida.")
            } else buildString {
                appendLine("Cuándo usar $localizedName:")
                appendLine(trigger.ifBlank { catalogTip.ifBlank { purpose } })
                if (against.isNotBlank()) appendLine("\nContra qué campeones/composiciones:\n$against")
                appendLine("\nPor qué funciona con $championName ($roleName):")
                appendLine(catalogTip.ifBlank { purpose })
                append("\nRegla de compra: no reemplaces el core por defecto; cambia un objeto solo cuando esa amenaza sea una de las condiciones principales de la partida.")
            }
        } else {
            if (pt) buildString {
                appendLine("Por que $localizedName é núcleo para $championName ($roleName):")
                appendLine(catalogTip.ifBlank { purpose })
                appendLine("\nQuando completar:")
                appendLine(coreItemTimingHint(itemName, pt))
                val matchup = against.ifBlank { coreHint }
                if (matchup.isNotBlank()) append("\nPartidas em que rende mais:\n$matchup")
            } else buildString {
                appendLine("Por qué $localizedName es core para $championName ($roleName):")
                appendLine(catalogTip.ifBlank { purpose })
                appendLine("\nCuándo completarlo:")
                appendLine(coreItemTimingHint(itemName, pt))
                val matchup = against.ifBlank { coreHint }
                if (matchup.isNotBlank()) append("\nPartidas donde rinde más:\n$matchup")
            }
        }.trim()
    }


    fun contextualBootAdvice(
        bootName: String,
        championName: String,
        roleName: String,
        language: String,
        situational: Boolean
    ): String {
        val lang = AppLanguage.normalize(language)
        val pt = lang == "pt"
        val catalog = WildRiftItemsData.getItemByName(bootName)
        val localizedName = catalog?.getLocalizedName(lang) ?: bootName
        val condition = bootScenario(bootName, pt)
        val mechanic = catalog?.getLocalizedCoachTip(lang)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: catalog?.getLocalizedPassive(lang)?.substringBefore("\n")?.trim().orEmpty()

        return if (pt) buildString {
            appendLine("Por que $localizedName nesta build de $championName ($roleName):")
            if (mechanic.isNotBlank()) appendLine(mechanic)
            appendLine("\nQuando escolher:")
            appendLine(condition)
            if (situational) append("\nTroca situacional: use esta bota quando a ameaça descrita for mais importante que o plano padrão de botas da build.")
        } else buildString {
            appendLine("Por qué $localizedName en esta build de $championName ($roleName):")
            if (mechanic.isNotBlank()) appendLine(mechanic)
            appendLine("\nCuándo elegirla:")
            appendLine(condition)
            if (situational) append("\nCambio situacional: úsala cuando la amenaza descrita sea más importante que el plan de botas predeterminado de la build.")
        }.trim()
    }

    fun contextualRuneAdvice(
        runeName: String,
        championName: String,
        roleName: String,
        language: String,
        situational: Boolean
    ): String {
        val lang = AppLanguage.normalize(language)
        val pt = lang == "pt"
        val rune = WildRiftSpellsAndRunes.getRuneByName(runeName)
        val localizedName = rune?.getLocalizedName(lang) ?: runeName
        val description = rune?.getLocalizedDescription(lang)
            ?.substringBefore("\n")
            ?.trim()
            .orEmpty()
        val scenario = runeScenario(runeName, pt)

        return if (pt) buildString {
            appendLine("Função de $localizedName para $championName ($roleName):")
            appendLine(description.ifBlank { "Esta runa reforça uma condição específica da build." })
            appendLine("\nQuando usar:")
            appendLine(scenario)
            if (situational) append("\nTroque uma runa principal por esta somente quando essa condição realmente aparecer na partida.")
        } else buildString {
            appendLine("Función de $localizedName para $championName ($roleName):")
            appendLine(description.ifBlank { "Esta runa refuerza una condición concreta de la build." })
            appendLine("\nCuándo usarla:")
            appendLine(scenario)
            if (situational) append("\nCámbiala por una runa principal solo cuando esa condición realmente aparezca en la partida.")
        }.trim()
    }

    fun contextualSpellAdvice(
        spellName: String,
        championName: String,
        roleName: String,
        language: String
    ): String {
        if (isFlash(spellName)) return ""
        val lang = AppLanguage.normalize(language)
        val pt = lang == "pt"
        val spell = WildRiftSpellsAndRunes.getSpellByName(spellName)
        val localizedName = spell?.getLocalizedName(lang) ?: spellName
        val description = SpellCatalogFormatting.split(spell?.getLocalizedDescription(lang).orEmpty(), lang)
            .description.substringBefore("\n").trim()
        val scenario = spellScenario(spellName, pt)

        return if (pt) buildString {
            appendLine("Quando usar $localizedName com $championName ($roleName):")
            appendLine(scenario)
            if (description.isNotBlank()) append("\nO que o feitiço oferece:\n$description")
        } else buildString {
            appendLine("Cuándo usar $localizedName con $championName ($roleName):")
            appendLine(scenario)
            if (description.isNotBlank()) append("\nQué aporta el hechizo:\n$description")
        }.trim()
    }

    private fun coreItemTimingHint(name: String, pt: Boolean): String {
        val n = key(name)
        fun pick(es: String, br: String) = if (pt) br else es
        return when {
            "abrazo del serafin" in n -> pick("Complétalo tras acumular el maná del objeto previo; el pico real llega al transformarse, cuando conviertes maná en poder y obtienes el escudo defensivo.", "Complete depois de acumular o mana do item anterior; o pico real chega na transformação, quando o mana vira poder e você obtém o escudo defensivo.")
            "arcoescudo inmortal" in n -> pick("Priorízalo como pico temprano o medio cuando eres un carry de crítico que necesita sobrevivir al primer burst sin renunciar a daño.", "Priorize como pico inicial ou médio quando você é um carry crítico que precisa sobreviver ao primeiro burst sem abrir mão de dano.")
            "baile de la muerte" in n -> pick("Constrúyelo después de tu primer pico ofensivo cuando debes entrar en rango de daño físico explosivo y necesitas convertir esa ráfaga en daño diferido.", "Construa depois do primeiro pico ofensivo quando precisa entrar no alcance de dano físico explosivo e transformar a rajada em dano diferido.")
            "calibrador de sterak" in n -> pick("Complétalo después de acumular vida y daño base suficientes, antes de las peleas donde vas a recibir foco al entrar como luchador.", "Complete depois de acumular Vida e dano base suficientes, antes das lutas em que você receberá foco ao entrar como lutador.")
            "shurelya" in n -> pick("Priorízala cuando tu equipo necesita una ventana clara de entrada o retirada y puedes activar su utilidad antes de una pelea por objetivo.", "Priorize quando sua equipe precisa de uma janela clara de entrada ou retirada e você pode ativar a utilidade antes de uma luta por objetivo.")
            "canon de fuego rapido" in n -> pick("Añádelo después de establecer tu base de crítico/velocidad de ataque cuando el alcance energizado te permita golpear primero sin exponerte.", "Adicione depois de estabelecer sua base de crítico/velocidade de ataque quando o alcance energizado permitir atacar primeiro sem se expor.")
            "cetro de cristal de rylai" in n -> pick("Complétalo cuando ya puedes aplicar daño de habilidad repetido y necesitas que cada impacto ayude a mantener al rival dentro de tu zona de daño.", "Complete quando já consegue aplicar dano de habilidade repetido e precisa que cada acerto mantenha o rival dentro da sua zona de dano.")
            "convergencia de zeke" in n -> pick("Constrúyela cuando tu H4/Ulti inicia o acompaña peleas y ya tienes un aliado de daño fiable que puede aprovechar tu entrada.", "Construa quando sua H4/Ulti inicia ou acompanha lutas e já existe um aliado de dano confiável para aproveitar sua entrada.")
            "coraza del muerto" in n -> pick("Añádela tras la defensa básica cuando el siguiente problema sea llegar antes a una escaramuza, flanquear o pegarte a un objetivo móvil.", "Adicione após a defesa básica quando o problema passa a ser chegar antes à escaramuça, flanquear ou alcançar um alvo móvel.")
            "coraza dual purpurea" in n -> pick("Déjala para el tramo medio/tardío, cuando ya tienes vida suficiente y las peleas largas contra daño mixto permiten cargar sus resistencias.", "Deixe para o meio/fim da partida, quando já há Vida suficiente e lutas longas contra dano misto permitem acumular suas resistências.")
            "corazon de acero" in n -> pick("Si va a ser core, cómpralo temprano: necesita tiempo y contactos repetidos con campeones para convertir sus acumulaciones en vida real durante la partida.", "Se for núcleo, compre cedo: ele precisa de tempo e contatos repetidos com campeões para transformar acúmulos em Vida ao longo da partida.")
            "corazon de hielo" in n -> pick("Complétalo después de tu primera capa de supervivencia cuando dos o más amenazas dependen de ataques rápidos y permanecerás cerca de ellas.", "Complete depois da primeira camada de sobrevivência quando duas ou mais ameaças dependem de ataques rápidos e você ficará perto delas.")
            "corona de la reina fragmentada" in n -> pick("Priorízala antes de entrar en peleas decisivas contra burst o iniciación difícil de esquivar; conserva el escudo evitando poke innecesario antes del combate.", "Priorize antes de lutas decisivas contra burst ou iniciação difícil de evitar; preserve o escudo evitando poke desnecessário antes do combate.")
            "creagrietas" in n || "riftmaker" in n -> pick("Complétalo cuando tu campeón puede permanecer varios segundos pegando habilidades o ataques; pierde valor si la pelea se decide en una sola rotación.", "Complete quando seu campeão consegue permanecer vários segundos causando dano; perde valor se a luta termina em uma única rotação.")
            "cuchilla negra" in n -> pick("Priorízala temprano en luchadores AD que golpean varias veces, especialmente cuando tu equipo también aprovecha la reducción progresiva de armadura.", "Priorize cedo em lutadores AD que acertam várias vezes, especialmente quando sua equipe também aproveita a redução progressiva de Armadura.")
            "diente de nashor" in n -> pick("Cómpralo temprano en campeones AP cuyo DPS depende de alternar habilidades con ataques básicos; retrásalo si no puedes mantener autos.", "Compre cedo em campeões AP cujo DPS depende de alternar habilidades com ataques básicos; adie se não conseguir manter ataques.")
            "eclipse" in n -> pick("Priorízalo como pico temprano para intercambios cortos de dos impactos contra objetivos a los que puedes entrar y salir sin prolongar demasiado la pelea.", "Priorize como pico inicial para trocas curtas de dois acertos contra alvos nos quais você consegue entrar e sair sem prolongar demais a luta.")
            "eco armonico" in n -> pick("Complétalo pronto en encantadores que curan o escudan con frecuencia; cuanto antes empiezan las peleas agrupadas, antes rentabilizas sus rebotes.", "Complete cedo em encantadores que curam ou dão escudo com frequência; quanto antes começarem as lutas agrupadas, mais cedo os saltos rendem valor.")
            "eco de luden" in n || "luden" in n -> pick("Priorízalo temprano cuando tu plan es poke, limpieza de oleada y ráfaga corta contra objetivos frágiles.", "Priorize cedo quando seu plano é poke, limpeza de onda e explosão curta contra alvos frágeis.")
            "egida de fuego solar" in n -> pick("Priorízala temprano en tanques que permanecen cuerpo a cuerpo: acelera limpieza y convierte el tiempo dentro de la pelea en daño constante.", "Priorize cedo em tanques que permanecem corpo a corpo: acelera a limpeza e transforma o tempo dentro da luta em dano constante.")
            n == "el final" || "terminus" in n -> pick("Complétalo después de conseguir velocidad de ataque suficiente; necesita autos continuos para acumular sus bonificaciones y no es un pico de ráfaga instantánea.", "Complete depois de obter Velocidade de Ataque suficiente; exige ataques contínuos para acumular bônus e não é um pico de burst instantâneo.")
            "escudo reliquia" in n -> pick("Es compra inicial de Soporte tanque/iniciador: empieza la misión desde la primera oleada para no retrasar su evolución ni la economía compartida.", "É compra inicial de Suporte tanque/iniciador: comece a missão desde a primeira onda para não atrasar a evolução nem a economia compartilhada.")
            "filo de la noche" in n -> pick("Cómpralo antes de la pelea donde una sola habilidad de control puede cortar tu entrada; llega con el escudo disponible y no lo regales al poke.", "Compre antes da luta em que uma única habilidade de controle pode interromper sua entrada; chegue com o escudo disponível e não o perca para poke.")
            "filo fantasmal de youmuu" in n || "youmuu" in n -> pick("Priorízalo temprano cuando tu ventaja depende de rotar, flanquear y llegar primero a objetivos frágiles antes de que acumulen armadura.", "Priorize cedo quando sua vantagem depende de rotacionar, flanquear e chegar primeiro aos alvos frágeis antes que acumulem Armadura.")
            "filo infinito" in n -> pick("Complétalo cuando ya tienes suficiente probabilidad de crítico para multiplicar un patrón de daño que realmente puede criticar; no lo adelantes sin esa base.", "Complete quando já houver chance de crítico suficiente para multiplicar um padrão de dano que realmente possa critar; não antecipe sem essa base.")
            "filoscuro de draktharr" in n || "draktharr" in n -> pick("Priorízalo en el primer tramo de la build si tu plan es aislar objetivos frágiles desde niebla o flanco; pierde prioridad contra armadura temprana.", "Priorize no início da build se o plano é isolar alvos frágeis pela névoa ou flanco; perde prioridade contra Armadura cedo.")
            "filoveloz de navori" in n || "navori" in n -> pick("Añádelo después de una base de crítico cuando tus habilidades básicas son parte central del DPS y necesitas recuperarlas entre ataques.", "Adicione depois de uma base de crítico quando suas habilidades básicas são parte central do DPS e precisam voltar entre ataques.")
            "final del ingenio" in n -> pick("Priorízalo como segundo/tercer pico cuando puedes autoatacar de forma sostenida y el daño mágico rival justifica convertir una compra ofensiva en resistencia.", "Priorize como segundo/terceiro pico quando consegue atacar continuamente e o dano mágico inimigo justifica transformar uma compra ofensiva em resistência.")
            "flechas de los yun tal" in n || "yun tal" in n -> pick("Priorízalo temprano en tiradores de críticos que pueden atacar de forma continua; su valor crece cuando activas repetidamente el pico de velocidad de ataque.", "Priorize cedo em atiradores críticos que conseguem atacar continuamente; o valor cresce ao ativar repetidamente o pico de Velocidade de Ataque.")
            "fuerza de la naturaleza" in n -> pick("Constrúyela cuando el daño mágico llega en impactos repetidos o quemaduras; dale tiempo para acumularse antes de medir su resistencia real.", "Construa quando o dano mágico chega em impactos repetidos ou queimaduras; dê tempo para acumular antes de avaliar a resistência real.")
            "fuerza de trinidad" in n || "trinity" in n -> pick("Complétala cuando puedes alternar habilidades y ataques básicos constantemente para aprovechar cada activación de Espada Hechizada.", "Complete quando consegue alternar habilidades e ataques básicos constantemente para aproveitar cada ativação de Lâmina Mágica.")
            "guantelete de hielo" in n -> pick("Priorízalo en un luchador/tanque que activa Espada Hechizada con frecuencia y necesita pegarse a amenazas físicas mediante la zona de ralentización.", "Priorize em lutador/tanque que ativa Lâmina Mágica com frequência e precisa ficar colado em ameaças físicas usando a zona de lentidão.")
            "hoja del rey arruinado" in n || "rei destruido" in n || "ruined" in n -> pick("Priorízala contra luchadores y tanques de mucha vida cuando puedes mantener ataques básicos; pierde valor si no alcanzas a golpear de forma sostenida.", "Priorize contra lutadores e tanques de muita Vida quando consegue manter ataques básicos; perde valor se não conseguir atacar continuamente.")
            "hoz espectral" in n -> pick("Es compra inicial de Soporte de poke: empieza a generar oro desde la primera fase de línea y evita retrasar su misión con juego demasiado pasivo.", "É compra inicial de Suporte de poke: comece a gerar ouro desde a fase de rotas e evite atrasar a missão jogando passivamente demais.")
            "huracan de runaan" in n || "runaan" in n -> pick("Añádelo después de tu primer pico de daño/velocidad cuando puedes golpear varios objetivos y tus efectos al impacto o críticos se benefician de los proyectiles extra.", "Adicione depois do primeiro pico de dano/velocidade quando consegue atingir vários alvos e seus efeitos ao contato ou críticos aproveitam os projéteis extras.")
            "incensario ardiente" in n -> pick("Complétalo cuando curas o escudas repetidamente y tu carry principal realmente convierte velocidad de ataque y daño al impacto en DPS.", "Complete quando cura ou dá escudo repetidamente e seu carry principal realmente converte Velocidade de Ataque e dano ao contato em DPS.")
            "malla de espinas" in n -> pick("Priorízala cuando los atacantes físicos también dependen de curación; termina el objeto cuando necesitas armadura y anti-curación sostenida, no solo una respuesta puntual.", "Priorize quando atacantes físicos também dependem de cura; finalize quando precisa de Armadura e anti-cura contínua, não apenas resposta pontual.")
            "manamune" in n || "muramana" in n -> pick("Compra temprano el componente de maná para acumular; el pico llega al completar la transformación y convertir esa reserva en daño.", "Compre cedo o componente de Mana para acumular; o pico chega ao concluir a transformação e converter essa reserva em dano.")
            "mandato imperial" in n -> pick("Complétalo cuando aplicas ralentizaciones o inmovilizaciones con frecuencia y tus aliados pueden consumir la marca durante escaramuzas agrupadas.", "Complete quando aplica lentidões ou imobilizações com frequência e seus aliados conseguem consumir a marca em escaramuças agrupadas.")
            "morellonomicon" in n || "morellonomicon" in n -> pick("Adelántalo solo cuando la curación rival ya decide intercambios o peleas; si no existe esa amenaza, prioriza primero tu pico de daño principal.", "Antecipe apenas quando a cura inimiga já decide trocas ou lutas; sem essa ameaça, priorize primeiro seu pico principal de dano.")
            "orbe infinito" in n -> pick("Complétalo después de tu primer pico de poder cuando tu patrón baja objetivos a vida de ejecución y puedes convertir ese umbral en remates consistentes.", "Complete depois do primeiro pico de poder quando seu padrão deixa alvos em Vida de execução e você consegue converter esse limite em abates consistentes.")
            "perdicion del liche" in n || "lich" in n -> pick("Priorízala después de reunir AP suficiente si puedes intercalar un ataque básico tras cada habilidad; no la fuerces si tu patrón no entra a rango de auto.", "Priorize depois de reunir AP suficiente se consegue intercalar um ataque básico após cada habilidade; não force se seu padrão não entra no alcance de ataque.")
            "promesa de caballero" in n -> pick("Constrúyela cuando ya está claro qué aliado es tu condición de victoria y puedes permanecer cerca de él para absorber presión durante las peleas.", "Construa quando já estiver claro qual aliado é sua condição de vitória e você consegue permanecer perto dele para absorver pressão nas lutas.")
            "recaudadora" in n -> pick("Priorízala temprano/medio contra composiciones frágiles cuando letalidad, crítico y umbral de ejecución aceleran tus remates; retrásala frente a mucha armadura.", "Priorize cedo/meio contra composições frágeis quando Letalidade, crítico e execução aceleram abates; adie contra muita Armadura.")
            "recuerdos de lord dominik" in n || "lord dominik" in n -> pick("Complétalo cuando la primera línea rival ya está acumulando vida y armadura; cuanto más defensiva sea su compra, más urgente se vuelve tu penetración.", "Complete quando a linha de frente inimiga já acumula Vida e Armadura; quanto mais defensiva a compra deles, mais urgente fica sua penetração.")
            "rencor de serylda" in n || "serylda" in n -> pick("Complétalo cuando necesitas penetración de armadura y tus habilidades pueden aprovechar la ralentización para mantener distancia o perseguir; no lo compres por anti-curación.", "Complete quando precisa de penetração de Armadura e suas habilidades aproveitam a lentidão para manter distância ou perseguir; não compre por anti-cura.")
            "rompecascos" in n -> pick("Cómpralo antes de una ventana real de presión lateral, cuando tu plan es obligar al rival a responderte lejos del siguiente objetivo; pierde valor si vas a agruparte siempre.", "Compre antes de uma janela real de pressão lateral, quando o plano é obrigar o rival a responder longe do próximo objetivo; perde valor se você sempre agrupar.")
            "sanguinaria" in n -> pick("Añádela tras asegurar tu daño base cuando necesitas sostén para mantenerte en mapa y llegar a la siguiente pelea con vida sin depender de regresar.", "Adicione depois de garantir o dano base quando precisa de sustentação para permanecer no mapa e chegar à próxima luta com Vida sem depender de retorno.")
            "segador de esencia" in n -> pick("Priorízalo temprano en tiradores que gastan maná y rotan habilidades entre ataques; su valor cae si el campeón apenas usa habilidades para hacer DPS.", "Priorize cedo em atiradores que gastam Mana e alternam habilidades entre ataques; perde valor se o campeão quase não usa habilidades para causar DPS.")
            "sombrero mortal de rabadon" in n || "rabadon" in n -> pick("Complétalo después de tener una base sólida de poder de habilidad; rinde mucho más como multiplicador del AP acumulado que como primera compra.", "Complete depois de já ter uma base sólida de Poder de Habilidade; rende muito mais como multiplicador do AP acumulado do que como primeira compra.")
            "tormento de liandry" in n || "liandry" in n -> pick("Priorízalo cuando las peleas se alargan y el rival acumula mucha vida; necesitas tiempo de contacto para rentabilizar la quemadura.", "Priorize quando as lutas se prolongam e o rival acumula muita Vida; é preciso tempo de contato para rentabilizar a queimadura.")
            "vara de las edades" in n -> pick("Si forma parte del core, cómprala lo antes posible: necesita minutos para acumularse y alcanzar su pico antes de las peleas tardías.", "Se fizer parte do núcleo, compre o mais cedo possível: precisa de minutos para acumular e atingir o pico antes das lutas tardias.")
            "verdugo de krakens" in n || "kraken" in n -> pick("Priorízalo cuando puedes mantener secuencias de ataques y activar repetidamente el tercer golpe; es mejor contra objetivos que no puedes borrar en una sola ráfaga.", "Priorize quando consegue manter sequências de ataques e ativar repetidamente o terceiro acerto; é melhor contra alvos que não caem em uma única rajada.")
            else -> pick("Complétalo cuando el efecto descrito del objeto sea una condición activa de tu patrón de daño, supervivencia o utilidad; si no puedes activar esa condición, retrasa la compra.", "Complete quando o efeito descrito do item for uma condição ativa do seu padrão de dano, sobrevivência ou utilidade; se não conseguir ativá-lo, adie a compra.")
        }
    }

    private fun coreItemMatchupHint(name: String, pt: Boolean): String {
        val n = key(name)
        return when {
            "rabadon" in n -> if (pt) "Contra composições frágeis ou quando você já está à frente e precisa converter AP em dano/escudos maiores." else "Contra composiciones frágiles o cuando ya vas por delante y necesitas convertir el AP acumulado en mucho más daño/escudos."
            "luden" in n -> if (pt) "Bom contra carries frágeis e equipes que permitem poke: Lux, Jinx, Caitlyn, Ziggs." else "Bueno contra carries frágiles y equipos que permiten poke: Lux, Jinx, Caitlyn, Ziggs."
            "liandry" in n -> if (pt) "Especialmente contra Ornn, Sion, Dr. Mundo, Maokai e outros alvos de muita vida." else "Especialmente contra Ornn, Sion, Dr. Mundo, Maokai y otros objetivos de mucha vida."
            "infinito" in n || "infinity" in n -> if (pt) "Rende quando você já tem chance crítica suficiente e precisa explodir carries de baixa/média armadura." else "Rinde cuando ya tienes suficiente crítico y necesitas bajar carries de armadura baja/media."
            "rei destruido" in n || "ruined" in n -> if (pt) "Forte contra Sion, Ornn, Dr. Mundo e bruisers que acumulam vida." else "Fuerte contra Sion, Ornn, Dr. Mundo y bruisers que acumulan vida."
            else -> ""
        }
    }

    private fun runeScenario(name: String, pt: Boolean): String {
        val n = key(name)
        fun pick(es: String, br: String) = if (pt) br else es
        return when {
            n == "aery" -> pick("Para hostigamiento constante o para campeones que curan/escudan aliados; elige intercambios frecuentes donde Aery pueda salir y volver varias veces.", "Para poke constante ou campeões que curam/dão escudo; escolha trocas frequentes em que Aery consiga sair e voltar várias vezes.")
            "banda de mana" in n -> pick("En campeones con maná que pueden golpear habilidades o ataques potenciados durante la línea; empieza a acumularla temprano para llegar al objetivo de maná antes de las peleas clave.", "Em campeões com Mana que conseguem acertar habilidades ou ataques fortalecidos na rota; comece a acumular cedo para chegar ao limite antes das lutas-chave.")
            n == "brutal" -> pick("Para presión temprana con ataques frecuentes sobre campeones; rinde cuando puedes convertir cada intercambio corto en daño adicional desde los primeros niveles.", "Para pressão inicial com ataques frequentes em campeões; rende quando você transforma cada troca curta em dano extra desde os primeiros níveis.")
            "capa del nimbo" in n -> pick("Cuando un hechizo de invocador marca tu entrada o salida: úsala para cerrar distancia tras Prender/Aplastar o escapar después de gastar una herramienta defensiva.", "Quando um feitiço de invocador marca sua entrada ou saída: use para encurtar distância após Incendiar/Golpear ou escapar depois de gastar uma ferramenta defensiva.")
            n == "celeridad" -> pick("En campeones que ya reciben velocidad de movimiento de habilidades, objetos o Fantasmal y convierten ese extra en kiteo, persecución o rotaciones.", "Em campeões que já recebem Velocidade de Movimento de habilidades, itens ou Fantasma e convertem esse bônus em kite, perseguição ou rotações.")
            "coleccion de globos oculares" in n -> pick("Cuando tu plan busca participar pronto en eliminaciones de campeones o monstruos épicos y convertir esas participaciones en poder adaptable para el juego medio.", "Quando o plano busca participar cedo de abates de campeões ou monstros épicos e converter essas participações em força adaptativa para o meio da partida.")
            "cometa" in n -> pick("En líneas de poke donde impactas habilidades a distancia con frecuencia; funciona mejor contra objetivos de movilidad limitada como Lux, Orianna o Ziggs, a los que puedes volver a acertar cuando el cometa esté disponible.", "Em rotas de poke onde você acerta habilidades à distância com frequência; funciona melhor contra alvos de pouca mobilidade como Lux, Orianna ou Ziggs, que podem ser atingidos novamente quando o cometa estiver disponível.")
            "compas letal" in n -> pick("En carries de autoataques que pueden permanecer pegando el tiempo suficiente para alcanzar seis acumulaciones; priorízalo contra frentes que no te obligan a salir de rango de inmediato.", "Em carries de ataques básicos que conseguem permanecer atacando até seis acúmulos; priorize contra linhas de frente que não obriguem você a sair do alcance imediatamente.")
            "conquistador" in n -> pick("En peleas prolongadas donde puedes encadenar ataques y habilidades distintos hasta seis acumulaciones; destaca contra luchadores y tanques que no caen en una sola rotación.", "Em lutas prolongadas onde você encadeia ataques e habilidades diferentes até seis acúmulos; destaca contra lutadores e tanques que não caem em uma única rotação.")
            "cosecha oscura" in n -> pick("Cuando puedes tocar repetidamente rivales por debajo del 50% de vida y participar en remates; escala mejor en partidas con muchas escaramuzas que en líneas sin acción.", "Quando consegue atingir repetidamente rivais abaixo de 50% de Vida e participar de finalizações; escala melhor em partidas com muitas escaramuças do que em rotas sem ação.")
            "demoler" in n -> pick("Cuando puedes ganar prioridad de línea, cargar junto a la torreta y convertir una ventana sin rival en daño estructural; no aporta si nunca puedes quedarte pegando a torre.", "Quando consegue prioridade de rota, carregar perto da torre e converter uma janela sem rival em dano estrutural; não rende se nunca puder ficar batendo na torre.")
            "electrocut" in n -> pick("Cuando tu combo puede conectar tres impactos rápidos y retirarse; úsala para burst sobre objetivos frágiles, no para peleas donde tardas demasiado en activar el tercer golpe.", "Quando seu combo conecta três acertos rápidos e sai; use para burst em alvos frágeis, não em lutas onde demora demais para ativar o terceiro acerto.")
            "fortalecimiento" in n -> pick("En campeones que pueden encadenar tres ataques sobre el mismo objetivo y seguir peleando después; activa primero los tres golpes y aprovecha la amplificación del 8% mientras continúe el combate.", "Em campeões que conseguem encadear três ataques no mesmo alvo e continuar lutando; ative os três golpes e aproveite a amplificação de 8% enquanto o combate continuar.")
            "fuente de vida" in n -> pick("En dúo o composiciones agrupadas donde golpeas campeones con frecuencia y casi siempre tienes un aliado herido cerca; evita elegirla si vas a jugar aislado gran parte de la partida.", "Em rota dupla ou composições agrupadas onde você acerta campeões com frequência e quase sempre há um aliado ferido por perto; evite se for jogar isolado grande parte da partida.")
            "fuerzas renovadas" in n -> pick("Contra poke repetido en línea: absorbe un impacto, retrocede y deja trabajar la regeneración antes del siguiente intercambio en vez de encadenar trades sin pausa.", "Contra poke repetido na rota: absorva um acerto, recue e deixe a regeneração trabalhar antes da próxima troca, em vez de encadear trocas sem pausa.")
            "garras del inmortal" in n -> pick("En líneas donde puedes permanecer en combate tres segundos y después tocar al rival con un ataque potenciado; es especialmente eficiente en campeones resistentes cuerpo a cuerpo.", "Em rotas onde consegue ficar em combate por três segundos e depois tocar o rival com um ataque fortalecido; é especialmente eficiente em campeões resistentes corpo a corpo.")
            "golpe bajo" in n -> pick("Cuando tu kit ralentiza o inmoviliza antes de tu daño principal; ordena el combo para aplicar primero el control y después consumir el daño verdadero adicional.", "Quando seu kit desacelera ou imobiliza antes do dano principal; ordene o combo para aplicar primeiro o controle e depois aproveitar o dano verdadeiro extra.")
            "golpe de gracia" in n -> pick("Cuando tu campeón puede bajar objetivos por debajo del 40% y necesita convertir esa ventana en ejecución antes de que escapen, se curen o reciban protección.", "Quando seu campeão consegue deixar alvos abaixo de 40% e precisa transformar essa janela em execução antes que escapem, curem ou recebam proteção.")
            "guardian" in n || "guardian" in n -> pick("Cuando tu prioridad es proteger a un carry frente a burst o engage: mantente dentro del radio o usa una habilidad sobre él antes de recibir la ráfaga.", "Quando sua prioridade é proteger um carry contra burst ou engage: permaneça no raio ou use uma habilidade nele antes de receber a rajada.")
            "hextello" in n -> pick("En iniciadores que pueden preparar una canalización segura desde arbusto o niebla mientras Destello está en enfriamiento; úsalo para crear un ángulo nuevo, no en visión enemiga.", "Em iniciadores que conseguem canalizar com segurança do arbusto ou da névoa enquanto Flash está em recarga; use para criar um ângulo novo, não sob visão inimiga.")
            "impacto repentino" in n -> pick("En campeones que entran con deslizamiento, salto, teleportación o sigilo justo antes de su daño principal; activa la movilidad primero y golpea dentro de la ventana.", "Em campeões que entram com avanço, salto, teleporte ou furtividade antes do dano principal; ative a mobilidade primeiro e acerte dentro da janela.")
            "irrupcion de fase" in n || "phase rush" in n -> pick("Contra rivales que te castigan si mantienen contacto; conecta tres impactos rápidos y usa la velocidad para reposicionarte o seguir la persecución.", "Contra rivais que punem se mantiverem contato; conecte três acertos rápidos e use a velocidade para reposicionar ou continuar a perseguição.")
            "leyenda linaje" in n || "bloodline" in n -> pick("Cuando tu campeón mantiene ataques o daño continuo y necesita omnisucción acumulable para sostenerse entre peleas; empieza a cargarla con farmeo y participaciones.", "Quando seu campeão mantém ataques ou dano contínuo e precisa de Omnivamp acumulável para sustentação entre lutas; acumule com farm e participações.")
            "leyenda presteza" in n || "alacrity" in n -> pick("En campeones cuyo DPS depende de ataques básicos y velocidad de ataque sostenida; acumúlala con farmeo y objetivos antes de las peleas importantes.", "Em campeões cujo DPS depende de ataques básicos e Velocidade de Ataque sustentada; acumule com farm e objetivos antes das lutas importantes.")
            "leyenda velocidad" in n || "haste" in n -> pick("Cuando tu campeón depende de repetir habilidades y valora más velocidad de habilidades acumulada que velocidad de ataque; llega a las peleas con la runa ya avanzada mediante farmeo.", "Quando seu campeão depende de repetir habilidades e valoriza mais Aceleração acumulada do que Velocidade de Ataque; chegue às lutas com a runa avançada pelo farm.")
            "pies veloces" in n || "fleet footwork" in n -> pick("En líneas de desgaste o matchups donde necesitas curación y una ráfaga corta de velocidad para entrar, golpear y salir; prepara 100 de energía antes del intercambio.", "Em rotas de desgaste ou matchups onde precisa de cura e uma rajada curta de velocidade para entrar, atacar e sair; prepare 100 de energia antes da troca.")
            "pirolaser" in n -> pick("Para presión temprana de línea y poke frecuente; sincroniza el intercambio con su enfriamiento de 8 s en vez de gastar habilidades cuando la quemadura aún no está lista.", "Para pressão inicial de rota e poke frequente; sincronize a troca com a recarga de 8 s em vez de gastar habilidades quando a queimadura ainda não estiver pronta.")
            "primer golpe" in n || "first strike" in n -> pick("Cuando puedes iniciar tú el intercambio desde rango o niebla; protege la runa del poke rival y abre el combate antes de recibir daño para obtener amplificación y oro.", "Quando você consegue iniciar a troca de longe ou da névoa; proteja a runa do poke rival e abra o combate antes de receber dano para ganhar amplificação e ouro.")
            "revestimiento de huesos" in n || "bone plating" in n -> pick("Contra all-ins y combos cortos de campeones como Renekton, Pantheon, Riven o Jayce: deja que reduzca la primera cadena de impactos y evita gastarla con poke insignificante antes de la ventana de amenaza.", "Contra all-ins e combos curtos de campeões como Renekton, Pantheon, Riven ou Jayce: deixe reduzir a primeira sequência de acertos e evite gastá-la com poke irrelevante antes da janela de ameaça.")
            "revitalizar" in n -> pick("Cuando tu campeón cura o escuda con frecuencia, o depende de una defensa propia al caer de vida; su valor aumenta especialmente sobre objetivos por debajo del 40%.", "Quando seu campeão cura ou dá escudo com frequência, ou depende de defesa própria com pouca Vida; o valor aumenta especialmente em alvos abaixo de 40%.")
            "se avecina tormenta" in n || "gathering storm" in n -> pick("En partidas donde tu condición de victoria escala: sacrifica presión inmediata a cambio de llegar a 6, 9, 12 minutos y posteriores con más poder adaptable.", "Em partidas cuja condição de vitória escala: abra mão de pressão imediata para chegar aos 6, 9, 12 minutos e além com mais força adaptativa.")
            "soberano gelido" in n || "glacial" in n -> pick("En supports/tanques con inmovilización: inicia sobre el objetivo principal y usa las zonas de hielo para cortar la respuesta del resto del equipo rival.", "Em suportes/tanques com imobilização: inicie no alvo principal e use as zonas de gelo para cortar a resposta do restante da equipe inimiga.")
            "sobrecrecimiento" in n -> pick("En tanques y escaladores que pasarán mucho tiempo cerca de oleadas o campamentos; prioriza una ruta de farmeo que te permita alcanzar 30 acumulaciones sin abandonar objetivos.", "Em tanques e campeões de escala que passarão muito tempo perto de ondas ou campos; priorize uma rota de farm que permita chegar a 30 acúmulos sem abandonar objetivos.")
            n == "tirano" -> pick("Cuando tu campeón puede bajar rivales por debajo del 50% y seguir presionando durante esa ventana; reserva tu siguiente daño para activar el golpe adicional sobre un objetivo realmente rematable.", "Quando seu campeão consegue deixar rivais abaixo de 50% e continuar pressionando; reserve o próximo dano para ativar o golpe extra em um alvo realmente finalizável.")
            "trascendencia" in n -> pick("En builds dependientes de habilidades donde repetir H1/H2/H3 importa más que una sola rotación; aprovecha sus picos de nivel y el reintegro parcial desde nivel 9.", "Em builds dependentes de habilidades onde repetir H1/H2/H3 importa mais do que uma única rotação; aproveite os picos de nível e a redução parcial a partir do nível 9.")
            "triunfo" in n -> pick("En campeones que entran a peleas y pueden encadenar derribos: busca el primer objetivo alcanzable para activar la recuperación y la velocidad antes de continuar.", "Em campeões que entram em lutas e conseguem encadear abates: busque o primeiro alvo alcançável para ativar a recuperação e a velocidade antes de continuar.")
            "ultimo esfuerzo" in n || "last stand" in n -> pick("Para luchadores que siguen siendo peligrosos con poca vida; úsala cuando tu patrón te obliga a permanecer en combate por debajo del 60%, no si debes retirarte al primer daño.", "Para lutadores que continuam perigosos com pouca Vida; use quando seu padrão exige permanecer em combate abaixo de 60%, não se precisar recuar ao primeiro dano.")
            "orbe anulador" in n || "nullifying" in n -> pick("Contra burst que puede llevarte por debajo del 35% en una sola secuencia; conserva el escudo para la ventana de all-in en vez de exponerte a daño gratuito.", "Contra burst que pode levar você abaixo de 35% em uma única sequência; preserve o escudo para a janela de all-in em vez de receber dano gratuito.")
            else -> pick("Elige esta runa únicamente cuando puedas activar de forma repetible la condición descrita por su efecto dentro del patrón real de tu campeón y rol.", "Escolha esta runa apenas quando conseguir ativar de forma repetível a condição descrita pelo efeito dentro do padrão real do campeão e da função.")
        }
    }

    private fun spellScenario(name: String, pt: Boolean): String {
        val n = key(name)
        fun pick(es: String, br: String) = if (pt) br else es
        return when {
            "prender" in n || "ignite" in n || "ignicion" in n -> pick("Cuando necesitas presión de asesinato y reducir la recuperación de rivales con mucha curación; úsalo dentro del all-in, no después de que el rival ya haya escapado.", "Quando precisa de pressão de abate e reduzir a recuperação de rivais com muita cura; use durante o all-in, não depois que o rival já escapou.")
            "extenuacion" in n || "exhaust" in n -> pick("Contra asesinos o carries que concentran su daño en una ventana corta, como Zed, Akali, Katarina, Samira o Master Yi; lánzalo antes de su ráfaga principal, no cuando el daño ya terminó.", "Contra assassinos ou carries que concentram dano em uma janela curta, como Zed, Akali, Katarina, Samira ou Master Yi; use antes da rajada principal, não depois que o dano já terminou.")
            "barrera" in n || "barrier" in n -> pick("Cuando esperas burst difícil de evitar, por ejemplo de Syndra, Lux, Zoe o Fizz, y necesitas sobrevivir a la última parte del combo; actívala antes del impacto letal para no desperdiciar el escudo.", "Quando espera burst difícil de evitar, por exemplo de Syndra, Lux, Zoe ou Fizz, e precisa sobreviver ao fim do combo; ative antes do impacto letal para não desperdiçar o escudo.")
            "fantasmal" in n || "ghost" in n -> pick("En peleas largas donde necesitas perseguir o kitear; actívalo al inicio de la persecución para aprovechar toda la duración y sus extensiones por derribos.", "Em lutas longas onde precisa perseguir ou kitear; ative no início da perseguição para aproveitar toda a duração e extensões por abates.")
            "curar" in n || "curacion" in n || "heal" in n -> pick("En dúo cuando el valor principal es sobrevivir juntos a un 2v2 o salvar al compañero de una ráfaga; evita duplicarlo si tu aliado ya lo lleva.", "Na rota dupla quando o valor principal é sobreviver juntos a um 2v2 ou salvar o parceiro de burst; evite duplicar se o aliado já usa.")
            "limpiar" in n || "cleanse" in n -> pick("Contra cadenas de control eliminable; úsalo para cortar el control que habilita el daño siguiente. No lo elijas esperando quitar levantamientos o desplazamientos.", "Contra cadeias de controle removível; use para cortar o controle que habilita o dano seguinte. Não escolha esperando remover arremessos ou deslocamentos.")
            "aplastar" in n || "castigo" in n || "smite" in n -> pick("Solo para Jungla: conserva una carga cuando se acerque Dragón, Heraldo o Barón y coordina tu daño de habilidad con Aplastar para cerrar el objetivo.", "Somente para Selva: preserve uma carga quando Dragão, Arauto ou Barão estiverem próximos e sincronize o dano da habilidade com Golpear para finalizar o objetivo.")
            "teletransporte" in n || "teleport" in n -> pick("Cuando tu plan depende de presión lateral, volver a una oleada importante o incorporarte a una pelea desde otra línea.", "Quando seu plano depende de pressão lateral, voltar a uma onda importante ou entrar em uma luta a partir de outra rota.")
            "claridad" in n || "clarity" in n -> pick("Cuando el modo de juego y tu campeón sufren por maná en peleas prolongadas y necesitas restaurarlo también a aliados cercanos.", "Quando o modo de jogo e seu campeão sofrem com Mana em lutas prolongadas e você precisa restaurá-lo também para aliados próximos.")
            "marca" in n || "mark" in n -> pick("En modos donde Marca está disponible, para campeones que necesitan cerrar distancia e iniciar sobre objetivos de retaguardia; evita lanzarla sin una ruta segura para el segundo uso.", "Em modos onde Marca está disponível, para campeões que precisam encurtar distância e iniciar sobre alvos da retaguarda; evite lançar sem uma rota segura para o segundo uso.")
            else -> pick("Elige este hechizo cuando su ventana de uso resuelva una necesidad concreta de tu campeón y rol; planifica de antemano qué amenaza u objetivo justificará gastarlo.", "Escolha este feitiço quando a janela de uso resolver uma necessidade concreta do campeão e da função; planeje antes qual ameaça ou objetivo justificará gastá-lo.")
        }
    }

    private fun bootScenario(name: String, pt: Boolean): String {
        val n = key(name)
        fun pick(es: String, br: String) = if (pt) br else es
        return when {
            "grebas codiciosas" in n || "gluttonous" in n -> pick("Cuando tu campeón convierte daño constante en sustain y puede permanecer golpeando durante la pelea. Priorízalas si la omnisucción te permite seguir presionando; evita elegirlas si necesitas resistencias específicas para sobrevivir al primer combo.", "Quando seu campeão converte dano constante em sustentação e consegue permanecer causando dano. Priorize se o Omnivamp permitir continuar pressionando; evite se precisar de resistências específicas para sobreviver ao primeiro combo.")
            "grebas de berserker" in n || "berserker" in n -> pick("Para tiradores y duelistas cuyo DPS escala directamente con velocidad de ataque y que pueden mantener básicos sobre el objetivo. Cambia a una bota defensiva si el rival te elimina antes de poder atacar.", "Para atiradores e duelistas cujo DPS escala diretamente com Velocidade de Ataque e que conseguem manter ataques básicos no alvo. Troque por bota defensiva se o rival eliminar você antes de conseguir atacar.")
            "botas de mercurio" in n || "mercury" in n -> pick("Contra daño mágico relevante y cadenas de control que la Tenacidad sí puede reducir. Son especialmente valiosas si debes atravesar CC para entrar o seguir pegando; no las compres solo por una ralentización menor.", "Contra dano mágico relevante e cadeias de controle que a Tenacidade realmente reduz. São especialmente valiosas se você precisa atravessar CC para entrar ou continuar causando dano; não compre apenas por uma lentidão pequena.")
            "botas blindadas" in n || "plated steelcaps" in n -> pick("Contra composiciones físicas centradas en ataques básicos, especialmente tiradores y duelistas. Priorizarlas tiene sentido cuando la mayor amenaza te golpea con autos repetidos, no cuando el daño principal es mágico o de habilidades.", "Contra composições físicas centradas em ataques básicos, especialmente atiradores e duelistas. Priorize quando a maior ameaça causa dano com autos repetidos, não quando o dano principal é mágico ou de habilidades.")
            "botas jonias de la lucidez" in n || "ionian" in n -> pick("Cuando tu campeón gana más por repetir habilidades y recuperar antes sus hechizos de invocador que por penetración o resistencia. Son fuertes si tus ventanas dependen de enfriamientos cortos y recursos suficientes.", "Quando seu campeão ganha mais repetindo habilidades e recuperando feitiços de invocador mais cedo do que com penetração ou resistência. São fortes se suas janelas dependem de recargas curtas e recursos suficientes.")
            "botas de mana" in n -> pick("Para magos y soportes AP que necesitan penetración mágica, maná y limpieza de oleada desde temprano. Elige otra opción si tu problema principal es sobrevivir a burst o control.", "Para magos e suportes AP que precisam de Penetração Mágica, Mana e limpeza de onda cedo. Escolha outra opção se o principal problema for sobreviver a burst ou controle.")
            "botas dinamicas" in n -> pick("Para campeones AD que quieren un pico ofensivo temprano de daño y penetración de armadura. Son mejores contra objetivos de poca armadura cuando puedes imponer tempo antes de necesitar defensa.", "Para campeões AD que querem um pico ofensivo inicial de dano e Penetração de Armadura. São melhores contra alvos com pouca Armadura quando você consegue impor ritmo antes de precisar de defesa.")
            "botas inmortales" in n || "immortal treds" in n -> pick("Mejora Grebas codiciosas cuando la pelea sigue premiando daño sostenido y omnisucción: entra con vida alta para aprovechar el daño y usa la mejora de curaciones/escudos al caer de vida.", "Melhore Grevas Gulosas quando a luta ainda recompensa dano sustentado e Omnivamp: entre com Vida alta para aproveitar o dano e use o aumento de curas/escudos ao perder Vida.")
            "grebas de metal" in n || "gunmetal" in n -> pick("Evoluciona Grebas de berserker cuando puedes mantener ataques sobre campeones y necesitas más velocidad de ataque, sustain físico y movilidad para perseguir o kitear.", "Evolua Grevas de Berserker quando consegue manter ataques em campeões e precisa de mais Velocidade de Ataque, sustentação física e mobilidade para perseguir ou kitear.")
            "trituradoras encadenadas" in n || "chainlaced" in n -> pick("Evoluciona Botas de mercurio cuando el daño mágico sigue siendo una condición de derrota: el escudo mágico tras recibir daño te protege del seguimiento, mientras la Tenacidad mantiene tu ventana de acción.", "Evolua Botas de Mercúrio quando o dano mágico continua sendo condição de derrota: o escudo mágico após receber dano protege do seguimento, enquanto a Tenacidade mantém sua janela de ação.")
            "avance blindado" in n || "armored advance" in n -> pick("Evoluciona Botas blindadas cuando la amenaza física continúa dominando las peleas. El escudo físico posterior al impacto refuerza intercambios contra tiradores, duelistas y burst AD.", "Evolua Botas Blindadas quando a ameaça física continua dominando as lutas. O escudo físico após o impacto reforça trocas contra atiradores, duelistas e burst AD.")
            "lucidez carmesi" in n || "crimson lucidity" in n -> pick("Evoluciona las Jonias cuando tu condición sigue siendo lanzar habilidades y hechizos con máxima frecuencia; aprovecha la velocidad de movimiento posterior para reposicionarte después de cada acción.", "Evolua as Ionianas quando sua condição continua sendo usar habilidades e feitiços com máxima frequência; aproveite a Velocidade de Movimento posterior para reposicionar após cada ação.")
            "botas del lanzahechizos" in n || "spellslinger" in n -> pick("Evoluciona Botas de maná cuando el plan sigue siendo daño mágico: la penetración plana y porcentual gana valor cuando los rivales empiezan a comprar resistencia mágica.", "Evolua Botas de Mana quando o plano continua sendo dano mágico: a penetração plana e percentual ganha valor quando os rivais começam a comprar Resistência Mágica.")
            "botas quebrantarmaduras" in n || "armorcrusher" in n -> pick("Evoluciona Botas dinámicas cuando necesitas conservar el pico AD y añadir penetración porcentual para seguir dañando tras la primera compra de armadura rival; usa la velocidad fuera de combate para crear ángulos.", "Evolua Botas Dinâmicas quando precisa manter o pico AD e adicionar penetração percentual para continuar causando dano após a primeira compra de Armadura inimiga; use a velocidade fora de combate para criar ângulos.")
            else -> pick("Elige estas botas solo si sus estadísticas y pasiva responden a la principal amenaza o al patrón de daño de esta partida.", "Escolha estas botas apenas se os atributos e a passiva responderem à principal ameaça ou ao padrão de dano desta partida.")
        }
    }
}
