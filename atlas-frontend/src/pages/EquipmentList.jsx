import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getAllEquipment, deleteEquipment } from '../api/apiService.js';
import StatusBadge from '../components/StatusBadge.jsx';

export default function EquipmentList() {
  const [equipmentList, setEquipmentList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const loadEquipment = async () => {
    try {
      const data = await getAllEquipment();
      setEquipmentList(data);
    } catch (err) {
      setError('Failed to load equipment. Is the backend running?');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadEquipment(); }, []);

  const handleDelete = async (id, name) => {
    if (!window.confirm(`Delete "${name}"? This cannot be undone.`)) return;
    try {
      await deleteEquipment(id);
      setEquipmentList((prev) => prev.filter((item) => item.id !== id));
    } catch (err) {
      setError('Failed to delete equipment.');
    }
  };

  if (loading) return <div className="loading">Loading equipment...</div>;

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Equipment</h1>
        <Link to="/equipment/new" className="btn btn-primary">+ Add Equipment</Link>
      </div>

      {error && <div className="error-message">{error}</div>}

      {equipmentList.length === 0 ? (
        <div className="empty-state">No equipment found. Add your first item above.</div>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>#</th>
              <th>Name</th>
              <th>Category</th>
              <th>Total Qty</th>
              <th>Available</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {equipmentList.map((item, index) => (
              <tr key={item.id}>
                <td>{index + 1}</td>
                <td><strong>{item.name}</strong></td>
                <td>{item.category?.name ?? '—'}</td>
                <td>{item.totalQuantity}</td>
                <td>{item.availableQuantity}</td>
                <td><StatusBadge status={item.status} /></td>
                <td>
                  <button
                    className="btn btn-sm btn-secondary"
                    onClick={() => navigate(`/equipment/edit/${item.id}`)}
                  >
                    Edit
                  </button>
                  <button
                    className="btn btn-sm btn-danger"
                    onClick={() => handleDelete(item.id, item.name)}
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
