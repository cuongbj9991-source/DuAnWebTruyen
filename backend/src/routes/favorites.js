const router = require('express').Router();
const { verifyToken } = require('../middleware/auth');

// Các điểm cuối yêu thích
router.get('/', verifyToken, (req, res) => {
  // Lấy danh sách truyện yêu thích của người dùng
  res.json({ message: 'Điểm cuối lấy yêu thích' });
});

router.post('/:storyId', verifyToken, (req, res) => {
  // Thêm truyện vào yêu thích
  res.json({ message: 'Điểm cuối thêm vào yêu thích' });
});

router.delete('/:storyId', verifyToken, (req, res) => {
  // Xóa truyện khỏi yêu thích
  res.json({ message: 'Điểm cuối xóa khỏi yêu thích' });
});

module.exports = router;
