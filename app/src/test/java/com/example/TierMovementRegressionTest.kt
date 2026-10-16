package com.example

import android.app.Application
import com.example.data.WildRiftRepository
import com.example.data.sync.MetaRegion
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class)
class TierMovementRegressionTest {
    @Test fun sAndAAreDifferentCategoriesAndUnchangedPollsKeepTheIcon() {
        WildRiftRepository.initChampions(RuntimeEnvironment.getApplication(),forceReload=true)
        fun change(now:String,old:String)=WildRiftRepository.applyRegionalTierList(MetaRegion.DEFAULT,
            mapOf("ahri" to now),previousTiers=mapOf("ahri" to old))
        change("S","A")
        assertEquals(1,WildRiftRepository.champions.first { it.id=="ahri" }.tierMovement)
        change("S","S")
        assertEquals(1,WildRiftRepository.champions.first { it.id=="ahri" }.tierMovement)
        change("A","S")
        assertEquals(-1,WildRiftRepository.champions.first { it.id=="ahri" }.tierMovement)
    }
}
