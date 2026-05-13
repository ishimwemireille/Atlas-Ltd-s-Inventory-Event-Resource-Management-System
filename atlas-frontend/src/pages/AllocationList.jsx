import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  getAllocationsByEvent,
  deployEquipment,
  returnEquipment,
  getEventById,
} from '../api/apiService.js';
import StatusBadge from '../components/StatusBadge.jsx';

export default function AllocationList() {
  const { eventId } = useParams();
  const navigate = useNavigate();

  const [allocations, setAllocations] = useState([]);
  const [eventName, setEventName] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadAllocations = async () => {
    try {
      const [allocationData, eventData] = await Promise.all([
        getAllocationsByEvent(eventId),
        getEventById(eventId),
      ]);
      setAllocations(allocationData);
      setEventName(eventData.name);
    } catch (err) {
      setError('Failed to load allocations.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadAllocations(); }, [eventId]);

  const handleDeploy = async (allocationId) => {
    try {
      // STATE PATTERN: RESERVED → DEPLOYED
      await deployEquipment(allocationId);
      await loadAllocations();
    } catch (err) {
      setError('Failed to deploy equipment.');
    }
  };

  const handleReturn = async (allocationId) => {
    try {
      // STATE PATTERN: DEPLOYED → IN_STOCK
      await returnEquipment(allocationId);
      await loadAllocations();
    } catch (err) {
      setError('Failed to return equipment.');
    }
  };

  if (loading) return <div className="loading">Loading allocations...</div>;

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Allocations</h1>
          <p className="page-subtitle">{eventName}</p>
        </div>
        <div>
          <button className="btn btn-secondary" onClick={() => navigate('/allocations')}>
            + New Allocation
          </button>
          <button className="btn btn-secondary" onClick={() => navigate('/events')} style={{ marginLeft: '8px' }}>
            ← Events
          </button>
        </div>
      </div>

      {error && <div className="error-message">{error}</div>}

      {allocations.length === 0 ? (
        <div className="empty-state">
          No equipment allocated to this event yet.{' '}
          <button className="link-button" onClick={() => navigate('/allocations')}>
            Allocate now →
          </button>
        </div>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>#</th>
              <th>Equipment</th>
              <th>Category</th>
              <th>Qty Allocated</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {allocations.map((allocation, index) => (
              <tr key={allocation.id}>
                <td>{index + 1}</td>
                <td><strong>{allocation.equipment?.name}</strong></td>
                <td>{allocation.equipment?.category?.name ?? '—'}</td>
                <td>{allocation.quantityAllocated}</td>
                <td><StatusBadge status={allocation.allocationStatus} /></td>
                <td>
                  {allocation.allocationStatus === 'RESERVED' && (
                    <button
                      className="btn btn-sm btn-primary"
                      onClick={() => handleDeploy(allocation.id)}
                    >
                      Deploy
                    </button>
                  )}
                  {allocation.allocationStatus === 'DEPLOYED' && (
                    <button
                      className="btn btn-sm btn-secondary"
                      onClick={() => handleReturn(allocation.id)}
                    >
                      Return
                    </button>
                  )}
                  {allocation.allocationStatus === 'RETURNED' && (
                    <span style={{ color: '#6c757d', fontSize: '13px' }}>Returned ✓</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <div className="pattern-note">
        <strong>State Pattern in action:</strong> Deploy button triggers RESERVED → DEPLOYED.
        Return button triggers DEPLOYED → IN_STOCK. Each transition is enforced server-side
        through <code>equipment.deploy()</code> and <code>equipment.returnStock()</code>.
      </div>
    </div>
  );
}
