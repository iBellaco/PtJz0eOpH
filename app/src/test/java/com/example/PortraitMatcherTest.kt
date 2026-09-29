package com.example

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.data.WildRiftRepository
import com.example.model.Champion
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
