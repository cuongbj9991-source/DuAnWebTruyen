import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import api from '../services/api';
import '../styles/CreateStory.css';

const CreateStory = () => {
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);
  const [loading, setLoading] = useState(false);
  const [step, setStep] = useState(1); // Bước 1: Thông tin truyện, Bước 2: Tạo chương đầu tiên

  const [formData, setFormData] = useState({
    title: '',
    description: '',
    genre: 'fantasy',
    setting: '',
    main_characters: '',
    tone: 'bình thường',
    target_audience: 'Mọi độc giả',
    visibility: 'private'
  });

  const [chapter, setChapter] = useState({
    chapter_number: 1,
    chapter_prompt: '',
    tone: 'bình thường'
  });

  const [generatedContent, setGeneratedContent] = useState(null);
  const [error, setError] = useState('');
  const [aiStats, setAiStats] = useState(null);

  if (!user) {
    return (
      <div className="create-story-container">
        <div className="error-box">
          ⚠️ Bạn cần đăng nhập để tạo truyện
        </div>
      </div>
    );
  }

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleChapterChange = (e) => {
    const { name, value } = e.target;
    setChapter(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const createStory = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      // Prepare data to send
      const storyData = {
        title: formData.title,
        description: formData.description,
        summary: formData.description,
        genre: formData.genre,
        story_type: formData.visibility === 'public' ? 'sáng tác' : 'sáng tác',
        status: 'ongoing'
      };

      const response = await api.post('/stories', storyData);
      
      // Handle response - could be real DB response or mock fallback
      const storyId = response.data?.id || response.data?._id;
      
      if (!storyId) {
        setError('Không thể lấy ID truyện từ phản hồi');
        setLoading(false);
        return;
      }

      setFormData({
        ...formData,
        _id: storyId,
        story_id: storyId
      });
      setStep(2); // Chuyển sang bước tạo chương
    } catch (err) {
      console.error('Error creating story:', err);
      setError(err.response?.data?.error || 'Lỗi tạo truyện');
    } finally {
      setLoading(false);
    }
  };

  const generateChapter = async (e) => {
    e.preventDefault();

    // Xác nhận trước khi generate
    if (!window.confirm(
      `⚠️ Tạo chương AI sẽ tốn 1 lượt!\n` +
      `Bạn còn ${aiStats?.remaining || '?'} lượt hôm nay.\n\nTiếp tục?`
    )) {
      return;
    }

    setLoading(true);
    setError('');
    setGeneratedContent(null);

    try {
      const response = await api.post(`/stories/${formData.story_id}/generate-chapter`, {
        ...chapter,
        story_id: formData.story_id
      });

      setGeneratedContent(response.data.chapter);
      setAiStats({
        remaining: response.data.remaining,
        limit: response.data.limit,
        cost: response.data.cost
      });

      // Hiển thị thông báo thành công
      alert(`✅ ${response.data.message}`);

    } catch (err) {
      const errorMsg = err.response?.data?.message || err.response?.data?.error || 'Lỗi tạo chương';
      setError(errorMsg);

      if (err.response?.status === 429) {
        alert('⏸️ ' + errorMsg);
      }
    } finally {
      setLoading(false);
    }
  };

  const finishStory = () => {
    navigate(`/story/${formData.story_id}`);
  };

  // Bước 1: Tạo truyện
  if (step === 1) {
    return (
      <div className="create-story-container">
        <h1>📖 Tạo Truyện Mới</h1>

        <form onSubmit={createStory} className="story-form">
          <div className="form-group">
            <label>Tiêu đề *</label>
            <input
              type="text"
              name="title"
              value={formData.title}
              onChange={handleInputChange}
              placeholder="Nhập tiêu đề truyện..."
              required
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Thể loại</label>
              <select name="genre" value={formData.genre} onChange={handleInputChange}>
                <option value="fantasy">Fantasy</option>
                <option value="romance">Lang Mạn</option>
                <option value="mystery">Bí Ẩn</option>
                <option value="horror">Kinh Dị</option>
                <option value="sci-fi">Khoa Học Viễn Tưởng</option>
                <option value="martial-arts">Kiếm Hiệp</option>
                <option value="slice-of-life">Đời Thường</option>
                <option value="other">Khác</option>
              </select>
            </div>

            <div className="form-group">
              <label>Tông Điệu</label>
              <select name="tone" value={formData.tone} onChange={handleInputChange}>
                <option value="bình thường">Bình Thường</option>
                <option value="hài hước">Hài Hước</option>
                <option value="đen tối">Đen Tối</option>
                <option value="lang mạn">Lang Mạn</option>
                <option value="anh hùng">Anh Hùng</option>
              </select>
            </div>
          </div>

          <div className="form-group">
            <label>Mô Tả *</label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleInputChange}
              placeholder="Mô tả nội dung truyện của bạn..."
              rows="4"
              required
            />
          </div>

          <div className="form-group">
            <label>Bối Cảnh</label>
            <input
              type="text"
              name="setting"
              value={formData.setting}
              onChange={handleInputChange}
              placeholder="VD: Thế giới phương Tây, năm 2050, thành phố Tokyo..."
            />
          </div>

          <div className="form-group">
            <label>Nhân Vật Chính</label>
            <textarea
              name="main_characters"
              value={formData.main_characters}
              onChange={handleInputChange}
              placeholder="VD: Tên, tuổi, tính cách&#10;Liệt kê các nhân vật chính..."
              rows="3"
            />
          </div>

          <div className="form-group">
            <label>Loại Hiển Thị</label>
            <select name="visibility" value={formData.visibility} onChange={handleInputChange}>
              <option value="private">Riêng Tư</option>
              <option value="public">Công Khai</option>
              <option value="followers">Chỉ Theo Dõi Viên</option>
            </select>
          </div>

          {error && <div className="error-box">{error}</div>}

          <button type="submit" disabled={loading} className="btn btn-primary">
            {loading ? 'Đang tạo...' : '✍️ Tạo Truyện'}
          </button>
        </form>
      </div>
    );
  }

  // Bước 2: Tạo chương đầu tiên
  return (
    <div className="create-story-container">
      <h1>📝 Tạo Chương Đầu Tiên - {formData.title}</h1>

      <div className="story-preview">
        <h3>{formData.title}</h3>
        <p><strong>Thể loại:</strong> {formData.genre}</p>
        <p><strong>Tóm tắt:</strong> {formData.description}</p>
      </div>

      <form onSubmit={generateChapter} className="chapter-form">
        <div className="form-group">
          <label>Số Chương</label>
          <input
            type="number"
            name="chapter_number"
            value={chapter.chapter_number}
            onChange={handleChapterChange}
            disabled
            min="1"
          />
        </div>

        <div className="form-group">
          <label>Mô Tả Chương</label>
          <textarea
            name="chapter_prompt"
            value={chapter.chapter_prompt}
            onChange={handleChapterChange}
            placeholder="Mô tả nội dung chương này (VD: Nhân vật chính gặp người yêu lần đầu...)&#10;Càng chi tiết càng tốt!"
            rows="4"
            required
          />
        </div>

        <div className="form-group">
          <label>Tông Điệu (tuỳ chọn)</label>
          <select name="tone" value={chapter.tone} onChange={handleChapterChange}>
            <option value="bình thường">Bình Thường</option>
            <option value="hài hước">Hài Hước</option>
            <option value="đen tối">Đen Tối</option>
            <option value="lang mạn">Lang Mạn</option>
            <option value="anh hùng">Anh Hùng</option>
          </select>
        </div>

        <div className="ai-info">
          ⚡ <strong>Chi Phí AI:</strong> 1 lượt/chương
          {aiStats && (
            <span className="remaining">
              Còn <strong>{aiStats.remaining}</strong>/<strong>{aiStats.limit}</strong> hôm nay
            </span>
          )}
        </div>

        {error && <div className="error-box">{error}</div>}

        {generatedContent ? (
          <div className="generated-content">
            <h3>✅ Chương Được Tạo</h3>
            <div className="content">
              {generatedContent.content}
            </div>
            <div className="actions">
              <button type="button" onClick={() => setGeneratedContent(null)} className="btn btn-secondary">
                ↻ Tạo Lại
              </button>
              <button type="button" onClick={finishStory} className="btn btn-primary">
                👉 Xem Truyện
              </button>
            </div>
          </div>
        ) : (
          <div className="actions">
            <button type="submit" disabled={loading} className="btn btn-primary">
              {loading ? '⏳ Đang tạo...' : '🎨 Tạo Chương Với AI'}
            </button>
            <button type="button" onClick={finishStory} className="btn btn-secondary">
              Bỏ Qua & Xem Truyện
            </button>
          </div>
        )}
      </form>
    </div>
  );
};

export default CreateStory;
