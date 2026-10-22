package com.example

import android.app.Application
import android.graphics.BitmapFactory
import android.graphics.Rect
import com.example.service.screen.DraftRingAlignment
import com.example.service.screen.VisionCalibrationConfig
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34], application=Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DraftRingAlignmentTest {
    @Test fun twoIncoherentRingsCannotRescaleOrTranslateTheWholeColumn() {
        val ys = VisionCalibrationConfig().enemySlotYRatios
        assertEquals(ys, DraftRingAlignment.alignRows(ys,579,32,listOf(0 to 87,3 to 343)))
        assertEquals(ys, DraftRingAlignment.alignRows(ys,579,32,listOf(0 to 109,3 to 366)))
    }
    @Test fun twoCoherentRingsCanTranslateButNotStretchTheGrid() {
        val ys = VisionCalibrationConfig().enemySlotYRatios
        val result = DraftRingAlignment.alignRows(ys,579,32,listOf(0 to 119,3 to 352))
        ys.indices.forEach { assertEquals(ys[it]+10f/579,result[it],.002f) }
    }
    @Test fun anIndependentlyDetectedForeignColumnCanStillRecoverItsInitialSpacing() {
        val ys = VisionCalibrationConfig.defaultsForDevice("Xiaomi","2412DPC0AG").enemySlotYRatios
        val observations = listOf(1 to 190, 4 to 419)
        val result = DraftRingAlignment.alignRows(ys,579,32,observations,recoverForeignColumn=true)
        observations.forEach { (slot,y) -> assertEquals(y.toFloat()/579,result[slot],.002f) }
        assertNotEquals(ys,result)
    }
    @Test fun s26VisibleRivalRingsReplaceAnotherPhonesCoordinates() {
        val bitmap=javaClass.getResourceAsStream("/s26/draft.jpg").use { BitmapFactory.decodeStream(it) }
        try {
            val aligned=DraftRingAlignment.align(bitmap,VisionCalibrationConfig(),Rect(60,0,590,591),ignoreDebugRings=true)
            assertEquals(1168f/1280f,aligned.enemyAvatarCenterX,.012f)
            assertTrue(aligned.enemySlotXRatios.all { kotlin.math.abs(it-1168f/1280f)<.012f })
            // Occluded allies cannot be located from the HUD's cyan borders.
            assertEquals(VisionCalibrationConfig().allySlotXRatios,aligned.allySlotXRatios)
        } finally { bitmap.recycle() }
    }
    @Test fun bothColumnsFollowVisibleRingsAcrossDifferentResolutions() {
        for ((w,h) in listOf(1280 to 720,1920 to 864)) {
            val bitmap=android.graphics.Bitmap.createBitmap(w,h,android.graphics.Bitmap.Config.ARGB_8888)
            try {
                val canvas=android.graphics.Canvas(bitmap)
                canvas.drawColor(android.graphics.Color.rgb(10,15,30))
                val paint=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                val base=VisionCalibrationConfig()
                for (ally in listOf(true,false)) for (slot in 0..4) {
                    val cx=w*(if(ally).106f else .891f)
                    val cy=h*(if(ally)base.allySlotYRatios else base.enemySlotYRatios)[slot]
                    val radius=h*.055f
                    paint.style=android.graphics.Paint.Style.FILL
                    paint.color=android.graphics.Color.WHITE
                    canvas.drawCircle(cx,cy,radius*.35f,paint)
                    paint.style=android.graphics.Paint.Style.STROKE;paint.strokeWidth=5f
                    paint.color=if(ally)android.graphics.Color.rgb(30,190,220) else android.graphics.Color.rgb(220,40,60)
                    canvas.drawCircle(cx,cy,radius,paint)
                }
                // Reproduce a viewer still drawing the wrong default columns over the live frame.
                paint.strokeWidth=1.5f
                for(ally in listOf(true,false)) for(slot in 0..4) {
                    paint.color=if(ally)android.graphics.Color.CYAN else android.graphics.Color.RED
                    canvas.drawCircle(w*(if(ally)base.getAllySlotX(slot) else base.getEnemySlotX(slot)),
                        h*(if(ally)base.allySlotYRatios else base.enemySlotYRatios)[slot],h*.055f,paint)
                }
                val aligned=DraftRingAlignment.align(bitmap,base,null,ignoreDebugRings=true)
                assertEquals(.106f,aligned.allyAvatarCenterX,.008f)
                assertEquals(.891f,aligned.enemyAvatarCenterX,.008f)
            } finally {bitmap.recycle()}
        }
    }
    @Test fun blankImageDoesNotInventAColumn() {
        val bitmap=android.graphics.Bitmap.createBitmap(1280,591,android.graphics.Bitmap.Config.ARGB_8888)
        try { assertEquals(VisionCalibrationConfig(),DraftRingAlignment.align(bitmap,VisionCalibrationConfig(),null)) }
        finally { bitmap.recycle() }
    }

    @Test fun visibleRingsUpdateTheirOwnRowsWhenTheFrameHasAVerticalOffset() {
        val w = 1280
        val h = 720
        val bitmap = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
        try {
            val canvas = android.graphics.Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.rgb(10, 15, 30))
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 5f
                color = android.graphics.Color.rgb(220, 40, 60)
            }
            val base = VisionCalibrationConfig()
            for (slot in 0..4) {
                val shiftedY = h * base.enemySlotYRatios[slot] + 10f
                canvas.drawCircle(w * .891f, shiftedY, h * .055f, paint)
                paint.style = android.graphics.Paint.Style.FILL
                paint.color = android.graphics.Color.WHITE
                canvas.drawCircle(w * .891f, shiftedY, h * .02f, paint)
                paint.style = android.graphics.Paint.Style.STROKE
                paint.color = android.graphics.Color.rgb(220, 40, 60)
            }
            val aligned = DraftRingAlignment.align(bitmap, base, null, ignoreDebugRings = true)
            assertNotNull(aligned)
            assertEquals(.891f, aligned.enemyAvatarCenterX, .008f)
            base.enemySlotYRatios.forEachIndexed { index, y ->
                assertEquals(y + 10f / h, aligned.enemySlotYRatios[index], .012f)
            }
        } finally { bitmap.recycle() }
    }
}
