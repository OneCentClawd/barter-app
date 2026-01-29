package com.barter.service;

import com.barter.dto.ReviewDto;
import com.barter.entity.Review;
import com.barter.entity.TradeRequest;
import com.barter.entity.User;
import com.barter.repository.ReviewRepository;
import com.barter.repository.TradeRequestRepository;
import com.barter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final TradeRequestRepository tradeRequestRepository;
    private final UserRepository userRepository;
    private final ItemService itemService;

    @Transactional
    public ReviewDto.ReviewResponse createReview(ReviewDto.CreateRequest request, User reviewer) {
        TradeRequest tradeRequest = tradeRequestRepository.findById(request.getTradeRequestId())
                .orElseThrow(() -> new RuntimeException("交易不存在"));

        if (tradeRequest.getStatus() != TradeRequest.TradeStatus.COMPLETED) {
            throw new RuntimeException("只能评价已完成的交易");
        }

        // 确定被评价人
        User reviewedUser;
        if (tradeRequest.getRequester().getId().equals(reviewer.getId())) {
            reviewedUser = tradeRequest.getTargetItem().getOwner();
        } else if (tradeRequest.getTargetItem().getOwner().getId().equals(reviewer.getId())) {
            reviewedUser = tradeRequest.getRequester();
        } else {
            throw new RuntimeException("您不是此交易的参与方");
        }

        Review review = new Review();
        review.setReviewer(reviewer);
        review.setReviewedUser(reviewedUser);
        review.setTradeRequest(tradeRequest);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        review = reviewRepository.save(review);

        // 更新被评价用户的评分
        updateUserRating(reviewedUser);

        return toReviewResponse(review);
    }

    public Page<ReviewDto.ReviewResponse> getUserReviews(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return reviewRepository.findByReviewedUser(user, pageable)
                .map(this::toReviewResponse);
    }

    private void updateUserRating(User user) {
        Page<Review> reviews = reviewRepository.findByReviewedUser(user, Pageable.unpaged());

        if (!reviews.isEmpty()) {
            double avgRating = reviews.getContent().stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(5.0);

            user.setRating(Math.round(avgRating * 10) / 10.0);
            user.setRatingCount((int) reviews.getTotalElements());
            userRepository.save(user);
        }
    }

    private ReviewDto.ReviewResponse toReviewResponse(Review review) {
        ReviewDto.ReviewResponse response = new ReviewDto.ReviewResponse();
        response.setId(review.getId());
        response.setReviewer(itemService.toUserBrief(review.getReviewer()));
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}
