const { Client } = require('pg');

const client = new Client({
  host: 'gondola.proxy.rlwy.net',
  port: 31141,
  database: 'railway',
  user: 'postgres',
  password: 'vpWAUpPfLcXMXaLeraiFAeaISCtWwXHl',
});

async function seed() {
  try {
    await client.connect();
    console.log('✅ Connected to PostgreSQL');

    // Create tables if not exist
    await client.query(`
      CREATE TABLE IF NOT EXISTS users (
        id BIGSERIAL PRIMARY KEY,
        username VARCHAR(100) UNIQUE NOT NULL,
        email VARCHAR(255) UNIQUE NOT NULL,
        password_hash VARCHAR(255) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
    `);
    console.log('✅ Users table created');

    // Seed users
    await client.query(`
      INSERT INTO users (username, email, password_hash, created_at) VALUES
      ('user1', 'user1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/KFm', NOW()),
      ('user2', 'user2@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/KFm', NOW())
      ON CONFLICT (email) DO NOTHING;
    `);
    console.log('✅ Users seeded');

    // Create stories table if not exist
    await client.query(`
      CREATE TABLE IF NOT EXISTS stories (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT REFERENCES users(id),
        title VARCHAR(255) NOT NULL,
        description TEXT,
        author VARCHAR(100),
        genre VARCHAR(100),
        type VARCHAR(50),
        status VARCHAR(50),
        source VARCHAR(100),
        cover_url VARCHAR(500),
        views_total BIGINT DEFAULT 0,
        likes INTEGER DEFAULT 0,
        rating DECIMAL(3, 2) DEFAULT 0,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
    `);
    console.log('✅ Stories table created');

    // Seed stories
    await client.query(`
      INSERT INTO stories (title, user_id, description, author, genre, type, status, source, created_at, updated_at) VALUES
      ('Thiên Ngoại Chi Tinh', 1, 'Một câu chuyện kỳ diệu về một chiến sĩ hành động', 'Tác giả Việt', 'Tiên Hiệp', 'Sáng tác', 'Ongoing', 'original', NOW(), NOW()),
      ('Đấu Phá Thương Khung', 1, 'Câu chuyện về một thiếu niên với khí vận phi thường', 'Tác giả Trung Quốc', 'Tiên Hiệp', 'Dịch', 'Completed', 'Qidian', NOW(), NOW()),
      ('Mộng Hương Lục', 2, 'Hành trình tìm kiếm tình yêu trong một thế giới ma thuật', 'Tác giả Việt', 'Ngôn Tình', 'Sáng tác', 'Ongoing', 'original', NOW(), NOW())
      ON CONFLICT DO NOTHING;
    `);
    console.log('✅ Stories seeded');

    console.log('✅ Seed completed!');
  } catch (error) {
    console.error('❌ Error:', error.message);
  } finally {
    await client.end();
  }
}

seed();
