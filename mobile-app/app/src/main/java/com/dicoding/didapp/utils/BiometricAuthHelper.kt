package com.dicoding.didapp.utils

import android.os.Bundle
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import android.util.Log

/**
 * Helper class untuk menangani biometric authentication
 */
class BiometricAuthHelper(private val activity: FragmentActivity) {
    
    private val executor = ContextCompat.getMainExecutor(activity)
    private val biometricManager = SecurityManager(activity)
    
    companion object {
        private const val TAG = "BiometricAuthHelper"
    }
    
    /**
     * Show biometric prompt untuk authentication
     */
    fun showBiometricPrompt(
        title: String = "Biometric Authentication",
        subtitle: String = "Use your fingerprint or face to authenticate",
        negativeButtonText: String = "Cancel",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit
    ) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .build()
        
        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(TAG, "Biometric authentication succeeded")
                onSuccess()
            }
            
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.e(TAG, "Biometric authentication error: $errString")
                onError(errString.toString())
            }
            
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.w(TAG, "Biometric authentication failed")
                onFailed()
            }
        })
        
        biometricPrompt.authenticate(promptInfo)
    }
    
    /**
     * Check if biometric is available and enabled
     */
    fun canUseBiometric(): Boolean {
        return biometricManager.isBiometricAvailable() && biometricManager.isBiometricEnabled()
    }
    
    /**
     * Get biometric status message
     */
    fun getBiometricStatusMessage(): String {
        return when {
            !biometricManager.isBiometricAvailable() -> "Biometric authentication not available on this device"
            !biometricManager.isBiometricEnabled() -> "Biometric authentication is disabled"
            else -> "Biometric authentication is ready"
        }
    }
}




