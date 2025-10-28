package com.dicoding.didapp.ui.wallet

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.didapp.R
import com.dicoding.didapp.data.model.PatientData
import com.dicoding.didapp.data.model.MedicalRecord
import android.util.Log

/**
 * Activity untuk menampilkan detail pasien
 */
class PatientDetailsActivity : AppCompatActivity() {
    
    private lateinit var patientData: PatientData
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_details)
        
        try {
            // Get patient data from intent with error handling
            val intentData = intent.getSerializableExtra("patient_data")
            if (intentData == null) {
                Log.e("PatientDetails", "No patient data found in intent")
                Toast.makeText(this, "No patient data found", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            
            patientData = intentData as PatientData
            Log.d("PatientDetails", "Patient data loaded: ${patientData.name}")
            
            setupUI()
            displayPatientData()
        } catch (e: Exception) {
            Log.e("PatientDetails", "Error loading patient data: ${e.message}")
            Toast.makeText(this, "Error loading patient data: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun setupUI() {
        // Setup toolbar
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { finish() }
        }
    }
    
    private fun displayPatientData() {
        try {
            // Display basic patient information
            findViewById<TextView>(R.id.tv_patient_name).text = patientData.name
            findViewById<TextView>(R.id.tv_patient_age).text = "Age: ${patientData.age}"
            findViewById<TextView>(R.id.tv_patient_gender).text = "Gender: ${patientData.gender}"
            findViewById<TextView>(R.id.tv_patient_did).text = "DID: ${patientData.did}"
            findViewById<TextView>(R.id.tv_blood_type).text = "Blood Type: ${patientData.bloodType ?: "Not specified"}"
            findViewById<TextView>(R.id.tv_phone).text = "Phone: ${patientData.phone ?: "Not specified"}"
            findViewById<TextView>(R.id.tv_address).text = "Address: ${patientData.address ?: "Not specified"}"
            findViewById<TextView>(R.id.tv_verification_status).text = "Status: ${patientData.verificationStatus}"
            
            // Display medical records if available
            displayMedicalRecords()
            
        } catch (e: Exception) {
            Log.e("PatientDetails", "Error displaying patient data: ${e.message}")
            Toast.makeText(this, "Error displaying patient data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun displayMedicalRecords() {
        try {
            if (patientData.medicalRecords.isNotEmpty()) {
                // Show medical records section
                findViewById<TextView>(R.id.tv_medical_records_title).visibility = android.view.View.VISIBLE
                findViewById<RecyclerView>(R.id.recycler_medical_records).visibility = android.view.View.VISIBLE
                
                // Setup RecyclerView for medical records
                val recyclerView = findViewById<RecyclerView>(R.id.recycler_medical_records)
                recyclerView.layoutManager = LinearLayoutManager(this)
                
                // Create adapter for medical records
                val adapter = MedicalRecordAdapter(patientData.medicalRecords)
                recyclerView.adapter = adapter
                
                Log.d("PatientDetails", "Displaying ${patientData.medicalRecords.size} medical records")
            } else {
                // Hide medical records section if no records
                findViewById<TextView>(R.id.tv_medical_records_title).visibility = android.view.View.GONE
                findViewById<RecyclerView>(R.id.recycler_medical_records).visibility = android.view.View.GONE
                Log.d("PatientDetails", "No medical records to display")
            }
        } catch (e: Exception) {
            Log.e("PatientDetails", "Error displaying medical records: ${e.message}")
            // Hide medical records section on error
            findViewById<TextView>(R.id.tv_medical_records_title).visibility = android.view.View.GONE
            findViewById<RecyclerView>(R.id.recycler_medical_records).visibility = android.view.View.GONE
        }
    }
}
