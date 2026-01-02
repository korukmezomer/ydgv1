import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { haberAPI } from '../../services/api';
import './Dashboard.css';

const AdminDashboard = ({ sidebarOpen, setSidebarOpen }) => {
  const navigate = useNavigate();
  const [bekleyenHaberler, setBekleyenHaberler] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [size] = useState(50);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    fetchBekleyenHaberler(0);
  }, []);

  const fetchBekleyenHaberler = async (pageToLoad = page) => {
    try {
      setLoading(true);
      
      // Önce toplam sayfa sayısını al (eğer bilinmiyorsa)
      let totalPagesCount = totalPages;
      if (totalPagesCount === 0) {
        const firstResponse = await haberAPI.getBekleyen({ page: 0, size });
        totalPagesCount = firstResponse.data.totalPages || 1;
        setTotalPages(totalPagesCount);
      }
      
      // Sayfa numarasını tersine çevir (son sayfa ilk sayfa olarak görünsün)
      // Frontend'de page 0 = Backend'de son sayfa (totalPages-1)
      // Frontend'de page 1 = Backend'de totalPages-2
      // Frontend'de son sayfa = Backend'de page 0
      const backendPage = totalPagesCount > 0 ? Math.max(0, totalPagesCount - 1 - pageToLoad) : 0;
      
      const response = await haberAPI.getBekleyen({ page: backendPage, size });
      const data = response.data;
      
      // Backend'den gelen veriyi tarihe göre ters sırala (en yeni en başta)
      let haberler = data.content || [];
      haberler = haberler.sort((a, b) => {
        const dateA = new Date(a.createdAt || a.olusturmaTarihi);
        const dateB = new Date(b.createdAt || b.olusturmaTarihi);
        return dateB - dateA; // En yeni en başta
      });
      
      setBekleyenHaberler(haberler);
      setPage(pageToLoad); // Frontend'de gösterilen sayfa numarası
      // totalPages zaten set edildi, tekrar set etmeye gerek yok
    } catch (error) {
      console.error('Haberler yüklenirken hata:', error);
      setBekleyenHaberler([]);
    } finally {
      setLoading(false);
    }
  };

  const handleOnayla = async (id) => {
    try {
      await haberAPI.yayinOnayla(id);
      fetchBekleyenHaberler(page);
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
        fetchBekleyenHaberler(page);
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
              
              {totalPages > 1 && (
                <div className="admin-pagination">
                  <button
                    className="admin-btn admin-btn-secondary"
                    disabled={page === 0}
                    onClick={() => fetchBekleyenHaberler(page - 1)}
                  >
                    Önceki
                  </button>
                  <span>
                    Sayfa {page + 1} / {totalPages}
                  </span>
                  <button
                    className="admin-btn admin-btn-secondary"
                    disabled={page + 1 >= totalPages}
                    onClick={() => fetchBekleyenHaberler(page + 1)}
                  >
                    Sonraki
                  </button>
                </div>
              )}
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

