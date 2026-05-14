import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { createEvent, updateEvent, getEventById } from '../api/apiService.js';

// empty form shape — used for initialisation and after a successful create
const EMPTY_FORM = {
  name: '', venue: '', eventDate: '', description: '', status: 'PLANNED',
  clientName: '', clientPhone: '', clientEmail: '',
};

export default function EventForm() {
  const { id } = useParams();
  const isEditing = Boolean(id);
  const navigate = useNavigate();
  const [formData, setFormData] = useState(EMPTY_FORM);
  const [error, setError]       = useState('');
  const [saving, setSaving]     = useState(false);

  // fetch data on component mount — only runs if we are in edit mode
  useEffect(() => {
    if (!isEditing) return;
    const load = async () => {
      try {
        // populate form fields with existing event data
        const ev = await getEventById(id);
        setFormData({
          name: ev.name, venue: ev.venue, eventDate: ev.eventDate,
          description: ev.description ?? '', status: ev.status,
          clientName: ev.clientName ?? '', clientPhone: ev.clientPhone ?? '',
          clientEmail: ev.clientEmail ?? '',
        });
      } catch {
        // show error state if API call fails
        setError('Failed to load event.');
      }
    };
    load();
  }, [id, isEditing]);

  // single handler for all text/select/date inputs — updates only the changed field
  const handleChange = e => {
    const { name, value } = e.target;
    setFormData(p => ({ ...p, [name]: value }));
  };

  const handleSubmit = async e => {
    e.preventDefault();

    // validate input before making the API call — check required fields are not empty
    if (!formData.name.trim()) {
      setError('Event name is required.');
      return;
    }
    if (!formData.venue.trim()) {
      setError('Venue is required.');
      return;
    }
    if (!formData.eventDate) {
      setError('Event date is required.');
      return;
    }

    setSaving(true);
    setError('');
    try {
      if (isEditing) {
        await updateEvent(id, formData);
      } else {
        await createEvent(formData);
      }
      // navigate back to the events list after successful save
      navigate('/events');
    } catch (err) {
      // show error state if API call fails — e.g. validation error from the backend
      setError(err.response?.data?.message ?? 'Failed to save event.');
    } finally { setSaving(false); }
  };

  return (
    <div className="form-container">
      <h1 className="page-title">{isEditing ? 'Edit Event' : 'Create Event'}</h1>
      {error && <div className="error-message">{error}</div>}
      <form onSubmit={handleSubmit} className="form">

        <div className="form-group">
          <label htmlFor="name">Event Name *</label>
          <input id="name" name="name" type="text" value={formData.name}
            onChange={handleChange} required placeholder="e.g. Kigali Business Summit" />
        </div>
        <div className="form-group">
          <label htmlFor="venue">Venue *</label>
          <input id="venue" name="venue" type="text" value={formData.venue}
            onChange={handleChange} required placeholder="e.g. Kigali Convention Centre" />
        </div>
        <div className="form-group">
          <label htmlFor="eventDate">Event Date *</label>
          <input id="eventDate" name="eventDate" type="date" value={formData.eventDate}
            onChange={handleChange} required />
        </div>
        <div className="form-group">
          <label htmlFor="description">Description</label>
          <textarea id="description" name="description" value={formData.description}
            onChange={handleChange} rows={3} placeholder="Brief description of the event" />
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

        {/* Client Details — optional but recorded for contact and billing purposes */}
        <h4 style={{ margin: '16px 0 8px', color: '#42103C', borderBottom: '1px solid #e0d5df', paddingBottom: '6px' }}>
          Client Details
        </h4>
        <div className="form-group">
          <label htmlFor="clientName">Client Name</label>
          <input id="clientName" name="clientName" type="text" value={formData.clientName}
            onChange={handleChange} placeholder="e.g. John Doe" />
        </div>
        <div className="form-row">
          <div className="form-group">
            <label htmlFor="clientPhone">Client Phone</label>
            <input id="clientPhone" name="clientPhone" type="text" value={formData.clientPhone}
              onChange={handleChange} placeholder="e.g. +250 788 000 000" />
          </div>
          <div className="form-group">
            <label htmlFor="clientEmail">Client Email</label>
            <input id="clientEmail" name="clientEmail" type="email" value={formData.clientEmail}
              onChange={handleChange} placeholder="e.g. client@example.com" />
          </div>
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
