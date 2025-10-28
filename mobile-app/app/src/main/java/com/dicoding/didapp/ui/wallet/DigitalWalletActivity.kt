package com.dicoding.didapp.ui.wallet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.dicoding.didapp.R
import com.dicoding.didapp.data.model.PatientData
import com.dicoding.didapp.data.model.SavedVIC
import com.dicoding.didapp.network.ApiClient
import com.dicoding.didapp.utils.LocalStorage
import com.dicoding.didapp.ui.wallet.VICDetailsActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Digital Wallet Activity untuk melihat dan mengelola rekam medis yang tersimpan
 */
class DigitalWalletActivity : AppCompatActivity() {
    
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var pagerAdapter: WalletPagerAdapter
    private lateinit var localStorage: LocalStorage
    private var patients: List<PatientData> = emptyList()
    private var vics: List<SavedVIC> = emptyList()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_digital_wallet_with_tabs)
        
        setupUI()
        loadData()
    }
    
    private fun setupUI() {
        // Setup toolbar
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { finish() }
            inflateMenu(R.menu.digital_wallet_menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_refresh -> {
                        loadData()
                        true
                    }
                    else -> false
                }
            }
        }
        
        // Setup ViewPager and TabLayout
        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)
        
        
        // Setup Create VC Share button
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_create_vc_share).setOnClickListener {
            createVCShare()
        }
        
        // Setup Copy VC Share button
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_copy_vc_share).setOnClickListener {
            copyVCShare()
        }
        
        // Initialize local storage
        localStorage = LocalStorage(this)
    }
    
    private fun setupViewPager() {
        Log.d("DigitalWallet", "Setting up ViewPager with ${patients.size} patients and ${vics.size} VICs")
        
        pagerAdapter = WalletPagerAdapter(
            this,
            patients,
            vics,
            onPatientClick = { patientData ->
                openPatientDetails(patientData)
            },
            onVICClick = { savedVIC ->
                showVICOptions(savedVIC)
            }
        )
        
        viewPager.adapter = pagerAdapter
        
        // Setup TabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Patients"
                1 -> tab.text = "VICs"
            }
        }.attach()
        
        Log.d("DigitalWallet", "ViewPager setup completed")
        
        // Log current tab
        Log.d("DigitalWallet", "Current tab: ${viewPager.currentItem}")
        
        // Switch to VICs tab to ensure it's visible
        if (vics.isNotEmpty()) {
            viewPager.currentItem = 1
            Log.d("DigitalWallet", "Switched to VICs tab")
            
            // Force immediate refresh
            viewPager.post {
                Log.d("DigitalWallet", "Post-switch: current item = ${viewPager.currentItem}")
            }
        }
    }
    
    private fun loadData() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Load patients and VICs from local storage
                patients = localStorage.getAllPatients()
                vics = localStorage.getAllVICs()
                
                Log.d("DigitalWallet", "Loaded ${patients.size} patients and ${vics.size} VICs")
                
                // Add test VIC data if none exists
                if (vics.isEmpty()) {
                    addTestVICData()
                    vics = localStorage.getAllVICs()
                    Log.d("DigitalWallet", "Added test VIC data, now have ${vics.size} VICs")
                }
                
                // Setup ViewPager after data is loaded
                setupViewPager()
                
                // Update existing fragments if they exist
                updateExistingFragments()
                
                // Force refresh the ViewPager
                viewPager.post {
                    Log.d("DigitalWallet", "Post-load: forcing ViewPager refresh")
                    pagerAdapter.notifyDataSetChanged()
                }
                
            } catch (e: Exception) {
                Log.e("DigitalWallet", "Error loading data: ${e.message}")
                Toast.makeText(this@DigitalWalletActivity, 
                    "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun addTestVICData() {
        try {
            val testVIC = com.dicoding.didapp.data.model.SavedVIC(
                id = "test_vic_${System.currentTimeMillis()}",
                transactionHash = "0x1234567890abcdef1234567890abcdef12345678",
                patientName = "John Doe",
                patientDID = "did:example:patient123",
                verificationDate = System.currentTimeMillis(),
                verificationStatus = "VERIFIED",
                barcodeData = "VIC_TEST_DATA_123456789",
                issuerInfo = "Test Hospital",
                expiryDate = null,
                additionalData = mapOf(
                    "diagnosis" to "Common Cold",
                    "treatment" to "Rest and fluids",
                    "doctor" to "Dr. Smith",
                    "date" to "2024-01-15",
                    "notes" to "Test VIC data for debugging"
                )
            )
            
            localStorage.saveVIC(testVIC)
            Log.d("DigitalWallet", "Test VIC data added successfully")
            
        } catch (e: Exception) {
            Log.e("DigitalWallet", "Error adding test VIC data: ${e.message}")
        }
    }
    
    private fun updateExistingFragments() {
        try {
            // Update pager adapter with new data
            if (::pagerAdapter.isInitialized) {
                pagerAdapter.updateData(patients, vics)
                Log.d("DigitalWallet", "Updated pager adapter with ${patients.size} patients and ${vics.size} VICs")
                
                // Also update fragments directly
                pagerAdapter.getPatientFragment()?.updateData(patients)
                pagerAdapter.getVICFragment()?.updateData(vics)
            }
            
            // Update VICListFragment if it exists
            val vicFragment = supportFragmentManager.fragments.find { 
                it is VICListFragment 
            } as? VICListFragment
            vicFragment?.updateData(vics)
            
            // Update PatientListFragment if it exists
            val patientFragment = supportFragmentManager.fragments.find { 
                it is PatientListFragment 
            } as? PatientListFragment
            patientFragment?.updateData(patients)
            
        } catch (e: Exception) {
            android.util.Log.e("DigitalWallet", "Error updating fragments: ${e.message}")
        }
    }
    
    private fun startQRScanner() {
        val intent = Intent(this, com.dicoding.didapp.ui.scanner.QRScannerActivity::class.java)
        startActivityForResult(intent, 1001)
    }
    
    private fun openPatientDetails(patientData: PatientData) {
        try {
            android.util.Log.d("DigitalWallet", "Opening patient details for: ${patientData.name}")
            
            val intent = Intent(this, PatientDetailsActivity::class.java)
            intent.putExtra("patient_data", patientData)
            startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("DigitalWallet", "Error opening patient details: ${e.message}")
            Toast.makeText(this, "Error opening patient details: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun showVICOptions(savedVIC: SavedVIC) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("VIC Options")
            .setMessage("Patient: ${savedVIC.patientName}\nTransaction: ${savedVIC.transactionHash}")
            .setPositiveButton("Copy Barcode") { _, _ ->
                copyVICBarcode(savedVIC)
            }
            .setNeutralButton("View Details") { _, _ ->
                // Show VIC details
                showVICDetails(savedVIC)
            }
            .setNegativeButton("Close") { _, _ ->
                // Just close
            }
            .show()
    }
    
    private fun copyVICBarcode(savedVIC: SavedVIC) {
        try {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("VIC Barcode", savedVIC.barcodeData)
            clipboard.setPrimaryClip(clip)
            
            Toast.makeText(this, "VIC barcode copied to clipboard!", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error copying barcode: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showVICDetails(savedVIC: SavedVIC) {
        try {
            // Convert SavedVIC to VICData for VICDetailsActivity
            val vicData = com.dicoding.didapp.data.model.VICData(
                type = "VIC",
                hospital = savedVIC.issuerInfo ?: "Digital Wallet",
                patientId = savedVIC.patientDID,
                patientName = savedVIC.patientName,
                diagnosis = savedVIC.additionalData["diagnosis"] ?: "Not specified",
                treatment = savedVIC.additionalData["treatment"] ?: "Not specified",
                doctor = savedVIC.additionalData["doctor"] ?: "Not specified",
                date = savedVIC.additionalData["date"] ?: java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                notes = savedVIC.additionalData["notes"],
                transactionHash = savedVIC.transactionHash,
                blockNumber = savedVIC.additionalData["blockNumber"]?.toIntOrNull() ?: 0, // Try to get from additionalData
                timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date(savedVIC.verificationDate)),
                verificationUrl = "", // Exclude verification URL as requested
                demoMode = savedVIC.additionalData["demoMode"]?.toBoolean() ?: false
            )
            
            Log.d("DigitalWallet", "Converted VICData: ${vicData.patientName}, ${vicData.hospital}, ${vicData.diagnosis}")
            
            // Open VICDetailsActivity with full data
            VICDetailsActivity.start(this, vicData)
            
        } catch (e: Exception) {
            android.util.Log.e("DigitalWallet", "Error opening VIC details: ${e.message}")
            Toast.makeText(this, "Error opening VIC details: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun createVCShare() {
        if (vics.isEmpty()) {
            Toast.makeText(this, "No VIC data available to share", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show dialog to select VIC to share
        val vicNames = vics.map { "${it.patientName} (${it.transactionHash.take(8)}...)" }
        val items = vicNames.toTypedArray()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select VIC to Share")
            .setItems(items) { _, which ->
                val selectedVIC = vics[which]
                startCreateVCShareActivity(selectedVIC)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun startCreateVCShareActivity(savedVIC: SavedVIC) {
        try {
            android.util.Log.d("DigitalWallet", "Starting CreateVICShareActivity for VIC: ${savedVIC.patientName}")
            
            // Convert SavedVIC to VICData for CreateVICShareActivity
            val vicData = com.dicoding.didapp.data.model.VICData(
                type = "VIC",
                hospital = "Digital Wallet",
                patientId = savedVIC.patientDID,
                patientName = savedVIC.patientName,
                diagnosis = "Available in Digital Wallet",
                treatment = "Available in Digital Wallet", 
                doctor = "Digital Wallet User",
                date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                notes = "This VIC is stored in Digital Wallet",
                transactionHash = savedVIC.transactionHash,
                blockNumber = 0,
                timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date()),
                verificationUrl = "https://verify.example.com/${savedVIC.transactionHash}",
                demoMode = false
            )
            
            android.util.Log.d("DigitalWallet", "VICData created: ${vicData.patientName}, ${vicData.transactionHash}")
            
            com.dicoding.didapp.ui.wallet.CreateVICShareActivity.start(this, vicData)
            
            android.util.Log.d("DigitalWallet", "CreateVICShareActivity.start() called successfully")
        } catch (e: Exception) {
            android.util.Log.e("DigitalWallet", "Error starting CreateVICShareActivity: ${e.message}")
            android.util.Log.e("DigitalWallet", "Exception details: ${e.stackTraceToString()}")
            Toast.makeText(this, "Error opening Create VC Share: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun copyVCShare() {
        if (vics.isEmpty()) {
            Toast.makeText(this, "No VIC data available to copy", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show dialog to select VIC to copy
        val vicNames = vics.map { "${it.patientName} (${it.transactionHash.take(8)}...)" }
        val items = vicNames.toTypedArray()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select VIC to Copy")
            .setItems(items) { _, which ->
                val selectedVIC = vics[which]
                copyVICShareToClipboard(selectedVIC)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun copyVICShareToClipboard(savedVIC: SavedVIC) {
        try {
            // Create VIC Share data in JSON format
            val vicShareData = mapOf(
                "type" to "VIC",
                "hospital" to "Digital Wallet",
                "patient_id" to savedVIC.patientDID,
                "patient_name" to savedVIC.patientName,
                "diagnosis" to "Shared from Digital Wallet",
                "treatment" to "Available in Digital Wallet",
                "doctor" to "Digital Wallet User",
                "date" to java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                "notes" to "This VIC was shared from Digital Wallet",
                "transaction_hash" to savedVIC.transactionHash,
                "block_number" to 0,
                "timestamp" to java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date()),
                "verification_url" to "https://verify.example.com/${savedVIC.transactionHash}",
                "demo_mode" to false
            )
            
            val jsonString = com.google.gson.Gson().toJson(vicShareData)
            
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("VIC Share", jsonString)
            clipboard.setPrimaryClip(clip)
            
            Toast.makeText(this, "VIC Share data copied to clipboard!", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            android.util.Log.e("DigitalWallet", "Error copying VIC share: ${e.message}")
            Toast.makeText(this, "Error copying VIC share: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // Handle patient data
            val patientData = data?.getSerializableExtra("patient_data") as? PatientData
            if (patientData != null) {
                // Save to local storage
                localStorage.savePatient(patientData)
                // Refresh the list
                loadData()
                Toast.makeText(this, "Patient data saved successfully!", Toast.LENGTH_SHORT).show()
            }
            
            // Handle VIC data
            val vicData = data?.getSerializableExtra("vic_data") as? com.dicoding.didapp.data.model.VICData
            if (vicData != null) {
                // Convert VICData to SavedVIC and save
                val verificationResult = com.dicoding.didapp.data.model.VerificationResult(
                    success = true,
                    message = "VIC verified from QR scan"
                )
                val savedVIC = com.dicoding.didapp.data.model.SavedVIC.fromVICData(vicData, verificationResult)
                localStorage.saveVIC(savedVIC)
                // Refresh the list
                loadData()
                Toast.makeText(this, "VIC data saved successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
