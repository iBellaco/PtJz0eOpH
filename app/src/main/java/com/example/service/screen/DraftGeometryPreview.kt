package com.example.service.screen

import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

/** Two fresh frames per preview request, never a background scanning loop. */
internal suspend fun <T : Any> refreshDraftGeometryPreview(
    capture: () -> T?,
    release: (T) -> Unit,
    canContinue: () -> Boolean,
    process: suspend (T) -> Unit
) {
    repeat(2) { index ->
        var frame: T? = null
        try {
            val acquired = withTimeoutOrNull(800L) {
                while (canContinue()) {
                    frame = capture()
                    if (frame != null) return@withTimeoutOrNull true
                    delay(60L)
                }
                false
            } ?: false
            if (!acquired || !canContinue()) return
            process(checkNotNull(frame))
        } finally {
            // Includes cancellation at the timeout boundary, after acquisition.
            frame?.let(release)
        }
        if (index == 0) delay(300L)
    }
}
