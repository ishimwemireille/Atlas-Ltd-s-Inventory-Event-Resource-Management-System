import React, { useEffect, useState } from 'react';
import { getAllUsers, createUser, updateUser, deleteUser } from '../api/apiService.js';
import { useAuth } from '../context/AuthContext.jsx';

const EMPTY_FORM   = { username: '', email: '', password: '', role: 'STAFF' };
const EMPTY_EDIT   = { username: '', email: '', password: '', role: 'STAFF' };

export default function UserManagement() {
  const [users, setUsers]           = useState([]);
  const [formData, setFormData]     = useState(EMPTY_FORM);
  const [editTarget, setEditTarget] = useState(null);   // user being edited
  const [editData, setEditData]     = useState(EMPTY_EDIT);
  const [error, setError]           = useState('');
  const [success, setSuccess]       = useState('');
  const [saving, setSaving]         = useState(false);
  const { user: currentUser }       = useAuth();

  const loadUsers = async () => {
    try {
      const data = await getAllUsers();
      setUsers(data);
    } catch {
      setError('Failed to load users.');
    }
  };

  useEffect(() => { loadUsers(); }, []);

  // ── Create ──────────────────────────────────────────────────────────────────
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
      await createUser(formData);
      setSuccess(`Account created for "${formData.username}" (${formData.role}).`);
      setFormData(EMPTY_FORM);
      await loadUsers();
    } catch (err) {
      setError(err.response?.data?.message ?? 'Failed to create user.');
    } finally {
      setSaving(false);
    }
  };

  // ── Edit ─────────────────────────────────────────────────────────────────────
  const openEdit = (u) => {
    setEditTarget(u);
    setEditData({ username: u.username, email: u.email, password: '', role: u.role });
    setError('');
    setSuccess('');
  };

  const closeEdit = () => {
    setEditTarget(null);
    setEditData(EMPTY_EDIT);
  };

  const handleEditChange = (e) => {
    const { name, value } = e.target;
    setEditData((prev) => ({ ...prev, [name]: value }));
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      await updateUser(editTarget.id, editData);
      setSuccess(`Profile updated for "${editData.username}".`);
      closeEdit();
      await loadUsers();
    } catch (err) {
      setError(err.response?.data?.message ?? 'Failed to update user.');
    } finally {
      setSaving(false);
    }
  };

  // ── Delete ───────────────────────────────────────────────────────────────────
  const handleDelete = async (id, username) => {
    if (username === currentUser?.username) {
      setError('You cannot delete your own account.');
      return;
    }
    if (!window.confirm(`Delete account "${username}"?`)) return;
    try {
      await deleteUser(id);
      await loadUsers();
    } catch {
      setError('Failed to delete user.');
    }
  };

  return (
    <div>
      <h1 className="page-title">User Management</h1>
      <p className="page-subtitle">Internal accounts only — users cannot self-register.</p>

      {error   && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.5fr', gap: '24px', marginTop: '24px' }}>

        {/* ── Create Form ── */}
        <div className="form-container" style={{ maxWidth: '100%' }}>
          <h3 style={{ marginBottom: '16px', color: '#1a2c4e' }}>Create New Account</h3>
          <form onSubmit={handleSubmit} className="form" style={{ marginTop: 0 }}>
            <div className="form-group">
              <label htmlFor="username">Username *</label>
              <input id="username" name="username" type="text" value={formData.username}
                onChange={handleChange} required placeholder="e.g. john.doe" />
            </div>
            <div className="form-group">
              <label htmlFor="email">Email *</label>
              <input id="email" name="email" type="email" value={formData.email}
                onChange={handleChange} required placeholder="e.g. john@atlasturbo.rw" />
            </div>
            <div className="form-group">
              <label htmlFor="password">Temporary Password *</label>
              <input id="password" name="password" type="password" value={formData.password}
                onChange={handleChange} required placeholder="Set a password for this user" />
            </div>
            <div className="form-group">
              <label htmlFor="role">Role *</label>
              <select id="role" name="role" value={formData.role} onChange={handleChange}>
                <option value="STAFF">Staff</option>
                <option value="ADMIN">Admin</option>
              </select>
            </div>
            <button type="submit" className="btn btn-primary" disabled={saving} style={{ width: '100%' }}>
              {saving ? 'Creating...' : 'Create Account'}
            </button>
          </form>
        </div>

        {/* ── Users Table ── */}
        <div>
          <h3 style={{ marginBottom: '16px', color: '#1a2c4e' }}>Existing Accounts</h3>
          <table className="table">
            <thead>
              <tr>
                <th>Username</th>
                <th>Email</th>
                <th>Role</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>
                    <strong>{u.username}</strong>
                    {u.username === currentUser?.username && (
                      <span style={{ color: '#6c757d', fontSize: '12px' }}> (you)</span>
                    )}
                  </td>
                  <td>{u.email}</td>
                  <td>
                    <span style={{
                      background: u.role === 'ADMIN' ? '#d4edda' : '#cce5ff',
                      color:      u.role === 'ADMIN' ? '#155724' : '#004085',
                      padding: '2px 8px', borderRadius: '10px', fontSize: '12px', fontWeight: 600,
                    }}>
                      {u.role}
                    </span>
                  </td>
                  <td style={{ display: 'flex', gap: '8px' }}>
                    <button className="btn btn-sm btn-secondary" onClick={() => openEdit(u)}>
                      Edit
                    </button>
                    <button className="btn btn-sm btn-danger"
                      onClick={() => handleDelete(u.id, u.username)}>
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* ── Edit Modal ── */}
      {editTarget && (
        <div style={{
          position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.45)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000,
        }}>
          <div style={{
            background: '#fff', borderRadius: '12px', padding: '32px',
            width: '100%', maxWidth: '440px', boxShadow: '0 8px 32px rgba(0,0,0,0.18)',
          }}>
            <h3 style={{ marginBottom: '20px', color: '#1a2c4e' }}>
              Edit Profile — {editTarget.username}
            </h3>
            <form onSubmit={handleEditSubmit} className="form" style={{ marginTop: 0 }}>
              <div className="form-group">
                <label htmlFor="edit-username">Username *</label>
                <input id="edit-username" name="username" type="text"
                  value={editData.username} onChange={handleEditChange} required />
              </div>
              <div className="form-group">
                <label htmlFor="edit-email">Email *</label>
                <input id="edit-email" name="email" type="email"
                  value={editData.email} onChange={handleEditChange} required />
              </div>
              <div className="form-group">
                <label htmlFor="edit-role">Role *</label>
                <select id="edit-role" name="role" value={editData.role} onChange={handleEditChange}>
                  <option value="STAFF">Staff</option>
                  <option value="ADMIN">Admin</option>
                </select>
              </div>
              <div className="form-group">
                <label htmlFor="edit-password">New Password <span style={{ color: '#888', fontWeight: 400 }}>(leave blank to keep current)</span></label>
                <input id="edit-password" name="password" type="password"
                  value={editData.password} onChange={handleEditChange}
                  placeholder="Leave blank to keep existing password" />
              </div>
              <div style={{ display: 'flex', gap: '12px', marginTop: '8px' }}>
                <button type="submit" className="btn btn-primary" disabled={saving} style={{ flex: 1 }}>
                  {saving ? 'Saving...' : 'Save Changes'}
                </button>
                <button type="button" className="btn btn-secondary" onClick={closeEdit} style={{ flex: 1 }}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
