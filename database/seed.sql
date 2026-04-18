-- Dữ liệu mẫu cho phát triển
INSERT INTO users (username, email, password) VALUES
('reader1', 'reader1@example.com', '$2a$10$YIvxJxJJxJxJxJxJxJxJx.placeholder.hash'),
('reader2', 'reader2@example.com', '$2a$10$YIvxJxJJxJxJxJxJxJxJx.placeholder.hash');

INSERT INTO stories (
  title, title_alternative, author, description, summary, 
  category, genre, story_type, status, total_chapters, 
  source, source_url, views_total, views_week, views_day, 
  likes, follows, bookmarks, rating, rating_count
) VALUES
(
  'Truyện Kiếm Khách', 
  '劍客傳',
  'Tác giả 1', 
  'Một tác phẩm về các hiệp sĩ kiếm khắp thiên hạ',
  'Trong một thế giới của kỳ kiếm, một hiệp sĩ trẻ tuổi phải vượt qua nhiều thử thách để trở thành cao thủ hàng đầu. Câu chuyện về danh dự, tình bạn và luyện tập không ngừng.',
  'fantasy', 'Huyền huyễn', 'sáng tác', 'ongoing', 250,
  'original', 'https://example.com/story/1', 15000, 3500, 800,
  1200, 850, 500, 4.5, 320
),
(
  'Thế Giới Thần Thoại', 
  '神話世界',
  'Tác giả 2', 
  'Hành trình khám phá thế giới ma quái đầy kỳ bí',
  'Một danh nhân được tái sinh vào một thế giới khác nơi những vị thần thoại vẫn còn tồn tại. Anh phải vượt qua nhiều thử thách liên quan đến thần thoại và phép thuật.',
  'fantasy', 'Huyền huyễn', 'dịch', 'ongoing', 380,
  'falool', 'https://falool.com/story/2', 22000, 5200, 1200,
  1800, 1200, 800, 4.7, 450
),
(
  'Tình Yêu Mùa Thu', 
  '秋日戀戀',
  'Tác giả 3', 
  'Một câu chuyện tình cảm lãng mạn lãng du',
  'Trong mùa thu vàng của thành phố, hai tâm hồn gặp gỡ và cảm nhận được tình yêu của mùa. Một câu chuyện đầy cảm xúc về tình bạn, tình yêu và những quyết định khó khăn.',
  'romance', 'Ngôn tình', 'sáng tác', 'completed', 145,
  'original', 'https://example.com/story/3', 18000, 2800, 400,
  950, 620, 410, 4.3, 280
),
(
  'Bí Mật Trong Cabin', 
  '小屋的秘密',
  'Tác giả 4', 
  'Một bí ẩn chờ khám phá trong canh đêm dài',
  'Một nhóm bạn bị mắc kẹt trong một cabin núi vào mùa đông. Họ phải giải quyết những bí ẩn lạnh thấu xương để sống sót. Một câu chuyện kinh dị-bí ẩn đầy twist.',
  'mystery', 'Đô thị', 'sáng tác', 'ongoing', 92,
  'original', 'https://example.com/story/4', 12000, 2100, 520,
  750, 480, 320, 4.2, 210
),
(
  'Võng Du Chi Kiếp', 
  '網遊之劫',
  'Tác giả 5', 
  'Một gamer bị nhập vào thế giới game của chính mình',
  'Một nhà phát triển game thần kỳ tỉnh dậy bên trong thế giới game mà anh ta vừa tạo ra. Anh ta phải sử dụng kiến thức của mình để tồn tại trong thế giới đầy nguy hiểm này.',
  'fantasy', 'Võng du', 'sáng tác', 'ongoing', 520,
  'original', 'https://example.com/story/5', 35000, 8500, 2100,
  2800, 1950, 1200, 4.8, 670
);

INSERT INTO chapters (story_id, chapter_number, title, content) VALUES
(1, 1, 'Chương 1: Khởi Đầu', 'Nội dung chương 1 của Truyện Kiếm Khách - Một hiệp sĩ trẻ bắt đầu hành trình của mình...'),
(1, 2, 'Chương 2: Hành Trình', 'Nội dung chương 2 - Anh ta gặp những người bạn đầu tiên...'),
(1, 3, 'Chương 3: Phép Khí', 'Nội dung chương 3 - Anh ta học được phép khí đầu tiên...'),
(2, 1, 'Chương 1: Cánh Cửa Mới', 'Nội dung chương 1 của Thế Giới Thần Thoại - Thế giới bí ẩn chào đón...'),
(2, 2, 'Chương 2: Gặp Gỡ', 'Nội dung chương 2 - Cuộc gặp gỡ với các vị thần...'),
(3, 1, 'Chương 1: Mùa Thu Đến', 'Nội dung chương 1 - Hai trái tim gặp gỡ...'),
(4, 1, 'Chương 1: Mắc Kẹt', 'Nội dung chương 1 - Họ bị mắc kẹt trong cabin...'),
(5, 1, 'Chương 1: Tỉnh Dậy', 'Nội dung chương 1 - Anh tỉnh dậy trong thế giới mà anh tạo ra...');

INSERT INTO favorites (user_id, story_id) VALUES
(1, 1),
(1, 3),
(1, 5),
(2, 2),
(2, 4),
(2, 5);

INSERT INTO comments (user_id, story_id, content, rating) VALUES
(1, 1, 'Truyện rất hay, mong chờ chương tiếp theo!', 5),
(2, 1, 'Cốt truyện hấp dẫn, nhân vật được phát triển tốt', 4),
(1, 2, 'Thế giới quan rất độc đáo và sáng tạo', 5),
(2, 3, 'Tình yêu được miêu tả rất tự nhiên', 4),
(1, 5, 'Concept võng du rất lý thú', 5),
(2, 2, 'Những twist khiến tôi bất ngờ', 4);
