const express = require('express');
const cors = require('cors');
const config = require('../config');
const { globalLimiter } = require('./middleware/rateLimiter');
const { logAIUsage } = require('./middleware/aiLimits');

const app = express();

// Middleware
app.use(cors());
app.use(express.json());

// Áp dụng giới hạn tỷ lệ toàn cầu cho tất cả các tuyến API
app.use('/api/', globalLimiter);

// Ghi log sử dụng AI cho tất cả các yêu cầu
app.use(logAIUsage);

// Các tuyến đường
app.use('/api/auth', require('./routes/auth'));
app.use('/api/stories', require('./routes/stories'));
app.use('/api/users', require('./routes/users'));
app.use('/api/comments', require('./routes/comments'));
app.use('/api/favorites', require('./routes/favorites'));

// Điểm kiểm tra sức khỏe
app.get('/health', (req, res) => {
  res.json({ 
    status: 'ổn',
    timestamp: new Date().toISOString(),
    ai_enabled: !!config.openai.apiKey
  });
});

// Middleware xử lý lỗi
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ error: err.message });
});

// Khởi động máy chủ
const PORT = config.app.port;
app.listen(PORT, () => {
  console.log(`🚀 Máy chủ chạy trên cổng ${PORT}`);
  console.log(`✅ Dịch vụ AI: ${config.openai.apiKey ? 'BẬT' : 'TẮT'}`);
});

module.exports = app;
