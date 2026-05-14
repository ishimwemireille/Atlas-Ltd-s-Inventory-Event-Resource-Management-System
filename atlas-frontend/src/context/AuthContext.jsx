import React, { createContext, useContext, useState } from 'react';

// shared auth context — provides user state and login/logout actions to the entire app
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  // initialise user from localStorage so the session survives a page refresh
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('atlas_user');
    return saved ? JSON.parse(saved) : null;
  });

  // persist user data and JWT token to localStorage on successful login
  const login = (userData) => {
    localStorage.setItem('atlas_user', JSON.stringify(userData));
    localStorage.setItem('atlas_token', userData.token);
    setUser(userData);
  };

  // clear all session data from localStorage and reset in-memory state
  const logout = () => {
    localStorage.removeItem('atlas_user');
    localStorage.removeItem('atlas_token');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

// custom hook — components call useAuth() instead of importing AuthContext directly
export const useAuth = () => useContext(AuthContext);
