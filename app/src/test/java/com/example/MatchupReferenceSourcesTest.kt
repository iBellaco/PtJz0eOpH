package com.example

import android.app.Application
import com.example.data.WildRiftRepository
import com.example.data.sync.BestBuildWrScraper
import com.example.util.WebMatchupReferences
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class)
class MatchupReferenceSourcesTest {
    @Test fun fiveEditorialSourcesAreBundledAndNotPromotedToStatisticalFeeds() {
        val context=RuntimeEnvironment.getApplication()
        WildRiftRepository.initChampions(context,forceReload=true)
        val payload=context.assets.open("matchup_references.json").bufferedReader().use { JSONObject(it.readText()) }
        val sources=payload.getJSONObject("sources")
        assertEquals(setOf("bestbuildwr","wildriftfire","wildriftcore","wildriftcounter","thewildpick"),sources.keys().asSequence().toSet())
        sources.keys().forEach { id -> assertTrue("$id must contribute real relationships",sources.getJSONObject(id).getInt("withRelations")>0) }
        assertEquals(142,WebMatchupReferences.byChampion.size)
        val roster=WildRiftRepository.baseChampionsList.map { it.id }.toSet()
        WebMatchupReferences.byChampion.forEach { (id,result) ->
            assertTrue(id in roster)
            assertTrue((result.advantages+result.counters+result.synergies).all { it in roster && it!=id })
        }
        val feeds=BestBuildWrScraper.GLOBAL_SCRAPING_SOURCES.map { it.id }.toSet()
        assertEquals(setOf("wildriftcore","wildriftguides","wildriftfire"),feeds)
        assertFalse(feeds.contains("bestbuildwr"))
        assertFalse(feeds.contains("wildriftcounter"))
        assertFalse(feeds.contains("thewildpick"))
    }
}
