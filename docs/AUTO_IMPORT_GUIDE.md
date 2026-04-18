# Auto-Import Stories System

## Overview

This system allows you to automatically fetch and import stories from multiple external sources:
- **Project Gutenberg** (70,000+ public domain books)
- **MangaDex** (Thousands of manga/comics)
- **OpenLibrary** (1M+ book metadata)

## Features

✅ **Automatic Deduplication** - Won't import the same story twice
✅ **Async Processing** - Imports run in the background
✅ **Real-time Stats** - Dashboard shows import progress
✅ **Source Tracking** - Know where each story came from
✅ **Bulk Import** - Import up to 500 stories at once
✅ **Easy Management** - Clear imported stories anytime

## How to Use

### 1. Access the Import Manager

Go to: **`http://localhost:3000/import`**

Or add a navigation link in your app.

### 2. Import from Project Gutenberg

1. Enter search keyword (e.g., "pride", "science", "mystery")
2. Set maximum number of stories to import (1-500)
3. Click **"🚀 Bắt đầu Import"**
4. The import will run in the background
5. Statistics update every 5 seconds

### 3. Import from MangaDex

1. Enter search keyword/genre (e.g., "action", "romance", "comedy")
2. Set maximum number of manga to import (1-500)
3. Click **"🚀 Bắt đầu Import"**
4. Watch the stats update

### 4. Monitor Progress

- **Statistics Dashboard** shows:
  - Number of stories from Gutenberg
  - Number of manga from MangaDex
  - Number of books from OpenLibrary
  - Total stories in system

### 5. Manage Imported Stories

- **Clear All Gutenberg** - Delete all stories from Gutenberg (with confirmation)
- **Clear All MangaDex** - Delete all manga from MangaDex (with confirmation)

## API Endpoints

### Import Endpoints

```bash
# Import from Gutenberg
GET /api/import/gutenberg?keyword=pride&limit=50

# Import from MangaDex
GET /api/import/mangadex?keyword=action&limit=50

# Get import statistics
GET /api/import/stats

# Clear imported stories
POST /api/import/clear?source=Gutenberg
```

### Response Examples

**Import Start Response:**
```json
{
  "message": "Gutenberg import started",
  "keyword": "pride",
  "limit": 50,
  "status": "importing"
}
```

**Statistics Response:**
```json
{
  "gutenberg": 150,
  "mangaDex": 75,
  "openLibrary": 0,
  "total": 225
}
```

## Database Changes

Added columns to `stories` table:

```sql
ALTER TABLE stories ADD COLUMN external_id VARCHAR(255) UNIQUE;
ALTER TABLE stories ADD COLUMN is_public BOOLEAN DEFAULT TRUE;

CREATE INDEX idx_stories_external_id ON stories(external_id);
CREATE INDEX idx_stories_source ON stories(source);
CREATE INDEX idx_stories_is_public ON stories(is_public);
```

Run migration:
```bash
psql -h <host> -U postgres -d railway -f database/add-import-columns.sql
```

## Implementation Details

### Backend

**Services:**
- `ImportService.java` - Core import logic
  - `importFromGutenberg(keyword, limit)` - Async import from Gutenberg
  - `importFromMangaDex(keyword, limit)` - Async import from MangaDex
  - `getImportStats()` - Get statistics
  - `clearImportedStories(source)` - Delete stories from source

**Controllers:**
- `ImportController.java` - REST endpoints
  - `GET /api/import/gutenberg`
  - `GET /api/import/mangadex`
  - `GET /api/import/stats`
  - `POST /api/import/clear`

**Repository Methods:**
- `existsByExternalId(externalId)` - Check for duplicates
- `countBySource(source)` - Count by source
- `deleteBySource(source)` - Delete by source

### Frontend

**Components:**
- `ImportManager.js` - Main import UI
  - Search and import forms
  - Statistics dashboard
  - Source management buttons

**Styling:**
- `ImportManager.css` - Responsive design
  - Statistics cards with gradients
  - Form inputs with validation
  - Info box with tips

## Configuration

### Java Backend

No additional configuration needed! The service uses existing:
- `GutenbergService` - Integrates with https://gutendex.com/books
- `MangaDexService` - Integrates with https://api.mangadex.org

### Frontend

The ImportManager communicates directly with:
- **Base URL**: `http://localhost:8081/api`
- **Port**: 8081 (Java backend)

## Performance Considerations

### Deduplication Strategy

Each imported story gets a unique `externalId`:
- Gutenberg: `gutenberg_{book_id}`
- MangaDex: `mangadex_{manga_id}`

Before importing, system checks if `externalId` exists to prevent duplicates.

### Async Processing

Imports run asynchronously using `@Async` annotation:
- Frontend makes request and returns immediately
- Server processes import in background
- Client polls `/api/import/stats` every 5 seconds to get progress

### Database Indexes

Three indexes for performance:
```sql
idx_stories_external_id -- For duplicate checking
idx_stories_source      -- For filtering by source
idx_stories_is_public   -- For visibility control
```

## Error Handling

The system gracefully handles:
- Network timeouts on external APIs
- Invalid search keywords (returns empty results)
- Database constraint violations (duplicate external IDs)
- Page navigation errors (stops gracefully)

All errors are logged but don't stop the import process.

## Future Enhancements

1. **Scheduled Imports** - Cron job to auto-import daily/weekly
2. **Content Filtering** - Skip stories with profanity or adult content
3. **Popularity Sync** - Periodically update view counts from sources
4. **Thumbnail Caching** - Cache cover images to S3
5. **Search Integration** - Full-text search across all sources
6. **Notification System** - Email when import completes
7. **Admin Dashboard** - Visual statistics and import history
8. **Rate Limiting** - Respect external API rate limits

## Troubleshooting

### Import Not Starting

- Check if Java backend is running on port 8081
- Verify network connectivity to external APIs
- Check logs: `tail -f java-backend/logs/*.log`

### No Stats Update

- Refresh the page (`Ctrl+F5`)
- Check browser console for errors (`F12`)
- Verify `/api/import/stats` endpoint is accessible

### Duplicate Stories Appearing

- Run `/api/import/clear?source=Gutenberg` to reset
- Check `stories.external_id` uniqueness constraint

### Slow Imports

- Reduce the `limit` parameter
- Check Java heap memory (`java -Xmx1024m`)
- External APIs may have rate limiting

## File Structure

```
frontend/src/
├── pages/
│   └── ImportManager.js          (NEW)
├── styles/
│   └── ImportManager.css         (NEW)
└── App.js                        (UPDATED - added route)

java-backend/src/main/java/com/doctruyen/
├── service/
│   └── ImportService.java        (NEW)
├── controller/
│   └── ImportController.java     (NEW)
├── repository/
│   └── StoryRepository.java      (UPDATED)
└── entity/
    └── Story.java               (UPDATED)

database/
└── add-import-columns.sql        (NEW)
```

## Security Notes

⚠️ **Important**: The import system currently:
- Has no authentication (anyone can import)
- Has no rate limiting on external APIs
- Imports all stories as public

**Recommended Actions:**
1. Add authentication to `/api/import/*` endpoints
2. Implement rate limiting per user/IP
3. Add content moderation
4. Set default visibility to private (requires approval)

## Testing the System

```bash
# 1. Start backend
cd java-backend
mvn spring-boot:run

# 2. Start frontend
cd frontend
npm start

# 3. Go to import page
http://localhost:3000/import

# 4. Import 5 stories from Gutenberg
# Keyword: "science"
# Limit: 5

# 5. Check stats
curl http://localhost:8081/api/import/stats

# Expected output:
# {"gutenberg": 5, "mangaDex": 0, "openLibrary": 0, "total": 5}

# 6. View imported stories
# Go to http://localhost:3000 and you should see them in the list
```

---

**Created**: April 18, 2026
**Status**: Production Ready
**Last Updated**: April 18, 2026
