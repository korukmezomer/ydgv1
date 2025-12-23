import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { haberAPI, kategoriAPI } from '../../services/api';
import './HaberForm.css';

const HaberDuzenle = () => {
  const { id } = useParams();
  const [formData, setFormData] = useState({
    baslik: '',
    ozet: '',
    icerik: '',
    kapakResmiUrl: '',
    kategoriId: '',
    metaDescription: '',
    degisiklikNotu: '',
  });
  const [kategoriler, setKategoriler] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadingData, setLoadingData] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchKategoriler();
    fetchHaber();
  }, [id]);

  const fetchKategoriler = async () => {
    try {
      const response = await kategoriAPI.getAll();
      setKategoriler(response.data);
    } catch (error) {
      console.error('Kategoriler yüklenirken hata:', error);
    }
  };

  const fetchHaber = async () => {
    try {
      const response = await haberAPI.getById(id);
      const haber = response.data;
      setFormData({
        baslik: haber.baslik || '',
        ozet: haber.ozet || '',
        icerik: haber.icerik || '',
        kapakResmiUrl: haber.kapakResmiUrl || '',
        kategoriId: haber.kategoriId || '',
        metaDescription: haber.metaDescription || '',
        degisiklikNotu: '',
      });
    } catch (error) {
      setError('Haber yüklenirken hata oluştu');
    } finally {
      setLoadingData(false);
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
      await haberAPI.update(id, dataToSend);
      navigate('/yazar/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Haber güncellenirken hata oluştu');
    } finally {
      setLoading(false);
    }
  };

  if (loadingData) {
    return <div className="loading">Yükleniyor...</div>;
  }

  return (
    <div className="haber-form-container">
      <div className="haber-form-card">
        <h2>Haber Düzenle</h2>

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
            <label htmlFor="metaDescription">Meta Description</label>
            <textarea
              id="metaDescription"
              name="metaDescription"
              value={formData.metaDescription}
              onChange={handleChange}
              rows="2"
              maxLength={160}
            />
          </div>

          <div className="form-group">
            <label htmlFor="degisiklikNotu">Değişiklik Notu</label>
            <textarea
              id="degisiklikNotu"
              name="degisiklikNotu"
              value={formData.degisiklikNotu}
              onChange={handleChange}
              rows="2"
              placeholder="Bu güncellemede ne değişti?"
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
              {loading ? 'Güncelleniyor...' : 'Güncelle'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default HaberDuzenle;

