# Quick Start Guide - Data Sources & User Uploads

This guide will help you set up and test the new external data source integrations and user upload features.

## Prerequisites

- Java 17+ installed
- Maven installed
- PostgreSQL 12+ running
- Node.js 16+ and npm installed
- .NET 8 SDK (for user service)
- All three services configured

## Step-by-Step Setup

### 1. Database Setup

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE doctruyen;
\c doctruyen

# Run schema
\i database/schema.sql

# Optional: Run seed script with sample Gutenberg stories
\i database/seed-external-sources.sql

\q
```

### 2. Java Backend (Story Service)

```bash
cd java-backend

# Copy and configure environment
cp .env.example .env
# Edit .env with your database credentials

# Build and run
mvn clean install
mvn spring-boot:run

# Should see:
# [main] Started StoryServiceApplication in 5.234 seconds
# Server running on http://localhost:8080
```

### 3. .NET Backend (User Service)

```bash
cd dotnet-backend

# Build
dotnet build

# Run
dotnet run

# Should see:
# info: Microsoft.Hosting.Lifetime
# Now listening on: http://localhost:5001
```

### 4. React Frontend

```bash
cd frontend

# Install dependencies if needed
npm install

# Start development server
npm start

# Should open http://localhost:3000 in browser
```

## Testing External Data Sources

### Test 1: Search Gutenberg Books

```bash
# Search for "Pride and Prejudice"
curl -X GET "http://localhost:8080/external-sources/search?keyword=pride&page=1"

# Expected response:
# {
#   "keyword": "pride",
#   "page": 1,
#   "gutenberg": [
#     {
#       "id": "1342",
#       "title": "Pride and Prejudice",
#       "author": "Jane Austen",
#       ...
#     }
#   ],
#   "openLibrary": [...],
#   "mangaDex": [...],
#   "totalResults": 42
# }
```

### Test 2: Get Recommended Books

```bash
curl -X GET "http://localhost:8080/external-sources/recommended"

# Should return curated list from Gutenberg
```

### Test 3: Browser Testing

1. Open http://localhost:3000
2. Look for books section (to be added in UI in future)
3. Should see integration working if databases are properly seeded

## Testing User Story Uploads

### Test 1: Register & Login

```bash
# 1. Register user
curl -X POST "http://localhost:5001/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test@1234"
  }'

# Expected response includes JWT token

# 2. Login
curl -X POST "http://localhost:5001/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test@1234"
  }'

# Save the returned token
export TOKEN="<jwt-token-from-response>"
export USER_ID="<user-id-from-response>"
```

### Test 2: Create Story Upload

```bash
curl -X POST "http://localhost:8080/uploads" \
  -H "Authorization: Bearer $TOKEN" \
  -H "User-Id: $USER_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My Amazing Story",
    "author": "John Doe",
    "description": "An incredible adventure",
    "genre": "Adventure",
    "type": "original",
    "coverUrl": "https://...",
    "content": "Chapter 1: The Beginning\n\nOnce upon a time..."
  }'

# Should return:
# {
#   "id": 1,
#   "userId": 123,
#   "title": "My Amazing Story",
#   "status": "pending_review",
#   "isApproved": false,
#   "isPublic": false,
#   ...
# }
```

### Test 3: View My Uploads

```bash
curl -X GET "http://localhost:8080/uploads/my-uploads?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" \
  -H "User-Id: $USER_ID"

# Should return array with your upload
# Status should be "pending_review"
```

### Test 4: Admin Approve Upload

```bash
# As admin, approve the upload (story ID = 1 from previous response)
curl -X POST "http://localhost:8080/uploads/1/approve" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Should return story with:
# "status": "published"
# "isApproved": true
# "approvedAt": "2024-12-XX..."
```

### Test 5: User Publishes Story

```bash
curl -X POST "http://localhost:8080/uploads/1/publish" \
  -H "User-Id: $USER_ID"

# Should return 200 OK
```

### Test 6: View Published Stories

```bash
# Anyone can view (no auth needed)
curl -X GET "http://localhost:8080/uploads/published?page=0&size=12"

# Should include your story in results
```

### Test 7: Search Published Stories

```bash
curl -X GET "http://localhost:8080/uploads/published/search?keyword=amazing&page=0&size=12"

# Should find "My Amazing Story"
```

## Frontend Testing

### Manual UI Tests

1. **Registration Flow**
   - Go to http://localhost:3000
   - Click "Đăng nhập"
   - Click "Đăng ký"
   - Fill in credentials
   - Register and auto-login

2. **Upload Page**
   - After login, click "📝 Tải Lên" button in header
   - Fill in story details
   - Paste story content or upload .txt file
   - Click "📤 Tải Lên"
   - Should see "✅ Truyện đã được tải lên thành công!"
   - View "My Uploads" section

3. **External Sources** (when UI component is added)
   - Search for book keywords
   - View results from Gutenberg, OpenLibrary, MangaDex
   - See book covers and details
   - (Import button for future feature)

## Troubleshooting

### "Connection refused" on http://localhost:8080
- Ensure Java backend is running: `mvn spring-boot:run`
- Check database is running
- Verify PORT 8080 is not in use

### "Connection refused" on http://localhost:5001
- Ensure .NET backend is running: `dotnet run`
- Verify PORT 5001 is not in use

### "Database error" when uploading
- Verify PostgreSQL is running
- Check schema is created: `\dt` in psql
- Verify story_uploads table exists

### "User not found" error
- Make sure you're logged in (token in localStorage)
- Check token is being sent in Authorization header
- Try registering a new user

### "CORS error" in browser
- Check CORS_ORIGINS in .env includes http://localhost:3000
- Restart Java backend after changing .env

## Accessing Admin Features

### View Pending Approvals

```bash
curl -X GET "http://localhost:8080/uploads/admin/pending-reviews?page=0&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Returns list of pending uploads
```

### Reject Upload with Reason

```bash
curl -X POST "http://localhost:8080/uploads/1/reject?reason=Inappropriate%20content" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Returns rejected story with reason stored
```

## Common Task Workflows

### Workflow 1: Find and Import Gutenberg Book

```bash
# 1. Search for book
curl "http://localhost:8080/external-sources/search?keyword=sherlock&page=1"

# 2. Get details of specific book
curl "http://localhost:8080/external-sources/search?keyword=sherlock&page=1" | grep -A 10 '"id"'

# 3. (Future feature) Import to collection
# Will add endpoint to convert Gutenberg data to Story
```

### Workflow 2: Complete User Upload Journey

```bash
# 1. Register
curl -X POST http://localhost:5001/api/auth/register ...

# 2. Upload story
curl -X POST http://localhost:8080/uploads \
  -H "Authorization: Bearer $TOKEN" \
  -H "User-Id: $USER_ID" \
  -d {...}

# 3. Check status
curl http://localhost:8080/uploads/my-uploads \
  -H "User-Id: $USER_ID"

# (Wait for admin approval)

# 4. Publish
curl -X POST http://localhost:8080/uploads/1/publish \
  -H "User-Id: $USER_ID"

# 5. View published
curl http://localhost:8080/uploads/published
```

## Performance Considerations

- **Gutenberg API**: 70k books, relatively fast
- **OpenLibrary API**: 1M+ books, can be slow on large searches
- **MangaDex API**: Depends on their server, may rate limit
- **Database**: With indexes, story_uploads queries should be < 100ms

## Next Steps

1. Integrate import feature (Gutenberg → Story)
2. Add admin dashboard UI for managing uploads
3. Implement file upload UI improvements (.txt, .epub, .pdf)
4. Add email notifications for approval/rejection
5. Set up automated content moderation
6. Performance optimization with caching

## References

- Project Gutenberg: https://www.gutenberg.org/ | API: https://gutendex.com/
- Open Library: https://openlibrary.org/developers/api
- MangaDex: https://api.mangadex.org/docs/
- Full documentation: See `docs/DATA_SOURCES_AND_UPLOADS.md`

---

**Need Help?**
- Check logs: `tail -f logs/application.log` (Java backend)
- Test API directly with curl or Postman
- Verify database with: `psql -U postgres -c "SELECT COUNT(*) FROM story_uploads;"`
