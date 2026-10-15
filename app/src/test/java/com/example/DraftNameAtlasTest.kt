package com.example

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.service.screen.AdaptiveScreenLayoutEngine
import com.example.service.screen.DraftNameAtlas
import com.example.service.screen.VisionCalibrationConfig
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DraftNameAtlasTest {
    @Test fun goldTitleSurvivesBadgeSuppressionOnBothMeasuredPhoneProfiles() {
        for (config in listOf(VisionCalibrationConfig(),
            VisionCalibrationConfig.defaultsForDevice("Xiaomi", "2412DPC0AG"))) {
            val source = Bitmap.createBitmap(1920, 868, Bitmap.Config.ARGB_8888)
            try {
                source.eraseColor(Color.BLACK)
                val band = AdaptiveScreenLayoutEngine.calculateSlotNameRect(1920, 868, true, 0, config)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(220, 180, 80); textSize = 20f }
                Canvas(source).drawText("SERAPHINE", band.left + 3f, band.bottom - 8f, paint)
                val atlas = DraftNameAtlas.create(source, config, emptyList())
                try {
                    val raw = atlas.tiles.single { it.ally && it.slot == 0 && it.original }
                    val processed = atlas.tiles.single { it.ally && it.slot == 0 && !it.original }
                    fun goldPixels(tile: DraftNameAtlas.Tile): Int {
                        var count = 0
                        val end = tile.region.left + (band.height() * .65f).toInt() * 2 - 2
                        for (y in tile.region.top until tile.region.bottom) for (x in tile.region.left until end) {
                            val pixel = atlas.bitmap.getPixel(x, y)
                            if (Color.red(pixel) > 95 && Color.green(pixel) > 65) count++
                        }
                        return count
                    }
                    assertTrue("The untouched title must retain its leading letters", goldPixels(raw) > 20)
                    assertEquals("Badge removal would have erased those letters", 0, goldPixels(processed))
                } finally { atlas.bitmap.recycle() }
            } finally { source.recycle() }
        }
    }
}
