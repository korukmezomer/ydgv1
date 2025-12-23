import { useState, useEffect } from 'react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { haberAPI, begeniAPI, kategoriAPI, kayitliHaberAPI, takipAPI } from '../../services/api';
import api from '../../services/api';
import AddToListModal from '../../components/AddToListModal';
import './Dashboard.css';

const ReaderDashboard = ({ sidebarOpen, setSidebarOpen }) => {
  const { user } = useAuth();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [forYouHaberler, setForYouHaberler] = useState([]);
  const [featuredHaberler, setFeaturedHaberler] = useState([]);
  const [staffPicks, setStaffPicks] = useState([]);
  const [kategoriler, setKategoriler] = useState([]);
  const [followingStories, setFollowingStories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('foryou');
  const [begenilenHaberler, setBegenilenHaberler] = useState(new Set());
  const [showAddToListModal, setShowAddToListModal] = useState(false);
  const [selectedStoryId, setSelectedStoryId] = useState(null);
  const [addToListPosition, setAddToListPosition] = useState(null);

  useEffect(() => {
    fetchData();
    if (user?.id) {
      fetchFollowingStories();
    }
  }, [user]);

  useEffect(() => {
    const tab = searchParams.get('tab') || 'foryou';
    setActiveTab(tab);
  }, [searchParams]);
  
  // URL'deki tab değiştiğinde içeriği güncelle
  useEffect(() => {
    const tab = searchParams.get('tab') || 'foryou';
    if (tab === 'featured' && activeTab !== 'featured') {
      setActiveTab('featured');
    } else if (tab === 'foryou' && activeTab !== 'foryou') {
      setActiveTab('foryou');
    }
  }, [searchParams, activeTab]);

  const fetchData = async () => {
    try {
      setLoading(true);
      
      // Paralel olarak tüm verileri çek
      const [
        forYouResponse,
        featuredResponse,
        populerResponse,
        kategorilerResponse
      ] = await Promise.all([
        // For You: Son yayınlanan haberler
        haberAPI.getAll({ page: 0, size: 20, sortBy: 'createdAt', sortDir: 'DESC' }),
        // Featured: Popüler haberler
        haberAPI.getPopular({ page: 0, size: 10 }),
        // Staff Picks: Editör seçimleri
        haberAPI.getEditorPicks({ page: 0, size: 5 }),
        // Kategoriler
        kategoriAPI.getAll()
      ]);

      const forYouHaberler = forYouResponse.data.content || [];
      const featuredHaberler = featuredResponse.data.content || [];

      setForYouHaberler(forYouHaberler);
      setFeaturedHaberler(featuredHaberler);
      setStaffPicks(populerResponse.data.content || []);
      setKategoriler(kategorilerResponse.data || []);

      // Her haber için beğeni durumunu kontrol et
      const begenilenIds = new Set();
      await Promise.all(
        [...forYouHaberler, ...featuredHaberler].map(async (haber) => {
          try {
            const response = await begeniAPI.begenildiMi(haber.id);
            if (response.data === true) {
              begenilenIds.add(haber.id);
            }
          } catch (error) {
            // Hata durumunda beğenilmemiş say
          }
        })
      );
      setBegenilenHaberler(begenilenIds);

    } catch (error) {
      console.error('Veri yüklenirken hata:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchFollowingStories = async () => {
    try {
      if (!user?.id) return;

      // Takip edilen kullanıcıları al
      const followingResponse = await takipAPI.getTakipEdilenler(user.id);
      const followingUsers = followingResponse.data || [];

      if (followingUsers.length === 0) {
        setFollowingStories([]);
        return;
      }

      // Her takip edilen kullanıcının son yazılarını al
      const allStories = [];
      await Promise.all(
        followingUsers.map(async (followedUser) => {
          try {
            const storiesResponse = await haberAPI.getByKullanici(followedUser.id, { page: 0, size: 3 });
            const stories = (storiesResponse.data?.content || []).map(story => ({
              ...story,
              authorName: followedUser.username || followedUser.kullaniciAdi || followedUser.email,
              authorId: followedUser.id
            }));
            allStories.push(...stories);
          } catch (error) {
            console.error(`Kullanıcı ${followedUser.id} yazıları yüklenirken hata:`, error);
          }
        })
      );

      // Tarihe göre sırala (en yeni önce)
      allStories.sort((a, b) => {
        const dateA = new Date(a.createdAt || a.publishedAt);
        const dateB = new Date(b.createdAt || b.publishedAt);
        return dateB - dateA;
      });

      // En son 5 yazıyı al
      setFollowingStories(allStories.slice(0, 5));
    } catch (error) {
      console.error('Takip edilen kullanıcıların yazıları yüklenirken hata:', error);
    }
  };

  const handleBegen = async (haberId, e) => {
    e.preventDefault();
    e.stopPropagation();
    try {
      if (begenilenHaberler.has(haberId)) {
        await begeniAPI.begeniyiKaldir(haberId);
        setBegenilenHaberler(prev => {
          const newSet = new Set(prev);
          newSet.delete(haberId);
          return newSet;
        });
      } else {
        await begeniAPI.begen(haberId);
        setBegenilenHaberler(prev => new Set(prev).add(haberId));
      }
    } catch (error) {
      console.error('Beğeni hatası:', error);
    }
  };

  const handleKaydet = async (haberId, e) => {
    e.preventDefault();
    e.stopPropagation();
    try {
      await kayitliHaberAPI.kaydet(haberId);
      fetchData();
    } catch (error) {
      console.error('Kaydetme hatası:', error);
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

  return (
    <div className={`reader-dashboard ${sidebarOpen ? 'sidebar-open' : ''}`}>
      <main className="reader-main">
        <div className="reader-layout">
          {/* Ana İçerik Alanı */}
          <div className="reader-content">
            {/* Tabs */}
            <div className="content-tabs">
              <button
                className={`content-tab ${activeTab === 'foryou' ? 'active' : ''}`}
                onClick={() => {
                  setActiveTab('foryou');
                  navigate('/reader/dashboard?tab=foryou');
                }}
              >
                Senin için
              </button>
              <button
                className={`content-tab ${activeTab === 'featured' ? 'active' : ''}`}
                onClick={() => {
                  setActiveTab('featured');
                  navigate('/reader/dashboard?tab=featured');
                }}
              >
                Öne Çıkanlar
              </button>
            </div>

            {/* Content */}
            {loading ? (
              <div className="loading">Yükleniyor...</div>
            ) : (
              <>
                {activeTab === 'foryou' && (
                  <div className="articles-container">
                    {forYouHaberler.length > 0 ? (
                      forYouHaberler.map((haber) => (
                        <article 
                          key={haber.id} 
                          className="article-card"
                          onClick={() => handleArticleClick(haber.slug)}
                        >
                          <div className="article-content">
                            <div className="article-header">
                              {haber.kategoriAdi && (
                                <span className="article-publication">
                                  In {haber.kategoriAdi}
                                </span>
                              )}
                              {haber.kullaniciAdi && (
                                <span className="article-author">by {haber.kullaniciAdi}</span>
                              )}
                            </div>
                            <h2 className="article-title">{haber.baslik}</h2>
                            <p className="article-excerpt">{haber.ozet}</p>
                            <div className="article-footer">
                              <div className="article-meta">
                                <span className="article-date">{formatDate(haber.createdAt)}</span>
                                <span className="article-read-time">• {haber.okunmaSayisi || 0} dk okuma</span>
                                <div className="article-engagement">
                                  <span className="engagement-item">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                                      <path d="M7.5 21L12 18l4.5 3-1.5-5.5L20 12l-5.5-.5L12 6l-2.5 5.5L4 12l4.5 3.5L7.5 21z" fill="currentColor"/>
                                    </svg>
                                    {haber.begeniSayisi || 0}
                                  </span>
                                  <span className="engagement-item">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                                      <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" stroke="currentColor" strokeWidth="2"/>
                                    </svg>
                                    {haber.yorumSayisi || 0}
                                  </span>
                                </div>
                              </div>
                              <div className="article-actions">
                                <button 
                                  className={`article-action-btn ${begenilenHaberler.has(haber.id) ? 'active' : ''}`}
                                  onClick={(e) => handleBegen(haber.id, e)}
                                  title="Beğen"
                                >
                                  <svg width="20" height="20" viewBox="0 0 24 24" fill={begenilenHaberler.has(haber.id) ? "currentColor" : "none"}>
                                    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" stroke="currentColor" strokeWidth="2"/>
                                  </svg>
                                </button>
                                <button 
                                  className="article-action-btn"
                                  onClick={(e) => {
                                    e.preventDefault();
                                    e.stopPropagation();
                                    const rect = e.currentTarget.getBoundingClientRect();
                                    setAddToListPosition({
                                      top: rect.bottom + 8,
                                      left: rect.left - 200
                                    });
                                    setSelectedStoryId(haber.id);
                                    setShowAddToListModal(true);
                                  }}
                                  title="Listeye ekle"
                                >
                                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                                    <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z" stroke="currentColor" strokeWidth="2"/>
                                    <path d="M17 21v-8H7v8M7 3v5h8" stroke="currentColor" strokeWidth="2"/>
                                  </svg>
                                </button>
                              </div>
                            </div>
                          </div>
                          {haber.kapakResmiUrl && (
                            <div className="article-image">
                              <img src={haber.kapakResmiUrl} alt={haber.baslik} />
                            </div>
                          )}
                        </article>
                      ))
                    ) : (
                      <div className="empty-state">
                        <p>Henüz haber bulunmamaktadır.</p>
                      </div>
                    )}
                  </div>
                )}

                {activeTab === 'featured' && (
                  <div className="articles-container">
                    {featuredHaberler.length > 0 ? (
                      featuredHaberler.map((haber) => (
                        <article 
                          key={haber.id} 
                          className="article-card"
                          onClick={() => handleArticleClick(haber.slug)}
                        >
                          <div className="article-content">
                            <div className="article-header">
                              {haber.kategoriAdi && (
                                <span className="article-publication">
                                  In {haber.kategoriAdi}
                                </span>
                              )}
                              {haber.kullaniciAdi && (
                                <span className="article-author">by {haber.kullaniciAdi}</span>
                              )}
                            </div>
                            <h2 className="article-title">{haber.baslik}</h2>
                            <p className="article-excerpt">{haber.ozet}</p>
                            <div className="article-footer">
                              <div className="article-meta">
                                <span className="article-date">{formatDate(haber.createdAt)}</span>
                                <span className="article-read-time">• {haber.okunmaSayisi || 0} dk okuma</span>
                                <div className="article-engagement">
                                  <span className="engagement-item">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                                      <path d="M7.5 21L12 18l4.5 3-1.5-5.5L20 12l-5.5-.5L12 6l-2.5 5.5L4 12l4.5 3.5L7.5 21z" fill="currentColor"/>
                                    </svg>
                                    {haber.begeniSayisi || 0}
                                  </span>
                                  <span className="engagement-item">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                                      <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" stroke="currentColor" strokeWidth="2"/>
                                    </svg>
                                    {haber.yorumSayisi || 0}
                                  </span>
                                </div>
                              </div>
                              <div className="article-actions">
                                <button 
                                  className={`article-action-btn ${begenilenHaberler.has(haber.id) ? 'active' : ''}`}
                                  onClick={(e) => handleBegen(haber.id, e)}
                                  title="Beğen"
                                >
                                  <svg width="20" height="20" viewBox="0 0 24 24" fill={begenilenHaberler.has(haber.id) ? "currentColor" : "none"}>
                                    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" stroke="currentColor" strokeWidth="2"/>
                                  </svg>
                                </button>
                                <button 
                                  className="article-action-btn"
                                  onClick={(e) => {
                                    e.preventDefault();
                                    e.stopPropagation();
                                    const rect = e.currentTarget.getBoundingClientRect();
                                    setAddToListPosition({
                                      top: rect.bottom + 8,
                                      left: rect.left - 200
                                    });
                                    setSelectedStoryId(haber.id);
                                    setShowAddToListModal(true);
                                  }}
                                  title="Listeye ekle"
                                >
                                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                                    <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z" stroke="currentColor" strokeWidth="2"/>
                                    <path d="M17 21v-8H7v8M7 3v5h8" stroke="currentColor" strokeWidth="2"/>
                                  </svg>
                                </button>
                              </div>
                            </div>
                          </div>
                          {haber.kapakResmiUrl && (
                            <div className="article-image">
                              <img src={haber.kapakResmiUrl} alt={haber.baslik} />
                            </div>
                          )}
                        </article>
                      ))
                    ) : (
                      <div className="empty-state">Henüz haber bulunmamaktadır.</div>
                    )}
                  </div>
                )}
              </>
            )}
          </div>

          {/* Sağ Sidebar */}
          <aside className="reader-sidebar-right">
            {/* Takip Ettiklerin */}
            {followingStories.length > 0 && (
              <div className="sidebar-section">
                <h3 className="sidebar-title">Takip Ettiklerin</h3>
                <div className="staff-picks-list">
                  {followingStories.map((haber) => (
                    <div 
                      key={haber.id} 
                      className="staff-pick-item"
                      onClick={() => handleArticleClick(haber.slug)}
                    >
                      <div className="staff-pick-author">
                        {haber.authorName || haber.kullaniciAdi || 'Yazar'}
                      </div>
                      <div className="staff-pick-title">{haber.baslik || haber.title}</div>
                      <div className="staff-pick-date">{formatDate(haber.createdAt || haber.publishedAt)}</div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Staff Picks */}
            <div className="sidebar-section">
              <h3 className="sidebar-title">Editör Seçimleri</h3>
              <div className="staff-picks-list">
                {staffPicks.slice(0, 3).map((haber) => (
                  <div 
                    key={haber.id} 
                    className="staff-pick-item"
                    onClick={() => handleArticleClick(haber.slug)}
                  >
                    <div className="staff-pick-author">
                      {haber.kullaniciAdi || 'Yazar'}
                    </div>
                    <div className="staff-pick-title">{haber.baslik}</div>
                    <div className="staff-pick-date">{formatDate(haber.createdAt)}</div>
                  </div>
                ))}
              </div>
              <button 
                className="sidebar-link-more"
                onClick={() => {
                  setActiveTab('featured');
                  navigate('/reader/dashboard?tab=featured');
                }}
              >
                Tüm listeyi gör
              </button>
            </div>

            {/* Recommended Topics */}
            <div className="sidebar-section">
              <h3 className="sidebar-title">Önerilen Konular</h3>
              <div className="topics-list">
                {kategoriler.slice(0, 6).map((kategori) => (
                  <button
                    key={kategori.id}
                    className="topic-button"
                    onClick={() => navigate(`/kategoriler/${kategori.slug}`)}
                  >
                    {kategori.ad}
                  </button>
                ))}
              </div>
            </div>
          </aside>
        </div>
      </main>

      {/* Add to List Modal */}
      <AddToListModal
        storyId={selectedStoryId}
        isOpen={showAddToListModal}
        position={addToListPosition}
        onClose={() => {
          setShowAddToListModal(false);
          setSelectedStoryId(null);
          setAddToListPosition(null);
        }}
        onSuccess={() => {
          // Başarılı ekleme sonrası isteğe bağlı işlemler
        }}
      />
    </div>
  );
};

export default ReaderDashboard;
