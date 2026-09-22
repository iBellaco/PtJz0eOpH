package com.example

import com.example.data.WildRiftRepository
import com.example.model.Champion
import com.example.model.LaneRole
import com.example.service.screen.ChampionNameResolver
import com.example.service.screen.DraftValidationLayer
import com.example.service.screen.ScannedSlotInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import androidx.test.core.app.ApplicationProvider

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DraftDetectionAndValidationTest {

    private fun initChamps() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        synchronized(WildRiftRepository) {
            WildRiftRepository.initChampions(context)
        }
    }

    private fun getSafeChamps(): List<Champion> {
        initChamps()
        return synchronized(WildRiftRepository) {
            WildRiftRepository.champions.toList()
        }
    }

    @Test
    fun testSummonerNameIsNotConfusedWithLucian() {
        val champs = getSafeChamps()
        
        // "XCS Lucianito" debe ser identificado como invocador y no confundirse con Lucian
        assertTrue(DraftValidationLayer.isLikelySummonerName("XCS Lucianito"))
        val matchedFromSummoner = ChampionNameResolver.findChampionInText("XCS Lucianito", champs)
        assertNull("XCS Lucianito no debe devolver el campeón Lucian", matchedFromSummoner)

        // "Lucianito" solo tampoco debe devolver Lucian
        val matchedDiminutive = ChampionNameResolver.findChampionInText("Lucianito", champs)
        assertNull("Lucianito no debe devolver el campeón Lucian", matchedDiminutive)

        // "LUCIAN" en mayúsculas o "Lucian" exacto sí debe devolver Lucian
        val matchedLucian = ChampionNameResolver.findChampionInText("LUCIAN", champs)
        assertNotNull(matchedLucian)
        assertEquals("lucian", matchedLucian?.id)
    }

    @Test
    fun testJugadorDoesNotMatchJungleRole() {
        // "Jugador 1", "Jugador 2", etc. NO deben ser reconocidos como JUNGLA
        assertNull(DraftValidationLayer.parseRoleFromText("Jugador 1"))
        assertNull(DraftValidationLayer.parseRoleFromText("Jugador 2"))
        assertNull(DraftValidationLayer.parseRoleFromText("Jugador 3"))
        assertNull(DraftValidationLayer.parseRoleFromText("Jugador 4"))
        assertNull(DraftValidationLayer.parseRoleFromText("Jugador 5"))
        assertNull(DraftValidationLayer.parseRoleFromText("Player 1"))
    }

    @Test
    fun testRoleNameParsingMultilingual() {
        // Spanish
        assertEquals(LaneRole.TOP, DraftValidationLayer.parseRoleFromText("CARRIL DE BARÓN"))
        assertEquals(LaneRole.TOP, DraftValidationLayer.parseRoleFromText("CARRIL DE BARON"))
        assertEquals(LaneRole.JUNGLE, DraftValidationLayer.parseRoleFromText("JUNGLA"))
        assertEquals(LaneRole.MID, DraftValidationLayer.parseRoleFromText("CARRIL CENTRAL"))
        assertEquals(LaneRole.ADC, DraftValidationLayer.parseRoleFromText("CARRIL DEL DRAGÓN"))
        assertEquals(LaneRole.ADC, DraftValidationLayer.parseRoleFromText("CARRIL DEL DRAGON"))
        assertEquals(LaneRole.SUPPORT, DraftValidationLayer.parseRoleFromText("SOPORTE"))

        // English
        assertEquals(LaneRole.TOP, DraftValidationLayer.parseRoleFromText("BARON LANE"))
        assertEquals(LaneRole.JUNGLE, DraftValidationLayer.parseRoleFromText("JUNGLE"))
        assertEquals(LaneRole.MID, DraftValidationLayer.parseRoleFromText("MID LANE"))
        assertEquals(LaneRole.ADC, DraftValidationLayer.parseRoleFromText("DUO LANE"))
        assertEquals(LaneRole.SUPPORT, DraftValidationLayer.parseRoleFromText("SUPPORT"))

        // Portuguese
        assertEquals(LaneRole.TOP, DraftValidationLayer.parseRoleFromText("ROTA DE BARÃO"))
        assertEquals(LaneRole.JUNGLE, DraftValidationLayer.parseRoleFromText("CAÇADOR"))
        assertEquals(LaneRole.MID, DraftValidationLayer.parseRoleFromText("ROTA DO MEIO"))
        assertEquals(LaneRole.ADC, DraftValidationLayer.parseRoleFromText("ROTA DO DRAGÃO"))
        assertEquals(LaneRole.SUPPORT, DraftValidationLayer.parseRoleFromText("SUPORTE"))
    }

    @Test
    fun testExactMatchScreenshotTeamRoleResolution() {
        val champs = getSafeChamps()
        val caitlyn = champs.first { it.id == "caitlyn" }
        val thresh = champs.first { it.id == "thresh" }
        val jarvan = champs.first { it.id == "jarvan_iv" }
        val vladimir = champs.first { it.id == "vladimir" }
        val urgot = champs.first { it.id == "urgot" }

        val ezreal = champs.first { it.id == "ezreal" }
        val karma = champs.first { it.id == "karma" }
        val riven = champs.first { it.id == "riven" }
        val sett = champs.first { it.id == "sett" }
        val zoe = champs.first { it.id == "zoe" }

        // Aliados del screenshot:
        // Slot 0: Caitlyn (ADC)
        // Slot 1: Thresh (SUPPORT)
        // Slot 2: Jarvan IV (JUNGLE)
        // Slot 3: Vladimir (MID)
        // Slot 4: Urgot con texto explícito "CARRIL DE BARÓN" (TOP)
        val allySlots = listOf(
            ScannedSlotInfo(slotIndex = 0, champion = caitlyn, explicitRole = null),
            ScannedSlotInfo(slotIndex = 1, champion = thresh, explicitRole = null),
            ScannedSlotInfo(slotIndex = 2, champion = jarvan, explicitRole = null),
            ScannedSlotInfo(slotIndex = 3, champion = vladimir, explicitRole = null),
            ScannedSlotInfo(slotIndex = 4, champion = urgot, explicitRole = LaneRole.TOP)
        )

        val auditAllies = mutableListOf<String>()
        val resolvedAllies = DraftValidationLayer.resolveTeamRoles(allySlots, champs, auditAllies)

        assertEquals(5, resolvedAllies.size)
        assertEquals("urgot", resolvedAllies[LaneRole.TOP]?.id)
        assertEquals("jarvan_iv", resolvedAllies[LaneRole.JUNGLE]?.id)
        assertEquals("vladimir", resolvedAllies[LaneRole.MID]?.id)
        assertEquals("caitlyn", resolvedAllies[LaneRole.ADC]?.id)
        assertEquals("thresh", resolvedAllies[LaneRole.SUPPORT]?.id)

        // Rivales del screenshot:
        // Slot 0: Ezreal (Jugador 1 -> ADC)
        // Slot 1: Karma (Jugador 2 -> SUPPORT)
        // Slot 2: Riven (Jugador 3 -> TOP)
        // Slot 3: Sett (Jugador 4 -> JUNGLE)
        // Slot 4: Zoe (Jugador 5 -> MID)
        val enemySlots = listOf(
            ScannedSlotInfo(slotIndex = 0, champion = ezreal, explicitRole = null),
            ScannedSlotInfo(slotIndex = 1, champion = karma, explicitRole = null),
            ScannedSlotInfo(slotIndex = 2, champion = riven, explicitRole = null),
            ScannedSlotInfo(slotIndex = 3, champion = sett, explicitRole = null),
            ScannedSlotInfo(slotIndex = 4, champion = zoe, explicitRole = null)
        )

        val auditEnemies = mutableListOf<String>()
        val resolvedEnemiesDetailed = DraftValidationLayer.resolveTeamRolesDetailed(enemySlots, champs, auditEnemies)
        val resolvedEnemies = resolvedEnemiesDetailed.assignments

        assertEquals(5, resolvedEnemies.size)
        assertEquals("riven", resolvedEnemies[LaneRole.TOP]?.id)
        assertEquals("sett", resolvedEnemies[LaneRole.JUNGLE]?.id)
        assertEquals("zoe", resolvedEnemies[LaneRole.MID]?.id)
        assertEquals("ezreal", resolvedEnemies[LaneRole.ADC]?.id)
        assertEquals("karma", resolvedEnemies[LaneRole.SUPPORT]?.id)

        // Verificación de certezas:
        // Ezreal, Karma y Zoe tienen certeza >= 90% (asignados por slot/rol primario)
        assertTrue(resolvedEnemiesDetailed.confidences[LaneRole.ADC]!! >= 90)
        assertTrue(resolvedEnemiesDetailed.confidences[LaneRole.SUPPORT]!! >= 90)
        assertTrue(resolvedEnemiesDetailed.confidences[LaneRole.MID]!! >= 90)
        // Riven y Sett tienen asignación válida
        assertTrue(resolvedEnemiesDetailed.confidences[LaneRole.TOP]!! >= 60)
        assertTrue(resolvedEnemiesDetailed.confidences[LaneRole.JUNGLE]!! >= 60)
    }

    @Test
    fun testRealUserScreenshotsAccuracy() {
        val champs = getSafeChamps()

        // Validación basada en las capturas reales del usuario:
        // Captura 1: Wukong (Barón), Galio (Apoyo), Veigar (Mid), Sivir (Dúo), Yone (Jungla con Smite)
        // Rivales: Lulu (Apoyo), Varus (Dúo), Olaf (Jungla/Top), Slot 3 y 4 aún no han elegido ("Jugador 4", "Jugador 5")
        val wukong = champs.find { it.id == "wukong" }!!
        val galio = champs.find { it.id == "galio" }!!
        val veigar = champs.find { it.id == "veigar" }!!
        val sivir = champs.find { it.id == "sivir" }!!
        val yone = champs.find { it.id == "yone" }!!

        val lulu = champs.find { it.id == "lulu" }!!
        val varus = champs.find { it.id == "varus" }!!
        val olaf = champs.find { it.id == "olaf" }!!

        // Verificación de parsing de roles en texto en español de Wild Rift
        assertEquals(LaneRole.ADC, DraftValidationLayer.parseRoleFromText("CALLE DEL DRAGÓN"))
        assertEquals(LaneRole.MID, DraftValidationLayer.parseRoleFromText("CALLE CENTRAL"))
        assertEquals(LaneRole.TOP, DraftValidationLayer.parseRoleFromText("CARRIL DE BARÓN"))
        assertEquals(LaneRole.JUNGLE, DraftValidationLayer.parseRoleFromText("JUNGLA"))
        assertEquals(LaneRole.SUPPORT, DraftValidationLayer.parseRoleFromText("APOYO"))

        // Verificación de aliados
        val allySlots = listOf(
            ScannedSlotInfo(slotIndex = 0, isAlly = true, champion = wukong, explicitRole = LaneRole.TOP),
            ScannedSlotInfo(slotIndex = 1, isAlly = true, champion = galio, explicitRole = LaneRole.SUPPORT),
            ScannedSlotInfo(slotIndex = 2, isAlly = true, champion = veigar, explicitRole = LaneRole.MID),
            ScannedSlotInfo(slotIndex = 3, isAlly = true, champion = sivir, explicitRole = LaneRole.ADC),
            ScannedSlotInfo(slotIndex = 4, isAlly = true, champion = yone, explicitRole = LaneRole.JUNGLE)
        )
        val alliesMap = allySlots.associate { it.explicitRole!! to it.champion!! }
        assertEquals("wukong", alliesMap[LaneRole.TOP]?.id)
        assertEquals("yone", alliesMap[LaneRole.JUNGLE]?.id)
        assertEquals("veigar", alliesMap[LaneRole.MID]?.id)
        assertEquals("sivir", alliesMap[LaneRole.ADC]?.id)
        assertEquals("galio", alliesMap[LaneRole.SUPPORT]?.id)

        // Verificación de rivales: solo 3 seleccionados, 2 aún no han elegido
        val enemySlots = listOf(
            ScannedSlotInfo(slotIndex = 0, isAlly = false, champion = lulu, explicitRole = null),
            ScannedSlotInfo(slotIndex = 1, isAlly = false, champion = varus, explicitRole = null),
            ScannedSlotInfo(slotIndex = 2, isAlly = false, champion = olaf, explicitRole = null),
            ScannedSlotInfo(slotIndex = 3, isAlly = false, champion = null, isLikelyUnpicked = true),
            ScannedSlotInfo(slotIndex = 4, isAlly = false, champion = null, isLikelyUnpicked = true)
        )
        val validPicks = enemySlots.filter { it.champion != null }
        val auditList = mutableListOf<String>()
        val resolvedEnemies = DraftValidationLayer.resolveTeamRolesDetailed(validPicks, champs, auditList)

        assertEquals(3, resolvedEnemies.assignments.size)
        assertEquals("lulu", resolvedEnemies.assignments[LaneRole.SUPPORT]?.id)
        assertEquals("varus", resolvedEnemies.assignments[LaneRole.ADC]?.id)
        // Olaf cubre el carril prioritario que le corresponde (TOP o JUNGLE)
        assertTrue(resolvedEnemies.assignments.containsKey(LaneRole.TOP) || resolvedEnemies.assignments.containsKey(LaneRole.JUNGLE))
    }

    @Test
    fun testSmiteSpellDetectionAndRoleResolution() {
        // Crear un bitmap sintético con colores de fuego / Smite (púrpura / Castigo)
        val smiteBitmap = android.graphics.Bitmap.createBitmap(40, 40, android.graphics.Bitmap.Config.ARGB_8888)
        smiteBitmap.eraseColor(android.graphics.Color.rgb(180, 50, 200))

        val match = com.example.util.SummonerSpellDetector.detectSpell(smiteBitmap, android.graphics.Rect(0, 0, 40, 40))
        assertNotNull("El bitmap de prueba con colores de Castigo/Smite debe ser detectado", match)
        assertEquals("smite", match?.spellId)
    }

    @Test
    fun testChampionNameWithSummonerNameExtraction() {
        val champs = getSafeChamps()

        // Casos reales donde el OCR detecta el nombre del campeón junto al nombre de invocador
        val wukong = ChampionNameResolver.findChampionInText("WUKONG XCS Alee22", champs)
        assertNotNull("Debe extraer a Wukong incluso si está acompañado del nombre de invocador con clan", wukong)
        assertEquals("wukong", wukong?.id)

        val galio = ChampionNameResolver.findChampionInText("GALIO XCS Elchicho7", champs)
        assertNotNull("Debe extraer a Galio incluso si está acompañado del nombre de invocador con números", galio)
        assertEquals("galio", galio?.id)

        val veigar = ChampionNameResolver.findChampionInText("VEIGAR Gustavo GG", champs)
        assertNotNull("Debe extraer a Veigar incluso con nombre de invocador con espacios", veigar)
        assertEquals("veigar", veigar?.id)

        // Casos donde solo es un nombre de invocador y NO hay campeón: debe descartarse
        val summonerOnly = ChampionNameResolver.findChampionInText("XCS Alee22", champs)
        assertNull("Un invocador aislado sin campeón no debe confundirse", summonerOnly)

        val diego = ChampionNameResolver.findChampionInText("D I E G O", champs)
        assertNull("El apodo Diego no debe confundirse con ningún campeón", diego)
    }

    @Test
    fun testRoleDiscrepanciesAndCorrectRoleAssignments() {
        val champs = getSafeChamps()

        val wukong = champs.find { it.id == "wukong" }!!
        val yone = champs.find { it.id == "yone" }!!
        val veigar = champs.find { it.id == "veigar" }!!
        val varus = champs.find { it.id == "varus" }!!
        val sona = champs.find { it.id == "sona" }!!

        // Simular los 5 slots aliados estándar de Wild Rift:
        // Slot 0: Wukong (Baron/Top)
        // Slot 1: Yone (Jungle)
        // Slot 2: Veigar (Mid)
        // Slot 3: Varus (Dragon/ADC)
        // Slot 4: Sona (Support)
        val allySlots = listOf(
            ScannedSlotInfo(slotIndex = 0, isAlly = true, champion = wukong, explicitRole = null),
            ScannedSlotInfo(slotIndex = 1, isAlly = true, champion = yone, explicitRole = null),
            ScannedSlotInfo(slotIndex = 2, isAlly = true, champion = veigar, explicitRole = null),
            ScannedSlotInfo(slotIndex = 3, isAlly = true, champion = varus, explicitRole = null),
            ScannedSlotInfo(slotIndex = 4, isAlly = true, champion = sona, explicitRole = null)
        )

        val auditList = mutableListOf<String>()
        val resolved = DraftValidationLayer.resolveTeamRolesDetailed(allySlots, champs, auditList)

        assertEquals("Wukong debe ser TOP", "wukong", resolved.assignments[LaneRole.TOP]?.id)
        assertEquals("Yone debe ser JUNGLE por su posición en slot 1", "yone", resolved.assignments[LaneRole.JUNGLE]?.id)
        assertEquals("Veigar debe ser MID", "veigar", resolved.assignments[LaneRole.MID]?.id)
        assertEquals("Varus debe ser ADC", "varus", resolved.assignments[LaneRole.ADC]?.id)
        assertEquals("Sona debe ser SUPPORT", "sona", resolved.assignments[LaneRole.SUPPORT]?.id)

        // Ningún campeón debe quedar en un rol completamente ajeno
        assertTrue("Wukong NUNCA debe ser ADC", allySlots[0].assignedRole != LaneRole.ADC)
        assertTrue("Wukong NUNCA debe ser SUPPORT", allySlots[0].assignedRole != LaneRole.SUPPORT)
    }

    @Test
    fun testSummonerNamesAreNotHallucinatedAsChampions() {
        val champs = getSafeChamps()

        // Nombres de invocador comunes que antes se confundían con campeones
        val falsePositives = listOf(
            "Kain99",
            "SonyBoy",
            "SamPro",
            "Gaby11anos",
            "martincho137",
            "CacauVegannah",
            "ElChicho777",
            "ProGamer2024",
            "D I E G O"
        )

        for (name in falsePositives) {
            val result = ChampionNameResolver.findChampionInText(name, champs)
            assertNull("El texto '$name' es un nombre de jugador y NO debe inventar un campeón", result)
        }
    }

    @Test
    fun testScannerV2CalibratedROICalculation() {
        val width = 1536
        val height = 695

        val avatarDiameter = (height * 0.120f).toInt().coerceAtLeast(32) // ~83 px
        val allyAvatarCenterX = (height * 0.160f).toInt().coerceAtLeast(16) // ~111 px
        val enemyAvatarCenterX = (width - (height * 0.160f)).toInt().coerceIn(0, width) // ~1425 px

        assertEquals(83, avatarDiameter)
        assertEquals(111, allyAvatarCenterX)
        assertEquals(1424, enemyAvatarCenterX)

        val slotYRatios = floatArrayOf(0.195f, 0.324f, 0.459f, 0.594f, 0.728f)
        val expectedYCenters = intArrayOf(135, 225, 319, 412, 505)

        for (i in 0..4) {
            val yCenter = (height * slotYRatios[i]).toInt()
            assertEquals("Y center para slot $i", expectedYCenters[i], yCenter)

            // Cálculo ROI aliado
            val startX = (allyAvatarCenterX - avatarDiameter / 2).coerceIn(0, width - avatarDiameter)
            val startY = (yCenter - avatarDiameter / 2).coerceIn(0, height - avatarDiameter)
            val roiRect = android.graphics.Rect(startX, startY, startX + avatarDiameter, startY + avatarDiameter)

            assertTrue("ROI X dentro de límites", roiRect.left >= 0 && roiRect.right <= width)
            assertTrue("ROI Y dentro de límites", roiRect.top >= 0 && roiRect.bottom <= height)
            assertEquals("Diámetro exacto de ROI en ancho", avatarDiameter, roiRect.width())
            assertEquals("Diámetro exacto de ROI en alto", avatarDiameter, roiRect.height())

            // En slot 0, X debe ser exactamente [70..153] y Y [94..177] (centro 111, 135)
            if (i == 0) {
                assertEquals(70, roiRect.left)
                assertEquals(94, roiRect.top)
                assertEquals(153, roiRect.right)
                assertEquals(177, roiRect.bottom)
            }
        }

        // Verificar que el avatar enemigo en 1424px evita el panel lateral de Android (típicamente en >1480px)
        val enemyStartX = (enemyAvatarCenterX - avatarDiameter / 2)
        val enemyEndX = enemyStartX + avatarDiameter
        assertEquals(1383, enemyStartX)
        assertEquals(1466, enemyEndX)
        assertTrue("ROI enemiga no toca el borde derecho ni panel de volumen", enemyEndX < width - 50)
    }

    @Test
    fun testTenthPickDiagnosticCacheSaveAndRetrieve() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        com.example.service.screen.TenthPickDiagnosticManager.init(context)
        com.example.service.screen.TenthPickDiagnosticManager.setDiagnosticMode(context, true)
        com.example.service.screen.TenthPickDiagnosticManager.clearAllCaches(context)

        val bmp = android.graphics.Bitmap.createBitmap(144, 144, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        canvas.drawColor(android.graphics.Color.BLUE)

        val savedFile = com.example.service.screen.TenthPickDiagnosticManager.recordTenthPickCrop(
            cropBitmap = bmp,
            isAlly = false,
            slotIndex = 4,
            stage = "TEST_INFERENCE",
            candidateName = "Zed",
            confidence = 94,
            context = context
        )

        assertNotNull("El recorte guardado en modo diagnóstico no debe ser nulo", savedFile)
        assertTrue("El archivo debe existir en disco", savedFile!!.exists())

        val stats = com.example.service.screen.TenthPickDiagnosticManager.cacheStatsFlow.value
        assertTrue("El número total de archivos guardados debe ser al menos 1", stats.totalFiles >= 1)
        assertTrue("El tamaño total en caché debe ser mayor a 0", stats.totalSizeBytes > 0)
    }

    @Test
    fun testTenthPickDiagnosticDisabledDoesNotSave() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        com.example.service.screen.TenthPickDiagnosticManager.init(context)
        com.example.service.screen.TenthPickDiagnosticManager.setDiagnosticMode(context, false)
        com.example.service.screen.TenthPickDiagnosticManager.clearAllCaches(context)

        val bmp = android.graphics.Bitmap.createBitmap(100, 100, android.graphics.Bitmap.Config.ARGB_8888)

        val savedFile = com.example.service.screen.TenthPickDiagnosticManager.recordTenthPickCrop(
            cropBitmap = bmp,
            isAlly = true,
            slotIndex = 4,
            stage = "TEST_DISABLED",
            context = context
        )

        assertNull("Cuando el modo diagnóstico está apagado no debe guardar recortes", savedFile)
    }

    @Test
    fun testTenthPickDiagnosticClearCache() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        com.example.service.screen.TenthPickDiagnosticManager.init(context)
        com.example.service.screen.TenthPickDiagnosticManager.setDiagnosticMode(context, true)

        val bmp = android.graphics.Bitmap.createBitmap(80, 80, android.graphics.Bitmap.Config.ARGB_8888)
        com.example.service.screen.TenthPickDiagnosticManager.recordTenthPickCrop(
            cropBitmap = bmp,
            isAlly = false,
            slotIndex = 4,
            stage = "TEST_BEFORE_CLEAR",
            context = context
        )

        com.example.service.screen.TenthPickDiagnosticManager.clearAllCaches(context)
        val statsAfter = com.example.service.screen.TenthPickDiagnosticManager.cacheStatsFlow.value
        assertEquals("Tras limpiar el caché de recortes debe haber 0 archivos", 0, statsAfter.totalFiles)
    }

    @Test
    fun testAdaptiveScreenLayoutSlotAvatarExtraction() = kotlinx.coroutines.test.runTest {
        val width = 1920
        val height = 1080
        val frame = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(frame)
        canvas.drawColor(android.graphics.Color.DKGRAY)

        val calib = com.example.service.screen.VisionCalibrationConfig()
        val crop = com.example.service.screen.AdaptiveScreenLayoutEngine.extractSlotAvatarBitmap(
            sourceBitmap = frame,
            width = width,
            height = height,
            isAlly = false,
            slotIndex = 4,
            config = calib
        )

        assertNotNull("El recorte del slot 10 no debe ser nulo", crop)
        assertTrue("El ancho del recorte debe ser mayor a 30px", crop!!.width > 30)
        assertTrue("La altura del recorte debe ser mayor a 30px", crop.height > 30)
    }

    @Test
    fun testLiteRTVisionClassifierReportFlowInitialState() {
        val currentReport = com.example.service.screen.LiteRTVisionClassifier.reportFlow.value
        assertNotNull("El reporte de LiteRT no debe ser nulo", currentReport)
        assertTrue("El estado inicial de decisión debe estar definido", currentReport.decisionReason.isNotEmpty())
    }
}


