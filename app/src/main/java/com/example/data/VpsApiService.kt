package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object VpsService {
    private const val TAG = "VpsService"
    private const val PREFS_NAME = "ColorJetServerPrefs"
    private const val KEY_SERVER_URL = "vps_server_url"
    private const val KEY_SERVER_TOKEN = "vps_server_token"
    private const val KEY_SERVER_MODE = "vps_server_mode"

    // Defaults
    private const val DEFAULT_QA_URL = "https://ais-dev-bmklslqqqnkhqj75qrfrau-160697771856.asia-southeast1.run.app/backend/"
    private const val DEFAULT_PROD_URL = "http://YOUR_VPS_IP_OR_DOMAIN/backend/"
    private const val DEFAULT_TOKEN = "CJ_ERP_BD_MIGRATE_2026_SECURE_KEY"

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun getServerMode(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SERVER_MODE, "QA") ?: "QA"
    }

    fun getServerUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val mode = getServerMode(context)
        val defaultUrl = if (mode == "QA") DEFAULT_QA_URL else DEFAULT_PROD_URL
        return prefs.getString(KEY_SERVER_URL, defaultUrl) ?: defaultUrl
    }

    fun getServerToken(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SERVER_TOKEN, DEFAULT_TOKEN) ?: DEFAULT_TOKEN
    }

    fun saveServerSettings(context: Context, mode: String, url: String, token: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_SERVER_MODE, mode)
            .putString(KEY_SERVER_URL, url)
            .putString(KEY_SERVER_TOKEN, token)
            .apply()
        Log.d(TAG, "VPS settings saved. Mode: $mode, URL: $url")
    }

    /**
     * Test connection to the VPS server
     */
    suspend fun testConnection(context: Context): JSONObject = withContext(Dispatchers.IO) {
        try {
            val baseUrl = getServerUrl(context)
            val url = if (baseUrl.endsWith("/")) "${baseUrl}api.php?action=test" else "$baseUrl/api.php?action=test"
            val token = getServerToken(context)

            val request = Request.Builder()
                .url(url)
                .header("X-ColorJet-Token", token)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: "{}"
                if (response.isSuccessful) {
                    JSONObject(bodyStr)
                } else {
                    JSONObject().apply {
                        put("status", "error")
                        put("message", "HTTP ${response.code}: ${response.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "VPS connection test failed", e)
            JSONObject().apply {
                put("status", "error")
                put("message", e.localizedMessage ?: "Network error connecting to VPS")
            }
        }
    }

    /**
     * Push dataset to the VPS
     */
    suspend fun pushData(context: Context, payload: JSONObject): JSONObject = withContext(Dispatchers.IO) {
        try {
            val baseUrl = getServerUrl(context)
            val url = if (baseUrl.endsWith("/")) "${baseUrl}api.php" else "$baseUrl/api.php"
            val token = getServerToken(context)

            val requestBody = payload.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(url)
                .header("X-ColorJet-Token", token)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: "{}"
                if (response.isSuccessful) {
                    JSONObject(bodyStr)
                } else {
                    JSONObject().apply {
                        put("status", "error")
                        put("message", "HTTP ${response.code}: ${response.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "VPS pushData failed", e)
            JSONObject().apply {
                put("status", "error")
                put("message", e.localizedMessage ?: "Sync push failed")
            }
        }
    }

    /**
     * Pull dataset from the VPS
     */
    suspend fun pullData(context: Context, payload: JSONObject): JSONObject = withContext(Dispatchers.IO) {
        try {
            val baseUrl = getServerUrl(context)
            val url = if (baseUrl.endsWith("/")) "${baseUrl}api.php" else "$baseUrl/api.php"
            val token = getServerToken(context)

            val requestBody = payload.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(url)
                .header("X-ColorJet-Token", token)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: "{}"
                if (response.isSuccessful) {
                    JSONObject(bodyStr)
                } else {
                    JSONObject().apply {
                        put("status", "error")
                        put("message", "HTTP ${response.code}: ${response.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "VPS pullData failed", e)
            JSONObject().apply {
                put("status", "error")
                put("message", e.localizedMessage ?: "Sync pull failed")
            }
        }
    }
}
