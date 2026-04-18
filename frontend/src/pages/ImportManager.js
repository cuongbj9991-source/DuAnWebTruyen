import React, { useState, useEffect } from 'react';
import { storyService } from '../services/api';
import '../styles/ImportManager.css';

function ImportManager() {
  const [gutenbergKeyword, setGutenbergKeyword] = useState('story');
  const [gutenbergLimit, setGutenbergLimit] = useState(50);
  const [mangaDexKeyword, setMangaDexKeyword] = useState('action');
  const [mangaDexLimit, setMangaDexLimit] = useState(50);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState(''); // 'success', 'error', 'info'

  useEffect(() => {
    fetchStats();
    const interval = setInterval(fetchStats, 5000); // Refresh every 5 seconds
    return () => clearInterval(interval);
  }, []);

  const fetchStats = async () => {
    try {
      const baseURL = process.env.REACT_APP_STORY_SERVICE || 'http://localhost:8081/api';
      const response = await fetch(`${baseURL}/import/stats`);
      const data = await response.json();
      setStats(data);
    } catch (error) {
      console.error('Error fetching stats:', error);
    }
  };

  const handleImportGutenberg = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const baseURL = process.env.REACT_APP_STORY_SERVICE || 'http://localhost:8081/api';
      const response = await fetch(
        `${baseURL}/import/gutenberg?keyword=${gutenbergKeyword}&limit=${gutenbergLimit}`
      );
      const data = await response.json();
      
      setMessage(`📚 Bắt đầu import từ Gutenberg: "${gutenbergKeyword}" (${gutenbergLimit} cuốn)`);
      setMessageType('info');
      
      // Refresh stats after a delay
      setTimeout(fetchStats, 2000);
    } catch (error) {
      setMessage(`❌ Lỗi: ${error.message}`);
      setMessageType('error');
    } finally {
      setLoading(false);
    }
  };

  const handleImportMangaDex = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const baseURL = process.env.REACT_APP_STORY_SERVICE || 'http://localhost:8081/api';
      const response = await fetch(
        `${baseURL}/import/mangadex?keyword=${mangaDexKeyword}&limit=${mangaDexLimit}`
      );
      const data = await response.json();
      
      setMessage(`🎨 Bắt đầu import từ MangaDex: "${mangaDexKeyword}" (${mangaDexLimit} truyện)`);
      setMessageType('info');
      
      // Refresh stats after a delay
      setTimeout(fetchStats, 2000);
    } catch (error) {
      setMessage(`❌ Lỗi: ${error.message}`);
      setMessageType('error');
    } finally {
      setLoading(false);
    }
  };

  const handleClearSource = async (source) => {
    if (!window.confirm(`Xóa tất cả truyện từ ${source}? Hành động này không thể hoàn tác!`)) {
      return;
    }
    
    setLoading(true);
    try {
      const baseURL = process.env.REACT_APP_STORY_SERVICE || 'http://localhost:8081/api';
      const response = await fetch(
        `${baseURL}/import/clear?source=${source}`,
        { method: 'POST' }
      );
      const data = await response.json();
      
      setMessage(`🗑️ Đã xóa truyện từ ${source}`);
      setMessageType('success');
      
      fetchStats();
    } catch (error) {
      setMessage(`❌ Lỗi: ${error.message}`);
      setMessageType('error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="import-manager">
      <h1>📥 Quản lý Import Truyện</h1>
      
      {/* Statistics */}
      <div className="stats-section">
        <h2>📊 Thống kê</h2>
        {stats ? (
          <div className="stats-grid">
            <div className="stat-card gutenberg">
              <h3>📚 Project Gutenberg</h3>
              <p className="stat-number">{stats.gutenberg}</p>
              <p className="stat-label">cuốn sách</p>
            </div>
            <div className="stat-card mangadex">
              <h3>🎨 MangaDex</h3>
              <p className="stat-number">{stats.mangaDex}</p>
              <p className="stat-label">truyện tranh</p>
            </div>
            <div className="stat-card openlibrary">
              <h3>📖 OpenLibrary</h3>
              <p className="stat-number">{stats.openLibrary}</p>
              <p className="stat-label">tài liệu</p>
            </div>
            <div className="stat-card total">
              <h3>✨ Tổng cộng</h3>
              <p className="stat-number">{stats.total}</p>
              <p className="stat-label">truyện</p>
            </div>
          </div>
        ) : (
          <p>⏳ Đang tải...</p>
        )}
      </div>

      {/* Message */}
      {message && (
        <div className={`message message-${messageType}`}>
          {message}
        </div>
      )}

      {/* Import Forms */}
      <div className="import-forms">
        {/* Gutenberg Import */}
        <div className="form-section">
          <h2>📚 Import từ Project Gutenberg</h2>
          <p className="description">
            Project Gutenberg có hơn 70,000 cuốn sách điện tử miễn phí.
          </p>
          
          <form onSubmit={handleImportGutenberg}>
            <div className="form-group">
              <label htmlFor="gutenberg-keyword">Từ khóa tìm kiếm:</label>
              <input
                id="gutenberg-keyword"
                type="text"
                value={gutenbergKeyword}
                onChange={(e) => setGutenbergKeyword(e.target.value)}
                placeholder="e.g., pride, science, mystery"
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label htmlFor="gutenberg-limit">Số lượng tối đa:</label>
              <input
                id="gutenberg-limit"
                type="number"
                value={gutenbergLimit}
                onChange={(e) => setGutenbergLimit(e.target.value ? parseInt(e.target.value) : 50)}
                min="1"
                max="500"
                disabled={loading}
              />
            </div>

            <button 
              type="submit" 
              className="btn-primary"
              disabled={loading}
            >
              {loading ? '⏳ Đang import...' : '🚀 Bắt đầu Import'}
            </button>
          </form>

          <button 
            className="btn-danger"
            onClick={() => handleClearSource('Gutenberg')}
            disabled={loading || stats?.gutenberg === 0}
          >
            🗑️ Xóa tất cả Gutenberg
          </button>
        </div>

        {/* MangaDex Import */}
        <div className="form-section">
          <h2>🎨 Import từ MangaDex</h2>
          <p className="description">
            MangaDex có hàng nghìn truyện tranh từ khắp thế giới.
          </p>
          
          <form onSubmit={handleImportMangaDex}>
            <div className="form-group">
              <label htmlFor="mangadex-keyword">Thể loại/Từ khóa:</label>
              <input
                id="mangadex-keyword"
                type="text"
                value={mangaDexKeyword}
                onChange={(e) => setMangaDexKeyword(e.target.value)}
                placeholder="e.g., action, romance, comedy"
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label htmlFor="mangadex-limit">Số lượng tối đa:</label>
              <input
                id="mangadex-limit"
                type="number"
                value={mangaDexLimit}
                onChange={(e) => setMangaDexLimit(e.target.value ? parseInt(e.target.value) : 50)}
                min="1"
                max="500"
                disabled={loading}
              />
            </div>

            <button 
              type="submit" 
              className="btn-primary"
              disabled={loading}
            >
              {loading ? '⏳ Đang import...' : '🚀 Bắt đầu Import'}
            </button>
          </form>

          <button 
            className="btn-danger"
            onClick={() => handleClearSource('MangaDex')}
            disabled={loading || stats?.mangaDex === 0}
          >
            🗑️ Xóa tất cả MangaDex
          </button>
        </div>
      </div>

      {/* Info Box */}
      <div className="info-box">
        <h3>ℹ️ Thông tin</h3>
        <ul>
          <li>✅ Import chạy ở chế độ không đồng bộ (async), không cần đợi hoàn thành</li>
          <li>✅ Tự động kiểm tra trùng lặp - sẽ không import 2 lần cùng cuốn sách</li>
          <li>✅ Mỗi 5 giây sẽ cập nhật thống kê</li>
          <li>⚠️ Xóa truyện sẽ xóa vĩnh viễn, không thể khôi phục</li>
          <li>📖 Truyện từ Gutenberg được đánh dấu là "Sách điện tử" với loại "dịch"</li>
          <li>🎨 Truyện từ MangaDex được đánh dấu là "Tranh" với loại "dịch"</li>
        </ul>
      </div>
    </div>
  );
}

export default ImportManager;
