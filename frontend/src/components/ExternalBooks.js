import React, { useState, useEffect } from 'react';
import { multiSourceSearchService } from '../services/api';
import '../styles/ExternalBooks.css';

const ExternalBooks = () => {
  const [searchResults, setSearchResults] = useState(null);
  const [keyword, setKeyword] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    loadRecommended();
  }, []);

  const loadRecommended = async () => {
    setLoading(true);
    try {
      const result = await multiSourceSearchService.getRecommended();
      setSearchResults(result.data);
      setMessage('📚 Những sách được đề xuất');
    } catch (error) {
      setMessage('❌ Lỗi: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!keyword.trim()) {
      setMessage('⚠️ Vui lòng nhập từ khóa tìm kiếm');
      return;
    }

    setLoading(true);
    try {
      const result = await multiSourceSearchService.searchAllSources(keyword, 1);
      setSearchResults(result.data);
      setMessage(`📚 Tìm thấy ${result.data.totalResults} kết quả`);
    } catch (error) {
      setMessage('❌ Lỗi: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const importToCollection = (book, source) => {
    console.log('Import book:', { ...book, source });
    setMessage(`✅ Sách "${book.title}" đã được thêm vào bộ sưu tập (tính năng sắp có)`);
  };

  return (
    <div className="external-books">
      <div className="search-section">
        <h2>🔍 Tìm Kiếm Sách Ngoài</h2>
        <form onSubmit={handleSearch}>
          <input
            type="text"
            placeholder="Tìm kiếm từ Project Gutenberg, Open Library, MangaDex..."
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
          />
          <button type="submit" disabled={loading}>
            {loading ? '⏳ Đang tìm kiếm...' : '🔍 Tìm Kiếm'}
          </button>
        </form>
        <button onClick={loadRecommended} className="btn-recommended" disabled={loading}>
          ⭐ Xem Đề Xuất
        </button>
      </div>

      {message && <div className="message">{message}</div>}

      {searchResults && (
        <div className="results-container">
          {searchResults.gutenberg && searchResults.gutenberg.length > 0 && (
            <div className="source-section">
              <h3>📖 Project Gutenberg</h3>
              <div className="books-grid">
                {searchResults.gutenberg.map((book) => (
                  <div key={book.id} className="book-card">
                    {book.coverUrl && (
                      <img src={book.coverUrl} alt={book.title} className="book-cover" />
                    )}
                    <div className="book-info">
                      <h4>{book.title}</h4>
                      {book.author && <p className="author">{book.author}</p>}
                      {book.genre && <p className="genre">{book.genre}</p>}
                      {book.description && (
                        <p className="description">{book.description.substring(0, 100)}...</p>
                      )}
                      <button
                        className="btn-import"
                        onClick={() => importToCollection(book, 'Gutenberg')}
                      >
                        📥 Nhập
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {searchResults.openLibrary && searchResults.openLibrary.length > 0 && (
            <div className="source-section">
              <h3>📚 Open Library</h3>
              <div className="books-grid">
                {searchResults.openLibrary.map((book) => (
                  <div key={book.id} className="book-card">
                    {book.coverUrl && (
                      <img src={book.coverUrl} alt={book.title} className="book-cover" />
                    )}
                    <div className="book-info">
                      <h4>{book.title}</h4>
                      {book.author && <p className="author">{book.author}</p>}
                      {book.genre && <p className="genre">{book.genre}</p>}
                      <button
                        className="btn-import"
                        onClick={() => importToCollection(book, 'OpenLibrary')}
                      >
                        📥 Nhập
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {searchResults.mangaDex && searchResults.mangaDex.length > 0 && (
            <div className="source-section">
              <h3>🎨 MangaDex</h3>
              <div className="books-grid">
                {searchResults.mangaDex.map((book) => (
                  <div key={book.id} className="book-card">
                    {book.coverUrl && (
                      <img src={book.coverUrl} alt={book.title} className="book-cover" />
                    )}
                    <div className="book-info">
                      <h4>{book.title}</h4>
                      {book.author && <p className="author">{book.author}</p>}
                      {book.genre && <p className="genre">{book.genre}</p>}
                      <button
                        className="btn-import"
                        onClick={() => importToCollection(book, 'MangaDex')}
                      >
                        📥 Nhập
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {searchResults.totalResults === 0 && (
            <div className="no-results">
              ❌ Không tìm thấy kết quả nào
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default ExternalBooks;
