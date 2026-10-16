package com.example.data

import com.example.util.AppLanguage
import java.text.Normalizer
import java.util.Locale

/** Distinguishes the two supported support languages without sending message text elsewhere. */
object SupportLanguageDetector {
    private val portuguese = setOf("nao", "voce", "voces", "obrigado", "obrigada", "preciso", "ajuda", "quando", "porque", "tambem", "mensagem", "aplicativo", "assinatura", "erro", "tela")
    private val spanish = setOf("no", "usted", "ustedes", "gracias", "necesito", "ayuda", "cuando", "porque", "tambien", "mensaje", "aplicacion", "suscripcion", "error", "pantalla")

    fun detect(title: String, message: String, fallback: String = AppLanguage.current.value): String {
        val normalized = Normalizer.normalize("$title $message", Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT)
        val words = Regex("[a-z]+", RegexOption.IGNORE_CASE).findAll(normalized).map { it.value }.toList()
        val pt = words.count { it in portuguese } + if (Regex("\\b(para|com|uma|meu|minha|isso|esta)\\b").containsMatchIn(normalized)) 1 else 0
        val es = words.count { it in spanish } + if (Regex("\\b(el|los|las|una|mi|esto|esta)\\b").containsMatchIn(normalized)) 1 else 0
        return when {
            pt > es -> "pt"
            es > pt -> "es"
            else -> AppLanguage.normalize(fallback)
        }
    }

    fun flag(language: String): String = if (AppLanguage.normalize(language) == "pt") "🇧🇷" else "🇪🇸"
}
