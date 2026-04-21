package com.doctruyen.service;

import com.doctruyen.dto.CommentDTO;
import com.doctruyen.entity.Comment;
import com.doctruyen.repository.CommentRepository;
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
public class CommentService {
    private final CommentRepository commentRepository;

    public Page<CommentDTO> getCommentsByStoryId(Long storyId, Pageable pageable) {
        return commentRepository.findByStoryId(storyId, pageable)
                .map(this::convertToDTO);
    }

    public Page<CommentDTO> getCommentsByUserId(Long userId, Pageable pageable) {
        return commentRepository.findByUserId(userId, pageable)
                .map(this::convertToDTO);
    }

    @SuppressWarnings("unchecked")
    public CommentDTO getCommentById(Long id) {
        return commentRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
    }

    @Transactional
    public CommentDTO createComment(CommentDTO commentDTO) {
        Comment comment = new Comment();
        comment.setUserId(commentDTO.getUserId());
        comment.setStoryId(commentDTO.getStoryId());
        comment.setContent(commentDTO.getContent());
        comment.setRating(commentDTO.getRating());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);
        log.info("Comment {} created", saved.getId());
        return convertToDTO(saved);
    }

    @Transactional
    public CommentDTO updateComment(Long id, CommentDTO commentDTO) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        comment.setContent(commentDTO.getContent());
        comment.setRating(commentDTO.getRating());
        comment.setUpdatedAt(LocalDateTime.now());

        Comment updated = commentRepository.save(comment);
        log.info("Comment {} updated", updated.getId());
        return convertToDTO(updated);
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new RuntimeException("Comment not found");
        }
        commentRepository.deleteById(id);
        log.info("Comment {} deleted", id);
    }

    private CommentDTO convertToDTO(Comment comment) {
        return new CommentDTO(
                comment.getId(),
                comment.getUserId(),
                comment.getStoryId(),
                comment.getContent(),
                comment.getRating(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
