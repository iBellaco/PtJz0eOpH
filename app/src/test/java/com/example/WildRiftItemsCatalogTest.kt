package com.example

import com.example.data.WildRiftItemsData
import com.example.data.WildRiftRepository
import org.junit.Assert.*
import org.junit.Test

class WildRiftItemsCatalogTest {

    @Test fun `components are strictly grouped and repeated data is shared`() {
        val counts = mapOf("Luchador" to (4 to 10), "Asesino" to (3 to 7), "Tirador" to (4 to 13),
            "Mágico" to (4 to 18), "Defensa" to (4 to 16), "Apoyo" to (0 to 15))
        val components = com.example.data.WildRiftComponentItemsData
        assertEquals(52, components.items.size)
        var memberships = 0
        counts.forEach { (section, expected) ->
            val basic = components.getItems(section, "Básico")
            val medium = components.getItems(section, "Nivel Medio")
            assertEquals(expected.first, basic.size)
            assertEquals(expected.second, medium.size)
            memberships += basic.size + medium.size
            (basic + medium).forEach { assertSame(it, WildRiftItemsData.getItemById(it.id)) }
            assertEquals(basic.map { it.id }, WildRiftItemsData.getCatalogGroups(section).firstOrNull { it.level == "Básico" }?.items.orEmpty().map { it.id })
        }
        assertEquals(98, memberships)
        assertTrue(components.getItems("Apoyo","Básico").isEmpty())
        assertEquals(listOf("spectral_sickle","relic_shield"),WildRiftItemsData.getCatalogGroups("Apoyo").first { it.level == "Inicial" }.items.map { it.id })
        assertNull(WildRiftItemsData.getItemByName("objeto inexistente zzz987"))
        assertEquals("quicksilver_sash_mid_tier", WildRiftItemsData.getItemByName("quicksilver sash")?.id)
        assertEquals("quicksilver_sash_mid_tier", WildRiftItemsData.getItemByName("fajin de mercurio")?.id)
        assertEquals(300, WildRiftItemsData.getItemById("revelation_ring")!!.goldCost)
        val bramble = WildRiftItemsData.getItemById("bramble_vest")!!
        assertTrue(bramble.stats.isEmpty())
        assertFalse(bramble.passive.any { it.isDigit() })
    }

    @Test
    fun `support category contains basic starter items and completed support items`() {
        val supportItems = WildRiftItemsData.getItemsForCategory("Apoyo")
        val supportIds = supportItems.map { it.id }

        assertEquals(27, supportItems.size)
        assertEquals("spectral_sickle", supportIds[0])
        assertEquals("relic_shield", supportIds[1])
        assertEquals("black_mist_scythe", supportIds[2])
        assertEquals("bulwark_of_the_mountain", supportIds[3])
        assertEquals("echoes_of_helia", supportIds[4])
        assertEquals("whispering_headband", supportIds[5])
        assertEquals("diadem_of_songs", supportIds[6])

        assertTrue("Hoz espectral must be in Apoyo", "spectral_sickle" in supportIds)
        assertTrue("Escudo reliquia must be in Apoyo", "relic_shield" in supportIds)
        assertTrue("Guadaña de la Niebla Negra must be in Apoyo", "black_mist_scythe" in supportIds)
        assertTrue("Baluarte de la montaña must be in Apoyo", "bulwark_of_the_mountain" in supportIds)
        assertTrue("Diadema susurrante must be in Apoyo", "whispering_headband" in supportIds)
        assertTrue("Diadema melodiosa must be in Apoyo", "diadem_of_songs" in supportIds)
        assertFalse("Moneda antigua must be deleted", "ancient_coin" in supportIds)
    }

    @Test
    fun `evolution items have zero gold cost and declare evolvesFrom`() {
        val muramana = WildRiftItemsData.getItemById("muramana")
        assertNotNull(muramana)
        assertEquals(0, muramana!!.goldCost)
        assertEquals("Manamune", muramana.evolvesFrom)

        val diadem = WildRiftItemsData.getItemById("diadem_of_songs")
        assertNotNull(diadem)
        assertEquals(0, diadem!!.goldCost)
        assertEquals("Diadema susurrante", diadem.evolvesFrom)

        val seraph = WildRiftItemsData.getItemById("seraph_s_embrace")
        assertNotNull(seraph)
        assertEquals(0, seraph!!.goldCost)
        assertEquals("Báculo del arcángel", seraph.evolvesFrom)

        val granInvierno = WildRiftItemsData.getItemById("fimbulwinter")
        assertNotNull(granInvierno)
        assertEquals(0, granInvierno!!.goldCost)
        assertEquals("Llegada del invierno", granInvierno.evolvesFrom)

        val scythe = WildRiftItemsData.getItemById("black_mist_scythe")
        assertNotNull(scythe)
        assertEquals(0, scythe!!.goldCost)
        assertEquals("Hoz espectral", scythe.evolvesFrom)

        val bulwark = WildRiftItemsData.getItemById("bulwark_of_the_mountain")
        assertNotNull(bulwark)
        assertEquals(0, bulwark!!.goldCost)
        assertEquals("Escudo reliquia", bulwark.evolvesFrom)
    }

    @Test
    fun `all updated categories have exact counts and order`() {
        val asesino = WildRiftItemsData.getItemsForCategory("Asesino")
        assertEquals(11, asesino.size)
        assertEquals("serpent_s_fang", asesino.first().id)
        assertEquals("guardian_angel", asesino.last().id)

        val tirador = WildRiftItemsData.getItemsForCategory("Tirador")
        assertEquals(29, tirador.size)
        assertEquals("fiendhunter_bolts", tirador.first().id)
        assertEquals("infinity_edge", tirador.last().id)

        val magico = WildRiftItemsData.getItemsForCategory("Mágico")
        assertEquals(30, magico.size)
        assertFalse("crown_of_the_shattered_queen" in magico.map { it.id })
        assertFalse("void_staff" in magico.map { it.id })

        val defensa = WildRiftItemsData.getItemsForCategory("Defensa")
        assertEquals(31, defensa.size)
        assertEquals("midday_tunic", defensa[5].id)
        assertFalse("mantle_of_the_twelfth_hour" in defensa.map { it.id })
        assertFalse("searing_crown" in defensa.map { it.id })
    }

    @Test
    fun `magic category contains Seraphs Embrace and Archangel Staff`() {
        val magicItems = WildRiftItemsData.getItemsForCategory("Mágico")
        val magicIds = magicItems.map { it.id }.toSet()

        assertTrue("Abrazo del serafín must be in Mágico", "seraph_s_embrace" in magicIds)
        assertTrue("Báculo del arcángel must be in Mágico", "archangel_s_staff" in magicIds)
    }

    @Test
    fun `item lookup by name and aliases resolves accurately`() {
        assertNotNull(WildRiftItemsData.getItemByName("Hoz espectral"))
        assertNotNull(WildRiftItemsData.getItemByName("hoz"))
        assertNotNull(WildRiftItemsData.getItemByName("Escudo reliquia"))
        assertNotNull(WildRiftItemsData.getItemByName("relic shield"))
        assertNotNull(WildRiftItemsData.getItemByName("Guadaña de la Niebla Negra"))
        assertNotNull(WildRiftItemsData.getItemByName("Baluarte de la montaña"))
        assertNotNull(WildRiftItemsData.getItemByName("Diadema melodiosa"))
        assertNotNull(WildRiftItemsData.getItemByName("Diadema susurrante"))
        assertNotNull(WildRiftItemsData.getItemByName("Abrazo del serafín"))
        assertNotNull(WildRiftItemsData.getItemByName("seraph's embrace"))

        assertEquals("spectral_sickle", WildRiftItemsData.getItemByName("Hoz espectral")?.id)
        assertEquals("relic_shield", WildRiftItemsData.getItemByName("Escudo reliquia")?.id)
        assertEquals("black_mist_scythe", WildRiftItemsData.getItemByName("Guadaña de la Niebla Negra")?.id)
        assertEquals("bulwark_of_the_mountain", WildRiftItemsData.getItemByName("Baluarte de la montaña")?.id)
        assertEquals("diadem_of_songs", WildRiftItemsData.getItemByName("Diadema melodiosa")?.id)
        assertEquals("seraph_s_embrace", WildRiftItemsData.getItemByName("Abrazo del serafín")?.id)
    }

    @Test
    fun `item stats match official in-game properties`() {
        val diadema = WildRiftItemsData.getItemByName("Diadema melodiosa")
        assertNotNull(diadema)
        assertTrue(diadema!!.stats.contains("+1200 Maná máximo"))
        assertTrue(diadema.stats.contains("+8% Poder de curaciones y escudos"))
        assertTrue(diadema.passive.contains("0,25%"))
        assertTrue(diadema.passive.contains("0,8%"))

        val seraph = WildRiftItemsData.getItemByName("Abrazo del serafín")
        assertNotNull(seraph)
        assertTrue(seraph!!.stats.contains("+1200 Maná máximo"))
        assertTrue(seraph.stats.contains("+25 Velocidad de habilidades"))
        assertTrue(seraph.passive.contains("16%"))

        val granInvierno = WildRiftItemsData.getItemByName("El gran invierno")
        assertNotNull(granInvierno)
        assertEquals("fimbulwinter", granInvierno!!.id)
        assertTrue(granInvierno.stats.contains("+500 Vida máxima"))
        assertTrue(granInvierno.stats.contains("+1200 Maná máximo"))
        assertTrue(granInvierno.passive.contains("Coloso helado"))

        val muramana = WildRiftItemsData.getItemByName("Muramana")
        assertNotNull(muramana)
        assertEquals("muramana", muramana!!.id)
        assertTrue(muramana.stats.contains("+40 Daño de ataque"))
        assertTrue(muramana.stats.contains("+1200 Maná máximo"))
        assertTrue(muramana.passive.contains("Impacto"))
    }

    @Test
    fun `accent insensitive item lookup works reliably`() {
        assertNotNull(WildRiftItemsData.getItemByName("serafin"))
        assertNotNull(WildRiftItemsData.getItemByName("guadana"))
        assertNotNull(WildRiftItemsData.getItemByName("montana"))
        assertNotNull(WildRiftItemsData.getItemByName("arcangel"))
        assertEquals("seraph_s_embrace", WildRiftItemsData.getItemByName("serafin")?.id)
        assertEquals("black_mist_scythe", WildRiftItemsData.getItemByName("guadana")?.id)
        assertEquals("bulwark_of_the_mountain", WildRiftItemsData.getItemByName("montana")?.id)
    }

    @Test
    fun `all official categories contain items without omissions`() {
        for (category in WildRiftItemsData.officialCategoryOrder) {
            val items = WildRiftItemsData.getItemsForCategory(category)
            assertTrue("Category $category should not be empty", items.isNotEmpty())
        }
    }

    @Test
    fun `catalog contains zero duplicate items and zero legacy PC items`() {
        val allItems = WildRiftItemsData.list
        val ids = allItems.map { it.id }
        val names = allItems.map { it.name }

        val duplicateIds = ids.groupBy { it }.filter { it.value.size > 1 }.keys
        val duplicateNames = names.groupBy { it }.filter { it.value.size > 1 }.keys

        assertTrue("Found duplicate item IDs: $duplicateIds", duplicateIds.isEmpty())
        assertTrue("Found duplicate item Names: $duplicateNames", duplicateNames.isEmpty())

        val legacyIds = setOf("quicksilver_sash", "seeker_s_armguard", "soul_transfer")
        for (legacy in legacyIds) {
            assertNull("Legacy item $legacy must not exist in catalog", allItems.find { it.id == legacy })
        }
    }
}
