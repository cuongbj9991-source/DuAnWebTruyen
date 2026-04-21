package com.doctruyen.controller;

import com.doctruyen.dto.ChapterDTO;
import com.doctruyen.service.ChapterService;
import com.doctruyen.service.TranslationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chapters")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "https://duanwebtruyen-production.up.railway.app"}, 
             allowedHeaders = "*", 
             methods = {org.springframework.web.bind.annotation.RequestMethod.GET, org.springframework.web.bind.annotation.RequestMethod.POST, org.springframework.web.bind.annotation.RequestMethod.PUT, org.springframework.web.bind.annotation.RequestMethod.DELETE},
             allowCredentials = "true")
public class ChapterController {
    private final ChapterService chapterService;
    private final TranslationService translationService;

    @GetMapping("/story/{storyId}")
    public ResponseEntity<List<ChapterDTO>> getChaptersByStoryId(@PathVariable Long storyId) {
        return ResponseEntity.ok(chapterService.getChaptersByStoryId(storyId));
    }

    @GetMapping("/story/{storyId}/paged")
    public ResponseEntity<Page<ChapterDTO>> getChaptersPagedByStoryId(
            @PathVariable Long storyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(chapterService.getChaptersPagedByStoryId(storyId, pageable));
    }

    @GetMapping("/story/{storyId}/chapter/{chapterNumber}")
    public ResponseEntity<ChapterDTO> getChapterByNumber(
            @PathVariable Long storyId,
            @PathVariable Integer chapterNumber) {
        
        return ResponseEntity.ok(chapterService.getChapterByStoryIdAndNumber(storyId, chapterNumber));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChapterDTO> getChapterById(@PathVariable Long id) {
        return ResponseEntity.ok(chapterService.getChapterById(id));
    }

    /**
     * Dịch nội dung chương sang Tiếng Việt
     * POST /api/chapters/{id}/translate
     */
    @PostMapping("/{id}/translate")
    public ResponseEntity<Map<String, Object>> translateChapter(@PathVariable Long id) {
        try {
            ChapterDTO chapter = chapterService.getChapterById(id);
            String translatedContent = translationService.translateChapterContent(chapter.getContent());
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", id);
            response.put("title", chapter.getTitle());
            response.put("originalContent", chapter.getContent());
            response.put("translatedContent", translatedContent);
            response.put("language", "vi");
            response.put("message", "✅ Dịch thành công sang Tiếng Việt");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Lỗi dịch chương: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Dịch đoạn văn bản ngắn
     * POST /api/chapters/translate-text
     */
    @PostMapping("/translate-text")
    public ResponseEntity<Map<String, String>> translateText(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            String translatedText = translationService.translateToVietnamese(text);
            
            Map<String, String> response = new HashMap<>();
            response.put("originalText", text);
            response.put("translatedText", translatedText);
            response.put("language", "vi");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi dịch: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
