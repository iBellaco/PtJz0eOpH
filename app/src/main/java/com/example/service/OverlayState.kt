package com.example.service

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.model.Champion
import com.example.model.LaneRole

class OverlayState {
    var isExpanded by androidx.compose.runtime.mutableStateOf(false)
    var overlayHubTab by androidx.compose.runtime.mutableStateOf(OverlayHubTab.DRAFT)
    var showSaveDraftDialog by androidx.compose.runtime.mutableStateOf(false)
    var showRoleChangeDialog by androidx.compose.runtime.mutableStateOf(false)
    var isSavedRecently by androidx.compose.runtime.mutableStateOf(false)
    var activeRole by androidx.compose.runtime.mutableStateOf(LaneRole.MID)
    var isRoleManuallySelected by androidx.compose.runtime.mutableStateOf(false)
    var isFirstPick by androidx.compose.runtime.mutableStateOf(false)
    var isFirstPickManuallySelected by androidx.compose.runtime.mutableStateOf(false)
    var isLegendaryQueue by androidx.compose.runtime.mutableStateOf(false)
    var isCompactBubble by androidx.compose.runtime.mutableStateOf(false)
    var isScanning by androidx.compose.runtime.mutableStateOf(false)
    var autoScanEnabled by androidx.compose.runtime.mutableStateOf(false)
    var scanNoticeMessage by androidx.compose.runtime.mutableStateOf<String?>(null)
    var isDraggingBubble by androidx.compose.runtime.mutableStateOf(false)
    var dragAccumulatedY by androidx.compose.runtime.mutableFloatStateOf(0f)
    var isNearCloseThreshold by androidx.compose.runtime.mutableStateOf(false)
    var selectedChampionDetail by androidx.compose.runtime.mutableStateOf<com.example.model.Champion?>(null)
    var showChampionPickerForSlot by androidx.compose.runtime.mutableStateOf<Pair<Boolean, Int>?>(null)
    var isLoadingScreenMode by androidx.compose.runtime.mutableStateOf(false)
    var isOverlayTabsMinimized by androidx.compose.runtime.mutableStateOf(false)
    val allies = androidx.compose.runtime.mutableStateListOf<com.example.model.Champion?>().apply { repeat(5) { add(null) } }
    val enemies = androidx.compose.runtime.mutableStateListOf<com.example.model.Champion?>().apply { repeat(5) { add(null) } }
    val enemyConfidences = androidx.compose.runtime.mutableStateMapOf<LaneRole, Int>()
    val manualLockedAllySlots = androidx.compose.runtime.mutableStateMapOf<Int, Boolean>()
    val manualLockedEnemySlots = androidx.compose.runtime.mutableStateMapOf<Int, Boolean>()
    val allySummonerNames = androidx.compose.runtime.mutableStateMapOf<Int, String>()
    val enemySummonerNames = androidx.compose.runtime.mutableStateMapOf<Int, String>()
    val allySpells = androidx.compose.runtime.mutableStateMapOf<Int, List<String>>()
    val enemySpells = androidx.compose.runtime.mutableStateMapOf<Int, List<String>>()
}
