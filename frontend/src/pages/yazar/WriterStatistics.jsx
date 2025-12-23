import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';
import './WriterStatistics.css';

const WriterStatistics = ({ sidebarOpen, setSidebarOpen }) => {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    toplamHaber: 0,
    toplamOkunma: 0,
    toplamBegeni: 0,
    toplamYorum: 0,
    yayinlananHaber: 0,
    taslakHaber: 0
  });

  useEffect(() => {
    if (user?.id) {
      fetchStats();
    }
  }, [user]);

  const fetchStats = async () => {
    try {
      setLoading(true);
      
      // Kullanıcının tüm haberlerini getir
      const haberlerResponse = await api.get(`/haberler/kullanici/${user.id}`, { 
        params: { page: 0, size: 100 } 
      }).catch(() => ({ data: { content: [] } }));
      
      const haberler = haberlerResponse.data?.content || [];
      
      // İstatistikleri hesapla
      const toplamHaber = haberler.length;
      const yayinlananHaber = haberler.filter(h => h.durum === 'YAYINLANDI' || h.status === 'YAYINLANDI').length;
      const taslakHaber = haberler.filter(h => h.durum === 'TASLAK' || h.status === 'TASLAK').length;
      const toplamOkunma = haberler.reduce((sum, haber) => sum + (haber.okunmaSayisi || haber.viewCount || 0), 0);
      const toplamBegeni = haberler.reduce((sum, haber) => sum + (haber.begeniSayisi || haber.likeCount || 0), 0);
      
      // Yorum sayısını StoryResponse'dan al (alt yorumlar dahil)
      // Backend'de commentCount her yorum oluşturulduğunda (ana yorum veya alt yorum) artırılıyor
      const toplamYorum = haberler.reduce((sum, haber) => {
        const yorumSayisi = haber.yorumSayisi || haber.commentCount || 0;
        return sum + yorumSayisi;
      }, 0);
      
      setStats({
        toplamHaber,
        toplamOkunma,
        toplamBegeni,
        toplamYorum,
        yayinlananHaber,
        taslakHaber
      });
    } catch (error) {
      console.error('İstatistikler yüklenirken hata:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className={`writer-statistics-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
        <div className="loading">Yükleniyor...</div>
      </div>
    );
  }

  return (
    <div className={`writer-statistics-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
      <main className="writer-statistics-main">
        <div className="writer-statistics-container">
          <h1 className="writer-statistics-title">İstatistikler</h1>
          
          <div className="writer-statistics-grid">
            <div className="writer-stat-card">
              <div className="writer-stat-icon">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M14 2v6h6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </div>
              <div className="writer-stat-value">{stats.toplamHaber}</div>
              <div className="writer-stat-label">Toplam Hikaye</div>
            </div>
            
            <div className="writer-stat-card">
              <div className="writer-stat-icon">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  <circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </div>
              <div className="writer-stat-value">{stats.toplamOkunma}</div>
              <div className="writer-stat-label">Toplam Okunma</div>
            </div>
            
            <div className="writer-stat-card">
              <div className="writer-stat-icon">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                  <path d="M7.5 21L12 18l4.5 3-1.5-5.5L20 12l-5.5-.5L12 6l-2.5 5.5L4 12l4.5 3.5L7.5 21z" fill="currentColor"/>
                </svg>
              </div>
              <div className="writer-stat-value">{stats.toplamBegeni}</div>
              <div className="writer-stat-label">Toplam Beğeni</div>
            </div>
            
            <div className="writer-stat-card">
              <div className="writer-stat-icon">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </div>
              <div className="writer-stat-value">{stats.toplamYorum}</div>
              <div className="writer-stat-label">Toplam Yorum</div>
            </div>

            <div className="writer-stat-card">
              <div className="writer-stat-icon">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                  <path d="M9 11l3 3L22 4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </div>
              <div className="writer-stat-value">{stats.yayinlananHaber}</div>
              <div className="writer-stat-label">Yayınlanan</div>
            </div>

            <div className="writer-stat-card">
              <div className="writer-stat-icon">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M14 2v6h6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M16 13H8" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </div>
              <div className="writer-stat-value">{stats.taslakHaber}</div>
              <div className="writer-stat-label">Taslak</div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default WriterStatistics;

