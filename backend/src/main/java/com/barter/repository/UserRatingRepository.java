package com.barter.repository;

import com.barter.entity.User;
import com.barter.entity.UserRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface UserRatingRepository extends JpaRepository<UserRating, Long> {
    
    Optional<UserRating> findByRaterAndRatedUser(User rater, User ratedUser);
    
    Page<UserRating> findByRatedUserOrderByCreatedAtDesc(User ratedUser, Pageable pageable);
    
    @Query("SELECT AVG(r.rating) FROM UserRating r WHERE r.ratedUser = :user")
    Double getAverageRating(User user);
    
    @Query("SELECT COUNT(r) FROM UserRating r WHERE r.ratedUser = :user")
    Integer getRatingCount(User user);
}
