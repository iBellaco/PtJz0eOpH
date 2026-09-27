package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.util.SubscriptionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
data class BlueEssencePurchase(
    val amount: Int,
    val price: Double,
    val currency: String = "USD",
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class OrangeEssencePurchase(
    val amount: Int,
    val price: Double,
    val currency: String = "USD",
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class AccountProfile(
    val id: String,
    val name: String,
    val tag: String = "",
    val avatarId: String = "default_poro",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val blueEssence: Int = 0,
    val orangeEssence: Int = 0,
    val purchaseHistory: List<BlueEssencePurchase> = emptyList(),
    val orangePurchaseHistory: List<OrangeEssencePurchase> = emptyList()
)

object AccountProfileManager {

    private const val PREFS_NAME = "wr_account_profiles_prefs"
    private const val KEY_PROFILES_JSON = "account_profiles_json"
    private const val KEY_ACTIVE_PROFILE_ID = "active_account_profile_id"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val _allProfiles = MutableStateFlow<List<AccountProfile>>(emptyList())
    val allProfiles: StateFlow<List<AccountProfile>> = _allProfiles.asStateFlow()

    private val _activeProfileId = MutableStateFlow<String>("default")
    val activeProfileId: StateFlow<String> = _activeProfileId.asStateFlow()

    private val _activeProfile = MutableStateFlow<AccountProfile>(
        AccountProfile("default", "Cuenta Principal", tag = "Main", isDefault = true)
    )
    val activeProfile: StateFlow<AccountProfile> = _activeProfile.asStateFlow()

    private fun updateActiveProfileInternal() {
        val currentId = _activeProfileId.value
        val found = _allProfiles.value.find { it.id == currentId }
            ?: _allProfiles.value.firstOrNull()
            ?: AccountProfile("default", "Cuenta Principal", tag = "Main", isDefault = true)
        _activeProfile.value = found
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun init(context: Context) {
        val prefs = getPrefs(context)
        val rawJson = prefs.getString(KEY_PROFILES_JSON, null)
        val defaultName = SubscriptionManager.userName.value.ifBlank { "Cuenta Principal" }

        val profiles = if (!rawJson.isNullOrBlank()) {
            try {
                json.decodeFromString<List<AccountProfile>>(rawJson)
            } catch (_: Exception) {
                listOf(AccountProfile("default", defaultName, tag = "Main", isDefault = true))
            }
        } else {
            listOf(AccountProfile("default", defaultName, tag = "Main", isDefault = true))
        }

        val savedActiveId = prefs.getString(KEY_ACTIVE_PROFILE_ID, "default") ?: "default"
        val activeExists = profiles.any { it.id == savedActiveId }

        _allProfiles.value = profiles
        _activeProfileId.value = if (activeExists) savedActiveId else profiles.firstOrNull()?.id ?: "default"
        updateActiveProfileInternal()
    }

    fun getActiveProfile(context: Context): AccountProfile {
        if (_allProfiles.value.isEmpty()) {
            init(context)
        }
        val currentId = _activeProfileId.value
        return _allProfiles.value.find { it.id == currentId }
            ?: _allProfiles.value.firstOrNull()
            ?: AccountProfile("default", "Cuenta Principal", tag = "Main", isDefault = true)
    }

    fun setActiveProfile(context: Context, profileId: String) {
        _activeProfileId.value = profileId
        getPrefs(context).edit().putString(KEY_ACTIVE_PROFILE_ID, profileId).apply()
        updateActiveProfileInternal()
    }

    fun createProfile(context: Context, name: String, tag: String = "", avatarId: String = "default_poro"): AccountProfile {
        val currentList = _allProfiles.value.toMutableList()
        val newProfile = AccountProfile(
            id = "acc_${UUID.randomUUID().toString().take(8)}",
            name = name.ifBlank { "Invocador #${currentList.size + 1}" },
            tag = tag.trim(),
            avatarId = avatarId,
            isDefault = currentList.isEmpty()
        )
        currentList.add(newProfile)
        saveProfiles(context, currentList)
        setActiveProfile(context, newProfile.id)
        return newProfile
    }

    fun updateProfile(context: Context, profileId: String, newName: String, newTag: String = "", newAvatarId: String = "default_poro") {
        val updated = _allProfiles.value.map { profile ->
            if (profile.id == profileId) {
                profile.copy(
                    name = newName.ifBlank { profile.name },
                    tag = newTag.trim(),
                    avatarId = newAvatarId
                )
            } else {
                profile
            }
        }
        saveProfiles(context, updated)
    }

    fun deleteProfile(context: Context, profileId: String) {
        if (_allProfiles.value.size <= 1) return // Do not delete the last profile
        val updated = _allProfiles.value.filterNot { it.id == profileId }
        saveProfiles(context, updated)
        if (_activeProfileId.value == profileId) {
            val fallback = updated.firstOrNull()?.id ?: "default"
            setActiveProfile(context, fallback)
        }
    }

    fun importProfiles(context: Context, profiles: List<AccountProfile>, targetActiveId: String? = null) {
        if (profiles.isNotEmpty()) {
            saveProfiles(context, profiles)
            val newActive = if (targetActiveId != null && profiles.any { it.id == targetActiveId }) {
                targetActiveId
            } else {
                profiles.first().id
            }
            setActiveProfile(context, newActive)
        }
    }

    fun buyBlueEssence(context: Context, profileId: String, amount: Int, price: Double) {
        val currentProfiles = _allProfiles.value.toMutableList()
        val index = currentProfiles.indexOfFirst { it.id == profileId }
        if (index != -1) {
            val prof = currentProfiles[index]
            val newPurchase = BlueEssencePurchase(amount, price)
            val updatedProf = prof.copy(
                blueEssence = prof.blueEssence + amount,
                purchaseHistory = prof.purchaseHistory + newPurchase
            )
            currentProfiles[index] = updatedProf
            saveProfiles(context, currentProfiles)
        }
    }

    fun buyOrangeEssence(context: Context, profileId: String, amount: Int, price: Double) {
        val currentProfiles = _allProfiles.value.toMutableList()
        val index = currentProfiles.indexOfFirst { it.id == profileId }
        if (index != -1) {
            val prof = currentProfiles[index]
            val newPurchase = OrangeEssencePurchase(amount, price)
            val updatedProf = prof.copy(
                orangeEssence = prof.orangeEssence + amount,
                orangePurchaseHistory = prof.orangePurchaseHistory + newPurchase
            )
            currentProfiles[index] = updatedProf
            saveProfiles(context, currentProfiles)
        }
    }

    fun spendBlueEssence(context: Context, profileId: String, amount: Int): Boolean {
        val currentProfiles = _allProfiles.value.toMutableList()
        val index = currentProfiles.indexOfFirst { it.id == profileId }
        if (index != -1) {
            val prof = currentProfiles[index]
            if (prof.blueEssence >= amount) {
                val updatedProf = prof.copy(
                    blueEssence = prof.blueEssence - amount
                )
                currentProfiles[index] = updatedProf
                saveProfiles(context, currentProfiles)
                return true
            }
        }
        return false
    }

    fun spendOrangeEssence(context: Context, profileId: String, amount: Int): Boolean {
        val currentProfiles = _allProfiles.value.toMutableList()
        val index = currentProfiles.indexOfFirst { it.id == profileId }
        if (index != -1) {
            val prof = currentProfiles[index]
            if (prof.orangeEssence >= amount) {
                val updatedProf = prof.copy(
                    orangeEssence = prof.orangeEssence - amount
                )
                currentProfiles[index] = updatedProf
                saveProfiles(context, currentProfiles)
                return true
            }
        }
        return false
    }

    private fun saveProfiles(context: Context, profiles: List<AccountProfile>) {
        _allProfiles.value = profiles
        val encoded = json.encodeToString(profiles)
        getPrefs(context).edit().putString(KEY_PROFILES_JSON, encoded).apply()
        updateActiveProfileInternal()
    }
}
