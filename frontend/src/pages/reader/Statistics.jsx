import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { kayitliHaberAPI } from '../../services/api';
import './Statistics.css';

const Statistics = ({ sidebarOpen, setSidebarOpen }) => {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    toplamHaber: 0,
    toplamOkunma: 0,
    toplamBegeni: 0,
    toplamYorum: 0
  });

  useEffect(() => {
    // Reader istatistikleri için sadece login olmuş olmak yeterli
    if (user) {
      fetchStats();
    }
  }, [user]);

  const fetchStats = async () => {
    try {
      setLoading(true);

      // Reader için istatistikleri KAYITLI HİKAYELER üzerinden hesapla
      // (kütüphaneye eklediği / kaydettiği yazılar)
      const page = 0;
      const size = 200;

      const kayitliResponse = await kayitliHaberAPI
        .getAll({ page, size })
        .catch((error) => {
          console.error('📉 Reader istatistikleri için kayıtlı haberler yüklenirken hata:', error);
          return { data: { content: [] } };
        });

      const kayitliHaberler = kayitliResponse.data?.content || [];

      console.log('📊 Reader istatistikleri - kayıtlı haberler:', kayitliHaberler);

      // İstatistikleri hesapla
      const toplamHaber = kayitliHaberler.length;
      const toplamOkunma = kayitliHaberler.reduce(
        (sum, haber) => sum + (haber.okunmaSayisi || haber.viewCount || 0),
        0
      );
      const toplamBegeni = kayitliHaberler.reduce(
        (sum, haber) => sum + (haber.begeniSayisi || haber.likeCount || 0),
        0
      );

      // Yorum sayısını StoryResponse'dan al (alt yorumlar dahil)
      // Backend'de commentCount her yorum oluşturulduğunda (ana yorum veya alt yorum) artırılıyor
      const toplamYorum = kayitliHaberler.reduce((sum, haber) => {
        const yorumSayisi = haber.yorumSayisi || haber.commentCount || 0;
        return sum + yorumSayisi;
      }, 0);

      console.log('📊 Reader istatistikleri hesaplandı:', {
        toplamHaber,
        toplamOkunma,
        toplamBegeni,
        toplamYorum,
      });

      setStats({
        toplamHaber,
        toplamOkunma,
        toplamBegeni,
        toplamYorum
      });
    } catch (error) {
      console.error('İstatistikler yüklenirken hata:', error);
      // Hata durumunda boş istatistikler göster
      setStats({
        toplamHaber: 0,
        toplamOkunma: 0,
        toplamBegeni: 0,
        toplamYorum: 0
      });
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className={`statistics-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
        <div className="loading">Yükleniyor...</div>
      </div>
    );
  }

  return (
    <div className={`statistics-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
      <main className="statistics-main">
        <div className="statistics-container">
          <h1 className="statistics-title">İstatistikler</h1>
          
          <div className="statistics-grid">
            <div className="stat-card">
              <div className="stat-icon">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M14 2v6h6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </div>
              <div className="stat-value">{stats.toplamHaber}</div>
              <div className="stat-label">Toplam Hikaye</div>
            </div>
            
            <div className="stat-card">
              <div className="stat-icon">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  <circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </div>
              <div className="stat-value">{stats.toplamOkunma}</div>
              <div className="stat-label">Toplam Okunma</div>
            </div>
            
            <div className="stat-card">
              <div className="stat-icon">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                  <path d="M7.5 21L12 18l4.5 3-1.5-5.5L20 12l-5.5-.5L12 6l-2.5 5.5L4 12l4.5 3.5L7.5 21z" fill="currentColor"/>
                </svg>
              </div>
              <div className="stat-value">{stats.toplamBegeni}</div>
              <div className="stat-label">Toplam Beğeni</div>
            </div>
            
            <div className="stat-card">
              <div className="stat-icon">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </div>
              <div className="stat-value">{stats.toplamYorum}</div>
              <div className="stat-label">Toplam Yorum</div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Statistics;

