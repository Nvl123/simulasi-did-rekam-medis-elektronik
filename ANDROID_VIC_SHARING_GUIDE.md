# 📱 Android VIC Sharing Integration Guide

## 🎯 Overview

This guide explains how to integrate VIC (Verifiable Identity Credential) sharing functionality into your Android application. The system allows patients to share their medical records with hospitals through secure tokens with customizable permissions and expiration times.

## 🔧 API Integration

### Base API URL
```
http://31.97.108.98:8502
```

### 1. Create VIC Share

**Endpoint:** `POST /api/vic-share/create`

**Request Body:**
```json
{
    "transaction_hash": "0x5dff8cd1f681461c87ca379738c62290ad7db473",
    "patient_id": "P001",
    "shared_by": "Patient",
    "shared_with_hospital": "Rumah Sakit B", // Optional, null for any hospital
    "expires_in_hours": 24,
    "access_permissions": {
        "diagnosis": true,
        "treatment": true,
        "doctor": true,
        "date": true,
        "notes": false
    }
}
```

**Response:**
```json
{
    "success": true,
    "share_token": "VIC_abc123def456...",
    "expires_at": "2024-01-16T10:30:00.000Z",
    "message": "VIC share created successfully"
}
```

### 2. Access Shared VIC

**Endpoint:** `GET /api/vic-share/{share_token}?hospital={hospital_name}`

**Response:**
```json
{
    "success": true,
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
        "timestamp": 1705312200.0
    },
    "permissions": {
        "diagnosis": true,
        "treatment": true,
        "doctor": true,
        "date": true,
        "notes": false
    },
    "shared_by": "Patient",
    "created_at": "2024-01-15T10:30:00.000Z",
    "expires_at": "2024-01-16T10:30:00.000Z"
}
```

### 3. Get Patient VIC Shares

**Endpoint:** `GET /api/vic-share/patient/{patient_id}`

**Response:**
```json
{
    "success": true,
    "shares": [
        {
            "id": 1,
            "share_token": "VIC_abc123def456...",
            "original_transaction_hash": "0x5dff8cd1f681461c87ca379738c62290ad7db473",
            "shared_by": "Patient",
            "shared_with_hospital": "Rumah Sakit B",
            "access_permissions": {
                "diagnosis": true,
                "treatment": true,
                "doctor": true,
                "date": true,
                "notes": false
            },
            "expires_at": "2024-01-16T10:30:00.000Z",
            "is_active": true,
            "created_at": "2024-01-15T10:30:00.000Z",
            "last_accessed": "2024-01-15T14:20:00.000Z"
        }
    ]
}
```

### 4. Revoke VIC Share

**Endpoint:** `POST /api/vic-share/{share_token}/revoke`

**Response:**
```json
{
    "success": true,
    "message": "VIC share revoked successfully"
}
```

## 📱 Android Implementation

### 1. Dependencies

Add these dependencies to your `app/build.gradle`:

```gradle
dependencies {
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
    implementation 'com.google.zxing:core:3.5.2'
    implementation 'com.journeyapps:zxing-android-embedded:4.3.0'
}
```

### 2. Data Models

**VICShareRequest.java:**
```java
public class VICShareRequest {
    private String transaction_hash;
    private String patient_id;
    private String shared_by;
    private String shared_with_hospital;
    private int expires_in_hours;
    private Map<String, Boolean> access_permissions;
    
    // Constructors, getters, setters
}
```

**VICShareResponse.java:**
```java
public class VICShareResponse {
    private boolean success;
    private String share_token;
    private String expires_at;
    private String message;
    private String error;
    
    // Constructors, getters, setters
}
```

**VICAccessResponse.java:**
```java
public class VICAccessResponse {
    private boolean success;
    private VICData data;
    private Map<String, Boolean> permissions;
    private String shared_by;
    private String created_at;
    private String expires_at;
    private String error;
    
    // Constructors, getters, setters
}
```

### 3. API Service

**VICSharingService.java:**
```java
public interface VICSharingService {
    @POST("api/vic-share/create")
    Call<VICShareResponse> createShare(@Body VICShareRequest request);
    
    @GET("api/vic-share/{share_token}")
    Call<VICAccessResponse> accessShare(
        @Path("share_token") String shareToken,
        @Query("hospital") String hospital
    );
    
    @GET("api/vic-share/patient/{patient_id}")
    Call<PatientSharesResponse> getPatientShares(@Path("patient_id") String patientId);
    
    @POST("api/vic-share/{share_token}/revoke")
    Call<RevokeResponse> revokeShare(@Path("share_token") String shareToken);
}
```

### 4. QR Code Integration

**VIC Share QR Code Format:**
```json
{
    "type": "VIC_SHARE",
    "share_token": "VIC_abc123def456...",
    "hospital": "Rumah Sakit A",
    "expires_at": "2024-01-16T10:30:00.000Z"
}
```

**QR Code Scanner Integration:**
```java
public class VICShareQRScanner extends AppCompatActivity {
    private DecoratedBarcodeView barcodeView;
    
    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            try {
                JSONObject qrData = new JSONObject(result.getText());
                if ("VIC_SHARE".equals(qrData.getString("type"))) {
                    String shareToken = qrData.getString("share_token");
                    String hospital = qrData.getString("hospital");
                    accessSharedVIC(shareToken, hospital);
                }
            } catch (JSONException e) {
                showError("Invalid VIC Share QR Code");
            }
        }
    };
    
    private void accessSharedVIC(String shareToken, String hospital) {
        VICSharingService service = RetrofitClient.getService();
        Call<VICAccessResponse> call = service.accessShare(shareToken, hospital);
        
        call.enqueue(new Callback<VICAccessResponse>() {
            @Override
            public void onResponse(Call<VICAccessResponse> call, Response<VICAccessResponse> response) {
                if (response.isSuccessful() && response.body().isSuccess()) {
                    VICAccessResponse data = response.body();
                    showVICData(data.getData(), data.getPermissions());
                } else {
                    showError("Failed to access VIC data");
                }
            }
            
            @Override
            public void onFailure(Call<VICAccessResponse> call, Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }
}
```

### 5. VIC Sharing Activity

**VICSharingActivity.java:**
```java
public class VICSharingActivity extends AppCompatActivity {
    private VICSharingService sharingService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vic_sharing);
        
        sharingService = RetrofitClient.getService();
        
        // Load existing VIC data
        String transactionHash = getIntent().getStringExtra("transaction_hash");
        loadVICForSharing(transactionHash);
    }
    
    private void createVICShare(VICData vicData, VICShareRequest shareRequest) {
        Call<VICShareResponse> call = sharingService.createShare(shareRequest);
        
        call.enqueue(new Callback<VICShareResponse>() {
            @Override
            public void onResponse(Call<VICShareResponse> call, Response<VICShareResponse> response) {
                if (response.isSuccessful() && response.body().isSuccess()) {
                    VICShareResponse result = response.body();
                    showShareResult(result.getShare_token(), result.getExpires_at());
                } else {
                    showError("Failed to create VIC share");
                }
            }
            
            @Override
            public void onFailure(Call<VICShareResponse> call, Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }
    
    private void showShareResult(String shareToken, String expiresAt) {
        // Generate QR code for sharing
        JSONObject qrData = new JSONObject();
        qrData.put("type", "VIC_SHARE");
        qrData.put("share_token", shareToken);
        qrData.put("hospital", "Your Hospital Name");
        qrData.put("expires_at", expiresAt);
        
        // Display QR code and share token
        generateAndDisplayQRCode(qrData.toString());
        displayShareToken(shareToken);
    }
}
```

## 🔐 Security Features

### 1. Time-based Expiration
- VIC shares automatically expire after the specified time
- Default expiration: 24 hours
- Maximum expiration: 1 year (8760 hours)

### 2. Permission Controls
- Granular control over what data can be accessed
- Available permissions:
  - `diagnosis`: Medical diagnosis information
  - `treatment`: Treatment details
  - `doctor`: Doctor information
  - `date`: Date of visit
  - `notes`: Additional notes (usually restricted)

### 3. Hospital Restrictions
- Option to restrict access to specific hospitals
- If not specified, any hospital can access the shared VIC

### 4. Access Logging
- All access attempts are logged
- Includes timestamp, hospital name, and accessed data
- Patients can view access logs for their shared VICs

## 📋 Usage Workflow

### For Patients (Creating Shares):
1. Scan VIC QR code to load medical data
2. Navigate to sharing options
3. Configure sharing settings:
   - Expiration time
   - Hospital restrictions
   - Data permissions
4. Generate share token and QR code
5. Share with hospital via QR code or token

### For Hospitals (Accessing Shares):
1. Receive share token from patient
2. Scan QR code or enter token manually
3. Access filtered medical data based on permissions
4. View access logs and expiration status

## 🚨 Error Handling

### Common Error Responses:
```json
{
    "success": false,
    "error": "Share token not found"
}
```

```json
{
    "success": false,
    "error": "Share token has expired"
}
```

```json
{
    "success": false,
    "error": "Access denied for this hospital"
}
```

```json
{
    "success": false,
    "error": "Share token has been revoked"
}
```

## 🔄 Testing

### Test Share Token:
Use this token for testing: `VIC_test123456789`

### Test Hospital Names:
- "Rumah Sakit A"
- "Rumah Sakit B"

### Test Patient ID:
- "P001"
- "P002"

## 📞 Support

For technical support or questions about VIC sharing integration, please contact the development team or refer to the API documentation at `http://31.97.108.98:8502/docs`.



