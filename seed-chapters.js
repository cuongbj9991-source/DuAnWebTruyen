const { Client } = require('pg');

const client = new Client({
  host: 'gondola.proxy.rlwy.net',
  port: 31141,
  database: 'railway',
  user: 'postgres',
  password: 'vpWAUpPfLcXMXaLeraiFAeaISCtWwXHl',
});

async function seedChapters() {
  try {
    await client.connect();
    console.log('✅ Connected to PostgreSQL');

    // Seed chapters for story 1 (Thiên Ngoại Chi Tinh)
    await client.query(`
      INSERT INTO chapters (story_id, chapter_number, title, content, word_count, created_at, updated_at) VALUES
      (1, 1, 'Chương 1: Bắt đầu hành trình', 'Vào một ngày tháng năm, cậu bé tên là Vô Thiên đã bắt đầu hành trình của mình. Quyết tâm trở thành chiến sĩ hành động vĩ đại...', 500, NOW(), NOW()),
      (1, 2, 'Chương 2: Gặp gỡ thầy phó', 'Trên đường đi, Vô Thiên gặp một vị thầy trang nhân. Vị thầy này có vẻ biết được nhiều điều bí mật...', 450, NOW(), NOW()),
      (1, 3, 'Chương 3: Bước vào thế giới mới', 'Thế giới trước mắt hoàn toàn khác với những gì Vô Thiên từng tưởng tượng. Sức mạnh thần bí...', 520, NOW(), NOW())
      ON CONFLICT DO NOTHING;
    `);
    console.log('✅ Chapters for story 1 seeded');

    // Seed chapters for story 2 (Đấu Phá Thương Khung)
    await client.query(`
      INSERT INTO chapters (story_id, chapter_number, title, content, word_count, created_at, updated_at) VALUES
      (2, 1, 'Chương 1: Thiên tài bị bỏ lại phía sau', 'Xiao Yan, một thiên tài thuở xưa, đã bị một cô gái bí ẩn cướp mất khí vận...', 480, NOW(), NOW()),
      (2, 2, 'Chương 2: Hộ Thân Kính', 'Để khôi phục sức mạnh, Xiao Yan phải sử dụng lò nung đã có từ xưa...', 510, NOW(), NOW()),
      (2, 3, 'Chương 3: Con đường tu luyện', 'Với sự giúp đỡ từ lò nung, Xiao Yan bắt đầu con đường tu luyện lại...', 540, NOW(), NOW()),
      (2, 4, 'Chương 4: Sức mạnh mới', 'Sau mấy tháng rèn luyện, Xiao Yan đã cảm nhận được sức mạnh mới...', 500, NOW(), NOW())
      ON CONFLICT DO NOTHING;
    `);
    console.log('✅ Chapters for story 2 seeded');

    // Seed chapters for story 3 (Mộng Hương Lục)
    await client.query(`
      INSERT INTO chapters (story_id, chapter_number, title, content, word_count, created_at, updated_at) VALUES
      (3, 1, 'Chương 1: Thế giới ma thuật', 'Cô gái tên Linh đi vào một cánh rừng bí ẩn và phát hiện ra sự tồn tại của thế giới ma thuật...', 490, NOW(), NOW()),
      (3, 2, 'Chương 2: Tìm kiếm tình yêu', 'Trong thế giới này, Linh gặp một cậu trai bí ẩn có khả năng kiểm soát lửa...', 510, NOW(), NOW()),
      (3, 3, 'Chương 3: Mối nhân duyên', 'Họ dần hiểu được rằng, số phận đã kết nối hai tâm hồn với nhau...', 530, NOW(), NOW())
      ON CONFLICT DO NOTHING;
    `);
    console.log('✅ Chapters for story 3 seeded');

    console.log('✅ All chapters seeded successfully!');
    await client.end();
  } catch (error) {
    console.error('❌ Error:', error.message);
    process.exit(1);
  }
}

seedChapters();
