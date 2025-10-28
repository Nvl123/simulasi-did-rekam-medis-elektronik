# 📱 DID Mobile App - Digital Wallet untuk Rekam Medis Elektronik

Aplikasi mobile Android yang mengintegrasikan dengan sistem DID Blockchain untuk mengelola rekam medis elektronik secara aman dan terdesentralisasi.

## 🏗️ Arsitektur Aplikasi

Aplikasi ini dibangun menggunakan:
- **Platform**: Android (Kotlin)
- **UI Framework**: Android Views dengan Material Design
- **Network**: Retrofit untuk API calls
- **Security**: Biometric authentication, PIN protection
- **QR Code**: ZXing untuk scanning dan generation
- **Storage**: SharedPreferences untuk local storage

## 🚀 Fitur Utama

### 1. Digital Wallet
- **VIC Management**: Menyimpan dan mengelola Verifiable Identity Credentials
- **Patient Data**: Menyimpan data pasien dan rekam medis
- **QR Code Scanner**: Scan QR code untuk akses VIC
- **VIC Sharing**: Berbagi akses VIC dengan pihak lain

### 2. Security Features
- **Biometric Authentication**: Fingerprint/Face unlock
- **PIN Protection**: 6-digit PIN untuk akses aplikasi
- **Encrypted Storage**: Data tersimpan dengan enkripsi
- **Secure Communication**: HTTPS untuk semua API calls

### 3. VIC Verification
- **Real-time Verification**: Verifikasi VIC dengan blockchain
- **Multiple API Support**: FastAPI dan Flask API
- **Offline Support**: Cache data untuk akses offline
- **Status Tracking**: Monitor status verifikasi

### 4. User Interface
- **Material Design**: UI modern dan user-friendly
- **Dark/Light Theme**: Dukungan tema gelap dan terang
- **Responsive Layout**: Optimal di berbagai ukuran layar
- **Accessibility**: Dukungan aksesibilitas

## 📋 Prerequisites

### Development Requirements
- **Android Studio**: Arctic Fox atau lebih baru
- **JDK**: Java 11 atau lebih baru
- **Android SDK**: API Level 24 (Android 7.0) atau lebih tinggi
- **Gradle**: 7.0 atau lebih baru

### Runtime Requirements
- **Android Version**: 7.0 (API 24) atau lebih tinggi
- **RAM**: Minimal 2GB
- **Storage**: 100MB untuk instalasi
- **Network**: Internet connection untuk API calls

## 🛠️ Instalasi dan Setup

### 1. Clone Repository
```bash
git clone https://github.com/Nvl123/simulasi-did-rekam-medis-elektronik.git
cd simulasi-did-rekam-medis-elektronik/mobile-app
```

### 2. Buka di Android Studio
1. Buka Android Studio
2. Pilih "Open an existing project"
3. Navigate ke folder `mobile-app`
4. Tunggu Gradle sync selesai

### 3. Konfigurasi Server
1. Buka `app/src/main/java/com/dicoding/didapp/utils/Config.kt`
2. Update URL server sesuai dengan setup Anda:
```kotlin
object Config {
    const val DEFAULT_API_BASE_URL = "http://your-server-ip:8502"
    const val DEFAULT_FLASK_BASE_URL = "http://your-server-ip:8505"
}
```

### 4. Build dan Install
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

## 🔧 Konfigurasi

### Server Settings
Aplikasi mendukung konfigurasi server dinamis melalui Settings:

1. Buka aplikasi
2. Tap menu Settings (⚙️)
3. Pilih "Server Settings"
4. Masukkan URL server yang sesuai:
   - **API Server**: `http://your-ip:8502`
   - **Flask API**: `http://your-ip:8505`

### Security Settings
1. Buka Settings
2. Pilih "Security Settings"
3. Konfigurasi:
   - **PIN**: Set 6-digit PIN
   - **Biometric**: Enable/disable biometric auth
   - **Auto-lock**: Set timeout untuk auto-lock

## 📱 Cara Penggunaan

### 1. Setup Awal
1. **Install aplikasi** di device Android
2. **Set PIN** untuk keamanan
3. **Enable biometric** (opsional)
4. **Konfigurasi server** di Settings

### 2. Scan VIC QR Code
1. Buka aplikasi
2. Tap "Scan QR Code"
3. Arahkan kamera ke QR code
4. Tunggu proses scanning
5. VIC akan tersimpan otomatis

### 3. Manual Input VIC
1. Buka aplikasi
2. Tap "Manual Input"
3. Masukkan data VIC secara manual
4. Tap "Save VIC"

### 4. Kelola VIC
1. Buka tab "VIC List"
2. Pilih VIC yang ingin dikelola
3. Tap untuk melihat detail
4. Gunakan tombol aksi sesuai kebutuhan

### 5. Berbagi VIC
1. Pilih VIC yang ingin dibagikan
2. Tap "Share VIC"
3. Set permissions dan durasi
4. Generate QR code untuk sharing

## 🔒 Keamanan

### Data Protection
- **Encryption**: Semua data dienkripsi sebelum disimpan
- **Secure Storage**: Menggunakan Android Keystore
- **PIN Protection**: 6-digit PIN untuk akses aplikasi
- **Biometric Auth**: Fingerprint/Face unlock

### Network Security
- **HTTPS Only**: Semua komunikasi menggunakan HTTPS
- **Certificate Pinning**: Validasi sertifikat server
- **Request Signing**: Request ditandatangani untuk validasi

### Privacy
- **Local Storage**: Data disimpan lokal di device
- **No Cloud Sync**: Tidak ada sinkronisasi ke cloud
- **User Control**: User memiliki kontrol penuh atas data

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Manual Testing
1. **VIC Scanning**: Test dengan berbagai format QR code
2. **Network**: Test dengan berbagai kondisi jaringan
3. **Security**: Test PIN dan biometric authentication
4. **UI**: Test di berbagai ukuran layar

## 📊 Monitoring dan Debugging

### Logs
Aplikasi menggunakan Android Log dengan tag:
- `DIDApp`: General app logs
- `VICVerification`: VIC verification logs
- `Network`: Network request logs
- `Security`: Security-related logs

### Debug Mode
```bash
# Enable debug logging
adb shell setprop log.tag.DIDApp VERBOSE
```

## 🐛 Troubleshooting

### Common Issues

#### 1. VIC Tidak Bisa Di-scan
- **Penyebab**: QR code tidak valid atau rusak
- **Solusi**: Pastikan QR code jelas dan tidak rusak

#### 2. Koneksi Server Gagal
- **Penyebab**: Server tidak running atau URL salah
- **Solusi**: 
  1. Pastikan server running
  2. Cek URL di Settings
  3. Test koneksi di Server Settings

#### 3. Biometric Tidak Berfungsi
- **Penyebab**: Device tidak support atau tidak terdaftar
- **Solusi**: 
  1. Pastikan device support biometric
  2. Daftarkan fingerprint/face di device settings
  3. Enable biometric di app settings

#### 4. Aplikasi Crash
- **Penyebab**: Memory issue atau bug
- **Solusi**: 
  1. Restart aplikasi
  2. Clear app data
  3. Update ke versi terbaru

### Debug Steps
1. **Check Logs**: Gunakan `adb logcat` untuk melihat error
2. **Test Network**: Pastikan server accessible
3. **Clear Data**: Clear app data dan setup ulang
4. **Reinstall**: Uninstall dan install ulang

## 🔄 Update dan Maintenance

### Version Management
- **Version Code**: Increment untuk setiap release
- **Version Name**: Semantic versioning (e.g., 1.0.0)
- **Changelog**: Dokumentasi perubahan di setiap versi

### Backup dan Restore
- **Local Backup**: Data tersimpan di device
- **Export VIC**: Export VIC data untuk backup
- **Import VIC**: Import VIC data dari backup

## 📞 Support

### Documentation
- **API Documentation**: Lihat dokumentasi API server
- **Code Comments**: Kode dilengkapi dengan komentar
- **README**: Dokumentasi lengkap di repository

### Contact
- **Issues**: Buat issue di GitHub repository
- **Email**: [Your email]
- **Discord**: [Your Discord]

## 📄 License

Aplikasi ini menggunakan lisensi MIT. Lihat file `LICENSE` untuk detail.

## 🤝 Contributing

1. Fork repository
2. Buat feature branch
3. Commit perubahan
4. Push ke branch
5. Buat Pull Request

## 📈 Roadmap

### Version 1.1
- [ ] Offline mode support
- [ ] Cloud backup integration
- [ ] Advanced security features
- [ ] Multi-language support

### Version 1.2
- [ ] Wear OS support
- [ ] Widget support
- [ ] Advanced analytics
- [ ] Custom themes

---

**DID Mobile App**  
_Digital Wallet untuk Rekam Medis Elektronik yang Aman dan Terdesentralisasi_
