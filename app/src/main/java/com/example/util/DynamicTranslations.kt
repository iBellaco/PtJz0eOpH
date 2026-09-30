package com.example.util

import android.content.Context
import org.json.JSONObject

object DynamicTranslations {
    @Volatile private var catalog: TranslationCatalog? = null

    // Correcciones explícitas para etiquetas de UI que el catálogo histórico dejó
    // idénticas al español. Tienen prioridad para impedir mezcla ES/PT.
    private val ptUiOverrides = mapOf(
        "Diagnóstico:" to "Diagnóstico:",
        "Dispositivo:" to "Dispositivo:",
        "Runas recomendadas:" to "Runas recomendadas:",
        "ALTERNATIVAS SITUACIONALES:" to "ALTERNATIVAS SITUACIONAIS:",
        "Buscar avatar..." to "Buscar avatar...",
        "Buscar botas..." to "Buscar botas...",
        "Buscar runa..." to "Buscar runa...",
        "Buscar..." to "Buscar...",
        "Barra Inferior" to "Barra Inferior",
        "Barrera de maná" to "Barreira de mana",
        "Cofre de Artesano" to "Cofre de Artesão",
        "Compás Letal" to "Ritmo Fatal",
        "Configuradas" to "Configuradas",
        "Configurar" to "Configurar",
        "Consumo / Gasto" to "Consumo / Gasto",
        "Cálculo 1v1 Automático" to "Cálculo 1v1 Automático",
        "Cálculo Automático de Matchups 1v1" to "Cálculo Automático de Confrontos 1v1",
        "Deslizar para Minimizar" to "Deslize para Minimizar",
        "Dispositivos Conectados:" to "Dispositivos Conectados:",
        "Draft registrado como Derrota" to "Draft registrado como Derrota",
        "Editar URL vertical manualmente" to "Editar URL vertical manualmente",
        "Enviado por:" to "Enviado por:",
        "Enviado por: %s" to "Enviado por: %s",
        "Escanear" to "Escanear",
        "Expandir barra" to "Expandir barra",
        "Expandir filtros" to "Expandir filtros",
        "Expandir panel" to "Expandir painel",
        "Filtros / Buscar" to "Filtros / Buscar",
        "Habilidades" to "Habilidades",
        "Habilidades de" to "Habilidades de",
        "Idioma" to "Idioma",
        "Marcar como Favorito" to "Marcar como Favorito",
        "Minimizar panel" to "Minimizar painel",
        "Modo Claro" to "Modo Claro",
        "Mostrar barra" to "Mostrar barra",
        "Objetivos" to "Objetivos",
        "Objetivos de Mapa" to "Objetivos do Mapa",
        "Ordenar:" to "Ordenar:",
        "Perfil de Invocador" to "Perfil de Invocador",
        "Personalizar Tema" to "Personalizar Tema",
        "Precisión / Dominación" to "Precisão / Dominação",
        "Principal:" to "Principal:",
        "Recarga de Esencia Azul" to "Recarga de Essência Azul",
        "Recarga de Esencia Naranja" to "Recarga de Essência Laranja",
        "Recorte Escaneado" to "Recorte Escaneado",
        "Reiniciar Draft" to "Reiniciar Draft",
        "Reportar Bug" to "Reportar Bug",
        "Responder" to "Responder",
        "Respondido por:" to "Respondido por:",
        "Runa Principal (Keystone)" to "Runa Principal (Keystone)",
        "Servidor / Meta:" to "Servidor / Meta:",
        "Servidor Global (Meta Live)" to "Servidor Global (Meta ao Vivo)",
        "Sincronizando..." to "Sincronizando...",
        "Sincronizar" to "Sincronizar",
        "Sinergia" to "Sinergia",
        "Sinergia / Combo:" to "Sinergia / Combo:",
        "Sinergias" to "Sinergias",
        "Sinergias:" to "Sinergias:",
        "Sugerir Build" to "Sugerir Build",
        "Tipo de Partida" to "Tipo de Partida",
        "Tipo de Partida:" to "Tipo de Partida:",
        "Todas" to "Todas",
        "Todos" to "Todos",
        "Total de elementos" to "Total de elementos",
        "Tier List Global activa" to "Tier List Global ativa",
        "Tier List NA activa" to "Tier List NA ativa",
        "Meta Global" to "Meta Global",
        "Meta NA" to "Meta NA",
        "🇨🇳 China" to "🇨🇳 China",
        "🌐 Global" to "🌐 Global",
        "🇺🇸 NA" to "🇺🇸 NA"
    )

    private val esUiOverrides = mapOf(
        "Espanhol" to "Español",
        "Escolha seu idioma" to "Elige tu idioma",
        "Selecione o idioma do assistente tático" to "Selecciona el idioma del asistente táctico",
        "Continuar em Português" to "Continuar en Portugués",
        "VISÃO" to "VISIÓN",
        "Referência local" to "Referencia local",
        "Tier List Global ativa" to "Tier List Global activa",
        "Tier List NA ativa" to "Tier List NA activa",
        "Meta ao Vivo" to "Meta en vivo",
        "Painel" to "Panel"
    )
    fun load(context: Context) = loadSync(context)
    fun loadSync(context: Context) {
        if (catalog != null) return
        synchronized(this) {
            if (catalog != null) return
            try {
                fun read(name: String): Map<String, String> {
                    val json = JSONObject(context.assets.open(name).bufferedReader().use { it.readText() })
                    return json.keys().asSequence().associateWith { json.getString(it) }
                }
                catalog = TranslationCatalog(read("translations_pt.json"), read("translations_es.json"))
            } catch (e: Exception) {
                AppLogger.e("Translations", "Unable to load offline language catalog", e)
            }
        }
    }
    fun get(language: String, text: String): String? {
        if (catalog == null) com.example.WildRiftApp.instance?.let { loadSync(it) }
        val normalized = AppLanguage.normalize(language)
        if (normalized == "pt") ptUiOverrides[text]?.let { return it }
        else esUiOverrides[text]?.let { return it }
        return catalog?.translate(normalized, text)?.takeIf { it != text }
    }
}
