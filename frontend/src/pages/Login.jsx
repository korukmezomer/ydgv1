import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authAPI } from '../services/api';
import { getUserFromToken } from '../utils/jwt';
import mediumLogo from '../assets/medium-logo-png_seeklogo-347160.png';
import './Auth.css';

const Login = () => {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
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
      const response = await authAPI.girisYap(formData);
      const { token, roller } = response.data;
      
      login(token);
      
      // Tüm roller için home sayfasına yönlendir
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Giriş yapılırken bir hata oluştu');
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
            <h1 className="auth-title">Giriş yap</h1>
            <p className="auth-subtitle">Hesabınıza giriş yapın</p>

            {error && <div className="auth-error">{error}</div>}

            <form onSubmit={handleSubmit} className="auth-form">
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
                  placeholder="••••••••"
                  className="form-input"
                />
              </div>

              <button type="submit" className="auth-submit-btn" disabled={loading}>
                {loading ? 'Giriş yapılıyor...' : 'Giriş yap'}
              </button>
            </form>

            <div className="auth-divider">
              <span>veya</span>
            </div>

            <p className="auth-footer">
              Hesabınız yok mu? <Link to="/register" className="auth-link">Kayıt ol</Link>
            </p>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Login;
