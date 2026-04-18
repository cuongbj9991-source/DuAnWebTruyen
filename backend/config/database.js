const { Pool } = require('pg');
const config = require('./index');

const pool = new Pool({
  user: config.database.user,
  password: config.database.password,
  host: config.database.host,
  port: config.database.port,
  database: config.database.database
});

pool.on('error', (err) => {
  console.error('Lỗi không mong đợi trên khách hàng không hoạt động', err);
});

module.exports = pool;
