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
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();
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
            // Simplified query - collection filter too restrictive
            String query = keyword.replace(" ", "+") + "+mediatype:texts";
            
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
     * Download and parse content from Archive.org
     * Returns list of chapters extracted from the text file
     */
    public List<ChapterContent> downloadAndParseContent(String identifier) {
        List<ChapterContent> chapters = new ArrayList<>();
        try {
            // First, get metadata to find the actual text file
            String metadataUrl = "https://archive.org/metadata/" + identifier;
            log.info("Fetching metadata from: {}", metadataUrl);
            
            HttpRequest metadataRequest = HttpRequest.newBuilder()
                    .uri(URI.create(metadataUrl))
                    .GET()
                    .header("User-Agent", "DocTruyen/1.0")
                    .timeout(java.time.Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> metadataResponse = httpClient.send(metadataRequest, HttpResponse.BodyHandlers.ofString());
            
            if (metadataResponse.statusCode() != 200) {
                log.warn("Failed to get metadata (Status: {}), using default chapters", metadataResponse.statusCode());
                return createDefaultChapters();
            }

            // Parse metadata to find text files
            JsonNode root = objectMapper.readTree(metadataResponse.body());
            String downloadUrl = null;
            
            if (root.has("files")) {
                // Look for text files: .txt, _djvu.txt, etc.
                for (JsonNode file : root.get("files")) {
                    if (file.has("name")) {
                        String name = file.get("name").asText();
                        // Prefer .txt files but can use other formats
                        if (name.endsWith(".txt") || name.endsWith("_djvu.txt")) {
                            downloadUrl = "https://archive.org/download/" + identifier + "/" + name;
                            log.info("Found text file: {}", name);
                            break;
                        }
                    }
                }
            }
            
            // If no text file found in metadata, try common patterns
            if (downloadUrl == null) {
                // Try common filename patterns
                String[] tryUrls = {
                    "https://archive.org/download/" + identifier + "/" + identifier + "_djvu.txt",
                    "https://archive.org/download/" + identifier + "/" + identifier + ".txt",
                    "https://archive.org/download/" + identifier + "/" + identifier + "_plaintext.txt"
                };
                
                for (String url : tryUrls) {
                    try {
                        HttpRequest headRequest = HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                                .header("User-Agent", "DocTruyen/1.0")
                                .timeout(java.time.Duration.ofSeconds(5))
                                .build();
                        
                        HttpResponse<Void> headResponse = httpClient.send(headRequest, HttpResponse.BodyHandlers.discarding());
                        if (headResponse.statusCode() == 200) {
                            downloadUrl = url;
                            log.info("Found file at: {}", url);
                            break;
                        }
                    } catch (Exception e) {
                        log.debug("File not found at {}", url);
                    }
                }
            }
            
            if (downloadUrl == null) {
                log.warn("No text file found for identifier: {}", identifier);
                return createDefaultChapters();
            }
            
            // Now download the actual content
            log.info("Downloading from: {}", downloadUrl);
            HttpRequest contentRequest = HttpRequest.newBuilder()
                    .uri(URI.create(downloadUrl))
                    .GET()
                    .header("User-Agent", "DocTruyen/1.0")
                    .timeout(java.time.Duration.ofSeconds(60))  // Longer timeout for actual download
                    .build();

            HttpResponse<String> contentResponse = httpClient.send(contentRequest, HttpResponse.BodyHandlers.ofString());
            
            if (contentResponse.statusCode() != 200) {
                log.warn("Failed to download content (Status: {}), using default chapters", contentResponse.statusCode());
                return createDefaultChapters();
            }

            String content = contentResponse.body();
            log.info("Downloaded {} bytes from Archive.org", content.length());
            
            // Parse chapters from content
            chapters = parseChapters(content, identifier);
            
            if (chapters.isEmpty()) {
                log.warn("No chapters found, creating default chapters");
                return createDefaultChapters();
            }
            
            log.info("Successfully parsed {} chapters from {}", chapters.size(), identifier);
            return chapters;
        } catch (Exception e) {
            log.error("Error downloading/parsing content from Archive.org: {}", e.getMessage(), e);
            return createDefaultChapters();
        }
    }

    /**
     * Parse text content into chapters
     * Looks for chapter markers or divides by length
     */
    private List<ChapterContent> parseChapters(String content, String identifier) {
        List<ChapterContent> chapters = new ArrayList<>();
        
        // Clean content
        content = content.replaceAll("\\r\\n", "\n").trim();
        
        // Try to find chapter markers (Chapter 1, Chapter 2, CHAPTER I, etc.)
        String[] chapterMarkers = {"Chapter ", "CHAPTER ", "Chapter\\n", "PART ", "Part ", "Section "};
        String[] lines = content.split("\n");
        
        StringBuilder currentChapter = new StringBuilder();
        String currentChapterTitle = "Chapter 1";
        int chapterNumber = 1;
        int lineCount = 0;
        int charsPerChapter = 5000; // ~5000 chars per chapter
        
        for (String line : lines) {
            line = line.trim();
            
            // Check if line is a chapter marker
            boolean isMarker = false;
            for (String marker : chapterMarkers) {
                if (line.toLowerCase().startsWith(marker.toLowerCase()) && line.length() < 100) {
                    isMarker = true;
                    
                    // Save previous chapter if it has content
                    if (currentChapter.length() > 0) {
                        ChapterContent chapter = new ChapterContent();
                        chapter.setChapterNumber(chapterNumber);
                        chapter.setTitle(currentChapterTitle);
                        chapter.setContent(currentChapter.toString().trim());
                        chapters.add(chapter);
                        
                        chapterNumber++;
                        currentChapter = new StringBuilder();
                    }
                    
                    currentChapterTitle = line;
                    break;
                }
            }
            
            // Skip empty lines and metadata
            if (line.isEmpty() || line.startsWith("***") || line.length() < 3) {
                continue;
            }
            
            // Add line to current chapter
            if (!isMarker && !line.matches(".*\\d{4}.*")) { // Skip lines with years (metadata)
                currentChapter.append(line).append("\n");
                lineCount++;
                
                // If chapter is getting too long, split it
                if (currentChapter.length() > charsPerChapter && !isMarker) {
                    ChapterContent chapter = new ChapterContent();
                    chapter.setChapterNumber(chapterNumber);
                    chapter.setTitle(currentChapterTitle + " (Part " + (chapterNumber) + ")");
                    chapter.setContent(currentChapter.toString().trim());
                    chapters.add(chapter);
                    
                    chapterNumber++;
                    currentChapter = new StringBuilder();
                }
            }
        }
        
        // Add last chapter if it has content
        if (currentChapter.length() > 0) {
            ChapterContent chapter = new ChapterContent();
            chapter.setChapterNumber(chapterNumber);
            chapter.setTitle(currentChapterTitle);
            chapter.setContent(currentChapter.toString().trim());
            chapters.add(chapter);
        }
        
        // If no chapters found by marker, divide content into sections
        if (chapters.isEmpty() && content.length() > 0) {
            return divideIntoSections(content);
        }
        
        return chapters;
    }

    /**
     * Divide content into equal sections if no chapter markers found
     */
    private List<ChapterContent> divideIntoSections(String content) {
        List<ChapterContent> chapters = new ArrayList<>();
        int charsPerChapter = 5000;
        int totalChapters = Math.max(1, (content.length() / charsPerChapter) + 1);
        
        for (int i = 0; i < totalChapters; i++) {
            int start = i * charsPerChapter;
            int end = Math.min(start + charsPerChapter, content.length());
            
            ChapterContent chapter = new ChapterContent();
            chapter.setChapterNumber(i + 1);
            chapter.setTitle("Chapter " + (i + 1));
            chapter.setContent(content.substring(start, end).trim());
            
            if (chapter.getContent().length() > 100) { // Only add if has meaningful content
                chapters.add(chapter);
            }
        }
        
        return chapters;
    }

    /**
     * Create default placeholder chapters
     */
    private List<ChapterContent> createDefaultChapters() {
        List<ChapterContent> chapters = new ArrayList<>();
        
        ChapterContent chapter = new ChapterContent();
        chapter.setChapterNumber(1);
        chapter.setTitle("Chapter 1");
        chapter.setContent("[Nội dung chương này là placeholder từ Archive.org]\n\n" +
                "Chương này hiển thị nội dung mặc định vì không thể tải được nội dung từ Archive.org. " +
                "Vui lòng thử lại sau hoặc kiểm tra kết nối Internet.\n\n" +
                "Để xem nội dung đầy đủ, vui lòng truy cập trực tiếp trang Archive.org của cuốn sách này.");
        chapters.add(chapter);
        
        return chapters;
    }

    /**
     * ChapterContent DTO for holding parsed chapter data
     */
    public static class ChapterContent {
        private int chapterNumber;
        private String title;
        private String content;

        public int getChapterNumber() { return chapterNumber; }
        public void setChapterNumber(int chapterNumber) { this.chapterNumber = chapterNumber; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
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
