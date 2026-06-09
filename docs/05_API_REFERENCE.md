# REST API Specification

This document provides a comprehensive specification for the GlobalPulse REST API, outlining the available endpoints, required parameters, and authorization constraints utilized for client-server communication.

## 1. Content Delivery Endpoints (Public)

These endpoints provide paginated access to the aggregated content repository and do not require authentication.

### 1.1. Retrieve Aggregated News
- **Endpoint**: `/api/news`
- **HTTP Method**: `GET`
- **Query Parameters**:
  - `page` (Integer, Optional): The zero-indexed page number (default: `0`).
  - `size` (Integer, Optional): The number of records per page (default: `10`).
- **Description**: Returns a paginated `Page<NewsDTO>` object representing the global aggregated news stream, sorted descending by publication timestamp.

### 1.2. Retrieve Categorical Content
- **Endpoint**: `/api/news/category/{category}`
- **HTTP Method**: `GET`
- **Path Variable**: `category` (String): The categorical domain (e.g., `Technology`, `Finance`).
- **Query Parameters**: `page`, `size`
- **Description**: Returns a paginated list of news entities filtered by the specified category. This endpoint implements the `@Cacheable` abstraction to optimize read operations for highly requested domains.

### 1.3. Execute Keyword Search
- **Endpoint**: `/api/news/search`
- **HTTP Method**: `GET`
- **Query Parameters**: 
  - `keyword` (String, Required): The search parameter.
  - `page`, `size`
- **Description**: Executes an asynchronous `LIKE` query against the `title` and `description` fields across the entire persistence layer, returning relevant `NewsDTO` objects.

## 2. Identity and Authentication Endpoints

These endpoints manage user provisioning and stateless session initialization.

### 2.1. User Registration
- **Endpoint**: `/api/auth/register`
- **HTTP Method**: `POST`
- **Request Payload**: JSON object containing `username`, `email`, and `password`.
- **Description**: Persists a new user record following password encryption and schema validation.

### 2.2. Session Initialization
- **Endpoint**: `/api/auth/login`
- **HTTP Method**: `POST`
- **Request Payload**: JSON object containing `username` and `password`.
- **Response Payload**: Returns a signed JSON Web Token (JWT) string upon successful credential verification.

## 3. Bookmark Operations (Restricted)

These operations manipulate user-specific data and require a valid JWT passed within the HTTP `Authorization` header utilizing the `Bearer` schema.

### 3.1. Persist Bookmark
- **Endpoint**: `/api/bookmarks/{newsId}`
- **HTTP Method**: `POST`
- **Authorization**: `Bearer <JWT>`
- **Description**: Creates a relational mapping between the authenticated `User` context and the target `News` entity.

### 3.2. Delete Bookmark
- **Endpoint**: `/api/bookmarks/{newsId}`
- **HTTP Method**: `DELETE`
- **Authorization**: `Bearer <JWT>`
- **Description**: Terminates the relational mapping between the authenticated `User` context and the target `News` entity.

### 3.3. Retrieve User Bookmarks
- **Endpoint**: `/api/bookmarks`
- **HTTP Method**: `GET`
- **Authorization**: `Bearer <JWT>`
- **Query Parameters**: `page`, `size`
- **Description**: Returns a paginated `Page<Bookmark>` object containing all relational entity mappings associated with the current authenticated user session.
