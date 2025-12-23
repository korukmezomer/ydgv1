import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { kayitliHaberAPI, listeAPI } from '../../services/api';
import './Library.css';

const Library = ({ sidebarOpen, setSidebarOpen }) => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('lists');
  const [kayitliStories, setKayitliStories] = useState([]);
  const [listeler, setListeler] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateBanner, setShowCreateBanner] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newListName, setNewListName] = useState('');
  const [newListDescription, setNewListDescription] = useState('');
  const [newListPrivate, setNewListPrivate] = useState(false);
  const [showDescription, setShowDescription] = useState(false);
  const [creating, setCreating] = useState(false);
  const [openMenuId, setOpenMenuId] = useState(null); // Hangi menü açık

  useEffect(() => {
    fetchData();
  }, []);

  // Menü dışına tıklanınca kapat
  useEffect(() => {
    const handleClickOutside = (event) => {
      // Menü container'larından birine tıklanmadıysa menüyü kapat
      if (!event.target.closest('.list-menu-container')) {
        setOpenMenuId(null);
      }
    };

    if (openMenuId) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [openMenuId]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [kayitliResponse, listelerResponse] = await Promise.all([
        kayitliHaberAPI.getAll({ page: 0, size: 100 }).catch(() => ({ data: { content: [] } })),
        listeAPI.getAll({ page: 0, size: 100 }).catch(() => ({ data: { content: [] } }))
      ]);
      const stories = (kayitliResponse.data?.content || []).map(item => item.haber || item);
      setKayitliStories(stories);
      
      const listeler = listelerResponse.data?.content || [];
      console.log('📚 Listeler API Response:', listeler);
      console.log('📖 İlk liste stories:', listeler[0]?.stories || listeler[0]?.haberler);
      
      setListeler(listeler);
    } catch (error) {
      console.error('Veri yüklenirken hata:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateList = async () => {
    if (!newListName.trim()) {
      alert('Lütfen liste adı girin');
      return;
    }

    try {
      setCreating(true);
      const response = await listeAPI.create({
        name: newListName.trim(),
        description: newListDescription.trim() || null,
        isPrivate: newListPrivate
      });
      
      // Modal'ı kapat ve formu sıfırla
      setShowCreateModal(false);
      setNewListName('');
      setNewListDescription('');
      setNewListPrivate(false);
      setShowDescription(false);
      
      // Listeyi detay sayfasına yönlendir
      if (response.data?.slug) {
        navigate(`/reader/list/${response.data.slug}`);
      } else {
        // Slug yoksa listeleri yeniden yükle
        await fetchData();
      }
    } catch (error) {
      console.error('Liste oluşturulurken hata:', error);
      alert('Liste oluşturulurken bir hata oluştu');
    } finally {
      setCreating(false);
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
    <div className={`library-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
      <main className="library-main">
        <div className="library-content">
          <div className="library-header">
            <h1 className="library-title">Your library</h1>
            <button 
              className="new-list-button"
              onClick={() => setShowCreateModal(true)}
            >
              New list
            </button>
          </div>

          {/* Tabs */}
          <div className="library-tabs">
            <button
              className={`library-tab ${activeTab === 'lists' ? 'active' : ''}`}
              onClick={() => setActiveTab('lists')}
            >
              Your lists
            </button>
            <button
              className={`library-tab ${activeTab === 'saved' ? 'active' : ''}`}
              onClick={() => setActiveTab('saved')}
            >
              Saved lists
            </button>
            <button
              className={`library-tab ${activeTab === 'highlights' ? 'active' : ''}`}
              onClick={() => setActiveTab('highlights')}
            >
              Highlights
            </button>
            <button
              className={`library-tab ${activeTab === 'history' ? 'active' : ''}`}
              onClick={() => setActiveTab('history')}
            >
              Reading history
            </button>
            <button
              className={`library-tab ${activeTab === 'responses' ? 'active' : ''}`}
              onClick={() => setActiveTab('responses')}
            >
              Responses
            </button>
          </div>

          {/* Create List Banner */}
          {showCreateBanner && activeTab === 'lists' && (
            <div className="create-list-banner">
              <div className="banner-content">
                <div className="banner-text">
                  <h3>Hikayeleri kolayca organize etmek ve paylaşmak için bir liste oluşturun</h3>
                  <button 
                    className="start-list-button"
                    onClick={() => setShowCreateModal(true)}
                  >
                    Liste başlat
                  </button>
                </div>
                <div className="banner-icon">
                  <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
                    <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" stroke="currentColor" strokeWidth="2"/>
                    <path d="M12 11v6M9 14h6" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                  </svg>
                </div>
              </div>
              <button 
                className="banner-close"
                onClick={() => setShowCreateBanner(false)}
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                </svg>
              </button>
            </div>
          )}

          {/* Content */}
          {loading ? (
            <div className="loading">Yükleniyor...</div>
          ) : (
            <>
              {activeTab === 'lists' && (
                <div className="library-section">
                  {/* Default Reading List */}
                  <div className="reading-list-card">
                    <div className="list-header">
                      <div className="list-author">
                        <div className="author-avatar">
                          {user?.email?.charAt(0).toUpperCase() || 'K'}
                        </div>
                        <div className="author-info">
                          <div className="author-name">{user?.kullaniciAdi || user?.email || 'Kullanıcı'}</div>
                          <div className="list-name">Reading list</div>
                          <div className="list-meta-info">
                            {kayitliStories.length} {kayitliStories.length === 1 ? 'story' : 'stories'}
                          </div>
                        </div>
                      </div>
                      <div className="list-menu-container">
                        <button 
                          className="list-menu"
                          onClick={(e) => {
                            e.stopPropagation();
                            setOpenMenuId(openMenuId === 'default' ? null : 'default');
                          }}
                        >
                          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                            <circle cx="12" cy="5" r="1.5" fill="currentColor"/>
                            <circle cx="12" cy="12" r="1.5" fill="currentColor"/>
                            <circle cx="12" cy="19" r="1.5" fill="currentColor"/>
                          </svg>
                        </button>
                        {openMenuId === 'default' && (
                          <div className="list-menu-dropdown">
                            <button 
                              className="list-menu-item"
                              onClick={(e) => {
                                e.stopPropagation();
                                // Okuma listesi için özel işlem yok, sadece kapat
                                setOpenMenuId(null);
                              }}
                            >
                              <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                                <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" stroke="currentColor" strokeWidth="2"/>
                                <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" stroke="currentColor" strokeWidth="2"/>
                              </svg>
                              <span>Listeyi görüntüle</span>
                            </button>
                          </div>
                        )}
                      </div>
                    </div>
                    <div className="list-content-wrapper">
                      {kayitliStories.length > 0 ? (
                        <div className="list-preview">
                          {kayitliStories.slice(0, 3).map((story, idx) => (
                            <div key={story.id} className="list-preview-item" style={{ zIndex: 3 - idx }}>
                              {story.kapakResmiUrl ? (
                                <img src={story.kapakResmiUrl} alt={story.baslik || story.title} />
                              ) : (
                                <div className="list-preview-placeholder">
                                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                                    <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" stroke="currentColor" strokeWidth="2"/>
                                    <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" stroke="currentColor" strokeWidth="2"/>
                                  </svg>
                                </div>
                              )}
                            </div>
                          ))}
                        </div>
                      ) : null}
                    </div>
                  </div>

                  {/* User Created Lists */}
                  {listeler.length > 0 && (
                    <div className="user-lists">
                      {listeler.map((liste) => (
                        <div 
                          key={liste.id} 
                          className="reading-list-card"
                          onClick={() => navigate(`/reader/list/${liste.slug}`)}
                        >
                          <div className="list-header">
                            <div className="list-author">
                              <div className="author-avatar">
                                {user?.email?.charAt(0).toUpperCase() || 'K'}
                              </div>
                              <div className="author-info">
                                <div className="author-name">{user?.kullaniciAdi || user?.email || 'Kullanıcı'}</div>
                                <div className="list-name">{liste.name || liste.ad}</div>
                                <div className="list-meta-info">
                                  {(() => {
                                    const storyCount = (liste.stories || liste.haberler || []).length;
                                    return storyCount === 0 
                                      ? 'No stories'
                                      : `${storyCount} ${storyCount === 1 ? 'story' : 'stories'}`;
                                  })()}
                                  {liste.isPrivate && (
                                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" style={{ marginLeft: '8px' }}>
                                      <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" strokeWidth="2"/>
                                      <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" strokeWidth="2"/>
                                    </svg>
                                  )}
                                </div>
                              </div>
                            </div>
                            <div className="list-menu-container">
                              <button 
                                className="list-menu"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  setOpenMenuId(openMenuId === liste.id ? null : liste.id);
                                }}
                              >
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                                  <circle cx="12" cy="5" r="1.5" fill="currentColor"/>
                                  <circle cx="12" cy="12" r="1.5" fill="currentColor"/>
                                  <circle cx="12" cy="19" r="1.5" fill="currentColor"/>
                                </svg>
                              </button>
                              {openMenuId === liste.id && (
                                <div className="list-menu-dropdown">
                                  <button 
                                    className="list-menu-item"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      navigate(`/reader/list/${liste.slug}`);
                                      setOpenMenuId(null);
                                    }}
                                  >
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                                      <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" stroke="currentColor" strokeWidth="2"/>
                                      <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" stroke="currentColor" strokeWidth="2"/>
                                    </svg>
                                    <span>Listeyi görüntüle</span>
                                  </button>
                                  <button 
                                    className="list-menu-item"
                                    onClick={async (e) => {
                                      e.stopPropagation();
                                      if (window.confirm(`"${liste.name || liste.ad}" listesini silmek istediğinizden emin misiniz?`)) {
                                        try {
                                          await listeAPI.delete(liste.id);
                                          await fetchData();
                                        } catch (error) {
                                          console.error('Liste silinirken hata:', error);
                                          alert('Liste silinirken bir hata oluştu');
                                        }
                                      }
                                      setOpenMenuId(null);
                                    }}
                                  >
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                                      <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                    </svg>
                                    <span>Listeyi sil</span>
                                  </button>
                                </div>
                              )}
                            </div>
                          </div>
                          <div className="list-content-wrapper">
                            {(() => {
                              const stories = liste.stories || liste.haberler || [];
                              console.log(`📖 Liste "${liste.name || liste.ad}" stories:`, stories);

                              if (stories.length === 0) {
                                return null;
                              }

                              return (
                                <>
                                  <div className="list-stories">
                                    {stories.slice(0, 3).map((story, idx) => {
                                      const imageUrl =
                                        story.kapakResmiUrl ||
                                        story.coverImageUrl ||
                                        story.kapak_resmi_url;

                                      const title =
                                        story.baslik || story.title || 'Başlıksız yazı';

                                      return (
                                        <div
                                          key={story.id || idx}
                                          className="list-story-item"
                                          onClick={(e) => {
                                            e.stopPropagation();
                                            if (story.slug) {
                                              navigate(`/haberler/${story.slug}`);
                                            }
                                          }}
                                        >
                                          <div className="story-content">
                                            <div className="story-title">{title}</div>
                                          </div>
                                          <div className="story-image">
                                            {imageUrl ? (
                                              <img src={imageUrl} alt={title} />
                                            ) : (
                                              <div className="list-preview-placeholder">
                                                <svg
                                                  width="24"
                                                  height="24"
                                                  viewBox="0 0 24 24"
                                                  fill="none"
                                                >
                                                  <path
                                                    d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"
                                                    stroke="currentColor"
                                                    strokeWidth="2"
                                                  />
                                                  <path
                                                    d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"
                                                    stroke="currentColor"
                                                    strokeWidth="2"
                                                  />
                                                </svg>
                                              </div>
                                            )}
                                          </div>
                                        </div>
                                      );
                                    })}
                                    {stories.length > 3 && (
                                      <div className="story-meta">
                                        +{stories.length - 3} başka yazı
                                      </div>
                                    )}
                                  </div>

                                  <div className="list-preview">
                                    {stories.slice(0, 3).map((story, idx) => {
                                      const imageUrl =
                                        story.kapakResmiUrl ||
                                        story.coverImageUrl ||
                                        story.kapak_resmi_url;

                                      return (
                                        <div
                                          key={story.id || idx}
                                          className="list-preview-item"
                                          style={{ zIndex: 3 - idx }}
                                        >
                                          {imageUrl ? (
                                            <img
                                              src={imageUrl}
                                              alt={story.baslik || story.title || 'Story'}
                                            />
                                          ) : (
                                            <div className="list-preview-placeholder">
                                              <svg
                                                width="24"
                                                height="24"
                                                viewBox="0 0 24 24"
                                                fill="none"
                                              >
                                                <path
                                                  d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"
                                                  stroke="currentColor"
                                                  strokeWidth="2"
                                                />
                                                <path
                                                  d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"
                                                  stroke="currentColor"
                                                  strokeWidth="2"
                                                />
                                              </svg>
                                            </div>
                                          )}
                                        </div>
                                      );
                                    })}
                                  </div>
                                </>
                              );
                            })()}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {activeTab === 'saved' && (
                <div className="library-section">
                  <div className="empty-state">
                    <p>Kaydedilen liste bulunmamaktadır.</p>
                  </div>
                </div>
              )}

              {activeTab === 'highlights' && (
                <div className="library-section">
                  <div className="empty-state">
                    <p>Vurgu bulunmamaktadır.</p>
                  </div>
                </div>
              )}

              {activeTab === 'history' && (
                <div className="library-section">
                  <div className="empty-state">
                    <p>Okuma geçmişi bulunmamaktadır.</p>
                  </div>
                </div>
              )}

              {activeTab === 'responses' && (
                <div className="library-section">
                  <div className="empty-state">
                    <p>Yanıt bulunmamaktadır.</p>
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      </main>

      {/* Create New List Modal */}
      {showCreateModal && (
        <div className="create-list-modal-overlay" onClick={() => setShowCreateModal(false)}>
          <div className="create-list-modal" onClick={(e) => e.stopPropagation()}>
            <div className="create-list-modal-header">
              <h2 className="create-list-modal-title">Yeni liste oluştur</h2>
              <button 
                className="create-list-modal-close"
                onClick={() => setShowCreateModal(false)}
              >
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                  <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                  <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                </svg>
              </button>
            </div>

            <div className="create-list-modal-body">
              <div className="create-list-input-group">
                <input
                  type="text"
                  className="create-list-input"
                  placeholder="Bir isim verin"
                  value={newListName}
                  onChange={(e) => {
                    const value = e.target.value;
                    if (value.length <= 60) {
                      setNewListName(value);
                    }
                  }}
                  autoFocus
                />
                <div className="create-list-char-count">
                  {newListName.length}/60
                </div>
              </div>

              {showDescription ? (
                <div className="create-list-description-group">
                  <textarea
                    className="create-list-description"
                    placeholder="Açıklama ekleyin..."
                    value={newListDescription}
                    onChange={(e) => setNewListDescription(e.target.value)}
                    rows="3"
                  />
                </div>
              ) : (
                <button 
                  className="create-list-add-description"
                  onClick={() => setShowDescription(true)}
                >
                  Açıklama ekle
                </button>
              )}

              <div className="create-list-checkbox-group">
                <label className="create-list-checkbox-label">
                  <input
                    type="checkbox"
                    checked={newListPrivate}
                    onChange={(e) => setNewListPrivate(e.target.checked)}
                  />
                  <span>Gizli yap</span>
                </label>
              </div>
            </div>

            <div className="create-list-modal-footer">
              <button 
                className="create-list-cancel"
                onClick={() => {
                  setShowCreateModal(false);
                  setNewListName('');
                  setNewListDescription('');
                  setNewListPrivate(false);
                  setShowDescription(false);
                }}
              >
                İptal
              </button>
              <button 
                className="create-list-create"
                onClick={handleCreateList}
                disabled={creating || !newListName.trim()}
              >
                {creating ? 'Oluşturuluyor...' : 'Oluştur'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Library;

