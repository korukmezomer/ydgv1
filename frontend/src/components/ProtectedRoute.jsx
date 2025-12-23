import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ProtectedRoute = ({ children, requiredRole = null }) => {
  const { isAuthenticated, hasRole, loading } = useAuth();

  if (loading) {
    return <div className="loading">Yükleniyor...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole) {
    // Eğer requiredRole bir array ise, kullanıcının bu rollerden birine sahip olması gerekir
    if (Array.isArray(requiredRole)) {
      const hasAnyRole = requiredRole.some(role => hasRole(role));
      if (!hasAnyRole) {
        // Kullanıcının rolüne göre dashboard'a yönlendir
        if (hasRole('ADMIN')) return <Navigate to="/admin/dashboard" replace />;
        if (hasRole('WRITER')) return <Navigate to="/yazar/dashboard" replace />;
        return <Navigate to="/reader/dashboard" replace />;
      }
    } else {
      // Eğer requiredRole bir string ise, normal kontrol
      if (!hasRole(requiredRole)) {
        // Kullanıcının rolüne göre dashboard'a yönlendir
        if (hasRole('ADMIN')) return <Navigate to="/admin/dashboard" replace />;
        if (hasRole('WRITER')) return <Navigate to="/yazar/dashboard" replace />;
        return <Navigate to="/reader/dashboard" replace />;
      }
    }
  }

  return children;
};

export default ProtectedRoute;

