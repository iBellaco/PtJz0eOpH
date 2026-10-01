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
        assertEquals("Consejo del catálogo", BuildElementAdvice.resolve("Destello",listOf("Destello" to " "),"Consejo del catálogo"))
        assertEquals("", BuildElementAdvice.resolve("Destello",emptyList()," "))
    }
}
