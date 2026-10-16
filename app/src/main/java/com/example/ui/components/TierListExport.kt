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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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
        modifier = modifier.wrapContentWidth().height(48.dp).then(Modifier.testTag("tier_list_export")),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = HextechGold, contentColor = HextechDarkBg)
    ) {
        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
        Text(tr("Descargar tier list"), fontSize = 10.sp)
    }
}

private fun saveTierListImage(context: Context, champions: List<Champion>): Boolean = runCatching {
    val bitmap = renderTierListImage(context, champions)
    try {
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
    val written = resolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    if (written != true) {
        resolver.delete(uri, null, null)
        return false
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
    }
    true
    } finally { bitmap.recycle() }
}.getOrDefault(false)

internal fun renderTierListImage(context: Context, champions: List<Champion>): Bitmap {
    val groups = listOf("S+", "S", "A").map { tier -> tier to champions.filter { it.tier == tier || tier == "A" && it.tier == "A+" } }
    val columns = if (champions.size > 80) 5 else 4
    val rowCount = groups.sumOf { (_, entries) -> (entries.size + columns - 1) / columns }
    val bitmap = Bitmap.createBitmap(2160, 3840, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.scale(2f, 2f)
    canvas.drawColor(android.graphics.Color.rgb(6, 17, 30))
    loadAssetBitmap(context, "file:///android_asset/summoners_rift.png")?.let { map ->
        val scale = maxOf(1080f / map.width, 1920f / map.height)
        val dw = map.width * scale; val dh = map.height * scale
        canvas.drawBitmap(map, null, android.graphics.RectF((1080-dw)/2, (1920-dh)/2, (1080+dw)/2, (1920+dh)/2), Paint().apply { alpha=95 })
        map.recycle()
    }
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    fun text(value: String, x: Float, y: Float, size: Float, color: Int, maxWidth: Float,
             centered: Boolean = false) {
        paint.color=color; paint.textSize=size; paint.typeface=Typeface.DEFAULT_BOLD
        paint.textAlign=if (centered) Paint.Align.CENTER else Paint.Align.LEFT
        while (paint.measureText(value)>maxWidth && paint.textSize>7f) paint.textSize-=.5f
        canvas.drawText(value,x,y,paint)
    }
    val white=android.graphics.Color.WHITE
    text("COACH",40f,62f,34f,white,500f)
    text("TIER LIST · S+ / S / A",40f,100f,20f,0xFF32D8CC.toInt(),650f)
    paint.textAlign=Paint.Align.RIGHT;paint.textSize=18f;paint.color=white
    canvas.drawText(SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.getDefault()).format(Date()),1040f,56f,paint)
    val groupSpace=groups.count { it.second.isNotEmpty() } * 50f
    val cellHeight=minOf(118f,(1540f-groupSpace)/rowCount.coerceAtLeast(1))
    val cellWidth=1000f/columns
    var y=140f
    groups.forEach { (tier, entries) ->
        if (entries.isEmpty()) return@forEach
        val accent=when(tier) { "S+"->0xFF32D8CC.toInt();"S"->0xFF6BDE9C.toInt();else->0xFFE8CA82.toInt() }
        paint.color=0xDD0C2434.toInt(); canvas.drawRoundRect(32f,y,1048f,y+36,10f,10f,paint)
        text(tier,48f,y+26,27f,accent,120f)
        text("${entries.size}",990f,y+24,17f,white,45f,true)
        y+=44f
        entries.chunked(columns).forEach { row ->
            row.forEachIndexed { index, champion ->
                val x=40f+index*cellWidth; val center=x+cellWidth/2
                paint.color=0xCC071521.toInt(); canvas.drawRoundRect(x+3,y,x+cellWidth-3,y+cellHeight-3,8f,8f,paint)
                val radius=minOf(28f,cellHeight*.20f)
                val cy=y+radius+3
                paint.color=accent;canvas.drawCircle(center,cy,radius+1.5f,paint)
                val source = loadAssetBitmap(context,champion.getLocalAvatarUri())
                    ?: loadAssetBitmap(context,champion.avatarUrl)
                source?.let {
                    canvas.save()
                    canvas.clipPath(android.graphics.Path().apply { addCircle(center,cy,radius,android.graphics.Path.Direction.CW) })
                    canvas.drawBitmap(it,null,android.graphics.RectF(center-radius,cy-radius,center+radius,cy+radius),null)
                    canvas.restore();it.recycle()
                }
                val nameY=y+cellHeight*.68f
                text(champion.name,center,nameY,minOf(21f,cellHeight*.23f),white,cellWidth-12,true)
                text("WR ${percent(champion.winrate)} · P ${percent(champion.pickRate)} · B ${percent(champion.banRate)}",
                    center,y+cellHeight*.94f,minOf(15f,cellHeight*.21f),0xFFB4D8DE.toInt(),cellWidth-12,true)
                if(champion.tierMovement!=0) text(if(champion.tierMovement>0) "↑" else "↓",x+cellWidth-24,y+19,20f,
                    if(champion.tierMovement>0)0xFF6BDE9C.toInt() else 0xFFFF6677.toInt(),20f,true)
            }
            y+=cellHeight
        }
        y+=6f
    }
    // Footer is reserved regardless of roster size. Logos never cover a champion.
    paint.color=0xE806121F.toInt();canvas.drawRoundRect(32f,1710f,1048f,1890f,20f,20f,paint)
    android.graphics.BitmapFactory.decodeResource(context.resources,com.example.R.drawable.ic_app_logo)?.let { logo ->
        canvas.drawBitmap(logo,null,android.graphics.RectF(470f,1730f,610f,1870f),null);logo.recycle()
    }
    loadAssetBitmap(context,"file:///android_asset/wild_rift_logo.png")?.let { logo ->
        canvas.drawBitmap(logo,null,android.graphics.RectF(825f,1750f,1025f,1886f),null);logo.recycle()
    }
    return bitmap
}

private fun percent(value: Double): String = if(value.isFinite()) String.format(Locale.US,"%.2f%%",value) else "—"

private fun loadAssetBitmap(context: Context, url: String): Bitmap? = runCatching {
    val path=url.removePrefix("file:///android_asset/")
    if(!url.startsWith("file:///android_asset/")) return@runCatching null
    context.assets.open(path).use { android.graphics.BitmapFactory.decodeStream(it) }
}.getOrNull()
