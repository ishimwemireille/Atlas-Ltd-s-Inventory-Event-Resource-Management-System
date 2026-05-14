import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getAllEquipment, getAllEvents, getLowStockEquipment } from '../api/apiService.js';
import StatusBadge from '../components/StatusBadge.jsx';

export default function Dashboard() {
  const [equipmentCount, setEquipmentCount] = useState(0);
  const [eventCount, setEventCount]         = useState(0);
  const [lowStockItems, setLowStockItems]   = useState([]);
  const [upcomingEvents, setUpcomingEvents] = useState([]);
  const [deployedItems, setDeployedItems]   = useState([]);
  const [loading, setLoading]               = useState(true);

  // fetch data on component mount — load all three data sources in parallel
  useEffect(() => {
    const load = async () => {
      try {
        // parallel requests reduce total wait time on the dashboard
        const [equipment, events, lowStock] = await Promise.all([
          getAllEquipment(),
          getAllEvents(),
          getLowStockEquipment(),
        ]);
        setEquipmentCount(equipment.length);
        setEventCount(events.length);
        // OBSERVER PATTERN: low-stock items are surfaced here after backend publishes LowStockEvent
        setLowStockItems(lowStock);

        // upcoming events: next 60 days, excluding completed or cancelled events
        const today = new Date();
        const future = new Date(); future.setDate(today.getDate() + 60);
        const upcoming = events
          .filter(e => {
            const d = new Date(e.eventDate);
            return d >= today && d <= future && e.status !== 'COMPLETED' && e.status !== 'CANCELLED';
          })
          .sort((a, b) => new Date(a.eventDate) - new Date(b.eventDate))
          // cap at 5 items to keep the dashboard concise
          .slice(0, 5);
        setUpcomingEvents(upcoming);

        // equipment currently deployed — shown in the deployment status panel
        setDeployedItems(equipment.filter(e => e.status === 'DEPLOYED'));
      } catch (err) {
        // show error state if API call fails — log for debugging but don't crash the page
        console.error('Failed to load dashboard data:', err);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  // format a date string or Date object into a readable short date
  const fmt = d => d ? new Date(d).toLocaleDateString('en-GB', { day:'2-digit', month:'short', year:'numeric' }) : '—';

  if (loading) return <div className="loading">Loading dashboard...</div>;

  return (
    <div>
      <h1 className="page-title">Dashboard</h1>
      <p className="page-subtitle">Atlas Turbo LTD — Inventory & Event Resource Management</p>

      {/* summary stat cards — quick overview of system totals */}
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
        <div className="stat-card">
          <div className="stat-number">{deployedItems.length}</div>
          <div className="stat-label">Currently Deployed</div>
          <Link to="/events" className="stat-link">View events →</Link>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px', marginTop: '24px' }}>

        {/* Upcoming Events panel — next 60 days, sorted by date ascending */}
        <div className="alert-box">
          <h3 className="alert-title">Upcoming Events (next 60 days)</h3>
          {upcomingEvents.length === 0 ? (
            <p style={{ color: '#666', fontSize: '14px' }}>No upcoming events.</p>
          ) : (
            <table className="table" style={{ marginTop: '8px' }}>
              <thead>
                <tr><th>Event</th><th>Venue</th><th>Date</th><th>Status</th></tr>
              </thead>
              <tbody>
                {upcomingEvents.map(ev => (
                  <tr key={ev.id}>
                    <td><strong>{ev.name}</strong></td>
                    <td>{ev.venue}</td>
                    <td style={{ whiteSpace: 'nowrap' }}>{fmt(ev.eventDate)}</td>
                    <td><StatusBadge status={ev.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {/* Deployed Equipment panel — equipment currently out at events */}
        <div className="alert-box">
          <h3 className="alert-title">Equipment Currently Deployed</h3>
          {deployedItems.length === 0 ? (
            <p style={{ color: '#666', fontSize: '14px' }}>No equipment currently deployed.</p>
          ) : (
            <table className="table" style={{ marginTop: '8px' }}>
              <thead>
                <tr><th>Equipment</th><th>Category</th><th>Status</th></tr>
              </thead>
              <tbody>
                {deployedItems.map(item => (
                  <tr key={item.id}>
                    <td><strong>{item.name}</strong></td>
                    <td>{item.category?.name ?? '—'}</td>
                    <td><StatusBadge status={item.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {/* Low Stock alert panel — only shown when there are items below the threshold */}
      {lowStockItems.length > 0 && (
        <div className="alert-box" style={{ marginTop: '24px' }}>
          <h3 className="alert-title">⚠ Low Stock Alerts</h3>
          <table className="table">
            <thead>
              <tr><th>Equipment</th><th>Available</th><th>Status</th></tr>
            </thead>
            <tbody>
              {lowStockItems.map(item => (
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

      {/* quick action buttons for common workflows */}
      <div className="quick-actions" style={{ marginTop: '24px' }}>
        <h3>Quick Actions</h3>
        <div className="action-buttons">
          <Link to="/equipment/new" className="btn btn-primary">+ Add Equipment</Link>
          <Link to="/events/new" className="btn btn-secondary">+ Create Event</Link>
          <Link to="/allocations" className="btn btn-secondary">Allocate Equipment</Link>
          <Link to="/maintenance" className="btn btn-secondary">Maintenance</Link>
        </div>
      </div>
    </div>
  );
}
