import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import atlasLogo from '../assets/atlas-logo.png';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <img src={atlasLogo} alt="Atlas Turbo LTD" className="brand-logo" />
        <span className="brand-name">Atlas EMS</span>
      </div>
      <ul className="navbar-links">
        <li>
          <NavLink to="/" end className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            Dashboard
          </NavLink>
        </li>
        <li>
          <NavLink to="/equipment" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            Equipment
          </NavLink>
        </li>
        <li>
          <NavLink to="/events" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            Events
          </NavLink>
        </li>
        <li>
          <NavLink to="/allocations" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            Allocations
          </NavLink>
        </li>
        {user?.role === 'ADMIN' && (
          <li>
            <NavLink to="/users" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
              Users
            </NavLink>
          </li>
        )}
        <li>
          <NavLink to="/sales" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            Sales
          </NavLink>
        </li>
        {user?.role === 'ADMIN' && (
          <li>
            <NavLink to="/reports" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
              Reports
            </NavLink>
          </li>
        )}
      </ul>
      <div className="navbar-user">
        <span className="user-badge">{user?.role}</span>
        <span className="user-name">{user?.username}</span>
        <button className="btn-logout" onClick={handleLogout}>Logout</button>
      </div>
    </nav>
  );
}
