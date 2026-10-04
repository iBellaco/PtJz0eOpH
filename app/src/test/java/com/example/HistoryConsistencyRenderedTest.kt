package com.example

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.*
import com.example.model.SubscriptionRecord
import com.example.ui.components.*
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppLanguage
import com.example.util.DynamicTranslations
import com.github.takahirom.roborazzi.captureRoboImage
import java.io.File
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w411dp-h891dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HistoryConsistencyRenderedTest(private val language: String) {
    companion object {
        @JvmStatic @ParameterizedRobolectricTestRunner.Parameters(name="{0}")
        fun languages() = listOf(arrayOf("es"), arrayOf("pt"))
    }
    @get:Rule val compose = createComposeRule()
    @Before fun setup() {
        val context = RuntimeEnvironment.getApplication()
        DynamicTranslations.loadSync(context)
        AppLanguage.select(context, language)
    }
    private fun capture(name: String) {
        val out = File("build/reports/portuguese-rendered").apply { mkdirs() }
        compose.onRoot().captureRoboImage(File(out, "history-consistency-$name-$language.png").path)
    }
    @Test fun `managed balance updates without a personal recharge action`() {
        val balances = mutableStateOf(HistoryBalances(0, 100))
        var recharged = false
        compose.setContent { MyApplicationTheme { HistoryEssenceBalanceBar(balances.value, false) { recharged = true } } }
        compose.onNodeWithText("0 EA").assertExists()
        compose.onNodeWithText("100 EN").assertExists()
        compose.onNodeWithText(if (language=="pt") "+ Recarregar" else "+ Recargar").assertDoesNotExist()
        compose.runOnIdle { balances.value = HistoryBalances(25, 90) }
        compose.onNodeWithText("25 EA").assertExists()
        compose.onNodeWithText("90 EN").assertExists()
        compose.onNodeWithText("100 EN").assertDoesNotExist()
        Assert.assertFalse(recharged)
        capture("managed-wallet")
    }
    @Test fun `personal wallet retains recharge and orange selection`() {
        var currency = ""
        compose.setContent { MyApplicationTheme { HistoryEssenceBalanceBar(HistoryBalances(50, 10), true) { currency = it } } }
        compose.onNodeWithText("10 EN").performClick()
        compose.runOnIdle { Assert.assertEquals("ORANGE", currency) }
        compose.onNodeWithText(if(language=="pt") "+ Recarregar" else "+ Recargar").performClick()
        compose.runOnIdle { Assert.assertEquals("BLUE", currency) }
    }
    @Test fun `unloaded managed wallet is unknown instead of the viewers balance`() {
        compose.setContent { MyApplicationTheme { HistoryEssenceBalanceBar(null, false) {} } }
        compose.onAllNodesWithText(if(language=="pt") "Não disponível" else "No disponible").assertCountEquals(2)
        compose.onNodeWithText("0 EA").assertDoesNotExist()
        compose.onNodeWithText("0 EN").assertDoesNotExist()
    }
    @Test fun `withdrawn premium displays its removal date without lifetime expiry`() {
        compose.setContent { MyApplicationTheme { SubscriptionHistoryItem(SubscriptionRecord(
            id="removed", timestamp=1791021600000, planName="Suscripción Premium retirada", status="Completado", amount="0", source="ADMIN_REVOCATION")) } }
        compose.onAllNodesWithText(if(language=="pt") "Assinatura Premium removida" else "Suscripción Premium retirada").assertCountEquals(1)
        compose.onNodeWithText(if(language=="pt") "Assinatura removida" else "Suscripción retirada").assertExists()
        compose.onAllNodesWithText("Vitalicio", substring=true).assertCountEquals(0)
        compose.onAllNodesWithText("Vitalício", substring=true).assertCountEquals(0)
        compose.onAllNodesWithText(if(language=="pt") "Vence:" else "Vence:", substring=true).assertCountEquals(0)
        capture("removed-premium")
    }
    @Test fun `known count survives size failure and denied access has no false zero`() {
        compose.setContent { MyApplicationTheme { SavedDataStatisticsContent(listOf(
            SavedDataStatistic("Movimientos del historial", "Registros guardados y sincronizados entre dispositivos.", count=42,
                failureReason="Recuento disponible. No se pudo estimar el tamaño de esta categoría."),
            SavedDataStatistic("Contadores de streamers", "Clics acumulados de cada publicación.", failed=true,
                failureReason="El servicio rechazó el acceso a esta categoría. Sus permisos deben actualizarse."))) } }
        compose.onNodeWithTag("saved_data_count_0").assertTextEquals("42")
        compose.onNodeWithTag("saved_data_count_1").assertTextEquals(if(language=="pt") "Não disponível" else "No disponible")
        compose.onNodeWithText(if(language=="pt") "Contagem disponível. Não foi possível estimar o tamanho desta categoria." else "Recuento disponible. No se pudo estimar el tamaño de esta categoría.").assertExists()
        compose.onNodeWithText(if(language=="pt") "O serviço recusou o acesso a esta categoria. As permissões precisam ser atualizadas." else "El servicio rechazó el acceso a esta categoría. Sus permisos deben actualizarse.").assertExists()
        capture("statistics")
    }
}
