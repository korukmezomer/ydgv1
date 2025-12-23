// JWT token decode ve utility fonksiyonları

export const decodeJWT = (token) => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (error) {
    return null;
  }
};

export const getTokenFromStorage = () => {
  return localStorage.getItem('token');
};

export const setTokenToStorage = (token) => {
  localStorage.setItem('token', token);
};

export const removeTokenFromStorage = () => {
  localStorage.removeItem('token');
};

export const getUserFromToken = (token) => {
  if (!token) {
    token = getTokenFromStorage();
  }
  if (!token) return null;
  
  const decoded = decodeJWT(token);
  if (!decoded) return null;
  
  // Backend'den gelen roller array olarak gelir
  const roller = decoded.roller || decoded.roles || [];
  
  return {
    id: decoded.userId,
    email: decoded.sub,
    kullaniciAdi: decoded.kullaniciAdi || decoded.sub?.split('@')[0],
    roles: Array.isArray(roller) ? roller : []
  };
};

export const isTokenExpired = (token) => {
  const decoded = decodeJWT(token);
  if (!decoded || !decoded.exp) return true;
  
  const currentTime = Date.now() / 1000;
  return decoded.exp < currentTime;
};

export const hasRole = (user, role) => {
  if (!user || !user.roles) return false;
  return user.roles.includes(role);
};
