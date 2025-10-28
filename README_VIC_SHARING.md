# 🏥 VIC Sharing System - Quick Start Guide

## 📋 Ringkasan
Sistem VIC (Verifiable Identity Credential) Sharing memungkinkan rumah sakit untuk berbagi data medis pasien secara aman dengan kontrol penuh atas data yang di-share dan jangka waktunya.

## 🚀 Quick Start

### 1. Issue VIC (Penerbitan Kredensial)
1. Buka tab "📋 Issue VIC"
2. Isi data pasien (Patient ID, Name, Diagnosis, Treatment, Doctor)
3. Klik "🔗 Issue VIC to Blockchain"
4. Dapatkan QR code untuk akses data

### 2. VIC Sharing (Berbagi Data)
1. Buka tab "🔗 VIC Sharing" → "📤 Create VIC Share"
2. Masukkan Transaction Hash (dimulai dengan "0x")
3. Klik "🔍 Load VIC Data"
4. Konfigurasi sharing:
   - **Duration**: 1-8760 jam (default: 24 jam)
   - **Hospital**: Batasi ke rumah sakit tertentu (opsional)
   - **Permissions**: Pilih data yang dapat diakses
5. Klik "📤 Create VIC Share"
6. Dapatkan Share Token dan QR Code

### 3. Akses Data yang Di-share
1. Buka tab "🔗 VIC Sharing" → "🔍 Access Shared VIC"
2. Masukkan VIC Share Token (dimulai dengan "VIC_")
3. Klik "🔍 Access VIC Data"
4. Lihat data sesuai permission yang diberikan

## ⚙️ Kontrol Data Sharing

### Data yang Dapat Di-share:
- ✅ **Diagnosis** - Diagnosis medis
- ✅ **Treatment** - Perawatan yang diberikan  
- ✅ **Doctor** - Nama dokter
- ✅ **Date** - Tanggal perawatan
- ❌ **Notes** - Catatan tambahan (default: tidak di-share)

### Jangka Waktu Sharing:
- **Minimum**: 1 jam
- **Maximum**: 8760 jam (1 tahun)
- **Default**: 24 jam

### Kontrol Akses:
- **Universal**: Semua rumah sakit dapat akses
- **Restricted**: Hanya rumah sakit tertentu yang dapat akses

## 🔐 Keamanan
- Data tersimpan di blockchain (tidak dapat dimanipulasi)
- Token-based access dengan masa berlaku
- Kontrol granular atas data yang dapat diakses
- Akses otomatis berakhir setelah waktu yang ditentukan

## 📖 Dokumentasi Lengkap
Lihat file `VIC_SHARING_DOCUMENTATION.md` untuk panduan lengkap, troubleshooting, dan FAQ.

## 🆘 Dukungan
- **Email**: support@hospital-a.com
- **Phone**: +62-xxx-xxxx-xxxx
- **Hours**: 24/7

---
*Sistem VIC Sharing v1.0.0 - Rumah Sakit A*


