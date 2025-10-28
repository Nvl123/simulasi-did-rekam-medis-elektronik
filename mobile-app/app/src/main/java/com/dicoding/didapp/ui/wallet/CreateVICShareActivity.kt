package com.dicoding.didapp.ui.wallet

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.didapp.R
import com.dicoding.didapp.data.model.VICData
import com.dicoding.didapp.data.model.VICShareRequest
import com.dicoding.didapp.data.model.VICShareResponse
import com.dicoding.didapp.data.model.VICAccessPermissions
import com.dicoding.didapp.network.VICShareApiClient
import com.dicoding.didapp.utils.Config
import com.dicoding.didapp.utils.QRCodeUtils
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity untuk membuat VIC Share
 */
class CreateVICShareActivity : AppCompatActivity() {
    
    private lateinit var vicData: VICData
    private var shareResponse: VICShareResponse? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_vic_share)
        
        Log.d(Config.LOG_TAG_VIC, "CreateVICShareActivity onCreate started")
        
        // Get VIC data from intent
        val vicJson = intent.getStringExtra("vic_data_json")
        Log.d(Config.LOG_TAG_VIC, "VIC JSON from intent: ${vicJson != null}")
        
        if (vicJson != null) {
            try {
                val gson = com.google.gson.Gson()
                vicData = gson.fromJson(vicJson, VICData::class.java)
                Log.d(Config.LOG_TAG_VIC, "VICData loaded successfully: ${vicData.patientName}")
            } catch (e: Exception) {
                Log.e(Config.LOG_TAG_VIC, "Error parsing VIC JSON: ${e.message}")
                Toast.makeText(this, "Error parsing VIC data: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
        } else {
            Log.e(Config.LOG_TAG_VIC, "VIC data not found in intent")
            Toast.makeText(this, "VIC data not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        Log.d(Config.LOG_TAG_VIC, "Setting up UI")
        setupUI()
        
        Log.d(Config.LOG_TAG_VIC, "Loading VIC data to UI")
        loadVICData()
        
        Log.d(Config.LOG_TAG_VIC, "CreateVICShareActivity onCreate completed")
    }
    
    private fun setupUI() {
        Log.d(Config.LOG_TAG_VIC, "Setting up UI elements")
        
        // Setup toolbar
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener { finish() }
            Log.d(Config.LOG_TAG_VIC, "Toolbar setup completed")
        } else {
            Log.e(Config.LOG_TAG_VIC, "Toolbar not found")
        }
        
        // Setup create share button
        val createShareButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_create_share)
        if (createShareButton != null) {
            createShareButton.setOnClickListener {
                Log.d(Config.LOG_TAG_VIC, "Create VIC Share button clicked")
                Toast.makeText(this, "Create VIC Share button clicked", Toast.LENGTH_SHORT).show()
                createVICShare()
            }
            Log.d(Config.LOG_TAG_VIC, "Create share button setup completed")
        } else {
            Log.e(Config.LOG_TAG_VIC, "Create share button not found")
        }
        
        // Setup share options button
        val shareOptionsButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_share_options)
        if (shareOptionsButton != null) {
            shareOptionsButton.setOnClickListener {
                showShareOptions()
            }
            Log.d(Config.LOG_TAG_VIC, "Share options button setup completed")
        } else {
            Log.e(Config.LOG_TAG_VIC, "Share options button not found")
        }
        
        // Setup select all permissions button
        val selectAllButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_select_all_permissions)
        if (selectAllButton != null) {
            selectAllButton.setOnClickListener {
                toggleAllPermissions()
            }
            Log.d(Config.LOG_TAG_VIC, "Select all permissions button setup completed")
        } else {
            Log.e(Config.LOG_TAG_VIC, "Select all permissions button not found")
        }
        
        Log.d(Config.LOG_TAG_VIC, "UI setup completed")
    }
    
    private fun loadVICData() {
        Log.d(Config.LOG_TAG_VIC, "Loading VIC data to UI")
        Log.d(Config.LOG_TAG_VIC, "VIC Data details:")
        Log.d(Config.LOG_TAG_VIC, "  - Patient Name: ${vicData.patientName}")
        Log.d(Config.LOG_TAG_VIC, "  - Patient ID: ${vicData.patientId}")
        Log.d(Config.LOG_TAG_VIC, "  - Hospital: ${vicData.hospital}")
        Log.d(Config.LOG_TAG_VIC, "  - Diagnosis: ${vicData.diagnosis}")
        Log.d(Config.LOG_TAG_VIC, "  - Treatment: ${vicData.treatment}")
        Log.d(Config.LOG_TAG_VIC, "  - Doctor: ${vicData.doctor}")
        Log.d(Config.LOG_TAG_VIC, "  - Date: ${vicData.date}")
        Log.d(Config.LOG_TAG_VIC, "  - Transaction Hash: ${vicData.transactionHash}")
        Log.d(Config.LOG_TAG_VIC, "  - Block Number: ${vicData.blockNumber}")
        Log.d(Config.LOG_TAG_VIC, "  - Timestamp: ${vicData.timestamp}")
        Log.d(Config.LOG_TAG_VIC, "  - Demo Mode: ${vicData.demoMode}")
        
        // Display VIC information with null checks
        findViewById<android.widget.TextView>(R.id.tv_patient_name).text = vicData.patientName.ifEmpty { "N/A" }
        findViewById<android.widget.TextView>(R.id.tv_patient_id).text = vicData.patientId.ifEmpty { "N/A" }
        findViewById<android.widget.TextView>(R.id.tv_hospital).text = vicData.hospital.ifEmpty { "N/A" }
        findViewById<android.widget.TextView>(R.id.tv_diagnosis).text = vicData.diagnosis.ifEmpty { "N/A" }
        findViewById<android.widget.TextView>(R.id.tv_treatment).text = vicData.treatment.ifEmpty { "N/A" }
        findViewById<android.widget.TextView>(R.id.tv_doctor).text = vicData.doctor.ifEmpty { "N/A" }
        findViewById<android.widget.TextView>(R.id.tv_date).text = vicData.date.ifEmpty { "N/A" }
        findViewById<android.widget.TextView>(R.id.tv_transaction_hash).text = vicData.getFormattedTransactionHash()
        
        Log.d(Config.LOG_TAG_VIC, "VIC data loaded to UI successfully")
    }
    
    private fun createVICShare() {
        Log.d(Config.LOG_TAG_VIC, "CreateVICShare button clicked")
        
        // Check if vicData is initialized
        if (!::vicData.isInitialized) {
            Log.e(Config.LOG_TAG_VIC, "VICData is not initialized")
            showInputError("VIC data is not available. Please restart the activity.")
            return
        }
        
        // Validate input data first
        if (!validateInputData()) {
            Log.e(Config.LOG_TAG_VIC, "Input validation failed")
            return
        }
        
        Log.d(Config.LOG_TAG_VIC, "Input validation passed, starting VIC share creation")
        
        val progressDialog = android.app.ProgressDialog.show(this, "Creating VIC Share", "Please wait...")
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Get sharing options from UI with validation
                val sharedByEditText = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_shared_by)
                val expiresInHoursEditText = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_expires_hours)
                val sharedWithHospitalEditText = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_shared_with_hospital)
                
                if (sharedByEditText == null || expiresInHoursEditText == null || sharedWithHospitalEditText == null) {
                    progressDialog.dismiss()
                    Log.e(Config.LOG_TAG_VIC, "UI elements not found: sharedBy=$sharedByEditText, expiresInHours=$expiresInHoursEditText, sharedWithHospital=$sharedWithHospitalEditText")
                    showInputError("UI elements not found. Please restart the activity.")
                    return@launch
                }
                
                val sharedBy = sharedByEditText.text.toString().trim().takeIf { it.isNotEmpty() } ?: "Patient"
                val expiresInHours = expiresInHoursEditText.text.toString().toIntOrNull() ?: 24
                val sharedWithHospital = sharedWithHospitalEditText.text.toString().trim().takeIf { it.isNotEmpty() }
                
                Log.d(Config.LOG_TAG_VIC, "Sharing options: sharedBy=$sharedBy, expiresInHours=$expiresInHours, sharedWithHospital=$sharedWithHospital")
                
                // Validate expiration hours (1-8760 hours as per checklist)
                if (expiresInHours < 1 || expiresInHours > 8760) {
                    progressDialog.dismiss()
                    Log.e(Config.LOG_TAG_VIC, "Invalid expiration hours: $expiresInHours")
                    showInputError("Expiration hours must be between 1 and 8760 hours")
                    return@launch
                }
                
                // Get permissions with null check
                val diagnosisCheckbox = findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.cb_diagnosis)
                val treatmentCheckbox = findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.cb_treatment)
                val doctorCheckbox = findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.cb_doctor)
                val dateCheckbox = findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.cb_date)
                val notesCheckbox = findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.cb_notes)
                
                if (diagnosisCheckbox == null || treatmentCheckbox == null || doctorCheckbox == null || 
                    dateCheckbox == null || notesCheckbox == null) {
                    progressDialog.dismiss()
                    Log.e(Config.LOG_TAG_VIC, "Permission checkboxes not found")
                    showInputError("Permission checkboxes not found. Please restart the activity.")
                    return@launch
                }
                
                val permissions = VICAccessPermissions(
                    diagnosis = diagnosisCheckbox.isChecked,
                    treatment = treatmentCheckbox.isChecked,
                    doctor = doctorCheckbox.isChecked,
                    date = dateCheckbox.isChecked,
                    notes = notesCheckbox.isChecked
                )
                
                Log.d(Config.LOG_TAG_VIC, "Permissions: diagnosis=${permissions.diagnosis}, treatment=${permissions.treatment}, doctor=${permissions.doctor}, date=${permissions.date}, notes=${permissions.notes}")
                
                // Validate that at least one permission is selected
                if (!permissions.diagnosis && !permissions.treatment && !permissions.doctor && !permissions.date && !permissions.notes) {
                    progressDialog.dismiss()
                    Log.e(Config.LOG_TAG_VIC, "No permissions selected")
                    showInputError("Please select at least one permission for sharing")
                    return@launch
                }
                
                // Create VIC share request
                val request = VICShareRequest(
                    transactionHash = vicData.transactionHash,
                    patientId = vicData.patientId,
                    sharedBy = sharedBy,
                    sharedWithHospital = sharedWithHospital,
                    expiresInHours = expiresInHours,
                    accessPermissions = permissions
                )
                
                Log.d(Config.LOG_TAG_VIC, "Creating VIC share request: $request")
                
                // Call API with timeout handling
                Log.d(Config.LOG_TAG_VIC, "Calling VICShareApiClient.createVICShare")
                val response = withContext(Dispatchers.IO) {
                    try {
                        val result = VICShareApiClient.createVICShare(request)
                        Log.d(Config.LOG_TAG_VIC, "API call successful: success=${result.success}, message=${result.message}")
                        result
                    } catch (e: java.net.SocketTimeoutException) {
                        Log.e(Config.LOG_TAG_VIC, "API timeout: ${e.message}")
                        VICShareResponse(false, null, null, "Request timeout. Please check your connection and try again.")
                    } catch (e: java.net.UnknownHostException) {
                        Log.e(Config.LOG_TAG_VIC, "Network error: ${e.message}")
                        VICShareResponse(false, null, null, "Network error. Please check your internet connection.")
                    } catch (e: Exception) {
                        Log.e(Config.LOG_TAG_VIC, "API error: ${e.message}")
                        Log.e(Config.LOG_TAG_VIC, "Exception details: ${e.stackTraceToString()}")
                        VICShareResponse(false, null, null, "Server error: ${e.message}")
                    }
                }
                
                progressDialog.dismiss()
                
                if (response.success) {
                    shareResponse = response
                    Log.d(Config.LOG_TAG_VIC, "VIC share created successfully: ${response.shareToken}")
                    showShareSuccess(response)
                } else {
                    val errorMessage = response.message ?: "Unknown error occurred"
                    Log.e(Config.LOG_TAG_VIC, "Failed to create VIC share: $errorMessage")
                    showShareError(errorMessage)
                }
                
            } catch (e: Exception) {
                progressDialog.dismiss()
                Log.e(Config.LOG_TAG_VIC, "Unexpected error creating VIC share: ${e.message}")
                Log.e(Config.LOG_TAG_VIC, "Exception details: ${e.stackTraceToString()}")
                showShareError("Unexpected error: ${e.message}")
            }
        }
    }
    
    private fun showShareSuccess(response: VICShareResponse) {
        val message = "VIC Share created successfully!\n\n" +
                "Share Token: ${response.shareToken}\n" +
                "Expires: ${response.expiresAt}\n\n" +
                "You can now share this VIC with other hospitals."
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("VIC Share Created")
            .setMessage(message)
            .setPositiveButton("Generate QR Code") { _, _ ->
                generateQRCode(response)
            }
            .setNeutralButton("Copy Share Token") { _, _ ->
                copyShareToken(response.shareToken ?: "")
            }
            .setNegativeButton("Close") { _, _ ->
                finish()
            }
            .show()
    }
    
    private fun generateQRCode(response: VICShareResponse) {
        try {
            val qrData = com.dicoding.didapp.data.model.VICShareQRData(
                shareToken = response.shareToken ?: "",
                hospital = vicData.hospital,
                expiresAt = response.expiresAt ?: ""
            )
            
            val gson = com.google.gson.Gson()
            val qrJson = gson.toJson(qrData)
            
            // Show QR code dialog with JSON data
            showQRCodeDialog(qrJson, response.shareToken ?: "")
            
        } catch (e: Exception) {
            Log.e(Config.LOG_TAG_VIC, "Error generating QR code: ${e.message}")
            Toast.makeText(this, "Error generating QR code: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showQRCodeDialog(qrJson: String, shareToken: String) {
        val message = "VIC Share created successfully!\n\nShare Token: $shareToken\n\nQR Data: $qrJson"
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("VIC Share Created")
            .setMessage(message)
            .setPositiveButton("Copy Token") { _, _ ->
                copyShareToken(shareToken)
            }
            .setNegativeButton("Close", null)
            .show()
    }
    
    
    private fun copyShareToken(shareToken: String) {
        try {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("VIC Share Token", shareToken)
            clipboard.setPrimaryClip(clip)
            
            Toast.makeText(this, "Share token copied to clipboard!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(Config.LOG_TAG_VIC, "Error copying share token: ${e.message}")
            Toast.makeText(this, "Error copying share token: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun validateInputData(): Boolean {
        Log.d(Config.LOG_TAG_VIC, "Validating input data")
        Log.d(Config.LOG_TAG_VIC, "VIC data: transactionHash=${vicData.transactionHash}, patientId=${vicData.patientId}, patientName=${vicData.patientName}")
        
        // Validate VIC data
        if (vicData.transactionHash.isEmpty()) {
            Log.e(Config.LOG_TAG_VIC, "Validation failed: Transaction hash is empty")
            showInputError("Invalid VIC data: Transaction hash is empty")
            return false
        }
        
        if (vicData.patientId.isEmpty()) {
            Log.e(Config.LOG_TAG_VIC, "Validation failed: Patient ID is empty")
            showInputError("Invalid VIC data: Patient ID is empty")
            return false
        }
        
        if (vicData.patientName.isEmpty()) {
            Log.e(Config.LOG_TAG_VIC, "Validation failed: Patient name is empty")
            showInputError("Invalid VIC data: Patient name is empty")
            return false
        }
        
        // Validate transaction hash format
        if (!vicData.transactionHash.startsWith("0x") || vicData.transactionHash.length < 10) {
            Log.e(Config.LOG_TAG_VIC, "Validation failed: Invalid transaction hash format")
            showInputError("Invalid transaction hash format")
            return false
        }
        
        Log.d(Config.LOG_TAG_VIC, "Input validation passed")
        return true
    }
    
    private fun showInputError(message: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Input Error")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
    
    private fun showShareError(message: String) {
        val errorMessage = "Failed to create VIC share:\n\n$message\n\nPossible reasons:\n" +
                "• Network connection issue\n" +
                "• Server is temporarily unavailable\n" +
                "• Invalid VIC data\n" +
                "• API rate limit exceeded"
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Share Creation Failed")
            .setMessage(errorMessage)
            .setPositiveButton("Retry") { _, _ ->
                createVICShare()
            }
            .setNegativeButton("Close") { _, _ ->
                finish()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
    
    private fun toggleAllPermissions() {
        val diagnosisCheckbox = findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.cb_diagnosis)
        val treatmentCheckbox = findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.cb_treatment)
        val doctorCheckbox = findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.cb_doctor)
        val dateCheckbox = findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.cb_date)
        val notesCheckbox = findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.cb_notes)
        val selectAllButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_select_all_permissions)
        
        // Check if all are selected
        val allSelected = diagnosisCheckbox.isChecked && treatmentCheckbox.isChecked && 
                         doctorCheckbox.isChecked && dateCheckbox.isChecked && notesCheckbox.isChecked
        
        // Toggle all checkboxes
        val newState = !allSelected
        diagnosisCheckbox.isChecked = newState
        treatmentCheckbox.isChecked = newState
        doctorCheckbox.isChecked = newState
        dateCheckbox.isChecked = newState
        notesCheckbox.isChecked = newState
        
        // Update button text
        selectAllButton.text = if (newState) "Deselect All" else "Select All"
    }
    
    private fun showShareOptions() {
        val message = "VIC Sharing Options:\n\n" +
                "• Shared By: Who is sharing this VIC\n" +
                "• Expires In: How long the share is valid (1-8760 hours)\n" +
                "• Hospital: Restrict access to specific hospital (optional)\n" +
                "• Permissions: What data can be accessed\n\n" +
                "Security Features:\n" +
                "• Token-based access control\n" +
                "• Time-limited sharing\n" +
                "• Hospital restriction option\n" +
                "• Permission-based data access\n\n" +
                "The shared VIC will be accessible via QR code or share token."
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Sharing Options Help")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }
    
    
    companion object {
        fun start(context: android.content.Context, vicData: VICData) {
            try {
                Log.d(Config.LOG_TAG_VIC, "CreateVICShareActivity.start() called")
                Log.d(Config.LOG_TAG_VIC, "VICData: ${vicData.patientName}, ${vicData.transactionHash}")
                
                val intent = Intent(context, CreateVICShareActivity::class.java)
                val gson = com.google.gson.Gson()
                val vicJson = gson.toJson(vicData)
                intent.putExtra("vic_data_json", vicJson)
                
                Log.d(Config.LOG_TAG_VIC, "Intent created with VIC data JSON")
                Log.d(Config.LOG_TAG_VIC, "Starting activity...")
                
                context.startActivity(intent)
                
                Log.d(Config.LOG_TAG_VIC, "Activity started successfully")
            } catch (e: Exception) {
                Log.e(Config.LOG_TAG_VIC, "Error in CreateVICShareActivity.start(): ${e.message}")
                Log.e(Config.LOG_TAG_VIC, "Exception details: ${e.stackTraceToString()}")
                throw e
            }
        }
    }
}
