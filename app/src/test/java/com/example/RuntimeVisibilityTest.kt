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
import com.example.model.*
import com.example.ui.components.StreamerUrlRecommendations
import com.example.ui.screens.*
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
            "tier-guest", "tier-registered", "tier-registration", "streamer", "streamer-admin").map { arrayOf(it) }
    }
    @get:Rule val compose = createComposeRule()
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
        DynamicTranslations.loadSync(context)
        WildRiftRepository.initChampions(context, forceReload = true)
        AppLanguage.select(context, "pt")
        AuthManager.isSignedIn.value
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        val field = AuthManager::class.java.getDeclaredField("_isSignedIn").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        (field.get(AuthManager) as MutableStateFlow<Boolean>).value = screen == "tier-registered"
    }
    @After fun release() {
        awaitTask(FirebaseFirestore.getInstance().terminate())
        FirebaseApp.getApps(context).forEach { it.delete() }
    }

    @Composable private fun surface() {
        when {
            screen.startsWith("streamer") -> Column { StreamerUrlRecommendations(screen == "streamer-admin") {} }
            screen.startsWith("tier") -> TierListTab(onSelectChampion = {})
            else -> {
                val own = WildRiftRepository.champions.first { it.id == "ahri" }
                    .takeIf { screen == "draft-own-only" || screen == "draft-both" }
                val rival = WildRiftRepository.champions.first { it.id == "yasuo" }
                    .takeIf { screen == "draft-rival-only" || screen == "draft-both" }
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
            "draft-empty" -> {
                compose.onNodeWithTag("draft_recommendations").assertDoesNotExist()
                compose.onNodeWithTag("open_matchup_preview_button").assertDoesNotExist()
                compose.onNodeWithTag("draft_matchup_missing_selection").performScrollTo()
                compose.onNodeWithText("Selecione seu campeão e o adversário para ver o confronto 1 contra 1.").assertExists()
                compose.onNodeWithText("Selecione seu campeão").assertExists()
            }
            "draft-own-only", "draft-rival-only" -> {
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
            "streamer", "streamer-admin" -> {
                compose.onNodeWithTag("streamer_channel_example").assertExists()
                if (screen == "streamer-admin") compose.onNodeWithTag("streamer_admin_google_example").assertExists()
                else compose.onNodeWithTag("streamer_admin_google_example").assertDoesNotExist()
            }
        }
        inspect("verified")
    }
}
