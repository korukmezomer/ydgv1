import { Link, useNavigate } from 'react-router-dom';
import { useMemo, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import mediumLogo from '../assets/medium-logo-png_seeklogo-347160.png';
import heroImage from '../assets/image.png';
import './Home.css';

const Home = () => {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();

  const dashboardPath = useMemo(() => {
    if (!isAuthenticated || !user) return '/login';
    const roles = user.roles || [];
    if (roles.includes('ADMIN')) return '/admin/dashboard';
    if (roles.includes('WRITER')) return '/yazar/dashboard';
    return '/reader/dashboard';
  }, [isAuthenticated, user]);

  // Tüm roller için giriş yapınca direkt dashboard'a yönlendir
  useEffect(() => {
    if (isAuthenticated && user) {
      const roles = user.roles || [];
      if (roles.includes('ADMIN')) {
        navigate('/admin/dashboard', { replace: true });
      } else if (roles.includes('WRITER')) {
        navigate('/yazar/dashboard', { replace: true });
      } else if (roles.length > 0) {
        navigate('/reader/dashboard', { replace: true });
      }
    }
  }, [isAuthenticated, user, navigate]);

  return (
    <div className="home-landing">
      {/* Header */}
      <header className="landing-header">
        <div className="landing-header-container">
          <Link to="/" className="landing-logo">
            <img src={mediumLogo} alt="Medium" className="medium-logo-img" />
          </Link>
          
          <nav className="landing-nav">
            <Link to="/about" className="landing-nav-link">Hikayemiz</Link>
            <Link to="/membership" className="landing-nav-link">Üyelik</Link>
            {isAuthenticated ? (
              <>
                {user?.roles?.includes('WRITER') && (
                  <Link to="/reader/new-story" className="landing-nav-link">Yaz</Link>
                )}
                <Link to={dashboardPath} className="landing-nav-link">Dashboard</Link>
              </>
            ) : (
              <>
                <Link to="/login" className="landing-nav-link">Giriş Yap</Link>
                <Link to="/register" className="landing-get-started-btn">Başla</Link>
              </>
            )}
          </nav>
        </div>
      </header>

      {/* Hero Section */}
      <main className="landing-hero">
        <div className="landing-hero-container">
          <div className="landing-hero-content">
            <div className="landing-hero-left">
              <h1 className="landing-hero-title">
                İnsan<br />
                hikayeleri & fikirler
              </h1>
              <p className="landing-hero-subtitle">
                Okumak, yazmak ve anlayışınızı derinleştirmek için bir yer
              </p>
              <Link to={isAuthenticated ? dashboardPath : "/register"} className="landing-start-reading-btn">
                Okumaya başla
              </Link>
            </div>
            
            <div className="landing-hero-right">
              <div className="landing-hero-image">
                <img src={heroImage} alt="Medium" className="hero-image" />
              </div>
            </div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="landing-footer">
        <div className="landing-footer-container">
          <Link to="/help" className="landing-footer-link">Yardım</Link>
          <Link to="/status" className="landing-footer-link">Durum</Link>
          <Link to="/about" className="landing-footer-link">Hakkında</Link>
          <Link to="/careers" className="landing-footer-link">Kariyer</Link>
          <Link to="/press" className="landing-footer-link">Basın</Link>
          <Link to="/blog" className="landing-footer-link">Blog</Link>
          <Link to="/privacy" className="landing-footer-link">Gizlilik</Link>
          <Link to="/rules" className="landing-footer-link">Kurallar</Link>
          <Link to="/terms" className="landing-footer-link">Şartlar</Link>
          <Link to="/text-to-speech" className="landing-footer-link">Metinden sese</Link>
        </div>
      </footer>
    </div>
  );
};

export default Home;
