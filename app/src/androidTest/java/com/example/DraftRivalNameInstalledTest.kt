package com.example

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.service.applyConfirmedLastPick
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.TimeUnit
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference
import java.util.Locale
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/** Real pixels from the reported name bands, without account/player names. */
@RunWith(AndroidJUnit4::class)
class DraftRivalNameInstalledTest {
    @Test fun currentMilioNameIsReadFromTheReportedCapture() = inspect("milio")
    @Test fun currentCaitlynNameIsReadFromTheReportedCapture() = inspect("caitlyn")

    @Test fun reportedDraftPortraitSelectsViOnInstalledRelease() = inspectVi("vi-reported-draft")
    @Test fun reportedViewerPortraitSelectsViOnInstalledRelease() = inspectVi("vi-reported-viewer")

    private fun inspectVi(fixture: String) = kotlinx.coroutines.runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val json = instrumentation.context.assets.open("draft/$fixture.json").bufferedReader().use { it.readText() }
        val bytes = android.util.Base64.decode(org.json.JSONObject(json).getString("pngBase64"), android.util.Base64.DEFAULT)
        val crop = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)!!
        val classifier = com.example.service.screen.LiteRTVisionClassifier
        val originalThreshold = classifier.getEffectiveThreshold(context)
        try {
            classifier.reset()
            classifier.setThreshold(0.80f, context)
            classifier.ensureIndexed(context)
            val hud = com.example.service.OverlayState().apply {
                listOf("skarner", "pantheon", "brand", "jinx", "yuumi").forEachIndexed { index, id ->
                    allies[index] = com.example.model.Champion(id = id)
                }
                listOf("darius", null, "mel", "caitlyn", "milio").forEachIndexed { index, id ->
                    enemies[index] = id?.let { com.example.model.Champion(id = it) }
                }
            }
            val original = (hud.allies + hud.enemies).map { it?.id }
            val excluded = original.filterNotNull().toSet()
            assertNull(classifier.executeTenthPickInference(crop, false, excluded, 9, context = context))
            val detected = classifier.executeTenthPickInference(crop, false, excluded, 9, context = context)
            assertEquals("vi", detected?.first?.id)
            assertTrue(classifier.reportFlow.value.isConfirmed)
            assertTrue(classifier.reportFlow.value.confidencePercent >= 80)
            hud.applyConfirmedLastPick(com.example.service.screen.DraftScanResult(
                hud.allies.filterNotNull(), hud.enemies.filterNotNull(),
                isLastPickConfirmed = true, lastPickChampion = detected?.first,
                tenthPickIsAlly = false, tenthPickSlotIndex = 4, isSuccessful = true, statusMessage = ""))
            assertEquals("vi", hud.enemies[1]?.id)
            original.forEachIndexed { index, id ->
                if (id != null) assertEquals(id, (hud.allies + hud.enemies)[index]?.id)
            }
        } finally {
            classifier.reset()
            classifier.setThreshold(originalThreshold, context)
            crop.recycle()
        }
    }

    private fun inspect(expected: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val crop = instrumentation.context.assets.open("draft/$expected-name-band.png")
            .use { BitmapFactory.decodeStream(it)!! }
        val scaled = Bitmap.createScaledBitmap(crop, crop.width * 3, crop.height * 3, true)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        try {
            val completed = CountDownLatch(1)
            val reading = AtomicReference<com.google.mlkit.vision.text.Text?>()
            val failure = AtomicReference<Exception?>()
            recognizer.process(InputImage.fromBitmap(scaled, 0))
                .addOnSuccessListener { reading.set(it); completed.countDown() }
                .addOnFailureListener { failure.set(it); completed.countDown() }
            assertTrue("Native OCR timed out", completed.await(45, TimeUnit.SECONDS))
            failure.get()?.let { throw it }
            val result = reading.get() ?: error("Native OCR returned no reading")
            // Exercise the bundled native reader directly. R8 can inline internal
            // app methods across the APK boundary; resolver identity is covered
            // separately by the real-catalog unit regressions.
            val detected = result.textBlocks.flatMap { it.lines }.map {
                it.text.replace(Regex("\\s+"), "").lowercase(Locale.ROOT)
            }
            assertTrue("Current name band must read $expected exactly: ${result.text}",
                expected in detected)
        } finally {
            recognizer.close()
            if (scaled !== crop) scaled.recycle()
            crop.recycle()
        }
    }
}
