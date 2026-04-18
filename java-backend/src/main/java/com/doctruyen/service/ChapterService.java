package com.doctruyen.service;

import com.doctruyen.dto.ChapterDTO;
import com.doctruyen.entity.Chapter;
import com.doctruyen.repository.ChapterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChapterService {
    private final ChapterRepository chapterRepository;

    public List<ChapterDTO> getChaptersByStoryId(Long storyId) {
        return chapterRepository.findByStoryIdOrderByChapterNumberAsc(storyId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<ChapterDTO> getChaptersPagedByStoryId(Long storyId, Pageable pageable) {
        return chapterRepository.findByStoryId(storyId, pageable)
                .map(this::convertToDTO);
    }

    public ChapterDTO getChapterByStoryIdAndNumber(Long storyId, Integer chapterNumber) {
        Chapter chapter = chapterRepository.findByStoryIdAndChapterNumber(storyId, chapterNumber);
        if (chapter == null) {
            throw new RuntimeException("Chapter not found");
        }
        return convertToDTO(chapter);
    }

    public ChapterDTO getChapterById(Long id) {
        return chapterRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));
    }

    private ChapterDTO convertToDTO(Chapter chapter) {
        return new ChapterDTO(
                chapter.getId(),
                chapter.getStoryId(),
                chapter.getChapterNumber(),
                chapter.getTitle(),
                chapter.getContent(),
                chapter.getPages(),
                chapter.getWordCount(),
                chapter.getCreatedAt(),
                chapter.getUpdatedAt()
        );
    }
}
