#!/bin/bash

echo "🛑 Stopping DID Blockchain Medical Records System..."
echo "=================================================="

# Stop all containers
docker-compose down

echo "✅ All services stopped successfully!"
echo ""
echo "💡 To start again, run: ./start.sh"
