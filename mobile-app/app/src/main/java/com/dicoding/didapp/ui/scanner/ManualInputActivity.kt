package com.dicoding.didapp.ui.scanner

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.didapp.R
import com.dicoding.didapp.data.model.PatientData
import com.dicoding.didapp.network.ApiClient
import com.dicoding.didapp.utils.LocalStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Manual Input Activity untuk input data pasien secara manual
 */
class ManualInputActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_input)
        
        setupUI()
    }
    
    private fun setupUI() {
        // Setup toolbar
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { finish() }
        }
        
        // Setup buttons
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_save).setOnClickListener {
            savePatientData()
        }
        
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_cancel).setOnClickListener {
            finish()
        }
    }
    
    private fun savePatientData() {
        val name = findViewById<android.widget.EditText>(R.id.et_name).text.toString()
        val age = findViewById<android.widget.EditText>(R.id.et_age).text.toString().toIntOrNull() ?: 0
        val gender = findViewById<android.widget.EditText>(R.id.et_gender).text.toString()
        val bloodType = findViewById<android.widget.EditText>(R.id.et_blood_type).text.toString()
        val phone = findViewById<android.widget.EditText>(R.id.et_phone).text.toString()
        val address = findViewById<android.widget.EditText>(R.id.et_address).text.toString()
        
        if (name.isEmpty() || age == 0 || gender.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Generate a simple DID
        val did = "did:example:${System.currentTimeMillis()}"
        
        val patientData = PatientData(
            did = did,
            name = name,
            age = age,
            gender = gender,
            bloodType = bloodType.takeIf { it.isNotEmpty() },
            phone = phone.takeIf { it.isNotEmpty() },
            address = address.takeIf { it.isNotEmpty() }
        )
        
        // Save patient data locally
        try {
            val localStorage = LocalStorage(this)
            localStorage.savePatient(patientData)
            
            Toast.makeText(this, "Patient data saved successfully!", Toast.LENGTH_SHORT).show()
            
            val intent = Intent()
            intent.putExtra("patient_data", patientData)
            setResult(RESULT_OK, intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save patient data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
