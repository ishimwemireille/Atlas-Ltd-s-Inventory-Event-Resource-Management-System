import React, { useEffect, useState } from 'react';
import { api } from '../api/apiService.js';

export default function AuditLog() {
  const [logs, setLogs]       = useState([]);
  const [search, setSearch]   = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState('');

  // fetch data on component mount — audit log is read-only, no need to refresh
  useEffect(() => {
    const load = async () => {
      try {
        const res = await api.get('/audit-logs');
        setLogs(res.data);
      } catch {
        // show error state if API call fails — likely a permissions issue (Admin only)
        setError('Failed to load audit logs.');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  // client-side search — filter across user, action, description, and module fields
  const filtered = logs.filter(l =>
    l.performedBy?.toLowerCase().includes(search.toLowerCase()) ||
    l.action?.toLowerCase().includes(search.toLowerCase()) ||
    l.description?.toLowerCase().includes(search.toLowerCase()) ||
    l.entityType?.toLowerCase().includes(search.toLowerCase())
  );

  // map action labels to semantic colours for the badge display
  const actionColor = (action) => {
    const map = {
      DEPLOY: '#0D47A1', RETURN: '#1B5E20', SALE: '#E65100',
      MAINTENANCE_START: '#B71C1C', MAINTENANCE_COMPLETE: '#1B5E20',
      CREATE: '#4A148C', UPDATE: '#1565C0', DELETE: '#B71C1C',
    };
    return map[action] || '#333';
  };

  // format a datetime string into a short locale date-time string
  const fmt = (dt) => dt ? new Date(dt).toLocaleString('en-GB', {
    day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
  }) : '—';

  if (loading) return <div className="page-loading">Loading audit log…</div>;

  return (
    <div>
      <h1 className="page-title">Audit Log</h1>
      <p className="page-subtitle">Complete history of every action performed in the system.</p>

      {error && <div className="error-message">{error}</div>}

      {/* search input — filters the displayed log entries client-side */}
      <div style={{ margin: '16px 0' }}>
        <input
          type="text"
          placeholder="Search by user, action, or description…"
          value={search}
          onChange={e => setSearch(e.target.value)}
          style={{ width: '100%', maxWidth: '420px', padding: '8px 12px', borderRadius: '6px', border: '1px solid #ccc', fontSize: '14px' }}
        />
      </div>

      {filtered.length === 0 ? (
        <div className="empty-state">No audit log entries found.</div>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>#</th>
              <th>Date &amp; Time</th>
              <th>User</th>
              <th>Action</th>
              <th>Module</th>
              <th>Description</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((log, i) => (
              <tr key={log.id}>
                <td>{i + 1}</td>
                <td style={{ fontSize: '12px', whiteSpace: 'nowrap' }}>{fmt(log.performedAt)}</td>
                <td><strong>{log.performedBy}</strong></td>
                <td>
                  {/* colour-coded action badge — makes scan-reading the log easier */}
                  <span style={{
                    background: actionColor(log.action) + '1A',
                    color: actionColor(log.action),
                    padding: '2px 8px', borderRadius: '10px',
                    fontSize: '11px', fontWeight: 700
                  }}>
                    {log.action}
                  </span>
                </td>
                <td style={{ fontSize: '12px' }}>{log.entityType}</td>
                <td style={{ fontSize: '13px' }}>{log.description}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
