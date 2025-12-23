import { useState, useEffect } from 'react';
import { haberAPI, yorumAPI } from '../../services/api';
import api from '../../services/api';
import './Dashboard.css';

const EditorDashboard = () => {
  const [bekleyenHaberler, setBekleyenHaberler] = useState([]);
  const [bekleyenYorumlar, setBekleyenYorumlar] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('haberler');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const [haberResponse, yorumResponse] = await Promise.all([
        haberAPI.getAll({ page: 0, size: 50 }),
        api.get('/yorumlar/durum/BEKLIYOR', { params: { page: 0, size: 50 } }).catch(() => ({ data: { content: [] } }))
      ]);
      
      const bekleyen = haberResponse.data.content.filter(
        (h) => h.durum === 'YAYIN_BEKLIYOR'
      );
      setBekleyenHaberler(bekleyen);
      
      setBekleyenYorumlar(yorumResponse.data?.content || []);
    } catch (error) {
      console.error('Veri yüklenirken hata:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleHaberOnayla = async (id) => {
    try {
      await haberAPI.yayinOnayla(id);
      fetchData();
      alert('Haber onaylandı!');
    } catch (error) {
      alert('Haber onaylanırken hata oluştu');
    }
  };

  const handleHaberReddet = async (id) => {
    const sebep = prompt('Red sebebi:');
    if (sebep) {
      try {
        await haberAPI.yayinReddet(id, sebep);
        fetchData();
        alert('Haber reddedildi!');
      } catch (error) {
        alert('Haber reddedilirken hata oluştu');
      }
    }
  };

  const handleYorumOnayla = async (id) => {
    try {
      await api.post(`/yorumlar/${id}/onayla`);
      fetchData();
      alert('Yorum onaylandı!');
    } catch (error) {
      alert('Yorum onaylanırken hata oluştu');
    }
  };

  const handleYorumReddet = async (id) => {
    const sebep = prompt('Red sebebi:');
    if (sebep) {
      try {
        await api.post(`/yorumlar/${id}/reddet`, null, { params: { sebep } });
        fetchData();
        alert('Yorum reddedildi!');
      } catch (error) {
        alert('Yorum reddedilirken hata oluştu');
      }
    }
  };

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h1>Editör Dashboard</h1>
      </div>

      <div className="dashboard-tabs">
        <button
          className={activeTab === 'haberler' ? 'active' : ''}
          onClick={() => setActiveTab('haberler')}
        >
          Onay Bekleyen Haberler ({bekleyenHaberler.length})
        </button>
        <button
          className={activeTab === 'yorumlar' ? 'active' : ''}
          onClick={() => setActiveTab('yorumlar')}
        >
          Onay Bekleyen Yorumlar ({bekleyenYorumlar.length})
        </button>
      </div>

      {loading ? (
        <div className="loading">Yükleniyor...</div>
      ) : (
        <>
          {activeTab === 'haberler' && (
            <div className="dashboard-section">
              <h2>Onay Bekleyen Haberler</h2>
              {bekleyenHaberler.length > 0 ? (
                <div className="haber-list">
                  {bekleyenHaberler.map((haber) => (
                    <div key={haber.id} className="haber-item">
                      <div className="haber-info">
                        <h3>{haber.baslik}</h3>
                        <p className="haber-yazar">Yazar: {haber.kullaniciAdi}</p>
                        <p className="haber-ozet">{haber.ozet}</p>
                        <div className="haber-meta">
                          <span>Kategori: {haber.kategoriAdi || 'Genel'}</span>
                          <span>•</span>
                          <span>{new Date(haber.createdAt).toLocaleDateString('tr-TR')}</span>
                        </div>
                      </div>
                      <div className="haber-actions">
                        <button
                          onClick={() => handleHaberOnayla(haber.id)}
                          className="btn btn-primary btn-sm"
                        >
                          Onayla
                        </button>
                        <button
                          onClick={() => handleHaberReddet(haber.id)}
                          className="btn btn-danger btn-sm"
                        >
                          Reddet
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="empty-state">Onay bekleyen haber bulunmamaktadır.</div>
              )}
            </div>
          )}

          {activeTab === 'yorumlar' && (
            <div className="dashboard-section">
              <h2>Onay Bekleyen Yorumlar</h2>
              {bekleyenYorumlar.length > 0 ? (
                <div className="yorum-list">
                  {bekleyenYorumlar.map((yorum) => (
                    <div key={yorum.id} className="yorum-item">
                      <div className="yorum-info">
                        <p className="yorum-icerik">{yorum.icerik}</p>
                        <div className="yorum-meta">
                          <span>Yazar: {yorum.kullaniciAdi}</span>
                          <span>•</span>
                          <span>Haber ID: {yorum.haberId}</span>
                          <span>•</span>
                          <span>{new Date(yorum.createdAt).toLocaleDateString('tr-TR')}</span>
                        </div>
                      </div>
                      <div className="yorum-actions">
                        <button
                          onClick={() => handleYorumOnayla(yorum.id)}
                          className="btn btn-primary btn-sm"
                        >
                          Onayla
                        </button>
                        <button
                          onClick={() => handleYorumReddet(yorum.id)}
                          className="btn btn-danger btn-sm"
                        >
                          Reddet
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="empty-state">Onay bekleyen yorum bulunmamaktadır.</div>
              )}
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default EditorDashboard;

