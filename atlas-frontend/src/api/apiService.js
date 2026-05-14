import axios from 'axios';

// In Docker, VITE_API_URL is set to '/api' (proxied by Nginx to the backend container).
// In local development, it falls back to the Spring Boot dev server.
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
});

// attach JWT token to every outgoing request automatically
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('atlas_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// redirect to /login automatically on 401 — clears stale session data
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('atlas_user');
      localStorage.removeItem('atlas_token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// ── Auth ──────────────────────────────────────────────────────────────────────

/** @param {string} username @param {string} password @returns {Promise<{token, username, role}>} */
export const loginUser = async (username, password) => {
  const response = await api.post('/auth/login', { username, password });
  return response.data;
};

// ── Users ─────────────────────────────────────────────────────────────────────

/** @returns {Promise<User[]>} all registered users (Admin only) */
export const getAllUsers = async () => {
  const response = await api.get('/users');
  return response.data;
};

/** @param {object} user - new user data including username, password, role @returns {Promise<User>} */
export const createUser = async (user) => {
  const response = await api.post('/users', user);
  return response.data;
};

/** @param {number} id @param {object} user - fields to update @returns {Promise<User>} */
export const updateUser = async (id, user) => {
  const response = await api.put(`/users/${id}`, user);
  return response.data;
};

/** @param {number} id - user ID to delete @returns {Promise<void>} */
export const deleteUser = async (id) => {
  await api.delete(`/users/${id}`);
};

// ── Equipment ─────────────────────────────────────────────────────────────────

/** @returns {Promise<Equipment[]>} all equipment items */
export const getAllEquipment = async () => {
  const response = await api.get('/equipment');
  return response.data;
};

/** @param {number} id @returns {Promise<Equipment>} single equipment item */
export const getEquipmentById = async (id) => {
  const response = await api.get(`/equipment/${id}`);
  return response.data;
};

/** @param {object} equipment - equipment data to create @returns {Promise<Equipment>} */
export const createEquipment = async (equipment) => {
  const response = await api.post('/equipment', equipment);
  return response.data;
};

/** @param {number} id @param {object} equipment - updated equipment data @returns {Promise<Equipment>} */
export const updateEquipment = async (id, equipment) => {
  const response = await api.put(`/equipment/${id}`, equipment);
  return response.data;
};

/** @param {number} id - equipment ID to delete @returns {Promise<void>} */
export const deleteEquipment = async (id) => {
  await api.delete(`/equipment/${id}`);
};

/** @returns {Promise<Equipment[]>} equipment with availableQuantity ≤ 2 */
export const getLowStockEquipment = async () => {
  const response = await api.get('/equipment/low-stock');
  return response.data;
};

// ── Categories ────────────────────────────────────────────────────────────────

/** @returns {Promise<Category[]>} all equipment categories */
export const getAllCategories = async () => {
  const response = await api.get('/categories');
  return response.data;
};

/** @param {object} category - category data to create @returns {Promise<Category>} */
export const createCategory = async (category) => {
  const response = await api.post('/categories', category);
  return response.data;
};

/** @param {number} id - category ID to delete @returns {Promise<void>} */
export const deleteCategory = async (id) => {
  await api.delete(`/categories/${id}`);
};

// ── Events ────────────────────────────────────────────────────────────────────

/** @returns {Promise<Event[]>} all events */
export const getAllEvents = async () => {
  const response = await api.get('/events');
  return response.data;
};

/** @param {number} id @returns {Promise<Event>} single event */
export const getEventById = async (id) => {
  const response = await api.get(`/events/${id}`);
  return response.data;
};

/** @param {object} event - event data to create @returns {Promise<Event>} */
export const createEvent = async (event) => {
  const response = await api.post('/events', event);
  return response.data;
};

/** @param {number} id @param {object} event - updated event data @returns {Promise<Event>} */
export const updateEvent = async (id, event) => {
  const response = await api.put(`/events/${id}`, event);
  return response.data;
};

/** @param {number} id - event ID to delete @returns {Promise<void>} */
export const deleteEvent = async (id) => {
  await api.delete(`/events/${id}`);
};

// ── Allocations ───────────────────────────────────────────────────────────────

/**
 * Reserves equipment for an event — triggers the State Pattern (IN_STOCK → RESERVED)
 * and optionally the Observer Pattern (LowStockEvent when stock drops to ≤ 2).
 *
 * @param {number} eventId
 * @param {number} equipmentId
 * @param {number} quantityAllocated
 * @param {number|null} rentalPricePerUnit - optional rental price for this allocation
 * @returns {Promise<EquipmentAllocation>}
 */
export const allocateEquipment = async (eventId, equipmentId, quantityAllocated, rentalPricePerUnit = null) => {
  const body = { eventId, equipmentId, quantityAllocated };
  if (rentalPricePerUnit !== null) body.rentalPricePerUnit = rentalPricePerUnit;
  const response = await api.post('/allocations', body);
  return response.data;
};

/**
 * Deploys an allocation — triggers the State Pattern (RESERVED → DEPLOYED).
 * @param {number} allocationId @returns {Promise<void>}
 */
export const deployEquipment = async (allocationId) => {
  await api.post(`/allocations/${allocationId}/deploy`);
};

/**
 * Returns allocated equipment — triggers the State Pattern (DEPLOYED → IN_STOCK).
 * @param {number} allocationId
 * @param {'GOOD'|'DAMAGED'|'MISSING_PARTS'} condition - return condition
 * @param {string|null} damageNotes - optional damage description
 * @returns {Promise<void>}
 */
export const returnEquipment = async (allocationId, condition = 'GOOD', damageNotes = null) => {
  await api.post(`/allocations/${allocationId}/return`, { condition, damageNotes });
};

/** @param {number} eventId @returns {Promise<EquipmentAllocation[]>} allocations for a given event */
export const getAllocationsByEvent = async (eventId) => {
  const response = await api.get(`/allocations/event/${eventId}`);
  return response.data;
};

// ── Sales ─────────────────────────────────────────────────────────────────────

/** @returns {Promise<EquipmentSale[]>} all sales ordered by date descending */
export const getAllSales = async () => {
  const response = await api.get('/sales');
  return response.data;
};

/** @param {object} sale - sale data including equipmentId, quantity, buyer @returns {Promise<EquipmentSale>} */
export const recordSale = async (sale) => {
  const response = await api.post('/sales', sale);
  return response.data;
};

// ── Reports (Admin only) ──────────────────────────────────────────────────────

/** @returns {Promise<ReportSummaryDTO>} high-level dashboard counters */
export const getReportSummary = async () => {
  const response = await api.get('/reports/summary');
  return response.data;
};

/** @returns {Promise<EquipmentReportDTO[]>} per-equipment report data */
export const getEquipmentReport = async () => {
  const response = await api.get('/reports/equipment');
  return response.data;
};

/** @returns {Promise<EventReportDTO[]>} per-event report data */
export const getEventsReport = async () => {
  const response = await api.get('/reports/events');
  return response.data;
};

/** @returns {Promise<AllocationReportDTO[]>} per-allocation report data */
export const getAllocationsReport = async () => {
  const response = await api.get('/reports/allocations');
  return response.data;
};

/** @returns {Promise<SaleReportDTO[]>} sales report data */
export const getSalesReport = async () => {
  const response = await api.get('/reports/sales');
  return response.data;
};
