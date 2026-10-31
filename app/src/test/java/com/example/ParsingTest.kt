package com.example

import org.junit.Test
import java.io.File
import kotlinx.serialization.json.Json
import com.example.model.Champion

class ParsingTest {
    @Test
    fun testParse() {
        val file = File("src/main/assets/champions.json").let {
            if (it.exists()) it else File("app/src/main/assets/champions.json")
        }
        if (!file.exists()) {
            println("champions.json not present; skipping asset parsing test")
            return
        }
        val jsonString = file.readText()
        val format = Json { ignoreUnknownKeys = true }
        try {
            val champions = format.decodeFromString<List<Champion>>(jsonString)
            println("Parsed ${champions.size} champions!")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
