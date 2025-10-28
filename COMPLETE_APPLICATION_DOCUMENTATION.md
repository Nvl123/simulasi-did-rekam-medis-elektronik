# 📚 Dokumentasi Lengkap Aplikasi VIC Sharing System

## 🎯 Overview

Sistem VIC (Verifiable Identity Credential) Sharing adalah aplikasi blockchain untuk rekam medis elektronik yang memungkinkan pasien membagikan data medis mereka ke rumah sakit lain dengan kontrol penuh atas akses dan waktu berlakunya.

## 🏗️ Arsitektur Sistem

### Komponen Utama:
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Android App   │    │  Hospital A     │    │  Hospital B     │
│   (QR Scanner)  │───▶│  (Port 8503)    │───▶│  (Port 8504)    │
│                 │    │  VIC Sharing    │    │  VIC Sharing    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   QR Code       │    │  API Server     │    │   Database      │
│   (VIC Share)   │    │  (Port 8502)    │    │   (MariaDB)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### URL Endpoints:
- **Hospital A**: http://localhost:8503
- **Hospital B**: http://localhost:8504
- **API Server**: http://localhost:8502
- **Blockchain Server**: http://localhost:8501
- **Database**: localhost:3306

## 🚀 Cara Menjalankan Aplikasi

### Prerequisites:
- Docker
- Docker Compose
- Browser modern (Chrome, Firefox, Safari, Edge)

### Langkah-langkah:

1. **Clone dan masuk ke direktori proyek**:
   ```bash
   cd /root/did-uts-new
   ```

2. **Jalankan semua service dengan Docker Compose**:
   ```bash
   docker-compose up --build
   ```

3. **Akses aplikasi**:
   - Hospital A: http://localhost:8503
   - Hospital B: http://localhost:8504
   - API Documentation: http://localhost:8502/docs

## 📱 Panduan Penggunaan Hospital Interface

### Hospital A (Port 8503)

#### 1. Tab "📋 Issue VIC" (Fitur Existing)
- **Fungsi**: Mengeluarkan VIC baru untuk pasien
- **Langkah-langkah**:
  1. Isi form data pasien (Patient ID, Name, Diagnosis, Treatment, Doctor, Date, Notes)
  2. Klik "Issue VIC"
  3. QR Code akan ditampilkan untuk pasien
  4. Transaction hash akan dihasilkan

#### 2. Tab "🔗 VIC Sharing" (Fitur Baru)

##### Sub-tab "🔍 Access Shared VIC":
- **Fungsi**: Mengakses VIC yang dibagikan oleh pasien
- **Langkah-langkah**:
  1. Masukkan VIC Share Token (format: `VIC_abc123...`)
  2. Klik "🔍 Access VIC Data"
  3. Data medis akan ditampilkan sesuai permissions

##### Sub-tab "📤 Create VIC Share":
- **Fungsi**: Membuat VIC share untuk dibagikan ke rumah sakit lain
- **Langkah-langkah**:
  1. Masukkan Transaction Hash dari VIC yang ingin dibagikan
  2. Klik "🔍 Load VIC Data"
  3. Konfigurasikan sharing options:
     - **Shared By**: Nama yang membagikan (default: "Patient")
     - **Expires in**: Waktu kadaluarsa (1-8760 jam)
     - **Specific Hospital**: Rumah sakit spesifik (optional)
     - **Permissions**: Centang data yang boleh diakses
  4. Klik "📤 Create VIC Share"
  5. QR Code dan Share Token akan ditampilkan

### Hospital B (Port 8504)
- **Fungsi**: Sama dengan Hospital A
- **Perbedaan**: Nama hospital otomatis terisi "Rumah Sakit B"

## 🔐 Fitur Keamanan

### 1. Time-based Expiration
- **Default**: 24 jam
- **Range**: 1 jam - 1 tahun (8760 jam)
- **Automatic**: Expired shares otomatis ditolak

### 2. Permission Controls
- **Granular Access**: Kontrol detail data yang bisa diakses
- **Available Permissions**:
  - ✅ **Diagnosis**: Informasi diagnosis (default: allowed)
  - ✅ **Treatment**: Detail pengobatan (default: allowed)
  - ✅ **Doctor**: Informasi dokter (default: allowed)
  - ✅ **Date**: Tanggal kunjungan (default: allowed)
  - ❌ **Notes**: Catatan tambahan (default: restricted)

### 3. Hospital Restrictions
- **Specific Hospital**: Restrict akses ke satu rumah sakit saja
- **Any Hospital**: Allow akses dari rumah sakit manapun (default)

### 4. Access Logging
- **Complete Audit Trail**: Semua akses dicatat
- **Detailed Information**: Hospital, timestamp, data yang diakses
- **Privacy Monitoring**: Pasien bisa lihat siapa yang akses data mereka

## 🔌 API Documentation

### Base URL: `http://localhost:8502`

#### 1. Create VIC Share
```http
POST /api/vic-share/create
Content-Type: application/json

{
    "transaction_hash": "0x5dff8cd1f681461c87ca379738c62290ad7db473",
    "patient_id": "P001",
    "shared_by": "Patient",
    "shared_with_hospital": "Rumah Sakit B",
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

**Response**:
```json
{
    "success": true,
    "share_token": "VIC_abc123def456...",
    "expires_at": "2024-01-16T10:30:00.000Z",
    "message": "VIC share created successfully"
}
```

#### 2. Access Shared VIC
```http
GET /api/vic-share/{share_token}?hospital={hospital_name}
```

**Response**:
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

#### 3. Get Patient VIC Shares
```http
GET /api/vic-share/patient/{patient_id}
```

#### 4. Revoke VIC Share
```http
POST /api/vic-share/{share_token}/revoke
```

#### 5. Get Access Logs
```http
GET /api/vic-share/{share_token}/access-logs
```

## 📱 Android Integration

### QR Code Format untuk VIC Sharing:
```json
{
    "type": "VIC_SHARE",
    "share_token": "VIC_abc123def456...",
    "hospital": "Rumah Sakit A",
    "expires_at": "2024-01-16T10:30:00.000Z"
}
```

### Android App Features:
1. **Scan VIC Share QR Code**
2. **Access Shared Medical Data**
3. **Create VIC Shares**
4. **Manage Share Permissions**
5. **View Access Logs**

### Dependencies untuk Android:
```gradle
dependencies {
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
    implementation 'com.google.zxing:core:3.5.2'
    implementation 'com.journeyapps:zxing-android-embedded:4.3.0'
}
```

## 🗄️ Database Schema

### Tabel VICShares:
```sql
CREATE TABLE vic_shares (
    id INT PRIMARY KEY AUTO_INCREMENT,
    share_token VARCHAR(255) UNIQUE NOT NULL,
    original_transaction_hash VARCHAR(255) NOT NULL,
    patient_id VARCHAR(255) NOT NULL,
    shared_by VARCHAR(255) NOT NULL,
    shared_with_hospital VARCHAR(255),
    access_permissions TEXT,
    expires_at DATETIME,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_accessed DATETIME
);
```

### Tabel VICAccessLogs:
```sql
CREATE TABLE vic_access_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    share_token VARCHAR(255) NOT NULL,
    accessed_by_hospital VARCHAR(255) NOT NULL,
    accessed_data TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

## 🧪 Testing Workflow

### Scenario 1: Patient dari Hospital A ke Hospital B

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

### Test Data:
- **Test Share Token**: `VIC_test123456789`
- **Test Hospital Names**: "Rumah Sakit A", "Rumah Sakit B"
- **Test Patient ID**: "P001", "P002"

## 🔧 Troubleshooting

### Jika Tab VIC Sharing Tidak Muncul:

1. **Clear Browser Cache**:
   - Windows/Linux: `Ctrl + Shift + R`
   - Mac: `Cmd + Shift + R`

2. **Restart Container**:
   ```bash
   docker restart did-hospital1 did-hospital2
   ```

3. **Rebuild Container** (jika ada perubahan kode):
   ```bash
   docker-compose build hospital1 hospital2
   docker-compose up -d hospital1 hospital2
   ```

4. **Cek Log untuk Error**:
   ```bash
   docker logs did-hospital1 --tail 50
   docker logs did-hospital2 --tail 50
   ```

### Jika Share Token Tidak Bekerja:

1. **Cek Expiration**: Token mungkin sudah expired
2. **Cek Hospital Name**: Pastikan nama hospital sesuai
3. **Cek Transaction Hash**: Pastikan VIC sudah ada di database
4. **Cek API Server**: Pastikan API server berjalan di port 8502

### Jika API Error:

1. **Cek API Server Status**:
   ```bash
   curl http://localhost:8502/api/health
   ```

2. **Cek Database Connection**:
   ```bash
   docker exec did-mariadb mysql -u root -ppassword -e "USE did_blockchain; SHOW TABLES;"
   ```

3. **Restart API Server**:
   ```bash
   docker restart did-api-server
   ```

## 📊 Monitoring dan Maintenance

### Health Checks:
- **API Server**: `GET /api/health`
- **Hospital Apps**: `GET /_stcore/health`
- **Database**: `mysqladmin ping`

### Log Locations:
- **Hospital 1**: `docker logs did-hospital1`
- **Hospital 2**: `docker logs did-hospital2`
- **API Server**: `docker logs did-api-server`
- **Database**: `docker logs did-mariadb`

### Backup Strategy:
```bash
# Backup database
docker exec did-mariadb mysqldump -u root -ppassword did_blockchain > backup.sql

# Restore database
docker exec -i did-mariadb mysql -u root -ppassword did_blockchain < backup.sql
```

## 🚀 Deployment ke Production

### Environment Variables:
```bash
# Database
MYSQL_ROOT_PASSWORD=your_secure_password
MYSQL_DATABASE=did_blockchain
MYSQL_USER=did_user
MYSQL_PASSWORD=your_secure_password

# API Server
DATABASE_URL=mysql+pymysql://root:your_secure_password@mariadb:3306/did_blockchain
```

### Security Considerations:
1. **Change Default Passwords**
2. **Use HTTPS in Production**
3. **Implement Rate Limiting**
4. **Add Authentication/Authorization**
5. **Regular Security Updates**
6. **Monitor Access Logs**

### Scaling:
- **Load Balancer**: Nginx atau HAProxy
- **Database**: MariaDB Cluster atau MySQL Cluster
- **Caching**: Redis untuk session management
- **CDN**: Untuk static assets

## 📞 Support dan Contact

### Documentation:
- **API Docs**: http://localhost:8502/docs
- **User Guide**: `VIC_SHARING_USER_GUIDE.md`
- **Android Guide**: `ANDROID_VIC_SHARING_GUIDE.md`
- **Implementation Summary**: `VIC_SHARING_IMPLEMENTATION_SUMMARY.md`

### Common Issues:
1. **Container tidak start**: Cek Docker dan Docker Compose
2. **Port conflict**: Cek port yang digunakan
3. **Database connection error**: Cek database container
4. **API tidak response**: Cek API server container
5. **UI tidak load**: Clear browser cache

### Performance Tips:
1. **Use SSD storage** untuk database
2. **Allocate sufficient RAM** untuk containers
3. **Monitor resource usage** dengan `docker stats`
4. **Optimize database queries**
5. **Use connection pooling**

## 🎯 Roadmap dan Future Enhancements

### Planned Features:
1. **Mobile App**: Native iOS dan Android apps
2. **Advanced Analytics**: Dashboard untuk monitoring
3. **Multi-language Support**: Bahasa Indonesia dan English
4. **Integration APIs**: Integrasi dengan sistem rumah sakit existing
5. **Blockchain Explorer**: Interface untuk melihat blockchain
6. **Notification System**: Email/SMS notifications
7. **Audit Reports**: Laporan audit compliance
8. **Backup Automation**: Automated backup system

### Technical Improvements:
1. **Microservices Architecture**: Split monolith ke microservices
2. **Event-driven Architecture**: Async processing dengan message queues
3. **GraphQL API**: Alternative to REST API
4. **Real-time Updates**: WebSocket untuk real-time notifications
5. **Advanced Security**: OAuth2, JWT, encryption at rest
6. **Performance Optimization**: Caching, indexing, query optimization

---

**Dokumentasi ini akan terus diupdate sesuai dengan perkembangan aplikasi. Untuk pertanyaan atau bantuan, silakan hubungi tim development.**



