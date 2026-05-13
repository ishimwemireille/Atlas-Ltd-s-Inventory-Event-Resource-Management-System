import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import Navbar from './components/Navbar.jsx';
import Login from './pages/Login.jsx';
import Dashboard from './pages/Dashboard.jsx';
import EquipmentList from './pages/EquipmentList.jsx';
import EquipmentForm from './pages/EquipmentForm.jsx';
import EventList from './pages/EventList.jsx';
import EventForm from './pages/EventForm.jsx';
import AllocationForm from './pages/AllocationForm.jsx';
import AllocationList from './pages/AllocationList.jsx';
import UserManagement from './pages/UserManagement.jsx';
import Reports from './pages/Reports.jsx';
import Sales from './pages/Sales.jsx';

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/*"
          element={
            <ProtectedRoute>
              <div className="app-container">
                <Navbar />
                <main className="main-content">
                  <Routes>
                    <Route path="/" element={<Dashboard />} />
                    <Route path="/equipment" element={<EquipmentList />} />
                    <Route path="/equipment/new" element={<EquipmentForm />} />
                    <Route path="/equipment/edit/:id" element={<EquipmentForm />} />
                    <Route path="/events" element={<EventList />} />
                    <Route path="/events/new" element={<EventForm />} />
                    <Route path="/events/edit/:id" element={<EventForm />} />
                    <Route path="/allocations" element={<AllocationForm />} />
                    <Route path="/allocations/event/:eventId" element={<AllocationList />} />
                    <Route path="/users" element={<UserManagement />} />
                    <Route path="/sales" element={<Sales />} />
                    <Route path="/reports" element={<Reports />} />
                    <Route path="*" element={<Navigate to="/" replace />} />
                  </Routes>
                </main>
              </div>
            </ProtectedRoute>
          }
        />
      </Routes>
    </AuthProvider>
  );
}
