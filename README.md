# Atlas EMS — Inventory & Event Resource Management System

A full-stack web application built for **Atlas Turbo LTD**, a Kigali-based company that provides professional audio-visual equipment for events such as concerts, weddings, corporate conferences, and cultural celebrations.

---

## The Problem

Atlas Turbo LTD owns a large inventory of high-value audio-visual equipment — amplifiers, speakers, lighting rigs, microphones, mixers, and more. As the business grew and the number of simultaneous events increased, managing equipment manually became a serious operational problem:

- **No visibility on stock.** Staff had no reliable way to know how many units of a given item were available at any moment. Equipment was sometimes promised to two events at once.
- **No equipment lifecycle tracking.** Once equipment left the warehouse, there was no record of where it went, who took it, or when it was supposed to come back.
- **No damage accountability.** When equipment was returned damaged, there was no record of what condition it was in or who was responsible.
- **No audit trail.** Management had no way to see who performed which actions in the system, making accountability and dispute resolution impossible.
- **No client records.** Client contact information was stored in WhatsApp chats and notebooks, making follow-up and billing inconsistent.
- **No sales tracking.** Equipment sold permanently to buyers was not recorded separately from rented items, causing inventory confusion.
- **No reports.** There was no way to generate summaries for business decisions — which events used the most equipment, which items were most in demand, what revenue was generated from rentals vs sales.
- **No role separation.** Everyone had access to everything, with no distinction between what Admin staff and regular Staff could do.

---

## The Solution

Atlas EMS is a purpose-built inventory and event resource management system that digitises and automates the entire equipment lifecycle — from the moment a piece of equipment is added to stock, through reservation, deployment, return, and eventually sale.

### How it solves each problem

| Problem | Solution |
|---|---|
| No stock visibility | Real-time available/total quantity tracking per equipment item. Dashboard shows low-stock alerts automatically |
| Double booking | Reservation system reduces available quantity immediately. Equipment with zero stock is disabled in the allocation form |
| No lifecycle tracking | Equipment moves through enforced states: IN_STOCK → RESERVED → DEPLOYED → IN_STOCK. Every transition is timestamped |
| No damage accountability | Return form records condition (GOOD / DAMAGED / MISSING_PARTS) and damage notes, stored permanently on the allocation record |
| No audit trail | Every allocation, deployment, return, and sale is automatically logged with the username of who performed it and the exact timestamp |
| No client records | Events store client name, phone, and email alongside the event details |
| No sales tracking | Dedicated Sales module records who bought what, when, and for how much — permanently reducing inventory |
| No reports | Reports page with four tabs: Equipment, Events, Allocations, and Sales — each with a downloadable summary |
| No role separation | JWT-based role system: Admin can manage users, view audit logs, and access reports. Staff handles day-to-day operations |

---

## Technology Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3, Spring Security, Spring Data JPA |
| Frontend | React 18, Vite, React Router, Axios |
| Database | PostgreSQL 15 |
| Authentication | JWT (JSON Web Tokens) with BCrypt password hashing |
| Build & Deploy | Docker, Docker Compose, Nginx |
| Testing | JUnit 5, Mockito |

---

## Design Patterns Implemented

### 1. State Pattern — `Equipment.java`

Equipment transitions through defined lifecycle states via controlled methods only. Direct status field mutation is not allowed anywhere in the codebase — all transitions go through the pattern methods, ensuring the lifecycle is never corrupted.

```
IN_STOCK → RESERVED → DEPLOYED → IN_STOCK (on return)
```

```java
equipment.reserve(qty);     // IN_STOCK → RESERVED (reduces available quantity)
equipment.deploy();         // RESERVED → DEPLOYED (equipment leaves warehouse)
equipment.returnStock(qty); // DEPLOYED → IN_STOCK (equipment returns to warehouse)
```

If someone tries to reserve more units than are available, `reserve()` throws an `IllegalStateException` which is caught and re-thrown as `InsufficientStockException` — the request is rejected before any database write.

### 2. Observer Pattern — `LowStockEvent` + `LowStockListener`

When available stock drops to 2 or below after an allocation, `AllocationService` publishes a `LowStockEvent` via Spring's `ApplicationEventPublisher`. `LowStockListener` observes the event and logs a structured warning alert — the publisher and observer are completely decoupled. Adding a new reaction to low stock (e.g. sending an email) requires only a new listener, not changing `AllocationService`.

```java
// publisher — AllocationService.java
if (equipment.getAvailableQuantity() <= LOW_STOCK_THRESHOLD) {
    eventPublisher.publishEvent(new LowStockEvent(this, equipment));
}

// observer — LowStockListener.java
@EventListener
public void handleLowStock(LowStockEvent event) {
    logger.warn("LOW STOCK ALERT: {} — {} units remaining",
        equipment.getName(), equipment.getAvailableQuantity());
}
```

### 3. Repository Pattern — Spring Data JPA Repositories

All database access is abstracted behind repository interfaces. No SQL is written manually anywhere — Spring Data JPA generates fully parameterized queries automatically. This means SQL injection is architecturally impossible, and if the database engine is ever changed, only the configuration changes.

```java
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByStatus(EquipmentStatus status);
    List<Equipment> findByAvailableQuantityLessThanEqual(int threshold);
}
```

---

## Features

### Equipment Management
- Add, edit, and delete equipment with category, quantity, and selling price per unit
- Track available vs total quantity in real time
- Status lifecycle enforced by State Pattern: IN_STOCK → RESERVED → DEPLOYED → RETURNED
- Low stock alerts on the dashboard when available units drop to 2 or below
- Equipment recorded-on date auto-set on creation
- Search by name or category, filter by status

### Event Management
- Create and manage events with name, venue, date, description, and status
- Store client contact details (name, phone, email) per event
- Calendar view showing events per day with clickable dots
- List view with search by name, venue, or client — filter by status (Planned, In Progress, Completed, Cancelled)

### Equipment Allocation
- Reserve equipment for events — triggers the State Pattern (IN_STOCK → RESERVED)
- Set a rental price per unit per allocation (separate from the equipment's selling price)
- Deploy reserved equipment when it physically leaves (RESERVED → DEPLOYED) — records deployed-on timestamp
- Return deployed equipment with a condition report (GOOD / DAMAGED / MISSING_PARTS) — records returned-on timestamp and damage notes
- Allocation table shows rental price, deployed-on date, returned-on date, and condition badge

### Sales
- Record permanent equipment sales with buyer name, quantity, date, and notes
- Sales permanently reduce both available and total inventory counts
- Full sales history with equipment name, category, quantity, date, and buyer
- Sales accessible to both Admin and Staff

### Audit Log (Admin only)
- Every allocation, deployment, return, and sale is automatically logged
- Captures the username of who performed the action (resolved from JWT token)
- Captures the exact timestamp of each action
- Searchable by user, action type, module, or description

### Reports (Admin only)
- Summary stat cards: total equipment, events, allocations, total sales, units sold
- Four detailed report tabs: Equipment, Events, Allocations, Sales

### User Management (Admin only)
- Create Staff and Admin accounts with username, email, and password
- Edit any user's username, email, role, and password
- Delete user accounts
- Passwords are BCrypt-hashed — plain-text passwords are never stored or returned by the API

### Dashboard
- Real-time stat cards: total equipment, total events, low stock alerts, deployed count
- Upcoming events panel (next 60 days, excluding Completed and Cancelled)
- Currently deployed equipment panel

---

## Project Structure

```
atlas-backend/
├── src/
│   ├── main/java/rw/auca/atlas/
│   │   ├── controller/      — REST API endpoints (one controller per resource)
│   │   ├── service/         — Business logic and design pattern implementations
│   │   ├── model/           — JPA entities and enums
│   │   ├── repository/      — Spring Data JPA interfaces (Repository Pattern)
│   │   ├── event/           — Observer Pattern (LowStockEvent, LowStockListener)
│   │   ├── dto/             — Data Transfer Objects for report responses
│   │   ├── exception/       — Custom exceptions and global exception handler
│   │   ├── config/          — Spring Security and JWT configuration
│   │   └── filter/          — JWT authentication filter (runs on every request)
│   └── test/
│       └── AllocationServiceTest.java  — Unit tests covering State and Observer patterns
├── atlas-frontend/
│   ├── src/
│   │   ├── api/             — Axios API service (all HTTP calls centralised here)
│   │   ├── components/      — Navbar, StatusBadge, ProtectedRoute
│   │   ├── context/         — AuthContext (JWT session stored in localStorage)
│   │   └── pages/           — Dashboard, Equipment, Events, Allocations, Sales,
│   │                          AuditLog, Reports, UserManagement, Login
│   ├── Dockerfile           — Multi-stage build: Vite build → Nginx serve
│   └── nginx.conf           — Proxies /api requests to the backend container
├── Dockerfile               — Multi-stage build: Maven compile → JRE run
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
- `postgres` — PostgreSQL 15 database
- `backend` — Spring Boot REST API on port 8080
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
| POST | `/api/auth/login` | Login — returns JWT token |

### Equipment
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/equipment` | List all equipment |
| GET | `/api/equipment/{id}` | Get single equipment item |
| POST | `/api/equipment` | Create equipment |
| PUT | `/api/equipment/{id}` | Update equipment |
| DELETE | `/api/equipment/{id}` | Delete equipment |
| GET | `/api/equipment/low-stock` | Equipment with available stock ≤ 2 |

### Events
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/events` | List all events |
| GET | `/api/events/{id}` | Get single event |
| POST | `/api/events` | Create event |
| PUT | `/api/events/{id}` | Update event |
| DELETE | `/api/events/{id}` | Delete event |

### Allocations
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/allocations` | Reserve equipment for an event |
| POST | `/api/allocations/{id}/deploy` | Deploy allocation (RESERVED → DEPLOYED) |
| POST | `/api/allocations/{id}/return` | Return equipment with condition report |
| GET | `/api/allocations/event/{eventId}` | Get all allocations for an event |

### Sales
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/sales` | List all sales |
| POST | `/api/sales` | Record a new sale |

### Reports (Admin only)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/reports/summary` | High-level stat counters |
| GET | `/api/reports/equipment` | Equipment report |
| GET | `/api/reports/events` | Events report |
| GET | `/api/reports/allocations` | Allocations report |
| GET | `/api/reports/sales` | Sales report |

### Audit Log (Admin only)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/audit-logs` | Full audit trail ordered by most recent |

### Users (Admin only)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/users` | List all users |
| POST | `/api/users` | Create a new user account |
| PUT | `/api/users/{id}` | Update user profile |
| DELETE | `/api/users/{id}` | Delete user account |

---

## Unit Tests

```bash
./mvnw test
```

**AllocationServiceTest** — 9 test cases covering both the State and Observer patterns:

| Test | Pattern |
|---|---|
| Allocate all stock → status becomes RESERVED | State |
| Allocate partial stock → status stays IN_STOCK | State |
| Allocate more than available → throws InsufficientStockException | State |
| Deploy allocation → status becomes DEPLOYED | State |
| Return equipment → stock restored, status becomes IN_STOCK | State |
| Stock drops to exactly 2 → LowStockEvent is published | Observer |
| Stock drops below 2 → LowStockEvent is published | Observer |
| Stock stays above 2 → LowStockEvent is NOT published | Observer |
| Equipment or event not found → throws ResourceNotFoundException | Validation |

---

## Best Practices Applied

**Java — Google Java Style Guide**
- `UpperCamelCase` classes, `lowerCamelCase` methods and variables
- `@NotBlank`, `@Email`, `@NotNull`, `@Min` bean-validation constraints on all entity fields
- `@Valid` on all controller POST/PUT endpoints — invalid requests rejected before reaching the database
- Constructor injection throughout — no field injection (improves testability and immutability)
- `@Transactional` on all service methods — connection lifecycle managed automatically
- Named constants instead of magic numbers (e.g. `LOW_STOCK_THRESHOLD = 2`)
- BCrypt password hashing — plain-text passwords never stored or returned
- Parameterized queries via Spring JPA — SQL injection is architecturally impossible
- `@JsonProperty(access = WRITE_ONLY)` — password field excluded from all API responses
- `@PreAuthorize("hasRole('ADMIN')")` — role-based access enforced at the method level

**JavaScript / React**
- `const`/`let` everywhere — `var` is never used
- `async/await` with `try/catch/finally` on every API call
- Input validation in every form component before the API call is made
- Arrow functions throughout
- Single-responsibility API service (`apiService.js`) — all HTTP communication in one file
- JSDoc `@param`/`@returns` comments on every exported function
- JWT token attached automatically via Axios request interceptor
- `401` responses clear session and redirect to `/login` automatically via response interceptor

---

## Author

**ISHIMWE Mireille** — Adventist University of Central Africa (AUCA)
Final Project — Phase 3: Full-Stack Web Application
