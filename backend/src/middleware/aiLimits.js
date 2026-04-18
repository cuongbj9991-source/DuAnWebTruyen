const aiService = require('../services/aiService');

/**
 * Middleware: Kiểm tra giới hạn AI hàng ngày
 * - Người dùng miễn phí: 5 lượt/ngày
 * - Người dùng cao cấp: 50 lượt/ngày
 */
const checkAILimit = async (req, res, next) => {
  try {
    // Nếu người dùng chưa đăng nhập, từ chối
    if (!req.user) {
      return res.status(401).json({
        success: false,
        message: 'Bạn phải đăng nhập để sử dụng AI'
      });
    }

    // Kiểm tra xem người dùng có cao cấp không
    const userQuery = await require('../config/database').query(
      'SELECT is_premium FROM users WHERE id = $1',
      [req.user.id]
    );

    const isPremium = userQuery.rows[0]?.is_premium || false;
    const dailyLimit = isPremium 
      ? require('../config').ai_limits.premium_daily_limit 
      : require('../config').ai_limits.daily_limit;

    // Lấy sử dụng hôm nay
    const today = new Date().toISOString().split('T')[0];
    const usageQuery = await require('../config/database').query(
      'SELECT usage_count FROM user_ai_usage WHERE user_id = $1 AND date = $2',
      [req.user.id, today]
    );

    const currentUsage = usageQuery.rows[0]?.usage_count || 0;
    const remaining = dailyLimit - currentUsage;

    // Gắn thông tin vào yêu cầu
    req.ai = {
      remaining,
      used: currentUsage,
      limit: dailyLimit,
      isPremium,
      hasLimit: remaining <= 0
    };

    // Nếu hết lượt và không phải cao cấp
    if (remaining <= 0 && !isPremium) {
      return res.status(429).json({
        success: false,
        message: `Bạn đã dùng hết ${dailyLimit} lượt AI trong ngày. Vui lòng thử lại vào ngày mai.`,
        remaining: 0,
        limit: dailyLimit,
        upgrade: 'Nâng cấp lên Cao Cấp để có 50 lượt/ngày!'
      });
    }

    // Nếu hết lượt cao cấp
    if (remaining <= 0 && isPremium) {
      return res.status(429).json({
        success: false,
        message: `Bạn đã dùng hết ${dailyLimit} lượt AI Cao Cấp trong ngày.`,
        remaining: 0,
        limit: dailyLimit
      });
    }

    next();
  } catch (error) {
    console.error('Lỗi trong middleware checkAILimit:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi kiểm tra giới hạn AI',
      error: error.message
    });
  }
};

/**
 * Middleware: Ghi log sử dụng AI sau khi yêu cầu thành công
 */
const logAIUsage = async (req, res, next) => {
  // Lưu hàm json gốc
  const originalJson = res.json;

  res.json = function (data) {
    // Nếu AI được sử dụng (có chi phí) và yêu cầu thành công
    if (req.user && req.aiGeneration && res.statusCode === 200) {
      aiService.logUsage(req.user.id, {
        story_id: req.body.story_id || null,
        chapter_id: req.body.chapter_id || null,
        action: req.aiGeneration.action || 'tạo_chương',
        model: req.aiGeneration.model,
        prompt: req.aiGeneration.prompt,
        result: req.aiGeneration.result,
        tokens_used: req.aiGeneration.tokens,
        cost: req.aiGeneration.cost,
        status: 'thành_công'
      }).catch(err => {
        console.error('Không thể ghi log sử dụng AI:', err);
      });
    }

    // Gọi lại hàm json gốc
    return originalJson.call(this, data);
  };

  next();
};

module.exports = {
  checkAILimit,
  logAIUsage
};
