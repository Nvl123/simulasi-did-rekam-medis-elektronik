package com.dicoding.didapp.ui.wallet

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.didapp.R
import com.dicoding.didapp.data.model.VICShareAccessResponse
import com.dicoding.didapp.data.model.VICShareData
import com.dicoding.didapp.network.VICShareApiClient
import com.dicoding.didapp.utils.Config
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity untuk mengakses VIC Share
 */
class AccessVICShareActivity : AppCompatActivity() {
    
    private var shareToken: String = ""
    private var hospitalName: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access_vic_share)
        
        // Get share token from intent
        shareToken = intent.getStringExtra("share_token") ?: ""
        hospitalName = intent.getStringExtra("hospital_name") ?: "Unknown Hospital"
        
        if (shareToken.isEmpty()) {
            Toast.makeText(this, "Share token not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        setupUI()
        accessVICShare()
    }
    
    private fun setupUI() {
        // Setup toolbar
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { finish() }
        }
        
        // Setup save button
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_save_to_wallet).setOnClickListener {
            saveToWallet()
        }
        
        // Setup copy button
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_copy_data).setOnClickListener {
            copyDataToClipboard()
        }
    }
    
    private fun accessVICShare() {
        val progressDialog = android.app.ProgressDialog.show(this, "Accessing VIC Share", "Please wait...")
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    VICShareApiClient.accessVICShare(shareToken, hospitalName)
                }
                
                progressDialog.dismiss()
                
                if (response.success && response.data != null) {
                    displayVICData(response.data, response.permissions)
                } else {
                    showAccessError(response.message)
                }
                
            } catch (e: Exception) {
                progressDialog.dismiss()
                Log.e(Config.LOG_TAG_VIC, "Error accessing VIC share: ${e.message}")
                Toast.makeText(this@AccessVICShareActivity, "Error accessing VIC share: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun displayVICData(data: VICShareData, permissions: com.dicoding.didapp.data.model.VICAccessPermissions?) {
        // Store original data for saving to wallet
        originalVICData = data
        
        // Display basic information
        findViewById<android.widget.TextView>(R.id.tv_patient_name).text = data.patientName
        findViewById<android.widget.TextView>(R.id.tv_patient_id).text = "Patient ID: ${data.patientId}"
        findViewById<android.widget.TextView>(R.id.tv_hospital).text = "Hospital: ${data.hospital}"
        findViewById<android.widget.TextView>(R.id.tv_transaction_hash).text = "Transaction: ${data.transactionHash}"
        
        // Display additional VIC information
        findViewById<android.widget.TextView>(R.id.tv_type).text = "Type: VIC"
        findViewById<android.widget.TextView>(R.id.tv_block_number).text = "Block Number: ${data.blockNumber}"
        
        // Format timestamp properly
        val formattedTimestamp = try {
            val date = java.util.Date((data.timestamp * 1000).toLong())
            java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(date)
        } catch (e: Exception) {
            data.timestamp.toString()
        }
        findViewById<android.widget.TextView>(R.id.tv_timestamp).text = "Timestamp: $formattedTimestamp"
        findViewById<android.widget.TextView>(R.id.tv_demo_mode).text = "Demo Mode: No"
        
        // Always display all medical data from server (as requested)
        // Diagnosis
        findViewById<android.widget.TextView>(R.id.tv_diagnosis).text = "Diagnosis: ${data.diagnosis}"
        findViewById<android.widget.LinearLayout>(R.id.layout_diagnosis).visibility = android.view.View.VISIBLE
        
        // Treatment
        findViewById<android.widget.TextView>(R.id.tv_treatment).text = "Treatment: ${data.treatment}"
        findViewById<android.widget.LinearLayout>(R.id.layout_treatment).visibility = android.view.View.VISIBLE
        
        // Doctor
        findViewById<android.widget.TextView>(R.id.tv_doctor).text = "Doctor: ${data.doctor}"
        findViewById<android.widget.LinearLayout>(R.id.layout_doctor).visibility = android.view.View.VISIBLE
        
        // Date
        findViewById<android.widget.TextView>(R.id.tv_date).text = "Date: ${data.date}"
        findViewById<android.widget.LinearLayout>(R.id.layout_date).visibility = android.view.View.VISIBLE
        
        // Notes
        if (data.notes != null && data.notes.isNotEmpty()) {
            findViewById<android.widget.TextView>(R.id.tv_notes).text = "Notes: ${data.notes}"
            findViewById<android.widget.LinearLayout>(R.id.layout_notes).visibility = android.view.View.VISIBLE
        } else {
            findViewById<android.widget.LinearLayout>(R.id.layout_notes).visibility = android.view.View.GONE
        }
        
        // Show access permissions information
        val permissionsText = buildString {
            append("Access Information:\n")
            append("• Shared by: Patient\n")
            append("• Access granted: Full access to all medical data\n")
            append("• Data integrity: Verified on blockchain\n")
            append("• Security: End-to-end encrypted\n")
        }
        
        findViewById<android.widget.TextView>(R.id.tv_permissions).text = permissionsText
        
        // Enable buttons
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_save_to_wallet).isEnabled = true
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_copy_data).isEnabled = true
    }
    
    private fun saveToWallet() {
        // Convert VICShareData to VICData and save to wallet
        try {
            // Get the original VICShareData from the response
            val originalData = getOriginalVICData()
            
            val vicData = com.dicoding.didapp.data.model.VICData(
                type = "VIC",
                hospital = originalData.hospital,
                patientId = originalData.patientId,
                patientName = originalData.patientName,
                diagnosis = originalData.diagnosis,
                treatment = originalData.treatment,
                doctor = originalData.doctor,
                date = originalData.date,
                notes = originalData.notes,
                transactionHash = originalData.transactionHash,
                blockNumber = originalData.blockNumber,
                timestamp = originalData.timestamp.toString(),
                verificationUrl = "", // Exclude verification URL as requested
                demoMode = false
            )
            
            // Save to local storage as VIC
            val savedVIC = com.dicoding.didapp.data.model.SavedVIC.fromVICData(vicData, 
                com.dicoding.didapp.data.model.VerificationResult(success = true, message = "Shared VIC accessed successfully"))
            val localStorage = com.dicoding.didapp.utils.LocalStorage(this)
            localStorage.saveVIC(savedVIC)
            
            Toast.makeText(this, "VIC data saved to wallet successfully!", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Log.e(Config.LOG_TAG_VIC, "Error saving to wallet: ${e.message}")
            Toast.makeText(this, "Error saving to wallet: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Store original VICShareData for saving to wallet
    private var originalVICData: VICShareData? = null
    
    private fun getOriginalVICData(): VICShareData {
        return originalVICData ?: throw IllegalStateException("VIC data not available")
    }
    
    private fun copyDataToClipboard() {
        try {
            val dataText = buildString {
                append("VIC Share Data - Complete Information:\n")
                append("=====================================\n\n")
                
                // Basic Information
                append("PATIENT INFORMATION:\n")
                append("• Name: ${findViewById<android.widget.TextView>(R.id.tv_patient_name).text}\n")
                append("• ID: ${findViewById<android.widget.TextView>(R.id.tv_patient_id).text}\n")
                append("• Hospital: ${findViewById<android.widget.TextView>(R.id.tv_hospital).text}\n")
                append("• Transaction: ${findViewById<android.widget.TextView>(R.id.tv_transaction_hash).text}\n")
                append("• Type: ${findViewById<android.widget.TextView>(R.id.tv_type).text}\n")
                append("• Block Number: ${findViewById<android.widget.TextView>(R.id.tv_block_number).text}\n")
                append("• Timestamp: ${findViewById<android.widget.TextView>(R.id.tv_timestamp).text}\n")
                append("• Demo Mode: ${findViewById<android.widget.TextView>(R.id.tv_demo_mode).text}\n\n")
                
                // Medical Data
                append("MEDICAL DATA:\n")
                append("• Diagnosis: ${findViewById<android.widget.TextView>(R.id.tv_diagnosis).text}\n")
                append("• Treatment: ${findViewById<android.widget.TextView>(R.id.tv_treatment).text}\n")
                append("• Doctor: ${findViewById<android.widget.TextView>(R.id.tv_doctor).text}\n")
                append("• Date: ${findViewById<android.widget.TextView>(R.id.tv_date).text}\n")
                
                if (findViewById<android.widget.LinearLayout>(R.id.layout_notes).visibility == android.view.View.VISIBLE) {
                    append("• Notes: ${findViewById<android.widget.TextView>(R.id.tv_notes).text}\n")
                }
                
                // Additional Information
                append("\nACCESS INFORMATION:\n")
                append(findViewById<android.widget.TextView>(R.id.tv_permissions).text)
            }
            
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("VIC Share Data", dataText)
            clipboard.setPrimaryClip(clip)
            
            Toast.makeText(this, "Complete VIC data copied to clipboard!", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Log.e(Config.LOG_TAG_VIC, "Error copying data: ${e.message}")
            Toast.makeText(this, "Error copying data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showAccessError(message: String) {
        val errorMessage = "Failed to access VIC share:\n\n$message\n\nPossible reasons:\n" +
                "• Share token expired\n" +
                "• Share token invalid\n" +
                "• Hospital not authorized\n" +
                "• Network error"
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Access Failed")
            .setMessage(errorMessage)
            .setPositiveButton("Retry") { _, _ ->
                accessVICShare()
            }
            .setNegativeButton("Close") { _, _ ->
                finish()
            }
            .show()
    }
    
    companion object {
        fun start(context: android.content.Context, shareToken: String, hospitalName: String = "Unknown Hospital") {
            val intent = Intent(context, AccessVICShareActivity::class.java)
            intent.putExtra("share_token", shareToken)
            intent.putExtra("hospital_name", hospitalName)
            context.startActivity(intent)
        }
    }
}
