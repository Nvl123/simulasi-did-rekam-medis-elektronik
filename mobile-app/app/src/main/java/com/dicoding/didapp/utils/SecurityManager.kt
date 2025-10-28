package com.dicoding.didapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import android.util.Log

/**
 * Security Manager untuk menangani authentication dengan biometric
 */
class SecurityManager(private val context: Context) {
    
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences("security_preferences", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_PIN_SET = "pin_set"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val TAG = "SecurityManager"
    }
    
    /**
     * Check if biometric authentication is available
     */
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> false
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> false
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> false
            else -> false
        }
    }
    
    /**
     * Check if biometric is enabled
     */
    fun isBiometricEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }
    
    /**
     * Enable biometric authentication
     */
    fun enableBiometric() {
        sharedPrefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, true).apply()
        Log.d(TAG, "Biometric authentication enabled")
    }
    
    /**
     * Disable biometric authentication
     */
    fun disableBiometric() {
        sharedPrefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, false).apply()
        Log.d(TAG, "Biometric authentication disabled")
    }
    
    /**
     * Check if PIN is set
     */
    fun isPinSet(): Boolean {
        return sharedPrefs.getBoolean(KEY_PIN_SET, false)
    }
    
    /**
     * Set PIN
     */
    fun setPin(pin: String) {
        val hashedPin = hashPin(pin)
        sharedPrefs.edit()
            .putBoolean(KEY_PIN_SET, true)
            .putString(KEY_PIN_HASH, hashedPin)
            .apply()
        Log.d(TAG, "PIN set successfully")
    }
    
    /**
     * Verify PIN
     */
    fun verifyPin(pin: String): Boolean {
        val storedHash = sharedPrefs.getString(KEY_PIN_HASH, null)
        return if (storedHash != null) {
            val inputHash = hashPin(pin)
            storedHash == inputHash
        } else {
            false
        }
    }
    
    /**
     * Clear PIN
     */
    fun clearPin() {
        sharedPrefs.edit()
            .putBoolean(KEY_PIN_SET, false)
            .remove(KEY_PIN_HASH)
            .apply()
        Log.d(TAG, "PIN cleared")
    }
    
    /**
     * Hash PIN for security
     */
    private fun hashPin(pin: String): String {
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(pin.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error hashing PIN: ${e.message}")
            pin // Fallback to plain text (not recommended for production)
        }
    }
    
    /**
     * Get available authentication methods
     */
    fun getAvailableAuthMethods(): List<String> {
        val methods = mutableListOf<String>()
        
        if (isBiometricAvailable()) {
            methods.add("Biometric")
        }
        
        if (isPinSet()) {
            methods.add("PIN")
        }
        
        return methods
    }
    
    /**
     * Check if any authentication method is available
     */
    fun hasAnyAuthMethod(): Boolean {
        return isBiometricAvailable() || isPinSet()
    }
    
    /**
     * Reset all security settings
     */
    fun resetSecurity() {
        sharedPrefs.edit().clear().apply()
        Log.d(TAG, "All security settings reset")
    }
}




