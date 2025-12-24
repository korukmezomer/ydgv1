import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { yorumAPI, haberAPI } from '../../services/api';
import './Comments.css';

const WriterComments = ({ sidebarOpen, setSidebarOpen }) => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [yorumlar, setYorumlar] = useState([]);
  const [haberler, setHaberler] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedHaber, setSelectedHaber] = useState(null);
  const [filter, setFilter] = useState('all'); // all, pending, approved, rejected

  useEffect(() => {
    if (user?.id) {
      fetchData();
    }
  }, [user]);

  useEffect(() => {
    if (user?.id && selectedHaber) {
      fetchYorumlarForHaber(selectedHaber);
    } else if (user?.id) {
      fetchAllYorumlar();
    }
  }, [user, selectedHaber, filter]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const haberlerResponse = await haberAPI.getAll({ page: 0, size: 100 });
      const userHaberler = haberlerResponse.data.content.filter(
        (h) => h.kullaniciId === user?.id
      );
      setHaberler(userHaberler);
    } catch (error) {
      console.error('Veri yüklenirken hata:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchAllYorumlar = async () => {
    try {
      setLoading(true);
      const response = await yorumAPI.getByYazarId(user.id, { page: 0, size: 100 });
      let filteredYorumlar = response.data?.content || [];
      
      if (filter === 'pending') {
        filteredYorumlar = filteredYorumlar.filter(y => (y.status || y.durum) === 'BEKLIYOR');
      } else if (filter === 'approved') {
        filteredYorumlar = filteredYorumlar.filter(y => (y.status || y.durum) === 'ONAYLANDI');
      } else if (filter === 'rejected') {
        filteredYorumlar = filteredYorumlar.filter(y => (y.status || y.durum) === 'REDDEDILDI');
      }
      
      setYorumlar(filteredYorumlar);
    } catch (error) {
      console.error('Yorumlar yüklenirken hata:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchYorumlarForHaber = async (haberId) => {
    try {
      setLoading(true);
      const response = await yorumAPI.getByYazarIdAndHaberId(user.id, haberId, { page: 0, size: 100 });
      let filteredYorumlar = response.data?.content || [];
      
      if (filter === 'pending') {
        filteredYorumlar = filteredYorumlar.filter(y => (y.status || y.durum) === 'BEKLIYOR');
      } else if (filter === 'approved') {
        filteredYorumlar = filteredYorumlar.filter(y => (y.status || y.durum) === 'ONAYLANDI');
      } else if (filter === 'rejected') {
        filteredYorumlar = filteredYorumlar.filter(y => (y.status || y.durum) === 'REDDEDILDI');
      }
      
      setYorumlar(filteredYorumlar);
    } catch (error) {
      console.error('Yorumlar yüklenirken hata:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const months = ['Oca', 'Şub', 'Mar', 'Nis', 'May', 'Haz', 'Tem', 'Ağu', 'Eyl', 'Eki', 'Kas', 'Ara'];
    return `${months[date.getMonth()]} ${date.getDate()}, ${date.getFullYear()}`;
  };

  const getStatusColor = (durum) => {
    const status = durum || '';
    switch (status) {
      case 'ONAYLANDI':
        return '#10b981';
      case 'BEKLIYOR':
        return '#f59e0b';
      case 'REDDEDILDI':
        return '#ef4444';
      default:
        return '#6b6b6b';
    }
  };

  const getStatusText = (durum) => {
    const status = durum || '';
    switch (status) {
      case 'ONAYLANDI':
        return 'Onaylandı';
      case 'BEKLIYOR':
        return 'Onay bekliyor';
      case 'REDDEDILDI':
        return 'Reddedildi';
      default:
        return durum || 'Bilinmeyen';
    }
  };


  if (loading && yorumlar.length === 0) {
    return (
      <div className={`writer-comments-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
        <div className="loading">Yükleniyor...</div>
      </div>
    );
  }

  return (
    <div className={`writer-comments-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
      <main className="writer-comments-main">
        <div className="writer-comments-container">
          <div className="writer-comments-header">
            <h1 className="writer-comments-title">Yorumlar</h1>
            <div className="writer-comments-filters">
              <button
                className={`filter-btn ${filter === 'all' ? 'active' : ''}`}
                onClick={() => setFilter('all')}
              >
                Tümü
              </button>
              <button
                className={`filter-btn ${filter === 'pending' ? 'active' : ''}`}
                onClick={() => setFilter('pending')}
              >
                Onay bekliyor
              </button>
              <button
                className={`filter-btn ${filter === 'approved' ? 'active' : ''}`}
                onClick={() => setFilter('approved')}
              >
                Onaylandı
              </button>
              <button
                className={`filter-btn ${filter === 'rejected' ? 'active' : ''}`}
                onClick={() => setFilter('rejected')}
              >
                Reddedildi
              </button>
            </div>
          </div>

          {/* Hikaye Filtresi */}
          <div className="writer-comments-haber-filter">
            <label>Hikayeye göre filtrele:</label>
            <select
              value={selectedHaber || ''}
              onChange={(e) => setSelectedHaber(e.target.value ? Number(e.target.value) : null)}
              className="haber-select"
            >
              <option value="">Tüm hikayeler</option>
              {haberler.map((haber) => (
                <option key={haber.id} value={haber.id}>
                  {haber.baslik}
                </option>
              ))}
            </select>
          </div>

          {/* Yorumlar Listesi */}
          {yorumlar.length > 0 ? (
            <div className="writer-comments-list">
              {yorumlar.map((yorum) => (
                <article key={yorum.id} className="writer-comment-item">
                  <div className="writer-comment-header">
                    <div className="writer-comment-author">
                      <div className="writer-comment-avatar">
                        {(yorum.username || yorum.kullaniciAdi || 'K').charAt(0).toUpperCase()}
                      </div>
                      <div className="writer-comment-author-info">
                        <div className="writer-comment-author-name">{yorum.username || yorum.kullaniciAdi || 'Kullanıcı'}</div>
                        <div className="writer-comment-date">{formatDate(yorum.createdAt)}</div>
                      </div>
                    </div>
                    <span
                      className="writer-comment-status"
                      style={{ color: getStatusColor(yorum.status || yorum.durum) }}
                    >
                      {getStatusText(yorum.status || yorum.durum)}
                    </span>
                  </div>
                  <div className="writer-comment-content">
                    <p>{yorum.content || yorum.icerik}</p>
                  </div>
                  <div className="writer-comment-footer">
                    <Link
                      to={`/haberler/${yorum.storySlug || haberler.find(h => h.id === (yorum.storyId || yorum.haberId))?.slug || ''}`}
                      className="writer-comment-haber-link"
                    >
                      {yorum.storyTitle || haberler.find(h => h.id === (yorum.storyId || yorum.haberId))?.baslik || 'Bilinmeyen hikaye'}
                    </Link>
                    <div className="writer-comment-stats">
                      <span>{yorum.likeCount || yorum.begeniSayisi || 0} beğeni</span>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          ) : (
            <div className="writer-comments-empty">
              <div className="writer-comments-empty-icon">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </div>
              <h3 className="writer-comments-empty-title">Henüz yorum yok</h3>
              <p className="writer-comments-empty-text">
                {selectedHaber 
                  ? 'Bu hikayeye henüz yorum yapılmamış.'
                  : 'Hikayelerinize henüz yorum yapılmamış.'}
              </p>
            </div>
          )}
        </div>
      </main>
    </div>
  );
};

export default WriterComments;

