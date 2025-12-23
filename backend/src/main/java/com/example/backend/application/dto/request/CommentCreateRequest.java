package com.example.backend.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {
    
    @NotBlank(message = "Yorum içeriği zorunludur")
    private String content;
    
    private Long parentCommentId;
}
