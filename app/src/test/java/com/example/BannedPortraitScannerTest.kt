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

    @Test fun marginalCropScoreCannotReplaceAnAcceptedBanIdentity() {
        assertFalse(com.example.service.screen.isClearlyStrongerBan(.8514074f, .83980703f))
        assertFalse(com.example.service.screen.isClearlyStrongerBan(.89f, .81f))
        assertFalse(com.example.service.screen.isClearlyStrongerBan(.94f, .92f))
        assertTrue(com.example.service.screen.isClearlyStrongerBan(.94f, .81f))
    }

    @Test fun fiveObservedCirclesKeepTheirMeasuredPositionsInsteadOfAnEvenlySpacedGrid() {
        val scanner = BannedPortraitScanner()
        val ids = listOf("camille", "vladimir", "karma", "lee_sin", "rammus")
        val centers = listOf(.07578125f, .11171875f, .14609376f, .19765624f, .23203126f)
        val observations = ids.mapIndexed { index, id ->
            com.example.service.screen.BanObservation(com.example.service.screen.BanSide.ALLY,
                com.example.service.screen.BanRegion(centers[index], .052f, .03f), id, similarity = .92f)
        }
        // An incomplete first frame may establish a provisional grid. A full row
        // must replace that geometry without dropping the wider-spaced last pair.
        scanner.update(observations.take(3), 1280f / 579f)
        repeat(2) { scanner.update(observations, 1280f / 579f) }
        val allied = scanner.slots.filter { it.side == com.example.service.screen.BanSide.ALLY }
        assertEquals(ids, allied.map { it.championId })
        assertEquals(centers, allied.map { it.region!!.x })
        scanner.update(emptyList(), 1280f / 579f)
        assertEquals(ids, scanner.slots.take(5).map { it.championId })
    }

    @Test fun strongerMelEvidenceCorrectsWeakSionOnlyAfterTwoNewFrames() {
        val scanner = BannedPortraitScanner()
        val region = com.example.service.screen.BanRegion(.9f, .05f, .03f)
        val wrong = com.example.service.screen.BanObservation(com.example.service.screen.BanSide.ENEMY,
            region, "sion", similarity = .81f)
        repeat(2) { scanner.update(listOf(wrong), 2f) }
        assertEquals("sion", scanner.slots.first { it.championId != null }.championId)
        val correct = wrong.copy(championId = "mel", similarity = .94f)
        scanner.update(listOf(correct), 2f)
        assertTrue(scanner.slots.any { it.championId == "sion" })
        scanner.update(listOf(correct), 2f)
        assertTrue(scanner.slots.any { it.championId == "mel" })
        repeat(3) { scanner.update(listOf(wrong), 2f) }
        assertFalse(scanner.slots.any { it.championId == "sion" })
        assertEquals(10, scanner.slots.size)
    }

    @Test fun anAlreadyConfirmedPickCannotRemainInTheBanList() {
        val scanner = BannedPortraitScanner()
        val observation = com.example.service.screen.BanObservation(com.example.service.screen.BanSide.ALLY,
            com.example.service.screen.BanRegion(.1f, .05f, .03f), "sion", similarity = .83f)
        repeat(2) { scanner.update(listOf(observation), 2f) }
        val bitmap = Bitmap.createBitmap(1280, 576, Bitmap.Config.ARGB_8888)
        try {
            assertFalse("Selected identities invalidate stale bans",
                "sion" in scanner.scan(bitmap, context, 0, listOf(Rect(0, 0, 1280, 576)), pickedIds = setOf("sion")))
        } finally { bitmap.recycle() }
    }

    @Test fun syntheticMelAndDariusHeaderIsNotMisidentifiedAsSionAcrossCaptureScales() {
        // Constructed from local champion assets: this is not the tester's screenshot.
        val expected = listOf("caitlyn", "master_yi", "nocturne", "jax", "samira",
            "rammus", "darius", "varus", "lulu", "mel")
        val original = Bitmap.createBitmap(1280, 576, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(original)
        canvas.drawColor(android.graphics.Color.rgb(9, 15, 30))
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
        expected.forEachIndexed { index, id ->
            val cx = if (index < 5) 96f + index * 46f else 995f + (index - 5) * 46f
            val cy = 30f
            paint.color = android.graphics.Color.rgb(120, 45, 65)
            canvas.drawCircle(cx, cy, 20f, paint)
            val portrait = context.assets.open("champions/$id.png").use { BitmapFactory.decodeStream(it)!! }
            try {
                canvas.save()
                canvas.clipPath(android.graphics.Path().apply { addCircle(cx, cy, 18f, android.graphics.Path.Direction.CW) })
                canvas.drawBitmap(portrait, null, android.graphics.RectF(cx - 18, cy - 18, cx + 18, cy + 18), paint)
                canvas.restore()
            } finally { portrait.recycle() }
        }
        try { for (scale in listOf(1f, 1.5f, 2f)) {
            val bitmap = Bitmap.createScaledBitmap(original, (1280 * scale).toInt(), (576 * scale).toInt(), true)
            try {
                val scanner = BannedPortraitScanner()
                scanner.scan(bitmap, context, 0)
                scanner.scan(bitmap, context, 1500)
                assertEquals("Synthetic capture at scale $scale", expected, scanner.slots.map { it.championId })
            } finally { if (bitmap !== original) bitmap.recycle() }
        } } finally { original.recycle() }
    }

    @Test fun aTexturedNeighbourCannotDisplaceTheFifthVisibleRivalBan() {
        val observations = listOf("lee_sin", "mordekaiser", "syndra", "cho_gath", "hwei").mapIndexed { index, id ->
            com.example.service.screen.BanObservation(com.example.service.screen.BanSide.ENEMY,
                com.example.service.screen.BanRegion(.714f + index * .05f, .052f, .03f), id)
        }
        val positions = com.example.service.screen.fillHeaderPositions(observations, 1.6f) { region ->
            kotlin.math.abs(region.x - .964f) < .005f
        }
        assertEquals(5, positions.size)
        assertEquals("mordekaiser", positions.first().championId)
        assertEquals(.964f, positions.last().region.x, .005f)
        assertFalse(positions.any { it.championId == "lee_sin" })
    }

    @Test fun tester8067UsesTheFiveActualBansAndNeverBackgroundCaitlyn() {
        val path = listOf(java.io.File("src/androidTest/assets/draft/poco-8067-header.json"),
            java.io.File("app/src/androidTest/assets/draft/poco-8067-header.json")).first { it.isFile }
        val bytes = android.util.Base64.decode(org.json.JSONObject(path.readText()).getString("pngBase64"),android.util.Base64.DEFAULT)
        val original = BitmapFactory.decodeByteArray(bytes,0,bytes.size)!!
        val expected = listOf("lulu","senna","morgana","nocturne","hwei",
            "lee_sin","cho_gath","senna","kassadin","yunara")
        try { for(scale in listOf(1f,1.5f)) {
            val bitmap = if(scale==1f) original else Bitmap.createScaledBitmap(original,
                (original.width*scale).toInt(),(original.height*scale).toInt(),true)
            try {
                val scanner = BannedPortraitScanner()
                if(scale==1f) {
                    val emptyPhase = LiteRTVisionClassifier.findHeaderBanObservations(bitmap,context,emptyList())
                        .map { it.copy(championId=null,empty=true) }
                    repeat(2) { scanner.update(emptyPhase,bitmap.width.toFloat()/bitmap.height) }
                    assertTrue("Empty ban slots can still be filled",scanner.slots.all {
                        it.state==com.example.service.screen.BanState.EMPTY })
                }
                scanner.scan(bitmap,context,0)
                scanner.scan(bitmap,context,1500)
                assertEquals("Actual circles at scale $scale",expected,scanner.slots.map { it.championId })
                assertFalse(scanner.slots.any { it.championId=="caitlyn" })
                assertTrue(scanner.slots.all { it.state==com.example.service.screen.BanState.CONFIRMED })
            } finally { if(bitmap!==original) bitmap.recycle() }
        } } finally { original.recycle() }
    }

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

    @Test fun physicalDisplayMasksAreConvertedBeforeRecognizingBansOnTheReducedFrame() {
        val vision = com.example.service.screen.DraftVisionScanner
        val previousOverlay = vision.overlayRect
        val previousMasks = vision.debugTextRects
        try { for (name in listOf("7448", "7449")) {
            val original = javaClass.classLoader!!.getResourceAsStream("bans/live-$name.png")!!.use { BitmapFactory.decodeStream(it)!! }
            val bitmap = Bitmap.createScaledBitmap(original, 1920, 868, true)
            try {
                vision.publishScreenOverlay(Rect(1029,29,1974,1000),2688,1216)
                vision.publishScreenDiagnosticText(listOf(Rect(0,100,546,137),Rect(2100,100,2688,137)),2688,1216)
                val masks = vision.diagnosticTextForFrame(bitmap.width, bitmap.height) + listOfNotNull(vision.overlayForFrame(bitmap.width, bitmap.height))
                val scanner = BannedPortraitScanner()
                scanner.scan(bitmap, context, 0, masks)
                val ids = scanner.scan(bitmap, context, 1500, masks)
                assertTrue("$name: $ids", ids.containsAll(setOf("samira", "nocturne", "cho_gath", "hwei", "zeri", "fiora", "rammus", "blitzcrank", "vex")))
            } finally { if (bitmap !== original) bitmap.recycle(); original.recycle() }
        } } finally { vision.overlayRect = previousOverlay; vision.debugTextRects = previousMasks }
    }

    @Test fun pocoLegendaryHeaderUsesTheFiveOuterBanPositionsAndDiscardsAdjacentProfile() {
        val bitmap = javaClass.classLoader!!.getResourceAsStream("bans/poco-7457.png")!!.use { BitmapFactory.decodeStream(it)!! }
        val scanner = BannedPortraitScanner()
        try {
            scanner.scan(bitmap, context, 0)
            val ids = scanner.scan(bitmap, context, 250)
            assertTrue("Poco bans=$ids", ids.containsAll(setOf("hwei", "varus", "kayn", "nocturne", "zyra", "norra", "k_sante")))
            assertFalse("Adjacent profile must not occupy a ban", "caitlyn" in ids)
            assertEquals(listOf("hwei","varus","kayn","nocturne",null),scanner.slots.filter { it.side==com.example.service.screen.BanSide.ALLY }.sortedBy {it.index}.map {it.championId})
            assertEquals(listOf("zyra","norra","hwei","k_sante",null),scanner.slots.filter { it.side==com.example.service.screen.BanSide.ENEMY }.sortedBy {it.index}.map {it.championId})
            assertEquals("zyra",scanner.slots.first { it.side == com.example.service.screen.BanSide.ENEMY && it.index==0 }.championId)
            assertEquals(com.example.service.screen.BanState.EMPTY,scanner.slots.first { it.side == com.example.service.screen.BanSide.ENEMY && it.index==4 }.state)
        } finally { bitmap.recycle() }
    }

    @Test fun pocoResampledCapturePreservesNorraAndTheFivePositionsOnEachSide() {
        val original = javaClass.classLoader!!.getResourceAsStream("bans/poco-7457.png")!!.use { BitmapFactory.decodeStream(it)!! }
        val bitmap = android.graphics.Bitmap.createScaledBitmap(original,1920,862,true)
        val scanner = BannedPortraitScanner()
        val boxes=listOf(Rect(60,47,340,75),Rect(1010,47,1280,75),Rect(370,3,909,557))
        val masks=boxes.map { box ->
            val physical=Rect(kotlin.math.floor(box.left*2712/1280.0).toInt(),kotlin.math.floor(box.top*1220/576.0).toInt(),
                kotlin.math.ceil(box.right*2712/1280.0).toInt(),kotlin.math.ceil(box.bottom*1220/576.0).toInt())
            com.example.service.screen.CaptureCoordinateSpace.map(physical,2712,1220,1920,862)
        }
        try {
            scanner.scan(bitmap,context,0,masks)
            scanner.scan(bitmap,context,1500,masks)
            assertEquals(listOf("hwei","varus","kayn","nocturne",null),scanner.slots.filter { it.side==com.example.service.screen.BanSide.ALLY }.sortedBy {it.index}.map {it.championId})
            assertEquals(listOf("zyra","norra","hwei","k_sante",null),scanner.slots.filter { it.side==com.example.service.screen.BanSide.ENEMY }.sortedBy {it.index}.map {it.championId})
        } finally { bitmap.recycle(); original.recycle() }
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
        assertEquals(listOf("camille", "vladimir", "karma", "lee_sin", "rammus"),
            scanner.slots.take(5).map { it.championId })
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

    @Test fun confirmedBanSurvivesEmptyReplacementAndViewportUntilDraftClear() {
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
        assertEquals(com.example.service.screen.BanState.CONFIRMED,scanner.slots.first().state)
        assertEquals("ahri",scanner.slots.first().championId)
        val replacement = portrait.copy(championId="sona")
        scanner.update(listOf(replacement),2f)
        scanner.update(listOf(replacement),2f)
        assertEquals("ahri",scanner.slots.first().championId)
        scanner.update(listOf(replacement),3f)
        assertEquals("ahri",scanner.slots.first().championId)
        scanner.reset()
        assertTrue(scanner.slots.all { it.state == com.example.service.screen.BanState.UNKNOWN })
        scanner.update(listOf(replacement),3f)
        scanner.update(listOf(replacement),3f)
        assertEquals("sona",scanner.slots.first().championId)
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
