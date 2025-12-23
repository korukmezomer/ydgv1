package com.example.backend.application.dto.response;

import com.example.backend.domain.entity.Comment.CommentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    
    private Long id;
    private String content;
    private CommentStatus status;
    private Long likeCount;
    private Long userId;
    private String username;
    private String storyTitle;
    private String storySlug;
    private Long storyId;
    private Long parentCommentId;
    private List<CommentResponse> replies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
