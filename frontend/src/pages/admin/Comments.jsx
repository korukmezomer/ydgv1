import { useState, useEffect } from 'react';
import { yorumAPI } from '../../services/api';
import './Dashboard.css';

const STATUSES = [
  { value: 'ONAY_BEKLIYOR', label: 'Onay Bekleyen' },
  { value: 'ONAYLANDI', label: 'Onaylanan' },
  { value: 'REDDEDILDI', label: 'Reddedilen' },
];

const AdminComments = ({ sidebarOpen }) => {
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [status, setStatus] = useState('ONAY_BEKLIYOR');
  const [page, setPage] = useState(0);
  const [size] = useState(20);
  const [totalPages, setTotalPages] = useState(0);

  const fetchComments = async (statusToLoad = status, pageToLoad = page) => {
    try {
      setLoading(true);
      
      // Önce toplam sayfa sayısını al (eğer bilinmiyorsa)
      let totalPagesCount = totalPages;
      if (totalPagesCount === 0) {
        const firstResponse = await yorumAPI.getByDurum(statusToLoad, {
          page: 0,
          size,
        });
        totalPagesCount = firstResponse.data.totalPages || 1;
        setTotalPages(totalPagesCount);
      }
      
      // Sayfa numarasını tersine çevir (son sayfa ilk sayfa olarak görünsün)
      const backendPage = totalPagesCount > 0 ? Math.max(0, totalPagesCount - 1 - pageToLoad) : 0;
      
      const response = await yorumAPI.getByDurum(statusToLoad, {
        page: backendPage,
        size,
      });
      const data = response.data;
      // Backend'den zaten sıralı geliyor (ORDER BY c.id DESC)
      setComments(data.content || []);
      setPage(pageToLoad); // Frontend'de gösterilen sayfa numarası
    } catch (error) {
      console.error('Yorumlar yüklenirken hata:', error);
      setComments([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchComments('ONAY_BEKLIYOR', 0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleStatusChange = (e) => {
    const newStatus = e.target.value;
    setStatus(newStatus);
    setTotalPages(0); // Status değiştiğinde totalPages'i sıfırla
    fetchComments(newStatus, 0);
  };

  const handleApprove = async (commentId) => {
    if (!window.confirm('Bu yorumu onaylamak istediğinize emin misiniz?')) return;
    try {
      await yorumAPI.onayla(commentId);
      fetchComments();
    } catch (error) {
      console.error('Yorum onaylanırken hata:', error);
      alert('Yorum onaylanırken hata oluştu');
    }
  };

  const handleReject = async (commentId) => {
    const sebep = window.prompt('Red sebebi (opsiyonel):') || '';
    try {
      await yorumAPI.reddet(commentId, sebep);
      fetchComments();
    } catch (error) {
      console.error('Yorum reddedilirken hata:', error);
      alert('Yorum reddedilirken hata oluştu');
    }
  };

  const handleDelete = async (commentId) => {
    if (!window.confirm('Bu yorumu silmek istediğinize emin misiniz?')) return;
    try {
      await yorumAPI.delete(commentId);
      fetchComments();
    } catch (error) {
      console.error('Yorum silinirken hata:', error);
      alert('Yorum silinirken hata oluştu');
    }
  };

  return (
    <div className={`admin-dashboard ${sidebarOpen ? 'sidebar-open' : ''}`}>
      <main className="admin-dashboard-main">
        <div className="admin-dashboard-container">
          <div className="admin-dashboard-header">
            <h1 className="admin-dashboard-title">Yorumlar</h1>
            <select
              className="admin-select"
              value={status}
              onChange={handleStatusChange}
            >
              {STATUSES.map((s) => (
                <option key={s.value} value={s.value}>
                  {s.label}
                </option>
              ))}
            </select>
          </div>

          {loading ? (
            <div className="admin-loading">Yükleniyor...</div>
          ) : comments.length === 0 ? (
            <div className="admin-empty-state">Seçilen duruma ait yorum bulunmamaktadır.</div>
          ) : (
            <div className="admin-haber-list">
              {comments.map((yorum) => (
                <div key={yorum.id} className="admin-haber-item">
                  <div className="admin-haber-info">
                    <h3 className="admin-haber-title">
                      {yorum.storyTitle || 'Başlıksız yazı'}
                    </h3>
                    <p className="admin-haber-yazar">
                      Yazan: {yorum.username}
                    </p>
                    <p className="admin-haber-ozet">{yorum.icerik || yorum.content}</p>
                    <p className="admin-haber-date">
                      Durum: {yorum.durum || yorum.status} • ID: {yorum.id}
                    </p>
                  </div>
                  <div className="admin-haber-actions">
                    {status === 'ONAY_BEKLIYOR' && (
                      <>
                        <button
                          className="admin-btn admin-btn-primary"
                          onClick={() => handleApprove(yorum.id)}
                        >
                          Onayla
                        </button>
                        <button
                          className="admin-btn admin-btn-secondary"
                          onClick={() => handleReject(yorum.id)}
                        >
                          Reddet
                        </button>
                      </>
                    )}
                    <button
                      className="admin-btn admin-btn-danger"
                      onClick={() => handleDelete(yorum.id)}
                    >
                      Sil
                    </button>
                  </div>
                </div>
              ))}

              {totalPages > 1 && (
                <div className="admin-pagination">
                  <button
                    className="admin-btn admin-btn-secondary"
                    disabled={page === 0}
                    onClick={() => fetchComments(status, page - 1)}
                  >
                    Önceki
                  </button>
                  <span>
                    Sayfa {page + 1} / {totalPages}
                  </span>
                  <button
                    className="admin-btn admin-btn-secondary"
                    disabled={page + 1 >= totalPages}
                    onClick={() => fetchComments(status, page + 1)}
                  >
                    Sonraki
                  </button>
                </div>
              )}
            </div>
          )}
        </div>
      </main>
    </div>
  );
};

export default AdminComments;


