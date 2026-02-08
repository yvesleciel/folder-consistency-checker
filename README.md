# Folder Consistency Checker Service

A reactive Java service implementing **Hexagonal Architecture** to detect inconsistencies between user-specific folder data and global folder data from a REST API.

## 🏗️ Architecture

This project implements a **clean hexagonal architecture** with strict separation of concerns:

```
domain/                  → Pure business logic (framework-agnostic)
├── model/              → Value Objects & Entities (immutable)
├── port/
│   ├── driving/        → Primary ports (ForXXX interfaces)
│   └── driven/         → Secondary ports (ForXXX interfaces)
└── service/            → Use cases (business logic)

adapter/                → Technical implementations
├── driving/            → REST controllers (Spring WebFlux)
│   └── rest/
└── driven/             → REST API client (WebClient)
    └── rest/

configuration/          → Spring configuration & dependency injection
```

### Design Principles

- ✅ **Domain-first**: No framework dependencies in domain layer
- ✅ **Port & Adapter pattern**: Clear interfaces between layers
- ✅ **Immutability**: All domain models are immutable
- ✅ **Value Objects**: Strong typing with Email, FolderId, FolderName
- ✅ **Reactive**: Non-blocking I/O with Spring WebFlux
- ✅ **Parallel processing**: ExecutorService for concurrent API calls
- ✅ **Test Doubles**: Fake implementations (not mocks) for testing

---

## 📋 Requirements

- **Java 17** or higher
- **Maven 3.8+**
- **Docker** (for running the mock API)

---

##  Building the Service

```bash
./mvnw clean package
```

This will:
- Compile the code
- Run all tests
- Create executable JAR in `target/consistency-0.0.1-SNAPSHOT.jar`

---

## ▶️ Running the Service

### 1. Start the Mock API

First, start the mock API from the root project directory:

```bash
cd ..
docker compose up -d
```

The mock API will be available at `http://localhost:8080`.

### 2. Start the Consistency Service

```bash
cd consistency
./mvnw spring-boot:run
```

Or run the JAR directly:

```bash
java -jar target/consistency-0.0.1-SNAPSHOT.jar
```

The service starts on **port 8081** by default.

---

## 📡 API Endpoint

### `GET /inconsistencies`

Returns all detected inconsistencies between user-specific folders and global folders.

**Request:**
```bash
curl http://localhost:8081/inconsistencies
```

**Response Format:**

```json
{
  "summary": {
    "totalInconsistencies": 3,
    "countsByType": {
      "NAME_MISMATCH": 1,
      "MISSING_IN_GLOBAL": 1,
      "MISSING_IN_USER_FOLDERS": 1
    }
  },
  "inconsistencies": [
    {
      "type": "NAME_MISMATCH",
      "folderId": "55cc5502-7237-4e5c-b4da-4d4aebca58e0",
      "userEmail": "john@linagora.com",
      "globalFolderName": "Wrong name",
      "userFolderName": "Receipts"
    },
    {
      "type": "MISSING_IN_GLOBAL",
      "folderId": "550e8400-e29b-41d4-a716-123456789abc",
      "userEmail": "alice@linagora.com",
      "globalFolderName": null,
      "userFolderName": "Personal"
    },
    {
      "type": "MISSING_IN_USER_FOLDERS",
      "folderId": "550e8400-e29b-41d4-a716-987654321def",
      "userEmail": "bob@linagora.com",
      "globalFolderName": "Archive",
      "userFolderName": null
    }
  ]
}
```

### Response Fields

#### Summary
- **`totalInconsistencies`** (integer): Total number of detected inconsistencies
- **`countsByType`** (object): Breakdown of inconsistencies by type

#### Inconsistency Object
- **`type`** (string): Type of inconsistency
  - `NAME_MISMATCH`: Folder exists in both sources but with different names
  - `MISSING_IN_GLOBAL`: Folder exists in user data but not in global data
  - `MISSING_IN_USER_FOLDERS`: Folder exists in global data but not in user data
- **`folderId`** (string): UUID of the folder
- **`userEmail`** (string): Email of the user owning the folder
- **`globalFolderName`** (string|null): Folder name from global endpoint (null if missing)
- **`userFolderName`** (string|null): Folder name from user endpoint (null if missing)

---

## 🧪 Running Tests

```bash
./mvnw test
```

### Test Strategy

This project demonstrates a **Test Doubles approach** using **Fake implementations** instead of mocking frameworks:

#### 1. **Domain Model Tests**
- Value Object validation (Email, FolderId, FolderName)
- Immutability guarantees
- Equality semantics

#### 2. **Business Logic Tests** (using Fakes)
- `InconsistencyDetectionServiceTest`: Core use case testing
- **Fake Test Doubles**: `FakeUserRetriever`, `FakeUserFoldersRetriever`, `FakeGlobalFoldersRetriever`
- Tests all inconsistency types:
  - Name mismatches
  - Missing folders in global data
  - Missing folders in user data
  - Multiple users scenarios
  - Multiple folders per user

#### 3. **Why Fakes over Mocks?**
- ✅ **Real behavior**: Fakes implement actual logic, not stubs
- ✅ **Better refactoring**: No coupling to method calls
- ✅ **Readable tests**: No mock verification noise
- ✅ **Reusable**: Same fakes across multiple tests

---

## ⚙️ Configuration

Edit `src/main/resources/application.yaml`:

```yaml
server:
  port: 8081                          # Service port

mock:
  api:
    base-url: http://localhost:8080   # Mock API URL
    timeout-seconds: 10               # HTTP request timeout
```

---

## 🔧 Technical Details

### Reactive Stack
- **Spring WebFlux**: Reactive web framework (Netty)
- **WebClient**: Non-blocking HTTP client
- **Reactor**: Mono/Flux for reactive streams

### Performance Optimization
- **Parallel API calls**: `ExecutorService` with thread pool (CPU cores × 2)
- **Concurrent user folder fetching**: All users fetched in parallel
- **Efficient indexing**: HashMap-based lookups for O(1) comparison

### Reactivity
- Controller returns `Mono<InconsistencyReportDto>` (non-blocking)
- Runs on `Schedulers.boundedElastic()` to avoid blocking Netty threads
- WebClient uses reactive streams internally

---

## 📁 Project Structure

```
src/main/java/com/linagora/consistency/
├── adapter/
│   ├── driven/
│   │   └── rest/
│   │       ├── dto/                    # REST DTOs
│   │       └── RestApiAdapter.java     # HTTP client adapter
│   └── driving/
│       └── rest/
│           ├── dto/                    # API response DTOs
│           └── InconsistencyController.java
├── configuration/
│   ├── AdapterConfiguration.java       # WebClient & adapters
│   └── DomainConfiguration.java        # Domain services & ExecutorService
├── domain/
│   ├── model/                          # Value Objects & Entities
│   │   ├── Email.java
│   │   ├── FolderId.java
│   │   ├── FolderName.java
│   │   ├── UserFolder.java
│   │   ├── GlobalFolder.java
│   │   ├── UserFolders.java
│   │   ├── Inconsistency.java
│   │   ├── InconsistencyType.java
│   │   └── InconsistencyReport.java
│   ├── port/
│   │   ├── driven/                     # Secondary ports
│   │   │   ├── ForRetrievingUsers.java
│   │   │   ├── ForRetrievingUserFolders.java
│   │   │   └── ForRetrievingGlobalFolders.java
│   │   └── driving/                    # Primary port
│   │       └── ForDetectingInconsistencies.java
│   └── service/
│       └── InconsistencyDetectionService.java
└── ConsistencyApplication.java

src/test/java/com/linagora/consistency/
├── domain/
│   ├── fake/                           # Test Doubles (Fakes)
│   │   ├── FakeUserRetriever.java
│   │   ├── FakeUserFoldersRetriever.java
│   │   └── FakeGlobalFoldersRetriever.java
│   ├── model/                          # Value Object tests
│   │   ├── EmailTest.java
│   │   └── FolderIdTest.java
│   └── service/
│       └── InconsistencyDetectionServiceTest.java
```

---

## 🎯 Design Decisions

### 1. **Hexagonal Architecture**
- Allows testing business logic without Spring dependencies
- Easy to swap adapters (e.g., replace WebClient with RestTemplate)
- Clear separation between "what" (domain) and "how" (adapters)

### 2. **ForXXX Port Naming Convention**
- `ForDetectingInconsistencies`: Primary port (use case)
- `ForRetrievingUsers`, `ForRetrievingUserFolders`, `ForRetrievingGlobalFolders`: Secondary ports
- Emphasizes the **purpose** of each port

### 3. **Immutable Domain Models**
- All entities and value objects are immutable
- Thread-safe by design
- Predictable behavior

### 4. **Blocking in Adapter, Non-blocking in Controller**
- Domain service uses synchronous interfaces (simpler logic)
- WebClient handles reactivity in adapter layer
- Controller returns Mono for end-to-end non-blocking

### 5. **ExecutorService for Parallelization**
- Fetches all user folders concurrently
- Configurable thread pool size
- Better performance than sequential API calls

---

## 🔍 Inconsistency Detection Logic

1. **Fetch data from 3 endpoints**:
   - `/users` → List of user emails
   - `/users/{email}/folders` → Per-user folders (parallel)
   - `/folders` → Global folder list

2. **Index global folders** by (user, folderId) for O(1) lookup

3. **Compare for each user**:
   - Iterate user folders:
     - If not in global → `MISSING_IN_GLOBAL`
     - If in global but name differs → `NAME_MISMATCH`
   - Check global folders not visited → `MISSING_IN_USER_FOLDERS`

4. **Return aggregated report** with summary and details

---

## 📝 Notes

- Service dynamically fetches data (no hardcoded datasets)
- Works with any data structure the mock API serves
- Timeout configurable for slow networks
- Thread pool size auto-configured based on CPU cores

---

## 🧑‍💻 Author

Built with **clean architecture principles** for the Linagora coding exercise.
