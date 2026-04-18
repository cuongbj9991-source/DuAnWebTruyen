const axios = require('axios');
const cheerio = require('cheerio');
const config = require('../../config');

class StoryScraperService {
  /**
   * Scrape story from Falool.com
   */
  static async scrapeFromFalool(storyUrl) {
    try {
      const response = await axios.get(storyUrl, {
        timeout: config.scraper.timeout,
        headers: {
          'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        }
      });

      const $ = cheerio.load(response.data);
      
      const title = $('h1.story-title').text().trim();
      const author = $('.story-author').text().trim();
      const description = $('.story-description').text().trim();
      const coverImage = $('img.story-cover').attr('src');
      const chapters = [];

      // Extract chapters
      $('.chapter-list li').each((index, element) => {
        const link = $(element).find('a');
        chapters.push({
          title: link.text().trim(),
          url: link.attr('href')
        });
      });

      return {
        title,
        author,
        description,
        coverImage,
        chapters,
        source: 'falool',
        sourceUrl: storyUrl
      };
    } catch (error) {
      console.error('Error scraping from Falool:', error.message);
      throw new Error('Failed to scrape story from Falool');
    }
  }

  /**
   * Scrape chapter content
   */
  static async scrapeChapterContent(chapterUrl) {
    try {
      const response = await axios.get(chapterUrl, {
        timeout: config.scraper.timeout,
        headers: {
          'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        }
      });

      const $ = cheerio.load(response.data);
      
      const chapterTitle = $('h1.chapter-title').text().trim();
      const content = $('div.chapter-content').html();

      return {
        title: chapterTitle,
        content,
        sourceUrl: chapterUrl
      };
    } catch (error) {
      console.error('Error scraping chapter content:', error.message);
      throw new Error('Failed to scrape chapter content');
    }
  }
}

module.exports = StoryScraperService;
