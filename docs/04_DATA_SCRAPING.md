# Data Ingestion Subsystem

The GlobalPulse data ingestion subsystem employs a resilient strategy for content aggregation. It minimizes reliance on fragile HTML DOM parsing by prioritizing standard RSS/XML syndication feeds and dedicated REST APIs. This approach ensures high availability, structural consistency, and mitigation of common anti-automation mechanisms.

## 1. Core Architecture

The ingestion process is governed by the `NewsScraper` interface, ensuring polymorphic execution across diverse data sources.

```java
public interface NewsScraper {
    List<News> scrape();
    String getSourceName();
}
```

The `ScraperService` manages the execution lifecycle of all implementing classes. It provides centralized transaction management, error handling, and data deduplication prior to invoking the repository layer.

## 2. Active Ingestion Modules

### 2.1. GoogleNewsIndiaScraper
- **Ingestion Method**: XML Parsing of regional Google News RSS endpoints.
- **Categorical Mapping**: Discovers and maps content to General, Technology, Sports, Health, and Politics domains.
- **Characteristics**: Provides high availability and standardized data structures suitable for direct entity mapping.

### 2.2. TechCrunchScraper
- **Ingestion Method**: Direct XML Parsing of the primary TechCrunch RSS syndication feed.
- **Categorical Mapping**: Statically mapped to the `Technology` domain.
- **Characteristics**: Extracts full HTML payloads and extracts secondary metadata to construct comprehensive `News` entities.

### 2.3. RBIScraper
- **Ingestion Method**: Aggregated proxy parsing via specialized news indexing engines.
- **Categorical Mapping**: Finance.
- **Characteristics**: Designed to reliably aggregate regulatory and financial data while respecting strict source-level automation blocks present on primary institutional websites.

### 2.4. NewsDataIoScraper
- **Ingestion Method**: Direct JSON parsing via the NewsData.io REST API utilizing `RestTemplate`.
- **Categorical Mapping**: Dynamic mapping based on JSON payload classification arrays (Technology, Sports, Business/Finance, Health, Politics).
- **Characteristics**: Provides highly structured, rate-limited, and authenticated data acquisition independent of conventional web scraping vulnerabilities.

## 3. Operational Lifecycle

1. **System Initialization**: Upon context load, a `CommandLineRunner` inspects the persistence layer. If the repository is empty, it immediately invokes the `ScraperService` to populate baseline data.
2. **Scheduled Orchestration**: The `NewsScraperScheduler` component utilizes Spring's `@Scheduled` context to trigger the execution sequence at a fixed rate (default: 10 minutes), ensuring data currency.
3. **Data Hydration and Validation**: Incoming payloads are validated against the entity schema. Missing dimensional data (e.g., semantic images) is deferred to the presentation layer. The `ScraperService` queries the repository to identify existing titles, discarding duplicates to maintain data integrity before committing the transaction block.
