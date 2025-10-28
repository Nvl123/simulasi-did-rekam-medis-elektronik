# 📖 Panduan Penggunaan VIC Sharing - Hospital Interface

## 🎯 Cara Mengakses Fitur VIC Sharing

### Hospital A (Port 8503)
**URL**: http://localhost:8503

### Hospital B (Port 8504)
**URL**: http://localhost:8504

## 🔍 Cara Melihat Tab VIC Sharing

Setelah membuka aplikasi hospital di browser, Anda akan melihat:

1. **Header**: "Verifiable Identity Credential (VIC)"
2. **Sub-header**: "{Nama Hospital} - Medical Records & Sharing"
3. **Tabs**: Ada 2 tab di bagian atas konten:
   - 📋 **Issue VIC** (tab pertama - default)
   - 🔗 **VIC Sharing** (tab kedua - fitur sharing)

## 📝 Cara Menggunakan Fitur VIC Sharing

### Tab 1: Access Shared VIC (Mengakses VIC yang Dibagikan)

1. **Klik tab "🔗 VIC Sharing"**
2. **Pilih sub-tab "🔍 Access Shared VIC"**
3. **Masukkan VIC Share Token**:
   - Token format: `VIC_abc123def456...`
   - Token ini diberikan oleh pasien
4. **Klik tombol "🔍 Access VIC Data"**
5. **Lihat data medis yang dibagikan**:
   - Data akan ditampilkan sesuai permissions yang diberikan
   - Hanya data yang diizinkan yang akan terlihat

### Tab 2: Create VIC Share (Membuat VIC Share)

1. **Klik tab "🔗 VIC Sharing"**
2. **Pilih sub-tab "📤 Create VIC Share"**
3. **Masukkan Transaction Hash**:
   - Hash dari VIC yang ingin dibagikan
   - Format: `0x...`
4. **Klik tombol "🔍 Load VIC Data"**
5. **Konfigurasikan sharing**:
   - **Shared By**: Nama yang membagikan (default: "Patient")
   - **Expires in**: Berapa lama share berlaku (1-8760 jam)
   - **Specific Hospital**: Rumah sakit spesifik (optional)
   - **Permissions**: Centang data apa saja yang dibagikan
     - ✅ Diagnosis
     - ✅ Treatment
     - ✅ Doctor
     - ✅ Date
     - ❌ Notes (default tidak dibagikan)
6. **Klik tombol "📤 Create VIC Share"**
7. **QR Code dan Share Token akan ditampilkan**

## 🧪 Testing Workflow

### Scenario 1: Hospital A Issue VIC, Patient Share ke Hospital B

1. **Di Hospital A (localhost:8503)**:
   ```
   - Buka tab "📋 Issue VIC"
   - Isi form VIC
   - Klik "Issue VIC"
   - Copy Transaction Hash yang dihasilkan
   ```

2. **Buat VIC Share**:
   ```
   - Klik tab "🔗 VIC Sharing"
   - Klik sub-tab "📤 Create VIC Share"
   - Paste Transaction Hash
   - Klik "Load VIC Data"
   - Set permissions dan expiration
   - Klik "Create VIC Share"
   - Copy Share Token yang dihasilkan
   ```

3. **Di Hospital B (localhost:8504)**:
   ```
   - Buka tab "🔗 VIC Sharing"
   - Klik sub-tab "🔍 Access Shared VIC"
   - Paste Share Token
   - Klik "Access VIC Data"
   - Lihat data medis yang dibagikan
   ```

## 🔑 API Endpoints (untuk referensi)

```
POST /api/vic-share/create
GET  /api/vic-share/{share_token}?hospital={hospital_name}
GET  /api/vic-share/patient/{patient_id}
POST /api/vic-share/{share_token}/revoke
GET  /api/vic-share/{share_token}/access-logs
```

## ⚠️ Troubleshooting

### Jika Tab VIC Sharing Tidak Muncul:

1. **Clear Browser Cache**:
   - Tekan Ctrl+Shift+R (Windows/Linux)
   - Tekan Cmd+Shift+R (Mac)

2. **Restart Container**:
   ```bash
   docker restart did-hospital1 did-hospital2
   ```

3. **Cek Log untuk Error**:
   ```bash
   docker logs did-hospital1 --tail 50
   docker logs did-hospital2 --tail 50
   ```

4. **Verifikasi Container Berjalan**:
   ```bash
   docker ps
   ```

### Jika Share Token Tidak Bekerja:

1. **Cek Expiration**: Token mungkin sudah expired
2. **Cek Hospital Name**: Pastikan nama hospital sesuai
3. **Cek Transaction Hash**: Pastikan VIC sudah ada di database

## 📱 Struktur UI

```
┌─────────────────────────────────────────────────────┐
│  🏥 Verifiable Identity Credential (VIC)            │
│  Rumah Sakit A - Medical Records & Sharing          │
├─────────────────────────────────────────────────────┤
│  [📋 Issue VIC]  [🔗 VIC Sharing]  ← TABS          │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Tab Content Area                                   │
│                                                     │
│  (Konten tab yang dipilih akan muncul di sini)     │
│                                                     │
└─────────────────────────────────────────────────────┘
```

## 💡 Tips

1. **Save Share Token**: Simpan share token dengan aman
2. **Set Expiration Wisely**: Jangan terlalu lama untuk keamanan
3. **Limit Permissions**: Hanya bagikan data yang diperlukan
4. **Monitor Access**: Cek access logs secara berkala
5. **Revoke When Done**: Revoke access setelah tidak diperlukan

## 📞 Contact

Jika ada pertanyaan atau masalah, silakan hubungi tim development.



