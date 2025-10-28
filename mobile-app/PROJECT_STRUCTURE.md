# 📁 Project Structure - DID Mobile App

This document describes the structure and organization of the DID Mobile App project.

## 🏗️ Root Directory Structure

```
mobile-app/
├── 📁 app/                          # Main application module
├── 📁 gradle/                       # Gradle wrapper and configuration
├── 📄 build.gradle.kts              # Project-level build configuration
├── 📄 settings.gradle.kts           # Project settings
├── 📄 gradle.properties             # Gradle properties
├── 📄 gradlew                       # Gradle wrapper script (Unix)
├── 📄 gradlew.bat                   # Gradle wrapper script (Windows)
├── 📄 local.properties              # Local development properties
├── 📄 README.md                     # Project documentation
├── 📄 DEVELOPER_GUIDE.md            # Developer documentation
├── 📄 INSTALLATION_GUIDE.md         # Installation guide
├── 📄 CONTRIBUTING.md               # Contributing guidelines
├── 📄 SECURITY.md                   # Security policy
├── 📄 CHANGELOG.md                  # Version history
├── 📄 LICENSE                       # License information
├── 📄 PROJECT_STRUCTURE.md          # This file
└── 📄 .gitignore                    # Git ignore rules
```

## 📱 App Module Structure

```
app/
├── 📁 build/                        # Build outputs (generated)
├── 📄 build.gradle.kts              # App-level build configuration
├── 📄 proguard-rules.pro            # ProGuard rules
└── 📁 src/                          # Source code
    ├── 📁 androidTest/              # Instrumented tests
    ├── 📁 main/                     # Main source code
    └── 📁 test/                     # Unit tests
```

## 🔧 Main Source Code Structure

```
app/src/main/
├── 📄 AndroidManifest.xml           # App manifest
├── 📄 ic_launcher-playstore.png     # App icon
├── 📁 java/                         # Kotlin/Java source code
│   └── 📁 com/dicoding/didapp/      # Package structure
│       ├── 📄 MainActivity.kt       # Main activity
│       ├── 📁 data/                 # Data layer
│       ├── 📁 network/              # Network layer
│       ├── 📁 ui/                   # UI layer
│       └── 📁 utils/                # Utility classes
└── 📁 res/                          # Android resources
    ├── 📁 drawable/                 # Drawable resources
    ├── 📁 layout/                   # Layout files
    ├── 📁 menu/                     # Menu resources
    ├── 📁 mipmap-*/                 # App icons
    ├── 📁 values/                   # Values resources
    ├── 📁 values-night/             # Night mode values
    └── 📁 xml/                      # XML resources
```

## 📦 Package Structure

```
com.dicoding.didapp/
├── 📄 MainActivity.kt               # Main entry point
├── 📁 data/                         # Data layer
│   └── 📁 model/                    # Data models
│       ├── 📄 PatientData.kt        # Patient data model
│       ├── 📄 SavedVIC.kt           # VIC data model
│       ├── 📄 VICData.kt            # VIC data structure
│       ├── 📄 MedicalRecord.kt      # Medical record model
│       ├── 📄 VerificationResult.kt # Verification result model
│       └── 📄 VICShare*.kt          # VIC sharing models
├── 📁 network/                      # Network layer
│   ├── 📄 ApiClient.kt              # API client
│   └── 📄 VICShareApiClient.kt      # VIC sharing API client
├── 📁 ui/                           # UI layer
│   ├── 📁 auth/                     # Authentication UI
│   │   └── 📄 SecuritySettingsActivity.kt
│   ├── 📁 scanner/                  # QR scanner UI
│   │   ├── 📄 QRScannerActivity.kt
│   │   └── 📄 ManualInputActivity.kt
│   ├── 📁 settings/                 # Settings UI
│   │   └── 📄 ServerSettingsActivity.kt
│   └── 📁 wallet/                   # Digital wallet UI
│       ├── 📄 DigitalWalletActivity.kt
│       ├── 📄 VICDetailsActivity.kt
│       ├── 📄 PatientDetailsActivity.kt
│       ├── 📄 CreateVICShareActivity.kt
│       ├── 📄 AccessVICShareActivity.kt
│       ├── 📄 VICListFragment.kt
│       ├── 📄 PatientListFragment.kt
│       ├── 📄 WalletPagerAdapter.kt
│       ├── 📄 VICAdapter.kt
│       ├── 📄 PatientDataAdapter.kt
│       └── 📄 MedicalRecordAdapter.kt
└── 📁 utils/                        # Utility classes
    ├── 📄 AppSecurityManager.kt     # Security management
    ├── 📄 BiometricAuthHelper.kt    # Biometric authentication
    ├── 📄 Config.kt                 # Configuration constants
    ├── 📄 LocalStorage.kt           # Local storage utilities
    ├── 📄 QRCodeUtils.kt            # QR code utilities
    ├── 📄 SecurityManager.kt        # Security utilities
    └── 📄 ServerPreferences.kt      # Server preferences
```

## 🎨 Resources Structure

```
res/
├── 📁 drawable/                     # Drawable resources
│   ├── 📄 ic_*.xml                  # Vector icons
│   ├── 📄 button_*.xml              # Button styles
│   ├── 📄 card_*.xml                # Card styles
│   ├── 📄 gradient_*.xml            # Gradient backgrounds
│   └── 📄 bg_*.xml                  # Background styles
├── 📁 layout/                       # Layout files
│   ├── 📄 activity_*.xml            # Activity layouts
│   ├── 📄 fragment_*.xml            # Fragment layouts
│   ├── 📄 item_*.xml                # RecyclerView item layouts
│   └── 📄 dialog_*.xml              # Dialog layouts
├── 📁 menu/                         # Menu resources
│   └── 📄 digital_wallet_menu.xml   # Digital wallet menu
├── 📁 mipmap-*/                     # App icons (different densities)
│   ├── 📄 ic_launcher.webp          # App launcher icon
│   ├── 📄 ic_launcher_foreground.webp
│   └── 📄 ic_launcher_round.webp    # Round launcher icon
├── 📁 values/                       # Values resources
│   ├── 📄 colors.xml                # Color definitions
│   ├── 📄 strings.xml               # String resources
│   ├── 📄 themes.xml                # Theme definitions
│   └── 📄 ic_launcher_background.xml
├── 📁 values-night/                 # Night mode values
│   └── 📄 themes.xml                # Dark theme
└── 📁 xml/                          # XML resources
    ├── 📄 backup_rules.xml          # Backup rules
    ├── 📄 data_extraction_rules.xml # Data extraction rules
    └── 📄 network_security_config.xml # Network security config
```

## 🧪 Test Structure

```
src/
├── 📁 androidTest/                  # Instrumented tests
│   └── 📁 java/
│       └── 📁 com/dicoding/didapp/
│           └── 📄 ExampleInstrumentedTest.kt
└── 📁 test/                         # Unit tests
    └── 📁 java/
        └── 📁 com/dicoding/didapp/
            ├── 📄 ExampleUnitTest.kt
            └── 📄 VICVerificationTest.kt
```

## 🔧 Build Configuration

### Project Level (`build.gradle.kts`)
```kotlin
plugins {
    id("com.android.application") version "7.4.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.0" apply false
}
```

### App Level (`app/build.gradle.kts`)
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

## 📱 Key Components

### 1. MainActivity
- **Purpose**: Entry point of the application
- **Features**: Navigation, API connection check, lifecycle management
- **Location**: `MainActivity.kt`

### 2. Data Models
- **PatientData**: Patient information structure
- **VICData**: Verifiable Identity Credential data
- **MedicalRecord**: Medical record information
- **Location**: `data/model/`

### 3. Network Layer
- **ApiClient**: Main API communication
- **VICShareApiClient**: VIC sharing API
- **Location**: `network/`

### 4. UI Components
- **Activities**: Main UI screens
- **Fragments**: Reusable UI components
- **Adapters**: RecyclerView adapters
- **Location**: `ui/`

### 5. Utilities
- **SecurityManager**: Security utilities
- **LocalStorage**: Local data storage
- **QRCodeUtils**: QR code handling
- **Location**: `utils/`

## 🔒 Security Components

### 1. Authentication
- **BiometricAuthHelper**: Biometric authentication
- **SecuritySettingsActivity**: Security configuration
- **PIN Protection**: 6-digit PIN system

### 2. Data Protection
- **AppSecurityManager**: App-level security
- **SecurityManager**: Security utilities
- **Encrypted Storage**: Local data encryption

### 3. Network Security
- **HTTPS**: Secure communication
- **Certificate Pinning**: Server validation
- **Request Signing**: API request security

## 📊 Dependencies

### Core Dependencies
```kotlin
implementation("androidx.core:core-ktx:1.9.0")
implementation("androidx.appcompat:appcompat:1.6.1")
implementation("com.google.android.material:material:1.8.0")
```

### Network Dependencies
```kotlin
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
```

### QR Code Dependencies
```kotlin
implementation("com.journeyapps:zxing-android-embedded:4.3.0")
```

### Biometric Dependencies
```kotlin
implementation("androidx.biometric:biometric:1.1.0")
```

## 🚀 Build Outputs

### APK Files
- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release.apk`

### Test Reports
- **Unit Tests**: `app/build/reports/tests/testDebugUnitTest/`
- **Instrumented Tests**: `app/build/reports/tests/testDebugAndroidTest/`
- **Lint Reports**: `app/build/reports/lint-results-debug.html`

## 📝 Documentation Files

### Project Documentation
- **README.md**: Main project documentation
- **DEVELOPER_GUIDE.md**: Developer guide
- **INSTALLATION_GUIDE.md**: Installation instructions
- **CONTRIBUTING.md**: Contributing guidelines
- **SECURITY.md**: Security policy
- **CHANGELOG.md**: Version history

### Code Documentation
- **Inline Comments**: Code-level documentation
- **KDoc**: Kotlin documentation comments
- **README**: Package-level documentation

## 🔄 Development Workflow

### 1. Feature Development
1. Create feature branch
2. Implement feature
3. Write tests
4. Update documentation
5. Create pull request

### 2. Bug Fixes
1. Create fix branch
2. Fix bug
3. Add test case
4. Update documentation
5. Create pull request

### 3. Release Process
1. Update version numbers
2. Update CHANGELOG.md
3. Create release tag
4. Build release APK
5. Deploy to app stores

---

**This structure provides a clear organization for the DID Mobile App project, making it easy to navigate and maintain. 🚀**
