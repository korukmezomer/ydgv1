import { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import dropdownImage from '../assets/image copy.png';
import './ProfileDropdown.css';

const ProfileDropdown = ({ user }) => {
  const { logout, hasRole } = useAuth();
  const navigate = useNavigate();
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isOpen]);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  // Kullanıcının rolüne göre profil sayfası URL'ini belirle
  const getProfilePath = () => {
    if (hasRole('WRITER')) {
      return '/yazar/profil';
    }
    return '/reader/profile';
  };

  const displayName = user?.kullaniciAdi || user?.email || 'Kullanıcı';
  const displayEmail = user?.email || '';

  return (
    <div className="profile-dropdown-container" ref={dropdownRef}>
      <button 
        className="profile-dropdown-trigger"
        onClick={() => setIsOpen(!isOpen)}
      >
        <div className="profile-avatar">
          {displayName.charAt(0).toUpperCase()}
        </div>
      </button>

      {isOpen && (
        <div className="profile-dropdown">
          <div className="dropdown-header">
            <div className="dropdown-avatar">
              {displayName.charAt(0).toUpperCase()}
            </div>
            <div className="dropdown-user-info">
              <div className="dropdown-name">{displayName}</div>
              <Link 
                to={getProfilePath()} 
                className="dropdown-view-profile"
                onClick={() => setIsOpen(false)}
              >
                Profili görüntüle
              </Link>
            </div>
          </div>

          <div className="dropdown-medium-header">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" className="dropdown-hamburger">
              <path d="M3 12h18M3 6h18M3 18h18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
            </svg>
            <span className="dropdown-medium-title">Medium</span>
          </div>

          <div className="dropdown-image-section">
            <img src={dropdownImage} alt="Medium" className="dropdown-image" />
          </div>

          <div className="dropdown-menu">
            <Link 
              to="/reader/library" 
              className="dropdown-item"
              onClick={() => setIsOpen(false)}
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" stroke="currentColor" strokeWidth="2"/>
                <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" stroke="currentColor" strokeWidth="2"/>
              </svg>
              <span>Kütüphane</span>
            </Link>

            <Link 
              to="/reader/profile" 
              className="dropdown-item"
              onClick={() => setIsOpen(false)}
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path d="M4 6h16M4 12h16M4 18h16" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
              </svg>
              <span>Hikayeler</span>
            </Link>

            <Link 
              to="/reader/profile" 
              className="dropdown-item"
              onClick={() => setIsOpen(false)}
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path d="M3 3v18h18" stroke="currentColor" strokeWidth="2"/>
                <path d="M18 17V9M12 17V5M6 17v-3" stroke="currentColor" strokeWidth="2"/>
              </svg>
              <span>İstatistikler</span>
            </Link>
          </div>

          <div className="dropdown-section">
            <div className="dropdown-notice">
              <p>Kütüphane, Hikayeler ve İstatistikler artık yeni sidebar'da, Medium'un tüm favori bölümlerinize kolay erişim için.</p>
              <Link to="/reader/dashboard" className="dropdown-notice-link" onClick={() => setIsOpen(false)}>
                Tamam, anladım
              </Link>
            </div>
          </div>

          <div className="dropdown-divider"></div>

          <div className="dropdown-menu">
            <Link 
              to="/reader/settings" 
              className="dropdown-item"
              onClick={() => setIsOpen(false)}
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="2"/>
                <path d="M12 1v6m0 6v6M5.64 5.64l4.24 4.24m4.24 4.24l4.24 4.24M1 12h6m6 0h6M5.64 18.36l4.24-4.24m4.24-4.24l4.24-4.24" stroke="currentColor" strokeWidth="2"/>
              </svg>
              <span>Ayarlar</span>
            </Link>

            <Link 
              to="/reader/profile" 
              className="dropdown-item"
              onClick={() => setIsOpen(false)}
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                <path d="M12 16v-4M12 8h.01" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
              </svg>
              <span>Yardım</span>
            </Link>
          </div>

          <div className="dropdown-divider"></div>

          <div className="dropdown-menu">
            <div className="dropdown-item premium">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" stroke="currentColor" strokeWidth="2"/>
              </svg>
              <span>Medium üyesi ol</span>
            </div>

            <div className="dropdown-item">
              <span>Partner Programına başvur</span>
            </div>
          </div>

          <div className="dropdown-divider"></div>

          <div className="dropdown-footer">
            <button className="dropdown-signout" onClick={handleLogout}>
              Çıkış yap
            </button>
            <div className="dropdown-email">{displayEmail}</div>
          </div>

          <div className="dropdown-footer-links">
            <Link to="/about" className="dropdown-footer-link" onClick={() => setIsOpen(false)}>Hakkında</Link>
            <Link to="/blog" className="dropdown-footer-link" onClick={() => setIsOpen(false)}>Blog</Link>
            <Link to="/careers" className="dropdown-footer-link" onClick={() => setIsOpen(false)}>Kariyer</Link>
            <Link to="/privacy" className="dropdown-footer-link" onClick={() => setIsOpen(false)}>Gizlilik</Link>
            <Link to="/terms" className="dropdown-footer-link" onClick={() => setIsOpen(false)}>Şartlar</Link>
            <Link to="/text-to-speech" className="dropdown-footer-link" onClick={() => setIsOpen(false)}>Metinden sese</Link>
            <Link to="/more" className="dropdown-footer-link" onClick={() => setIsOpen(false)}>Daha fazla</Link>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProfileDropdown;

