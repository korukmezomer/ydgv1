import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { haberAPI } from '../../services/api';
import './EditorPicks.css';

const EditorPicks = ({ sidebarOpen, setSidebarOpen }) => {
  const navigate = useNavigate();
  const [yayinlananHaberler, setYayinlananHaberler] = useState([]);
  const [editorPicks, setEditorPicks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    fetchYayinlananHaberler();
    fetchEditorPicks();
  }, [page]);

  const fetchYayinlananHaberler = async () => {
    try {
      setLoading(true);
      
      // Önce toplam sayfa sayısını al (eğer bilinmiyorsa)
      let totalPagesCount = totalPages;
      if (totalPagesCount === 0) {
        const firstResponse = await haberAPI.getAll({ page: 0, size: 20 });
        if (firstResponse.data && firstResponse.data.content) {
          totalPagesCount = firstResponse.data.totalPages || 1;
        } else {
          totalPagesCount = 1;
        }
        setTotalPages(totalPagesCount);
      }
      
      // Sayfa numarasını tersine çevir (son sayfa ilk sayfa olarak görünsün)
      const backendPage = totalPagesCount > 0 ? Math.max(0, totalPagesCount - 1 - page) : 0;
      
      // Yayınlanmış story'leri getir (getAll zaten yayınlanmış story'leri getiriyor)
      const response = await haberAPI.getAll({ page: backendPage, size: 20 });
      if (response.data && response.data.content) {
        // Backend'den zaten sıralı geliyor (ORDER BY publishedAt DESC)
        setYayinlananHaberler(response.data.content || []);
      } else {
        // Eğer response.data direkt array ise
        setYayinlananHaberler(response.data || []);
      }
    } catch (error) {
      console.error('Haberler yüklenirken hata:', error);
      setYayinlananHaberler([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchEditorPicks = async () => {
    try {
      const response = await haberAPI.getEditorPicks({ page: 0, size: 100 });
      const picks = response.data.content || [];
      const picksSet = new Set(picks.map(p => p.id));
      setEditorPicks(picksSet);
    } catch (error) {
      console.error('Editör seçimleri yüklenirken hata:', error);
    }
  };

  const handleToggleEditorPick = async (haberId) => {
    try {
      await haberAPI.toggleEditorPick(haberId);
      await fetchEditorPicks();
      await fetchYayinlananHaberler();
    } catch (error) {
      alert('Editör seçimi güncellenirken hata oluştu: ' + (error.response?.data?.message || error.message));
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const months = ['Oca', 'Şub', 'Mar', 'Nis', 'May', 'Haz', 'Tem', 'Ağu', 'Eyl', 'Eki', 'Kas', 'Ara'];
    return `${months[date.getMonth()]} ${date.getDate()}, ${date.getFullYear()}`;
  };

  return (
    <div className={`admin-editor-picks ${sidebarOpen ? 'sidebar-open' : ''}`}>
      <main className="admin-editor-picks-main">
        <div className="admin-editor-picks-container">
          <div className="admin-editor-picks-header">
            <h1 className="admin-editor-picks-title">Editör Seçimleri</h1>
            <p className="admin-editor-picks-subtitle">
              Yayınlanmış yazıları seçerek dashboard'da "Editör Seçimleri" bölümünde görüntülenmesini sağlayabilirsiniz.
            </p>
          </div>

          {loading ? (
            <div className="admin-loading">Yükleniyor...</div>
          ) : (
            <>
              <div className="admin-editor-picks-list">
                {yayinlananHaberler.map((haber) => {
                  const isEditorPick = editorPicks.has(haber.id);
                  return (
                    <div key={haber.id} className="admin-editor-pick-item">
                      <div className="admin-editor-pick-info">
                        <h3 
                          className="admin-editor-pick-title" 
                          onClick={() => navigate(`/haberler/${haber.slug}`)}
                        >
                          {haber.baslik}
                        </h3>
                        <p className="admin-editor-pick-yazar">Yazar: {haber.kullaniciAdi}</p>
                        <p className="admin-editor-pick-ozet">
                          {haber.ozet || (haber.icerik ? haber.icerik.substring(0, 200) + '...' : '')}
                        </p>
                        <p className="admin-editor-pick-date">{formatDate(haber.yayinlanmaTarihi || haber.createdAt)}</p>
                      </div>
                      <div className="admin-editor-pick-actions">
                        <button
                          onClick={() => window.open(`/haberler/${haber.slug}`, '_blank')}
                          className="admin-btn admin-btn-secondary"
                        >
                          İncele
                        </button>
                        <button
                          onClick={() => handleToggleEditorPick(haber.id)}
                          className={`admin-btn ${isEditorPick ? 'admin-btn-success' : 'admin-btn-primary'}`}
                        >
                          {isEditorPick ? '✓ Editör Seçimi' : 'Editör Seçimi Yap'}
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>

              {totalPages > 1 && (
                <div className="admin-pagination">
                  <button
                    onClick={() => setPage(p => Math.max(0, p - 1))}
                    disabled={page === 0}
                    className="admin-pagination-btn"
                  >
                    Önceki
                  </button>
                  <span className="admin-pagination-info">
                    Sayfa {page + 1} / {totalPages}
                  </span>
                  <button
                    onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                    disabled={page >= totalPages - 1}
                    className="admin-pagination-btn"
                  >
                    Sonraki
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </main>
    </div>
  );
};

export default EditorPicks;

