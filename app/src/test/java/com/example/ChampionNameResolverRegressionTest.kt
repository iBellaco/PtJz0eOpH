package com.example

import com.example.data.WildRiftRepository
import com.example.service.screen.ChampionNameResolver
import org.junit.Assert.assertEquals
import org.junit.Test

class ChampionNameResolverRegressionTest {
    @Test
    fun shortViOcrVariantsResolveToVi() {
        val champions = WildRiftRepository.champions

        assertEquals("vi", ChampionNameResolver.findChampionInText("Vi", champions)?.id)
        assertEquals("vi", ChampionNameResolver.findChampionInText("vl", champions)?.id)
        assertEquals("vi", ChampionNameResolver.findChampionInText("v1", champions)?.id)
    }
}
