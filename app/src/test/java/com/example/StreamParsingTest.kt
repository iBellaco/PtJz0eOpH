package com.example

import org.junit.Test
import java.io.File
import kotlinx.serialization.json.Json
import com.example.model.Champion
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.ExperimentalSerializationApi

class StreamParsingTest {
    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testParse() {
        val file = File("src/main/assets/champions.json").let {
            if (it.exists()) it else File("app/src/main/assets/champions.json")
        }
        if (!file.exists()) {
            println("champions.json not present; skipping stream parsing test")
            return
        }
        val stream = file.inputStream()
        val format = Json { ignoreUnknownKeys = true }
        try {
            val champions = format.decodeFromStream<List<Champion>>(stream)
            println("Parsed ${champions.size} champions!")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
