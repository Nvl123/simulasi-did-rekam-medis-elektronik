from sqlalchemy import create_engine, Column, Integer, String, Text, DateTime, Float, Boolean
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from datetime import datetime
import os

# Database configuration
DATABASE_URL = os.getenv("DATABASE_URL", "mysql+pymysql://root:password@localhost:3306/did_blockchain")

# Create engine
engine = create_engine(DATABASE_URL, echo=True)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# Database Models
class Block(Base):
    __tablename__ = "blocks"
    
    id = Column(Integer, primary_key=True, index=True)
    index = Column(Integer, nullable=False)
    timestamp = Column(Float, nullable=False)
    previous_hash = Column(String(255), nullable=False)
    hash = Column(String(255), nullable=False)
    nonce = Column(Integer, default=0)
    created_at = Column(DateTime, default=datetime.utcnow)

class Transaction(Base):
    __tablename__ = "transactions"
    
    id = Column(Integer, primary_key=True, index=True)
    block_id = Column(Integer, nullable=True)  # NULL for pending transactions
    transaction_hash = Column(String(255), unique=True, nullable=False)
    from_address = Column(String(255), nullable=True)
    to_address = Column(String(255), nullable=False)
    amount = Column(Float, default=0)
    transaction_type = Column(String(50), nullable=False)
    hospital = Column(String(255), nullable=True)
    patient_id = Column(String(255), nullable=True)
    medical_data = Column(Text, nullable=True)  # JSON string
    timestamp = Column(Float, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)

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

class VICShares(Base):
    __tablename__ = "vic_shares"
    
    id = Column(Integer, primary_key=True, index=True)
    share_token = Column(String(255), unique=True, nullable=False)
    original_transaction_hash = Column(String(255), nullable=False)
    patient_id = Column(String(255), nullable=False)
    shared_by = Column(String(255), nullable=False)  # User who shared the VIC
    shared_with_hospital = Column(String(255), nullable=True)  # Specific hospital or null for any
    access_permissions = Column(Text, nullable=True)  # JSON string of what data can be accessed
    expires_at = Column(DateTime, nullable=True)  # When the share expires
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    last_accessed = Column(DateTime, nullable=True)

class VICAccessLogs(Base):
    __tablename__ = "vic_access_logs"
    
    id = Column(Integer, primary_key=True, index=True)
    share_token = Column(String(255), nullable=False)
    accessed_by_hospital = Column(String(255), nullable=False)
    accessed_data = Column(Text, nullable=True)  # JSON string of what was accessed
    ip_address = Column(String(45), nullable=True)
    user_agent = Column(Text, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)

# Create tables
def create_tables():
    Base.metadata.create_all(bind=engine)

# Database functions
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

def save_transaction(db, transaction_data):
    """Save transaction to database"""
    transaction = Transaction(
        transaction_hash=transaction_data['transaction_hash'],
        from_address=transaction_data.get('from_address'),
        to_address=transaction_data['to_address'],
        amount=transaction_data.get('amount', 0),
        transaction_type=transaction_data['transaction_type'],
        hospital=transaction_data.get('hospital'),
        patient_id=transaction_data.get('patient_id'),
        medical_data=transaction_data.get('medical_data'),
        timestamp=transaction_data['timestamp']
    )
    db.add(transaction)
    db.commit()
    db.refresh(transaction)
    return transaction

def save_vic_issuance(db, vic_data):
    """Save VIC issuance to database"""
    vic = VICIssuance(
        transaction_hash=vic_data['transaction_hash'],
        block_number=vic_data['block_number'],
        hospital=vic_data['hospital'],
        patient_id=vic_data['patient_id'],
        patient_name=vic_data['patient_name'],
        diagnosis=vic_data['diagnosis'],
        treatment=vic_data['treatment'],
        doctor=vic_data['doctor'],
        date=vic_data['date'],
        notes=vic_data.get('notes', ''),
        timestamp=vic_data['timestamp']
    )
    db.add(vic)
    db.commit()
    db.refresh(vic)
    return vic

def get_all_transactions(db):
    """Get all transactions from database"""
    return db.query(Transaction).order_by(Transaction.created_at.desc()).all()

def get_all_vic_issuances(db):
    """Get all VIC issuances from database"""
    return db.query(VICIssuance).order_by(VICIssuance.created_at.desc()).all()

def get_all_blocks(db):
    """Get all blocks from database"""
    return db.query(Block).order_by(Block.index.asc()).all()

# VIC Sharing Functions
def create_vic_share(db, share_data):
    """Create a new VIC share"""
    share = VICShares(
        share_token=share_data['share_token'],
        original_transaction_hash=share_data['original_transaction_hash'],
        patient_id=share_data['patient_id'],
        shared_by=share_data['shared_by'],
        shared_with_hospital=share_data.get('shared_with_hospital'),
        access_permissions=share_data.get('access_permissions'),
        expires_at=share_data.get('expires_at'),
        is_active=share_data.get('is_active', True)
    )
    db.add(share)
    db.commit()
    db.refresh(share)
    return share

def get_vic_share_by_token(db, share_token):
    """Get VIC share by token"""
    return db.query(VICShares).filter(VICShares.share_token == share_token).first()

def get_vic_shares_by_patient(db, patient_id):
    """Get all VIC shares for a patient"""
    return db.query(VICShares).filter(VICShares.patient_id == patient_id).order_by(VICShares.created_at.desc()).all()

def revoke_vic_share(db, share_token):
    """Revoke a VIC share"""
    share = db.query(VICShares).filter(VICShares.share_token == share_token).first()
    if share:
        share.is_active = False
        db.commit()
        return True
    return False

def log_vic_access(db, access_data):
    """Log VIC access"""
    access_log = VICAccessLogs(
        share_token=access_data['share_token'],
        accessed_by_hospital=access_data['accessed_by_hospital'],
        accessed_data=access_data.get('accessed_data'),
        ip_address=access_data.get('ip_address'),
        user_agent=access_data.get('user_agent')
    )
    db.add(access_log)
    db.commit()
    db.refresh(access_log)
    return access_log

def update_vic_share_last_accessed(db, share_token):
    """Update last accessed time for VIC share"""
    share = db.query(VICShares).filter(VICShares.share_token == share_token).first()
    if share:
        share.last_accessed = datetime.utcnow()
        db.commit()
        return True
    return False

def get_vic_access_logs(db, share_token=None, hospital=None):
    """Get VIC access logs"""
    query = db.query(VICAccessLogs)
    if share_token:
        query = query.filter(VICAccessLogs.share_token == share_token)
    if hospital:
        query = query.filter(VICAccessLogs.accessed_by_hospital == hospital)
    return query.order_by(VICAccessLogs.created_at.desc()).all()
