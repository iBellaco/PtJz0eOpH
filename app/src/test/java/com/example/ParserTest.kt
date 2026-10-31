package com.example

import org.junit.Test
import kotlinx.serialization.json.Json
import java.io.File
import com.example.model.Champion

class ParserTest {
    @Test
    fun testParse() {
        val text = File("src/main/res/raw/champions_part1.json").readText()
        val format = Json { ignoreUnknownKeys = true }
        try {
            val list = format.decodeFromString<List<Champion>>(text)
            println("SUCCESS, size = " + list.size)
        } catch (e: Exception) {
            println("FAILURE: " + e.message)
            e.printStackTrace()
        }
    }
}
