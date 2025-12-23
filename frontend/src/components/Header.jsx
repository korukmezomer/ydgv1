import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Header.css';

const Header = () => {
  const { isAuthenticated, user, logout, hasRole } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const getDashboardPath = () => {
    if (!user) return '/login';
    if (hasRole('ADMIN')) return '/admin/dashboard';
    if (hasRole('WRITER')) return '/yazar/dashboard';
    return '/reader/dashboard';
  };

  return (
    <header className="header">
      <div className="header-container">
        <Link to="/" className="logo">
          <h1>Yazılım Doğrulama</h1>
        </Link>
        
        <nav className="nav">
          {isAuthenticated ? (
            <>
              <Link to={getDashboardPath()} className="nav-link">
                Dashboard
              </Link>
              <span className="nav-user">Merhaba, {user?.email}</span>
              <button onClick={handleLogout} className="btn btn-outline">
                Çıkış Yap
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-primary">
                Giriş Yap
              </Link>
              <Link to="/register" className="btn btn-outline">
                Kayıt Ol
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
};

export default Header;

