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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.height
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
        modifier = modifier.wrapContentWidth().height(34.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = HextechGold, contentColor = HextechDarkBg)
    ) {
        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
        Text(tr("Descargar tier list"), fontSize = 10.sp)
    }
}

private fun saveTierListImage(context: Context, champions: List<Champion>): Boolean = runCatching {
    val groups = listOf("S+", "S", "A").map { tier -> tier to champions.filter { it.tier == tier } }
    val rows = groups.sumOf { (_, entries) -> (entries.size + 1) / 2 + 1 }
    // Formato vertical 9:16 para compartir directamente en el teléfono.
    val width = 1080
    val height = 1920
    val rowHeight = 112
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(6, 17, 34) }
    canvas.drawColor(bg.color)
    val title = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE; textSize = 46f; typeface = Typeface.DEFAULT_BOLD }
    val subtitle = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(35, 220, 207); textSize = 25f }
    val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(180, 198, 216); textSize = 20f; textAlign = Paint.Align.RIGHT }
    canvas.drawText("Coach · Tier List", 52f, 78f, title)
    canvas.drawText("S+ · S · A  |  Wild Rift", 52f, 120f, subtitle)
    canvas.drawText(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()), width - 52f, 78f, datePaint)
    var y = 180f
    groups.forEach { (tier, entries) ->
        if (entries.isEmpty()) return@forEach
        val tierPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = when (tier) { "S+" -> android.graphics.Color.rgb(35,220,207); "S" -> android.graphics.Color.rgb(80,220,150); else -> android.graphics.Color.rgb(242,201,76) }; textSize = 30f; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("TIER $tier", 48f, y, tierPaint)
        y += 34f
        entries.chunked(3).forEach { row ->
            row.forEachIndexed { index, champion ->
                val x = 42f + index * 344f
                val avatarX = x + 48f
                val avatarY = y + 38f
                val avatarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(38, 83, 115) }
                canvas.drawCircle(avatarX, avatarY, 35f, avatarPaint)
                loadAssetBitmap(context, champion.avatarUrl.ifBlank { champion.getLocalAvatarUri() })?.let { source ->
                    val clipped = android.graphics.Path().apply { addCircle(avatarX, avatarY, 35f, android.graphics.Path.Direction.CW) }
                    canvas.save(); canvas.clipPath(clipped)
                    canvas.drawBitmap(source, null, android.graphics.RectF(avatarX - 35f, avatarY - 35f, avatarX + 35f, avatarY + 35f), null)
                    canvas.restore(); source.recycle()
                }
                val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE; textSize = 22f; typeface = Typeface.DEFAULT_BOLD }
                canvas.drawText(champion.name, x + 96f, y + 31f, namePaint)
                val stats = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(170, 190, 210); textSize = 17f }
                canvas.drawText("WR ${percent(champion.winrate)}", x + 96f, y + 60f, stats)
                canvas.drawText("P ${percent(champion.pickRate)} · B ${percent(champion.banRate)}", x + 96f, y + 84f, stats)
            }
            y += rowHeight
        }
        y += 22f
    }
    val logo = android.graphics.BitmapFactory.decodeResource(context.resources, com.example.R.drawable.ic_app_logo)
    if (logo != null) {
        val scaled = Bitmap.createScaledBitmap(logo, 68, 68, true)
        canvas.drawBitmap(scaled, (width - 68) / 2f, height - 170f, null)
    }
    val wildRiftLogo = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE; textSize = 20f; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
    canvas.drawText("LEAGUE OF LEGENDS: WILD RIFT", width / 2f, height - 82f, wildRiftLogo)
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

private fun loadAssetBitmap(context: Context, url: String): Bitmap? = runCatching {
    val path = url.removePrefix("file:///android_asset/")
    if (!url.startsWith("file:///android_asset/")) return@runCatching null
    context.assets.open(path).use { android.graphics.BitmapFactory.decodeStream(it) }
}.getOrNull()
