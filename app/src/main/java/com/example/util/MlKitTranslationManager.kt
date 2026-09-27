package com.example.util

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap

sealed class MlKitDownloadStatus {
    object Idle : MlKitDownloadStatus()
    data class Downloading(val progressMessage: String = "Baixando pacote de idioma do Google ML Kit...") : MlKitDownloadStatus()
    object Downloaded : MlKitDownloadStatus()
    data class Error(val message: String) : MlKitDownloadStatus()
}

object MlKitTranslationManager {
    private const val TAG = "MlKitTranslationManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _downloadStatus = MutableStateFlow<MlKitDownloadStatus>(MlKitDownloadStatus.Idle)
    val downloadStatus: StateFlow<MlKitDownloadStatus> = _downloadStatus.asStateFlow()

    private var ptTranslator: Translator? = null
    private val translationMemoryCache = ConcurrentHashMap<String, String>()
    private var isInitialized = false

    init {
        checkExistingModels()
    }

    private fun checkExistingModels() {
        scope.launch {
            try {
                val modelManager = RemoteModelManager.getInstance()
                val ptModel = TranslateRemoteModel.Builder(TranslateLanguage.PORTUGUESE).build()
                val isDownloaded = modelManager.isModelDownloaded(ptModel).await()
                if (isDownloaded) {
                    _downloadStatus.value = MlKitDownloadStatus.Downloaded
                    initPtTranslator()
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error comprobando modelo de idioma ML Kit: ${e.message}")
            }
        }
    }

    private fun initPtTranslator() {
        if (ptTranslator == null) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.SPANISH)
                .setTargetLanguage(TranslateLanguage.PORTUGUESE)
                .build()
            ptTranslator = Translation.getClient(options)
            isInitialized = true
        }
    }

    suspend fun downloadPortugueseModel(onProgress: ((String) -> Unit)? = null): Boolean {
        return try {
            _downloadStatus.value = MlKitDownloadStatus.Downloading("Iniciando download do pacote Google ML Kit...")
            onProgress?.invoke("Iniciando download do pacote Google ML Kit...")

            initPtTranslator()
            val conditions = DownloadConditions.Builder().build()
            
            _downloadStatus.value = MlKitDownloadStatus.Downloading("Baixando modelo de tradução neural (Espanhol ➔ Português)...")
            onProgress?.invoke("Baixando modelo de tradução neural (Espanhol ➔ Português)...")

            ptTranslator?.downloadModelIfNeeded(conditions)?.await()

            _downloadStatus.value = MlKitDownloadStatus.Downloaded
            onProgress?.invoke("Pacote de idioma instalado e pronto para uso!")
            AppLogger.d(TAG, "Google ML Kit Translate (Português) baixado com sucesso")
            true
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Erro ao baixar pacote de idioma ML Kit"
            _downloadStatus.value = MlKitDownloadStatus.Error(errorMsg)
            AppLogger.e(TAG, "Falha no download do pacote de idioma ML Kit: $errorMsg")
            false
        }
    }

    fun isPortugueseDownloaded(): Boolean {
        return _downloadStatus.value is MlKitDownloadStatus.Downloaded
    }

    suspend fun translateAsync(text: String, targetLang: String = "pt"): String {
        if (text.isBlank() || targetLang == "es") return text

        val cached = translationMemoryCache[text]
        if (cached != null) return cached

        // 1. Verificar traducciones estáticas primero
        val staticTranslation = translations[targetLang]?.get(text)
        if (staticTranslation != null && staticTranslation.isNotBlank()) {
            translationMemoryCache[text] = staticTranslation
            return staticTranslation
        }

        // 2. Usar motor Google ML Kit si está disponible
        val translator = ptTranslator
        if (translator != null && isPortugueseDownloaded()) {
            try {
                val result = translator.translate(text).await()
                if (result.isNotBlank()) {
                    translationMemoryCache[text] = result
                    return result
                }
            } catch (e: Exception) {
                AppLogger.w(TAG, "ML Kit Translate error fallback: ${e.message}")
            }
        }

        // 3. Fallback a reemplazos heurísticos de términos clave
        val fallback = trStr(targetLang, text)
        translationMemoryCache[text] = fallback
        return fallback
    }

    fun translateSync(text: String, targetLang: String = "pt"): String {
        if (text.isBlank() || targetLang == "es") return text

        val cached = translationMemoryCache[text]
        if (cached != null) return cached

        val staticTrans = translations[targetLang]?.get(text)
        if (staticTrans != null && staticTrans.isNotBlank()) {
            translationMemoryCache[text] = staticTrans
            return staticTrans
        }

        val fallback = trStr(targetLang, text)
        translationMemoryCache[text] = fallback

        // Lanzar traducción en segundo plano con ML Kit para actualizar la memoria futura
        val translator = ptTranslator
        if (translator != null && isPortugueseDownloaded()) {
            scope.launch {
                try {
                    val mlResult = translator.translate(text).await()
                    if (mlResult.isNotBlank()) {
                        translationMemoryCache[text] = mlResult
                    }
                } catch (_: Exception) {}
            }
        }

        return fallback
    }

    fun warmUpCommonPhrases() {
        scope.launch {
            if (isPortugueseDownloaded()) {
                initPtTranslator()
            }
        }
    }
}
