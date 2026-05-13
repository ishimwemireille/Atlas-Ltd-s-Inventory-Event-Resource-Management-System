import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getAllEquipment, getAllEvents, getLowStockEquipment } from '../api/apiService.js';
import StatusBadge from '../components/StatusBadge.jsx';

export default function Dashboard() {
  const [equipmentCount, setEquipmentCount] = useState(0);
  const [eventCount, setEventCount] = useState(0);
  const [lowStockItems, setLowStockItems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadSummary = async () => {
      try {
        const [equipment, events, lowStock] = await Promise.all([
          getAllEquipment(),
          getAllEvents(),
          getLowStockEquipment(),
        ]);
        setEquipmentCount(equipment.length);
        setEventCount(events.length);
        setLowStockItems(lowStock);
      } catch (error) {
        console.error('Failed to load dashboard data:', error);
      } finally {
        setLoading(false);
      }
    };
    loadSummary();
  }, []);

  if (loading) return <div className="loading">Loading dashboard...</div>;

  return (
    <div>
      <h1 className="page-title">Dashboard</h1>
      <p className="page-subtitle">Atlas Turbo LTD — Inventory & Event Resource Management</p>

      <div className="stat-cards">
        <div className="stat-card">
          <div className="stat-number">{equipmentCount}</div>
          <div className="stat-label">Equipment Items</div>
          <Link to="/equipment" className="stat-link">View all →</Link>
        </div>
        <div className="stat-card">
          <div className="stat-number">{eventCount}</div>
          <div className="stat-label">Events</div>
          <Link to="/events" className="stat-link">View all →</Link>
        </div>
        <div className="stat-card stat-card-warning">
          <div className="stat-number">{lowStockItems.length}</div>
          <div className="stat-label">Low Stock Alerts</div>
          <Link to="/equipment" className="stat-link">Check stock →</Link>
        </div>
      </div>

      {lowStockItems.length > 0 && (
        <div className="alert-box">
          <h3 className="alert-title">⚠ Low Stock Alerts</h3>
          <table className="table">
            <thead>
              <tr>
                <th>Equipment</th>
                <th>Available</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {lowStockItems.map((item) => (
                <tr key={item.id}>
                  <td>{item.name}</td>
                  <td>{item.availableQuantity}</td>
                  <td><StatusBadge status={item.status} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <div className="quick-actions">
        <h3>Quick Actions</h3>
        <div className="action-buttons">
          <Link to="/equipment/new" className="btn btn-primary">+ Add Equipment</Link>
          <Link to="/events/new" className="btn btn-secondary">+ Create Event</Link>
          <Link to="/allocations" className="btn btn-secondary">Allocate Equipment</Link>
        </div>
      </div>
    </div>
  );
}
