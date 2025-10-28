package com.dicoding.didapp.ui.scanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.dicoding.didapp.R
import com.dicoding.didapp.data.model.PatientData
import com.dicoding.didapp.network.ApiClient
import com.dicoding.didapp.ui.wallet.AccessVICShareActivity
import com.dicoding.didapp.utils.QRCodeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * QR Scanner Activity untuk memindai QR code yang berisi Verifiable Credential
 * Menggunakan CodeScanner library untuk scanning yang lebih stabil
 */
class QRScannerActivity : AppCompatActivity() {
    
    private lateinit var codeScanner: CodeScanner
    private val CAMERA_PERMISSION_REQUEST = 1001
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)
        
        setupUI()
        checkCameraPermission()
    }
    
    private fun setupUI() {
        findViewById<android.widget.Button>(R.id.btn_cancel).setOnClickListener {
            finish()
        }
        
        findViewById<android.widget.Button>(R.id.btn_manual_input).setOnClickListener {
            showManualInputDialog()
        }
        
        // Setup CodeScanner
        setupCodeScanner()
    }
    
    private fun setupCodeScanner() {
        val scannerView = findViewById<com.budiyev.android.codescanner.CodeScannerView>(R.id.scanner_view)
        codeScanner = CodeScanner(this, scannerView)
        
        // Parameters
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false
        
        // Callbacks
        codeScanner.decodeCallback = DecodeCallback { result ->
            runOnUiThread {
                processQRCode(result.text)
            }
        }
        
        codeScanner.errorCallback = ErrorCallback { exception ->
            runOnUiThread {
                Toast.makeText(this, "Camera error: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
        
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }
    
    private fun processQRCode(qrData: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                android.util.Log.d("QRScanner", "Processing QR code: ${qrData.take(100)}...")
                
                // Validate QR code format first
                if (!QRCodeUtils.isValidQRCode(qrData)) {
                    Toast.makeText(this@QRScannerActivity, 
                        "Invalid QR code format", Toast.LENGTH_LONG).show()
                    codeScanner.startPreview()
                    return@launch
                }
                
                // Check QR code type
                val qrCodeType = QRCodeUtils.getQRCodeType(qrData)
                android.util.Log.d("QRScanner", "QR Code type: $qrCodeType")
                
                when (qrCodeType) {
                    "VIC" -> {
                        // Handle VIC format
                        android.util.Log.d("QRScanner", "Processing VIC QR code...")
                        val vicData = QRCodeUtils.parseVICQRCode(qrData)
                        android.util.Log.d("QRScanner", "Parsed VIC: ${vicData.patientName} (${vicData.patientId})")
                        android.util.Log.d("QRScanner", "VIC Data: type=${vicData.type}, hospital=${vicData.hospital}")
                        
                        // Navigate to VIC details using JSON string approach
                        val intent = Intent(this@QRScannerActivity, com.dicoding.didapp.ui.wallet.VICDetailsActivity::class.java)
                        intent.putExtra("vic_data_json", qrData) // Pass original QR data as JSON string
                        android.util.Log.d("QRScanner", "Starting VICDetailsActivity with VIC data")
                        android.util.Log.d("QRScanner", "Intent extras: ${intent.extras?.keySet()}")
                        startActivity(intent)
                        finish()
                    }
                    "VIC_SHARE" -> {
                        // Handle VIC Share format
                        android.util.Log.d("QRScanner", "Processing VIC Share QR code...")
                        val shareData = QRCodeUtils.parseVICShareQRCode(qrData)
                        if (shareData != null) {
                            android.util.Log.d("QRScanner", "Parsed VIC Share: ${shareData.shareToken}")
                            
                            // Navigate to VIC share access
                            AccessVICShareActivity.start(this@QRScannerActivity, shareData.shareToken, shareData.hospital)
                            finish()
                        } else {
                            Toast.makeText(this@QRScannerActivity, "Invalid VIC Share QR code", Toast.LENGTH_SHORT).show()
                            codeScanner.startPreview()
                        }
                    }
                    "VerifiableCredential", "PatientData" -> {
                        // Handle VC or PatientData format
                        val patientData = QRCodeUtils.parseQRCode(qrData)
                        android.util.Log.d("QRScanner", "Parsed patient: ${patientData.name} (${patientData.did})")
                        
                        // Extract DID for verification
                        val patientDID = QRCodeUtils.extractPatientDID(qrData)
                        if (patientDID.isNullOrEmpty()) {
                            Toast.makeText(this@QRScannerActivity, 
                                "Could not extract patient DID", Toast.LENGTH_LONG).show()
                            codeScanner.startPreview()
                            return@launch
                        }
                        
                        // Save to local storage
                        savePatientData(patientData)
                        
                        // Show success message
                        Toast.makeText(this@QRScannerActivity, 
                            "Patient data loaded successfully!\nName: ${patientData.name}", Toast.LENGTH_LONG).show()
                        
                        // Return to main activity with result
                        val intent = Intent()
                        intent.putExtra("patient_data", patientData)
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                    else -> {
                        Toast.makeText(this@QRScannerActivity, 
                            "Unsupported QR code format", Toast.LENGTH_LONG).show()
                        codeScanner.startPreview()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("QRScanner", "Error processing QR code: ${e.message}")
                Toast.makeText(this@QRScannerActivity, 
                    "Error processing QR code: ${e.message}", Toast.LENGTH_LONG).show()
                codeScanner.startPreview()
            }
        }
    }
    
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            startScanner()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanner()
            } else {
                Toast.makeText(this, "Camera permission required for QR scanning", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    private fun startScanner() {
        codeScanner.startPreview()
    }
    
    private fun showManualInputDialog() {
        val intent = Intent(this, ManualInputActivity::class.java)
        startActivityForResult(intent, 1001)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val patientData = data?.getSerializableExtra("patient_data") as? PatientData
            if (patientData != null) {
                // Save to local storage
                savePatientData(patientData)
                
                // Show success message
                Toast.makeText(this, "Patient data saved successfully!", Toast.LENGTH_LONG).show()
                
                // Return to main activity with result
                val intent = Intent()
                intent.putExtra("patient_data", patientData)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }
    
    private fun savePatientData(patientData: PatientData) {
        try {
            // Save to local storage using LocalStorage
            val localStorage = com.dicoding.didapp.utils.LocalStorage(this)
            localStorage.savePatient(patientData)
            
            android.util.Log.d("QRScanner", "Patient data saved: ${patientData.name}")
        } catch (e: Exception) {
            android.util.Log.e("QRScanner", "Error saving patient data: ${e.message}")
        }
    }
    
    override fun onResume() {
        super.onResume()
        if (::codeScanner.isInitialized) {
            codeScanner.startPreview()
        }
    }
    
    override fun onPause() {
        if (::codeScanner.isInitialized) {
            codeScanner.releaseResources()
        }
        super.onPause()
    }
}
