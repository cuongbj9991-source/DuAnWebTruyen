package com.doctruyen.repository;

import com.doctruyen.entity.Chapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByStoryIdOrderByChapterNumberAsc(Long storyId);
    
    Page<Chapter> findByStoryId(Long storyId, Pageable pageable);
    
    Chapter findByStoryIdAndChapterNumber(Long storyId, Integer chapterNumber);
}
