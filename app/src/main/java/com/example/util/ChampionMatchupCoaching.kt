package com.example.util

import com.example.model.Champion
import com.example.model.ChampionSkill
import com.example.model.LaneRole
import java.text.Normalizer

data class ChampionDuelPlan(val early: String, val ultimate: String, val late: String,
    val rivalHeading: String, val rival: String, val winCondition: String, val verdict: String)

/** Advice uses the two actual kits, rather than a shared paragraph selected by a counter badge. */
object ChampionMatchupCoaching {
    private fun normalized(text: String) = Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}"), "")
    private fun Champion.skill(slot: String) = skills.firstOrNull { it.slot == slot }
    private fun ChampionSkill.matches(vararg words: String) = words.any { normalized(description).contains(it) }
    private fun Champion.threat(): ChampionSkill? = if (id == "smolder") skill("3") else
        skills.firstOrNull { it.slot in listOf("1", "2", "3") && it.matches("aturd", "inmovil", "silenci", "derrib", "encant", "gancho") }
            ?: skills.firstOrNull { it.slot in listOf("1", "2", "3") && it.matches("desplaz", "salta", "vuelo", "embiste", "invisible") }
            ?: skill("1")
    private fun label(skill: ChampionSkill?, lang: String): String {
        if (skill == null) return if (lang == "pt") "habilidade principal" else "habilidad principal"
        val slot = if (skill.slot == "P") (if (lang == "pt") "Passiva" else "Pasiva") else "H${skill.slot}"
        return "$slot · ${skill.getLocalizedName(lang)}"
    }
    private fun fact(skill: ChampionSkill?, lang: String): String = skill?.getLocalizedDescription(lang)
        ?.replace(Regex("<[^>]*>"), "")?.replace("\n", " ")?.trim().orEmpty()
    private fun advice(champion: Champion, lang: String) = trStr(lang, champion.tacticalAdvice)

    fun forDuel(own: Champion, enemy: Champion, role: LaneRole, language: String): ChampionDuelPlan {
        val lang = AppLanguage.normalize(language)
        val pt = lang == "pt"
        val me = own.getLocalizedName(lang)
        val rival = enemy.getLocalizedName(lang)
        val h1 = label(own.skill("1"), lang)
        val h4 = label(own.skill("4"), lang)
        val threat = enemy.threat()
        val danger = label(threat, lang)
        val enemyUlt = label(enemy.skill("4"), lang)
        val ownControl = own.skills.firstOrNull { it.slot != "P" && it.matches("inmovil", "aturd", "derrib", "encant", "silenci") }
        val punish = if (pt) "Contra $rival, abra a troca depois que ele gastar $danger; preserve ${label(ownControl ?: own.skill("3"), lang)} para a resposta."
            else "Contra $rival, abre el intercambio después de que gaste $danger; conserva ${label(ownControl ?: own.skill("3"), lang)} para responder."
        val early = when (own.id) {
            "varus" -> if (pt) "$me: aplique três marcas de Infecção com ataques e detone com H1 ou H3. Use H3 para facilitar a mira da H1; não carregue a flecha parado no alcance de $danger de $rival."
                else "$me: aplica tres marcas de Infección con ataques y detónalas con H1 o H3. Usa H3 para facilitar la puntería de H1; no cargues la flecha quieto al alcance de $danger de $rival."
            "jhin" -> if (pt) "$me: prepare o quarto disparo antes de trocar com $rival e recue durante a recarga. Marque-o com um ataque ou H1 e só então use H2 para enraizar. Não desperdice o quarto disparo em uma tropa quando $danger estiver indisponível."
                else "$me: prepara el cuarto disparo antes de intercambiar con $rival y retrocede durante la recarga. Márcalo con un ataque o H1 y solo entonces usa H2 para inmovilizar. No gastes el cuarto disparo en un súbdito cuando $danger esté indisponible."
            else -> "$me · ${role.getLocalizedName(lang)}. ${label(own.skill("P"), lang)}: ${fact(own.skill("P"), lang)} $h1: ${fact(own.skill("1"), lang)} $punish"
        }
        val ultimate = when (own.id) {
            "varus" -> if (pt) "$me: use $h4 para imobilizar $rival, aplique marcas durante o controle e detone com H1. ${flightResponse(enemy, lang)}"
                else "$me: usa $h4 para inmovilizar a $rival, aplica marcas durante el control y detona con H1. ${flightResponse(enemy, lang)}"
            "jhin" -> if (pt) "$me: abra $h4 de uma posição protegida, após $rival gastar $danger. Os disparos param no primeiro campeão; ajuste o ângulo se houver alguém protegendo o rival e use a lentidão para alinhar os próximos tiros."
                else "$me: abre $h4 desde una posición protegida, después de que $rival gaste $danger. Los disparos se detienen en el primer campeón; ajusta el ángulo si alguien protege al rival y usa la ralentización para alinear los siguientes tiros."
            else -> "$me · $h4: ${fact(own.skill("4"), lang)} $punish"
        } + cooldown(own.skill("4"), lang)
        val late = when (own.id) {
            "varus" -> if (pt) "$me: mantenha o dano com ataques e detonações de Infecção no alvo acessível. Antes de disputar um objetivo contra $rival, use H1 para desgastar e guarde H4 para quem entrar na sua equipe; H3 reduz a cura dentro da área."
                else "$me: mantén el daño con ataques y detonaciones de Infección sobre el objetivo accesible. Antes de disputar un objetivo contra $rival, usa H1 para desgastar y guarda H4 para quien entre en tu equipo; H3 reduce la curación dentro del área."
            "jhin" -> if (pt) "$me: use H3 nas entradas do objetivo e H2 para prender $rival após o dano de um aliado. Planeje a recarga atrás da linha de frente: seus quatro disparos não oferecem dano contínuo. Use H4 para finalizar de longe depois de $danger."
                else "$me: coloca H3 en las entradas del objetivo y usa H2 para atrapar a $rival tras el daño de un aliado. Planea la recarga detrás de la primera línea: tus cuatro disparos no ofrecen daño continuo. Usa H4 para rematar desde lejos después de $danger."
            else -> if (pt) "$me: ${advice(own, lang)} Contra $rival, escolha o alvo e o ângulo de $h4 considerando $danger. ${label(own.skill("3"), lang)}: ${fact(own.skill("3"), lang)}"
                else "$me: ${advice(own, lang)} Contra $rival, elige el objetivo y el ángulo de $h4 teniendo en cuenta $danger. ${label(own.skill("3"), lang)}: ${fact(own.skill("3"), lang)}"
        }
        val counterplay = when {
            enemy.id == "smolder" -> if (pt) "$rival pode atravessar terreno com H3. Não persiga através da parede; pressione a saída do voo com ${label(ownControl ?: own.skill("1"), lang)}. Sua H1 acumula poder: puna o farm com $h1 quando H3 estiver indisponível. Desvie do centro da H4 antes de retomar a troca."
                else "$rival puede cruzar terreno con H3. No lo persigas a través de la pared; presiona la salida del vuelo con ${label(ownControl ?: own.skill("1"), lang)}. Su H1 acumula poder: castiga el farmeo con $h1 cuando H3 esté indisponible. Evita el centro de H4 antes de retomar el intercambio."
            threat?.matches("aturd", "inmovil", "encant", "gancho") == true -> if (pt) "$rival · $danger: ${fact(threat, lang)} Com $me, espere esse controle ser gasto antes de se expor; então execute seu plano com $h1."
                else "$rival · $danger: ${fact(threat, lang)} Con $me, espera a que gaste ese control antes de exponerte; entonces ejecuta tu plan con $h1."
            else -> "$rival · $danger: ${fact(threat, lang)} $punish"
        }
        val enemyUltimate = if (pt) "$enemyUlt: ${fact(enemy.skill("4"), lang)}" else "$enemyUlt: ${fact(enemy.skill("4"), lang)}"
        val win = when (own.id) {
            "varus" -> if (pt) "$me vence pela sequência controle → marcas → detonação. Contra $rival, desgaste antes de iniciar e não avance sem H4 ou proteção; force $danger antes da tentativa de abate."
                else "$me gana mediante control → marcas → detonación. Contra $rival, desgasta antes de iniciar y no avances sin H4 o protección; fuerza $danger antes del intento de eliminación."
            "jhin" -> if (pt) "$me vence por capturas e finalizações, não por uma troca contínua durante a recarga. Contra $rival, combine a marca de dano com H2 e o quarto disparo; encerre a troca antes de ficar sem munição."
                else "$me gana mediante capturas y remates, no mediante un intercambio continuo durante la recarga. Contra $rival, combina la marca de daño con H2 y el cuarto disparo; termina el intercambio antes de quedarte sin munición."
            else -> if (pt) "$me: ${advice(own, lang)} A condição contra $rival é executar esse mecanismo após $danger. ${label(own.skill("2"), lang)}: ${fact(own.skill("2"), lang)}"
                else "$me: ${advice(own, lang)} La condición contra $rival es ejecutar ese mecanismo después de $danger. ${label(own.skill("2"), lang)}: ${fact(own.skill("2"), lang)}"
        }
        val verdict = if (pt) "$me contra $rival: ${advice(own, lang)} $punish" else "$me contra $rival: ${advice(own, lang)} $punish"
        return ChampionDuelPlan(early, ultimate, late, "$rival · $danger", "$counterplay\n\n$enemyUltimate", win, verdict)
    }

    private fun flightResponse(enemy: Champion, lang: String) = if (enemy.id == "smolder") {
        if (lang == "pt") "Espere a H3 de Smolder terminar ou mire no ponto de saída do voo; não desperdice a imobilização durante a fuga."
        else "Espera a que termine H3 de Smolder o apunta a la salida del vuelo; no gastes la inmovilización durante la huida."
    } else if (lang == "pt") "Espere ${label(enemy.threat(), lang)} ser gasto antes de iniciar."
        else "Espera a que gaste ${label(enemy.threat(), lang)} antes de iniciar."

    private fun cooldown(skill: ChampionSkill?, lang: String) = skill?.cooldown?.takeIf { it.isNotBlank() }?.let {
        if (lang == "pt") " Recarga indicada para esta habilidade: $it, conforme o nível."
        else " Recarga indicada para esta habilidad: $it, según el nivel."
    }.orEmpty()

    fun championPlan(champion: Champion, role: LaneRole, language: String): String {
        val lang = AppLanguage.normalize(language)
        fun title(slot: String): String {
            val skill = champion.skill(slot)
            return when (slot) {
                "1" -> if (lang == "pt") skill?.let { "sua H1 (${it.getLocalizedName(lang)})" } ?: "sua Habilidade 1 (H1)"
                    else skill?.let { "su H1 (${it.getLocalizedName(lang)})" } ?: "su Habilidad 1 (H1)"
                "4" -> if (lang == "pt") skill?.let { "sua Definitiva (${it.getLocalizedName(lang)})" } ?: "sua Definitiva (H4)"
                    else skill?.let { "su Definitiva (${it.getLocalizedName(lang)})" } ?: "su Definitiva (H4)"
                else -> label(skill, lang)
            }
        }
        val slots = (champion.skills.map { it.slot } + listOf("1", "4")).distinct()
        return "${champion.getLocalizedName(lang)} · ${role.getLocalizedName(lang)}\n${advice(champion, lang)}\n\n" +
            slots.joinToString("\n\n") { slot ->
                val skill = champion.skill(slot)
                val details = fact(skill, lang).ifBlank {
                    if (lang == "pt") "Descrição não disponível para este campeão." else "Descripción no disponible para este campeón."
                }
                "${title(slot)}: $details${cooldown(skill, lang)}"
            }
    }
}
