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

    @Test fun liveHeaderAndItsOwnCaptionMasksRecognizeAllBansAtCaptureAndPhysicalResolution() {
        for (name in listOf("live-7394", "live-7395")) {
            val original = javaClass.classLoader!!.getResourceAsStream("bans/$name.png")!!.use { BitmapFactory.decodeStream(it)!! }
            try { for (factor in listOf(1, 2)) {
                val bitmap = if (factor == 1) original else Bitmap.createScaledBitmap(original, original.width * 2, original.height * 2, true)
                try {
                    val masks = listOf(Rect(0, 48*factor, 260*factor, 65*factor), Rect(1000*factor, 48*factor, 1280*factor, 65*factor))
                    val scanner = BannedPortraitScanner()
                    scanner.scan(bitmap, context, 0, masks)
                    val ids = scanner.scan(bitmap, context, 1500, masks)
                    assertTrue("$name/$factor: $ids", ids.containsAll(setOf("samira", "nocturne", "cho_gath", "hwei", "zeri", "fiora", "rammus", "blitzcrank", "vex")))
                } finally { if (bitmap !== original) bitmap.recycle() }
            } } finally { original.recycle() }
        }
    }

    @Test fun latestReportedHeaderRecognizesAllNineRealBansAndTheEmptyPosition() {
        val bitmap = javaClass.classLoader!!.getResourceAsStream("bans/reported-7296.jpg")!!.use { BitmapFactory.decodeStream(it)!! }
        val scanner = BannedPortraitScanner()
        try {
            scanner.scan(bitmap, context, 0)
            val ids = scanner.scan(bitmap, context, 1500)
            assertTrue(ids.toString(), ids.containsAll(setOf("samira", "nocturne", "cho_gath", "hwei", "zeri", "fiora", "rammus", "blitzcrank", "vex")))
            assertEquals(com.example.service.screen.BanState.EMPTY, scanner.slots.first {
                it.side == com.example.service.screen.BanSide.ALLY && it.index == 2 }.state)
        } finally { bitmap.recycle() }
    }

    @Test fun reportedGreenPortraitIsZeriRatherThanAnEmptyBan() {
        val bitmap = javaClass.classLoader!!.getResourceAsStream("bans/zeri-header.jpg")!!.use { BitmapFactory.decodeStream(it)!! }
        val scanner = BannedPortraitScanner()
        try {
            scanner.scan(bitmap, context, 0)
            val ids = scanner.scan(bitmap, context, 1500)
            assertTrue(ids.toString(), ids.containsAll(setOf("zeri", "fiora", "rammus", "blitzcrank", "vex")))
            val first = scanner.slots.first { it.side == com.example.service.screen.BanSide.ENEMY && it.index == 0 }
            assertEquals("zeri", first.championId)
            assertEquals(com.example.service.screen.BanState.CONFIRMED, first.state)
        } finally { bitmap.recycle() }
    }

    @Test fun redMagicUnknownPortraitIsRefinedAndAllBanCirclesHaveOneDiameter() {
        val bitmap = javaClass.classLoader!!.getResourceAsStream("bans/red-magic-header.jpg")!!.use { BitmapFactory.decodeStream(it)!! }
        val scanner = BannedPortraitScanner()
        try {
            assertTrue(scanner.scan(bitmap, context, 0).isEmpty())
            val bans = scanner.scan(bitmap, context, 1500)
            assertTrue(bans.toString(), "malphite" in bans)
            assertTrue(bans.toString(), bans.containsAll(setOf("senna", "yuumi", "hwei", "darius", "blitzcrank", "cho_gath")))
            val radii = scanner.slots.mapNotNull { it.region?.radius }
            assertEquals(10, radii.size)
            assertEquals(1, radii.distinct().size)
        } finally { bitmap.recycle() }
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

    @Test fun splitHeaderRecognizesEmptyPositionAndKeepsBothTeams() {
        val bitmap = javaClass.classLoader!!.getResourceAsStream("bans/split-header.jpg")!!.use { BitmapFactory.decodeStream(it)!! }
        val scanner = BannedPortraitScanner()
        scanner.scan(bitmap, context, 0)
        scanner.scan(bitmap, context, 1500)
        assertEquals(10, scanner.slots.size)
        assertEquals(scanner.slots.toString(), com.example.service.screen.BanState.EMPTY, scanner.slots.first { it.side == com.example.service.screen.BanSide.ALLY && it.index == 1 }.state)
        assertTrue(scanner.slots.count { it.state == com.example.service.screen.BanState.CONFIRMED } >= 6)
        assertTrue(scanner.slots.filter { it.state == com.example.service.screen.BanState.CONFIRMED }.map { it.side }.toSet().size == 2)
        bitmap.recycle()
    }

    @Test fun emptyEvidenceClearsABanButAmbiguousOrHiddenFramesDoNot() {
        val scanner = BannedPortraitScanner()
        val side = com.example.service.screen.BanSide.ALLY
        val region = com.example.service.screen.BanRegion(.05f,.05f,.03f)
        val portrait = com.example.service.screen.BanObservation(side,region,"ahri")
        val empty = com.example.service.screen.BanObservation(side,region,empty=true)
        scanner.update(listOf(portrait), 2f)
        assertEquals(com.example.service.screen.BanState.PENDING,scanner.slots.first().state)
        scanner.update(listOf(portrait), 2f)
        assertEquals("ahri",scanner.slots.first().championId)
        scanner.update(emptyList(), 2f)
        assertEquals("ahri",scanner.slots.first().championId)
        scanner.update(listOf(empty), 2f)
        assertEquals("ahri",scanner.slots.first().championId)
        scanner.update(listOf(empty), 2f)
        assertEquals(com.example.service.screen.BanState.EMPTY,scanner.slots.first().state)
        assertNull(scanner.slots.first().championId)
        val replacement = portrait.copy(championId="sona")
        scanner.update(listOf(replacement),2f)
        scanner.update(listOf(replacement),2f)
        assertEquals("sona",scanner.slots.first().championId)
        scanner.reset()
        assertTrue(scanner.slots.all { it.state == com.example.service.screen.BanState.UNKNOWN })
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
