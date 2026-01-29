package com.barter.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;

    @Enumerated(EnumType.STRING)
    private ItemCondition condition = ItemCondition.GOOD;

    @Enumerated(EnumType.STRING)
    private ItemStatus status = ItemStatus.AVAILABLE;

    // 想要交换什么
    @Column(columnDefinition = "TEXT")
    private String wantedItems;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<ItemImage> images;

    private Integer viewCount = 0;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum ItemCondition {
        NEW,        // 全新
        LIKE_NEW,   // 几乎全新
        GOOD,       // 良好
        FAIR,       // 一般
        POOR        // 较差
    }

    public enum ItemStatus {
        AVAILABLE,  // 可交换
        PENDING,    // 交换中
        TRADED,     // 已交换
        REMOVED     // 已下架
    }
}
