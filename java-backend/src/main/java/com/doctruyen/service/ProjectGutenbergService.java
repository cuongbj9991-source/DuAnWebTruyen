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

@Slf4j
@Service
public class ProjectGutenbergService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class BookMetadata {
        public String id;
        public String title;
        public String author;
        public String description;
        public String coverUrl;

        public BookMetadata(String id, String title, String author, String description, String coverUrl) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.description = description;
            this.coverUrl = coverUrl;
        }
    }

    public List<BookMetadata> searchBooks(String keyword, int limit) {
        try {
            String query = keyword.replace(" ", "%20");
            String searchUrl = "https://gutendex.com/books?search=" + query;
            
            log.info("Searching Project Gutenberg: {}", searchUrl);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(searchUrl))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log.warn("Project Gutenberg search failed: {}", response.statusCode());
                return new ArrayList<>();
            }
            
            JsonNode root = objectMapper.readTree(response.body());
            List<BookMetadata> books = new ArrayList<>();
            JsonNode results = root.get("results");
            
            if (results != null && results.isArray()) {
                int count = 0;
                for (JsonNode book : results) {
                    if (count >= limit) break;
                    
                    String bookId = book.get("id").asText();
                    String title = book.get("title").asText();
                    JsonNode authorsNode = book.get("authors");
                    String author = "";
                    
                    if (authorsNode != null && authorsNode.isArray() && authorsNode.size() > 0) {
                        author = authorsNode.get(0).get("name").asText();
                    }
                    
                    String coverUrl = book.get("cover_image").asText("");
                    String description = title + " - Project Gutenberg";
                    
                    books.add(new BookMetadata(bookId, title, author, description, coverUrl));
                    count++;
                }
            }
            
            log.info("Found {} books from Project Gutenberg", books.size());
            return books;
            
        } catch (Exception e) {
            log.error("Error searching Project Gutenberg", e);
            return new ArrayList<>();
        }
    }

    public String downloadBookContent(String bookId) {
        try {
            // Try text version first
            String textUrl = "https://www.gutenberg.org/cache/epub/" + bookId + "/pg" + bookId + ".txt";
            
            log.info("Downloading from Project Gutenberg: {}", textUrl);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(textUrl))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                log.info("Successfully downloaded content from Project Gutenberg (bookId: {})", bookId);
                return cleanGutenbergText(response.body());
            } else {
                log.warn("Failed to download from Project Gutenberg: Status {}", response.statusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Error downloading book content from Project Gutenberg", e);
            return null;
        }
    }

    private String cleanGutenbergText(String text) {
        // Remove Project Gutenberg header and footer
        int startIndex = text.indexOf("*** START");
        int endIndex = text.indexOf("*** END");
        
        if (startIndex != -1 && endIndex != -1) {
            text = text.substring(startIndex + 50, endIndex);
        }
        
        return text.trim();
    }
}
