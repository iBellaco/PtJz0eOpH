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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Message
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
@org.robolectric.annotation.SQLiteMode(org.robolectric.annotation.SQLiteMode.Mode.NATIVE)
class RuntimeVisibilityTest(private val screen: String) {
    companion object {
        @JvmStatic @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun screens() = listOf("draft-known-first-pick", "draft-empty", "draft-own-only", "draft-rival-only", "draft-both",
            "tier-guest", "tier-registered", "tier-registration", "champion-guest", "champion-registered", "champion-item-advice", "champion-spell-advice", "champion-rune-advice",
            "user-notification", "user-notification-empty", "streamer", "streamer-admin", "streamer-live", "streamer-feedback", "streamer-history", "streamer-guest-live", "streamer-approved-review", "support-followup", "support-legacy-followup", "support-closed",
            "matchup-varus", "matchup-jhin", "matchup-garen",
            "draft-placeholder", "draft-placeholder-own", "draft-placeholder-rival",
            "moderation-admin", "moderation-claim", "moderation-secondary", "premium-editor", "premium-editor-secondary", "profile-admin", "profile-admin-large", "premium-editor-admin", "premium-editor-grant", "premium-status-near-expiry", "creator-reader", "premium-editor-occupied", "profile-admin-notifications", "panel-notification-animation", "streamer-live-name-preserved", "essence-plans-blue", "essence-plans-orange", "essence-plans-insufficient", "cash-redemption-options", "saved-data-statistics", "storage-summary", "storage-summary-partial", "inbox-circle-badge", "premium-purchase-confirm", "usdt-wallet-fields", "support-admin-notification", "cash-redemption-confirm", "sponsor-layout-small", "sponsor-layout-large", "sponsor-read-retry", "managed-user-secondary", "managed-user-balance-live", "history-circle-notification", "redemption-entry-visible", "redemption-entry-hidden", "premium-plans-overview", "history-receipts", "component-catalog-luchador", "component-catalog-asesino", "component-catalog-tirador", "component-catalog-magico", "component-catalog-defensa", "component-catalog-apoyo").map { arrayOf(it) }
    }
    @get:Rule val compose = createComposeRule()
    private var copiedSummary = ""
    private var grantResult: ((Result<Map<String, Any>>) -> Unit)? = null
    private var grantedAccount: Map<String, Any>? = null
    private var readAttempts = 0
    private var renewed = false
    private val fixedGrantNow = java.time.Instant.parse("2026-10-03T12:00:00Z").toEpochMilli()
    private val inheritedDeadline = java.time.Instant.parse("2030-11-18T19:27:00Z").toEpochMilli()
    private var occupiedAccount = mapOf<String, Any>("uid" to "local-occupied", "role" to "creador", "secondaryRole" to "streamer", "premiumUntil" to inheritedDeadline)
    private var requestedDays = 0
    private var requestedExtension = false
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
        if (screen.startsWith("sponsor-layout") || screen == "sponsor-read-retry") {
            RuntimeEnvironment.setQualifiers("w320dp-h891dp-xxhdpi")
            if (screen == "sponsor-layout-large") {
                val configuration = android.content.res.Configuration(context.resources.configuration).apply { fontScale = 1.5f }
                @Suppress("DEPRECATION")
                context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            }
        }
        if (screen == "managed-user-balance-live") database.collection("users").document("local-managed").set(mapOf("blueEssence" to 1L, "orangeEssence" to 2L))
        DynamicTranslations.loadSync(context)
        WildRiftRepository.initChampions(context, forceReload = true)
        AppLanguage.select(context, "pt")
        AuthManager.isSignedIn.value
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        val field = AuthManager::class.java.getDeclaredField("_isSignedIn").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        (field.get(AuthManager) as MutableStateFlow<Boolean>).value = screen.endsWith("-registered") || screen == "creator-reader" || screen == "support-admin-notification"
        SubscriptionManager.userRole.value
        fun setFlow(target: Any, name: String, value: Any) {
            val variable = target.javaClass.getDeclaredField(name).apply { isAccessible = true }
            @Suppress("UNCHECKED_CAST")
            (variable.get(target) as MutableStateFlow<Any>).value = value
        }
        setFlow(SubscriptionManager, "_userRole", if (screen == "support-admin-notification" || screen == "moderation-admin" || screen.startsWith("profile-admin") || screen in listOf("premium-editor-grant", "premium-editor-occupied")) "admin" else "free")
        if (screen == "premium-purchase-confirm") { setFlow(SubscriptionManager,"_blueEssence",1200L); setFlow(SubscriptionManager,"_orangeEssence",100L) }
        else { setFlow(SubscriptionManager,"_blueEssence",0L); setFlow(SubscriptionManager,"_orangeEssence",0L) }
        setFlow(SubscriptionManager,"_currentUserUid",if (screen == "support-admin-notification") "local-notification-admin" else "")
        if (screen == "support-admin-notification") {
            database.collection("support_reports").document("admin-new-message").set(mapOf("userId" to "other-user", "status" to "PENDING", "staffRead" to false,
                "conversation" to listOf(mapOf("id" to "new-message", "senderRole" to "USER", "text" to "Ajuda"))))
        }
        setFlow(SubscriptionManager, "_secondaryRole", if (screen == "moderation-secondary") "moderador" else "")
        setFlow(AuthManager, "_isAdminClaim", screen == "moderation-claim")
        if (screen == "creator-reader") setFlow(com.example.data.local.CustomChampionBuildsManager, "_customBuilds",
            com.example.data.local.CustomChampionBuildsManager.getDefaultBuilds(context))
        if (screen.startsWith("champion")) setFlow(com.example.data.local.CustomChampionBuildsManager, "_customBuilds",
            com.example.data.local.CustomChampionBuildsManager.getDefaultBuilds(context))
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
            screen.startsWith("component-catalog-") -> ItemsCatalogTab()
            screen == "history-receipts" -> Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(12.dp)) {
                listOf(
                    SubscriptionRecord(id = "blue-gift",timestamp = fixedGrantNow,planName = "Regalo de Esencias",status = "Añadido por Administrador",amount = "+100 EA",source = "ADMIN_ESSENCE_ADJUSTMENT"),
                    SubscriptionRecord(id = "orange-gift",timestamp = fixedGrantNow,planName = "Regalo de Esencias",status = "Añadido por Administrador",amount = "+25 EN",source = "ADMIN_ESSENCE_ADJUSTMENT"),
                    SubscriptionRecord(id = "premium-gift",timestamp = fixedGrantNow,planName = "Suscripción Premium regalada",status = "Completado",amount = "Regalo",source = "ADMIN_GIFT",durationMillis = 30L*86400000),
                    SubscriptionRecord(id = "monthly",timestamp = fixedGrantNow,planName = "Suscripción Premium mensual",status = "Completado",amount = "-9 EN",source = "ESSENCE_PURCHASE",durationMillis = 30L*86400000),
                    SubscriptionRecord(id = "payment",timestamp = fixedGrantNow,planName = "Canje de Esencia Naranja",status = "Pendiente",amount = "-25 EN",source = "CASH_REDEMPTION")
                ).forEach { SubscriptionHistoryItem(it) }
            }
            screen.startsWith("sponsor-layout") || screen == "sponsor-read-retry" -> Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(12.dp)) {
                AdminSponsorNoticeItem(com.example.data.AppNotice(id = "local-sponsor", title = "Teste de anúncio", content = "Conteúdo do anúncio",
                    sponsorEmail = "barbachavezdiego@example.invalid", isApproved = false, budget = 25.0, durationValue = 3), {}, {},
                    readAction = { event -> readAttempts++; if (readAttempts == 1) error("local-failure") else com.example.data.PanelReadRepository.updateRead(setOf(com.example.data.PanelReadRepository.key(event))) })
            }
            screen.startsWith("managed-user") -> UserDetailManagementDialog(mapOf("uid" to "local-managed", "name" to "Teste", "role" to "creador",
                "secondaryRole" to "aspirante", "blueEssence" to 1L, "orangeEssence" to 2L), {}, {}, {}, {})
            screen == "history-circle-notification" -> CircularPanelNotificationButton(3,com.example.data.NotificationPanel.HISTORY,tr("Historial"),
                androidx.compose.material.icons.Icons.Filled.History) {}
            screen.startsWith("redemption-entry") -> OrangeEssenceRedemptionEntry(if (screen.endsWith("hidden")) 0 else 10) { copiedSummary = "opened" }
            screen == "premium-plans-overview" -> Column(Modifier.verticalScroll(rememberScrollState())) { PremiumPlansOverview(1200, 100); EssencePlanOptions(1200, 100) { _, _ -> } }
            screen == "cash-redemption-confirm" -> CashRedemptionConfirmation(25,com.example.data.UsdtNetwork.ERC20,"0x1111111111111111111111111111111111111111",false,onConfirm={copiedSummary="confirmed"},onDismiss={copiedSummary="cancelled"})
            screen == "premium-purchase-confirm" -> SubscriptionPlansBottomSheet {}
            screen == "usdt-wallet-fields" -> UsdtWalletFields(com.example.data.UsdtNetwork.ERC20,"0x1111111111111111111111111111111111111111",true,{},{})
            screen == "support-admin-notification" -> {
                val summary = userPanelNotificationSummary()
                Column {
                    PanelNotificationBadge(summary.count(com.example.data.NotificationPanel.SUPPORT),com.example.data.NotificationPanel.SUPPORT)
                    UserNotificationIcon(summary.total)
                }
            }
            screen.startsWith("essence-plans") -> Column(Modifier.verticalScroll(rememberScrollState())) {
                EssencePlanOptions(if (screen == "essence-plans-insufficient") 99 else 1200,
                    if (screen == "essence-plans-blue") 0 else if (screen == "essence-plans-insufficient") 8 else 100) { plan, currency -> copiedSummary = "${plan.name}:${currency.name}" }
            }
            screen == "cash-redemption-options" -> CashRedemptionOptions(25) { copiedSummary = it.toString() }
            screen.startsWith("storage-summary") -> StorageConsumptionSummary(com.example.data.StorageConsumption(
                estimatedBytes = 2_097_152, dailyGrowthBytes = if (screen.endsWith("partial")) null else 1024,
                sampledAtMillis = fixedGrantNow, complete = !screen.endsWith("partial")))
            screen == "saved-data-statistics" -> SavedDataStatisticsContent(listOf(
                com.example.data.SavedDataStatistic("Cuentas registradas", "Perfiles, roles, saldos, suscripciones y dispositivos registrados.", 42),
                com.example.data.SavedDataStatistic("Contadores de streamers", "Clics acumulados de cada publicación.", 0),
                com.example.data.SavedDataStatistic("Solicitudes de canje", "Importes solicitados, pagos manuales y devoluciones.", failed = true)))
            screen == "inbox-circle-badge" -> Box(Modifier.padding(16.dp)) { CircularPanelNotificationButton(12, com.example.data.NotificationPanel.INBOX,
                tr("Bandeja de Entrada"), androidx.compose.material.icons.Icons.Default.Message) { copiedSummary = "opened" } }
            screen == "panel-notification-animation" -> PanelNotificationBadge(3, com.example.data.NotificationPanel.CREATOR)
            screen.startsWith("user-notification") -> UserNotificationIcon(if (screen == "user-notification-empty") 0 else 3)
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
            screen in listOf("streamer-live", "streamer-live-name-preserved") -> LiveStreamerChip(if (screen == "streamer-live-name-preserved") "hola" else "Canal Coach") {}
            screen == "streamer-feedback" -> StreamerSubmissionFeedback(false, true)
            screen == "streamer-history" -> {
                val now = System.currentTimeMillis()
                val records = listOf("APPROVED", "REJECTED", "ENDED", "PENDING").mapIndexed { i, status ->
                    mapOf<String, Any>("publicationId" to "history-$i", "channelName" to "Canal $i", "status" to status,
                        "submittedAtMillis" to now - (if (status == "PENDING") 4 * 60 * 60 * 1000L else (i + 1) * 24 * 60 * 60 * 1000L), "endedAtMillis" to now - 3600000L, "clickCount" to if (i == 0) 42L else i.toLong())
                }
                Column(Modifier.verticalScroll(rememberScrollState())) { StreamerPublicationHistory(records + mapOf<String, Any>("publicationId" to "history-old", "channelName" to "Canal Expirado", "status" to "ENDED", "endedAtMillis" to now - StreamerPublicationPolicy.HISTORY_WINDOW_MILLIS,
                    "submittedAtMillis" to now - StreamerPublicationPolicy.HISTORY_WINDOW_MILLIS), now) { copiedSummary = it } }
            }
            screen.startsWith("streamer") -> Column { StreamerUrlRecommendations(screen == "streamer-admin") {} }
            screen.startsWith("moderation") -> ModeratorDashboardDialog {}
            screen.startsWith("profile-admin") -> {
                val user = Mockito.mock(FirebaseUser::class.java)
                Mockito.`when`(user.uid).thenReturn("local-profile-test")
                Mockito.`when`(user.email).thenReturn("coach@example.invalid")
                Mockito.`when`(user.displayName).thenReturn("Coach Teste")
                val panels = when (screen) {
                    "profile-admin-notifications" -> com.example.data.PanelNotificationState(com.example.data.NotificationPanel.entries.associateWith { setOf("event:${it.name}") })
                    "profile-admin-large" -> com.example.data.PanelNotificationState(mapOf(com.example.data.NotificationPanel.SPONSOR_MODERATION to setOf("notice:local-pending")))
                    else -> com.example.data.PanelNotificationState()
                }
                AuthenticatedProfilePanel(user, panelNotifications = panels) {}
            }
            screen == "creator-reader" -> AdminCreatorBuildsDialog {}
            screen == "premium-status-near-expiry" -> PremiumStatusCard("premium", until = System.currentTimeMillis() + 65000L, onRenew = { renewed = true })
            screen == "premium-editor-admin" -> UserDetailManagementDialog(mapOf("uid" to "local-admin", "role" to "admin"), {}, {}, {}, {})
            screen == "premium-editor-occupied" -> UserDetailManagementDialog(occupiedAccount, {}, { occupiedAccount = it; grantedAccount = it }, {}, {},
                premiumGrantAction = { days, extend, complete -> requestedDays = days; requestedExtension = extend; grantResult = complete })
            screen == "premium-editor-grant" -> UserDetailManagementDialog(mapOf("uid" to "local-gift", "role" to "free"), {}, { grantedAccount = it }, {}, {},
                premiumGrantAction = { _, _, complete -> grantResult = complete })
            screen.startsWith("premium-editor") -> UserDetailManagementDialog(mapOf("uid" to "local-test", "name" to "Teste",
                "role" to if (screen == "premium-editor-secondary") "creador" else "premium",
                "secondaryRole" to if (screen == "premium-editor-secondary") "moderador" else "",
                "premiumUntil" to System.currentTimeMillis() + 86400000L), {}, {}, {}, {})
            screen.startsWith("champion") -> ChampionDetailSheet(isOverlay = screen.endsWith("-advice"), champion = WildRiftRepository.champions.first { it.id == "garen" }, onDismiss = {})
            screen.startsWith("tier") -> TierListTab(onSelectChampion = {})
            else -> {
                val realOwn = WildRiftRepository.champions.first { it.id == "ahri" }
                    .takeIf { screen == "draft-own-only" || screen == "draft-both" || screen == "draft-placeholder-rival" }
                val realRival = WildRiftRepository.champions.first { it.id == "yasuo" }
                    .takeIf { screen == "draft-known-first-pick" || screen == "draft-rival-only" || screen == "draft-both" || screen == "draft-placeholder-own" }
                val own = if (screen in listOf("draft-placeholder", "draft-placeholder-own")) WildRiftRepository.EMPTY_CHAMPION else realOwn
                val rival = if (screen in listOf("draft-placeholder", "draft-placeholder-rival")) WildRiftRepository.EMPTY_CHAMPION else realRival
                val allies = own?.let { listOf(DraftSlot(it, LaneRole.MID)) }.orEmpty()
                val enemies = rival?.let { listOf(DraftSlot(it, LaneRole.MID)) }.orEmpty()
                val analysis = WildRiftRepository.analyzeDraft(LaneRole.MID, allies.map { it.champion },
                    enemies.map { it.champion }, rival, isFirstPick = screen == "draft-known-first-pick", lang = "pt")
                DraftAnalysisTab(myChampion = own, activeRole = LaneRole.MID, allySlots = allies,
                    enemySlots = enemies, analysis = analysis, isFirstPick = screen == "draft-known-first-pick", enemyLaneOpponent = rival,
                    onToggleFirstPick = {}, onChangeRole = {}, onPickAllyRole = {}, onPickEnemyRole = {},
                    onRemoveAllyRole = {}, onRemoveEnemyRole = {}, onPickRecommendation = {},
                    onSelectChampion = {}, onOpenHistory = {}, onClearAll = {})
            }
        }
    }

    private fun inspect(step: String) {
        compose.waitForIdle()
        val nodes = compose.onAllNodes(SemanticsMatcher("all") { true }, useUnmergedTree = true).fetchSemanticsNodes()
        val strings = nodes.flatMap {
                it.config.getOrNull(SemanticsProperties.Text).orEmpty().map { text -> text.text } +
                    it.config.getOrNull(SemanticsProperties.ContentDescription).orEmpty()
            }.filter { it.isNotBlank() }.distinct()
        Assert.assertTrue(strings.isNotEmpty())
        File(output, "$screen-$step.json").writeText(JSONArray(strings).toString(2))
        val capture = if (screen == "premium-purchase-confirm" && step == "confirmation")
            compose.onNode(isRoot() and hasAnyDescendant(hasTestTag("premium_purchase_confirm")))
            else compose.onAllNodes(isRoot()).onLast()
        capture.captureRoboImage(filePath = File(output, "$screen-$step.png").path)
        val interfaceStrings = nodes.flatMap {
            val text = if (it.config.getOrNull(SemanticsProperties.TestTag) == "streamer_channel_name") emptyList()
                else it.config.getOrNull(SemanticsProperties.Text).orEmpty().map { value -> value.text }
            text + it.config.getOrNull(SemanticsProperties.ContentDescription).orEmpty()
        }
        val failures = interfaceStrings.filter { SpanishUiResidue.pattern.containsMatchIn(it.replace("Lee Sin", "LeeSin")) }
        Assert.assertTrue("Spanish on $screen: ${failures.joinToString()}", failures.isEmpty())
    }

    @Test fun `visibility and Portuguese wording follow actual selections and account access`() {
        // Enable animation frames before composition: the test framework cancels
        // infinite animations while its clock advances automatically.
        if (screen in listOf("streamer-live", "panel-notification-animation")) compose.mainClock.autoAdvance = false
        compose.setContent { MyApplicationTheme { Box(Modifier.fillMaxSize()) { surface() } } }
        if (screen in listOf("streamer-live", "panel-notification-animation")) compose.mainClock.advanceTimeBy(32)
        compose.waitForIdle()
        if (screen.startsWith("component-catalog-")) {
            val section = mapOf("luchador" to "Luchador","asesino" to "Asesino","tirador" to "Tirador","magico" to "Mágico","defensa" to "Defensa","apoyo" to "Apoyo").getValue(screen.removePrefix("component-catalog-"))
            compose.onNodeWithTag("catalog_section_$section").performClick()
            compose.onNodeWithContentDescription(appTr("Minimizar filtros")).performClick()
            compose.onNodeWithTag("catalog_level_3").performScrollTo().performClick()
            if (section == "Apoyo") compose.onNodeWithTag("catalog_group_${section}_Básico").assertDoesNotExist()
            else {
                compose.onNodeWithTag("catalog_group_${section}_Básico").assertExists()
                val first = com.example.data.WildRiftComponentItemsData.getItems(section,"Básico").first()
                compose.onNodeWithText(first.getLocalizedName("pt"),useUnmergedTree=true).assertExists()
            }
            compose.onNodeWithTag("catalog_group_${section}_Nivel Medio").assertDoesNotExist()
            inspect("basic")
            compose.onNodeWithTag("catalog_level_2").performScrollTo().performClick()
            compose.onNodeWithTag("catalog_group_${section}_Nivel Medio").assertExists()
            compose.onNodeWithTag("catalog_group_${section}_Básico").assertDoesNotExist()
            val first = com.example.data.WildRiftComponentItemsData.getItems(section,"Nivel Medio").first()
            compose.onNodeWithText(first.getLocalizedName("pt"),useUnmergedTree=true).assertExists()
            inspect("medium")
            compose.onNodeWithTag("catalog_level_0").performScrollTo().performClick()
            compose.onAllNodesWithTag("catalog_panel_$section").assertCountEquals(1)
            val groups = com.example.data.WildRiftItemsData.getCatalogGroups(section)
            val headers = groups.map { group ->
                val node = compose.onNodeWithTag("catalog_group_${section}_${group.level}", useUnmergedTree = true).fetchSemanticsNode()
                var ancestor = node.parent
                while (ancestor != null && ancestor.config.getOrNull(SemanticsProperties.TestTag) != "catalog_panel_$section") ancestor = ancestor.parent
                Assert.assertNotNull("Every level must belong to the same section panel", ancestor)
                // boundsInRoot clips offscreen headers to zero. Use their placed
                // positions to check order inside a panel taller than the viewport.
                node.positionInRoot.y
            }
            Assert.assertEquals("Completed, medium, then starting items", headers.sorted(), headers)
            inspect("unified")
        }
        when (screen) {
            "history-receipts" -> {
                compose.onNodeWithText("+100 EA").assertExists()
                compose.onNodeWithText("-25 EN").performScrollTo().assertIsDisplayed()
                inspect("cash")
                compose.onNodeWithText("+100 EA").performScrollTo()
            }
            "sponsor-layout-small", "sponsor-layout-large" -> {
                val email = compose.onNodeWithText(appTr("Patrocinador: barbachavezdiego@example.invalid")).performScrollTo().fetchSemanticsNode().boundsInRoot
                inspect("email")
                Assert.assertTrue("Sponsor email must retain usable width", email.width > context.resources.displayMetrics.density * 170)
                Assert.assertTrue("No one-letter column", email.height < context.resources.displayMetrics.density * 100)
                compose.onNodeWithText(appTr("Marcar como leído")).performScrollTo().assertIsDisplayed()
            }
            "sponsor-read-retry" -> {
                compose.onNodeWithText(appTr("Marcar como leído")).performScrollTo().performClick()
                compose.onNodeWithText(appTr("No se pudo marcar como leído. Vuelve a intentarlo.")).performScrollTo().assertIsDisplayed()
                inspect("failure")
                compose.onNodeWithText(appTr("Marcar como leído")).performScrollTo().performClick()
                compose.onNodeWithText(appTr("Marcar como leído")).assertDoesNotExist()
                compose.onNodeWithText(appTr("No se pudo marcar como leído. Vuelve a intentarlo.")).assertDoesNotExist()
                Assert.assertEquals(2,readAttempts)
            }
            "managed-user-secondary" -> {
                compose.onNodeWithTag("managed_user_secondary_role",useUnmergedTree = true).assertExists()
                compose.onNodeWithText(appTr("ASPIRANTE")).assertExists()
            }
            "managed-user-balance-live" -> {
                compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasTestTag("managed_user_blue_balance"))
                compose.onNodeWithTag("managed_user_blue_balance").assertTextEquals("1 EA")
                FirebaseFirestore.getInstance().collection("users").document("local-managed").update(mapOf("blueEssence" to 35L, "orangeEssence" to 10L))
                compose.waitUntil(10000) { compose.onNodeWithTag("managed_user_blue_balance").fetchSemanticsNode().config[SemanticsProperties.Text].first().text == "35 EA" }
                compose.onNodeWithTag("managed_user_orange_balance").assertTextEquals("10 EN")
                FirebaseFirestore.getInstance().collection("users").document("local-managed").update("blueEssence",5L)
                compose.waitUntil(10000) { compose.onNodeWithTag("managed_user_blue_balance").fetchSemanticsNode().config[SemanticsProperties.Text].first().text == "5 EA" }
            }
            "history-circle-notification" -> {
                compose.onNodeWithTag("panel_notification_badge_HISTORY",useUnmergedTree = true).onChildren().onFirst().assertTextEquals("3")
                compose.onNodeWithTag("panel_notification_icon_HISTORY",useUnmergedTree = true).assertExists()
            }
            "redemption-entry-visible" -> {
                compose.onNodeWithTag("orange_redemption_entry").assertIsDisplayed().performClick()
                Assert.assertEquals("opened",copiedSummary)
            }
            "redemption-entry-hidden" -> {
                compose.onNodeWithTag("orange_redemption_entry").assertDoesNotExist()
                return
            }
            "premium-plans-overview" -> {
                compose.onNodeWithText(appTr("Tu próximo nivel en Coach")).assertIsDisplayed()
                compose.onNodeWithTag("premium_ANNUAL_ORANGE").performScrollTo().assertIsDisplayed()
                inspect("annual")
                compose.onNodeWithText(appTr("Tu próximo nivel en Coach")).performScrollTo()
            }
            "draft-known-first-pick" -> {
                compose.onNodeWithTag("draft_recommendations").performScrollTo().assertExists()
                compose.onNodeWithText(appTr(" #1 RECOMENDACIÓN BLIND PICK")).assertDoesNotExist()
                compose.onNodeWithText(appTr(" #1 MEJOR ELECCIÓN TÁCTICA")).performScrollTo().assertExists()
            }
            "cash-redemption-confirm" -> {
                compose.onNodeWithText(appTr("Confirmar canje")).assertExists()
                compose.onNodeWithTag("cash_redemption_confirm").assertIsEnabled()
                compose.onNodeWithTag("cash_redemption_cancel").performClick()
                Assert.assertEquals("cancelled",copiedSummary)
            }

            "premium-purchase-confirm" -> {
                compose.onNodeWithTag("premium_MONTHLY_ORANGE").performScrollTo().performClick()
                compose.onNodeWithText(appTr("Confirmar suscripción")).assertIsDisplayed()
                compose.onNodeWithTag("premium_MONTHLY_ORANGE").assertDoesNotExist()
                compose.onNodeWithTag("premium_purchase_confirm").assertIsEnabled()
                inspect("confirmation")
                compose.onNodeWithText(appTr("Cancelar")).performClick()
                compose.onNodeWithTag("premium_purchase_confirm").assertDoesNotExist()
                compose.onNodeWithTag("premium_MONTHLY_ORANGE").assertExists()
            }
            "usdt-wallet-fields" -> {
                compose.onNodeWithTag("usdt_wallet").assertTextContains("0x1111111111111111111111111111111111111111")
                compose.onNodeWithText(appTr("Billetera USDT no válida")).assertDoesNotExist()
            }
            "support-admin-notification" -> {
                compose.waitUntil(10000) { compose.onAllNodesWithTag("panel_notification_badge_SUPPORT",useUnmergedTree=true).fetchSemanticsNodes().isNotEmpty() }
                compose.onNodeWithTag("panel_notification_badge_SUPPORT",useUnmergedTree=true).assertExists()
                compose.onNodeWithTag("user_navigation_badge",useUnmergedTree=true).assertExists()
            }

            "essence-plans-blue" -> {
                compose.onNodeWithTag("premium_MONTHLY_BLUE").assertIsEnabled()
                compose.onNodeWithTag("premium_ANNUAL_BLUE").performScrollTo().assertIsEnabled()
                compose.onNodeWithTag("premium_MONTHLY_ORANGE").assertDoesNotExist()
                compose.onNodeWithTag("premium_ANNUAL_ORANGE").assertDoesNotExist()
            }
            "essence-plans-orange" -> {
                compose.onNodeWithTag("premium_MONTHLY_ORANGE").performScrollTo().assertIsEnabled().performClick()
                Assert.assertEquals("MONTHLY:ORANGE", copiedSummary)
                compose.onNodeWithTag("premium_ANNUAL_ORANGE").performScrollTo().assertIsEnabled().performClick()
                Assert.assertEquals("ANNUAL:ORANGE", copiedSummary)
            }
            "essence-plans-insufficient" -> {
                for (plan in listOf("MONTHLY", "ANNUAL")) for (currency in listOf("BLUE", "ORANGE"))
                    compose.onNodeWithTag("premium_${plan}_$currency").performScrollTo().assertIsNotEnabled()
            }
            "cash-redemption-options" -> {
                compose.onNodeWithTag("cash_redemption_10").assertIsEnabled()
                compose.onNodeWithTag("cash_redemption_25").assertIsEnabled().performClick()
                Assert.assertEquals("25", copiedSummary)
                compose.onNodeWithTag("cash_redemption_50").assertIsNotEnabled()
            }
            "storage-summary", "storage-summary-partial" -> {
                compose.onNodeWithText(appTr("Tu almacenamiento")).assertIsDisplayed()
                compose.onNodeWithText(appTr("Contenido guardado estimado") + ": 2.00 MB").assertIsDisplayed()
                compose.onNodeWithText(appTr("Límite del plan: consulta del proveedor no conectada")).assertExists()
            }
            "saved-data-statistics" -> {
                compose.onNodeWithTag("saved_data_count_0").assertTextEquals("42")
                compose.onNodeWithTag("saved_data_count_1").assertTextEquals("0")
                compose.onNodeWithTag("saved_data_count_2").assertTextEquals(appTr("No disponible"))
            }
            "inbox-circle-badge" -> {
                val icon = compose.onNodeWithTag("panel_notification_icon_INBOX", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
                val badge = compose.onNodeWithTag("panel_notification_badge_INBOX", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
                Assert.assertTrue("Count is outside the bell", badge.left > icon.right)
                compose.onNodeWithContentDescription(appTr("Bandeja de Entrada")).performClick()
                Assert.assertEquals("opened", copiedSummary)
            }

            "user-notification" -> {
                compose.onNodeWithTag("user_navigation_badge").assertExists()
                compose.onNodeWithText("3").assertExists()
                compose.onNodeWithContentDescription("Notificações").assertExists()
            }
            "user-notification-empty" -> {
                compose.onNodeWithTag("user_navigation_badge").assertDoesNotExist()
                compose.onNodeWithContentDescription("Usuário").assertExists()
            }
            "champion-item-advice", "champion-spell-advice", "champion-rune-advice" -> {
                val category = screen.removePrefix("champion-").removeSuffix("-advice")
                if (category == "item") {
                    // The item strip scrolls horizontally; first reveal its parent vertically.
                    compose.onNodeWithTag("build_core_items_section", useUnmergedTree = true).performScrollTo()
                    compose.onAllNodesWithTag("build_item_details", useUnmergedTree = true).onFirst().assertIsDisplayed().performClick()
                } else compose.onAllNodesWithTag("build_${category}_details", useUnmergedTree = true).onFirst().performScrollTo().performClick()
                inspect("opened")
                compose.onNodeWithTag("build_element_advice_card", useUnmergedTree = true).performScrollTo().assertExists()
                compose.onNodeWithTag("build_element_advice_card", useUnmergedTree = true).captureRoboImage(filePath = File(output, "$screen-framed-card.png").path)
            }
            "champion-guest" -> {
                compose.onNodeWithTag("detailed_trend_graph").assertDoesNotExist()
                compose.onNodeWithTag("champion_trend_header").assertDoesNotExist()
                compose.onNodeWithTag("matchup_sign_in_hint").performScrollTo().assertExists()
                inspect("account-hint")
            }
            "champion-registered" -> {
                compose.onNodeWithTag("champion_trend_header").performScrollTo().assertExists()
                compose.onNodeWithTag("detailed_trend_graph").performScrollTo().assertExists()
                compose.onNodeWithTag("matchup_sign_in_hint").assertDoesNotExist()
            }
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
            "streamer-live-name-preserved" -> {
                compose.onNodeWithTag("streamer_channel_name", useUnmergedTree = true).assertTextEquals("hola")
                compose.onNodeWithText("Ao vivo").assertExists()
            }
            "streamer-live" -> {
                compose.onNodeWithText("Canal Coach").assertExists()
                compose.onNodeWithText("Ao vivo").assertExists()
                compose.onNodeWithTag("live_streamer_chip").assertExists()
                compose.onNodeWithTag("streamer_live_animation", useUnmergedTree = true).assertExists()
                compose.mainClock.autoAdvance = false
                inspect("animation-start")
                compose.mainClock.advanceTimeBy(480)
                inspect("animation-next")
                Assert.assertFalse(File(output, "$screen-animation-start.png").readBytes().contentEquals(
                    File(output, "$screen-animation-next.png").readBytes()))
                compose.mainClock.autoAdvance = true
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
                compose.onNodeWithText("Encerrar conversa").assertExists()
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
                val feedback = ChampionMatchupCoaching.sovereignFeedback(champion, if (screen == "matchup-garen") LaneRole.TOP else LaneRole.ADC,
                    "pt", WildRiftRepository.champions.first { it.id == "smolder" })
                compose.onNodeWithText(feedback).performScrollTo().assertExists()
                val titles = listOf("Diagnóstico do erro/situação", "Decisão Soberano", "Micro e Macro detalhe", "Regra aplicável")
                Assert.assertTrue(titles.zipWithNext().all { (a, b) -> feedback.indexOf(a) < feedback.indexOf(b) })
                inspect("sovereign-feedback")
            }
            "streamer-feedback" -> compose.onNodeWithText("Solicitação enviada com sucesso. Você será avisado quando ela for analisada.").assertExists()
            "streamer-history" -> {
                compose.onNodeWithText("Histórico de publicações").assertExists()
                compose.onNodeWithText("Canal Expirado").assertDoesNotExist()
                compose.onNodeWithTag("streamer_history_copy_history-0").performScrollTo().performClick()
                Assert.assertTrue(copiedSummary, copiedSummary.contains("Cliques para abrir o canal: 42"))
                Assert.assertTrue(copiedSummary, copiedSummary.contains("Aceita"))
                Assert.assertTrue(copiedSummary, copiedSummary.contains("Data e hora:"))
                Assert.assertTrue(copiedSummary, copiedSummary.contains("A contagem de sete dias começará"))
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
                compose.onNodeWithText("Solicitação pendente").assertDoesNotExist()
                if (screen == "profile-admin-large") compose.onNodeWithTag("panel_notification_badge_SPONSOR_MODERATION", useUnmergedTree = true).assertExists()
                inspect("role-buttons")
                compose.onNodeWithText(appTr("Panel de Soporte y Moderación")).performScrollTo().performClick()
                compose.onNodeWithText(appTr("Bandeja de Moderación")).assertExists()
            }
            "creator-reader" -> {
                compose.onNodeWithTag("creator_create_build").assertDoesNotExist()
                compose.onNodeWithText(appTr("Panel de Creador")).assertExists()
                compose.onAllNodes(hasTestTag("creator_build_list"), useUnmergedTree = true).assertCountEquals(0)
            }
            "premium-editor-admin" -> {
                compose.onNodeWithTag("premium_status_card").assertExists()
                compose.onNodeWithText(appTr("Gestión de Suscripción Premium")).assertDoesNotExist()
                compose.onNodeWithTag("premium_duration_+1 Día").assertDoesNotExist()
            }
            "premium-status-near-expiry" -> {
                compose.onNodeWithTag("premium_remaining_time").assertExists()
                compose.onNodeWithTag("premium_expiry_warning").assertExists()
                compose.onNodeWithTag("premium_renew_button").performClick()
                Assert.assertTrue(renewed)
            }
            "premium-editor-grant" -> {
                compose.onNodeWithTag("premium_duration_+1 Día").performScrollTo().performClick()
                compose.onNodeWithTag("premium_duration_+1 Día").assertIsNotEnabled()
                compose.runOnIdle {
                    val update = com.example.data.PremiumGrantPolicy.apply(mapOf("uid" to "local-gift", "role" to "free"), 1, true, System.currentTimeMillis(), "gift-ui")
                    grantResult!!(Result.success(update))
                }
                compose.onNodeWithTag("premium_duration_+1 Día").assertIsEnabled()
                compose.onNodeWithTag("premium_remaining_time").performScrollTo().assertExists()
                Assert.assertEquals("free", grantedAccount!!["role"])
                Assert.assertEquals(1, (grantedAccount!!["subscriptionHistory"] as List<*>).size)
            }
            "premium-editor-occupied" -> {
                compose.onNodeWithTag("premium_remaining_time").performScrollTo().assertExists()
                for ((label, days) in listOf("+1 Día" to 1, "+7 Días" to 7, "+30 Días" to 30, "+90 Días (3m)" to 90, "+1 Año (365d)" to 365)) {
                    val before = occupiedAccount["premiumUntil"] as Long
                    compose.onNodeWithTag("premium_duration_$label").performScrollTo().performClick()
                    compose.onNodeWithTag("premium_duration_$label").assertIsNotEnabled()
                    Assert.assertEquals(days, requestedDays)
                    Assert.assertTrue(requestedExtension)
                    compose.runOnIdle {
                        grantResult!!(Result.success(com.example.data.PremiumGrantPolicy.apply(occupiedAccount, requestedDays,
                            requestedExtension, fixedGrantNow, "occupied-$days")))
                    }
                    compose.onNodeWithTag("premium_duration_$label").assertIsEnabled()
                    Assert.assertEquals(before + days * PremiumAccessPolicy.DAY_MILLIS, occupiedAccount["premiumUntil"])
                    Assert.assertEquals("creador", occupiedAccount["role"])
                    Assert.assertEquals("streamer", occupiedAccount["secondaryRole"])
                    inspect("added-$days-days")
                }
                Assert.assertEquals(5, (occupiedAccount["subscriptionHistory"] as List<*>).size)
            }
            "profile-admin-notifications" -> {
                for (panel in com.example.data.NotificationPanel.entries) {
                    compose.onNodeWithTag("panel_notification_badge_${panel.name}", useUnmergedTree = true).performScrollTo().assertExists()
                    inspect("panel-${panel.name.lowercase()}")
                }
                compose.onNodeWithText("Solicitação pendente").assertDoesNotExist()
            }
            "panel-notification-animation" -> {
                compose.onNodeWithTag("panel_notification_badge_CREATOR", useUnmergedTree = true).assertExists()
                compose.onNodeWithContentDescription("3 notificações pendentes", useUnmergedTree = true).assertExists()
                inspect("animation-start")
                compose.mainClock.advanceTimeBy(480)
                inspect("animation-next")
                Assert.assertFalse(File(output, "$screen-animation-start.png").readBytes().contentEquals(File(output, "$screen-animation-next.png").readBytes()))
                compose.mainClock.autoAdvance = true
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
