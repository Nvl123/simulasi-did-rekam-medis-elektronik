# 🏥 Hospital VIC Issuance System - Streamlit Version

Sistem penerbitan Verifiable Identity Credential (VIC) untuk rumah sakit menggunakan Streamlit.

## 🚀 Quick Start

### 1. Jalankan dengan Docker (Recommended)

```bash
# Build dan jalankan semua service
docker-compose up --build -d

# Cek status container
docker ps

# Lihat logs
docker-compose logs -f
```

### 2. Akses Aplikasi

- **🏥 Hospital 1 (Rumah Sakit A)**: http://localhost:8503
- **🏥 Hospital 2 (Rumah Sakit B)**: http://localhost:8504
- **🔗 Blockchain Server**: http://localhost:8501
- **📡 API Server**: http://localhost:8502
- **🗄️ Database**: localhost:3306

### 3. Stop Aplikasi

```bash
docker-compose down
```

## 📋 Fitur Aplikasi

### ✅ **Notifikasi yang Berfungsi**
- **Success Notification** (hijau) - saat VIC berhasil diterbitkan
- **Error Notification** (merah) - saat ada error API
- **Warning Notification** (kuning) - saat menggunakan mode demo
- **Info Notification** (biru) - untuk informasi umum

### 🎯 **Fitur Utama**
- **Form Input** yang user-friendly dengan validasi
- **QR Code Generation** untuk medical records
- **Real-time Notifications** dengan styling yang konsisten
- **Demo Mode** jika API tidak tersedia
- **Session State** untuk tracking VIC yang diterbitkan
- **Responsive Design** dengan sidebar informasi

### 🔧 **Technical Features**
- **Streamlit Framework** untuk UI yang modern
- **Docker Containerization** untuk deployment yang mudah
- **Health Checks** untuk monitoring
- **Auto-restart** jika container crash
- **Network Isolation** dengan Docker networks

## 📁 Struktur File

```
did-uts-new/
├── hospital1/
│   ├── app.py              # Streamlit app untuk Hospital 1
│   └── config.js           # Configuration (legacy)
├── hospital2/
│   ├── app.py              # Streamlit app untuk Hospital 2
│   └── config.js           # Configuration (legacy)
├── requirements.txt        # Python dependencies
├── Dockerfile.streamlit    # Dockerfile untuk Streamlit apps
├── docker-compose.yml      # Docker Compose configuration
└── README_STREAMLIT.md     # Dokumentasi ini
```

## 🛠️ Development

### Install Dependencies

```bash
pip install -r requirements.txt
```

### Run Locally (tanpa Docker)

```bash
# Hospital 1
streamlit run hospital1/app.py --server.port 8503

# Hospital 2 (terminal baru)
streamlit run hospital2/app.py --server.port 8504
```

## 🔍 Troubleshooting

### Container tidak start
```bash
# Cek logs
docker-compose logs hospital1
docker-compose logs hospital2

# Restart specific service
docker-compose restart hospital1
```

### Port conflict
```bash
# Cek port yang digunakan
netstat -tulpn | grep :850

# Stop semua container
docker-compose down
```

### Database connection issues
```bash
# Cek database status
docker-compose logs mariadb

# Restart database
docker-compose restart mariadb
```

## 📊 Monitoring

### Health Checks
- Hospital 1: `curl http://localhost:8503/_stcore/health`
- Hospital 2: `curl http://localhost:8504/_stcore/health`
- Blockchain: `curl http://localhost:8501/_stcore/health`
- API: `curl http://localhost:8502/api/health`

### Container Status
```bash
# Lihat semua container
docker ps

# Lihat resource usage
docker stats

# Lihat logs real-time
docker-compose logs -f
```

## 🎨 UI Features

### Notifications
- **Auto-dismiss** setelah 5 detik
- **Manual close** dengan tombol ×
- **Color-coded** berdasarkan tipe (success/error/warning/info)
- **Smooth animations** dengan slide-in effect

### Form Validation
- **Required fields** validation
- **Real-time feedback** untuk user
- **Loading states** saat processing

### QR Code
- **High-quality** QR code generation
- **Medical data** embedded dalam QR
- **Demo mode** indicator jika tidak tersimpan ke blockchain

## 🔐 Security

- **Network isolation** dengan Docker networks
- **No direct database access** dari hospital apps
- **API-based communication** dengan blockchain server
- **Input validation** untuk mencegah injection

## 📈 Performance

- **Lazy loading** untuk komponen besar
- **Session state** untuk caching
- **Efficient QR generation** dengan PIL
- **Optimized Docker images** dengan multi-stage builds

---

## 🎉 Success!

Aplikasi Streamlit sekarang berjalan dengan:
- ✅ **Notifikasi yang berfungsi** dengan styling yang konsisten
- ✅ **UI yang modern** dan user-friendly
- ✅ **Docker deployment** yang mudah
- ✅ **Health monitoring** yang reliable
- ✅ **Clean code structure** yang maintainable

**Hospital 1**: http://localhost:8503  
**Hospital 2**: http://localhost:8504
