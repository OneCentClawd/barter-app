package com.barter.dto;

import com.barter.entity.Item;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

public class ItemDto {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "标题不能为空")
        private String title;

        private String description;
        private String category;
        private Item.ItemCondition condition = Item.ItemCondition.GOOD;
        private String wantedItems;
    }

    @Data
    public static class UpdateRequest {
        private String title;
        private String description;
        private String category;
        private Item.ItemCondition condition;
        private String wantedItems;
        private Item.ItemStatus status;
    }

    @Data
    public static class ItemResponse {
        private Long id;
        private String title;
        private String description;
        private String category;
        private Item.ItemCondition condition;
        private Item.ItemStatus status;
        private String wantedItems;
        private UserBrief owner;
        private List<String> images;
        private Integer viewCount;
        private Integer wishCount;
        private Boolean isWished;
        private LocalDateTime createdAt;
        
        // 交易信息（已交换的物品才有）
        private TradeInfo tradeInfo;
    }
    
    @Data
    public static class TradeInfo {
        private Long tradeRequestId;
        private UserBrief previousOwner;      // 原主人
        private ItemBrief tradedForItem;      // 用什么换来的
        private LocalDateTime tradedAt;
    }
    
    @Data
    public static class ItemBrief {
        private Long id;
        private String title;
        private String coverImage;
    }
    
    @Data
    public static class WishResponse {
        private Long itemId;
        private Boolean isWished;
        private Integer wishCount;
    }

    @Data
    public static class ItemListResponse {
        private Long id;
        private String title;
        private String category;
        private Item.ItemCondition condition;
        private Item.ItemStatus status;
        private String coverImage;
        private UserBrief owner;
        private LocalDateTime createdAt;
    }

    @Data
    public static class UserBrief {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        private Double rating;
    }
}
