package com.dicoding.didapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * Model untuk menyimpan VIC yang sudah diverifikasi di e-wallet
 */
@Parcelize
data class SavedVIC(
    val id: String,
    val transactionHash: String,
    val patientName: String,
    val patientDID: String,
    val verificationDate: Long,
    val verificationStatus: String,
    val barcodeData: String,
    val issuerInfo: String? = null,
    val expiryDate: Long? = null,
    val additionalData: Map<String, String> = emptyMap()
) : Parcelable {
    
    companion object {
        fun fromVICData(vicData: VICData, verificationResult: VerificationResult): SavedVIC {
            return SavedVIC(
                id = "vic_${System.currentTimeMillis()}",
                transactionHash = vicData.transactionHash,
                patientName = vicData.patientName,
                patientDID = vicData.patientId, // Use patientId from VICData
                verificationDate = System.currentTimeMillis(),
                verificationStatus = if (verificationResult.success) "VERIFIED" else "FAILED",
                barcodeData = generateBarcodeData(vicData),
                issuerInfo = vicData.hospital, // Use hospital as issuer
                expiryDate = null, // No expiry date in VICData
                additionalData = mapOf(
                    "verificationMessage" to verificationResult.message,
                    "verificationTime" to System.currentTimeMillis().toString(),
                    "hospital" to vicData.hospital,
                    "diagnosis" to vicData.diagnosis,
                    "treatment" to vicData.treatment,
                    "doctor" to vicData.doctor,
                    "date" to vicData.date
                )
            )
        }
        
        private fun generateBarcodeData(vicData: VICData): String {
            // Generate barcode data yang bisa di-scan untuk verifikasi
            return "VIC:${vicData.transactionHash}:${vicData.patientId}:${System.currentTimeMillis()}"
        }
    }
}
