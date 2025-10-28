#!/bin/bash

# Script untuk memulai blockchain server services
echo "🚀 Starting DID Blockchain Server Services..."

# Kill existing processes on ports 8501 and 8502
echo "🔄 Stopping existing services..."
pkill -f "streamlit.*8501" 2>/dev/null || true
pkill -f "python.*api_server" 2>/dev/null || true
pkill -f "python.*flask_api" 2>/dev/null || true

# Wait a moment for processes to stop
sleep 2

# Check if ports are free
if lsof -Pi :8501 -sTCP:LISTEN -t >/dev/null ; then
    echo "❌ Port 8501 is still in use"
    exit 1
fi

if lsof -Pi :8502 -sTCP:LISTEN -t >/dev/null ; then
    echo "❌ Port 8502 is still in use"
    exit 1
fi

# Start API server in background
echo "🔗 Starting API Server on port 8502..."
cd /root/did-uts-new/blockchain-server
nohup python3 api_server.py > api_server.log 2>&1 &
API_PID=$!

# Wait for API server to start
sleep 3

# Start Streamlit in background
echo "🌐 Starting Streamlit UI on port 8501..."
nohup streamlit run app.py --server.port=8501 --server.address=0.0.0.0 --server.headless=true > streamlit.log 2>&1 &
STREAMLIT_PID=$!

# Wait for services to start
sleep 5

# Check if services are running
echo "🔍 Checking service status..."

# Check API server
if curl -s http://localhost:8502/ > /dev/null; then
    echo "✅ API Server: Running"
else
    echo "❌ API Server: Failed to start"
fi

# Check Streamlit
if curl -s http://localhost:8501/ > /dev/null; then
    echo "✅ Streamlit UI: Running"
else
    echo "❌ Streamlit UI: Failed to start"
fi

echo ""
echo "📋 Service Information:"
echo "🔗 API Server: http://localhost:8502"
echo "🌐 Streamlit UI: http://localhost:8501"
echo "📊 VIC Issuances: http://localhost:8502/api/vic-issuances"
echo "💳 Transactions: http://localhost:8502/api/transactions"
echo ""
echo "📝 Logs:"
echo "   API Server: api_server.log"
echo "   Streamlit: streamlit.log"
echo ""
echo "🛑 To stop services:"
echo "   kill $API_PID $STREAMLIT_PID"
echo ""
echo "✅ Services started successfully!"
