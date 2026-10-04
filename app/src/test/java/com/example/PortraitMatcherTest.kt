package com.example

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.data.WildRiftRepository
import com.example.model.Champion
import com.example.model.LaneRole
import com.example.service.OverlayState
import com.example.service.applyConfirmedLastPick
import com.example.service.screen.DraftScanResult
import com.example.service.screen.LiteRTVisionClassifier
import com.example.service.screen.PortraitMatcher
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PortraitMatcherTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private var savedCatalog: List<Champion> = emptyList()

    @Before fun prepare() {
        savedCatalog = WildRiftRepository.champions.toList()
        WildRiftRepository.champions.clear()
        context.assets.list("champions")!!.filter { it.endsWith(".png") }.forEach {
            WildRiftRepository.champions.add(Champion(id = it.removeSuffix(".png"), name = it.removeSuffix(".png")))
        }
        LiteRTVisionClassifier.reset()
        LiteRTVisionClassifier.setThreshold(0.80f, context)
    }

    @After fun restore() {
        LiteRTVisionClassifier.reset()
        WildRiftRepository.champions.clear()
        WildRiftRepository.champions.addAll(savedCatalog)
    }

    private fun fixture(name: String): Bitmap = javaClass.classLoader!!.getResourceAsStream("portraits/$name.png")!!
        .use { BitmapFactory.decodeStream(it)!! }

    @Test fun screenshotRanksVolibearAcrossEntireCatalogAndSurvivesLoading() = runBlocking {
        val crop = fixture("volibear-slot")
        try {
            assertNull(LiteRTVisionClassifier.executeTenthPickInference(crop, false, emptySet(), 9, context = context))
            val first = LiteRTVisionClassifier.reportFlow.value
            assertEquals("volibear", first.topCandidates.first().champion.id)
            assertTrue(first.topCandidates.first().similarityScore >= 0.80f)
            assertTrue(first.topCandidates[0].similarityScore - first.topCandidates[1].similarityScore > 0.08f)
            val result = LiteRTVisionClassifier.executeTenthPickInference(crop, false, emptySet(), 9, context = context)
            assertEquals("volibear", result?.first?.id)
            assertEquals("volibear", LiteRTVisionClassifier.executeTenthPickInference(null, false, emptySet(), 9, context = context)?.first?.id)
            assertTrue(LiteRTVisionClassifier.reportFlow.value.isConfirmed)
        } finally { crop.recycle() }
    }

    private fun viFixture(): Bitmap {
        val json = javaClass.classLoader!!.getResourceAsStream("portraits/vi-tenth-slot.json")!!
            .bufferedReader().use { it.readText() }
        val bytes = java.util.Base64.getDecoder().decode(org.json.JSONObject(json).getString("pngBase64"))
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)!!
    }

    private fun ninePickState(finalIsAlly: Boolean): OverlayState {
        val full = listOf("skarner", "pantheon", "brand", "jinx", "yuumi").map { Champion(id = it) }
        val pending = listOf("darius", null, "mel", "caitlyn", "milio").map { it?.let { Champion(id = it) } }
        return OverlayState().apply {
            (if (finalIsAlly) enemies else allies).apply { clear(); addAll(full) }
            (if (finalIsAlly) allies else enemies).apply { clear(); addAll(pending) }
        }
    }

    @Test fun screenshotViAutomaticallyFillsOnlyTenthHudVacancyOnEitherSide() = runBlocking {
        val crop = viFixture()
        try {
            for (finalIsAlly in listOf(false, true)) {
                LiteRTVisionClassifier.reset()
                // The supplied capture is a rival slot. For the ally-side regression,
                // change only the outer team ring, outside the descriptor's sampled disk.
                val teamCrop = if (!finalIsAlly) crop else crop.copy(Bitmap.Config.ARGB_8888, true).apply {
                    for (y in 0 until height) for (x in 0 until width) {
                        val nx = (x + 0.5f) / width - 0.5f
                        val ny = (y + 0.5f) / height - 0.5f
                        if (nx * nx + ny * ny >= 0.43f * 0.43f)
                            setPixel(x, y, android.graphics.Color.rgb(40, 150, 240))
                    }
                }
                val hud = ninePickState(finalIsAlly)
                val original = (hud.allies + hud.enemies).map { it?.id }
                val selected = original.filterNotNull().toSet()
                assertNull(LiteRTVisionClassifier.executeTenthPickInference(teamCrop, finalIsAlly, selected, 9, context = context))
                val first = LiteRTVisionClassifier.reportFlow.value
                assertEquals("vi", first.topCandidates.first().champion.id)
                assertTrue(first.topCandidates.first().similarityScore >= 0.80f)
                assertTrue(first.topCandidates[0].similarityScore - first.topCandidates[1].similarityScore >= PortraitMatcher.MIN_MARGIN)
                assertFalse(first.isConfirmed)
                val detected = LiteRTVisionClassifier.executeTenthPickInference(teamCrop, finalIsAlly, selected, 9, context = context)
                assertEquals("vi", detected?.first?.id)
                val scan = DraftScanResult(hud.allies.filterNotNull(), hud.enemies.filterNotNull(),
                    isLastPickConfirmed = LiteRTVisionClassifier.reportFlow.value.isConfirmed,
                    lastPickChampion = detected?.first, tenthPickIsAlly = finalIsAlly, tenthPickSlotIndex = 4,
                    allyRolesBySlot = mapOf(4 to LaneRole.JUNGLE), isSuccessful = true, statusMessage = "")
                hud.applyConfirmedLastPick(scan.copy(isLastPickConfirmed = false))
                assertEquals(original, (hud.allies + hud.enemies).map { it?.id })
                val locked = if (finalIsAlly) hud.manualLockedAllySlots else hud.manualLockedEnemySlots
                locked[1] = true
                hud.applyConfirmedLastPick(scan)
                assertEquals(original, (hud.allies + hud.enemies).map { it?.id })
                locked.clear()
                hud.applyConfirmedLastPick(scan)
                assertEquals("vi", (if (finalIsAlly) hud.allies else hud.enemies)[1]?.id)
                original.forEachIndexed { index, id -> if (id != null) assertEquals(id, (hud.allies + hud.enemies)[index]?.id) }
                assertEquals(10, (hud.allies + hud.enemies).filterNotNull().map { it.id }.distinct().size)
                assertEquals("vi", LiteRTVisionClassifier.executeTenthPickInference(null, finalIsAlly, selected, 9, context = context)?.first?.id)
                if (teamCrop !== crop) teamCrop.recycle()
            }
        } finally { crop.recycle() }
    }

    @Test fun priorPortraitEvidenceSurvivesNinthPickOcrArrivingAfterSlotDisappears() = runBlocking {
        val crop = fixture("volibear-slot")
        try {
            repeat(2) { assertNull(LiteRTVisionClassifier.executeTenthPickInference(crop, false, emptySet(), 8, context = context)) }
            assertEquals("volibear", LiteRTVisionClassifier.executeTenthPickInference(null, false, emptySet(), 9, context = context)?.first?.id)
        } finally { crop.recycle() }
    }

    @Test fun waitingIconAndAbsentSlotCannotConfirmChampion() = runBlocking {
        val crop = fixture("waiting-slot")
        try {
            repeat(4) { assertNull(LiteRTVisionClassifier.executeTenthPickInference(crop, false, emptySet(), 9, context = context)) }
            assertFalse(LiteRTVisionClassifier.reportFlow.value.isConfirmed)
            assertNull(LiteRTVisionClassifier.executeTenthPickInference(null, false, emptySet(), 9, context = context))
        } finally { crop.recycle() }
    }

    @Test fun ambiguousHighScoresNeverPassRegardlessOfRepeatedFrames() {
        assertFalse(PortraitMatcher.accepts(0.92f, 0.90f, 0.80f))
        assertFalse(PortraitMatcher.accepts(0.79f, 0.40f, 0.80f))
        assertTrue(PortraitMatcher.accepts(0.92f, 0.61f, 0.80f))
    }

    @Test fun resetRemovesPriorVisualConfirmation() = runBlocking {
        LiteRTVisionClassifier.manuallyConfirmTenthPick(Champion(id = "volibear"))
        LiteRTVisionClassifier.reset()
        assertNull(LiteRTVisionClassifier.executeTenthPickInference(null, false, emptySet(), 9, context = context))
        assertFalse(LiteRTVisionClassifier.reportFlow.value.isConfirmed)
    }
}
