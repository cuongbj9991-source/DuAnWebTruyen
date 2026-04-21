import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { favoriteService, storyService } from '../services/api';
import { useAuth } from '../context/AuthContext';
import '../styles/FavoritesPage.css';

function FavoritesPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [favorites, setFavorites] = useState([]);
  const [stories, setStories] = useState({});
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }

    const fetchFavorites = async () => {
      try {
        setLoading(true);
        const response = await favoriteService.getFavorites();
        const favList = Array.isArray(response.data) ? response.data : response.data.data || [];
        setFavorites(favList);

        // Fetch story details for each favorite
        const storiesMap = {};
        for (const fav of favList) {
          try {
            const storyRes = await storyService.getStoryById(fav.storyId);
            storiesMap[fav.storyId] = storyRes.data;
          } catch (err) {
            console.error(`Error fetching story ${fav.storyId}:`, err);
          }
        }
        setStories(storiesMap);
      } catch (error) {
        console.error('Error fetching favorites:', error);
        setMessage('❌ Lỗi tải yêu thích');
      } finally {
        setLoading(false);
      }
    };

    fetchFavorites();
  }, [user, navigate]);

  const handleRemoveFavorite = async (storyId) => {
    try {
      await favoriteService.removeFromFavorites(storyId);
      setFavorites(prev => prev.filter(f => f.storyId !== storyId));
      setMessage('✅ Đã xóa khỏi yêu thích');
      setTimeout(() => setMessage(''), 3000);
    } catch (error) {
      console.error('Error removing favorite:', error);
      setMessage('❌ Lỗi xóa yêu thích');
    }
  };

  if (loading) return <div className="loading">⏳ Đang tải...</div>;

  return (
    <div className="favorites-page">
      <div className="header">
        <h1>❤️ Yêu Thích Của Tôi</h1>
        <Link to="/" className="back-link">← Quay lại</Link>
      </div>

      {message && (
        <div className={`message ${message.includes('✅') ? 'success' : 'error'}`}>
          {message}
        </div>
      )}

      {favorites.length === 0 ? (
        <div className="empty-state">
          <p>📚 Chưa có truyện yêu thích nào</p>
          <Link to="/" className="btn-explore">Khám phá truyện</Link>
        </div>
      ) : (
        <div className="favorites-grid">
          {favorites.map((favorite) => {
            const story = stories[favorite.storyId];
            if (!story) return null;

            return (
              <div key={favorite.id} className="favorite-card">
                <div className="card-image">
                  <img
                    src={story.coverUrl || 'https://placehold.co/150x200/4F46E5/FFFFFF?text=Story'}
                    alt={story.title}
                  />
                </div>

                <div className="card-content">
                  <h3>
                    <Link to={`/story/${favorite.storyId}`}>{story.title}</Link>
                  </h3>

                  {story.titleAlt && (
                    <p className="alt-title">{story.titleAlt}</p>
                  )}

                  <p className="author">👤 {story.author}</p>

                  <div className="meta">
                    <span className="badge">{story.genre}</span>
                    <span className="badge">{story.status}</span>
                  </div>

                  <div className="stats">
                    <span>👁️ {story.viewsTotal}</span>
                    <span>❤️ {story.likes}</span>
                    <span>⭐ {story.rating?.toFixed(1) || 0}/5</span>
                  </div>

                  <div className="actions">
                    <Link
                      to={`/story/${favorite.storyId}`}
                      className="btn-read"
                    >
                      📖 Đọc
                    </Link>
                    <button
                      className="btn-remove"
                      onClick={() => handleRemoveFavorite(favorite.storyId)}
                    >
                      🗑️ Xóa
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}

export default FavoritesPage;
