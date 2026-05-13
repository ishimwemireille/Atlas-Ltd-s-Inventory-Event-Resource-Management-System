import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getAllEvents, deleteEvent } from '../api/apiService.js';
import StatusBadge from '../components/StatusBadge.jsx';

export default function EventList() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const loadEvents = async () => {
    try {
      const data = await getAllEvents();
      setEvents(data);
    } catch (err) {
      setError('Failed to load events. Is the backend running?');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadEvents(); }, []);

  const handleDelete = async (id, name) => {
    if (!window.confirm(`Delete event "${name}"?`)) return;
    try {
      await deleteEvent(id);
      setEvents((prev) => prev.filter((event) => event.id !== id));
    } catch (err) {
      setError('Failed to delete event.');
    }
  };

  if (loading) return <div className="loading">Loading events...</div>;

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Events</h1>
        <Link to="/events/new" className="btn btn-primary">+ Create Event</Link>
      </div>

      {error && <div className="error-message">{error}</div>}

      {events.length === 0 ? (
        <div className="empty-state">No events found. Create your first event above.</div>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>#</th>
              <th>Event Name</th>
              <th>Venue</th>
              <th>Date</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {events.map((event, index) => (
              <tr key={event.id}>
                <td>{index + 1}</td>
                <td><strong>{event.name}</strong></td>
                <td>{event.venue}</td>
                <td>{event.eventDate}</td>
                <td><StatusBadge status={event.status} /></td>
                <td>
                  <button
                    className="btn btn-sm btn-secondary"
                    onClick={() => navigate(`/events/edit/${event.id}`)}
                  >
                    Edit
                  </button>
                  <button
                    className="btn btn-sm btn-info"
                    onClick={() => navigate(`/allocations/event/${event.id}`)}
                  >
                    Allocations
                  </button>
                  <button
                    className="btn btn-sm btn-danger"
                    onClick={() => handleDelete(event.id, event.name)}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
