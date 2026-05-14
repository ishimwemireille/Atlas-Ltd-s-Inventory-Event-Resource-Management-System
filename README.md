# Atlas EMS — Inventory & Event Resource Management System

A full-stack web application for **Atlas Turbo LTD** to manage audio-visual equipment inventory, event allocations, sales, and operational reporting.

---

## Technology Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3, Spring Security, Spring Data JPA |
| Frontend | React 18, Vite, React Router, Axios |
| Database | PostgreSQL 15 |
| Auth | JWT (JSON Web Tokens) |
| Build & Deploy | Docker, Docker Compose, Nginx |
| Testing | JUnit 5, Mockito |

---

## Design Patterns Implemented

### 1. State Pattern — `Equipment.java`
Equipment transitions through defined lifecycle states via controlled methods. Direct field mutation is not allowed — only the pattern methods may change the state.

```
IN_STOCK → RESERVED → DEPLOYED → IN_STOCK (on return)
```

```java
equipment.reserve(qty);    // IN_STOCK → RESERVED
equipment.deploy();        // RESERVED → DEPLOYED
equipment.returnStock(qty); // DEPLOYED → IN_STOCK
```

### 2. Observer Pattern — `LowStockEvent` + `LowStockListener`
When available stock drops to 2 or below after an allocation, `AllocationService` publishes a `LowStockEvent` via Spring's `ApplicationEventPublisher`. `LowStockListener` observes the event and logs a warning alert automatically — no coupling between the publisher and listener.

```java
// publisher (AllocationService)
if (equipment.getAvailableQuantity() <= LOW_STOCK_THRESHOLD) {
    eventPublisher.publishEvent(new LowStockEvent(this, equipment));
}

// observer (LowStockListener)
@EventListener
public void handleLowStock(LowStockEvent event) { ... }
```

### 3. Repository Pattern — Spring Data JPA Repositories
All database access is abstracted behind repository interfaces. No SQL is written manually — Spring generates parameterized queries automatically, preventing SQL injection.

```java
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByStatus(EquipmentStatus status);
    List<Equipment> findByAvailableQuantityLessThanEqual(int threshold);
}
```

---

## Features

### Equipment Management
- Add, edit, and delete equipment with category, quantity, and selling price
- Track available vs total quantity in real time
- Status lifecycle: IN_STOCK → RESERVED → DEPLOYED → RETURNED
- Low stock alerts on the dashboard (Observer Pattern)
- Search by name or category, filter by status

### Event Management
- Create and manage events with date, venue, and status
- Store client contact details (name, phone, email)
- Calendar view and list view with search and status filter

### Equipment Allocation
- Reserve equipment for events (triggers State Pattern)
- Set a rental price per unit per allocation (separate from selling price)
- Deploy reserved equipment (RESERVED → DEPLOYED)
- Return deployed equipment with condition report (GOOD / DAMAGED / MISSING_PARTS)
- Track deployed-on and returned-on timestamps

### Sales
- Record permanent equipment sales with buyer details and date sold
- Sales reduce both available and total inventory counts
- Full sales history with category and equipment details

### Audit Log (Admin only)
- Every allocation, deployment, return, and sale is automatically recorded
- Captures who performed the action (resolved from JWT token)
- Searchable by user, action, module, or description

### Reports (Admin only)
- Summary dashboard: total equipment, events, allocations, sales
- Detailed tabs: Equipment, Events, Allocations, Sales

### User Management (Admin only)
- Create Staff and Admin accounts
- Edit username, email, role, and password
- Passwords are BCrypt-hashed — never stored in plain text

### Dashboard
- Real-time stat cards: equipment count, event count, low stock alerts, deployed items
- Upcoming events (next 60 days)
- Currently deployed equipment panel

---

## Project Structure

```
atlas-backend/
├── src/
│   ├── main/java/rw/auca/atlas/
│   │   ├── controller/      — REST API endpoints
│   │   ├── service/         — Business logic & design patterns
│   │   ├── model/           — JPA entities & enums
│   │   ├── repository/      — Spring Data JPA interfaces (Repository Pattern)
│   │   ├── event/           — Observer Pattern (LowStockEvent, LowStockListener)
│   │   ├── dto/             — Data Transfer Objects for reports
│   │   ├── exception/       — Global exception handler
│   │   ├── config/          — Security, JWT configuration
│   │   └── filter/          — JWT authentication filter
│   └── test/
│       └── AllocationServiceTest.java  — Unit tests (State & Observer patterns)
├── atlas-frontend/
│   ├── src/
│   │   ├── api/             — Axios API service (all HTTP calls in one place)
│   │   ├── components/      — Navbar, StatusBadge, ProtectedRoute
│   │   ├── context/         — AuthContext (JWT session management)
│   │   └── pages/           — Dashboard, Equipment, Events, Allocations, Sales, Reports, etc.
│   ├── Dockerfile
│   └── nginx.conf
├── Dockerfile
└── README.md
```

---

## Running the Application

### Prerequisites
- Docker Desktop installed and running

### Start everything

```bash
docker compose up --build
```

This starts three containers:
- `postgres` — PostgreSQL database on port 5433
- `backend` — Spring Boot API on port 8080
- `frontend` — React app served by Nginx on port 5173

### Open the app

```
http://localhost:5173
```

### Default credentials

| Username | Password | Role |
|---|---|---|
| admin | admin123 | ADMIN |
| staff | staff123 | STAFF |

---

## API Endpoints

### Auth
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/login` | Login and receive JWT token |

### Equipment
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/equipment` | List all equipment |
| GET | `/api/equipment/{id}` | Get single equipment |
| POST | `/api/equipment` | Create equipment |
| PUT | `/api/equipment/{id}` | Update equipment |
| DELETE | `/api/equipment/{id}` | Delete equipment |
| GET | `/api/equipment/low-stock` | Equipment with stock ≤ 2 |

### Events
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/events` | List all events |
| POST | `/api/events` | Create event |
| PUT | `/api/events/{id}` | Update event |
| DELETE | `/api/events/{id}` | Delete event |

### Allocations
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/allocations` | Reserve equipment for event |
| POST | `/api/allocations/{id}/deploy` | Deploy allocation |
| POST | `/api/allocations/{id}/return` | Return equipment with condition |
| GET | `/api/allocations/event/{eventId}` | Get allocations for an event |

### Sales
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/sales` | List all sales |
| POST | `/api/sales` | Record a sale |

### Reports (Admin only)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/reports/summary` | High-level counters |
| GET | `/api/reports/equipment` | Equipment report |
| GET | `/api/reports/events` | Events report |
| GET | `/api/reports/allocations` | Allocations report |
| GET | `/api/reports/sales` | Sales report |

### Audit Log (Admin only)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/audit-logs` | Full audit trail |

### Users (Admin only)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/users` | List all users |
| POST | `/api/users` | Create user |
| PUT | `/api/users/{id}` | Update user |
| DELETE | `/api/users/{id}` | Delete user |

---

## Unit Tests

```bash
cd atlas-backend
./mvnw test
```

**AllocationServiceTest** covers 9 test cases:
- State Pattern: allocate all stock sets RESERVED status
- State Pattern: partial allocation keeps IN_STOCK status
- State Pattern: exceeds stock throws InsufficientStockException
- State Pattern: deploy transitions to DEPLOYED
- State Pattern: return restores stock and sets IN_STOCK
- Observer Pattern: LowStockEvent published when stock drops to 2
- Observer Pattern: LowStockEvent published when stock drops below 2
- Observer Pattern: LowStockEvent NOT published when stock stays above 2
- ResourceNotFoundException when equipment or event not found

---

## Best Practices Applied

**Java (Google Java Style Guide)**
- `UpperCamelCase` classes, `lowerCamelCase` methods and variables
- `@NotBlank`, `@Email`, `@NotNull`, `@Min` bean-validation on all entity fields
- `@Valid` on all controller POST/PUT endpoints — rejects invalid input before DB access
- Constructor injection throughout — no field injection
- `@Transactional` on all service methods — automatic connection lifecycle management
- Named constants instead of magic numbers (`LOW_STOCK_THRESHOLD = 2`)
- BCrypt password hashing — plain-text passwords never stored or returned
- Parameterized queries via Spring JPA — SQL injection is not possible
- `@JsonProperty(access = WRITE_ONLY)` — passwords excluded from all API responses

**JavaScript / React**
- `const`/`let` everywhere — `var` is never used
- `async/await` with `try/catch/finally` on every API call
- Input validation before every API call in all form components
- Arrow functions throughout
- Single-responsibility API service (`apiService.js`) — all HTTP calls in one place
- JSDoc `@param`/`@returns` on every exported function
- JWT token attached automatically via Axios request interceptor
- `401` responses redirect to `/login` automatically via response interceptor

---

## Author

**ISHIMWE Mireille** — Adventist University of Central Africa (AUCA)
Final Project — Phase 3: Full-Stack Web Application
