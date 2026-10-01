package com.example

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.WildRiftRepository
import com.example.model.LaneRole
import com.example.ui.auth.*
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppLanguage
import com.example.util.DynamicTranslations
import com.github.takahirom.roborazzi.captureRoboImage
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import java.io.File
import org.json.JSONArray
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Reads what real production composables present, rather than checking dictionary membership. */
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PortugueseRenderedAuditTest(private val screen: String) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "{0}")
        fun screens() = listOf("information", "faq", "onboarding", "tutorial", "home", "catalog", "tier-list",
            "draft", "champion", "personal-tier", "login", "register", "recover", "legal", "donation", "exit")
            .map { arrayOf(it) }
    }
    @get:Rule val compose = createComposeRule()
    private val context get() = RuntimeEnvironment.getApplication()
    private val findings = linkedSetOf<String>()
    private val output = File("build/reports/portuguese-rendered").apply { mkdirs() }

    @Before fun prepare() {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context, FirebaseOptions.Builder().setApplicationId("1:123:android:audit")
                .setProjectId("demo-coach-audit").setApiKey("audit-local-only").build())
        }
        DynamicTranslations.loadSync(context)
        WildRiftRepository.initChampions(context, forceReload = true)
        AppLanguage.select(context, "pt")
    }

    @Composable private fun surface() {
        when (screen) {
            "information" -> InfoScreen({}, {})
            "faq" -> FAQScreen({})
            "onboarding" -> OnboardingScreen({})
            "tutorial" -> TutorialScreen({})
            "home" -> MainDraftingScreen({}, {}, {}, {}, LaneRole.MID, {}, LaneRole.TOP, {}, LaneRole.SUPPORT, {}, "pt", {})
            "catalog" -> MetaAndDraftScreen(MetaScreenMode.CATALOG, LaneRole.MID, onNavigateBack = {})
            "tier-list" -> MetaAndDraftScreen(MetaScreenMode.TIER_LIST, LaneRole.MID, onNavigateBack = {})
            "draft" -> MetaAndDraftScreen(MetaScreenMode.DRAFTING, LaneRole.MID, onNavigateBack = {})
            "champion" -> ChampionDetailSheet(champion = WildRiftRepository.champions.first { it.id == "hwei" }, onDismiss = {})
            "personal-tier" -> PersonalTierListView(emptyList(), {})
            "login" -> LoginScreen(AuthViewModel(), {}, {}, {})
            "register" -> RegisterScreen(AuthViewModel(), {}, {})
            "recover" -> ForgotPasswordScreen(AuthViewModel(), {})
            "legal" -> PrivacyPolicyDialog(isMandatoryAcceptance = true, onDismiss = {})
            "donation" -> DonationDialog({})
            "exit" -> ExitConfirmationDialog({}, {})
        }
    }

    private fun inspect(step: String) {
        compose.waitForIdle()
        val strings = compose.onAllNodes(SemanticsMatcher("all nodes") { true }, useUnmergedTree = true)
            .fetchSemanticsNodes().flatMap { node ->
                node.config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text } +
                    node.config.getOrNull(SemanticsProperties.ContentDescription).orEmpty()
            }.filter { it.isNotBlank() }.distinct()
        Assert.assertTrue("Empty rendered surface: $screen/$step", strings.isNotEmpty())
        File(output, "$screen-$step.json").writeText(JSONArray(strings).toString(2))
        compose.onAllNodes(isRoot()).onLast().captureRoboImage(filePath = File(output, "$screen-$step.png").path)
        strings.filter { SpanishUiResidue.pattern.containsMatchIn(it.replace("Lee Sin", "LeeSin")) }
            .forEach { findings.add("$screen/$step: $it") }
    }

    @Test fun `Portuguese rendered surfaces contain no Spanish wording`() {
        compose.setContent { MyApplicationTheme { Box(Modifier.fillMaxSize()) { surface() } } }
        inspect("initial")
        if (screen == "catalog") {
            for (tab in listOf("Itens", "Runas", "Feitiços")) {
                compose.onNodeWithText(tab).performClick()
                inspect(tab)
            }
        }
        Assert.assertTrue("Rendered Spanish remains:\n${findings.joinToString("\n")}", findings.isEmpty())
    }
}

internal object SpanishUiResidue {
    val pattern = Regex(
        "(?<![\\p{L}\\p{N}_-])(?:el|los|las|del|al|una|unos|unas|tus|puedes|debes|añadir|añade|daño|hechizos?|velocidad|consejo|campeones|cerrar|guardar|jugadores?|vuelve|pantalla|sesión|contraseña|después|todavía|aunque|otorgar|obligatori[oa]|revocación|reproducir|esencia|naranja|izquierda|derecha|arriba|abajo|descripción|versión|ninguna|cantidad|legendario|actualizando|desfavorable|mensaje|cerrado|resuelto|borrar|archivo|resolución|elige|tienes|tiene|tienen|cuando|rápidamente|mejor|entrer|asesinos|tardío|dragones|heraldo|empuja|recuerda|agrupa|flanquea|muévete|pasillos|deniega|farmeo|apoindo|acierta|automáticamente|bloquea|estuneado|relanzamiento|selección|táctico|canalización|días|años|desactivando|depurado|cuenta|rendimiento|requiere|requieren|soporte|usuario|usuarios|guardián|común|clásico|débil|fuerte|último|habilidades? especiales|principalmente defensivo)(?![\\p{L}\\p{N}_])|[¿¡ñ]",
        RegexOption.IGNORE_CASE
    )
}
