import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { haberAPI } from '../../services/api';
import './Dashboard.css';

const AdminDashboard = ({ sidebarOpen, setSidebarOpen }) => {
  const navigate = useNavigate();
  const [bekleyenHaberler, setBekleyenHaberler] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchBekleyenHaberler();
  }, []);

  const fetchBekleyenHaberler = async () => {
    try {
      const response = await haberAPI.getBekleyen({ page: 0, size: 50 });
      setBekleyenHaberler(response.data.content || []);
    } catch (error) {
      console.error('Haberler yüklenirken hata:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleOnayla = async (id) => {
    try {
      await haberAPI.yayinOnayla(id);
      fetchBekleyenHaberler();
      alert('Haber onaylandı ve yayınlandı!');
    } catch (error) {
      alert('Haber onaylanırken hata oluştu: ' + (error.response?.data?.message || error.message));
    }
  };

  const handleReddet = async (id) => {
    const sebep = prompt('Red sebebi:');
    if (sebep) {
      try {
        await haberAPI.yayinReddet(id, sebep);
        fetchBekleyenHaberler();
        alert('Haber reddedildi!');
      } catch (error) {
        alert('Haber reddedilirken hata oluştu: ' + (error.response?.data?.message || error.message));
      }
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const months = ['Oca', 'Şub', 'Mar', 'Nis', 'May', 'Haz', 'Tem', 'Ağu', 'Eyl', 'Eki', 'Kas', 'Ara'];
    return `${months[date.getMonth()]} ${date.getDate()}, ${date.getFullYear()}`;
  };

  return (
    <div className={`admin-dashboard ${sidebarOpen ? 'sidebar-open' : ''}`}>
      <main className="admin-dashboard-main">
        <div className="admin-dashboard-container">
          <div className="admin-dashboard-header">
            <h1 className="admin-dashboard-title">Onay Bekleyen Haberler</h1>
          </div>
          
          {loading ? (
            <div className="admin-loading">Yükleniyor...</div>
          ) : bekleyenHaberler.length > 0 ? (
            <div className="admin-haber-list">
              {bekleyenHaberler.map((haber) => (
                <div key={haber.id} className="admin-haber-item">
                  <div className="admin-haber-info">
                    <h3 className="admin-haber-title" onClick={() => navigate(`/haberler/${haber.slug}`)}>
                      {haber.baslik}
                    </h3>
                    <p className="admin-haber-yazar">Yazar: {haber.kullaniciAdi}</p>
                    <p className="admin-haber-ozet">{haber.ozet || (haber.icerik ? haber.icerik.substring(0, 200) + '...' : '')}</p>
                    <p className="admin-haber-date">{formatDate(haber.createdAt)}</p>
                  </div>
                  <div className="admin-haber-actions">
                    <button
                      onClick={() => navigate(`/haberler/${haber.slug}`)}
                      className="admin-btn admin-btn-secondary"
                    >
                      İncele
                    </button>
                    <button
                      onClick={() => handleOnayla(haber.id)}
                      className="admin-btn admin-btn-primary"
                    >
                      Onayla ve Yayınla
                    </button>
                    <button
                      onClick={() => handleReddet(haber.id)}
                      className="admin-btn admin-btn-danger"
                    >
                      Reddet
                    </button>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="admin-empty-state">Onay bekleyen haber bulunmamaktadır.</div>
          )}
        </div>
      </main>
    </div>
  );
};

export default AdminDashboard;

