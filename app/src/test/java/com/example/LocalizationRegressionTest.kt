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

    @Test fun `Latin American Spanish stays authoritative for canonical labels and catalog fields`() {
        AppLanguage.select(context, "pt")
        AppLanguage.select(context, "es-419")
        assertEquals("es-419", context.resources.configuration.locales[0].toLanguageTag())
        assertEquals("es-419", AppLanguage.localizedContext(context).resources.configuration.locales[0].toLanguageTag())
        for (label in listOf("COPIAR", " Copiar ", "PRINCIPAL", "MINIMIZAR", "PATROCINADOR", "DERROTA")) {
            assertEquals(label, label, trStr("es-419", label))
        }
        com.example.data.WildRiftItemsData.list.forEach { item ->
            listOf(item.name, item.stats, item.passive, item.coachTip).forEach { source ->
                assertEquals("Spanish field changed for ${item.id}", source, trStr("es-419", source))
            }
        }
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

    @Test fun `decorations casing and copied Portuguese fields cannot leak Spanish`() {
        assertEquals("⚔️ FEITIÇOS:", trStr("pt", "⚔️ HECHIZOS:"))
        assertEquals("PERGUNTAS FREQUENTES", trStr("pt", "PREGUNTAS FRECUENTES"))
        assertEquals("🔥 Perguntas Frequentes", trStr("pt", "🔥 Preguntas Frecuentes"))
        assertEquals("Perguntas Frequentes", trStr("pt", "Preguntas  Frecuentes"))
        assertEquals("Preguntas Frecuentes", trStr("es", "Preguntas Frecuentes"))
        assertFalse(trStr("pt", "Aplica 40% de Heridas Graves al recibir ataques de los rivales e inmovilizarlos.").contains("Heridas Graves"))
        assertEquals("NA sem fonte disponível • Consulte a lista Global", trStr("pt", "NA sin fuente disponible • Consulta la lista Global"))
    }

    @Test fun `known UI labels never leak Spanish panel wording into Portuguese`() {
        assertEquals("Expandir painel", trStr("pt", "Expandir panel"))
        assertEquals("Minimizar painel", trStr("pt", "Minimizar panel"))
        assertEquals("Recarga de Essência Azul", trStr("pt", "Recarga de Esencia Azul"))
        assertEquals("Tier List Global ativa", trStr("pt", "Tier List Global activa"))
        assertEquals("Tier List NA ativa", trStr("pt", "Tier List NA activa"))
    }

    @Test fun `unknown user text and URLs remain intact`() {
        val message = "Diego_42: GG Volibear!"
        assertEquals(message, trStr("pt", message))
        assertEquals("https://example.com/es/guardar", trStr("pt", "https://example.com/es/guardar"))
    }

    @Test fun `bundled champion and equipment descriptions contain no Spanish UI wording in Portuguese`() {
        val repository = com.example.data.WildRiftRepository
        repository.initChampions(context, forceReload = true)
        assertTrue(repository.champions.size > 100)
        val descriptions = repository.champions.flatMap { champion ->
            listOf(champion.getLocalizedTitle("pt"), champion.getLocalizedSummary("pt")) +
                champion.skills.flatMap { listOf(it.getLocalizedName("pt"), it.getLocalizedDescription("pt")) }
        } + repository.items.flatMap {
            listOf(it.getLocalizedName("pt"), it.getLocalizedStats("pt"), it.getLocalizedPassive("pt"), it.getLocalizedCoachTip("pt"))
        } + repository.runes.flatMap { listOf(it.getLocalizedName("pt"), it.getLocalizedDescription("pt")) } +
            repository.summonerSpells.flatMap { listOf(it.getLocalizedName("pt"), it.getLocalizedDescription("pt")) }
        val spanish = Regex(
            "(?<![\\p{L}\\p{N}_-])(?:daño|enemigos?|enemigas?|campeones?|hechizos?|asesinos|tardío|dragones|heraldo|empuja|recuerda|agrupa|flanquea|muévete|pasillos|farmeo|apoindo|acierta|automáticamente|bloquea|estuneado|projectoil|projetoil|relanzamiento|selección|táctico|canalización|consejo|jugadores?|pantalla|cerrar|guardar|después|todavía|aunque)(?![\\p{L}\\p{N}_])|[¿¡ñ]",
            RegexOption.IGNORE_CASE
        )
        descriptions.forEach { text ->
            assertFalse("Portuguese description contains Spanish: $text", spanish.containsMatchIn(text.replace("Lee Sin", "LeeSin")))
        }
    }
}
