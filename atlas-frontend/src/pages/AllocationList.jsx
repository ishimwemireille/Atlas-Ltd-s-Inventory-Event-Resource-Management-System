import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getAllocationsByEvent, deployEquipment, returnEquipment, getEventById } from '../api/apiService.js';
import StatusBadge from '../components/StatusBadge.jsx';

// available return condition options — matches the ReturnCondition enum on the backend
const CONDITION_OPTIONS = [
  { value: 'GOOD', label: 'Good — No damage' },
  { value: 'DAMAGED', label: 'Damaged — Needs repair' },
  { value: 'MISSING_PARTS', label: 'Missing Parts' },
];

export default function AllocationList() {
  const { eventId } = useParams();
  const navigate = useNavigate();

  const [allocations, setAllocations] = useState([]);
  const [eventName, setEventName]     = useState('');
  const [loading, setLoading]         = useState(true);
  const [error, setError]             = useState('');

  // return condition modal state
  const [returnModal, setReturnModal]     = useState(null); // allocation being returned
  const [returnData, setReturnData]       = useState({ condition: 'GOOD', damageNotes: '' });

  // fetch allocations and event name together to avoid a second round trip
  const load = async () => {
    try {
      const [allocs, ev] = await Promise.all([getAllocationsByEvent(eventId), getEventById(eventId)]);
      setAllocations(allocs);
      setEventName(ev.name);
    } catch {
      // show error state if API call fails
      setError('Failed to load allocations.');
    }
    finally { setLoading(false); }
  };

  // fetch data on component mount — re-fetches whenever eventId changes in the URL
  useEffect(() => { load(); }, [eventId]);

  const handleDeploy = async (id) => {
    try {
      // STATE PATTERN: trigger RESERVED → DEPLOYED transition via the backend
      await deployEquipment(id);
      // refresh list to reflect new status
      await load();
    }
    catch { setError('Failed to deploy equipment.'); }
  };

  const openReturn = (allocation) => {
    // initialise modal with default GOOD condition before showing it
    setReturnModal(allocation);
    setReturnData({ condition: 'GOOD', damageNotes: '' });
  };

  const handleReturn = async () => {
    try {
      // STATE PATTERN: trigger DEPLOYED → IN_STOCK transition with condition info
      await returnEquipment(returnModal.id, returnData.condition, returnData.damageNotes);
      // close modal and refresh list after successful return
      setReturnModal(null);
      await load();
    } catch { setError('Failed to return equipment.'); }
  };

  // map return condition values to colour-coded badge styles
  const conditionBadge = (c) => {
    if (!c) return null;
    const map = { GOOD: ['#1B5E20','#e8f5e9'], DAMAGED: ['#B71C1C','#FFEBEE'], MISSING_PARTS: ['#E65100','#FFF3E0'] };
    const [color, bg] = map[c] || ['#333','#eee'];
    return <span style={{ background: bg, color, padding:'1px 7px', borderRadius:'10px', fontSize:'11px', fontWeight:700 }}>{c.replace('_',' ')}</span>;
  };

  // format a datetime string into a readable short date
  const fmt = dt => dt ? new Date(dt).toLocaleDateString('en-GB', { day:'2-digit', month:'short', year:'numeric' }) : '—';

  if (loading) return <div className="loading">Loading allocations...</div>;

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Allocations</h1>
          <p className="page-subtitle">{eventName}</p>
        </div>
        <div>
          <button className="btn btn-secondary" onClick={() => navigate('/allocations')}>+ New Allocation</button>
          <button className="btn btn-secondary" onClick={() => navigate('/events')} style={{ marginLeft:'8px' }}>← Events</button>
        </div>
      </div>

      {error && <div className="error-message">{error}</div>}

      {allocations.length === 0 ? (
        <div className="empty-state">
          No equipment allocated to this event yet.{' '}
          <button className="link-button" onClick={() => navigate('/allocations')}>Allocate now →</button>
        </div>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>#</th><th>Equipment</th><th>Category</th><th>Qty</th>
              <th>Rental Price/Unit</th><th>Status</th><th>Deployed On</th><th>Returned On</th><th>Condition</th><th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {allocations.map((a, i) => (
              <tr key={a.id}>
                <td>{i + 1}</td>
                <td><strong>{a.equipment?.name}</strong></td>
                <td>{a.equipment?.category?.name ?? '—'}</td>
                <td>{a.quantityAllocated}</td>
                <td style={{ fontSize:'13px' }}>
                  {a.rentalPricePerUnit ? `RWF ${Number(a.rentalPricePerUnit).toLocaleString()}` : '—'}
                </td>
                <td><StatusBadge status={a.allocationStatus} /></td>
                <td style={{ fontSize:'12px', color:'#555' }}>{fmt(a.deployedAt)}</td>
                <td style={{ fontSize:'12px', color:'#555' }}>{fmt(a.returnedAt)}</td>
                <td>{conditionBadge(a.returnCondition)}</td>
                <td>
                  {/* show Deploy button only while allocation is in RESERVED state */}
                  {a.allocationStatus === 'RESERVED' && (
                    <button className="btn btn-sm btn-primary" onClick={() => handleDeploy(a.id)}>Deploy</button>
                  )}
                  {/* show Return button only while allocation is in DEPLOYED state */}
                  {a.allocationStatus === 'DEPLOYED' && (
                    <button className="btn btn-sm btn-secondary" onClick={() => openReturn(a)}>Return</button>
                  )}
                  {a.allocationStatus === 'RETURNED' && (
                    <span style={{ color:'#6c757d', fontSize:'13px' }}>Returned ✓</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {/* Return Condition Modal — shown when user clicks Return on a DEPLOYED allocation */}
      {returnModal && (
        <div style={{ position:'fixed', inset:0, background:'rgba(0,0,0,0.45)', display:'flex', alignItems:'center', justifyContent:'center', zIndex:1000 }}>
          <div style={{ background:'#fff', borderRadius:'12px', padding:'32px', width:'100%', maxWidth:'420px', boxShadow:'0 8px 32px rgba(0,0,0,0.18)' }}>
            <h3 style={{ marginBottom:'16px', color:'#1a2c4e' }}>Return Equipment — {returnModal.equipment?.name}</h3>
            <div className="form-group">
              <label>Condition on Return *</label>
              <select value={returnData.condition}
                onChange={e => setReturnData(p => ({ ...p, condition: e.target.value }))}
                style={{ width:'100%', padding:'8px', borderRadius:'6px', border:'1px solid #ccc' }}>
                {CONDITION_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
              </select>
            </div>
            {/* only show damage notes textarea when condition is not GOOD */}
            {returnData.condition !== 'GOOD' && (
              <div className="form-group">
                <label>Damage / Notes</label>
                <textarea rows={3} value={returnData.damageNotes} placeholder="Describe the damage or missing parts…"
                  onChange={e => setReturnData(p => ({ ...p, damageNotes: e.target.value }))}
                  style={{ width:'100%', padding:'8px', borderRadius:'6px', border:'1px solid #ccc' }} />
              </div>
            )}
            <div style={{ display:'flex', gap:'12px', marginTop:'8px' }}>
              <button className="btn btn-primary" onClick={handleReturn} style={{ flex:1 }}>Confirm Return</button>
              <button className="btn btn-secondary" onClick={() => setReturnModal(null)} style={{ flex:1 }}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
