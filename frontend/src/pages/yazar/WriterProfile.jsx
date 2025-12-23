import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { takipAPI } from '../../services/api';
import api from '../../services/api';
import './WriterProfile.css';

const WriterProfile = ({ sidebarOpen, setSidebarOpen }) => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('home');
  const [kullaniciBilgileri, setKullaniciBilgileri] = useState(null);
  const [yazarProfili, setYazarProfili] = useState(null);
  const [kullaniciStories, setKullaniciStories] = useState([]);
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
      
      const [kullaniciResponse, profilResponse, storiesResponse, takipciSayisiRes, takipEdilenSayisiRes, takipcilerRes, takipEdilenlerRes] = await Promise.all([
        api.get(`/kullanicilar/${user.id}`).catch((err) => {
          if (err.response?.status === 404 || err.response?.status === 400) {
            return { data: null };
          }
          throw err;
        }),
        // Yazar profili WRITER rolündeki kullanıcılar için var olabilir
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
        api.get(`/haberler/kullanici/${user.id}`, { params: { page: 0, size: 20 } }).catch(() => ({ data: { content: [] } })),
        takipAPI.takipciSayisi(user.id).catch(() => ({ data: 0 })),
        takipAPI.takipEdilenSayisi(user.id).catch(() => ({ data: 0 })),
        takipAPI.getTakipciler(user.id).catch(() => ({ data: [] })),
        takipAPI.getTakipEdilenler(user.id).catch(() => ({ data: [] }))
      ]);

      setKullaniciBilgileri(kullaniciResponse.data);
      setYazarProfili(profilResponse.data);
      setKullaniciStories(storiesResponse.data?.content || []);
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
    return `${months[date.getMonth()]} ${date.getDate()}, ${date.getFullYear()}`;
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
    <div className={`writer-profile-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
      <main className="writer-profile-main">
        <div className="writer-profile-layout">
          {/* Ana İçerik */}
          <div className="writer-profile-content">
            <div className="writer-profile-header">
              <h1 className="writer-profile-name">{displayName}</h1>
              <button className="writer-profile-menu">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                  <circle cx="12" cy="5" r="1.5" fill="currentColor"/>
                  <circle cx="12" cy="12" r="1.5" fill="currentColor"/>
                  <circle cx="12" cy="19" r="1.5" fill="currentColor"/>
                </svg>
              </button>
            </div>

            {/* Tabs */}
            <div className="writer-profile-tabs">
              <button
                className={`writer-profile-tab ${activeTab === 'home' ? 'active' : ''}`}
                onClick={() => setActiveTab('home')}
              >
                Ana Sayfa
              </button>
              <button
                className={`writer-profile-tab ${activeTab === 'about' ? 'active' : ''}`}
                onClick={() => setActiveTab('about')}
              >
                Hakkında
              </button>
              <button
                className={`writer-profile-tab ${activeTab === 'followers' ? 'active' : ''}`}
                onClick={() => setActiveTab('followers')}
              >
                Takipçiler ({takipciSayisi})
              </button>
              <button
                className={`writer-profile-tab ${activeTab === 'following' ? 'active' : ''}`}
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
                  <div className="writer-profile-section">
                    {/* User Stories */}
                    {kullaniciStories.length > 0 ? (
                      <div className="writer-user-stories-section">
                        <h2 className="writer-section-title">Yazılarım</h2>
                        <div className="writer-stories-list">
                          {kullaniciStories.map((story) => (
                            <article 
                              key={story.id} 
                              className="writer-story-card"
                              onClick={() => handleArticleClick(story.slug)}
                            >
                              <div className="writer-story-content">
                                <h3 className="writer-story-title">{story.baslik}</h3>
                                <p className="writer-story-excerpt">{story.ozet}</p>
                                <div className="writer-story-meta">
                                  <span className={`writer-story-status status-${story.durum?.toLowerCase()}`}>
                                    {story.durum?.replace('_', ' ')}
                                  </span>
                                  <span className="writer-story-date">{formatDate(story.createdAt)}</span>
                                  <span className="writer-story-read-time">• {story.okunmaSayisi || 0} okunma</span>
                                  <span className="writer-story-likes">• {story.begeniSayisi || 0} beğeni</span>
                                </div>
                              </div>
                              {story.kapakResmiUrl && (
                                <div className="writer-story-image">
                                  <img src={story.kapakResmiUrl} alt={story.baslik} />
                                </div>
                              )}
                            </article>
                          ))}
                        </div>
                      </div>
                    ) : (
                      <div className="writer-profile-empty">
                        <p>Henüz hikaye oluşturmadınız.</p>
                        <button 
                          className="writer-create-story-button"
                          onClick={() => navigate('/reader/new-story')}
                        >
                          İlk hikayenizi oluşturun
                        </button>
                      </div>
                    )}
                  </div>
                )}

                {activeTab === 'about' && (
                  <div className="writer-profile-section">
                    <div className="writer-about-content">
                      {bio ? (
                        <p className="writer-bio-text">{bio}</p>
                      ) : (
                        <p className="writer-bio-empty">Henüz bir biyografi eklenmemiş.</p>
                      )}
                      <div className="writer-about-meta">
                        <div className="writer-meta-item">
                          <span className="writer-meta-label">E-posta:</span>
                          <span className="writer-meta-value">{displayEmail}</span>
                        </div>
                        {kullaniciBilgileri?.ad && (
                          <div className="writer-meta-item">
                            <span className="writer-meta-label">Ad:</span>
                            <span className="writer-meta-value">{kullaniciBilgileri.ad}</span>
                          </div>
                        )}
                        {kullaniciBilgileri?.soyad && (
                          <div className="writer-meta-item">
                            <span className="writer-meta-label">Soyad:</span>
                            <span className="writer-meta-value">{kullaniciBilgileri.soyad}</span>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                )}

                {activeTab === 'followers' && (
                  <div className="writer-profile-section">
                    <h2 className="writer-section-title">Takipçiler ({takipciSayisi})</h2>
                    {takipciler.length > 0 ? (
                      <div className="writer-users-list">
                        {takipciler.map((takipci) => (
                          <div key={takipci.id} className="writer-user-card">
                            <div className="writer-user-avatar">
                              {(takipci.kullaniciAdi || takipci.email || 'U').charAt(0).toUpperCase()}
                            </div>
                            <div className="writer-user-info">
                              <div className="writer-user-name">{takipci.kullaniciAdi || takipci.email || 'Kullanıcı'}</div>
                              {takipci.ad && takipci.soyad && (
                                <div className="writer-user-full-name">{takipci.ad} {takipci.soyad}</div>
                              )}
                            </div>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <div className="writer-empty-state">
                        <p>Henüz takipçiniz yok.</p>
                      </div>
                    )}
                  </div>
                )}

                {activeTab === 'following' && (
                  <div className="writer-profile-section">
                    <h2 className="writer-section-title">Takip Edilenler ({takipEdilenSayisi})</h2>
                    {takipEdilenler.length > 0 ? (
                      <div className="writer-users-list">
                        {takipEdilenler.map((takipEdilen) => (
                          <div key={takipEdilen.id} className="writer-user-card">
                            <div className="writer-user-avatar">
                              {(takipEdilen.kullaniciAdi || takipEdilen.email || 'U').charAt(0).toUpperCase()}
                            </div>
                            <div className="writer-user-info">
                              <div className="writer-user-name">{takipEdilen.kullaniciAdi || takipEdilen.email || 'Kullanıcı'}</div>
                              {takipEdilen.ad && takipEdilen.soyad && (
                                <div className="writer-user-full-name">{takipEdilen.ad} {takipEdilen.soyad}</div>
                              )}
                            </div>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <div className="writer-empty-state">
                        <p>Henüz kimseyi takip etmiyorsunuz.</p>
                      </div>
                    )}
                  </div>
                )}
              </>
            )}
          </div>

          {/* Sağ Sidebar */}
          <aside className="writer-profile-sidebar">
            <div className="writer-profile-card">
              <div className="writer-profile-avatar-large">
                {displayName.charAt(0).toUpperCase()}
              </div>
              <h2 className="writer-profile-name-sidebar">{displayName}</h2>
              <button className="writer-edit-profile-button" onClick={handleEditProfile}>Profili düzenle</button>
              
              {/* Takip İstatistikleri */}
              <div className="writer-profile-stats">
                <div className="writer-stat-item" onClick={() => setActiveTab('followers')}>
                  <div className="writer-stat-number">{takipciSayisi}</div>
                  <div className="writer-stat-label">Takipçi</div>
                </div>
                <div className="writer-stat-item" onClick={() => setActiveTab('following')}>
                  <div className="writer-stat-number">{takipEdilenSayisi}</div>
                  <div className="writer-stat-label">Takip Edilen</div>
                </div>
              </div>
            </div>
          </aside>
        </div>
      </main>
    </div>
  );
};

export default WriterProfile;

