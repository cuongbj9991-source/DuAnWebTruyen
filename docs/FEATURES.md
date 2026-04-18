# Các Tính Năng Mới - Advanced Filtering & Sorting

## 🎯 Tổng Quan

Web Đọc Truyện hiện tại hỗ trợ tìm kiếm, lọc, và sắp xếp nâng cao giống như **Falool.com**, cho phép người dùng dễ dàng tìm kiếm truyện theo nhiều tiêu chí khác nhau.

## 🔍 Chức Năng Tìm Kiếm

### 1. Tìm Từ Khóa
- **Tìm kiếm theo tiêu đề**: Tìm kiếm theo tên truyện
- **Tìm kiếm theo tên hán việt/tên khác**: Tìm kiếm theo tên thay thế của truyện
- **Tìm kiếm theo tác giả**: Tìm kiếm theo tên tác giả

Ví dụ:
```
URL: /stories?search=kiếm khách
Kết quả: Tất cả truyện có "kiếm khách" trong tiêu đề, tên hán việt, hoặc tác giả
```

### 2. Tìm Kiếm Tóm Tắt
- Tìm kiếm thông tin chi tiết trong phần tóm tắt của truyện

Ví dụ:
```
URL: /stories?search_summary=hiệp sĩ kiếm
Kết quả: Tất cả truyện có "hiệp sĩ kiếm" trong tóm tắt
```

## 📊 Bộ Lọc Koooooo

### 1. Lọc Theo Số Chương
Cho phép lọc truyện theo số lượng chương:
- `> 50` - Trên 50 chương
- `> 100` - Trên 100 chương
- `> 200` - Trên 200 chương
- `> 500` - Trên 500 chương
- `> 1000` - Trên 1000 chương
- `> 1500` - Trên 1500 chương
- `> 2000` - Trên 2000 chương

Ví dụ:
```bash
GET /stories?min_chapters=500
```

### 2. Lọc Theo Loại Truyện
- **📖 Sáng tác** - Truyện được tác giả sáng tác
- **🌍 Dịch** - Truyện được dịch từ các nước khác
- **🤖 Txt dịch tự động** - Truyện dịch tự động từ nguồn
- **📸 Scan ảnh** - Truyện dưới dạng ảnh quét

Hỗ trợ chọn nhiều loại:
```bash
GET /stories?story_type=sáng tác,dịch
```

### 3. Lọc Theo Thể Loại
Các thể loại chính:
- 🔮 Huyền huyễn
- 🏙️ Đô thị
- 💕 Ngôn tình
- 🎮 Võng du
- 🚀 Khoa học viễn tưởng
- 📜 Lịch sử
- 👥 Đông nhân
- ✨ Dị năng
- 👻 Linh dị
- 📚 Light Novel

Hỗ trợ chọn nhiều thể loại:
```bash
GET /stories?genre=huyền huyễn,ngôn tình
```

### 4. Lọc Theo Trạng Thái
- 📖 **Còn tiếp** (`ongoing`) - Truyện đang được cập nhật
- ✅ **Hoàn thành** (`completed`) - Truyện đã hoàn thành
- ⏸️ **Tạm ngưng** (`paused`) - Truyện tạm ngưng cập nhật

Hỗ trợ chọn nhiều trạng thái:
```bash
GET /stories?status=ongoing,completed
```

### 5. Lọc Theo Nguồn
Lọc truyện từ các nguồn khác nhau:
- 📌 **Original** - Truyện gốc
- 🌐 **Falool** - Từ Falool.com
- 📘 **Qidian** - Từ Qidian.com
- 🎭 **Fanqie** - Từ Fanqie.com
- Và các nguồn khác...

Hỗ trợ chọn nhiều nguồn:
```bash
GET /stories?source=falool,qidian,fanqie
```

### 6. Lọc Theo Đánh Giá
- Lọc truyện có đánh giá tối thiểu từ 0-5 sao

Ví dụ:
```bash
GET /stories?min_rating=4.0
```

## ⬆️ Tính Năng Sắp Xếp

### Các Tùy Chọn Sắp Xếp

1. **📊 Lượt Đọc Tổng** (`views_total`) - Mặc định
   - Sắp xếp theo tổng số lượt xem của truyện

2. **📈 Lượt Đọc Tuần** (`views_week`)
   - Sắp xếp theo lượt xem trong tuần gần nhất

3. **🔥 Lượt Đọc Ngày** (`views_day`)
   - Sắp xếp theo lượt xem hôm nay

4. **🆕 Mới Cập Nhật** (`updated`)
   - Sắp xếp theo chương cập nhật gần nhất

5. **🎉 Mới Nhập Kho** (`newest`)
   - Sắp xếp theo truyện mới được thêm vào

6. **❤️ Lượt Thích** (`likes`)
   - Sắp xếp theo số lượng thích

7. **📌 Lượt Theo Dõi** (`follows`)
   - Sắp xếp theo số lượng người theo dõi

8. **🔖 Lượt Đánh Dấu** (`bookmarks`)
   - Sắp xếp theo số lượt lưu bookmark

9. **⭐ Đánh Giá** (`rating`)
   - Sắp xếp theo điểm đánh giá

Ví dụ:
```bash
GET /stories?sortBy=views_week
GET /stories?sortBy=updated&genre=huyền huyễn
```

## 🔗 Kết Hợp Các Bộ Lọc

Có thể kết hợp nhiều bộ lọc và tùy chọn sắp xếp:

```bash
# Truyện huyền huyễn, sáng tác, hoàn thành, trên 200 chương, sắp xếp theo đánh giá
GET /stories?genre=huyền huyễn&story_type=sáng tác&status=completed&min_chapters=200&sortBy=rating

# Truyện dịch từ Falool, ngôn tình, đang cập nhật, sắp xếp theo lượt xem tuần
GET /stories?source=falool&story_type=dịch&genre=ngôn tình&status=ongoing&sortBy=views_week

# Tìm kiếm truyện về "võng du", từ nhiều nguồn, sắp xếp theo mới cập nhật
GET /stories?search=võng du&source=original,falool,qidian&sortBy=updated
```

## 💾 Các Thống Kê & Dữ Liệu

Mỗi truyện hiện cung cấp các thông tin chi tiết:

```json
{
  "id": 1,
  "title": "Tiêu đề truyện",
  "title_alternative": "Tên hán việt",
  "author": "Tác giả",
  "genre": "Thể loại chính",
  "story_type": "Loại truyện",
  "status": "Trạng thái",
  "total_chapters": 250,
  "views_total": 15000,        // Tổng lượt xem
  "views_week": 3500,          // Lượt xem tuần này
  "views_day": 800,            // Lượt xem hôm nay
  "likes": 1200,               // Số lượng thích
  "follows": 850,              // Số lượng theo dõi
  "bookmarks": 500,            // Số lượng đánh dấu
  "rating": 4.5,               // Điểm đánh giá
  "rating_count": 320          // Số lượng người đánh giá
}
```

## 🛠️ API Endpoints

### Lấy Tất Cả Tùy Chọn Bộ Lọc

```bash
GET /api/stories/api/filter-options
```

Response:
```json
{
  "genres": ["Huyền huyễn", "Đô thị", ...],
  "authors": ["Tác giả 1", ...],
  "sources": ["original", "falool", ...],
  "story_types": ["sáng tác", "dịch", ...],
  "statuses": ["ongoing", "completed", "paused"],
  "sort_options": [...]
}
```

## 🎨 Frontend Component

### FilterPanel Component
Thành phần giao diện cho phép người dùng:
- Tìm kiếm bằng từ khóa
- Mở rộng/sập gọn các bộ lọc
- Chọn nhiều lựa chọn
- Xóa tất cả bộ lọc

### StoryList Component
Hiển thị:
- Danh sách truyện dạng grid
- Thông tin tóm tắt cho mỗi truyện
- Lượt xem, đánh giá, số chương
- Hỗ trợ phân trang (Load More)
- Anipausemations khi hover

## 📱 Responsive Design

Tất cả bộ lọc và giao diện hoàn toàn responsive:
- Desktop (1024px+) - Sidebar bên trái + nội dung bên phải
- Tablet (768px-1024px) - Bộ lọc và nội dung xếp chồng
- Mobile (<768px) - Full width, bộ lọc ở trên cùng

## 🚀 Các Cải Tiến Hiệu Suất

1. **Indexed Queries** - Database indexes trên các trường lọc chính
2. **Pagination** - Tải từng trang truyện thay vì tất cả
3. **Lazy Loading** - Hình ảnh tải khi cần
4. **Caching** - Cache các tùy chọn bộ lọc

## 📋 Ví Dụ Sử Dụng

### JavaScript/React
```javascript
import { storyService } from '../services/api';

// Tìm kiếm truyện huyền huyễn hoàn thành
const stories = await storyService.getAllStories({
  genre: 'huyền huyễn',
  status: 'completed',
  sortBy: 'rating',
  limit: 20,
  offset: 0
});

// Tìm kiếm và lọc theo nhiều tiêu chí
const results = await storyService.getAllStories({
  search: 'võng du',
  source: 'falool,qidian',
  min_chapters: 100,
  genre: 'huyền huyễn,ngôn tình',
  sortBy: 'views_week'
});
```

### cURL
```bash
# Truyện huyền huyễn hoàn thành
curl "http://localhost:5000/api/stories?genre=huyền huyễn&status=completed"

# Tìm kiếm với nhiều bộ lọc
curl "http://localhost:5000/api/stories?search=kiếm&min_chapters=100&sortBy=views_week"

# Lọc theo nhiều thể loại
curl "http://localhost:5000/api/stories?genre=huyền huyễn,ngôn tình&status=ongoing,completed"
```

## 🔮 Các Tính Năng Sắp Tới

- ✅ Lọc theo tác giả
- ✅ Tìm kiếm nâng cao (AND/OR logic)
- ✅ Lưu các tìm kiếm yêu thích
- ✅ Gợi ý dựa trên lịch sử xem
- ✅ Thêm truyện từ scrapers (Falool, Qidian, etc.)
