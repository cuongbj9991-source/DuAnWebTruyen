-- Web Đọc Truyện - Simplified Schema (No AI Generation)
-- Story Reading Application

-- Users table
CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(100) UNIQUE NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  avatar_url VARCHAR(500),
  bio TEXT,
  is_premium BOOLEAN DEFAULT FALSE,
  email_verified BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Stories table - Read-only stories from various sources
CREATE TABLE IF NOT EXISTS stories (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
  title VARCHAR(255) NOT NULL,
  title_alt VARCHAR(255),
  description TEXT,
  author VARCHAR(100),
  genre VARCHAR(100),
  type VARCHAR(50),  -- sáng tác, dịch, txt dịch, scan
  status VARCHAR(50), -- ongoing, completed, paused
  source VARCHAR(100), -- Falool, Qidian, original, etc.
  cover_url VARCHAR(500),
  views_total BIGINT DEFAULT 0,
  likes INTEGER DEFAULT 0,
  comments_count INTEGER DEFAULT 0,
  rating DECIMAL(3, 2) DEFAULT 0,
  rating_count INTEGER DEFAULT 0,
  last_chapter_updated TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Chapters table
CREATE TABLE IF NOT EXISTS chapters (
  id BIGSERIAL PRIMARY KEY,
  story_id BIGINT NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
  chapter_number INTEGER NOT NULL,
  title VARCHAR(255),
  content TEXT NOT NULL,
  word_count INTEGER DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(story_id, chapter_number)
);

-- Reading progress table
CREATE TABLE IF NOT EXISTS reading_progress (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  story_id BIGINT NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
  last_chapter_read INTEGER DEFAULT 0,
  scroll_position INTEGER DEFAULT 0,
  last_read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(user_id, story_id)
);

-- Favorites table
CREATE TABLE IF NOT EXISTS favorites (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  story_id BIGINT NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(user_id, story_id)
);

-- Comments table
CREATE TABLE IF NOT EXISTS comments (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  story_id BIGINT NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
  content TEXT NOT NULL,
  rating INTEGER CHECK (rating >= 1 AND rating <= 5),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Story Uploads table - User-uploaded stories
CREATE TABLE IF NOT EXISTS story_uploads (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
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

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_stories_genre ON stories(genre);
CREATE INDEX IF NOT EXISTS idx_stories_type ON stories(type);
CREATE INDEX IF NOT EXISTS idx_stories_status ON stories(status);
CREATE INDEX IF NOT EXISTS idx_stories_source ON stories(source);
CREATE INDEX IF NOT EXISTS idx_stories_created_at ON stories(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_stories_views ON stories(views_total DESC);
CREATE INDEX IF NOT EXISTS idx_stories_rating ON stories(rating DESC);
CREATE INDEX IF NOT EXISTS idx_stories_title ON stories(title);
CREATE INDEX IF NOT EXISTS idx_chapters_story_id ON chapters(story_id);
CREATE INDEX IF NOT EXISTS idx_reading_progress_user_id ON reading_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_reading_progress_story_id ON reading_progress(story_id);
CREATE INDEX IF NOT EXISTS idx_favorites_user_id ON favorites(user_id);
CREATE INDEX IF NOT EXISTS idx_comments_story_id ON comments(story_id);
CREATE INDEX IF NOT EXISTS idx_comments_user_id ON comments(user_id);
CREATE INDEX IF NOT EXISTS idx_comments_created_at ON comments(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_story_uploads_user_id ON story_uploads(user_id);
CREATE INDEX IF NOT EXISTS idx_story_uploads_status ON story_uploads(status);
CREATE INDEX IF NOT EXISTS idx_story_uploads_is_approved ON story_uploads(is_approved);
CREATE INDEX IF NOT EXISTS idx_story_uploads_is_public ON story_uploads(is_public);
CREATE INDEX IF NOT EXISTS idx_story_uploads_created_at ON story_uploads(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_story_uploads_views ON story_uploads(views_count DESC);
