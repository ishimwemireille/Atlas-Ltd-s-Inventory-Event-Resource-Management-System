import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getAllEquipment, deleteEquipment } from '../api/apiService.js';
import StatusBadge from '../components/StatusBadge.jsx';

export default function EquipmentList() {
  const [equipmentList, setEquipmentList] = useState([]);
  const [loading, setLoading]             = useState(true);
  const [error, setError]                 = useState('');
  const [search, setSearch]               = useState('');
  const [statusFilter, setStatusFilter]   = useState('ALL');
  const navigate = useNavigate();

  // load all equipment from the API
  const load = async () => {
    try { setEquipmentList(await getAllEquipment()); }
    catch {
      // show error state if API call fails
      setError('Failed to load equipment. Is the backend running?');
    }
    finally { setLoading(false); }
  };

  // fetch data on component mount
  useEffect(() => { load(); }, []);

  const handleDelete = async (id, name) => {
    if (!window.confirm(`Delete "${name}"? This cannot be undone.`)) return;
    try {
      await deleteEquipment(id);
      // optimistically remove the deleted item from local state — avoids a full reload
      setEquipmentList(p => p.filter(i => i.id !== id));
    }
    catch {
      // show error state if API call fails
      setError('Failed to delete equipment.');
    }
  };

  // all status values shown in the filter dropdown
  const statuses = ['ALL', 'IN_STOCK', 'RESERVED', 'DEPLOYED', 'RETURNED'];

  // client-side filtering — keeps the list reactive to both search text and status filter
  const filtered = equipmentList.filter(item => {
    const matchSearch = item.name.toLowerCase().includes(search.toLowerCase()) ||
      (item.category?.name || '').toLowerCase().includes(search.toLowerCase());
    const matchStatus = statusFilter === 'ALL' || item.status === statusFilter;
    return matchSearch && matchStatus;
  });

  if (loading) return <div className="loading">Loading equipment...</div>;

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Equipment</h1>
        <Link to="/equipment/new" className="btn btn-primary">+ Add Equipment</Link>
      </div>

      {error && <div className="error-message">{error}</div>}

      {/* search and filter controls */}
      <div style={{ display:'flex', gap:'12px', flexWrap:'wrap', margin:'16px 0' }}>
        <input type="text" placeholder="Search by name or category…" value={search}
          onChange={e => setSearch(e.target.value)}
          style={{ padding:'8px 12px', borderRadius:'6px', border:'1px solid #ccc', fontSize:'14px', minWidth:'260px' }} />
        <select value={statusFilter} onChange={e => setStatusFilter(e.target.value)}
          style={{ padding:'8px 12px', borderRadius:'6px', border:'1px solid #ccc', fontSize:'14px' }}>
          {statuses.map(s => <option key={s} value={s}>{s === 'ALL' ? 'All Statuses' : s.replace('_',' ')}</option>)}
        </select>
      </div>

      {filtered.length === 0 ? (
        <div className="empty-state">No equipment found.</div>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>#</th><th>Name</th><th>Category</th>
              <th>Total Qty</th><th>Available</th>
              <th>Selling Price/Unit</th><th>Status</th><th>Recorded On</th><th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((item, i) => (
              <tr key={item.id}>
                <td>{i + 1}</td>
                <td><strong>{item.name}</strong></td>
                <td>{item.category?.name ?? '—'}</td>
                <td>{item.totalQuantity}</td>
                <td>{item.availableQuantity}</td>
                <td>{item.sellingPricePerUnit ? `RWF ${Number(item.sellingPricePerUnit).toLocaleString()}` : '—'}</td>
                <td><StatusBadge status={item.status} /></td>
                <td style={{ fontSize:'12px', color:'#666' }}>
                  {item.createdAt ? new Date(item.createdAt).toLocaleDateString('en-GB', { day:'2-digit', month:'short', year:'numeric' }) : '—'}
                </td>
                <td>
                  <button className="btn btn-sm btn-secondary" onClick={() => navigate(`/equipment/edit/${item.id}`)}>Edit</button>
                  <button className="btn btn-sm btn-danger" onClick={() => handleDelete(item.id, item.name)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
