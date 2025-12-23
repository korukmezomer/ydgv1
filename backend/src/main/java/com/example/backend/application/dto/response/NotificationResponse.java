package com.example.backend.application.dto.response;

import com.example.backend.domain.entity.Notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    
    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private Boolean isRead;
    private Long relatedStoryId;
    private String relatedStorySlug;
    private Long relatedCommentId;
    private LocalDateTime createdAt;
}
