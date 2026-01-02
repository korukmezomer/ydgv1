import { useState, useEffect } from 'react';
import { kullaniciAPI } from '../../services/api';
import './Dashboard.css';

const AdminUsers = ({ sidebarOpen }) => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [size] = useState(20);
  const [totalPages, setTotalPages] = useState(0);
  const [search, setSearch] = useState('');

  const fetchUsers = async (pageToLoad = page, q = search) => {
    try {
      setLoading(true);
      
      // Önce toplam sayfa sayısını al (eğer bilinmiyorsa)
      let totalPagesCount = totalPages;
      if (totalPagesCount === 0) {
        const firstParams = { page: 0, size };
        if (q && q.trim()) {
          firstParams.q = q.trim();
        }
        const firstResponse = await kullaniciAPI.getAll(firstParams);
        totalPagesCount = firstResponse.data.totalPages || 1;
        setTotalPages(totalPagesCount);
      }
      
      // Sayfa numarasını tersine çevir (son sayfa ilk sayfa olarak görünsün)
      const backendPage = totalPagesCount > 0 ? Math.max(0, totalPagesCount - 1 - pageToLoad) : 0;
      
      const params = { page: backendPage, size };
      if (q && q.trim()) {
        params.q = q.trim();
      }
      const response = await kullaniciAPI.getAll(params);
      const data = response.data;
      // Backend'den zaten sıralı geliyor (ORDER BY u.id DESC)
      setUsers(data.content || []);
      setPage(pageToLoad); // Frontend'de gösterilen sayfa numarası
    } catch (error) {
      console.error('Kullanıcılar yüklenirken hata:', error);
      setUsers([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setTotalPages(0); // Arama yapıldığında totalPages'i sıfırla
    fetchUsers(0, search);
  };

  const handleToggleActive = async (user) => {
    const yeniDurum = !user.isActive;
    if (
      !window.confirm(
        `"${user.email}" kullanıcısını ${yeniDurum ? 'aktif' : 'pasif'} yapmak istediğinize emin misiniz?`
      )
    ) {
      return;
    }

    try {
      await kullaniciAPI.setActive(user.id, yeniDurum);
      fetchUsers(page);
    } catch (error) {
      console.error('Kullanıcı güncellenirken hata:', error);
      alert('Kullanıcı güncellenirken bir hata oluştu');
    }
  };

  const handleDelete = async (user) => {
    if (!window.confirm(`"${user.email}" kullanıcısını silmek istediğinize emin misiniz?`)) {
      return;
    }
    try {
      await kullaniciAPI.delete(user.id);
      fetchUsers(page);
    } catch (error) {
      console.error('Kullanıcı silinirken hata:', error);
      alert('Kullanıcı silinirken bir hata oluştu');
    }
  };

  return (
    <div className={`admin-dashboard ${sidebarOpen ? 'sidebar-open' : ''}`}>
      <main className="admin-dashboard-main">
        <div className="admin-dashboard-container">
          <div className="admin-dashboard-header">
            <h1 className="admin-dashboard-title">Kullanıcılar</h1>
            <form onSubmit={handleSearchSubmit} className="admin-search-form">
              <input
                type="text"
                className="admin-search-input"
                placeholder="Email veya ad ile ara"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
              <button type="submit" className="admin-btn admin-btn-secondary">
                Ara
              </button>
            </form>
          </div>

          {loading ? (
            <div className="admin-loading">Yükleniyor...</div>
          ) : users.length === 0 ? (
            <div className="admin-empty-state">Kullanıcı bulunamadı.</div>
          ) : (
            <div className="admin-table-wrapper">
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Email</th>
                    <th>Kullanıcı Adı</th>
                    <th>Aktif</th>
                    <th>İşlemler</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((user) => (
                    <tr key={user.id}>
                      <td>{user.id}</td>
                      <td>{user.email}</td>
                      <td>{user.kullaniciAdi}</td>
                      <td>{user.isActive ? 'Evet' : 'Hayır'}</td>
                      <td>
                        <button
                          className="admin-btn admin-btn-secondary"
                          onClick={() => handleToggleActive(user)}
                        >
                          {user.isActive ? 'Pasif Yap' : 'Aktif Yap'}
                        </button>
                        <button
                          className="admin-btn admin-btn-danger"
                          onClick={() => handleDelete(user)}
                        >
                          Sil
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>

              {totalPages > 1 && (
                <div className="admin-pagination">
                  <button
                    className="admin-btn admin-btn-secondary"
                    disabled={page === 0}
                    onClick={() => fetchUsers(page - 1)}
                  >
                    Önceki
                  </button>
                  <span>
                    Sayfa {page + 1} / {totalPages}
                  </span>
                  <button
                    className="admin-btn admin-btn-secondary"
                    disabled={page + 1 >= totalPages}
                    onClick={() => fetchUsers(page + 1)}
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

export default AdminUsers;


