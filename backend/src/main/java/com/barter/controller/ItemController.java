package com.barter.controller;

import com.barter.dto.ApiResponse;
import com.barter.dto.ItemDto;
import com.barter.entity.User;
import com.barter.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ApiResponse<ItemDto.ItemResponse> createItem(
            @Valid @RequestPart("item") ItemDto.CreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            throw new RuntimeException("请先登录");
        }
        return ApiResponse.success("物品发布成功", itemService.createItem(request, user, images));
    }

    @PutMapping("/{id}")
    public ApiResponse<ItemDto.ItemResponse> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemDto.UpdateRequest request,
            @AuthenticationPrincipal User user) {
        return ApiResponse.success("物品更新成功", itemService.updateItem(id, request, user));
    }

    @GetMapping("/{id}")
    public ApiResponse<ItemDto.ItemResponse> getItem(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ApiResponse.success(itemService.getItem(id, user));
    }

    @GetMapping("/list")
    public ApiResponse<Page<ItemDto.ItemListResponse>> listItems(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal User user) {
        return ApiResponse.success(itemService.listItems(pageable, user));
    }

    @GetMapping("/search")
    public ApiResponse<Page<ItemDto.ItemListResponse>> searchItems(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal User user) {
        return ApiResponse.success(itemService.searchItems(keyword, pageable, user));
    }

    @GetMapping("/my")
    public ApiResponse<Page<ItemDto.ItemListResponse>> getMyItems(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(itemService.getMyItems(user, pageable));
    }

    @GetMapping("/my/available")
    public ApiResponse<List<ItemDto.ItemListResponse>> getMyAvailableItems(
            @AuthenticationPrincipal User user) {
        return ApiResponse.success(itemService.getMyAvailableItems(user));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteItem(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        itemService.deleteItem(id, user);
        return ApiResponse.success("物品已删除", null);
    }
    
    @PostMapping("/{id}/wish")
    public ApiResponse<ItemDto.WishResponse> toggleWish(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ApiResponse.success(itemService.toggleWish(id, user));
    }
    
    @GetMapping("/wishes")
    public ApiResponse<Page<ItemDto.ItemListResponse>> getMyWishes(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(itemService.getMyWishes(user, pageable));
    }
}
