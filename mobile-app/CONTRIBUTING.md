# Contributing to DID Mobile App

Thank you for your interest in contributing to the DID Mobile App! This document provides guidelines and information for contributors.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Contributing Process](#contributing-process)
- [Coding Standards](#coding-standards)
- [Testing](#testing)
- [Documentation](#documentation)
- [Pull Request Process](#pull-request-process)
- [Issue Reporting](#issue-reporting)
- [Feature Requests](#feature-requests)

## 🤝 Code of Conduct

This project follows a code of conduct that we expect all contributors to follow:

- **Be respectful**: Treat everyone with respect and kindness
- **Be inclusive**: Welcome contributors from all backgrounds
- **Be constructive**: Provide helpful feedback and suggestions
- **Be patient**: Remember that everyone is learning and growing
- **Be professional**: Keep discussions focused on the project

## 🚀 Getting Started

### Prerequisites

Before contributing, make sure you have:

- **Android Studio**: Arctic Fox (2020.3.1) or newer
- **JDK**: Java 11 or newer
- **Android SDK**: API 24-33
- **Git**: For version control
- **Docker**: For testing with backend services

### Development Setup

1. **Fork the repository**
   ```bash
   # Fork on GitHub, then clone your fork
   git clone https://github.com/YOUR_USERNAME/simulasi-did-rekam-medis-elektronik.git
   cd simulasi-did-rekam-medis-elektronik/mobile-app
   ```

2. **Set up development environment**
   ```bash
   # Open in Android Studio
   # Sync Gradle files
   # Install required SDK components
   ```

3. **Configure backend services**
   ```bash
   # Start Docker services
   cd ..
   docker-compose up -d
   ```

4. **Configure mobile app**
   - Update server URLs in `Config.kt`
   - Test connection in app settings

## 🔧 Contributing Process

### 1. Choose an Issue

- Look for issues labeled `good first issue` for beginners
- Check `help wanted` for more complex tasks
- Create a new issue if you have a feature request

### 2. Create a Branch

```bash
# Create and switch to a new branch
git checkout -b feature/your-feature-name

# Or for bug fixes
git checkout -b fix/issue-number-description
```

### 3. Make Changes

- Follow the coding standards
- Write tests for new features
- Update documentation as needed
- Test your changes thoroughly

### 4. Commit Changes

```bash
# Stage your changes
git add .

# Commit with descriptive message
git commit -m "feat: add new feature description"
```

### 5. Push and Create PR

```bash
# Push to your fork
git push origin feature/your-feature-name

# Create Pull Request on GitHub
```

## 📝 Coding Standards

### Kotlin Style Guide

Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

```kotlin
// Use meaningful variable names
val patientData = PatientData(...)

// Use camelCase for variables and functions
fun processVICData(vicData: VICData) { ... }

// Use PascalCase for classes
class VICDetailsActivity : AppCompatActivity() { ... }

// Use UPPER_SNAKE_CASE for constants
companion object {
    private const val MAX_RETRY_ATTEMPTS = 3
}
```

### Code Organization

```kotlin
// 1. Imports
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

// 2. Class declaration
class ExampleActivity : AppCompatActivity() {
    
    // 3. Companion object
    companion object {
        private const val TAG = "ExampleActivity"
    }
    
    // 4. Properties
    private lateinit var binding: ActivityExampleBinding
    
    // 5. Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Implementation
    }
    
    // 6. Private methods
    private fun setupUI() { ... }
    
    // 7. Public methods
    fun updateData() { ... }
}
```

### Naming Conventions

- **Files**: Use PascalCase for classes, camelCase for functions
- **Resources**: Use snake_case for XML files and resources
- **Variables**: Use camelCase
- **Constants**: Use UPPER_SNAKE_CASE
- **Packages**: Use lowercase with dots

### Documentation

```kotlin
/**
 * Processes VIC data and verifies it with the blockchain.
 * 
 * @param vicData The VIC data to process
 * @param callback Callback to handle the result
 */
fun processVICData(vicData: VICData, callback: (Result<VerificationResult>) -> Unit) {
    // Implementation
}
```

## 🧪 Testing

### Unit Tests

```kotlin
@Test
fun testVICVerification() {
    // Given
    val vicData = VICData(...)
    
    // When
    val result = processVICData(vicData)
    
    // Then
    assertTrue(result.isSuccess)
}
```

### Instrumented Tests

```kotlin
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.dicoding.didapp", appContext.packageName)
    }
}
```

### Test Coverage

- Aim for at least 80% test coverage
- Test all public methods
- Test error conditions
- Test edge cases

## 📚 Documentation

### Code Comments

```kotlin
// Good: Explains why, not what
// Check if VIC is expired before processing
if (vicData.isExpired()) {
    return
}

// Bad: Just describes what the code does
// Check if VIC is expired
if (vicData.isExpired()) {
    return
}
```

### README Updates

- Update README.md for new features
- Add screenshots for UI changes
- Update installation instructions
- Document new configuration options

### API Documentation

- Document new API endpoints
- Update request/response examples
- Document error codes
- Add authentication requirements

## 🔄 Pull Request Process

### Before Submitting

1. **Test your changes**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

2. **Check code style**
   ```bash
   ./gradlew ktlintCheck
   ```

3. **Update documentation**
   - Update README if needed
   - Add code comments
   - Update CHANGELOG.md

4. **Squash commits**
   ```bash
   git rebase -i HEAD~n
   ```

### PR Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests pass
- [ ] Instrumented tests pass
- [ ] Manual testing completed

## Screenshots
(if applicable)

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] Tests added/updated
```

### Review Process

1. **Automated checks** must pass
2. **Code review** by maintainers
3. **Testing** by QA team
4. **Approval** from maintainers
5. **Merge** to main branch

## 🐛 Issue Reporting

### Bug Reports

Use the bug report template:

```markdown
**Describe the bug**
A clear description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior:
1. Go to '...'
2. Click on '....'
3. Scroll down to '....'
4. See error

**Expected behavior**
What you expected to happen.

**Screenshots**
If applicable, add screenshots.

**Device information**
- Device: [e.g. Samsung Galaxy S21]
- OS: [e.g. Android 12]
- App version: [e.g. 1.0.0]

**Additional context**
Any other context about the problem.
```

### Feature Requests

Use the feature request template:

```markdown
**Is your feature request related to a problem?**
A clear description of what the problem is.

**Describe the solution you'd like**
A clear description of what you want to happen.

**Describe alternatives you've considered**
A clear description of any alternative solutions.

**Additional context**
Add any other context or screenshots.
```

## 🎯 Feature Requests

### Guidelines

- **Check existing issues** first
- **Be specific** about the feature
- **Explain the use case**
- **Consider implementation complexity**
- **Provide mockups** if possible

### Process

1. Create issue with feature request template
2. Discuss with maintainers
3. Get approval for implementation
4. Create implementation plan
5. Start development

## 📞 Getting Help

### Resources

- **Documentation**: README.md, DEVELOPER_GUIDE.md
- **Issues**: GitHub Issues
- **Discussions**: GitHub Discussions
- **Email**: [Your contact email]

### Community

- **Discord**: [Your Discord server]
- **Slack**: [Your Slack workspace]
- **Forum**: [Your forum URL]

## 🏆 Recognition

Contributors will be recognized in:

- **README.md** - Contributors section
- **CHANGELOG.md** - Release notes
- **GitHub** - Contributors page
- **Documentation** - Special thanks

## 📄 License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

**Thank you for contributing to DID Mobile App! 🚀**
