import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { storyService, chapterService, readingProgressService } from '../services/api';
import { useAuth } from '../context/AuthContext';
import '../styles/ChapterReader.css';

function ChapterReader() {
  const { storyId, chapterNumber } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [chapter, setChapter] = useState(null);
  const [story, setStory] = useState(null);
  const [chapters, setChapters] = useState([]);
  const [loading, setLoading] = useState(true);
  const [fontSize, setFontSize] = useState(16);
  const [lineHeight, setLineHeight] = useState(1.6);
  const [theme, setTheme] = useState('light');

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        
        // Fetch story info
        const storyResponse = await storyService.getStoryById(storyId);
        setStory(storyResponse.data);

        // Fetch all chapters for this story
        const chaptersResponse = await chapterService.getChaptersByStoryId(storyId);
        const chaptersArray = Array.isArray(chaptersResponse.data) 
          ? chaptersResponse.data 
          : chaptersResponse.data.data || [];
        setChapters(chaptersArray);

        // Fetch specific chapter
        const chapterResponse = await chapterService.getChapterByNumber(storyId, parseInt(chapterNumber));
        setChapter(chapterResponse.data);

        // Lưu tiến độ đọc nếu user đã đăng nhập
        if (user && user.id) {
          try {
            await readingProgressService.updateReadingProgress(storyId, {
              currentChapterNumber: parseInt(chapterNumber),
              lastReadAt: new Date().toISOString()
            });
          } catch (err) {
            // .NET User Service not available - skip reading progress
            console.debug('Reading progress service unavailable');
          }
        }
      } catch (error) {
        console.error('Error fetching chapter data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [storyId, chapterNumber, user]);

  const getCurrentChapterIndex = () => {
    return chapters.findIndex(c => c.chapterNumber === parseInt(chapterNumber));
  };

  const currentIndex = getCurrentChapterIndex();
  const prevChapter = currentIndex > 0 ? chapters[currentIndex - 1] : null;
  const nextChapter = currentIndex < chapters.length - 1 ? chapters[currentIndex + 1] : null;

  const handlePrevious = () => {
    if (prevChapter) {
      navigate(`/story/${storyId}/chapter/${prevChapter.chapterNumber}`);
    }
  };

  const handleNext = () => {
    if (nextChapter) {
      navigate(`/story/${storyId}/chapter/${nextChapter.chapterNumber}`);
    }
  };

  const handleFontSizeChange = (delta) => {
    setFontSize(prev => Math.max(12, Math.min(24, prev + delta)));
  };

  const toggleTheme = () => {
    setTheme(prev => prev === 'light' ? 'dark' : 'light');
  };

  if (loading) return <div className="loading">⏳ Đang tải chương...</div>;
  if (!chapter) return <div className="error">❌ Không tìm thấy chương</div>;

  return (
    <div className={`chapter-reader ${theme}`}>
      {/* Header */}
      <div className="reader-header">
        <Link to={`/story/${storyId}`} className="back-button">← Quay lại</Link>
        <div className="header-info">
          <h2>{story?.title}</h2>
          <p>Chương {chapter.chapterNumber}</p>
        </div>
        <div className="header-spacer"></div>
      </div>

      {/* Toolbar */}
      <div className="reader-toolbar">
        <div className="toolbar-group">
          <button 
            title="Giảm cỡ chữ"
            onClick={() => handleFontSizeChange(-2)}
            className="toolbar-btn"
          >
            A−
          </button>
          <span className="font-size-display">{fontSize}px</span>
          <button 
            title="Tăng cỡ chữ"
            onClick={() => handleFontSizeChange(2)}
            className="toolbar-btn"
          >
            A+
          </button>
        </div>

        <div className="toolbar-group">
          <button 
            title="Đổi chế độ"
            onClick={toggleTheme}
            className="toolbar-btn theme-btn"
          >
            {theme === 'light' ? '🌙' : '☀️'}
          </button>
        </div>

        <div className="toolbar-group">
          <select 
            className="chapter-select"
            value={chapterNumber}
            onChange={(e) => navigate(`/story/${storyId}/chapter/${e.target.value}`)}
          >
            <option value="">-- Chọn chương --</option>
            {chapters.map(ch => (
              <option key={ch.id} value={ch.chapterNumber}>
                Chương {ch.chapterNumber}: {ch.title}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Content */}
      <div 
        className="chapter-content"
        style={{
          fontSize: `${fontSize}px`,
          lineHeight: lineHeight
        }}
      >
        <h1 className="chapter-title">Chương {chapter.chapterNumber}: {chapter.title}</h1>
        <div className="chapter-meta">
          <span>📖 {chapter.wordCount || 0} từ</span>
          <span>📅 {new Date(chapter.createdAt).toLocaleDateString('vi-VN')}</span>
        </div>
        
        {/* Display chapter images if available */}
        {chapter.pages && Array.isArray(JSON.parse(chapter.pages || '[]')) && JSON.parse(chapter.pages || '[]').length > 0 ? (
          <div className="chapter-images">
            {JSON.parse(chapter.pages).map((imageUrl, index) => (
              <img 
                key={index}
                src={imageUrl} 
                alt={`Page ${index + 1}`}
                className="chapter-page"
                onError={(e) => {
                  e.target.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="400" height="600"%3E%3Crect fill="%23f0f0f0" width="400" height="600"/%3E%3Ctext x="50%25" y="50%25" font-family="Arial" font-size="16" fill="%23999" text-anchor="middle" dy=".3em"%3EUnable to load image%3C/text%3E%3C/svg%3E';
                }}
              />
            ))}
          </div>
        ) : (
          /* Display text content as fallback */
          <div className="chapter-text">
            {chapter.content?.split('\n\n').map((paragraph, index) => (
              <p key={index}>{paragraph}</p>
            ))}
          </div>
        )}
      </div>

      {/* Navigation */}
      <div className="chapter-nav">
        <button 
          className="nav-button prev-button"
          onClick={handlePrevious}
          disabled={!prevChapter}
        >
          ← {prevChapter ? `Chương ${prevChapter.chapterNumber}` : 'Chương trước'}
        </button>

        <div className="nav-info">
          <span>Chương {chapter.chapterNumber} / {chapters.length}</span>
          <div className="progress-bar">
            <div 
              className="progress-fill"
              style={{width: `${((currentIndex + 1) / chapters.length) * 100}%`}}
            ></div>
          </div>
        </div>

        <button 
          className="nav-button next-button"
          onClick={handleNext}
          disabled={!nextChapter}
        >
          {nextChapter ? `Chương ${nextChapter.chapterNumber}` : 'Chương tiếp'} →
        </button>
      </div>

      {/* Footer */}
      <div className="reader-footer">
        <p>📚 {story?.title}</p>
        <p>Tác giả: {story?.author}</p>
      </div>
    </div>
  );
}

export default ChapterReader;
