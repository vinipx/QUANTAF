---
sidebar_position: 11
title: Contributing
description: MIT License, contribution guide, and code standards
---

# Contributing & License

## License

QUANTAF is distributed under the **MIT License**, which permits:

- ✅ **Commercial Use**: Use in production and commercial systems
- ✅ **Modification**: Modify and adapt the framework
- ✅ **Distribution**: Redistribute with or without modifications
- ✅ **Private Use**: Use privately without publishing

See [LICENSE](https://github.com/vinipx/QUANTAF/blob/main/LICENSE) file for full details.

## Contributing

We welcome contributions from the community!

### Getting Started

```bash
# Fork and clone
git clone https://github.com/YOUR-USERNAME/QUANTAF.git
cd QUANTAF

# Create a feature branch
git checkout -b feature/amazing-feature

# Build and test
./gradlew build
./gradlew test
```

### Coding Standards

- Follow Google Java Style Guide
- **Classes**: `PascalCase` (e.g., `FixStubRegistry`, `TradeLedger`)
- **Methods**: `camelCase` (e.g., `generatePrice()`, `reconcileAll()`)
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `FINANCIAL_PRECISION`)
- **Test methods**: descriptive with underscores (e.g., `generatePrice_shouldReturnPositiveValue`)

### Testing Requirements

All contributions must include tests:

```java
// Unit test example
@Test
public void generatePrice_shouldReturnPositiveValue() {
    MarketMaker mm = new MarketMaker();
    BigDecimal price = mm.generatePrice(150.0, 5.0);
    assertThat(price).isPositive();
}
```

### Pull Request Process

1. Ensure all tests pass: `./gradlew test`
2. Update documentation if needed
3. Commit with [conventional commits](https://www.conventionalcommits.org/):
   ```
   feat(protocol): add Kafka broker implementation
   fix(core): handle timezone in settlement dates
   docs(api): add configuration examples
   ```
4. Push and create a Pull Request

### Branch Strategy

- `main`: Production-ready code
- `develop`: Integration branch
- `feature/*`: Individual features
- `bugfix/*`: Bug fixes

## Reporting Security Issues

⚠️ **Do not** file security issues publicly. Contact maintainers via GitHub.

## Code of Conduct

Be respectful and inclusive. Welcome all backgrounds and experience levels.

## Getting Help

- **Questions**: Use [GitHub Discussions](https://github.com/vinipx/QUANTAF/discussions)
- **Bugs**: File an issue on GitHub
- **Ideas**: Create a feature request

Thank you for contributing! 🎉
