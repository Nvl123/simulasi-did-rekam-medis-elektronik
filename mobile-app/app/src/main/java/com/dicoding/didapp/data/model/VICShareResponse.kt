package com.dicoding.didapp.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Model untuk response VIC Share
 */
@Parcelize
data class VICShareResponse(
    val success: Boolean,
    @SerializedName("share_token")
    val shareToken: String? = null,
    @SerializedName("expires_at")
    val expiresAt: String? = null,
    val message: String
) : Parcelable

/**
 * Model untuk data VIC yang di-share
 */
@Parcelize
data class VICShareData(
    val transactionHash: String,
    val blockNumber: Int,
    val hospital: String,
    val patientId: String,
    val patientName: String,
    val diagnosis: String,
    val treatment: String,
    val doctor: String,
    val date: String,
    val timestamp: Double,
    val notes: String? = null
) : Parcelable

/**
 * Model untuk response akses VIC Share
 */
@Parcelize
data class VICShareAccessResponse(
    val success: Boolean,
    val data: VICShareData? = null,
    val permissions: VICAccessPermissions? = null,
    val sharedBy: String? = null,
    val createdAt: String? = null,
    val expiresAt: String? = null,
    val message: String
) : Parcelable

/**
 * Model untuk QR Code VIC Share
 */
@Parcelize
data class VICShareQRData(
    val type: String = "VIC_SHARE",
    val shareToken: String,
    val hospital: String,
    val expiresAt: String
) : Parcelable
