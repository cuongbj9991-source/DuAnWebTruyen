const pool = require('../../config/database');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const config = require('../../config');

class UserModel {
  /**
   * Lấy người dùng theo ID
   */
  static async getById(id) {
    const result = await pool.query('SELECT id, username, email, created_at FROM users WHERE id = $1', [id]);
    return result.rows[0];
  }

  /**
   * Lấy người dùng theo email
   */
  static async getByEmail(email) {
    const result = await pool.query('SELECT * FROM users WHERE email = $1', [email]);
    return result.rows[0];
  }

  /**
   * Tạo người dùng mới
   */
  static async create(username, email, password) {
    const hashedPassword = await bcrypt.hash(password, 10);
    const query = `
      INSERT INTO users (username, email, password)
      VALUES ($1, $2, $3)
      RETURNING id, username, email, created_at
    `;
    const result = await pool.query(query, [username, email, hashedPassword]);
    return result.rows[0];
  }

  /**
   * Xác minh mật khẩu
   */
  static async verifyPassword(plainPassword, hashedPassword) {
    return await bcrypt.compare(plainPassword, hashedPassword);
  }

  /**
   * Tạo JWT token
   */
  static generateToken(userId) {
    return jwt.sign({ id: userId }, config.jwt.secret, { expiresIn: config.jwt.expiresIn });
  }

  /**
   * Cập nhật hồ sơ người dùng
   */
  static async updateProfile(id, data) {
    const query = `
      UPDATE users 
      SET username = COALESCE($1, username),
          email = COALESCE($2, email),
          updated_at = NOW()
      WHERE id = $3
      RETURNING id, username, email, created_at
    `;
    const result = await pool.query(query, [data.username, data.email, id]);
    return result.rows[0];
  }

  /**
   * Lấy thống kê AI của người dùng
   */
  static async getAIStats(userId) {
    try {
      const today = new Date().toISOString().split('T')[0];

      // Sử dụng hôm nay
      const todayResult = await pool.query(
        `SELECT usage_count, total_tokens, total_cost FROM user_ai_usage 
         WHERE user_id = $1 AND date = $2`,
        [userId, today]
      );

      // Tổng sử dụng
      const totalResult = await pool.query(
        `SELECT 
           SUM(usage_count) as total_usage, 
           SUM(total_tokens) as total_tokens, 
           SUM(total_cost) as total_cost 
         FROM user_ai_usage 
         WHERE user_id = $1`,
        [userId]
      );

      // Thông tin người dùng (kiểm tra premium)
      const userResult = await pool.query(
        'SELECT is_premium FROM users WHERE id = $1',
        [userId]
      );

      const user = userResult.rows[0];
      const dailyLimit = (user?.is_premium) 
        ? config.ai_limits.premium_daily_limit 
        : config.ai_limits.daily_limit;

      const todayStats = todayResult.rows[0] || {
        usage_count: 0,
        total_tokens: 0,
        total_cost: 0
      };

      const totalStats = totalResult.rows[0];

      return {
        today: {
          usage_count: parseInt(todayStats.usage_count) || 0,
          total_tokens: parseInt(todayStats.total_tokens) || 0,
          total_cost: parseFloat(todayStats.total_cost) || 0,
          remaining: Math.max(0, dailyLimit - (parseInt(todayStats.usage_count) || 0))
        },
        all_time: {
          usage_count: parseInt(totalStats.total_usage) || 0,
          total_tokens: parseInt(totalStats.total_tokens) || 0,
          total_cost: parseFloat(totalStats.total_cost) || 0
        },
        daily_limit: dailyLimit,
        is_premium: user?.is_premium || false,
        cost_currency: 'USD'
      };
    } catch (error) {
      throw error;
    }
  }

  /**
   * Get user recent AI logs
   */
  static async getAILogs(userId, limit = 20) {
    try {
      const query = `
        SELECT id, action, model, tokens_used, cost, status, created_at
        FROM ai_logs
        WHERE user_id = $1
        ORDER BY created_at DESC
        LIMIT $2
      `;

      const result = await pool.query(query, [userId, limit]);
      return result.rows;
    } catch (error) {
      throw error;
    }
  }

  /**
   * Upgrade to premium
   */
  static async upgradeToPremium(userId) {
    try {
      const query = `
        UPDATE users 
        SET is_premium = true, updated_at = CURRENT_TIMESTAMP
        WHERE id = $1
        RETURNING id, username, email, is_premium
      `;

      const result = await pool.query(query, [userId]);
      return result.rows[0];
    } catch (error) {
      throw error;
    }
  }
}

module.exports = UserModel;
