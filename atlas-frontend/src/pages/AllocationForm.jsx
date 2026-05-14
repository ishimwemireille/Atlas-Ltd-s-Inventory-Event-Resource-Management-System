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
  const [rentalPricePerUnit, setRentalPricePerUnit] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [saving, setSaving] = useState(false);
  const navigate = useNavigate();

  // fetch data on component mount — both events and equipment are needed to populate dropdowns
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
        // show error state if API call fails
        setError('Failed to load data. Is the backend running?');
      }
    };
    loadData();
  }, []);

  // derive selected equipment details for inline stock display
  const selectedEquipment = equipmentList.find(
    (item) => item.id === Number(selectedEquipmentId)
  );

  const handleSubmit = async (event) => {
    event.preventDefault();

    // validate input before making the API call — ensure selections are complete
    if (!selectedEventId || !selectedEquipmentId) {
      setError('Please select both an event and a piece of equipment.');
      return;
    }
    if (Number(quantity) < 1) {
      setError('Quantity must be at least 1.');
      return;
    }

    setSaving(true);
    setError('');
    setSuccess('');
    try {
      await allocateEquipment(
        Number(selectedEventId),
        Number(selectedEquipmentId),
        Number(quantity),
        rentalPricePerUnit ? Number(rentalPricePerUnit) : null
      );
      setSuccess('Equipment reserved successfully!');
      // clear form after successful submission — ready for the next allocation
      setSelectedEquipmentId('');
      setQuantity(1);
      setRentalPricePerUnit('');
    } catch (err) {
      // show error state if API call fails — e.g. insufficient stock
      setError(err.response?.data?.message ?? 'Failed to allocate equipment.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="form-container">
      <h1 className="page-title">Allocate Equipment to Event</h1>

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
                // disable equipment with no available stock — prevents invalid submissions
                disabled={item.availableQuantity === 0}
              >
                {item.name} — Available: {item.availableQuantity}
              </option>
            ))}
          </select>

          {/* show live stock info for the selected equipment */}
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

        <div className="form-group">
          <label htmlFor="rentalPricePerUnit">Rental Price per Unit (RWF)</label>
          <input
            id="rentalPricePerUnit"
            type="number"
            min="0"
            step="100"
            value={rentalPricePerUnit}
            onChange={(e) => setRentalPricePerUnit(e.target.value)}
            placeholder="e.g. 50000 (optional)"
          />
          {/* hint the rental rate relative to the equipment's base selling price */}
          {selectedEquipment?.sellingPricePerUnit && (
            <small style={{ color: '#666', marginTop: '4px', display: 'block' }}>
              Selling price: RWF {Number(selectedEquipment.sellingPricePerUnit).toLocaleString()} — rental price may differ
            </small>
          )}
        </div>

        <div className="form-actions">
          <button type="submit" className="btn btn-primary" disabled={saving}>
            {saving ? 'Reserving...' : 'Reserve Equipment'}
          </button>
        </div>
      </form>

      {/* quick navigation to view existing allocations for the selected event */}
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
