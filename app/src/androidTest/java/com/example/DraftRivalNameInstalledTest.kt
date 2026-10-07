package com.example

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.WildRiftRepository
import com.example.service.screen.ChampionNameResolver
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/** Real pixels from the reported name bands, without account/player names. */
@RunWith(AndroidJUnit4::class)
class DraftRivalNameInstalledTest {
    @Test fun currentMilioNameIsReadFromTheReportedCapture() = inspect("milio")
    @Test fun currentCaitlynNameIsReadFromTheReportedCapture() = inspect("caitlyn")

    private fun inspect(expected: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        WildRiftRepository.initChampions(instrumentation.targetContext, forceReload = true)
        val crop = instrumentation.context.assets.open("draft/$expected-name-band.png")
            .use { BitmapFactory.decodeStream(it)!! }
        val scaled = Bitmap.createScaledBitmap(crop, crop.width * 3, crop.height * 3, true)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        try {
            val result = Tasks.await(recognizer.process(InputImage.fromBitmap(scaled, 0)), 45, TimeUnit.SECONDS)
            val detected = result.textBlocks.flatMap { it.lines }.mapNotNull {
                ChampionNameResolver.findChampionInText(it.text, WildRiftRepository.champions)?.id
            }.distinct()
            assertEquals("Current name band must resolve exactly one champion: ${result.text}",
                listOf(expected), detected)
        } finally {
            recognizer.close()
            if (scaled !== crop) scaled.recycle()
            crop.recycle()
        }
    }
}
