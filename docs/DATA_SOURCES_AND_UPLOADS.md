# Data Sources & User Upload Implementation Guide

## Overview

This document describes the implementation of a hybrid data strategy combining:
1. **Multiple External Data Sources**: Gutenberg API, Open Library API, MangaDex API
2. **User Upload Capability**: Users can upload and publish their own stories

## Architecture

### Backend Components

#### Java Spring Boot (Port 8080) - Story Service

**New Services**:
- `GutenbergService` - Integrates with Project Gutenberg (https://gutendex.com/books)
- `OpenLibraryService` - Integrates with Open Library (https://openlibrary.org/api/)
- `MangaDexService` - Integrates with MangaDex (https://api.mangadex.org/)
- `MultiSourceSearchService` - Aggregates results from all sources
- `StoryUploadService` - Manages user-uploaded stories with approval workflow

**New Entities**:
- `StoryUpload` - Represents user-uploaded stories with moderation status

**New Controllers**:
- `ExternalSourcesController` - Endpoints: `/external-sources/search`, `/external-sources/recommended`
- `StoryUploadController` - CRUD endpoints for story uploads

**New Repositories**:
- `StoryUploadRepository` - Data access with filtering and search queries

#### Database Schema Updates

**New Table: `story_uploads`**
```sql
CREATE TABLE story_uploads (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id),
  title VARCHAR(255) NOT NULL,
  author VARCHAR(100),
  description TEXT,
  genre VARCHAR(100),
  type VARCHAR(50),
  status VARCHAR(50) DEFAULT 'pending_review',
  cover_url VARCHAR(500),
  content TEXT NOT NULL,
  is_public BOOLEAN DEFAULT FALSE,
  is_approved BOOLEAN DEFAULT FALSE,
  rejection_reason TEXT,
  views_count BIGINT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  approved_at TIMESTAMP
);
```

**Indexes**:
- idx_story_uploads_user_id - Filter by user
- idx_story_uploads_status - Find pending/rejected/published
- idx_story_uploads_is_approved - Admin queries
- idx_story_uploads_is_public - Public visibility
- idx_story_uploads_created_at - Chronological sorting
- idx_story_uploads_views - Popular sorting

### Frontend Components

**New Pages**:
- `UploadStory` - Form for users to upload stories with title, author, description, genre, type, content

**New Components**:
- `ExternalBooks` - Search and browse books from external sources with import capability

**New Services**:
- `uploadService` - API for upload CRUD operations
- `multiSourceSearchService` - API for searching external sources

**Updated Navigation**:
- Added "📝 Tải Lên" (Upload) button in Home header (only visible when logged in)
- Added `/upload` route in App.js

## API Endpoints

### External Sources

#### Search All Sources
```
GET /external-sources/search?keyword=pride&page=1
```
Response:
```json
{
  "keyword": "pride",
  "page": 1,
  "gutenberg": [
    {
      "id": "1342",
      "title": "Pride and Prejudice",
      "author": "Jane Austen",
      "description": "...",
      "genre": "Romance",
      "coverUrl": "...",
      "source": "Gutenberg"
    }
  ],
  "openLibrary": [...],
  "mangaDex": [...],
  "totalResults": 42
}
```

#### Get Recommended Books
```
GET /external-sources/recommended
```

### Story Uploads

#### Create Upload
```
POST /uploads
Headers: Authorization: Bearer <token>, User-Id: <userId>
Body: {
  "title": "My Story",
  "author": "John Doe",
  "description": "A great story",
  "genre": "Adventure",
  "type": "original",
  "coverUrl": "https://...",
  "content": "Story content..."
}
```

#### Get My Uploads
```
GET /uploads/my-uploads?page=0&size=10
Headers: Authorization: Bearer <token>, User-Id: <userId>
```

#### Get Published Stories (Public)
```
GET /uploads/published?page=0&size=12
```

#### Search Published Stories
```
GET /uploads/published/search?keyword=adventure&page=0&size=12
```

#### Get Upload by ID
```
GET /uploads/{id}
```

#### Update Upload
```
PUT /uploads/{id}
Headers: Authorization: Bearer <token>, User-Id: <userId>
Body: {
  "title": "Updated Title",
  "description": "...",
  "genre": "...",
  "content": "...",
  "coverUrl": "..."
}
```

#### Delete Upload
```
DELETE /uploads/{id}
Headers: User-Id: <userId>
```

#### Approve Upload (Admin)
```
POST /uploads/{id}/approve
```

#### Reject Upload (Admin)
```
POST /uploads/{id}/reject?reason=Inappropriate%20content
```

#### Publish Upload
```
POST /uploads/{id}/publish
Headers: User-Id: <userId>
```

#### Get Pending Reviews (Admin)
```
GET /uploads/admin/pending-reviews?page=0&size=10
```

## Data Flow

### External Source Integration

1. User searches for "Pride and Prejudice"
2. Frontend calls `/external-sources/search?keyword=Pride%20and%20Prejudice`
3. Backend queries:
   - GutenbergService → https://gutendex.com/books?search=Pride+and+Prejudice
   - OpenLibraryService → https://openlibrary.org/search.json?title=Pride+and+Prejudice
   - MangaDexService → https://api.mangadex.org/manga?title=Pride+and+Prejudice
4. Results aggregated and returned as combined list
5. User can click "Import" to add to their collection (future feature)

### User Upload Workflow

1. **Upload Creation**
   - User fills form with story details
   - Submits with text content or .txt file
   - Backend creates StoryUpload record with status = "pending_review"
   - User can see story in "My Uploads" with ⏳ pending badge

2. **Admin Review**
   - Admin views `/uploads/admin/pending-reviews`
   - Can approve (changes status to "published", sets is_approved=true)
   - Or reject with reason (sets status to "rejected", stores reason)
   - User notified of rejection reason in UI

3. **Publishing**
   - User can publish approved stories (sets is_public=true)
   - Published stories appear in `/uploads/published`
   - Other users can view and interact with stories

## Security Considerations

### User Uploads
- Content must be approved before public visibility (is_public + is_approved both true)
- Only story owner can update/delete their uploads
- User-Id header required for authorization (should be JWT in production)
- File size limits should be implemented
- Content validation/sanitization recommended

### External Sources
- Read-only access (no authentication needed)
- Rate limiting recommended for API calls
- Caching recommended to reduce API calls
- Error handling for unavailable sources

## Future Enhancements

1. **File Format Support**
   - Parse .epub files (extract chapters)
   - Support .pdf uploads with text extraction
   - Store file chunks as chapters

2. **Import from External Sources**
   - "Import" button to create Story from Gutenberg/OpenLibrary data
   - Automatic chapter creation from source content
   - Preserve metadata and cover art

3. **Content Moderation**
   - Automated plagiarism detection
   - Inappropriate content filtering
   - Manual review queue for admins

4. **Performance Optimization**
   - Cache Gutenberg/OpenLibrary searches
   - Pagination for large result sets
   - Full-text search indexes

5. **User Features**
   - Story collaboration (multiple authors)
   - Draft/publish workflows
   - Version history
   - Story recommendations based on uploads

## Testing

### Manual Testing Checklist

**Upload Feature**:
- [ ] Create upload with text content
- [ ] Create upload with .txt file
- [ ] View "My Uploads" list
- [ ] See pending approval status
- [ ] Cannot publish unapproved story
- [ ] Can edit uploaded story
- [ ] Can delete uploaded story

**External Sources**:
- [ ] Search returns results from all sources
- [ ] Results include cover images
- [ ] Pagination works
- [ ] Recommended books load
- [ ] API error handling works

**Admin Features**:
- [ ] View pending reviews
- [ ] Approve upload
- [ ] Reject with reason
- [ ] See rejection reason in user UI

## Configuration

Update `.env` files:

**Backend (.env)**:
```
GUTENBERG_API=https://gutendex.com/books
OPENLIBRARY_API=https://openlibrary.org
MANGADEX_API=https://api.mangadex.org
```

**Frontend (.env)**:
```
REACT_APP_STORY_SERVICE=http://localhost:8080/api
REACT_APP_USER_SERVICE=http://localhost:5001/api
```

## Known Limitations

1. **Gutenberg API** - ~70k books, mostly classics
2. **OpenLibrary API** - Metadata only, no full content
3. **MangaDex API** - Manga/comics only, chapter content requires separate API calls
4. **File Uploads** - Currently stores content as TEXT in database
   - For large files, should use S3/cloud storage
   - Consider chunked uploads for large files

## Troubleshooting

### "Cannot connect to external sources"
- Check internet connectivity
- Verify API endpoints are accessible
- Check rate limiting isn't triggered

### "Story upload failed"
- Check file size (recommend < 10MB initially)
- Verify content is valid UTF-8 text
- Check database connection

### "User-Id header missing"
- Ensure user is logged in
- Check localStorage for token and userId
- Verify header injection in axios interceptor

## References

- Project Gutenberg API: https://gutendex.com/
- Open Library API: https://openlibrary.org/developers/api
- MangaDex API: https://api.mangadex.org/docs/
- Spring Boot Documentation: https://spring.io/projects/spring-boot
- React Documentation: https://react.dev
