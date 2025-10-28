package com.dicoding.didapp.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.didapp.R
import com.dicoding.didapp.network.ApiClient
import com.dicoding.didapp.utils.ServerPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Server Settings Activity untuk mengatur IP address server
 */
class ServerSettingsActivity : AppCompatActivity() {
    
    private lateinit var serverPreferences: ServerPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_settings)
        
        serverPreferences = ServerPreferences(this)
        setupUI()
        loadCurrentSettings()
    }
    
    private fun setupUI() {
        // Setup toolbar
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { finish() }
        }
        
        // Setup buttons
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_save).setOnClickListener {
            saveServerSettings()
        }
        
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_test_connection).setOnClickListener {
            testServerConnection()
        }
        
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_reset_default).setOnClickListener {
            resetToDefault()
        }
        
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_scan_network).setOnClickListener {
            scanForServers()
        }
    }
    
    private fun loadCurrentSettings() {
        val currentIp = serverPreferences.getServerIp()
        val currentPort = serverPreferences.getServerPort()
        
        findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_server_ip).setText(currentIp)
        findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_server_port).setText(currentPort.toString())
        
        updateConnectionStatus()
    }
    
    private fun saveServerSettings() {
        val ip = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_server_ip).text.toString().trim()
        val portText = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_server_port).text.toString().trim()
        
        if (ip.isEmpty()) {
            Toast.makeText(this, "IP address cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (portText.isEmpty()) {
            Toast.makeText(this, "Port cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        
        val port = try {
            portText.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid port number", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (port < 1 || port > 65535) {
            Toast.makeText(this, "Port must be between 1 and 65535", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Save settings
        serverPreferences.setServerIp(ip)
        serverPreferences.setServerPort(port)
        
        // Note: ApiClient uses Config.kt for URLs, no need to update here
        
        Toast.makeText(this, "Server settings saved successfully!", Toast.LENGTH_SHORT).show()
        updateConnectionStatus()
    }
    
    private fun testServerConnection() {
        val ip = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_server_ip).text.toString().trim()
        val portText = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_server_port).text.toString().trim()
        
        if (ip.isEmpty() || portText.isEmpty()) {
            Toast.makeText(this, "Please enter IP and port first", Toast.LENGTH_SHORT).show()
            return
        }
        
        val port = try {
            portText.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid port number", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Test connection using the specified IP and port
        // Note: ApiClient uses Config.kt for URLs
        
        CoroutineScope(Dispatchers.Main).launch {
            findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_test_connection).isEnabled = false
            findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_test_connection).text = "Testing..."
            
            try {
                val isConnected = withContext(Dispatchers.IO) {
                    ApiClient.checkHealth()
                }
                
                if (isConnected) {
                    Toast.makeText(this@ServerSettingsActivity, 
                        "✅ Connection successful!", Toast.LENGTH_LONG).show()
                    updateConnectionStatus(true)
                } else {
                    Toast.makeText(this@ServerSettingsActivity, 
                        "❌ Connection failed - Server not responding", Toast.LENGTH_LONG).show()
                    updateConnectionStatus(false)
                }
            } catch (e: Exception) {
                Toast.makeText(this@ServerSettingsActivity, 
                    "❌ Connection failed: ${e.message}", Toast.LENGTH_LONG).show()
                updateConnectionStatus(false)
            } finally {
                // Note: ApiClient uses Config.kt for URLs
                findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_test_connection).isEnabled = true
                findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_test_connection).text = "Test Connection"
            }
        }
    }
    
    private fun resetToDefault() {
        serverPreferences.setServerIp("31.97.108.98")
        serverPreferences.setServerPort(8000)
        
        findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_server_ip).setText("31.97.108.98")
        findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_server_port).setText("8000")
        
        // Note: ApiClient uses Config.kt for URLs
        
        Toast.makeText(this, "Reset to default settings", Toast.LENGTH_SHORT).show()
        updateConnectionStatus()
    }
    
    private fun scanForServers() {
        Toast.makeText(this, "Network scanning feature coming soon!", Toast.LENGTH_SHORT).show()
        // TODO: Implement network scanning
    }
    
    private fun updateConnectionStatus(isConnected: Boolean? = null) {
        val statusText = findViewById<android.widget.TextView>(R.id.tv_connection_status)
        val statusIcon = findViewById<android.widget.ImageView>(R.id.iv_connection_status)
        
        if (isConnected == true) {
            statusText.text = "✅ Connected"
            statusText.setTextColor(getColor(R.color.success))
            statusIcon.setImageResource(R.drawable.ic_check_circle)
            statusIcon.setColorFilter(getColor(R.color.success))
        } else if (isConnected == false) {
            statusText.text = "❌ Disconnected"
            statusText.setTextColor(getColor(R.color.error))
            statusIcon.setImageResource(R.drawable.ic_error_circle)
            statusIcon.setColorFilter(getColor(R.color.error))
        } else {
            statusText.text = "⏳ Checking..."
            statusText.setTextColor(getColor(R.color.warning))
            statusIcon.setImageResource(R.drawable.ic_refresh)
            statusIcon.setColorFilter(getColor(R.color.warning))
        }
    }
}
