import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getAllEvents, deleteEvent } from '../api/apiService.js';
import StatusBadge from '../components/StatusBadge.jsx';

// filter options for the status dropdown — ALL shows every event regardless of status
const STATUS_OPTIONS = ['ALL', 'PLANNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'];
const DAYS = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
const MONTHS = ['January','February','March','April','May','June','July','August','September','October','November','December'];

export default function EventList() {
  const [events, setEvents]       = useState([]);
  const [loading, setLoading]     = useState(true);
  const [error, setError]         = useState('');
  const [search, setSearch]       = useState('');
  const [statusFilter, setStatus] = useState('ALL');
  const [view, setView]           = useState('list'); // 'list' | 'calendar'
  const [calMonth, setCalMonth]   = useState(new Date());
  const navigate = useNavigate();

  // load all events from the API
  const load = async () => {
    try {
      setEvents(await getAllEvents());
    } catch {
      // show error state if API call fails
      setError('Failed to load events.');
    }
    finally { setLoading(false); }
  };

  // fetch data on component mount
  useEffect(() => { load(); }, []);

  const handleDelete = async (id, name) => {
    if (!window.confirm(`Delete event "${name}"?`)) return;
    try {
      await deleteEvent(id);
      // optimistically remove the deleted item from local state — avoids a full reload
      setEvents(p => p.filter(e => e.id !== id));
    }
    catch {
      // show error state if API call fails
      setError('Failed to delete event.');
    }
  };

  // client-side filtering — keeps the list reactive to both search text and status filter
  const filtered = events.filter(ev => {
    const matchSearch = ev.name.toLowerCase().includes(search.toLowerCase()) ||
      ev.venue.toLowerCase().includes(search.toLowerCase()) ||
      (ev.clientName || '').toLowerCase().includes(search.toLowerCase());
    const matchStatus = statusFilter === 'ALL' || ev.status === statusFilter;
    return matchSearch && matchStatus;
  });

  // calendar navigation helpers — step one month at a time
  const prevMonth = () => setCalMonth(d => new Date(d.getFullYear(), d.getMonth() - 1, 1));
  const nextMonth = () => setCalMonth(d => new Date(d.getFullYear(), d.getMonth() + 1, 1));

  const renderCalendar = () => {
    const year = calMonth.getFullYear();
    const month = calMonth.getMonth();
    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    // build cell array with null padding for days before the 1st of the month
    const cells = [];
    for (let i = 0; i < firstDay; i++) cells.push(null);
    for (let d = 1; d <= daysInMonth; d++) cells.push(d);

    // return filtered events that fall on the given day
    const eventsOnDay = (day) => filtered.filter(ev => {
      const d = new Date(ev.eventDate);
      return d.getFullYear() === year && d.getMonth() === month && d.getDate() === day;
    });

    // map event status to a corresponding dot colour for the calendar view
    const statusDot = s => ({ PLANNED:'#6c757d', IN_PROGRESS:'#0D47A1', COMPLETED:'#1B5E20', CANCELLED:'#B71C1C' })[s] || '#999';

    return (
      <div>
        <div style={{ display:'flex', alignItems:'center', gap:'16px', marginBottom:'12px' }}>
          <button className="btn btn-sm btn-secondary" onClick={prevMonth}>‹ Prev</button>
          <strong style={{ fontSize:'16px' }}>{MONTHS[month]} {year}</strong>
          <button className="btn btn-sm btn-secondary" onClick={nextMonth}>Next ›</button>
        </div>
        <div style={{ display:'grid', gridTemplateColumns:'repeat(7,1fr)', gap:'4px' }}>
          {DAYS.map(d => (
            <div key={d} style={{ textAlign:'center', fontWeight:700, fontSize:'12px', padding:'6px', color:'#42103C' }}>{d}</div>
          ))}
          {cells.map((day, i) => {
            const dayEvents = day ? eventsOnDay(day) : [];
            const isToday = day && new Date().getDate() === day && new Date().getMonth() === month && new Date().getFullYear() === year;
            return (
              <div key={i} style={{
                minHeight:'72px', background: day ? '#fff' : 'transparent',
                border: day ? (isToday ? '2px solid #42103C' : '1px solid #e0d5df') : 'none',
                borderRadius:'6px', padding:'4px', fontSize:'12px',
              }}>
                {day && <div style={{ fontWeight: isToday ? 700 : 400, color: isToday ? '#42103C' : '#333', marginBottom:'2px' }}>{day}</div>}
                {dayEvents.map(ev => (
                  // clicking an event in the calendar navigates to its allocations
                  <div key={ev.id} title={ev.name} onClick={() => navigate(`/allocations/event/${ev.id}`)}
                    style={{ background: statusDot(ev.status) + '22', color: statusDot(ev.status), borderRadius:'4px', padding:'1px 4px', marginBottom:'2px', cursor:'pointer', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap', fontSize:'11px', fontWeight:600 }}>
                    {ev.name}
                  </div>
                ))}
              </div>
            );
          })}
        </div>
      </div>
    );
  };

  if (loading) return <div className="loading">Loading events...</div>;

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Events</h1>
        <Link to="/events/new" className="btn btn-primary">+ Create Event</Link>
      </div>

      {error && <div className="error-message">{error}</div>}

      {/* search, status filter, and list/calendar view toggle */}
      <div style={{ display:'flex', gap:'12px', flexWrap:'wrap', margin:'16px 0', alignItems:'center' }}>
        <input type="text" placeholder="Search by name, venue, or client…" value={search}
          onChange={e => setSearch(e.target.value)}
          style={{ padding:'8px 12px', borderRadius:'6px', border:'1px solid #ccc', fontSize:'14px', minWidth:'260px' }} />
        <select value={statusFilter} onChange={e => setStatus(e.target.value)}
          style={{ padding:'8px 12px', borderRadius:'6px', border:'1px solid #ccc', fontSize:'14px' }}>
          {STATUS_OPTIONS.map(s => <option key={s} value={s}>{s === 'ALL' ? 'All Statuses' : s.replace('_',' ')}</option>)}
        </select>
        <div style={{ marginLeft:'auto', display:'flex', gap:'8px' }}>
          <button className={`btn btn-sm ${view === 'list' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setView('list')}>List</button>
          <button className={`btn btn-sm ${view === 'calendar' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setView('calendar')}>Calendar</button>
        </div>
      </div>

      {view === 'calendar' ? renderCalendar() : (
        filtered.length === 0 ? (
          <div className="empty-state">No events found.</div>
        ) : (
          <table className="table">
            <thead>
              <tr><th>#</th><th>Event Name</th><th>Client</th><th>Venue</th><th>Date</th><th>Status</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {filtered.map((ev, i) => (
                <tr key={ev.id}>
                  <td>{i + 1}</td>
                  <td><strong>{ev.name}</strong></td>
                  <td>
                    <div>{ev.clientName || '—'}</div>
                    {ev.clientPhone && <div style={{ fontSize:'11px', color:'#666' }}>{ev.clientPhone}</div>}
                  </td>
                  <td>{ev.venue}</td>
                  <td style={{ whiteSpace:'nowrap' }}>{ev.eventDate}</td>
                  <td><StatusBadge status={ev.status} /></td>
                  <td>
                    <button className="btn btn-sm btn-secondary" onClick={() => navigate(`/events/edit/${ev.id}`)}>Edit</button>
                    <button className="btn btn-sm btn-info" onClick={() => navigate(`/allocations/event/${ev.id}`)}>Allocations</button>
                    <button className="btn btn-sm btn-danger" onClick={() => handleDelete(ev.id, ev.name)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )
      )}
    </div>
  );
}
