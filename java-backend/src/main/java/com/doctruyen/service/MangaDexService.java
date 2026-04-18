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
public class MangaDexService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String MANGADEX_API = "https://api.mangadex.org";

    public List<MangaDexManga> searchManga(String keyword, int page) {
        try {
            int limit = 20;
            int offset = (page - 1) * limit;
            String url = MANGADEX_API + "/manga?title=" + keyword.replace(" ", "%20")
                    + "&limit=" + limit + "&offset=" + offset
                    + "&includes[]=author&includes[]=artist&includes[]=cover_art";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("User-Agent", "DocTruyen/1.0")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log.error("MangaDex API error: {}", response.statusCode());
                return new ArrayList<>();
            }

            JsonNode root = objectMapper.readTree(response.body());
            List<MangaDexManga> mangas = new ArrayList<>();

            if (root.has("data")) {
                root.get("data").forEach(manga -> {
                    try {
                        MangaDexManga m = new MangaDexManga();
                        
                        if (manga.has("id")) {
                            m.setId(manga.get("id").asText());
                        }
                        
                        if (manga.has("attributes")) {
                            JsonNode attr = manga.get("attributes");
                            if (attr.has("title")) {
                                String title = attr.get("title").get("en").asText();
                                m.setTitle(title);
                            }
                            if (attr.has("description")) {
                                String desc = attr.get("description").get("en").asText();
                                m.setDescription(desc.length() > 200 ? desc.substring(0, 200) : desc);
                            }
                            if (attr.has("status")) {
                                m.setStatus(attr.get("status").asText());
                            }
                        }
                        
                        // Get author from relationships
                        if (manga.has("relationships")) {
                            manga.get("relationships").forEach(rel -> {
                                if (rel.has("type") && "author".equals(rel.get("type").asText())) {
                                    if (rel.has("attributes") && rel.get("attributes").has("name")) {
                                        m.setAuthor(rel.get("attributes").get("name").asText());
                                    }
                                }
                            });
                        }
                        
                        // Generate cover URL
                        if (manga.has("relationships")) {
                            manga.get("relationships").forEach(rel -> {
                                if (rel.has("type") && "cover_art".equals(rel.get("type").asText())) {
                                    if (rel.has("attributes") && rel.get("attributes").has("fileName")) {
                                        String fileName = rel.get("attributes").get("fileName").asText();
                                        m.setCoverUrl("https://uploads.mangadex.org/covers/" + m.getId() + "/" + fileName);
                                    }
                                }
                            });
                        }
                        
                        m.setGenre("Manga");
                        m.setSource("MangaDex");
                        
                        mangas.add(m);
                    } catch (Exception e) {
                        log.warn("Error parsing manga: {}", e.getMessage());
                    }
                });
            }

            log.info("Found {} mangas from MangaDex for keyword: {}", mangas.size(), keyword);
            return mangas;
        } catch (Exception e) {
            log.error("Error searching MangaDex API: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public Optional<MangaDexManga> getMangaById(String id) {
        try {
            String url = MANGADEX_API + "/manga/" + id + "?includes[]=author&includes[]=artist&includes[]=cover_art";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("User-Agent", "DocTruyen/1.0")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode manga = root.get("data");
            
            MangaDexManga m = new MangaDexManga();
            m.setId(id);
            
            if (manga.has("attributes")) {
                JsonNode attr = manga.get("attributes");
                if (attr.has("title")) {
                    m.setTitle(attr.get("title").get("en").asText());
                }
                if (attr.has("description")) {
                    m.setDescription(attr.get("description").get("en").asText());
                }
                if (attr.has("status")) {
                    m.setStatus(attr.get("status").asText());
                }
            }
            
            m.setGenre("Manga");
            m.setSource("MangaDex");
            
            return Optional.of(m);
        } catch (Exception e) {
            log.error("Error fetching manga from MangaDex: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public List<MangaDexChapter> getChaptersForManga(String mangaId, int limit) {
        try {
            String url = MANGADEX_API + "/manga/" + mangaId + "/feed"
                    + "?limit=" + Math.min(limit, 50)
                    + "&order[chapter]=asc"
                    + "&contentRating[]=safe&contentRating[]=suggestive&contentRating[]=erotica&contentRating[]=pornographic";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("User-Agent", "DocTruyen/1.0")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log.warn("MangaDex chapters API returned: {}", response.statusCode());
                return new ArrayList<>();
            }

            JsonNode root = objectMapper.readTree(response.body());
            List<MangaDexChapter> chapters = new ArrayList<>();

            if (root.has("data")) {
                root.get("data").forEach(ch -> {
                    try {
                        if (ch.has("attributes") && ch.has("id")) {
                            JsonNode attr = ch.get("attributes");
                            String chapterNum = attr.has("chapter") ? attr.get("chapter").asText() : "0";
                            String title = attr.has("title") ? attr.get("title").asText() : "Chapter " + chapterNum;
                            
                            MangaDexChapter chapter = new MangaDexChapter();
                            chapter.setId(ch.get("id").asText());
                            chapter.setChapterNumber(chapterNum);
                            chapter.setTitle(title);
                            chapters.add(chapter);
                        }
                    } catch (Exception e) {
                        log.debug("Error parsing chapter: {}", e.getMessage());
                    }
                });
            }

            log.info("Fetched {} chapters for manga: {}", chapters.size(), mangaId);
            return chapters;
        } catch (Exception e) {
            log.error("Error fetching chapters from MangaDex: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public String getChapterContent(String chapterId) {
        try {
            String url = MANGADEX_API + "/at/home/api/manga/" + chapterId + "/aggregate?translatedLanguage[]=en";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("User-Agent", "DocTruyen/1.0")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return "Nội dung từ MangaDex Chapter " + chapterId + " - " + response.body().substring(0, Math.min(500, response.body().length()));
            }
            
            return "Không thể tải nội dung từ MangaDex";
        } catch (Exception e) {
            log.warn("Error fetching chapter content: {}", e.getMessage());
            return "Lỗi tải nội dung: " + e.getMessage();
        }
    }

    /**
     * Fetch chapter page images from MangaDex @ Home API
     * Returns JSON array of image URLs
     */
    public String getChapterPages(String chapterId) {
        try {
            // Don't try to fetch real images - use fallback for now
            // MangaDex @ Home API is complex and has rate limits
            log.debug("Chapter image fetching: using fallback for chapter {}", chapterId);
            return "[]";
            
            /* Disabled for MVP - too complex
            String url = "https://api.mangadex.org/at-home/server/" + chapterId;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("User-Agent", "DocTruyen/1.0")
                    .timeout(java.time.Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log.warn("MangaDex @ Home API returned: {}", response.statusCode());
                return "[]";
            }

            JsonNode root = objectMapper.readTree(response.body());
            if (!root.has("baseUrl") || !root.has("chapter")) {
                log.warn("Invalid MangaDex @ Home response structure");
                return "[]";
            }

            JsonNode chapterNode = root.get("chapter");
            JsonNode pages = chapterNode.has("dataSaver") 
                ? chapterNode.get("dataSaver") 
                : chapterNode.get("data");

            if (pages == null || pages.size() == 0) {
                return "[]";
            }

            String baseUrl = root.get("baseUrl").asText();
            String hash = chapterNode.get("hash").asText();
            
            java.util.List<String> imageUrls = new java.util.ArrayList<>();
            for (int i = 0; i < pages.size(); i++) {
                String filename = pages.get(i).asText();
                String imageUrl = baseUrl + "/data-saver/" + hash + "/" + filename;
                imageUrls.add(imageUrl);
            }

            String pagesJson = objectMapper.writeValueAsString(imageUrls);
            log.info("✅ Fetched {} pages for chapter: {}", imageUrls.size(), chapterId);
            return pagesJson;
            */

        } catch (Exception e) {
            log.debug("Error fetching chapter pages: {}", e.getMessage());
            return "[]";
        }
    }

    public static class MangaDexManga {
        private String id;
        private String title;
        private String author;
        private String description;
        private String genre;
        private String coverUrl;
        private String source;
        private String status;

        public MangaDexManga() {
            this.source = "MangaDex";
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

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class MangaDexChapter {
        private String id;
        private String chapterNumber;
        private String title;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getChapterNumber() { return chapterNumber; }
        public void setChapterNumber(String chapterNumber) { this.chapterNumber = chapterNumber; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }
}
