import { useState, useEffect } from 'react';
import { etiketAPI } from '../../services/api';
import './Dashboard.css';

const AdminTags = ({ sidebarOpen }) => {
  const [tags, setTags] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [size] = useState(20);
  const [totalPages, setTotalPages] = useState(0);
  const [search, setSearch] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingTag, setEditingTag] = useState(null);
  const [formData, setFormData] = useState({ name: '' });

  const fetchTags = async (pageToLoad = page, q = search) => {
    try {
      setLoading(true);
      const params = { page: pageToLoad, size };
      const response = await etiketAPI.getAllSayfali(params);
      const data = response.data;
      let filteredTags = data.content || [];
      
      if (q && q.trim()) {
        filteredTags = filteredTags.filter(tag => 
          tag.name.toLowerCase().includes(q.toLowerCase())
        );
      }
      
      setTags(filteredTags);
      setPage(data.page ?? pageToLoad);
      setTotalPages(data.totalPages ?? 0);
    } catch (error) {
      console.error('Etiketler yüklenirken hata:', error);
      setTags([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTags(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchTags(0, search);
  };

  const handleOpenModal = (tag = null) => {
    if (tag) {
      setEditingTag(tag);
      setFormData({ name: tag.name });
    } else {
      setEditingTag(null);
      setFormData({ name: '' });
    }
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingTag(null);
    setFormData({ name: '' });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingTag) {
        await etiketAPI.update(editingTag.id, formData);
      } else {
        await etiketAPI.create(formData);
      }
      handleCloseModal();
      fetchTags(page);
    } catch (error) {
      console.error('Etiket kaydedilirken hata:', error);
      alert(error.response?.data?.message || 'Etiket kaydedilirken bir hata oluştu');
    }
  };

  const handleDelete = async (tag) => {
    if (!window.confirm(`"${tag.name}" etiketini silmek istediğinize emin misiniz?`)) {
      return;
    }
    try {
      await etiketAPI.delete(tag.id);
      fetchTags(page);
    } catch (error) {
      console.error('Etiket silinirken hata:', error);
      alert('Etiket silinirken bir hata oluştu');
    }
  };

  return (
    <div className={`admin-dashboard ${sidebarOpen ? 'sidebar-open' : ''}`}>
      <main className="admin-dashboard-main">
        <div className="admin-dashboard-container">
          <div className="admin-dashboard-header">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h1 className="admin-dashboard-title">Etiketler</h1>
              <button
                className="admin-btn admin-btn-primary"
                onClick={() => handleOpenModal()}
              >
                + Yeni Etiket
              </button>
            </div>
            <form onSubmit={handleSearchSubmit} className="admin-search-form" style={{ marginTop: '16px' }}>
              <input
                type="text"
                className="admin-search-input"
                placeholder="Etiket adı ile ara"
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
          ) : tags.length === 0 ? (
            <div className="admin-empty-state">Etiket bulunamadı.</div>
          ) : (
            <div className="admin-table-wrapper">
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Ad</th>
                    <th>Slug</th>
                    <th>İşlemler</th>
                  </tr>
                </thead>
                <tbody>
                  {tags.map((tag) => (
                    <tr key={tag.id}>
                      <td>{tag.id}</td>
                      <td>{tag.name}</td>
                      <td>{tag.slug}</td>
                      <td>
                        <button
                          className="admin-btn admin-btn-secondary"
                          onClick={() => handleOpenModal(tag)}
                          style={{ marginRight: '8px' }}
                        >
                          Düzenle
                        </button>
                        <button
                          className="admin-btn admin-btn-danger"
                          onClick={() => handleDelete(tag)}
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
                    onClick={() => fetchTags(page - 1)}
                  >
                    Önceki
                  </button>
                  <span>
                    Sayfa {page + 1} / {totalPages}
                  </span>
                  <button
                    className="admin-btn admin-btn-secondary"
                    disabled={page + 1 >= totalPages}
                    onClick={() => fetchTags(page + 1)}
                  >
                    Sonraki
                  </button>
                </div>
              )}
            </div>
          )}
        </div>
      </main>

      {showModal && (
        <div className="admin-modal-overlay" onClick={handleCloseModal}>
          <div className="admin-modal" onClick={(e) => e.stopPropagation()}>
            <h2>{editingTag ? 'Etiket Düzenle' : 'Yeni Etiket'}</h2>
            <form onSubmit={handleSubmit}>
              <div className="admin-form-group">
                <label>Etiket Adı *</label>
                <input
                  type="text"
                  className="admin-form-input"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  required
                />
              </div>
              <div className="admin-modal-actions">
                <button type="button" className="admin-btn admin-btn-secondary" onClick={handleCloseModal}>
                  İptal
                </button>
                <button type="submit" className="admin-btn admin-btn-primary">
                  {editingTag ? 'Güncelle' : 'Oluştur'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminTags;

