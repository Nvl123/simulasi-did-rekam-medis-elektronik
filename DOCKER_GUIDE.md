# 🐳 Docker Setup Guide

Panduan lengkap untuk menjalankan DID Blockchain Medical Records System menggunakan Docker.

## 📋 Prerequisites

- Docker Engine 20.10 atau lebih baru
- Docker Compose 2.0 atau lebih baru
- Minimal 4GB RAM
- Minimal 10GB disk space

## 🚀 Quick Start

### 1. Clone Repository
```bash
git clone https://github.com/Nvl123/simulasi-did-rekam-medis-elektronik.git
cd simulasi-did-rekam-medis-elektronik
```

### 2. Start All Services
```bash
# Menggunakan script otomatis (recommended)
chmod +x start.sh
./start.sh

# Atau manual
docker-compose up --build -d
```

### 3. Verify Services
```bash
# Cek status containers
docker-compose ps

# Cek logs
docker-compose logs -f
```

## 🏗️ Service Architecture

### Container Overview
| Service | Container | Port | Health Check |
|---------|-----------|------|--------------|
| Blockchain Server | `did-blockchain-server` | 8501 | `/health` |
| API Server | `did-api-server` | 8502 | `/api/health` |
| Flask API | `did-flask-api` | 8505 | `/health` |
| Hospital 1 | `did-hospital1` | 8503 | `/health` |
| Hospital 2 | `did-hospital2` | 8504 | `/health` |
| MariaDB | `did-mariadb` | 3306 | `mysqladmin ping` |

### Network Configuration
- **Network Name**: `did-network`
- **Driver**: `bridge`
- **Subnet**: Auto-assigned

## 🔧 Docker Commands

### Basic Operations
```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# Restart specific service
docker-compose restart blockchain-server

# View logs
docker-compose logs -f blockchain-server

# Execute command in container
docker-compose exec blockchain-server bash
```

### Development Commands
```bash
# Rebuild and start
docker-compose up --build -d

# Force rebuild (no cache)
docker-compose build --no-cache
docker-compose up -d

# View resource usage
docker stats

# Clean up
docker-compose down -v
docker system prune -f
```

## 🗄️ Database Management

### MariaDB Access
```bash
# Connect to database
docker-compose exec mariadb mysql -u root -p did_blockchain

# Backup database
docker-compose exec mariadb mysqldump -u root -p did_blockchain > backup.sql

# Restore database
docker-compose exec -T mariadb mysql -u root -p did_blockchain < backup.sql
```

### Database Credentials
- **Host**: `mariadb` (internal) / `localhost` (external)
- **Port**: `3306`
- **Database**: `did_blockchain`
- **Username**: `root`
- **Password**: `password`

## 🔍 Troubleshooting

### Common Issues

#### 1. Port Already in Use
```bash
# Check port usage
netstat -tulpn | grep :8501

# Kill process using port
sudo kill -9 <PID>
```

#### 2. Container Won't Start
```bash
# Check logs
docker-compose logs blockchain-server

# Check container status
docker-compose ps

# Restart specific service
docker-compose restart blockchain-server
```

#### 3. Database Connection Issues
```bash
# Check database logs
docker-compose logs mariadb

# Test database connection
docker-compose exec blockchain-server python -c "
import pymysql
conn = pymysql.connect(host='mariadb', user='root', password='password', database='did_blockchain')
print('Database connected successfully')
"
```

#### 4. Memory Issues
```bash
# Check memory usage
docker stats

# Increase Docker memory limit in Docker Desktop settings
# Or add memory limits in docker-compose.yml
```

### Reset Everything
```bash
# Stop and remove all containers, networks, and volumes
docker-compose down -v

# Remove all unused Docker resources
docker system prune -af

# Rebuild and start fresh
docker-compose up --build -d
```

## 📊 Monitoring

### Health Checks
All services include health checks that monitor:
- Service availability
- Database connectivity
- API responsiveness

### Log Monitoring
```bash
# View all logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f blockchain-server

# Follow logs with timestamps
docker-compose logs -f -t
```

### Performance Monitoring
```bash
# Container resource usage
docker stats

# Container details
docker inspect did-blockchain-server

# Network information
docker network inspect did-network
```

## 🔒 Security Considerations

### Production Deployment
1. Change default passwords
2. Use environment variables for secrets
3. Enable SSL/TLS
4. Configure firewall rules
5. Regular security updates

### Environment Variables
Create `.env` file:
```env
MYSQL_ROOT_PASSWORD=your_secure_password
MYSQL_DATABASE=did_blockchain
MYSQL_USER=did_user
MYSQL_PASSWORD=your_user_password
```

## 📝 Development

### Adding New Services
1. Add service definition to `docker-compose.yml`
2. Create Dockerfile if needed
3. Add to network: `did-network`
4. Configure health checks
5. Update documentation

### Custom Builds
```bash
# Build specific service
docker-compose build blockchain-server

# Build with custom tag
docker build -t my-did-app ./blockchain-server
```

## 🆘 Support

For issues related to Docker setup:
1. Check this guide first
2. Review container logs
3. Check GitHub issues
4. Create new issue with logs

---

**Happy Dockerizing! 🐳**
