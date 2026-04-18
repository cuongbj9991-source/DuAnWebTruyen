import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { storyService, favoriteService, commentService, chapterService } from '../services/api';
import { useAuth } from '../context/AuthContext';
import '../styles/StoryDetail.css';

function StoryDetail() {
  const { id } = useParams();
  const { user } = useAuth();
  const [story, setStory] = useState(null);
  const [chapters, setChapters] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isFavorited, setIsFavorited] = useState(false);
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');
  const [newRating, setNewRating] = useState(5);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        
        // Fetch story
        const storyResponse = await storyService.getStoryById(id);
        setStory(storyResponse.data);

        // Fetch chapters
        const chaptersResponse = await chapterService.getChaptersByStoryId(id);
        setChapters(Array.isArray(chaptersResponse.data) ? chaptersResponse.data : chaptersResponse.data.data || []);

        // Fetch comments
        const commentsResponse = await commentService.getComments(id);
        setComments(Array.isArray(commentsResponse.data) ? commentsResponse.data : commentsResponse.data.data || []);

        // Check favorite status if user logged in
        if (user && user.id) {
          try {
            const favResponse = await favoriteService.checkFavorite(id);
            setIsFavorited(favResponse.data.isFavorite);
          } catch (err) {
            // .NET User Service not available - skip favorite check
            console.debug('Favorite service unavailable');
          }
        }
      } catch (error) {
        console.error('Error fetching data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [id, user]);

  const handleAddFavorite = async () => {
    if (!user) {
      alert('Vui lòng đăng nhập để thêm yêu thích');
      return;
    }
    try {
      if (isFavorited) {
        await favoriteService.removeFromFavorites(id);
      } else {
        await favoriteService.addToFavorites(id);
      }
      setIsFavorited(!isFavorited);
    } catch (error) {
      console.error('Error updating favorite:', error);
    }
  };

  const refetchComments = async () => {
    try {
      const response = await commentService.getComments(id);
      setComments(Array.isArray(response.data) ? response.data : response.data.data || []);
    } catch (error) {
      console.error('Error fetching comments:', error);
    }
  };

  const handleCommentSubmit = async (e) => {
    e.preventDefault();
    if (!user) {
      alert('Vui lòng đăng nhập để bình luận');
      return;
    }

    try {
      await commentService.createComment({
        story_id: parseInt(id),
        content: newComment,
        rating: newRating
      });
      setNewComment('');
      setNewRating(5);
      refetchComments();
    } catch (error) {
      console.error('Error posting comment:', error);
    }
  };

  if (loading) return <div className="loading">Đang tải...</div>;
  if (!story) return <div className="error">Không tìm thấy truyện</div>;

  return (
    <div className="story-detail">
      <div className="story-header">
        {story.coverUrl && <img src={story.coverUrl} alt={story.title} className="cover-image" />}
        <div className="story-info">
          <h1>{story.title}</h1>
          {story.titleAlt && <p className="alt-title">({story.titleAlt})</p>}
          <p className="author">Tác giả: {story.author}</p>
          <p className="meta">
            <span className="badge">{story.genre}</span>
            <span className="badge">{story.type}</span>
            <span className="badge">{story.status}</span>
          </p>
          <p className="description">{story.description}</p>
          <div className="stats">
            <span>👁️ {story.viewsTotal} lượt xem</span>
            <span>❤️ {story.likes} lượt thích</span>
            <span>⭐ {story.rating?.toFixed(1) || 0}/5 ({story.ratingCount} đánh giá)</span>
          </div>
          <button 
            className={`btn-favorite ${isFavorited ? 'active' : ''}`}
            onClick={handleAddFavorite}
          >
            {isFavorited ? '❤️ Bỏ yêu thích' : '🤍 Yêu thích'}
          </button>
        </div>
      </div>

      {chapters.length > 0 && (
        <div className="chapters-section">
          <h2>Danh sách chương ({chapters.length})</h2>
          <div className="chapters-list">
            {chapters.map((chapter) => (
              <div key={chapter.id} className="chapter-item">
                <Link to={`/story/${id}/chapter/${chapter.chapterNumber}`}>
                  <span className="chapter-number">Chương {chapter.chapterNumber}</span>
                  <span className="chapter-title">{chapter.title}</span>
                </Link>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="comments-section">
        <h2>💬 Bình luận ({comments.length})</h2>
        
        {user ? (
          <form onSubmit={handleCommentSubmit} className="comment-form">
            <textarea
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              placeholder="Chia sẻ cảm nhận của bạn..."
              required
              minLength={5}
            />
            <div className="form-actions">
              <div className="rating-select">
                <label>Đánh giá:</label>
                <select value={newRating} onChange={(e) => setNewRating(parseInt(e.target.value))}>
                  <option value={1}>⭐ Tệ</option>
                  <option value={2}>⭐⭐ Không tốt</option>
                  <option value={3}>⭐⭐⭐ Bình thường</option>
                  <option value={4}>⭐⭐⭐⭐ Tốt</option>
                  <option value={5}>⭐⭐⭐⭐⭐ Tuyệt vời</option>
                </select>
              </div>
              <button type="submit" className="btn-submit-comment">📤 Đăng bình luận</button>
            </div>
          </form>
        ) : (
          <div className="login-prompt">
            <p>🔐 <Link to="/login" style={{color: '#5b7cfa'}}>Đăng nhập</Link> để bình luận</p>
          </div>
        )}

        <div className="comments-list">
          {comments && comments.length > 0 ? (
            comments.map((comment) => (
              <div key={comment.id} className="comment-item">
                <div className="comment-header">
                  <span className="comment-user">👤 {comment.username || 'Ẩn danh'}</span>
                  <span className="comment-rating">{'⭐'.repeat(comment.rating)}</span>
                  <span className="comment-date">
                    {new Date(comment.createdAt).toLocaleDateString('vi-VN')}
                  </span>
                </div>
                <p className="comment-content">{comment.content}</p>
              </div>
            ))
          ) : (
            <p className="no-comments">Chưa có bình luận nào. Hãy là người đầu tiên!</p>
          )}
        </div>
      </div>
    </div>
  );
}

export default StoryDetail;
