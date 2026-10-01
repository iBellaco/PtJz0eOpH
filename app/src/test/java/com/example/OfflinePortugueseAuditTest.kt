package com.example

import com.example.util.TranslationCatalog
import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

/** Runs independently of Android so bundled advice cannot silently escape localization. */
class OfflinePortugueseAuditTest {
    private val assets = File("src/main/assets").takeIf { it.isDirectory } ?: File("app/src/main/assets")
    private fun phrases(name: String): Map<String, String> {
        val json = JSONObject(File(assets, name).readText())
        return json.keys().asSequence().associateWith { json.getString(it) }
    }
    private val catalog by lazy {
        TranslationCatalog(phrases("translations_pt.json"), portugueseAliases = phrases("translations_pt_aliases.json"))
    }

    @Test fun `every bundled build advice is localized including contextual suffixes`() {
        val builds = JSONArray(File(assets, "champions_creator_builds.json").readText())
        var descriptions = 0
        fun inspect(value: Any) {
            when (value) {
                is JSONObject -> value.keys().asSequence().forEach { key ->
                    val child = value.get(key)
                    if (key == "description" && child is String && child.isNotBlank()) {
                        val translated = catalog.translate("pt", child)
                        assertNotEquals("Advice remained Spanish: $child", child, translated)
                        assertFalse("Mixed advice: $translated", Regex("\\b(?:daño|curación|velocidad|hechizos|enemigos|cómpralo|úsalo|Línea de|los|las|del)\\b", RegexOption.IGNORE_CASE).containsMatchIn(translated))
                        descriptions++
                    } else if (child is JSONObject || child is JSONArray) inspect(child)
                }
                is JSONArray -> (0 until value.length()).forEach { inspect(value.get(it)) }
            }
        }
        inspect(builds)
        assertEquals(300, builds.length())
        assertTrue(descriptions > 3000)
    }

    @Test fun `runtime amounts session messages and build controls use Portuguese`() {
        assertEquals("Ver dica da build", catalog.translate("pt", "Ver consejo de la build"))
        assertEquals("Assinatura cancelada com sucesso", catalog.translate("pt", "Suscripción cancelada correctamente"))
        assertEquals("3. Itens principais (2/3) *Descrição obrigatória", catalog.translate("pt", "3. Objetos Core (2/3) *Desc. Obligatoria"))
        assertEquals("Erro ao abrir link: teste", catalog.translate("pt", "Error al abrir enlace: teste"))
        assertEquals("Diego_42: GG Volibear!", catalog.translate("pt", "Diego_42: GG Volibear!"))
    }
}
