import React, { createContext, useContext, useState } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('atlas_user');
    return saved ? JSON.parse(saved) : null;
  });

  const login = (userData) => {
    localStorage.setItem('atlas_user', JSON.stringify(userData));
    localStorage.setItem('atlas_token', userData.token);
    setUser(userData);
  };

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

export const useAuth = () => useContext(AuthContext);
