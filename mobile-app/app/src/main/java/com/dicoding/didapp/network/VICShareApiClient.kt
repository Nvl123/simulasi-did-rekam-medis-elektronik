package com.dicoding.didapp.network

import android.util.Log
import com.dicoding.didapp.data.model.*
import com.dicoding.didapp.utils.Config
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * API Client untuk VIC Sharing System
 */
object VICShareApiClient {
    
    private val gson = Gson()
    private const val API_BASE_URL = "http://31.97.108.98:8502/api"
    
    /**
     * Membuat VIC Share
     */
    suspend fun createVICShare(request: VICShareRequest): VICShareResponse {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$API_BASE_URL/vic-share/create")
                val connection = url.openConnection() as HttpURLConnection
                
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("User-Agent", Config.USER_AGENT)
                connection.connectTimeout = Config.CONNECTION_TIMEOUT.toInt()
                connection.readTimeout = Config.READ_TIMEOUT.toInt()
                
                // Send request body
                val requestJson = gson.toJson(request)
                Log.d(Config.LOG_TAG_API, "Request JSON: $requestJson")
                connection.outputStream.use { outputStream ->
                    outputStream.write(requestJson.toByteArray())
                }
                
                val responseCode = connection.responseCode
                Log.d(Config.LOG_TAG_API, "Create VIC Share response code: $responseCode")
                
                val responseBody = if (responseCode == 200) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    connection.errorStream.bufferedReader().use { it.readText() }
                }
                
                Log.d(Config.LOG_TAG_API, "Create VIC Share response: $responseBody")
                
                // Check if response body is empty or null
                if (responseBody.isNullOrEmpty()) {
                    Log.e(Config.LOG_TAG_API, "Empty response body from server")
                    VICShareResponse(
                        success = false,
                        message = "Empty response from server"
                    )
                } else {
                    // Try to parse response
                    val response = try {
                        gson.fromJson(responseBody, VICShareResponse::class.java)
                    } catch (e: Exception) {
                        Log.e(Config.LOG_TAG_API, "Error parsing response JSON: ${e.message}")
                        Log.e(Config.LOG_TAG_API, "Response body: $responseBody")
                        VICShareResponse(
                            success = false,
                            message = "Invalid response format from server"
                        )
                    }
                    
                    // Validate response
                    if (response == null) {
                        Log.e(Config.LOG_TAG_API, "Parsed response is null")
                        VICShareResponse(
                            success = false,
                            message = "Failed to parse server response"
                        )
                    } else {
                        Log.d(Config.LOG_TAG_API, "Parsed response: success=${response.success}, message=${response.message}")
                        response
                    }
                }
                
            } catch (e: Exception) {
                Log.e(Config.LOG_TAG_API, "Error creating VIC share: ${e.message}")
                VICShareResponse(
                    success = false,
                    message = "Network error: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Mengakses VIC Share
     */
    suspend fun accessVICShare(shareToken: String, hospitalName: String): VICShareAccessResponse {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$API_BASE_URL/vic-share/$shareToken?hospital=$hospitalName")
                val connection = url.openConnection() as HttpURLConnection
                
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", Config.USER_AGENT)
                connection.connectTimeout = Config.CONNECTION_TIMEOUT.toInt()
                connection.readTimeout = Config.READ_TIMEOUT.toInt()
                
                val responseCode = connection.responseCode
                Log.d(Config.LOG_TAG_API, "Access VIC Share response code: $responseCode")
                
                val responseBody = if (responseCode == 200) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    connection.errorStream.bufferedReader().use { it.readText() }
                }
                
                Log.d(Config.LOG_TAG_API, "Access VIC Share response: $responseBody")
                
                val response = gson.fromJson(responseBody, VICShareAccessResponse::class.java)
                response
                
            } catch (e: Exception) {
                Log.e(Config.LOG_TAG_API, "Error accessing VIC share: ${e.message}")
                VICShareAccessResponse(
                    success = false,
                    message = "Network error: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Mendapatkan VIC Shares untuk pasien
     */
    suspend fun getPatientVICShares(patientId: String): List<VICShareResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$API_BASE_URL/vic-share/patient/$patientId")
                val connection = url.openConnection() as HttpURLConnection
                
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", Config.USER_AGENT)
                connection.connectTimeout = Config.CONNECTION_TIMEOUT.toInt()
                connection.readTimeout = Config.READ_TIMEOUT.toInt()
                
                val responseCode = connection.responseCode
                Log.d(Config.LOG_TAG_API, "Get patient VIC shares response code: $responseCode")
                
                if (responseCode == 200) {
                    val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d(Config.LOG_TAG_API, "Get patient VIC shares response: $responseBody")
                    
                    val response = gson.fromJson(responseBody, Array<VICShareResponse>::class.java)
                    response.toList()
                } else {
                    emptyList()
                }
                
            } catch (e: Exception) {
                Log.e(Config.LOG_TAG_API, "Error getting patient VIC shares: ${e.message}")
                emptyList()
            }
        }
    }
    
    /**
     * Revoke VIC Share
     */
    suspend fun revokeVICShare(shareToken: String): VICShareResponse {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$API_BASE_URL/vic-share/$shareToken/revoke")
                val connection = url.openConnection() as HttpURLConnection
                
                connection.requestMethod = "POST"
                connection.setRequestProperty("User-Agent", Config.USER_AGENT)
                connection.connectTimeout = Config.CONNECTION_TIMEOUT.toInt()
                connection.readTimeout = Config.READ_TIMEOUT.toInt()
                
                val responseCode = connection.responseCode
                Log.d(Config.LOG_TAG_API, "Revoke VIC Share response code: $responseCode")
                
                val responseBody = if (responseCode == 200) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    connection.errorStream.bufferedReader().use { it.readText() }
                }
                
                Log.d(Config.LOG_TAG_API, "Revoke VIC Share response: $responseBody")
                
                val response = gson.fromJson(responseBody, VICShareResponse::class.java)
                response
                
            } catch (e: Exception) {
                Log.e(Config.LOG_TAG_API, "Error revoking VIC share: ${e.message}")
                VICShareResponse(
                    success = false,
                    message = "Network error: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Mendapatkan access logs untuk VIC Share
     */
    suspend fun getVICShareAccessLogs(shareToken: String): List<Map<String, Any>> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$API_BASE_URL/vic-share/$shareToken/access-logs")
                val connection = url.openConnection() as HttpURLConnection
                
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", Config.USER_AGENT)
                connection.connectTimeout = Config.CONNECTION_TIMEOUT.toInt()
                connection.readTimeout = Config.READ_TIMEOUT.toInt()
                
                val responseCode = connection.responseCode
                Log.d(Config.LOG_TAG_API, "Get VIC Share access logs response code: $responseCode")
                
                if (responseCode == 200) {
                    val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d(Config.LOG_TAG_API, "Get VIC Share access logs response: $responseBody")
                    
                    // Parse JSON array to list of maps
                    val jsonArray = gson.fromJson(responseBody, com.google.gson.JsonArray::class.java)
                    val result = mutableListOf<Map<String, Any>>()
                    for (element in jsonArray) {
                        val map = gson.fromJson(element, Map::class.java) as Map<String, Any>
                        result.add(map)
                    }
                    result
                } else {
                    emptyList()
                }
                
            } catch (e: Exception) {
                Log.e(Config.LOG_TAG_API, "Error getting VIC share access logs: ${e.message}")
                emptyList()
            }
        }
    }
}
