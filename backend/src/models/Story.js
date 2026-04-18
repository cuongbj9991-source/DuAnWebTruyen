const pool = require('../../config/database');

class StoryModel {
  /**
   * Lấy tất cả truyện với lọc nâng cao, tìm kiếm và sắp xếp
   */
  static async getAll(limit = 20, offset = 0, filters = {}) {
    let query = 'SELECT * FROM stories WHERE 1=1';
    const params = [];
    let paramIndex = 1;

    // Search by title or alternative title or author
    if (filters.search) {
      query += ` AND (
        title ILIKE $${paramIndex} OR 
        title_alternative ILIKE $${paramIndex} OR 
        author ILIKE $${paramIndex}
      )`;
      params.push(`%${filters.search}%`);
      paramIndex++;
    }

    // Search by summary
    if (filters.search_summary) {
      query += ` AND summary ILIKE $${paramIndex}`;
      params.push(`%${filters.search_summary}%`);
      paramIndex++;
    }

    // Filter by source
    if (filters.source) {
      if (Array.isArray(filters.source)) {
        const placeholders = filters.source.map(() => `$${paramIndex++}`).join(',');
        query += ` AND source IN (${placeholders})`;
        params.push(...filters.source);
      } else {
        query += ` AND source = $${paramIndex}`;
        params.push(filters.source);
        paramIndex++;
      }
    }

    // Filter by total chapters
    if (filters.min_chapters) {
      query += ` AND total_chapters >= $${paramIndex}`;
      params.push(parseInt(filters.min_chapters));
      paramIndex++;
    }

    if (filters.max_chapters) {
      query += ` AND total_chapters <= $${paramIndex}`;
      params.push(parseInt(filters.max_chapters));
      paramIndex++;
    }

    // Filter by genre
    if (filters.genre) {
      if (Array.isArray(filters.genre)) {
        const placeholders = filters.genre.map(() => `$${paramIndex++}`).join(',');
        query += ` AND genre IN (${placeholders})`;
        params.push(...filters.genre);
      } else {
        query += ` AND genre = $${paramIndex}`;
        params.push(filters.genre);
        paramIndex++;
      }
    }

    // Filter by story type
    if (filters.story_type) {
      if (Array.isArray(filters.story_type)) {
        const placeholders = filters.story_type.map(() => `$${paramIndex++}`).join(',');
        query += ` AND story_type IN (${placeholders})`;
        params.push(...filters.story_type);
      } else {
        query += ` AND story_type = $${paramIndex}`;
        params.push(filters.story_type);
        paramIndex++;
      }
    }

    // Filter by status
    if (filters.status) {
      if (Array.isArray(filters.status)) {
        const placeholders = filters.status.map(() => `$${paramIndex++}`).join(',');
        query += ` AND status IN (${placeholders})`;
        params.push(...filters.status);
      } else {
        query += ` AND status = $${paramIndex}`;
        params.push(filters.status);
        paramIndex++;
      }
    }

    // Filter by rating
    if (filters.min_rating) {
      query += ` AND rating >= $${paramIndex}`;
      params.push(parseFloat(filters.min_rating));
      paramIndex++;
    }

    // Sorting
    let sortBy = 'views_total';
    let sortOrder = 'DESC';

    if (filters.sortBy) {
      switch (filters.sortBy) {
        case 'newest':
          sortBy = 'created_at';
          sortOrder = 'DESC';
          break;
        case 'updated':
          sortBy = 'last_chapter_updated';
          sortOrder = 'DESC';
          break;
        case 'views_total':
          sortBy = 'views_total';
          sortOrder = 'DESC';
          break;
        case 'views_week':
          sortBy = 'views_week';
          sortOrder = 'DESC';
          break;
        case 'views_day':
          sortBy = 'views_day';
          sortOrder = 'DESC';
          break;
        case 'likes':
          sortBy = 'likes';
          sortOrder = 'DESC';
          break;
        case 'follows':
          sortBy = 'follows';
          sortOrder = 'DESC';
          break;
        case 'bookmarks':
          sortBy = 'bookmarks';
          sortOrder = 'DESC';
          break;
        case 'rating':
          sortBy = 'rating';
          sortOrder = 'DESC';
          break;
        default:
          sortBy = 'views_total';
          sortOrder = 'DESC';
      }
    }

    query += ` ORDER BY ${sortBy} ${sortOrder}`;
    query += ` LIMIT $${paramIndex} OFFSET $${paramIndex + 1}`;
    params.push(limit, offset);

    const result = await pool.query(query, params);
    return result.rows;
  }

  /**
   * Get story by ID
   */
  static async getById(id) {
    const result = await pool.query('SELECT * FROM stories WHERE id = $1', [id]);
    return result.rows[0];
  }

  /**
   * Create new story
   */
  static async create(data) {
    const query = `
      INSERT INTO stories (
        title, title_alternative, author, description, summary, cover_image, 
        category, genre, story_type, status, source, source_url, source_id
      )
      VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)
      RETURNING *
    `;
    const result = await pool.query(query, [
      data.title,
      data.title_alternative,
      data.author,
      data.description,
      data.summary,
      data.coverImage,
      data.category,
      data.genre,
      data.story_type,
      data.status || 'ongoing',
      data.source,
      data.sourceUrl,
      data.source_id
    ]);
    return result.rows[0];
  }

  /**
   * Update story
   */
  static async update(id, data) {
    const query = `
      UPDATE stories 
      SET title = COALESCE($1, title),
          title_alternative = COALESCE($2, title_alternative),
          author = COALESCE($3, author),
          description = COALESCE($4, description),
          summary = COALESCE($5, summary),
          cover_image = COALESCE($6, cover_image),
          genre = COALESCE($7, genre),
          story_type = COALESCE($8, story_type),
          status = COALESCE($9, status),
          updated_at = NOW()
      WHERE id = $10
      RETURNING *
    `;
    const result = await pool.query(query, [
      data.title,
      data.title_alternative,
      data.author,
      data.description,
      data.summary,
      data.coverImage,
      data.genre,
      data.story_type,
      data.status,
      id
    ]);
    return result.rows[0];
  }

  /**
   * Update view stats
   */
  static async updateViewStats(id) {
    const query = `
      UPDATE stories 
      SET views_total = views_total + 1,
          updated_at = NOW()
      WHERE id = $1
    `;
    await pool.query(query, [id]);
  }

  /**
   * Delete story
   */
  static async delete(id) {
    const result = await pool.query('DELETE FROM stories WHERE id = $1 RETURNING *', [id]);
    return result.rows[0];
  }

  /**
   * Get unique genres
   */
  static async getGenres() {
    const result = await pool.query(
      'SELECT DISTINCT genre FROM stories WHERE genre IS NOT NULL ORDER BY genre'
    );
    return result.rows.map(row => row.genre);
  }

  /**
   * Get unique authors
   */
  static async getAuthors() {
    const result = await pool.query(
      'SELECT DISTINCT author FROM stories WHERE author IS NOT NULL ORDER BY author LIMIT 100'
    );
    return result.rows.map(row => row.author);
  }

  /**
   * Get unique sources
   */
  static async getSources() {
    const result = await pool.query(
      'SELECT DISTINCT source FROM stories WHERE source IS NOT NULL ORDER BY source'
    );
    return result.rows.map(row => row.source);
  }
}

module.exports = StoryModel;
