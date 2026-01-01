import axios from 'axios';

// Backend URL'i environment variable'dan al, yoksa otomatik tespit et
// Vite'da environment variable'lar VITE_ prefix'i ile başlamalı
const getApiBaseUrl = () => {
  // Environment variable'dan al (öncelikli)
  if (import.meta.env.VITE_API_URL) {
    return import.meta.env.VITE_API_URL;
  }
  
  // Browser'ın çalıştığı host'u kontrol et
  const hostname = window.location.hostname;
  const protocol = window.location.protocol;
  const port = window.location.port;
  
  // Eğer host.docker.internal veya Docker network IP'si ise
  // Browser Jenkins container'ında çalışıyor, backend container'ına erişebilir
  if (hostname === 'host.docker.internal' || hostname.startsWith('172.17.') || hostname.startsWith('172.20.')) {
    // Jenkins container'ından backend container'ına erişim
    // Browser Jenkins container'ında çalışıyor, Docker network'üne erişebilir
    // backend:8080 hostname'i Docker network'ünde çözümlenir
    return 'http://backend:8080/api';
  }
  
  // Varsayılan: localhost (normal development)
  return 'http://localhost:8080/api';
};

const API_BASE_URL = getApiBaseUrl();

// Debug için log
console.log('API Base URL:', API_BASE_URL);

// Axios instance oluştur
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - token ekle
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - 401 hatası durumunda logout
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authAPI = {
  kayitOl: (data) => api.post('/auth/kayit', data),
  girisYap: (data) => api.post('/auth/giris', data),
  cikisYap: () => api.post('/auth/cikis'),
};

// Haber API (Story API)
export const haberAPI = {
  getAll: (params) => api.get('/haberler', { params }),
  getById: (id) => api.get(`/haberler/${id}`),
  getBySlug: (slug) => api.get(`/haberler/slug/${slug}`),
  create: (data) => api.post('/haberler', data),
  update: (id, data) => api.put(`/haberler/${id}`, data),
  delete: (id) => api.delete(`/haberler/${id}`),
  yayinla: (id) => api.post(`/haberler/${id}/yayinla`),
  yayinOnayla: (id) => api.post(`/haberler/${id}/onayla`),
  yayinReddet: (id, sebep) => api.post(`/haberler/${id}/reddet`, null, { params: { sebep } }),
  search: (q, params) => api.get('/haberler/arama', { params: { q, ...params } }),
  getPopular: (params) => api.get('/haberler/populer', { params }),
  getBekleyen: (params) => api.get('/haberler/bekleyen', { params }),
  getByKategori: (kategoriId, params) => api.get(`/haberler/kategori/${kategoriId}`, { params }),
  getByKullanici: (kullaniciId, params) => api.get(`/haberler/kullanici/${kullaniciId}`, { params }),
  getEditorPicks: (params) => api.get('/haberler/editor-secimleri', { params }),
  toggleEditorPick: (id) => api.post(`/haberler/${id}/editor-secimi`),
};

// Story API (alias for haberAPI)
export const storyAPI = haberAPI;

// Kategori API
export const kategoriAPI = {
  getAll: () => api.get('/kategoriler'),
  getById: (id) => api.get(`/kategoriler/${id}`),
  getBySlug: (slug) => api.get(`/kategoriler/slug/${slug}`),
  getAllSayfali: (params) => api.get('/kategoriler/sayfali', { params }),
  create: (data) => api.post('/kategoriler', data),
  update: (id, data) => api.put(`/kategoriler/${id}`, data),
  delete: (id) => api.delete(`/kategoriler/${id}`),
};

// Etiket API
export const etiketAPI = {
  getAll: () => api.get('/etiketler'),
  getById: (id) => api.get(`/etiketler/${id}`),
  getBySlug: (slug) => api.get(`/etiketler/slug/${slug}`),
  getAllSayfali: (params) => api.get('/etiketler/sayfali', { params }),
  create: (data) => api.post('/etiketler', data),
  update: (id, data) => api.put(`/etiketler/${id}`, data),
  delete: (id) => api.delete(`/etiketler/${id}`),
};

// Yorum API
export const yorumAPI = {
  getByHaberId: (haberId) => api.get(`/yorumlar/haber/${haberId}`),
  create: (haberId, data) => api.post(`/yorumlar/haber/${haberId}`, data),
  update: (id, icerik) => api.put(`/yorumlar/${id}`, { body: icerik }),
  delete: (id) => api.delete(`/yorumlar/${id}`),
  getByDurum: (durum, params) => api.get(`/yorumlar/durum/${durum}`, { params }),
  onayla: (id) => api.post(`/yorumlar/${id}/onayla`),
  reddet: (id, sebep) => api.post(`/yorumlar/${id}/reddet`, null, { params: { sebep } }),
  getByYazarId: (yazarId, params) => api.get(`/yorumlar/yazar/${yazarId}`, { params }),
  getByYazarIdAndHaberId: (yazarId, haberId, params) => api.get(`/yorumlar/yazar/${yazarId}/haber/${haberId}`, { params }),
};

// Begeni API
export const begeniAPI = {
  begen: (haberId) => api.post(`/begeniler/haber/${haberId}`),
  begeniyiKaldir: (haberId) => api.delete(`/begeniler/haber/${haberId}`),
  begenildiMi: (haberId) => api.get(`/begeniler/haber/${haberId}/durum`),
  begeniSayisi: (haberId) => api.get(`/begeniler/haber/${haberId}/sayi`),
};

// Kullanıcı API
export const kullaniciAPI = {
  getById: (id) => api.get(`/kullanicilar/${id}`),
  update: (id, data) => api.put(`/kullanicilar/${id}`, data),
  search: (q, params) => api.get('/kullanicilar/arama', { params: { q, ...params } }),
  getAll: (params) => api.get('/kullanicilar', { params }),
  delete: (id) => api.delete(`/kullanicilar/${id}`),
  setActive: (id, aktif) => api.patch(`/kullanicilar/${id}/aktif`, null, { params: { aktif } }),
};

// Yazar Profili API
export const yazarProfiliAPI = {
  getByKullaniciId: (kullaniciId) => api.get(`/yazar-profilleri/kullanici/${kullaniciId}`),
  createOrUpdate: (kullaniciId, data) => api.post(`/yazar-profilleri/kullanici/${kullaniciId}`, data),
  update: (kullaniciId, data) => api.put(`/yazar-profilleri/kullanici/${kullaniciId}`, data),
};

// Kayıtlı Haber API
export const kayitliHaberAPI = {
  getAll: (params) => api.get('/kayitli-haberler', { params }),
  kaydet: (haberId) => api.post(`/kayitli-haberler/haber/${haberId}`),
  kaldir: (haberId) => api.delete(`/kayitli-haberler/haber/${haberId}`),
};

// Bildirim API
export const bildirimAPI = {
  getAll: (params) => api.get('/bildirimler', { params }),
  getOkunmamis: (params) => api.get('/bildirimler/okunmamis', { params }),
  getOkunmamisSayisi: () => api.get('/bildirimler/okunmamis-sayi'),
  okunduIsaretle: (id) => api.patch(`/bildirimler/${id}/okundu`),
  tumunuOkunduIsaretle: () => api.patch('/bildirimler/tumunu-okundu'),
};

// Dosya Yükleme API
export const dosyaAPI = {
  yukle: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/dosyalar/yukle', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
  getById: (id) => api.get(`/dosyalar/${id}`),
  sil: (id) => api.delete(`/dosyalar/${id}`),
};

// Liste API
export const listeAPI = {
  getAll: (params) => api.get('/listeler', { params }),
  getById: (id) => api.get(`/listeler/${id}`),
  getBySlug: (slug) => api.get(`/listeler/slug/${slug}`),
  create: (data) => api.post('/listeler', data),
  update: (id, data) => api.put(`/listeler/${id}`, data),
  delete: (id) => api.delete(`/listeler/${id}`),
  addHaber: (listeId, haberId) => api.post(`/listeler/${listeId}/haber/${haberId}`),
  removeHaber: (listeId, haberId) => api.delete(`/listeler/${listeId}/haber/${haberId}`),
};

// Takip API
export const takipAPI = {
  takipEt: (kullaniciId) => api.post(`/takip/${kullaniciId}`),
  takibiBirak: (kullaniciId) => api.delete(`/takip/${kullaniciId}`),
  takipEdiliyorMu: (kullaniciId) => api.get(`/takip/${kullaniciId}/durum`),
  takipciSayisi: (kullaniciId) => api.get(`/takip/${kullaniciId}/takipci-sayisi`),
  takipEdilenSayisi: (kullaniciId) => api.get(`/takip/${kullaniciId}/takip-edilen-sayisi`),
  getTakipciler: (kullaniciId) => api.get(`/takip/${kullaniciId}/takipciler`),
  getTakipEdilenler: (kullaniciId) => api.get(`/takip/${kullaniciId}/takip-edilenler`),
};

export default api;

