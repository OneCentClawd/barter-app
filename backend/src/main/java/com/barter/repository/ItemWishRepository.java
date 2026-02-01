package com.barter.repository;

import com.barter.entity.Item;
import com.barter.entity.ItemWish;
import com.barter.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ItemWishRepository extends JpaRepository<ItemWish, Long> {
    Optional<ItemWish> findByUserAndItem(User user, Item item);
    
    boolean existsByUserAndItem(User user, Item item);
    
    void deleteByUserAndItem(User user, Item item);
    
    Page<ItemWish> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    @Query("SELECT COUNT(w) FROM ItemWish w WHERE w.item = :item")
    Integer countByItem(Item item);
}
