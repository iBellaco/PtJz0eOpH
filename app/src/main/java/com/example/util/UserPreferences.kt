package com.example.util

import android.content.Context
import com.example.model.LaneRole

object UserPreferences {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_MAIN_ROLE = "saved_main_role"
    private const val KEY_SECOND_ROLE = "saved_second_role"
    private const val KEY_AUTOFILL_ROLE = "saved_autofill_role"
    private const val KEY_ACTIVE_DRAFT_ROLE = "saved_active_draft_role"

    fun getMainRole(context: Context): LaneRole {
        val prefs = com.example.util.AppSecurityManager.getEncryptedSharedPreferences(context, PREFS_NAME + "_enc")
        val raw = prefs.getString(KEY_MAIN_ROLE, LaneRole.MID.name) ?: LaneRole.MID.name
        return try {
            LaneRole.valueOf(raw)
        } catch (e: Exception) {
            LaneRole.MID
        }
    }

    fun setMainRole(context: Context, role: LaneRole) {
        val prefs = com.example.util.AppSecurityManager.getEncryptedSharedPreferences(context, PREFS_NAME + "_enc")
        prefs.edit().putString(KEY_MAIN_ROLE, role.name).apply()
    }

    fun getSecondRole(context: Context): LaneRole {
        val prefs = com.example.util.AppSecurityManager.getEncryptedSharedPreferences(context, PREFS_NAME + "_enc")
        val raw = prefs.getString(KEY_SECOND_ROLE, LaneRole.ADC.name) ?: LaneRole.ADC.name
        return try {
            LaneRole.valueOf(raw)
        } catch (e: Exception) {
            LaneRole.ADC
        }
    }

    fun setSecondRole(context: Context, role: LaneRole) {
        val prefs = com.example.util.AppSecurityManager.getEncryptedSharedPreferences(context, PREFS_NAME + "_enc")
        prefs.edit().putString(KEY_SECOND_ROLE, role.name).apply()
    }

    fun getAutofillRole(context: Context): LaneRole {
        val prefs = com.example.util.AppSecurityManager.getEncryptedSharedPreferences(context, PREFS_NAME + "_enc")
        val raw = prefs.getString(KEY_AUTOFILL_ROLE, LaneRole.SUPPORT.name) ?: LaneRole.SUPPORT.name
        return try {
            LaneRole.valueOf(raw)
        } catch (e: Exception) {
            LaneRole.SUPPORT
        }
    }

    fun setAutofillRole(context: Context, role: LaneRole) {
        val prefs = com.example.util.AppSecurityManager.getEncryptedSharedPreferences(context, PREFS_NAME + "_enc")
        prefs.edit().putString(KEY_AUTOFILL_ROLE, role.name).apply()
    }

    fun getActiveDraftRole(context: Context): LaneRole {
        val prefs = com.example.util.AppSecurityManager.getEncryptedSharedPreferences(context, PREFS_NAME + "_enc")
        val raw = prefs.getString(KEY_ACTIVE_DRAFT_ROLE, null)
        return if (raw != null) {
            try {
                LaneRole.valueOf(raw)
            } catch (e: Exception) {
                getMainRole(context)
            }
        } else {
            getMainRole(context)
        }
    }

    fun setActiveDraftRole(context: Context, role: LaneRole) {
        val prefs = com.example.util.AppSecurityManager.getEncryptedSharedPreferences(context, PREFS_NAME + "_enc")
        prefs.edit().putString(KEY_ACTIVE_DRAFT_ROLE, role.name).apply()
    }
}
