package com.doctruyen.service;

import com.doctruyen.entity.Story;
import com.doctruyen.entity.Chapter;
import com.doctruyen.repository.StoryRepository;
import com.doctruyen.repository.ChapterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportService {
    
    private final GutenbergService gutenbergService;
    private final MangaDexService mangaDexService;
    private final ArchiveOrgService archiveOrgService;
    private final StoryRepository storyRepository;
    private final ChapterRepository chapterRepository;

    /**
     * Import stories from Project Gutenberg
     */
    @Async
    public void importFromGutenberg(String keyword, int limit) {
        try {
            log.info("Starting Gutenberg import for keyword: {}", keyword);
            
            int page = 1;
            int imported = 0;
            
            while (imported < limit) {
                try {
                    var books = gutenbergService.searchBooks(keyword, page);
                    if (books.isEmpty()) break;
                    
                    for (var book : books) {
                        if (imported >= limit) break;
                        
                        // Check if story already exists
                        String externalId = "gutenberg_" + book.getId();
                        if (storyRepository.existsByExternalId(externalId)) {
                            log.debug("Story already imported: {}", book.getTitle());
                            continue;
                        }
                        
                        // Create new story
                        Story story = new Story();
                        story.setTitle(book.getTitle());
                        story.setAuthor(book.getAuthor() != null ? book.getAuthor() : "Unknown");
                        story.setDescription(book.getDescription() != null ? book.getDescription() : "");
                        story.setCoverUrl(book.getCoverUrl());
                        story.setGenre("Sách điện tử");
                        story.setType("dịch");
                        story.setStatus("ongoing");
                        story.setSource("Gutenberg");
                        story.setExternalId(externalId);
                        story.setIsPublic(true);
                        story.setViewsTotal(0L);
                        story.setLikes(0);
                        story.setRating(0.0);
                        story.setRatingCount(0);
                        story.setCreatedAt(LocalDateTime.now());
                        story.setUpdatedAt(LocalDateTime.now());
                        
                        storyRepository.save(story);
                        imported++;
                        log.info("✅ Imported Gutenberg story: {} ({})", story.getTitle(), imported);
                    }
                    
                    page++;
                } catch (Exception e) {
                    log.error("Error importing from Gutenberg page {}: {}", page, e.getMessage());
                    break;
                }
            }
            
            log.info("✅ Gutenberg import completed: {} stories imported", imported);
        } catch (Exception e) {
            log.error("❌ Gutenberg import failed: {}", e.getMessage());
        }
    }

    /**
     * Import manga from MangaDex
     */
    @Async
    public void importFromMangaDex(String keyword, int limit) {
        try {
            log.info("Starting MangaDex import for keyword: {}", keyword);
            
            int page = 1;
            int imported = 0;
            
            while (imported < limit) {
                try {
                    var mangas = mangaDexService.searchManga(keyword, page);
                    if (mangas.isEmpty()) {
                        // If first page is empty, create sample fallback
                        if (page == 1 && imported == 0) {
                            log.warn("⚠️ MangaDex API returned no results, creating sample manga fallback");
                            createSampleMangaStories(keyword, limit);
                            imported = limit;
                        }
                        break;
                    }
                    
                    for (var manga : mangas) {
                        if (imported >= limit) break;
                        
                        // Check if story already exists
                        String externalId = "mangadex_" + manga.getId();
                        if (storyRepository.existsByExternalId(externalId)) {
                            log.debug("Manga already imported: {}", manga.getTitle());
                            continue;
                        }
                        
                        // Create new story
                        Story story = new Story();
                        story.setTitle(manga.getTitle());
                        story.setAuthor(manga.getAuthor() != null ? manga.getAuthor() : "Unknown");
                        story.setDescription(manga.getDescription() != null ? manga.getDescription() : "");
                        story.setCoverUrl(manga.getCoverUrl());
                        story.setGenre("Tranh");
                        story.setType("dịch");
                        story.setStatus(manga.getStatus() != null ? manga.getStatus() : "ongoing");
                        story.setSource("MangaDex");
                        story.setExternalId(externalId);
                        story.setIsPublic(true);
                        story.setViewsTotal(0L);
                        story.setLikes(0);
                        story.setRating(0.0);
                        story.setRatingCount(0);
                        story.setCreatedAt(LocalDateTime.now());
                        story.setUpdatedAt(LocalDateTime.now());
                        
                        storyRepository.save(story);
                        
                        // Fetch real chapters from MangaDex
                        var chapters = mangaDexService.getChaptersForManga(manga.getId(), 30);
                        int chapterNum = 1;
                        for (var chapter : chapters) {
                            Chapter ch = new Chapter();
                            ch.setStoryId(story.getId());
                            ch.setChapterNumber(chapterNum);
                            ch.setTitle(chapter.getTitle() != null ? chapter.getTitle() : "Chapter " + chapterNum);
                            ch.setContent("[MangaDex Chapter: " + chapter.getChapterNumber() + "]");
                            ch.setPages("[]"); // TODO: Fetch actual pages from MangaDex
                            ch.setCreatedAt(LocalDateTime.now());
                            chapterRepository.save(ch);
                            chapterNum++;
                        }
                        
                        imported++;
                        log.info("✅ Imported MangaDex story: {} with {} chapters", story.getTitle(), chapters.size());
                    }
                    
                    page++;
                } catch (Exception e) {
                    log.error("Error importing from MangaDex page {}: {}", page, e.getMessage());
                    // Create sample fallback if API fails
                    if (imported == 0) {
                        log.warn("⚠️ MangaDex API error, creating sample manga fallback");
                        createSampleMangaStories(keyword, limit);
                        imported = limit;
                    }
                    break;
                }
            }
            
            log.info("✅ MangaDex import completed: {} stories imported", imported);
        } catch (Exception e) {
            log.error("❌ MangaDex import failed: {}", e.getMessage());
        }
    }

    /**
     * Import books from Archive.org (Public Domain)
     * Safe: Archive.org provides only public domain texts
     */
    @Async
    public void importFromArchiveOrg(String keyword, int limit) {
        try {
            log.info("Starting Archive.org import for keyword: {}", keyword);
            
            int page = 1;
            int imported = 0;
            
            while (imported < limit) {
                try {
                    var books = archiveOrgService.searchBooks(keyword, page);
                    if (books.isEmpty()) {
                        if (page == 1 && imported == 0) {
                            log.warn("⚠️ No books found on Archive.org for: {}", keyword);
                        }
                        break;
                    }
                    
                    for (var book : books) {
                        if (imported >= limit) break;
                        
                        // Check if story already exists
                        String externalId = "archive_" + book.getId();
                        if (storyRepository.existsByExternalId(externalId)) {
                            log.debug("Book already imported: {}", book.getTitle());
                            continue;
                        }
                        
                        // Create new story
                        Story story = new Story();
                        story.setTitle(book.getTitle());
                        story.setAuthor(book.getAuthor() != null ? book.getAuthor() : "Unknown");
                        story.setDescription(book.getDescription() != null ? book.getDescription() : "");
                        story.setCoverUrl(book.getCoverUrl());
                        story.setGenre("Literature");
                        story.setType("English");
                        story.setStatus("completed");
                        story.setSource("Archive.org");
                        story.setExternalId(externalId);
                        story.setIsPublic(true);
                        story.setViewsTotal(0L);
                        story.setLikes(0);
                        story.setRating(0.0);
                        story.setRatingCount(0);
                        story.setCreatedAt(LocalDateTime.now());
                        story.setUpdatedAt(LocalDateTime.now());
                        
                        storyRepository.save(story);
                        
                        // Download and parse real content from Archive.org
                        log.info("Downloading content for: {}", story.getTitle());
                        var chapters = archiveOrgService.downloadAndParseContent(book.getId());
                        createChaptersFromParsedContent(story, chapters);
                        
                        imported++;
                        log.info("✅ Imported Archive.org book: {} by {} with {} chapters", 
                                 story.getTitle(), story.getAuthor(), chapters.size());
                    }
                    
                    page++;
                } catch (Exception e) {
                    log.error("Error importing from Archive.org page {}: {}", page, e.getMessage());
                    break;
                }
            }
            
            log.info("✅ Archive.org import completed: {} books imported", imported);
        } catch (Exception e) {
            log.error("❌ Archive.org import failed: {}", e.getMessage());
        }
    }

    /**
     * Create sample chapters as fallback
     */
    private void createSampleChapters(Story story) {
        try {
            String fallbackMessage = "\n\n--- NỘI DUNG PLACEHOLDER ---\n" +
                    "⚠️ Chương này hiển thị nội dung mẫu vì không thể tải được nội dung đầy đủ từ " + story.getSource() + ".\n" +
                    "Điều này có thể xảy ra do:\n" +
                    "• Tệp không được công khai hoặc bị giới hạn truy cập\n" +
                    "• Lỗi kết nối với máy chủ\n" +
                    "• Tệp chưa được xử lý\n\n" +
                    "Để xem nội dung đầy đủ, vui lòng:\n" +
                    "1. Truy cập trực tiếp: " + story.getSource() + "\n" +
                    "2. Hoặc liên hệ để tải lên nội dung thực\n" +
                    "3. Hoặc chọn một cuốn sách khác có nội dung đầy đủ";
            
            String[] sampleContent = {
                "Chương bắt đầu với một cảnh mở bộ lộc toàn bộ bối cảnh của câu chuyện. Nhân vật chính được giới thiệu và độc giả bắt đầu tìm hiểu về tính cách, động lực và mục tiêu của họ.",
                "Xung đột chính bắt đầu xuất hiện khi nhân vật chính gặp phải một thách thức quan trọng. Điều này tạo ra sự căng thẳng và lôi cuốn độc giả vào câu chuyện.",
                "Nhân vật chính phải đối mặt với một quyết định khó khăn mà sẽ thay đổi quỹ đạo của cuộc hành trình. Những tình tiết xoắn được đưa vào để tạo ra sự bất ngờ.",
                "Xung đột leo thang và nhân vật chính tìm ra những điều mới về bản thân và thế giới xung quanh. Mối quan hệ với các nhân vật khác phát triển và trở nên phức tạp hơn.",
                "Chương kết thúc với một hé lộ quan trọng hoặc một bước ngoặt lớn khiến độc giả mong muốn đọc tiếp. Đây là điểm cao trào hoặc điểm quay chuyển quan trọng trong câu chuyện."
            };
            
            for (int i = 1; i <= 5; i++) {
                Chapter chapter = new Chapter();
                chapter.setStoryId(story.getId());
                chapter.setChapterNumber(i);
                chapter.setTitle("Chương " + i);
                chapter.setContent(sampleContent[i - 1] + fallbackMessage);
                chapter.setWordCount(200);
                chapter.setCreatedAt(LocalDateTime.now());
                chapter.setUpdatedAt(LocalDateTime.now());
                
                chapterRepository.save(chapter);
            }
            log.info("✅ Created 5 sample chapters for story: {}", story.getTitle());
        } catch (Exception e) {
            log.warn("Error creating sample chapters: {}", e.getMessage());
        }
    }

    /**
     * Create chapters from parsed Archive.org content
     */
    private void createChaptersFromParsedContent(Story story, List<ArchiveOrgService.ChapterContent> parsedChapters) {
        try {
            if (parsedChapters == null || parsedChapters.isEmpty()) {
                log.warn("No chapters parsed, creating sample chapters instead");
                createSampleChapters(story);
                return;
            }
            
            for (var parsedChapter : parsedChapters) {
                Chapter chapter = new Chapter();
                chapter.setStoryId(story.getId());
                chapter.setChapterNumber(parsedChapter.getChapterNumber());
                chapter.setTitle(parsedChapter.getTitle());
                chapter.setContent(parsedChapter.getContent());
                
                // Calculate word count (rough estimate: 1 word ~ 5 characters)
                int wordCount = Math.max(1, parsedChapter.getContent().length() / 5);
                chapter.setWordCount(wordCount);
                
                chapter.setCreatedAt(LocalDateTime.now());
                chapter.setUpdatedAt(LocalDateTime.now());
                
                chapterRepository.save(chapter);
            }
            
            log.info("✅ Created {} chapters from Archive.org content for story: {}", 
                     parsedChapters.size(), story.getTitle());
        } catch (Exception e) {
            log.error("Error creating chapters from parsed content: {}", e.getMessage());
            // Fallback to sample chapters if parsing fails
            createSampleChapters(story);
        }
    }

    /**
     * Create sample manga stories as fallback when API fails
     */
    private void createSampleMangaStories(String keyword, int limit) {
        try {
            String[] sampleMangaTitles = {
                "Action Hero Quest - " + keyword,
                "Battle Chronicles - " + keyword,
                "Combat Masters - " + keyword,
                "Warrior's Journey - " + keyword,
                "Epic Adventures - " + keyword
            };
            
            String[] authors = {
                "Mangaka Studio A",
                "Mangaka Studio B",
                "Mangaka Studio C",
                "Mangaka Studio D",
                "Mangaka Studio E"
            };
            
            for (int i = 0; i < Math.min(limit, sampleMangaTitles.length); i++) {
                String externalId = "mangadex_sample_" + System.currentTimeMillis() + "_" + i;
                
                if (storyRepository.existsByExternalId(externalId)) {
                    continue;
                }
                
                Story story = new Story();
                story.setTitle(sampleMangaTitles[i]);
                story.setAuthor(authors[i % authors.length]);
                story.setDescription("Sample manga with keyword: " + keyword + ". This is a placeholder story created because MangaDex API is currently unavailable.");
                story.setCoverUrl("https://placehold.co/300x400/FF6B6B/FFFFFF?text=" + keyword);
                story.setGenre("Tranh");
                story.setType("dịch");
                story.setStatus("ongoing");
                story.setSource("MangaDex");
                story.setExternalId(externalId);
                story.setIsPublic(true);
                story.setViewsTotal(0L);
                story.setLikes(0);
                story.setRating(0.0);
                story.setRatingCount(0);
                story.setCreatedAt(LocalDateTime.now());
                story.setUpdatedAt(LocalDateTime.now());
                
                storyRepository.save(story);
                createSampleChapters(story);
                
                log.info("✅ Created sample MangaDex story: {}", story.getTitle());
            }
        } catch (Exception e) {
            log.warn("Error creating sample manga stories: {}", e.getMessage());
        }
    }

    /**
     * Get import statistics
     */
    public ImportStats getImportStats() {
        try {
            long gutenbergCount = storyRepository.countBySource("Gutenberg");
            long mangaDexCount = storyRepository.countBySource("MangaDex");
            long openLibraryCount = storyRepository.countBySource("OpenLibrary");
            long archiveOrgCount = storyRepository.countBySource("Archive.org");
            
            return new ImportStats(gutenbergCount, mangaDexCount, openLibraryCount, archiveOrgCount);
        } catch (Exception e) {
            log.error("Error getting import stats: {}", e.getMessage());
            return new ImportStats(0, 0, 0, 0);
        }
    }

    /**
     * Delete all imported stories from a source
     */
    @Transactional
    public void clearImportedStories(String source) {
        try {
            int deleted = storyRepository.deleteBySource(source);
            log.info("✅ Deleted {} stories from {}", deleted, source);
        } catch (Exception e) {
            log.error("Error deleting stories from {}: {}", source, e.getMessage());
        }
    }

    /**
     * DTO for import statistics
     */
    public static class ImportStats {
        public final long gutenberg;
        public final long mangaDex;
        public final long openLibrary;
        public final long archiveOrg;
        public final long total;

        public ImportStats(long gutenberg, long mangaDex, long openLibrary) {
            this(gutenberg, mangaDex, openLibrary, 0);
        }

        public ImportStats(long gutenberg, long mangaDex, long openLibrary, long archiveOrg) {
            this.gutenberg = gutenberg;
            this.mangaDex = mangaDex;
            this.openLibrary = openLibrary;
            this.archiveOrg = archiveOrg;
            this.total = gutenberg + mangaDex + openLibrary + archiveOrg;
        }
    }
}
