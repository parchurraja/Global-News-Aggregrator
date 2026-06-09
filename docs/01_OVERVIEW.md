# GlobalPulse: Project Overview

GlobalPulse is a distributed news aggregation platform engineered to automate the collection, deduplication, and presentation of digital news content across multiple categories. The system integrates diverse data sources—including direct HTML scraping and RESTful APIs—into a centralized, high-performance application.

## 1. Core Capabilities

1. **Automated Data Ingestion**: Systematically aggregates news data across predefined categories (Technology, Sports, Health, Finance, General) utilizing modular scraper interfaces and external REST API integrations.
2. **Data Integrity and Deduplication**: Implements title-based similarity algorithms at the service layer to intercept and discard redundant data cross-published by multiple sources before database insertion.
3. **Optimized Performance Mechanics**:
   - **Service-Level Caching**: Utilizes Spring's `@Cacheable` infrastructure to cache high-frequency read operations, significantly reducing database I/O latency.
   - **Automated Data Retention**: Employs scheduled background tasks to automatically identify and purge records exceeding a 30-day retention threshold, maintaining database efficiency.
4. **Dynamic Presentation Layer**: 
   - A Single Page Application (SPA) utilizing vanilla JavaScript and Bootstrap 5.
   - Features dynamic theme toggling (Light/Dark mode) relying on native CSS variables and semantic HTML attributes.
   - Implements keyword-based sentiment analysis to provide rapid visual classification of article context.
5. **AI-Assisted Media Fallback**: In the event of missing or malformed image URIs from the source provider, the application dynamically constructs fallback queries to the Pollinations.ai API, utilizing article identifiers as seeds to guarantee deterministic, context-aware imagery.

## 2. Project Lifecycle and Phases

The development of GlobalPulse was segmented into four distinct engineering phases:

### Phase 1: Core Infrastructure Initialization
- Provisioned the Spring Boot application environment.
- Configured Spring Data JPA, Hibernate, and the underlying MySQL schema.
- Developed foundational entity models (`News`, `User`, `Bookmark`).
- Implemented preliminary Jsoup-based HTML scraping utilities.

### Phase 2: Ingestion and API Architecture
- Integrated external REST APIs to expand data sourcing capabilities.
- Developed the `ScraperService` to serve as a central coordinator for all scraping modules, enforcing strict data deduplication policies.
- Constructed the RESTful API controllers to expose paginated data endpoints for client consumption.

### Phase 3: Client Interface Development
- Engineered a responsive, decoupled Single Page Application (SPA) using standard web technologies.
- Implemented cross-session state management for search history and user preferences using the browser's Web Storage API.
- Integrated the Pollinations.ai API to ensure high-quality, uninterrupted media delivery for all aggregated content.

### Phase 4: Performance Optimization and Maintenance
- Integrated the Spring Cache abstraction layer to optimize high-throughput API endpoints.
- Developed and deployed automated database maintenance routines via Spring's scheduling framework.
- Finalized technical documentation and deployment guidelines.
