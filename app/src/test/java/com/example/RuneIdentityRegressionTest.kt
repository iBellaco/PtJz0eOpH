package com.example

import com.example.data.WildRiftRepository
import com.example.data.WildRiftSpellsAndRunes
import com.example.util.BuildElementAdvice
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RuneIdentityRegressionTest {
    @Test fun aftershockDoesNotOpenGraspOrGlacialAndLocalizedEffectsRemainIndividual() {
        WildRiftRepository.initChampions(RuntimeEnvironment.getApplication(), forceReload = true)
        val aftershock = WildRiftSpellsAndRunes.getRuneByName("Aftershock")!!
        val grasp = WildRiftSpellsAndRunes.getRuneByName("Garras del Inmortal")!!
        val glacial = WildRiftSpellsAndRunes.getRuneByName("Soberano Gélido")!!
        assertEquals("Réplica", aftershock.name)
        assertNotEquals(grasp.id, aftershock.id)
        assertNotEquals(glacial.id, aftershock.id)
        assertEquals(aftershock.iconUrl, WildRiftSpellsAndRunes.getRuneIconByName("Aftershock"))
        for (lang in listOf("es", "pt")) {
            assertNotEquals(grasp.getLocalizedDescription(lang), aftershock.getLocalizedDescription(lang))
            assertNotEquals(BuildElementAdvice.contextualRuneAdvice(grasp.name, "Alistar", "Soporte", lang, true),
                BuildElementAdvice.contextualRuneAdvice(aftershock.name, "Alistar", "Soporte", lang, true))
        }
    }

    @Test fun differentRunesAreNotAliasesAndPartialNamesCannotChooseAnUnrelatedRune() {
        for (name in listOf("Claridad Mental", "Perspicacia Cósmica", "Mercado del Futuro", "Cazador Voraz", "Leyenda: Tenacidad", "Leyenda")) {
            assertNull(name, WildRiftSpellsAndRunes.getRuneByName(name))
        }
    }
}
