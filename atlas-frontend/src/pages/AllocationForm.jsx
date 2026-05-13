import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAllEvents, getAllEquipment, allocateEquipment } from '../api/apiService.js';
import StatusBadge from '../components/StatusBadge.jsx';

export default function AllocationForm() {
  const [events, setEvents] = useState([]);
  const [equipmentList, setEquipmentList] = useState([]);
  const [selectedEventId, setSelectedEventId] = useState('');
  const [selectedEquipmentId, setSelectedEquipmentId] = useState('');
  const [quantity, setQuantity] = useState(1);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [saving, setSaving] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const loadData = async () => {
      try {
        const [eventsData, equipmentData] = await Promise.all([
          getAllEvents(),
          getAllEquipment(),
        ]);
        setEvents(eventsData);
        setEquipmentList(equipmentData);
      } catch (err) {
        setError('Failed to load data. Is the backend running?');
      }
    };
    loadData();
  }, []);

  const selectedEquipment = equipmentList.find(
    (item) => item.id === Number(selectedEquipmentId)
  );

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      await allocateEquipment(
        Number(selectedEventId),
        Number(selectedEquipmentId),
        Number(quantity)
      );
      setSuccess('Equipment reserved successfully!');
      setSelectedEquipmentId('');
      setQuantity(1);
    } catch (err) {
      setError(err.response?.data?.message ?? 'Failed to allocate equipment.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="form-container">
      <h1 className="page-title">Allocate Equipment to Event</h1>
      <p className="page-subtitle">
        Reserving equipment triggers the <strong>State Pattern</strong>: IN_STOCK → RESERVED
      </p>

      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      <form onSubmit={handleSubmit} className="form">
        <div className="form-group">
          <label htmlFor="eventId">Select Event *</label>
          <select
            id="eventId"
            value={selectedEventId}
            onChange={(e) => setSelectedEventId(e.target.value)}
            required
          >
            <option value="">— Choose an event —</option>
            {events.map((event) => (
              <option key={event.id} value={event.id}>
                {event.name} — {event.venue} ({event.eventDate})
              </option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="equipmentId">Select Equipment *</label>
          <select
            id="equipmentId"
            value={selectedEquipmentId}
            onChange={(e) => setSelectedEquipmentId(e.target.value)}
            required
          >
            <option value="">— Choose equipment —</option>
            {equipmentList.map((item) => (
              <option
                key={item.id}
                value={item.id}
                disabled={item.availableQuantity === 0}
              >
                {item.name} — Available: {item.availableQuantity}
              </option>
            ))}
          </select>

          {selectedEquipment && (
            <div className="equipment-info">
              <StatusBadge status={selectedEquipment.status} />
              <span style={{ marginLeft: '8px' }}>
                {selectedEquipment.availableQuantity} of {selectedEquipment.totalQuantity} available
              </span>
            </div>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="quantity">Quantity to Reserve *</label>
          <input
            id="quantity"
            type="number"
            min="1"
            max={selectedEquipment?.availableQuantity ?? 999}
            value={quantity}
            onChange={(e) => setQuantity(e.target.value)}
            required
          />
        </div>

        <div className="form-actions">
          <button type="submit" className="btn btn-primary" disabled={saving}>
            {saving ? 'Reserving...' : 'Reserve Equipment'}
          </button>
        </div>
      </form>

      {selectedEventId && (
        <div style={{ marginTop: '24px' }}>
          <button
            className="btn btn-secondary"
            onClick={() => navigate(`/allocations/event/${selectedEventId}`)}
          >
            View Allocations for This Event →
          </button>
        </div>
      )}
    </div>
  );
}
