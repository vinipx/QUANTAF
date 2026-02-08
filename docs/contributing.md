# Contributing & License

## License

QUANTAF is distributed under the **MIT License**, which permits:

- ✅ **Commercial Use**: Use in production and commercial systems
- ✅ **Modification**: Modify and adapt the framework
- ✅ **Distribution**: Redistribute with or without modifications
- ✅ **Private Use**: Use privately without publishing

With minimal requirements:
- Include the original license and copyright notice
- State significant changes to the code

See [LICENSE](../LICENSE) file for full details.

## Contributing

We welcome contributions from the community! Here's how to get involved:

### Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/YOUR-USERNAME/QUANTAF.git
   cd QUANTAF
   ```
3. **Create a feature branch**:
   ```bash
   git checkout -b feature/amazing-feature
   ```

### Development Setup

1. **Install dependencies**:
   ```bash
   ./gradlew build
   ```

2. **Set up local services**:
   ```bash
   docker-compose up -d
   ```

3. **Run tests**:
   ```bash
   ./gradlew test
   ```

### Coding Standards

#### Java Code Style

Follow Google Java Style Guide:

```java
// Class declaration
public class FixAdapter implements ProtocolAdapter {
    // Constants: UPPER_SNAKE_CASE
    private static final int DEFAULT_TIMEOUT_MS = 5000;
    
    // Fields: camelCase with private
    private String host;
    private int port;
    
    // Methods: camelCase, descriptive names
    public void connect(String host, int port) throws IOException {
        // ...
    }
    
    // Javadoc for public APIs
    /**
     * Send a FIX message to the server.
     * 
     * @param message the FIX message to send
     * @throws IOException if connection fails
     */
    public void send(FixMessage message) throws IOException {
        // ...
    }
}
```

#### Naming Conventions

- **Classes**: `PascalCase` (e.g., `FixAdapter`, `TradeLedger`)
- **Methods/Variables**: `camelCase` (e.g., `sendMessage()`, `orderQty`)
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `MAX_RETRIES`)
- **Packages**: lowercase, dot-separated (e.g., `protocol.fix`)

#### Code Organization

- Keep methods under 20 lines
- One responsibility per class
- Write javadoc for public APIs
- Use meaningful variable names

### Testing Requirements

All contributions must include tests:

#### Unit Tests

Test individual classes in isolation:

```java
@Test
public void testMarketMakerDistribution() {
    MarketMaker mm = new MarketMaker();
    
    for (int i = 0; i < 100; i++) {
        BigDecimal price = mm.generatePrice("AAPL");
        assertThat(price).isGreaterThan(BigDecimal.ZERO);
    }
}
```

#### Integration Tests

Test adapter interactions:

```java
@Test
public void testFixAdapterOrderExecution() throws IOException {
    // Arrange
    FixAdapter adapter = new FixAdapter();
    adapter.connect("localhost", 9876);
    
    // Act
    FixMessage order = buildTestOrder();
    adapter.send(order);
    FixMessage execution = adapter.receive(5000);
    
    // Assert
    assertThat(execution).isNotNull();
    assertThat(execution.getField(35)).isEqualTo("8");
    
    // Cleanup
    adapter.disconnect();
}
```

#### Naming Tests

Use descriptive names following the pattern:
```
test<Feature><Scenario><ExpectedBehavior>
```

Examples:
- `testMarketMakerGeneratesPricesWithinRange()`
- `testFixAdapterRejectsInvalidMessages()`
- `testTradeLedgerRecordsTransactions()`

### Pull Request Process

1. **Ensure all tests pass**:
   ```bash
   ./gradlew test
   ```

2. **Run code quality checks**:
   ```bash
   ./gradlew build
   ```

3. **Generate Allure report** (optional):
   ```bash
   ./gradlew allureReport
   ```

4. **Update documentation**:
   - Add javadoc for public APIs
   - Update README if needed
   - Add/update relevant docs/ pages

5. **Commit with clear messages**:
   ```bash
   git commit -m "feat: add NLP support for order placement

   - Implement FixScenarioAgent for natural language translation
   - Add unit tests for phrase parsing
   - Update documentation with examples
   
   Fixes #123"
   ```

6. **Push to your fork**:
   ```bash
   git push origin feature/amazing-feature
   ```

7. **Create a Pull Request**:
   - Use descriptive title
   - Link to related issues
   - Describe changes and motivation
   - Attach test results/screenshots

### Commit Message Guidelines

Follow conventional commits format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Code style (formatting, semicolons, etc.)
- `refactor`: Code refactoring
- `perf`: Performance improvements
- `test`: Adding or updating tests
- `ci`: CI/CD configuration

**Examples:**
```
feat(protocol): add SWIFT message adapter

fix(core): handle timezone conversion in ledger

docs(api): add configuration examples

test(fix): increase timeout for flaky tests
```

### Issue Tracking

Before starting work:

1. **Check existing issues** to avoid duplication
2. **Create an issue** if not already reported
3. **Reference issue** in your PR

Issue format:
- **Description**: What is the problem?
- **Steps to reproduce** (for bugs)
- **Expected behavior**: What should happen?
- **Actual behavior**: What currently happens?
- **Environment**: OS, Java version, etc.

## Code Review Process

All PRs are reviewed for:

- ✅ **Correctness**: Code works as intended
- ✅ **Testing**: Adequate test coverage
- ✅ **Quality**: Follows code standards
- ✅ **Documentation**: Clear javadoc and examples
- ✅ **Performance**: No regressions
- ✅ **Security**: No vulnerabilities introduced

### Review Feedback

- Constructive criticism only
- Suggest improvements with examples
- Ask clarifying questions if needed
- Approve once standards are met

## Development Workflow

### Branch Strategy

- `main`: Production-ready code, tagged releases
- `develop`: Integration branch for features
- `feature/*`: Individual feature branches
- `bugfix/*`: Bug fix branches
- `docs/*`: Documentation updates

```bash
# Update from upstream
git fetch upstream
git rebase upstream/develop

# Create feature branch
git checkout -b feature/my-feature develop

# Push changes
git push origin feature/my-feature

# Create PR to develop
```

### Local Testing

Before committing:

```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests "TestClassName"

# Generate reports
./gradlew allureReport

# Check code quality
./gradlew build
```

## Documentation Contribution

Improve docs/ pages:

1. **Edit Markdown files** in docs/
2. **Preview locally**:
   ```bash
   ./docs.sh
   # Open http://localhost:8000
   ```
3. **Commit and PR** with documentation changes

### Documentation Standards

- **Clear Language**: Write for all skill levels
- **Examples**: Include code samples
- **Links**: Cross-reference related sections
- **Currency**: Keep examples up-to-date
- **Format**: Follow Markdown conventions

## Reporting Security Issues

⚠️ **Do not** file security issues publicly.

1. Email security details to: security@quantaf.dev
2. Include:
   - Description of vulnerability
   - Steps to reproduce
   - Impact assessment
   - Suggested fix (if available)

We will:
- Acknowledge receipt within 48 hours
- Work on fix in private repository
- Release patch and security advisory

## Code of Conduct

Be respectful and inclusive:

- ✅ Welcome all backgrounds and experience levels
- ✅ Use inclusive language
- ✅ Give credit to contributors
- ❌ No harassment or discrimination
- ❌ No spam or self-promotion

## Recognition

Contributors are recognized in:

- **CONTRIBUTORS.md** file
- **Release notes** for their contributions
- **GitHub contributors** page

## Getting Help

- **Questions**: Use GitHub Discussions
- **Bugs**: File an issue on GitHub
- **Ideas**: Create a feature request
- **Chat**: Join our community channels

## Resources

- [GitHub Flow Guide](https://guides.github.com/introduction/flow/)
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [MIT License](https://opensource.org/licenses/MIT)

## Thank You!

Your contributions make QUANTAF better for everyone. Thank you for investing your time and effort! 🎉

---

For questions or guidance, please:
- Open a [GitHub Discussion](https://github.com/vinipx/QUANTAF/discussions)
- Comment on related issues
- Reach out to the maintainers
