package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.InputStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Detector y clasificador inteligente de posibles modelos de celulares a partir de fotos y capturas de pantalla.
 * Analiza la resolución geométrica, relación de aspecto y densidad para inferir con alta precisión
 * los dispositivos móviles compatibles o de procedencia (incluyendo Samsung Galaxy S26 Ultra, S25 Ultra, S24 Ultra, etc.).
 */
object DevicePhotoModelDetector {

    data class DevicePhotoAnalysis(
        val width: Int,
        val height: Int,
        val landscapeWidth: Int,
        val landscapeHeight: Int,
        val aspectRatio: Float,
        val aspectRatioLabel: String,
        val probableDeviceModels: List<String>,
        val primaryDeviceSummary: String
    )

    /**
     * Analiza un Bitmap en memoria y devuelve la identificación geométrica y los posibles modelos de celulares.
     */
    fun analyzeBitmap(bitmap: Bitmap?): DevicePhotoAnalysis? {
        if (bitmap == null || bitmap.width <= 0 || bitmap.height <= 0) return null
        return analyzeDimensions(bitmap.width, bitmap.height)
    }

    /**
     * Analiza una imagen en formato Base64 sin necesidad de cargarla completamente en memoria de alta resolución.
     */
    fun analyzeBase64(base64String: String): DevicePhotoAnalysis? {
        return try {
            val cleanB64 = if (base64String.contains(",")) base64String.substringAfter(",") else base64String
            val bytes = Base64.decode(cleanB64, Base64.DEFAULT)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            if (options.outWidth > 0 && options.outHeight > 0) {
                analyzeDimensions(options.outWidth, options.outHeight)
            } else null
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Analiza una URI de imagen mediante el ContentResolver leyendo únicamente sus encabezados.
     */
    fun analyzeUri(context: Context, uri: Uri): DevicePhotoAnalysis? {
        return try {
            val input: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(input, null, options)
            input?.close()
            if (options.outWidth > 0 && options.outHeight > 0) {
                analyzeDimensions(options.outWidth, options.outHeight)
            } else null
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Clasifica las dimensiones en base a la base de datos de modelos de celulares de mercado.
     */
    fun analyzeDimensions(width: Int, height: Int): DevicePhotoAnalysis {
        val longDim = max(width, height)
        val shortDim = min(width, height).coerceAtLeast(1)
        val ratio = longDim.toFloat() / shortDim.toFloat()

        val ratioLabel = when {
            abs(ratio - (19.5f / 9f)) < 0.04f -> "19.5:9"
            abs(ratio - (20f / 9f)) < 0.04f -> "20:9"
            abs(ratio - (19.3f / 9f)) < 0.04f -> "19.3:9"
            abs(ratio - (19f / 9f)) < 0.04f -> "19:9"
            abs(ratio - (18.5f / 9f)) < 0.04f -> "18.5:9"
            abs(ratio - (18f / 9f)) < 0.04f -> "18:9"
            abs(ratio - (16f / 9f)) < 0.04f -> "16:9"
            abs(ratio - (21f / 9f)) < 0.04f -> "21:9"
            abs(ratio - (4f / 3f)) < 0.05f -> "4:3 (Tablet)"
            abs(ratio - (16f / 10f)) < 0.05f -> "16:10 (Tablet)"
            else -> String.format(java.util.Locale.US, "%.2f:1", ratio)
        }

        val models = mutableListOf<String>()

        // 1. Detección por resolución exacta o cercana (Tolerancia ±3%)
        when {
            // Serie Samsung Galaxy Ultra QHD+ (S26 Ultra / S25 Ultra / S24 Ultra)
            (longDim in 3080..3160 && shortDim in 1400..1460) -> {
                models.add("Samsung Galaxy S26 Ultra (QHD+)")
                models.add("Samsung Galaxy S25 Ultra (QHD+)")
                models.add("Samsung Galaxy S24 Ultra (QHD+)")
            }

            // Serie Samsung Galaxy Note 20 Ultra / S23 Ultra / S22 Ultra
            (longDim in 3040..3095 && shortDim in 1410..1450) -> {
                models.add("Samsung Galaxy S23 Ultra")
                models.add("Samsung Galaxy S22 Ultra")
                models.add("Samsung Galaxy Note 20 Ultra")
            }

            // Flagships Ultrawide 2K/QHD+ (Xiaomi 13/14 Pro, OnePlus 11/12)
            (longDim in 3180..3240 && shortDim in 1420..1460) -> {
                models.add("Xiaomi 14 Pro / 13 Pro (2K)")
                models.add("OnePlus 12 / 11 (2K)")
                models.add("Vivo X100 Pro")
            }

            // Resolución 1.5K muy popular (Xiaomi 13T Pro / Redmi K60 / K70 / Poco F6)
            (longDim in 2680..2750 && shortDim in 1200..1240) -> {
                models.add("Xiaomi 13T Pro / 14T Pro")
                models.add("POCO F6 / Redmi K70 (1.5K)")
                models.add("Realme GT Neo / OnePlus Ace")
            }

            // Estándar Flagship FHD+ 19.5:9 (Samsung Galaxy S26, S25, S24, S23, S22, Pixel 8/9)
            (longDim in 2300..2380 && shortDim in 1060..1100) -> {
                models.add("Samsung Galaxy S26 / S25 / S24 (FHD+)")
                models.add("Samsung Galaxy S23 / S22")
                models.add("Google Pixel 9 / Pixel 8")
                models.add("iPhone 15 / 16 (Captura convertida)")
            }

            // Estándar Gama Media / Alta 20:9 (Xiaomi, POCO, Samsung Serie A54/A55, Moto Edge)
            (longDim in 2380..2460 && shortDim in 1060..1100) -> {
                models.add("Xiaomi Redmi Note 13 / 12 Pro")
                models.add("POCO X6 Pro / F5")
                models.add("Samsung Galaxy A55 / A54")
                models.add("Motorola Edge / Moto G84")
            }

            // Resolución iPhone Pro Max (2796 x 1290 o similar)
            (longDim in 2760..2820 && shortDim in 1270..1310) -> {
                models.add("Apple iPhone 16 Pro Max / 15 Pro Max")
                models.add("Apple iPhone 14 Pro Max")
            }

            // Resolución iPhone Pro estándar (2556 x 1179)
            (longDim in 2520..2580 && shortDim in 1160..1200) -> {
                models.add("Apple iPhone 16 Pro / 15 Pro")
                models.add("Apple iPhone 14 Pro")
            }

            // Tablets y Plegables
            (ratio < 1.6f && longDim >= 1900) -> {
                models.add("Samsung Galaxy Tab S9 / S8")
                models.add("iPad Air / Pro")
                models.add("Samsung Galaxy Z Fold (Pantalla Interior)")
            }

            // Estándar 16:9 clásico (1920x1080 / 1280x720 / Emuladores)
            (longDim in 1900..1940 && shortDim in 1060..1100 && abs(ratio - 1.777f) < 0.05f) -> {
                models.add("Emulador PC (BlueStacks / LDPlayer 1080p)")
                models.add("Dispositivo 16:9 Estándar (FHD)")
            }

            // Genéricos según relación de aspecto
            else -> {
                when {
                    abs(ratio - (19.5f / 9f)) < 0.08f -> {
                        models.add("Gama Alta Panorámica (19.5:9, ej. Samsung Galaxy S-Series / Pixel)")
                    }
                    abs(ratio - (20f / 9f)) < 0.08f -> {
                        models.add("Gama Media Panorámica (20:9, ej. Xiaomi / POCO / Galaxy A-Series)")
                    }
                    abs(ratio - (19.3f / 9f)) < 0.08f -> {
                        models.add("Samsung Galaxy Note / Ultra Series (19.3:9)")
                    }
                    else -> {
                        models.add("Dispositivo Móvil Android (${ratioLabel})")
                    }
                }
            }
        }

        val primarySummary = if (models.isNotEmpty()) {
            "${models.first()} [${longDim}x${shortDim}, $ratioLabel]"
        } else {
            "${longDim}x${shortDim} ($ratioLabel)"
        }

        return DevicePhotoAnalysis(
            width = width,
            height = height,
            landscapeWidth = longDim,
            landscapeHeight = shortDim,
            aspectRatio = ratio,
            aspectRatioLabel = ratioLabel,
            probableDeviceModels = models,
            primaryDeviceSummary = primarySummary
        )
    }
}
