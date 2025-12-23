package com.example.backend.domain.repository;

import com.example.backend.domain.entity.Follow;
import com.example.backend.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    
    @Query("SELECT f FROM Follow f WHERE f.follower.id = :followerId AND f.followed.id = :followedId")
    Optional<Follow> findByFollowerIdAndFollowedId(@Param("followerId") Long followerId, @Param("followedId") Long followedId);
    
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Follow f WHERE f.follower.id = :followerId AND f.followed.id = :followedId")
    boolean existsByFollowerIdAndFollowedId(@Param("followerId") Long followerId, @Param("followedId") Long followedId);
    
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.followed.id = :followedId")
    Long countByFollowedId(@Param("followedId") Long followedId);
    
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :followerId")
    Long countByFollowerId(@Param("followerId") Long followerId);
    
    @Query("SELECT f.followed.id FROM Follow f WHERE f.follower.id = :followerId AND f.isActive = true")
    List<Long> findFollowedIdsByFollowerId(@Param("followerId") Long followerId);
    
    @Query("SELECT f.follower FROM Follow f WHERE f.followed.id = :followedId AND f.isActive = true")
    List<User> findFollowersByFollowedId(@Param("followedId") Long followedId);
    
    @Query("SELECT f.followed FROM Follow f WHERE f.follower.id = :followerId AND f.isActive = true")
    List<User> findFollowedByFollowerId(@Param("followerId") Long followerId);
}

