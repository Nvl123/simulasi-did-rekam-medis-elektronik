package com.dicoding.didapp.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.dicoding.didapp.ui.auth.SecuritySettingsActivity
import android.util.Log

/**
 * App Security Manager untuk menangani authentication saat masuk aplikasi
 */
class AppSecurityManager(private val context: Context) {
    
    private val securityManager = SecurityManager(context)
    private val biometricHelper = BiometricAuthHelper(context as FragmentActivity)
    
    companion object {
        private const val TAG = "AppSecurityManager"
        private const val KEY_APP_LOCKED = "app_locked"
        private const val KEY_LAST_AUTH_TIME = "last_auth_time"
        private const val AUTH_TIMEOUT = 5 * 60 * 1000L // 5 minutes
    }
    
    /**
     * Check if app needs authentication
     */
    fun needsAuthentication(): Boolean {
        val sharedPrefs = context.getSharedPreferences("app_security", Context.MODE_PRIVATE)
        val isLocked = sharedPrefs.getBoolean(KEY_APP_LOCKED, false)
        val lastAuthTime = sharedPrefs.getLong(KEY_LAST_AUTH_TIME, 0)
        val currentTime = System.currentTimeMillis()
        
        return isLocked || (currentTime - lastAuthTime) > AUTH_TIMEOUT
    }
    
    /**
     * Show authentication prompt
     */
    fun showAuthenticationPrompt(
        onSuccess: () -> Unit,
        onFailed: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!securityManager.hasAnyAuthMethod()) {
            // No authentication method available, show setup dialog
            showSetupDialog(onSuccess, onFailed, onError)
            return
        }
        
        if (securityManager.isBiometricEnabled() && biometricHelper.canUseBiometric()) {
            // Use biometric authentication
            biometricHelper.showBiometricPrompt(
                title = "App Authentication",
                subtitle = "Use your fingerprint or face to unlock the app",
                onSuccess = {
                    markAsAuthenticated()
                    onSuccess()
                },
                onError = { error ->
                    onError(error)
                },
                onFailed = {
                    onFailed()
                }
            )
        } else if (securityManager.isPinSet()) {
            // Use PIN authentication
            showPinDialog(onSuccess, onFailed, onError)
        } else {
            // No authentication method available
            showSetupDialog(onSuccess, onFailed, onError)
        }
    }
    
    /**
     * Show PIN authentication dialog
     */
    private fun showPinDialog(
        onSuccess: () -> Unit,
        onFailed: () -> Unit,
        onError: (String) -> Unit
    ) {
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
            .setTitle("App Authentication")
            .setMessage("Enter your PIN to unlock the app")
            .setView(com.dicoding.didapp.R.layout.dialog_pin_input)
            .setPositiveButton("Unlock") { _, _ ->
                val pinInput = (context as FragmentActivity).findViewById<com.google.android.material.textfield.TextInputEditText>(com.dicoding.didapp.R.id.et_pin_input)
                val pin = pinInput?.text?.toString() ?: ""
                
                if (securityManager.verifyPin(pin)) {
                    markAsAuthenticated()
                    onSuccess()
                } else {
                    onFailed()
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                onFailed()
            }
            .setNeutralButton("Setup") { _, _ ->
                showSetupDialog(onSuccess, onFailed, onError)
            }
            .create()
        
        dialog.show()
    }
    
    /**
     * Show setup dialog when no authentication method is available
     */
    private fun showSetupDialog(
        onSuccess: () -> Unit,
        onFailed: () -> Unit,
        onError: (String) -> Unit
    ) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
            .setTitle("Security Setup Required")
            .setMessage("No authentication method is set up. Would you like to set up security now?")
            .setPositiveButton("Setup Security") { _, _ ->
                val intent = Intent(context, SecuritySettingsActivity::class.java)
                context.startActivity(intent)
                onFailed() // User needs to set up security first
            }
            .setNegativeButton("Skip") { _, _ ->
                markAsAuthenticated()
                onSuccess()
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * Mark user as authenticated
     */
    private fun markAsAuthenticated() {
        val sharedPrefs = context.getSharedPreferences("app_security", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putBoolean(KEY_APP_LOCKED, false)
            .putLong(KEY_LAST_AUTH_TIME, System.currentTimeMillis())
            .apply()
        
        Log.d(TAG, "User authenticated successfully")
    }
    
    /**
     * Lock the app
     */
    fun lockApp() {
        val sharedPrefs = context.getSharedPreferences("app_security", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean(KEY_APP_LOCKED, true).apply()
        Log.d(TAG, "App locked")
    }
    
    /**
     * Check if app is locked
     */
    fun isAppLocked(): Boolean {
        val sharedPrefs = context.getSharedPreferences("app_security", Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean(KEY_APP_LOCKED, false)
    }
    
    /**
     * Get authentication status
     */
    fun getAuthenticationStatus(): String {
        return when {
            !securityManager.hasAnyAuthMethod() -> "No authentication method set up"
            securityManager.isBiometricEnabled() && biometricHelper.canUseBiometric() -> "Biometric authentication ready"
            securityManager.isPinSet() -> "PIN authentication ready"
            else -> "Authentication not configured"
        }
    }
}
