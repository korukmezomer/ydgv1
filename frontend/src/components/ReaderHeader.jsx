import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import ProfileDropdown from './ProfileDropdown';
import { bildirimAPI } from '../services/api';
import mediumLogo from '../assets/medium-logo-png_seeklogo-347160.png';
import './ReaderHeader.css';

const ReaderHeader = ({ onSidebarToggle }) => {
  const { user, hasRole, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [searchQuery, setSearchQuery] = useState('');
  const [isSearchFocused, setIsSearchFocused] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const [badgeVisible, setBadgeVisible] = useState(true);

  // Kullanıcının rolüne göre dashboard path'ini belirle
  const getDashboardPath = () => {
    if (!user) return '/login';
    if (hasRole('ADMIN')) return '/admin/dashboard';
    if (hasRole('WRITER')) return '/yazar/dashboard';
    return '/reader/dashboard';
  };

  // Kullanıcının rolüne göre bildirimler path'ini belirle
  const getNotificationsPath = () => {
    if (!user) return '/login';
    if (hasRole('ADMIN')) return '/admin/notifications';
    if (hasRole('WRITER')) return '/yazar/notifications';
    return '/reader/notifications';
  };

  // Kullanıcının rolüne göre arama path'ini belirle
  const getSearchPath = (query) => {
    if (!user) return '/login';
    if (hasRole('ADMIN')) return `/admin/search?q=${encodeURIComponent(query)}`;
    if (hasRole('WRITER')) return `/yazar/search?q=${encodeURIComponent(query)}`;
    return `/reader/search?q=${encodeURIComponent(query)}`;
  };

  // URL'deki query parametresini oku
  useEffect(() => {
    if (location.pathname.includes('/search')) {
      const params = new URLSearchParams(location.search);
      const q = params.get('q') || '';
      setSearchQuery(q);
    }
  }, [location]);

  // Okunmamış bildirim sayısını çek
  const fetchUnreadCount = async () => {
    if (!isAuthenticated || !user?.id) return;
    
    try {
      const response = await bildirimAPI.getOkunmamisSayisi();
      const count = response.data || 0;
      const previousCount = unreadCount;
      
      setUnreadCount(count);
      
      // Eğer sayı azaldıysa (bildirimler okundu), badge'i kaybet
      if (count === 0 && previousCount > 0) {
        setBadgeVisible(false);
      } else if (count > 0) {
        setBadgeVisible(true);
      }
    } catch (error) {
      console.error('Okunmamış bildirim sayısı yüklenirken hata:', error);
      setUnreadCount(0);
      setBadgeVisible(false);
    }
  };

  // İlk yüklemede ve periyodik olarak okunmamış bildirim sayısını kontrol et
  useEffect(() => {
    if (isAuthenticated && user?.id) {
      fetchUnreadCount();
      
      // Her 30 saniyede bir kontrol et
      const interval = setInterval(fetchUnreadCount, 30000);
      
      return () => clearInterval(interval);
    }
  }, [isAuthenticated, user?.id]);

  // Bildirimler sayfasına gidildiğinde sayacı güncelle ve badge'i gizle
  useEffect(() => {
    if (location.pathname.includes('/notifications')) {
      fetchUnreadCount();
      // Bildirimler sayfasındayken badge'i gizle (bildirimler görülüyor)
      setBadgeVisible(false);
    } else {
      // Bildirimler sayfasından çıkıldığında tekrar kontrol et
      fetchUnreadCount();
    }
  }, [location.pathname]);

  // Bildirim okundu olarak işaretlendiğinde sayacı güncelle
  useEffect(() => {
    const handleNotificationRead = () => {
      // Badge'i kaybet animasyonu ile gizle
      setBadgeVisible(false);
      // Sonra sayacı güncelle
      setTimeout(() => {
        fetchUnreadCount();
      }, 300);
    };

    window.addEventListener('notificationRead', handleNotificationRead);
    return () => {
      window.removeEventListener('notificationRead', handleNotificationRead);
    };
  }, []);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(getSearchPath(searchQuery.trim()));
    }
  };

  const handleSearchFocus = () => {
    setIsSearchFocused(true);
    // Eğer arama sayfasında değilsek, kullanıcının rolüne göre arama sayfasına git
    const getSearchPagePath = () => {
      if (hasRole('ADMIN')) return '/admin/search';
      if (hasRole('WRITER')) return '/yazar/search';
      return '/reader/search';
    };
    
    if (!location.pathname.includes('/search')) {
      navigate(getSearchPagePath());
    }
  };

  return (
    <>
      <header className="reader-header">
        <div className="reader-header-content">
          <button 
            className="hamburger-menu"
            onClick={onSidebarToggle}
            aria-label="Menu"
          >
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M3 12h18M3 6h18M3 18h18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
            </svg>
          </button>
          
          <Link to={getDashboardPath()} className="reader-logo">
            <img src={mediumLogo} alt="Medium" className="medium-logo-img" />
          </Link>

          <div className="reader-header-center">
            <form onSubmit={handleSearchSubmit} className="search-box-form">
              <div className={`search-box ${isSearchFocused ? 'focused' : ''}`}>
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" className="search-icon">
                  <circle cx="11" cy="11" r="8" stroke="currentColor" strokeWidth="2"/>
                  <path d="m21 21-4.35-4.35" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                </svg>
                <input
                  type="text"
                  className="search-input-header"
                  placeholder="Ara"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  onFocus={handleSearchFocus}
                  onBlur={() => setIsSearchFocused(false)}
                />
              </div>
            </form>
          </div>

          <div className="reader-header-right">
            {hasRole('WRITER') && (
              <Link to="/reader/new-story" className="write-button">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M14 2v6h6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M16 13H8" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M16 17H8" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M10 9H8" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
                <span>Yaz</span>
              </Link>
            )}
            <Link to={getNotificationsPath()} className="notification-button">
              <div className="notification-icon-wrapper">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                  <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M13.73 21a2 2 0 0 1-3.46 0" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
                {unreadCount > 0 && badgeVisible && !location.pathname.includes('/notifications') && (
                  <>
                    <span className="notification-badge">{unreadCount > 99 ? '99+' : unreadCount}</span>
                    <span className="notification-pulse"></span>
                  </>
                )}
              </div>
            </Link>
            <ProfileDropdown user={user} />
          </div>
        </div>
      </header>
    </>
  );
};

export default ReaderHeader;

