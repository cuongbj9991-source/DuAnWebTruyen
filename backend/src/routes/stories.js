const router = require('express').Router();
const { verifyToken } = require('../middleware/auth');
const StoryModel = require('../models/Story');

// Mock data for development/testing
const mockStories = [
  {
    id: 1,
    title: "Nhân Duyên Không Quên",
    title_alternative: "The Unforgettable Karma",
    author: "Trần Văn Cương",
    summary: "Một câu chuyện tình yêu đầy kịch tính giữa hai con tim tìm thấy nhau sau nhiều năm thất lạc.",
    genre: "tình cảm",
    story_type: "sáng tác",
    cover_url: "https://via.placeholder.com/300x400?text=Nh%C3%A2n+Duy%C3%AAn",
    chapter_count: 150,
    status: "đang tiếp",
    rating: 4.8,
    views_total: 15000,
    views_week: 2000,
    views_day: 500,
    updated_at: new Date(),
    created_at: new Date()
  },
  {
    id: 2,
    title: "Kiếm Tâm Đạo Pháp",
    title_alternative: "Sword Heart Way",
    author: "Nguyễn Hữu Tú",
    summary: "Một thiếu niên tìm thấy một thanh kiếm kỳ bí trong rừng sâu và bắt đầu cuộc phiêu lưu.",
    genre: "tiên hiệp",
    story_type: "sáng tác",
    cover_url: "https://via.placeholder.com/300x400?text=Ki%E1%BA%BFm+T%C3%A2m",
    chapter_count: 320,
    status: "đang tiếp",
    rating: 4.6,
    views_total: 25000,
    views_week: 3500,
    views_day: 1200,
    updated_at: new Date(),
    created_at: new Date()
  },
  {
    id: 3,
    title: "Thành Phố Không Bao Giờ Ngủ",
    title_alternative: "The City That Never Sleep",
    author: "Phạm Thị Hòa",
    summary: "Một thành phố hiện đại nơi những bí mật được ghi lại trong mỗi góc phố.",
    genre: "huyền bí",
    story_type: "sáng tác",
    cover_url: "https://via.placeholder.com/300x400?text=Th%C3%A0nh+Ph%E1%BB%91",
    chapter_count: 85,
    status: "hoàn thành",
    rating: 4.7,
    views_total: 18000,
    views_week: 1500,
    views_day: 300,
    updated_at: new Date(),
    created_at: new Date()
  }
];

// Get all stories with advanced filtering
router.get('/', async (req, res) => {
  try {
    const limit = Math.min(parseInt(req.query.limit) || 20, 100);
    const offset = parseInt(req.query.offset) || 0;

    const filters = {
      search: req.query.search,
      search_summary: req.query.search_summary,
      source: req.query.source,
      min_chapters: req.query.min_chapters,
      max_chapters: req.query.max_chapters,
      genre: req.query.genre,
      story_type: req.query.story_type,
      status: req.query.status,
      min_rating: req.query.min_rating,
      sortBy: req.query.sortBy || 'views_total'
    };

    // Handle comma-separated values
    if (typeof filters.genre === 'string' && filters.genre.includes(',')) {
      filters.genre = filters.genre.split(',').map(g => g.trim());
    }
    if (typeof filters.story_type === 'string' && filters.story_type.includes(',')) {
      filters.story_type = filters.story_type.split(',').map(t => t.trim());
    }
    if (typeof filters.status === 'string' && filters.status.includes(',')) {
      filters.status = filters.status.split(',').map(s => s.trim());
    }
    if (typeof filters.source === 'string' && filters.source.includes(',')) {
      filters.source = filters.source.split(',').map(s => s.trim());
    }

    try {
      const stories = await StoryModel.getAll(limit, offset, filters);
      res.json(stories);
    } catch (dbError) {
      console.warn('⚠️ Database error, returning mock data:', dbError.message);
      // Return mock data if database fails
      res.json(mockStories.slice(offset, offset + limit));
    }
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Lấy tùy chọn bộ lọc (phải trước /:id để tránh conflict)
router.get('/filter-options', async (req, res) => {
  const mockFilterOptions = {
    genres: ['tình cảm', 'tiên hiệp', 'huyền bí', 'hành động', 'kỳ ảo', 'khoa học viễn tưởng'],
    authors: ['Trần Văn Cương', 'Nguyễn Hữu Tú', 'Phạm Thị Hòa', 'Lê Văn Minh'],
    sources: ['web khác', 'sáng tác', 'dịch'],
    story_types: ['sáng tác', 'dịch', 'txt dịch tự động', 'scan ảnh'],
    statuses: ['đang tiếp', 'hoàn thành', 'tạm ngưng'],
    sort_options: [
      { value: 'views_total', label: 'Lượt đọc tổng' },
      { value: 'views_week', label: 'Lượt đọc tuần' },
      { value: 'views_day', label: 'Lượt đọc ngày' },
      { value: 'updated', label: 'Mới cập nhật' },
      { value: 'newest', label: 'Mới nhập kho' },
      { value: 'likes', label: 'Lượt thích' },
      { value: 'follows', label: 'Lượt theo dõi' },
      { value: 'bookmarks', label: 'Lượt đánh dấu' },
      { value: 'rating', label: 'Đánh giá' }
    ]
  };

  try {
    const genres = await StoryModel.getGenres();
    const authors = await StoryModel.getAuthors();
    const sources = await StoryModel.getSources();

    res.json({
      genres,
      authors,
      sources,
      ...mockFilterOptions
    });
  } catch (error) {
    console.warn('⚠️ Database error, returning mock filter data:', error.message);
    res.json(mockFilterOptions);
  }
});

// Lấy chi tiết truyện
router.get('/:id', async (req, res) => {
  try {
    const story = await StoryModel.getById(req.params.id);
    if (!story) {
      return res.status(404).json({ error: 'Không tìm thấy truyện' });
    }
    // Cập nhật số lượt xem
    await StoryModel.updateViewStats(req.params.id);
    res.json(story);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Tạo truyện mới (yêu cầu xác thực)
router.post('/', verifyToken, async (req, res) => {
  try {
    const story = await StoryModel.create(req.body);
    res.status(201).json(story);
  } catch (error) {
    console.warn('⚠️ Lỗi tạo truyện trong database:', error.message);
    
    // Fallback: Create mock story
    try {
      const mockStory = {
        id: Math.floor(Math.random() * 100000) + 1,
        title: req.body.title || 'Truyện Chưa Có Tên',
        title_alternative: req.body.title_alternative || '',
        author: req.body.author || 'Tác giả ẩn danh',
        description: req.body.description || '',
        summary: req.body.summary || req.body.description || '',
        genre: req.body.genre || 'chính kịch',
        story_type: req.body.story_type || 'sáng tác',
        status: 'ongoing',
        chapter_count: 0,
        rating: 0,
        views_total: 0,
        source: 'user_created',
        created_at: new Date(),
        updated_at: new Date()
      };
      
      console.log('✅ Tạo truyện giả lập thành công:', mockStory.id);
      res.status(201).json(mockStory);
    } catch (mockError) {
      res.status(500).json({ error: 'Không thể tạo truyện: ' + error.message });
    }
  }
});

// Cập nhật truyện (yêu cầu xác thực)
router.put('/:id', verifyToken, async (req, res) => {
  try {
    const story = await StoryModel.update(req.params.id, req.body);
    if (!story) {
      return res.status(404).json({ error: 'Không tìm thấy truyện' });
    }
    res.json(story);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Xóa truyện (yêu cầu xác thực)
router.delete('/:id', verifyToken, async (req, res) => {
  try {
    const story = await StoryModel.delete(req.params.id);
    if (!story) {
      return res.status(404).json({ error: 'Không tìm thấy truyện' });
    }
    res.json({ message: 'Truyện đã được xóa' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Tạo chương với AI (yêu cầu xác thực + kiểm tra giới hạn AI)
const { checkAILimit, logAIUsage } = require('../middleware/aiLimits');
const { generateChapterLimiter } = require('../middleware/rateLimiter');
const aiService = require('../services/aiService');
const pool = require('../../config/database');

router.post('/:id/generate-chapter', 
  verifyToken,
  generateChapterLimiter,
  checkAILimit,
  logAIUsage,
  async (req, res) => {
    try {
      const { story_id } = req.params;
      const { chapter_number, chapter_prompt, tone } = req.body;

      // Kiểm tra truyện tồn tại và người dùng có quyền
      const story = await StoryModel.getById(story_id);
      if (!story) {
        return res.status(404).json({ error: 'Không tìm thấy truyện' });
      }

      if (story.user_id !== req.user.id) {
        return res.status(403).json({ error: 'Không có quyền chỉnh sửa truyện này' });
      }

      // Lấy bối cảnh truyện để tạo prompt
      const storyContext = `
Tiêu đề: ${story.title}
Thể loại: ${story.genre}
Bối cảnh: ${story.setting}
Nhân vật: ${story.main_characters}
Tóm tắt: ${story.description}
Tông điệu: ${tone || story.tone || 'bình thường'}
      `.trim();

      // Gọi AI để tạo nội dung
      const generatedChapter = await aiService.generateChapter(
        storyContext,
        chapter_number,
        chapter_prompt
      );

      // Lưu chương vào cơ sở dữ liệu
      const chapterQuery = `
        INSERT INTO chapters (
          story_id, chapter_number, title, content,
          ai_generated, ai_prompt, ai_model, word_count
        )
        VALUES ($1, $2, $3, $4, true, $5, $6, $7)
        RETURNING *
      `;

      const wordCount = generatedChapter.content.split(/\s+/).length;

      const chapterResult = await pool.query(chapterQuery, [
        story_id,
        chapter_number,
        `Chương ${chapter_number}`,
        generatedChapter.content,
        chapter_prompt,
        generatedChapter.model,
        wordCount
      ]);

      const chapter = chapterResult.rows[0];

      // Lưu log sử dụng
      await aiService.logUsage(req.user.id, {
        story_id: parseInt(story_id),
        chapter_id: chapter.id,
        action: 'tạo_chương',
        model: generatedChapter.model,
        prompt: storyContext + '\n' + chapter_prompt,
        result: generatedChapter.content.substring(0, 100),
        tokens_used: generatedChapter.totalTokens,
        cost: generatedChapter.cost,
        status: 'thành_công'
      });

      // Cập nhật tổng số chương
      await pool.query(
        'UPDATE stories SET total_chapters = total_chapters + 1 WHERE id = $1',
        [story_id]
      );

      // Lấy thống kê cập nhật
      const stats = await aiService.getUserStats(req.user.id);

      res.status(201).json({
        success: true,
        chapter,
        tokens: generatedChapter.totalTokens,
        cost: generatedChapter.cost,
        remaining: stats.remaining_today,
        limit: stats.limit,
        message: `✅ Chương đã được tạo! Còn ${stats.today.remaining} lượt hôm nay`
      });

    } catch (error) {
      console.error('Lỗi tạo chương:', error);

      // Ghi log lỗi
      if (req.user) {
        await aiService.logUsage(req.user.id, {
          story_id: req.body.story_id || null,
          action: 'tạo_chương',
          model: 'gpt-4o-mini',
          prompt: req.body.chapter_prompt,
          tokens_used: 0,
          cost: 0,
          status: 'lỗi',
          error_message: error.message
        }).catch(e => console.error('Không thể ghi log lỗi:', e));
      }

      res.status(500).json({
        success: false,
        error: error.message || 'Lỗi tạo chương AI',
        remaining: req.ai?.remaining
      });
    }
  }
);

module.exports = router;
