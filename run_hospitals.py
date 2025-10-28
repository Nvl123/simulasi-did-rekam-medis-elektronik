#!/usr/bin/env python3
"""
Script to run both hospital Streamlit applications
"""
import subprocess
import sys
import os
import time
import webbrowser
from threading import Thread

def run_streamlit_app(port, app_path, hospital_name):
    """Run a Streamlit app on specified port"""
    try:
        print(f"🏥 Starting {hospital_name} on port {port}...")
        subprocess.run([
            sys.executable, "-m", "streamlit", "run", 
            app_path, 
            "--server.port", str(port),
            "--server.headless", "true",
            "--server.enableCORS", "false",
            "--server.enableXsrfProtection", "false"
        ])
    except KeyboardInterrupt:
        print(f"🛑 {hospital_name} stopped")
    except Exception as e:
        print(f"❌ Error running {hospital_name}: {e}")

def main():
    print("🚀 Starting Hospital VIC Issuance System")
    print("=" * 50)
    
    # Check if requirements are installed
    try:
        import streamlit
        import requests
        import qrcode
        print("✅ All dependencies are installed")
    except ImportError as e:
        print(f"❌ Missing dependency: {e}")
        print("Please run: pip install -r requirements.txt")
        return
    
    # Get current directory
    current_dir = os.path.dirname(os.path.abspath(__file__))
    hospital1_path = os.path.join(current_dir, "hospital1", "app.py")
    hospital2_path = os.path.join(current_dir, "hospital2", "app.py")
    
    # Check if app files exist
    if not os.path.exists(hospital1_path):
        print(f"❌ Hospital 1 app not found: {hospital1_path}")
        return
    
    if not os.path.exists(hospital2_path):
        print(f"❌ Hospital 2 app not found: {hospital2_path}")
        return
    
    print("🏥 Hospital 1: http://localhost:8501")
    print("🏥 Hospital 2: http://localhost:8502")
    print("=" * 50)
    print("Press Ctrl+C to stop all applications")
    
    # Start both applications in separate threads
    hospital1_thread = Thread(
        target=run_streamlit_app, 
        args=(8501, hospital1_path, "Rumah Sakit A")
    )
    hospital2_thread = Thread(
        target=run_streamlit_app, 
        args=(8502, hospital2_path, "Rumah Sakit B")
    )
    
    try:
        # Start both threads
        hospital1_thread.start()
        time.sleep(2)  # Small delay between starts
        hospital2_thread.start()
        
        # Open browsers
        time.sleep(3)
        print("🌐 Opening browsers...")
        webbrowser.open("http://localhost:8501")
        webbrowser.open("http://localhost:8502")
        
        # Wait for threads to complete
        hospital1_thread.join()
        hospital2_thread.join()
        
    except KeyboardInterrupt:
        print("\n🛑 Shutting down applications...")
        hospital1_thread.join(timeout=5)
        hospital2_thread.join(timeout=5)
        print("✅ All applications stopped")

if __name__ == "__main__":
    main()
