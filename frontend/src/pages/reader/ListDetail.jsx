import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';
import './ListDetail.css';

const ListDetail = ({ sidebarOpen, setSidebarOpen }) => {
  const { slug } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [liste, setListe] = useState(null);
  const [haberler, setHaberler] = useState([]);

  useEffect(() => {
    if (slug) {
      fetchListe();
    }
  }, [slug]);

  const fetchListe = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/listeler/slug/${slug}`).catch(() => ({ data: null }));
      
      if (response.data) {
        setListe(response.data);
        setHaberler(response.data.haberler || []);
      }
    } catch (error) {
      console.error('Liste yüklenirken hata:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    return `${months[date.getMonth()]} ${date.getDate()}, ${date.getFullYear()}`;
  };

  const displayName = liste?.kullaniciAdi || user?.kullaniciAdi || user?.email || 'Kullanıcı';

  if (loading) {
    return (
      <div className={`list-detail-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
        <div className="loading">Yükleniyor...</div>
      </div>
    );
  }

  if (!liste) {
    return (
      <div className={`list-detail-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
        <div className="list-not-found">
          <p>Liste bulunamadı</p>
        </div>
      </div>
    );
  }

  return (
    <div className={`list-detail-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
      <main className="list-detail-main">
        <div className="list-detail-container">
          {/* List Header */}
          <div className="list-detail-header">
            <div className="list-detail-author">
              <div className="list-detail-avatar">
                {displayName.charAt(0).toUpperCase()}
              </div>
              <div className="list-detail-author-info">
                <div className="list-detail-author-name">{displayName}</div>
                <div className="list-detail-date">{formatDate(liste.createdAt)}</div>
              </div>
            </div>
            <div className="list-detail-actions">
              <button className="list-detail-action-btn" title="Kaydet">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </button>
              <button className="list-detail-action-btn" title="Yorum">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </button>
              <button className="list-detail-action-btn" title="Daha fazla">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <circle cx="12" cy="5" r="1.5" fill="currentColor"/>
                  <circle cx="12" cy="12" r="1.5" fill="currentColor"/>
                  <circle cx="12" cy="19" r="1.5" fill="currentColor"/>
                </svg>
              </button>
            </div>
          </div>

          {/* List Title */}
          <h1 className="list-detail-title">{liste.ad}</h1>

          {/* List Content */}
          {haberler.length > 0 ? (
            <div className="list-detail-stories">
              {haberler.map((haber) => (
                <article 
                  key={haber.id} 
                  className="list-detail-story"
                  onClick={() => navigate(`/haberler/${haber.slug}`)}
                >
                  <div className="list-detail-story-content">
                    <h2 className="list-detail-story-title">{haber.baslik}</h2>
                    <p className="list-detail-story-excerpt">{haber.ozet || haber.icerik?.substring(0, 200)}</p>
                    <div className="list-detail-story-meta">
                      <span className="list-detail-story-author">{haber.kullaniciAdi || 'Yazar'}</span>
                      <span className="list-detail-story-date">{formatDate(haber.createdAt)}</span>
                    </div>
                  </div>
                  {haber.kapakResmiUrl && (
                    <div className="list-detail-story-image">
                      <img src={haber.kapakResmiUrl} alt={haber.baslik} />
                    </div>
                  )}
                </article>
              ))}
            </div>
          ) : (
            <div className="list-detail-empty">
              <div className="list-detail-empty-box">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" className="list-detail-empty-icon">
                  <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
                <p className="list-detail-empty-text">
                  Favori hikayelerinizi listenize ekleyin. Herhangi bir Medium hikayesindeki{' '}
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" style={{ display: 'inline', verticalAlign: 'middle' }}>
                    <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
                  {' '}ikonuna tıklayarak başlayın.
                </p>
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  );
};

export default ListDetail;

