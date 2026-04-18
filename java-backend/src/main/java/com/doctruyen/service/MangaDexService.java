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
}
