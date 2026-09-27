package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import com.example.data.WildRiftSpellsAndRunes

/**
 * Detector y Clasificador de Hechizos de Invocador (Summoner Spells) de Wild Rift.
 * Analiza la firma cromática y espectral de los iconos en pantalla
 * para identificar Destello, Prender, Castigo, Curar, Barrera, Extenuación, etc.
 */
object SummonerSpellDetector {

    data class SpellMatch(
        val spellId: String,
        val spellName: String,
        val iconUrl: String,
        val rect: Rect
    )

    fun detectSpell(spellBitmap: Bitmap, rect: Rect): SpellMatch? {
        if (spellBitmap.width < 8 || spellBitmap.height < 8) return null
        val w = spellBitmap.width
        val h = spellBitmap.height
        val pixels = IntArray(w * h)
        spellBitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        var totalValid = 0
        var totalLum = 0L

        var smiteCount = 0      // Smite (Castigo) - Garra/Fuego de bestia naranja/ámbar cálido o púrpura
        var yellowCount = 0     // Flash (Destello) - Amarillo puro eléctrico
        var amberGoldCount = 0  // Barrier (Barrera) - Escudo ámbar / dorado
        var redFireCount = 0    // Ignite (Prender) - Fuego rojo carmesí profundo
        var greenCount = 0      // Heal (Curar) - Verde esmeralda vivo
        var cyanCount = 0       // Ghost (Fantasmal) - Cyan / Azul brillante
        var darkBrownCount = 0  // Exhaust (Extenuación) - Bronce / Marrón apagado

        // Tomar zona central interna (evitar bordes circulares o bordes oscuros)
        val startX = (w * 0.15f).toInt()
        val endX = (w * 0.85f).toInt()
        val startY = (h * 0.15f).toInt()
        val endY = (h * 0.85f).toInt()

        for (y in startY..endY) {
            for (x in startX..endX) {
                val color = pixels[y * w + x]
                val r = Color.red(color)
                val g = Color.green(color)
                val b = Color.blue(color)
                val lum = (r * 299 + g * 587 + b * 114) / 1000
                totalLum += lum
                totalValid++

                // 1. Castigo (Smite): En Wild Rift es una garra de bestia ígnea naranja/roja brillante con destellos cálidos (R alto, G medio-alto, B bajo) o halo púrpura
                if ((r in 140..255 && g in 45..145 && b < 80 && (r - g) in 30..120 && (g - b) >= 15) ||
                    (r > 100 && b > 100 && g < 120 && (r + b) > (2 * g + 20))) {
                    smiteCount++
                }
                // 2. Curar (Heal): Verde esmeralda vivo predominante
                else if (g > 120 && g > r + 25 && g > b + 25) {
                    greenCount++
                }
                // 3. Fantasmal (Ghost): Cyan / Azul claro brillante
                else if (b > 130 && g > 110 && r < 120 && (b - r) > 25) {
                    cyanCount++
                }
                // 4. Prender (Ignite): Rojo intenso llameante carmesí (G muy bajo)
                else if (r > 155 && g < 55 && b < 65 && (r - g) > 90) {
                    redFireCount++
                }
                // 5. Destello (Flash): Amarillo eléctrico puro y chispas doradas de Flash
                else if (r > 165 && g > 140 && b < 125 && kotlin.math.abs(r - g) < 50) {
                    yellowCount++
                }
                // 6. Barrera (Barrier): Ámbar / Dorado esférico puro
                else if (r > 170 && g in 110..175 && b in 40..105 && (r - g) in 30..75) {
                    amberGoldCount++
                }
                // 7. Extenuación (Exhaust): Marrón / Bronce oscuro
                else if (r in 110..180 && g in 75..135 && b < 75 && (r - g) in 25..65) {
                    darkBrownCount++
                }
            }
        }

        if (totalValid == 0) return null
        val avgLum = totalLum.toFloat() / totalValid.toFloat()
        // Si el área es muy oscura (< 25 lum), se descarta (slot vacío o sin pick)
        if (avgLum < 25f) return null

        val smiteRatio = smiteCount.toFloat() / totalValid.toFloat()
        val greenRatio = greenCount.toFloat() / totalValid.toFloat()
        val cyanRatio = cyanCount.toFloat() / totalValid.toFloat()
        val redRatio = redFireCount.toFloat() / totalValid.toFloat()
        val yellowRatio = yellowCount.toFloat() / totalValid.toFloat()
        val amberRatio = amberGoldCount.toFloat() / totalValid.toFloat()
        val brownRatio = darkBrownCount.toFloat() / totalValid.toFloat()

        val (spellId, name) = when {
            // 1. Castigo (Smite): Garra de bestia ígnea / daga
            smiteRatio > 0.04f -> "smite" to "Castigo"
            // 2. Curar: Verde vivo
            greenRatio > 0.06f -> "heal" to "Curar"
            // 3. Fantasmal: Cyan vivo
            cyanRatio > 0.06f -> "ghost" to "Fantasmal"
            // 4. Prender: Fuego rojo vivo carmesí
            redRatio > 0.06f -> "ignite" to "Prender"
            // 5. Destello: Amarillo eléctrico característico de Flash (predominante)
            yellowRatio > 0.035f -> "flash" to "Destello"
            // 6. Barrera: Solo si hay ámbar muy marcado sin componente amarillo
            amberRatio > 0.12f && yellowRatio < 0.02f -> "barrier" to "Barrera"
            // 7. Extenuación: Bronce / marrón
            brownRatio > 0.10f -> "exhaust" to "Extenuación"
            // 8. Destello fallback si hay destello dorado
            yellowRatio > 0.02f || amberRatio > 0.05f -> "flash" to "Destello"
            else -> return null // Evitar inventar si no hay coincidencia cromática real
        }

        val item = WildRiftSpellsAndRunes.getSpellByName(spellId)
        val iconUrl = item?.iconUrl ?: "file:///android_asset/offline_images/b4f2c9b975912c3a88ceb806e74c23f1.webp"
        return SpellMatch(spellId, name, iconUrl, rect)
    }
}
