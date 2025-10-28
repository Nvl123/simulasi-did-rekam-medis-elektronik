#!/usr/bin/env python3
"""
Script untuk mengecek status blockchain server
"""
import requests
import json
import sys
from datetime import datetime

def check_service(url, name):
    """Check if service is running"""
    try:
        response = requests.get(url, timeout=5)
        if response.status_code == 200:
            print(f"✅ {name}: Running (Status: {response.status_code})")
            return True
        else:
            print(f"❌ {name}: Error (Status: {response.status_code})")
            return False
    except requests.exceptions.RequestException as e:
        print(f"❌ {name}: Connection failed - {str(e)}")
        return False

def check_api_endpoints():
    """Check API endpoints"""
    print("🔍 Checking API Endpoints...")
    
    # Check main API server
    api_running = check_service("http://localhost:8502/", "API Server (Port 8502)")
    
    if api_running:
        try:
            # Check VIC issuances
            response = requests.get("http://localhost:8502/api/vic-issuances", timeout=5)
            if response.status_code == 200:
                data = response.json()
                vic_count = len(data.get('vic_issuances', []))
                print(f"📊 VIC Issuances: {vic_count} records")
            else:
                print(f"❌ VIC Issuances API: Error {response.status_code}")
        except Exception as e:
            print(f"❌ VIC Issuances API: {str(e)}")
        
        try:
            # Check transactions
            response = requests.get("http://localhost:8502/api/transactions", timeout=5)
            if response.status_code == 200:
                data = response.json()
                tx_count = len(data.get('transactions', []))
                print(f"💳 Transactions: {tx_count} records")
            else:
                print(f"❌ Transactions API: Error {response.status_code}")
        except Exception as e:
            print(f"❌ Transactions API: {str(e)}")

def check_streamlit():
    """Check Streamlit interface"""
    print("\n🔍 Checking Streamlit Interface...")
    streamlit_running = check_service("http://localhost:8501/", "Streamlit UI (Port 8501)")
    
    if streamlit_running:
        try:
            # Check health endpoint
            response = requests.get("http://localhost:8501/_stcore/health", timeout=5)
            if response.status_code == 200:
                print("✅ Streamlit Health: OK")
            else:
                print(f"❌ Streamlit Health: Error {response.status_code}")
        except Exception as e:
            print(f"❌ Streamlit Health: {str(e)}")

def main():
    print("🚀 DID Blockchain Server Status Check")
    print("=" * 50)
    print(f"⏰ Timestamp: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print()
    
    # Check Streamlit
    check_streamlit()
    
    # Check API
    check_api_endpoints()
    
    print("\n" + "=" * 50)
    print("📋 Access URLs:")
    print("🌐 Streamlit UI: http://localhost:8501")
    print("🔗 API Server: http://localhost:8502")
    print("📊 VIC Issuances: http://localhost:8502/api/vic-issuances")
    print("💳 Transactions: http://localhost:8502/api/transactions")
    print("=" * 50)

if __name__ == "__main__":
    main()
