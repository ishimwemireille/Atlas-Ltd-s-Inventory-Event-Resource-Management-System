import React, { useEffect, useState } from 'react';
import { getAllSales, recordSale, getAllEquipment } from '../api/apiService.js';

const EMPTY_FORM = {
  equipmentId: '',
  quantitySold: 1,
  saleDate: new Date().toISOString().split('T')[0],
  buyerName: '',
  notes: '',
};

export default function Sales() {
  const [sales, setSales]         = useState([]);
  const [equipment, setEquipment] = useState([]);
  const [formData, setFormData]   = useState(EMPTY_FORM);
  const [error, setError]         = useState('');
  const [success, setSuccess]     = useState('');
  const [saving, setSaving]       = useState(false);
  const [loading, setLoading]     = useState(true);

  const load = async () => {
    try {
      const [s, eq] = await Promise.all([getAllSales(), getAllEquipment()]);
      setSales(s);
      setEquipment(eq);
    } catch {
      setError('Failed to load data.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      await recordSale({
        equipment: { id: Number(formData.equipmentId) },
        quantitySold: Number(formData.quantitySold),
        saleDate: formData.saleDate,
        buyerName: formData.buyerName || null,
        notes: formData.notes || null,
      });
      const eq = equipment.find(e => e.id === Number(formData.equipmentId));
      setSuccess(`Sale recorded: ${formData.quantitySold} unit(s) of "${eq?.name}".`);
      setFormData(EMPTY_FORM);
      await load();
    } catch (err) {
      setError(err.response?.data?.message ?? 'Failed to record sale.');
    } finally {
      setSaving(false);
    }
  };

  const fmt = (dateStr) => {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
  };

  if (loading) return <div className="page-loading">Loading…</div>;

  return (
    <div>
      <h1 className="page-title">Sales</h1>
      <p className="page-subtitle">Record equipment sold and permanently removed from inventory.</p>

      {error   && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.6fr', gap: '24px', marginTop: '24px' }}>

        {/* ── Record Sale Form ── */}
        <div className="form-container" style={{ maxWidth: '100%' }}>
          <h3 style={{ marginBottom: '16px', color: '#1a2c4e' }}>Record New Sale</h3>
          <form onSubmit={handleSubmit} className="form" style={{ marginTop: 0 }}>
            <div className="form-group">
              <label htmlFor="equipmentId">Equipment *</label>
              <select id="equipmentId" name="equipmentId" value={formData.equipmentId}
                onChange={handleChange} required>
                <option value="">— Select equipment —</option>
                {equipment.map(eq => (
                  <option key={eq.id} value={eq.id}>
                    {eq.name} (Available: {eq.availableQuantity})
                  </option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label htmlFor="quantitySold">Quantity Sold *</label>
              <input id="quantitySold" name="quantitySold" type="number" min="1"
                value={formData.quantitySold} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label htmlFor="saleDate">Date Sold *</label>
              <input id="saleDate" name="saleDate" type="date"
                value={formData.saleDate} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label htmlFor="buyerName">Buyer Name <span style={{ color: '#888', fontWeight: 400 }}>(optional)</span></label>
              <input id="buyerName" name="buyerName" type="text"
                value={formData.buyerName} onChange={handleChange}
                placeholder="e.g. John Doe" />
            </div>
            <div className="form-group">
              <label htmlFor="notes">Notes <span style={{ color: '#888', fontWeight: 400 }}>(optional)</span></label>
              <textarea id="notes" name="notes" rows={2}
                value={formData.notes} onChange={handleChange}
                placeholder="Any additional details about the sale" />
            </div>
            <button type="submit" className="btn btn-primary" disabled={saving} style={{ width: '100%' }}>
              {saving ? 'Recording...' : 'Record Sale'}
            </button>
          </form>
        </div>

        {/* ── Sales History Table ── */}
        <div>
          <h3 style={{ marginBottom: '16px', color: '#1a2c4e' }}>Sales History</h3>
          {sales.length === 0 ? (
            <div className="empty-state">No sales recorded yet.</div>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Equipment</th>
                  <th>Category</th>
                  <th>Qty Sold</th>
                  <th>Date Sold</th>
                  <th>Buyer</th>
                  <th>Notes</th>
                  <th>Recorded At</th>
                </tr>
              </thead>
              <tbody>
                {sales.map((s, i) => (
                  <tr key={s.id}>
                    <td>{i + 1}</td>
                    <td><strong>{s.equipment?.name}</strong></td>
                    <td>{s.equipment?.category?.name ?? '—'}</td>
                    <td>{s.quantitySold}</td>
                    <td>{fmt(s.saleDate)}</td>
                    <td>{s.buyerName || '—'}</td>
                    <td style={{ maxWidth: '160px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {s.notes || '—'}
                    </td>
                    <td style={{ fontSize: '12px', color: '#666' }}>
                      {s.recordedAt ? new Date(s.recordedAt).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }) : '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}
