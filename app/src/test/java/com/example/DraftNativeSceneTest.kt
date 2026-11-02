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
@Config(sdk = [34], application = Application::class, manifest = Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DraftNativeSceneTest {
    @Test fun nativeScenePreservesSmallCaptureDonorCoordinatesAndMasks() {
        val source = Bitmap.createBitmap(854,480,Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) }
        val config = VisionCalibrationConfig(nameBandScale=.8041667f,
            allySlotXRatios=List(5){.071428575f},
            allySlotYRatios=listOf(.2504075f,.35833332f,.46679574f,.5729167f,.68077964f))
        val mask = android.graphics.Rect(400,100,450,150)
        val atlas = DraftNameAtlas.create(source,config,listOf(mask))
        val scene = DraftNameAtlas.sceneTitles(source,config,listOf(mask),nativeSize=true)
        try {
            assertEquals(854,scene.bitmap.width)
            assertEquals(480,scene.bitmap.height)
            assertEquals(Color.BLACK,scene.bitmap.getPixel(425,125))
            assertEquals(Color.WHITE,source.getPixel(425,125))
            val target = atlas.tiles.single { it.original && it.ally && it.slot==4 }
            // Native ML Kit box from the failed real capture, not an expected
            // champion injection. Nickname/central text must not be donors.
            val nativeBox = android.graphics.Rect(91,160,123,168)
            val sources = DraftNameAtlas.sceneContextSources(atlas,scene,listOf(nativeBox,
                android.graphics.Rect(90,174,146,183),android.graphics.Rect(418,340,437,348)),target)
            val (donor,bounds) = sources.single()
            assertTrue(donor.ally)
            assertEquals(1,donor.slot)
            assertEquals(android.graphics.Rect(
                (91-donor.source.left)*donor.region.width()/donor.source.width(),
                (160-donor.source.top)*donor.region.height()/donor.source.height(),
                (123-donor.source.left)*donor.region.width()/donor.source.width(),
                (168-donor.source.top)*donor.region.height()/donor.source.height()),bounds)
        } finally { scene.bitmap.recycle(); atlas.bitmap.recycle(); source.recycle() }
    }

}
