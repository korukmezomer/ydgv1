import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { kayitliHaberAPI, takipAPI } from '../../services/api';
import api from '../../services/api';
import './Profile.css';

const Profile = ({ sidebarOpen, setSidebarOpen }) => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('home');
  const [kullaniciBilgileri, setKullaniciBilgileri] = useState(null);
  const [yazarProfili, setYazarProfili] = useState(null);
  const [kayitliHaberler, setKayitliHaberler] = useState([]);
  const [kullaniciHaberleri, setKullaniciHaberleri] = useState([]);
  const [loading, setLoading] = useState(true);
  const [takipciSayisi, setTakipciSayisi] = useState(0);
  const [takipEdilenSayisi, setTakipEdilenSayisi] = useState(0);
  const [takipciler, setTakipciler] = useState([]);
  const [takipEdilenler, setTakipEdilenler] = useState([]);

  useEffect(() => {
    if (user?.id) {
      fetchData();
    }
  }, [user]);

  const fetchData = async () => {
    try {
      setLoading(true);
      
      const [kullaniciResponse, profilResponse, kayitliResponse, haberlerResponse, takipciSayisiRes, takipEdilenSayisiRes, takipcilerRes, takipEdilenlerRes] = await Promise.all([
        api.get(`/kullanicilar/${user.id}`).catch((err) => {
          if (err.response?.status === 404 || err.response?.status === 400) {
            return { data: null };
          }
          throw err;
        }),
        // Yazar profili sadece WRITER rolündeki kullanıcılar için var olabilir
        // Profil yoksa null döner, hata fırlatmaz
        api.get(`/yazar-profilleri/kullanici/${user.id}`)
          .then(response => ({ data: response.data }))
          .catch((err) => {
            // 404 veya diğer hatalar için null döndür
            if (err.response?.status === 404 || err.response?.status === 400) {
              return { data: null };
            }
            // Diğer hatalar için de null döndür (profil yoksa normal)
            return { data: null };
          }),
        kayitliHaberAPI.getAll({ page: 0, size: 10 }).catch(() => ({ data: { content: [] } })),
        api.get(`/haberler/kullanici/${user.id}`, { params: { page: 0, size: 10 } }).catch(() => ({ data: { content: [] } })),
        takipAPI.takipciSayisi(user.id).catch(() => ({ data: 0 })),
        takipAPI.takipEdilenSayisi(user.id).catch(() => ({ data: 0 })),
        takipAPI.getTakipciler(user.id).catch(() => ({ data: [] })),
        takipAPI.getTakipEdilenler(user.id).catch(() => ({ data: [] }))
      ]);

      setKullaniciBilgileri(kullaniciResponse.data);
      setYazarProfili(profilResponse.data);
      
      const kayitliHaberler = (kayitliResponse.data?.content || []).map(item => item.haber || item);
      setKayitliHaberler(kayitliHaberler);
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

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const months = ['Oca', 'Şub', 'Mar', 'Nis', 'May', 'Haz', 'Tem', 'Ağu', 'Eyl', 'Eki', 'Kas', 'Ara'];
    return `${months[date.getMonth()]} ${date.getDate()}`;
  };

  const handleArticleClick = (slug) => {
    navigate(`/haberler/${slug}`);
  };

  const handleEditProfile = () => {
    navigate('/reader/settings?modal=profile');
  };


  const displayName = kullaniciBilgileri?.kullaniciAdi || kullaniciBilgileri?.email || user?.email || 'Kullanıcı';
  const displayEmail = kullaniciBilgileri?.email || user?.email || '';
  const bio = yazarProfili?.bio || '';

  return (
    <div className={`profile-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
      <main className="profile-main">
        <div className="profile-layout">
          {/* Ana İçerik */}
          <div className="profile-content">
            <div className="profile-header">
              <h1 className="profile-name">{displayName}</h1>
              <button className="profile-menu">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                  <circle cx="12" cy="5" r="1.5" fill="currentColor"/>
                  <circle cx="12" cy="12" r="1.5" fill="currentColor"/>
                  <circle cx="12" cy="19" r="1.5" fill="currentColor"/>
                </svg>
              </button>
            </div>

            {/* Tabs */}
            <div className="profile-tabs">
              <button
                className={`profile-tab ${activeTab === 'home' ? 'active' : ''}`}
                onClick={() => setActiveTab('home')}
              >
                Ana Sayfa
              </button>
              <button
                className={`profile-tab ${activeTab === 'about' ? 'active' : ''}`}
                onClick={() => setActiveTab('about')}
              >
                Hakkında
              </button>
              <button
                className={`profile-tab ${activeTab === 'followers' ? 'active' : ''}`}
                onClick={() => setActiveTab('followers')}
              >
                Takipçiler ({takipciSayisi})
              </button>
              <button
                className={`profile-tab ${activeTab === 'following' ? 'active' : ''}`}
                onClick={() => setActiveTab('following')}
              >
                Takip Edilenler ({takipEdilenSayisi})
              </button>
            </div>

            {/* Content */}
            {loading ? (
              <div className="loading">Yükleniyor...</div>
            ) : (
              <>
                {activeTab === 'home' && (
                  <div className="profile-section">
                    {/* Reading List */}
                    <div className="reading-list-section">
                      <div className="section-header">
                        <h2 className="section-title">Okuma listesi</h2>
                      </div>
                      <div className="reading-list-card">
                        <div className="list-author-info">
                          <div className="author-avatar-small">
                            {displayName.charAt(0).toUpperCase()}
                          </div>
                          <div className="author-name-small">{displayName}</div>
                        </div>
                        {kayitliHaberler.length > 0 ? (
                          <div className="list-stories-grid">
                            {kayitliHaberler.slice(0, 3).map((haber) => (
                              <div 
                                key={haber.id} 
                                className="story-placeholder"
                                onClick={() => handleArticleClick(haber.slug)}
                              >
                                {haber.kapakResmiUrl ? (
                                  <img src={haber.kapakResmiUrl} alt={haber.baslik} />
                                ) : (
                                  <div className="placeholder-content">
                                    <h3>{haber.baslik}</h3>
                                  </div>
                                )}
                              </div>
                            ))}
                          </div>
                        ) : (
                          <div className="list-empty">
                            <p>Hikaye yok</p>
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                              <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" strokeWidth="2"/>
                              <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" strokeWidth="2"/>
                            </svg>
                          </div>
                        )}
                      </div>
                    </div>

                    {/* User Stories */}
                    {kullaniciHaberleri.length > 0 && (
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
                                <h3 className="story-title">{haber.baslik}</h3>
                                <p className="story-excerpt">{haber.ozet}</p>
                                <div className="story-meta">
                                  <span className="story-date">{formatDate(haber.createdAt)}</span>
                                  <span className="story-read-time">• {haber.okunmaSayisi || 0} dk okuma</span>
                                </div>
                              </div>
                              {haber.kapakResmiUrl && (
                                <div className="story-image">
                                  <img src={haber.kapakResmiUrl} alt={haber.baslik} />
                                </div>
                              )}
                            </article>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                )}

                {activeTab === 'about' && (
                  <div className="profile-section">
                    <div className="about-content">
                      {bio ? (
                        <p className="bio-text">{bio}</p>
                      ) : (
                        <p className="bio-empty">Henüz bir biyografi eklenmemiş.</p>
                      )}
                      <div className="about-meta">
                        <div className="meta-item">
                          <span className="meta-label">E-posta:</span>
                          <span className="meta-value">{displayEmail}</span>
                        </div>
                        {kullaniciBilgileri?.ad && (
                          <div className="meta-item">
                            <span className="meta-label">Ad:</span>
                            <span className="meta-value">{kullaniciBilgileri.ad}</span>
                          </div>
                        )}
                        {kullaniciBilgileri?.soyad && (
                          <div className="meta-item">
                            <span className="meta-label">Soyad:</span>
                            <span className="meta-value">{kullaniciBilgileri.soyad}</span>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                )}

                {activeTab === 'followers' && (
                  <div className="profile-section">
                    <h2 className="section-title">Takipçiler ({takipciSayisi})</h2>
                    {takipciler.length > 0 ? (
                      <div className="users-list">
                        {takipciler.map((takipci) => (
                          <div key={takipci.id} className="user-card">
                            <div className="user-avatar">
                              {(takipci.kullaniciAdi || takipci.email || 'U').charAt(0).toUpperCase()}
                            </div>
                            <div className="user-info">
                              <div className="user-name">{takipci.kullaniciAdi || takipci.email || 'Kullanıcı'}</div>
                              {takipci.ad && takipci.soyad && (
                                <div className="user-full-name">{takipci.ad} {takipci.soyad}</div>
                              )}
                            </div>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <div className="empty-state">
                        <p>Henüz takipçiniz yok.</p>
                      </div>
                    )}
                  </div>
                )}

                {activeTab === 'following' && (
                  <div className="profile-section">
                    <h2 className="section-title">Takip Edilenler ({takipEdilenSayisi})</h2>
                    {takipEdilenler.length > 0 ? (
                      <div className="users-list">
                        {takipEdilenler.map((takipEdilen) => (
                          <div key={takipEdilen.id} className="user-card">
                            <div className="user-avatar">
                              {(takipEdilen.kullaniciAdi || takipEdilen.email || 'U').charAt(0).toUpperCase()}
                            </div>
                            <div className="user-info">
                              <div className="user-name">{takipEdilen.kullaniciAdi || takipEdilen.email || 'Kullanıcı'}</div>
                              {takipEdilen.ad && takipEdilen.soyad && (
                                <div className="user-full-name">{takipEdilen.ad} {takipEdilen.soyad}</div>
                              )}
                            </div>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <div className="empty-state">
                        <p>Henüz kimseyi takip etmiyorsunuz.</p>
                      </div>
                    )}
                  </div>
                )}
              </>
            )}
          </div>

          {/* Sağ Sidebar */}
          <aside className="profile-sidebar">
            <div className="profile-card">
              <div className="profile-avatar-large">
                {displayName.charAt(0).toUpperCase()}
              </div>
              <h2 className="profile-name-sidebar">{displayName}</h2>
              <button className="edit-profile-button" onClick={handleEditProfile}>Profili düzenle</button>
              
              {/* Takip İstatistikleri */}
              <div className="profile-stats">
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

export default Profile;

