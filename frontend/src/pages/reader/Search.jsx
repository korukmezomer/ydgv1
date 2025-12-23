import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import { haberAPI, kategoriAPI, kullaniciAPI } from '../../services/api';
import './Search.css';

const Search = ({ sidebarOpen, setSidebarOpen }) => {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const query = searchParams.get('q') || '';
  const [searchQuery, setSearchQuery] = useState(query);
  const [results, setResults] = useState([]);
  const [kullaniciResults, setKullaniciResults] = useState([]);
  const [kategoriler, setKategoriler] = useState([]);
  const [staffPicks, setStaffPicks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('stories');

  useEffect(() => {
    if (query) {
      performSearch(query);
    }
    fetchSidebarData();
  }, [query]);

  const fetchSidebarData = async () => {
    try {
      const [populerResponse, kategorilerResponse] = await Promise.all([
        haberAPI.getPopular({ page: 0, size: 5 }).catch(() => ({ data: { content: [] } })),
        kategoriAPI.getAll().catch(() => ({ data: [] }))
      ]);
      setStaffPicks(populerResponse.data?.content || []);
      setKategoriler(kategorilerResponse.data || []);
    } catch (error) {
      console.error('Sidebar verileri yüklenirken hata:', error);
    }
  };

  const performSearch = async (searchTerm) => {
    if (!searchTerm.trim()) {
      setResults([]);
      setKullaniciResults([]);
      return;
    }

    setLoading(true);
    try {
      const [haberResponse, kullaniciResponse] = await Promise.all([
        haberAPI.search(searchTerm, { page: 0, size: 20 }).catch(() => ({ data: { content: [] } })),
        kullaniciAPI.search(searchTerm, { page: 0, size: 20 }).catch(() => ({ data: { content: [] } }))
      ]);
      setResults(haberResponse.data?.content || []);
      setKullaniciResults(kullaniciResponse.data?.content || []);
    } catch (error) {
      console.error('Arama hatası:', error);
      setResults([]);
      setKullaniciResults([]);
    } finally {
      setLoading(false);
    }
  };

  // URL'deki query değiştiğinde searchQuery'yi güncelle
  useEffect(() => {
    if (query) {
      setSearchQuery(query);
    }
  }, [query]);

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const months = ['Oca', 'Şub', 'Mar', 'Nis', 'May', 'Haz', 'Tem', 'Ağu', 'Eyl', 'Eki', 'Kas', 'Ara'];
    return `${months[date.getMonth()]} ${date.getDate()}`;
  };

  const handleArticleClick = (slug) => {
    navigate(`/haberler/${slug}`);
  };

  return (
    <div className={`search-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
      <main className="search-main">
        <div className="search-content">
          {query && (
            <h1 className="search-title">
              {query} için sonuçlar
            </h1>
          )}

          {/* Tabs - Sadece query varsa göster */}
          {query && (
            <div className="search-tabs">
              <button
                className={`search-tab ${activeTab === 'stories' ? 'active' : ''}`}
                onClick={() => setActiveTab('stories')}
              >
                Hikayeler
              </button>
              <button
                className={`search-tab ${activeTab === 'people' ? 'active' : ''}`}
                onClick={() => setActiveTab('people')}
              >
                İnsanlar
              </button>
              <button
                className={`search-tab ${activeTab === 'publications' ? 'active' : ''}`}
                onClick={() => setActiveTab('publications')}
              >
                Yayınlar
              </button>
              <button
                className={`search-tab ${activeTab === 'topics' ? 'active' : ''}`}
                onClick={() => setActiveTab('topics')}
              >
                Konular
              </button>
              <button
                className={`search-tab ${activeTab === 'lists' ? 'active' : ''}`}
                onClick={() => setActiveTab('lists')}
              >
                Listeler
              </button>
            </div>
          )}

          {/* Results */}
          {loading ? (
            <div className="loading">Aranıyor...</div>
          ) : query && activeTab === 'stories' ? (
            results.length > 0 ? (
              <div className="search-results">
                {results.map((story) => (
                  <article 
                    key={story.id} 
                    className="search-result-item"
                    onClick={() => handleArticleClick(story.slug)}
                  >
                    <div className="result-content">
                      <div className="result-header">
                        <span className="result-author">
                          {story.kullaniciAdi || 'Yazar'}
                        </span>
                        <span className="result-date">{formatDate(story.createdAt)}</span>
                      </div>
                      <h2 className="result-title">{story.baslik}</h2>
                      <p className="result-excerpt">{story.ozet || story.icerik?.substring(0, 200)}</p>
                      <div className="result-footer">
                        <div className="result-engagement">
                          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                            <path d="M7.5 21L12 18l4.5 3-1.5-5.5L20 12l-5.5-.5L12 6l-2.5 5.5L4 12l4.5 3.5L7.5 21z" fill="currentColor"/>
                          </svg>
                          <span>{story.begeniSayisi || 0}</span>
                        </div>
                        <div className="result-actions" onClick={(e) => e.stopPropagation()}>
                          <button 
                            className="result-action-btn" 
                            title="Kaydet"
                            onClick={(e) => {
                              e.preventDefault();
                              e.stopPropagation();
                              // TODO: Kaydet işlevi
                            }}
                          >
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                              <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" stroke="currentColor" strokeWidth="2"/>
                            </svg>
                          </button>
                          <button 
                            className="result-action-btn" 
                            title="Daha fazla"
                            onClick={(e) => {
                              e.preventDefault();
                              e.stopPropagation();
                              // TODO: Daha fazla menü
                            }}
                          >
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                              <circle cx="12" cy="5" r="1.5" fill="currentColor"/>
                              <circle cx="12" cy="12" r="1.5" fill="currentColor"/>
                              <circle cx="12" cy="19" r="1.5" fill="currentColor"/>
                            </svg>
                          </button>
                        </div>
                      </div>
                    </div>
                    {story.kapakResmiUrl && (
                      <div className="result-image">
                        <img src={story.kapakResmiUrl} alt={story.baslik} />
                      </div>
                    )}
                  </article>
                ))}
              </div>
            ) : (
              <div className="search-empty">
                <p>Tüm kelimelerin doğru yazıldığından emin olun.</p>
                <p>Farklı anahtar kelimeleri deneyin.</p>
                <p>Daha genel anahtar kelimeler deneyin.</p>
              </div>
            )
          ) : activeTab === 'people' ? (
            loading ? (
              <div className="loading">Aranıyor...</div>
            ) : kullaniciResults.length > 0 ? (
              <div className="search-results">
                {kullaniciResults.map((kullanici) => (
                  <div 
                    key={kullanici.id} 
                    className="search-result-item search-user-item"
                    onClick={() => {
                      // Başka kullanıcının profil sayfasına yönlendir
                      navigate(`/reader/user/${kullanici.id}`);
                    }}
                  >
                    <div className="user-avatar-large">
                      {kullanici.kullaniciAdi ? (
                        <span>{kullanici.kullaniciAdi.charAt(0).toUpperCase()}</span>
                      ) : kullanici.email ? (
                        <span>{kullanici.email.charAt(0).toUpperCase()}</span>
                      ) : (
                        <span>U</span>
                      )}
                    </div>
                    <div className="user-info">
                      <h3 className="user-name">
                        {kullanici.kullaniciAdi || kullanici.email || 'Kullanıcı'}
                      </h3>
                      {(kullanici.ad || kullanici.soyad) && (
                        <p className="user-full-name">
                          {kullanici.ad} {kullanici.soyad}
                        </p>
                      )}
                      {kullanici.email && (
                        <p className="user-email">{kullanici.email}</p>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="search-empty">
                <p>Bu arama için kullanıcı bulunamadı.</p>
              </div>
            )
          ) : !query ? (
            <div className="search-empty">
              <p>Arama yapmak için yukarıdaki arama kutusunu kullanın.</p>
            </div>
          ) : (
            <div className="search-empty">
              <p>Bu kategori için henüz sonuç bulunmamaktadır.</p>
            </div>
          )}
        </div>

        {/* Right Sidebar - Sadece query varsa göster */}
        {query && (
          <aside className="search-sidebar">
            {/* People matching */}
            <div className="sidebar-section">
              <h3 className="sidebar-title">{query} ile eşleşen kişiler</h3>
              <div className="people-list">
                {kullaniciResults.length > 0 ? (
                  kullaniciResults.slice(0, 5).map((kullanici) => (
                    <div 
                      key={kullanici.id} 
                      className="people-item"
                      onClick={() => {
                        // Başka kullanıcının profil sayfasına yönlendir
                        navigate(`/reader/user/${kullanici.id}`);
                      }}
                    >
                      <div className="people-avatar">
                        {kullanici.kullaniciAdi ? (
                          <span>{kullanici.kullaniciAdi.charAt(0).toUpperCase()}</span>
                        ) : kullanici.email ? (
                          <span>{kullanici.email.charAt(0).toUpperCase()}</span>
                        ) : (
                          <span>U</span>
                        )}
                      </div>
                      <div className="people-info">
                        <div className="people-name">
                          {kullanici.kullaniciAdi || kullanici.email || 'Kullanıcı'}
                        </div>
                        {(kullanici.ad || kullanici.soyad) && (
                          <div className="people-full-name">
                            {kullanici.ad} {kullanici.soyad}
                          </div>
                        )}
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="people-empty">
                    <p>Henüz eşleşen kişi bulunamadı</p>
                  </div>
                )}
              </div>
              {kullaniciResults.length > 5 && (
                <button 
                  className="sidebar-link-more"
                  onClick={() => setActiveTab('people')}
                >
                  Tümünü gör ({kullaniciResults.length})
                </button>
              )}
            </div>

            {/* Publications matching */}
            <div className="sidebar-section">
              <h3 className="sidebar-title">{query} ile eşleşen yayınlar</h3>
              <div className="publications-list">
                {/* TODO: Publications API eklendiğinde buraya eklenecek */}
                <div className="publications-empty">
                  <p>Henüz eşleşen yayın bulunamadı</p>
                </div>
              </div>
              <button className="sidebar-link-more">
                Tümünü gör
              </button>
            </div>
          </aside>
        )}
      </main>
    </div>
  );
};

export default Search;

