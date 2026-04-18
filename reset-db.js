const { Client } = require('pg');

const client = new Client({
  host: 'gondola.proxy.rlwy.net',
  port: 31141,
  database: 'railway',
  user: 'postgres',
  password: 'vpWAUpPfLcXMXaLeraiFAeaISCtWwXHl',
});

async function resetDb() {
  try {
    await client.connect();
    console.log('✅ Connected');
    
    // Drop all tables
    const tables = ['story_uploads', 'comments', 'favorites', 'reading_progress', 'chapters', 'stories', 'users'];
    for (const table of tables) {
      await client.query(`DROP TABLE IF EXISTS ${table} CASCADE`);
    }
    console.log('✅ Tables dropped');
    
    // Create tables fresh
    await client.query(`
      CREATE TABLE users (
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
      
      CREATE TABLE stories (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
        title VARCHAR(255) NOT NULL,
        title_alt VARCHAR(255),
        description TEXT,
        author VARCHAR(100),
        genre VARCHAR(100),
        type VARCHAR(50),
        status VARCHAR(50),
        source VARCHAR(100),
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
      
      CREATE TABLE chapters (
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
      
      CREATE TABLE reading_progress (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        story_id BIGINT NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
        last_chapter_read INTEGER DEFAULT 0,
        scroll_position INTEGER DEFAULT 0,
        last_read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        UNIQUE(user_id, story_id)
      );
      
      CREATE TABLE favorites (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        story_id BIGINT NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        UNIQUE(user_id, story_id)
      );
      
      CREATE TABLE comments (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        story_id BIGINT NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
        content TEXT NOT NULL,
        rating INTEGER CHECK (rating >= 1 AND rating <= 5),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
      
      CREATE TABLE story_uploads (
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
      
      CREATE INDEX idx_stories_genre ON stories(genre);
      CREATE INDEX idx_stories_type ON stories(type);
      CREATE INDEX idx_stories_status ON stories(status);
      CREATE INDEX idx_stories_source ON stories(source);
      CREATE INDEX idx_stories_created_at ON stories(created_at DESC);
      CREATE INDEX idx_stories_views ON stories(views_total DESC);
      CREATE INDEX idx_stories_rating ON stories(rating DESC);
      CREATE INDEX idx_stories_title ON stories(title);
      CREATE INDEX idx_chapters_story_id ON chapters(story_id);
      CREATE INDEX idx_reading_progress_user_id ON reading_progress(user_id);
      CREATE INDEX idx_reading_progress_story_id ON reading_progress(story_id);
      CREATE INDEX idx_favorites_user_id ON favorites(user_id);
      CREATE INDEX idx_comments_story_id ON comments(story_id);
      CREATE INDEX idx_comments_user_id ON comments(user_id);
      CREATE INDEX idx_comments_created_at ON comments(created_at DESC);
      CREATE INDEX idx_users_email ON users(email);
      CREATE INDEX idx_story_uploads_user_id ON story_uploads(user_id);
      CREATE INDEX idx_story_uploads_status ON story_uploads(status);
      CREATE INDEX idx_story_uploads_is_approved ON story_uploads(is_approved);
      CREATE INDEX idx_story_uploads_is_public ON story_uploads(is_public);
      CREATE INDEX idx_story_uploads_created_at ON story_uploads(created_at DESC);
      CREATE INDEX idx_story_uploads_views ON story_uploads(views_count DESC);
    `);
    console.log('✅ Schema created');
    await client.end();
  } catch (err) {
    console.error('❌ Error:', err.message);
    process.exit(1);
  }
}

resetDb();
