package com.example

import com.example.util.SpellCatalogFormatting
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SpellCatalogFormattingTest {
    @Test
    fun `wild rift and howling abyss header becomes two labels`() {
        val source = "Mapas aplicables: Wild Rift, Abismo de los Lamentos\n\nOtorga un escudo durante 2,5 s."
        val result = SpellCatalogFormatting.split(source, "es")
        assertEquals(listOf("Wild Rift", "Abismo de los Lamentos"), result.mapLabels)
        assertEquals("Otorga un escudo durante 2,5 s.", result.description)
        assertFalse(result.description.contains("Mapas aplicables"))
    }

    @Test
    fun `conjunction and single map variants are parsed`() {
        val cleanse = SpellCatalogFormatting.split(
            "Mapas aplicables: Wild Rift y el Abismo de los Lamentos.\n\nElimina las inhabilitaciones.",
            "es"
        )
        assertEquals(listOf("Wild Rift", "Abismo de los Lamentos"), cleanse.mapLabels)

        val smite = SpellCatalogFormatting.split(
            "Mapa aplicable: Wild Rift\n\nInflige daño verdadero a monstruos.",
            "es"
        )
        assertEquals(listOf("Wild Rift"), smite.mapLabels)
    }

    @Test
    fun `portuguese catalog localizes howling abyss label`() {
        val result = SpellCatalogFormatting.split(
            "Mapas aplicables: Wild Rift, Abismo de los Lamentos\n\nTexto",
            "pt"
        )
        assertEquals(listOf("Wild Rift", "Abismo dos Lamentos"), result.mapLabels)
    }
}
