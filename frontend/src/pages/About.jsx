import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import mediumLogo from '../assets/medium-logo-png_seeklogo-347160.png';
import './About.css';

const About = () => {
  const { isAuthenticated } = useAuth();

  return (
    <div className="about-page">
      <header className="about-header">
        <div className="about-header-container">
          <Link to="/" className="about-logo">
            <img src={mediumLogo} alt="Medium" className="medium-logo-img" />
          </Link>
          
          <nav className="about-nav">
            {isAuthenticated ? (
              <>
                <Link to="/reader/new-story" className="about-nav-link">Yaz</Link>
                <Link to="/reader/dashboard" className="about-nav-link">Dashboard</Link>
              </>
            ) : (
              <>
                <Link to="/membership" className="about-nav-link">Üyelik</Link>
                <Link to="/login" className="about-nav-link">Giriş Yap</Link>
                <Link to="/register" className="about-get-started-btn">Başla</Link>
              </>
            )}
          </nav>
        </div>
      </header>

      <main className="about-main">
        <div className="about-hero">
          <h1 className="about-hero-title">Hikayemiz</h1>
          <p className="about-hero-subtitle">
            Fikirleri paylaşmak, hikayeleri anlatmak ve düşünceleri derinleştirmek için bir platform
          </p>
        </div>

        <div className="about-container">
          <article className="about-content">
            <section className="about-section">
              <h2 className="about-section-title">Nasıl Başladık</h2>
              <div className="about-section-content">
                <p>
                  2024 yılında, yazılım doğrulama ve geçerleme dersi kapsamında başladığımız bu proje, 
                  Medium.com'dan ilham alarak geliştirildi. Amacımız, kullanıcıların fikirlerini özgürce 
                  paylaşabileceği, kaliteli içerikler oluşturabileceği ve birbirleriyle etkileşime geçebileceği 
                  bir platform oluşturmaktı.
                </p>
                <p>
                  Başlangıçta basit bir blog platformu olarak tasarlanan sistem, zamanla gelişerek 
                  kapsamlı bir içerik yönetim platformuna dönüştü. Kullanıcılar artık sadece okumakla kalmıyor, 
                  aynı zamanda kendi hikayelerini yazabiliyor, kod blokları ekleyebiliyor ve görsellerle 
                  içeriklerini zenginleştirebiliyorlar.
                </p>
              </div>
            </section>

            <section className="about-section">
              <h2 className="about-section-title">Ne Yaptık</h2>
              <div className="about-section-content">
                <p>
                  Platformumuz, modern web teknolojileri kullanılarak geliştirildi. Backend tarafında 
                  Spring Boot ve Java, frontend tarafında ise React kullanıldı. Sistem, rol tabanlı 
                  erişim kontrolü ile güvenli bir şekilde çalışıyor.
                </p>
                <h3 className="about-subsection-title">Özellikler</h3>
                <ul className="about-features-list">
                  <li>
                    <strong>Zengin Metin Editörü:</strong> Medium.com benzeri, kod blokları ve görseller 
                    ekleyebileceğiniz gelişmiş bir editör
                  </li>
                  <li>
                    <strong>Rol Tabanlı Sistem:</strong> Okuyucu, Yazar, Editör ve Admin rolleri ile 
                    esnek yetkilendirme
                  </li>
                  <li>
                    <strong>Etkileşim Özellikleri:</strong> Beğeni, yorum, kaydetme ve paylaşma özellikleri
                  </li>
                  <li>
                    <strong>Kategori ve Etiket Sistemi:</strong> İçerikleri organize etmek için kapsamlı 
                    kategorilendirme
                  </li>
                  <li>
                    <strong>Dosya Yükleme:</strong> Görseller ve diğer medya dosyalarını güvenli bir şekilde 
                    yükleme imkanı
                  </li>
                  <li>
                    <strong>Bildirim Sistemi:</strong> Kullanıcıları önemli olaylardan haberdar eden 
                    bildirim sistemi
                  </li>
                </ul>
              </div>
            </section>

            <section className="about-section">
              <h2 className="about-section-title">Teknoloji Stack</h2>
              <div className="about-section-content">
                <div className="tech-grid">
                  <div className="tech-item">
                    <h4>Backend</h4>
                    <ul>
                      <li>Spring Boot</li>
                      <li>Java 17</li>
                      <li>PostgreSQL</li>
                      <li>JWT Authentication</li>
                    </ul>
                  </div>
                  <div className="tech-item">
                    <h4>Frontend</h4>
                    <ul>
                      <li>React</li>
                      <li>React Router</li>
                      <li>Axios</li>
                      <li>CSS Modules</li>
                    </ul>
                  </div>
                  <div className="tech-item">
                    <h4>Özellikler</h4>
                    <ul>
                      <li>RESTful API</li>
                      <li>File Upload</li>
                      <li>Role-Based Access</li>
                      <li>Responsive Design</li>
                    </ul>
                  </div>
                </div>
              </div>
            </section>

            <section className="about-section">
              <h2 className="about-section-title">Gelecek Planlarımız</h2>
              <div className="about-section-content">
                <p>
                  Platformumuz sürekli gelişiyor. Gelecekte eklemeyi planladığımız özellikler arasında:
                </p>
                <ul className="about-features-list">
                  <li>Gerçek zamanlı yorum sistemi</li>
                  <li>Gelişmiş arama ve filtreleme</li>
                  <li>Kullanıcı takip sistemi</li>
                  <li>Premium üyelik özellikleri</li>
                  <li>Mobil uygulama</li>
                  <li>Çoklu dil desteği</li>
                </ul>
              </div>
            </section>

            <section className="about-section">
              <h2 className="about-section-title">Bize Ulaşın</h2>
              <div className="about-section-content">
                <p>
                  Sorularınız, önerileriniz veya geri bildirimleriniz için bizimle iletişime geçebilirsiniz.
                </p>
                <div className="about-contact">
                  <p>
                    <strong>Email:</strong> info@medium.com
                  </p>
                  <p>
                    <strong>Adres:</strong> İstanbul, Türkiye
                  </p>
                </div>
              </div>
            </section>
          </article>
        </div>
      </main>

      <footer className="about-footer">
        <div className="about-footer-container">
          <Link to="/help" className="about-footer-link">Yardım</Link>
          <Link to="/status" className="about-footer-link">Durum</Link>
          <Link to="/about" className="about-footer-link">Hakkında</Link>
          <Link to="/careers" className="about-footer-link">Kariyer</Link>
          <Link to="/press" className="about-footer-link">Basın</Link>
          <Link to="/blog" className="about-footer-link">Blog</Link>
          <Link to="/privacy" className="about-footer-link">Gizlilik</Link>
          <Link to="/rules" className="about-footer-link">Kurallar</Link>
          <Link to="/terms" className="about-footer-link">Şartlar</Link>
          <Link to="/text-to-speech" className="about-footer-link">Metinden sese</Link>
        </div>
      </footer>
    </div>
  );
};

export default About;

