# Hướng Dẫn Cài Đặt Hoàn Chỉnh - Web Đọc Truyện Với AI

Hướng dẫn này sẽ giúp bạn cài đặt nền tảng tạo truyện được hỗ trợ bởi AI hoàn chỉnh.

## 📋 Yêu Cầu Công Nghệ

### Yêu Cầu Hệ Thống
- **Node.js** phiên bản 16 trở lên
- **PostgreSQL** 12 trở lên
- **npm** hoặc **yarn**
- **Tài Khoản OpenAI** (để tạo truyện bằng AI)

### Cài Đặt
1. Tải và cài đặt Node.js: https://nodejs.org
2. Tải và cài đặt PostgreSQL: https://www.postgresql.org/download/
3. Tạo tài khoản OpenAI: https://platform.openai.com

## 🔧 Bước 1: Cài Đặt Cơ Sở Dữ Liệu

### Tạo Cơ Sở Dữ Liệu PostgreSQL
```bash
# Kết nối đến PostgreSQL
psql -U postgres

# Bên trong psql:
CREATE DATABASE web_doc_truyen;
\q
```

### Chạy Sơ Đồ
```bash
cd database
psql -U postgres -d web_doc_truyen -f schema.sql
psql -U postgres -d web_doc_truyen -f seed.sql

# Xác minh các bảng được tạo
psql -U postgres -d web_doc_truyen -c "\dt"
```

## ⚙️ Bước 2: Cài Đặt Backend

### Cài Đặt Phụ Thuộc
```bash
cd backend
npm install
```

### Tạo Tệp .env
```bash
cp .env.example .env
```

### Cấu Hình .env
Chỉnh sửa `backend/.env` với các cài đặt của bạn:

```env
# Máy Chủ
PORT=5000
NODE_ENV=development

# Cơ Sở Dữ Liệu
DB_USER=postgres
DB_PASSWORD=mật_khẩu_postgres_của_bạn
DB_HOST=localhost
DB_PORT=5432
DB_NAME=web_doc_truyen

# JWT
JWT_SECRET=khóa-bí-mật-rất-bảo-mật-thay-đổi-trong-sản-xuất
JWT_EXPIRE=7d

# Cấu Hình OpenAI
OPENAI_API_KEY=sk-khóa-api-của-bạn-ở-đây
OPENAI_MODEL=gpt-4o-mini
OPENAI_MAX_TOKENS=500

# Giới Hạn AI
AI_DAILY_LIMIT=5
AI_PREMIUM_DAILY_LIMIT=50
AI_REQUEST_TIMEOUT=60000
```

### Lấy Khóa API OpenAI
1. Truy cập https://platform.openai.com/account/api-keys
2. Nhấp "Tạo khóa bí mật mới"
3. Sao chép khóa vào `.env`

### Đặt Giới Hạn Hóa Đơn Trên OpenAI
1. Truy cập https://platform.openai.com/account/billing/limits
2. Đặt "Giới hạn cứng" thành $5-10
3. Bật cảnh báo email

### Kiểm Tra Backend
```bash
npm run dev
# Nên thấy: 🚀 Máy chủ chạy trên cổng 5000
# ✅ Dịch vụ AI: BẬT
```

## 🎨 Bước 3: Cài Đặt Frontend

### Cài Đặt Phụ Thuộc
```bash
cd frontend
npm install
```

### Tạo Tệp .env (Tùy Chọn)
```bash
cp .env.example .env
# Chỉnh sửa nếu cần, mặc định là http://localhost:5000
```

### Khởi Động Frontend
```bash
npm start
# Nên mở http://localhost:3000
```

## ✅ Xác Minh Cài Đặt

### Kiểm Tra Điểm Cuối Sức Khỏe
```bash
curl http://localhost:5000/health

# Phản hồi nên là:
# {"status":"ok","timestamp":"...","ai_enabled":true}
```

### Kiểm Tra Tạo Tài Khoản
1. Mở http://localhost:3000
2. Nhấp "Đăng nhập"
3. Nhấp "Tạo tài khoản mới"
4. Điền biểu mẫu và gửi

### Kiểm Tra Tạo AI
1. Đăng nhập bằng tài khoản của bạn
2. Nhấp nút "✍️ Tạo Truyện"
3. Điền thông tin truyện
4. Nhấp "✍️ Tạo Truyện"
5. Thêm mô tả chương và nhấp "🎨 Tạo Chương Với AI"
6. Nhấp xác nhận
7. Đợi AI tạo (5-10 giây)
8. Xem chương được tạo!

## 🚀 Triển Khai Sản Xuất

### Triển Khai Backend (Ví Dụ Heroku)
```bash
# Tạo ứng dụng Heroku
heroku create tên-ứng-dụng-của-bạn

# Đặt biến môi trường
heroku config:set OPENAI_API_KEY=sk-...
heroku config:set DB_USER=...
heroku config:set JWT_SECRET=...
# (đặt tất cả các biến .env khác)

# Triển Khai
git push heroku main
```

### Triển Khai Frontend (Ví Dụ Netlify/Vercel)
```bash
# Xây dựng gói sản xuất
npm run build

# Triển khai thư mục xây dựng
```

## 📊 Xác Minh Cơ Sở Dữ Liệu

### Kiểm Tra Các Bảng AI Được Tạo
```bash
psql -U postgres -d web_doc_truyen

# Liệt kê tất cả các bảng
\dt

# Kiểm tra bảng ai_logs
SELECT * FROM ai_logs LIMIT 5;

# Kiểm tra bảng user_ai_usage
SELECT * FROM user_ai_usage LIMIT 5;

# Thoát
\q
```

## 🛠️ Khắc Phục Sự Cố

### Sự Cố: "Dịch Vụ AI: TẮT"
**Giải Pháp:** Kiểm tra OPENAI_API_KEY trong `.env`
```bash
grep OPENAI_API_KEY backend/.env
```

### Sự Cố: Lỗi Kết Nối Cơ Sở Dữ Liệu
**Giải Pháp:** Xác minh PostgreSQL đang chạy và cơ sở dữ liệu tồn tại
```bash
psql -U postgres -d web_doc_truyen -c "SELECT 1"
```

### Sự Cố: Cổng 5000 Đang Được Sử Dụng
**Giải Pháp:** Sử dụng cổng khác
```bash
PORT=5001 npm run dev
```

### Sự Cố: Hết Thời Gian Tạo AI
**Giải Pháp:** Kiểm tra khóa API OpenAI có hợp lệ không
```bash
curl https://api.openai.com/v1/models \
  -H "Authorization: Bearer KHÓA_OPENAI_CỦA_BẠN"
```

## 📚 Lệnh Hữu Dụng

### Backend
```bash
# Phát Triển
npm run dev

# Kiểm Tra API
npm test

# Kiểm Tra Lỗi
npm run lint
```

### Cơ Sở Dữ Liệu
```bash
# Sao Lưu Cơ Sở Dữ Liệu
pg_dump -U postgres web_doc_truyen > backup.sql

# Khôi Phục Cơ Sở Dữ Liệu
psql -U postgres -d web_doc_truyen < backup.sql

# Xem Nhật Ký
psql -U postgres -d web_doc_truyen \
  -c "SELECT * FROM ai_logs ORDER BY created_at DESC LIMIT 10;"
```

## 💡 Mẹo & Thực Hành Tốt Nhất

### Phát Triển
- Sử dụng `.env.example` làm mẫu
- Không bao giờ cam kết `.env` vào git
- Kiểm tra các điểm cuối API bằng curl hoặc Postman
- Kiểm tra nhật ký cơ sở dữ liệu để tìm lỗi

### Quản Lý Chi Phí
- Giám sát sử dụng AI hàng ngày
- Đặt cảnh báo hóa đơn trên OpenAI
- Giữ giới hạn cứng được bật
- Trước tiên kiểm tra bằng nhắc nhỏ

### Bảo Mật
- Thay đổi JWT_SECRET trong sản xuất
- Sử dụng bí mật môi trường, không có giá trị được hardcode
- Xoay khóa API thường xuyên
- Bật HTTPS trong sản xuất

## 📖 Tài Nguyên Bổ Sung

- **Hướng Dẫn Kiểm Soát Chi Phí AI**: [docs/AI_COST_CONTROL.md](docs/AI_COST_CONTROL.md)
- **Tài Liệu API**: [docs/API.md](docs/API.md)
- **Tài Liệu Tính Năng**: [docs/FEATURES.md](docs/FEATURES.md)
- **Hướng Dẫn Thu Thập Web**: [docs/WEB_SCRAPING.md](docs/WEB_SCRAPING.md)

## 🤝 Hỗ Trợ

Để có các câu hỏi hoặc vấn đề:
1. Kiểm tra phần khắc phục sự cố ở trên
2. Xem lại các tệp tài liệu
3. Kiểm tra nhật ký backend: `npm run dev`
4. Giám sát bảng điều khiển frontend: DevTools (F12)

---

**Hạnh Phúc Mã Hóa! 🚀**

Bước tiếp theo sau khi cài đặt:
- [ ] Thành công tải lên người dùng ✅
- [ ] Tạo một truyện thử nghiệm
- [ ] Tạo chương AI
- [ ] Giám sát sử dụng AI
- [ ] Thiết lập triển khai sản xuất
