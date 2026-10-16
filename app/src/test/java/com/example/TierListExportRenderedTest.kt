package com.example

import android.app.Application
import com.example.data.WildRiftRepository
import com.example.ui.components.renderTierListImage
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TierListExportRenderedTest {
    @Test fun fullRosterFitsPortraitPosterWithReservedFooterAndLocalAvatars() {
        val context=RuntimeEnvironment.getApplication()
        com.example.util.AppLanguage.select(context,"es")
        WildRiftRepository.initChampions(context,forceReload=true)
        val roster=WildRiftRepository.baseChampionsList.mapIndexed { i,c ->
            c.copy(tier=listOf("S+","S","A")[i%3],tierMovement=if(i%2==0)1 else -1)
        }
        assertEquals(142,roster.size)
        roster.forEach { c -> context.assets.open(c.getLocalAvatarUri().removePrefix("file:///android_asset/")).close() }
        val bitmap=renderTierListImage(context,roster)
        try {
            assertEquals(2160,bitmap.width);assertEquals(3840,bitmap.height)
            val path=java.io.File("build/reports/portuguese-rendered/tier-list-export-es.png")
            path.parentFile!!.mkdirs()
            path.outputStream().use { assertTrue(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)) }
            assertTrue(path.length()>10000)
        } finally { bitmap.recycle() }
    }
}
