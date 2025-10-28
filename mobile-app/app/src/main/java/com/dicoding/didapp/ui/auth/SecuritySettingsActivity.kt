package com.dicoding.didapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import com.dicoding.didapp.R
import com.dicoding.didapp.utils.SecurityManager
import com.dicoding.didapp.utils.BiometricAuthHelper
import android.util.Log

/**
 * Security Settings Activity untuk mengatur authentication
 */
class SecuritySettingsActivity : AppCompatActivity() {
    
    private lateinit var securityManager: SecurityManager
    private lateinit var biometricHelper: BiometricAuthHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security_settings)
        
        securityManager = SecurityManager(this)
        biometricHelper = BiometricAuthHelper(this)
        
        setupUI()
        updateUI()
    }
    
    private fun setupUI() {
        // Setup toolbar
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { finish() }
        }
        
        // Setup biometric toggle
        findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switch_biometric).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableBiometric()
            } else {
                disableBiometric()
            }
        }
        
        // Setup PIN buttons
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_set_pin).setOnClickListener {
            showSetPinDialog()
        }
        
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_change_pin).setOnClickListener {
            showChangePinDialog()
        }
        
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_clear_pin).setOnClickListener {
            clearPin()
        }
        
        // Setup test authentication
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_test_auth).setOnClickListener {
            testAuthentication()
        }
        
        // Setup reset security
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_reset_security).setOnClickListener {
            resetSecurity()
        }
    }
    
    private fun updateUI() {
        // Update biometric switch
        val biometricSwitch = findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switch_biometric)
        biometricSwitch.isChecked = securityManager.isBiometricEnabled()
        
        // Update biometric status
        val biometricStatus = findViewById<android.widget.TextView>(R.id.tv_biometric_status)
        biometricStatus.text = biometricHelper.getBiometricStatusMessage()
        
        // Update PIN status
        val pinStatus = findViewById<android.widget.TextView>(R.id.tv_pin_status)
        pinStatus.text = if (securityManager.isPinSet()) "PIN is set" else "PIN is not set"
        
        // Update PIN buttons visibility
        val setPinBtn = findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_set_pin)
        val changePinBtn = findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_change_pin)
        val clearPinBtn = findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_clear_pin)
        
        if (securityManager.isPinSet()) {
            setPinBtn.visibility = android.view.View.GONE
            changePinBtn.visibility = android.view.View.VISIBLE
            clearPinBtn.visibility = android.view.View.VISIBLE
        } else {
            setPinBtn.visibility = android.view.View.VISIBLE
            changePinBtn.visibility = android.view.View.GONE
            clearPinBtn.visibility = android.view.View.GONE
        }
        
        // Update test button
        val testBtn = findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_test_auth)
        testBtn.isEnabled = securityManager.hasAnyAuthMethod()
    }
    
    private fun enableBiometric() {
        if (securityManager.isBiometricAvailable()) {
            securityManager.enableBiometric()
            Toast.makeText(this, "Biometric authentication enabled", Toast.LENGTH_SHORT).show()
            updateUI()
        } else {
            Toast.makeText(this, "Biometric authentication not available", Toast.LENGTH_LONG).show()
            findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switch_biometric).isChecked = false
        }
    }
    
    private fun disableBiometric() {
        securityManager.disableBiometric()
        Toast.makeText(this, "Biometric authentication disabled", Toast.LENGTH_SHORT).show()
        updateUI()
    }
    
    private fun showSetPinDialog() {
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Set PIN")
            .setMessage("Enter a 4-digit PIN for authentication")
            .setView(R.layout.dialog_pin_input)
            .setPositiveButton("Set PIN") { _, _ ->
                val pinInput = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_pin_input)
                val pin = pinInput.text.toString()
                if (pin.length == 4 && pin.all { it.isDigit() }) {
                    securityManager.setPin(pin)
                    Toast.makeText(this, "PIN set successfully", Toast.LENGTH_SHORT).show()
                    updateUI()
                } else {
                    Toast.makeText(this, "Please enter a valid 4-digit PIN", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
    
    private fun showChangePinDialog() {
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Change PIN")
            .setMessage("Enter your current PIN and new PIN")
            .setView(R.layout.dialog_change_pin)
            .setPositiveButton("Change PIN") { _, _ ->
                val currentPinInput = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_current_pin)
                val newPinInput = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_new_pin)
                val currentPin = currentPinInput.text.toString()
                val newPin = newPinInput.text.toString()
                
                if (securityManager.verifyPin(currentPin)) {
                    if (newPin.length == 4 && newPin.all { it.isDigit() }) {
                        securityManager.setPin(newPin)
                        Toast.makeText(this, "PIN changed successfully", Toast.LENGTH_SHORT).show()
                        updateUI()
                    } else {
                        Toast.makeText(this, "Please enter a valid 4-digit new PIN", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Current PIN is incorrect", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
    
    private fun clearPin() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Clear PIN")
            .setMessage("Are you sure you want to clear your PIN?")
            .setPositiveButton("Clear") { _, _ ->
                securityManager.clearPin()
                Toast.makeText(this, "PIN cleared", Toast.LENGTH_SHORT).show()
                updateUI()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun testAuthentication() {
        if (securityManager.isBiometricEnabled() && biometricHelper.canUseBiometric()) {
            // Test biometric authentication
            biometricHelper.showBiometricPrompt(
                title = "Test Biometric Authentication",
                subtitle = "Use your fingerprint or face to test authentication",
                onSuccess = {
                    Toast.makeText(this, "Biometric authentication successful!", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    Toast.makeText(this, "Biometric error: $error", Toast.LENGTH_LONG).show()
                },
                onFailed = {
                    Toast.makeText(this, "Biometric authentication failed", Toast.LENGTH_SHORT).show()
                }
            )
        } else if (securityManager.isPinSet()) {
            // Test PIN authentication
            showPinTestDialog()
        } else {
            Toast.makeText(this, "No authentication method available", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showPinTestDialog() {
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Test PIN")
            .setMessage("Enter your PIN to test authentication")
            .setView(R.layout.dialog_pin_input)
            .setPositiveButton("Test") { _, _ ->
                val pinInput = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_pin_input)
                val pin = pinInput.text.toString()
                if (securityManager.verifyPin(pin)) {
                    Toast.makeText(this, "PIN authentication successful!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "PIN authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
    
    private fun resetSecurity() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Reset Security")
            .setMessage("Are you sure you want to reset all security settings? This will clear all authentication methods.")
            .setPositiveButton("Reset") { _, _ ->
                securityManager.resetSecurity()
                Toast.makeText(this, "Security settings reset", Toast.LENGTH_SHORT).show()
                updateUI()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}