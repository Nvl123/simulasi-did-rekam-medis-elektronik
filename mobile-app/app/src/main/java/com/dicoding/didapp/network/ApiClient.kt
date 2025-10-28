package com.dicoding.didapp.network

import com.dicoding.didapp.data.model.VerificationResult
import com.dicoding.didapp.data.model.VerificationResponse
import com.dicoding.didapp.utils.Config
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * API Client for communicating with Flask and FastAPI servers
 */
object ApiClient {
    
    private var context: android.content.Context? = null
    
    /**
     * Initialize ApiClient with context
     */
    fun initialize(context: android.content.Context) {
        this.context = context
        Log.d(Config.LOG_TAG_API, "ApiClient initialized")
    }
    
    /**
     * Check overall API health (both Flask and FastAPI)
     */
    suspend fun checkHealth(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Try Flask API first
                val flaskHealthy = checkFlaskHealth()
                if (flaskHealthy) {
                    Log.d(Config.LOG_TAG_API, "Flask API is healthy")
                    return@withContext true
                }
                
                // Try FastAPI as fallback
                val fastApiHealthy = checkFastAPIHealth()
                if (fastApiHealthy) {
                    Log.d(Config.LOG_TAG_API, "FastAPI is healthy")
                    return@withContext true
                }
                
                Log.w(Config.LOG_TAG_API, "Both APIs are unhealthy")
                false
            } catch (e: Exception) {
                Log.e(Config.LOG_TAG_API, "Health check failed: ${e.message}")
                false
            }
        }
    }
    
    /**
     * Check Flask API health
     */
    private fun checkFlaskHealth(): Boolean {
        return try {
            val url = "${Config.FLASK_API_URL}${Config.HEALTH_ENDPOINT}"
            val connection = URL(url).openConnection() as HttpURLConnection
            
            connection.requestMethod = "GET"
            connection.connectTimeout = Config.CONNECTION_TIMEOUT.toInt()
            connection.readTimeout = Config.READ_TIMEOUT.toInt()
            connection.setRequestProperty("User-Agent", Config.USER_AGENT)
            
            val responseCode = connection.responseCode
            Log.d(Config.LOG_TAG_API, "Flask health check response code: $responseCode")
            
            responseCode == 200
        } catch (e: Exception) {
            Log.e(Config.LOG_TAG_API, "Flask health check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check FastAPI health
     */
    private fun checkFastAPIHealth(): Boolean {
        return try {
            val url = "${Config.FASTAPI_URL}${Config.HEALTH_ENDPOINT}"
            val connection = URL(url).openConnection() as HttpURLConnection
            
            connection.requestMethod = "GET"
            connection.connectTimeout = Config.CONNECTION_TIMEOUT.toInt()
            connection.readTimeout = Config.READ_TIMEOUT.toInt()
            connection.setRequestProperty("User-Agent", Config.USER_AGENT)
            
            val responseCode = connection.responseCode
            Log.d(Config.LOG_TAG_API, "FastAPI health check response code: $responseCode")
            
            responseCode == 200
        } catch (e: Exception) {
            Log.e(Config.LOG_TAG_API, "FastAPI health check failed: ${e.message}")
            false
        }
    }
    
    
    /**
     * Verify VIC using Flask API (Port 8505)
     */
    suspend fun verifyVICWithFlask(transactionHash: String): VerificationResult {
        return withContext(Dispatchers.IO) {
            val url = "${Config.FLASK_API_URL}${Config.VERIFY_ENDPOINT}/$transactionHash"
            verifyWithAPI(url, "Flask API")
        }
    }
    
    /**
     * Verify VIC using FastAPI (Port 8502)
     */
    suspend fun verifyVICWithFastAPI(transactionHash: String): VerificationResult {
        return withContext(Dispatchers.IO) {
            val url = "${Config.FASTAPI_URL}${Config.VERIFY_ENDPOINT}/$transactionHash"
            verifyWithAPI(url, "FastAPI")
        }
    }
    
    
    /**
     * Generic API verification method
     */
    private suspend fun verifyWithAPI(url: String, apiName: String): VerificationResult {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            
            connection.requestMethod = "GET"
            connection.connectTimeout = Config.CONNECTION_TIMEOUT.toInt()
            connection.readTimeout = Config.READ_TIMEOUT.toInt()
            connection.setRequestProperty("Content-Type", Config.CONTENT_TYPE_JSON)
            connection.setRequestProperty("User-Agent", Config.USER_AGENT)
            
            val responseCode = connection.responseCode
            Log.d(Config.LOG_TAG_API, "$apiName response code: $responseCode")
            Log.d(Config.LOG_TAG_API, "$apiName URL: $url")
            
            if (responseCode == 200) {
                val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(Config.LOG_TAG_API, "$apiName response: $responseBody")
                
                val gson = com.google.gson.Gson()
                val verificationResponse = gson.fromJson(responseBody, VerificationResponse::class.java)
                
                if (verificationResponse.verified == true) {
                    VerificationResult(
                        success = true,
                        message = "VIC verified successfully via $apiName"
                    )
                } else {
                    VerificationResult(
                        success = false,
                        message = "VIC verification failed: ${verificationResponse.message ?: "Unknown error"}"
                    )
                }
            } else {
                VerificationResult(
                    success = false,
                    message = "$apiName error: $responseCode"
                )
            }
        } catch (e: Exception) {
            Log.e(Config.LOG_TAG_API, "Error calling $apiName: ${e.message}")
            VerificationResult(
                success = false,
                message = "$apiName error: ${e.message}"
            )
        }
    }
}