package com.example

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.BitmapFactory
import android.util.Base64
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class CalibrationEvidenceImageTest {
    @Test fun encodesBoundedImageWithCalibrationOverlayAndHash() {
        val source = Bitmap.createBitmap(640, 360, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.rgb(8, 20, 32))
        }
        try {
            val encoded = CalibrationEvidenceImage.encode(
                CalibrationEvidenceFrame(source, android.os.SystemClock.elapsedRealtime()),
                VisionCalibrationConfig(),
                "es"
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

    @Test fun evidenceRepeatsConfirmedPortraitsAndUsesSavedBanRegions() {
        val source = Bitmap.createBitmap(640, 360, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.rgb(8, 20, 32))
        }
        val portrait = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.rgb(240, 40, 60))
        }
        try {
            val config = VisionCalibrationConfig(
                allyBanXRatios = listOf(.02f, .06f, .10f, .14f, .18f),
                enemyBanXRatios = listOf(.82f, .86f, .90f, .94f, .98f)
            )
            val incompleteObservation = BanSlot(
                BanSide.ALLY, 0, BanRegion(.8f, .8f, .02f),
                championId = "jax", state = BanState.CONFIRMED
            )
            val fallbackRegions = CalibrationEvidenceImage.evidenceBanRegions(
                config, listOf(incompleteObservation), isAlly = true
            )
            assertEquals("La observación parcial conserva la geometría guardada", .02f, fallbackRegions[0].x, .0001f)

            val completeObservation = (0..4).map { index ->
                BanSlot(
                    BanSide.ALLY, index, BanRegion(.30f + index * .02f, .08f, .03f),
                    championId = "champion_$index", state = BanState.CONFIRMED
                )
            }
            val observedRegions = CalibrationEvidenceImage.evidenceBanRegions(
                config, completeObservation, isAlly = true
            )
            assertEquals("Una fila completa conserva las regiones observadas", .30f, observedRegions[0].x, .0001f)
            assertEquals(.38f, observedRegions[4].x, .0001f)

            val encoded = requireNotNull(CalibrationEvidenceImage.encode(
                CalibrationEvidenceFrame(source, android.os.SystemClock.elapsedRealtime()),
                config,
                slotLabels = mapOf("ally_0" to "Jax"),
                banSlots = listOf(incompleteObservation),
                avatarBitmaps = mapOf("ally_0" to portrait, "ally_ban_0" to portrait)
            ))
            val bytes = Base64.decode(encoded.base64, Base64.NO_WRAP)
            val decoded = requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
            assertTrue("La evidencia codificada conserva una imagen válida", decoded.width > 0 && decoded.height > 0)
            decoded.recycle()
        } finally {
            source.recycle()
            portrait.recycle()
        }
    }
}
