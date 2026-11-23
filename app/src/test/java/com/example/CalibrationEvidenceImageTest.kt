package com.example

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.model.Champion
import com.example.service.screen.DraftVisionScanner
import com.example.service.screen.CalibrationEvidenceFrame
import com.example.service.screen.CalibrationEvidenceImage
import com.example.service.screen.BanRegion
import com.example.service.screen.BanSide
import com.example.service.screen.BanSlot
import com.example.service.screen.BanState
import com.example.service.screen.VisionCalibrationConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.abs
import kotlin.math.roundToInt

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CalibrationEvidenceImageTest {
    @Test fun encodesBoundedImageWithCalibrationOverlayAndHash() {
        val source = Bitmap.createBitmap(640, 360, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.rgb(8, 20, 32))
        }
        try {
            val encoded = CalibrationEvidenceImage.encode(
                CalibrationEvidenceFrame(source, android.os.SystemClock.elapsedRealtime()),
                VisionCalibrationConfig()
            )
            assertNotNull(encoded)
            val result = encoded!!
            assertTrue(result.base64.isNotBlank())
            assertTrue(result.sha256.matches(Regex("[0-9a-f]{64}")))
            assertTrue(result.width <= 1280)
            assertTrue(result.height > 0)
            val decoded = BitmapFactory.decodeByteArray(
                Base64.decode(result.base64, Base64.NO_WRAP), 0,
                Base64.decode(result.base64, Base64.NO_WRAP).size
            )
            assertNotNull(decoded)
            decoded.recycle()
        } finally {
            source.recycle()
        }
    }

    @Test fun observedAndManualBanCoordinatesAreSharedByBothVisorsAndTheReport() {
        val config = VisionCalibrationConfig()
        val row = List(5) { index ->
            BanSlot(BanSide.ALLY, index, BanRegion(.065f + index * .036f, .06f, .03f))
        }
        val manual = config.adjustBanSlot(true, 1, deltaX = .01f)
        assertEquals(manual, manual.withTrackedBanRegions(row))
        assertEquals(config, config.withTrackedBanRegions(row.drop(2)))
        assertEquals(row.map { it.region }, config.withTrackedBanRegions(row.reversed()).banRegions(true))
        assertEquals(config.banRegions(false), config.withTrackedBanRegions(row).banRegions(false))
    }

    @Test fun attachmentKeepsGamePixelsInsidePicksAndBansAndDrawsOneHeaderRow() {
        val source = Bitmap.createBitmap(1280, 576, Bitmap.Config.ARGB_8888)
        val background = Color.rgb(22, 32, 46)
        val gameColor = Color.rgb(56, 171, 112)
        val config = VisionCalibrationConfig()
        val row = List(5) { index ->
            BanSlot(BanSide.ALLY, index, BanRegion(.065f + index * .036f, .06f, .03f),
                championId = "ban$index", state = BanState.CONFIRMED)
        }
        val geometry = config.withTrackedBanRegions(row)
        val allyX = (.102f * source.width).roundToInt()
        val enemyX = (.927f * source.width).roundToInt()
        val pickY = (.188f * source.height).roundToInt()
        val banX = (.065f * source.width).roundToInt()
        val banY = (.06f * source.height).roundToInt()
        try {
            source.eraseColor(background)
            val paint = Paint().apply { color = gameColor }
            val canvas = Canvas(source)
            val originalCenters = listOf(allyX to pickY, enemyX to pickY, banX to banY)
            for ((x, y) in originalCenters) canvas.drawCircle(x.toFloat(), y.toFloat(), 11f, paint)
            val frame = CalibrationEvidenceFrame(source, android.os.SystemClock.elapsedRealtime())
            val result = requireNotNull(CalibrationEvidenceImage.encode(frame, geometry,
                slotLabels = mapOf("ally_0" to "Zoe", "enemy_0" to "Aurora")))
            val bytes = Base64.decode(result.base64, Base64.NO_WRAP)
            val decoded = requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
            try {
                fun assertPixel(x: Int, y: Int, expected: Int) {
                    val actual = decoded.getPixel(x, y)
                    assertTrue("pixel ($x,$y) = ${Integer.toHexString(actual)}",
                        abs(Color.red(actual) - Color.red(expected)) < 18 &&
                        abs(Color.green(actual) - Color.green(expected)) < 18 &&
                        abs(Color.blue(actual) - Color.blue(expected)) < 18)
                }
                for ((x, y) in originalCenters) assertPixel(x, y, gameColor)
                // The reported Samsung defaults are offset from the synthetic originals.
                assertPixel((.072f * source.width).roundToInt(), pickY, background)
                assertPixel((.959f * source.width).roundToInt(), pickY, background)
                val oldX = (.028f * source.width).roundToInt()
                assertPixel(oldX, 5, background) // former white top grid
                assertPixel(oldX, 13, background) // unmeasured default ban ring
                val ring = decoded.getPixel(banX, ((.06f - .03f) * source.height).roundToInt())
                assertTrue("Measured ban rim", Color.red(ring) > 145 && Color.green(ring) > 95)
                assertEquals(background, source.getPixel(banX, 17))
            } finally { decoded.recycle() }
        } finally { source.recycle() }
    }

    @Test fun reportsTenIdentitiesWithTheLiveVisorHidden() {
        val allies = listOf("Zoe", "Rell", "Yasuo", "Vayne", "Lee Sin")
        val rivals = listOf("Aurora", "Caitlyn", "Thresh", "Vi", "Sett")
        DraftVisionScanner.resetSlotMemory()
        try {
            for (i in 0..4) {
                DraftVisionScanner.allySlotConfirmedChampions[i] = Champion(name = allies[i])
                DraftVisionScanner.enemySlotConfirmedChampions[i] = Champion(name = rivals[i])
            }
            assertTrue(DraftVisionScanner.debugVisualMatches.value.isEmpty())
            val snapshot = DraftVisionScanner.calibrationReportSnapshot()
            assertEquals(10, snapshot.picks.size)
            assertEquals("Lee Sin", snapshot.slotLabels("es")["ally_4"])
            assertEquals("Sett", snapshot.slotLabels("pt")["enemy_4"])
            assertTrue(snapshot.detectionEvidenceReport("es").contains("confirmado=Zoe"))
        } finally { DraftVisionScanner.resetSlotMemory() }
    }

    @Test fun evidenceReportKeepsBothSidesWhenTheHubHidesTheRivalText() {
        DraftVisionScanner.resetSlotMemory()
        try {
            DraftVisionScanner.allySlotConfirmedChampions[0] = Champion(name = "Volibear")
            val report = DraftVisionScanner.calibrationReportSnapshot().detectionEvidenceReport("es")
            assertTrue(report.contains("Aliado slot 1: fuente=N/D") || report.contains("Aliado slot 1: confirmado=Volibear"))
            assertTrue(report.contains("Aliado slot 2: fuente=N/D; confirmado=N/D"))
            assertTrue(report.contains("Rival slot 1: fuente=N/D; confirmado=N/D"))
            assertTrue(report.contains("Rival slot 5: fuente=N/D; confirmado=N/D"))
            assertTrue(report.contains("Baneo Rival slot 1:"))
            assertTrue(report.contains("Baneo Rival slot 5:"))
        } finally { DraftVisionScanner.resetSlotMemory() }
    }

    @Test fun cleanAndAuditedEvidenceBothEncodeSuccessfully() {
        val source = Bitmap.createBitmap(640, 360, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.rgb(8, 20, 32))
        }
        try {
            val clean = requireNotNull(CalibrationEvidenceImage.encode(
                CalibrationEvidenceFrame(source, android.os.SystemClock.elapsedRealtime()),
                VisionCalibrationConfig(),
                visorActive = false
            ))
            val audited = requireNotNull(CalibrationEvidenceImage.encode(
                CalibrationEvidenceFrame(source, android.os.SystemClock.elapsedRealtime()),
                VisionCalibrationConfig(),
                visorActive = true
            ))
            assertTrue(clean.sha256.matches(Regex("[0-9a-f]{64}")))
            assertTrue(audited.sha256.matches(Regex("[0-9a-f]{64}")))
            assertEquals(clean.width, audited.width)
            assertEquals(clean.height, audited.height)
        } finally {
            source.recycle()
        }
    }
}
