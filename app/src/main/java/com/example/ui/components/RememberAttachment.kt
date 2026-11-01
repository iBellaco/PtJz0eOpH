package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

private val attachmentDecoders = Semaphore(2)

/** Bound work and memory for untrusted image attachments, away from the UI thread. */
@Composable
internal fun rememberAttachment(encoded: String, maxDimension: Int = 1600): State<Bitmap?> =
    produceState<Bitmap?>(null, encoded, maxDimension) {
        value = null
        value = withContext(Dispatchers.IO) {
            attachmentDecoders.withPermit {
                if (encoded.length > 12 * 1024 * 1024) return@withPermit null
                runCatching {
                    val bytes = Base64.decode(encoded.substringAfter(',', encoded), Base64.DEFAULT)
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                    if (options.outWidth <= 0 || options.outHeight <= 0) return@runCatching null
                    options.inSampleSize = 1
                    while (maxOf(options.outWidth, options.outHeight) / options.inSampleSize > maxDimension) {
                        options.inSampleSize *= 2
                    }
                    options.inJustDecodeBounds = false
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                }.getOrNull()
            }
        }
    }
