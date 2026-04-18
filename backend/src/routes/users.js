const router = require('express').Router();
const { verifyToken } = require('../middleware/auth');
const UserModel = require('../models/User');

// Các điểm cuối của người dùng
router.get('/:id', (req, res) => {
  // Lấy hồ sơ người dùng
  res.json({ message: 'Điểm cuối lấy hồ sơ người dùng' });
});

router.put('/:id', verifyToken, (req, res) => {
  // Cập nhật hồ sơ người dùng
  res.json({ message: 'Điểm cuối cập nhật hồ sơ người dùng' });
});

router.get('/:id/reading-history', verifyToken, (req, res) => {
  // Lấy lịch sử đọc và tiến độ của người dùng
  res.json({ message: 'Điểm cuối lấy lịch sử đọc' });
});

// Lấy thống kê AI của người dùng
router.get('/:id/ai-stats', verifyToken, async (req, res) => {
  try {
    // Xác minh người dùng đang yêu cầu thống kê của họ hoặc là quản trị viên
    if (req.user.id !== parseInt(req.params.id)) {
      return res.status(403).json({ error: 'Không có quyền xem thống kê của người khác' });
    }

    const stats = await UserModel.getAIStats(req.user.id);
    res.json(stats);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Lấy nhật ký AI của người dùng
router.get('/:id/ai-logs', verifyToken, async (req, res) => {
  try {
    // Xác minh người dùng đang yêu cầu nhật ký của họ hoặc là quản trị viên
    if (req.user.id !== parseInt(req.params.id)) {
      return res.status(403).json({ error: 'Không có quyền xem nhật ký của người khác' });
    }

    const limit = Math.min(parseInt(req.query.limit) || 20, 100);
    const logs = await UserModel.getAILogs(req.user.id, limit);
    
    res.json({
      count: logs.length,
      logs
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
