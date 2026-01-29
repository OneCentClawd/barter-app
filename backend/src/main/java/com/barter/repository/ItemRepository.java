package com.barter.repository;

import com.barter.entity.Item;
import com.barter.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findByStatus(Item.ItemStatus status, Pageable pageable);

    Page<Item> findByOwner(User owner, Pageable pageable);

    Page<Item> findByCategory(String category, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.status = :status AND " +
           "(LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Item> searchByKeyword(@Param("keyword") String keyword,
                               @Param("status") Item.ItemStatus status,
                               Pageable pageable);

    List<Item> findByOwnerAndStatus(User owner, Item.ItemStatus status);
}
