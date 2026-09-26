package com.example.data.supabase

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import kotlinx.serialization.json.Json
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Gestor dinámico del cliente de Supabase.
 * Permite usar las claves por defecto de BuildConfig/Secrets o configurar
 * credenciales personalizadas desde el Panel de Administrador.
 */
object SupabaseClientManager {

    private const val TAG = "SupabaseClientManager"
    private const val PREFS_NAME = "supabase_config_prefs"
    private const val KEY_CUSTOM_URL = "custom_supabase_url"
    private const val KEY_CUSTOM_KEY = "custom_supabase_key"

    @Volatile
    private var customUrl: String? = null
    @Volatile
    private var customKey: String? = null
    @Volatile
    private var cachedClient: SupabaseClient? = null

    fun init(context: Context) {
        val prefs = getEncryptedPrefs(context)
        customUrl = prefs.getString(KEY_CUSTOM_URL, null)
        customKey = prefs.getString(KEY_CUSTOM_KEY, null)
    }

    private fun getEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = androidx.security.crypto.MasterKey.Builder(context)
            .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
            .build()
        return androidx.security.crypto.EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getActiveUrl(): String {
        return customUrl?.takeIf { it.isNotBlank() }
            ?: BuildConfig.SUPABASE_URL.takeIf { it.isNotBlank() }
            ?: ""
    }

    fun getActiveKey(): String {
        return customKey?.takeIf { it.isNotBlank() }
            ?: BuildConfig.SUPABASE_ANON_KEY.takeIf { it.isNotBlank() }
            ?: ""
    }

    fun isUsingCustomCredentials(): Boolean {
        return !customUrl.isNullOrBlank() && !customKey.isNullOrBlank()
    }

    fun saveCustomCredentials(context: Context, url: String, key: String) {
        val cleanUrl = url.trim().removeSuffix("/")
        val cleanKey = key.trim()
        
        if (cleanKey.contains("service_role") || cleanKey.contains("secret") || (!cleanKey.startsWith("eyJ") && !cleanKey.startsWith("sb_publishable_"))) {
            Log.e(TAG, "Rechazada service_role key en el cliente.")
            return
        }

        val prefs = getEncryptedPrefs(context)
        prefs.edit()
            .putString(KEY_CUSTOM_URL, cleanUrl)
            .putString(KEY_CUSTOM_KEY, cleanKey)
            .apply()
        customUrl = cleanUrl
        customKey = cleanKey
        cachedClient = null // Forzar recreación del cliente
    }

    fun resetToDefaultCredentials(context: Context) {
        val prefs = getEncryptedPrefs(context)
        prefs.edit().clear().apply()
        customUrl = null
        customKey = null
        cachedClient = null
    }

    val client: SupabaseClient
        get() {
            cachedClient?.let { return it }
            synchronized(this) {
                cachedClient?.let { return it }
                val url = getActiveUrl()
                val key = getActiveKey()
                Log.d(TAG, "Inicializando cliente de Supabase con URL: $url")
                val newClient = createSupabaseClient(
                    supabaseUrl = url,
                    supabaseKey = key
                ) {
                    install(Postgrest)
                    install(Auth)
                    defaultSerializer = KotlinXSerializer(Json {
                        ignoreUnknownKeys = true
                        encodeDefaults = true
                    })
                }
                cachedClient = newClient
                return newClient
            }
        }

    /**
     * Prueba la conexión realizando una consulta mínima a Supabase.
     */
        suspend fun fetchCurrentPatchVersion(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val patches = client.postgrest.from("wr_patches").select().decodeList<com.example.data.supabase.model.WrPatchDto>()
            if (patches.isNotEmpty()) {
                val latestPatch = patches.first().version
                com.example.data.WildRiftRepository.CURRENT_PATCH_VERSION = latestPatch
                Result.success(latestPatch)
            } else {
                Result.failure(Exception("No se encontraron parches en la base de datos."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo la versión del parche de Supabase: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Se asume que existe la tabla feedbacks
            val count = client.postgrest.from("feedbacks").select().data
            Result.success("Conexión exitosa. Se pudo conectar al panel de reportes.")
        } catch (e: Exception) {
            Log.e(TAG, "Error probando conexión a Supabase: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Retorna el script SQL oficial para que el usuario pueda crearlo con 1 clic en Supabase SQL Editor.
     */
        fun getSupabaseSqlSchema(): String {
        return """
-- =========================================================
-- ESQUEMA OFICIAL SUPABASE PARA WILD RIFT APP (REPORTES Y PARCHE)
-- =========================================================

-- 1. TABLA DE CONTROL DE PARCHES
CREATE TABLE IF NOT EXISTS public.wr_patches (
    id TEXT PRIMARY KEY DEFAULT 'current',
    version TEXT NOT NULL,
    notes TEXT DEFAULT '',
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. TABLA DE REPORTES / SUGERENCIAS
CREATE TABLE IF NOT EXISTS public.feedbacks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    type TEXT NOT NULL,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    app_version TEXT,
    device_info TEXT,
    status TEXT DEFAULT 'PENDING',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. HABILITAR SEGURIDAD (RLS) ESTRICTA
ALTER TABLE public.wr_patches ENABLE ROW LEVEL SECURITY;
-- Solo lectura pública para los parches (la actualización se hace manual desde el panel de Supabase)
CREATE POLICY "Allow public read wr_patches" ON public.wr_patches FOR SELECT USING (true);

ALTER TABLE public.feedbacks ENABLE ROW LEVEL SECURITY;
-- Los usuarios solo pueden insertar nuevos reportes, nunca leer los de otros ni modificarlos
CREATE POLICY "Allow public insert feedbacks" ON public.feedbacks FOR INSERT WITH CHECK (true);
        """.trimIndent()
    }
}
