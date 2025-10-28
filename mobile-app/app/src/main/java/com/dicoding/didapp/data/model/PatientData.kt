package com.dicoding.didapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class untuk menyimpan informasi pasien
 */
@Parcelize
data class PatientData(
    val did: String,
    val name: String,
    val age: Int,
    val gender: String,
    val bloodType: String?,
    val phone: String?,
    val address: String?,
    val verificationStatus: String = "verified",
    val lastCheckup: String? = null,
    val medicalRecords: List<MedicalRecord> = emptyList()
) : Parcelable

@Parcelize
data class MedicalRecord(
    val id: String,
    val diagnosis: String,
    val treatment: String,
    val medications: String?,
    val notes: String?,
    val checkupDate: String,
    val doctorName: String,
    val hospitalName: String
) : Parcelable

// VerificationResult moved to separate file


