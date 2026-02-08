# Architecture Hexagonale - Détails d'Implémentation

## 🎯 Vue d'Ensemble

Ce projet implémente une **architecture hexagonale pure** (aussi appelée Ports & Adapters) avec les principes suivants :

### Principes Clés

1. **Indépendance du Domain** : Le cœur métier ne dépend d'aucun framework
2. **Inversion de dépendance** : Le domain définit les contrats (ports), les adapters les implémentent
3. **Testabilité** : Logique métier testable sans infrastructure
4. **Flexibilité** : Facile de changer d'adapter (REST → gRPC, WebClient → RestTemplate)

---

## 📦 Structure des Layers

### 1. Domain Layer (Hexagone Central)

**Responsabilité** : Logique métier pure, règles de gestion

```
domain/
├── model/                          # Modèles métier
│   ├── Email.java                  # Value Object (validation email)
│   ├── FolderId.java              # Value Object (validation UUID)
│   ├── FolderName.java            # Value Object
│   ├── UserFolder.java            # Entity (dossier utilisateur)
│   ├── GlobalFolder.java          # Entity (dossier global)
│   ├── UserFolders.java           # Aggregate (collection de dossiers)
│   ├── Inconsistency.java         # Entity (incohérence détectée)
│   ├── InconsistencyType.java     # Enum (types d'incohérences)
│   └── InconsistencyReport.java   # Aggregate (rapport complet)
│
├── port/
│   ├── driving/                    # Ports primaires (use cases)
│   │   └── ForDetectingInconsistencies.java
│   │
│   └── driven/                     # Ports secondaires (dépendances externes)
│       ├── ForRetrievingUsers.java
│       ├── ForRetrievingUserFolders.java
│       └── ForRetrievingGlobalFolders.java
│
└── service/                        # Implémentation des use cases
    └── InconsistencyDetectionService.java
```

#### Caractéristiques du Domain

✅ **Aucune annotation Spring** (100% framework-agnostic)
✅ **Immutabilité totale** (tous les objets sont immuables)
✅ **Validation stricte** (Value Objects valident à la construction)
✅ **Logique pure** (pas d'I/O, pas d'effets de bord)

---

### 2. Adapter Layer (Couche Technique)

**Responsabilité** : Implémentation des ports, interaction avec l'extérieur

#### 2.1 Driven Adapters (Adaptateurs Sortants)

Implémentent les **ports secondaires** (driven).

```
adapter/driven/rest/
├── dto/
│   ├── UserFolderDto.java         # Record pour JSON (API)
│   └── GlobalFolderDto.java       # Record pour JSON (API)
│
└── RestApiAdapter.java            # Implémente les 3 ports driven
                                   # Utilise WebClient (reactive)
```

**RestApiAdapter** :
- Implémente `ForRetrievingUsers`, `ForRetrievingUserFolders`, `ForRetrievingGlobalFolders`
- Utilise **Spring WebClient** (reactive)
- Traduit DTOs → Domain Models
- Gère les timeouts et erreurs HTTP

#### 2.2 Driving Adapters (Adaptateurs Entrants)

Exposent les **ports primaires** (driving).

```
adapter/driving/rest/
├── dto/
│   ├── InconsistencyDto.java              # Response DTO
│   ├── InconsistencySummaryDto.java       # Summary DTO
│   └── InconsistencyReportDto.java        # Rapport complet DTO
│
└── InconsistencyController.java           # REST Controller (WebFlux)
                                           # Dépend de ForDetectingInconsistencies
```

**InconsistencyController** :
- Utilise **Spring WebFlux** (`@RestController`)
- Retourne `Mono<InconsistencyReportDto>` (reactive)
- Traduit Domain Models → DTOs
- Exécute sur `Schedulers.boundedElastic()` (non-blocking)

---

### 3. Configuration Layer

**Responsabilité** : Wiring des dépendances (DI)

```
configuration/
├── DomainConfiguration.java       # Configure le domain service
│                                  # Crée ExecutorService
│
└── AdapterConfiguration.java      # Configure WebClient
                                   # Crée adapters REST
```

**DomainConfiguration** :
- Instancie `InconsistencyDetectionService` avec ses dépendances
- Configure `ExecutorService` pour parallélisation (CPU cores × 2)

**AdapterConfiguration** :
- Crée `WebClient` avec base URL configurable
- Instancie `RestApiAdapter`
- Expose les implémentations des ports comme beans Spring

---

## 🔄 Flux de Données

### Request Flow (GET /inconsistencies)

```
1. HTTP Request → InconsistencyController (driving adapter)
                  ↓
2. Controller appelle → ForDetectingInconsistencies (port primaire)
                  ↓
3. InconsistencyDetectionService (domain service) exécute :
   a. Appelle ForRetrievingUsers → RestApiAdapter (driven adapter)
   b. Appelle ForRetrievingUserFolders (en parallèle via ExecutorService)
   c. Appelle ForRetrievingGlobalFolders
                  ↓
4. RestApiAdapter fait les appels HTTP (WebClient reactive)
                  ↓
5. Domain service compare et détecte les incohérences
                  ↓
6. Retourne InconsistencyReport (domain model)
                  ↓
7. Controller traduit en InconsistencyReportDto
                  ↓
8. HTTP Response (JSON)
```

---

## 🧪 Stratégie de Test

### Test Doubles - Fake Pattern

Plutôt que d'utiliser des **mocks** (Mockito), ce projet utilise des **Fakes** :

```
test/java/com/linagora/consistency/domain/fake/
├── FakeUserRetriever.java
├── FakeUserFoldersRetriever.java
└── FakeGlobalFoldersRetriever.java
```

#### Avantages des Fakes vs Mocks

| Critère | Fakes | Mocks (Mockito) |
|---------|-------|-----------------|
| **Comportement réel** | ✅ Implémentent la logique | ❌ Stubs configurés |
| **Refactoring-safe** | ✅ Pas couplé aux méthodes | ❌ Tests cassent si API change |
| **Lisibilité** | ✅ Code simple | ❌ `verify()`, `when()` verbeux |
| **Réutilisabilité** | ✅ Même fake pour tous les tests | ❌ Setup dans chaque test |
| **Tests de contrat** | ✅ Vérifie l'implémentation | ❌ Vérifie les interactions |

### Tests Implémentés

1. **Value Objects Tests** (`EmailTest`, `FolderIdTest`)
   - Validation des formats
   - Immutabilité
   - Égalité

2. **Domain Service Tests** (`InconsistencyDetectionServiceTest`)
   - Détection des 3 types d'incohérences
   - Scénarios multi-utilisateurs
   - Scénarios multi-dossiers
   - Cas limites (données vides)

---

## ⚡ Optimisations de Performance

### 1. Parallélisation avec ExecutorService

```java
// InconsistencyDetectionService.java
private List<UserFolders> fetchAllUserFoldersInParallel(List<Email> users) {
    List<Callable<UserFolders>> tasks = users.stream()
        .map(email -> (Callable<UserFolders>) () ->
            userFoldersRetriever.retrieveFoldersForUser(email))
        .toList();

    executorService.invokeAll(tasks); // Parallel execution
}
```

**Gain** : Si 10 users, au lieu de 10 appels séquentiels (10s), on fait 1 batch parallèle (~1s).

### 2. Indexation des Données

```java
// InconsistencyDetectionService.java
Map<Email, Map<FolderId, GlobalFolder>> globalFoldersByUser =
    indexGlobalFoldersByUser(globalFolders);
```

**Gain** : Lookup O(1) au lieu de O(n) pour chaque comparaison.

### 3. WebClient Reactive

```java
// RestApiAdapter.java
webClient.get()
    .uri("/folders")
    .retrieve()
    .bodyToFlux(GlobalFolderDto.class) // Streaming non-blocking
    .collectList()
```

**Gain** : Pas de thread bloqué pendant les I/O réseau.

---

## 🎨 Design Patterns Utilisés

### 1. **Hexagonal Architecture (Ports & Adapters)**
- Séparation domain / infrastructure
- Inversion de dépendance

### 2. **Value Object Pattern**
- Email, FolderId, FolderName : validation + immutabilité
- Typage fort (pas de String primitives)

### 3. **Aggregate Pattern (DDD)**
- `UserFolders` : agrège UserFolder + Email
- `InconsistencyReport` : agrège Inconsistency + statistiques

### 4. **Factory Methods**
```java
Email.of("test@example.com")
Inconsistency.nameMismatch(...)
InconsistencyReport.of(...)
```

### 5. **Strategy Pattern (implicite)**
- Ports = stratégies interchangeables
- Facile d'ajouter un autre adapter (gRPC, Kafka...)

### 6. **Test Double - Fake Pattern**
- Fakes au lieu de mocks pour tests

---

## 🔐 Principes SOLID Appliqués

### S - Single Responsibility
- Chaque classe a une seule raison de changer
- `Email` : validation d'email uniquement
- `InconsistencyDetectionService` : détection uniquement

### O - Open/Closed
- Facile d'ajouter un nouveau type d'incohérence (enum extensible)
- Nouveaux adapters sans modifier le domain

### L - Liskov Substitution
- Tous les adapters implémentent correctement leurs ports
- Fakes substituables aux adapters réels

### I - Interface Segregation
- Ports granulaires (`ForRetrievingUsers` vs `ForRetrievingUserFolders`)
- Pas d'interface "god" avec 10 méthodes

### D - Dependency Inversion
- Domain dépend des ports (abstractions)
- Adapters dépendent des ports
- Configuration wire les dépendances

---

## 📊 Diagramme de Dépendances

```
┌─────────────────────────────────────────────┐
│         Spring Boot Application             │
│             (Bootstrap)                      │
└───────────────┬─────────────────────────────┘
                │
                ├─────────────────────┐
                │                     │
                ▼                     ▼
    ┌───────────────────┐   ┌───────────────────┐
    │  Configuration    │   │  Configuration    │
    │    (Domain)       │   │   (Adapters)      │
    └────────┬──────────┘   └────────┬──────────┘
             │                       │
             ▼                       ▼
    ┌─────────────────────────────────────────┐
    │           DOMAIN LAYER                  │
    │  (No framework dependencies)            │
    │                                         │
    │  ┌────────────┐      ┌──────────────┐  │
    │  │  Models    │      │   Ports      │  │
    │  │            │      │  (Interfaces)│  │
    │  └────────────┘      └──────────────┘  │
    │                                         │
    │  ┌────────────────────────────────┐    │
    │  │  Domain Services               │    │
    │  │  (Business Logic)              │    │
    │  └────────────────────────────────┘    │
    └─────────────────────────────────────────┘
                    ▲           ▲
                    │           │
         Implements │           │ Depends on
                    │           │
        ┌───────────┴───┐   ┌───┴──────────┐
        │   Driving     │   │    Driven    │
        │   Adapters    │   │   Adapters   │
        │               │   │              │
        │ - Controller  │   │ - REST Client│
        │   (WebFlux)   │   │   (WebClient)│
        └───────────────┘   └──────────────┘
                │                   │
                │                   │
                ▼                   ▼
         ┌──────────┐        ┌──────────┐
         │   HTTP   │        │ Mock API │
         │  Client  │        │  (NGINX) │
         └──────────┘        └──────────┘
```

---

## 🚀 Comment Étendre le Système

### Ajouter un nouveau type d'incohérence

1. Ajouter dans `InconsistencyType.java` : `NEW_TYPE`
2. Ajouter factory method dans `Inconsistency.java`
3. Modifier logique dans `InconsistencyDetectionService`
4. Ajouter tests dans `InconsistencyDetectionServiceTest`

✅ **Aucun changement dans les adapters ou configuration**

### Changer l'adapter REST (ex: remplacer WebClient par RestTemplate)

1. Créer `RestTemplateApiAdapter implements ForRetrieving*`
2. Modifier `AdapterConfiguration` pour instancier le nouvel adapter
3. **Le domain et les tests ne changent pas**

### Ajouter un nouveau endpoint

1. Créer nouveau port driving (ex: `ForGettingStatistics`)
2. Créer service domain implémentant ce port
3. Créer controller (driving adapter)
4. Tester avec un Fake

---

## 💡 Lessons Learned

### Ce qui fonctionne bien

✅ **Tests rapides** : Domain tests en < 1s (pas de Spring context)
✅ **Refactoring sûr** : Ports permettent de changer d'implémentation
✅ **Code lisible** : Séparation claire des responsabilités
✅ **Performance** : ExecutorService + WebClient = appels parallèles

### Compromis

⚠️ **Plus de code** : Architecture hexagonale = plus de classes (ports, adapters, DTOs)
⚠️ **Learning curve** : Équipe doit comprendre les concepts DDD
⚠️ **Over-engineering ?** : Pour un petit projet, peut sembler lourd

### Quand utiliser cette architecture ?

✅ **OUI** :
- Projet avec logique métier complexe
- Besoin de tester le métier isolément
- Multiples sources de données / APIs
- Évolution long-terme prévue

❌ **NON** :
- CRUD simple sans logique
- Prototype / POC rapide
- Équipe débutante en DDD

---

## 📚 Ressources

- [Hexagonal Architecture par Alistair Cockburn](https://alistair.cockburn.us/hexagonal-architecture/)
- [Domain-Driven Design (DDD) par Eric Evans](https://www.dddcommunity.org/)
- [Clean Architecture par Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Test Doubles - Martin Fowler](https://martinfowler.com/bliki/TestDouble.html)

---

**Auteur** : Architecture conçue pour l'exercice Linagora 2026
**Date** : Février 2026
