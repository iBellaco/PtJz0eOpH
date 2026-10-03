package com.example

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.TimeUnit

/** Actual installed APK: verify Android extraction and ML Kit's native inference. */
@RunWith(AndroidJUnit4::class)
class NativeOcrPackagingTest {
    @Test fun packagedNativeOcrStillRecognizesChampionNames() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        assertTrue(File(context.applicationInfo.nativeLibraryDir, "libmlkit_google_ocr_pipeline.so").isFile)
        val bitmap = Bitmap.createBitmap(900, 240, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawText("AHRI YASUO", 40f, 150f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK; textSize = 84f; typeface = android.graphics.Typeface.DEFAULT_BOLD
        })
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        try {
            val recognized = Tasks.await(recognizer.process(InputImage.fromBitmap(bitmap, 0)), 45, TimeUnit.SECONDS).text.uppercase()
            assertTrue("Native OCR recognized: $recognized", recognized.contains("AHRI") && recognized.contains("YASUO"))
        } finally { recognizer.close(); bitmap.recycle() }
    }
}
