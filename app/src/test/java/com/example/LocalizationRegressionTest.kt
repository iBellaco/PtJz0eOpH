package com.example

import android.app.Application
import com.example.util.AppLanguage
import com.example.util.DynamicTranslations
import com.example.util.appTr
import com.example.util.trStr
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class LocalizationRegressionTest {
    private val context get() = RuntimeEnvironment.getApplication()
    @Before fun load() { DynamicTranslations.loadSync(context) }

    @Test fun `explicit locale codes normalize independently of device language`() {
        java.util.Locale.setDefault(java.util.Locale("pt", "BR"))
        assertEquals("es", AppLanguage.normalize("es-BO"))
        assertEquals("pt", AppLanguage.normalize(" pt-BR "))
        assertEquals("es", AppLanguage.normalize("auto"))
    }

    @Test fun `menus and native messages follow each language change`() {
        AppLanguage.select(context, "pt-BR")
        assertEquals("Salvar", appTr("Guardar"))
        assertEquals("Fechar", appTr("Cerrar"))
        assertEquals("Conta Suspensa", appTr("Cuenta Suspendida"))
        assertEquals("pt", context.getSharedPreferences("app_prefs", 0).getString("selected_language", ""))
        AppLanguage.select(context, "es")
        assertEquals("Guardar", appTr("Guardar"))
        assertEquals("Cerrar", appTr("Cerrar"))
        assertEquals("Cuenta Suspendida", appTr("Cuenta Suspendida"))
    }

    @Test fun `full sentence templates preserve runtime values`() {
        assertEquals("Selecionar Runa Secundária (3/4)", trStr("pt", "Seleccionar Runa Secundaria (3/4)"))
        assertEquals("Dano Inimigo: AD 60% | AP 40%", trStr("pt", "Daño Enemigo: AD 60% | AP 40%"))
        val diagnostic = trStr("pt", " DOMINAS LÍNEA (Senna vs Sett)")
        assertEquals(" VOCÊ DOMINA A ROTA (Senna vs Sett)", diagnostic)
    }

    @Test fun `Spanish restores Portuguese labels without changing champion names`() {
        assertEquals("Guardar", trStr("es", "Salvar"))
        assertEquals("Cerrar", trStr("es", "Fechar"))
        assertEquals("Portugués", trStr("es", "Português"))
        assertEquals("Senna", trStr("pt", "Senna"))
        assertEquals("Volibear", trStr("es", "Volibear"))
    }

    @Test fun `new region and notification labels are localized offline`() {
        assertEquals("Referência local", trStr("pt", "Referencia local"))
        assertEquals("Coach Ativo", trStr("pt", "Coach Activo"))
        assertFalse(trStr("pt", "Referencia local • Sin estadísticas regionales en vivo").contains("Sin estadísticas"))
    }

    @Test fun `unknown user text and URLs remain intact`() {
        val message = "Diego_42: GG Volibear!"
        assertEquals(message, trStr("pt", message))
        assertEquals("https://example.com/es/guardar", trStr("pt", "https://example.com/es/guardar"))
    }
}
