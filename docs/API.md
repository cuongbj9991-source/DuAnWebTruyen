# Tài Liệu API

## URL Cơ Bản
```
http://localhost:5000/api
```

## Xác Thực
Sử dụng JWT token trong header:
```
Authorization: Bearer <token>
```

---

## Các Điểm Cuối Xác Thực

### Đăng Ký
**POST** `/auth/register`

Request body:
```json
{
  "username": "username",
  "email": "email@example.com",
  "password": "password123"
}
```

Response:
```json
{
  "user": {
    "id": 1,
    "username": "username",
    "email": "email@example.com"
  },
  "token": "eyJhbGciOiJIUzI1NiIs..."
}
```

### Đăng Nhập
**POST** `/auth/login`

Request body:
```json
{
  "email": "email@example.com",
  "password": "password123"
}
```

Response: (same as register)

### Đăng Xuất
**POST** `/auth/logout`

Yêu cầu xác thực.

---

## Các Điểm Cuối Truyện

### Lấy Tất Cả Truyện
**GET** `/stories`

Advanced filtering with support for search, filtering by multiple criteria, and sorting.

Query parameters:
- `limit` (default: 20, max: 100) - Số lượng truyện mỗi trang
- `offset` (default: 0) - Offset cho pagination
- `search` - Từ khóa tìm kiếm (tiêu đề, tên hán việt, tác giả)
- `search_summary` - Tìm kiếm trong phần tóm tắt truyện
- `source` - Lọc theo nguồn: falool, qidian, fanqie, original, etc. (Hỗ trợ dấu phẩy để chọn nhiều)
- `min_chapters` - Số chương tối thiểu (e.g., 50, 100, 200, 500, 1000, 1500, 2000)
- `max_chapters` - Số chương tối đa
- `genre` - Thể loại: huyền huyễn, đô thị, ngôn tình, võng du, khoa học viễn tưởng, lịch sử, etc. (Hỗ trợ dấu phẩy)
- `story_type` - Loại truyện: sáng tác, dịch, txt dịch tự động, scan ảnh (Hỗ trợ dấu phẩy)
- `status` - Trạng thái: ongoing (còn tiếp), completed (hoàn thành), paused (tạm ngưng) (Hỗ trợ dấu phẩy)
- `min_rating` - Đánh giá tối thiểu (0-5)
- `sortBy` - Sắp xếp theo:
  - `views_total` - Lượt đọc tổng (mặc định)
  - `views_week` - Lượt đọc tuần
  - `views_day` - Lượt đọc ngày
  - `updated` - Mới cập nhật
  - `newest` - Mới nhập kho
  - `likes` - Lượt thích
  - `follows` - Lượt theo dõi
  - `bookmarks` - Lượt đánh dấu
  - `rating` - Đánh giá

Example requests:
```bash
GET /stories?limit=20&offset=0
GET /stories?search=kiếm%20khách
GET /stories?genre=huyền%20huyễn&status=ongoing
GET /stories?min_chapters=100&sortBy=views_week
GET /stories?source=falool,qidian&genre=ngôn%20tình,huyền%20huyễn
```

Response:
```json
[
  {
    "id": 1,
    "title": "Truyện Kiếm Khách",
    "title_alternative": "劍客傳",
    "author": "Tác giả 1",
    "description": "Một tác phẩm về các hiệp sĩ kiếm khắp thiên hạ",
    "summary": "Trong một thế giới kỳ kiếm...",
    "cover_image": "https://...",
    "category": "fantasy",
    "genre": "huyền huyễn",
    "story_type": "sáng tác",
    "status": "ongoing",
    "total_chapters": 250,
    "source": "original",
    "source_url": "https://...",
    "views_total": 15000,
    "views_week": 3500,
    "views_day": 800,
    "likes": 1200,
    "follows": 850,
    "bookmarks": 500,
    "rating": 4.5,
    "rating_count": 320,
    "last_chapter_updated": "2024-03-30T10:30:00Z",
    "created_at": "2024-01-01T00:00:00Z"
  }
]
```

### Lấy Tùy Chọn Lọc
**GET** `/stories/api/filter-options`

Lấy tất cả tùy chọn bộ lọc có sẵn (thể loại, tác giả, nguồn, etc.)

Response:
```json
{
  "genres": [
    "Huyền huyễn",
    "Đô thị",
    "Ngôn tình",
    "Võng du",
    "Khoa học viễn tưởng",
    "Lịch sử",
    "Đông nhân",
    "Dị năng",
    "Linh dị",
    "Light Novel"
  ],
  "authors": ["Tác giả 1", "Tác giả 2", ...],
  "sources": ["original", "falool", "qidian", "fanqie", "61kekan"],
  "story_types": ["sáng tác", "dịch", "txt dịch tự động", "scan ảnh"],
  "statuses": ["ongoing", "completed", "paused"],
  "sort_options": [
    {"value": "views_total", "label": "Lượt đọc tổng"},
    {"value": "views_week", "label": "Lượt đọc tuần"},
    {"value": "views_day", "label": "Lượt đọc ngày"},
    {"value": "updated", "label": "Mới cập nhật"},
    {"value": "newest", "label": "Mới nhập kho"},
    {"value": "likes", "label": "Lượt thích"},
    {"value": "follows", "label": "Lượt theo dõi"},
    {"value": "bookmarks", "label": "Lượt đánh dấu"},
    {"value": "rating", "label": "Đánh giá"}
  ]
}
```

### Lấy Chi Tiết Truyện
**GET** `/stories/:id`

Lấy chi tiết truyện (tự động tăng lượt xem)

Response: (single story object như ở Get All Stories)

### Tạo Truyện
**POST** `/stories`

Yêu cầu xác thực.

Request body:
```json
{
  "title": "Tiêu đề",
  "title_alternative": "Tên hán việt",
  "author": "Tác giả",
  "description": "Mô tả ngắn",
  "summary": "Tóm tắt chi tiết",
  "cover_image": "https://...",
  "category": "fantasy",
  "genre": "huyền huyễn",
  "story_type": "sáng tác",
  "status": "ongoing",
  "source": "original",
  "source_url": "https://...",
  "source_id": "id từ nguồn ban đầu"
}
```

### Cập Nhật Truyện
**PUT** `/stories/:id`

Yêu cầu xác thực.

Request body: (các trường cần cập nhật)

### Xóa Truyện
**DELETE** `/stories/:id`

Yêu cầu xác thực.

---

## Các Điểm Cuối Người Dùng

### Lấy Hồ Sơ Người Dùng
**GET** `/users/:id`

Response:
```json
{
  "id": 1,
  "username": "username",
  "email": "email@example.com",
  "avatar_url": "https://...",
  "bio": "User bio",
  "created_at": "2024-01-01T00:00:00Z"
}
```

### Cập Nhật Hồ Sơ Người Dùng
**PUT** `/users/:id`

Yêu cầu xác thực.

Request body:
```json
{
  "username": "new_username",
  "email": "new_email@example.com",
  "avatar_url": "https://...",
  "bio": "New bio"
}
```

### Lấy Lịch Sử Đọc
**GET** `/users/:id/reading-history`

Yêu cầu xác thực.

Response:
```json
[
  {
    "story_id": 1,
    "story_title": "Tiêu đề",
    "last_chapter_read": 10,
    "last_read_at": "2024-01-01T00:00:00Z"
  }
]
```

---

## Các Điểm Cuối Yêu Thích

### Lấy Yêu Thích
**GET** `/favorites`

Yêu cầu xác thực.

Response: (mảng đối tượng truyện)

### Thêm Vào Yêu Thích
**POST** `/favorites/:storyId`

Yêu cầu xác thực.

Response:
```json
{
  "message": "Thêm vào yêu thích"
}
```

### Xóa Khỏi Yêu Thích
**DELETE** `/favorites/:storyId`

Yêu cầu xác thực.

Response:
```json
{
  "message": "Xóa khỏi yêu thích"
}
```

---

## Các Điểm Cuối Bình Luận

### Lấy Bình Luận Truyện
**GET** `/comments/story/:storyId`

Query parameters:
- `limit` (default: 20)
- `offset` (default: 0)

Response:
```json
[
  {
    "id": 1,
    "user_id": 1,
    "username": "username",
    "content": "Truyện rất hay!",
    "rating": 5,
    "created_at": "2024-01-01T00:00:00Z"
  }
]
```

### Tạo Bình Luận
**POST** `/comments`

Yêu cầu xác thực.

Request body:
```json
{
  "story_id": 1,
  "content": "Nội dung bình luận",
  "rating": 5
}
```

### Cập Nhật Bình Luận
**PUT** `/comments/:id`

Yêu cầu xác thực.

Request body:
```json
{
  "content": "Nội dung cập nhật",
  "rating": 4
}
```

### Xóa Bình Luận
**DELETE** `/comments/:id`

Yêu cầu xác thực.

---

## Phản Hồi Lỗi

### 400 Yêu Cầu Không Hợp Lệ
```json
{
  "error": "Invalid input data"
}
```

### 401 Unauthorized
```json
{
  "error": "No token provided" 
}
```

```json
{
  "error": "Invalid token"
}
```

### 404 Not Found
```json
{
  "error": "Resource not found"
}
```

### 500 Internal Server Error
```json
{
  "error": "Server error message"
}
```

---

## Rate Limiting

API có rate limiting:
- 100 requests per 15 minutes per IP

Response khi vượt limit:
```
HTTP/1.1 429 Too Many Requests

{
  "error": "Too many requests, please try again later"
}
```

---

## Testing dengan cURL

### Get all stories
```bash
curl http://localhost:5000/api/stories
```

### Login
```bash
curl -X POST http://localhost:5000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

### Create story (với token)
```bash
curl -X POST http://localhost:5000/api/stories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"title":"Story","author":"Author","description":"Desc"}'
```
