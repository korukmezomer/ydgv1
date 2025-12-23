import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link, useSearchParams } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { storyAPI, begeniAPI, kayitliHaberAPI, yorumAPI, takipAPI } from '../../services/api';
import './ArticleDetail.css';

const ArticleDetail = ({ sidebarOpen, setSidebarOpen }) => {
  const { slug } = useParams();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { user, isAuthenticated } = useAuth();
  const [story, setStory] = useState(null);
  const [loading, setLoading] = useState(true);
  const [begenildi, setBegenildi] = useState(false);
  const [kayitli, setKayitli] = useState(false);
  const [yorumlar, setYorumlar] = useState([]);
  const [yorumIcerik, setYorumIcerik] = useState('');
  const [yorumYukleniyor, setYorumYukleniyor] = useState(false);
  const [replyingTo, setReplyingTo] = useState(null); // Hangi yoruma cevap veriliyor
  const [replyContent, setReplyContent] = useState({}); // Her yorum için cevap içeriği
  const [yazarHaberler, setYazarHaberler] = useState([]);
  const [takipEdiliyor, setTakipEdiliyor] = useState(false);
  const [takipYukleniyor, setTakipYukleniyor] = useState(false);
  const [takipciSayisi, setTakipciSayisi] = useState(0);
  const [showShareMenu, setShowShareMenu] = useState(false);

  useEffect(() => {
    fetchArticle();
  }, [slug]);

  useEffect(() => {
    if (story && isAuthenticated) {
      checkBegeni();
      checkKayitli();
      fetchYorumlar();
      fetchYazarHaberler();
      checkTakip();
      fetchTakipciSayisi();
    }
  }, [story, isAuthenticated]);

  // Paylaş menüsünü dışarı tıklanınca kapat
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (showShareMenu && !event.target.closest('.share-button-wrapper')) {
        setShowShareMenu(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showShareMenu]);

  const fetchArticle = async () => {
    try {
      setLoading(true);
      const response = await storyAPI.getBySlug(slug);
      setStory(response.data);
    } catch (error) {
      console.error('Yazı yüklenirken hata:', error);
      navigate('/reader/dashboard');
    } finally {
      setLoading(false);
    }
  };

  const checkBegeni = async () => {
    try {
      const response = await begeniAPI.begenildiMi(story.id);
      setBegenildi(response.data === true);
    } catch (error) {
      // Hata durumunda beğenilmemiş say
    }
  };

  const checkKayitli = async () => {
    try {
      // Kayıtlı storyler listesinde bu story var mı kontrol et
      const response = await kayitliHaberAPI.getAll({ page: 0, size: 100 });
      const kayitliList = response.data?.content || [];
      const varMi = kayitliList.some(item => 
        (item.story?.id || item.id) === story.id
      );
      setKayitli(varMi);
    } catch (error) {
      // Hata durumunda kayıtlı değil say
    }
  };

  const fetchYorumlar = async () => {
    try {
      const response = await yorumAPI.getByHaberId(story.id);
      setYorumlar(response.data || []);
      
      // URL'de comment parametresi varsa o comment'e scroll yap
      const commentId = searchParams.get('comment');
      if (commentId) {
        setTimeout(() => {
          scrollToComment(commentId);
        }, 500); // Yorumlar render olduktan sonra scroll yap
      }
    } catch (error) {
      console.error('Yorumlar yüklenirken hata:', error);
    }
  };

  const scrollToComment = (commentId) => {
    const commentElement = document.getElementById(`comment-${commentId}`);
    if (commentElement) {
      commentElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
      // Comment'i highlight et
      commentElement.style.backgroundColor = '#fff3cd';
      setTimeout(() => {
        commentElement.style.backgroundColor = '';
      }, 2000);
    }
  };

  const fetchYazarHaberler = async () => {
    try {
      if (!story.kullaniciId) return;
      const response = await storyAPI.getByKullanici(story.kullaniciId, { 
        page: 0, 
        size: 10
      });
      const digerHaberler = (response.data?.content || [])
        .filter(h => h.id !== story.id);
      setYazarHaberler(digerHaberler.slice(0, 3));
    } catch (error) {
      console.error('Yazar storyleri yüklenirken hata:', error);
    }
  };

  const checkTakip = async () => {
    try {
      const yazarId = story.kullaniciId || story.userId;
      if (!yazarId || !isAuthenticated) return;
      
      // Admin kullanıcıları takip edilemez
      const yazarRolleri = story.kullaniciRolleri || story.userRoles || [];
      if (yazarRolleri.includes('ADMIN')) {
        setTakipEdiliyor(false);
        return;
      }
      
      const response = await takipAPI.takipEdiliyorMu(yazarId);
      setTakipEdiliyor(response.data === true);
    } catch (error) {
      console.error('Takip durumu kontrol edilirken hata:', error);
      setTakipEdiliyor(false);
    }
  };

  const fetchTakipciSayisi = async () => {
    try {
      const yazarId = story.kullaniciId || story.userId;
      if (!yazarId) return;
      const response = await takipAPI.takipciSayisi(yazarId);
      setTakipciSayisi(response.data || 0);
    } catch (error) {
      console.error('Takipçi sayısı yüklenirken hata:', error);
    }
  };

  const handleTakip = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    
    if (!story.kullaniciId) return;
    
    // Kendini takip edemez
    if (user?.id === story.kullaniciId) {
      return;
    }

    try {
      setTakipYukleniyor(true);
      if (takipEdiliyor) {
        await takipAPI.takibiBirak(story.kullaniciId);
        setTakipEdiliyor(false);
        setTakipciSayisi(prev => Math.max(0, prev - 1));
      } else {
        await takipAPI.takipEt(story.kullaniciId);
        setTakipEdiliyor(true);
        setTakipciSayisi(prev => prev + 1);
      }
    } catch (error) {
      console.error('Takip hatası:', error);
      alert('Takip işlemi sırasında bir hata oluştu');
    } finally {
      setTakipYukleniyor(false);
    }
  };

  const handleBegen = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    try {
      if (begenildi) {
        await begeniAPI.begeniyiKaldir(story.id);
        setBegenildi(false);
        setStory({ ...story, begeniSayisi: (story.begeniSayisi || 0) - 1 });
      } else {
        await begeniAPI.begen(story.id);
        setBegenildi(true);
        setStory({ ...story, begeniSayisi: (story.begeniSayisi || 0) + 1 });
      }
    } catch (error) {
      console.error('Beğeni hatası:', error);
    }
  };

  const handleKaydet = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    try {
      if (kayitli) {
        await kayitliHaberAPI.kaldir(story.id);
        setKayitli(false);
      } else {
        await kayitliHaberAPI.kaydet(story.id);
        setKayitli(true);
      }
    } catch (error) {
      console.error('Kaydetme hatası:', error);
    }
  };

  const handleYorumGonder = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    const trimmedContent = yorumIcerik.trim();
    if (!trimmedContent) return;

    try {
      setYorumYukleniyor(true);
      // Ana yorum için sadece content gönder
      const requestBody = { content: trimmedContent };
      console.log('Yorum gönderme request:', {
        haberId: story.id,
        requestBody,
        token: localStorage.getItem('token') ? 'Token var' : 'Token yok'
      });
      await yorumAPI.create(story.id, requestBody);
      setYorumIcerik('');
      
      // Yorumları yeniden yükle
      await fetchYorumlar();
      
      // Yorum sayısını güncelle
      setStory({ ...story, yorumSayisi: (story.yorumSayisi || 0) + 1 });
    } catch (error) {
      console.error('Yorum gönderme hatası:', error);
      console.error('Error response:', error.response);
      console.error('Error response data:', error.response?.data);
      console.error('Request config:', error.config);
      
      let errorMessage = 'Yorum gönderilirken bir hata oluştu';
      
      if (error.response?.data) {
        if (error.response.data.errors) {
          // Validation errors
          const fieldErrors = Object.values(error.response.data.errors).join(', ');
          errorMessage = `Hata: ${fieldErrors}`;
        } else if (error.response.data.message) {
          errorMessage = error.response.data.message;
        }
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      alert(errorMessage);
    } finally {
      setYorumYukleniyor(false);
    }
  };

  const handleReplyGonder = async (ustYorumId) => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    const replyText = replyContent[ustYorumId];
    const trimmedReply = replyText?.trim();
    if (!trimmedReply) return;

    try {
      setYorumYukleniyor(true);
      // parentCommentId'yi number'a çevir (backend Long bekliyor)
      // yorum.id zaten number olmalı ama emin olmak için Number() kullan
      const parentCommentIdNum = Number(ustYorumId);
      if (isNaN(parentCommentIdNum)) {
        console.error('Geçersiz yorum ID:', ustYorumId);
        alert('Geçersiz yorum ID');
        return;
      }
      
      const requestBody = { 
        content: trimmedReply,
        parentCommentId: parentCommentIdNum
      };
      console.log('Cevap gönderme request:', {
        haberId: story.id,
        storyIdType: typeof story.id,
        ustYorumId,
        ustYorumIdType: typeof ustYorumId,
        parentCommentId: parentCommentIdNum,
        parentCommentIdType: typeof parentCommentIdNum,
        requestBody: JSON.stringify(requestBody),
        token: localStorage.getItem('token') ? 'Token var' : 'Token yok'
      });
      await yorumAPI.create(story.id, requestBody);
      
      // Cevap içeriğini temizle
      setReplyContent({ ...replyContent, [ustYorumId]: '' });
      setReplyingTo(null);
      
      // Yorumları yeniden yükle
      await fetchYorumlar();
      
      // Yorum sayısını güncelle
      setStory({ ...story, yorumSayisi: (story.yorumSayisi || 0) + 1 });
    } catch (error) {
      console.error('Cevap gönderme hatası:', error);
      console.error('Error response:', error.response);
      console.error('Error response data:', JSON.stringify(error.response?.data, null, 2));
      console.error('Request config:', JSON.stringify({
        url: error.config?.url,
        method: error.config?.method,
        data: error.config?.data,
        headers: error.config?.headers
      }, null, 2));
      
      let errorMessage = 'Cevap gönderilirken bir hata oluştu';
      
      if (error.response?.data) {
        if (error.response.data.errors) {
          // Validation errors
          const fieldErrors = Object.values(error.response.data.errors).join(', ');
          errorMessage = `Hata: ${fieldErrors}`;
        } else if (error.response.data.message) {
          errorMessage = error.response.data.message;
        }
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      alert(errorMessage);
    } finally {
      setYorumYukleniyor(false);
    }
  };

  const handleReplyClick = (yorumId) => {
    if (replyingTo === yorumId) {
      setReplyingTo(null);
      setReplyContent({ ...replyContent, [yorumId]: '' });
    } else {
      setReplyingTo(yorumId);
      if (!replyContent[yorumId]) {
        setReplyContent({ ...replyContent, [yorumId]: '' });
      }
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const months = ['Oca', 'Şub', 'Mar', 'Nis', 'May', 'Haz', 'Tem', 'Ağu', 'Eyl', 'Eki', 'Kas', 'Ara'];
    return `${months[date.getMonth()]} ${date.getDate()}, ${date.getFullYear()}`;
  };

  const handleShare = () => {
    setShowShareMenu(!showShareMenu);
  };

  const handleCopyLink = () => {
    const url = window.location.href;
    navigator.clipboard.writeText(url).then(() => {
      alert('Link kopyalandı!');
      setShowShareMenu(false);
    }).catch(() => {
      alert('Link kopyalanırken bir hata oluştu');
    });
  };

  const handleShareTwitter = () => {
    const url = window.location.href;
    const text = story?.baslik || story?.title || '';
    const twitterUrl = `https://twitter.com/intent/tweet?url=${encodeURIComponent(url)}&text=${encodeURIComponent(text)}`;
    window.open(twitterUrl, '_blank', 'width=550,height=420');
    setShowShareMenu(false);
  };

  const handleShareFacebook = () => {
    const url = window.location.href;
    const facebookUrl = `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(url)}`;
    window.open(facebookUrl, '_blank', 'width=550,height=420');
    setShowShareMenu(false);
  };

  const handleShareLinkedIn = () => {
    const url = window.location.href;
    const linkedInUrl = `https://www.linkedin.com/sharing/share-offsite/?url=${encodeURIComponent(url)}`;
    window.open(linkedInUrl, '_blank', 'width=550,height=420');
    setShowShareMenu(false);
  };

  const handleShareEmail = () => {
    const url = window.location.href;
    const subject = encodeURIComponent(story?.baslik || story?.title || '');
    const body = encodeURIComponent(`Bu yazıyı okumanı öneriyorum: ${url}`);
    window.location.href = `mailto:?subject=${subject}&body=${body}`;
    setShowShareMenu(false);
  };

  const calculateReadTime = (icerik) => {
    if (!icerik) return 0;
    const words = icerik.split(/\s+/).length;
    return Math.ceil(words / 200); // Ortalama 200 kelime/dakika
  };

  // İçeriği parse edip render et
  const renderContent = (icerik) => {
    if (!icerik) {
      return <div className="article-content-text">İçerik bulunamadı.</div>;
    }

    const parts = [];
    let lastIndex = 0;

    // [CODE:language]...[/CODE] pattern
    const codePattern = /\[CODE:([^\]]*?)\]([\s\S]*?)\[\/CODE\]/g;
    // [IMAGE:url] pattern
    const imagePattern = /\[IMAGE:([^\]]+?)\]/g;
    // [VIDEO:url] pattern
    const videoPattern = /\[VIDEO:([^\]]+?)\]/g;
    // [EMBED:url] pattern
    const embedPattern = /\[EMBED:([^\]]+?)\]/g;
    // [TOC] pattern
    const tocPattern = /\[TOC\]/g;

    const matches = [];
    let match;

    while ((match = codePattern.exec(icerik)) !== null) {
      matches.push({
        type: 'code',
        start: match.index,
        end: match.index + match[0].length,
        language: match[1],
        content: match[2],
        match: match[0]
      });
    }

    codePattern.lastIndex = 0;

    while ((match = imagePattern.exec(icerik)) !== null) {
      matches.push({
        type: 'image',
        start: match.index,
        end: match.index + match[0].length,
        url: match[1],
        match: match[0]
      });
    }

    imagePattern.lastIndex = 0;

    while ((match = videoPattern.exec(icerik)) !== null) {
      matches.push({
        type: 'video',
        start: match.index,
        end: match.index + match[0].length,
        url: match[1],
        match: match[0]
      });
    }

    videoPattern.lastIndex = 0;

    while ((match = embedPattern.exec(icerik)) !== null) {
      matches.push({
        type: 'embed',
        start: match.index,
        end: match.index + match[0].length,
        url: match[1],
        match: match[0]
      });
    }

    embedPattern.lastIndex = 0;

    // TOC için başlıkları bul
    const headings = [];
    const headingPattern = /^(#{1,3})\s+(.+)$/gm;
    let headingMatch;
    while ((headingMatch = headingPattern.exec(icerik)) !== null) {
      headings.push({
        level: headingMatch[1].length,
        text: headingMatch[2].trim(),
        index: headingMatch.index
      });
    }

    while ((match = tocPattern.exec(icerik)) !== null) {
      matches.push({
        type: 'toc',
        start: match.index,
        end: match.index + match[0].length,
        headings: headings,
        match: match[0]
      });
    }

    matches.sort((a, b) => a.start - b.start);

    matches.forEach((match) => {
      if (match.start > lastIndex) {
        const text = icerik.substring(lastIndex, match.start);
        if (text.trim()) {
          parts.push({ type: 'text', content: text });
        }
      }

      if (match.type === 'code') {
        parts.push({
          type: 'code',
          language: match.language,
          content: match.content
        });
      } else if (match.type === 'image') {
        parts.push({
          type: 'image',
          url: match.url
        });
      } else if (match.type === 'video') {
        parts.push({
          type: 'video',
          url: match.url
        });
      } else if (match.type === 'embed') {
        parts.push({
          type: 'embed',
          url: match.url
        });
      } else if (match.type === 'toc') {
        parts.push({
          type: 'toc',
          headings: match.headings
        });
      }

      lastIndex = match.end;
    });

    if (lastIndex < icerik.length) {
      const text = icerik.substring(lastIndex);
      if (text.trim()) {
        parts.push({ type: 'text', content: text });
      }
    }

    if (parts.length === 0) {
      return <div className="article-content-text">{icerik}</div>;
    }

    // Metni parse et (başlıklar, quote'lar, listeler)
    const parseText = (text) => {
      const lines = text.split('\n');
      const elements = [];
      let currentList = null;
      let listItems = [];
      let currentQuote = null;
      let quoteLines = [];

      lines.forEach((line, i) => {
        // Başlıklar
        if (line.match(/^#{1,3}\s+/)) {
          if (currentList) {
            elements.push({ type: 'list', ordered: currentList.ordered, items: listItems });
            currentList = null;
            listItems = [];
          }
          if (currentQuote) {
            elements.push({ type: 'quote', text: quoteLines.join(' ') });
            currentQuote = null;
            quoteLines = [];
          }
          const level = line.match(/^(#{1,3})/)[1].length;
          const text = line.replace(/^#{1,3}\s+/, '');
          elements.push({ type: `h${level}`, text });
        }
        // Quote'lar - birden fazla satırlı quote'ları birleştir
        else if (line.match(/^>\s*/)) {
          if (currentList) {
            elements.push({ type: 'list', ordered: currentList.ordered, items: listItems });
            currentList = null;
            listItems = [];
          }
          const text = line.replace(/^>\s*/, '').trim();
          if (text) {
            if (!currentQuote) {
              currentQuote = true;
              quoteLines = [];
            }
            quoteLines.push(text);
          } else if (currentQuote && quoteLines.length > 0) {
            // Boş satır quote'u bitirir
            elements.push({ type: 'quote', text: quoteLines.join(' ') });
            currentQuote = null;
            quoteLines = [];
          }
        }
        // Numaralı liste
        else if (line.match(/^\d+\.\s+/)) {
          if (currentList && !currentList.ordered) {
            elements.push({ type: 'list', ordered: false, items: listItems });
            listItems = [];
          }
          if (!currentList) {
            currentList = { ordered: true };
          }
          const text = line.replace(/^\d+\.\s+/, '');
          listItems.push(text);
        }
        // Madde işaretli liste
        else if (line.match(/^[-*]\s+/)) {
          if (currentList && currentList.ordered) {
            elements.push({ type: 'list', ordered: true, items: listItems });
            listItems = [];
          }
          if (!currentList) {
            currentList = { ordered: false };
          }
          const text = line.replace(/^[-*]\s+/, '');
          listItems.push(text);
        }
        // Normal paragraf
        else if (line.trim()) {
          if (currentList) {
            elements.push({ type: 'list', ordered: currentList.ordered, items: listItems });
            currentList = null;
            listItems = [];
          }
          if (currentQuote) {
            elements.push({ type: 'quote', text: quoteLines.join(' ') });
            currentQuote = null;
            quoteLines = [];
          }
          elements.push({ type: 'p', text: line });
        } else {
          // Boş satır - mevcut quote veya listeyi bitir
          if (currentQuote && quoteLines.length > 0) {
            elements.push({ type: 'quote', text: quoteLines.join(' ') });
            currentQuote = null;
            quoteLines = [];
          }
        }
      });

      if (currentList) {
        elements.push({ type: 'list', ordered: currentList.ordered, items: listItems });
      }
      if (currentQuote && quoteLines.length > 0) {
        elements.push({ type: 'quote', text: quoteLines.join(' ') });
      }

      return elements;
    };

    return (
      <div className="article-content">
        {parts.map((part, index) => {
          if (part.type === 'code') {
            return (
              <div key={index} className="article-code-block">
                <div className="code-block-header">
                  <span className="code-language-badge">{part.language || 'text'}</span>
                </div>
                <pre className="code-block-content">
                  <code>{part.content}</code>
                </pre>
              </div>
            );
          } else if (part.type === 'image') {
            return (
              <div key={index} className="article-image-block">
                <img src={part.url} alt="Article" onError={(e) => {
                  e.target.style.display = 'none';
                  const errorDiv = e.target.nextSibling;
                  if (errorDiv) errorDiv.style.display = 'block';
                }} />
                <div style={{display: 'none', padding: '20px', textAlign: 'center', color: '#6b6b6b'}}>
                  Resim yüklenemedi
                </div>
              </div>
            );
          } else if (part.type === 'video') {
            const getYouTubeEmbedUrl = (url) => {
              const regExp = /^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|\&v=)([^#\&\?]*).*/;
              const match = url.match(regExp);
              if (match && match[2].length === 11) {
                return `https://www.youtube.com/embed/${match[2]}?autoplay=0&fs=1&iv_load_policy=3&rel=0&showinfo=0`;
              }
              return null;
            };

            const youtubeEmbedUrl = getYouTubeEmbedUrl(part.url);
            
            return (
              <div key={index} className="article-video-block">
                {youtubeEmbedUrl ? (
                  <div className="video-embed">
                    <iframe
                      src={youtubeEmbedUrl}
                      allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                      allowFullScreen
                      title="Video"
                    />
                  </div>
                ) : (
                  <video className="video-player" controls>
                    <source src={part.url} type="video/mp4" />
                    Tarayıcınız video oynatmayı desteklemiyor.
                  </video>
                )}
              </div>
            );
          } else if (part.type === 'embed') {
            return (
              <div key={index} className="article-embed-block">
                <div className="embed-preview">
                  <iframe
                    src={part.url}
                    title="Embed"
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                    allowFullScreen
                  />
                </div>
              </div>
            );
          } else if (part.type === 'toc') {
            return (
              <div key={index} className="article-toc">
                <h3 className="toc-title">İçindekiler</h3>
                <ul className="toc-list">
                  {part.headings && part.headings.length > 0 ? (
                    part.headings.map((heading, hIndex) => (
                      <li 
                        key={hIndex} 
                        className={`toc-item toc-level-${heading.level}`}
                      >
                        {heading.text}
                      </li>
                    ))
                  ) : (
                    <li className="toc-empty">Henüz başlık eklenmemiş</li>
                  )}
                </ul>
              </div>
            );
          } else {
            const elements = parseText(part.content);
            return (
              <div key={index} className="article-content-text">
                {elements.map((el, elIndex) => {
                  if (el.type === 'h1') {
                    return <h1 key={elIndex} className="article-heading article-h1">{el.text}</h1>;
                  } else if (el.type === 'h2') {
                    return <h2 key={elIndex} className="article-heading article-h2">{el.text}</h2>;
                  } else if (el.type === 'h3') {
                    return <h3 key={elIndex} className="article-heading article-h3">{el.text}</h3>;
                  } else if (el.type === 'quote') {
                    return <blockquote key={elIndex} className="article-quote">{el.text}</blockquote>;
                  } else if (el.type === 'list') {
                    if (el.ordered) {
                      return (
                        <ol key={elIndex} className="article-list article-ordered-list">
                          {el.items.map((item, itemIndex) => (
                            <li key={itemIndex}>{item}</li>
                          ))}
                        </ol>
                      );
                    } else {
                      return (
                        <ul key={elIndex} className="article-list article-unordered-list">
                          {el.items.map((item, itemIndex) => (
                            <li key={itemIndex}>{item}</li>
                          ))}
                        </ul>
                      );
                    }
                  } else {
                    return <p key={elIndex}>{el.text || '\u00A0'}</p>;
                  }
                })}
              </div>
            );
          }
        })}
      </div>
    );
  };

  if (loading) {
    return (
      <div className="article-detail-page">
        <div className="article-loading">Yükleniyor...</div>
      </div>
    );
  }

  if (!story) {
    return (
      <div className="article-detail-page">
        <div className="article-error">Yazı bulunamadı</div>
      </div>
    );
  }

  const readTime = calculateReadTime(story.icerik || story.content);

  return (
    <div className={`article-detail-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
      <main className="article-main">
        <article className="article-container">
          {/* Article Header */}
          <header className="article-header-section">
            <h1 className="article-title">{story.baslik}</h1>
            
            <div className="article-meta-top">
              <div className="article-author-info">
                <div className="author-avatar">
                  {story.kullaniciAdi ? (
                    <span>{story.kullaniciAdi.charAt(0).toUpperCase()}</span>
                  ) : (
                    <span>U</span>
                  )}
                </div>
                <div className="author-details">
                  <div className="author-name">{story.kullaniciAdi || 'Yazar'}</div>
                  <div className="article-meta-info">
                    <span>{readTime} dk okuma</span>
                    <span>•</span>
                    <span>{formatDate(story.createdAt)}</span>
                  </div>
                </div>
              </div>
              
              <div className="article-actions-top">
                <button 
                  className={`action-btn ${begenildi ? 'active' : ''}`}
                  onClick={handleBegen}
                  title="Beğen"
                >
                  <svg width="24" height="24" viewBox="0 0 24 24" fill={begenildi ? "currentColor" : "none"}>
                    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" stroke="currentColor" strokeWidth="2"/>
                  </svg>
                  <span>{story.begeniSayisi || 0}</span>
                </button>
                <button className="action-btn" title="Yorum">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" stroke="currentColor" strokeWidth="2"/>
                  </svg>
                  <span>{story.yorumSayisi || 0}</span>
                </button>
                <button 
                  className={`action-btn ${kayitli ? 'active' : ''}`}
                  onClick={handleKaydet}
                  title="Kaydet"
                >
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                    <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" stroke="currentColor" strokeWidth="2"/>
                  </svg>
                </button>
                <div className="share-button-wrapper">
                  <button 
                    className={`action-btn ${showShareMenu ? 'active' : ''}`}
                    title="Paylaş"
                    onClick={handleShare}
                  >
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                      <path d="M18 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6zM6 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6zM18 15a3 3 0 1 0 0 6 3 3 0 0 0 0-6z" stroke="currentColor" strokeWidth="2"/>
                      <path d="M8.59 13.51l6.82 3.98M15.41 6.51l-6.82 3.98" stroke="currentColor" strokeWidth="2"/>
                    </svg>
                  </button>
                  {showShareMenu && (
                    <div className="share-menu">
                      <div className="share-menu-header">
                        <h4>Paylaş</h4>
                        <button 
                          className="share-menu-close"
                          onClick={() => setShowShareMenu(false)}
                        >
                          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                            <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                          </svg>
                        </button>
                      </div>
                      <div className="share-menu-options">
                        <button className="share-option" onClick={handleCopyLink}>
                          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                            <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                          </svg>
                          <span>Linki kopyala</span>
                        </button>
                        <button className="share-option" onClick={handleShareTwitter}>
                          <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                            <path d="M23 3a10.9 10.9 0 0 1-3.14 1.53 4.48 4.48 0 0 0-7.86 3v1A10.66 10.66 0 0 1 3 4s-4 9 5 13a11.64 11.64 0 0 1-7 2c9 5 20 0 20-11.5a4.5 4.5 0 0 0-.08-.83A7.72 7.72 0 0 0 23 3z"/>
                          </svg>
                          <span>Twitter'da paylaş</span>
                        </button>
                        <button className="share-option" onClick={handleShareFacebook}>
                          <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                            <path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z"/>
                          </svg>
                          <span>Facebook'ta paylaş</span>
                        </button>
                        <button className="share-option" onClick={handleShareLinkedIn}>
                          <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                            <path d="M16 8a6 6 0 0 1 6 6v7h-4v-7a2 2 0 0 0-2-2 2 2 0 0 0-2 2v7h-4v-7a6 6 0 0 1 6-6zM2 9h4v12H2z"/>
                            <circle cx="4" cy="4" r="2"/>
                          </svg>
                          <span>LinkedIn'de paylaş</span>
                        </button>
                        <button className="share-option" onClick={handleShareEmail}>
                          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                            <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" stroke="currentColor" strokeWidth="2"/>
                            <polyline points="22,6 12,13 2,6" stroke="currentColor" strokeWidth="2"/>
                          </svg>
                          <span>E-posta ile paylaş</span>
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </header>

          {/* Article Content */}
          <div className="article-body">
            {renderContent(story.icerik || story.content)}
          </div>

          {/* Tags */}
          {story.kategoriAdi && (
            <div className="article-tags">
              <span className="tag">{story.kategoriAdi}</span>
            </div>
          )}

          {/* Author Card */}
          <div className="article-author-card">
            <div className="author-card-avatar">
              {story.kullaniciAdi ? (
                <span>{story.kullaniciAdi.charAt(0).toUpperCase()}</span>
              ) : (
                <span>U</span>
              )}
            </div>
            <div className="author-card-info">
              <div className="author-card-name">Yazan: {story.kullaniciAdi || story.username || 'Yazar'}</div>
              <div className="author-card-bio">Yazılım Geliştirici</div>
              {(() => {
                const yazarId = story.kullaniciId || story.userId;
                const yazarRolleri = story.kullaniciRolleri || story.userRoles || [];
                const isAdmin = yazarRolleri.includes('ADMIN');
                const isSelf = user?.id === yazarId;
                
                if (isSelf || isAdmin) {
                  return null; // Kendini veya admin'i takip edemez
                }
                
                return (
                  <button 
                    className={`author-follow-btn ${takipEdiliyor ? 'following' : ''}`}
                    onClick={handleTakip}
                    disabled={takipYukleniyor}
                  >
                    {takipYukleniyor ? 'Yükleniyor...' : (takipEdiliyor ? 'Takip Ediliyor' : 'Takip Et')}
                  </button>
                );
              })()}
              {takipciSayisi > 0 && (
                <div className="author-followers-count">{takipciSayisi} takipçi</div>
              )}
            </div>
          </div>

          {/* Comments Section */}
          <div className="article-comments">
            <h3 className="comments-title">
              {yorumlar.length === 0 ? 'Henüz yorum yok' : `${yorumlar.length} Yorum`}
            </h3>
            
            {isAuthenticated && (
              <div className="comment-form">
                <div className="comment-input-wrapper">
                  <div className="comment-avatar-small">
                    {user?.kullaniciAdi ? (
                      <span>{user.kullaniciAdi.charAt(0).toUpperCase()}</span>
                    ) : (
                      <span>U</span>
                    )}
                  </div>
                  <textarea
                    className="comment-textarea"
                    placeholder="Düşüncelerinizi yazın..."
                    value={yorumIcerik}
                    onChange={(e) => setYorumIcerik(e.target.value)}
                    rows={3}
                  />
                </div>
                <button 
                  className="comment-submit-btn"
                  onClick={handleYorumGonder}
                  disabled={yorumYukleniyor || !yorumIcerik.trim()}
                >
                  {yorumYukleniyor ? 'Gönderiliyor...' : 'Yorum Yap'}
                </button>
              </div>
            )}

            <div className="comments-list">
              {yorumlar.map((yorum) => {
                const isYazar = (yorum.userId || yorum.kullaniciId) === story.kullaniciId;
                const isCurrentUser = (yorum.userId || yorum.kullaniciId) === user?.id;
                
                return (
                  <div key={yorum.id} id={`comment-${yorum.id}`} className="comment-item">
                    <div className="comment-avatar">
                      {(yorum.username || yorum.kullaniciAdi) ? (
                        <span>{(yorum.username || yorum.kullaniciAdi).charAt(0).toUpperCase()}</span>
                      ) : (
                        <span>U</span>
                      )}
                    </div>
                    <div className="comment-content">
                      <div className="comment-header">
                        <div className="comment-author">
                          {yorum.username || yorum.kullaniciAdi || 'Kullanıcı'}
                          {isYazar && (
                            <span className="comment-author-badge">Yazar</span>
                          )}
                        </div>
                        <div className="comment-meta">
                          <span>{formatDate(yorum.createdAt)}</span>
                        </div>
                      </div>
                      <div className="comment-text">{yorum.content || yorum.icerik}</div>
                      <div className="comment-actions">
                        {isAuthenticated && !isCurrentUser && (
                          <button 
                            className="comment-reply-btn"
                            onClick={() => handleReplyClick(yorum.id)}
                          >
                            Cevap Ver
                          </button>
                        )}
                      </div>
                      
                      {/* Cevap Formu */}
                      {replyingTo === yorum.id && (
                        <div className="reply-form">
                          <div className="comment-input-wrapper">
                            <div className="comment-avatar-small">
                              {user?.kullaniciAdi ? (
                                <span>{user.kullaniciAdi.charAt(0).toUpperCase()}</span>
                              ) : (
                                <span>U</span>
                              )}
                            </div>
                            <textarea
                              className="comment-textarea"
                              placeholder={`${yorum.kullaniciAdi || 'Kullanıcı'}'ya cevap ver...`}
                              value={replyContent[yorum.id] || ''}
                              onChange={(e) => setReplyContent({ ...replyContent, [yorum.id]: e.target.value })}
                              rows={2}
                            />
                          </div>
                          <div className="reply-actions">
                            <button 
                              className="comment-submit-btn"
                              onClick={() => handleReplyGonder(yorum.id)}
                              disabled={yorumYukleniyor || !replyContent[yorum.id]?.trim()}
                            >
                              {yorumYukleniyor ? 'Gönderiliyor...' : 'Cevap Gönder'}
                            </button>
                            <button 
                              className="comment-cancel-btn"
                              onClick={() => handleReplyClick(yorum.id)}
                            >
                              İptal
                            </button>
                          </div>
                        </div>
                      )}
                      
                      {/* Alt Yorumlar (Replies) */}
                      {(yorum.replies || yorum.altYorumlar) && (yorum.replies || yorum.altYorumlar).length > 0 && (
                        <div className="comment-replies">
                          {(yorum.replies || yorum.altYorumlar).map((altYorum) => {
                            const isAltYorumYazar = (altYorum.userId || altYorum.kullaniciId) === story.kullaniciId;
                            return (
                              <div key={altYorum.id} id={`comment-${altYorum.id}`} className="comment-item comment-reply">
                                <div className="comment-avatar">
                                  {(altYorum.username || altYorum.kullaniciAdi) ? (
                                    <span>{(altYorum.username || altYorum.kullaniciAdi).charAt(0).toUpperCase()}</span>
                                  ) : (
                                    <span>U</span>
                                  )}
                                </div>
                                <div className="comment-content">
                                  <div className="comment-header">
                                    <div className="comment-author">
                                      {altYorum.username || altYorum.kullaniciAdi || 'Kullanıcı'}
                                      {isAltYorumYazar && (
                                        <span className="comment-author-badge">Yazar</span>
                                      )}
                                    </div>
                                    <div className="comment-meta">
                                      <span>{formatDate(altYorum.createdAt)}</span>
                                    </div>
                                  </div>
                                  <div className="comment-text">{altYorum.content || altYorum.icerik}</div>
                                </div>
                              </div>
                            );
                          })}
                        </div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* More from Author */}
          {yazarHaberler.length > 0 && (
            <div className="more-from-author">
              <h3 className="more-from-author-title">{story.kullaniciAdi || 'Yazar'}'dan daha fazla</h3>
              <div className="more-articles-list">
                {yazarHaberler.map((h) => (
                  <Link key={h.id} to={`/storyler/${h.slug}`} className="more-article-item">
                    <h4 className="more-article-title">{h.baslik}</h4>
                    <div className="more-article-meta">
                      <span>{formatDate(h.createdAt)}</span>
                      <span>•</span>
                      <span>{calculateReadTime(h.icerik)} dk okuma</span>
                    </div>
                  </Link>
                ))}
              </div>
            </div>
          )}
        </article>
      </main>
    </div>
  );
};

export default ArticleDetail;

