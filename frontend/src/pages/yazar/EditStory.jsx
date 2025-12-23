import { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate, Link, useParams } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { haberAPI, kategoriAPI, dosyaAPI } from '../../services/api';
import mediumLogo from '../../assets/medium-logo-png_seeklogo-347160.png';
import '../reader/NewStory.css';

// Unique ID generator
const generateId = () => Math.random().toString(36).substr(2, 9);

// İçerik string'ini bloklara çevir
const contentToBlocks = (content) => {
  if (!content || !content.trim()) {
    return [{ id: generateId(), type: 'text', content: '' }];
  }

  const blocks = [];
  
  // Önce tüm özel blokları (code, image, video, embed) bul ve çıkar
  // Code block - çok satırlı olabilir, önce bunu handle et
  const codeBlockRegex = /\[CODE:(\w+)\]\n?([\s\S]*?)\n?\[\/CODE\]/g;
  const codeBlocks = [];
  let codeMatch;
  
  // Tüm kod bloklarını bul
  while ((codeMatch = codeBlockRegex.exec(content)) !== null) {
    codeBlocks.push({
      index: codeMatch.index,
      fullMatch: codeMatch[0],
      language: codeMatch[1] || 'javascript',
      content: codeMatch[2] || ''
    });
  }
  
  // Code bloklarını içerikten çıkar ve placeholder ekle
  let processedContent = content;
  const placeholders = [];
  
  // Ters sırada replace yap (index'ler değişmesin diye)
  codeBlocks.sort((a, b) => b.index - a.index).forEach((codeBlock, idx) => {
    const placeholder = `__CODE_BLOCK_${idx}__`;
    placeholders.push({
      placeholder,
      block: {
        id: generateId(),
        type: 'code',
        content: codeBlock.content.trim(),
        language: codeBlock.language
      }
    });
    // Tam eşleşmeyi replace et
    const before = processedContent.substring(0, codeBlock.index);
    const after = processedContent.substring(codeBlock.index + codeBlock.fullMatch.length);
    processedContent = before + placeholder + after;
  });
  
  // Şimdi kalan içeriği parse et
  const lines = processedContent.split('\n\n');
  
  for (let line of lines) {
    line = line.trim();
    if (!line) continue;

    // Placeholder kontrolü - code block
    const codePlaceholder = placeholders.find(p => line === p.placeholder || line.includes(p.placeholder));
    if (codePlaceholder) {
      blocks.push(codePlaceholder.block);
      // Placeholder'ı kaldır
      line = line.replace(codePlaceholder.placeholder, '').trim();
      if (!line) continue;
    }

    // Image
    if (line.startsWith('[IMAGE:')) {
      const url = line.match(/\[IMAGE:(.+?)\]/)?.[1];
      if (url) {
        blocks.push({
          id: generateId(),
          type: 'image',
          url: url,
          caption: ''
        });
      }
      continue;
    }

    // Video
    if (line.startsWith('[VIDEO:')) {
      const url = line.match(/\[VIDEO:(.+?)\]/)?.[1];
      if (url) {
        blocks.push({
          id: generateId(),
          type: 'video',
          url: url
        });
      }
      continue;
    }

    // Embed
    if (line.startsWith('[EMBED:')) {
      const url = line.match(/\[EMBED:(.+?)\]/)?.[1];
      if (url) {
        blocks.push({
          id: generateId(),
          type: 'embed',
          url: url
        });
      }
      continue;
    }

    // Heading
    if (line.startsWith('#')) {
      const match = line.match(/^(#{1,6})\s+(.+)$/);
      if (match) {
        blocks.push({
          id: generateId(),
          type: 'heading',
          content: match[2],
          level: match[1].length
        });
      }
      continue;
    }

    // Quote
    if (line.startsWith('> ')) {
      blocks.push({
        id: generateId(),
        type: 'quote',
        content: line.substring(2)
      });
      continue;
    }

    // Divider
    if (line === '---') {
      blocks.push({
        id: generateId(),
        type: 'divider'
      });
      continue;
    }

    // List
    if (line.match(/^(\d+\.|-)\s+/)) {
      const items = [];
      const listLines = line.split('\n');
      let ordered = false;
      
      for (const listLine of listLines) {
        const itemMatch = listLine.match(/^(\d+\.|-)\s+(.+)$/);
        if (itemMatch) {
          items.push(itemMatch[2]);
          if (itemMatch[1].match(/^\d+\./)) {
            ordered = true;
          }
        }
      }
      
      if (items.length > 0) {
        blocks.push({
          id: generateId(),
          type: 'list',
          items: items,
          ordered: ordered
        });
      }
      continue;
    }

    // Regular text
    blocks.push({
      id: generateId(),
      type: 'text',
      content: line
    });
  }

  return blocks.length > 0 ? blocks : [{ id: generateId(), type: 'text', content: '' }];
};

const EditStory = ({ sidebarOpen, setSidebarOpen }) => {
  const { id } = useParams();
  const { user, hasRole } = useAuth();
  const navigate = useNavigate();
  
  // Kullanıcının rolüne göre dashboard path'ini belirle
  const getDashboardPath = () => {
    if (!user) return '/login';
    if (hasRole('ADMIN')) return '/admin/dashboard';
    if (hasRole('WRITER')) return '/yazar/dashboard';
    return '/reader/dashboard';
  };

  const [baslik, setBaslik] = useState('');
  const [blocks, setBlocks] = useState([
    { id: generateId(), type: 'text', content: '' }
  ]);
  const [activeBlockId, setActiveBlockId] = useState(null);
  const [hoveredBlockId, setHoveredBlockId] = useState(null);
  const [showAddMenu, setShowAddMenu] = useState(false);
  const [addMenuBlockId, setAddMenuBlockId] = useState(null);
  const [kategoriler, setKategoriler] = useState([]);
  const [kategoriId, setKategoriId] = useState('');
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [loadingData, setLoadingData] = useState(true);
  
  // Formatting toolbar state
  const [showFormatToolbar, setShowFormatToolbar] = useState(false);
  const [formatToolbarPosition, setFormatToolbarPosition] = useState({ top: 0, left: 0 });
  const [selectedText, setSelectedText] = useState('');
  const [selectionBlockId, setSelectionBlockId] = useState(null);
  const [selectionRange, setSelectionRange] = useState({ start: 0, end: 0 });
  
  const fileInputRef = useRef(null);
  const blockRefs = useRef({});
  const formatToolbarRef = useRef(null);

  const codeLanguages = [
    'javascript', 'typescript', 'python', 'java', 'c', 'cpp', 'csharp',
    'go', 'rust', 'ruby', 'php', 'swift', 'kotlin', 'sql', 'html', 'css',
    'bash', 'json', 'xml', 'yaml', 'markdown'
  ];

  useEffect(() => {
    fetchKategoriler();
    fetchStory();
  }, [id]);

  const fetchKategoriler = async () => {
    try {
      const response = await kategoriAPI.getAll();
      setKategoriler(response.data);
    } catch (error) {
      console.error('Kategoriler yüklenirken hata:', error);
    }
  };

  const fetchStory = async () => {
    try {
      setLoadingData(true);
      const response = await haberAPI.getById(id);
      const story = response.data;
      
      setBaslik(story.baslik || '');
      setKategoriId(story.kategoriId || '');
      
      // İçeriği bloklara çevir - hem icerik hem content field'larını kontrol et
      const content = story.icerik || story.content || '';
      if (content) {
        const parsedBlocks = contentToBlocks(content);
        setBlocks(parsedBlocks);
      } else {
        setBlocks([{ id: generateId(), type: 'text', content: '' }]);
      }
    } catch (error) {
      console.error('Hikaye yüklenirken hata:', error);
    } finally {
      setLoadingData(false);
    }
  };

  // Handle text selection for formatting toolbar
  useEffect(() => {
    const handleMouseUp = () => {
      setTimeout(() => {
        const selection = window.getSelection();
        const selectedText = selection?.toString()?.trim();
        
        if (selectedText && selectedText.length > 0) {
          const range = selection.getRangeAt(0);
          const rect = range.getBoundingClientRect();
          
          let targetBlockId = null;
          for (const [blockId, element] of Object.entries(blockRefs.current)) {
            if (element && element.contains(range.startContainer)) {
              targetBlockId = blockId;
              break;
            }
          }
          
          if (targetBlockId) {
            const textarea = blockRefs.current[targetBlockId];
            if (textarea) {
              setSelectedText(selectedText);
              setSelectionBlockId(targetBlockId);
              setSelectionRange({
                start: textarea.selectionStart,
                end: textarea.selectionEnd
              });
              
              setFormatToolbarPosition({
                top: rect.top - 50 + window.scrollY,
                left: rect.left + (rect.width / 2)
              });
              setShowFormatToolbar(true);
            }
          }
        } else {
          setShowFormatToolbar(false);
        }
      }, 10);
    };

    const handleMouseDown = (e) => {
      if (formatToolbarRef.current && !formatToolbarRef.current.contains(e.target)) {
        setShowFormatToolbar(false);
      }
    };

    document.addEventListener('mouseup', handleMouseUp);
    document.addEventListener('mousedown', handleMouseDown);

    return () => {
      document.removeEventListener('mouseup', handleMouseUp);
      document.removeEventListener('mousedown', handleMouseDown);
    };
  }, []);

  // Formatting toolbar fonksiyonları (NewStory.jsx'ten kopyalandı)
  const applyFormat = (format) => {
    if (!selectionBlockId) return;
    
    const block = blocks.find(b => b.id === selectionBlockId);
    if (!block || block.type !== 'text') return;
    
    const textarea = blockRefs.current[selectionBlockId];
    if (!textarea) return;
    
    const content = block.content;
    const before = content.substring(0, selectionRange.start);
    const selected = content.substring(selectionRange.start, selectionRange.end);
    const after = content.substring(selectionRange.end);
    
    let formattedText = selected;
    switch (format) {
      case 'bold':
        formattedText = `**${selected}**`;
        break;
      case 'italic':
        formattedText = `*${selected}*`;
        break;
      case 'link':
        const url = prompt('Link URL girin:', 'https://');
        if (url) {
          formattedText = `[${selected}](${url})`;
        } else {
          return;
        }
        break;
      case 'h1':
        formattedText = `# ${selected}`;
        break;
      case 'h2':
        formattedText = `## ${selected}`;
        break;
      case 'quote':
        formattedText = `> ${selected}`;
        break;
      default:
        return;
    }
    
    const newContent = before + formattedText + after;
    updateBlock(selectionBlockId, { content: newContent });
    setShowFormatToolbar(false);
  };

  // Blok ekleme fonksiyonu
  const addBlockAfter = (afterBlockId, newBlock) => {
    setBlocks(prevBlocks => {
      const index = prevBlocks.findIndex(b => b.id === afterBlockId);
      const newBlocks = [...prevBlocks];
      newBlocks.splice(index + 1, 0, newBlock);
      return newBlocks;
    });
    setShowAddMenu(false);
    return newBlock.id;
  };

  // Blok silme fonksiyonu
  const deleteBlock = (blockId) => {
    setBlocks(prevBlocks => {
      if (prevBlocks.length <= 1) {
        return [{ id: generateId(), type: 'text', content: '' }];
      }
      return prevBlocks.filter(b => b.id !== blockId);
    });
  };

  // Blok güncelleme fonksiyonu
  const updateBlock = (blockId, updates) => {
    setBlocks(prevBlocks =>
      prevBlocks.map(b => b.id === blockId ? { ...b, ...updates } : b)
    );
  };

  // Text bloğu için Enter tuşu işleme
  const handleTextKeyDown = (e, block) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      const textarea = e.target;
      const cursorPos = textarea.selectionStart;
      const content = block.content || '';
      
      const beforeCursor = content.substring(0, cursorPos);
      const afterCursor = content.substring(cursorPos);
      
      updateBlock(block.id, { content: beforeCursor });
      
      const newBlockId = generateId();
      const newBlock = { id: newBlockId, type: 'text', content: afterCursor };
      addBlockAfter(block.id, newBlock);
      
      setTimeout(() => {
        const newTextarea = blockRefs.current[newBlockId];
        if (newTextarea) {
          newTextarea.focus();
          newTextarea.setSelectionRange(0, 0);
        }
      }, 10);
    } else if (e.key === 'Backspace' && block.content === '') {
      e.preventDefault();
      const index = blocks.findIndex(b => b.id === block.id);
      if (index > 0) {
        const prevBlock = blocks[index - 1];
        if (prevBlock.type === 'text') {
          const prevTextarea = blockRefs.current[prevBlock.id];
          if (prevTextarea) {
            prevTextarea.focus();
            prevTextarea.setSelectionRange(prevBlock.content.length, prevBlock.content.length);
          }
        }
        deleteBlock(block.id);
      }
    }
  };

  // Add menüsünü aç
  const handleShowAddMenu = (blockId) => {
    if (showAddMenu && addMenuBlockId === blockId) {
      setShowAddMenu(false);
      setAddMenuBlockId(null);
    } else {
      setShowAddMenu(true);
      setAddMenuBlockId(blockId);
    }
  };

  // İçerik ekleme
  const handleAddContent = (type, blockId) => {
    const newBlockId = generateId();
    let newBlock;

    switch (type) {
      case 'image':
        setAddMenuBlockId(blockId);
        fileInputRef.current?.click();
        return;
      case 'code':
        newBlock = { 
          id: newBlockId, 
          type: 'code-editing', 
          content: '', 
          language: 'javascript' 
        };
        break;
      case 'heading':
        newBlock = { id: newBlockId, type: 'heading', content: '', level: 2 };
        break;
      case 'subheading':
        newBlock = { id: newBlockId, type: 'heading', content: '', level: 3 };
        break;
      case 'quote':
        newBlock = { id: newBlockId, type: 'quote', content: '' };
        break;
      case 'divider':
        newBlock = { id: newBlockId, type: 'divider' };
        break;
      case 'list':
        newBlock = { id: newBlockId, type: 'list', items: [''], ordered: false };
        break;
      case 'ordered-list':
        newBlock = { id: newBlockId, type: 'list', items: [''], ordered: true };
        break;
      case 'video':
        const videoUrl = prompt('Video URL girin (YouTube, Vimeo vb.):', 'https://');
        if (videoUrl && videoUrl !== 'https://') {
          newBlock = { id: newBlockId, type: 'video', url: videoUrl };
        } else {
          setShowAddMenu(false);
          return;
        }
        break;
      case 'embed':
        const embedUrl = prompt('Embed URL girin:', 'https://');
        if (embedUrl && embedUrl !== 'https://') {
          newBlock = { id: newBlockId, type: 'embed', url: embedUrl };
        } else {
          setShowAddMenu(false);
          return;
        }
        break;
      default:
        newBlock = { id: newBlockId, type: 'text', content: '' };
    }

    addBlockAfter(blockId, newBlock);
    setShowAddMenu(false);
    
    setTimeout(() => {
      const newElement = blockRefs.current[newBlockId];
      if (newElement) {
        newElement.focus();
      }
    }, 10);
  };

  // Kod bloğu onaylama
  const handleCodeBlockConfirm = (blockId) => {
    const block = blocks.find(b => b.id === blockId);
    if (!block) return;
    
    if (!block.content?.trim()) {
      deleteBlock(blockId);
      return;
    }

    updateBlock(blockId, { type: 'code' });
    
    const textBlockId = generateId();
    const textBlock = { id: textBlockId, type: 'text', content: '' };
    
    setTimeout(() => {
      addBlockAfter(blockId, textBlock);
      setTimeout(() => {
        const textElement = blockRefs.current[textBlockId];
        if (textElement) {
          textElement.focus();
        }
      }, 10);
    }, 10);
  };

  // Kod bloğu iptal
  const handleCodeBlockCancel = (blockId) => {
    deleteBlock(blockId);
  };

  // Resim yükleme
  const handleImageUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    try {
      setLoading(true);
      const formData = new FormData();
      formData.append('file', file);
      const response = await dosyaAPI.yukle(formData);
      const imageUrl = response.data.url || response.data.dosyaUrl;

      if (addMenuBlockId) {
        // Eğer mevcut bir resim bloğunu güncelliyorsak
        const existingBlock = blocks.find(b => b.id === addMenuBlockId);
        if (existingBlock && existingBlock.type === 'image') {
          updateBlock(addMenuBlockId, { url: imageUrl });
        } else {
          // Yeni resim bloğu ekleme
          const newBlockId = generateId();
          const newBlock = {
            id: newBlockId,
            type: 'image',
            url: imageUrl,
            caption: ''
          };
          addBlockAfter(addMenuBlockId, newBlock);
        }
      }
    } catch (error) {
      console.error('Resim yüklenirken hata:', error);
      alert('Resim yüklenirken bir hata oluştu');
    } finally {
      setLoading(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  // Liste öğesi için Enter tuşu
  const handleListItemKeyDown = (e, block, itemIndex) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      const items = [...block.items];
      items.splice(itemIndex + 1, 0, '');
      updateBlock(block.id, { items });
      
      setTimeout(() => {
        const listElement = blockRefs.current[block.id];
        if (listElement) {
          const inputs = listElement.querySelectorAll('input');
          if (inputs[itemIndex + 1]) {
            inputs[itemIndex + 1].focus();
          }
        }
      }, 10);
    } else if (e.key === 'Backspace' && block.items[itemIndex] === '') {
      e.preventDefault();
      if (block.items.length > 1) {
        const items = block.items.filter((_, i) => i !== itemIndex);
        updateBlock(block.id, { items });
        
        setTimeout(() => {
          const listElement = blockRefs.current[block.id];
          if (listElement) {
            const inputs = listElement.querySelectorAll('input');
            const targetIndex = Math.max(0, itemIndex - 1);
            if (inputs[targetIndex]) {
              inputs[targetIndex].focus();
            }
          }
        }, 10);
      } else {
        updateBlock(block.id, { type: 'text', content: '', items: undefined, ordered: undefined });
      }
    }
  };

  // Blokları içerik string'ine dönüştür
  const blocksToContent = () => {
    return blocks
      .filter(block => block.type !== 'code-editing')
      .map(block => {
        switch (block.type) {
          case 'text':
            return block.content;
          case 'heading':
            const prefix = '#'.repeat(block.level || 2);
            return `${prefix} ${block.content}`;
          case 'code':
            return `[CODE:${block.language}]\n${block.content}\n[/CODE]`;
          case 'image':
            return `[IMAGE:${block.url}]`;
          case 'video':
            return `[VIDEO:${block.url}]`;
          case 'embed':
            return `[EMBED:${block.url}]`;
          case 'quote':
            return `> ${block.content}`;
          case 'divider':
            return '---';
          case 'list':
            return block.items.map((item, i) => 
              block.ordered ? `${i + 1}. ${item}` : `- ${item}`
            ).join('\n');
          default:
            return '';
        }
      }).join('\n\n');
  };

  // Kaydet
  const handleSave = async () => {
    if (!baslik.trim()) {
      alert('Lütfen bir başlık girin');
      return;
    }

    const content = blocksToContent();
    if (!content.trim()) {
      alert('Lütfen içerik girin');
      return;
    }

    try {
      setSaving(true);

      await haberAPI.update(id, {
        baslik: baslik.trim(),
        icerik: content,
        ozet: content.substring(0, 200).replace(/\[.*?\]/g, '').trim(),
        kategoriId: kategoriId ? parseInt(kategoriId) : null,
        kapakResmiUrl: null,
        metaDescription: baslik.trim(),
      });

      navigate('/yazar/dashboard');
    } catch (error) {
      console.error('Güncellenirken hata:', error);
      alert('Güncellenirken bir hata oluştu');
    } finally {
      setSaving(false);
    }
  };

  // Yayınla
  const handlePublish = async () => {
    if (!baslik.trim()) {
      alert('Lütfen bir başlık girin');
      return;
    }

    const content = blocksToContent();
    if (!content.trim()) {
      alert('Lütfen içerik girin');
      return;
    }

    try {
      setSaving(true);

      await haberAPI.update(id, {
        baslik: baslik.trim(),
        icerik: content,
        ozet: content.substring(0, 200).replace(/\[.*?\]/g, '').trim(),
        kategoriId: kategoriId ? parseInt(kategoriId) : null,
        kapakResmiUrl: null,
        metaDescription: baslik.trim(),
      });

      await haberAPI.yayinla(id);

      const storyResponse = await haberAPI.getById(id);
      const slug = storyResponse.data.slug;
      navigate(`/haberler/${slug}`);
    } catch (error) {
      console.error('Yayınlanırken hata:', error);
      alert('Yayınlanırken bir hata oluştu');
    } finally {
      setSaving(false);
    }
  };

  // Sil
  const handleDelete = async () => {
    if (!window.confirm('Bu hikayeyi silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.')) {
      return;
    }

    try {
      setSaving(true);
      await haberAPI.delete(id);
      navigate(getDashboardPath());
    } catch (error) {
      console.error('Silinirken hata:', error);
      alert('Silinirken bir hata oluştu');
    } finally {
      setSaving(false);
    }
  };

  // Blok render fonksiyonu (NewStory.jsx ile aynı)
  const renderBlock = (block, index) => {
    const isHovered = hoveredBlockId === block.id;
    const isMenuOpen = showAddMenu && addMenuBlockId === block.id;
    const showPlusButton = block.type === 'text' && block.content === '' && (isHovered || isMenuOpen);

    switch (block.type) {
      case 'text':
        return (
          <div 
            key={block.id} 
            className="editor-block text-block"
            onMouseEnter={() => setHoveredBlockId(block.id)}
            onMouseLeave={() => !isMenuOpen && setHoveredBlockId(null)}
          >
            <button
              className={`block-add-button ${showPlusButton ? 'visible' : ''} ${isMenuOpen ? 'active' : ''}`}
              onClick={() => handleShowAddMenu(block.id)}
            >
              {isMenuOpen ? (
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                  <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                </svg>
              ) : (
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <line x1="12" y1="5" x2="12" y2="19" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                  <line x1="5" y1="12" x2="19" y2="12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                </svg>
              )}
            </button>
            {isMenuOpen && (
              <div className="block-add-menu">
                <button onClick={() => handleAddContent('image', block.id)} title="Resim">
                  <svg viewBox="0 0 24 24" fill="none">
                    <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" strokeWidth="1.5"/>
                    <circle cx="8.5" cy="8.5" r="1.5" fill="currentColor"/>
                    <path d="M21 15l-5-5L5 21" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
                </button>
                <button onClick={() => handleAddContent('heading', block.id)} title="Başlık">
                  <svg viewBox="0 0 24 24" fill="none">
                    <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" strokeWidth="1.5"/>
                    <path d="M8 17V7M16 17V7M8 12h8" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
                  </svg>
                </button>
                <button onClick={() => handleAddContent('video', block.id)} title="Video">
                  <svg viewBox="0 0 24 24" fill="none">
                    <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" strokeWidth="1.5"/>
                    <path d="M10 8l6 4-6 4V8z" fill="currentColor"/>
                  </svg>
                </button>
                <button onClick={() => handleAddContent('code', block.id)} title="Kod">
                  <svg viewBox="0 0 24 24" fill="none">
                    <polyline points="16 18 22 12 16 6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                    <polyline points="8 6 2 12 8 18" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
                </button>
                <button onClick={() => handleAddContent('embed', block.id)} title="Gömülü İçerik">
                  <svg viewBox="0 0 24 24" fill="none">
                    <path d="M7 7h10v10H7V7z" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
                    <path d="M7 3v4M17 3v4M3 7h4M17 7h4M3 17h4M17 17h4M7 17v4M17 17v4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
                  </svg>
                </button>
                <button onClick={() => handleAddContent('list', block.id)} title="Liste">
                  <svg viewBox="0 0 24 24" fill="none">
                    <line x1="9" y1="6" x2="20" y2="6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
                    <line x1="9" y1="12" x2="20" y2="12" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
                    <line x1="9" y1="18" x2="20" y2="18" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
                    <circle cx="5" cy="6" r="1.5" fill="currentColor"/>
                    <circle cx="5" cy="12" r="1.5" fill="currentColor"/>
                    <circle cx="5" cy="18" r="1.5" fill="currentColor"/>
                  </svg>
                </button>
              </div>
            )}
            <textarea
              ref={el => blockRefs.current[block.id] = el}
              className="block-textarea"
              value={block.content}
              onChange={(e) => updateBlock(block.id, { content: e.target.value })}
              onKeyDown={(e) => handleTextKeyDown(e, block)}
              onFocus={() => setActiveBlockId(block.id)}
              placeholder={index === 0 && blocks.length === 1 ? "Hikayenizi anlatın..." : ""}
              rows={1}
            />
          </div>
        );

      case 'heading':
        return (
          <div key={block.id} className="editor-block heading-block">
            <input
              ref={el => blockRefs.current[block.id] = el}
              type="text"
              className={`block-heading block-heading-${block.level || 2}`}
              value={block.content}
              onChange={(e) => updateBlock(block.id, { content: e.target.value })}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  e.preventDefault();
                  const newBlockId = generateId();
                  addBlockAfter(block.id, { id: newBlockId, type: 'text', content: '' });
                  setTimeout(() => {
                    const el = blockRefs.current[newBlockId];
                    if (el) el.focus();
                  }, 10);
                } else if (e.key === 'Backspace' && block.content === '') {
                  e.preventDefault();
                  deleteBlock(block.id);
                }
              }}
              onFocus={() => setActiveBlockId(block.id)}
              placeholder="Başlık..."
            />
          </div>
        );

      case 'code-editing':
        return (
          <div key={block.id} className="editor-block code-editing-block">
            <div className="code-editor-inline">
              <div className="code-editor-inline-header">
                <select
                  value={block.language}
                  onChange={(e) => updateBlock(block.id, { language: e.target.value })}
                  className="code-language-select-inline"
                >
                  {codeLanguages.map(lang => (
                    <option key={lang} value={lang}>{lang.toUpperCase()}</option>
                  ))}
                </select>
                <div className="code-editor-inline-actions">
                  <button 
                    className="code-editor-btn cancel"
                    onClick={() => handleCodeBlockCancel(block.id)}
                    title="İptal"
                  >
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                      <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" strokeWidth="2"/>
                      <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" strokeWidth="2"/>
                    </svg>
                  </button>
                  <button 
                    className="code-editor-btn confirm"
                    onClick={() => handleCodeBlockConfirm(block.id)}
                    title="Onayla"
                    disabled={!block.content?.trim()}
                  >
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                      <polyline points="20 6 9 17 4 12" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                  </button>
                </div>
              </div>
              <textarea
                ref={el => blockRefs.current[block.id] = el}
                className="code-editor-inline-textarea"
                value={block.content}
                onChange={(e) => updateBlock(block.id, { content: e.target.value })}
                onKeyDown={(e) => {
                  if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
                    e.preventDefault();
                    handleCodeBlockConfirm(block.id);
                  }
                  if (e.key === 'Escape') {
                    e.preventDefault();
                    handleCodeBlockCancel(block.id);
                  }
                }}
                placeholder="Kodunuzu buraya yazın... (Ctrl+Enter ile onayla, Escape ile iptal)"
                autoFocus
              />
            </div>
          </div>
        );

      case 'code':
        return (
          <div key={block.id} className="editor-block code-block-container">
            <div className="code-block-display">
              <div className="code-block-header">
                <span className="code-language-badge">{block.language?.toUpperCase() || 'CODE'}</span>
                <div className="code-block-actions">
                  <button 
                    className="code-block-edit-btn"
                    onClick={() => updateBlock(block.id, { type: 'code-editing' })}
                    title="Düzenle"
                  >
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                  </button>
                  <button 
                    className="code-block-edit-btn"
                    onClick={() => deleteBlock(block.id)}
                    title="Kaldır"
                  >
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                      <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                  </button>
                </div>
              </div>
              <pre className="code-block-content">
                <code>{block.content}</code>
              </pre>
            </div>
          </div>
        );

      case 'image':
        return (
          <div key={block.id} className="editor-block image-block-container">
            <div className="media-block-wrapper">
              <div className="media-block-actions">
                <button 
                  className="media-block-btn"
                  onClick={() => {
                    setAddMenuBlockId(block.id);
                    fileInputRef.current?.click();
                  }}
                  title="Resmi Değiştir"
                >
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
                </button>
                <button 
                  className="media-block-btn"
                  onClick={() => deleteBlock(block.id)}
                  title="Kaldır"
                >
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                    <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
                </button>
              </div>
              <img src={block.url} alt="" className="block-image" />
            </div>
            <input
              type="text"
              className="image-caption"
              value={block.caption || ''}
              onChange={(e) => updateBlock(block.id, { caption: e.target.value })}
              placeholder="Açıklama ekle (isteğe bağlı)"
            />
          </div>
        );

      case 'video':
        return (
          <div key={block.id} className="editor-block video-block-container">
            <div className="media-block-wrapper">
              <div className="media-block-actions">
                <button 
                  className="media-block-btn"
                  onClick={() => {
                    const newUrl = prompt('Video URL\'sini girin:', block.url);
                    if (newUrl && newUrl.trim() !== '') {
                      updateBlock(block.id, { url: newUrl });
                    }
                  }}
                  title="Video URL'sini Değiştir"
                >
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
                </button>
                <button 
                  className="media-block-btn"
                  onClick={() => deleteBlock(block.id)}
                  title="Kaldır"
                >
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                    <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
                </button>
              </div>
              <div className="video-embed">
                {block.url?.includes('youtube') || block.url?.includes('youtu.be') ? (
                  <iframe
                    src={block.url.replace('watch?v=', 'embed/').replace('youtu.be/', 'youtube.com/embed/')}
                    title="Video"
                    frameBorder="0"
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                    allowFullScreen
                  />
                ) : block.url?.includes('vimeo') ? (
                  <iframe
                    src={block.url.replace('vimeo.com/', 'player.vimeo.com/video/')}
                    title="Video"
                    frameBorder="0"
                    allow="autoplay; fullscreen; picture-in-picture"
                    allowFullScreen
                  />
                ) : (
                  <video src={block.url} controls className="video-player" />
                )}
              </div>
            </div>
          </div>
        );

      case 'embed':
        return (
          <div key={block.id} className="editor-block embed-block-container">
            <div className="media-block-wrapper">
              <div className="media-block-actions">
                <button 
                  className="media-block-btn"
                  onClick={() => {
                    const newUrl = prompt('Embed URL\'sini girin:', block.url);
                    if (newUrl && newUrl.trim() !== '') {
                      updateBlock(block.id, { url: newUrl });
                    }
                  }}
                  title="Embed URL'sini Değiştir"
                >
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
                </button>
                <button 
                  className="media-block-btn"
                  onClick={() => deleteBlock(block.id)}
                  title="Kaldır"
                >
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                    <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
                </button>
              </div>
              <div className="embed-preview">
                <iframe
                  src={block.url}
                  title="Embed"
                  frameBorder="0"
                  sandbox="allow-scripts allow-same-origin"
                />
              </div>
            </div>
          </div>
        );

      case 'quote':
        return (
          <div key={block.id} className="editor-block quote-block">
            <textarea
              ref={el => blockRefs.current[block.id] = el}
              className="block-quote"
              value={block.content}
              onChange={(e) => updateBlock(block.id, { content: e.target.value })}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault();
                  const newBlockId = generateId();
                  addBlockAfter(block.id, { id: newBlockId, type: 'text', content: '' });
                  setTimeout(() => {
                    const el = blockRefs.current[newBlockId];
                    if (el) el.focus();
                  }, 10);
                } else if (e.key === 'Backspace' && block.content === '') {
                  e.preventDefault();
                  deleteBlock(block.id);
                }
              }}
              onFocus={() => setActiveBlockId(block.id)}
              placeholder="Alıntı yazın..."
              rows={1}
            />
          </div>
        );

      case 'divider':
        return (
          <div key={block.id} className="editor-block divider-block">
            <hr className="block-divider" />
          </div>
        );

      case 'list':
        return (
          <div key={block.id} className="editor-block list-block" ref={el => blockRefs.current[block.id] = el}>
            {block.ordered ? (
              <ol className="block-list ordered">
                {block.items.map((item, itemIndex) => (
                  <li key={itemIndex}>
                    <input
                      type="text"
                      value={item}
                      onChange={(e) => {
                        const items = [...block.items];
                        items[itemIndex] = e.target.value;
                        updateBlock(block.id, { items });
                      }}
                      onKeyDown={(e) => handleListItemKeyDown(e, block, itemIndex)}
                      placeholder="Liste öğesi..."
                    />
                  </li>
                ))}
              </ol>
            ) : (
              <ul className="block-list">
                {block.items.map((item, itemIndex) => (
                  <li key={itemIndex}>
                    <input
                      type="text"
                      value={item}
                      onChange={(e) => {
                        const items = [...block.items];
                        items[itemIndex] = e.target.value;
                        updateBlock(block.id, { items });
                      }}
                      onKeyDown={(e) => handleListItemKeyDown(e, block, itemIndex)}
                      placeholder="Liste öğesi..."
                    />
                  </li>
                ))}
              </ul>
            )}
          </div>
        );

      default:
        return null;
    }
  };

  if (loadingData) {
    return (
      <div className="loading-overlay">
        <div className="loading-spinner"></div>
      </div>
    );
  }

  return (
    <div className={`new-story-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
      {/* Header */}
      <header className="story-header">
        <div className="story-header-content">
          <Link to={getDashboardPath()} className="story-logo">
            <img src={mediumLogo} alt="Medium" className="medium-logo-img" />
          </Link>
          
          <div className="story-header-info">
            <span className="draft-info">Taslak</span>
          </div>
          
          <div className="story-header-actions">
            <button 
              className="publish-button" 
              onClick={handlePublish}
              disabled={saving || !baslik.trim()}
            >
              {saving ? 'Yayınlanıyor...' : 'Yayınla'}
            </button>
            <button 
              className="publish-button delete-button" 
              onClick={handleDelete}
              disabled={saving}
            >
              {saving ? 'Siliniyor...' : 'Sil'}
            </button>
            <button className="story-menu-button">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="5" r="1.5" fill="currentColor"/>
                <circle cx="12" cy="12" r="1.5" fill="currentColor"/>
                <circle cx="12" cy="19" r="1.5" fill="currentColor"/>
              </svg>
            </button>
          </div>
        </div>
      </header>

      {/* Formatting Toolbar */}
      {showFormatToolbar && (
        <div 
          ref={formatToolbarRef}
          className="format-toolbar"
          style={{
            top: formatToolbarPosition.top,
            left: formatToolbarPosition.left
          }}
        >
          <button onClick={() => applyFormat('bold')} title="Kalın (Bold)">
            <strong>B</strong>
          </button>
          <button onClick={() => applyFormat('italic')} title="İtalik">
            <em>i</em>
          </button>
          <button onClick={() => applyFormat('link')} title="Link">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
              <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
            </svg>
          </button>
          <div className="toolbar-divider"></div>
          <button onClick={() => applyFormat('h1')} title="Büyük Başlık">
            <strong>T</strong>
          </button>
          <button onClick={() => applyFormat('h2')} title="Küçük Başlık">
            <span style={{ fontSize: '12px' }}>T</span>
          </button>
          <button onClick={() => applyFormat('quote')} title="Alıntı">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M3 21c3 0 7-1 7-8V5c0-1.25-.756-2.017-2-2H4c-1.25 0-2 .75-2 1.972V11c0 1.25.75 2 2 2 1 0 1 0 1 1v1c0 1-1 2-2 2s-1 .008-1 1.031V21" stroke="currentColor" strokeWidth="2"/>
            </svg>
          </button>
        </div>
      )}

      {/* Editor */}
      <div className="story-editor">
        {/* Title */}
        <input
          type="text"
          className="story-title-input"
          value={baslik}
          onChange={(e) => setBaslik(e.target.value)}
          placeholder="Başlık"
        />

        {/* Blocks */}
        <div className="editor-blocks">
          {blocks.map((block, index) => renderBlock(block, index))}
        </div>
      </div>

      {/* Hidden file input */}
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        style={{ display: 'none' }}
        onChange={handleImageUpload}
      />

      {loading && (
        <div className="loading-overlay">
          <div className="loading-spinner"></div>
        </div>
      )}
    </div>
  );
};

export default EditStory;

