import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { haberAPI, kategoriAPI } from '../../services/api';
import './HaberForm.css';

const HaberOlustur = () => {
  const [formData, setFormData] = useState({
    baslik: '',
    ozet: '',
    icerik: '',
    kapakResmiUrl: '',
    kategoriId: '',
    metaDescription: '',
  });
  const [kategoriler, setKategoriler] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchKategoriler();
  }, []);

  const fetchKategoriler = async () => {
    try {
      const response = await kategoriAPI.getAll();
      setKategoriler(response.data);
    } catch (error) {
      console.error('Kategoriler yüklenirken hata:', error);
    }
  };

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const dataToSend = {
        ...formData,
        kategoriId: formData.kategoriId ? Number(formData.kategoriId) : null,
      };
      await haberAPI.create(dataToSend);
      navigate('/yazar/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Haber oluşturulurken hata oluştu');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="haber-form-container">
      <div className="haber-form-card">
        <h2>Yeni Haber Oluştur</h2>

        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit} className="haber-form">
          <div className="form-group">
            <label htmlFor="baslik">Başlık *</label>
            <input
              type="text"
              id="baslik"
              name="baslik"
              value={formData.baslik}
              onChange={handleChange}
              required
              placeholder="Haber başlığı"
            />
          </div>

          <div className="form-group">
            <label htmlFor="ozet">Özet</label>
            <textarea
              id="ozet"
              name="ozet"
              value={formData.ozet}
              onChange={handleChange}
              rows="3"
              placeholder="Haber özeti (maksimum 500 karakter)"
              maxLength={500}
            />
          </div>

          <div className="form-group">
            <label htmlFor="icerik">İçerik *</label>
            <textarea
              id="icerik"
              name="icerik"
              value={formData.icerik}
              onChange={handleChange}
              required
              rows="15"
              placeholder="Haber içeriği (Markdown desteklenir)"
            />
          </div>

          <div className="form-group">
            <label htmlFor="kapakResmiUrl">Kapak Resmi URL</label>
            <input
              type="url"
              id="kapakResmiUrl"
              name="kapakResmiUrl"
              value={formData.kapakResmiUrl}
              onChange={handleChange}
              placeholder="https://example.com/resim.jpg"
            />
          </div>

          <div className="form-group">
            <label htmlFor="kategoriId">Kategori</label>
            <select
              id="kategoriId"
              name="kategoriId"
              value={formData.kategoriId}
              onChange={handleChange}
            >
              <option value="">Kategori Seçin</option>
              {kategoriler.map((kategori) => (
                <option key={kategori.id} value={kategori.id}>
                  {kategori.kategoriAdi}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="metaDescription">Meta Description (SEO)</label>
            <textarea
              id="metaDescription"
              name="metaDescription"
              value={formData.metaDescription}
              onChange={handleChange}
              rows="2"
              placeholder="SEO için meta açıklama (maksimum 160 karakter)"
              maxLength={160}
            />
          </div>

          <div className="form-actions">
            <button
              type="button"
              onClick={() => navigate('/yazar/dashboard')}
              className="btn btn-outline"
            >
              İptal
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Kaydediliyor...' : 'Taslak Olarak Kaydet'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default HaberOlustur;

