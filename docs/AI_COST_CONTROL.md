# Tạo Truyện Với AI - Hướng Dẫn Kiểm Soát Chi Phí

**Mục tiêu:** Tạo truyện với sự trợ giúp của AI mà không phải lo lắng về chi phí API quá cao.

## 🎯 Kiến Trúc Chi Phí Kiểm Soát

Hệ thống được thiết kế với **3 lớp bảo vệ** để ngăn chặn chi phí API:

### 1. **Frontend Protection** (Giao diện người dùng)
```javascript
// Nút tạo bị vô hiệu hóa khi đang loading
<button disabled={loading}>Tạo Chương</button>

// Hiển thị số lượt còn lại trước khi tạo
"Còn 3/5 lượt hôm nay"

// Yêu cầu xác nhận trước mỗi yêu cầu AI
Confirm: "Dùng AI sẽ tốn 1 lượt. Tiếp tục?"
```

### 2. **Backend Protection** (Rate Limiting + API Limits)
```javascript
// Rate Limiter: 3 requests/phút
const generateChapterLimiter = rateLimit({
  windowMs: 60 * 1000,
  max: 3
});

// Daily Limits Middleware
if (usage_today >= daily_limit) {
  return 429 "Hết lượt hôm nay, thử lại vào ngày mai"
}
```

### 3. **Database Protection** (Tracking & Logging)
```sql
-- Mỗi lần gọi API được log
INSERT INTO ai_logs (
  user_id, tokens_used, cost, status
) VALUES (...)

-- Theo dõi usage hàng ngày
UPDATE user_ai_usage SET
  usage_count = usage_count + 1,
  total_tokens = total_tokens + 145,
  total_cost = total_cost + 0.001
```

## 📊 Mô Hình Giá

**Model:** GPT-4o-mini (Rẻ nhất của OpenAI)

| Loại Token | Giá |
|-----------|-----|
| Input | $0.00015 / 1000 tokens |
| Output | $0.0006 / 1000 tokens |

**Chi phí trung bình per chương:**
- Prompt: ~150 tokens → ~$0.0000225
- Output: ~400 tokens → ~$0.00024
- **Tổng: ~$0.00026 (~250 VND) per chương**

## 👤 User Tiers

### Free User
```
- 5 lượt/ngày
- Mô hình: gpt-4o-mini
- Max 500 tokens output/chương
```

### Premium User
```
- 50 lượt/ngày (10x)
- Mô hình: gpt-4o-mini (same)
- Max 500 tokens output/chương
```

## 🛡️ Thiết Lập Bảo Mật OpenAI

### 1. **Đặt Hard Billing Limit**
- Đăng nhập OpenAI Dashboard
- Settings → Billing → Usage limits
- Đặt **$5-10** làm hard limit
- Nếu vượt, API tự động bị chặn

### 2. **Enable Usage Alerts**
- Settings → Billing → Email notifications
- Nhận email khi chi phí đạt 50%, 80%, 100%

### 3. **API Key Rotation**
```bash
# Tạo key mới định kỳ (3 tháng 1 lần)
# Xóa key cũ sau khi migration
```

## 📋 Cách Sử Dụng

### Tạo Truyện Mới
```
1. Truy cập /create
2. Nhập thông tin truyện (tiêu đề, mô tả, genre, nhân vật, v.v.)
3. Nhấn "Tạo Truyện"
```

### Tạo Chương Đầu Tiên
```
1. Mô tả nội dung chương muốn tạo
2. Hệ thống sẽ hiển thị: "Còn 5/5 lượt hôm nay"
3. Nhấn "Tạo Chương Với AI"
4. Xác nhận popup: "Dùng AI sẽ tốn 1 lượt"
5. Chờ 5-10 giây để AI tạo
6. Xem chương được tạo
```

### Xem Thống Kê AI
```
GET /api/users/ai-stats
- Response: {
    today: { usage_count, total_tokens, total_cost },
    all_time: { usage_count, total_tokens, total_cost },
    remaining_today: 3
  }
```

## 🔧 Environment Variables

Tạo file `.env` trong thư mục `backend`:

```bash
# OpenAI API
OPENAI_API_KEY=sk-your-key-here
OPENAI_MODEL=gpt-4o-mini
OPENAI_MAX_TOKENS=500

# Daily Limits
AI_DAILY_LIMIT=5              # Free users
AI_PREMIUM_DAILY_LIMIT=50     # Premium users
AI_REQUEST_TIMEOUT=60000      # Timeout 60 seconds
```

## 📈 Monitoring

### View Usage Logs
```sql
SELECT * FROM ai_logs ORDER BY created_at DESC LIMIT 20;
```

### Check Today's Usage
```sql
SELECT user_id, usage_count, total_cost 
FROM user_ai_usage 
WHERE date = CURRENT_DATE
ORDER BY total_cost DESC;
```

### Monthly Cost Analysis
```sql
SELECT 
  DATE_TRUNC('month', created_at) as month,
  COUNT(*) as total_calls,
  SUM(tokens_used) as total_tokens,
  SUM(cost) as total_cost
FROM ai_logs
GROUP BY DATE_TRUNC('month', created_at);
```

## 🚨 Error Handling

### Lỗi Thường Gặp & Giải Pháp

| Lỗi | Nguyên Nhân | Giải Pháp |
|------|-----------|---------|
| `429 Too Many Requests` | Vượt rate limit | Đợi 1 phút |
| `Daily limit exceeded` | Hết lượt hôm nay | Nâng cấp Premium hoặc đợi ngày mai |
| `Invalid API key` | OPENAI_API_KEY sai/hết hạn | Kiểm tra .env, generate key mới |
| `Timeout after 60s` | AI phát sinh quá lâu | Đơn giản prompt hoặc thử lại |

## 💡 Best Practices

### 1. **Viết Prompt Tốt**
```
❌ Xấu: "Viết chương"
✅ Tốt: "Viết chương 3 - Nhân vật chính gặp yêu tinh lần đầu. Format: mô tả bối cảnh (2 câu) → thoại → hành động. Độ dài 300-400 từ."
```

### 2. **Tối Ưu Chi Phí**
- Dùng template prompt sẵn thay vì custom mỗi lần
- Giới hạn output tokens (500 là đủ cho chương)
- Sử dụng batch processing nếu có nhiều chương

### 3. **Kiểm Tra Trước**
```python
# Trước khi production
- Test với 5-10 chapters đầu
- Tính chi phí (tokens_used × rate)
- Đảm bảo cost < hard limit
```

## 📞 Troubleshooting

### AI Service Không Hoạt Động?

1. **Check .env file**
   ```bash
   # Verify OPENAI_API_KEY exists
   grep OPENAI_API_KEY backend/.env
   ```

2. **Check API Key Valid**
   ```bash
   curl https://api.openai.com/v1/models \
     -H "Authorization: Bearer YOUR_API_KEY"
   ```

3. **Check Server Logs**
   ```bash
   # Terminal backend
   npm run dev
   # Xem logs có error không
   ```

4. **Check Database**
   ```sql
   SELECT * FROM ai_logs ORDER BY created_at DESC LIMIT 5;
   -- Xem có log được ghi không
   ```

## 🎓 Ví Dụ Chi Phí Thực Tế

**Scenario: Tạo 1 cuốn truyện 30 chương**

```
Prompt per chương: ~150 tokens = $0.0000225
Output per chương: ~400 tokens = $0.00024
Cost per chương: ~$0.00026

Tổng 30 chương: 30 × $0.00026 = $0.0078 (~78,000 VND)
```

**Với Free User (5 lượt/ngày):**
- Mất 6 ngày để tạo 30 chương
- Tổng chi phí: ~78,000 VND (rất rẻ!)

**Với Premium User (50 lượt/ngày):**
- Mất 1 ngày để tạo 30 chương
- Tổng chi phí: vẫn ~78,000 VND
- Nhưng có thêm features khác...

## 🎉 Kết Luận

Hệ thống AI story generation được thiết kế để:
1. ✅ **An toàn chi phí** - Multiple layers of protection
2. ✅ **Minh bạch** - Tracking and logging đầy đủ
3. ✅ **Dễ sử dụng** - Simple UI with confirmation dialogs
4. ✅ **Mở rộng được** - Can add more models, tiers later

Happy writing! 📝✨
