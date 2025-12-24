import { useState, useEffect } from 'react';
import { kategoriAPI } from '../../services/api';
import './Dashboard.css';

const AdminCategories = ({ sidebarOpen }) => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [size] = useState(20);
  const [totalPages, setTotalPages] = useState(0);
  const [search, setSearch] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingCategory, setEditingCategory] = useState(null);
  const [formData, setFormData] = useState({ name: '', description: '' });

  const fetchCategories = async (pageToLoad = page, q = search) => {
    try {
      setLoading(true);
      const params = { page: pageToLoad, size };
      const response = await kategoriAPI.getAllSayfali(params);
      const data = response.data;
      let filteredCategories = data.content || [];
      
      if (q && q.trim()) {
        filteredCategories = filteredCategories.filter(cat => 
          cat.name.toLowerCase().includes(q.toLowerCase()) ||
          (cat.description && cat.description.toLowerCase().includes(q.toLowerCase()))
        );
      }
      
      setCategories(filteredCategories);
      setPage(data.page ?? pageToLoad);
      setTotalPages(data.totalPages ?? 0);
    } catch (error) {
      console.error('Kategoriler yüklenirken hata:', error);
      setCategories([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategories(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchCategories(0, search);
  };

  const handleOpenModal = (category = null) => {
    if (category) {
      setEditingCategory(category);
      setFormData({ name: category.name, description: category.description || '' });
    } else {
      setEditingCategory(null);
      setFormData({ name: '', description: '' });
    }
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingCategory(null);
    setFormData({ name: '', description: '' });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingCategory) {
        await kategoriAPI.update(editingCategory.id, formData);
      } else {
        await kategoriAPI.create(formData);
      }
      handleCloseModal();
      fetchCategories(page);
    } catch (error) {
      console.error('Kategori kaydedilirken hata:', error);
      alert(error.response?.data?.message || 'Kategori kaydedilirken bir hata oluştu');
    }
  };

  const handleDelete = async (category) => {
    if (!window.confirm(`"${category.name}" kategorisini silmek istediğinize emin misiniz?`)) {
      return;
    }
    try {
      await kategoriAPI.delete(category.id);
      fetchCategories(page);
    } catch (error) {
      console.error('Kategori silinirken hata:', error);
      alert('Kategori silinirken bir hata oluştu');
    }
  };

  return (
    <div className={`admin-dashboard ${sidebarOpen ? 'sidebar-open' : ''}`}>
      <main className="admin-dashboard-main">
        <div className="admin-dashboard-container">
          <div className="admin-dashboard-header">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h1 className="admin-dashboard-title">Kategoriler</h1>
              <button
                className="admin-btn admin-btn-primary"
                onClick={() => handleOpenModal()}
              >
                + Yeni Kategori
              </button>
            </div>
            <form onSubmit={handleSearchSubmit} className="admin-search-form" style={{ marginTop: '16px' }}>
              <input
                type="text"
                className="admin-search-input"
                placeholder="Kategori adı veya açıklama ile ara"
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
          ) : categories.length === 0 ? (
            <div className="admin-empty-state">Kategori bulunamadı.</div>
          ) : (
            <div className="admin-table-wrapper">
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Ad</th>
                    <th>Açıklama</th>
                    <th>Slug</th>
                    <th>İşlemler</th>
                  </tr>
                </thead>
                <tbody>
                  {categories.map((category) => (
                    <tr key={category.id}>
                      <td>{category.id}</td>
                      <td>{category.name}</td>
                      <td>{category.description || '-'}</td>
                      <td>{category.slug}</td>
                      <td>
                        <button
                          className="admin-btn admin-btn-secondary"
                          onClick={() => handleOpenModal(category)}
                          style={{ marginRight: '8px' }}
                        >
                          Düzenle
                        </button>
                        <button
                          className="admin-btn admin-btn-danger"
                          onClick={() => handleDelete(category)}
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
                    onClick={() => fetchCategories(page - 1)}
                  >
                    Önceki
                  </button>
                  <span>
                    Sayfa {page + 1} / {totalPages}
                  </span>
                  <button
                    className="admin-btn admin-btn-secondary"
                    disabled={page + 1 >= totalPages}
                    onClick={() => fetchCategories(page + 1)}
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
            <h2>{editingCategory ? 'Kategori Düzenle' : 'Yeni Kategori'}</h2>
            <form onSubmit={handleSubmit}>
              <div className="admin-form-group">
                <label>Kategori Adı *</label>
                <input
                  type="text"
                  className="admin-form-input"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  required
                />
              </div>
              <div className="admin-form-group">
                <label>Açıklama</label>
                <textarea
                  className="admin-form-input"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  rows="3"
                />
              </div>
              <div className="admin-modal-actions">
                <button type="button" className="admin-btn admin-btn-secondary" onClick={handleCloseModal}>
                  İptal
                </button>
                <button type="submit" className="admin-btn admin-btn-primary">
                  {editingCategory ? 'Güncelle' : 'Oluştur'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminCategories;

