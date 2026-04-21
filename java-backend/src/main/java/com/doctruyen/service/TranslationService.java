package com.doctruyen.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class TranslationService {
    
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Dịch văn bản sang Tiếng Việt bằng Google Translate API (free)
     */
    public String translateToVietnamese(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        try {
            // Sử dụng MyMemory API - free translation service
            String sourceText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = "https://api.mymemory.translated.net/get?q=" + sourceText + "&langpair=en|vi";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String translatedText = root.path("responseData").path("translatedText").asText();
                
                if (!translatedText.isEmpty()) {
                    log.info("✅ Dịch thành công {} ký tự", text.length());
                    return translatedText;
                }
            }
            
            log.warn("⚠️ Dịch thất bại: {}", response.statusCode());
            return text;
            
        } catch (Exception e) {
            log.error("❌ Lỗi dịch: {}", e.getMessage());
            return text; // Return original if translation fails
        }
    }

    /**
     * Dịch nội dung chương (từng đoạn)
     */
    public String translateChapterContent(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        try {
            // Chia thành các đoạn để dịch
            String[] paragraphs = content.split("\n\n");
            StringBuilder translatedContent = new StringBuilder();
            
            for (int i = 0; i < paragraphs.length; i++) {
                String translatedParagraph = translateToVietnamese(paragraphs[i]);
                translatedContent.append(translatedParagraph);
                
                if (i < paragraphs.length - 1) {
                    translatedContent.append("\n\n");
                }
                
                // Rate limiting - sleep 500ms between requests
                if (i < paragraphs.length - 1) {
                    Thread.sleep(500);
                }
            }
            
            log.info("✅ Dịch chương hoàn tất");
            return translatedContent.toString();
            
        } catch (InterruptedException e) {
            log.error("Dịch bị gián đoạn: {}", e.getMessage());
            Thread.currentThread().interrupt();
            return content;
        } catch (Exception e) {
            log.error("Lỗi dịch chương: {}", e.getMessage());
            return content;
        }
    }
}
