package com.barter.controller;

import com.barter.dto.ApiResponse;
import com.barter.dto.TradeDto;
import com.barter.entity.User;
import com.barter.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @PostMapping
    public ApiResponse<TradeDto.TradeResponse> createTradeRequest(
            @Valid @RequestBody TradeDto.CreateRequest request,
            @AuthenticationPrincipal User user) {
        return ApiResponse.success("交换请求已发送", tradeService.createTradeRequest(request, user));
    }

    @GetMapping("/{id}")
    public ApiResponse<TradeDto.TradeResponse> getTradeRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ApiResponse.success(tradeService.getTradeRequest(id, user));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<TradeDto.TradeResponse> updateTradeStatus(
            @PathVariable Long id,
            @Valid @RequestBody TradeDto.StatusUpdateRequest request,
            @AuthenticationPrincipal User user) {
        return ApiResponse.success(tradeService.updateTradeStatus(id, request.getStatus(), user));
    }

    @GetMapping("/sent")
    public ApiResponse<Page<TradeDto.TradeResponse>> getSentRequests(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(tradeService.getSentRequests(user, pageable));
    }

    @GetMapping("/received")
    public ApiResponse<Page<TradeDto.TradeResponse>> getReceivedRequests(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(tradeService.getReceivedRequests(user, pageable));
    }
}
