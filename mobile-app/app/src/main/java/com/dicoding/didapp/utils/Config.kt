package com.dicoding.didapp.utils

/**
 * Configuration constants for the application
 */
object Config {
    
    // Server URLs
    const val FLASK_API_URL = "http://31.97.108.98:8505"
    const val FASTAPI_URL = "http://31.97.108.98:8502"
    
    // API Endpoints
    const val HEALTH_ENDPOINT = "/health"
    const val VERIFY_ENDPOINT = "/verify"
    
    // Timeouts
    const val VERIFICATION_TIMEOUT = 10000L
    const val CONNECTION_TIMEOUT = 10000L
    const val READ_TIMEOUT = 10000L
    
    // API Headers
    const val CONTENT_TYPE_JSON = "application/json"
    const val USER_AGENT = "DID-Medical-Records-Android/1.0.0"
    
    // Verification Settings
    const val MAX_RETRY_ATTEMPTS = 3
    const val RETRY_DELAY_MS = 1000L
    
    // Logging
    const val LOG_TAG_VIC = "VICDetails"
    const val LOG_TAG_API = "APIClient"
    const val LOG_TAG_VERIFICATION = "Verification"
}
