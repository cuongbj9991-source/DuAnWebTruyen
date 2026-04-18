// Cấu hình môi trường backend
require('dotenv').config();

module.exports = {
  app: {
    port: process.env.PORT || 5000,
    env: process.env.NODE_ENV || 'development'
  },
  database: {
    user: process.env.DB_USER || 'postgres',
    password: process.env.DB_PASSWORD || 'password',
    host: process.env.DB_HOST || 'localhost',
    port: process.env.DB_PORT || 5432,
    database: process.env.DB_NAME || 'web_doc_truyen'
  },
  jwt: {
    secret: process.env.JWT_SECRET || 'your_secret_key_change_in_production',
    expiresIn: process.env.JWT_EXPIRE || '7d'
  },
  scraper: {
    timeout: process.env.SCRAPER_TIMEOUT || 10000,
    retries: process.env.SCRAPER_RETRIES || 3
  },
  openai: {
    apiKey: process.env.OPENAI_API_KEY || '',
    model: process.env.OPENAI_MODEL || 'gpt-4o-mini',
    maxTokens: parseInt(process.env.OPENAI_MAX_TOKENS) || 500
  },
  ai_limits: {
    daily_limit: parseInt(process.env.AI_DAILY_LIMIT) || 5,
    premium_daily_limit: parseInt(process.env.AI_PREMIUM_DAILY_LIMIT) || 50,
    request_timeout: parseInt(process.env.AI_REQUEST_TIMEOUT) || 60000
  }
};
