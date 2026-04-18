package com.doctruyen.repository;

import com.doctruyen.entity.StoryUpload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StoryUploadRepository extends JpaRepository<StoryUpload, Long> {
    Page<StoryUpload> findByUserId(Long userId, Pageable pageable);

    Page<StoryUpload> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    @Query("SELECT s FROM StoryUpload s WHERE s.isApproved = true AND s.isPublic = true")
    Page<StoryUpload> findPublishedStories(Pageable pageable);

    @Query("SELECT s FROM StoryUpload s WHERE s.status = 'pending_review'")
    Page<StoryUpload> findPendingReviews(Pageable pageable);

    @Query("SELECT s FROM StoryUpload s WHERE " +
           "LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<StoryUpload> searchPublishedStories(@Param("keyword") String keyword, Pageable pageable);
}
