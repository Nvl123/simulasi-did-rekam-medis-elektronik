# 🔐 Panduan Verifikasi VIC (Verifiable Identity Credential)

## 🎯 **Overview**

Sistem verifikasi VIC memungkinkan aplikasi Android untuk memverifikasi autentisitas kredensial medis yang diterbitkan oleh rumah sakit melalui blockchain.

## 🏗️ **Arsitektur Verifikasi**

### **1. Komponen Sistem**
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Android App   │    │  Blockchain     │    │   API Server    │
│   (QR Scanner)  │───▶│  Server         │───▶│   (FastAPI)     │
│                 │    │  (Streamlit)    │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   QR Code       │    │   Database      │    │   Database      │
│   (VIC Data)    │    │   (MariaDB)     │    │   (MariaDB)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### **2. Flow Verifikasi**
```
1. Scan QR Code → 2. Parse VIC Data → 3. Extract Transaction Hash
                                        ↓
4. Call Verification API → 5. Database Lookup → 6. Return Result
                                        ↓
7. Display Verification Status → 8. Show VIC Details
```

## 🔧 **Endpoint Verifikasi**

### **1. Blockchain Server (Streamlit + Flask)**
- **URL**: `http://31.97.108.98:8501`
- **Port**: 8501
- **Framework**: Streamlit + Flask

#### **Health Check**
```http
GET /health
```
Response:
```json
{
    "status": "healthy",
    "database": "connected",
    "timestamp": "2024-01-15T10:30:00.000Z",
    "statistics": {
        "total_vic_issuances": 25,
        "total_transactions": 50,
        "total_blocks": 10
    }
}
```

#### **VIC Verification**
```http
GET /verify/{transaction_hash}
```
Response (Success):
```json
{
    "verified": true,
    "message": "VIC verified successfully",
    "data": {
        "transaction_hash": "0x5dff8cd1f681461c87ca379738c62290ad7db473",
        "block_number": 4652,
        "hospital": "Rumah Sakit A",
        "patient_id": "P001",
        "patient_name": "John Doe",
        "diagnosis": "Common Cold",
        "treatment": "Rest and Medication",
        "doctor": "Dr. Smith",
        "date": "2024-01-15",
        "notes": "Additional medical information",
        "timestamp": "2024-01-15T10:30:00.000Z"
    }
}
```

Response (Not Found):
```json
{
    "verified": false,
    "message": "VIC not found in blockchain",
    "data": null
}
```

### **2. API Server (FastAPI)**
- **URL**: `http://31.97.108.98:8502`
- **Port**: 8502
- **Framework**: FastAPI

#### **VIC Verification**
```http
GET /verify/{transaction_hash}
```
Response format sama dengan Blockchain Server.

## 📱 **Implementasi Android**

### **1. VIC Verification Activity**

```kotlin
// VICVerificationActivity.kt
class VICVerificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVicVerificationBinding
    private var vicData: VICData? = null
    private var verificationResult: VerificationResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVicVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get VIC data from intent
        vicData = intent.getSerializableExtra("vic_data") as? VICData
        
        if (vicData != null) {
            displayVICData()
            verifyVIC()
        }
    }
    
    private fun displayVICData() {
        binding.hospitalText.text = vicData?.hospital
        binding.patientNameText.text = vicData?.patientName
        binding.diagnosisText.text = vicData?.diagnosis
        binding.treatmentText.text = vicData?.treatment
        binding.doctorText.text = vicData?.doctor
        binding.dateText.text = vicData?.date
        binding.notesText.text = vicData?.notes
        binding.transactionHashText.text = vicData?.transactionHash
        binding.blockNumberText.text = vicData?.blockNumber.toString()
        
        if (vicData?.demoMode == true) {
            binding.demoModeText.visibility = View.VISIBLE
            binding.demoModeText.text = "⚠️ DEMO MODE - Data tidak tersimpan ke blockchain"
        }
    }
    
    private fun verifyVIC() {
        binding.verifyButton.isEnabled = false
        binding.verifyButton.text = "Verifying..."
        binding.progressBar.visibility = View.VISIBLE
        
        // Start verification in background
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                verificationResult = verifyVICWithAPI()
                
                withContext(Dispatchers.Main) {
                    displayVerificationResult()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Verification failed: ${e.message}")
                }
            }
        }
    }
    
    private suspend fun verifyVICWithAPI(): VerificationResult {
        return try {
            if (vicData?.verificationUrl?.isNotEmpty() == true) {
                verifyUsingVerificationURL()
            } else {
                verifyUsingBasicValidation()
            }
        } catch (e: Exception) {
            verifyUsingBasicValidation()
        }
    }
    
    private suspend fun verifyUsingVerificationURL(): VerificationResult {
        val url = URL(vicData?.verificationUrl)
        val connection = url.openConnection() as HttpURLConnection
        
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        
        val responseCode = connection.responseCode
        
        if (responseCode == 200) {
            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            val verificationResponse = gson.fromJson(responseBody, VerificationResponse::class.java)
            
            return if (verificationResponse.verified == true) {
                VerificationResult(success = true, message = "VIC verified successfully")
            } else {
                VerificationResult(success = false, message = "VIC verification failed")
            }
        } else {
            return VerificationResult(success = false, message = "API error: $responseCode")
        }
    }
    
    private fun verifyUsingBasicValidation(): VerificationResult {
        val hasRequiredFields = vicData?.type == "VIC" &&
                vicData?.hospital?.isNotEmpty() == true &&
                vicData?.patientId?.isNotEmpty() == true &&
                vicData?.patientName?.isNotEmpty() == true &&
                vicData?.transactionHash?.isNotEmpty() == true
        
        val hasValidTransactionHash = vicData?.transactionHash?.startsWith("0x") == true &&
                vicData?.transactionHash?.length!! >= 10
        
        val hasValidBlockNumber = vicData?.blockNumber!! > 0
        
        return if (hasRequiredFields && hasValidTransactionHash && hasValidBlockNumber) {
            VerificationResult(success = true, message = "VIC validated (offline mode)")
        } else {
            VerificationResult(success = false, message = "VIC validation failed")
        }
    }
    
    private fun displayVerificationResult() {
        binding.progressBar.visibility = View.GONE
        binding.verifyButton.isEnabled = true
        binding.verifyButton.text = "Verify VIC"
        
        if (verificationResult?.success == true) {
            binding.verificationStatus.text = "✅ ${verificationResult?.message}"
            binding.verificationStatus.setTextColor(ContextCompat.getColor(this, R.color.green))
        } else {
            binding.verificationStatus.text = "❌ ${verificationResult?.message}"
            binding.verificationStatus.setTextColor(ContextCompat.getColor(this, R.color.red))
        }
        
        binding.verificationStatus.visibility = View.VISIBLE
    }
    
    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.verifyButton.isEnabled = true
        binding.verifyButton.text = "Verify VIC"
        
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
```

### **2. Data Models**

```kotlin
// VICData.kt
data class VICData(
    val type: String,
    val hospital: String,
    val patientId: String,
    val patientName: String,
    val diagnosis: String,
    val treatment: String,
    val doctor: String,
    val date: String,
    val notes: String,
    val transactionHash: String,
    val blockNumber: Int,
    val timestamp: String,
    val verificationUrl: String,
    val demoMode: Boolean = false
)

// VerificationResult.kt
data class VerificationResult(
    val success: Boolean,
    val message: String
)

// VerificationResponse.kt
data class VerificationResponse(
    val verified: Boolean?,
    val message: String?,
    val data: VICData?
)
```

### **3. Layout Files**

#### **activity_vic_verification.xml**
```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="VIC Verification"
            android:textSize="24sp"
            android:textStyle="bold"
            android:gravity="center"
            android:layout_marginBottom="16dp" />

        <TextView
            android:id="@+id/demo_mode_text"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="⚠️ DEMO MODE"
            android:textColor="#FF6B35"
            android:textSize="16sp"
            android:textStyle="bold"
            android:gravity="center"
            android:visibility="gone"
            android:layout_marginBottom="16dp" />

        <!-- Hospital Information -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Hospital Information"
            android:textSize="18sp"
            android:textStyle="bold"
            android:layout_marginTop="16dp" />

        <TextView
            android:id="@+id/hospital_text"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Hospital Name"
            android:textSize="16sp"
            android:layout_marginBottom="8dp" />

        <!-- Patient Information -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Patient Information"
            android:textSize="18sp"
            android:textStyle="bold"
            android:layout_marginTop="16dp" />

        <TextView
            android:id="@+id/patient_name_text"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Patient Name"
            android:textSize="16sp"
            android:layout_marginBottom="8dp" />

        <!-- Medical Information -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Medical Information"
            android:textSize="18sp"
            android:textStyle="bold"
            android:layout_marginTop="16dp" />

        <TextView
            android:id="@+id/diagnosis_text"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Diagnosis"
            android:textSize="16sp"
            android:layout_marginBottom="8dp" />

        <TextView
            android:id="@+id/treatment_text"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Treatment"
            android:textSize="16sp"
            android:layout_marginBottom="8dp" />

        <TextView
            android:id="@+id/doctor_text"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Doctor"
            android:textSize="16sp"
            android:layout_marginBottom="8dp" />

        <TextView
            android:id="@+id/date_text"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Date"
            android:textSize="16sp"
            android:layout_marginBottom="8dp" />

        <TextView
            android:id="@+id/notes_text"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Notes"
            android:textSize="16sp"
            android:layout_marginBottom="8dp" />

        <!-- Blockchain Information -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Blockchain Information"
            android:textSize="18sp"
            android:textStyle="bold"
            android:layout_marginTop="16dp" />

        <TextView
            android:id="@+id/transaction_hash_text"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Transaction Hash"
            android:textSize="14sp"
            android:fontFamily="monospace"
            android:layout_marginBottom="8dp" />

        <TextView
            android:id="@+id/block_number_text"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Block Number"
            android:textSize="16sp"
            android:layout_marginBottom="16dp" />

        <!-- Verification Section -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Verification"
            android:textSize="18sp"
            android:textStyle="bold"
            android:layout_marginTop="16dp" />

        <ProgressBar
            android:id="@+id/progress_bar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:visibility="gone"
            android:layout_marginBottom="16dp" />

        <TextView
            android:id="@+id/verification_status"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text=""
            android:textSize="16sp"
            android:textStyle="bold"
            android:gravity="center"
            android:visibility="gone"
            android:layout_marginBottom="16dp" />

        <Button
            android:id="@+id/verify_button"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Verify VIC"
            android:layout_marginBottom="8dp" />

        <Button
            android:id="@+id/scan_again_button"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Scan Again" />

    </LinearLayout>

</ScrollView>
```

## 🔍 **Testing Verifikasi**

### **1. Test dengan QR Code Generator**

```javascript
// Test data untuk generate QR code
const testVICData = {
    "type": "VIC",
    "hospital": "Rumah Sakit A",
    "patient_id": "P001",
    "patient_name": "John Doe",
    "diagnosis": "Common Cold",
    "treatment": "Rest and Medication",
    "doctor": "Dr. Smith",
    "date": "2024-01-15",
    "notes": "Additional medical information",
    "transaction_hash": "0x5dff8cd1f681461c87ca379738c62290ad7db473",
    "block_number": 4652,
    "timestamp": "2024-01-15T10:30:00.000Z",
    "verification_url": "http://31.97.108.98:8501/verify/0x5dff8cd1f681461c87ca379738c62290ad7db473",
    "demo_mode": false
};

// Generate QR code
const qrData = JSON.stringify(testVICData);
console.log(qrData);
```

### **2. Test API Endpoints**

```bash
# Test health check
curl http://31.97.108.98:8501/health

# Test VIC verification
curl http://31.97.108.98:8501/verify/0x5dff8cd1f681461c87ca379738c62290ad7db473

# Test API server
curl http://31.97.108.98:8502/verify/0x5dff8cd1f681461c87ca379738c62290ad7db473
```

### **3. Test Android App**

```kotlin
// Test VIC data parsing
val testQRData = """
{
    "type": "VIC",
    "hospital": "Rumah Sakit A",
    "patient_id": "P001",
    "patient_name": "John Doe",
    "diagnosis": "Common Cold",
    "treatment": "Rest and Medication",
    "doctor": "Dr. Smith",
    "date": "2024-01-15",
    "notes": "Additional medical information",
    "transaction_hash": "0x5dff8cd1f681461c87ca379738c62290ad7db473",
    "block_number": 4652,
    "timestamp": "2024-01-15T10:30:00.000Z",
    "verification_url": "http://31.97.108.98:8501/verify/0x5dff8cd1f681461c87ca379738c62290ad7db473",
    "demo_mode": false
}
"""

val vicData = gson.fromJson(testQRData, VICData::class.java)
assert(vicData.type == "VIC")
assert(vicData.hospital == "Rumah Sakit A")
assert(vicData.patientName == "John Doe")
```

## 🚀 **Deployment**

### **1. Server Deployment**

```bash
# Start blockchain server
cd /root/did-uts-new
docker-compose up -d blockchain-server

# Start API server
docker-compose up -d api-server

# Check status
docker-compose ps
```

### **2. Mobile App Deployment**

```bash
# Build APK
./gradlew assembleDebug

# Install APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 🔧 **Configuration**

### **1. Server URLs**

```kotlin
// Config.kt
object Config {
    const val BLOCKCHAIN_SERVER_URL = "http://31.97.108.98:8501"
    const val API_SERVER_URL = "http://31.97.108.98:8502"
    const val VERIFICATION_TIMEOUT = 10000L
}
```

### **2. Database Configuration**

```python
# database.py
DATABASE_URL = "mysql+pymysql://root:password@mariadb:3306/did_blockchain"
```

## 📊 **Monitoring**

### **1. Health Checks**

```bash
# Check blockchain server
curl http://31.97.108.98:8501/health

# Check API server
curl http://31.97.108.98:8502/health
```

### **2. Logs**

```bash
# View blockchain server logs
docker-compose logs blockchain-server

# View API server logs
docker-compose logs api-server
```

## 🔒 **Security**

### **1. Data Validation**

- **Transaction Hash Format** - Validasi format hash blockchain
- **Required Fields** - Validasi field wajib
- **Data Type Validation** - Validasi tipe data
- **Timestamp Validation** - Validasi format timestamp

### **2. API Security**

- **HTTPS Connection** - Koneksi aman ke server
- **Timeout Handling** - Timeout untuk mencegah hanging
- **Error Handling** - Penanganan error yang graceful
- **Rate Limiting** - Batasi request rate

## 📋 **Troubleshooting**

### **1. Common Issues**

#### **Verification Failed**
- Check internet connection
- Verify server is running
- Check VIC data format
- Try offline validation

#### **API Connection Error**
- Check server URL
- Verify server is running
- Check network connectivity
- Review server logs

#### **QR Code Not Scanning**
- Check camera permissions
- Ensure good lighting
- Clean camera lens
- Try different QR code

### **2. Debug Information**

```kotlin
// Enable debug logging
Log.d("VICVerification", "Verifying VIC: ${vicData.patientName}")
Log.d("VICVerification", "Transaction Hash: ${vicData.transactionHash}")
Log.d("VICVerification", "Verification URL: ${vicData.verificationUrl}")
```

## 🎯 **Best Practices**

### **1. Error Handling**

- **Network Errors** - Handle network connectivity issues
- **API Errors** - Handle API response errors
- **Parse Errors** - Handle JSON parsing errors
- **Validation Errors** - Handle data validation errors

### **2. User Experience**

- **Loading Indicators** - Show progress during verification
- **Error Messages** - Clear error messages for users
- **Offline Mode** - Basic validation when offline
- **Retry Options** - Allow users to retry failed operations

### **3. Performance**

- **Background Processing** - Process verification in background
- **Caching** - Cache verification results
- **Timeout Handling** - Set appropriate timeouts
- **Memory Management** - Optimize memory usage

---

## 📚 **References**

- [VIC QR Code Integration Guide](VIC_QR_CODE_INTEGRATION.md)
- [Android App Guide](ANDROID_APP_GUIDE.md)
- [API Documentation](API_DOCUMENTATION.md)

**Last Updated:** 2025-01-14  
**Status:** ✅ **IMPLEMENTED & WORKING**  
**Server:** `31.97.108.98:8501` (Blockchain) & `31.97.108.98:8502` (API)  
**Mobile App:** Ready for VIC verification
