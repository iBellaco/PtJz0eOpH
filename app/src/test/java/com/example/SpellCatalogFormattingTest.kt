package com.example

import com.example.util.SpellCatalogFormatting
import com.example.data.WildRiftSpellsAndRunes
import kotlinx.serialization.json.Json
import java.io.File
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

    @Test fun `singular portuguese headers and translated map names are recognized`() {
        for (header in listOf("Mapa aplicável", "Mapa disponível", "Mapas aplicáveis", "Mapas disponíveis")) {
            val result = SpellCatalogFormatting.split("$header: Howling Abyss\r\n\r\nEfeito", "pt-BR")
            assertEquals(listOf("Abismo dos Lamentos"), result.mapLabels)
            assertEquals("Efeito", result.description)
        }
        assertEquals(listOf("Wild Rift", "Abismo dos Lamentos"), SpellCatalogFormatting.split(
            "Mapas aplicáveis: Wild Rift e o Abismo dos Lamentos.\nEfeito", "pt").mapLabels)
    }

    @Test fun `every catalog spell separates real spanish and portuguese map headers from mechanics`() {
        val translations = Json.decodeFromString<Map<String, String>>(File("src/main/assets/translations_pt.json").readText())
        for (spell in WildRiftSpellsAndRunes.summonerSpells) {
            val spanish = SpellCatalogFormatting.split(spell.description, "es")
            val portuguese = SpellCatalogFormatting.split(translations[spell.description] ?: spell.description, "pt")
            assertFalse("${spell.id}: missing Spanish map labels", spanish.mapLabels.isEmpty())
            assertEquals("${spell.id}: Portuguese must retain all maps", spanish.mapLabels.size, portuguese.mapLabels.size)
            assertFalse("${spell.id}: missing mechanics", portuguese.description.isBlank())
            assertFalse(portuguese.description.startsWith("Mapa"))
            assertFalse(portuguese.mapLabels.contains("Howling Abyss"))
            assertFalse(portuguese.mapLabels.contains("Abismo de los Lamentos"))
        }
    }

    @Test fun `descriptions without map headers retain their complete mechanics`() {
        val text = "Inflige daño.\n\nPuede reactivarse."
        assertEquals(text, SpellCatalogFormatting.split(text, "es").description)
        assertEquals(emptyList<String>(), SpellCatalogFormatting.split(text, "es").mapLabels)
    }
}
