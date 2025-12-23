package com.example.backend.application.service.impl;

import com.example.backend.application.service.LikeService;
import com.example.backend.application.exception.BadRequestException;
import com.example.backend.application.exception.ResourceNotFoundException;
import com.example.backend.application.service.NotificationService;
import com.example.backend.domain.entity.Like;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.entity.Story;
import com.example.backend.domain.entity.Notification;
import com.example.backend.domain.repository.LikeRepository;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.domain.repository.StoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LikeServiceImpl implements LikeService {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void like(Long storyId, Long userId) {
        if (likeRepository.existsByUserIdAndStoryId(userId, storyId)) {
            throw new BadRequestException("Bu haberi zaten beğendiniz");
        }

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new ResourceNotFoundException("Story bulunamadı"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        Like like = new Like();
        like.setStory(story);
        like.setUser(user);
        likeRepository.save(like);

        // Haber beğeni sayısını artır
        story.setLikeCount(story.getLikeCount() + 1);
        storyRepository.save(story);

        // Story sahibine bildirim gönder (kendi yazısını beğenmediyse)
        Long storyOwnerId = story.getUser().getId();
        if (!storyOwnerId.equals(userId)) {
            String likerName = user.getUsername() != null 
                ? user.getUsername() 
                : (user.getFirstName() != null ? user.getFirstName() : "Bir kullanıcı");
            
            notificationService.createNotification(
                storyOwnerId,
                "Yazınız Beğenildi",
                likerName + " yazınızı beğendi: " + 
                (story.getTitle().length() > 50 
                    ? story.getTitle().substring(0, 50) + "..." 
                    : story.getTitle()),
                Notification.NotificationType.HABER_BEGENILDI,
                story.getId(),
                null
            );
        }
    }

    @Override
    public void unlike(Long storyId, Long userId) {
        Like like = likeRepository.findByUserIdAndStoryId(userId, storyId)
                .orElseThrow(() -> new ResourceNotFoundException("Beğeni bulunamadı"));

        Story story = like.getStory();
        likeRepository.delete(like);

        // Haber beğeni sayısını azalt
        story.setLikeCount(Math.max(0, story.getLikeCount() - 1));
        storyRepository.save(story);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLiked(Long storyId, Long userId) {
        return likeRepository.existsByUserIdAndStoryId(userId, storyId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getLikeCount(Long storyId) {
        return likeRepository.countActiveByStoryId(storyId);
    }
}

