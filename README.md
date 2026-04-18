# Web Đọc Truyện Chữ & Tranh

Một nền tảng web hiện đại để đọc truyện chữ và truyện tranh với các tính năng đầy đủ như tài khoản người dùng, yêu thích, theo dõi tiến độ đọc, bình luận, và khả năng nhúng truyện từ các trang ngoài.

## 🌟 Tính Năng

### 🤖 AI Story Generation (NEW!)
- ✍️ **Tạo Truyện Với AI** - Người dùng có thể tạo truyện mới với hỗ trợ của GPT
- 📝 **Tạo Chương Tự Động** - Dùng AI để viết chương dựa trên mô tả của user
- 💰 **Kiểm Soát Chi Phí** - Hệ thống giới hạn hàng ngày (5 lượt free, 50 lượt premium)
- 📊 **Theo Dõi Sử Dụng** - Xem thống kê chi phí AI, tokens used, lượt còn lại
- 🛡️ **3 Lớp Bảo Vệ** - Rate limiting, daily limits, logging all API calls

### 👥 Tìm Kiếm & Lọc Nâng Cao
- 📚 **Danh sách truyện** - Xem danh sách truyện với hình bìa, đánh giá, thống kê
- 🔍 **Tìm kiếm đa tiêu chí**:
  - Tìm theo tiêu đề, tên hán việt, tác giả
  - Tìm kiếm trong tóm tắt truyện
- 📊 **Lọc nâng cao**:
  - Lọc theo số chương (>50, >100, >200, >500, >1000, >1500, >2000)
  - Lọc theo thể loại (huyền huyễn, đô thị, ngôn tình, võng du, etc.)
  - Lọc theo loại truyện (sáng tác, dịch, txt dịch, scan ảnh)
  - Lọc theo trạng thái (còn tiếp, hoàn thành, tạm ngưng)
  - Lọc theo nguồn (falool, qidian, fanqie, original, etc.)
  - Lọc theo đánh giá (tối thiểu)
- ⬆️ **Sắp xếp đa dạng**:
  - Lượt đọc tổng, tuần, ngày
  - Mới cập nhật, mới nhập kho
  - Lượt thích, lượt theo dõi, lượt đánh dấu
  - Đánh giá

### 📖 Chức Năng Chính
- 📖 **Đọc Truyện** - Hiển thị nội dung truyện theo chương
- 👤 **Tài khoản & Đăng nhập** - Hệ thống đăng ký, đăng nhập bằng JWT
- ❤️ **Yêu Thích** - Lưu các truyện yêu thích
- ⏱️ **Theo dõi Tiến độ** - Lưu vị trí đọc và tiến độ
- 💬 **Bình luận & Đánh giá** - Người dùng có thể bình luận và đánh giá truyện
- 🌐 **Nhúng Truyện Ngoài** - Lấy truyện từ các trang như Falool bằng web scraping

### 📊 Thống Kê Truyện
- 👁️ Lượt xem (tổng, tuần, ngày)
- ❤️ Lượt thích
- 📌 Lượt theo dõi
- 🔖 Lượt đánh dấu
- ⭐ Đánh giá (0-5 sao)

## 🛠️ Công Nghệ Sử Dụng

### Backend
- **Node.js** - Runtime JavaScript
- **Express** - Web framework
- **PostgreSQL** - Database
- **JWT** - Xác thực
- **Cheerio & Axios** - Web scraping

### Frontend
- **React** - UI framework
- **React Router** - Routing
- **Axios** - HTTP client
- **CSS3** - Styling

## 📁 Cấu Trúc Dự Án

```
web-doc-truyen/
├── backend/                 # Node.js/Express server
│   ├── src/
│   │   ├── controllers/     # Business logic
│   │   ├── routes/          # API routes
│   │   ├── models/          # Database models
│   │   ├── middleware/      # Custom middleware
│   │   ├── services/        # Business services (scraping, etc.)
│   │   └── server.js        # Entry point
│   ├── config/              # Configuration files
│   ├── package.json
│   └── .env.example
├── frontend/                # React application
│   ├── src/
│   │   ├── components/      # Reusable components
│   │   ├── pages/           # Page components
│   │   ├── services/        # API services
│   │   ├── context/         # React context (Auth, etc.)
│   │   ├── styles/          # CSS files
│   │   ├── App.js
│   │   └── index.js
│   ├── public/
│   ├── package.json
│   └── .env.example
├── database/                # Database schema
│   ├── schema.sql           # Tables definition
│   └── seed.sql             # Sample data
└── .github/
    └── copilot-instructions.md
```

## 🚀 Hướng Dẫn Cài Đặt

### Yêu Cầu
- Node.js v16+
- npm hoặc yarn
- PostgreSQL 12+
- Git

### Cài Đặt Backend

1. Vào thư mục backend:
```bash
cd backend
npm install
```

2. Tạo file `.env` từ `.env.example`:
```bash
cp .env.example .env
```

3. Cập nhật thông tin database trong `.env`:
```
DB_USER=postgres
DB_PASSWORD=your_password
DB_HOST=localhost
DB_PORT=5432
DB_NAME=web_doc_truyen
```

4. **Cấu hình OpenAI (cho AI Story Generation)**:
```
OPENAI_API_KEY=sk-your-key-here
OPENAI_MODEL=gpt-4o-mini
OPENAI_MAX_TOKENS=500
AI_DAILY_LIMIT=5              # Free users
AI_PREMIUM_DAILY_LIMIT=50     # Premium users
```

   > **Cách lấy API Key:**
   > 1. Đăng nhập https://platform.openai.com
   > 2. Vào Settings → API Keys
   > 3. Tạo key mới, copy vào `.env`
   > 4. **QUAN TRỌNG:** Đặt hard billing limit ($5-10) để tránh chi phí bất ngờ
   > 5. Xem hướng dẫn chi tiết: [docs/AI_COST_CONTROL.md](docs/AI_COST_CONTROL.md)

5. Chạy migrations (tạo database và bảng):
```bash
npm run migrate
```

5. Khởi động server:
```bash
npm run dev
```

Server sẽ chạy trên http://localhost:5000

### Cài Đặt Database

1. Tạo database mới:
```bash
createdb web_doc_truyen
```

2. Chạy schema file:
```bash
psql -U postgres -d web_doc_truyen -f database/schema.sql
```

3. (Optional) Thêm dữ liệu mẫu:
```bash
psql -U postgres -d web_doc_truyen -f database/seed.sql
```

### Cài Đặt Frontend

1. Vào thư mục frontend:
```bash
cd frontend
npm install
```

2. Tạo file `.env` (nếu cần):
```bash
cp .env.example .env
```

3. Khởi động development server:
```bash
npm start
```

Ứng dụng sẽ mở tại http://localhost:3000

## 📝 API Endpoints

### Authentication
- `POST /api/auth/register` - Đăng ký tài khoản mới
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/logout` - Đăng xuất

### Stories
- `GET /api/stories` - Lấy danh sách truyện (có phân trang, tìm kiếm, lọc)
- `GET /api/stories/:id` - Lấy chi tiết truyện
- `POST /api/stories` - Tạo truyện mới (cần đăng nhập)
- `PUT /api/stories/:id` - Cập nhật truyện (cần đăng nhập)
- `DELETE /api/stories/:id` - Xóa truyện (cần đăng nhập)

### Users
- `GET /api/users/:id` - Lấy hồ sơ người dùng
- `PUT /api/users/:id` - Cập nhật hồ sơ (cần đăng nhập)
- `GET /api/users/:id/reading-history` - Lấy lịch sử đọc

### Favorites
- `GET /api/favorites` - Lấy danh sách truyện yêu thích (cần đăng nhập)
- `POST /api/favorites/:storyId` - Thêm vào yêu thích (cần đăng nhập)
- `DELETE /api/favorites/:storyId` - Xóa khỏi yêu thích (cần đăng nhập)

### Comments
- `GET /api/comments/story/:storyId` - Lấy bình luận của truyện
- `POST /api/comments` - Thêm bình luận (cần đăng nhập)
- `PUT /api/comments/:id` - Cập nhật bình luận (cần đăng nhập)
- `DELETE /api/comments/:id` - Xóa bình luận (cần đăng nhập)

## 💾 Database Schema

### Users
- `id` - Primary Key
- `username` - Tên người dùng (unique)
- `email` - Email (unique)
- `password` - Mật khẩu (hashed)
- `avatar_url` - Link ảnh đại diện
- `bio` - Tiểu sử
- `created_at`, `updated_at` - Timestamps

### Stories
- `id` - Primary Key
- `title` - Tiêu đề
- `author` - Tác giả
- `description` - Mô tả
- `cover_image` - Link ảnh bìa
- `category` - Thể loại
- `total_chapters` - Tổng số chương
- `source` - Nguồn (original, falool, etc.)
- `source_url` - URL nguồn
- `view_count` - Lượt xem
- `created_at`, `updated_at` - Timestamps

### Chapters
- `id` - Primary Key
- `story_id` - Foreign Key to Stories
- `chapter_number` - Số chương
- `title` - Tiêu đề chương
- `content` - Nội dung
- `source_url` - URL nguồn
- `created_at` - Timestamp

### Reading Progress
- `user_id` - Foreign Key to Users
- `story_id` - Foreign Key to Stories
- `last_chapter_read` - Chương cuối cùng đọc
- `scroll_position` - Vị trí scroll
- `last_read_at` - Lần cuối đọc

### Favorites
- `user_id` - Foreign Key to Users
- `story_id` - Foreign Key to Stories
- `created_at` - Timestamp

### Comments
- `id` - Primary Key
- `user_id` - Foreign Key to Users
- `story_id` - Foreign Key to Stories
- `content` - Nội dung bình luận
- `rating` - Đánh giá (1-5)
- `created_at`, `updated_at` - Timestamps

## 🔐 Xác Thực

Ứng dụng sử dụng JWT (JSON Web Token) để xác thực. Sau khi đăng nhập, token được lưu trong localStorage và gửi kèm mọi request đến backend thông qua header `Authorization: Bearer <token>`.

## 📚 Web Scraping

Ứng dụng có khả năng lấy truyện từ các trang ngoài như Falool bằng cách sử dụng:
- **Axios** - Để fetch HTML
- **Cheerio** - Để parse HTML

Xem `backend/src/services/scraperService.js` để biết cách scrape từ Falool.

## 📖 Hướng Dẫn Phát Triển Thêm

### Thêm Tính Năng Web Scraping

1. Mở `backend/src/services/scraperService.js`
2. Thêm method mới cho trang khác (ví dụ: `scrapeFromWebsiteX`)
3. Tạo selectors phù hợp với HTML của trang đó
4. Thêm route trong `backend/src/routes/stories.js`

### Tùy Chỉnh Giao Diện

- CSS files nằm trong `frontend/src/styles/`
- Edit `App.css` để thay đổi styling chính
- Tạo component CSS mới cho các component cụ thể

### Thêm Database Migrations

1. Tạo file SQL mới trong `database/`
2. Chạy lệnh psql để apply migration
3. Cập nhật schema.sql nếu cần

## 🐛 Troubleshooting

### Backend không kết nối được database
- Kiểm tra PostgreSQL đang chạy: `psql --version`
- Kiểm tra cấu hình `.env`
- Kiểm tra database đã được tạo: `psql -l`

### Frontend không kết nối được backend
- Kiểm tra backend đang chạy trên port 5000
- Kiểm tra CORS configuration trong `backend/src/server.js`
- Kiểm tra proxy setting trong `frontend/package.json`

### Port đã được sử dụng
```bash
# Thay đổi port trong backend: cập nhật PORT trong .env
# Thay đổi port frontend: npm start -- --port 3001
```

## 📄 License

MIT

## 👨‍💻 Đóng Góp

Quy trình đóng góp:
1. Fork repository
2. Tạo branch feature (`git checkout -b feature/amazing-feature`)
3. Commit thay đổi (`git commit -m 'Add amazing feature'`)
4. Push branch (`git push origin feature/amazing-feature`)
5. Mở Pull Request

## 📞 Hỗ Trợ

Nếu gặp vấn đề, vui lòng:
1. Kiểm tra documentation
2. Xem các issues đã tồn tại
3. Tạo issue mới với mô tả chi tiết
