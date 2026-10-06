package com.example

import com.example.util.BuildElementAdvice
import org.junit.Assert.*
import org.junit.Test

class BuildElementAdviceTest {
    @Test fun `advice belongs to the tapped element and not the first element of a build`() {
        val entries=listOf("Objeto A" to "Contra curaciones", "Objeto B" to "Contra escudos")
        assertEquals("Contra escudos", BuildElementAdvice.resolve("Objeto B",entries,"Consejo general"))
        assertEquals("Consejo general", BuildElementAdvice.resolve("Objeto C",entries,"Consejo general"))
    }
    @Test fun `rune punctuation and case do not hide its build advice`() {
        assertEquals("Para intercambios prolongados", BuildElementAdvice.resolve("LEYENDA: VELOCIDAD",
            listOf("Leyenda Velocidad" to "Para intercambios prolongados"),""))
    }
    @Test fun `blank notes fall back without replacing catalog mechanics`() {
        assertEquals("Consejo del catálogo", BuildElementAdvice.resolve("Prender",listOf("Prender" to " "),"Consejo del catálogo"))
        assertEquals("", BuildElementAdvice.resolve("Destello",emptyList()," "))
    }

    @Test fun `flash has no coaching even with a saved note or unrelated fallback`() {
        assertEquals("", BuildElementAdvice.resolve("Destello",listOf("Destello" to "Consejo antiguo"),"Consejo del primer objeto"))
        assertEquals("", BuildElementAdvice.resolve("FLASH",emptyList(),"Consejo general"))
    }

    @Test fun `copied rune mechanics do not become a second coach card`() {
        val mechanic="Otorga poder de habilidad al participar en eliminaciones."
        assertEquals("", BuildElementAdvice.resolve("Colección de Globos Oculares",listOf("Colección de Globos Oculares" to mechanic),"",mechanic))
        assertEquals("Prioriza participación segura", BuildElementAdvice.resolve("Colección de Globos Oculares",listOf("Colección de Globos Oculares" to "Prioriza participación segura"),"",mechanic))
    }

    @Test fun `situational item advice names concrete threats and a purchase trigger`() {
        val serylda = BuildElementAdvice.contextualItemAdvice("Rencor de Serylda", "Hwei", "Línea Central", "es", true)
        assertTrue(serylda.contains("Cuándo usar"))
        assertTrue(serylda.contains("Contra qué campeones/composiciones"))
        assertTrue(serylda.contains("Ornn"))
        assertTrue(serylda.contains("No aplica Heridas Graves"))
        assertFalse(serylda.contains("aplica 40% de Heridas Graves", ignoreCase = true))

        val maw = BuildElementAdvice.contextualItemAdvice("Fauces de Malmortius", "Hwei", "Línea Central", "es", true)
        assertTrue(maw.contains("Akali") || maw.contains("Syndra") || maw.contains("Fizz"))
        assertNotEquals(serylda, maw)
    }

    @Test fun `core item advice is about the tapped item instead of a repeated champion plan`() {
        val rabadon = BuildElementAdvice.contextualItemAdvice("Sombrero mortal de Rabadon", "Hwei", "Línea Central", "es", false)
        val liandry = BuildElementAdvice.contextualItemAdvice("Tormento de Liandry", "Hwei", "Línea Central", "es", false)
        assertTrue(rabadon.contains("Sombrero mortal de Rabadon"))
        assertTrue(rabadon.contains("Hwei"))
        assertTrue(rabadon.contains("Cuándo completarlo"))
        assertTrue(rabadon.contains("AP acumulado"))
        assertNotEquals(rabadon, liandry)
        assertFalse(rabadon.startsWith("Diagnóstico del error/situación"))
    }

    @Test fun `rune advice is specific to the selected rune and matchup condition`() {
        val comet = BuildElementAdvice.contextualRuneAdvice("Cometa Arcano", "Hwei", "Línea Central", "es", false)
        val bones = BuildElementAdvice.contextualRuneAdvice("Revestimiento de Huesos", "Hwei", "Línea Central", "es", true)
        assertTrue(comet.contains("Cuándo usarla"))
        assertTrue(comet.contains("Lux") || comet.contains("Orianna"))
        assertTrue(bones.contains("Renekton") || bones.contains("Pantheon"))
        assertNotEquals(comet, bones)
    }

    @Test fun `spell advice is matchup specific but flash never has coaching`() {
        assertEquals("", BuildElementAdvice.contextualSpellAdvice("Destello", "Hwei", "Línea Central", "es"))
        assertEquals("", BuildElementAdvice.contextualSpellAdvice("FLASH", "Hwei", "Línea Central", "es"))
        val exhaust = BuildElementAdvice.contextualSpellAdvice("Extenuación", "Hwei", "Línea Central", "es")
        val barrier = BuildElementAdvice.contextualSpellAdvice("Barrera", "Hwei", "Línea Central", "es")
        assertTrue(exhaust.contains("Zed") || exhaust.contains("Akali"))
        assertTrue(barrier.contains("Syndra") || barrier.contains("Lux"))
        assertNotEquals(exhaust, barrier)
    }
}
