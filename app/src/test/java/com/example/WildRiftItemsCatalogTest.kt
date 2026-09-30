package com.example

import com.example.data.WildRiftItemsData
import com.example.data.WildRiftRepository
import org.junit.Assert.*
import org.junit.Test

class WildRiftItemsCatalogTest {

    @Test
    fun `support category contains all new support items and mission evolutions`() {
        val supportItems = WildRiftItemsData.getItemsForCategory("Apoyo")
        val supportIds = supportItems.map { it.id }.toSet()

        assertTrue("Hoz espectral must be in Apoyo", "spectral_sickle" in supportIds)
        assertTrue("Escudo reliquia must be in Apoyo", "relic_shield" in supportIds)
        assertTrue("Guadaña de la Niebla Negra must be in Apoyo", "black_mist_scythe" in supportIds)
        assertTrue("Baluarte de la montaña must be in Apoyo", "bulwark_of_the_mountain" in supportIds)
        assertTrue("Diadema susurrante must be in Apoyo", "whispering_headband" in supportIds)
        assertTrue("Diadema melodiosa must be in Apoyo", "diadem_of_songs" in supportIds)
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
