package com.dicoding.didapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.dicoding.didapp.data.model.PatientData
import com.dicoding.didapp.data.model.SavedVIC
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Utility class untuk local storage menggunakan SharedPreferences
 */
class LocalStorage(context: Context) {
    
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences("did_app_data", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    /**
     * Save patient data to local storage
     */
    fun savePatient(patientData: PatientData) {
        val patients = getAllPatients().toMutableList()
        
        // Remove existing patient with same DID
        patients.removeAll { it.did == patientData.did }
        
        // Add new patient
        patients.add(patientData)
        
        // Save to SharedPreferences
        val json = gson.toJson(patients)
        sharedPrefs.edit().putString("patients", json).apply()
    }
    
    /**
     * Get all patients from local storage
     */
    fun getAllPatients(): List<PatientData> {
        val json = sharedPrefs.getString("patients", "[]") ?: "[]"
        val type = object : TypeToken<List<PatientData>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            android.util.Log.e("LocalStorage", "Error parsing patients: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Get patient by DID
     */
    fun getPatientByDid(did: String): PatientData? {
        return getAllPatients().find { it.did == did }
    }
    
    /**
     * Delete patient by DID
     */
    fun deletePatient(did: String) {
        val patients = getAllPatients().toMutableList()
        patients.removeAll { it.did == did }
        
        val json = gson.toJson(patients)
        sharedPrefs.edit().putString("patients", json).apply()
    }
    
    /**
     * Clear all patient data
     */
    fun clearAllPatients() {
        sharedPrefs.edit().remove("patients").apply()
    }
    
    /**
     * Get patient count
     */
    fun getPatientCount(): Int {
        return getAllPatients().size
    }
    
    /**
     * Check if patient exists
     */
    fun hasPatient(did: String): Boolean {
        return getPatientByDid(did) != null
    }
    
    // ========== VIC Storage Methods ==========
    
    /**
     * Save VIC to local storage
     */
    fun saveVIC(savedVIC: SavedVIC) {
        val vics = getAllVICs().toMutableList()
        
        // Remove existing VIC with same transaction hash
        vics.removeAll { it.transactionHash == savedVIC.transactionHash }
        
        // Add new VIC
        vics.add(savedVIC)
        
        // Save to SharedPreferences
        val json = gson.toJson(vics)
        sharedPrefs.edit().putString("saved_vics", json).apply()
    }
    
    /**
     * Get all saved VICs from local storage
     */
    fun getAllVICs(): List<SavedVIC> {
        val json = sharedPrefs.getString("saved_vics", "[]") ?: "[]"
        android.util.Log.d("LocalStorage", "getAllVICs JSON: $json")
        val type = object : TypeToken<List<SavedVIC>>() {}.type
        val result: List<SavedVIC> = try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            android.util.Log.e("LocalStorage", "Error parsing VICs: ${e.message}")
            emptyList()
        }
        android.util.Log.d("LocalStorage", "getAllVICs result: ${result.size} VICs")
        return result
    }
    
    /**
     * Get VIC by transaction hash
     */
    fun getVICByTransactionHash(transactionHash: String): SavedVIC? {
        return getAllVICs().find { it.transactionHash == transactionHash }
    }
    
    /**
     * Delete VIC by ID
     */
    fun deleteVIC(vicId: String) {
        val vics = getAllVICs().toMutableList()
        vics.removeAll { it.id == vicId }
        
        val json = gson.toJson(vics)
        sharedPrefs.edit().putString("saved_vics", json).apply()
    }
    
    /**
     * Clear all VICs
     */
    fun clearAllVICs() {
        sharedPrefs.edit().remove("saved_vics").apply()
    }
    
    /**
     * Get VIC count
     */
    fun getVICCount(): Int {
        return getAllVICs().size
    }
}




