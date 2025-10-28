package com.dicoding.didapp.data.model

/**
 * Data class untuk hasil verifikasi VIC
 */
data class VerificationResult(
    val success: Boolean,
    val message: String,
    val transactionHash: String? = null,
    val blockNumber: Int? = null,
    val timestamp: String? = null
)
