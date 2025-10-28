# Block Increment Fix

## Masalah
Block number tetap 1 meskipun sudah ada beberapa VIC issuances. Ini terjadi karena:
1. Block number dihitung dari jumlah blocks yang ada
2. Tapi block baru tidak dibuat untuk setiap VIC issuance
3. Sehingga block number selalu 1

## Solusi
Menambahkan pembuatan block baru untuk setiap VIC issuance:

### 1. Update API Server (`api_server.py`)
```python
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
```

### 2. Update Transaction Data
```python
# Create transaction data
transaction = {
    'block_id': new_block.id,  # Link to the new block
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
```

## Hasil
- Setiap VIC issuance sekarang membuat block baru
- Block number bertambah setiap kali ada VIC issuance
- Blockchain stats di Streamlit menampilkan jumlah blocks yang benar
- Database menyimpan blocks dengan benar

## Testing
```bash
# Test VIC issuance 1
curl -X POST http://31.97.108.98:8502/api/issue-vic \
  -H "Content-Type: application/json" \
  -d '{"hospital":"Rumah Sakit A","patient_id":"P005","medical_data":{"patient_name":"Alice Smith","diagnosis":"Fever","treatment":"Antibiotics","doctor":"Dr. Wilson","date":"2025-10-19","notes":"High temperature"}}'
# Result: blockNumber: 1

# Test VIC issuance 2  
curl -X POST http://31.97.108.98:8502/api/issue-vic \
  -H "Content-Type: application/json" \
  -d '{"hospital":"Rumah Sakit B","patient_id":"P006","medical_data":{"patient_name":"Bob Johnson","diagnosis":"Broken Arm","treatment":"Cast and Pain Medication","doctor":"Dr. Brown","date":"2025-10-19","notes":"X-ray needed"}}'
# Result: blockNumber: 2

# Test VIC issuance 3
curl -X POST http://31.97.108.98:8502/api/issue-vic \
  -H "Content-Type: application/json" \
  -d '{"hospital":"Rumah Sakit A","patient_id":"P007","medical_data":{"patient_name":"Carol Davis","diagnosis":"Allergy","treatment":"Antihistamines","doctor":"Dr. Green","date":"2025-10-19","notes":"Seasonal allergy"}}'
# Result: blockNumber: 3

# Check health
curl http://31.97.108.98:8502/api/health
# Result: {"status":"healthy","transactions":9,"vic_issuances":9,"blocks":3}
```

## Status
✅ **FIXED** - Block number sekarang bertambah dengan benar untuk setiap VIC issuance
