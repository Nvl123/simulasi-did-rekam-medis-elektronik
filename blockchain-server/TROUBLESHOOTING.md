# DID Blockchain Server - Troubleshooting Guide

## 🚀 Quick Status Check
```bash
cd /root/did-uts-new/blockchain-server
python3 check_status.py
```

## 🔧 Common Issues & Solutions

### 1. Port Conflicts
**Problem**: Services tidak bisa start karena port sudah digunakan
**Solution**:
```bash
# Kill processes on specific ports
sudo lsof -ti:8501 | xargs kill -9
sudo lsof -ti:8502 | xargs kill -9

# Or kill all blockchain processes
pkill -f "streamlit.*8501"
pkill -f "python.*api_server"
pkill -f "python.*flask_api"
```

### 2. Database Connection Issues
**Problem**: Database tidak bisa connect
**Solution**:
```bash
# Check database connection
mysql -u root -p -e "SHOW DATABASES;"

# Create database if needed
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS did_blockchain;"
```

### 3. Service Tidak Berjalan
**Problem**: API atau Streamlit tidak respond
**Solution**:
```bash
# Restart all services
./start_services.sh

# Or start manually
python3 api_server.py &
streamlit run app.py --server.port=8501 --server.address=0.0.0.0 --server.headless=true &
```

### 4. Dependencies Issues
**Problem**: Module tidak ditemukan
**Solution**:
```bash
# Install dependencies
pip3 install -r requirements.txt

# Or install specific packages
pip3 install streamlit fastapi uvicorn sqlalchemy pymysql
```

## 📊 Service Status

### ✅ Healthy Status
- **Streamlit UI**: http://localhost:8501 (Status: 200)
- **API Server**: http://localhost:8502 (Status: 200)
- **Database**: Connected with data

### 🔍 Check Endpoints
```bash
# Check API server
curl http://localhost:8502/

# Check VIC issuances
curl http://localhost:8502/api/vic-issuances

# Check transactions
curl http://localhost:8502/api/transactions

# Check Streamlit health
curl http://localhost:8501/_stcore/health
```

## 🛠️ Manual Service Management

### Start Services
```bash
# Start API server
cd /root/did-uts-new/blockchain-server
python3 api_server.py &

# Start Streamlit
streamlit run app.py --server.port=8501 --server.address=0.0.0.0 --server.headless=true &
```

### Stop Services
```bash
# Find and kill processes
ps aux | grep -E "(streamlit|api_server)"
kill <PID>

# Or kill by port
sudo lsof -ti:8501 | xargs kill -9
sudo lsof -ti:8502 | xargs kill -9
```

## 📝 Logs
- **API Server**: `api_server.log`
- **Streamlit**: `streamlit.log`
- **System**: `/var/log/syslog`

## 🔄 Restart Everything
```bash
cd /root/did-uts-new/blockchain-server
./start_services.sh
```

## 📞 Support
Jika masih ada masalah, check:
1. Port availability: `netstat -tlnp | grep -E "(8501|8502)"`
2. Process status: `ps aux | grep -E "(streamlit|python)"`
3. Log files: `tail -f api_server.log streamlit.log`
