import streamlit as st
import requests
import json
import qrcode
from io import BytesIO
import base64
from datetime import datetime
import time

# Configuration
API_BASE_URL = "http://did-api-server:8502"
HOSPITAL_NAME = "Rumah Sakit B"

# Page configuration
st.set_page_config(
    page_title="VIC Issuance - Rumah Sakit B",
    page_icon="🏥",
    layout="wide",
    initial_sidebar_state="expanded"
)

# Custom CSS
st.markdown("""
<style>
    .main-header {
        text-align: center;
        color: #e74c3c;
        margin-bottom: 2rem;
    }
    .success-box {
        background-color: #d4edda;
        border: 1px solid #c3e6cb;
        border-radius: 8px;
        padding: 1rem;
        margin: 1rem 0;
    }
    .error-box {
        background-color: #f8d7da;
        border: 1px solid #f5c6cb;
        border-radius: 8px;
        padding: 1rem;
        margin: 1rem 0;
    }
    .warning-box {
        background-color: #fff3cd;
        border: 1px solid #ffeaa7;
        border-radius: 8px;
        padding: 1rem;
        margin: 1rem 0;
    }
    .info-box {
        background-color: #d1ecf1;
        border: 1px solid #bee5eb;
        border-radius: 8px;
        padding: 1rem;
        margin: 1rem 0;
    }
</style>
""", unsafe_allow_html=True)

# API Endpoints
API_ENDPOINTS = [
    "http://blockchain-server:8501/api/issue-vic",
    "http://api-server:8502/api/issue-vic"
]

def show_notification(message, type="info"):
    """Show notification with appropriate styling"""
    if type == "success":
        st.markdown(f"""
        <div class="success-box">
            <strong>✅ {message}</strong>
        </div>
        """, unsafe_allow_html=True)
    elif type == "error":
        st.markdown(f"""
        <div class="error-box">
            <strong>❌ {message}</strong>
        </div>
        """, unsafe_allow_html=True)
    elif type == "warning":
        st.markdown(f"""
        <div class="warning-box">
            <strong>⚠️ {message}</strong>
        </div>
        """, unsafe_allow_html=True)
    else:
        st.markdown(f"""
        <div class="info-box">
            <strong>ℹ️ {message}</strong>
        </div>
        """, unsafe_allow_html=True)

def generate_qr_code(data):
    """Generate QR code for the given data"""
    qr = qrcode.QRCode(
        version=1,
        error_correction=qrcode.constants.ERROR_CORRECT_L,
        box_size=10,
        border=4,
    )
    qr.add_data(data)
    qr.make(fit=True)
    
    img = qr.make_image(fill_color="black", back_color="white")
    
    # Convert PIL Image to bytes for Streamlit
    import io
    img_bytes = io.BytesIO()
    img.save(img_bytes, format='PNG')
    img_bytes.seek(0)
    
    return img_bytes

def try_api_endpoints(request_data):
    """Try API endpoints with fallback"""
    for endpoint in API_ENDPOINTS:
        try:
            response = requests.post(
                endpoint,
                json=request_data,
                headers={'Content-Type': 'application/json'},
                timeout=10
            )
            
            if response.ok:
                result = response.json()
                if result.get('success'):
                    return result, True
                else:
                    show_notification(f"API Error: {result.get('error', 'Unknown error')}", "error")
            else:
                show_notification(f"HTTP Error: {response.status_code}", "error")
                
        except requests.exceptions.RequestException as e:
            st.write(f"Endpoint {endpoint} failed: {str(e)}")
            continue
    
    return None, False

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

def issue_vic_page():
    
    # Sidebar
    with st.sidebar:
        st.header("🏥 Hospital Info")
        st.info(f"**Hospital:** {HOSPITAL_NAME}")
        st.info("**Status:** Online")
        
        st.header("📊 Quick Stats")
        if 'total_issued' not in st.session_state:
            st.session_state.total_issued = 0
        st.metric("VICs Issued", st.session_state.total_issued)
        
        st.header("🔧 API Status")
        for i, endpoint in enumerate(API_ENDPOINTS, 1):
            st.write(f"API {i}: {endpoint}")
    
    # Main form
    with st.form("vic_form"):
        st.header("📋 Patient Information")
        
        col1, col2 = st.columns(2)
        
        with col1:
            patient_id = st.text_input(
                "Patient ID *",
                placeholder="P002",
                help="Unique patient identifier"
            )
            patient_name = st.text_input(
                "Patient Name *",
                placeholder="Jane Smith",
                help="Full name of the patient"
            )
            diagnosis = st.text_input(
                "Diagnosis *",
                placeholder="Hypertension",
                help="Medical diagnosis"
            )
        
        with col2:
            treatment = st.text_input(
                "Treatment *",
                placeholder="Blood Pressure Medication",
                help="Prescribed treatment"
            )
            doctor = st.text_input(
                "Doctor *",
                placeholder="Dr. Johnson",
                help="Attending physician"
            )
            date = st.date_input(
                "Date",
                value=datetime.now().date(),
                help="Date of treatment"
            )
        
        notes = st.text_area(
            "Additional Notes",
            placeholder="Additional medical information...",
            help="Any additional medical notes"
        )
        
        submitted = st.form_submit_button(
            "🔗 Issue VIC to Blockchain",
            type="primary",
            use_container_width=True
        )
        
        if submitted:
            # Validation
            if not all([patient_id, patient_name, diagnosis, treatment, doctor]):
                show_notification("Please fill in all required fields!", "error")
                st.stop()
            
            # Show processing
            with st.spinner("Processing VIC issuance..."):
                show_notification("Processing VIC issuance...", "info")
                
                # Prepare request data
                request_data = {
                    "hospital": HOSPITAL_NAME,
                    "patient_id": patient_id,
                    "medical_data": {
                        "patient_name": patient_name,
                        "diagnosis": diagnosis,
                        "treatment": treatment,
                        "doctor": doctor,
                        "date": str(date),
                        "notes": notes
                    }
                }
                
                # Try API endpoints
                result, success = try_api_endpoints(request_data)
                
                if success:
                    show_notification("VIC berhasil diterbitkan!", "success")
                    st.session_state.total_issued += 1
                    
                    # Show result
                    st.success("✅ VIC Successfully Issued!")
                    
                    col1, col2 = st.columns(2)
                    
                    with col1:
                        st.subheader("📄 Transaction Details")
                        st.code(f"Transaction Hash: {result.get('transactionHash', 'N/A')}")
                        st.code(f"Block Number: {result.get('blockNumber', 'N/A')}")
                        st.code(f"Patient ID: {patient_id}")
                    
                    with col2:
                        st.subheader("📱 QR Code")
                        # Generate QR code
                        qr_data = json.dumps({
                            "type": "VIC",
                            "hospital": HOSPITAL_NAME,
                            "patient_id": patient_id,
                            "patient_name": patient_name,
                            "diagnosis": diagnosis,
                            "treatment": treatment,
                            "doctor": doctor,
                            "date": str(date),
                            "notes": notes,
                            "transaction_hash": result.get('transactionHash', ''),
                            "block_number": result.get('blockNumber', ''),
                            "timestamp": datetime.now().isoformat(),
                            "verification_url": f"http://31.97.108.98:8501/verify/{result.get('transactionHash', '')}"
                        })
                        
                        qr_img = generate_qr_code(qr_data)
                        st.image(qr_img, caption="Scan with e-wallet to access medical records")
                    
                    # Show success message
                    st.success("✅ VIC berhasil diterbitkan! QR code akan tetap ditampilkan.")
                    
                else:
                    # Demo mode
                    show_notification("Menggunakan mode demo - data tidak tersimpan ke blockchain", "warning")
                    
                    # Generate demo result
                    import hashlib
                    hash_input = patient_id + str(datetime.now()) + str(time.time())
                    hash_hex = hashlib.sha256(hash_input.encode()).hexdigest()
                    demo_result = {
                        "transactionHash": f"0x{hash_hex[:40]}",
                        "blockNumber": int(time.time()) % 10000
                    }
                    
                    st.warning("⚠️ Demo Mode - Data not saved to blockchain")
                    
                    col1, col2 = st.columns(2)
                    
                    with col1:
                        st.subheader("📄 Demo Transaction Details")
                        st.code(f"Transaction Hash: {demo_result['transactionHash']}")
                        st.code(f"Block Number: {demo_result['blockNumber']}")
                        st.code(f"Patient ID: {patient_id}")
                    
                    with col2:
                        st.subheader("📱 QR Code")
                        # Generate QR code for demo
                        qr_data = json.dumps({
                            "type": "VIC",
                            "hospital": HOSPITAL_NAME,
                            "patient_id": patient_id,
                            "patient_name": patient_name,
                            "diagnosis": diagnosis,
                            "treatment": treatment,
                            "doctor": doctor,
                            "date": str(date),
                            "notes": notes,
                            "transaction_hash": demo_result['transactionHash'],
                            "block_number": demo_result['blockNumber'],
                            "timestamp": datetime.now().isoformat(),
                            "demo_mode": True,
                            "verification_url": f"http://31.97.108.98:8501/verify/{demo_result['transactionHash']}"
                        })
                        
                        qr_img = generate_qr_code(qr_data)
                        st.image(qr_img, caption="Scan with e-wallet to access medical records (Demo Mode)")
                    
                    # Show demo success message
                    st.success("✅ Demo VIC berhasil dibuat! QR code akan tetap ditampilkan.")
                    
                    st.session_state.total_issued += 1
    
    # Add refresh button outside of form
    if st.session_state.get('total_issued', 0) > 0:
        st.markdown("---")
        col1, col2, col3 = st.columns([1, 1, 1])
        with col2:
            if st.button("🔄 Refresh Page", type="primary", use_container_width=True):
                st.rerun()

def vic_sharing_page():
    st.header("🔗 VIC Sharing System")
    st.markdown("Share VIC data with other hospitals or access shared VIC data")
    
    # Sub-tabs for sharing functionality
    share_tab1, share_tab2 = st.tabs(["🔍 Access Shared VIC", "📤 Create VIC Share"])
    
    with share_tab1:
        access_shared_vic()
    
    with share_tab2:
        create_vic_share()

def access_shared_vic():
    st.subheader("🔍 Access Shared VIC Data")
    st.markdown("Enter a **VIC share token** to access patient medical records")
    
    # Add warning box
    st.markdown("""
    <div class="warning-box">
        <strong>⚠️ Important:</strong> This section is for accessing VIC data that has been shared by patients. 
        You need a <strong>VIC share token</strong> (starts with "VIC_") that was provided by the patient.
        <br><br>
        If you want to <strong>load VIC data to create a share</strong>, please use the "📤 Create VIC Share" tab instead.
    </div>
    """, unsafe_allow_html=True)
    
    col1, col2 = st.columns([2, 1])
    
    with col1:
        share_token = st.text_input(
            "VIC Share Token",
            placeholder="Enter VIC share token (e.g., VIC_abc123...)",
            help="This token is provided by the patient when they share their VIC. It should start with 'VIC_'",
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
            # Validate share token format
            if not share_token.startswith('VIC_'):
                st.error("❌ Invalid share token format. Share token should start with 'VIC_'")
                st.info("💡 If you have a transaction hash (starts with '0x'), please use the '📤 Create VIC Share' tab instead.")
            else:
                access_vic_data(share_token, hospital_name)
        else:
            st.error("Please enter a VIC share token")

def access_vic_data(share_token, hospital_name):
    """Access VIC data using share token"""
    try:
        # Call API to get VIC data
        response = requests.get(
            f"{API_BASE_URL}/api/vic-share/{share_token}",
            params={"hospital": hospital_name}
        )
        
        if response.status_code == 200:
            data = response.json()
            
            if data.get('success'):
                display_shared_vic_data(data['data'], data.get('permissions', {}))
                st.success("✅ VIC data accessed successfully!")
            else:
                st.error(f"❌ Error: {data.get('error', 'Unknown error')}")
        else:
            st.error(f"❌ API Error: {response.status_code}")
            
    except Exception as e:
        st.error(f"❌ Error accessing VIC data: {str(e)}")

def display_shared_vic_data(vic_data, permissions):
    """Display shared VIC data in a formatted way"""
    st.subheader("📋 Patient Medical Records")
    
    # Basic info
    col1, col2 = st.columns(2)
    
    with col1:
        st.info(f"**Patient ID:** {vic_data.get('patient_id', 'N/A')}")
        st.info(f"**Patient Name:** {vic_data.get('patient_name', 'N/A')}")
        st.info(f"**Hospital:** {vic_data.get('hospital', 'N/A')}")
    
    with col2:
        st.info(f"**Transaction Hash:** {vic_data.get('transaction_hash', 'N/A')}")
        st.info(f"**Block Number:** {vic_data.get('block_number', 'N/A')}")
        st.info(f"**Date:** {vic_data.get('date', 'N/A')}")
    
    # Medical data based on permissions
    st.subheader("🏥 Medical Information")
    
    if permissions.get('diagnosis', True) and 'diagnosis' in vic_data:
        st.write("**Diagnosis:**")
        st.write(vic_data['diagnosis'])
    
    if permissions.get('treatment', True) and 'treatment' in vic_data:
        st.write("**Treatment:**")
        st.write(vic_data['treatment'])
    
    if permissions.get('doctor', True) and 'doctor' in vic_data:
        st.write("**Doctor:**")
        st.write(vic_data['doctor'])
    
    if permissions.get('notes', False) and 'notes' in vic_data:
        st.write("**Notes:**")
        st.write(vic_data['notes'])
    
    # Access permissions info
    st.subheader("🔐 Access Permissions")
    permission_cols = st.columns(len(permissions))
    for i, (key, value) in enumerate(permissions.items()):
        with permission_cols[i]:
            if value:
                st.success(f"✅ {key.title()}")
            else:
                st.error(f"❌ {key.title()}")

def create_vic_share():
    st.subheader("📤 Create VIC Share")
    st.markdown("Create a shareable link for VIC data")
    
    # Initialize session state for form values if not exists (before widgets are created)
    # Only initialize if not already initialized to prevent conflicts
    if not st.session_state.get('form_initialized', False):
        if 'shared_by' not in st.session_state:
            st.session_state['shared_by'] = "Patient"
        if 'expires_hours' not in st.session_state:
            st.session_state['expires_hours'] = 24
        if 'specific_hospital' not in st.session_state:
            st.session_state['specific_hospital'] = ""
        if 'perm_diagnosis' not in st.session_state:
            st.session_state['perm_diagnosis'] = True
        if 'perm_treatment' not in st.session_state:
            st.session_state['perm_treatment'] = True
        if 'perm_doctor' not in st.session_state:
            st.session_state['perm_doctor'] = True
        if 'perm_date' not in st.session_state:
            st.session_state['perm_date'] = True
        if 'perm_notes' not in st.session_state:
            st.session_state['perm_notes'] = False
        st.session_state['form_initialized'] = True
    
    # Check if VIC is already loaded
    if st.session_state.get('vic_loaded', False) and st.session_state.get('vic_data'):
        st.success("✅ VIC data loaded successfully!")
        
        # Show option to load different VIC
        if st.button("🔄 Load Different VIC", key="load_different_btn"):
            st.session_state['vic_loaded'] = False
            st.session_state['vic_data'] = None
            st.rerun()
        
        # Show the sharing form with loaded data
        show_sharing_form(st.session_state['vic_data'])
    else:
        # Add info box
        st.markdown("""
        <div class="info-box">
            <strong>ℹ️ How to use:</strong> Enter a <strong>transaction hash</strong> (starts with "0x") to load VIC data, 
            then configure sharing options to create a shareable token and QR code.
        </div>
        """, unsafe_allow_html=True)
        
        # Get VIC data first
        transaction_hash = st.text_input(
            "Transaction Hash",
            placeholder="Enter VIC transaction hash (e.g., 0x06e09d1751c742ad37bec8f474ce273d961ca5fe)",
            help="This is the transaction hash of the VIC you want to share",
            key="create_share_hash"
        )
        
        if st.button("🔍 Load VIC Data", key="load_vic_btn"):
            if transaction_hash:
                # Validate transaction hash format
                if not transaction_hash.startswith('0x'):
                    st.error("❌ Invalid transaction hash format. Transaction hash should start with '0x'")
                else:
                    load_vic_for_sharing(transaction_hash)
            else:
                st.error("Please enter a transaction hash")

def load_vic_for_sharing(transaction_hash):
    """Load VIC data for sharing"""
    try:
        response = requests.get(f"{API_BASE_URL}/verify/{transaction_hash}")
        
        if response.status_code == 200:
            data = response.json()
            
            if data.get('verified'):
                vic_data = data.get('data', {})
                # Store VIC data in session state to prevent refresh issues
                st.session_state['vic_data'] = vic_data
                st.session_state['vic_loaded'] = True
                show_sharing_form(vic_data)
            else:
                st.error(f"❌ VIC not found: {data.get('message', 'Unknown error')}")
        else:
            st.error(f"❌ API Error: {response.status_code}")
            
    except Exception as e:
        st.error(f"❌ Error loading VIC: {str(e)}")

def show_sharing_form(vic_data):
    """Show form to create VIC share"""
    st.subheader("📋 VIC Information")
    
    col1, col2 = st.columns(2)
    
    with col1:
        st.write(f"**Patient ID:** {vic_data.get('patient_id', 'N/A')}")
        st.write(f"**Patient Name:** {vic_data.get('patient_name', 'N/A')}")
        st.write(f"**Hospital:** {vic_data.get('hospital', 'N/A')}")
    
    with col2:
        st.write(f"**Diagnosis:** {vic_data.get('diagnosis', 'N/A')}")
        st.write(f"**Doctor:** {vic_data.get('doctor', 'N/A')}")
        st.write(f"**Date:** {vic_data.get('date', 'N/A')}")
    
    st.markdown("---")
    st.subheader("🔧 Sharing Configuration")
    
    # Sharing options (session state already initialized in create_vic_share)
    col1, col2 = st.columns(2)
    
    with col1:
        shared_by = st.text_input("Shared By", value=st.session_state.get('shared_by', 'Patient'), key="shared_by")
        expires_in_hours = st.number_input("Expires in (hours)", min_value=1, max_value=8760, value=st.session_state.get('expires_hours', 24), key="expires_hours")
        shared_with_hospital = st.text_input("Specific Hospital (optional)", value=st.session_state.get('specific_hospital', ''), placeholder="Leave empty for any hospital", key="specific_hospital")
    
    with col2:
        st.write("**Access Permissions:**")
        diagnosis_perm = st.checkbox("Diagnosis", value=st.session_state.get('perm_diagnosis', True), key="perm_diagnosis")
        treatment_perm = st.checkbox("Treatment", value=st.session_state.get('perm_treatment', True), key="perm_treatment")
        doctor_perm = st.checkbox("Doctor", value=st.session_state.get('perm_doctor', True), key="perm_doctor")
        date_perm = st.checkbox("Date", value=st.session_state.get('perm_date', True), key="perm_date")
        notes_perm = st.checkbox("Notes", value=st.session_state.get('perm_notes', False), key="perm_notes")
    
    if st.button("📤 Create VIC Share", type="primary", key="create_share_btn"):
        create_share_request(vic_data, {
            'shared_by': shared_by,
            'expires_in_hours': expires_in_hours,
            'shared_with_hospital': shared_with_hospital if shared_with_hospital else None,
            'access_permissions': {
                'diagnosis': diagnosis_perm,
                'treatment': treatment_perm,
                'doctor': doctor_perm,
                'date': date_perm,
                'notes': notes_perm
            }
        })

def create_share_request(vic_data, share_options):
    """Create VIC share request"""
    try:
        share_data = {
            'transaction_hash': vic_data['transaction_hash'],
            'patient_id': vic_data['patient_id'],
            'shared_by': share_options['shared_by'],
            'shared_with_hospital': share_options['shared_with_hospital'],
            'expires_in_hours': share_options['expires_in_hours'],
            'access_permissions': share_options['access_permissions']
        }
        
        response = requests.post(
            f"{API_BASE_URL}/api/vic-share/create",
            json=share_data
        )
        
        if response.status_code == 200:
            data = response.json()
            
            if data.get('success'):
                share_token = data.get('share_token')
                expires_at = data.get('expires_at')
                
                st.success("✅ VIC share created successfully!")
                
                # Display share information
                st.subheader("📱 Share Information")
                
                col1, col2 = st.columns(2)
                
                with col1:
                    st.write(f"**Share Token:** `{share_token}`")
                    st.write(f"**Expires:** {expires_at}")
                
                with col2:
                    # Generate QR code for sharing
                    qr_data = {
                        "type": "VIC_SHARE",
                        "share_token": share_token,
                        "hospital": HOSPITAL_NAME,
                        "expires_at": expires_at
                    }
                    
                    qr_img = generate_qr_code(json.dumps(qr_data))
                    st.image(qr_img, caption="Share this QR code with hospitals")
                
                # Copy to clipboard button
                st.markdown(f"""
                <div style="margin: 1rem 0;">
                    <strong>Share Token:</strong>
                    <div style="background-color: #f0f2f6; padding: 0.5rem; border-radius: 4px; font-family: monospace; margin: 0.5rem 0;">
                        {share_token}
                    </div>
                </div>
                """, unsafe_allow_html=True)
                
                # Copy button with JavaScript
                st.markdown(f"""
                <button onclick="navigator.clipboard.writeText('{share_token}').then(() => alert('Share token copied to clipboard!'))" 
                        style="background-color: #ff4b4b; color: white; border: none; padding: 0.5rem 1rem; border-radius: 4px; cursor: pointer; margin: 0.5rem 0;">
                    📋 Copy Share Token
                </button>
                """, unsafe_allow_html=True)
                
                st.info("💡 Click the button above to copy the share token to your clipboard!")
                
            else:
                st.error(f"❌ Error: {data.get('error', 'Unknown error')}")
        else:
            st.error(f"❌ API Error: {response.status_code}")
            
    except Exception as e:
        st.error(f"❌ Error creating share: {str(e)}")

if __name__ == "__main__":
    main()
