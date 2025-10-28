package com.dicoding.didapp.utils

import com.dicoding.didapp.data.model.PatientData
import com.dicoding.didapp.data.model.MedicalRecord
import com.dicoding.didapp.data.model.VICData
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.JsonObject
import android.util.Log

/**
 * Utility class untuk menangani QR code operations
 * Mendukung format Verifiable Credential (VC) dari server
 */
object QRCodeUtils {
    
    private val gson = Gson()
    private const val TAG = "QRCodeUtils"
    
    /**
     * Parse QR code data menjadi PatientData
     * Mendukung format Verifiable Credential (VC) dan VIC dari server
     */
    fun parseQRCode(qrData: String): PatientData {
        return try {
            Log.d(TAG, "Parsing QR code data: ${qrData.take(100)}...")
            
            // Parse sebagai JSON
            val jsonObject = gson.fromJson(qrData, JsonObject::class.java)
            
            // Cek apakah ini format Verifiable Credential
            if (jsonObject.has("@context") && jsonObject.has("credentialSubject")) {
                Log.d(TAG, "Detected Verifiable Credential format")
                parseVerifiableCredential(jsonObject)
            } else if (jsonObject.has("type") && jsonObject.get("type").asString == "VIC") {
                Log.d(TAG, "Detected VIC format")
                parseVICFormat(jsonObject)
            } else {
                // Coba parse sebagai PatientData langsung
                Log.d(TAG, "Parsing as direct PatientData")
                gson.fromJson(qrData, PatientData::class.java)
            }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "JSON parsing error: ${e.message}")
            // Jika bukan JSON, coba parse sebagai format lain
            parseCustomFormat(qrData)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing QR code: ${e.message}")
            throw e
        }
    }
    
    /**
     * Parse Verifiable Credential format
     */
    private fun parseVerifiableCredential(vcJson: JsonObject): PatientData {
        try {
            val credentialSubject = vcJson.getAsJsonObject("credentialSubject")
            val medicalRecord = credentialSubject.getAsJsonObject("medicalRecord")
            
            // Extract patient information
            val patientId = credentialSubject.get("id")?.asString ?: ""
            val name = credentialSubject.get("name")?.asString ?: ""
            val age = credentialSubject.get("age")?.asInt ?: 0
            val gender = credentialSubject.get("gender")?.asString ?: ""
            
            // Extract medical record information
            val bloodType = medicalRecord?.get("blood_type")?.asString
            val phone = medicalRecord?.get("phone")?.asString
            val address = medicalRecord?.get("address")?.asString
            val diagnosis = medicalRecord?.get("diagnosis")?.asString ?: ""
            val treatment = medicalRecord?.get("treatment")?.asString ?: ""
            val medications = medicalRecord?.get("medications")?.asString ?: ""
            val notes = medicalRecord?.get("notes")?.asString ?: ""
            val checkupDate = medicalRecord?.get("checkup_date")?.asString ?: ""
            
            // Create medical record
            val medicalRecordObj = MedicalRecord(
                id = "mr_${System.currentTimeMillis()}",
                diagnosis = diagnosis,
                treatment = treatment,
                medications = medications,
                notes = notes,
                checkupDate = checkupDate,
                doctorName = "Dr. System",
                hospitalName = "DID Hospital"
            )
            
            Log.d(TAG, "Successfully parsed VC: $name ($patientId)")
            
            return PatientData(
                did = patientId,
                name = name,
                age = age,
                gender = gender,
                bloodType = bloodType,
                phone = phone,
                address = address,
                verificationStatus = "verified",
                lastCheckup = checkupDate,
                medicalRecords = listOf(medicalRecordObj)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Verifiable Credential: ${e.message}")
            throw Exception("Invalid Verifiable Credential format: ${e.message}")
        }
    }
    
    /**
     * Parse VIC format (Verifiable Identity Credential)
     */
    private fun parseVICFormat(vicJson: JsonObject): PatientData {
        try {
            // Extract VIC data
            val hospital = vicJson.get("hospital")?.asString ?: "Unknown Hospital"
            val patientId = vicJson.get("patient_id")?.asString ?: ""
            val patientName = vicJson.get("patient_name")?.asString ?: ""
            val diagnosis = vicJson.get("diagnosis")?.asString ?: ""
            val treatment = vicJson.get("treatment")?.asString ?: ""
            val doctor = vicJson.get("doctor")?.asString ?: "Unknown Doctor"
            val date = vicJson.get("date")?.asString ?: ""
            val notes = vicJson.get("notes")?.asString ?: ""
            val transactionHash = vicJson.get("transaction_hash")?.asString ?: ""
            val blockNumber = vicJson.get("block_number")?.asInt ?: 0
            val demoMode = vicJson.get("demo_mode")?.asBoolean ?: false
            
            // Create medical record from VIC data
            val medicalRecord = MedicalRecord(
                id = "vic_${System.currentTimeMillis()}",
                diagnosis = diagnosis,
                treatment = treatment,
                medications = null, // VIC format doesn't have medications field
                notes = notes,
                checkupDate = date,
                doctorName = doctor,
                hospitalName = hospital
            )
            
            // Create patient DID from patient_id
            val patientDID = "did:patient:${patientId}"
            
            Log.d(TAG, "Successfully parsed VIC: $patientName ($patientId)")
            
            return PatientData(
                did = patientDID,
                name = patientName,
                age = 0, // VIC format doesn't have age
                gender = "Unknown", // VIC format doesn't have gender
                bloodType = null, // VIC format doesn't have blood type
                phone = null, // VIC format doesn't have phone
                address = null, // VIC format doesn't have address
                verificationStatus = if (demoMode) "demo" else "verified",
                lastCheckup = date,
                medicalRecords = listOf(medicalRecord)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing VIC format: ${e.message}")
            throw Exception("Invalid VIC format: ${e.message}")
        }
    }
    
    /**
     * Parse format custom untuk QR code
     */
    private fun parseCustomFormat(qrData: String): PatientData {
        Log.d(TAG, "Parsing custom format: $qrData")
        
        // Format: DID|NAME|AGE|GENDER|BLOOD_TYPE|PHONE|ADDRESS
        val parts = qrData.split("|")
        
        return PatientData(
            did = parts.getOrNull(0) ?: "",
            name = parts.getOrNull(1) ?: "",
            age = parts.getOrNull(2)?.toIntOrNull() ?: 0,
            gender = parts.getOrNull(3) ?: "",
            bloodType = parts.getOrNull(4)?.takeIf { it.isNotEmpty() },
            phone = parts.getOrNull(5)?.takeIf { it.isNotEmpty() },
            address = parts.getOrNull(6)?.takeIf { it.isNotEmpty() }
        )
    }
    
    /**
     * Generate QR code data dari PatientData
     */
    fun generateQRCodeData(patientData: PatientData): String {
        return gson.toJson(patientData)
    }
    
    /**
     * Validate QR code format
     */
    fun isValidQRCode(qrData: String): Boolean {
        return try {
            val patientData = parseQRCode(qrData)
            patientData.did.isNotEmpty() && patientData.name.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Invalid QR code: ${e.message}")
            false
        }
    }
    
    /**
     * Extract patient DID from QR code data
     */
    fun extractPatientDID(qrData: String): String? {
        return try {
            val jsonObject = gson.fromJson(qrData, JsonObject::class.java)
            
            if (jsonObject.has("@context") && jsonObject.has("credentialSubject")) {
                // Verifiable Credential format
                val credentialSubject = jsonObject.getAsJsonObject("credentialSubject")
                credentialSubject.get("id")?.asString
            } else if (jsonObject.has("type") && jsonObject.get("type").asString == "VIC") {
                // VIC format
                val patientId = jsonObject.get("patient_id")?.asString
                if (patientId != null) "did:patient:$patientId" else null
            } else {
                // Direct PatientData format
                val patientData = gson.fromJson(qrData, PatientData::class.java)
                patientData.did
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting DID: ${e.message}")
            null
        }
    }
    
    /**
     * Get QR code type (VC, VIC, VIC_SHARE, or PatientData)
     */
    fun getQRCodeType(qrData: String): String {
        return try {
            val jsonObject = gson.fromJson(qrData, JsonObject::class.java)
            
            if (jsonObject.has("@context") && jsonObject.has("credentialSubject")) {
                "VerifiableCredential"
            } else if (jsonObject.has("type") && jsonObject.get("type").asString == "VIC") {
                "VIC"
            } else if (jsonObject.has("type") && jsonObject.get("type").asString == "VIC_SHARE") {
                "VIC_SHARE"
            } else {
                "PatientData"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    /**
     * Parse VIC Share QR code data
     */
    fun parseVICShareQRCode(jsonData: String): com.dicoding.didapp.data.model.VICShareQRData? {
        return try {
            gson.fromJson(jsonData, com.dicoding.didapp.data.model.VICShareQRData::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing VIC Share QR code: ${e.message}")
            null
        }
    }
    
    /**
     * Parse VIC QR code data menjadi VICData
     */
    fun parseVICQRCode(qrData: String): VICData {
        return try {
            Log.d(TAG, "Parsing VIC QR code data: ${qrData.take(100)}...")
            
            val jsonObject = gson.fromJson(qrData, JsonObject::class.java)
            
            if (jsonObject.has("type") && jsonObject.get("type").asString == "VIC") {
                parseVICData(jsonObject)
            } else {
                throw Exception("Not a VIC QR code")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing VIC QR code: ${e.message}")
            throw e
        }
    }
    
    /**
     * Parse VIC JSON data
     */
    private fun parseVICData(vicJson: JsonObject): VICData {
        try {
            val type = vicJson.get("type")?.asString ?: ""
            val hospital = vicJson.get("hospital")?.asString ?: "Unknown Hospital"
            val patientId = vicJson.get("patient_id")?.asString ?: ""
            val patientName = vicJson.get("patient_name")?.asString ?: ""
            val diagnosis = vicJson.get("diagnosis")?.asString ?: ""
            val treatment = vicJson.get("treatment")?.asString ?: ""
            val doctor = vicJson.get("doctor")?.asString ?: "Unknown Doctor"
            val date = vicJson.get("date")?.asString ?: ""
            val notes = vicJson.get("notes")?.asString
            val transactionHash = vicJson.get("transaction_hash")?.asString ?: ""
            val blockNumber = vicJson.get("block_number")?.asInt ?: 0
            val timestamp = vicJson.get("timestamp")?.asString ?: ""
            val verificationUrl = vicJson.get("verification_url")?.asString ?: ""
            val demoMode = vicJson.get("demo_mode")?.asBoolean ?: false
            
            Log.d(TAG, "Successfully parsed VIC: $patientName ($patientId)")
            
            return VICData(
                type = type,
                hospital = hospital,
                patientId = patientId,
                patientName = patientName,
                diagnosis = diagnosis,
                treatment = treatment,
                doctor = doctor,
                date = date,
                notes = notes,
                transactionHash = transactionHash,
                blockNumber = blockNumber,
                timestamp = timestamp,
                verificationUrl = verificationUrl,
                demoMode = demoMode
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing VIC data: ${e.message}")
            throw Exception("Invalid VIC data format: ${e.message}")
        }
    }
}


