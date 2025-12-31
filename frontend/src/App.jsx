import { Routes, Route, useLocation } from 'react-router-dom';
import { useState } from 'react';
import { useAuth } from './context/AuthContext';
import ReaderHeader from './components/ReaderHeader';
import ReaderSidebar from './components/ReaderSidebar';
import WriterSidebar from './components/WriterSidebar';
import AdminSidebar from './components/AdminSidebar';
import ProtectedRoute from './components/ProtectedRoute';

// Public Pages
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Membership from './pages/Membership';
import About from './pages/About';

// Role-based Dashboards
import YazarDashboard from './pages/yazar/Dashboard';
import YazarHaberOlustur from './pages/yazar/HaberOlustur';
import EditStory from './pages/yazar/EditStory';
import AdminDashboard from './pages/admin/Dashboard';
import AdminUsers from './pages/admin/Users';
import AdminComments from './pages/admin/Comments';
import EditorPicks from './pages/admin/EditorPicks';
import AdminCategories from './pages/admin/Categories';
import AdminTags from './pages/admin/Tags';
import ReaderDashboard from './pages/reader/Dashboard';
import ReaderLibrary from './pages/reader/Library';
import ReaderProfile from './pages/reader/Profile';
import UserProfile from './pages/reader/UserProfile';
import EditProfile from './pages/reader/EditProfile';
import ReaderSettings from './pages/reader/Settings';
import ReaderNewStory from './pages/reader/NewStory';
import ReaderNotifications from './pages/reader/Notifications';
import ReaderSearch from './pages/reader/Search';
import ReaderStatistics from './pages/reader/Statistics';
import ListDetail from './pages/reader/ListDetail';
import ArticleDetail from './pages/reader/ArticleDetail';
import WriterComments from './pages/yazar/Comments';
import WriterStatistics from './pages/yazar/WriterStatistics';
import WriterProfile from './pages/yazar/WriterProfile';

import './App.css';

function AppContent() {
  const location = useLocation();
  const { user } = useAuth();
  const isReaderPage = location.pathname.startsWith('/reader/');
  const isYazarPage = location.pathname.startsWith('/yazar/');
  const isNewStoryPage = location.pathname === '/reader/new-story';
  const isEditProfilePage = location.pathname === '/reader/profile/edit';
  const isHomePage = location.pathname === '/';
  const isArticlePage = location.pathname.startsWith('/haberler/');
  const isAuthPage = location.pathname === '/login' || location.pathname === '/register';
  const isMembershipPage = location.pathname === '/membership';
  const isAboutPage = location.pathname === '/about';
  const isPublicPage = isHomePage || isAuthPage || isMembershipPage || isAboutPage || isArticlePage;
  const isAdminPage = location.pathname.startsWith('/admin/');
  const [sidebarOpen, setSidebarOpen] = useState(false);

  // Kullanıcının rolünü belirle
  const userRole = user?.roller?.[0] || user?.roles?.[0] || null;

  return (
    <div className="app">
      {isReaderPage && !isNewStoryPage && !isEditProfilePage ? (
        <>
          <ReaderHeader onSidebarToggle={() => setSidebarOpen(!sidebarOpen)} />
          <ReaderSidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(!sidebarOpen)} />
        </>
      ) : isEditProfilePage ? (
        <>
          <ReaderHeader onSidebarToggle={() => setSidebarOpen(!sidebarOpen)} />
          <ReaderSidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(!sidebarOpen)} />
        </>
      ) : isArticlePage ? (
        <>
          <ReaderHeader onSidebarToggle={() => setSidebarOpen(!sidebarOpen)} />
          {userRole === 'ADMIN' ? (
            <AdminSidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(!sidebarOpen)} />
          ) : userRole === 'WRITER' ? (
            <WriterSidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(!sidebarOpen)} />
          ) : (
            <ReaderSidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(!sidebarOpen)} />
          )}
        </>
      ) : isYazarPage ? (
        <>
          <ReaderHeader onSidebarToggle={() => setSidebarOpen(!sidebarOpen)} />
          <WriterSidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(!sidebarOpen)} />
        </>
      ) : isAdminPage ? (
        <>
          <ReaderHeader onSidebarToggle={() => setSidebarOpen(!sidebarOpen)} />
          <AdminSidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(!sidebarOpen)} />
        </>
      ) : null}
      <main className={`main-content ${(isReaderPage || isYazarPage || isAdminPage) ? 'reader-main-wrapper' : ''}`}>
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/membership" element={<Membership />} />
          <Route path="/about" element={<About />} />
          <Route path="/haberler/:slug" element={<ArticleDetail sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />} />

          {/* Protected Routes - Yazar */}
          <Route
            path="/yazar/dashboard"
            element={
              <ProtectedRoute requiredRole="WRITER">
                <YazarDashboard sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/yazar/haber-olustur"
            element={
              <ProtectedRoute requiredRole="WRITER">
                <YazarHaberOlustur />
              </ProtectedRoute>
            }
          />
          <Route
            path="/yazar/haber-duzenle/:id"
            element={
              <ProtectedRoute requiredRole="WRITER">
                <EditStory sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/yazar/hikayelerim"
            element={
              <ProtectedRoute requiredRole="WRITER">
                <YazarDashboard sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/yazar/yorumlar"
            element={
              <ProtectedRoute requiredRole="WRITER">
                <WriterComments sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/yazar/istatistikler"
            element={
              <ProtectedRoute requiredRole="WRITER">
                <WriterStatistics sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/yazar/profil"
            element={
              <ProtectedRoute requiredRole="WRITER">
                <WriterProfile sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />

          {/* Protected Routes - Admin */}
          <Route
            path="/admin/dashboard"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <AdminDashboard sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/editor-secimleri"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <EditorPicks sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/users"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <AdminUsers sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/comments"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <AdminComments sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/kategoriler"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <AdminCategories sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/etiketler"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <AdminTags sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />


          {/* Protected Routes - Reader */}
          <Route
            path="/reader/dashboard"
            element={
              <ProtectedRoute requiredRole="USER">
                <ReaderDashboard sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reader/library"
            element={
              <ProtectedRoute requiredRole="USER">
                <ReaderLibrary sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reader/profile"
            element={
              <ProtectedRoute requiredRole="USER">
                <ReaderProfile sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reader/user/:userId"
            element={
              <ProtectedRoute requiredRole="USER">
                <UserProfile sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reader/profile/edit"
            element={
              <ProtectedRoute requiredRole="USER">
                <EditProfile sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reader/settings"
            element={
              <ProtectedRoute requiredRole="USER">
                <ReaderSettings sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reader/new-story"
            element={
              <ProtectedRoute requiredRole="WRITER">
                <ReaderNewStory sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reader/notifications"
            element={
              <ProtectedRoute requiredRole="USER">
                <ReaderNotifications sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/notifications"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <ReaderNotifications sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/yazar/notifications"
            element={
              <ProtectedRoute requiredRole="WRITER">
                <ReaderNotifications sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reader/search"
            element={
              <ProtectedRoute requiredRole="USER">
                <ReaderSearch sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/search"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <ReaderSearch sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/yazar/search"
            element={
              <ProtectedRoute requiredRole="WRITER">
                <ReaderSearch sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reader/istatistikler"
            element={
              <ProtectedRoute requiredRole="USER">
                <ReaderStatistics sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reader/list/:slug"
            element={
              <ProtectedRoute requiredRole="USER">
                <ListDetail sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} />
              </ProtectedRoute>
            }
          />
        </Routes>
      </main>
    </div>
  );
}

function App() {
  const { loading } = useAuth();

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Yükleniyor...</p>
      </div>
    );
  }

  return <AppContent />;
}

export default App;
