# Changelog

All notable changes to the DID Mobile App will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial release of DID Mobile App
- QR Code scanning functionality
- Digital wallet for VIC management
- Biometric authentication
- PIN protection
- VIC sharing capabilities
- Server configuration
- Security settings
- Patient data management
- Medical record storage
- API integration with blockchain server

### Changed
- N/A

### Deprecated
- N/A

### Removed
- N/A

### Fixed
- N/A

### Security
- N/A

## [1.0.0] - 2025-01-28

### Added
- **Core Features**:
  - Main activity with navigation
  - QR code scanner with auto-focus
  - Digital wallet interface
  - VIC management system
  - Patient data management
  - Medical record storage

- **Security Features**:
  - Biometric authentication (fingerprint/face)
  - PIN protection (6-digit)
  - Encrypted local storage
  - Secure API communication
  - Session management

- **UI/UX**:
  - Material Design interface
  - Dark/Light theme support
  - Responsive layout
  - Accessibility features
  - Modern card-based design

- **Network Features**:
  - API integration with FastAPI server
  - Flask API integration for VIC verification
  - Server configuration
  - Connection testing
  - Error handling

- **VIC Features**:
  - VIC scanning and storage
  - VIC verification with blockchain
  - VIC sharing with permissions
  - VIC access logging
  - QR code generation

- **Data Management**:
  - Local storage with SharedPreferences
  - Data encryption
  - Backup and restore
  - Data validation
  - Offline support

### Technical Details
- **Platform**: Android (Kotlin)
- **Min SDK**: API 24 (Android 7.0)
- **Target SDK**: API 33 (Android 13)
- **Architecture**: MVVM pattern
- **Dependencies**:
  - Retrofit for networking
  - ZXing for QR code scanning
  - Biometric API for authentication
  - Gson for JSON parsing
  - Coroutines for async operations

### Known Issues
- None at this time

### Migration Notes
- This is the initial release
- No migration required

## [0.9.0] - 2025-01-20

### Added
- Initial development version
- Basic app structure
- Core functionality implementation
- UI components
- Network layer
- Security implementation

### Changed
- N/A

### Deprecated
- N/A

### Removed
- N/A

### Fixed
- N/A

### Security
- N/A

## [0.8.0] - 2025-01-15

### Added
- Project setup
- Basic architecture
- Initial UI design
- Core data models
- Network configuration

### Changed
- N/A

### Deprecated
- N/A

### Removed
- N/A

### Fixed
- N/A

### Security
- N/A

---

## Release Notes

### Version 1.0.0
This is the first stable release of the DID Mobile App. The app provides a complete solution for managing Verifiable Identity Credentials (VIC) in a decentralized medical records system.

**Key Features**:
- Complete VIC lifecycle management
- Secure authentication and authorization
- QR code scanning and generation
- Digital wallet functionality
- Blockchain integration
- Modern Material Design UI

**System Requirements**:
- Android 7.0 (API 24) or higher
- 2GB RAM minimum
- 100MB storage space
- Internet connection for API calls
- Camera for QR code scanning
- Biometric sensor (optional)

**Installation**:
1. Download APK from releases
2. Enable "Install from unknown sources"
3. Install APK
4. Configure server settings
5. Set up PIN and biometric authentication

**Support**:
- GitHub Issues: [Create issue](https://github.com/Nvl123/simulasi-did-rekam-medis-elektronik/issues)
- Documentation: See README.md and DEVELOPER_GUIDE.md
- Email: [Your support email]

---

## Contributing

When contributing to this project, please update this changelog by adding a new entry under the `[Unreleased]` section. The entry should include:

- **Added**: for new features
- **Changed**: for changes in existing functionality
- **Deprecated**: for soon-to-be removed features
- **Removed**: for now removed features
- **Fixed**: for any bug fixes
- **Security**: for vulnerability fixes

## Format

- Use the format: `## [Version] - YYYY-MM-DD`
- Use semantic versioning (MAJOR.MINOR.PATCH)
- Group changes by type
- Use present tense ("Add feature" not "Added feature")
- Use imperative mood ("Move feature" not "Moved feature")
- Reference issues and pull requests when possible
