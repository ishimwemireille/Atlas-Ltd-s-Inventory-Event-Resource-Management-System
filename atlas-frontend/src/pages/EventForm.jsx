import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { createEvent, updateEvent, getEventById } from '../api/apiService.js';

const EMPTY_FORM = {
  name: '',
  venue: '',
  eventDate: '',
  description: '',
  status: 'PLANNED',
};

export default function EventForm() {
  const { id } = useParams();
  const isEditing = Boolean(id);
  const navigate = useNavigate();

  const [formData, setFormData] = useState(EMPTY_FORM);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!isEditing) return;
    const loadEvent = async () => {
      try {
        const event = await getEventById(id);
        setFormData({
          name: event.name,
          venue: event.venue,
          eventDate: event.eventDate,
          description: event.description ?? '',
          status: event.status,
        });
      } catch (err) {
        setError('Failed to load event.');
      }
    };
    loadEvent();
  }, [id, isEditing]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      if (isEditing) {
        await updateEvent(id, formData);
      } else {
        await createEvent(formData);
      }
      navigate('/events');
    } catch (err) {
      setError(err.response?.data?.message ?? 'Failed to save event.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="form-container">
      <h1 className="page-title">{isEditing ? 'Edit Event' : 'Create Event'}</h1>

      {error && <div className="error-message">{error}</div>}

      <form onSubmit={handleSubmit} className="form">
        <div className="form-group">
          <label htmlFor="name">Event Name *</label>
          <input
            id="name"
            name="name"
            type="text"
            value={formData.name}
            onChange={handleChange}
            required
            placeholder="e.g. Kigali Business Summit 2025"
          />
        </div>

        <div className="form-group">
          <label htmlFor="venue">Venue *</label>
          <input
            id="venue"
            name="venue"
            type="text"
            value={formData.venue}
            onChange={handleChange}
            required
            placeholder="e.g. Kigali Convention Centre"
          />
        </div>

        <div className="form-group">
          <label htmlFor="eventDate">Event Date *</label>
          <input
            id="eventDate"
            name="eventDate"
            type="date"
            value={formData.eventDate}
            onChange={handleChange}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            name="description"
            value={formData.description}
            onChange={handleChange}
            rows={3}
            placeholder="Brief description of the event"
          />
        </div>

        <div className="form-group">
          <label htmlFor="status">Status</label>
          <select id="status" name="status" value={formData.status} onChange={handleChange}>
            <option value="PLANNED">Planned</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>

        <div className="form-actions">
          <button type="submit" className="btn btn-primary" disabled={saving}>
            {saving ? 'Saving...' : isEditing ? 'Update Event' : 'Create Event'}
          </button>
          <button type="button" className="btn btn-secondary" onClick={() => navigate('/events')}>
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
