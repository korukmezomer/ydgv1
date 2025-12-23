import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useLocation, useNavigate } from 'react-router-dom';
import { kullaniciAPI, yazarProfiliAPI } from '../../services/api';
import api from '../../services/api';
import './Settings.css';

const Settings = ({ sidebarOpen, setSidebarOpen }) => {
  const { user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('account');
  const [showProfileModal, setShowProfileModal] = useState(false);
  const [loading, setLoading] = useState(true);
  const [kullaniciBilgileri, setKullaniciBilgileri] = useState(null);
  const [yazarProfili, setYazarProfili] = useState(null);
  const [editForm, setEditForm] = useState({
    bio: '',
    ad: '',
    soyad: '',
    kullaniciAdi: '',
    email: '',
    pronouns: '',
    avatarUrl: ''
  });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (user?.id) {
      fetchData();
    }
  }, [user]);

  // URL'deki modal parametresini kontrol et
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    if (params.get('modal') === 'profile') {
      setShowProfileModal(true);
      // URL'den modal parametresini kaldır (temiz URL için)
      navigate('/reader/settings', { replace: true });
    }
  }, [location.search, navigate]);

  const fetchData = async () => {
    try {
      setLoading(true);
      
      const [kullaniciResponse, profilResponse] = await Promise.all([
        api.get(`/kullanicilar/${user.id}`).catch((err) => {
          if (err.response?.status === 404 || err.response?.status === 400) {
            return { data: null };
          }
          throw err;
        }),
        api.get(`/yazar-profilleri/kullanici/${user.id}`).catch((err) => {
          if (err.response?.status === 404 || err.response?.status === 400) {
            return { data: null };
          }
          throw err;
        })
      ]);

      setKullaniciBilgileri(kullaniciResponse.data);
      setYazarProfili(profilResponse.data);

      if (kullaniciResponse.data) {
        setEditForm({
          bio: profilResponse.data?.bio || '',
          ad: kullaniciResponse.data.firstName || kullaniciResponse.data.ad || '',
          soyad: kullaniciResponse.data.lastName || kullaniciResponse.data.soyad || '',
          kullaniciAdi: kullaniciResponse.data.username || kullaniciResponse.data.kullaniciAdi || '',
          email: kullaniciResponse.data.email || '',
          pronouns: '', // Backend'de henüz yok
          avatarUrl: profilResponse.data?.avatarUrl || ''
        });
      }
    } catch (error) {
      console.error('Veri yüklenirken hata:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSaveProfile = async () => {
    try {
      setSaving(true);
      
      // Kullanıcı bilgilerini güncelle
      if (user?.id) {
        await kullaniciAPI.update(user.id, {
          email: editForm.email,
          firstName: editForm.ad,
          lastName: editForm.soyad,
          username: editForm.kullaniciAdi
          // Password göndermiyoruz - update için optional
        });
      }
      
      // Yazar profilini güncelle
      if (user?.id) {
        await yazarProfiliAPI.createOrUpdate(user.id, {
          bio: editForm.bio,
          avatarUrl: editForm.avatarUrl
        });
      }
      
      // Verileri yeniden yükle
      await fetchData();
      setShowProfileModal(false);
    } catch (error) {
      console.error('Profil güncellenirken hata:', error);
      alert('Profil güncellenirken bir hata oluştu');
    } finally {
      setSaving(false);
    }
  };

  const displayName = kullaniciBilgileri?.kullaniciAdi || kullaniciBilgileri?.email || user?.email || 'Kullanıcı';
  const displayEmail = kullaniciBilgileri?.email || user?.email || '';

  if (loading) {
    return (
      <div className={`settings-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
        <div className="loading">Yükleniyor...</div>
      </div>
    );
  }

  return (
    <div className={`settings-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
      <main className="settings-main">
        <div className="settings-container">
          <h1 className="settings-title">Ayarlar</h1>
          
          <div className="settings-tabs">
            <button
              className={`settings-tab ${activeTab === 'account' ? 'active' : ''}`}
              onClick={() => setActiveTab('account')}
            >
              Hesap
            </button>
            <button
              className={`settings-tab ${activeTab === 'publishing' ? 'active' : ''}`}
              onClick={() => setActiveTab('publishing')}
            >
              Yayınlama
            </button>
          </div>

          {activeTab === 'account' && (
            <div className="settings-content">
              <div className="settings-section">
                <div className="settings-item">
                  <div className="settings-item-content">
                    <h3 className="settings-item-title">E-posta adresi</h3>
                    <p className="settings-item-description">{displayEmail}</p>
                  </div>
                </div>

                <div className="settings-item">
                  <div className="settings-item-content">
                    <h3 className="settings-item-title">Kullanıcı adı ve alt alan adları</h3>
                    <p className="settings-item-description">{displayName}</p>
                  </div>
                </div>

                <div className="settings-item">
                  <div className="settings-item-content">
                    <h3 className="settings-item-title">Profil bilgileri</h3>
                    <p className="settings-item-description">Fotoğrafınızı, adınızı düzenleyin</p>
                  </div>
                  <button 
                    className="settings-edit-button"
                    onClick={() => setShowProfileModal(true)}
                  >
                    Düzenle
                  </button>
                </div>

                <div className="settings-item">
                  <div className="settings-item-content">
                    <h3 className="settings-item-title">Profil tasarımı</h3>
                    <p className="settings-item-description">Profil sayfanızın görünümünü özelleştirin</p>
                  </div>
                </div>

                <div className="settings-item">
                  <div className="settings-item-content">
                    <h3 className="settings-item-title">Özel alan adı</h3>
                    <p className="settings-item-description">Kendi alan adınızı kullanın</p>
                  </div>
                </div>

                <div className="settings-item">
                  <div className="settings-item-content">
                    <h3 className="settings-item-title">Ortaklık Programı</h3>
                    <p className="settings-item-description">Yazılarınızdan para kazanın</p>
                  </div>
                </div>

                <div className="settings-item">
                  <div className="settings-item-content">
                    <h3 className="settings-item-title">Medium Özetiniz</h3>
                    <p className="settings-item-description">E-posta bildirimlerinizi yönetin</p>
                  </div>
                </div>
              </div>
            </div>
          )}

          {activeTab === 'publishing' && (
            <div className="settings-content">
              <div className="settings-section">
                <h2 className="settings-section-title">Yayınlama Tercihleri</h2>
                
                <div className="settings-item">
                  <div className="settings-item-content">
                    <h3 className="settings-item-title">Varsayılan yayın durumu</h3>
                    <p className="settings-item-description">
                      Yeni yazılarınız için varsayılan yayın durumunu seçin
                    </p>
                  </div>
                  <select className="settings-select" defaultValue="draft">
                    <option value="draft">Taslak</option>
                    <option value="pending">Yayın Bekliyor</option>
                  </select>
                </div>

                <div className="settings-item">
                  <div className="settings-item-content">
                    <h3 className="settings-item-title">Otomatik kaydetme</h3>
                    <p className="settings-item-description">
                      Yazılarınız otomatik olarak kaydedilir
                    </p>
                  </div>
                  <label className="settings-toggle">
                    <input type="checkbox" defaultChecked />
                    <span className="settings-toggle-slider"></span>
                  </label>
                </div>

                <div className="settings-item">
                  <div className="settings-item-content">
                    <h3 className="settings-item-title">Yorumlara izin ver</h3>
                    <p className="settings-item-description">
                      Yazılarınızda yorum yapılmasına izin ver
                    </p>
                  </div>
                  <label className="settings-toggle">
                    <input type="checkbox" defaultChecked />
                    <span className="settings-toggle-slider"></span>
                  </label>
                </div>

                <div className="settings-item">
                  <div className="settings-item-content">
                    <h3 className="settings-item-title">Paylaşım linkleri</h3>
                    <p className="settings-item-description">
                      Yazılarınızın paylaşım linklerini göster
                    </p>
                  </div>
                  <label className="settings-toggle">
                    <input type="checkbox" defaultChecked />
                    <span className="settings-toggle-slider"></span>
                  </label>
                </div>

                <div className="settings-item">
                  <div className="settings-item-content">
                    <h3 className="settings-item-title">E-posta bildirimleri</h3>
                    <p className="settings-item-description">
                      Yorum ve beğeni bildirimlerini e-posta ile al
                    </p>
                  </div>
                  <label className="settings-toggle">
                    <input type="checkbox" defaultChecked />
                    <span className="settings-toggle-slider"></span>
                  </label>
                </div>

                <div className="settings-item">
                  <div className="settings-item-content">
                    <h3 className="settings-item-title">Yayın önizlemesi</h3>
                    <p className="settings-item-description">
                      Yayınlamadan önce önizleme göster
                    </p>
                  </div>
                  <label className="settings-toggle">
                    <input type="checkbox" defaultChecked />
                    <span className="settings-toggle-slider"></span>
                  </label>
                </div>
              </div>
            </div>
          )}
        </div>
      </main>

      {/* Profile Information Modal */}
      {showProfileModal && (
        <div className="profile-modal-overlay" onClick={() => setShowProfileModal(false)}>
          <div className="profile-modal" onClick={(e) => e.stopPropagation()}>
            <div className="profile-modal-header">
              <h2 className="profile-modal-title">Profil bilgileri</h2>
              <button 
                className="profile-modal-close"
                onClick={() => setShowProfileModal(false)}
              >
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                  <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                  <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                </svg>
              </button>
            </div>

            <div className="profile-modal-body">
              {/* Photo Section */}
              <div className="profile-modal-section">
                <label className="profile-modal-label">Fotoğraf</label>
                <div className="profile-photo-section">
                  <div className="profile-photo-preview">
                    {editForm.avatarUrl ? (
                      <img src={editForm.avatarUrl} alt="Profile" />
                    ) : (
                      <div className="profile-photo-placeholder">
                        {displayName.charAt(0).toUpperCase()}
                      </div>
                    )}
                  </div>
                  <div className="profile-photo-actions">
                    <button className="profile-photo-update">Güncelle</button>
                    <button className="profile-photo-remove">Kaldır</button>
                  </div>
                </div>
                <p className="profile-photo-hint">
                  Önerilen: Kare JPG, PNG veya GIF, en az 1,000 piksel her kenarda.
                </p>
              </div>

              {/* Name Section */}
              <div className="profile-modal-section">
                <label className="profile-modal-label">
                  Ad<span className="required">*</span>
                </label>
                <input
                  type="text"
                  className="profile-modal-input"
                  value={editForm.ad}
                  onChange={(e) => {
                    const value = e.target.value;
                    if (value.length <= 50) {
                      setEditForm({ ...editForm, ad: value });
                    }
                  }}
                  placeholder="Adınızı girin"
                />
                <div className="profile-modal-char-count">
                  {editForm.ad.length}/50
                </div>
              </div>

              {/* Last Name Section */}
              <div className="profile-modal-section">
                <label className="profile-modal-label">Soyad</label>
                <input
                  type="text"
                  className="profile-modal-input"
                  value={editForm.soyad}
                  onChange={(e) => {
                    const value = e.target.value;
                    if (value.length <= 50) {
                      setEditForm({ ...editForm, soyad: value });
                    }
                  }}
                  placeholder="Soyadınızı girin"
                />
                <div className="profile-modal-char-count">
                  {editForm.soyad.length}/50
                </div>
              </div>

              {/* Username Section */}
              <div className="profile-modal-section">
                <label className="profile-modal-label">Kullanıcı Adı</label>
                <input
                  type="text"
                  className="profile-modal-input"
                  value={editForm.kullaniciAdi}
                  onChange={(e) => {
                    const value = e.target.value;
                    if (value.length <= 50) {
                      setEditForm({ ...editForm, kullaniciAdi: value });
                    }
                  }}
                  placeholder="Kullanıcı adınızı girin"
                />
                <div className="profile-modal-char-count">
                  {editForm.kullaniciAdi.length}/50
                </div>
              </div>

              {/* Pronouns Section */}
              <div className="profile-modal-section">
                <label className="profile-modal-label">Zamirler</label>
                <input
                  type="text"
                  className="profile-modal-input"
                  value={editForm.pronouns}
                  onChange={(e) => {
                    const value = e.target.value;
                    if (value.length <= 4) {
                      setEditForm({ ...editForm, pronouns: value });
                    }
                  }}
                  placeholder="Ekle..."
                />
                <div className="profile-modal-char-count">
                  {editForm.pronouns.length}/4
                </div>
              </div>

              {/* Short Bio Section */}
              <div className="profile-modal-section">
                <label className="profile-modal-label">Kısa biyografi</label>
                <textarea
                  className="profile-modal-textarea"
                  value={editForm.bio}
                  onChange={(e) => {
                    const value = e.target.value;
                    if (value.length <= 160) {
                      setEditForm({ ...editForm, bio: value });
                    }
                  }}
                  placeholder="Hakkınızda kısa bir açıklama yazın..."
                  rows="4"
                />
                <div className="profile-modal-char-count">
                  {editForm.bio.length}/160
                </div>
              </div>

              {/* About Page Section */}
              <div className="profile-modal-section">
                <div className="profile-about-section">
                  <div className="profile-about-content">
                    <h3 className="profile-about-title">Hakkında Sayfası</h3>
                    <p className="profile-about-description">
                      Fotoğraflar ve daha fazlasıyla kişiselleştirin, kendinizi "Kısa biyografi"nizden daha canlı bir şekilde tanıtın.
                    </p>
                  </div>
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" className="profile-about-link-icon">
                    <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    <polyline points="15 3 21 3 21 9" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    <line x1="10" y1="14" x2="21" y2="3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
                </div>
              </div>
            </div>

            <div className="profile-modal-footer">
              <button 
                className="profile-modal-cancel"
                onClick={() => setShowProfileModal(false)}
              >
                İptal
              </button>
              <button 
                className="profile-modal-save"
                onClick={handleSaveProfile}
                disabled={saving || !editForm.ad.trim()}
              >
                {saving ? 'Kaydediliyor...' : 'Kaydet'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Settings;

