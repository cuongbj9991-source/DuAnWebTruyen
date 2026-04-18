package com.doctruyen.service;

import com.doctruyen.dto.StoryUploadDTO;
import com.doctruyen.dto.CreateUploadDTO;
import com.doctruyen.dto.UpdateUploadDTO;
import com.doctruyen.entity.StoryUpload;
import com.doctruyen.repository.StoryUploadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StoryUploadService {
    private final StoryUploadRepository uploadRepository;

    @Transactional
    public StoryUploadDTO createUpload(Long userId, CreateUploadDTO dto) {
        StoryUpload upload = new StoryUpload();
        upload.setUserId(userId);
        upload.setTitle(dto.getTitle());
        upload.setAuthor(dto.getAuthor());
        upload.setDescription(dto.getDescription());
        upload.setGenre(dto.getGenre());
        upload.setType(dto.getType());
        upload.setCoverUrl(dto.getCoverUrl());
        upload.setContent(dto.getContent());
        upload.setStatus("pending_review");
        upload.setIsPublic(false);
        upload.setIsApproved(false);

        StoryUpload saved = uploadRepository.save(upload);
        log.info("Story upload created by user {}: {}", userId, saved.getId());
        return convertToDTO(saved);
    }

    public Page<StoryUploadDTO> getUserUploads(Long userId, Pageable pageable) {
        return uploadRepository.findByUserId(userId, pageable)
                .map(this::convertToDTO);
    }

    public Page<StoryUploadDTO> getUserUploadsByStatus(Long userId, String status, Pageable pageable) {
        return uploadRepository.findByUserIdAndStatus(userId, status, pageable)
                .map(this::convertToDTO);
    }

    public Page<StoryUploadDTO> getPublishedStories(Pageable pageable) {
        return uploadRepository.findPublishedStories(pageable)
                .map(this::convertToDTO);
    }

    public Page<StoryUploadDTO> searchPublishedStories(String keyword, Pageable pageable) {
        return uploadRepository.searchPublishedStories(keyword, pageable)
                .map(this::convertToDTO);
    }

    public Page<StoryUploadDTO> getPendingReviews(Pageable pageable) {
        return uploadRepository.findPendingReviews(pageable)
                .map(this::convertToDTO);
    }

    public StoryUploadDTO getUploadById(Long id) {
        return uploadRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Story upload not found"));
    }

    @Transactional
    public StoryUploadDTO updateUpload(Long id, Long userId, UpdateUploadDTO dto) {
        StoryUpload upload = uploadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story upload not found"));

        // Only owner or admin can update
        if (!upload.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to update this upload");
        }

        upload.setTitle(dto.getTitle());
        upload.setDescription(dto.getDescription());
        upload.setGenre(dto.getGenre());
        upload.setContent(dto.getContent());
        if (dto.getCoverUrl() != null) {
            upload.setCoverUrl(dto.getCoverUrl());
        }
        upload.setUpdatedAt(LocalDateTime.now());

        StoryUpload updated = uploadRepository.save(upload);
        log.info("Story upload {} updated", id);
        return convertToDTO(updated);
    }

    @Transactional
    public void deleteUpload(Long id, Long userId) {
        StoryUpload upload = uploadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story upload not found"));

        if (!upload.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this upload");
        }

        uploadRepository.deleteById(id);
        log.info("Story upload {} deleted", id);
    }

    @Transactional
    public StoryUploadDTO approveUpload(Long id) {
        StoryUpload upload = uploadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story upload not found"));

        upload.setIsApproved(true);
        upload.setStatus("published");
        upload.setApprovedAt(LocalDateTime.now());

        StoryUpload updated = uploadRepository.save(upload);
        log.info("Story upload {} approved", id);
        return convertToDTO(updated);
    }

    @Transactional
    public StoryUploadDTO rejectUpload(Long id, String reason) {
        StoryUpload upload = uploadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story upload not found"));

        upload.setIsApproved(false);
        upload.setStatus("rejected");
        upload.setRejectionReason(reason);

        StoryUpload updated = uploadRepository.save(upload);
        log.info("Story upload {} rejected: {}", id, reason);
        return convertToDTO(updated);
    }

    @Transactional
    public void publishUpload(Long id, Long userId) {
        StoryUpload upload = uploadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story upload not found"));

        if (!upload.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to publish this upload");
        }

        if (!upload.getIsApproved()) {
            throw new RuntimeException("Story must be approved before publishing");
        }

        upload.setIsPublic(true);
        uploadRepository.save(upload);
        log.info("Story upload {} published", id);
    }

    private StoryUploadDTO convertToDTO(StoryUpload upload) {
        return new StoryUploadDTO(
                upload.getId(),
                upload.getUserId(),
                upload.getTitle(),
                upload.getAuthor(),
                upload.getDescription(),
                upload.getGenre(),
                upload.getType(),
                upload.getStatus(),
                upload.getCoverUrl(),
                upload.getIsPublic(),
                upload.getIsApproved(),
                upload.getRejectionReason(),
                upload.getViewsCount(),
                upload.getCreatedAt(),
                upload.getUpdatedAt(),
                upload.getApprovedAt()
        );
    }
}
