// Configuration for VIC Issuance
const CONFIG = {
    // API endpoints - will try public IP first, then localhost
    API_ENDPOINTS: [
        'http://31.97.108.98:8502/api/issue-vic',
        'http://localhost:8502/api/issue-vic'
    ],
    
    // Hospital information
    HOSPITAL_NAME: 'Rumah Sakit B',
    HOSPITAL_COLOR: '#e74c3c'
};
