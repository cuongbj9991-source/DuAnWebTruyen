package com.doctruyen.service;

import com.doctruyen.dto.StoryDTO;
import com.doctruyen.entity.Story;
import com.doctruyen.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
public class StoryService {
    private final StoryRepository storyRepository;

    @SuppressWarnings("unchecked")
    public Page<StoryDTO> getAllStories(Pageable pageable) {
        try {
            var result = storyRepository.findAll(pageable);
            return result.map(story -> {
                try {
                    return convertToDTO(story);
                } catch (Exception e) {
                    log.error("Error converting story {}: {}", story.getId(), e.getMessage());
                    return null;
                }
            });
        } catch (Exception e) {
            log.error("Error fetching stories: {}", e.getMessage(), e);
            return Page.empty(pageable);
        }
    }

    @SuppressWarnings("unchecked")
    public StoryDTO getStoryById(Long id) {
        return storyRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Story not found"));
    }

    public Page<StoryDTO> searchStories(String keyword, Pageable pageable) {
        return storyRepository.searchByKeyword(keyword, pageable)
                .map(this::convertToDTO);
    }

    public Page<StoryDTO> filterStories(String genre, String type, String status, 
                                       String source, Double minRating, Pageable pageable) {
        return storyRepository.filterStories(genre, type, status, source, minRating, pageable)
                .map(this::convertToDTO);
    }

    @Transactional
    public void incrementViewCount(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));
        story.setViewsTotal(story.getViewsTotal() + 1);
        story.setUpdatedAt(LocalDateTime.now());
        storyRepository.save(story);
        log.info("Story {} view count incremented", storyId);
    }

    public List<String> getAllGenres() {
        return storyRepository.findAll()
                .stream()
                .map(Story::getGenre)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<String> getAllTypes() {
        return storyRepository.findAll()
                .stream()
                .map(Story::getType)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<String> getAllStatuses() {
        return storyRepository.findAll()
                .stream()
                .map(Story::getStatus)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<String> getAllSources() {
        return storyRepository.findAll()
                .stream()
                .map(Story::getSource)
                .distinct()
                .collect(Collectors.toList());
    }

    private StoryDTO convertToDTO(Story story) {
        return new StoryDTO(
                story.getId(),
                story.getTitle(),
                story.getTitleAlt(),
                story.getDescription(),
                story.getAuthor(),
                story.getGenre(),
                story.getType(),
                story.getStatus(),
                story.getSource(),
                story.getCoverUrl(),
                story.getViewsTotal(),
                story.getLikes(),
                story.getCommentsCount(),
                story.getRating(),
                story.getRatingCount(),
                story.getLastChapterUpdated(),
                story.getCreatedAt(),
                story.getUpdatedAt()
        );
    }
}
