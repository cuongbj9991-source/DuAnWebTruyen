import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Home from './pages/Home';
import StoryDetail from './pages/StoryDetail';
import ChapterReader from './pages/ChapterReader';
import ImportManager from './pages/ImportManager';
import FavoritesPage from './pages/FavoritesPage';
import LoginForm from './components/LoginForm';
import RegisterForm from './components/RegisterForm';
import UploadStory from './pages/UploadStory';
import './styles/App.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/story/:id" element={<StoryDetail />} />
          <Route path="/story/:storyId/chapter/:chapterNumber" element={<ChapterReader />} />
          <Route path="/import" element={<ImportManager />} />
          <Route path="/favorites" element={<FavoritesPage />} />
          <Route path="/login" element={<LoginForm />} />
          <Route path="/register" element={<RegisterForm />} />
          <Route path="/upload" element={<UploadStory />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
