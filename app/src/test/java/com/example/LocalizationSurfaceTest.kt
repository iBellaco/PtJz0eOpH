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
import com.example.ui.screens.TierSelectionPanel
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
    @Test fun `real tier selector has ordered tabs and changes the displayed region`() {
        WildRiftRepository.initChampions(context, forceReload = true)
        context.getSharedPreferences("app_prefs", 0).edit().clear().commit()
        ChineseMetaSyncService.loadRegion(context)
        compose.setContent {
            val region by ChineseMetaSyncService.currentRegion.collectAsState()
            val scope = rememberCoroutineScope()
            TierSelectionPanel(TencentRankTier.DIAMOND_PLUS, ChineseSyncState.Idle, region, context, scope)
        }
        compose.onNodeWithTag("meta_region_GLOBAL").assertIsSelected()
        val cnLeft = compose.onNodeWithTag("meta_region_CN").fetchSemanticsNode().boundsInRoot.left
        val globalLeft = compose.onNodeWithTag("meta_region_GLOBAL").fetchSemanticsNode().boundsInRoot.left
        assertTrue(cnLeft < globalLeft)
        compose.onNodeWithTag("meta_region_NA").assertDoesNotExist()
        compose.onNodeWithTag("meta_region_CN").performClick().assertIsSelected()
        assertEquals("CN", WildRiftRepository.activeRegionName)
        compose.onNodeWithTag("meta_region_GLOBAL").performClick().assertIsSelected()
        assertEquals("GLOBAL", WildRiftRepository.activeRegionName)
    }
}
