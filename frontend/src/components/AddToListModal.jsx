import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { listeAPI } from '../services/api';
import './AddToListModal.css';

const AddToListModal = ({ storyId, isOpen, onClose, onSuccess, position }) => {
  const navigate = useNavigate();
  const [listeler, setListeler] = useState([]);
  const [loading, setLoading] = useState(false);
  const [creating, setCreating] = useState(false);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [newListName, setNewListName] = useState('');
  const [newListDescription, setNewListDescription] = useState('');
  const [newListPrivate, setNewListPrivate] = useState(false);
  const [storyInLists, setStoryInLists] = useState(new Set());
  const [updatingListId, setUpdatingListId] = useState(null);

  useEffect(() => {
    if (isOpen && storyId) {
      fetchListeler();
      setShowCreateForm(false);
      setNewListName('');
      setNewListDescription('');
      setNewListPrivate(false);
    }
  }, [isOpen, storyId]);

  const fetchListeler = async () => {
    try {
      setLoading(true);
      const response = await listeAPI.getAll({ page: 0, size: 100 });
      setListeler(response.data?.content || []);
    } catch (error) {
      console.error('Listeler yüklenirken hata:', error);
    } finally {
      setLoading(false);
    }
  };

  const checkStoryInLists = async () => {
    if (!storyId || listeler.length === 0) return;
    
    try {
      const inLists = new Set();
      for (const liste of listeler) {
        try {
          const stories = liste.stories || liste.haberler || [];
          const isInList = stories.some(s => (s.id || s.story?.id) === storyId);
          if (isInList) {
            inLists.add(liste.id);
          } else if (liste.slug) {
            // Liste detaylarını çek
            try {
              const detailResponse = await listeAPI.getBySlug(liste.slug);
              const detailStories = detailResponse.data?.stories || detailResponse.data?.haberler || [];
              const isInList = detailStories.some(s => (s.id || s.story?.id) === storyId);
              if (isInList) {
                inLists.add(liste.id);
              }
            } catch (err) {
              // Hata durumunda devam et
            }
          }
        } catch (error) {
          // Hata durumunda devam et
        }
      }
      setStoryInLists(inLists);
    } catch (error) {
      console.error('Liste kontrolü hatası:', error);
    }
  };

  useEffect(() => {
    if (listeler.length > 0 && storyId) {
      checkStoryInLists();
    }
  }, [listeler, storyId]);

  const handleToggleList = async (listeId, isChecked) => {
    try {
      setUpdatingListId(listeId);
      if (isChecked) {
        // Listeye ekle
        await listeAPI.addHaber(listeId, storyId);
        setStoryInLists(prev => new Set(prev).add(listeId));
      } else {
        // Listeden çıkar
        await listeAPI.removeHaber(listeId, storyId);
        setStoryInLists(prev => {
          const newSet = new Set(prev);
          newSet.delete(listeId);
          return newSet;
        });
      }
      if (onSuccess) {
        onSuccess();
      }
    } catch (error) {
      console.error('Liste güncelleme hatası:', error);
      alert('Liste güncellenirken bir hata oluştu: ' + (error.response?.data?.message || error.message));
    } finally {
      setUpdatingListId(null);
    }
  };

  const handleCreateAndAdd = async () => {
    if (!newListName.trim()) {
      alert('Lütfen liste adı girin');
      return;
    }

    try {
      setCreating(true);
      const response = await listeAPI.create({
        name: newListName.trim(),
        description: newListDescription.trim() || null,
        isPrivate: newListPrivate
      });

      // Yeni oluşturulan listeye ekle
      if (response.data?.id) {
        await handleToggleList(response.data.id, true);
      }
    } catch (error) {
      console.error('Liste oluşturulurken hata:', error);
      alert('Liste oluşturulurken bir hata oluştu');
    } finally {
      setCreating(false);
    }
  };

  if (!isOpen) return null;

  const modalStyle = position ? {
    position: 'fixed',
    top: `${position.top}px`,
    left: `${position.left}px`,
    transform: 'none',
    margin: 0
  } : {};

  return (
    <div className="add-to-list-modal-overlay" onClick={onClose}>
      <div 
        className="add-to-list-modal" 
        onClick={(e) => e.stopPropagation()}
        style={modalStyle}
      >
        {!showCreateForm ? (
          <>
            <div className="add-to-list-modal-header-compact">
              <div className="add-to-list-header-title">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" style={{ marginRight: '6px' }}>
                  <path d="M20 6L9 17l-5-5" stroke="#10b981" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" fill="none"/>
                </svg>
                <span>Reading list</span>
                {listeler.some(l => l.isPrivate) && (
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" style={{ marginLeft: '6px' }}>
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" strokeWidth="2" />
                    <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" strokeWidth="2" />
                  </svg>
                )}
              </div>
            </div>

            <div className="add-to-list-modal-body-compact">
              {loading ? (
                <div className="add-to-list-loading">Yükleniyor...</div>
              ) : listeler.length > 0 ? (
                <div className="add-to-list-list-compact">
                  {listeler.map((liste) => {
                    const isChecked = storyInLists.has(liste.id);
                    const isUpdating = updatingListId === liste.id;
                    return (
                      <label
                        key={liste.id}
                        className={`add-to-list-item-compact ${isChecked ? 'checked' : ''}`}
                      >
                        <input
                          type="checkbox"
                          checked={isChecked}
                          onChange={(e) => handleToggleList(liste.id, e.target.checked)}
                          disabled={isUpdating}
                        />
                        <span className="add-to-list-item-name-compact">{liste.name || liste.ad}</span>
                        {liste.isPrivate && (
                          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" style={{ marginLeft: 'auto', marginRight: '8px' }}>
                            <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" strokeWidth="2" />
                            <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" strokeWidth="2" />
                          </svg>
                        )}
                        {isUpdating && (
                          <div className="add-to-list-item-loading-compact">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                              <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2" strokeDasharray="31.416" strokeDashoffset="15.708">
                                <animateTransform attributeName="transform" type="rotate" values="0 12 12;360 12 12" dur="1s" repeatCount="indefinite" />
                              </circle>
                            </svg>
                          </div>
                        )}
                      </label>
                    );
                  })}
                </div>
              ) : null}

              <button
                className="add-to-list-create-button-compact"
                onClick={() => setShowCreateForm(true)}
              >
                Create new list
              </button>
            </div>
          </>
        ) : (
          <div className="add-to-list-create-form">
            <div className="add-to-list-input-group">
              <input
                type="text"
                className="add-to-list-input"
                placeholder="Bir isim verin"
                value={newListName}
                onChange={(e) => {
                  const value = e.target.value;
                  if (value.length <= 60) {
                    setNewListName(value);
                  }
                }}
                autoFocus
              />
              <div className="add-to-list-char-count">
                {newListName.length}/60
              </div>
            </div>

            <div className="add-to-list-description-group">
              <textarea
                className="add-to-list-description"
                placeholder="Açıklama ekleyin (isteğe bağlı)..."
                value={newListDescription}
                onChange={(e) => setNewListDescription(e.target.value)}
                rows="3"
              />
            </div>

            <div className="add-to-list-checkbox-group">
              <label className="add-to-list-checkbox-label">
                <input
                  type="checkbox"
                  checked={newListPrivate}
                  onChange={(e) => setNewListPrivate(e.target.checked)}
                />
                <span>Gizli yap</span>
              </label>
            </div>

            <div className="add-to-list-form-actions">
              <button
                className="add-to-list-cancel-button"
                onClick={() => {
                  setShowCreateForm(false);
                  setNewListName('');
                  setNewListDescription('');
                  setNewListPrivate(false);
                }}
              >
                İptal
              </button>
              <button
                className="add-to-list-create-submit"
                onClick={handleCreateAndAdd}
                disabled={creating || !newListName.trim()}
              >
                {creating ? 'Oluşturuluyor...' : 'Oluştur ve ekle'}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default AddToListModal;

