import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authAPI } from '../services/api';
import { getUserFromToken } from '../utils/jwt';
import mediumLogo from '../assets/medium-logo-png_seeklogo-347160.png';
import './Auth.css';

const Register = () => {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    username: '',
    roleName: 'USER', // Varsayılan rol
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await authAPI.kayitOl(formData);
      
      // Kayıt sonrası otomatik giriş yap
      const loginResponse = await authAPI.girisYap({
        email: formData.email,
        password: formData.password,
      });
      
      const { token, roller } = loginResponse.data;
      login(token);
      
      // Tüm roller için home sayfasına yönlendir
      navigate('/');
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Kayıt olurken bir hata oluştu';
      const errorStatus = err.response?.status;
      const errorData = err.response?.data;
      
      // Selenium testleri için detaylı hata bilgisi
      window.lastRegistrationError = {
        message: errorMessage,
        status: errorStatus,
        statusText: err.response?.statusText,
        data: errorData,
        requestUrl: err.config?.url,
        requestData: formData,
        timestamp: new Date().toISOString()
      };
      
      console.error('Kayıt hatası:', {
        message: errorMessage,
        status: errorStatus,
        statusText: err.response?.statusText,
        data: errorData,
        requestUrl: err.config?.url
      });
      
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <header className="auth-header">
        <Link to="/" className="auth-logo">
          <img src={mediumLogo} alt="Medium" className="medium-logo-img" />
        </Link>
      </header>

      <main className="auth-main">
        <div className="auth-container">
          <div className="auth-card">
            <h1 className="auth-title">Kayıt ol</h1>
            <p className="auth-subtitle">Yeni hesap oluşturun</p>

            {error && <div className="auth-error">{error}</div>}

            <form onSubmit={handleSubmit} className="auth-form">
              <div className="form-group">
                <label htmlFor="firstName">Ad</label>
                <input
                  type="text"
                  id="firstName"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleChange}
                  required
                  placeholder="Adınız"
                  className="form-input"
                  autoComplete="given-name"
                />
              </div>

              <div className="form-group">
                <label htmlFor="lastName">Soyad</label>
                <input
                  type="text"
                  id="lastName"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleChange}
                  placeholder="Soyadınız"
                  className="form-input"
                  autoComplete="family-name"
                />
              </div>

              <div className="form-group">
                <label htmlFor="email">Email</label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  required
                  placeholder="ornek@email.com"
                  className="form-input"
                  autoComplete="email"
                />
              </div>

              <div className="form-group">
                <label htmlFor="username">Kullanıcı Adı</label>
                <input
                  type="text"
                  id="username"
                  name="username"
                  value={formData.username}
                  onChange={handleChange}
                  placeholder="kullaniciadi"
                  className="form-input"
                  autoComplete="username"
                />
              </div>

              <div className="form-group">
                <label htmlFor="password">Şifre</label>
                <input
                  type="password"
                  id="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  required
                  minLength={6}
                  placeholder="En az 6 karakter"
                  className="form-input"
                  autoComplete="new-password"
                />
              </div>

              <div className="form-group">
                <label htmlFor="roleName">Rol Seçin</label>
                <select
                  id="roleName"
                  name="roleName"
                  value={formData.roleName}
                  onChange={handleChange}
                  required
                  className="form-input"
                >
                  <option value="USER">Kullanıcı - Okuma, beğeni, kayıt, liste oluşturma ve takip yetkisi</option>
                  <option value="WRITER">Yazar - İçerik oluşturma yetkisi</option>
                </select>
                <small className="form-hint">Kayıt olduktan sonra seçtiğiniz role göre dashboard'a yönlendirileceksiniz.</small>
              </div>

              <button type="submit" className="auth-submit-btn" disabled={loading}>
                {loading ? 'Kayıt yapılıyor...' : 'Kayıt ol'}
              </button>
            </form>

            <div className="auth-divider">
              <span>veya</span>
            </div>

            <p className="auth-footer">
              Zaten hesabınız var mı? <Link to="/login" className="auth-link">Giriş yap</Link>
            </p>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Register;
