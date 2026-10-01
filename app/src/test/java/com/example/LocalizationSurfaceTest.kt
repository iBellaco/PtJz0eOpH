package com.example

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.util.*
import com.example.data.WildRiftRepository
import com.example.data.sync.*
import com.example.ui.components.FormattedWildRiftText
import com.example.ui.screens.FAQScreen
import com.example.ui.screens.InfoScreen
import com.example.ui.screens.LanguageSelectionScreen
import com.example.ui.components.PrivacyPolicyDialog
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class LocalizationSurfaceTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = RuntimeEnvironment.getApplication()
    @Before fun prepare() {
        DynamicTranslations.loadSync(context)
        AppLanguage.select(context, "es")
    }
    @Test fun `styled sentence translates as a complete sentence in both directions`() {
        val original = "Curación y Escudos"
        compose.setContent {
            Column {
                FormattedWildRiftText(original)
                Text(tr(AnnotatedString(original, spanStyles = listOf(AnnotatedString.Range(SpanStyle(fontWeight = FontWeight.Bold), 0, 8)))))
            }
        }
        compose.onAllNodesWithText(original).assertCountEquals(2)
        compose.runOnIdle { AppLanguage.select(context, "pt") }
        compose.onAllNodesWithText("Curação e Escudos").assertCountEquals(2)
        compose.onAllNodesWithText(original).assertCountEquals(0)
        compose.runOnIdle { AppLanguage.select(context, "es") }
        compose.onAllNodesWithText(original).assertCountEquals(2)
        compose.onAllNodesWithText("Curação e Escudos").assertCountEquals(0)
    }
    @Test fun `FAQ screen updates its actual questions and headings immediately`() {
        compose.setContent {
            val language by AppLanguage.current.collectAsState()
            CompositionLocalProvider(LocalLanguage provides language) { FAQScreen(onNavigateBack = {}) }
        }
        compose.onNodeWithText("Preguntas Frecuentes").assertExists()
        compose.runOnIdle { AppLanguage.select(context, "pt") }
        compose.onNodeWithText("Perguntas Frequentes").assertExists()
        compose.onNodeWithText("Como funciona o overlay flutuante durante a partida?").assertExists()
        compose.onNodeWithText("¿Cómo funciona el overlay flotante durante la partida?").assertDoesNotExist()
        compose.runOnIdle { AppLanguage.select(context, "es") }
        compose.onNodeWithText("Preguntas Frecuentes").assertExists()
        compose.onNodeWithText("Perguntas Frequentes").assertDoesNotExist()
    }
    @Test fun `legacy region resolves to Global without a selector`() {
        assertEquals("GLOBAL", com.example.data.sync.MetaRegion.normalize("CN"))
        assertEquals(listOf("GLOBAL"), com.example.data.sync.MetaRegion.available)
    }

    @Test fun `standalone catalog fields and cached advice follow both language switches`() {
        val champion = com.example.model.Champion(name = "Hwei", skills = listOf(
            com.example.model.ChampionSkill(slot = "1", name = "Desastre", namePt = "Desastre")
        ))
        compose.setContent {
            val language = currentAppLanguage()
            val analysis = remember(language) {
                CoachingGenerator.generateTacticalAnalysis(champion, com.example.model.LaneRole.MID, language)
            }
            Column {
                Text(com.example.model.LaneRole.MID.getLocalizedName(language))
                Text(analysis)
            }
        }
        compose.onNodeWithText("Línea Central").assertExists()
        compose.runOnIdle { AppLanguage.select(context, "pt-BR") }
        compose.onNodeWithText("Rota do Meio").assertExists()
        compose.onNodeWithText("sua H1 (Desastre)", substring = true).assertExists()
        compose.onNodeWithText("Línea Central", substring = true).assertDoesNotExist()
        compose.runOnIdle { AppLanguage.select(context, "es") }
        compose.onNodeWithText("Línea Central").assertExists()
        compose.onNodeWithText("su H1 (Desastre)", substring = true).assertExists()
        compose.onNodeWithText("Rota do Meio", substring = true).assertDoesNotExist()
    }

    @Test fun `Portuguese locale variants are normalized for model fields`() {
        compose.setContent {
            CompositionLocalProvider(LocalLanguage provides "pt-BR") {
                Text(com.example.model.LaneRole.MID.getLocalizedName(currentAppLanguage()))
            }
        }
        compose.onNodeWithText("Rota do Meio").assertExists()
        compose.onNodeWithText("Línea Central").assertDoesNotExist()
    }

    @Test fun `initial Portuguese selection opens the complete information panel in Portuguese`() {
        compose.setContent {
            var selected by remember { mutableStateOf(false) }
            if (selected) {
                InfoScreen(onNavigateBack = {}, onNavigateToFAQ = {})
            } else {
                LanguageSelectionScreen { language ->
                    AppLanguage.select(context, language)
                    selected = true
                }
            }
        }
        compose.onNodeWithText("Português").performClick()
        compose.onNodeWithText("Continuar em Português").performClick()
        compose.onNodeWithText("Informação").assertIsDisplayed()
        compose.onNodeWithText("Información").assertDoesNotExist()
        compose.onNodeWithText("1. Compatibilidade e Patch Oficial").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Passo 1: Configure suas Rotas de Jogo").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Passo 4: Consulta de Builds e Runas").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Informações Legais e Privacidade").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Todos os direitos reservados.", substring = true).performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText("Compilação", substring = true).assertCountEquals(2)

        compose.runOnIdle { AppLanguage.select(context, "es") }
        compose.onNodeWithText("Información").assertIsDisplayed()
        compose.onNodeWithText("Paso 1: Configura tus Líneas de Juego").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Passo 1: Configure suas Rotas de Jogo").assertDoesNotExist()
        compose.runOnIdle { AppLanguage.select(context, "pt-BR") }
        compose.onNodeWithText("Passo 1: Configure suas Rotas de Jogo").assertIsDisplayed()
        compose.onNodeWithText("Paso 1: Configura tus Líneas de Juego").assertDoesNotExist()
    }

    @Test fun `mandatory legal information starts in Portuguese on every tab`() {
        AppLanguage.select(context, "pt")
        compose.setContent { PrivacyPolicyDialog(isMandatoryAcceptance = true, onDismiss = {}) }
        compose.onNodeWithText("Informações Legais").assertIsDisplayed()
        compose.onNodeWithText("1. Quais dados coletamos e por quê").assertExists()
        compose.onNodeWithText("Termos").performClick()
        compose.onNodeWithText("1. Aceitação dos Termos").assertExists()
        compose.onNodeWithText("Terceiros").performClick()
        compose.onNodeWithText("1. Isenção de Responsabilidade Oficial da Riot Games").assertExists()
        compose.onNodeWithText("Aceitar e Entrar").assertIsDisplayed()
        compose.onNodeWithText("Aceptar y Entrar").assertDoesNotExist()
    }

}
