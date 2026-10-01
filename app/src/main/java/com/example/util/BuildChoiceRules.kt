package com.example.util

import com.example.model.DamageType
import com.example.model.LaneRole

/** A situational choice needs a specific use case; an empty list is valid. */
object BuildChoiceRules {
    data class BootAlternative(val name: String, val reason: String)
    data class RuneChoice(val name: String, val branch: String)
    data class RuneAlternative(val rune: RuneChoice, val secondarySlot: Int?)

    fun boots(primary: String, damage: DamageType, frontline: Boolean, ranged: Boolean,
              role: LaneRole, championId: String = ""): List<BootAlternative> {
        val choice = when {
            frontline && championId !in setOf("olaf", "dr_mundo", "drmundo") -> BootAlternative(
                "Botas de mercurio", "Contra daño mágico y controles de masas que te impiden mantenerte en la primera línea.")
            ranged && damage == DamageType.PHYSICAL -> BootAlternative(
                "Botas blindadas", "Contra tiradores y duelistas cuyo daño depende de ataques básicos.")
            role == LaneRole.SUPPORT && !frontline -> BootAlternative(
                "Botas jonias de la lucidez", "Si necesitas lanzar escudos, curaciones y controles con más frecuencia.")
            role == LaneRole.TOP && !ranged -> BootAlternative(
                "Botas blindadas", "Si el rival de línea depende de ataques básicos y necesitas sobrevivir a sus intercambios.")
            damage == DamageType.PHYSICAL && role == LaneRole.JUNGLE -> BootAlternative(
                "Botas jonias de la lucidez", "Si priorizas más rotaciones de habilidades y hechizos sobre el daño de una sola ráfaga.")
            else -> null
        }
        return listOfNotNull(choice).filterNot { it.name.equals(primary, ignoreCase = true) }
    }

    /** Slots 1–3 keep their shared branch. Slot 4 may use another branch. */
    fun validRunePage(core: List<RuneChoice>): Boolean {
        if (core.size != 5 || core.first().branch != "Clave" || core.map { it.name.lowercase() }.distinct().size != 5) return false
        val branch = core[1].branch
        if (branch.isBlank() || branch == "Clave" || core.subList(1, 4).any { it.branch != branch } ||
            core[4].branch.isBlank() || core[4].branch == branch || core[4].branch == "Clave") return false
        return true
    }

    private val swapGroups = listOf(
        setOf("Brutal", "Triunfo", "Fervor de Batalla"),
        setOf("Último Esfuerzo", "Derribado", "Golpe de Gracia"),
        setOf("Leyenda: Presteza", "Leyenda: Velocidad", "Leyenda: Linaje"),
        setOf("Golpe Bajo", "Impacto Repentino", "Ataque Potenciado"),
        setOf("Asalto Encadenado", "Tirano", "Soberbia"),
        setOf("Colección de Globos Oculares", "Cazador Ingenioso", "Cazador Incesante", "Guardián Zombi"),
        setOf("Fuente de Vida", "Coraje del Coloso", "Orbe Anulador", "Inquebrantable"),
        setOf("Revestimiento de Huesos", "Fuerzas Renovadas"),
        setOf("Sobrecrecimiento", "Revitalizar", "Perseverancia"),
        setOf("Trascendencia", "Celeridad", "Concentración Absoluta"),
        setOf("Capa del Nimbo", "Piroláser", "Se Avecina Tormenta")
    )

    fun runeAlternatives(core: List<RuneChoice>, candidates: List<RuneChoice>): List<RuneAlternative> {
        if (!validRunePage(core)) return emptyList()
        val branch = core[1].branch
        return candidates.distinctBy { it.name.lowercase() }.mapNotNull { rune ->
            if (rune.branch.isBlank() || rune.branch == "Clave" || core.any { it.name.equals(rune.name, true) }) null
            else {
                val group = swapGroups.firstOrNull { names -> names.any { it.equals(rune.name, true) } }
                val sharedSlot = (1..3).firstOrNull { slot -> group?.any { it.equals(core[slot].name, true) } == true }
                RuneAlternative(rune, if (rune.branch == branch) sharedSlot else 4)
            }
        }
    }

    fun matchupLimit(premium: Boolean, signedIn: Boolean = true): Int = when {
        !signedIn -> 3
        premium -> 12
        else -> 6
    }
}
