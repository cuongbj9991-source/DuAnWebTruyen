import React, { useState } from 'react';
import axios from 'axios';
import '../styles/UploadStory.css';

const UploadStory = () => {
  const [formData, setFormData] = useState({
    title: '',
    author: '',
    description: '',
    genre: 'Phiêu Lưu',
    type: 'original',
    content: '',
  });
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [uploadedStories, setUploadedStories] = useState([]);

  const token = localStorage.getItem('token');
  const userId = localStorage.getItem('userId');

  const genres = [
    'Phiêu Lưu', 'Hành Động', 'Hài Hước', 'Lãng Mạn', 'Kinh Dị',
    'Viễn Tưởng', 'Khoa Học', 'Lịch Sử', 'Bí Ẩn', 'Tâm Lý'
  ];

  const types = [
    { value: 'original', label: 'Sáng Tác' },
    { value: 'translation', label: 'Dịch' },
    { value: 'adaptation', label: 'Chuyển Thể' }
  ];

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    if (selectedFile) {
      if (selectedFile.type === 'text/plain' || selectedFile.name.endsWith('.txt')) {
        setFile(selectedFile);
        setMessage('');
        // Read file content
        const reader = new FileReader();
        reader.onload = (event) => {
          setFormData(prev => ({
            ...prev,
            content: event.target.result
          }));
        };
        reader.readAsText(selectedFile);
      } else {
        setMessage('⚠️ Chỉ chấp nhận file .txt');
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.title.trim()) {
      setMessage('❌ Vui lòng nhập tên truyện');
      return;
    }

    if (!formData.content.trim()) {
      setMessage('❌ Vui lòng nhập nội dung hoặc tải file');
      return;
    }

    setLoading(true);
    try {
      const response = await axios.post(
        'http://localhost:8080/uploads',
        formData,
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'User-Id': userId,
            'Content-Type': 'application/json'
          }
        }
      );

      setMessage('✅ Truyện đã được tải lên thành công! Đang chờ duyệt.');
      setFormData({
        title: '',
        author: '',
        description: '',
        genre: 'Phiêu Lưu',
        type: 'original',
        content: '',
      });
      setFile(null);
      
      // Add to displayed stories
      setUploadedStories(prev => [response.data, ...prev]);
    } catch (error) {
      setMessage('❌ Lỗi: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const fetchMyUploads = async () => {
    try {
      setLoading(true);
      const response = await axios.get(
        'http://localhost:8080/uploads/my-uploads?page=0&size=10',
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'User-Id': userId
          }
        }
      );
      setUploadedStories(response.data.data || []);
    } catch (error) {
      setMessage('❌ Không thể tải danh sách: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="upload-story-container">
      <div className="upload-wrapper">
        <h1>📝 Tải Lên Truyện Của Bạn</h1>
        
        {message && <div className={`message ${message.includes('✅') ? 'success' : 'error'}`}>{message}</div>}

        <form onSubmit={handleSubmit} className="upload-form">
          <div className="form-group">
            <label>Tên Truyện *</label>
            <input
              type="text"
              name="title"
              value={formData.title}
              onChange={handleInputChange}
              placeholder="Nhập tên truyện"
              required
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Tác Giả</label>
              <input
                type="text"
                name="author"
                value={formData.author}
                onChange={handleInputChange}
                placeholder="Nhập tên tác giả"
              />
            </div>
            <div className="form-group">
              <label>Thể Loại</label>
              <select name="genre" value={formData.genre} onChange={handleInputChange}>
                {genres.map(g => <option key={g} value={g}>{g}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>Loại</label>
              <select name="type" value={formData.type} onChange={handleInputChange}>
                {types.map(t => <option key={t.value} value={t.value}>{t.label}</option>)}
              </select>
            </div>
          </div>

          <div className="form-group">
            <label>Mô Tả</label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleInputChange}
              placeholder="Nhập mô tả về truyện"
              rows={3}
            />
          </div>

          <div className="form-group">
            <label>Nội Dung</label>
            <div className="file-upload">
              <input
                type="file"
                accept=".txt"
                onChange={handleFileChange}
              />
              {file && <p className="file-name">📄 {file.name}</p>}
            </div>
            <p className="hint">Hoặc nhập nội dung trực tiếp:</p>
            <textarea
              name="content"
              value={formData.content}
              onChange={handleInputChange}
              placeholder="Nhập nội dung truyện hoặc tải file .txt"
              rows={8}
            />
          </div>

          <button type="submit" className="btn-submit" disabled={loading}>
            {loading ? '⏳ Đang tải lên...' : '📤 Tải Lên'}
          </button>
        </form>

        <button onClick={fetchMyUploads} className="btn-secondary">
          📚 Xem Truyện Của Tôi ({uploadedStories.length})
        </button>

        {uploadedStories.length > 0 && (
          <div className="uploaded-stories">
            <h2>Truyện Của Tôi</h2>
            <div className="stories-grid">
              {uploadedStories.map(story => (
                <div key={story.id} className="story-card">
                  <div className="story-info">
                    <h3>{story.title}</h3>
                    <p className="author">{story.author}</p>
                    <p className="genre">{story.genre}</p>
                    <div className="status-badge" style={{
                      background: story.status === 'published' ? '#4caf50' : 
                                 story.status === 'rejected' ? '#f44336' : '#ff9800'
                    }}>
                      {story.status === 'published' && '✅ Đã Phát Hành'}
                      {story.status === 'pending_review' && '⏳ Chờ Duyệt'}
                      {story.status === 'rejected' && '❌ Từ Chối'}
                    </div>
                    {story.rejectionReason && (
                      <p className="rejection-reason">
                        <strong>Lý do:</strong> {story.rejectionReason}
                      </p>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default UploadStory;
