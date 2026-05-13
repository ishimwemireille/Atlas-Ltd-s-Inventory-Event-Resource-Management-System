import axios from 'axios';

// In Docker, VITE_API_URL is set to '/api' (proxied by Nginx to the backend container).
// In local development, it falls back to the Spring Boot dev server.
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('atlas_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

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

export const loginUser = async (username, password) => {
  const response = await api.post('/auth/login', { username, password });
  return response.data;
};

// ── Users ─────────────────────────────────────────────────────────────────────

export const getAllUsers = async () => {
  const response = await api.get('/users');
  return response.data;
};

export const createUser = async (user) => {
  const response = await api.post('/users', user);
  return response.data;
};

export const deleteUser = async (id) => {
  await api.delete(`/users/${id}`);
};

// ── Equipment ─────────────────────────────────────────────────────────────────

export const getAllEquipment = async () => {
  const response = await api.get('/equipment');
  return response.data;
};

export const getEquipmentById = async (id) => {
  const response = await api.get(`/equipment/${id}`);
  return response.data;
};

export const createEquipment = async (equipment) => {
  const response = await api.post('/equipment', equipment);
  return response.data;
};

export const updateEquipment = async (id, equipment) => {
  const response = await api.put(`/equipment/${id}`, equipment);
  return response.data;
};

export const deleteEquipment = async (id) => {
  await api.delete(`/equipment/${id}`);
};

export const getLowStockEquipment = async () => {
  const response = await api.get('/equipment/low-stock');
  return response.data;
};

// ── Categories ────────────────────────────────────────────────────────────────

export const getAllCategories = async () => {
  const response = await api.get('/categories');
  return response.data;
};

export const createCategory = async (category) => {
  const response = await api.post('/categories', category);
  return response.data;
};

export const deleteCategory = async (id) => {
  await api.delete(`/categories/${id}`);
};

// ── Events ────────────────────────────────────────────────────────────────────

export const getAllEvents = async () => {
  const response = await api.get('/events');
  return response.data;
};

export const getEventById = async (id) => {
  const response = await api.get(`/events/${id}`);
  return response.data;
};

export const createEvent = async (event) => {
  const response = await api.post('/events', event);
  return response.data;
};

export const updateEvent = async (id, event) => {
  const response = await api.put(`/events/${id}`, event);
  return response.data;
};

export const deleteEvent = async (id) => {
  await api.delete(`/events/${id}`);
};

// ── Allocations ───────────────────────────────────────────────────────────────

export const allocateEquipment = async (eventId, equipmentId, quantityAllocated) => {
  const response = await api.post('/allocations', { eventId, equipmentId, quantityAllocated });
  return response.data;
};

export const deployEquipment = async (allocationId) => {
  await api.post(`/allocations/${allocationId}/deploy`);
};

export const returnEquipment = async (allocationId) => {
  await api.post(`/allocations/${allocationId}/return`);
};

export const getAllocationsByEvent = async (eventId) => {
  const response = await api.get(`/allocations/event/${eventId}`);
  return response.data;
};

// ── Reports (Admin only) ──────────────────────────────────────────────────────

export const getReportSummary = async () => {
  const response = await api.get('/reports/summary');
  return response.data;
};

export const getEquipmentReport = async () => {
  const response = await api.get('/reports/equipment');
  return response.data;
};

export const getEventsReport = async () => {
  const response = await api.get('/reports/events');
  return response.data;
};

export const getAllocationsReport = async () => {
  const response = await api.get('/reports/allocations');
  return response.data;
};
