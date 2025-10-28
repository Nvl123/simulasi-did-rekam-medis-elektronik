# 👨‍💻 Developer Guide - DID Mobile App

Panduan lengkap untuk developer yang ingin berkontribusi atau mengembangkan aplikasi DID Mobile App.

## 🏗️ Arsitektur Aplikasi

### Package Structure
```
com.dicoding.didapp/
├── data/
│   └── model/          # Data models dan DTOs
├── network/            # API clients dan network layer
├── ui/
│   ├── auth/           # Authentication activities
│   ├── scanner/        # QR scanner activities
│   ├── settings/       # Settings activities
│   └── wallet/         # Wallet activities dan fragments
└── utils/              # Utility classes dan helpers
```

### Architecture Pattern
Aplikasi menggunakan **MVVM (Model-View-ViewModel)** pattern dengan komponen:

- **Model**: Data classes dan business logic
- **View**: Activities, Fragments, dan XML layouts
- **ViewModel**: UI logic dan state management
- **Repository**: Data access layer (implied)

## 🔧 Setup Development Environment

### 1. Prerequisites
```bash
# Android Studio
# JDK 11+
# Android SDK API 24+
# Git
```

### 2. Clone dan Setup
```bash
git clone https://github.com/Nvl123/simulasi-did-rekam-medis-elektronik.git
cd simulasi-did-rekam-medis-elektronik/mobile-app
```

### 3. Android Studio Setup
1. Buka Android Studio
2. Import project dari folder `mobile-app`
3. Tunggu Gradle sync
4. Install required SDK components

### 4. Dependencies
```kotlin
// build.gradle.kts (app level)
dependencies {
    implementation "androidx.core:core-ktx:1.9.0"
    implementation "androidx.appcompat:appcompat:1.6.1"
    implementation "com.google.android.material:material:1.8.0"
    implementation "androidx.constraintlayout:constraintlayout:2.1.4"
    
    // Network
    implementation "com.squareup.retrofit2:retrofit:2.9.0"
    implementation "com.squareup.retrofit2:converter-gson:2.9.0"
    implementation "com.squareup.okhttp3:logging-interceptor:4.10.0"
    
    // QR Code
    implementation "com.journeyapps:zxing-android-embedded:4.3.0"
    
    // Biometric
    implementation "androidx.biometric:biometric:1.1.0"
    
    // Testing
    testImplementation "junit:junit:4.13.2"
    androidTestImplementation "androidx.test.ext:junit:1.1.5"
    androidTestImplementation "androidx.test.espresso:espresso-core:3.5.1"
}
```

## 📱 Core Components

### 1. MainActivity
**File**: `MainActivity.kt`
**Purpose**: Entry point aplikasi, menangani navigation dan lifecycle

```kotlin
class MainActivity : AppCompatActivity() {
    // Lifecycle management
    // Navigation handling
    // API connection check
}
```

### 2. Data Models
**Location**: `data/model/`

#### VICData
```kotlin
data class VICData(
    val patientId: String,
    val patientName: String,
    val medicalRecord: MedicalRecord,
    val hospitalId: String,
    val timestamp: Long,
    val hash: String
)
```

#### PatientData
```kotlin
data class PatientData(
    val id: String,
    val name: String,
    val dateOfBirth: String,
    val gender: String,
    val address: String
)
```

### 3. Network Layer
**Location**: `network/`

#### ApiClient
```kotlin
class ApiClient {
    companion object {
        fun getInstance(): ApiClient
        suspend fun verifyVICWithFastAPI(vicData: VICData): VerificationResult
        suspend fun verifyVICWithFlask(vicData: VICData): VerificationResult
        suspend fun checkHealth(): Boolean
    }
}
```

#### VICShareApiClient
```kotlin
class VICShareApiClient {
    suspend fun createVICShare(request: VICShareRequest): VICShareResponse
    suspend fun accessVICShare(shareId: String): VICShareAccessResponse
    suspend fun getPatientVICShares(patientId: String): List<VICShareData>
    suspend fun revokeVICShare(shareId: String): Boolean
}
```

### 4. UI Components

#### Activities
- **DigitalWalletActivity**: Main wallet interface
- **QRScannerActivity**: QR code scanning
- **VICDetailsActivity**: VIC detail view
- **ServerSettingsActivity**: Server configuration

#### Fragments
- **VICListFragment**: List of saved VICs
- **PatientListFragment**: List of patients

#### Adapters
- **VICAdapter**: RecyclerView adapter for VICs
- **PatientDataAdapter**: RecyclerView adapter for patients
- **MedicalRecordAdapter**: RecyclerView adapter for medical records

### 5. Utility Classes
**Location**: `utils/`

#### SecurityManager
```kotlin
class SecurityManager {
    companion object {
        fun encryptData(data: String): String
        fun decryptData(encryptedData: String): String
        fun generateHash(data: String): String
    }
}
```

#### BiometricAuthHelper
```kotlin
class BiometricAuthHelper {
    fun isBiometricAvailable(): Boolean
    fun showBiometricPrompt(callback: BiometricPrompt.AuthenticationCallback)
}
```

#### LocalStorage
```kotlin
class LocalStorage {
    fun saveVIC(vic: SavedVIC)
    fun getVIC(id: String): SavedVIC?
    fun getAllVICs(): List<SavedVIC>
    fun deleteVIC(id: String)
}
```

## 🔒 Security Implementation

### 1. Data Encryption
```kotlin
// Menggunakan AES encryption
private fun encryptData(data: String): String {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    // Implementation details...
}
```

### 2. Biometric Authentication
```kotlin
// Menggunakan Android Biometric API
private fun showBiometricPrompt() {
    val biometricPrompt = BiometricPrompt(activity, executor, callback)
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Authenticate")
        .setSubtitle("Use your biometric to authenticate")
        .setNegativeButtonText("Cancel")
        .build()
    
    biometricPrompt.authenticate(promptInfo)
}
```

### 3. PIN Protection
```kotlin
// 6-digit PIN validation
private fun validatePIN(pin: String): Boolean {
    return pin.length == 6 && pin.matches(Regex("\\d{6}"))
}
```

## 🌐 Network Configuration

### 1. API Endpoints
```kotlin
object Config {
    const val DEFAULT_API_BASE_URL = "http://localhost:8502"
    const val DEFAULT_FLASK_BASE_URL = "http://localhost:8505"
    
    // Endpoints
    const val VERIFY_VIC_ENDPOINT = "/api/verify-vic"
    const val HEALTH_CHECK_ENDPOINT = "/health"
    const val VIC_SHARE_ENDPOINT = "/api/vic-share"
}
```

### 2. Retrofit Configuration
```kotlin
private fun createRetrofit(baseUrl: String): Retrofit {
    val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()
    
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
```

## 🧪 Testing

### 1. Unit Tests
**Location**: `src/test/java/`

```kotlin
class VICVerificationTest {
    @Test
    fun testVICVerification() {
        // Test VIC verification logic
    }
}
```

### 2. Instrumented Tests
**Location**: `src/androidTest/java/`

```kotlin
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Test app context
    }
}
```

### 3. Test Data
```kotlin
object TestData {
    val sampleVIC = VICData(
        patientId = "P001",
        patientName = "John Doe",
        medicalRecord = MedicalRecord(...),
        hospitalId = "H001",
        timestamp = System.currentTimeMillis(),
        hash = "abc123"
    )
}
```

## 🔧 Build Configuration

### 1. Gradle Build Scripts

#### Project Level (`build.gradle.kts`)
```kotlin
plugins {
    id("com.android.application") version "7.4.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.0" apply false
}
```

#### App Level (`app/build.gradle.kts`)
```kotlin
android {
    compileSdk 33
    
    defaultConfig {
        applicationId "com.dicoding.didapp"
        minSdk 24
        targetSdk 33
        versionCode 1
        versionName "1.0"
    }
    
    buildTypes {
        release {
            isMinifyEnabled true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
```

### 2. ProGuard Rules
```proguard
# Keep data classes
-keep class com.dicoding.didapp.data.model.** { *; }

# Keep Retrofit interfaces
-keep interface com.dicoding.didapp.network.** { *; }

# Keep Gson annotations
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
```

## 📊 Performance Optimization

### 1. Memory Management
```kotlin
// Use WeakReference for callbacks
private var callback: WeakReference<Callback>? = null

// Clear references in onDestroy
override fun onDestroy() {
    super.onDestroy()
    callback = null
}
```

### 2. Network Optimization
```kotlin
// Use connection pooling
private val okHttpClient = OkHttpClient.Builder()
    .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
    .build()
```

### 3. Image Loading
```kotlin
// Use appropriate image loading library
// For QR codes, use simple bitmap loading
private fun loadQRCode(data: String): Bitmap {
    return QRCodeUtils.generateQRCode(data, 512, 512)
}
```

## 🐛 Debugging

### 1. Logging
```kotlin
// Use appropriate log levels
Log.d("DIDApp", "Debug message")
Log.i("DIDApp", "Info message")
Log.w("DIDApp", "Warning message")
Log.e("DIDApp", "Error message")
```

### 2. Debug Tools
```kotlin
// Enable debug mode
if (BuildConfig.DEBUG) {
    // Debug-specific code
}
```

### 3. Network Debugging
```kotlin
// Use HttpLoggingInterceptor
val logging = HttpLoggingInterceptor()
logging.level = HttpLoggingInterceptor.Level.BODY
```

## 📱 UI/UX Guidelines

### 1. Material Design
- Gunakan Material Design components
- Ikuti Material Design guidelines
- Gunakan consistent color scheme

### 2. Responsive Design
- Support berbagai ukuran layar
- Gunakan ConstraintLayout
- Test di berbagai device

### 3. Accessibility
- Tambahkan content descriptions
- Support screen readers
- Gunakan appropriate touch targets

## 🔄 Version Control

### 1. Git Workflow
```bash
# Feature branch
git checkout -b feature/new-feature

# Commit changes
git add .
git commit -m "feat: add new feature"

# Push and create PR
git push origin feature/new-feature
```

### 2. Commit Convention
- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation
- `style:` Code style changes
- `refactor:` Code refactoring
- `test:` Test changes

## 📚 Resources

### 1. Documentation
- [Android Developer Guide](https://developer.android.com/guide)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Retrofit Documentation](https://square.github.io/retrofit/)

### 2. Tools
- [Android Studio](https://developer.android.com/studio)
- [Postman](https://www.postman.com/) - API testing
- [Charles Proxy](https://www.charlesproxy.com/) - Network debugging

### 3. Libraries
- [Retrofit](https://square.github.io/retrofit/) - HTTP client
- [ZXing](https://github.com/journeyapps/zxing-android-embedded) - QR code
- [Gson](https://github.com/google/gson) - JSON parsing

## 🤝 Contributing

### 1. Code Style
- Ikuti Kotlin coding conventions
- Gunakan meaningful variable names
- Tambahkan comments untuk complex logic

### 2. Pull Request Process
1. Fork repository
2. Create feature branch
3. Make changes
4. Add tests
5. Create pull request

### 3. Code Review
- Review code quality
- Check security implications
- Verify functionality

---

**Happy Coding! 🚀**
