package com.dicoding.didapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

/**
 * Data model untuk VIC (Verifiable Identity Credential)
 */
@Parcelize
data class VICData(
    val type: String,
    val hospital: String,
    val patientId: String,
    val patientName: String,
    val diagnosis: String,
    val treatment: String,
    val doctor: String,
    val date: String,
    val notes: String?,
    val transactionHash: String,
    val blockNumber: Int,
    val timestamp: String,
    val verificationUrl: String,
    val demoMode: Boolean
) : Parcelable, Serializable {
    
    /**
     * Get formatted transaction hash (first 8 and last 8 characters)
     */
    fun getFormattedTransactionHash(): String {
        return if (transactionHash.length > 16) {
            "${transactionHash.take(8)}...${transactionHash.takeLast(8)}"
        } else {
            transactionHash
        }
    }
    
    /**
     * Get formatted timestamp
     */
    fun getFormattedTimestamp(): String {
        return try {
            val dateTime = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                .parse(timestamp)
            java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
                .format(dateTime ?: java.util.Date())
        } catch (e: Exception) {
            timestamp
        }
    }
    
    /**
     * Get verification status
     */
    fun getVerificationStatus(): String {
        return if (demoMode) "Demo Mode" else "Verified"
    }
    
    /**
     * Get verification status color
     */
    fun getVerificationStatusColor(): String {
        return if (demoMode) "#FF6B35" else "#4CAF50"
    }
}
