package com.dicoding.didapp

import com.dicoding.didapp.data.model.VICData
import com.dicoding.didapp.data.model.VerificationResult
import com.dicoding.didapp.network.ApiClient
import com.dicoding.didapp.utils.Config
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

/**
 * Test class for VIC verification functionality
 */
class VICVerificationTest {
    
    @Test
    fun testVICDataValidation() {
        // Test valid VIC data
        val validVICData = VICData(
            type = "VIC",
            hospital = "Rumah Sakit A",
            patientId = "P001",
            patientName = "John Doe",
            diagnosis = "Common Cold",
            treatment = "Rest and Medication",
            doctor = "Dr. Smith",
            date = "2024-01-15",
            notes = "Additional medical information",
            transactionHash = "0x5dff8cd1f681461c87ca379738c62290ad7db473",
            blockNumber = 4652,
            timestamp = "2024-01-15T10:30:00.000Z",
            verificationUrl = "http://31.97.108.98:8505/verify/0x5dff8cd1f681461c87ca379738c62290ad7db473",
            demoMode = false
        )
        
        // Test required fields
        assertTrue("Type should be VIC", validVICData.type == "VIC")
        assertTrue("Hospital should not be empty", validVICData.hospital.isNotEmpty())
        assertTrue("Patient ID should not be empty", validVICData.patientId.isNotEmpty())
        assertTrue("Patient name should not be empty", validVICData.patientName.isNotEmpty())
        assertTrue("Transaction hash should not be empty", validVICData.transactionHash.isNotEmpty())
        
        // Test transaction hash format
        assertTrue("Transaction hash should start with 0x", validVICData.transactionHash.startsWith("0x"))
        assertTrue("Transaction hash should be at least 10 characters", validVICData.transactionHash.length >= 10)
        
        // Test block number
        assertTrue("Block number should be positive", validVICData.blockNumber > 0)
        
        // Test timestamp format
        assertTrue("Timestamp should contain T", validVICData.timestamp.contains("T"))
        assertTrue("Timestamp should contain Z", validVICData.timestamp.contains("Z"))
    }
    
    @Test
    fun testConfigConstants() {
        // Test server URLs
        assertEquals("Flask API URL should be correct", "http://31.97.108.98:8505", Config.FLASK_API_URL)
        assertEquals("FastAPI URL should be correct", "http://31.97.108.98:8502", Config.FASTAPI_URL)
        
        // Test endpoints
        assertEquals("Health endpoint should be correct", "/health", Config.HEALTH_ENDPOINT)
        assertEquals("Verify endpoint should be correct", "/verify", Config.VERIFY_ENDPOINT)
        
        // Test timeouts
        assertEquals("Connection timeout should be 10 seconds", 10000L, Config.CONNECTION_TIMEOUT)
        assertEquals("Read timeout should be 10 seconds", 10000L, Config.READ_TIMEOUT)
        
        // Test content type
        assertEquals("Content type should be JSON", "application/json", Config.CONTENT_TYPE_JSON)
    }
    
    @Test
    fun testVerificationResult() {
        // Test success result
        val successResult = VerificationResult(
            success = true,
            message = "VIC verified successfully"
        )
        
        assertTrue("Success result should be successful", successResult.success)
        assertEquals("Success message should be correct", "VIC verified successfully", successResult.message)
        
        // Test failure result
        val failureResult = VerificationResult(
            success = false,
            message = "VIC verification failed"
        )
        
        assertFalse("Failure result should not be successful", failureResult.success)
        assertEquals("Failure message should be correct", "VIC verification failed", failureResult.message)
    }
    
    @Test
    fun testAPIEndpointConstruction() {
        val transactionHash = "0x5dff8cd1f681461c87ca379738c62290ad7db473"
        
        // Test Flask API endpoint
        val flaskEndpoint = "${Config.FLASK_API_URL}${Config.VERIFY_ENDPOINT}/$transactionHash"
        val expectedFlaskEndpoint = "http://31.97.108.98:8505/verify/0x5dff8cd1f681461c87ca379738c62290ad7db473"
        assertEquals("Flask endpoint should be constructed correctly", expectedFlaskEndpoint, flaskEndpoint)
        
        // Test FastAPI endpoint
        val fastApiEndpoint = "${Config.FASTAPI_URL}${Config.VERIFY_ENDPOINT}/$transactionHash"
        val expectedFastApiEndpoint = "http://31.97.108.98:8502/verify/0x5dff8cd1f681461c87ca379738c62290ad7db473"
        assertEquals("FastAPI endpoint should be constructed correctly", expectedFastApiEndpoint, fastApiEndpoint)
    }
    
    @Test
    fun testLogTags() {
        // Test log tags
        assertEquals("VIC log tag should be correct", "VICDetails", Config.LOG_TAG_VIC)
        assertEquals("API log tag should be correct", "APIClient", Config.LOG_TAG_API)
        assertEquals("Verification log tag should be correct", "Verification", Config.LOG_TAG_VERIFICATION)
    }
    
    @Test
    fun testUserAgent() {
        // Test user agent
        assertTrue("User agent should contain app name", Config.USER_AGENT.contains("DID-Medical-Records"))
        assertTrue("User agent should contain Android", Config.USER_AGENT.contains("Android"))
        assertTrue("User agent should contain version", Config.USER_AGENT.contains("1.0.0"))
    }
    
    @Test
    fun testRetrySettings() {
        // Test retry settings
        assertEquals("Max retry attempts should be 3", 3, Config.MAX_RETRY_ATTEMPTS)
        assertEquals("Retry delay should be 1 second", 1000L, Config.RETRY_DELAY_MS)
    }
}
