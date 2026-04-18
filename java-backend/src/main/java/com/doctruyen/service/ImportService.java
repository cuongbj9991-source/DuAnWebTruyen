package com.doctruyen.service;

import com.doctruyen.dto.StoryDTO;
import com.doctruyen.entity.Story;
import com.doctruyen.repository.StoryRepository;
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
