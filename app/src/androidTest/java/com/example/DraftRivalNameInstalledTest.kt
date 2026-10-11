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

    @Test fun SpanishLaneTitlesPersistThroughSelectionAndChampionTrades() = inspectAlliedTransition("es")
    @Test fun PortugueseLaneTitlesPersistThroughSelectionAndChampionTrades() = inspectAlliedTransition("pt")

    @Test fun reportedAlliedNameBandsReadTheExactFiveChampions() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val catalog = listOf("yuumi", "jinx", "brand", "skarner", "pantheon").map { com.example.model.Champion(id = it, name = it) }
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        try {
            for (champion in catalog) {
                val json = instrumentation.context.assets.open("draft/ally-${champion.id}-title.json").bufferedReader().use { it.readText() }
                val bytes = android.util.Base64.decode(org.json.JSONObject(json).getString("pngBase64"), android.util.Base64.DEFAULT)
                val crop = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)!!
                val scaled = Bitmap.createScaledBitmap(crop, crop.width * 3, crop.height * 3, true)
                try {
                    val reading = com.google.android.gms.tasks.Tasks.await(recognizer.process(InputImage.fromBitmap(scaled, 0)), 30, TimeUnit.SECONDS)
                    val title = com.example.service.screen.AllyDraftNameReader.readTitleRows(reading.textBlocks.flatMap { it.lines }.map {
                        it.text to (it.boundingBox?.centerY() ?: scaled.height / 2)
                    }, (scaled.height * 0.30f).toInt(), catalog)
                    assertEquals("Reported title: ${reading.text}", champion.id, title.champion?.id)
                } finally { scaled.recycle(); crop.recycle() }
            }
        } finally { recognizer.close() }
    }

    @Test fun reportedSlotsDoNotLoseSennaOrMalphiteAndFinishWithVisualRivalPick() = kotlinx.coroutines.runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val scanner = com.example.service.screen.DraftVisionScanner
        val original = scanner.calibrationConfig
        val fixture = InstrumentationRegistry.getInstrumentation().context.assets.open("draft/reported-ten-slots.json")
            .bufferedReader().use { org.json.JSONObject(it.readText()).getString("pngBase64") }
        val bytes = android.util.Base64.decode(fixture, android.util.Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)!!.copy(android.graphics.Bitmap.Config.ARGB_8888, true)!!
        try {
            scanner.resetSlotMemory()
            scanner.updateCalibration(context, com.example.service.screen.VisionCalibrationConfig())
            val first = scanner.scanDraftFromBitmap(bitmap, context, null, com.example.model.LaneRole.MID)
            assertTrue("A single OCR reading cannot select a player", first.alliesBySlot.isEmpty())
            val selected = scanner.scanDraftFromBitmap(bitmap, context, null, com.example.model.LaneRole.MID)
            assertEquals("senna", selected.alliesBySlot[0]?.id)
            assertEquals("fizz", selected.alliesBySlot[1]?.id)
            assertEquals("nami", selected.alliesBySlot[2]?.id)
            assertEquals("malphite", selected.alliesBySlot[3]?.id)
            assertNull("Lane title must not select the Akshan preview", selected.alliesBySlot[4])
            assertEquals("malphite", selected.alliesByRole[com.example.model.LaneRole.TOP]?.id)
            assertEquals("senna", selected.alliesByRole[com.example.model.LaneRole.ADC]?.id)
            // Final turn: ninth player is now selected; final rival has portrait but no title.
            val config = scanner.calibrationConfig
            val canvas = android.graphics.Canvas(bitmap)
            val black = android.graphics.Paint().apply { color = android.graphics.Color.BLACK }
            val allyTitle = com.example.service.screen.AdaptiveScreenLayoutEngine.calculateSlotNameRect(bitmap.width, bitmap.height, true, 4, config)
            canvas.drawRect(allyTitle, black)
            canvas.drawRect(com.example.service.screen.AdaptiveScreenLayoutEngine.calculateSlotNameRect(bitmap.width, bitmap.height, false, 4, config), black)
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE; textSize = 14f; typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            canvas.drawText("AKSHAN", allyTitle.left + 8f, bitmap.height * config.allySlotYRatios[4] - 7f, paint)
            scanner.resetSlotMemory()
            var finalResult = selected
            repeat(5) {
                finalResult = scanner.scanDraftFromBitmap(bitmap, context, null, com.example.model.LaneRole.MID)
                kotlinx.coroutines.delay(200)
            }
            assertEquals(true, finalResult.detectedFirstPick)
            assertEquals(false, finalResult.tenthPickIsAlly)
            assertEquals("fiddlesticks", finalResult.lastPickChampion?.id)
            assertTrue(finalResult.isLastPickConfirmed)
            assertEquals(10, finalResult.alliesBySlot.size + finalResult.enemiesBySlot.size)
        } finally { bitmap.recycle(); scanner.resetSlotMemory(); scanner.updateCalibration(context, original) }
    }

    @Test fun firstSelectionsAreReadWithRedMagicAndPocoCoordinates() = kotlinx.coroutines.runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val scanner = com.example.service.screen.DraftVisionScanner
        val original = scanner.calibrationConfig
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val fixture = InstrumentationRegistry.getInstrumentation().context.assets.open("draft/seraphine-first-title.json")
            .bufferedReader().use { org.json.JSONObject(it.readText()).getString("pngBase64") }
        val bytes = android.util.Base64.decode(fixture, android.util.Base64.DEFAULT)
        val titleCrop = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)!!
        val titleScaled = Bitmap.createScaledBitmap(titleCrop, titleCrop.width * 3, titleCrop.height * 3, true)
        try {
            val reading = com.google.android.gms.tasks.Tasks.await(recognizer.process(InputImage.fromBitmap(titleScaled, 0)), 30, TimeUnit.SECONDS)
            assertTrue("Reported first selection: ${reading.text}", reading.text.contains("SERAPHINE", ignoreCase = true))
        } finally { titleScaled.recycle(); titleCrop.recycle(); recognizer.close() }
        val redMagic = com.example.service.screen.VisionCalibrationConfig()
        val poco = redMagic.copy(allyAvatarCenterX = .12f, enemyAvatarCenterX = .911f,
            allySlotXRatios = List(5) { .12f }, enemySlotXRatios = List(5) { .911f },
            allySlotYRatios = listOf(.184f, .320f, .454f, .592f, .728f),
            enemySlotYRatios = listOf(.179f, .315f, .449f, .587f, .723f))
        try {
            for (config in listOf(redMagic, poco)) {
                scanner.resetSlotMemory()
                scanner.updateCalibration(context, config)
                val bitmap = Bitmap.createBitmap(1920, 868, Bitmap.Config.ARGB_8888)
                try {
                    bitmap.eraseColor(android.graphics.Color.BLACK)
                    val canvas = android.graphics.Canvas(bitmap)
                    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.WHITE; textSize = 20f; typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    // Game title positions are independent of the scanner's rectangle calculation.
                    canvas.drawText("SERAPHINE", 1920 * config.getAllySlotX(0) + 868 * .074f,
                        868 * config.allySlotYRatios[0] - 18f, paint)
                    canvas.drawText("MEL", 1920 * config.getEnemySlotX(0) - 868 * .074f - paint.measureText("MEL"),
                        868 * config.enemySlotYRatios[0] - 18f, paint)
                    val result = kotlinx.coroutines.withTimeout(60_000L) {
                        scanner.scanDraftFromBitmap(bitmap, context, true, com.example.model.LaneRole.TOP)
                        scanner.scanDraftFromBitmap(bitmap, context, true, com.example.model.LaneRole.TOP)
                    }
                    assertEquals("seraphine", result.alliesBySlot[0]?.id)
                    assertEquals("mel", result.enemiesBySlot[0]?.id)
                } finally { bitmap.recycle() }
            }
        } finally { scanner.resetSlotMemory(); scanner.updateCalibration(context, original) }
    }

    private fun inspectAlliedTransition(language: String) = kotlinx.coroutines.runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val scanner = com.example.service.screen.DraftVisionScanner
        val roles = listOf(com.example.model.LaneRole.JUNGLE, com.example.model.LaneRole.ADC,
            com.example.model.LaneRole.MID, com.example.model.LaneRole.TOP, com.example.model.LaneRole.SUPPORT)
        val labels = if (language == "es") listOf("JUNGLA", "CALLE DEL DRAGÓN", "CALLE CENTRAL", "CALLE DEL BARÓN", "APOYO")
            else listOf("SELVA", "ROTA DO DRAGÃO", "ROTA DO MEIO", "ROTA DO BARÃO", "SUPORTE")
        val picks = listOf("yuumi", "senna", "brand", "skarner", "pantheon")
        scanner.resetSlotMemory()
        fun frame(titles: List<String>): Bitmap = Bitmap.createBitmap(1920, 868, Bitmap.Config.ARGB_8888).apply {
            eraseColor(android.graphics.Color.BLACK)
            val canvas = android.graphics.Canvas(this)
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE; textSize = 20f; typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            val config = scanner.calibrationConfig
            for (i in 0..4) {
                val cy = height * config.allySlotYRatios[i]
                val x = width * (config.allyAvatarCenterX + 0.04f)
                canvas.drawText(titles[i], x, cy - 18f, paint)
                // Deliberately put champion names on other players' second line.
                canvas.drawText(if (i == 1) "(TÚ)" else "JINX", x, cy + 3f, paint)
            }
        }
        suspend fun scan(phase: String, titles: List<String>): com.example.service.screen.DraftScanResult {
            fun progress(message: String) = InstrumentationRegistry.getInstrumentation().sendStatus(2,
                android.os.Bundle().apply { putString("stream", "\nDRAFT_NATIVE_FRAME: $language $phase $message\n") })
            progress("start")
            val bitmap = frame(titles)
            return try {
                kotlinx.coroutines.withTimeout(60_000L) {
                    scanner.scanDraftFromBitmap(bitmap, context, true, com.example.model.LaneRole.TOP)
                        scanner.scanDraftFromBitmap(bitmap, context, true, com.example.model.LaneRole.TOP)
                }.also { progress("done ${it.allyRolesBySlot} ${it.alliesBySlot.mapValues { entry -> entry.value.id }}") }
            } finally { bitmap.recycle() }
        }
        try {
            val initial = scan("lanes", labels)
            assertEquals(roles.mapIndexed { index, role -> index to role }.toMap(), initial.allyRolesBySlot)
            assertTrue(initial.allies.isEmpty())
            assertEquals(com.example.model.LaneRole.ADC, initial.userExplicitlyDetectedRole)
            val selected = scan("selection", picks.map { it.uppercase() })
            picks.forEachIndexed { index, id -> assertEquals(id, selected.alliesByRole[roles[index]]?.id) }
            assertEquals(com.example.model.LaneRole.ADC, selected.userExplicitlyDetectedRole)
            val gap = scan("gap", List(5) { "" })
            assertEquals(selected.alliesByRole, gap.alliesByRole)
            val traded = scan("trade", listOf("SKARNER", "SENNA", "BRAND", "YUUMI", "PANTHEON"))
            assertEquals("skarner", traded.alliesByRole[com.example.model.LaneRole.JUNGLE]?.id)
            assertEquals("yuumi", traded.alliesByRole[com.example.model.LaneRole.TOP]?.id)
            assertEquals(com.example.model.LaneRole.ADC, traded.userExplicitlyDetectedRole)
        } finally { scanner.resetSlotMemory() }
    }

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
