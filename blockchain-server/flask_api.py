from flask import Flask, jsonify
from database import get_db, get_all_vic_issuances, get_all_transactions, get_all_blocks
from datetime import datetime

# Flask app untuk API endpoints
app = Flask(__name__)

@app.route('/health')
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

@app.route('/verify/<transaction_hash>')
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

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8501, debug=False)
