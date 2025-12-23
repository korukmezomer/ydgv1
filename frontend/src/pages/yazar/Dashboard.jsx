import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { haberAPI } from '../../services/api';
import './Dashboard.css';

const YazarDashboard = ({ sidebarOpen, setSidebarOpen }) => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [haberler, setHaberler] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('all');
  const [stats, setStats] = useState({
    toplam: 0,
    yayinlandi: 0,
    taslak: 0,
    bekliyor: 0,
    reddedildi: 0,
  });

  useEffect(() => {
    if (user?.id) {
      fetchHaberler();
    }
  }, [user]);

  const fetchHaberler = async () => {
    try {
      if (!user?.id) return;
      
      const response = await haberAPI.getByKullanici(user.id, { page: 0, size: 100 });
      const userHaberler = response.data.content || [];
      setHaberler(userHaberler);
      
      setStats({
        toplam: userHaberler.length,
        yayinlandi: userHaberler.filter((h) => h.durum === 'YAYINLANDI' || h.durum === 'PUBLISHED').length,
        taslak: userHaberler.filter((h) => h.durum === 'TASLAK' || h.durum === 'DRAFT').length,
        bekliyor: userHaberler.filter((h) => h.durum === 'YAYIN_BEKLIYOR' || h.durum === 'PENDING_REVIEW').length,
        reddedildi: userHaberler.filter((h) => h.durum === 'REDDEDILDI' || h.durum === 'REJECTED').length,
      });
    } catch (error) {
      console.error('Haberler yüklenirken hata:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleYayinla = async (id, e) => {
    e.preventDefault();
    e.stopPropagation();
    try {
      await haberAPI.yayinla(id);
      fetchHaberler();
    } catch (error) {
      alert('Haber yayınlanırken hata oluştu');
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const months = ['Oca', 'Şub', 'Mar', 'Nis', 'May', 'Haz', 'Tem', 'Ağu', 'Eyl', 'Eki', 'Kas', 'Ara'];
    return `${months[date.getMonth()]} ${date.getDate()}, ${date.getFullYear()}`;
  };

  const filteredHaberler = activeTab === 'all' 
    ? haberler 
    : activeTab === 'published'
    ? haberler.filter(h => h.durum === 'YAYINLANDI' || h.durum === 'PUBLISHED')
    : activeTab === 'drafts'
    ? haberler.filter(h => h.durum === 'TASLAK' || h.durum === 'DRAFT')
    : activeTab === 'pending'
    ? haberler.filter(h => h.durum === 'YAYIN_BEKLIYOR' || h.durum === 'PENDING_REVIEW')
    : activeTab === 'rejected'
    ? haberler.filter(h => h.durum === 'REDDEDILDI' || h.durum === 'REJECTED')
    : haberler;

  return (
    <div className={`yazar-dashboard ${sidebarOpen ? 'sidebar-open' : ''}`}>
      <main className="yazar-dashboard-main">
        <div className="yazar-dashboard-container">
          <div className="yazar-dashboard-header">
            <h1 className="yazar-dashboard-title">Yazılarınız</h1>
            <Link to="/reader/new-story" className="yazar-new-story-btn">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                <path d="M14 2v6h6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                <path d="M16 13H8" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                <path d="M16 17H8" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                <path d="M10 9H8" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
              Yeni hikaye
            </Link>
          </div>

          {/* Tabs */}
          <div className="yazar-dashboard-tabs">
            <button
              className={`yazar-dashboard-tab ${activeTab === 'all' ? 'active' : ''}`}
              onClick={() => setActiveTab('all')}
            >
              Tümü ({stats.toplam})
            </button>
            <button
              className={`yazar-dashboard-tab ${activeTab === 'published' ? 'active' : ''}`}
              onClick={() => setActiveTab('published')}
            >
              Yayınlanan ({stats.yayinlandi})
            </button>
            <button
              className={`yazar-dashboard-tab ${activeTab === 'drafts' ? 'active' : ''}`}
              onClick={() => setActiveTab('drafts')}
            >
              Taslaklar ({stats.taslak})
            </button>
            <button
              className={`yazar-dashboard-tab ${activeTab === 'pending' ? 'active' : ''}`}
              onClick={() => setActiveTab('pending')}
            >
              Bekleyen ({stats.bekliyor})
            </button>
            <button
              className={`yazar-dashboard-tab ${activeTab === 'rejected' ? 'active' : ''}`}
              onClick={() => setActiveTab('rejected')}
            >
              Reddedilen ({stats.reddedildi})
            </button>
          </div>

          {/* Stories List */}
          {loading ? (
            <div className="yazar-loading">Yükleniyor...</div>
          ) : filteredHaberler.length > 0 ? (
            <div className="yazar-stories-list">
              {filteredHaberler.map((haber) => (
                <article 
                  key={haber.id} 
                  className="yazar-story-item"
                  onClick={() => navigate(`/haberler/${haber.slug}`)}
                >
                  <div className="yazar-story-content">
                    <h2 className="yazar-story-title">{haber.baslik}</h2>
                    <p className="yazar-story-excerpt">{haber.ozet || haber.icerik?.substring(0, 150) + '...'}</p>
                    <div className="yazar-story-meta">
                      <span className={`yazar-story-status status-${(haber.durum || '').toLowerCase()}`}>
                        {haber.durum === 'YAYINLANDI' || haber.durum === 'PUBLISHED' ? 'Yayınlandı' : 
                         haber.durum === 'TASLAK' || haber.durum === 'DRAFT' ? 'Taslak' : 
                         haber.durum === 'REDDEDILDI' || haber.durum === 'REJECTED' ? 'Reddedildi' :
                         haber.durum === 'YAYIN_BEKLIYOR' || haber.durum === 'PENDING_REVIEW' ? 'Onay bekliyor' :
                         'Bilinmeyen'}
                      </span>
                      <span className="yazar-story-date">{formatDate(haber.createdAt)}</span>
                      <span className="yazar-story-stats">
                        {haber.okunmaSayisi || 0} okunma
                      </span>
                      <span className="yazar-story-stats">
                        {haber.begeniSayisi || 0} beğeni
                      </span>
                    </div>
                  </div>
                  <div className="yazar-story-actions" onClick={(e) => e.stopPropagation()}>
                    {haber.durum === 'TASLAK' && (
                      <button
                        onClick={(e) => handleYayinla(haber.id, e)}
                        className="yazar-story-publish-btn"
                      >
                        Yayınla
                      </button>
                    )}
                    <Link
                      to={`/yazar/haber-duzenle/${haber.id}`}
                      className="yazar-story-edit-btn"
                    >
                      Düzenle
                    </Link>
                    <button className="yazar-story-more-btn">
                      <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                        <circle cx="12" cy="5" r="1.5" fill="currentColor"/>
                        <circle cx="12" cy="12" r="1.5" fill="currentColor"/>
                        <circle cx="12" cy="19" r="1.5" fill="currentColor"/>
                      </svg>
                    </button>
                  </div>
                </article>
              ))}
            </div>
          ) : (
            <div className="yazar-empty-state">
              <div className="yazar-empty-icon">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M14 2v6h6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </div>
              <h3 className="yazar-empty-title">Henüz hikaye oluşturmadınız</h3>
              <p className="yazar-empty-text">İlk hikayenizi oluşturarak başlayın</p>
              <Link to="/reader/new-story" className="yazar-empty-btn">
                Yeni hikaye oluştur
              </Link>
            </div>
          )}
        </div>
      </main>
    </div>
  );
};

export default YazarDashboard;
