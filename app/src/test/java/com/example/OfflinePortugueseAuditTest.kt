package com.example

import com.example.util.TranslationCatalog
import com.example.util.TranslationAssets
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
        TranslationAssets.load(::phrases)
    }

    @Test fun `subscription overlay recommendation and download regressions use Portuguese`() {
        val examples = mapOf(
            "+7 Días" to "+7 Dias",
            "Desactivando..." to "Desativando...",
            "Depurado" to "Depuração",
            "Mi Pick" to "Minha escolha",
            "Suscripción Premium Activa" to "Assinatura Premium Ativa",
            "Cuenta Principal" to "Conta Principal",
            "Guardián (Soporte Global Definitiva)" to "Guardião (Suporte Global com a Definitiva)",
            "Primer Golpe & Escalado Nivel 13" to "Primeiro Ataque e Escalamento no Nível 13",
            "APERTURA FÍSICA" to "ABERTURA FÍSICA",
            "Pix QR Code" to "Código QR Pix",
            "Descargando código QR Pix" to "Baixando código QR Pix"
        )
        examples.forEach { (source, expected) ->
            assertEquals(source, expected, catalog.translate("pt", source))
            assertEquals(source, source, catalog.translate("es", source))
        }
        val build = catalog.translate("pt", "Build calculada por IA y análisis estadístico para Hwei: 3 Core Items indispensables, opciones situacionales y botas de Nivel 3 adaptadas.")
        assertEquals("Build calculada por IA e análise estatística para Hwei: 3 itens principais indispensáveis, opções situacionais e botas de Nível 3 adaptadas.", build)
        assertEquals("Poro Guardião", catalog.translate("pt", "Poro Guardián"))
        assertEquals("CLÁSSICO", catalog.translate("pt", "CLÁSICO"))
        assertEquals("COMUM", catalog.translate("pt", "COMÚN"))
        assertEquals("Elite / Alto Desempenho", catalog.translate("pt", "Élite / Alto Rendimiento"))
        assertEquals("Em Aprendizagem / Irregular", catalog.translate("pt", "En Aprendizaje / Irregular"))
        assertEquals("Nível 1 - Alto Desempenho", catalog.translate("pt", "Tier 1 - Alto Rendimiento"))
    }

    @Test fun `support catalog used by the app is included in the offline audit`() {
        assertEquals("Não foi possível carregar as mensagens. Verifique as permissões e tente novamente.",
            catalog.translate("pt", "No se pudieron cargar los mensajes. Comprueba los permisos y vuelve a intentarlo."))
        assertEquals("Responder novamente", catalog.translate("pt", "Responder de nuevo"))
        assertEquals("Status atualizado", catalog.translate("pt", "Estado actualizado"))
    }

    @Test fun `Portuguese resources cover every default string without Spanish residues`() {
        val resources = File(assets.parentFile, "res")
        fun strings(directory: String): Map<String, String> {
            val document = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(File(resources, "$directory/strings.xml"))
            val nodes = document.getElementsByTagName("string")
            return (0 until nodes.length).associate { index ->
                val node = nodes.item(index) as org.w3c.dom.Element
                node.getAttribute("name") to node.textContent
            }
        }
        val defaultKeys = strings("values").keys
        for (directory in listOf("values-pt", "values-pt-rBR")) {
            val translated = strings(directory)
            assertTrue("Missing Portuguese resources in $directory", translated.keys.containsAll(defaultKeys))
            translated.forEach { (key, value) ->
                assertFalse("Mixed resource $directory/$key: $value", spanishResidue.containsMatchIn(value))
            }
        }
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
                        // Some generated descriptions are already Portuguese in the
                        // source asset; the residue assertion below is the relevant
                        // invariant for those entries.
                        if (spanishResidue.containsMatchIn(translated)) {
                            println("PORTUGUESE_AUDIT_RESIDUE: $translated")
                            fail("Mixed advice: $translated")
                        }
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

    @Test fun `item model statistics and tier filter are translated`() {
        assertEquals("Filtrar por Rota", catalog.translate("pt", "Filtrar por Línea"))
        val source = "+25 Daño de ataque • +1000 Maná máximo • +20 Velocidad de habilidad"
        val translated = catalog.translate("pt", source)
        assertNotEquals(source, translated)
        assertFalse(Regex("\\b(?:daño|velocidad)\\b", RegexOption.IGNORE_CASE).containsMatchIn(translated))
    }

    @Test fun `Portuguese catalogs do not retain partially translated Spanish sentences`() {
        val fragments = spanishResidue
        for (file in TranslationAssets.portugueseFiles + "translations_pt_aliases.json") {
            phrases(file).forEach { (source, translated) ->
                val cleaned = catalog.translate("pt", translated)
                if (fragments.containsMatchIn(cleaned)) {
                    println("PORTUGUESE_CATALOG_RESIDUE: $source => $cleaned")
                    fail("Mixed Portuguese: $source => $cleaned")
                }
            }
        }
        assertEquals("Outros Itens", catalog.translate("pt", "Otros Objetos"))
        assertEquals("Fimbulwinter", catalog.translate("pt", "El gran invierno"))
        assertEquals("Conceder Status de Verificado", catalog.translate("pt", "Otorgar Estado de Verificado"))
    }

    @Test fun `Portuguese cleanup covers mixed champion, item and tactical text`() {
        val mixed = listOf(
            "Asesinos com combos all-in (Diana, Akali)",
            "Jogo Medio/Tardío: Empuja sua linha, lembra de agrupa e flanquea com o time",
            "Dragones e Heraldo aparecem no rio; Muévete pelos pasillos",
            "Acierta Invocación Estelar e bloquea automáticamente o inimigo estuneado",
            "A habilidade corriente inflige danos mágico por cada inimigo"
        )
        mixed.forEach { value ->
            val translated = catalog.translate("pt", value)
            assertFalse("Spanish residue: $translated", spanishResidue.containsMatchIn(translated))
        }
        assertEquals("Assassinos", catalog.translate("pt", "Asesinos"))
        assertEquals("Dragões", catalog.translate("pt", "Dragones"))
        assertEquals("Dragones", catalog.translate("es", "Dragones"))
    }

    @Test fun `runtime amounts session messages and build controls use Portuguese`() {
        assertEquals("Conselho do coach", catalog.translate("pt", "Consejo del coach"))
        assertEquals("Categorias globais atualizadas (3/3 fontes)", catalog.translate("pt", "Categorías globales actualizadas (3/3 fuentes)"))
        assertEquals("Assinatura cancelada com sucesso", catalog.translate("pt", "Suscripción cancelada correctamente"))
        assertEquals("3. Itens principais (2/3) *Descrição obrigatória", catalog.translate("pt", "3. Objetos Core (2/3) *Desc. Obligatoria"))
        assertEquals("Erro ao abrir link: teste", catalog.translate("pt", "Error al abrir enlace: teste"))
        assertEquals("Diego_42: GG Volibear!", catalog.translate("pt", "Diego_42: GG Volibear!"))
        // Corrections belong to known app phrases; player messages keep their wording.
        val playerMessage = "Diego: activa la ayuda, imprescindible para jugar."
        assertEquals(playerMessage, catalog.translate("pt", playerMessage))
    }

    @Test fun `hub media profile and runtime status labels use Portuguese`() {
        val examples = mapOf(
            "Volver al hub" to "Voltar ao hub",
            "IMAGEN" to "IMAGEM",
            "Reproducir" to "Reproduzir",
            "Perfil de Invocador" to "Perfil do Invocador",
            "Descripción" to "Descrição",
            "Revocación" to "Revogação",
            "Cantidad" to "Quantidade",
            "Disponible" to "Disponível",
            "EN COLA" to "NA FILA",
            "PROCESANDO" to "PROCESSANDO",
            "Arriba" to "Acima",
            "Izquierda" to "Esquerda",
            "Back" to "Voltar",
            "Close" to "Fechar"
        )
        examples.forEach { (source, expected) -> assertEquals(source, expected, catalog.translate("pt", source)) }
        assertEquals("Estado: EXCLUÍDO", catalog.translate("pt", "Estado: ELIMINADO"))
        assertEquals("Estado: APROVADA", catalog.translate("pt", "Estado: APROBADA"))
        assertEquals("Estado: RESOLVIDO", catalog.translate("pt", "Estado: RESUELTO"))
        assertEquals("Estado: ELIMINADO", catalog.translate("es", "Estado: ELIMINADO"))
        val subscription = catalog.translate("pt", "Necesitas al menos 15 de Esencia Naranja para suscribirte.")
        assertTrue(subscription, subscription.contains("15 de Essência Laranja"))
        assertFalse(subscription, subscription.contains("Esencia Naranja"))
    }
}

private val spanishResidue = SpanishUiResidue.pattern
