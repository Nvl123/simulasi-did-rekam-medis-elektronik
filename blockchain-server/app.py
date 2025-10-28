import streamlit as st
import json
import hashlib
import time
from datetime import datetime
import requests
from typing import List, Dict, Any
import pandas as pd
import streamlit.components.v1 as components
from streamlit.web.server import Server
import threading
import http.server
import socketserver
from urllib.parse import urlparse, parse_qs
from sqlalchemy.orm import Session
from database import get_db, get_all_transactions, get_all_vic_issuances, get_all_blocks
from flask import Flask, jsonify

# Konfigurasi halaman
st.set_page_config(
    page_title="DID Blockchain Explorer",
    page_icon="🔗",
    layout="wide",
    initial_sidebar_state="expanded"
)

# Inisialisasi session state untuk blockchain
if 'blockchain' not in st.session_state:
    st.session_state.blockchain = []
    st.session_state.pending_transactions = []

# Flask app untuk API endpoints
flask_app = Flask(__name__)

@flask_app.route('/verify/<transaction_hash>')
def verify_vic(transaction_hash):
    """Verify VIC using transaction hash"""
    try:
        # Get database session
        db = next(get_db())
        
        # Search for VIC issuance with this transaction hash
        vic_issuances = get_all_vic_issuances(db)
        
        for vic in vic_issuances:
            if vic.transaction_hash == transaction_hash:
                return jsonify({
                    "verified": True,
                    "message": "VIC verified successfully",
                    "data": {
                        "transaction_hash": vic.transaction_hash,
                        "block_number": vic.block_number,
                        "hospital": vic.hospital,
                        "patient_id": vic.patient_id,
                        "patient_name": vic.patient_name,
                        "diagnosis": vic.diagnosis,
                        "treatment": vic.treatment,
                        "doctor": vic.doctor,
                        "date": vic.date,
                        "notes": vic.notes,
                        "timestamp": vic.timestamp
                    }
                })
        
        return jsonify({
            "verified": False,
            "message": "VIC not found in blockchain",
            "data": None
        })
        
    except Exception as e:
        return jsonify({
            "verified": False,
            "message": f"Verification error: {str(e)}",
            "data": None
        })

@flask_app.route('/health')
def health_check():
    """Health check endpoint"""
    try:
        db = next(get_db())
        vic_count = len(get_all_vic_issuances(db))
        transaction_count = len(get_all_transactions(db))
        block_count = len(get_all_blocks(db))
        
        return jsonify({
            "status": "healthy",
            "database": "connected",
            "timestamp": datetime.now().isoformat(),
            "statistics": {
                "total_vic_issuances": vic_count,
                "total_transactions": transaction_count,
                "total_blocks": block_count
            }
        })
    except Exception as e:
        return jsonify({
            "status": "unhealthy",
            "database": "disconnected",
            "error": str(e)
        })

# Start Flask server in background thread
def start_flask_server():
    flask_app.run(host='0.0.0.0', port=8501, debug=False, use_reloader=False)

# Start Flask server
flask_thread = threading.Thread(target=start_flask_server, daemon=True)
flask_thread.start()

# blockchain_instance will be initialized after Blockchain class is defined

# Kelas untuk Block
class Block:
    def __init__(self, index: int, transactions: List[Dict], previous_hash: str, timestamp: float = None):
        self.index = index
        self.transactions = transactions
        self.timestamp = timestamp or time.time()
        self.previous_hash = previous_hash
        self.nonce = 0
        self.hash = self.calculate_hash()
    
    def calculate_hash(self):
        block_string = f"{self.index}{self.transactions}{self.timestamp}{self.previous_hash}{self.nonce}"
        return hashlib.sha256(block_string.encode()).hexdigest()
    
    def mine_block(self, difficulty: int = 2):
        target = "0" * difficulty
        while self.hash[:difficulty] != target:
            self.nonce += 1
            self.hash = self.calculate_hash()
    
    def to_dict(self):
        return {
            'index': self.index,
            'transactions': self.transactions,
            'timestamp': self.timestamp,
            'previous_hash': self.previous_hash,
            'hash': self.hash,
            'nonce': self.nonce
        }

# Kelas untuk Blockchain
class Blockchain:
    def __init__(self):
        self.chain = []
        self.difficulty = 2
        self.pending_transactions = []
        self.create_genesis_block()
    
    def create_genesis_block(self):
        genesis_block = Block(0, [], "0")
        genesis_block.mine_block(self.difficulty)
        self.chain.append(genesis_block)
    
    def add_transaction(self, transaction: Dict):
        self.pending_transactions.append(transaction)
    
    def mine_pending_transactions(self, mining_reward_address: str = "system"):
        if not self.pending_transactions:
            return False
        
        # Tambahkan reward untuk miner
        reward_transaction = {
            'from': None,
            'to': mining_reward_address,
            'amount': 1,
            'type': 'mining_reward',
            'timestamp': time.time()
        }
        
        block = Block(
            len(self.chain),
            self.pending_transactions + [reward_transaction],
            self.chain[-1].hash
        )
        
        block.mine_block(self.difficulty)
        self.chain.append(block)
        self.pending_transactions = []
        return True
    
    def get_balance(self, address: str):
        balance = 0
        for block in self.chain:
            for transaction in block.transactions:
                if transaction.get('from') == address:
                    balance -= transaction.get('amount', 0)
                if transaction.get('to') == address:
                    balance += transaction.get('amount', 0)
        return balance
    
    def get_chain(self):
        return [block.to_dict() for block in self.chain]

# Ensure blockchain_instance is always initialized
if 'blockchain_instance' not in st.session_state:
    st.session_state.blockchain_instance = Blockchain()

# Fungsi untuk mendapatkan data dari database
def get_database_data():
    try:
        db = next(get_db())
        transactions = get_all_transactions(db)
        vic_issuances = get_all_vic_issuances(db)
        blocks = get_all_blocks(db)
        return transactions, vic_issuances, blocks
    except Exception as e:
        st.error(f"Database error: {e}")
        return [], [], []

# Fungsi untuk menambahkan transaksi VIC (deprecated - now using API)
def add_vic_transaction(hospital_name: str, patient_id: str, medical_data: Dict):
    transaction = {
        'from': None,
        'to': patient_id,
        'amount': 1,  # 1 VIC token
        'type': 'vic_issuance',
        'hospital': hospital_name,
        'patient_id': patient_id,
        'medical_data': medical_data,
        'timestamp': time.time()
    }
    
    st.session_state.blockchain_instance.add_transaction(transaction)
    st.session_state.pending_transactions.append(transaction)
    return transaction

# UI Streamlit
st.title("🔗 DID Blockchain Explorer")
st.markdown("**Decentralized Identity untuk Rekam Medis Elektronik**")

# Sidebar untuk kontrol
with st.sidebar:
    st.header("🎛️ Blockchain Controls")
    
    if st.button("⛏️ Mine Pending Transactions", type="primary"):
        if st.session_state.blockchain_instance.mine_pending_transactions():
            st.success("Block berhasil ditambang!")
            st.rerun()
        else:
            st.warning("Tidak ada transaksi pending untuk di-mine")
    
    st.header("📊 Blockchain Stats")
    # Get data from database
    transactions, vic_issuances, blocks = get_database_data()
    st.metric("Total Blocks", len(blocks))
    st.metric("Pending Transactions", len(st.session_state.blockchain_instance.pending_transactions))
    
    # Form untuk menambahkan transaksi VIC
    st.header("🏥 Issue VIC")
    with st.form("vic_form"):
        hospital_name = st.selectbox("Hospital", ["Rumah Sakit A", "Rumah Sakit B"])
        patient_id = st.text_input("Patient ID", placeholder="P001")
        diagnosis = st.text_input("Diagnosis", placeholder="Common Cold")
        treatment = st.text_input("Treatment", placeholder="Rest and Medication")
        doctor = st.text_input("Doctor", placeholder="Dr. Smith")
        
        submitted = st.form_submit_button("Issue VIC", type="primary")
        
        if submitted and patient_id:
            medical_data = {
                'diagnosis': diagnosis,
                'treatment': treatment,
                'doctor': doctor,
                'date': datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            }
            
            transaction = add_vic_transaction(hospital_name, patient_id, medical_data)
            st.success(f"VIC issued for patient {patient_id}")
            st.rerun()

# Tabs untuk berbagai view
tab1, tab2, tab3, tab4, tab5 = st.tabs(["📋 Blockchain", "⏳ Pending Transactions", "👤 Patient Records", "📈 Analytics", "🗄️ Database Records"])

with tab1:
    st.header("Blockchain Chain")
    
    # Get data from database
    transactions, vic_issuances, blocks = get_database_data()
    
    if blocks:
        for block in blocks:
            with st.expander(f"Block {block.index} - {block.created_at.strftime('%Y-%m-%d %H:%M:%S')}"):
                col1, col2 = st.columns(2)
                
                with col1:
                    st.write(f"**Hash:** `{block.hash[:20]}...`")
                    st.write(f"**Previous Hash:** `{block.previous_hash[:20]}...`")
                    st.write(f"**Nonce:** {block.nonce}")
                
                with col2:
                    st.write(f"**Timestamp:** {block.created_at.strftime('%Y-%m-%d %H:%M:%S')}")
                    # Count transactions for this block
                    block_transactions = [t for t in transactions if hasattr(t, 'block_id') and t.block_id == block.id]
                    st.write(f"**Transactions:** {len(block_transactions)}")
                
                if block_transactions:
                    st.write("**Transactions:**")
                    for j, tx in enumerate(block_transactions):
                        with st.expander(f"Transaction {j+1}"):
                            st.write(f"**Hash:** `{tx.transaction_hash}`")
                            st.write(f"**Type:** {tx.transaction_type}")
                            st.write(f"**Hospital:** {tx.hospital}")
                            st.write(f"**Patient ID:** {tx.patient_id}")
                            if tx.medical_data:
                                st.write("**Medical Data:**")
                                st.json(json.loads(tx.medical_data))
    else:
        st.info("Blockchain kosong. Mine transaksi pertama untuk memulai!")

with tab2:
    st.header("Pending Transactions")
    
    if st.session_state.blockchain_instance.pending_transactions:
        for i, tx in enumerate(st.session_state.blockchain_instance.pending_transactions):
            with st.expander(f"Transaction {i+1} - {tx.get('type', 'unknown')}"):
                st.json(tx)
    else:
        st.info("Tidak ada transaksi pending")

with tab3:
    st.header("Patient Records")
    
    # Cari semua transaksi VIC untuk pasien tertentu
    patient_id = st.text_input("Enter Patient ID to search", placeholder="P001")
    
    if patient_id:
        patient_transactions = []
        for block in st.session_state.blockchain_instance.chain:
            for tx in block.transactions:
                if tx.get('patient_id') == patient_id and tx.get('type') == 'vic_issuance':
                    patient_transactions.append(tx)
        
        if patient_transactions:
            st.success(f"Found {len(patient_transactions)} VIC records for patient {patient_id}")
            for i, tx in enumerate(patient_transactions):
                with st.expander(f"VIC Record {i+1} from {tx.get('hospital')}"):
                    st.json(tx)
        else:
            st.warning(f"No VIC records found for patient {patient_id}")

with tab4:
    st.header("Blockchain Analytics")
    
    # Statistik blockchain
    col1, col2, col3, col4 = st.columns(4)
    
    # Get data from database
    transactions, vic_issuances, blocks = get_database_data()
    
    with col1:
        st.metric("Total Blocks", len(blocks))
    
    with col2:
        st.metric("Pending Transactions", len(st.session_state.blockchain_instance.pending_transactions))
    
    with col3:
        st.metric("Total Transactions", len(transactions))
    
    with col4:
        st.metric("VIC Transactions", len(vic_issuances))
    
    # Grafik transaksi per block
    if blocks:
        block_data = []
        for block in blocks:
            block_transactions = [t for t in transactions if hasattr(t, 'block_id') and t.block_id == block.id]
            block_data.append({
                'Block': block.index,
                'Transactions': len(block_transactions),
                'Timestamp': block.created_at.strftime('%H:%M:%S')
            })
        
        df = pd.DataFrame(block_data)
        st.line_chart(df.set_index('Block')['Transactions'])

with tab5:
    st.header("🗄️ Database Records")
    
    # Get data from database
    transactions, vic_issuances, blocks = get_database_data()
    
    col1, col2, col3 = st.columns(3)
    with col1:
        st.metric("Total Transactions", len(transactions))
    with col2:
        st.metric("VIC Issuances", len(vic_issuances))
    with col3:
        st.metric("Blocks", len(blocks))
    
    # Display VIC Issuances
    st.subheader("🏥 VIC Issuances")
    if vic_issuances:
        for vic in vic_issuances:
            with st.expander(f"VIC {vic.id} - {vic.patient_name} ({vic.hospital})"):
                col1, col2 = st.columns(2)
                with col1:
                    st.write(f"**Transaction Hash:** `{vic.transaction_hash}`")
                    st.write(f"**Block Number:** {vic.block_number}")
                    st.write(f"**Hospital:** {vic.hospital}")
                    st.write(f"**Patient ID:** {vic.patient_id}")
                with col2:
                    st.write(f"**Patient Name:** {vic.patient_name}")
                    st.write(f"**Diagnosis:** {vic.diagnosis}")
                    st.write(f"**Treatment:** {vic.treatment}")
                    st.write(f"**Doctor:** {vic.doctor}")
                    st.write(f"**Date:** {vic.date}")
                if vic.notes:
                    st.write(f"**Notes:** {vic.notes}")
                st.write(f"**Created:** {vic.created_at.strftime('%Y-%m-%d %H:%M:%S')}")
    else:
        st.info("No VIC issuances found in database")
    
    # Display Transactions
    st.subheader("💳 Transactions")
    if transactions:
        for tx in transactions:
            with st.expander(f"Transaction {tx.id} - {tx.transaction_type}"):
                col1, col2 = st.columns(2)
                with col1:
                    st.write(f"**Hash:** `{tx.transaction_hash}`")
                    st.write(f"**From:** {tx.from_address or 'N/A'}")
                    st.write(f"**To:** {tx.to_address}")
                    st.write(f"**Amount:** {tx.amount}")
                with col2:
                    st.write(f"**Type:** {tx.transaction_type}")
                    st.write(f"**Hospital:** {tx.hospital or 'N/A'}")
                    st.write(f"**Patient ID:** {tx.patient_id or 'N/A'}")
                    st.write(f"**Created:** {tx.created_at.strftime('%Y-%m-%d %H:%M:%S')}")
    else:
        st.info("No transactions found in database")

# Footer
st.markdown("---")
st.markdown("**DID Blockchain Explorer** - Demonstrasi Decentralized Identity untuk Rekam Medis Elektronik")

# API server is now running separately on port 8502
