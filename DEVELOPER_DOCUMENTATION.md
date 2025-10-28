# 👨‍💻 Developer Documentation - VIC Sharing System

## 🏗️ System Architecture

### Technology Stack:
- **Backend**: Python, FastAPI, Streamlit
- **Database**: MariaDB 10.11
- **Blockchain**: Custom blockchain implementation
- **Containerization**: Docker, Docker Compose
- **Frontend**: Streamlit (Hospital UI), Android (Mobile App)

### Project Structure:
```
/root/did-uts-new/
├── blockchain-server/
│   ├── app.py                 # Streamlit blockchain explorer
│   ├── api_server.py          # FastAPI server
│   ├── database.py            # Database models and functions
│   ├── flask_api.py           # Flask API for verification
│   └── requirements.txt       # Python dependencies
├── hospital1/
│   ├── app.py                 # Hospital A Streamlit app
│   └── assets/                # Static assets
├── hospital2/
│   ├── app.py                 # Hospital B Streamlit app
│   └── assets/                # Static assets
├── docker-compose.yml         # Docker orchestration
├── Dockerfile.streamlit       # Docker image for hospitals
└── Documentation files
```

## 🗄️ Database Design

### Entity Relationship Diagram:
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Blocks      │    │  Transactions   │    │  VICIssuances   │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ id (PK)         │    │ id (PK)         │    │ id (PK)         │
│ index           │    │ block_id (FK)   │    │ transaction_hash│
│ timestamp       │    │ transaction_hash│    │ block_number    │
│ previous_hash   │    │ from_address    │    │ hospital        │
│ hash            │    │ to_address      │    │ patient_id      │
│ nonce           │    │ amount          │    │ patient_name    │
│ created_at      │    │ transaction_type│    │ diagnosis       │
└─────────────────┘    │ hospital        │    │ treatment       │
                       │ patient_id      │    │ doctor          │
                       │ medical_data    │    │ date            │
                       │ timestamp       │    │ notes           │
                       │ created_at      │    │ timestamp       │
                       └─────────────────┘    │ created_at      │
                                              └─────────────────┘
                                                       │
                                                       │
┌─────────────────┐    ┌─────────────────┐            │
│   VICShares     │    │ VICAccessLogs   │            │
├─────────────────┤    ├─────────────────┤            │
│ id (PK)         │    │ id (PK)         │            │
│ share_token     │    │ share_token     │◄───────────┘
│ original_tx_hash│◄───┤ accessed_by_hosp│
│ patient_id      │    │ accessed_data   │
│ shared_by       │    │ ip_address      │
│ shared_with_hosp│    │ user_agent      │
│ access_perms    │    │ created_at      │
│ expires_at      │    └─────────────────┘
│ is_active       │
│ created_at      │
│ last_accessed   │
└─────────────────┘
```

### Database Models (SQLAlchemy):

#### Block Model:
```python
class Block(Base):
    __tablename__ = "blocks"
    
    id = Column(Integer, primary_key=True, index=True)
    index = Column(Integer, nullable=False)
    timestamp = Column(Float, nullable=False)
    previous_hash = Column(String(255), nullable=False)
    hash = Column(String(255), nullable=False)
    nonce = Column(Integer, default=0)
    created_at = Column(DateTime, default=datetime.utcnow)
```

#### Transaction Model:
```python
class Transaction(Base):
    __tablename__ = "transactions"
    
    id = Column(Integer, primary_key=True, index=True)
    block_id = Column(Integer, nullable=True)
    transaction_hash = Column(String(255), unique=True, nullable=False)
    from_address = Column(String(255), nullable=True)
    to_address = Column(String(255), nullable=False)
    amount = Column(Float, default=0)
    transaction_type = Column(String(50), nullable=False)
    hospital = Column(String(255), nullable=True)
    patient_id = Column(String(255), nullable=True)
    medical_data = Column(Text, nullable=True)
    timestamp = Column(Float, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)
```

#### VICIssuance Model:
```python
class VICIssuance(Base):
    __tablename__ = "vic_issuances"
    
    id = Column(Integer, primary_key=True, index=True)
    transaction_hash = Column(String(255), unique=True, nullable=False)
    block_number = Column(Integer, nullable=False)
    hospital = Column(String(255), nullable=False)
    patient_id = Column(String(255), nullable=False)
    patient_name = Column(String(255), nullable=False)
    diagnosis = Column(Text, nullable=False)
    treatment = Column(Text, nullable=False)
    doctor = Column(String(255), nullable=False)
    date = Column(String(50), nullable=False)
    notes = Column(Text, nullable=True)
    timestamp = Column(Float, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)
```

#### VICShares Model:
```python
class VICShares(Base):
    __tablename__ = "vic_shares"
    
    id = Column(Integer, primary_key=True, index=True)
    share_token = Column(String(255), unique=True, nullable=False)
    original_transaction_hash = Column(String(255), nullable=False)
    patient_id = Column(String(255), nullable=False)
    shared_by = Column(String(255), nullable=False)
    shared_with_hospital = Column(String(255), nullable=True)
    access_permissions = Column(Text, nullable=True)
    expires_at = Column(DateTime, nullable=True)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    last_accessed = Column(DateTime, nullable=True)
```

#### VICAccessLogs Model:
```python
class VICAccessLogs(Base):
    __tablename__ = "vic_access_logs"
    
    id = Column(Integer, primary_key=True, index=True)
    share_token = Column(String(255), nullable=False)
    accessed_by_hospital = Column(String(255), nullable=False)
    accessed_data = Column(Text, nullable=True)
    ip_address = Column(String(45), nullable=True)
    user_agent = Column(Text, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
```

## 🔌 API Implementation

### FastAPI Server (api_server.py):

#### Dependencies:
```python
from fastapi import FastAPI, Depends
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from database import get_db, create_vic_share, get_vic_share_by_token
```

#### CORS Configuration:
```python
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
```

#### VIC Sharing Endpoints:

##### Create VIC Share:
```python
@app.post("/api/vic-share/create")
async def create_vic_share_endpoint(data: Dict[str, Any], db: Session = Depends(get_db)):
    try:
        import secrets
        from datetime import datetime, timedelta
        
        # Generate unique share token
        share_token = 'VIC_' + secrets.token_urlsafe(32)
        
        # Parse expiration time
        expires_at = None
        if data.get('expires_in_hours'):
            expires_at = datetime.utcnow() + timedelta(hours=data['expires_in_hours'])
        
        share_data = {
            'share_token': share_token,
            'original_transaction_hash': data['transaction_hash'],
            'patient_id': data['patient_id'],
            'shared_by': data['shared_by'],
            'shared_with_hospital': data.get('shared_with_hospital'),
            'access_permissions': json.dumps(data.get('access_permissions', {
                'diagnosis': True,
                'treatment': True,
                'doctor': True,
                'date': True,
                'notes': False
            })),
            'expires_at': expires_at,
            'is_active': True
        }
        
        share = create_vic_share(db, share_data)
        
        return {
            'success': True,
            'share_token': share_token,
            'expires_at': expires_at.isoformat() if expires_at else None,
            'message': 'VIC share created successfully'
        }
        
    except Exception as e:
        return {
            'success': False,
            'error': str(e)
        }
```

##### Access VIC Share:
```python
@app.get("/api/vic-share/{share_token}")
async def get_vic_share(share_token: str, hospital: str = None, db: Session = Depends(get_db)):
    try:
        from datetime import datetime
        
        share = get_vic_share_by_token(db, share_token)
        
        if not share:
            return {'success': False, 'error': 'Share token not found'}
        
        if not share.is_active:
            return {'success': False, 'error': 'Share token has been revoked'}
        
        # Check expiration
        if share.expires_at and share.expires_at < datetime.utcnow():
            return {'success': False, 'error': 'Share token has expired'}
        
        # Check hospital restriction
        if share.shared_with_hospital and share.shared_with_hospital != hospital:
            return {'success': False, 'error': 'Access denied for this hospital'}
        
        # Get original VIC data and filter based on permissions
        # ... implementation details
        
        return {
            'success': True,
            'data': filtered_data,
            'permissions': permissions,
            'shared_by': share.shared_by,
            'created_at': share.created_at.isoformat(),
            'expires_at': share.expires_at.isoformat() if share.expires_at else None
        }
        
    except Exception as e:
        return {'success': False, 'error': str(e)}
```

## 🏥 Hospital Interface Implementation

### Streamlit App Structure:

#### Main Function:
```python
def main():
    # Header
    st.markdown(f"""
    <div class="main-header">
        <h1>🏥 Verifiable Identity Credential (VIC)</h1>
        <h3>{HOSPITAL_NAME} - Medical Records & Sharing</h3>
        <p>Issue digital medical records and manage VIC sharing using blockchain technology</p>
    </div>
    """, unsafe_allow_html=True)
    
    # Add tabs for different functionalities
    tab1, tab2 = st.tabs(["📋 Issue VIC", "🔗 VIC Sharing"])
    
    with tab1:
        issue_vic_page()
    
    with tab2:
        vic_sharing_page()
```

#### VIC Sharing Page:
```python
def vic_sharing_page():
    st.header("🔗 VIC Sharing System")
    st.markdown("Share VIC data with other hospitals or access shared VIC data")
    
    # Sub-tabs for sharing functionality
    share_tab1, share_tab2 = st.tabs(["🔍 Access Shared VIC", "📤 Create VIC Share"])
    
    with share_tab1:
        access_shared_vic()
    
    with share_tab2:
        create_vic_share()
```

#### Access Shared VIC Function:
```python
def access_shared_vic():
    st.subheader("🔍 Access Shared VIC Data")
    st.markdown("Enter a VIC share token to access patient medical records")
    
    col1, col2 = st.columns([2, 1])
    
    with col1:
        share_token = st.text_input(
            "VIC Share Token",
            placeholder="Enter VIC share token (e.g., VIC_abc123...)",
            help="This token is provided by the patient when they share their VIC",
            key="access_share_token"
        )
    
    with col2:
        hospital_name = st.text_input(
            "Hospital Name",
            value=HOSPITAL_NAME,
            disabled=True,
            key="access_hospital_name"
        )
    
    if st.button("🔍 Access VIC Data", type="primary", key="access_vic_btn"):
        if share_token:
            access_vic_data(share_token, hospital_name)
        else:
            st.error("Please enter a VIC share token")
```

## 🐳 Docker Configuration

### Docker Compose (docker-compose.yml):
```yaml
version: '3.8'

services:
  # API Server (FastAPI)
  api-server:
    build: ./blockchain-server
    container_name: did-api-server
    ports:
      - "8502:8502"
    volumes:
      - ./blockchain-server:/app
    networks:
      - did-network
    environment:
      - DATABASE_URL=mysql+pymysql://root:password@mariadb:3306/did_blockchain
    depends_on:
      - mariadb
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8502/api/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    restart: unless-stopped
    command: ["python", "api_server.py"]

  # Hospital 1 Streamlit App
  hospital1:
    build:
      context: .
      dockerfile: Dockerfile.streamlit
    container_name: did-hospital1
    ports:
      - "8503:8501"
    networks:
      - did-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8501/_stcore/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    restart: unless-stopped
    depends_on:
      - blockchain-server
    command: ["streamlit", "run", "hospital1/app.py", "--server.port=8501", "--server.address=0.0.0.0", "--server.headless=true"]

  # MariaDB Database
  mariadb:
    image: mariadb:10.11
    container_name: did-mariadb
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=password
      - MYSQL_DATABASE=did_blockchain
      - MYSQL_USER=did_user
      - MYSQL_PASSWORD=did_password
    volumes:
      - mariadb_data:/var/lib/mysql
    networks:
      - did-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 30s
      timeout: 10s
      retries: 3
    restart: unless-stopped

networks:
  did-network:
    driver: bridge

volumes:
  mariadb_data:
```

### Dockerfile for Hospitals:
```dockerfile
FROM python:3.9-slim

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    gcc \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements and install Python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application files
COPY hospital1/ ./hospital1/
COPY hospital2/ ./hospital2/
COPY run_hospitals.py .

# Create startup script
RUN echo '#!/bin/bash\necho "🚀 Starting Hospital VIC Issuance System"\necho "🏥 Hospital 1: http://localhost:8501"\necho "🏥 Hospital 2: http://localhost:8502"\necho "=" * 50\n\nstreamlit run hospital1/app.py --server.port 8501 --server.headless true --server.enableCORS false --server.enableXsrfProtection false &\n\nstreamlit run hospital2/app.py --server.port 8502 --server.headless true --server.enableCORS false --server.enableXsrfProtection false &\n\nwait' > start_hospitals.sh && chmod +x start_hospitals.sh

EXPOSE 8501 8502

CMD ["./start_hospitals.sh"]
```

## 🔧 Development Setup

### Local Development:

1. **Clone Repository**:
   ```bash
   git clone <repository-url>
   cd did-uts-new
   ```

2. **Install Dependencies**:
   ```bash
   pip install -r blockchain-server/requirements.txt
   ```

3. **Setup Database**:
   ```bash
   # Start MariaDB
   docker run -d --name mariadb \
     -e MYSQL_ROOT_PASSWORD=password \
     -e MYSQL_DATABASE=did_blockchain \
     -p 3306:3306 \
     mariadb:10.11
   ```

4. **Run API Server**:
   ```bash
   cd blockchain-server
   python api_server.py
   ```

5. **Run Hospital Apps**:
   ```bash
   # Hospital 1
   streamlit run hospital1/app.py --server.port 8503
   
   # Hospital 2
   streamlit run hospital2/app.py --server.port 8504
   ```

### Environment Variables:
```bash
# Database
DATABASE_URL=mysql+pymysql://root:password@localhost:3306/did_blockchain

# API Server
API_BASE_URL=http://localhost:8502

# Hospital Names
HOSPITAL_NAME_A=Rumah Sakit A
HOSPITAL_NAME_B=Rumah Sakit B
```

## 🧪 Testing

### Unit Tests:
```python
import pytest
from fastapi.testclient import TestClient
from api_server import app

client = TestClient(app)

def test_create_vic_share():
    response = client.post("/api/vic-share/create", json={
        "transaction_hash": "test123",
        "patient_id": "P001",
        "shared_by": "Test User",
        "expires_in_hours": 24,
        "access_permissions": {
            "diagnosis": True,
            "treatment": True,
            "doctor": True,
            "date": True,
            "notes": False
        }
    })
    assert response.status_code == 200
    assert response.json()["success"] == True
    assert "share_token" in response.json()

def test_access_vic_share():
    # First create a share
    create_response = client.post("/api/vic-share/create", json={...})
    share_token = create_response.json()["share_token"]
    
    # Then access it
    response = client.get(f"/api/vic-share/{share_token}?hospital=Test Hospital")
    assert response.status_code == 200
    assert response.json()["success"] == True
```

### Integration Tests:
```python
def test_complete_workflow():
    # 1. Issue VIC
    # 2. Create VIC Share
    # 3. Access VIC Share
    # 4. Verify permissions
    # 5. Check access logs
    pass
```

### Load Testing:
```python
import asyncio
import aiohttp

async def load_test_api():
    async with aiohttp.ClientSession() as session:
        tasks = []
        for i in range(100):
            task = session.get("http://localhost:8502/api/health")
            tasks.append(task)
        
        responses = await asyncio.gather(*tasks)
        print(f"Completed {len(responses)} requests")
```

## 📊 Monitoring and Logging

### Logging Configuration:
```python
import logging

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('app.log'),
        logging.StreamHandler()
    ]
)

logger = logging.getLogger(__name__)

# Usage in code
logger.info("VIC share created successfully")
logger.error(f"Error creating VIC share: {str(e)}")
```

### Health Checks:
```python
@app.get("/api/health")
async def health_check(db: Session = Depends(get_db)):
    try:
        # Check database connection
        db.execute("SELECT 1")
        
        # Check other services
        return {
            "status": "healthy",
            "timestamp": datetime.utcnow().isoformat(),
            "database": "connected",
            "api_server": "running"
        }
    except Exception as e:
        return {
            "status": "unhealthy",
            "error": str(e)
        }
```

### Metrics Collection:
```python
from prometheus_client import Counter, Histogram, generate_latest

# Define metrics
vic_shares_created = Counter('vic_shares_created_total', 'Total VIC shares created')
vic_shares_accessed = Counter('vic_shares_accessed_total', 'Total VIC shares accessed')
api_request_duration = Histogram('api_request_duration_seconds', 'API request duration')

# Usage
vic_shares_created.inc()
api_request_duration.observe(duration)
```

## 🔒 Security Implementation

### Input Validation:
```python
from pydantic import BaseModel, validator
from typing import Optional

class VICShareRequest(BaseModel):
    transaction_hash: str
    patient_id: str
    shared_by: str
    shared_with_hospital: Optional[str] = None
    expires_in_hours: int
    access_permissions: dict
    
    @validator('expires_in_hours')
    def validate_expires_in_hours(cls, v):
        if v < 1 or v > 8760:  # 1 hour to 1 year
            raise ValueError('expires_in_hours must be between 1 and 8760')
        return v
    
    @validator('transaction_hash')
    def validate_transaction_hash(cls, v):
        if not v.startswith('0x') or len(v) != 42:
            raise ValueError('Invalid transaction hash format')
        return v
```

### Rate Limiting:
```python
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded

limiter = Limiter(key_func=get_remote_address)
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

@app.post("/api/vic-share/create")
@limiter.limit("10/minute")
async def create_vic_share(request: Request, data: VICShareRequest, db: Session = Depends(get_db)):
    # Implementation
    pass
```

### Data Encryption:
```python
from cryptography.fernet import Fernet
import base64

class DataEncryption:
    def __init__(self, key: bytes):
        self.cipher = Fernet(key)
    
    def encrypt(self, data: str) -> str:
        encrypted_data = self.cipher.encrypt(data.encode())
        return base64.b64encode(encrypted_data).decode()
    
    def decrypt(self, encrypted_data: str) -> str:
        decoded_data = base64.b64decode(encrypted_data.encode())
        decrypted_data = self.cipher.decrypt(decoded_data)
        return decrypted_data.decode()
```

## 🚀 Deployment

### Production Docker Compose:
```yaml
version: '3.8'

services:
  api-server:
    build: ./blockchain-server
    container_name: did-api-server-prod
    ports:
      - "8502:8502"
    environment:
      - DATABASE_URL=mysql+pymysql://root:${DB_PASSWORD}@mariadb:3306/did_blockchain
      - ENVIRONMENT=production
    networks:
      - did-network
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 512M
        reservations:
          memory: 256M

  mariadb:
    image: mariadb:10.11
    container_name: did-mariadb-prod
    environment:
      - MYSQL_ROOT_PASSWORD=${DB_PASSWORD}
      - MYSQL_DATABASE=did_blockchain
    volumes:
      - mariadb_data:/var/lib/mysql
      - ./backups:/backups
    networks:
      - did-network
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 1G
        reservations:
          memory: 512M

  nginx:
    image: nginx:alpine
    container_name: did-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    depends_on:
      - api-server
      - hospital1
      - hospital2
    networks:
      - did-network
    restart: unless-stopped
```

### Nginx Configuration:
```nginx
events {
    worker_connections 1024;
}

http {
    upstream api_backend {
        server did-api-server:8502;
    }
    
    upstream hospital1_backend {
        server did-hospital1:8501;
    }
    
    upstream hospital2_backend {
        server did-hospital2:8502;
    }
    
    server {
        listen 80;
        server_name api.yourdomain.com;
        
        location / {
            proxy_pass http://api_backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }
    }
    
    server {
        listen 80;
        server_name hospital1.yourdomain.com;
        
        location / {
            proxy_pass http://hospital1_backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }
    }
}
```

## 📈 Performance Optimization

### Database Optimization:
```sql
-- Add indexes for better performance
CREATE INDEX idx_vic_shares_token ON vic_shares(share_token);
CREATE INDEX idx_vic_shares_patient ON vic_shares(patient_id);
CREATE INDEX idx_vic_shares_expires ON vic_shares(expires_at);
CREATE INDEX idx_vic_access_logs_token ON vic_access_logs(share_token);
CREATE INDEX idx_vic_access_logs_hospital ON vic_access_logs(accessed_by_hospital);
```

### Caching Strategy:
```python
from functools import lru_cache
import redis

# Redis connection
redis_client = redis.Redis(host='localhost', port=6379, db=0)

@lru_cache(maxsize=1000)
def get_vic_issuance_cached(transaction_hash: str):
    # Implementation with caching
    pass

def cache_vic_share(share_token: str, data: dict, ttl: int = 3600):
    redis_client.setex(f"vic_share:{share_token}", ttl, json.dumps(data))
```

### Connection Pooling:
```python
from sqlalchemy.pool import QueuePool

engine = create_engine(
    DATABASE_URL,
    poolclass=QueuePool,
    pool_size=10,
    max_overflow=20,
    pool_pre_ping=True,
    pool_recycle=3600
)
```

---

**Dokumentasi ini akan terus diupdate sesuai dengan perkembangan sistem. Untuk pertanyaan teknis, silakan hubungi tim development.**



