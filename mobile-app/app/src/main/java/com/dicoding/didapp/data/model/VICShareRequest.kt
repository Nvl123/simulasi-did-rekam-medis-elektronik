package com.dicoding.didapp.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Model untuk request membuat VIC Share
 */
@Parcelize
data class VICShareRequest(
    @SerializedName("transaction_hash")
    val transactionHash: String,
    @SerializedName("patient_id")
    val patientId: String,
    @SerializedName("shared_by")
    val sharedBy: String = "Patient",
    @SerializedName("shared_with_hospital")
    val sharedWithHospital: String? = null,
    @SerializedName("expires_in_hours")
    val expiresInHours: Int = 24,
    @SerializedName("access_permissions")
    val accessPermissions: VICAccessPermissions = VICAccessPermissions()
) : Parcelable

/**
 * Model untuk permissions akses VIC Share
 */
@Parcelize
data class VICAccessPermissions(
    val diagnosis: Boolean = true,
    val treatment: Boolean = true,
    val doctor: Boolean = true,
    val date: Boolean = true,
    val notes: Boolean = false
) : Parcelable
