import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { bildirimAPI, haberAPI, kategoriAPI } from '../../services/api';
import './Notifications.css';

const Notifications = ({ sidebarOpen, setSidebarOpen }) => {
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState([]);
  const [staffPicks, setStaffPicks] = useState([]);
  const [kategoriler, setKategoriler] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('all');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [notificationsResponse, editorPicksResponse, kategorilerResponse] = await Promise.all([
        bildirimAPI.getAll({ page: 0, size: 50 }).catch(() => ({ data: { content: [] } })),
        haberAPI.getEditorPicks({ page: 0, size: 5 }).catch(() => ({ data: { content: [] } })),
        kategoriAPI.getAll().catch(() => ({ data: [] }))
      ]);
      
      setNotifications(notificationsResponse.data?.content || []);
      setStaffPicks(editorPicksResponse.data?.content || []);
      setKategoriler(kategorilerResponse.data || []);
    } catch (error) {
      console.error('Veri yüklenirken hata:', error);
      setNotifications([]);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Az önce';
    if (diffMins < 60) return `${diffMins} dakika önce`;
    if (diffHours < 24) return `${diffHours} saat önce`;
    if (diffDays < 7) return `${diffDays} gün önce`;
    
    const months = ['Oca', 'Şub', 'Mar', 'Nis', 'May', 'Haz', 'Tem', 'Ağu', 'Eyl', 'Eki', 'Kas', 'Ara'];
    return `${months[date.getMonth()]} ${date.getDate()}`;
  };

  const handleNotificationClick = async (notification) => {
    // Bildirimi okundu olarak işaretle
    if (!notification.okundu && !notification.isRead) {
      try {
        await bildirimAPI.okunduIsaretle(notification.id);
        setNotifications(prev => 
          prev.map(n => n.id === notification.id ? { ...n, okundu: true, isRead: true } : n)
        );
        
        // Header'daki bildirim sayacını güncelle
        window.dispatchEvent(new CustomEvent('notificationRead'));
      } catch (error) {
        console.error('Bildirim okundu işaretlenirken hata:', error);
      }
    }

    // Bildirim türüne göre yönlendirme yap
    const storyId = notification.relatedStoryId || notification.ilgiliHaberId;
    const storySlug = notification.relatedStorySlug;
    const commentId = notification.relatedCommentId || notification.ilgiliYorumId;
    const notificationType = notification.type || notification.tip;

    // YENI_TAKIPCI bildirimleri için kullanıcı profiline yönlendirme
    if (notificationType === 'YENI_TAKIPCI' || notificationType === 'Yeni Takipçi') {
      // Mesajdan kullanıcı adını parse etmeye çalış (opsiyonel)
      // Şimdilik dashboard'a yönlendir
      navigate('/reader/dashboard');
      return;
    }

    if (storyId || storySlug) {
      try {
        let slug = storySlug;
        
        // Slug yoksa story'yi çek
        if (!slug && storyId) {
          const haberResponse = await haberAPI.getById(storyId);
          slug = haberResponse.data.slug;
        }

        if (slug) {
          // Sadece yorum bildirimleri için comment ID ekle
          const isCommentNotification = notificationType === 'YENI_YORUM' || 
                                         notificationType === 'YORUM_YANITI' || 
                                         notificationType === 'Yeni Yorum' || 
                                         notificationType === 'Yorum Yanıtı';
          
          // commentId varsa ve bildirim türü yorum ise comment'e scroll yap
          if (commentId && commentId !== null && commentId !== undefined && isCommentNotification) {
            navigate(`/haberler/${slug}?comment=${commentId}`);
          } else {
            // Diğer bildirimler için (HABER_BEGENILDI, HABER_YAYINLANDI, vb.) sadece story'ye git
            navigate(`/haberler/${slug}`);
          }
        }
      } catch (error) {
        console.error('Haber yüklenirken hata:', error);
      }
    } else {
      // Story ID veya slug yoksa dashboard'a yönlendir
      navigate('/reader/dashboard');
    }
  };

  const filteredNotifications = activeTab === 'responses' 
    ? notifications.filter(n => {
        const type = n.type || n.tip;
        return type === 'YENI_YORUM' || type === 'YORUM_YANITI' || type === 'Yeni Yorum' || type === 'Yorum Yanıtı';
      })
    : activeTab === 'likes'
    ? notifications.filter(n => {
        const type = n.type || n.tip;
        return type === 'HABER_BEGENILDI' || type === 'Haber Beğenildi';
      })
    : activeTab === 'followers'
    ? notifications.filter(n => {
        const type = n.type || n.tip;
        return type === 'YENI_TAKIPCI' || type === 'Yeni Takipçi';
      })
    : notifications;

  return (
    <div className={`notifications-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
      <main className="notifications-main" style={{ padding: '32px 24px' }}>
        <div className="notifications-content">
          <h1 className="notifications-title">Bildirimler</h1>

          {/* Tabs */}
          <div className="notifications-tabs">
            <button
              className={`notification-tab ${activeTab === 'all' ? 'active' : ''}`}
              onClick={() => setActiveTab('all')}
            >
              Tüm
            </button>
            <button
              className={`notification-tab ${activeTab === 'responses' ? 'active' : ''}`}
              onClick={() => setActiveTab('responses')}
            >
              Yanıtlar
            </button>
            <button
              className={`notification-tab ${activeTab === 'likes' ? 'active' : ''}`}
              onClick={() => setActiveTab('likes')}
            >
              Beğeniler
            </button>
            <button
              className={`notification-tab ${activeTab === 'followers' ? 'active' : ''}`}
              onClick={() => setActiveTab('followers')}
            >
              Takipçiler
            </button>
          </div>

          {/* Notifications List */}
          {loading ? (
            <div className="loading">Yükleniyor...</div>
          ) : filteredNotifications.length > 0 ? (
            <div className="notifications-list">
              {filteredNotifications.map((notification) => (
                <div
                  key={notification.id}
                  className={`notification-item ${!notification.okundu ? 'unread' : ''}`}
                  onClick={() => handleNotificationClick(notification)}
                >
                  <div className="notification-avatar">
                    {notification.baslik?.charAt(0).toUpperCase() || 'M'}
                  </div>
                  <div className="notification-content">
                    <div className="notification-text">
                      {(() => {
                        const title = notification.title || notification.baslik;
                        const message = notification.message || notification.mesaj;
                        const type = notification.type || notification.tip;
                        
                        // Bildirim türüne göre ikon ve mesaj
                        let icon = '';
                        let displayTitle = title;
                        let displayMessage = message;
                        
                        if (type === 'HABER_YAYINLANDI' || type === 'Haber Yayınlandı') {
                          icon = '📝';
                        } else if (type === 'YENI_YORUM' || type === 'Yeni Yorum') {
                          icon = '💬';
                        } else if (type === 'YORUM_YANITI' || type === 'Yorum Yanıtı') {
                          icon = '↩️';
                        } else if (type === 'HABER_BEGENILDI' || type === 'Haber Beğenildi') {
                          icon = '❤️';
                        } else if (type === 'YENI_TAKIPCI' || type === 'Yeni Takipçi') {
                          icon = '👤';
                        } else {
                          icon = '🔔';
                        }
                        
                        return (
                          <>
                            {icon && <span style={{ marginRight: '8px' }}>{icon}</span>}
                            {displayTitle && <strong>{displayTitle}</strong>}
                            {displayMessage && (
                              <span>{displayTitle ? ' - ' : ''}{displayMessage}</span>
                            )}
                            {!displayTitle && !displayMessage && 'Yeni bildirim'}
                          </>
                        );
                      })()}
                    </div>
                    <div className="notification-time">
                      {formatDate(notification.createdAt)}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="notifications-empty">
              <p>Hepiniz yetiştiniz.</p>
            </div>
          )}
        </div>

        {/* Right Sidebar */}
        <aside className="notifications-sidebar">
          {/* Staff Picks */}
          <div className="sidebar-section">
            <h3 className="sidebar-title">Editör Seçimleri</h3>
            <div className="staff-picks-list">
              {staffPicks.slice(0, 3).map((haber) => (
                <div 
                  key={haber.id} 
                  className="staff-pick-item"
                  onClick={() => navigate(`/haberler/${haber.slug}`)}
                >
                  <div className="staff-pick-author">{haber.kullaniciAdi || 'Yazar'}</div>
                  <div className="staff-pick-title">{haber.baslik}</div>
                  <div className="staff-pick-date">
                    {(() => {
                      const date = new Date(haber.yayinlanmaTarihi || haber.createdAt);
                      const months = ['Oca', 'Şub', 'Mar', 'Nis', 'May', 'Haz', 'Tem', 'Ağu', 'Eyl', 'Eki', 'Kas', 'Ara'];
                      return `${months[date.getMonth()]} ${date.getDate()}`;
                    })()}
                  </div>
                </div>
              ))}
            </div>
            <button 
              className="sidebar-link-more"
              onClick={() => {
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
                  {kategori.kategoriAdi || kategori.ad}
                </button>
              ))}
            </div>
          </div>
        </aside>
      </main>
    </div>
  );
};

export default Notifications;

