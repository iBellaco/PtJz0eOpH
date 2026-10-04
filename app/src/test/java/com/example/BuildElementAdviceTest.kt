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
}
