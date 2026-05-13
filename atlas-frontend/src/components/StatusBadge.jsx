import React from 'react';

const STATUS_STYLES = {
  IN_STOCK:  { background: '#d4edda', color: '#155724', label: 'In Stock' },
  RESERVED:  { background: '#fff3cd', color: '#856404', label: 'Reserved' },
  DEPLOYED:  { background: '#f8d7da', color: '#721c24', label: 'Deployed' },
  RETURNED:  { background: '#e2e3e5', color: '#383d41', label: 'Returned' },
  PLANNED:   { background: '#cce5ff', color: '#004085', label: 'Planned' },
  IN_PROGRESS: { background: '#fff3cd', color: '#856404', label: 'In Progress' },
  COMPLETED: { background: '#d4edda', color: '#155724', label: 'Completed' },
  CANCELLED: { background: '#f8d7da', color: '#721c24', label: 'Cancelled' },
};

export default function StatusBadge({ status }) {
  const style = STATUS_STYLES[status] || { background: '#e2e3e5', color: '#383d41', label: status };
  return (
    <span
      style={{
        background: style.background,
        color: style.color,
        padding: '3px 10px',
        borderRadius: '12px',
        fontSize: '12px',
        fontWeight: '600',
        whiteSpace: 'nowrap',
      }}
    >
      {style.label}
    </span>
  );
}
