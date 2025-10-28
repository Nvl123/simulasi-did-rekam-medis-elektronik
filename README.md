# DID Blockchain Medical Records System

Sistem demonstrasi Decentralized Identity (DID) untuk rekam medis elektronik menggunakan blockchain.

## 🏗️ Arsitektur Sistem

Sistem ini terdiri dari 3 komponen utama:

1. **Blockchain Server** (Port 8501) - Streamlit app untuk memvisualisasikan blockchain
2. **Rumah Sakit A** (Port 8081) - Website rumah sakit pertama
3. **Rumah Sakit B** (Port 8082) - Website rumah sakit kedua

## 🚀 Cara Menjalankan

### Prerequisites
- Docker
- Docker Compose

### Langkah-langkah

1. **Clone dan masuk ke direktori proyek**
```bash
cd /root/did-uts-new
```

2. **Jalankan semua service dengan Docker Compose**
```bash
docker-compose up --build
```

3. **Akses aplikasi**
- Blockchain Explorer: http://localhost:8501
- Rumah Sakit A: http://localhost:8081
- Rumah Sakit B: http://localhost:8082

## 🔧 Fitur Utama

### Blockchain Server (Streamlit)
- Visualisasi blockchain real-time
- Mining transaksi
- Issue VIC (Verifiable Identity Credential)
- Analytics dan statistik
- Pencarian rekam medis pasien

### Website Rumah Sakit
- Interface untuk mengeluarkan VIC
- Form input data medis
- Generate QR Code untuk e-wallet
- Integrasi dengan blockchain

## 📱 Workflow Sistem

1. **Pasien datang ke rumah sakit**
2. **Dokter mengisi form VIC** di website rumah sakit
3. **Sistem mengeluarkan VIC** ke blockchain
4. **QR Code dihasilkan** untuk akses via e-wallet
5. **Blockchain mencatat transaksi** secara permanen
6. **Pasien dapat mengakses** rekam medis melalui e-wallet

## 🔗 API Endpoints

### Blockchain Server
- `GET /` - Dashboard blockchain
- `POST /api/issue-vic` - Issue VIC ke blockchain
- `GET /api/patient/{patient_id}` - Cari rekam medis pasien

## 🛠️ Teknologi yang Digunakan

- **Frontend**: HTML, CSS, JavaScript, Bootstrap
- **Backend**: Python, Streamlit
- **Blockchain**: Custom blockchain implementation
- **Containerization**: Docker, Docker Compose
- **Web Server**: Nginx

## 📊 Monitoring

Semua service memiliki health check yang akan memantau status:
- Blockchain server: `http://localhost:8501/_stcore/health`
- Hospital websites: `http://localhost:8081` dan `http://localhost:8082`

## 🔒 Keamanan

- Semua transaksi dienkripsi dengan SHA-256
- Blockchain immutable dan terdesentralisasi
- QR Code berisi hash transaksi untuk verifikasi

## 📝 Demo Scenario

1. Buka **Rumah Sakit A** (http://localhost:8081/vic-issuance.html)
2. Isi form dengan data pasien
3. Klik "Issue VIC to Blockchain"
4. Buka **Blockchain Explorer** (http://localhost:8501)
5. Lihat transaksi di tab "Pending Transactions"
6. Klik "Mine Pending Transactions"
7. Lihat transaksi di blockchain
8. Ulangi dengan **Rumah Sakit B**

## 🐛 Troubleshooting

### Service tidak bisa start
```bash
docker-compose down
docker-compose up --build
```

### Port sudah digunakan
```bash
# Cek port yang digunakan
netstat -tulpn | grep :8501
netstat -tulpn | grep :8081
netstat -tulpn | grep :8082

# Stop service yang menggunakan port
sudo kill -9 <PID>
```

### Reset semua data
```bash
docker-compose down -v
docker system prune -af
docker-compose up --build
```

## 📞 Support

Untuk pertanyaan atau masalah, silakan buka issue di repository ini.

---

**DID Blockchain Medical Records System**  
*Demonstrasi Decentralized Identity untuk Rekam Medis Elektronik*
