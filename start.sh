#!/bin/bash

echo "🚀 Starting DID Blockchain Medical Records System..."
echo "=================================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Stop any existing containers
echo "🧹 Cleaning up existing containers..."
docker-compose down 2>/dev/null

# Build and start services
echo "🔨 Building and starting services..."
docker-compose up --build -d

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 15

# Check service status
echo "🔍 Checking service status..."
echo ""

# Check blockchain server
if curl -s http://localhost:8501/_stcore/health > /dev/null 2>&1; then
    echo "✅ Blockchain Server: http://localhost:8501"
else
    echo "❌ Blockchain Server: Not ready"
fi

# Check hospital 1
if curl -s http://localhost:8081 > /dev/null 2>&1; then
    echo "✅ Hospital 1: http://localhost:8081"
else
    echo "❌ Hospital 1: Not ready"
fi

# Check hospital 2
if curl -s http://localhost:8082 > /dev/null 2>&1; then
    echo "✅ Hospital 2: http://localhost:8082"
else
    echo "❌ Hospital 2: Not ready"
fi

echo ""
echo "🎉 System is ready!"
echo ""
echo "📋 Access URLs:"
echo "   🔗 Blockchain Explorer: http://localhost:8501"
echo "   🏥 Hospital 1: http://localhost:8081"
echo "   🏥 Hospital 2: http://localhost:8082"
echo ""
echo "📱 VIC Issuance Pages:"
echo "   🏥 Hospital 1 VIC: http://localhost:8081/vic-issuance.html"
echo "   🏥 Hospital 2 VIC: http://localhost:8082/vic-issuance.html"
echo ""
echo "🛑 To stop the system: docker-compose down"
