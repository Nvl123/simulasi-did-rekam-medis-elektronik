# 📥 Installation Guide - DID Mobile App

Panduan lengkap untuk menginstall dan mengkonfigurasi aplikasi DID Mobile App.

## 📋 Prerequisites

### System Requirements
- **Operating System**: Android 7.0 (API 24) atau lebih tinggi
- **RAM**: Minimal 2GB, direkomendasikan 4GB
- **Storage**: 100MB untuk instalasi, 500MB untuk data
- **Network**: Internet connection untuk API calls
- **Camera**: Untuk QR code scanning
- **Biometric**: Fingerprint atau Face unlock (opsional)

### Development Requirements
- **Android Studio**: Arctic Fox (2020.3.1) atau lebih baru
- **JDK**: Java 11 atau lebih baru
- **Android SDK**: API Level 24-33
- **Gradle**: 7.0 atau lebih baru
- **Git**: Untuk version control

## 🚀 Quick Start

### 1. Download APK (Production)
```bash
# Download APK dari releases
wget https://github.com/Nvl123/simulasi-did-rekam-medis-elektronik/releases/latest/download/did-app.apk

# Install via ADB
adb install did-app.apk
```

### 2. Build dari Source (Development)
```bash
# Clone repository
git clone https://github.com/Nvl123/simulasi-did-rekam-medis-elektronik.git
cd simulasi-did-rekam-medis-elektronik/mobile-app

# Build debug APK
./gradlew assembleDebug

# Install APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 🛠️ Detailed Installation

### Method 1: Android Studio (Recommended)

#### Step 1: Setup Android Studio
1. Download dan install [Android Studio](https://developer.android.com/studio)
2. Buka Android Studio
3. Install required SDK components:
   - Android SDK Platform 24-33
   - Android SDK Build-Tools
   - Android Emulator
   - Android SDK Platform-Tools

#### Step 2: Import Project
1. Buka Android Studio
2. Pilih "Open an existing project"
3. Navigate ke folder `mobile-app`
4. Pilih folder `mobile-app`
5. Klik "OK"

#### Step 3: Gradle Sync
1. Tunggu Gradle sync selesai
2. Jika ada error, install missing dependencies
3. Pastikan semua dependencies ter-resolve

#### Step 4: Build Project
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

#### Step 5: Run on Device/Emulator
1. Connect Android device atau start emulator
2. Enable USB debugging (untuk device)
3. Klik "Run" button di Android Studio
4. Pilih target device

### Method 2: Command Line

#### Step 1: Setup Environment
```bash
# Set ANDROID_HOME
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools

# Verify installation
adb version
```

#### Step 2: Clone dan Build
```bash
# Clone repository
git clone https://github.com/Nvl123/simulasi-did-rekam-medis-elektronik.git
cd simulasi-did-rekam-medis-elektronik/mobile-app

# Make gradlew executable
chmod +x gradlew

# Build project
./gradlew assembleDebug
```

#### Step 3: Install APK
```bash
# Install debug APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Install release APK
adb install app/build/outputs/apk/release/app-release.apk
```

## ⚙️ Configuration

### 1. Server Configuration

#### Via App Settings
1. Buka aplikasi
2. Tap menu Settings (⚙️)
3. Pilih "Server Settings"
4. Masukkan URL server:
   - **API Server**: `http://your-server-ip:8502`
   - **Flask API**: `http://your-server-ip:8505`
5. Tap "Test Connection"
6. Tap "Save" jika koneksi berhasil

#### Via Code (Development)
Edit file `app/src/main/java/com/dicoding/didapp/utils/Config.kt`:

```kotlin
object Config {
    const val DEFAULT_API_BASE_URL = "http://192.168.1.100:8502"
    const val DEFAULT_FLASK_BASE_URL = "http://192.168.1.100:8505"
}
```

### 2. Security Configuration

#### PIN Setup
1. Buka aplikasi untuk pertama kali
2. Aplikasi akan meminta setup PIN
3. Masukkan 6-digit PIN
4. Konfirmasi PIN
5. PIN akan digunakan untuk akses aplikasi

#### Biometric Setup
1. Buka Settings
2. Pilih "Security Settings"
3. Enable "Biometric Authentication"
4. Daftarkan fingerprint/face di device settings
5. Test biometric authentication

### 3. Network Configuration

#### HTTPS Setup (Production)
```kotlin
// Update Config.kt untuk HTTPS
object Config {
    const val DEFAULT_API_BASE_URL = "https://your-domain.com:8502"
    const val DEFAULT_FLASK_BASE_URL = "https://your-domain.com:8505"
}
```

#### Certificate Pinning (Optional)
```kotlin
// Add certificate pinning untuk security
private fun createOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .certificatePinner(
            CertificatePinner.Builder()
                .add("your-domain.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
                .build()
        )
        .build()
}
```

## 🔧 Troubleshooting

### Common Installation Issues

#### 1. Gradle Sync Failed
**Error**: `Gradle sync failed: Could not resolve dependencies`

**Solutions**:
```bash
# Clear Gradle cache
./gradlew clean

# Update dependencies
./gradlew build --refresh-dependencies

# Check internet connection
ping google.com
```

#### 2. Build Failed
**Error**: `Build failed with an exception`

**Solutions**:
```bash
# Check Java version
java -version

# Set JAVA_HOME
export JAVA_HOME=/path/to/java

# Clean and rebuild
./gradlew clean
./gradlew assembleDebug
```

#### 3. APK Installation Failed
**Error**: `INSTALL_FAILED_INSUFFICIENT_STORAGE`

**Solutions**:
```bash
# Check available storage
adb shell df /data

# Clear app data
adb shell pm clear com.dicoding.didapp

# Uninstall existing app
adb uninstall com.dicoding.didapp
```

#### 4. Device Not Recognized
**Error**: `adb: device not found`

**Solutions**:
```bash
# Check connected devices
adb devices

# Enable USB debugging
# Settings > Developer Options > USB Debugging

# Install drivers (Windows)
# Download and install device drivers
```

### Runtime Issues

#### 1. App Crashes on Startup
**Causes**: Missing permissions, corrupted data

**Solutions**:
```bash
# Clear app data
adb shell pm clear com.dicoding.didapp

# Grant permissions
adb shell pm grant com.dicoding.didapp android.permission.CAMERA
adb shell pm grant com.dicoding.didapp android.permission.INTERNET
```

#### 2. Network Connection Failed
**Causes**: Wrong server URL, server not running

**Solutions**:
1. Check server status
2. Verify server URL
3. Test network connectivity
4. Check firewall settings

#### 3. QR Code Scanning Not Working
**Causes**: Camera permission, corrupted camera

**Solutions**:
```bash
# Grant camera permission
adb shell pm grant com.dicoding.didapp android.permission.CAMERA

# Test camera
adb shell am start -a android.media.action.IMAGE_CAPTURE
```

## 📱 Device-Specific Setup

### Samsung Devices
1. Enable "Install unknown apps" untuk package installer
2. Disable "Samsung Knox" jika ada konflik
3. Enable "Developer Options" dan "USB Debugging"

### Xiaomi Devices
1. Enable "Install via USB" di Developer Options
2. Disable "MIUI Optimization" di Developer Options
3. Enable "USB Debugging (Security Settings)"

### Huawei Devices
1. Enable "Install apps from external sources"
2. Disable "HiSec" security features
3. Enable "Developer Options"

## 🔒 Security Considerations

### 1. Production Deployment
- Use signed APK dengan release keystore
- Enable ProGuard/R8 obfuscation
- Use HTTPS untuk semua API calls
- Implement certificate pinning

### 2. Development
- Use debug keystore untuk development
- Disable ProGuard untuk debugging
- Use HTTP untuk local development
- Enable logging untuk debugging

### 3. Testing
- Test di berbagai device dan Android versions
- Test dengan berbagai network conditions
- Test security features (PIN, biometric)
- Test error handling

## 📊 Performance Optimization

### 1. Build Optimization
```kotlin
// Enable build optimization
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
```

### 2. Runtime Optimization
- Use appropriate image sizes
- Implement lazy loading
- Use RecyclerView untuk large lists
- Optimize network calls

## 📝 Post-Installation

### 1. First Run Setup
1. **Launch app** untuk pertama kali
2. **Setup PIN** untuk keamanan
3. **Configure server** di Settings
4. **Test connection** ke server
5. **Enable biometric** (opsional)

### 2. Data Migration
Jika upgrade dari versi sebelumnya:
1. Backup data lama
2. Install versi baru
3. Restore data jika diperlukan

### 3. Verification
1. Test semua fitur utama
2. Verify security features
3. Test network connectivity
4. Test QR code scanning

## 📞 Support

### Getting Help
- **GitHub Issues**: [Create issue](https://github.com/Nvl123/simulasi-did-rekam-medis-elektronik/issues)
- **Documentation**: Lihat README.md dan DEVELOPER_GUIDE.md
- **Email**: [Your support email]

### Reporting Bugs
Saat melaporkan bug, sertakan:
- Device model dan Android version
- App version
- Steps to reproduce
- Log files (jika ada)
- Screenshots (jika relevan)

---

**Installation selesai! Selamat menggunakan DID Mobile App! 🎉**
