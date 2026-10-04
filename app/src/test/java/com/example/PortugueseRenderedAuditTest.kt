package com.example

import android.app.Application
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.tasks.Task
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import org.json.JSONArray
import org.junit.*
import org.junit.runner.RunWith
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
        @JvmStatic @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun screens() = listOf("information", "faq", "onboarding", "tutorial", "home", "catalog", "tier-list",
            "draft", "champion", "matchup", "personal-tier", "login", "register", "recover", "legal", "donation", "exit", "support-form", "support-inbox", "support-reply",
            "support-ticket-pending", "support-ticket-read", "support-ticket-solved", "support-ticket-unknown-date",
            "support-panel", "support-mailbox")
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
        // This audit uses bundled data. Keep cloud cache workers away from Robolectric's
        // SQLite connections, which are reset between parameterized screen tests.
        val database = FirebaseFirestore.getInstance()
        database.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
            .build()
        awaitDatabaseTask(database.disableNetwork())
        DynamicTranslations.loadSync(context)
        WildRiftRepository.initChampions(context, forceReload = true)
        AppLanguage.select(context, "pt")
        if (screen == "support-reply") {
            com.example.data.SupportReplyManager.saveConversation(context, "audit-reply",
                com.example.data.SupportConversationPolicy.initial("audit-reply", "Tester",
                    "Olá, preciso de ajuda com o hub.", System.currentTimeMillis()))
        }
        if (screen == "support-panel" || screen == "support-mailbox") {
            // Model a staff session locally; no account, network or production messages are used.
            com.example.util.SubscriptionManager.userRole.value
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
            val role = com.example.util.SubscriptionManager::class.java.getDeclaredField("_userRole").apply { isAccessible = true }
            @Suppress("UNCHECKED_CAST")
            (role.get(com.example.util.SubscriptionManager) as kotlinx.coroutines.flow.MutableStateFlow<String>).value = "admin"
            database.collection("support_reports").document("audit-seeded-ticket").set(mapOf(
                "subject" to "Ajuda com o hub", "content" to "Olá, preciso de ajuda com o hub.",
                "created_at" to System.currentTimeMillis(), "app_version" to "1.1.10.147 (863)",
                "device_info" to "Android 14", "type" to "SOPORTE", "status" to "PENDING"
            ))
        }
    }

    @After fun releaseCloudResources() {
        // Await shutdown before Robolectric tears down the current Android sandbox.
        awaitDatabaseTask(FirebaseFirestore.getInstance().terminate())
        FirebaseApp.getApps(context).forEach { it.delete() }
    }

    private fun awaitDatabaseTask(task: Task<Void>) {
        // Google Tasks forbid blocking Android's main thread, including Robolectric's.
        CompletableFuture.runAsync { Tasks.await(task, 10, TimeUnit.SECONDS) }
            .get(15, TimeUnit.SECONDS)
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
            "matchup" -> MatchupPreviewDialog(myChampion = WildRiftRepository.champions.first { it.id == "hwei" }, enemyOpponent = WildRiftRepository.champions.first { it.id == "yasuo" }, activeRole = LaneRole.MID, onDismiss = {})
            "personal-tier" -> PersonalTierListView(emptyList(), {})
            "login" -> LoginScreen(AuthViewModel(), {}, {}, {})
            "register" -> RegisterScreen(AuthViewModel(), {}, {})
            "recover" -> ForgotPasswordScreen(AuthViewModel(), {})
            "legal" -> PrivacyPolicyDialog(isMandatoryAcceptance = true, onDismiss = {})
            "donation" -> DonationDialog({})
            "exit" -> ExitConfirmationDialog({}, {})
            "support-panel" -> AdminFeedbackBottomSheet({})
            "support-mailbox" -> AdminSupportReportsDialog({})
            "support-form" -> SupportReportDialog({})
            "support-inbox" -> UserInboxDialog("audit-local-user", {})
            "support-reply" -> {
                SupportReplyDialog(reportId = "audit-reply", reportTitle = "Ajuda com o hub",
                reportDescription = "Olá, preciso de ajuda com o hub.", userEmail = "tester@example.invalid",
                userName = "Tester", userId = "audit-local-user", onDismiss = {}, onReplySent = { _, _ -> })
            }
            else -> if (screen.startsWith("support-ticket-")) {
                val status = when (screen) {
                    "support-ticket-read" -> "READ"
                    "support-ticket-solved" -> "SOLVED"
                    else -> "PENDING"
                }
                val data = mutableMapOf<String, Any>("subject" to "Ajuda com o hub",
                    "content" to "Olá, preciso de ajuda com o hub.", "type" to "SOPORTE",
                    "app_version" to "1.1.10.147 (863)", "device_info" to "Android 14", "status" to status)
                if (screen != "support-ticket-unknown-date") data["created_at"] = System.currentTimeMillis()
                val report = com.example.data.SupportReportDecoder.decode(screen, data)!!
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    ComprehensiveFeedbackCard(report, status, {}, onReply = {}, onDelete = null,
                        onCopy = {}, onOpenImage = {}, onItemClick = {})
                }
            }
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
        strings.filter { it.length > 2 && SpanishUiResidue.pattern.containsMatchIn(it.replace("Lee Sin", "LeeSin")) }
            .forEach { findings.add("$screen/$step: $it") }
    }

    @Test fun `Portuguese rendered surfaces contain no Spanish wording`() {
        compose.setContent { MyApplicationTheme(animateButtons = true) { Box(Modifier.fillMaxSize()) { surface() } } }
        if (screen == "support-panel" || screen == "support-mailbox") {
            compose.waitUntil(15_000) { compose.onAllNodesWithText("Ajuda com o hub").fetchSemanticsNodes().isNotEmpty() }
        }
        inspect("initial")
        if (screen == "onboarding" || screen == "tutorial") {
            repeat(3) { page ->
                compose.onNodeWithText("Seguinte").performClick()
                inspect("page-${page + 2}")
            }
        }
        if (screen == "catalog") {
            for (tab in listOf("Itens", "Runas", "Feitiços")) {
                compose.onNodeWithText(tab).performClick()
                inspect(tab)
            }
        }
        if (screen == "faq") {
            val questions = compose.onAllNodes(SemanticsMatcher("question") { node ->
                node.config.getOrNull(SemanticsProperties.Text).orEmpty().any { it.text.endsWith("?") }
            }, useUnmergedTree = true).fetchSemanticsNodes().flatMap {
                it.config.getOrNull(SemanticsProperties.Text).orEmpty().map { text -> text.text }
            }.distinct()
            Assert.assertTrue("FAQ questions were not rendered", questions.size > 50)
            questions.forEachIndexed { index, question ->
                compose.onNodeWithText(question).performScrollTo().performClick()
                inspect("answer-${index + 1}")
                compose.onNodeWithText("Entendido").performClick()
            }
        }
        if (screen == "support-reply") {
            compose.onNodeWithText("💡 Guia").assertExists()
        }
        if (screen.startsWith("support-ticket-")) {
            compose.onNodeWithText("Olá, preciso de ajuda com o hub.").assertExists()
            if (screen == "support-ticket-unknown-date") {
                Assert.assertTrue("Missing date must not expire the ticket", compose.onAllNodesWithText("Expirado", substring = true).fetchSemanticsNodes().isEmpty())
                compose.onAllNodesWithText("Data indisponível").onFirst().assertExists()
            }
            compose.onNodeWithContentDescription("Expandir").performClick()
            inspect("expanded")
            if (screen == "support-ticket-solved") {
                compose.onNodeWithText("Responder Mensagem").assertDoesNotExist()
                compose.onNodeWithTag("support_continue_reply").assertDoesNotExist()
            } else compose.onNodeWithText("Responder Mensagem").performScrollTo()
            inspect("reply-controls")
            compose.onNodeWithText("Versão:", substring = true).performScrollTo()
            inspect("device-details")
        }
        if (screen == "legal") {
            for (tab in listOf("Termos", "Terceiros")) {
                compose.onNodeWithText(tab).performClick()
                inspect(tab)
            }
        }
        Assert.assertTrue("Rendered Spanish remains:\n${findings.joinToString("\n")}", findings.isEmpty())
    }
}
