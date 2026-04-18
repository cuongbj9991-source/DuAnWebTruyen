package com.doctruyen.repository;

import com.doctruyen.entity.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    @Query("SELECT s FROM Story s WHERE " +
           "LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.titleAlt) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Story> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT s FROM Story s WHERE " +
           "(:genre IS NULL OR s.genre = :genre) AND " +
           "(:type IS NULL OR s.type = :type) AND " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:source IS NULL OR s.source = :source) AND " +
           "(:minRating IS NULL OR s.rating >= :minRating)")
    Page<Story> filterStories(
            @Param("genre") String genre,
            @Param("type") String type,
            @Param("status") String status,
            @Param("source") String source,
            @Param("minRating") Double minRating,
            Pageable pageable);

    Page<Story> findByGenre(String genre, Pageable pageable);
    Page<Story> findByType(String type, Pageable pageable);
    Page<Story> findByStatus(String status, Pageable pageable);
    
    // Import-related queries
    boolean existsByExternalId(String externalId);
    long countBySource(String source);
    int deleteBySource(String source);
}
