package com.example

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

/** Test-only pixel rearrangement: the source has no Teemo title to reveal as a ninth name. */
internal object DraftSequenceFixture {
    fun ids(firstPick: Boolean) = if (firstPick)
        listOf("senna", "ornn", "mel", "volibear", "vi", "alistar", "caitlyn", "nocturne", "vex", "teemo")
        else listOf("senna", "ornn", "mel", "volibear", "teemo", "alistar", "caitlyn", "nocturne", "vex", "vi")

    fun create(source: Bitmap, firstPick: Boolean): Bitmap = source.copy(Bitmap.Config.ARGB_8888, true).also { frame ->
        if (!firstPick) {
            // Retain the target team's ring. Exchange real portrait pixels, not generated artwork.
            for ((fromX, fromY, toX, toY) in listOf(listOf(1227,419,92,420), listOf(92,420,1227,419))) {
                for (dy in -27..27) for (dx in -27..27) if (dx*dx+dy*dy <= 27*27) {
                    frame.setPixel(toX+dx,toY+dy,source.getPixel(fromX+dx,fromY+dy))
                }
            }
            val canvas = Canvas(frame)
            canvas.drawRect(1040f,390f,1194f,420f,Paint().apply { color=Color.BLACK })
            // Copy the full original VI glyphs, without the mastery badge; do not redraw a name.
            canvas.drawBitmap(source,Rect(155,400,173,415),Rect(1174,399,1192,414),null)
        }
    }
}
