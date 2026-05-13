import React, { useEffect, useState } from 'react';
import { getAllUsers, createUser, deleteUser } from '../api/apiService.js';
import { useAuth } from '../context/AuthContext.jsx';

const EMPTY_FORM = { username: '', email: '', password: '', role: 'STAFF' };

export default function UserManagement() {
  const [users, setUsers] = useState([]);
  const [formData, setFormData] = useState(EMPTY_FORM);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [saving, setSaving] = useState(false);
  const { user: currentUser } = useAuth();

  const loadUsers = async () => {
    try {
      const data = await getAllUsers();
      setUsers(data);
    } catch (err) {
      setError('Failed to load users.');
    }
  };

  useEffect(() => { loadUsers(); }, []);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
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

  const handleDelete = async (id, username) => {
    if (username === currentUser?.username) {
      setError('You cannot delete your own account.');
      return;
    }
    if (!window.confirm(`Delete account "${username}"?`)) return;
    try {
      await deleteUser(id);
      await loadUsers();
    } catch (err) {
      setError('Failed to delete user.');
    }
  };

  return (
    <div>
      <h1 className="page-title">User Management</h1>
      <p className="page-subtitle">
        Internal accounts only — users cannot self-register.
      </p>

      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.5fr', gap: '24px', marginTop: '24px' }}>
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

        <div>
          <h3 style={{ marginBottom: '16px', color: '#1a2c4e' }}>Existing Accounts</h3>
          <table className="table">
            <thead>
              <tr>
                <th>Username</th>
                <th>Email</th>
                <th>Role</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td><strong>{u.username}</strong> {u.username === currentUser?.username && <span style={{ color: '#6c757d', fontSize: '12px' }}>(you)</span>}</td>
                  <td>{u.email}</td>
                  <td>
                    <span style={{
                      background: u.role === 'ADMIN' ? '#d4edda' : '#cce5ff',
                      color: u.role === 'ADMIN' ? '#155724' : '#004085',
                      padding: '2px 8px', borderRadius: '10px', fontSize: '12px', fontWeight: 600
                    }}>
                      {u.role}
                    </span>
                  </td>
                  <td>
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
    </div>
  );
}
