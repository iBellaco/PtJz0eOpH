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

    private fun interaction(own: Champion, enemy: Champion, lang: String): String {
        val me = own.getLocalizedName(lang)
        val rival = enemy.getLocalizedName(lang)
        val pt = lang == "pt"
        val danger = enemy.threat()
        val control = own.skills.firstOrNull { it.slot != "P" && it.matches("inmovil", "aturd", "encant", "derrib", "silenci") }
        val projectileBlock = own.skills.firstOrNull { it.matches("proyectil") && it.matches("bloque", "destruy", "intercept") }
        val knownProjectileSlot = mapOf("lux" to "1", "morgana" to "1", "ahri" to "3", "blitzcrank" to "1",
            "thresh" to "1", "nautilus" to "1", "ezreal" to "1", "varus" to "1", "jhin" to "2", "ashe" to "2")[enemy.id]
        val projectile = knownProjectileSlot?.let { enemy.skill(it) } ?: enemy.skills.firstOrNull {
            it.slot in listOf("1", "2", "3") && it.matches("proyectil", "dispara", "flecha") }
        val spellShield = when (enemy.id) {
            "morgana" -> enemy.skill("3")
            "sivir" -> enemy.skill("3")
            "nocturne" -> enemy.skill("2")
            else -> enemy.skills.firstOrNull { it.matches("escudo de hechizos", "bloquea la siguiente habilidad", "bloquea el siguiente hechizo") }
        }
        val invulnerable = enemy.skills.firstOrNull { it.slot != "P" && it.matches("invulnerable") }
        val antiHeal = own.skills.firstOrNull { it.matches("heridas graves") || (it.matches("reduce") && it.matches("curacion", "regeneracion")) }
        val healing = enemy.skills.firstOrNull { it.matches("cura", "regenera", "recupera vida") }
        val mobility = enemy.skills.firstOrNull { it.slot != "P" && it.matches("desplaza", "teletransport", "salta", "vuelo") }
        val dodgeAttacks = own.skills.firstOrNull { it.matches("esquiva", "bloquea ataques basicos", "evita los ataques basicos") }
        val empoweredAttacks = enemy.skills.firstOrNull { it.matches("ataques basicos", "siguiente ataque") }
        val myUlt = own.skill("4")
        return when {
            projectileBlock != null && projectile != null -> if (pt) "$me pode proteger a troca com ${label(projectileBlock, lang)} contra ${label(projectile, lang)} de $rival. Preserve essa defesa para o projétil, em vez de gastá-la na onda."
                else "$me puede proteger el intercambio con ${label(projectileBlock, lang)} frente a ${label(projectile, lang)} de $rival. Conserva esa defensa para el proyectil en lugar de gastarla en la oleada."
            spellShield != null && control != null -> if (pt) "$rival pode negar seu controle com ${label(spellShield, lang)}. Com $me, force essa proteção com dano de menor compromisso e espere o efeito terminar antes de usar ${label(control, lang)}."
                else "$rival puede negar tu control con ${label(spellShield, lang)}. Con $me, fuerza esa protección con daño de menor compromiso y espera a que termine antes de usar ${label(control, lang)}."
            invulnerable != null -> if (pt) "$rival evita dano durante ${label(invulnerable, lang)}. Com $me, adie ${label(myUlt, lang)} até esse efeito terminar e mantenha distância durante a proteção, sem gastar o dano decisivo na invulnerabilidade."
                else "$rival evita daño durante ${label(invulnerable, lang)}. Con $me, retrasa ${label(myUlt, lang)} hasta que termine ese efecto y mantén distancia durante la protección, sin gastar el daño decisivo durante la invulnerabilidad."
            antiHeal != null && healing != null -> if (pt) "$me tem redução de cura em ${label(antiHeal, lang)}. Aplique-a quando $rival usar ${label(healing, lang)} para recuperar vida; alinhe sua sequência de dano com essa janela."
                else "$me tiene reducción de curación en ${label(antiHeal, lang)}. Aplícala cuando $rival use ${label(healing, lang)} para recuperar vida; coordina tu secuencia de daño con esa ventana."
            mobility != null && control != null -> {
                val pressure = if (control.slot == "1") {
                    if (pt) "Com $me, aproxime-se e ameace uma troca curta sem gastar seu controle;"
                    else "Con $me, acércate y amenaza un intercambio corto sin gastar tu control;"
                } else {
                    if (pt) "Com $me, pressione primeiro com ${label(own.skill("1"), lang)} e"
                    else "Con $me, presiona primero con ${label(own.skill("1"), lang)} y"
                }
                if (pt) "$rival pode escapar com ${label(mobility, lang)}. $pressure guarde ${label(control, lang)} para o fim do deslocamento ou o ponto de saída."
                else "$rival puede escapar con ${label(mobility, lang)}. $pressure guarda ${label(control, lang)} para el final del desplazamiento o el punto de salida."
            }
            dodgeAttacks != null && empoweredAttacks != null -> if (pt) "$me: sincronize ${label(dodgeAttacks, lang)} com os ataques reforçados por ${label(empoweredAttacks, lang)} de $rival. Não inicie a troca longa com essa defesa indisponível."
                else "$me: sincroniza ${label(dodgeAttacks, lang)} con los ataques potenciados por ${label(empoweredAttacks, lang)} de $rival. No inicies el intercambio largo con esa defensa indisponible."
            myUlt?.matches("canaliza", "concentra") == true && danger?.matches("aturd", "inmovil", "encant", "derrib", "silenci") == true -> if (pt) "$me precisa de uma posição protegida para ${label(myUlt, lang)}. Espere $rival gastar ${label(danger, lang)} antes de canalizar; mantenha distância do alcance desse controle."
                else "$me necesita una posición protegida para ${label(myUlt, lang)}. Espera a que $rival gaste ${label(danger, lang)} antes de canalizar; mantén distancia respecto al alcance de ese control."
            else -> if (pt) "$me: ${advice(own, lang)} Contra $rival, a habilidade a respeitar é ${label(danger, lang)}: ${fact(danger, lang)}"
                else "$me: ${advice(own, lang)} Contra $rival, la habilidad que debes respetar es ${label(danger, lang)}: ${fact(danger, lang)}"
        }
    }

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
        val kitInteraction = interaction(own, enemy, lang)
        val punish = if (pt) "Contra $rival, abra a troca depois que ele gastar $danger; preserve ${label(ownControl ?: own.skill("3"), lang)} para a resposta."
            else "Contra $rival, abre el intercambio después de que gaste $danger; conserva ${label(ownControl ?: own.skill("3"), lang)} para responder."
        val early = when (own.id) {
            "varus" -> if (pt) "$me: aplique três marcas de Infecção com ataques e detone com H1 ou H3. Use H3 para facilitar a mira da H1; não carregue a flecha parado no alcance de $danger de $rival."
                else "$me: aplica tres marcas de Infección con ataques y detónalas con H1 o H3. Usa H3 para facilitar la puntería de H1; no cargues la flecha quieto al alcance de $danger de $rival."
            "jhin" -> if (pt) "$me: prepare o quarto disparo antes de trocar com $rival e recue durante a recarga. Marque-o com um ataque ou H1 e só então use H2 para enraizar. Não desperdice o quarto disparo em uma tropa quando $danger estiver indisponível."
                else "$me: prepara el cuarto disparo antes de intercambiar con $rival y retrocede durante la recarga. Márcalo con un ataque o H1 y solo entonces usa H2 para inmovilizar. No gastes el cuarto disparo en un súbdito cuando $danger esté indisponible."
            else -> "$me · ${role.getLocalizedName(lang)}. ${label(own.skill("P"), lang)}: ${fact(own.skill("P"), lang)} $h1: ${fact(own.skill("1"), lang)} $kitInteraction"
        }
        val ultimate = when (own.id) {
            "varus" -> if (pt) "$me: use $h4 para imobilizar $rival, aplique marcas durante o controle e detone com H1. ${flightResponse(enemy, lang)}"
                else "$me: usa $h4 para inmovilizar a $rival, aplica marcas durante el control y detona con H1. ${flightResponse(enemy, lang)}"
            "jhin" -> if (pt) "$me: abra $h4 de uma posição protegida, após $rival gastar $danger. Os disparos param no primeiro campeão; ajuste o ângulo se houver alguém protegendo o rival e use a lentidão para alinhar os próximos tiros."
                else "$me: abre $h4 desde una posición protegida, después de que $rival gaste $danger. Los disparos se detienen en el primer campeón; ajusta el ángulo si alguien protege al rival y usa la ralentización para alinear los siguientes tiros."
            else -> "$me · $h4: ${fact(own.skill("4"), lang)} $kitInteraction"
        }
        val late = when (own.id) {
            "varus" -> if (pt) "$me: mantenha o dano com ataques e detonações de Infecção no alvo acessível. Antes de disputar um objetivo contra $rival, use H1 para desgastar e guarde H4 para quem entrar na sua equipe; H3 reduz a cura dentro da área."
                else "$me: mantén el daño con ataques y detonaciones de Infección sobre el objetivo accesible. Antes de disputar un objetivo contra $rival, usa H1 para desgastar y guarda H4 para quien entre en tu equipo; H3 reduce la curación dentro del área."
            "jhin" -> if (pt) "$me: use H3 nas entradas do objetivo e H2 para prender $rival após o dano de um aliado. Planeje a recarga atrás da linha de frente: seus quatro disparos não oferecem dano contínuo. Use H4 para finalizar de longe depois de $danger."
                else "$me: coloca H3 en las entradas del objetivo y usa H2 para atrapar a $rival tras el daño de un aliado. Planea la recarga detrás de la primera línea: tus cuatro disparos no ofrecen daño continuo. Usa H4 para rematar desde lejos después de $danger."
            else -> if (pt) "$me: na ${role.getLocalizedName(lang)}, prepare a disputa contra $rival com ${label(own.skill("3"), lang)}: ${fact(own.skill("3"), lang)} Use $h4 de uma posição que permita responder a $danger."
                else "$me: en ${role.getLocalizedName(lang)}, prepara la disputa contra $rival con ${label(own.skill("3"), lang)}: ${fact(own.skill("3"), lang)} Usa $h4 desde una posición que te permita responder a $danger."
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
        val verdict = "$me contra $rival: $kitInteraction"
        return ChampionDuelPlan(early, ultimate, late, "$rival · $danger", "$counterplay\n\n$enemyUltimate", win, verdict)
    }

    private fun flightResponse(enemy: Champion, lang: String) = if (enemy.id == "smolder") {
        if (lang == "pt") "Espere a H3 de Smolder terminar ou mire no ponto de saída do voo; não desperdice a imobilização durante a fuga."
        else "Espera a que termine H3 de Smolder o apunta a la salida del vuelo; no gastes la inmovilización durante la huida."
    } else if (lang == "pt") "Espere ${label(enemy.threat(), lang)} ser gasto antes de iniciar."
        else "Espera a que gaste ${label(enemy.threat(), lang)} antes de iniciar."

    /** Four mandatory sections, grounded in the actual kit and observable game state. */
    fun sovereignFeedback(champion: Champion, role: LaneRole, language: String, enemy: Champion? = null): String {
        val lang = AppLanguage.normalize(language)
        val pt = lang == "pt"
        val me = champion.getLocalizedName(lang)
        val lane = role.getLocalizedName(lang)
        val primary = champion.skill("1")?.let { if (pt) "sua H1 (${it.getLocalizedName(lang)})" else "su H1 (${it.getLocalizedName(lang)})" }
            ?: if (pt) "sua Habilidade 1 (H1)" else "su Habilidad 1 (H1)"
        val escape = label(champion.skill("3"), lang)
        val ultimate = champion.skill("4")?.let { if (pt) "sua Definitiva (${it.getLocalizedName(lang)})" else "su Definitiva (${it.getLocalizedName(lang)})" }
            ?: if (pt) "sua Definitiva (H4)" else "su Definitiva (H4)"
        val danger = enemy?.let { label(it.threat(), lang) }
        val diagnostic = if (enemy != null) {
            if (pt) "$me contra ${enemy.getLocalizedName(lang)} em $lane: a janela depende de $danger. A seleção não informa vida, recargas nem posição do caçador; não presuma vantagem de troca apenas pelo confronto."
            else "$me contra ${enemy.getLocalizedName(lang)} en $lane: la ventana depende de $danger. El draft no informa vida, enfriamientos ni posición del jungla; no asumas ventaja de intercambio solo por el enfrentamiento."
        } else if (pt) "$me em $lane: perder o tempo entre a pressão da onda e a compra deixa sua condição de vitória sem recursos. Sem uma partida observada, isto é um risco a verificar, não um erro atribuído ao jogador."
            else "$me en $lane: perder el tempo entre la presión de la oleada y la compra deja tu condición de victoria sin recursos. Sin una partida observada, este es un riesgo por verificar, no un error atribuido al jugador."
        val decision = enemy?.let { interaction(champion, it, lang) } ?: if (pt)
            "Execute o mecanismo de $me: ${advice(champion, lang)} Reserve $ultimate para a janela em que seu alvo não possa neutralizá-la."
            else "Ejecuta el mecanismo de $me: ${advice(champion, lang)} Reserva $ultimate para la ventana en que tu objetivo no pueda neutralizarla."
        val macro = when (role) {
            LaneRole.JUNGLE -> if (pt) "Antes de cruzar o rio, confirme a prioridade das duas rotas próximas e a última posição do caçador rival. Sem prioridade, troque o objetivo por campos ou pressão no lado oposto; não inicie uma disputa sem informação do Castigo rival."
                else "Antes de cruzar el río, confirma la prioridad de las dos líneas cercanas y la última posición del jungla rival. Sin prioridad, intercambia el objetivo por campamentos o presión en el lado opuesto; no inicies una disputa sin información del Castigo rival."
            LaneRole.SUPPORT -> if (pt) "Saia da rota após uma onda entrar na torre rival ou no retorno combinado do atirador. Se a onda congelar contra seu aliado, resolva-a antes de abandonar a rota; preserve $escape para proteger a retirada."
                else "Sal de línea después de un crash contra la torre rival o durante el regreso coordinado del tirador. Si congelan la oleada contra tu aliado, resuélvela antes de abandonar la línea; conserva $escape para proteger la retirada."
            else -> if (pt) "Com o caçador rival sem localização, mantenha a onda no seu lado e preserve $escape. Para voltar à base ou rotacionar, forme uma onda lenta e faça o crash completo; se o rival segurar a onda fora da torre, cancele a saída até resolvê-la."
                else "Con el jungla rival sin localizar, conserva la oleada en tu lado y guarda $escape. Para regresar a base o rotar, forma un slow push y completa el crash; si el rival retiene la oleada fuera de torre, cancela la salida hasta resolverla."
        }
        val detail = "$primary: ${fact(champion.skill("1"), lang)}\n$ultimate: ${fact(champion.skill("4"), lang)}\n$macro\n" +
            if (pt) "Use o contador visível do objetivo: planeje sua compra e o trajeto antes de disputar. Confirme recargas na partida; não substitua o valor observado por um tempo fixo."
            else "Usa el contador visible del objetivo: planea la compra y el recorrido antes de disputar. Confirma los enfriamientos en partida; no sustituyas el dato observado por un tiempo fijo."
        val rule = if (pt) "Onda resolvida → recurso disponível → informação do caçador → decisão. Com $me, não transforme $primary em compromisso sem uma saída com $escape ou cobertura aliada."
            else "Oleada resuelta → recurso disponible → información del jungla → decisión. Con $me, no conviertas $primary en un compromiso sin salida con $escape o cobertura aliada."
        return listOf(
            (if (pt) "Diagnóstico do erro/situação" else "Diagnóstico del error/situación") to diagnostic,
            (if (pt) "Decisão Soberano" else "Decisión Soberano") to decision,
            (if (pt) "Micro e Macro detalhe" else "Micro y Macro detalle") to detail,
            (if (pt) "Regra aplicável" else "Regla aplicable") to rule
        ).joinToString("\n\n") { (title, body) -> "$title\n$body" }
    }

    fun championPlan(champion: Champion, role: LaneRole, language: String): String =
        sovereignFeedback(champion, role, language)
}
