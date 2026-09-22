package com.example.data

import com.example.model.WildRiftItem

object WildRiftItemsData {
    val items = emptyList<WildRiftItem>()

    val list: List<WildRiftItem> get() = items

    val categories = listOf("Luchador", "Asesino", "Tirador", "Mágico", "Defensa", "Apoyo", "Botas")

    fun getItemById(id: String): WildRiftItem? {
        return items.find { it.id == id }
    }

    fun getItemByName(name: String): WildRiftItem? {
        val s = normalize(name)
        return items.find { normalize(it.name) == s || normalize(it.nameEn) == s }
    }

    fun getItemIconByName(name: String): String {
        return getItemByName(name)?.iconUrl ?: ""
    }

    fun getItemsByCategory(category: String): List<WildRiftItem> {
        if (category.equals("Todos", ignoreCase = true)) return items
        return items.filter { it.category.equals(category, ignoreCase = true) }
    }

    private fun normalize(str: String): String {
        var s = str.lowercase()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("ñ", "n")
            .trim()
        val articles = listOf("el ", "la ", "los ", "las ", "the ", "un ", "una ", "unos ", "unas ")
        for (a in articles) {
            if (s.startsWith(a)) {
                s = s.substring(a.length).trim()
            }
        }
        return s
    }
}
