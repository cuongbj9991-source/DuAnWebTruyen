package com.doctruyen.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class GutenbergService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String GUTENBERG_API = "https://gutendex.com/books";

    public List<GutenbergBook> searchBooks(String keyword, int page) {
        try {
            String url = GUTENBERG_API + "?search=" + keyword + "&page=" + page;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log.error("Gutenberg API error: {}", response.statusCode());
                return new ArrayList<>();
            }

            JsonNode root = objectMapper.readTree(response.body());
            List<GutenbergBook> books = new ArrayList<>();

            if (root.has("results")) {
                root.get("results").forEach(book -> {
                    try {
                        GutenbergBook gutBook = new GutenbergBook();
                        gutBook.setId(book.get("id").asLong());
                        gutBook.setTitle(book.get("title").asText());
                        
                        if (book.has("authors") && book.get("authors").isArray() && book.get("authors").size() > 0) {
                            gutBook.setAuthor(book.get("authors").get(0).get("name").asText());
                        }
                        
                        if (book.has("cover_image")) {
                            gutBook.setCoverUrl(book.get("cover_image").asText());
                        }
                        
                        gutBook.setDescription("Classic literature from Project Gutenberg");
                        gutBook.setGenre("Classic Literature");
                        gutBook.setSource("Gutenberg");
                        
                        books.add(gutBook);
                    } catch (Exception e) {
                        log.warn("Error parsing book: {}", e.getMessage());
                    }
                });
            }

            log.info("Found {} books from Gutenberg for keyword: {}", books.size(), keyword);
            return books;
        } catch (Exception e) {
            log.error("Error searching Gutenberg API: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public Optional<GutenbergBook> getBookById(Long id) {
        try {
            String url = GUTENBERG_API + "/" + id;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                return Optional.empty();
            }

            JsonNode book = objectMapper.readTree(response.body());
            GutenbergBook gutBook = new GutenbergBook();
            gutBook.setId(book.get("id").asLong());
            gutBook.setTitle(book.get("title").asText());
            
            if (book.has("authors") && book.get("authors").isArray() && book.get("authors").size() > 0) {
                gutBook.setAuthor(book.get("authors").get(0).get("name").asText());
            }
            
            if (book.has("cover_image")) {
                gutBook.setCoverUrl(book.get("cover_image").asText());
            }
            
            gutBook.setDescription("Classic literature from Project Gutenberg");
            gutBook.setGenre("Classic Literature");
            gutBook.setSource("Gutenberg");
            
            return Optional.of(gutBook);
        } catch (Exception e) {
            log.error("Error fetching book from Gutenberg: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GutenbergBook {
        private Long id;
        private String title;
        private String author;
        private String description;
        private String genre;
        private String coverUrl;
        private String source;

        public GutenbergBook() {
            this.source = "Gutenberg";
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }

        public String getCoverUrl() { return coverUrl; }
        public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
    }
}
