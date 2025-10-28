from fastapi import FastAPI, Depends
from fastapi.middleware.cors import CORSMiddleware
import json
import hashlib
import time
from typing import Dict, Any
from sqlalchemy.orm import Session
from database import (
    create_tables, get_db, save_transaction, save_vic_issuance,
    get_all_transactions, get_all_vic_issuances, get_all_blocks,
    create_vic_share, get_vic_share_by_token, get_vic_shares_by_patient,
    revoke_vic_share, log_vic_access, update_vic_share_last_accessed,
    get_vic_access_logs
)

app = FastAPI()

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Create database tables
create_tables()

@app.get("/")
async def root():
    return {"message": "DID Blockchain API Server", "status": "running"}

@app.get("/verify/{transaction_hash}")
async def verify_vic(transaction_hash: str, db: Session = Depends(get_db)):
    """Verify VIC using transaction hash"""
    try:
        # Search for VIC issuance with this transaction hash
        vic_issuances = get_all_vic_issuances(db)
        
        for vic in vic_issuances:
            if vic.transaction_hash == transaction_hash:
                return {
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
                }
        
        return {
            "verified": False,
            "message": "VIC not found in blockchain",
            "data": None
        }
        
    except Exception as e:
        return {
            "verified": False,
            "message": f"Verification error: {str(e)}",
            "data": None
        }

@app.post("/api/issue-vic")
async def issue_vic(data: Dict[str, Any], db: Session = Depends(get_db)):
    try:
        # Generate transaction hash with timestamp to ensure uniqueness
        import time
        timestamp = time.time()
        transaction_data = json.dumps(data, sort_keys=True).encode()
        unique_data = transaction_data + str(timestamp).encode()
        transaction_hash = '0x' + hashlib.sha256(unique_data).hexdigest()[:40]
        
        # Get current blocks and create new block
        blocks = get_all_blocks(db)
        block_number = len(blocks) + 1
        
        # Create new block for this transaction
        from database import Block
        previous_hash = blocks[-1].hash if blocks else "0"
        block_hash = hashlib.sha256(f"{block_number}{timestamp}{previous_hash}".encode()).hexdigest()
        
        new_block = Block(
            index=block_number,
            timestamp=timestamp,
            previous_hash=previous_hash,
            hash=block_hash,
            nonce=0
        )
        
        # Save block to database
        db.add(new_block)
        db.commit()
        db.refresh(new_block)  # Get the ID after commit
        
        # Create transaction data
        transaction = {
            'block_id': new_block.id,
            'transaction_hash': transaction_hash,
            'from_address': None,
            'to_address': data['patient_id'],
            'amount': 1,
            'transaction_type': 'vic_issuance',
            'hospital': data['hospital'],
            'patient_id': data['patient_id'],
            'medical_data': json.dumps(data['medical_data']),
            'timestamp': timestamp
        }
        
        # Save transaction to database
        saved_transaction = save_transaction(db, transaction)
        
        # Save VIC issuance details
        vic_data = {
            'transaction_hash': transaction_hash,
            'block_number': block_number,
            'hospital': data['hospital'],
            'patient_id': data['patient_id'],
            'patient_name': data['medical_data']['patient_name'],
            'diagnosis': data['medical_data']['diagnosis'],
            'treatment': data['medical_data']['treatment'],
            'doctor': data['medical_data']['doctor'],
            'date': data['medical_data']['date'],
            'notes': data['medical_data'].get('notes', ''),
            'timestamp': timestamp
        }
        
        saved_vic = save_vic_issuance(db, vic_data)
        
        return {
            'success': True,
            'transactionHash': transaction_hash,
            'blockNumber': block_number,
            'patientId': data['patient_id']
        }
        
    except Exception as e:
        return {
            'success': False,
            'error': str(e)
        }

@app.get("/api/health")
async def health(db: Session = Depends(get_db)):
    transactions = get_all_transactions(db)
    vic_issuances = get_all_vic_issuances(db)
    blocks = get_all_blocks(db)
    
    return {
        "status": "healthy", 
        "transactions": len(transactions),
        "vic_issuances": len(vic_issuances),
        "blocks": len(blocks)
    }

@app.get("/api/transactions")
async def get_transactions(db: Session = Depends(get_db)):
    transactions = get_all_transactions(db)
    return {"transactions": [{
        "id": t.id,
        "transaction_hash": t.transaction_hash,
        "from_address": t.from_address,
        "to_address": t.to_address,
        "amount": t.amount,
        "transaction_type": t.transaction_type,
        "hospital": t.hospital,
        "patient_id": t.patient_id,
        "timestamp": t.timestamp,
        "created_at": t.created_at.isoformat()
    } for t in transactions]}

@app.get("/api/vic-issuances")
async def get_vic_issuances(db: Session = Depends(get_db)):
    vic_issuances = get_all_vic_issuances(db)
    return {"vic_issuances": [{
        "id": v.id,
        "transaction_hash": v.transaction_hash,
        "block_number": v.block_number,
        "hospital": v.hospital,
        "patient_id": v.patient_id,
        "patient_name": v.patient_name,
        "diagnosis": v.diagnosis,
        "treatment": v.treatment,
        "doctor": v.doctor,
        "date": v.date,
        "notes": v.notes,
        "timestamp": v.timestamp,
        "created_at": v.created_at.isoformat()
    } for v in vic_issuances]}

# VIC Sharing Endpoints
@app.post("/api/vic-share/create")
async def create_vic_share_endpoint(data: Dict[str, Any], db: Session = Depends(get_db)):
    """Create a new VIC share"""
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

@app.get("/api/vic-share/{share_token}")
async def get_vic_share(share_token: str, hospital: str = None, db: Session = Depends(get_db)):
    """Get VIC data using share token"""
    try:
        from datetime import datetime
        
        share = get_vic_share_by_token(db, share_token)
        
        if not share:
            return {
                'success': False,
                'error': 'Share token not found'
            }
        
        if not share.is_active:
            return {
                'success': False,
                'error': 'Share token has been revoked'
            }
        
        # Check expiration
        if share.expires_at and share.expires_at < datetime.utcnow():
            return {
                'success': False,
                'error': 'Share token has expired'
            }
        
        # Check hospital restriction
        if share.shared_with_hospital and share.shared_with_hospital != hospital:
            return {
                'success': False,
                'error': 'Access denied for this hospital'
            }
        
        # Get original VIC data
        vic_issuances = get_all_vic_issuances(db)
        original_vic = None
        for vic in vic_issuances:
            if vic.transaction_hash == share.original_transaction_hash:
                original_vic = vic
                break
        
        if not original_vic:
            return {
                'success': False,
                'error': 'Original VIC not found'
            }
        
        # Parse access permissions
        permissions = json.loads(share.access_permissions) if share.access_permissions else {}
        
        # Filter data based on permissions
        filtered_data = {
            'transaction_hash': original_vic.transaction_hash,
            'block_number': original_vic.block_number,
            'hospital': original_vic.hospital,
            'patient_id': original_vic.patient_id,
            'patient_name': original_vic.patient_name,
            'timestamp': original_vic.timestamp
        }
        
        if permissions.get('diagnosis', True):
            filtered_data['diagnosis'] = original_vic.diagnosis
        if permissions.get('treatment', True):
            filtered_data['treatment'] = original_vic.treatment
        if permissions.get('doctor', True):
            filtered_data['doctor'] = original_vic.doctor
        if permissions.get('date', True):
            filtered_data['date'] = original_vic.date
        if permissions.get('notes', False):
            filtered_data['notes'] = original_vic.notes
        
        # Log access
        log_vic_access(db, {
            'share_token': share_token,
            'accessed_by_hospital': hospital or 'unknown',
            'accessed_data': json.dumps(filtered_data),
            'ip_address': None,  # Could be added from request
            'user_agent': None   # Could be added from request
        })
        
        # Update last accessed time
        update_vic_share_last_accessed(db, share_token)
        
        return {
            'success': True,
            'data': filtered_data,
            'permissions': permissions,
            'shared_by': share.shared_by,
            'created_at': share.created_at.isoformat(),
            'expires_at': share.expires_at.isoformat() if share.expires_at else None
        }
        
    except Exception as e:
        return {
            'success': False,
            'error': str(e)
        }

@app.get("/api/vic-share/patient/{patient_id}")
async def get_patient_vic_shares(patient_id: str, db: Session = Depends(get_db)):
    """Get all VIC shares for a patient"""
    try:
        shares = get_vic_shares_by_patient(db, patient_id)
        
        return {
            'success': True,
            'shares': [{
                'id': s.id,
                'share_token': s.share_token,
                'original_transaction_hash': s.original_transaction_hash,
                'shared_by': s.shared_by,
                'shared_with_hospital': s.shared_with_hospital,
                'access_permissions': json.loads(s.access_permissions) if s.access_permissions else {},
                'expires_at': s.expires_at.isoformat() if s.expires_at else None,
                'is_active': s.is_active,
                'created_at': s.created_at.isoformat(),
                'last_accessed': s.last_accessed.isoformat() if s.last_accessed else None
            } for s in shares]
        }
        
    except Exception as e:
        return {
            'success': False,
            'error': str(e)
        }

@app.post("/api/vic-share/{share_token}/revoke")
async def revoke_vic_share_endpoint(share_token: str, db: Session = Depends(get_db)):
    """Revoke a VIC share"""
    try:
        success = revoke_vic_share(db, share_token)
        
        if success:
            return {
                'success': True,
                'message': 'VIC share revoked successfully'
            }
        else:
            return {
                'success': False,
                'error': 'Share token not found'
            }
        
    except Exception as e:
        return {
            'success': False,
            'error': str(e)
        }

@app.get("/api/vic-share/{share_token}/access-logs")
async def get_vic_access_logs(share_token: str, db: Session = Depends(get_db)):
    """Get access logs for a VIC share"""
    try:
        logs = get_vic_access_logs(db, share_token=share_token)
        
        return {
            'success': True,
            'logs': [{
                'id': l.id,
                'accessed_by_hospital': l.accessed_by_hospital,
                'accessed_data': json.loads(l.accessed_data) if l.accessed_data else None,
                'ip_address': l.ip_address,
                'user_agent': l.user_agent,
                'created_at': l.created_at.isoformat()
            } for l in logs]
        }
        
    except Exception as e:
        return {
            'success': False,
            'error': str(e)
        }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8502)
