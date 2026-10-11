package com.example

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import com.example.data.WildRiftRepository
import com.example.service.screen.BannedPortraitScanner
import com.example.service.screen.LiteRTVisionClassifier
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BannedPortraitScannerTest {
    private val context get() = RuntimeEnvironment.getApplication()
    @Before fun prepare() { WildRiftRepository.initChampions(context, forceReload = true) }

    @Test fun automaticBansAreRestrictedToAdministratorSessions() {
        assertTrue(com.example.service.screen.BanDetectionAccess.canDetect("admin", false))
        assertTrue(com.example.service.screen.BanDetectionAccess.canDetect("free", true))
        for (role in listOf("free", "premium", "moderador", "streamer")) {
            assertFalse(com.example.service.screen.BanDetectionAccess.canDetect(role, false))
        }
    }

    @Test fun videoHeaderPortraitsConfirmWithoutChangingTenthPickAndResetForNextDraft() {
        val bitmap = javaClass.classLoader!!.getResourceAsStream("bans/poco-header.jpg")!!.use { BitmapFactory.decodeStream(it)!! }
        val scanner = BannedPortraitScanner()
        val report = LiteRTVisionClassifier.reportFlow.value
        assertTrue(scanner.scan(bitmap, context, 0).isEmpty())
        val bans = scanner.scan(bitmap, context, 1500)
        assertTrue(bans.toString(), bans.containsAll(setOf("camille", "vladimir", "karma", "lee_sin", "rammus", "ahri", "lillia", "taliyah")))
        assertEquals(report, LiteRTVisionClassifier.reportFlow.value)
        scanner.reset()
        assertTrue(scanner.scan(bitmap, context, 3000).isEmpty())
        bitmap.recycle()
    }

    @Test fun blankFramesAndOurOverlayCannotCreateBans() {
        val bitmap = javaClass.classLoader!!.getResourceAsStream("bans/poco-header.jpg")!!.use { BitmapFactory.decodeStream(it)!! }
        val scanner = BannedPortraitScanner()
        val mask = listOf(Rect(0, 0, bitmap.width, bitmap.height))
        assertTrue(scanner.scan(bitmap, context, 0, mask).isEmpty())
        assertTrue(scanner.scan(bitmap, context, 1500, mask).isEmpty())
        bitmap.recycle()
        val blank = Bitmap.createBitmap(1280, 720, Bitmap.Config.ARGB_8888)
        assertTrue(scanner.scan(blank, context, 3000).isEmpty())
        assertTrue(scanner.scan(blank, context, 4500).isEmpty())
        blank.recycle()
    }

    @Test fun portraitRowsAdaptToSixteenByNineAndTwentyByNine() {
        for ((width, height) in listOf(1280 to 720, 1600 to 720)) {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            val radius = (height * .03f).toInt()
            val ids = listOf("ahri", "sona", "rammus", "senna")
            for ((index, id) in ids.withIndex()) {
                val portrait = context.assets.open("champions/$id.png").use { BitmapFactory.decodeStream(it)!! }
                val x = if (index < 2) width * (.08f + index * .07f) else width * (.85f + (index - 2) * .07f)
                val y = height * .052f
                canvas.drawBitmap(portrait, null, android.graphics.RectF(x-radius,y-radius,x+radius,y+radius), null)
                portrait.recycle()
            }
            val scanner = BannedPortraitScanner()
            assertTrue(scanner.scan(bitmap, context, 0).isEmpty())
            val bans = scanner.scan(bitmap, context, 1500)
            assertEquals("$width x $height: $bans", ids.toSet(), bans)
            bitmap.recycle()
        }
    }

    @Test fun bannedChampionsNeverAppearAsRecommendationsIncludingWhenAllChoicesAreBanned() {
        val initial = WildRiftRepository.analyzeDraft(myRole = com.example.model.LaneRole.MID,
            allies = emptyList(), enemies = emptyList())
        assertNotNull(initial.bestOverallPick)
        val banned = initial.bestOverallPick!!.champion.id
        val result = WildRiftRepository.analyzeDraft(myRole = com.example.model.LaneRole.MID,
            allies = emptyList(), enemies = emptyList(), bannedChampionIds = setOf(banned))
        assertFalse(result.recommendations.any { it.champion.id == banned })
        assertNotEquals(banned, result.bestOverallPick?.champion?.id)
        val empty = WildRiftRepository.analyzeDraft(myRole = com.example.model.LaneRole.MID,
            allies = emptyList(), enemies = emptyList(), bannedChampionIds = WildRiftRepository.champions.map { it.id }.toSet())
        assertTrue(empty.recommendations.isEmpty())
        assertNull(empty.bestOverallPick)
        assertNull(empty.directCounterBestPick)
    }
}
