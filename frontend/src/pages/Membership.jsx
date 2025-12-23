import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import mediumLogo from '../assets/medium-logo-png_seeklogo-347160.png';
import './Membership.css';

const Membership = () => {
  const { isAuthenticated } = useAuth();

  return (
    <div className="membership-page">
      <header className="membership-header">
        <div className="membership-header-container">
          <Link to="/" className="membership-logo">
            <img src={mediumLogo} alt="Medium" className="medium-logo-img" />
          </Link>
          
          <nav className="membership-nav">
            <Link to="/about" className="membership-nav-link">Hikayemiz</Link>
            {isAuthenticated ? (
              <Link to="/reader/dashboard" className="membership-nav-link">Dashboard</Link>
            ) : (
              <>
                <Link to="/login" className="membership-nav-link">Giriş Yap</Link>
                <Link to="/register" className="membership-get-started-btn">Başla</Link>
              </>
            )}
          </nav>
        </div>
      </header>

      <main className="membership-main">
        <div className="membership-hero">
          <h1 className="membership-hero-title">Medium'a katıl</h1>
          <p className="membership-hero-subtitle">
            Sınırsız erişim, daha iyi okuma deneyimi ve daha fazlası
          </p>
        </div>

        <div className="membership-container">
          <div className="membership-plans">
            <div className="membership-plan-card">
              <div className="plan-header">
                <h2 className="plan-name">Ücretsiz</h2>
                <div className="plan-price">
                  <span className="price-amount">₺0</span>
                  <span className="price-period">/ay</span>
                </div>
              </div>
              <ul className="plan-features">
                <li className="plan-feature">✓ Sınırlı makale okuma</li>
                <li className="plan-feature">✓ Temel özellikler</li>
                <li className="plan-feature">✓ Topluluk erişimi</li>
              </ul>
              <Link to="/register" className="plan-button">
                Başla
              </Link>
            </div>

            <div className="membership-plan-card featured">
              <div className="plan-badge">Önerilen</div>
              <div className="plan-header">
                <h2 className="plan-name">Premium</h2>
                <div className="plan-price">
                  <span className="price-amount">₺49</span>
                  <span className="price-period">/ay</span>
                </div>
              </div>
              <ul className="plan-features">
                <li className="plan-feature">✓ Sınırsız makale okuma</li>
                <li className="plan-feature">✓ Reklamsız deneyim</li>
                <li className="plan-feature">✓ Öncelikli destek</li>
                <li className="plan-feature">✓ Özel içerikler</li>
                <li className="plan-feature">✓ Gelişmiş analitikler</li>
              </ul>
              <Link to={isAuthenticated ? "/reader/dashboard" : "/register"} className="plan-button featured-btn">
                Premium'a geç
              </Link>
            </div>

            <div className="membership-plan-card">
              <div className="plan-header">
                <h2 className="plan-name">Yıllık</h2>
                <div className="plan-price">
                  <span className="price-amount">₺490</span>
                  <span className="price-period">/yıl</span>
                </div>
                <div className="plan-savings">2 ay ücretsiz</div>
              </div>
              <ul className="plan-features">
                <li className="plan-feature">✓ Tüm Premium özellikler</li>
                <li className="plan-feature">✓ %17 tasarruf</li>
                <li className="plan-feature">✓ Öncelikli güncellemeler</li>
              </ul>
              <Link to={isAuthenticated ? "/reader/dashboard" : "/register"} className="plan-button">
                Yıllık plana geç
              </Link>
            </div>
          </div>

          <div className="membership-faq">
            <h2 className="faq-title">Sık Sorulan Sorular</h2>
            <div className="faq-list">
              <div className="faq-item">
                <h3 className="faq-question">Premium üyelik nedir?</h3>
                <p className="faq-answer">
                  Premium üyelik ile sınırsız makale okuyabilir, reklamsız deneyim yaşayabilir ve özel içeriklere erişebilirsiniz.
                </p>
              </div>
              <div className="faq-item">
                <h3 className="faq-question">İstediğim zaman iptal edebilir miyim?</h3>
                <p className="faq-answer">
                  Evet, üyeliğinizi istediğiniz zaman iptal edebilirsiniz. İptal ettiğinizde, ödediğiniz süre boyunca premium özelliklere erişmeye devam edersiniz.
                </p>
              </div>
              <div className="faq-item">
                <h3 className="faq-question">Ödeme nasıl yapılır?</h3>
                <p className="faq-answer">
                  Güvenli ödeme yöntemleri ile kredi kartı veya banka kartı ile ödeme yapabilirsiniz.
                </p>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Membership;

