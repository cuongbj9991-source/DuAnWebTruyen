const OpenAI = require('openai');
const config = require('../../config');
const pool = require('../../config/database');

class AIService {
  constructor() {
    if (!config.openai.apiKey) {
      console.warn('⚠️ CẢNH BÁO: OPENAI_API_KEY chưa được đặt. Tính năng AI bị vô hiệu.');
      this.client = null;
    } else {
      this.client = new OpenAI({
        apiKey: config.openai.apiKey,
      });
    }
  }

  /**
   * Kiểm tra AI có được bật không
   */
  isEnabled() {
    return this.client !== null;
  }

  /**
   * Kiểm tra user còn lượt dùng AI trong ngày
   */
  async checkUserLimit(userId) {
    try {
      const today = new Date().toISOString().split('T')[0];

      const result = await pool.query(
        'SELECT usage_count FROM user_ai_usage WHERE user_id = $1 AND date = $2',
        [userId, today]
      );

      const usage = result.rows[0];
      const currentUsage = usage ? usage.usage_count : 0;
      const dailyLimit = config.ai_limits.daily_limit;

      return {
        remaining: Math.max(0, dailyLimit - currentUsage),
        used: currentUsage,
        limit: dailyLimit,
        hasLimit: currentUsage >= dailyLimit
      };
    } catch (error) {
      console.error('Error checking user limit:', error);
      throw error;
    }
  }

  /**
   * Ghi log AI usage
   */
  async logUsage(userId, data) {
    try {
      const query = `
        INSERT INTO ai_logs (
          user_id, story_id, chapter_id, action, model, 
          prompt, result, tokens_used, cost, status, error_message
        )
        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
        RETURNING *
      `;

      const values = [
        userId,
        data.story_id || null,
        data.chapter_id || null,
        data.action,
        data.model,
        data.prompt,
        data.result || null,
        data.tokens_used || 0,
        data.cost || 0,
        data.status,
        data.error_message || null
      ];

      const result = await pool.query(query, values);

      // Cập nhật daily usage
      await this.updateDailyUsage(userId, data.tokens_used || 0, data.cost || 0);

      return result.rows[0];
    } catch (error) {
      console.error('Error logging AI usage:', error);
      throw error;
    }
  }

  /**
   * Cập nhật daily usage statistics
   */
  async updateDailyUsage(userId, tokensUsed, cost) {
    try {
      const today = new Date().toISOString().split('T')[0];

      const query = `
        INSERT INTO user_ai_usage (user_id, date, usage_count, total_tokens, total_cost)
        VALUES ($1, $2, 1, $3, $4)
        ON CONFLICT (user_id, date) DO UPDATE SET
          usage_count = user_ai_usage.usage_count + 1,
          total_tokens = user_ai_usage.total_tokens + $3,
          total_cost = user_ai_usage.total_cost + $4
      `;

      await pool.query(query, [userId, today, tokensUsed, cost]);
    } catch (error) {
      console.error('Error updating daily usage:', error);
      throw error;
    }
  }

  /**
   * Tính chi phí dựa trên tokens
   * GPT-4o mini: $0.00015 per input token, $0.0006 per output token
   */
  calculateCost(promptTokens, completionTokens, model = 'gpt-4o-mini') {
    // Định giá gpt-4o-mini (rẻ nhất)
    const rates = {
      'gpt-4o-mini': { input: 0.00015, output: 0.0006 },
      'gpt-3.5-turbo': { input: 0.0005, output: 0.0015 },
      'gpt-4': { input: 0.03, output: 0.06 }
    };

    const rate = rates[model] || rates['gpt-4o-mini'];
    return (promptTokens * rate.input) + (completionTokens * rate.output);
  }

  /**
   * Generate chapter content sử dụng GPT
   * @param {string} storyContext - Context của truyện (tóm tắt, nhân vật, setting)
   * @param {number} chapterNumber - Số chương
   * @param {string} chapterPrompt - Prompt cho chương này
   */
  async generateChapter(storyContext, chapterNumber, chapterPrompt) {
    if (!this.isEnabled()) {
      throw new Error('AI service is not enabled. Set OPENAI_API_KEY.');
    }

    try {
      const systemPrompt = `Bạn là một nhà văn chuyên nghiệp viết những câu chuyện hấp dẫn.
Viết chương ${chapterNumber} dựa trên bối cảnh sau:

${storyContext}

Yêu cầu:
- Viết bằng tiếng Việt
- Độ dài khoảng 300-400 chữ
- Giữ phong cách nhất quán
- Có sự phát triển nhân vật và cốt truyện
- Không viết quá dài để tiết kiệm chi phí`;

      const response = await this.client.chat.completions.create({
        model: config.openai.model,
        messages: [
          {
            role: 'system',
            content: systemPrompt
          },
          {
            role: 'user',
            content: chapterPrompt || `Hãy viết chương ${chapterNumber} của truyện này.`
          }
        ],
        max_tokens: config.openai.maxTokens,
        temperature: 0.7
      });

      const content = response.choices[0].message.content;
      const promptTokens = response.usage.prompt_tokens;
      const completionTokens = response.usage.completion_tokens;
      const cost = this.calculateCost(promptTokens, completionTokens);

      return {
        content,
        promptTokens,
        completionTokens,
        totalTokens: response.usage.total_tokens,
        cost,
        model: config.openai.model
      };
    } catch (error) {
      console.error('Error generating chapter:', error);
      throw error;
    }
  }

  /**
   * Generate story summary/outline từ description
   */
  async generateStoryOutline(title, description, genre, characters) {
    if (!this.isEnabled()) {
      throw new Error('AI service is not enabled. Set OPENAI_API_KEY.');
    }

    try {
      const prompt = `Dựa trên thông tin sau, hãy tạo một phác thảo chi tiết cho truyện:

Tiêu đề: ${title}
Mô tả: ${description}
Thể loại: ${genre}
Nhân vật chính: ${characters}

Hãy tạo:
1. Tóm tắt chung (2-3 câu)
2. Cốt truyện chính (3-4 điểm quan trọng)
3. Kết thúc dự kiến
4. Gợi ý cho 3 chương đầu tiên

Giữ ngắn gọn để tiết kiệm chi phí!`;

      const response = await this.client.chat.completions.create({
        model: config.openai.model,
        messages: [
          {
            role: 'user',
            content: prompt
          }
        ],
        max_tokens: 600,
        temperature: 0.8
      });

      const outline = response.choices[0].message.content;
      const cost = this.calculateCost(
        response.usage.prompt_tokens,
        response.usage.completion_tokens
      );

      return {
        outline,
        tokens: response.usage.total_tokens,
        cost,
        model: config.openai.model
      };
    } catch (error) {
      console.error('Error generating story outline:', error);
      throw error;
    }
  }

  /**
   * Suggest next chapter
   */
  async suggestNextChapter(currentChapters, genre) {
    if (!this.isEnabled()) {
      throw new Error('AI service is not enabled. Set OPENAI_API_KEY.');
    }

    try {
      const prompt = `Dựa trên các chương sau trong thể loại ${genre}:

${currentChapters}

Hãy gợi ý 3 hướng phát triển cho chương tiếp theo (mỗi gợi ý 1-2 câu):`;

      const response = await this.client.chat.completions.create({
        model: config.openai.model,
        messages: [
          {
            role: 'user',
            content: prompt
          }
        ],
        max_tokens: 300,
        temperature: 0.7
      });

      return {
        suggestions: response.choices[0].message.content,
        tokens: response.usage.total_tokens,
        model: config.openai.model
      };
    } catch (error) {
      console.error('Error suggesting next chapter:', error);
      throw error;
    }
  }

  /**
   * Get user AI usage stats cho admin/user dashboard
   */
  async getUserStats(userId) {
    try {
      const today = new Date().toISOString().split('T')[0];

      // Hôm nay
      const todayResult = await pool.query(
        `SELECT usage_count, total_tokens, total_cost FROM user_ai_usage 
         WHERE user_id = $1 AND date = $2`,
        [userId, today]
      );

      // Tổng cộng (tất cả thời gian)
      const totalResult = await pool.query(
        `SELECT SUM(usage_count) as total_usage, SUM(total_tokens) as total_tokens, 
                SUM(total_cost) as total_cost FROM user_ai_usage 
         WHERE user_id = $1`,
        [userId]
      );

      const todayStats = todayResult.rows[0] || {
        usage_count: 0,
        total_tokens: 0,
        total_cost: 0
      };

      const totalStats = totalResult.rows[0];

      return {
        today: todayStats,
        all_time: {
          usage_count: parseInt(totalStats.total_usage) || 0,
          total_tokens: parseInt(totalStats.total_tokens) || 0,
          total_cost: parseFloat(totalStats.total_cost) || 0
        },
        remaining_today: config.ai_limits.daily_limit - (todayStats.usage_count || 0)
      };
    } catch (error) {
      console.error('Error getting user stats:', error);
      throw error;
    }
  }
}

module.exports = new AIService();
