# Hướng Dẫn Thu Thập Web

## Scraper Falool

### Cấu Trúc Trang Falool

Falool.com có cấu trúc HTML như sau:
```html
<h1 class="story-title">Tiêu đề truyện</h1>
<span class="story-author">Tác giả</span>
<div class="story-description">Mô tả</div>
<img class="story-cover" src="..." />
<div class="chapter-list">
  <li><a href="...">Chương 1</a></li>
</div>
```

### Sử Dụng Scraper

```javascript
const scraperService = require('../services/scraperService');

// Lấy thông tin truyện từ Falool
const storyData = await scraperService.scrapeFromFalool('https://falool.com/story/...');

// Lấy nội dung chương
const chapterContent = await scraperService.scrapeChapterContent('https://falool.com/chapter/...');
```

## Thêm Scraper Mới

### Step 1: Phân tích HTML của trang
Sử dụng browser DevTools để xem cấu trúc HTML của trang bạn muốn scrape.

### Step 2: Tạo method trong ScraperService
```javascript
static async scrapeFromNewSite(storyUrl) {
  try {
    const response = await axios.get(storyUrl, { /* options */ });
    const $ = cheerio.load(response.data);
    
    // Sử dụng CSS selectors để trích xuất dữ liệu
    const title = $('selector-for-title').text().trim();
    // ... trích xuất các trường khác
    
    return { title, author, ... };
  } catch (error) {
    console.error('Error:', error.message);
    throw new Error('Failed to scrape');
  }
}
```

### Step 3: Thêm route
```javascript
router.post('/scrape/:source', verifyToken, async (req, res) => {
  try {
    const { url } = req.body;
    const scraperService = require('../services/scraperService');
    
    let storyData;
    switch (req.params.source) {
      case 'falool':
        storyData = await scraperService.scrapeFromFalool(url);
        break;
      case 'newsource':
        storyData = await scraperService.scrapeFromNewSite(url);
        break;
      default:
        return res.status(400).json({ error: 'Unknown source' });
    }
    
    // Lưu vào database
    const story = await StoryModel.create(storyData);
    res.json(story);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});
```

## Best Practices

1. **Respect robots.txt** - Kiểm tra file robots.txt của trang trước khi scrape
2. **Add delays** - Thêm delay giữa các requests để không làm quá tải server
3. **Handle errors** - Luôn có error handling cho connection timeouts
4. **User-Agent** - Đặt User-Agent header để giả lập browser
5. **Cache results** - Cache dữ liệu để giảm số lần scrape
6. **Update selectors** - Kiểm tra regularly vì HTML có thể thay đổi

## Rate Limiting

Thêm rate limiting để tránh bị block:

```javascript
const Bottleneck = require('bottleneck');

const limiter = new Bottleneck({
  minTime: 1000, // 1 second between requests
  maxConcurrent: 1
});

const scrape = limiter.wrap(async (url) => {
  return await scraperService.scrapeFromFalool(url);
});
```

## Debugging

Khi gặp vấn đề scraping:

1. Kiểm tra HTML response:
```javascript
console.log(response.data); // Print HTML
```

2. Kiểm tra CSS selectors:
```javascript
console.log($('h1').text()); // Test selector
```

3. Kiểm tra headers:
```javascript
const headers = response.headers;
console.log(headers);
```

4. Kiểm tra rate limiting:
```javascript
// Server có thể trả về 429 (Too Many Requests)
```
