package com.dicoding.didapp.data.model

/**
 * Data class untuk response dari verification API
 */
data class VerificationResponse(
    val verified: Boolean?,
    val message: String? = null,
    val transactionHash: String? = null,
    val blockNumber: Int? = null,
    val timestamp: String? = null
)




