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

@Service
@Slf4j
public class ArchiveOrgService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String ARCHIVE_API = "https://archive.org/advancedsearch.php";

    /**
     * Search for public domain books
     * Only returns books with mediatype:texts and public domain status
     */
    public List<ArchiveBook> searchBooks(String keyword, int page) {
        try {
            int pageNumber = Math.max(1, page);
            int rows = 20;
            int start = (pageNumber - 1) * rows;
            
            // Query for public domain texts only
            // mediatype:texts = Books/texts
            // collection:openlibrary = Open Library collection
            // -collection:covers = Exclude just covers
            String query = "(" + keyword.replace(" ", "+OR+") + ")+AND+mediatype:texts+AND+collection:openlibrary";
            
            String url = ARCHIVE_API + "?q=" + query
                    + "&fl=identifier,title,creator,description,date,item_size,downloads"
                    + "&output=json"
                    + "&rows=" + rows
                    + "&start=" + start;
            
            log.info("Searching Archive.org: {}", url);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("User-Agent", "DocTruyen/1.0")
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log.error("Archive.org API error: {}", response.statusCode());
                return new ArrayList<>();
            }

            JsonNode root = objectMapper.readTree(response.body());
            List<ArchiveBook> books = new ArrayList<>();

            if (root.has("response") && root.get("response").has("docs")) {
                root.get("response").get("docs").forEach(doc -> {
                    try {
                        ArchiveBook book = new ArchiveBook();
                        
                        if (doc.has("identifier")) {
                            book.setId(doc.get("identifier").asText());
                        }
                        if (doc.has("title")) {
                            book.setTitle(doc.get("title").asText());
                        }
                        if (doc.has("creator")) {
                            if (doc.get("creator").isArray()) {
                                book.setAuthor(doc.get("creator").get(0).asText());
                            } else {
                                book.setAuthor(doc.get("creator").asText());
                            }
                        }
                        if (doc.has("description")) {
                            String desc = doc.get("description").asText();
                            book.setDescription(desc.length() > 300 ? desc.substring(0, 300) : desc);
                        }
                        if (doc.has("date")) {
                            book.setYear(doc.get("date").asText());
                        }
                        if (doc.has("downloads")) {
                            book.setDownloads(doc.get("downloads").asLong());
                        }
                        
                        // Generate cover URL
                        book.setCoverUrl("https://archive.org/services/img/" + book.getId());
                        
                        // Set genre/type
                        book.setGenre("Literature");
                        book.setSource("Archive.org");
                        
                        // Only add if has title and ID
                        if (book.getTitle() != null && !book.getTitle().isEmpty() 
                            && book.getId() != null && !book.getId().isEmpty()) {
                            books.add(book);
                        }
                    } catch (Exception e) {
                        log.debug("Error parsing archive.org book: {}", e.getMessage());
                    }
                });
            }

            log.info("Found {} books from Archive.org for keyword: {}", books.size(), keyword);
            return books;
        } catch (Exception e) {
            log.error("Error searching Archive.org API: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Search for classic literature (with lower download threshold)
     */
    public List<ArchiveBook> searchClassicLiterature(String keyword, int page) {
        try {
            int pageNumber = Math.max(1, page);
            int rows = 20;
            int start = (pageNumber - 1) * rows;
            
            // Focus on classic literature
            String query = "(" + keyword.replace(" ", "+OR+") + ")+AND+mediatype:texts+AND+subject:literature";
            
            String url = ARCHIVE_API + "?q=" + query
                    + "&fl=identifier,title,creator,description,date,item_size,downloads"
                    + "&output=json"
                    + "&rows=" + rows
                    + "&start=" + start
                    + "&sort=downloads+desc";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("User-Agent", "DocTruyen/1.0")
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                return new ArrayList<>();
            }

            JsonNode root = objectMapper.readTree(response.body());
            List<ArchiveBook> books = new ArrayList<>();

            if (root.has("response") && root.get("response").has("docs")) {
                root.get("response").get("docs").forEach(doc -> {
                    try {
                        ArchiveBook book = new ArchiveBook();
                        
                        if (doc.has("identifier")) {
                            book.setId(doc.get("identifier").asText());
                        }
                        if (doc.has("title")) {
                            book.setTitle(doc.get("title").asText());
                        }
                        if (doc.has("creator")) {
                            if (doc.get("creator").isArray()) {
                                book.setAuthor(doc.get("creator").get(0).asText());
                            } else {
                                book.setAuthor(doc.get("creator").asText());
                            }
                        }
                        if (doc.has("description")) {
                            String desc = doc.get("description").asText();
                            book.setDescription(desc.length() > 300 ? desc.substring(0, 300) : desc);
                        }
                        if (doc.has("date")) {
                            book.setYear(doc.get("date").asText());
                        }
                        
                        book.setCoverUrl("https://archive.org/services/img/" + book.getId());
                        book.setGenre("Literature");
                        book.setSource("Archive.org");
                        
                        if (book.getTitle() != null && !book.getTitle().isEmpty() 
                            && book.getId() != null && !book.getId().isEmpty()) {
                            books.add(book);
                        }
                    } catch (Exception e) {
                        log.debug("Error parsing classic: {}", e.getMessage());
                    }
                });
            }

            return books;
        } catch (Exception e) {
            log.error("Error searching Archive.org classics: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get book metadata and download link
     */
    public ArchiveBook getBookDetails(String identifier) {
        try {
            String url = "https://archive.org/metadata/" + identifier;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("User-Agent", "DocTruyen/1.0")
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                return null;
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode metadata = root.get("metadata");
            
            if (metadata == null) {
                return null;
            }

            ArchiveBook book = new ArchiveBook();
            book.setId(identifier);
            
            if (metadata.has("title")) {
                book.setTitle(metadata.get("title").asText());
            }
            if (metadata.has("creator")) {
                if (metadata.get("creator").isArray()) {
                    book.setAuthor(metadata.get("creator").get(0).asText());
                } else {
                    book.setAuthor(metadata.get("creator").asText());
                }
            }
            if (metadata.has("description")) {
                book.setDescription(metadata.get("description").asText());
            }
            if (metadata.has("date")) {
                book.setYear(metadata.get("date").asText());
            }
            
            book.setCoverUrl("https://archive.org/services/img/" + identifier);
            book.setGenre("Literature");
            book.setSource("Archive.org");
            
            // Find text file (e.g., .txt)
            if (root.has("files")) {
                root.get("files").forEach(file -> {
                    if (file.has("name")) {
                        String name = file.get("name").asText();
                        if (name.endsWith("_djvu.txt") || name.endsWith(".txt")) {
                            book.setDownloadUrl("https://archive.org/download/" + identifier + "/" + name);
                        }
                    }
                });
            }
            
            return book;
        } catch (Exception e) {
            log.error("Error fetching book details from Archive.org: {}", e.getMessage());
            return null;
        }
    }

    /**
     * ArchiveBook DTO
     */
    public static class ArchiveBook {
        private String id;
        private String title;
        private String author;
        private String description;
        private String year;
        private String coverUrl;
        private String downloadUrl;
        private String genre;
        private String source;
        private long downloads;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getYear() { return year; }
        public void setYear(String year) { this.year = year; }

        public String getCoverUrl() { return coverUrl; }
        public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

        public String getDownloadUrl() { return downloadUrl; }
        public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        public long getDownloads() { return downloads; }
        public void setDownloads(long downloads) { this.downloads = downloads; }
    }
}
