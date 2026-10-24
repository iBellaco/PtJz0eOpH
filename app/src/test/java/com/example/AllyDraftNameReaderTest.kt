package com.example

import com.example.model.Champion
import com.example.model.LaneRole
import com.example.service.screen.AllyDraftNameReader
import com.example.service.screen.ChampionNameResolver
import org.junit.Assert.*
import org.junit.Test

class AllyDraftNameReaderTest {
    @Test fun compressedWholeTitlesAreCorrectedWithoutAcceptingNicknamesOrAbbreviations() {
        val champs=listOf("nocturne","volibear").map { Champion(id=it,name=it) }
        assertEquals("nocturne",ChampionNameResolver.findChampionInNameBand("NDCTURNE",champs)?.id)
        for(text in listOf("VOLIREAR","VOLIEAR","V0LIREAR"))
            assertEquals("volibear",ChampionNameResolver.findChampionInNameBand(text,champs)?.id)
        for(text in listOf("XCS NDCTURNE","Mi VOLIREAR","Jugador 3 VOLIEAR","XCS V0LIREAR","0LIREAR","NDC","VLR","V","IA"))
            assertNull(text,ChampionNameResolver.findChampionInNameBand(text,champs))
        val lanes=mapOf("CALLE DEL DAAGCN" to LaneRole.ADC,"CALLE DEL BARCN" to LaneRole.TOP,
            "CALE CNTRAL" to LaneRole.MID,"JNALA" to LaneRole.JUNGLE,"APOrO" to LaneRole.SUPPORT)
        lanes.forEach { (text,role)->assertEquals(text,role,AllyDraftNameReader.read(listOf(text),champs).lane) }
        for(text in listOf("XCS JNALA","Jugador CALLE DEL DAAGCN","Mi CALE CNTRAL"))
            assertNull(text,AllyDraftNameReader.read(listOf(text),champs).lane)
    }
    @Test fun intactTitleWinsOverBadgeRemovalAndUnknownTitlesCanUseTheAlternative() {
        val original = AllyDraftNameReader.Reading(champion = Champion(id = "jhin", name = "Jhin"))
        val altered = AllyDraftNameReader.Reading(champion = Champion(id = "jinx", name = "Jinx"))
        assertEquals(original, AllyDraftNameReader.preferOriginal(original, altered))
        val lane = AllyDraftNameReader.Reading(lane = LaneRole.MID)
        assertEquals(lane, AllyDraftNameReader.preferOriginal(lane, altered))
        assertEquals(original, AllyDraftNameReader.preferOriginal(AllyDraftNameReader.Reading(), original))
        val ambiguous = AllyDraftNameReader.Reading(ambiguous = true)
        assertEquals(ambiguous, AllyDraftNameReader.preferOriginal(ambiguous, altered))
    }
    private val catalog = listOf("yuumi", "jinx", "brand", "skarner", "pantheon", "vi", "lee_sin").map {
        Champion(id = it, name = if (it == "lee_sin") "Lee Sin" else it)
    }

    @Test fun completeTitleFragmentsJoinOnlyInsideTheSameTopRow() {
        assertEquals("vi",AllyDraftNameReader.readTitleRows(listOf("V" to 0,"I" to 1),10,catalog).champion?.id)
        assertEquals("lee_sin",AllyDraftNameReader.readTitleRows(listOf("LEE" to 0,"SIN" to 1),10,catalog).champion?.id)
        assertNull(AllyDraftNameReader.readTitleRows(listOf("V" to 0,"I" to 25),10,catalog).champion)
        assertNull(AllyDraftNameReader.readTitleRows(listOf("V" to 0,"XCS" to 0,"I" to 0),10,catalog).champion)
        assertTrue(AllyDraftNameReader.readTitleRows(listOf("JINX" to 0,"YUUMI" to 1),10,catalog).ambiguous)
    }

    @Test fun laneTitlesBecomeNamesInTheSameBandInBothLanguages() {
        val labels = listOf("JUNGLA" to LaneRole.JUNGLE, "CALLE DEL DRAGÓN" to LaneRole.ADC,
            "CALLE CENTRAL" to LaneRole.MID, "CALLE DEL BARÓN" to LaneRole.TOP, "APOYO" to LaneRole.SUPPORT,
            "SELVA" to LaneRole.JUNGLE, "ROTA DO DRAGÃO" to LaneRole.ADC,
            "ROTA DO MEIO" to LaneRole.MID, "ROTA DO BARÃO" to LaneRole.TOP, "SUPORTE" to LaneRole.SUPPORT)
        labels.forEach { (text, lane) -> assertEquals(text, lane, AllyDraftNameReader.read(listOf(text), catalog).lane) }
        for (name in catalog) assertEquals(name, AllyDraftNameReader.read(listOf(name.name.uppercase()), catalog).champion)
    }

    @Test fun playerNicknamesAndMixedReadingsCannotConfirmAnotherChampion() {
        for (text in listOf("XCS Yuumi", "Mi Brand", "Skarner99", "Jugador 3 Brand", "SuperJinx", "Diego (Brand)"))
            assertNull(text, ChampionNameResolver.findChampionInNameBand(text, catalog))
        assertTrue(AllyDraftNameReader.read(listOf("JUNGLA", "YUUMI"), catalog).ambiguous)
        assertTrue(AllyDraftNameReader.read(listOf("JINX", "YUUMI"), catalog).ambiguous)
    }

    @Test fun explicitMasteryPrefixesAndSelfTagKeepTheWholeChampionName() {
        for (text in listOf("• JINX", "M7 JINX", "M7JINX", "VII JINX", "WJINX", "JINX (TÚ)"))
            assertEquals(text, "jinx", ChampionNameResolver.findChampionInNameBand(text, catalog)?.id)
        assertEquals("vi", ChampionNameResolver.findChampionInNameBand("V1", catalog)?.id)
        assertEquals("lee_sin", ChampionNameResolver.findChampionInNameBand("LEE SIN", catalog)?.id)
    }

    @Test fun ownSlotRequiresASelfMarkerOrExactKnownName() {
        assertTrue(AllyDraftNameReader.identifiesUser("(TÚ)", emptyList()))
        assertTrue(AllyDraftNameReader.identifiesUser("Jinx (Você)", emptyList()))
        assertTrue(AllyDraftNameReader.identifiesUser("D I E G O", listOf("Diego")))
        assertFalse(AllyDraftNameReader.identifiesUser("DiegoFan", listOf("Diego")))
        assertFalse(AllyDraftNameReader.identifiesUser("Diego", listOf("Otro")))
        assertFalse(AllyDraftNameReader.identifiesUser("Porcentaje de victorias", emptyList()))
    }

    @Test fun exactChampionNameOnPlayerSecondLineCannotReplaceTheTitle() {
        assertEquals("pantheon", AllyDraftNameReader.readTitleRows(listOf("PANTHEON" to 0, "YUUMI" to 25), 10, catalog).champion?.id)
        assertEquals(LaneRole.JUNGLE, AllyDraftNameReader.readTitleRows(listOf("JUNGLA" to 0, "JINX" to 25), 10, catalog).lane)
        assertNull(AllyDraftNameReader.readTitleRows(listOf("???" to 0, "JINX" to 25), 10, catalog).champion)
        assertNull(AllyDraftNameReader.readTitleRows(listOf("JINX" to 25), 10, catalog, titleMaxCenterY = 10).champion)
        assertEquals(LaneRole.TOP, AllyDraftNameReader.readTitleRows(listOf("CALLE DEL" to 0, "BARÓN" to 15), 10, catalog).lane)
    }
}
