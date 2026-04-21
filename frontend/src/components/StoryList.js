import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { storyService } from '../services/api';
import '../styles/StoryList.css';

function StoryList({ filters = {} }) {
  const [stories, setStories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

  const fetchStories = useCallback(async () => {
    try {
      setLoading(true);
      const params = {
        limit: 20,
        offset: currentPage * 20,
        ...filters
      };
      const response = await storyService.getAllStories(params);
      
      // API returns {total, pages, data: [...], currentPage}
      // Axios wraps it in response.data, so response.data = {total, pages, data: [...], currentPage}
      console.log('API Response:', response);
      
      const storiesArray = (response.data && Array.isArray(response.data.data)) 
        ? response.data.data 
        : (Array.isArray(response.data) ? response.data : []);
      
      console.log('Stories Array:', storiesArray);
      console.log('Setting stories, currentPage:', currentPage);
      
      if (currentPage === 0) {
        console.log('Setting stories to:', storiesArray);
        setStories(storiesArray);
      } else {
        setStories(prev => [...prev, ...storiesArray]);
      }
      
      setHasMore(storiesArray.length === 20);
      console.log('Stories state updated');
    } catch (error) {
      console.error('Error fetching stories:', error);
      setStories([]);
    } finally {
      setLoading(false);
    }
  }, [currentPage, filters]);

  useEffect(() => {
    setCurrentPage(0); // Reset to first page when filters change
  }, [filters]);

  useEffect(() => {
    fetchStories();
  }, [fetchStories]);

  const handleLoadMore = () => {
    setCurrentPage(prev => prev + 1);
  };

  const getStatusText = (status) => {
    switch(status) {
      case 'ongoing': return '📖 Còn tiếp';
      case 'completed': return '✅ Hoàn thành';
      case 'paused': return '⏸️ Tạm ngưng';
      default: return status;
    }
  };

  const getTypeText = (type) => {
    switch(type) {
      case 'sáng tác': return '🖊️ Sáng tác';
      case 'dịch': return '🌍 Dịch';
      case 'txt dịch tự động': return '🤖 Txt dịch';
      case 'scan ảnh': return '📸 Scan ảnh';
      default: return type;
    }
  };

  if (loading && currentPage === 0) return <div className="loading">⏳ Đang tải truyện...</div>;

  return (
    <div className="story-list-container">
      {!stories || stories.length === 0 ? (
        <div className="no-stories">
          <p>📭 Không có truyện</p>
        </div>
      ) : (
        <>
          <div className="stories-grid">
            {stories.map((story) => (
              <Link key={story.id} to={`/story/${story.id}`} style={{textDecoration: 'none'}}>
                <div className="story-card">
                  <div className="story-image-container">
                    <img 
                      src={story.coverUrl || 'https://placehold.co/200x300/4F46E5/FFFFFF?text=Story'} 
                      alt={story.title}
                      className="story-cover"
                    />
                    <div className="story-overlay">
                      <button className="read-button">Đọc Ngay</button>
                    </div>
                  </div>

                <div className="story-content">
                  <h3 className="story-title" title={story.title}>{story.title}</h3>
                  
                  {story.titleAlt && (
                    <p className="story-alt-title">{story.titleAlt}</p>
                  )}
                  
                  <p className="story-author">{story.author || 'Tác giả không xác định'}</p>

                  <div className="story-meta">
                    {story.status && (
                      <span className={`badge status-${story.status.toLowerCase()}`}>
                        {getStatusText(story.status.toLowerCase())}
                      </span>
                    )}
                    {story.type && (
                      <span className="badge type-badge">
                        {getTypeText(story.type.toLowerCase())}
                      </span>
                    )}
                  </div>

                  {story.genre && (
                    <p className="story-genre">{story.genre}</p>
                  )}

                  <div className="story-stats">
                    {story.viewsTotal && (
                      <span title="Lượt đọc">👁️ {story.viewsTotal.toLocaleString()}</span>
                    )}
                    {story.likes && (
                      <span title="Thích">❤️ {story.likes}</span>
                    )}
                    {story.rating && story.rating > 0 && (
                      <span title="Đánh giá">⭐ {story.rating.toFixed(1)}</span>
                    )}
                  </div>

                  {story.description && (
                    <p className="story-summary">{story.description.substring(0, 100)}...</p>
                  )}
                </div>
              </div>
              </Link>
            ))}
          </div>

          {hasMore && (
            <div className="load-more-container">
              <button 
                className="load-more-button"
                onClick={handleLoadMore}
                disabled={loading}
              >
                {loading ? 'Đang tải...' : 'Xem Thêm'}
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

export default StoryList;
