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
                appendLine(trigger.ifBlank { coreItemTimingHint(itemName, pt) })
                val matchup = against.ifBlank { coreHint }
                if (matchup.isNotBlank()) append("\nPartidas em que rende mais:\n$matchup")
                append("\nNão é um conselho genérico: este bloco descreve a função deste item específico dentro da build.")
            } else buildString {
                appendLine("Por qué $localizedName es core para $championName ($roleName):")
                appendLine(catalogTip.ifBlank { purpose })
                appendLine("\nCuándo completarlo:")
                appendLine(trigger.ifBlank { coreItemTimingHint(itemName, pt) })
                val matchup = against.ifBlank { coreHint }
                if (matchup.isNotBlank()) append("\nPartidas donde rinde más:\n$matchup")
                append("\nEste consejo corresponde a este objeto concreto, no al plan general del campeón.")
            }
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
        val description = spell?.getLocalizedDescription(lang)
            ?.substringBefore("\n")
            ?.trim()
            .orEmpty()
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
        return when {
            "rabadon" in n -> if (pt) "Complete depois de já ter uma base sólida de poder de habilidade; rende muito mais como multiplicador do AP acumulado do que como primeira compra." else "Complétalo después de tener una base sólida de poder de habilidad; rinde mucho más como multiplicador del AP acumulado que como primera compra."
            "luden" in n -> if (pt) "Priorize cedo quando seu plano é poke, limpeza de onda e explosão curta contra alvos frágeis." else "Priorízalo temprano cuando tu plan es poke, limpieza de oleada y ráfaga corta contra objetivos frágiles."
            "liandry" in n -> if (pt) "Priorize quando as lutas duram e o rival acumula muita vida." else "Priorízalo cuando las peleas se alargan y el rival acumula mucha vida."
            "trinidad" in n || "trinity" in n -> if (pt) "Complete quando você pode alternar habilidades e ataques básicos constantemente para aproveitar cada proc." else "Complétalo cuando puedes alternar habilidades y ataques básicos constantemente para aprovechar cada activación."
            "rei destruido" in n || "ruined" in n -> if (pt) "Priorize contra lutadores e tanques de muita vida quando você consegue manter ataques básicos." else "Priorízalo contra luchadores y tanques de mucha vida cuando puedes mantener ataques básicos."
            "manamune" in n || "muramana" in n -> if (pt) "Compre cedo o componente de mana para acumular; o pico chega quando a transformação é concluída." else "Compra temprano el componente de maná para acumular; el pico llega al completar la transformación."
            else -> if (pt) "Complete no ponto da build indicado quando sua passiva e estatísticas já podem ser aproveitadas de forma constante nas lutas." else "Complétalo en el punto indicado de la build cuando su pasiva y estadísticas ya puedan aprovecharse de forma constante en las peleas."
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
            "cometa" in n -> pick("En líneas de poke donde impactas habilidades a distancia con frecuencia; funciona bien contra campeones de movilidad limitada como Lux, Orianna o Ziggs.", "Em rotas de poke onde você acerta habilidades à distância com frequência; funciona bem contra campeões de mobilidade limitada como Lux, Orianna ou Ziggs.")
            "conquistador" in n -> pick("En peleas prolongadas contra luchadores/tanques donde puedes mantener varias acumulaciones, por ejemplo Darius, Sett, Ornn o Maokai.", "Em lutas prolongadas contra lutadores/tanques onde você mantém várias acumulações, como Darius, Sett, Ornn ou Maokai.")
            "electrocut" in n -> pick("Cuando buscas intercambios cortos de tres impactos y burst sobre objetivos frágiles como Jinx, Lux, Ahri o Ezreal.", "Quando busca trocas curtas de três acertos e burst em alvos frágeis como Jinx, Lux, Ahri ou Ezreal.")
            "primer golpe" in n || "first strike" in n -> pick("Cuando puedes iniciar tú el intercambio desde rango o niebla y el rival no puede golpearte primero con facilidad.", "Quando você consegue iniciar a troca de longe ou da névoa e o rival não consegue acertá-lo primeiro com facilidade.")
            "irrupcion de fase" in n || "phase rush" in n -> pick("Contra rivales que dependen de alcanzarte o ralentizarte, como Darius, Nasus u Olaf; activa tres impactos y reposiciónate.", "Contra rivais que dependem de alcançar ou desacelerar você, como Darius, Nasus ou Olaf; ative três impactos e reposicione.")
            "aery" in n -> pick("Para hostigamiento constante o para campeones que también escudan/curan aliados; prioriza intercambios frecuentes y seguros.", "Para poke constante ou campeões que também dão escudo/cura a aliados; priorize trocas frequentes e seguras.")
            "soberano gelido" in n || "glacial" in n -> pick("En supports/tanques con inmovilización contra composiciones que quieren entrar encima de tu carry.", "Em suportes/tanques com imobilização contra composições que querem mergulhar no seu carry.")
            "guardian" in n || "guardián" in n -> pick("Cuando tu prioridad es proteger a un carry frente a burst o engage rival, especialmente en dúo.", "Quando sua prioridade é proteger um carry contra burst ou engage inimigo, especialmente na rota dupla.")
            "revestimiento de huesos" in n || "bone plating" in n -> pick("Contra all-ins y combos cortos de campeones como Renekton, Pantheon, Riven o Jayce.", "Contra all-ins e combos curtos de campeões como Renekton, Pantheon, Riven ou Jayce.")
            "fuerzas renovadas" in n || "second wind" in n -> pick("Contra poke repetido en línea, por ejemplo Teemo, Kennen, Jayce o Ziggs; úsala para sobrevivir al desgaste.", "Contra poke repetido na rota, por exemplo Teemo, Kennen, Jayce ou Ziggs; use para sobreviver ao desgaste.")
            "orbe anulador" in n || "nullifying" in n -> pick("Contra burst mágico que puede bajarte de golpe, como Akali, Syndra, Fizz o Evelynn.", "Contra burst mágico que pode derrubá-lo rapidamente, como Akali, Syndra, Fizz ou Evelynn.")
            "derribado" in n || "cut down" in n -> pick("Contra equipos con mucha vida máxima: Ornn, Sion, Dr. Mundo, Maokai o Cho'Gath.", "Contra equipes com muita vida máxima: Ornn, Sion, Dr. Mundo, Maokai ou Cho'Gath.")
            "golpe de gracia" in n -> pick("Cuando tu campeón puede rematar objetivos por debajo de vida media y necesitas asegurar ejecuciones sobre carries.", "Quando seu campeão consegue finalizar alvos com pouca vida e você precisa garantir execuções nos carries.")
            "ultimo esfuerzo" in n || "last stand" in n -> pick("Para luchadores que siguen peleando con poca vida y obtienen valor al prolongar el duelo.", "Para lutadores que continuam brigando com pouca vida e ganham valor ao prolongar o duelo.")
            "banda de mana" in n -> pick("En campeones con maná que hostigan con habilidades y necesitan evitar quedarse sin recursos antes de objetivos.", "Em campeões com mana que fazem poke com habilidades e precisam evitar ficar sem recurso antes de objetivos.")
            "trascendencia" in n -> pick("En builds dependientes de habilidades donde reducir enfriamientos importa más que daño inmediato de una sola rotación.", "Em builds dependentes de habilidades onde reduzir recargas importa mais que dano imediato de uma única rotação.")
            "pirolaser" in n -> pick("Para presión temprana de línea y poke frecuente; maximiza valor contra rivales a los que puedes golpear sin comprometerte.", "Para pressão inicial de rota e poke frequente; maximize o valor contra rivais que você pode acertar sem se expor.")
            "demoler" in n -> pick("Cuando puedes ganar prioridad de línea y quedarte pegando a torres; especialmente útil para convertir una ventaja en placas.", "Quando você consegue prioridade de rota e pode bater em torres; especialmente útil para converter vantagem em placas.")
            "impacto repentino" in n -> pick("En campeones que entran con dash, salto, teleportación o sigilo antes de su daño principal.", "Em campeões que entram com dash, salto, teleporte ou furtividade antes do dano principal.")
            "leyenda presteza" in n || "alacrity" in n -> pick("En campeones cuyo daño depende de ataques básicos y velocidad de ataque sostenida.", "Em campeões cujo dano depende de ataques básicos e velocidade de ataque sustentada.")
            "leyenda linaje" in n || "bloodline" in n -> pick("Cuando necesitas más sustain durante peleas y farmeo y tu campeón puede aprovechar autos frecuentes.", "Quando precisa de mais sustain em lutas e farm e seu campeão aproveita ataques básicos frequentes.")
            "leyenda velocidad" in n || "haste" in n -> pick("Cuando tu campeón depende de repetir habilidades y valora más aceleración que velocidad de ataque.", "Quando seu campeão depende de repetir habilidades e valoriza mais aceleração do que velocidade de ataque.")
            else -> pick("Úsala cuando la condición descrita por su efecto sea parte real del plan de la partida; no la elijas solo porque aparezca en una plantilla.", "Use quando a condição descrita pelo efeito fizer parte real do plano da partida; não escolha apenas porque aparece em uma plantilla.")
        }
    }

    private fun spellScenario(name: String, pt: Boolean): String {
        val n = key(name)
        fun pick(es: String, br: String) = if (pt) br else es
        return when {
            "prender" in n || "ignite" in n || "ignicion" in n -> pick("Cuando necesitas presión de asesinato y reducir la recuperación de rivales con mucha curación, como Soraka, Yuumi, Aatrox o Dr. Mundo.", "Quando precisa de pressão de abate e reduzir a recuperação de rivais com muita cura, como Soraka, Yuumi, Aatrox ou Dr. Mundo.")
            "extenuacion" in n || "exhaust" in n -> pick("Contra asesinos o carries que concentran su daño en una ventana corta, como Zed, Akali, Katarina, Samira o Master Yi.", "Contra assassinos ou carries que concentram dano em uma janela curta, como Zed, Akali, Katarina, Samira ou Master Yi.")
            "barrera" in n || "barrier" in n -> pick("Cuando esperas burst difícil de evitar y necesitas sobrevivir a la última parte del combo, por ejemplo contra Syndra, Lux, Zoe o Fizz.", "Quando espera burst difícil de evitar e precisa sobreviver ao fim do combo, por exemplo contra Syndra, Lux, Zoe ou Fizz.")
            "fantasmal" in n || "ghost" in n -> pick("En peleas largas donde necesitas perseguir o kitear; rinde contra rivales que te ganan si consiguen mantener contacto, como Darius, Nasus u Olaf.", "Em lutas longas onde precisa perseguir ou kitear; rende contra rivais que vencem se mantiverem contato, como Darius, Nasus ou Olaf.")
            "curar" in n || "curacion" in n || "heal" in n -> pick("En dúo cuando el valor principal es sobrevivir juntos a un 2v2 o salvar al compañero de una ráfaga; evita duplicarlo si tu aliado ya lo lleva.", "Na rota dupla quando o valor principal é sobreviver juntos a um 2v2 ou salvar o parceiro de burst; evite duplicar se o aliado já usa.")
            "limpiar" in n || "cleanse" in n -> pick("Contra cadenas de control eliminable, como Ashe, Morgana, Leona o Varus. No lo elijas pensando que quitará levantamientos o supresiones.", "Contra cadeias de controle removível, como Ashe, Morgana, Leona ou Varus. Não escolha pensando que removerá knock-ups ou supressões.")
            "castigo" in n || "smite" in n -> pick("Solo para Jungla: úsalo para asegurar monstruos, acelerar la ruta y disputar Dragón, Heraldo o Barón; no es una elección de línea.", "Somente para Selva: use para garantir monstros, acelerar a rota e disputar Dragão, Arauto ou Barão; não é escolha de rota.")
            "teletransporte" in n || "teleport" in n -> pick("Cuando tu plan depende de presión lateral, volver a una oleada importante o incorporarte a una pelea desde otra línea.", "Quando seu plano depende de pressão lateral, voltar a uma onda importante ou entrar em uma luta a partir de outra rota.")
            "claridad" in n || "clarity" in n -> pick("Cuando el modo de juego y tu campeón sufren por maná en peleas prolongadas y necesitas restaurarlo también a aliados cercanos.", "Quando o modo de jogo e seu campeão sofrem com mana em lutas prolongadas e você precisa restaurá-la também para aliados próximos.")
            "marca" in n || "mark" in n -> pick("En modos donde Marca está disponible, para campeones que necesitan cerrar distancia e iniciar sobre objetivos de backline.", "Em modos onde Marca está disponível, para campeões que precisam fechar distância e iniciar sobre alvos da retaguarda.")
            else -> pick("Úsalo cuando su efecto resuelva una necesidad concreta de la partida y complemente el plan de tu campeón y línea.", "Use quando seu efeito resolver uma necessidade concreta da partida e complementar o plano do campeão e da rota.")
        }
    }
}
