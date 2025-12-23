import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { kullaniciAPI, yazarProfiliAPI } from '../../services/api';
import api from '../../services/api';
import './EditProfile.css';

const EditProfile = ({ sidebarOpen, setSidebarOpen }) => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [editForm, setEditForm] = useState({
    bio: '',
    ad: '',
    soyad: '',
    kullaniciAdi: '',
    email: ''
  });

  useEffect(() => {
    if (user?.id) {
      fetchData();
    }
  }, [user]);

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

      if (kullaniciResponse.data) {
        setEditForm({
          bio: profilResponse.data?.bio || '',
          ad: kullaniciResponse.data.ad || '',
          soyad: kullaniciResponse.data.soyad || '',
          kullaniciAdi: kullaniciResponse.data.kullaniciAdi || '',
          email: kullaniciResponse.data.email || ''
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
          ad: editForm.ad,
          soyad: editForm.soyad,
          kullaniciAdi: editForm.kullaniciAdi
        });
      }
      
      // Yazar profilini güncelle
      if (user?.id) {
        await yazarProfiliAPI.createOrUpdate(user.id, {
          bio: editForm.bio
        });
      }
      
      // Profil sayfasına geri dön
      navigate('/reader/profile');
    } catch (error) {
      console.error('Profil güncellenirken hata:', error);
      alert('Profil güncellenirken bir hata oluştu');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className={`edit-profile-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
        <div className="loading">Yükleniyor...</div>
      </div>
    );
  }

  return (
    <div className={`edit-profile-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
      <main className="edit-profile-main">
        <div className="edit-profile-container">
          <div className="edit-profile-header">
            <button className="back-button" onClick={() => navigate('/reader/profile')}>
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M19 12H5M12 19l-7-7 7-7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
            </button>
            <h1 className="edit-profile-title">Profili Düzenle</h1>
          </div>

          <div className="edit-profile-content">
            <div className="form-group">
              <label>Kullanıcı Adı</label>
              <input
                type="text"
                value={editForm.kullaniciAdi}
                onChange={(e) => setEditForm({ ...editForm, kullaniciAdi: e.target.value })}
                placeholder="Kullanıcı adı"
              />
            </div>
            <div className="form-group">
              <label>Ad</label>
              <input
                type="text"
                value={editForm.ad}
                onChange={(e) => setEditForm({ ...editForm, ad: e.target.value })}
                placeholder="Ad"
              />
            </div>
            <div className="form-group">
              <label>Soyad</label>
              <input
                type="text"
                value={editForm.soyad}
                onChange={(e) => setEditForm({ ...editForm, soyad: e.target.value })}
                placeholder="Soyad"
              />
            </div>
            <div className="form-group">
              <label>E-posta</label>
              <input
                type="email"
                value={editForm.email}
                onChange={(e) => setEditForm({ ...editForm, email: e.target.value })}
                placeholder="E-posta"
              />
            </div>
            <div className="form-group">
              <label>Biyografi</label>
              <textarea
                value={editForm.bio}
                onChange={(e) => setEditForm({ ...editForm, bio: e.target.value })}
                placeholder="Hakkınızda bir şeyler yazın..."
                rows="5"
                maxLength={500}
              />
              <div className="char-count">{editForm.bio.length}/500</div>
            </div>
          </div>

          <div className="edit-profile-footer">
            <button className="cancel-button" onClick={() => navigate('/reader/profile')}>
              İptal
            </button>
            <button className="save-button" onClick={handleSaveProfile} disabled={saving}>
              {saving ? 'Kaydediliyor...' : 'Kaydet'}
            </button>
          </div>
        </div>
      </main>
    </div>
  );
};

export default EditProfile;

