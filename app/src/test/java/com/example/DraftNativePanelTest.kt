package com.example

import android.app.Application
import android.graphics.BitmapFactory
import android.graphics.Rect
import com.example.service.screen.AdaptiveScreenLayoutEngine
import com.example.service.screen.DraftFrameGeometry
import com.example.service.screen.DraftGeometryStability
import com.example.service.screen.DraftRingAlignment
import com.example.service.screen.VisionCalibrationConfig
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.security.MessageDigest
import kotlin.math.hypot

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DraftNativePanelTest {
    private fun resource(path: String): ByteArray =
        requireNotNull(javaClass.getResourceAsStream("/$path")) { path }.use { it.readBytes() }

    @Test fun visibleSlotsInOriginalCaptures() {
        val fixtures = JSONArray(resource("scanner/native-panels.json").decodeToString())
        val deviations = mutableListOf<String>()
        val report = File("build/reports/scanner-native/paneles.csv")
        report.parentFile.mkdirs()
        report.bufferedWriter().use { out ->
            out.appendLine("archivo,ancho,alto,panel,puesto,x_referencia,y_referencia,x_escaner,y_escaner,error_px,resultado")
            for (index in 0 until fixtures.length()) {
                val fixture = fixtures.getJSONObject(index)
                val path = fixture.getString("file")
                val bytes = resource(path)
                val digest = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
                assertEquals(path, fixture.getString("sha256"), digest)
                val bitmap = requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size,
                    BitmapFactory.Options().apply { inScaled = false }))
                try {
                    val width = bitmap.width
                    val height = bitmap.height
                    val size = fixture.getJSONArray("size")
                    assertEquals(path, size.getInt(0), width)
                    assertEquals(path, size.getInt(1), height)
                    val viewport = DraftFrameGeometry.contentBounds(bitmap)
                    val base = DraftFrameGeometry.calculateMultideviceBoxes(VisionCalibrationConfig(), viewport, width, height)
                    val mask = fixture.optJSONArray("mask")?.let { Rect(it.getInt(0), it.getInt(1), it.getInt(2), it.getInt(3)) }
                    var enemyMeasured = false
                    val aligned = DraftRingAlignment.align(bitmap, base, mask, fixture.getBoolean("debugOverlay"),
                        onEnemyMeasured = { enemyMeasured = true }, bounds = viewport)
                    val config = DraftGeometryStability().update(aligned, enemyMeasured, base, height)
                    for (side in listOf("ally", "enemy")) {
                        val centers = fixture.getJSONArray(side)
                        assertEquals("$path $side", 5, centers.length())
                        var previousBottom = 0
                        for (slot in 0 until centers.length()) {
                            val prefix = "$path,$width,$height,$side,${slot + 1}"
                            val rect = AdaptiveScreenLayoutEngine.calculateSlotCropRect(width, height, side == "ally", slot, config)
                            assertTrue("$path $side row $slot overlaps its neighbor", previousBottom <= rect.top)
                            previousBottom = rect.bottom
                            val x = rect.exactCenterX()
                            val y = rect.exactCenterY()
                            if (centers.isNull(slot)) {
                                out.appendLine("$prefix,,,$x,$y,,oculto")
                                continue
                            }
                            val center = centers.getJSONArray(slot)
                            val dx = center.getInt(0)
                            val dy = center.getInt(1)
                            val error = hypot(x - dx, y - dy)
                            val status = if (error <= 6f) "OK" else "desviado"
                            out.appendLine("$prefix,$dx,$dy,$x,$y,$error,$status")
                            if (error > 6f) deviations.add("$path $side ${slot + 1} ($error px)")
                        }
                    }
                } finally {
                    bitmap.recycle()
                }
            }
        }
        assertTrue(deviations.joinToString("\n"), deviations.isEmpty())
    }
}
