const { Client } = require('pg');
const fs = require('fs');

const client = new Client({
  host: 'gondola.proxy.rlwy.net',
  port: 31141,
  database: 'railway',
  user: 'postgres',
  password: 'vpWAUpPfLcXMXaLeraiFAeaISCtWwXHl',
});

async function initDb() {
  try {
    await client.connect();
    console.log('✅ Connected to Railway database');
    
    const schema = fs.readFileSync('./database/schema.sql', 'utf8');
    
    // Split and execute statements individually
    const statements = schema
      .split(';')
      .map(stmt => stmt.trim())
      .filter(stmt => stmt.length > 0 && !stmt.startsWith('--'));
    
    for (let i = 0; i < statements.length; i++) {
      try {
        await client.query(statements[i]);
      } catch (err) {
        if (!err.message.includes('already exists')) {
          console.warn(`⚠️  Statement ${i + 1} warning: ${err.message}`);
        }
      }
    }
    
    console.log('✅ Schema initialized successfully');
    await client.end();
  } catch (err) {
    console.error('❌ Error:', err.message);
    process.exit(1);
  }
}

initDb();
