# 📱 Panduan Aplikasi Android untuk Scan VIC QR Code

## 🎯 Overview

Aplikasi Android ini memungkinkan user untuk scan QR code VIC (Verifiable Identity Credential) yang diterbitkan oleh rumah sakit dan menampilkan data medical records.

## 🔧 Setup Aplikasi Android

### 1. Dependencies yang Diperlukan

```gradle
// app/build.gradle
dependencies {
    implementation 'com.google.zxing:core:3.5.2'
    implementation 'com.journeyapps:zxing-android-embedded:4.3.0'
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
    implementation 'androidx.camera:camera-core:1.3.1'
    implementation 'androidx.camera:camera-camera2:1.3.1'
    implementation 'androidx.camera:camera-lifecycle:1.3.1'
    implementation 'androidx.camera:camera-view:1.3.1'
}
```

### 2. Permissions

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## 📋 Struktur Data VIC QR Code

### Format JSON yang Dihasilkan Hospital:

```json
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
```

## 🏗️ Implementasi Android

### 1. MainActivity.java

```java
package com.example.vicscanner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.zxing.Result;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST = 1001;
    private DecoratedBarcodeView barcodeView;
    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        barcodeView = findViewById(R.id.barcode_scanner);
        barcodeView.decodeContinuous(callback);
        
        checkCameraPermission();
    }

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null && !isScanning) {
                isScanning = true;
                processVICData(result.getText());
            }
        }
    };

    private void processVICData(String qrData) {
        try {
            // Parse JSON data
            VICData vicData = VICParser.parseQRData(qrData);
            
            if (vicData != null) {
                // Navigate to VIC details
                VICDetailsActivity.start(this, vicData);
            } else {
                showError("Invalid VIC QR Code");
            }
        } catch (Exception e) {
            showError("Error parsing VIC data: " + e.getMessage());
        } finally {
            isScanning = false;
        }
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA}, 
                CAMERA_PERMISSION_REQUEST);
        } else {
            startScanning();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startScanning() {
        barcodeView.resume();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isScanning) {
            barcodeView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }
}
```

### 2. VICData.java (Data Model)

```java
package com.example.vicscanner;

import java.util.Date;

public class VICData {
    private String type;
    private String hospital;
    private String patientId;
    private String patientName;
    private String diagnosis;
    private String treatment;
    private String doctor;
    private String date;
    private String notes;
    private String transactionHash;
    private int blockNumber;
    private String timestamp;
    private String verificationUrl;
    private boolean demoMode;

    // Constructors, getters, and setters
    public VICData() {}

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getHospital() { return hospital; }
    public void setHospital(String hospital) { this.hospital = hospital; }
    
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    
    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }
    
    public String getDoctor() { return doctor; }
    public void setDoctor(String doctor) { this.doctor = doctor; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }
    
    public int getBlockNumber() { return blockNumber; }
    public void setBlockNumber(int blockNumber) { this.blockNumber = blockNumber; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    public String getVerificationUrl() { return verificationUrl; }
    public void setVerificationUrl(String verificationUrl) { this.verificationUrl = verificationUrl; }
    
    public boolean isDemoMode() { return demoMode; }
    public void setDemoMode(boolean demoMode) { this.demoMode = demoMode; }
}
```

### 3. VICParser.java (JSON Parser)

```java
package com.example.vicscanner;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class VICParser {
    private static final Gson gson = new Gson();

    public static VICData parseQRData(String qrData) {
        try {
            // Validate if it's a VIC QR code
            if (!qrData.contains("\"type\":\"VIC\"")) {
                return null;
            }
            
            VICData vicData = gson.fromJson(qrData, VICData.class);
            
            // Validate required fields
            if (vicData.getType() == null || !vicData.getType().equals("VIC")) {
                return null;
            }
            
            return vicData;
        } catch (JsonSyntaxException e) {
            return null;
        }
    }
}
```

### 4. VICDetailsActivity.java (Display VIC Data)

```java
package com.example.vicscanner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class VICDetailsActivity extends AppCompatActivity {
    private VICData vicData;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vic_details);
        
        vicData = (VICData) getIntent().getSerializableExtra("vic_data");
        
        if (vicData != null) {
            displayVICData();
        }
    }
    
    private void displayVICData() {
        TextView hospitalText = findViewById(R.id.hospital_text);
        TextView patientNameText = findViewById(R.id.patient_name_text);
        TextView diagnosisText = findViewById(R.id.diagnosis_text);
        TextView treatmentText = findViewById(R.id.treatment_text);
        TextView doctorText = findViewById(R.id.doctor_text);
        TextView dateText = findViewById(R.id.date_text);
        TextView notesText = findViewById(R.id.notes_text);
        TextView transactionHashText = findViewById(R.id.transaction_hash_text);
        TextView blockNumberText = findViewById(R.id.block_number_text);
        TextView demoModeText = findViewById(R.id.demo_mode_text);
        
        hospitalText.setText(vicData.getHospital());
        patientNameText.setText(vicData.getPatientName());
        diagnosisText.setText(vicData.getDiagnosis());
        treatmentText.setText(vicData.getTreatment());
        doctorText.setText(vicData.getDoctor());
        dateText.setText(vicData.getDate());
        notesText.setText(vicData.getNotes());
        transactionHashText.setText(vicData.getTransactionHash());
        blockNumberText.setText(String.valueOf(vicData.getBlockNumber()));
        
        if (vicData.isDemoMode()) {
            demoModeText.setText("⚠️ DEMO MODE - Data tidak tersimpan ke blockchain");
            demoModeText.setVisibility(android.view.View.VISIBLE);
        } else {
            demoModeText.setVisibility(android.view.View.GONE);
        }
        
        Button verifyButton = findViewById(R.id.verify_button);
        verifyButton.setOnClickListener(v -> verifyVIC());
        
        Button scanAgainButton = findViewById(R.id.scan_again_button);
        scanAgainButton.setOnClickListener(v -> finish());
    }
    
    private void verifyVIC() {
        // Implement verification logic
        // Call API to verify VIC data
    }
    
    public static void start(android.content.Context context, VICData vicData) {
        Intent intent = new Intent(context, VICDetailsActivity.class);
        intent.putExtra("vic_data", vicData);
        context.startActivity(intent);
    }
}
```

## 📱 Layout Files

### activity_main.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Scan VIC QR Code"
        android:textSize="24sp"
        android:textStyle="bold"
        android:gravity="center"
        android:padding="16dp" />

    <com.journeyapps.barcodescanner.DecoratedBarcodeView
        android:id="@+id/barcode_scanner"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Point camera at VIC QR code"
        android:textSize="16sp"
        android:gravity="center"
        android:padding="16dp" />

</LinearLayout>
```

### activity_vic_details.xml

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
            android:text="VIC Medical Records"
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

## 🔍 Testing QR Code

### 1. Test dengan QR Code Generator Online

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

### 2. Validasi QR Code

```java
// Validasi di Android app
private boolean isValidVICQR(String qrData) {
    try {
        JSONObject json = new JSONObject(qrData);
        return json.has("type") && 
               json.getString("type").equals("VIC") &&
               json.has("hospital") &&
               json.has("patient_id") &&
               json.has("transaction_hash");
    } catch (JSONException e) {
        return false;
    }
}
```

## 🚀 Deployment

### 1. Build APK

```bash
./gradlew assembleRelease
```

### 2. Install di Device

```bash
adb install app-release.apk
```

## 📋 Troubleshooting

### 1. QR Code Tidak Terbaca

- **Pastikan QR code jelas dan tidak blur**
- **Cahaya cukup untuk kamera**
- **QR code tidak terpotong atau rusak**

### 2. Invalid Barcode Error

- **Cek format JSON di QR code**
- **Pastikan field `type` = "VIC"**
- **Validasi semua field required**

### 3. Network Issues

- **Cek koneksi internet**
- **Pastikan server API accessible**
- **Handle offline mode dengan cache**

## 🎯 Features yang Bisa Ditambahkan

1. **Offline Mode** - Cache VIC data untuk akses offline
2. **History** - Simpan riwayat scan VIC
3. **Export** - Export VIC data ke PDF
4. **Verification** - Verify VIC dengan blockchain
5. **Notifications** - Notifikasi untuk VIC baru

---

## 📞 Support

Jika ada masalah dengan implementasi, silakan cek:

1. **Logs Android Studio** untuk error details
2. **Network logs** untuk API calls
3. **QR code format** sesuai dokumentasi
4. **Permissions** camera dan internet

**Server API**: http://31.97.108.98:8501  
**Hospital 1**: http://31.97.108.98:8503  
**Hospital 2**: http://31.97.108.98:8504

