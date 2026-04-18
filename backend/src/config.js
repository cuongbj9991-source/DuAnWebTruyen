module.exports = {
  jwt: {
    secret: process.env.JWT_SECRET || 'your_jwt_secret_key_change_this',
    expiresIn: '7d'
  },
  database: {
    host: process.env.DB_HOST || 'localhost',
    port: process.env.DB_PORT || 5432,
    user: process.env.DB_USER || 'postgres',
    password: process.env.DB_PASSWORD || '',
    database: process.env.DB_NAME || 'doc_truyen'
  },
  email: {
    user: process.env.GMAIL_USER,
    password: process.env.GMAIL_PASSWORD
  }
};
