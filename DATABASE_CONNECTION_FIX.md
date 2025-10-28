# Database Connection Fix

## Masalah
Streamlit tidak bisa mengakses database MariaDB dengan error:
```
Database error: (pymysql.err.OperationalError) (2003, "Can't connect to MySQL server on 'localhost' ([Errno 111] Connection refused)")
```

## Root Cause
Streamlit container tidak memiliki environment variable `DATABASE_URL` yang diperlukan untuk koneksi ke database.

## Solusi
Menambahkan environment variable `DATABASE_URL` ke Streamlit container di `docker-compose.yml`:

### 1. Update docker-compose.yml
```yaml
blockchain-server:
  build: ./blockchain-server
  container_name: did-blockchain-server
  ports:
    - "8501:8501"
  environment:
    - STREAMLIT_SERVER_PORT=8501
    - STREAMLIT_SERVER_ADDRESS=0.0.0.0
    - DATABASE_URL=mysql+pymysql://root:password@mariadb:3306/did_blockchain  # ← TAMBAHAN
  volumes:
    - ./blockchain-server:/app
  networks:
    - did-network
  depends_on:
    - mariadb  # ← TAMBAHAN
```

### 2. Restart Container
```bash
# Stop dan remove container lama
docker-compose down blockchain-server

# Start container baru dengan environment variable
docker-compose up -d blockchain-server
```

## Testing
```bash
# Test environment variable
docker exec did-blockchain-server python -c "import os; print('DATABASE_URL:', os.getenv('DATABASE_URL', 'NOT SET'))"
# Result: DATABASE_URL: mysql+pymysql://root:password@mariadb:3306/did_blockchain

# Test database connection
docker exec did-blockchain-server python -c "from database import get_db; db = next(get_db()); print('Database connection successful')"
# Result: Database connection successful

# Test data access
docker exec did-blockchain-server python -c "from database import get_db, get_all_blocks; db = next(get_db()); blocks = get_all_blocks(db); print(f'Found {len(blocks)} blocks in database')"
# Result: Found 5 blocks in database
```

## Hasil
- ✅ **Streamlit bisa koneksi** ke MariaDB
- ✅ **Environment variable** ter-set dengan benar
- ✅ **Database access** berfungsi normal
- ✅ **Data ter-load** dengan benar (5 blocks ditemukan)

## Status
✅ **FIXED** - Streamlit sekarang bisa mengakses database MariaDB dengan benar
