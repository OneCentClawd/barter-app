package com.barter.controller;

import com.barter.dto.ApiResponse;
import com.barter.dto.ReviewDto;
import com.barter.entity.User;
import com.barter.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ApiResponse<ReviewDto.ReviewResponse> createReview(
            @Valid @RequestBody ReviewDto.CreateRequest request,
            @AuthenticationPrincipal User user) {
        return ApiResponse.success("评价成功", reviewService.createReview(request, user));
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<Page<ReviewDto.ReviewResponse>> getUserReviews(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(reviewService.getUserReviews(userId, pageable));
    }
}
