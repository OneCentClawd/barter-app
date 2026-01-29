package com.barter.repository;

import com.barter.entity.Review;
import com.barter.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByReviewedUser(User reviewedUser, Pageable pageable);
}
