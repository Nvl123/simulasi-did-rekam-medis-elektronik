package com.dicoding.didapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.didapp.ui.auth.SecuritySettingsActivity
import com.dicoding.didapp.ui.scanner.QRScannerActivity
import com.dicoding.didapp.ui.wallet.DigitalWalletActivity
import com.dicoding.didapp.network.ApiClient
import com.dicoding.didapp.utils.AppSecurityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main Activity - Entry point of the DID/VC Medical Records App
 * Provides navigation to main features: Scan QR Code and Digital Wallet
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var appSecurityManager: AppSecurityManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize security manager
        appSecurityManager = AppSecurityManager(this)
        
        // Initialize ApiClient with server preferences
        ApiClient.initialize(this)
        
        setupUI()
        checkApiConnection()
    }
    
    override fun onResume() {
        super.onResume()
        // Check if authentication is needed
        if (appSecurityManager.needsAuthentication()) {
            showAuthentication()
        }
    }
    
    private fun showAuthentication() {
        appSecurityManager.showAuthenticationPrompt(
            onSuccess = {
                // Authentication successful, continue with app
                Log.d("MainActivity", "Authentication successful")
            },
            onFailed = {
                // Authentication failed, show error
                android.widget.Toast.makeText(this, "Authentication failed", android.widget.Toast.LENGTH_SHORT).show()
            },
            onError = { error ->
                // Authentication error, show error
                android.widget.Toast.makeText(this, "Authentication error: $error", android.widget.Toast.LENGTH_LONG).show()
            }
        )
    }
    
    private fun setupUI() {
        findViewById<Button>(R.id.btn_scan_qr).setOnClickListener {
            val intent = Intent(this, QRScannerActivity::class.java)
            startActivity(intent)
        }
        
        findViewById<Button>(R.id.btn_digital_wallet).setOnClickListener {
            val intent = Intent(this, DigitalWalletActivity::class.java)
            startActivity(intent)
        }
        
        findViewById<Button>(R.id.btn_authentication).setOnClickListener {
            val intent = Intent(this, SecuritySettingsActivity::class.java)
            startActivity(intent)
        }
        
        // Server settings removed - no longer needed
    }
    
    private fun checkApiConnection() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val isConnected = ApiClient.checkHealth()
                if (isConnected) {
                    // API server is running
                    findViewById<android.widget.TextView>(R.id.tv_status).text = "✅ Connected to API Server"
                    findViewById<android.widget.TextView>(R.id.tv_status).setTextColor(android.graphics.Color.BLACK)
                } else {
                    // API server is not running
                    findViewById<android.widget.TextView>(R.id.tv_status).text = "❌ API Server Offline"
                    findViewById<android.widget.TextView>(R.id.tv_status).setTextColor(android.graphics.Color.RED)
                }
            } catch (e: Exception) {
                findViewById<android.widget.TextView>(R.id.tv_status).text = "⚠️ Connection Error"
                findViewById<android.widget.TextView>(R.id.tv_status).setTextColor(android.graphics.Color.RED)
            }
        }
    }
}