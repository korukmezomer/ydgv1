import './Footer.css';

const Footer = () => {
  return (
    <footer className="footer">
      <div className="footer-container">
        <div className="footer-content">
          <div className="footer-section">
            <h3>Yazılım Doğrulama</h3>
            <p>Fikirlerinizi paylaşın, dünyayı değiştirin.</p>
          </div>
          
          <div className="footer-section">
            <h4>Hızlı Linkler</h4>
            <ul>
              <li><a href="/">Ana Sayfa</a></li>
              <li><a href="/haberler">Haberler</a></li>
              <li><a href="/kategoriler">Kategoriler</a></li>
            </ul>
          </div>
          
          <div className="footer-section">
            <h4>İletişim</h4>
            <p>Email: info@yazilimdogrulama.com</p>
            <p>Tel: +90 (XXX) XXX XX XX</p>
          </div>
        </div>
        
        <div className="footer-bottom">
          <p>&copy; 2025 Yazılım Doğrulama. Tüm hakları saklıdır.</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;

