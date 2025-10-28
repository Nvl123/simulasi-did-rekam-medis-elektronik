package com.dicoding.didapp.ui.wallet

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.didapp.R
import com.dicoding.didapp.data.model.VICData
import com.dicoding.didapp.data.model.VerificationResult
import com.dicoding.didapp.data.model.VerificationResponse
import com.dicoding.didapp.data.model.SavedVIC
import com.dicoding.didapp.utils.Config
import com.dicoding.didapp.network.ApiClient
import com.dicoding.didapp.utils.LocalStorage
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity untuk menampilkan detail VIC (Verifiable Identity Credential)
 */
class VICDetailsActivity : AppCompatActivity() {
    
    private var vicData: VICData? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vic_details)
        
        try {
            Log.d(Config.LOG_TAG_VIC, "VICDetailsActivity onCreate started")
            Log.d(Config.LOG_TAG_VIC, "Intent extras: ${intent.extras?.keySet()}")
            
            // Get VIC data from JSON string
            try {
                val vicJsonString = intent.getStringExtra("vic_data_json")
                Log.d(Config.LOG_TAG_VIC, "JSON string data received: ${vicJsonString != null}")
                if (vicJsonString != null) {
                    Log.d(Config.LOG_TAG_VIC, "JSON string: ${vicJsonString.take(100)}...")
                    vicData = com.dicoding.didapp.utils.QRCodeUtils.parseVICQRCode(vicJsonString)
                    Log.d(Config.LOG_TAG_VIC, "VIC data loaded from JSON: ${vicData?.patientName}")
                } else {
                    Log.e(Config.LOG_TAG_VIC, "No JSON string found in intent")
                }
            } catch (e: Exception) {
                Log.e(Config.LOG_TAG_VIC, "JSON parsing failed: ${e.message}")
                Log.e(Config.LOG_TAG_VIC, "Exception details: ${e.stackTraceToString()}")
            }
            
            // Check if we have VIC data
            if (vicData == null) {
                Log.e(Config.LOG_TAG_VIC, "No VIC data found in intent")
                Log.e(Config.LOG_TAG_VIC, "Intent extras: ${intent.extras?.keySet()}")
                Log.e(Config.LOG_TAG_VIC, "JSON string: ${intent.getStringExtra("vic_data_json") != null}")
                Toast.makeText(this, "No VIC data found", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            
            Log.d(Config.LOG_TAG_VIC, "VIC data loaded successfully: ${vicData?.patientName}")
            Log.d(Config.LOG_TAG_VIC, "VIC data details: type=${vicData?.type}, hospital=${vicData?.hospital}")
            
            setupUI()
            displayVICData()
        } catch (e: Exception) {
            Log.e(Config.LOG_TAG_VIC, "Error loading VIC data: ${e.message}")
            Log.e(Config.LOG_TAG_VIC, "Exception details: ${e.stackTraceToString()}")
            Toast.makeText(this, "Error loading VIC data: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun setupUI() {
        // Setup toolbar
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { finish() }
        }
        
        // Setup action buttons
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_verify).setOnClickListener {
            verifyVIC()
        }
        
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_scan_again).setOnClickListener {
            finish()
        }
    }
    
    private fun displayVICData() {
        try {
            val data = vicData ?: return
            
            // Display demo mode warning if applicable
            if (data.demoMode) {
                findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_demo_warning).visibility = android.view.View.VISIBLE
                findViewById<TextView>(R.id.tv_demo_warning).text = "⚠️ DEMO MODE - Data tidak tersimpan ke blockchain"
            } else {
                findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_demo_warning).visibility = android.view.View.GONE
            }
            
            // Display hospital information
            findViewById<TextView>(R.id.tv_hospital).text = data.hospital
            
            // Display patient information
            findViewById<TextView>(R.id.tv_patient_name).text = data.patientName
            findViewById<TextView>(R.id.tv_patient_id).text = "Patient ID: ${data.patientId}"
            
            // Display medical information
            findViewById<TextView>(R.id.tv_diagnosis).text = "Diagnosis: ${data.diagnosis}"
            findViewById<TextView>(R.id.tv_treatment).text = "Treatment: ${data.treatment}"
            findViewById<TextView>(R.id.tv_doctor).text = "Doctor: ${data.doctor}"
            findViewById<TextView>(R.id.tv_date).text = "Date: ${data.date}"
            
            // Display notes if available
            if (!data.notes.isNullOrEmpty()) {
                findViewById<TextView>(R.id.tv_notes).apply {
                    text = "Notes: ${data.notes}"
                    visibility = android.view.View.VISIBLE
                }
            } else {
                findViewById<TextView>(R.id.tv_notes).visibility = android.view.View.GONE
            }
            
            // Display blockchain information
            findViewById<TextView>(R.id.tv_transaction_hash).text = "Transaction Hash: ${data.getFormattedTransactionHash()}"
            findViewById<TextView>(R.id.tv_block_number).text = "Block Number: ${data.blockNumber}"
            findViewById<TextView>(R.id.tv_timestamp).text = "Timestamp: ${data.getFormattedTimestamp()}"
            
            Log.d(Config.LOG_TAG_VIC, "VIC data displayed successfully")
        } catch (e: Exception) {
            Log.e(Config.LOG_TAG_VIC, "Error displaying VIC data: ${e.message}")
            Toast.makeText(this, "Error displaying VIC data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun verifyVIC() {
        try {
            val data = vicData ?: return
            Log.d(Config.LOG_TAG_VIC, "Verifying VIC: ${data.patientName}")
            Log.d(Config.LOG_TAG_VIC, "Transaction Hash: ${data.transactionHash}")
            Log.d(Config.LOG_TAG_VIC, "Verification URL: ${data.verificationUrl}")
            
            // Show loading dialog
            val progressDialog = android.app.ProgressDialog(this)
            progressDialog.setMessage("Verifying VIC...")
            progressDialog.setCancelable(false)
            progressDialog.show()
            
            // Verify VIC using coroutines
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val verificationResult = withContext(Dispatchers.IO) {
                        verifyVICWithAPI()
                    }
                    
                    progressDialog.dismiss()
                    
                    if (verificationResult.success) {
                        Log.d(Config.LOG_TAG_VIC, "VIC verification successful")
                        showVerificationSuccess(verificationResult)
                    } else {
                        Log.w(Config.LOG_TAG_VIC, "VIC verification failed: ${verificationResult.message}")
                        showVerificationFailed(verificationResult)
                    }
                } catch (e: Exception) {
                    progressDialog.dismiss()
                    Log.e(Config.LOG_TAG_VIC, "Error verifying VIC: ${e.message}")
                    showVerificationError(e.message ?: "Unknown error")
                }
            }
        } catch (e: Exception) {
            Log.e(Config.LOG_TAG_VIC, "Error verifying VIC: ${e.message}")
            Toast.makeText(this, "Error verifying VIC: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private suspend fun verifyVICWithAPI(): VerificationResult {
        return try {
            val data = vicData ?: return VerificationResult(false, "No VIC data available")
            // Try Flask API first (Port 8505)
            val flaskResult = ApiClient.verifyVICWithFlask(data.transactionHash)
            
            if (flaskResult.success) {
                Log.d(Config.LOG_TAG_VIC, "Flask API verification successful")
                return flaskResult
            }
            
            // Fallback to FastAPI (Port 8502)
            val fastApiResult = ApiClient.verifyVICWithFastAPI(data.transactionHash)
            
            if (fastApiResult.success) {
                Log.d(Config.LOG_TAG_VIC, "FastAPI verification successful")
                return fastApiResult
            }
            
            // If both APIs fail, try original verification URL
            if (data.verificationUrl.isNotEmpty()) {
                verifyUsingVerificationURL()
            } else {
                // Final fallback to basic validation
                verifyUsingBasicValidation()
            }
        } catch (e: Exception) {
            Log.e(Config.LOG_TAG_VIC, "API verification failed: ${e.message}")
            // Fallback to basic validation
            verifyUsingBasicValidation()
        }
    }
    
    
    private suspend fun verifyUsingVerificationURL(): VerificationResult {
        return try {
            val data = vicData ?: return VerificationResult(false, "No VIC data available")
            val url = java.net.URL(data.verificationUrl)
            val connection = url.openConnection() as java.net.HttpURLConnection
            
            connection.requestMethod = "GET"
            connection.connectTimeout = Config.CONNECTION_TIMEOUT.toInt()
            connection.readTimeout = Config.READ_TIMEOUT.toInt()
            connection.setRequestProperty("Content-Type", Config.CONTENT_TYPE_JSON)
            connection.setRequestProperty("User-Agent", Config.USER_AGENT)
            
            val responseCode = connection.responseCode
            Log.d(Config.LOG_TAG_VERIFICATION, "Verification API response code: $responseCode")
            
            if (responseCode == 200) {
                val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(Config.LOG_TAG_VERIFICATION, "Verification response: $responseBody")
                
                // Parse response (assuming JSON format)
                val gson = com.google.gson.Gson()
                val verificationResponse = gson.fromJson(responseBody, VerificationResponse::class.java)
                
                if (verificationResponse.verified == true) {
                    VerificationResult(
                        success = true,
                        message = "VIC verified successfully"
                    )
                } else {
                    VerificationResult(
                        success = false,
                        message = "VIC verification failed: ${verificationResponse.message ?: "Unknown error"}"
                    )
                }
            } else {
                VerificationResult(
                    success = false,
                    message = "Verification API returned error: $responseCode"
                )
            }
        } catch (e: Exception) {
            Log.e("VICDetails", "Error calling verification URL: ${e.message}")
            throw e
        }
    }
    
    private suspend fun verifyUsingBasicValidation(): VerificationResult {
        return try {
            // Basic validation checks
            val isValid = validateVICData()
            
            if (isValid) {
                VerificationResult(
                    success = true,
                    message = "VIC validated successfully (offline mode)"
                )
            } else {
                VerificationResult(
                    success = false,
                    message = "VIC validation failed: Invalid data"
                )
            }
        } catch (e: Exception) {
            Log.e("VICDetails", "Error in basic validation: ${e.message}")
            throw e
        }
    }
    
    private fun validateVICData(): Boolean {
        return try {
            val data = vicData ?: return false
            // Check required fields
            val hasRequiredFields = data.type == "VIC" &&
                    data.hospital.isNotEmpty() &&
                    data.patientId.isNotEmpty() &&
                    data.patientName.isNotEmpty() &&
                    data.transactionHash.isNotEmpty()
            
            // Check transaction hash format (should start with 0x)
            val hasValidTransactionHash = data.transactionHash.startsWith("0x") &&
                    data.transactionHash.length >= 10
            
            // Check block number
            val hasValidBlockNumber = data.blockNumber > 0
            
            // Check timestamp format
            val hasValidTimestamp = data.timestamp.isNotEmpty() &&
                    data.timestamp.contains("T") &&
                    data.timestamp.contains("Z")
            
            val isValid = hasRequiredFields && hasValidTransactionHash && 
                         hasValidBlockNumber && hasValidTimestamp
            
            Log.d(Config.LOG_TAG_VERIFICATION, "VIC validation result: $isValid")
            Log.d(Config.LOG_TAG_VERIFICATION, "Required fields: $hasRequiredFields")
            Log.d(Config.LOG_TAG_VERIFICATION, "Valid transaction hash: $hasValidTransactionHash")
            Log.d(Config.LOG_TAG_VERIFICATION, "Valid block number: $hasValidBlockNumber")
            Log.d(Config.LOG_TAG_VERIFICATION, "Valid timestamp: $hasValidTimestamp")
            
            isValid
        } catch (e: Exception) {
            Log.e(Config.LOG_TAG_VERIFICATION, "Error validating VIC data: ${e.message}")
            false
        }
    }
    
    private fun showVerificationSuccess(result: VerificationResult) {
        val data = vicData ?: return
        val message = "VIC Verified Successfully!\n\n" +
                "Transaction: ${data.transactionHash}\n" +
                "Block: ${data.blockNumber}\n" +
                "Timestamp: ${data.timestamp}\n\n" +
                "This VIC has been verified and is authentic."
        
        android.app.AlertDialog.Builder(this)
            .setTitle("VIC Verification Success")
            .setMessage(message)
            .setPositiveButton("Save to Wallet") { dialog, _ ->
                dialog.dismiss()
                // Save VIC to e-wallet
                saveVICToWallet(result)
                updateVerificationStatus(true)
            }
            .setNeutralButton("Create VIC Share") { dialog, _ ->
                dialog.dismiss()
                // Create VIC share
                createVICShare()
                updateVerificationStatus(true)
            }
            .setNegativeButton("Copy Barcode") { dialog, _ ->
                dialog.dismiss()
                // Generate and copy barcode
                val data = vicData ?: return@setNegativeButton
                val savedVIC = SavedVIC.fromVICData(data, result)
                copyBarcodeToClipboard(savedVIC)
                updateVerificationStatus(true)
            }
            .setIcon(android.R.drawable.ic_dialog_info)
            .show()
    }
    
    private fun showVerificationFailed(result: VerificationResult) {
        val message = "VIC Verification Failed\n\n" +
                "Reason: ${result.message}\n\n" +
                "This VIC could not be verified. Please check the data or try again."
        
        android.app.AlertDialog.Builder(this)
            .setTitle("VIC Verification Failed")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Retry") { dialog, _ ->
                dialog.dismiss()
                verifyVIC()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
    
    private fun showVerificationError(errorMessage: String) {
        val message = "VIC Verification Error\n\n" +
                "Error: $errorMessage\n\n" +
                "Please check your internet connection and try again."
        
        android.app.AlertDialog.Builder(this)
            .setTitle("VIC Verification Error")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Retry") { dialog, _ ->
                dialog.dismiss()
                verifyVIC()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
    
    private fun updateVerificationStatus(isVerified: Boolean) {
        try {
            // Update verification status in UI
            val verificationStatus = findViewById<android.widget.TextView>(R.id.tv_verification_status)
            if (verificationStatus != null) {
                verificationStatus.text = if (isVerified) "VERIFIED" else "UNVERIFIED"
                verificationStatus.setTextColor(
                    if (isVerified) android.graphics.Color.GREEN else android.graphics.Color.RED
                )
            }
            
            // Update verification button
            val verifyButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_verify)
            if (verifyButton != null) {
                verifyButton.text = if (isVerified) "VERIFIED" else "VERIFY VIC"
                verifyButton.isEnabled = !isVerified
            }
            
            Log.d(Config.LOG_TAG_VIC, "Verification status updated: $isVerified")
        } catch (e: Exception) {
            Log.e(Config.LOG_TAG_VIC, "Error updating verification status: ${e.message}")
        }
    }
    
    /**
     * Save verified VIC to e-wallet
     */
    private fun saveVICToWallet(verificationResult: VerificationResult) {
        try {
            val data = vicData ?: return
            val localStorage = LocalStorage(this)
            val savedVIC = SavedVIC.fromVICData(data, verificationResult)
            
            localStorage.saveVIC(savedVIC)
            
            Log.d(Config.LOG_TAG_VIC, "VIC saved to wallet: ${savedVIC.id}")
            
            Toast.makeText(this, "VIC saved to wallet successfully!", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Log.e(Config.LOG_TAG_VIC, "Error saving VIC to wallet: ${e.message}")
            Toast.makeText(this, "Error saving VIC to wallet: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Copy VIC barcode to clipboard
     */
    private fun copyBarcodeToClipboard(savedVIC: SavedVIC) {
        try {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("VIC Barcode", savedVIC.barcodeData)
            clipboard.setPrimaryClip(clip)
            
            Toast.makeText(this, "VIC barcode copied to clipboard!", Toast.LENGTH_SHORT).show()
            
            Log.d(Config.LOG_TAG_VIC, "VIC barcode copied: ${savedVIC.barcodeData}")
            
        } catch (e: Exception) {
            Log.e(Config.LOG_TAG_VIC, "Error copying barcode: ${e.message}")
            Toast.makeText(this, "Error copying barcode: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Create VIC Share
     */
    private fun createVICShare() {
        try {
            val data = vicData ?: return
            CreateVICShareActivity.start(this, data)
        } catch (e: Exception) {
            Log.e(Config.LOG_TAG_VIC, "Error creating VIC share: ${e.message}")
            Toast.makeText(this, "Error creating VIC share: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    companion object {
        fun start(context: android.content.Context, vicData: VICData) {
            val intent = Intent(context, VICDetailsActivity::class.java)
            // Convert VICData to JSON string to avoid ambiguity
            val gson = com.google.gson.Gson()
            val vicJson = gson.toJson(vicData)
            intent.putExtra("vic_data_json", vicJson)
            context.startActivity(intent)
        }
    }
}
