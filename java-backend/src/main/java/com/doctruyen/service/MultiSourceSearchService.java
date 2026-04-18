package com.doctruyen.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MultiSourceSearchService {
    private final GutenbergService gutenbergService;
    private final OpenLibraryService openLibraryService;
    private final MangaDexService mangaDexService;

    /**
     * Search across all sources (Gutenberg, OpenLibrary, MangaDex, UserUploads)
     */
    public MultiSourceSearchResult searchAllSources(String keyword, int page) {
        MultiSourceSearchResult result = new MultiSourceSearchResult();
        result.setKeyword(keyword);
        result.setPage(page);

        try {
            // Search Gutenberg
            List<Map<String, Object>> gutenbergResults = new ArrayList<>();
            List<GutenbergService.GutenbergBook> gutBooks = gutenbergService.searchBooks(keyword, page);
            for (GutenbergService.GutenbergBook book : gutBooks) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", "gut_" + book.getId());
                item.put("title", book.getTitle());
                item.put("author", book.getAuthor());
                item.put("description", book.getDescription());
                item.put("genre", book.getGenre());
                item.put("coverUrl", book.getCoverUrl());
                item.put("source", "Gutenberg");
                gutenbergResults.add(item);
            }
            result.setGutenberg(gutenbergResults);
        } catch (Exception e) {
            log.error("Error searching Gutenberg: {}", e.getMessage());
            result.setGutenberg(new ArrayList<>());
        }

        try {
            // Search OpenLibrary
            List<Map<String, Object>> openLibraryResults = new ArrayList<>();
            List<OpenLibraryService.OpenLibraryBook> libBooks = openLibraryService.searchBooks(keyword, page);
            for (OpenLibraryService.OpenLibraryBook book : libBooks) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", "lib_" + book.getId());
                item.put("title", book.getTitle());
                item.put("author", book.getAuthor());
                item.put("description", book.getDescription());
                item.put("genre", book.getGenre());
                item.put("coverUrl", book.getCoverUrl());
                item.put("source", "OpenLibrary");
                openLibraryResults.add(item);
            }
            result.setOpenLibrary(openLibraryResults);
        } catch (Exception e) {
            log.error("Error searching OpenLibrary: {}", e.getMessage());
            result.setOpenLibrary(new ArrayList<>());
        }

        try {
            // Search MangaDex
            List<Map<String, Object>> mangaDexResults = new ArrayList<>();
            List<MangaDexService.MangaDexManga> mangas = mangaDexService.searchManga(keyword, page);
            for (MangaDexService.MangaDexManga manga : mangas) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", "manga_" + manga.getId());
                item.put("title", manga.getTitle());
                item.put("author", manga.getAuthor());
                item.put("description", manga.getDescription());
                item.put("genre", manga.getGenre());
                item.put("coverUrl", manga.getCoverUrl());
                item.put("source", "MangaDex");
                mangaDexResults.add(item);
            }
            result.setMangaDex(mangaDexResults);
        } catch (Exception e) {
            log.error("Error searching MangaDex: {}", e.getMessage());
            result.setMangaDex(new ArrayList<>());
        }

        // Note: UserUploads are handled separately through StoryUploadController
        
        log.info("Multi-source search completed for keyword: {}. Results: Gutenberg={}, OpenLibrary={}, MangaDex={}",
                keyword, 
                result.gutenberg.size(),
                result.openLibrary.size(),
                result.mangaDex.size()
        );

        return result;
    }

    /**
     * Get recommended stories from multiple sources
     */
    public MultiSourceSearchResult getRecommendedBooks() {
        MultiSourceSearchResult result = new MultiSourceSearchResult();
        result.setKeyword("recommended");

        try {
            List<Map<String, Object>> gutenbergResults = new ArrayList<>();
            // Popular titles from Gutenberg
            String[] popularTitles = {"Pride and Prejudice", "Sherlock Holmes", "Alice", "Jane Eyre"};
            for (String title : popularTitles) {
                List<GutenbergService.GutenbergBook> books = gutenbergService.searchBooks(title, 1);
                if (!books.isEmpty()) {
                    GutenbergService.GutenbergBook book = books.get(0);
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", "gut_" + book.getId());
                    item.put("title", book.getTitle());
                    item.put("author", book.getAuthor());
                    item.put("description", book.getDescription());
                    item.put("genre", book.getGenre());
                    item.put("coverUrl", book.getCoverUrl());
                    item.put("source", "Gutenberg");
                    gutenbergResults.add(item);
                }
            }
            result.setGutenberg(gutenbergResults);
        } catch (Exception e) {
            log.error("Error getting recommended Gutenberg books: {}", e.getMessage());
            result.setGutenberg(new ArrayList<>());
        }

        return result;
    }

    public static class MultiSourceSearchResult {
        private String keyword;
        private int page;
        private List<Map<String, Object>> gutenberg = new ArrayList<>();
        private List<Map<String, Object>> openLibrary = new ArrayList<>();
        private List<Map<String, Object>> mangaDex = new ArrayList<>();

        // Getters and Setters
        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }

        public List<Map<String, Object>> getGutenberg() { return gutenberg; }
        public void setGutenberg(List<Map<String, Object>> gutenberg) { this.gutenberg = gutenberg; }

        public List<Map<String, Object>> getOpenLibrary() { return openLibrary; }
        public void setOpenLibrary(List<Map<String, Object>> openLibrary) { this.openLibrary = openLibrary; }

        public List<Map<String, Object>> getMangaDex() { return mangaDex; }
        public void setMangaDex(List<Map<String, Object>> mangaDex) { this.mangaDex = mangaDex; }

        public int getTotalResults() {
            return gutenberg.size() + openLibrary.size() + mangaDex.size();
        }
    }
}
