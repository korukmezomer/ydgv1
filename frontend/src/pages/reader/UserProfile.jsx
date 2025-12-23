import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { kullaniciAPI, haberAPI, takipAPI, yazarProfiliAPI } from '../../services/api';
import api from '../../services/api';
import './UserProfile.css';

const UserProfile = ({ sidebarOpen, setSidebarOpen }) => {
  const { userId } = useParams();
  const { user: currentUser } = useAuth();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('home');
  const [kullaniciBilgileri, setKullaniciBilgileri] = useState(null);
  const [yazarProfili, setYazarProfili] = useState(null);
  const [kullaniciHaberleri, setKullaniciHaberleri] = useState([]);
  const [loading, setLoading] = useState(true);
  const [takipciSayisi, setTakipciSayisi] = useState(0);
  const [takipEdilenSayisi, setTakipEdilenSayisi] = useState(0);
  const [takipciler, setTakipciler] = useState([]);
  const [takipEdilenler, setTakipEdilenler] = useState([]);
  const [isFollowing, setIsFollowing] = useState(false);
  const [isFollowingLoading, setIsFollowingLoading] = useState(false);

  useEffect(() => {
    if (userId) {
      fetchData();
    }
  }, [userId]);

  useEffect(() => {
    if (userId && currentUser?.id) {
      checkFollowingStatus();
    }
  }, [userId, currentUser]);

  const checkFollowingStatus = async () => {
    try {
      const response = await takipAPI.takipEdiliyorMu(userId);
      setIsFollowing(response.data === true);
    } catch (error) {
      console.error('Takip durumu kontrol edilirken hata:', error);
    }
  };

  const fetchData = async () => {
    try {
      setLoading(true);
      
      const [kullaniciResponse, profilResponse, haberlerResponse, takipciSayisiRes, takipEdilenSayisiRes, takipcilerRes, takipEdilenlerRes] = await Promise.all([
        kullaniciAPI.getById(userId).catch((err) => {
          if (err.response?.status === 404 || err.response?.status === 400) {
            return { data: null };
          }
          throw err;
        }),
        yazarProfiliAPI.getByKullaniciId(userId)
          .then(response => ({ data: response.data }))
          .catch(() => ({ data: null })),
        haberAPI.getByKullanici(userId, { page: 0, size: 20 }).catch(() => ({ data: { content: [] } })),
        takipAPI.takipciSayisi(userId).catch(() => ({ data: 0 })),
        takipAPI.takipEdilenSayisi(userId).catch(() => ({ data: 0 })),
        takipAPI.getTakipciler(userId).catch(() => ({ data: [] })),
        takipAPI.getTakipEdilenler(userId).catch(() => ({ data: [] }))
      ]);

      setKullaniciBilgileri(kullaniciResponse.data);
      setYazarProfili(profilResponse.data);
      setKullaniciHaberleri(haberlerResponse.data?.content || []);
      setTakipciSayisi(takipciSayisiRes.data || 0);
      setTakipEdilenSayisi(takipEdilenSayisiRes.data || 0);
      setTakipciler(takipcilerRes.data || []);
      setTakipEdilenler(takipEdilenlerRes.data || []);

    } catch (error) {
      console.error('Veri yüklenirken hata:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleFollow = async () => {
    if (!currentUser?.id) {
      navigate('/login');
      return;
    }

    try {
      setIsFollowingLoading(true);
      if (isFollowing) {
        await takipAPI.takibiBirak(userId);
        setIsFollowing(false);
        setTakipciSayisi(prev => Math.max(0, prev - 1));
      } else {
        await takipAPI.takipEt(userId);
        setIsFollowing(true);
        setTakipciSayisi(prev => prev + 1);
      }
    } catch (error) {
      console.error('Takip işlemi hatası:', error);
      alert('Takip işlemi sırasında bir hata oluştu');
    } finally {
      setIsFollowingLoading(false);
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const months = ['Oca', 'Şub', 'Mar', 'Nis', 'May', 'Haz', 'Tem', 'Ağu', 'Eyl', 'Eki', 'Kas', 'Ara'];
    return `${months[date.getMonth()]} ${date.getDate()}`;
  };

  const handleArticleClick = (slug) => {
    navigate(`/haberler/${slug}`);
  };

  const displayName = kullaniciBilgileri?.username || kullaniciBilgileri?.kullaniciAdi || kullaniciBilgileri?.email || 'Kullanıcı';
  const displayEmail = kullaniciBilgileri?.email || '';
  const bio = yazarProfili?.bio || '';
  const isOwnProfile = currentUser?.id === parseInt(userId);

  if (loading) {
    return (
      <div className={`user-profile-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
        <div className="loading">Yükleniyor...</div>
      </div>
    );
  }

  if (!kullaniciBilgileri) {
    return (
      <div className={`user-profile-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
        <div className="error-state">
          <p>Kullanıcı bulunamadı</p>
        </div>
      </div>
    );
  }

  return (
    <div className={`user-profile-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
      <main className="user-profile-main">
        <div className="user-profile-layout">
          {/* Ana İçerik */}
          <div className="user-profile-content">
            <div className="user-profile-header">
              <h1 className="user-profile-name">{displayName}</h1>
            </div>

            {/* Tabs */}
            <div className="user-profile-tabs">
              <button
                className={`user-profile-tab ${activeTab === 'home' ? 'active' : ''}`}
                onClick={() => setActiveTab('home')}
              >
                Ana Sayfa
              </button>
              <button
                className={`user-profile-tab ${activeTab === 'about' ? 'active' : ''}`}
                onClick={() => setActiveTab('about')}
              >
                Hakkında
              </button>
              <button
                className={`user-profile-tab ${activeTab === 'followers' ? 'active' : ''}`}
                onClick={() => setActiveTab('followers')}
              >
                Takipçiler ({takipciSayisi})
              </button>
              <button
                className={`user-profile-tab ${activeTab === 'following' ? 'active' : ''}`}
                onClick={() => setActiveTab('following')}
              >
                Takip Edilenler ({takipEdilenSayisi})
              </button>
            </div>

            {/* Content */}
            {activeTab === 'home' && (
              <div className="user-profile-section">
                {/* User Stories */}
                {kullaniciHaberleri.length > 0 ? (
                  <div className="user-stories-section">
                    <h2 className="section-title">Yazılar</h2>
                    <div className="stories-list">
                      {kullaniciHaberleri.map((haber) => (
                        <article 
                          key={haber.id} 
                          className="story-card"
                          onClick={() => handleArticleClick(haber.slug)}
                        >
                          <div className="story-content">
                            <h3 className="story-title">{haber.baslik || haber.title}</h3>
                            <p className="story-excerpt">{haber.ozet || haber.summary}</p>
                            <div className="story-meta">
                              <span className="story-date">{formatDate(haber.createdAt)}</span>
                              <span className="story-read-time">• {haber.okunmaSayisi || haber.viewCount || 0} dk okuma</span>
                            </div>
                          </div>
                          {(haber.kapakResmiUrl || haber.coverImageUrl) && (
                            <div className="story-image">
                              <img src={haber.kapakResmiUrl || haber.coverImageUrl} alt={haber.baslik || haber.title} />
                            </div>
                          )}
                        </article>
                      ))}
                    </div>
                  </div>
                ) : (
                  <div className="empty-state">
                    <p>Henüz yazı bulunmamaktadır.</p>
                  </div>
                )}
              </div>
            )}

            {activeTab === 'about' && (
              <div className="user-profile-section">
                <div className="about-content">
                  {bio ? (
                    <p className="bio-text">{bio}</p>
                  ) : (
                    <p className="bio-empty">Henüz bir biyografi eklenmemiş.</p>
                  )}
                  <div className="about-meta">
                    {displayEmail && (
                      <div className="meta-item">
                        <span className="meta-label">E-posta:</span>
                        <span className="meta-value">{displayEmail}</span>
                      </div>
                    )}
                    {kullaniciBilgileri?.firstName && (
                      <div className="meta-item">
                        <span className="meta-label">Ad:</span>
                        <span className="meta-value">{kullaniciBilgileri.firstName}</span>
                      </div>
                    )}
                    {kullaniciBilgileri?.lastName && (
                      <div className="meta-item">
                        <span className="meta-label">Soyad:</span>
                        <span className="meta-value">{kullaniciBilgileri.lastName}</span>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'followers' && (
              <div className="user-profile-section">
                <h2 className="section-title">Takipçiler ({takipciSayisi})</h2>
                {takipciler.length > 0 ? (
                  <div className="users-list">
                    {takipciler.map((takipci) => (
                      <div 
                        key={takipci.id} 
                        className="user-card"
                        onClick={() => navigate(`/reader/user/${takipci.id}`)}
                      >
                        <div className="user-avatar">
                          {(takipci.username || takipci.kullaniciAdi || takipci.email || 'U').charAt(0).toUpperCase()}
                        </div>
                        <div className="user-info">
                          <div className="user-name">{takipci.username || takipci.kullaniciAdi || takipci.email || 'Kullanıcı'}</div>
                          {takipci.firstName && takipci.lastName && (
                            <div className="user-full-name">{takipci.firstName} {takipci.lastName}</div>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="empty-state">
                    <p>Henüz takipçi yok.</p>
                  </div>
                )}
              </div>
            )}

            {activeTab === 'following' && (
              <div className="user-profile-section">
                <h2 className="section-title">Takip Edilenler ({takipEdilenSayisi})</h2>
                {takipEdilenler.length > 0 ? (
                  <div className="users-list">
                    {takipEdilenler.map((takipEdilen) => (
                      <div 
                        key={takipEdilen.id} 
                        className="user-card"
                        onClick={() => navigate(`/reader/user/${takipEdilen.id}`)}
                      >
                        <div className="user-avatar">
                          {(takipEdilen.username || takipEdilen.kullaniciAdi || takipEdilen.email || 'U').charAt(0).toUpperCase()}
                        </div>
                        <div className="user-info">
                          <div className="user-name">{takipEdilen.username || takipEdilen.kullaniciAdi || takipEdilen.email || 'Kullanıcı'}</div>
                          {takipEdilen.firstName && takipEdilen.lastName && (
                            <div className="user-full-name">{takipEdilen.firstName} {takipEdilen.lastName}</div>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="empty-state">
                    <p>Henüz kimseyi takip etmiyor.</p>
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Sağ Sidebar */}
          <aside className="user-profile-sidebar">
            <div className="user-profile-card">
              <div className="user-profile-avatar-large">
                {displayName.charAt(0).toUpperCase()}
              </div>
              <h2 className="user-profile-name-sidebar">{displayName}</h2>
              
              {!isOwnProfile && currentUser?.id && (
                <button 
                  className={`follow-button ${isFollowing ? 'following' : ''}`}
                  onClick={handleFollow}
                  disabled={isFollowingLoading}
                >
                  {isFollowingLoading ? 'Yükleniyor...' : isFollowing ? 'Takip Ediliyor' : 'Takip Et'}
                </button>
              )}

              {isOwnProfile && (
                <button 
                  className="edit-profile-button"
                  onClick={() => navigate('/reader/profile')}
                >
                  Profili görüntüle
                </button>
              )}
              
              {/* Takip İstatistikleri */}
              <div className="user-profile-stats">
                <div className="stat-item" onClick={() => setActiveTab('followers')}>
                  <div className="stat-number">{takipciSayisi}</div>
                  <div className="stat-label">Takipçi</div>
                </div>
                <div className="stat-item" onClick={() => setActiveTab('following')}>
                  <div className="stat-number">{takipEdilenSayisi}</div>
                  <div className="stat-label">Takip Edilen</div>
                </div>
              </div>
            </div>
          </aside>
        </div>
      </main>
    </div>
  );
};

export default UserProfile;

