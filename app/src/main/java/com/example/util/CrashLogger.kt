package com.example.util

import android.content.Context
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.util.Date

object CrashLogger {
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val file = File(context.filesDir, "crash_log.txt")
                // Cap persistent diagnostics; log stacks without exception messages.
                if (file.length() > 256 * 1024) file.writeText("")
                val writer = PrintWriter(FileWriter(file, true))
                writer.println("---- CRASH at ${Date()} ----")
                writer.println("Thread: ${DiagnosticPrivacy.redact(thread.name)}")
                writer.println(throwable.javaClass.name)
                throwable.stackTrace.take(64).forEach { writer.println("at $it") }
                writer.println("--------------------------------")
                writer.close()
            } catch (e: Exception) {
                // Ignore
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
