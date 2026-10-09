package com.example

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.WildRiftRepository
import com.example.model.LaneRole
import com.example.model.DraftSlot
import com.example.ui.screens.DraftSessionManager
import com.example.ui.screens.MetaAndDraftScreen
import com.example.ui.screens.MetaScreenMode
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppLanguage
import com.example.util.DynamicTranslations
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.*
import org.junit.Assert.*
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@org.robolectric.annotation.SQLiteMode(org.robolectric.annotation.SQLiteMode.Mode.NATIVE)
class DraftChampionSelectionTest(private val language: String) {
    companion object {
        @JvmStatic @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun languages() = listOf(arrayOf("es"), arrayOf("pt"))
    }

    @get:Rule val compose = createComposeRule()
    private val context get() = RuntimeEnvironment.getApplication()

    @Before fun prepare() {
        if (FirebaseApp.getApps(context).isEmpty()) FirebaseApp.initializeApp(context, FirebaseOptions.Builder()
            .setApplicationId("1:123:android:draft-selection")
            .setProjectId("demo-coach-draft").setApiKey("local-test-only").build())
        CompletableFuture.runAsync { Tasks.await(FirebaseFirestore.getInstance().disableNetwork()) }.get(10, TimeUnit.SECONDS)
        DynamicTranslations.loadSync(context)
        WildRiftRepository.initChampions(context, forceReload = true)
        AppLanguage.select(context, language)
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit()
            .putString("saved_active_role", LaneRole.MID.name).commit()
        setRole("free")
        setAdminClaim(false)
        DraftSessionManager.clearAll()
    }

    private fun setRole(role: String) {
        val field = com.example.util.SubscriptionManager::class.java.getDeclaredField("_userRole").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        (field.get(com.example.util.SubscriptionManager) as kotlinx.coroutines.flow.MutableStateFlow<String>).value = role
    }
    private fun setAdminClaim(enabled: Boolean) {
        val field = com.example.util.AuthManager::class.java.getDeclaredField("_isAdminClaim").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        (field.get(com.example.util.AuthManager) as kotlinx.coroutines.flow.MutableStateFlow<Boolean>).value = enabled
    }

    @Test fun savingDraftRequiresACompleteScoreAndOffersOnlyTwoRankedQueues() {
        var savedScore: String? = null
        var savedMode: String? = null
        compose.setContent { MyApplicationTheme {
            com.example.ui.components.SaveDraftDialog(
                isOverlay = true, myChampion = null, enemyLaneOpponent = null,
                userRole = LaneRole.MID, estimatedWinrate = 50.0, onDismiss = {},
                onSave = { _, _, _, _, _, mode, score -> savedMode = mode; savedScore = score }
            )
        } }
        compose.onNodeWithText("Normal").assertDoesNotExist()
        compose.onNodeWithTag("draft_score_input").performTextInput("12/2")
        compose.onNodeWithTag("confirm_save_draft_button").assertIsNotEnabled()
        compose.onNodeWithTag("draft_score_input").performTextReplacement("12/2/8")
        compose.onNodeWithTag("confirm_save_draft_button").assertIsEnabled().performClick()
        assertEquals("12/2/8", savedScore)
        assertEquals("RANKED", savedMode)
    }

    @After fun release() {
        DraftSessionManager.clearAll()
        setAdminClaim(false)
        CompletableFuture.runAsync { Tasks.await(FirebaseFirestore.getInstance().terminate()) }.get(10, TimeUnit.SECONDS)
        FirebaseApp.getApps(context).forEach { it.delete() }
    }

    @Test fun roleControlsStayCompactAndReadableOnNarrowScreens() {
        compose.setContent { MyApplicationTheme(animateButtons = true) {
            androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.width(280.dp)) {
                MetaAndDraftScreen(mode = MetaScreenMode.DRAFTING, userMainRole = LaneRole.MID, onNavigateBack = {})
            }
        } }
        val role = compose.onNodeWithTag("draft_active_role_pill")
        role.assertIsDisplayed()
        val bounds = role.getUnclippedBoundsInRoot()
        assertTrue("The lane card expanded vertically", bounds.bottom - bounds.top < 110.dp)
        compose.onNodeWithText(com.example.util.trStr(language, "Cambiar")).assertIsDisplayed()
        compose.onAllNodes(isRoot()).onLast().captureRoboImage(filePath = "build/reports/portuguese-rendered/draft-narrow-$language.png")
    }

    @Test fun missingLaneOpensTheLaneDialogInsteadOfTheChampionPicker() {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().remove("saved_active_role").commit()
        compose.setContent { MyApplicationTheme(animateButtons = true) {
            MetaAndDraftScreen(mode = MetaScreenMode.DRAFTING, userMainRole = LaneRole.MID, onNavigateBack = {})
        } }
        compose.onNodeWithTag("ally_pos_top").performScrollTo().performClick()
        compose.onNode(hasSetTextAction()).assertDoesNotExist()
        compose.onNodeWithText(com.example.util.trStr(language, "Selecciona tu Línea para esta Partida")).assertIsDisplayed()
        assertTrue(DraftSessionManager.allySlots.all { it.champion.id == "empty" })
        compose.onNode(hasText(com.example.util.trStr(language, LaneRole.MID.displayName)) and hasAnyAncestor(isDialog())).performClick()
        openField("enemy", LaneRole.TOP)
        choose("Darius")
        assertEquals("Darius", DraftSessionManager.enemySlots.single { it.assignedRole == LaneRole.TOP }.champion.name)
    }

    @Test fun administratorCanFillEverySlotWithDistinctPrimaryLaneChampions() {
        compose.setContent { MyApplicationTheme(animateButtons = true) {
            MetaAndDraftScreen(mode = MetaScreenMode.DRAFTING, userMainRole = LaneRole.MID, onNavigateBack = {})
        } }
        // Initial anonymous-auth callbacks run first. Then simulate the account's
        // confirmed role arriving, just as the profile listener does after login.
        compose.waitForIdle()
        compose.runOnIdle {
            setRole("admin")
            setAdminClaim(true)
        }
        compose.waitUntil(5_000) {
            compose.onAllNodesWithTag("random_draft_button").fetchSemanticsNodes().size == 1
        }
        assertEquals("admin", com.example.util.SubscriptionManager.userRole.value)
        compose.onNodeWithTag("random_draft_button").performScrollTo().performClick()
        compose.waitForIdle()
        val all = DraftSessionManager.allySlots + DraftSessionManager.enemySlots
        assertEquals(10, all.map { it.champion.id }.distinct().size)
        all.forEach { assertEquals(it.champion.primaryRole, it.assignedRole) }
        assertTrue(com.example.data.RandomDraftPolicy.isWomboTeam(DraftSessionManager.allySlots.map { it.champion }))
        java.io.File("build/reports/portuguese-rendered").mkdirs()
        compose.onRoot().captureRoboImage("build/reports/portuguese-rendered/draft-random-$language.png")
        setRole("free")
    }

    @Test fun theRandomFillActionIsHiddenForAnOrdinaryAccount() {
        compose.setContent { MyApplicationTheme(animateButtons = true) {
            MetaAndDraftScreen(mode = MetaScreenMode.DRAFTING, userMainRole = LaneRole.MID, onNavigateBack = {})
        } }
        compose.onNodeWithTag("random_draft_button").assertDoesNotExist()
    }

    private fun openField(team: String, role: LaneRole) {
        compose.onNodeWithTag("${team}_pos_${role.name.lowercase()}")
            .performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNode(hasSetTextAction()).assertIsDisplayed()
    }

    private fun choose(name: String) {
        compose.onNode(hasSetTextAction()).performTextInput(name)
        // Recommendations behind the modal can contain the same champion name.
        // Send the actual touch to the picker, rather than to the background screen.
        compose.onNode(hasText(name) and !hasSetTextAction() and hasAnyAncestor(isDialog())).performClick()
        compose.waitForIdle()
        compose.onNode(hasSetTextAction()).assertDoesNotExist()
    }

    @Test fun `all ten fields open and assign champions and occupied fields can change`() {
        compose.setContent {
            MyApplicationTheme {
                MetaAndDraftScreen(mode = MetaScreenMode.DRAFTING,
                    userMainRole = LaneRole.MID, isOverlay = false, onNavigateBack = {})
            }
        }
        compose.waitForIdle()
        val roles = listOf(LaneRole.TOP, LaneRole.JUNGLE, LaneRole.MID, LaneRole.ADC, LaneRole.SUPPORT)
        val teams = mapOf("ally" to listOf("Garen", "Xin Zhao", "Ahri", "Jhin", "Lulu"),
            "enemy" to listOf("Darius", "Lee Sin", "Yasuo", "Caitlyn", "Nami"))
        for ((team, names) in teams) for ((index, role) in roles.withIndex()) {
            openField(team, role)
            choose(names[index])
            val slots = if (team == "ally") DraftSessionManager.allySlots else DraftSessionManager.enemySlots
            Assert.assertEquals(names[index], slots.single { it.assignedRole == role }.champion.name)
            Assert.assertEquals(5, slots.size)
            if (team == "ally" && index == 0) {
                compose.onAllNodesWithTag("draft_secondary_recommendation").assertCountEquals(4)
                compose.onNodeWithTag("draft_more_recommendations").performScrollTo().performClick()
                compose.waitForIdle()
                compose.onAllNodesWithTag("draft_secondary_recommendation").assertCountEquals(9)
            } else if (team == "ally" && index == 1) {
                // A changed draft starts with the best five again, keeping the first frame small.
                compose.onAllNodesWithTag("draft_secondary_recommendation").assertCountEquals(4)
            }
        }
        val originalEnemies = DraftSessionManager.enemySlots.toList()
        openField("ally", LaneRole.TOP)
        choose("Teemo")
        Assert.assertEquals("Teemo", DraftSessionManager.allySlots.single { it.assignedRole == LaneRole.TOP }.champion.name)
        Assert.assertEquals(originalEnemies, DraftSessionManager.enemySlots.toList())
        Assert.assertEquals(10, (DraftSessionManager.allySlots + DraftSessionManager.enemySlots).map { it.champion.id }.distinct().size)
    }

    @Test fun `a full restored team with duplicate lanes can fill its missing final lane`() {
        // Old saved drafts can resolve an invalid lane to the champion's primary lane,
        // producing five entries with duplicate lanes and no support entry.
        val restored = listOf("Garen" to LaneRole.TOP, "Xin Zhao" to LaneRole.JUNGLE,
            "Ahri" to LaneRole.MID, "Lux" to LaneRole.MID, "Jhin" to LaneRole.ADC)
            .map { (name, role) -> DraftSlot(WildRiftRepository.champions.first { it.name == name }, role) }
        DraftSessionManager.allySlots.clear()
        DraftSessionManager.allySlots.addAll(restored)
        DraftSessionManager.enemySlots.clear()
        DraftSessionManager.enemySlots.addAll(restored)
        compose.setContent {
            MyApplicationTheme {
                MetaAndDraftScreen(mode = MetaScreenMode.DRAFTING,
                    userMainRole = LaneRole.MID, isOverlay = false, onNavigateBack = {})
            }
        }
        compose.waitForIdle()
        for ((team, name) in listOf("ally" to "Nami", "enemy" to "Braum")) {
            openField(team, LaneRole.SUPPORT)
            choose(name)
            val slots = if (team == "ally") DraftSessionManager.allySlots else DraftSessionManager.enemySlots
            Assert.assertEquals(5, slots.size)
            Assert.assertEquals(restored.take(4), slots.take(4))
            Assert.assertEquals(name, slots.single { it.assignedRole == LaneRole.SUPPORT }.champion.name)
        }
    }
}
