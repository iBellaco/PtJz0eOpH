package com.example.util

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogger {
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    fun d(tag: String, message: String) {
        Log.d(tag, DiagnosticPrivacy.redact(message))
        addLog("DEBUG", tag, message)
    }

    fun i(tag: String, message: String) {
        Log.i(tag, DiagnosticPrivacy.redact(message))
        addLog("INFO", tag, message)
    }

    fun w(tag: String, message: String) {
        Log.w(tag, DiagnosticPrivacy.redact(message))
        addLog("WARN", tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, DiagnosticPrivacy.redact("$message ${throwable?.javaClass?.simpleName.orEmpty()}"))
        val errorMessage = if (throwable != null) "$message: ${throwable.message}" else message
        addLog("ERROR", tag, errorMessage)
    }

    private fun addLog(level: String, tag: String, message: String) {
        val timestamp = synchronized(dateFormat) { dateFormat.format(Date()) }
        val logEntry = "[$timestamp] $level/$tag: ${DiagnosticPrivacy.redact(message)}"
        _logs.update { currentLogs -> 
            val newLogs = currentLogs.toMutableList()
            newLogs.add(0, logEntry)
            if (newLogs.size > 200) {
                newLogs.removeAt(newLogs.lastIndex)
            }
            newLogs
        }
    }

    fun clear() {
        _logs.value = emptyList()
    }
}
