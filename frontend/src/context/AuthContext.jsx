import { createContext, useContext, useState, useEffect } from 'react';
import { getUserFromToken, isTokenExpired, getTokenFromStorage, setTokenToStorage, removeTokenFromStorage } from '../utils/jwt';

const AuthContext = createContext(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = getTokenFromStorage();
    if (token && !isTokenExpired(token)) {
      const userData = getUserFromToken(token);
      setUser(userData);
    }
    setLoading(false);
  }, []);

  const login = (token) => {
    setTokenToStorage(token);
    const userData = getUserFromToken(token);
    setUser(userData);
  };

  const logout = () => {
    removeTokenFromStorage();
    setUser(null);
  };

  const hasRole = (role) => {
    if (!user || !user.roles) return false;
    return user.roles.includes(role);
  };

  const value = {
    user,
    loading,
    login,
    logout,
    hasRole,
    isAuthenticated: !!user,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

