package com.doctruyen.service;

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
public class OpenLibraryService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String OPENLIBRARY_API = "https://openlibrary.org";

    public List<OpenLibraryBook> searchBooks(String keyword, int page) {
        try {
            int limit = 20;
            int offset = (page - 1) * limit;
            String url = OPENLIBRARY_API + "/search.json?title=" + keyword.replace(" ", "+") 
                    + "&limit=" + limit + "&offset=" + offset;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("User-Agent", "DocTruyen/1.0")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log.error("Open Library API error: {}", response.statusCode());
                return new ArrayList<>();
            }

            JsonNode root = objectMapper.readTree(response.body());
            List<OpenLibraryBook> books = new ArrayList<>();

            if (root.has("docs")) {
                root.get("docs").forEach(doc -> {
                    try {
                        OpenLibraryBook book = new OpenLibraryBook();
                        
                        if (doc.has("key")) {
                            book.setId(doc.get("key").asText().replace("/works/", ""));
                        }
                        if (doc.has("title")) {
                            book.setTitle(doc.get("title").asText());
                        }
                        if (doc.has("author_name") && doc.get("author_name").isArray() && doc.get("author_name").size() > 0) {
                            book.setAuthor(doc.get("author_name").get(0).asText());
                        }
                        if (doc.has("first_publish_year")) {
                            book.setYear(doc.get("first_publish_year").asInt());
                        }
                        if (doc.has("isbn") && doc.get("isbn").isArray() && doc.get("isbn").size() > 0) {
                            book.setIsbn(doc.get("isbn").get(0).asText());
                        }
                        
                        // Generate cover URL
                        if (book.getIsbn() != null) {
                            book.setCoverUrl("https://covers.openlibrary.org/b/isbn/" + book.getIsbn() + "-M.jpg");
                        }
                        
                        book.setDescription("Book from Open Library");
                        book.setGenre("General");
                        book.setSource("OpenLibrary");
                        
                        books.add(book);
                    } catch (Exception e) {
                        log.warn("Error parsing book: {}", e.getMessage());
                    }
                });
            }

            log.info("Found {} books from Open Library for keyword: {}", books.size(), keyword);
            return books;
        } catch (Exception e) {
            log.error("Error searching Open Library API: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public Optional<OpenLibraryBook> getBookByKey(String key) {
        try {
            String url = OPENLIBRARY_API + "/works/" + key + ".json";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("User-Agent", "DocTruyen/1.0")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                return Optional.empty();
            }

            JsonNode doc = objectMapper.readTree(response.body());
            OpenLibraryBook book = new OpenLibraryBook();
            
            book.setId(key);
            if (doc.has("title")) {
                book.setTitle(doc.get("title").asText());
            }
            if (doc.has("authors") && doc.get("authors").isArray() && doc.get("authors").size() > 0) {
                book.setAuthor(doc.get("authors").get(0).get("name").asText());
            }
            if (doc.has("first_publish_date")) {
                book.setDescription(doc.get("description").asText());
            }
            if (doc.has("covers") && doc.get("covers").isArray() && doc.get("covers").size() > 0) {
                Long coverId = doc.get("covers").get(0).asLong();
                book.setCoverUrl("https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg");
            }
            
            book.setGenre("General");
            book.setSource("OpenLibrary");
            
            return Optional.of(book);
        } catch (Exception e) {
            log.error("Error fetching book from Open Library: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public static class OpenLibraryBook {
        private String id;
        private String title;
        private String author;
        private String description;
        private String genre;
        private String coverUrl;
        private String source;
        private Integer year;
        private String isbn;

        public OpenLibraryBook() {
            this.source = "OpenLibrary";
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

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

        public Integer getYear() { return year; }
        public void setYear(Integer year) { this.year = year; }

        public String getIsbn() { return isbn; }
        public void setIsbn(String isbn) { this.isbn = isbn; }
    }
}
