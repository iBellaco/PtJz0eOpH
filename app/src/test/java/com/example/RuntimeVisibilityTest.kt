package com.example

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.WildRiftRepository
import com.example.data.StreamerPublicationPolicy
import com.example.model.*
import com.example.ui.components.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.ui.screens.*
import com.example.ui.auth.AuthenticatedProfilePanel
import com.google.firebase.auth.FirebaseUser
import org.mockito.Mockito
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MyApplicationTheme
import com.example.util.*
import com.github.takahirom.roborazzi.captureRoboImage
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.*
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.tasks.Task
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RuntimeVisibilityTest(private val screen: String) {
    companion object {
        @JvmStatic @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun screens() = listOf("draft-empty", "draft-own-only", "draft-rival-only", "draft-both",
            "tier-guest", "tier-registered", "tier-registration", "champion-guest", "champion-registered",
            "streamer", "streamer-admin", "streamer-live", "streamer-feedback", "streamer-history", "streamer-guest-live", "streamer-approved-review", "support-followup", "support-legacy-followup", "support-closed",
            "matchup-varus", "matchup-jhin", "matchup-garen",
            "draft-placeholder", "draft-placeholder-own", "draft-placeholder-rival",
            "moderation-admin", "moderation-claim", "moderation-secondary", "premium-editor", "premium-editor-secondary", "profile-admin", "profile-admin-large").map { arrayOf(it) }
    }
    @get:Rule val compose = createComposeRule()
    private var copiedSummary = ""
    private val context get() = RuntimeEnvironment.getApplication()
    private val output = File("build/reports/portuguese-rendered").apply { mkdirs() }

    private fun awaitTask(task: Task<Void>) {
        CompletableFuture.runAsync { Tasks.await(task, 10, TimeUnit.SECONDS) }.get(15, TimeUnit.SECONDS)
    }
    @Before fun prepare() {
        if (FirebaseApp.getApps(context).isEmpty()) FirebaseApp.initializeApp(context,
            FirebaseOptions.Builder().setApplicationId("1:123:android:visibility")
                .setProjectId("demo-coach-visibility").setApiKey("local-test-only").build())
        val database = FirebaseFirestore.getInstance()
        database.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build()).build()
        awaitTask(database.disableNetwork())
        if (screen == "streamer-guest-live") database.collection("system_config").document("streamer_live").set(
            mapOf("entries" to listOf(
                mapOf("userId" to "local-streamer", "channelName" to "Canal Público", "channelUrl" to "https://twitch.tv/coach_test"),
                mapOf("userId" to "local-admin", "channelName" to "Canal Teste", "channelUrl" to "https://www.google.com", "platform" to "Google"))))
        DynamicTranslations.loadSync(context)
        WildRiftRepository.initChampions(context, forceReload = true)
        AppLanguage.select(context, "pt")
        AuthManager.isSignedIn.value
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        val field = AuthManager::class.java.getDeclaredField("_isSignedIn").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        (field.get(AuthManager) as MutableStateFlow<Boolean>).value = screen.endsWith("-registered")
        SubscriptionManager.userRole.value
        fun setFlow(target: Any, name: String, value: Any) {
            val variable = target.javaClass.getDeclaredField(name).apply { isAccessible = true }
            @Suppress("UNCHECKED_CAST")
            (variable.get(target) as MutableStateFlow<Any>).value = value
        }
        setFlow(SubscriptionManager, "_userRole", if (screen == "moderation-admin" || screen.startsWith("profile-admin")) "admin" else "free")
        setFlow(SubscriptionManager, "_secondaryRole", if (screen == "moderation-secondary") "moderador" else "")
        setFlow(AuthManager, "_isAdminClaim", screen == "moderation-claim")
        if (screen.startsWith("profile-admin")) setFlow(SubscriptionManager, "_isPremium", true)
        com.example.data.AppNoticeManager.notices.value
        setFlow(com.example.data.AppNoticeManager, "_notices", if (screen == "profile-admin-large")
            listOf(com.example.data.AppNotice(id = "local-pending", title = "Teste", content = "Teste", tag = "Publicidad", isApproved = false)) else emptyList<com.example.data.AppNotice>())
        if (screen == "profile-admin-large") {
            val configuration = android.content.res.Configuration(context.resources.configuration).apply { fontScale = 1.5f }
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
        }

    }
    @After fun release() {
        awaitTask(FirebaseFirestore.getInstance().terminate())
        FirebaseApp.getApps(context).forEach { it.delete() }
    }

    @Composable private fun surface() {
        when {
            screen == "streamer-guest-live" -> Column { LiveStreamersRow() }
            screen == "streamer-approved-review" -> ApprovedStreamerReviewCard(mapOf("channelName" to "Canal Aprovado", "channelUrl" to "https://www.google.com"), true, { copiedSummary = it }, {})
            screen.startsWith("support-") -> ComprehensiveFeedbackCard(
                report = com.example.data.remote.model.FeedbackReport(id = "local-thread", userId = "local-user", userName = "Teste",
                    type = if (screen == "support-legacy-followup") "REPORTE" else "SOPORTE", title = "Conversa Coach", description = "Detalhes do problema",
                    adminReply = "Resposta anterior", createdAt = com.example.data.SupportReportDecoder.isoDate(System.currentTimeMillis())),
                currentStatus = if (screen == "support-closed") "SOLVED" else "PENDING",
                onSelectStatus = {}, onReply = { copiedSummary = "continue" }, onDelete = null,
                onCopy = {}, onOpenImage = {}, onItemClick = {})
            screen.startsWith("matchup-") -> MatchupPreviewDialog(
                myChampion = WildRiftRepository.champions.first { it.id == screen.removePrefix("matchup-") },
                enemyOpponent = WildRiftRepository.champions.first { it.id == "smolder" },
                activeRole = if (screen == "matchup-garen") LaneRole.TOP else LaneRole.ADC, onDismiss = {})
            screen == "streamer-live" -> LiveStreamerChip("Canal Coach") {}
            screen == "streamer-feedback" -> StreamerSubmissionFeedback(false, true)
            screen == "streamer-history" -> {
                val now = System.currentTimeMillis()
                val records = listOf("APPROVED", "REJECTED", "ENDED", "PENDING").mapIndexed { i, status ->
                    mapOf<String, Any>("publicationId" to "history-$i", "channelName" to "Canal $i", "status" to status,
                        "submittedAtMillis" to now - (i + 1) * 24 * 60 * 60 * 1000L, "clickCount" to if (i == 0) 42L else i.toLong())
                }
                Column(Modifier.verticalScroll(rememberScrollState())) { StreamerPublicationHistory(records + mapOf<String, Any>("publicationId" to "history-old", "channelName" to "Canal Expirado", "status" to "APPROVED",
                    "submittedAtMillis" to now - StreamerPublicationPolicy.HISTORY_WINDOW_MILLIS), now) { copiedSummary = it } }
            }
            screen.startsWith("streamer") -> Column { StreamerUrlRecommendations(screen == "streamer-admin") {} }
            screen.startsWith("moderation") -> ModeratorDashboardDialog {}
            screen.startsWith("profile-admin") -> {
                val user = Mockito.mock(FirebaseUser::class.java)
                Mockito.`when`(user.uid).thenReturn("local-profile-test")
                Mockito.`when`(user.email).thenReturn("coach@example.invalid")
                Mockito.`when`(user.displayName).thenReturn("Coach Teste")
                AuthenticatedProfilePanel(user) {}
            }
            screen.startsWith("premium-editor") -> UserDetailManagementDialog(mapOf("uid" to "local-test", "name" to "Teste",
                "role" to if (screen == "premium-editor-secondary") "creador" else "premium",
                "secondaryRole" to if (screen == "premium-editor-secondary") "moderador" else "",
                "premiumUntil" to System.currentTimeMillis() + 86400000L), {}, {}, {}, {})
            screen.startsWith("champion") -> ChampionDetailSheet(champion = WildRiftRepository.champions.first { it.id == "garen" }, onDismiss = {})
            screen.startsWith("tier") -> TierListTab(onSelectChampion = {})
            else -> {
                val realOwn = WildRiftRepository.champions.first { it.id == "ahri" }
                    .takeIf { screen == "draft-own-only" || screen == "draft-both" || screen == "draft-placeholder-rival" }
                val realRival = WildRiftRepository.champions.first { it.id == "yasuo" }
                    .takeIf { screen == "draft-rival-only" || screen == "draft-both" || screen == "draft-placeholder-own" }
                val own = if (screen in listOf("draft-placeholder", "draft-placeholder-own")) WildRiftRepository.EMPTY_CHAMPION else realOwn
                val rival = if (screen in listOf("draft-placeholder", "draft-placeholder-rival")) WildRiftRepository.EMPTY_CHAMPION else realRival
                val allies = own?.let { listOf(DraftSlot(it, LaneRole.MID)) }.orEmpty()
                val enemies = rival?.let { listOf(DraftSlot(it, LaneRole.MID)) }.orEmpty()
                val analysis = WildRiftRepository.analyzeDraft(LaneRole.MID, allies.map { it.champion },
                    enemies.map { it.champion }, rival, lang = "pt")
                DraftAnalysisTab(myChampion = own, activeRole = LaneRole.MID, allySlots = allies,
                    enemySlots = enemies, analysis = analysis, isFirstPick = false, enemyLaneOpponent = rival,
                    onToggleFirstPick = {}, onChangeRole = {}, onPickAllyRole = {}, onPickEnemyRole = {},
                    onRemoveAllyRole = {}, onRemoveEnemyRole = {}, onPickRecommendation = {},
                    onSelectChampion = {}, onOpenHistory = {}, onClearAll = {})
            }
        }
    }

    private fun inspect(step: String) {
        compose.waitForIdle()
        val strings = compose.onAllNodes(SemanticsMatcher("all") { true }, useUnmergedTree = true)
            .fetchSemanticsNodes().flatMap {
                it.config.getOrNull(SemanticsProperties.Text).orEmpty().map { text -> text.text } +
                    it.config.getOrNull(SemanticsProperties.ContentDescription).orEmpty()
            }.filter { it.isNotBlank() }.distinct()
        Assert.assertTrue(strings.isNotEmpty())
        File(output, "$screen-$step.json").writeText(JSONArray(strings).toString(2))
        compose.onAllNodes(isRoot()).onLast().captureRoboImage(filePath = File(output, "$screen-$step.png").path)
        val failures = strings.filter { SpanishUiResidue.pattern.containsMatchIn(it.replace("Lee Sin", "LeeSin")) }
        Assert.assertTrue("Spanish on $screen: ${failures.joinToString()}", failures.isEmpty())
    }

    @Test fun `visibility and Portuguese wording follow actual selections and account access`() {
        compose.setContent { MyApplicationTheme { Box(Modifier.fillMaxSize()) { surface() } } }
        compose.waitForIdle()
        when (screen) {
            "champion-guest" -> compose.onNodeWithTag("detailed_trend_graph").assertDoesNotExist()
            "champion-registered" -> compose.onNodeWithTag("detailed_trend_graph").performScrollTo().assertExists()
            "draft-empty", "draft-placeholder" -> {
                compose.onAllNodesWithText("Cálculo 1v1 Automático", substring = true).assertCountEquals(0)
                compose.onAllNodesWithText("Ninguno").assertCountEquals(0)
                compose.onNodeWithTag("draft_recommendations").assertDoesNotExist()
                compose.onNodeWithTag("open_matchup_preview_button").assertDoesNotExist()
                compose.onNodeWithTag("draft_matchup_missing_selection").performScrollTo()
                compose.onNodeWithText("Selecione seu campeão e o adversário para ver o confronto 1 contra 1.").assertExists()
                compose.onNodeWithText("Selecione seu campeão").assertExists()
            }
            "draft-own-only", "draft-rival-only", "draft-placeholder-own", "draft-placeholder-rival" -> {
                compose.onNodeWithTag("open_matchup_preview_button").assertDoesNotExist()
                compose.onNodeWithTag("draft_recommendations").assertExists()
                compose.onNodeWithTag("draft_matchup_missing_selection").performScrollTo()
            }
            "draft-both" -> {
                compose.onNodeWithTag("draft_matchup_missing_selection").assertDoesNotExist()
                compose.onNodeWithTag("open_matchup_preview_button").performScrollTo().assertExists()
                compose.onNodeWithTag("draft_recommendations").assertExists()
            }
            "tier-registered" -> {
                compose.onNodeWithTag("tier_list").performScrollToNode(hasTestTag("tier_trend_header"))
                compose.onNodeWithTag("tier_trend_header").assertExists()
                compose.onNodeWithTag("tier_trend_sign_in").assertDoesNotExist()
                inspect("legend")
                compose.onNodeWithTag("tier_list", useUnmergedTree = true).performScrollToNode(hasTestTag("tier_trend_graph"))
                Assert.assertTrue(compose.onAllNodesWithTag("tier_trend_graph", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
            }
            "tier-guest", "tier-registration" -> {
                compose.onNodeWithTag("tier_list").performScrollToNode(hasTestTag("tier_trend_sign_in_button"))
                compose.onNodeWithTag("tier_trend_header").assertDoesNotExist()
                compose.onNodeWithTag("tier_trend_graph").assertDoesNotExist()
                if (screen == "tier-registration") {
                    compose.onNodeWithTag("tier_trend_sign_in_button").performClick()
                    inspect("login")
                    compose.onNodeWithText("Cadastre-se").performScrollTo().performClick()
                    compose.onNodeWithText("Criar uma conta").assertExists()
                }
            }
            "streamer-live" -> {
                compose.onNodeWithText("Canal Coach").assertExists()
                compose.onNodeWithText("Ao vivo").assertExists()
                compose.onNodeWithTag("live_streamer_chip").assertExists()
            }
            "streamer-guest-live" -> {
                compose.waitUntil(10000) { compose.onAllNodesWithText("Canal Público").fetchSemanticsNodes().isNotEmpty() }
                compose.onNodeWithText("Canal Teste").assertExists()
                Assert.assertEquals(2, compose.onAllNodesWithText("Ao vivo").fetchSemanticsNodes().size)
                Assert.assertFalse(AuthManager.isSignedIn.value)
            }
            "streamer-approved-review" -> {
                compose.onNodeWithText("Publicação aceita e visível para todos.").assertExists()
                compose.onNodeWithText("https://www.google.com").assertExists()
                compose.onNodeWithText(com.example.util.appTr("Abrir canal")).performClick()
                Assert.assertEquals("https://www.google.com", copiedSummary)
            }
            "support-followup", "support-legacy-followup", "support-closed" -> {
                compose.onNodeWithContentDescription("Expandir").performClick()
                if (screen == "support-closed") compose.onNodeWithTag("support_continue_reply").assertDoesNotExist()
                else {
                    compose.onNodeWithTag("support_continue_reply").assertExists().performClick()
                    Assert.assertEquals("continue", copiedSummary)
                }
                compose.onNodeWithContentDescription("Excluir").assertDoesNotExist()
            }
            "matchup-varus", "matchup-jhin", "matchup-garen" -> {
                compose.onNodeWithText("Smolder", substring = false).assertExists()
                val champion = WildRiftRepository.champions.first { it.id == screen.removePrefix("matchup-") }
                val plan = ChampionMatchupCoaching.forDuel(champion, WildRiftRepository.champions.first { it.id == "smolder" },
                    if (screen == "matchup-garen") LaneRole.TOP else LaneRole.ADC, "pt")
                compose.onNodeWithText(plan.early).assertExists()
                inspect("early")
                compose.onNodeWithText(plan.winCondition).performScrollTo().assertExists()
                inspect("condition")
                compose.onNodeWithText(plan.verdict).performScrollTo().assertExists()
            }
            "streamer-feedback" -> compose.onNodeWithText("Solicitação enviada com sucesso. Você será avisado quando ela for analisada.").assertExists()
            "streamer-history" -> {
                compose.onNodeWithText("Histórico de publicações").assertExists()
                compose.onNodeWithText("Canal Expirado").assertDoesNotExist()
                compose.onNodeWithTag("streamer_history_copy_history-0").performScrollTo().performClick()
                Assert.assertTrue(copiedSummary, copiedSummary.contains("Cliques para abrir o canal: 42"))
                Assert.assertTrue(copiedSummary, copiedSummary.contains("Aceita"))
                Assert.assertTrue(copiedSummary, copiedSummary.contains("Data e hora:"))
                Assert.assertTrue(copiedSummary, copiedSummary.contains("Remoção:"))
                Assert.assertTrue(copiedSummary, Regex("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}").containsMatchIn(copiedSummary))
                compose.onAllNodesWithText("Aceita").assertCountEquals(2)
                compose.onNodeWithText("Rejeitada automaticamente: passaram três horas sem aprovação.").performScrollTo().assertExists()
            }
            "moderation-admin", "moderation-claim", "moderation-secondary" -> {
                compose.onNodeWithText(appTr("Bandeja de Moderación")).assertExists()
                compose.onNodeWithText(appTr("Abrir Reportes de Soporte")).performClick()
                compose.onNodeWithText(appTr("Panel de Reportes & Sugerencias")).assertExists()
                if (screen != "moderation-secondary") compose.onNodeWithContentDescription(appTr("Eliminar solucionados")).assertExists()
            }
            "profile-admin", "profile-admin-large" -> {
                for (label in listOf("Painel de streamer", appTr("Panel de Administración"), appTr("Panel de Soporte y Moderación"), appTr("Panel de Patrocinador"))) {
                    compose.onNodeWithText(label).performScrollTo().assertExists()
                }
                compose.onNodeWithText(appTr("Panel de Moderador")).performScrollTo().assertExists()
                val icon = compose.onNodeWithTag("moderator_panel_icon", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
                val title = compose.onNodeWithTag("moderator_panel_title", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
                val maximumGap = 16f * context.resources.displayMetrics.density
                Assert.assertTrue("Moderator icon must sit beside its title: $icon $title", title.left >= icon.right && title.left - icon.right <= maximumGap)
                if (screen == "profile-admin-large") compose.onNodeWithText("Solicitação pendente").assertExists()
                inspect("role-buttons")
                compose.onNodeWithText(appTr("Panel de Soporte y Moderación")).performScrollTo().performClick()
                compose.onNodeWithText(appTr("Bandeja de Moderación")).assertExists()
            }
            "premium-editor", "premium-editor-secondary" -> {
                if (screen == "premium-editor-secondary") compose.onNodeWithText(appTr("Acceso Moderador (Vitalicio)")).performScrollTo().assertExists()
                compose.onNodeWithText(appTr("Editar o extender tiempo premium:")).performScrollTo().assertExists()
                compose.onAllNodesWithText("♾️ Vitalicio").assertCountEquals(0)
            }
            "streamer", "streamer-admin" -> {
                compose.onNodeWithTag("streamer_channel_example").assertExists()
                if (screen == "streamer-admin") compose.onNodeWithTag("streamer_admin_google_example").assertExists()
                else compose.onNodeWithTag("streamer_admin_google_example").assertDoesNotExist()
            }
        }
        inspect("verified")
    }
}
