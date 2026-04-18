const router = require('express').Router();
const { verifyToken } = require('../middleware/auth');

// Các điểm cuối bình luận
router.get('/story/:storyId', (req, res) => {
  // Lấy tất cả bình luận cho một truyện
  res.json({ message: 'Điểm cuối lấy bình luận' });
});

router.post('/', verifyToken, (req, res) => {
  // Tạo bình luận mới
  res.json({ message: 'Điểm cuối tạo bình luận' });
});

router.put('/:id', verifyToken, (req, res) => {
  // Cập nhật bình luận
  res.json({ message: 'Điểm cuối cập nhật bình luận' });
});

router.delete('/:id', verifyToken, (req, res) => {
  // Xóa bình luận
  res.json({ message: 'Điểm cuối xóa bình luận' });
});

module.exports = router;
