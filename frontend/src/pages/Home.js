import React, { useState, useContext } from 'react';
import { Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import StoryList from '../components/StoryList';
import FilterPanel from '../components/FilterPanel';
import '../styles/Home.css';

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('Error in Home:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{ padding: '20px', color: 'red', background: '#ffe6e6' }}>
          <h2>❌ Lỗi tải trang</h2>
          <p>{this.state.error?.message}</p>
        </div>
      );
    }
    return this.props.children;
  }
}

function Home() {
  const [filters, setFilters] = useState({});
  const { user, logout } = useContext(AuthContext);

  const handleFilterChange = (newFilters) => {
    setFilters(newFilters);
  };

  return (
    <ErrorBoundary>
      <div className="home">
        <header className="header">
          <div className="header-top">
            <h1>📚 Web Đọc Truyện</h1>
            <div className="header-actions">
              {user ? (
                <>
                  <span className="user-info">👤 {user.username || user.email}</span>
                  <Link to="/favorites" className="btn-favorites" title="Yêu thích của tôi">❤️ Yêu thích</Link>
                  <Link to="/import" className="btn-import" title="Nhập truyện từ Gutenberg & MangaDex">📥 Import</Link>
                  <Link to="/upload" className="btn-upload">📝 Tải Lên</Link>
                  <button onClick={logout} className="btn-logout">🚪 Đăng xuất</button>
                </>
              ) : (
                <>
                  <Link to="/login" className="btn-login">🔐 Đăng nhập</Link>
                  <Link to="/register" className="btn-register">✏️ Đăng ký</Link>
                </>
              )}
            </div>
          </div>
          <p>Khám phá hàng ngàn truyện hay</p>
        </header>

        <main className="main-content">
          <aside className="sidebar">
            <FilterPanel onFilterChange={handleFilterChange} initialFilters={filters} />
          </aside>

          <section className="content">
            <StoryList filters={filters} />
          </section>
        </main>
      </div>
    </ErrorBoundary>
  );
}

export default Home;
