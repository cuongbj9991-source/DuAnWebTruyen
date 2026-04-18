package com.doctruyen.service;

import com.doctruyen.dto.StoryDTO;
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
                    if (mangas.isEmpty()) break;
                    
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
                        
                        // Use sample chapters as default (MangaDex API is complex)
                        createSampleChapters(story);
                        
                        imported++;
                        log.info("✅ Imported MangaDex story: {} ({})", story.getTitle(), imported);
                    }
                    
                    page++;
                } catch (Exception e) {
                    log.error("Error importing from MangaDex page {}: {}", page, e.getMessage());
                    break;
                }
            }
            
            log.info("✅ MangaDex import completed: {} stories imported", imported);
        } catch (Exception e) {
            log.error("❌ MangaDex import failed: {}", e.getMessage());
        }
    }

    /**
     * Fetch and import chapters from MangaDex for a story
     */
    private void fetchMangaDexChapters(Story story, String mangaId) {
        try {
            var chapters = mangaDexService.getChaptersForManga(mangaId, 10);
            
            int chapterNumber = 1;
            for (var ch : chapters) {
                try {
                    Chapter chapter = new Chapter();
                    chapter.setStoryId(story.getId());
                    chapter.setChapterNumber(chapterNumber);
                    chapter.setTitle(ch.getTitle() != null && !ch.getTitle().isEmpty() ? 
                                    ch.getTitle() : ("Chương " + ch.getChapterNumber()));
                    
                    // Fetch actual chapter content
                    String content = mangaDexService.getChapterContent(ch.getId());
                    chapter.setContent(content);
                    chapter.setWordCount(content.length() / 5); // Rough estimate
                    chapter.setCreatedAt(LocalDateTime.now());
                    chapter.setUpdatedAt(LocalDateTime.now());
                    
                    chapterRepository.save(chapter);
                    chapterNumber++;
                } catch (Exception e) {
                    log.warn("Error fetching chapter {}: {}", ch.getChapterNumber(), e.getMessage());
                }
            }
            
            if (chapterNumber > 1) {
                log.info("✅ Imported {} chapters for story: {}", (chapterNumber - 1), story.getTitle());
            } else {
                // Fallback to sample chapters if no real chapters found
                createSampleChapters(story);
            }
        } catch (Exception e) {
            log.warn("Error fetching MangaDex chapters: {}", e.getMessage());
            // Fallback to sample chapters
            createSampleChapters(story);
        }
    }

    /**
     * Create sample chapters as fallback
     */
    private void createSampleChapters(Story story) {
        try {
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
                chapter.setContent(sampleContent[i - 1] + "\n\n[Nội dung chương này là placeholder từ " + story.getSource() + "]");
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
     * Get import statistics
     */
    public ImportStats getImportStats() {
        long gutenbergCount = storyRepository.countBySource("Gutenberg");
        long mangaDexCount = storyRepository.countBySource("MangaDex");
        long openLibraryCount = storyRepository.countBySource("OpenLibrary");
        
        return new ImportStats(gutenbergCount, mangaDexCount, openLibraryCount);
    }

    /**
     * Delete all imported stories from a source
     */
    @Transactional
    public void clearImportedStories(String source) {
        int deleted = storyRepository.deleteBySource(source);
        log.info("✅ Deleted {} stories from {}", deleted, source);
    }

    /**
     * DTO for import statistics
     */
    public static class ImportStats {
        public final long gutenberg;
        public final long mangaDex;
        public final long openLibrary;
        public final long total;

        public ImportStats(long gutenberg, long mangaDex, long openLibrary) {
            this.gutenberg = gutenberg;
            this.mangaDex = mangaDex;
            this.openLibrary = openLibrary;
            this.total = gutenberg + mangaDex + openLibrary;
        }
    }
}
