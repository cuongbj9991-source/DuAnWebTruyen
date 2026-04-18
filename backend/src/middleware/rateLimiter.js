const rateLimit = require('express-rate-limit');

/**
 * Giới hạn tỷ lệ: Ngăn chặn yêu cầu spam
 * - 3 yêu cầu trên điểm cuối /api/stories/generate per phút
 * - 10 yêu cầu mỗi giờ trên mỗi người dùng
 */
const generateChapterLimiter = rateLimit({
  windowMs: 60 * 1000, // 1 phút
  max: 3, // 3 yêu cầu mỗi phút
  message: 'Quá nhiều yêu cầu. Vui lòng đợi 1 phút.',
  statusCode: 429,
  standardHeaders: true, // Trả về thông tin giới hạn tỷ lệ trong tiêu đề RateLimit-*
  skipSuccessfulRequests: false,
  skipFailedRequests: false
});

/**
 * Giới hạn tỷ lệ: Giới hạn mỗi giờ
 */
const generateChapterHourlyLimiter = rateLimit({
  windowMs: 60 * 60 * 1000, // 1 giờ
  max: 10, // Tối đa 10 yêu cầu mỗi giờ
  message: 'Vượt quá giới hạn yêu cầu hàng giờ. Vui lòng thử lại sau.',
  statusCode: 429,
  store: new (require('express-rate-limit').MemoryStore)(),
  skip: (req, res) => {
    // Bỏ qua giới hạn tỷ lệ nếu người dùng là quản trị viên
    return req.user && req.user.role === 'admin';
  }
});

/**
 * Giới hạn tỷ lệ toàn cầu cho tất cả API
 * - 100 yêu cầu mỗi 15 phút
 */
const globalLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 100,
  message: 'Quá nhiều yêu cầu từ IP này. Vui lòng đợi 15 phút.',
  standardHeaders: true,
  skipSuccessfulRequests: false
});

module.exports = {
  generateChapterLimiter,
  generateChapterHourlyLimiter,
  globalLimiter
};
