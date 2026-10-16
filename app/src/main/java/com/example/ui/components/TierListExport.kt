package com.example.ui.components

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.model.Champion
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGreen
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.TextPrimary
import com.example.util.tr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Exporta la misma información compacta que aparece en la lista, sin texto de análisis. */
@Composable
fun TierListExportButton(champions: List<Champion>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Button(
        onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                val saved = saveTierListImage(context, champions)
                android.os.Handler(context.mainLooper).post {
                    Toast.makeText(context, if (saved) com.example.util.appTr("Tier list guardada en imágenes") else com.example.util.appTr("No se pudo guardar la tier list"), Toast.LENGTH_SHORT).show()
                }
            }
        },
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = HextechGold, contentColor = HextechDarkBg)
    ) {
        Icon(Icons.Default.Download, contentDescription = null)
        Text(tr("Descargar tier list"))
    }
}

private fun saveTierListImage(context: Context, champions: List<Champion>): Boolean = runCatching {
    val groups = listOf("S+", "S", "A+", "A").map { tier -> tier to champions.filter { it.tier == tier } }
    val rows = groups.sumOf { (_, entries) -> (entries.size + 1) / 2 + 1 }
    val width = 1080
    val rowHeight = 118
    val height = (220 + rows * rowHeight + 150).coerceAtMost(12000)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(7, 20, 38) }
    canvas.drawColor(bg.color)
    val title = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE; textSize = 42f; typeface = Typeface.DEFAULT_BOLD }
    val subtitle = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(35, 220, 207); textSize = 24f }
    canvas.drawText("Coach · Tier List", 48f, 70f, title)
    canvas.drawText("S+ a A · Win / Pick / Ban", 48f, 112f, subtitle)
    var y = 170f
    groups.forEach { (tier, entries) ->
        if (entries.isEmpty()) return@forEach
        val tierPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(242, 201, 76); textSize = 28f; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("TIER $tier", 48f, y, tierPaint)
        y += 24f
        entries.chunked(2).forEach { pair ->
            pair.forEachIndexed { index, champion ->
                val x = 48f + index * 510f
                val avatarX = x + 42f
                val avatarY = y + 38f
                val avatarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(38, 83, 115) }
                canvas.drawCircle(avatarX, avatarY, 32f, avatarPaint)
                val initials = champion.name.trim().split(Regex("\\s+")).mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase(Locale.ROOT)
                val initialsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE; textSize = 20f; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
                canvas.drawText(initials, avatarX, avatarY + 7f, initialsPaint)
                val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE; textSize = 24f; typeface = Typeface.DEFAULT_BOLD }
                canvas.drawText(champion.name, x + 88f, y + 30f, namePaint)
                val stats = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(170, 190, 210); textSize = 19f }
                canvas.drawText(
                    "WR ${percent(champion.winrate)} · Pick ${percent(champion.pickRate)} · Ban ${percent(champion.banRate)}",
                    x + 88f, y + 61f, stats
                )
            }
            y += rowHeight
        }
        y += 22f
    }
    val logo = android.graphics.BitmapFactory.decodeResource(context.resources, com.example.R.drawable.ic_app_logo)
    if (logo != null) {
        val scaled = Bitmap.createScaledBitmap(logo, 68, 68, true)
        canvas.drawBitmap(scaled, 48f, height - 112f, null)
    }
    val footer = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(170, 190, 210); textSize = 17f }
    canvas.drawText("Coach · ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}", 132f, height - 72f, footer)
    canvas.drawText("Todos los derechos reservados", 132f, height - 43f, footer)
    val filename = "Coach-TierList-${System.currentTimeMillis()}.png"
    val resolver = context.contentResolver
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Coach")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
    resolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        ?: return false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
    }
    true
}.getOrDefault(false)

private fun percent(value: Double): String = if (value.isFinite()) String.format(Locale.US, "%.2f%%", value) else "—"
