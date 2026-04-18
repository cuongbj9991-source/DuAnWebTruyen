package com.doctruyen.controller;

import com.doctruyen.dto.ChapterDTO;
import com.doctruyen.service.ChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chapters")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ChapterController {
    private final ChapterService chapterService;

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
}
