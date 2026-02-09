import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import styles from './index.module.css';

function HeroBanner() {
  const { siteConfig } = useDocusaurusContext();
  return (
    <header className={styles.heroBanner}>
      <div className="container">
        <div className={styles.heroContent}>
          <div className={styles.heroBadge}>Enterprise Test Automation</div>
          <h1 className={styles.heroTitle}>{siteConfig.title}</h1>
          <p className={styles.heroSubtitle}>
            High-Frequency Assurance Engine for Financial Systems
          </p>
          <p className={styles.heroDescription}>
            Validate trading platforms, settlement engines, and reconciliation systems
            across FIX, MQ, REST, and ISO 20022 protocols — powered by AI.
          </p>
          <div className={styles.heroButtons}>
            <Link className={styles.heroPrimary} to="/docs/overview">
              Get Started →
            </Link>
            <Link className={styles.heroSecondary} to="/docs/examples">
              View Examples
            </Link>
          </div>
        </div>
      </div>
    </header>
  );
}

const protocolFeatures = [
  {
    badge: 'FIX',
    color: '#16a34a',
    title: 'FIX Protocol (4.2, 4.4, 5.0)',
    description:
      'WireMock-like stubbing via FixStubRegistry with predicate matching, sequential responses, configurable delays, and call counting through QuickFIX/J.',
  },
  {
    badge: 'MQ',
    color: '#22c55e',
    title: 'Message Queues',
    description:
      'ActiveMQ Artemis via Jakarta JMS with pluggable MessageBroker interface. IBM MQ skeleton ready for extension. Pub/sub with filtered listening.',
  },
  {
    badge: 'REST',
    color: '#f59e0b',
    title: 'REST + OAuth2',
    description:
      'RestAssured wrapper with automatic OAuth2 token lifecycle — acquisition, caching, and refresh 60 seconds before expiry. Typed API clients included.',
  },
  {
    badge: 'ISO',
    color: '#8b5cf6',
    title: 'ISO 20022 (SWIFT)',
    description:
      'AI-powered XML generation for pacs.008, camt.053, and sese.023 messages via SmartStub with deterministic template fallback for CI.',
  },
];

const coreFeatures = [
  {
    icon: '🤖',
    title: 'AI-Powered Testing',
    description:
      'NLP-to-FIX translation via FixScenarioAgent converts natural language like "Buy 500 AAPL at 150 Limit" into structured OrderConfiguration objects. Pluggable LLM providers (OpenAI, Ollama) with template fallback.',
  },
  {
    icon: '🔄',
    title: 'Cross-Source Reconciliation',
    description:
      'Three-way field-by-field comparison of trade records from FIX, MQ, and API sources with configurable tolerance. TradeLedgerAssert provides a fluent assertion DSL.',
  },
  {
    icon: '📊',
    title: 'Statistical Data Generation',
    description:
      'MarketMaker generates realistic prices (Gaussian), volumes (Poisson), correlated series (Cholesky), settlement dates (T+N with NYSE/LSE/TSE calendars), and unique identifiers.',
  },
  {
    icon: '📈',
    title: 'Rich Allure Reports',
    description:
      'AllureFixAttachment formats FIX messages for human readability. ReconciliationReportStep creates detailed tabular comparison steps with pass/fail status.',
  },
  {
    icon: '🧪',
    title: 'Dual-Mode Testing',
    description:
      'TestNG via QuantafBaseTest for direct Java testing, or Cucumber BDD with Gherkin feature files and OrderStepDefs. Both modes generate Allure-compatible results.',
  },
  {
    icon: '🚀',
    title: 'CI/CD Ready',
    description:
      'GitHub Actions workflows for build/test and docs deployment. Docker Compose for local ActiveMQ Artemis. Testcontainers for ephemeral CI infrastructure.',
  },
];

const techStack = [
  { name: 'Java 21', desc: 'LTS' },
  { name: 'Gradle 9.3.1', desc: 'Kotlin DSL' },
  { name: 'QuickFIX/J 2.3.1', desc: 'FIX Protocol' },
  { name: 'LangChain4j 0.35.0', desc: 'AI/LLM' },
  { name: 'TestNG 7.10.2', desc: 'Test Runner' },
  { name: 'Cucumber 7.18.0', desc: 'BDD' },
  { name: 'Allure 2.27.0', desc: 'Reporting' },
  { name: 'RestAssured 5.4.0', desc: 'REST API' },
];

function ProtocolSection() {
  return (
    <section className={styles.protocols}>
      <div className="container">
        <div className={styles.sectionHeader}>
          <h2>Multi-Protocol Coverage</h2>
          <p>Test across every financial communication channel in a single framework</p>
        </div>
        <div className={styles.protocolGrid}>
          {protocolFeatures.map((item, idx) => (
            <div key={idx} className={styles.protocolCard}>
              <span className={styles.protocolBadge} style={{ backgroundColor: item.color }}>
                {item.badge}
              </span>
              <h3>{item.title}</h3>
              <p>{item.description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function FeaturesSection() {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className={styles.sectionHeader}>
          <h2>Built for Financial Systems</h2>
          <p>Enterprise capabilities for mission-critical test automation</p>
        </div>
        <div className={styles.featuresGrid}>
          {coreFeatures.map((item, idx) => (
            <div key={idx} className={styles.featureCard}>
              <div className={styles.featureIcon}>{item.icon}</div>
              <h3>{item.title}</h3>
              <p>{item.description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function ArchitectureSection() {
  return (
    <section className={styles.architecture}>
      <div className="container">
        <div className={styles.sectionHeader}>
          <h2>4-Layer Concentric Architecture</h2>
          <p>Clean separation of concerns with extensibility at every level</p>
        </div>
        <div className={styles.archDiagram}>
          <div className={styles.archLayer} data-layer="4">
            <div className={styles.archLabel}>Layer 4 — Test Definition</div>
            <div className={styles.archClasses}>
              TestNG · Cucumber BDD · Allure Reporting · QuantafBaseTest
            </div>
          </div>
          <div className={styles.archLayer} data-layer="3">
            <div className={styles.archLabel}>Layer 3 — AI Cortex</div>
            <div className={styles.archClasses}>
              FixScenarioAgent · SmartStub · LlmProvider (OpenAI / Ollama)
            </div>
          </div>
          <div className={styles.archLayer} data-layer="2">
            <div className={styles.archLabel}>Layer 2 — Logic Core</div>
            <div className={styles.archClasses}>
              MarketMaker · TradeLedger · BusinessCalendar · Domain Models
            </div>
          </div>
          <div className={styles.archLayer} data-layer="1">
            <div className={styles.archLabel}>Layer 1 — Protocol Adapters</div>
            <div className={styles.archClasses}>
              FIX (QuickFIX/J) · MQ (Jakarta JMS) · REST (RestAssured) · OAuth2
            </div>
          </div>
        </div>
        <div className={styles.archCta}>
          <Link to="/docs/architecture">Explore Full Architecture →</Link>
        </div>
      </div>
    </section>
  );
}

function TechStackSection() {
  return (
    <section className={styles.techStack}>
      <div className="container">
        <div className={styles.sectionHeader}>
          <h2>Technology Stack</h2>
          <p>Modern, battle-tested libraries for enterprise reliability</p>
        </div>
        <div className={styles.techGrid}>
          {techStack.map((item, idx) => (
            <div key={idx} className={styles.techPill}>
              <span className={styles.techName}>{item.name}</span>
              <span className={styles.techDesc}>{item.desc}</span>
            </div>
          ))}
        </div>
        <div className={styles.archCta}>
          <Link to="/docs/tech-stack">Full Tech Stack Details →</Link>
        </div>
      </div>
    </section>
  );
}

function QuickStartSection() {
  return (
    <section className={styles.quickStart}>
      <div className="container">
        <div className={styles.sectionHeader}>
          <h2>Quick Start</h2>
          <p>Up and running in under a minute</p>
        </div>
        <div className={styles.codeBlock}>
          <pre>
            <code>{`# Clone the repository
git clone https://github.com/vinipx/QUANTAF.git
cd QUANTAF

# Build the project
./gradlew build

# Run all tests (unit + scenario + BDD)
./gradlew test

# Generate Allure reports
./gradlew allureReport

# Serve documentation locally
./docs.sh`}</code>
          </pre>
        </div>
      </div>
    </section>
  );
}

export default function Home() {
  const { siteConfig } = useDocusaurusContext();
  return (
    <Layout
      title="Home"
      description="QUANTAF — High-Frequency Assurance Engine for Financial Systems. Enterprise-grade test automation for FIX, MQ, REST, and ISO 20022."
    >
      <HeroBanner />
      <main>
        <ProtocolSection />
        <ArchitectureSection />
        <FeaturesSection />
        <TechStackSection />
        <QuickStartSection />
      </main>
    </Layout>
  );
}
